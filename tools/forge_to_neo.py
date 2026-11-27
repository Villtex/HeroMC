#!/usr/bin/env python3
"""Mechanical first pass for porting Forge 1.20.1 Java sources to NeoForge 26.3 (Teil 27: Mantle, Tinkers 3.12.1).

Usage: python3 tools/forge_to_neo.py <dir-or-file> [...]

Rewrites the files in place: package moves (net.minecraftforge -> net.neoforged ...), class renames of Minecraft
(ResourceLocation -> Identifier ...) and simple call patterns. Everything this cannot do safely is left for the
compiler to point out. Run it once on freshly copied original sources, not on code that was already fixed by hand
(it is idempotent for the renames, but the call patterns may then hit hand-written code).
"""
import re
import sys
from pathlib import Path

# (regex, replacement) applied in order to the whole file text
RULES = [
    # --- Minecraft renames (1.20.1 -> 26.3) ---
    (r'\bnet\.minecraft\.resources\.ResourceLocation\b', 'net.minecraft.resources.Identifier'),
    (r'\bnew ResourceLocation\(([^(),]+(?:\([^()]*\))?),\s*', r'Identifier.fromNamespaceAndPath(\1, '),
    (r'\bnew ResourceLocation\(', 'Identifier.parse('),
    (r'\bResourceLocation\b', 'Identifier'),
    (r'\breadResourceLocation\(\)', 'readIdentifier()'),
    (r'\bwriteResourceLocation\(', 'writeIdentifier('),
    (r'\bnet\.minecraft\.Util\b', 'net.minecraft.util.Util'),
    (r'\bnet\.minecraft\.world\.entity\.player\.Player\b', 'net.minecraft.world.entity.player.Player'),
    # --- event bus / fml ---
    (r'\bnet\.minecraftforge\.eventbus\.api\.', 'net.neoforged.bus.api.'),
    (r'\bnet\.minecraftforge\.fml\.common\.Mod\.EventBusSubscriber\b', 'net.neoforged.fml.common.EventBusSubscriber'),
    (r'\bnet\.minecraftforge\.fml\.', 'net.neoforged.fml.'),
    (r'\bnet\.minecraftforge\.api\.distmarker\.', 'net.neoforged.api.distmarker.'),
    (r'\bnet\.minecraftforge\.common\.MinecraftForge\b', 'net.neoforged.neoforge.common.NeoForge'),
    (r'\bMinecraftForge\.EVENT_BUS\b', 'NeoForge.EVENT_BUS'),
    # --- registries ---
    (r'\bnet\.minecraftforge\.registries\.RegistryObject\b', 'net.neoforged.neoforge.registries.DeferredHolder'),
    (r'\bnet\.minecraftforge\.registries\.ForgeRegistries\b', 'net.minecraft.core.registries.BuiltInRegistries'),
    (r'\bnet\.minecraftforge\.registries\.IForgeRegistry\b', 'net.minecraft.core.Registry'),
    (r'\bIForgeRegistry<', 'Registry<'),
    (r'\bForgeRegistries\.ITEMS\b', 'BuiltInRegistries.ITEM'),
    (r'\bForgeRegistries\.BLOCKS\b', 'BuiltInRegistries.BLOCK'),
    (r'\bForgeRegistries\.FLUIDS\b', 'BuiltInRegistries.FLUID'),
    (r'\bForgeRegistries\.ENTITY_TYPES\b', 'BuiltInRegistries.ENTITY_TYPE'),
    (r'\bForgeRegistries\.BLOCK_ENTITY_TYPES\b', 'BuiltInRegistries.BLOCK_ENTITY_TYPE'),
    (r'\bForgeRegistries\.MENU_TYPES\b', 'BuiltInRegistries.MENU'),
    (r'\bForgeRegistries\.RECIPE_SERIALIZERS\b', 'BuiltInRegistries.RECIPE_SERIALIZER'),
    (r'\bForgeRegistries\.RECIPE_TYPES\b', 'BuiltInRegistries.RECIPE_TYPE'),
    (r'\bForgeRegistries\.MOB_EFFECTS\b', 'BuiltInRegistries.MOB_EFFECT'),
    (r'\bForgeRegistries\.POTIONS\b', 'BuiltInRegistries.POTION'),
    (r'\bForgeRegistries\.ENCHANTMENTS\b', 'BuiltInRegistries.ENCHANTMENT'),
    (r'\bForgeRegistries\.SOUND_EVENTS\b', 'BuiltInRegistries.SOUND_EVENT'),
    (r'\bForgeRegistries\.ATTRIBUTES\b', 'BuiltInRegistries.ATTRIBUTE'),
    (r'\bForgeRegistries\.PARTICLE_TYPES\b', 'BuiltInRegistries.PARTICLE_TYPE'),
    (r'\bForgeRegistries\.VILLAGER_PROFESSIONS\b', 'BuiltInRegistries.VILLAGER_PROFESSION'),
    (r'\bForgeRegistries\.STAT_TYPES\b', 'BuiltInRegistries.STAT_TYPE'),
    (r'\bForgeRegistries\.FEATURES\b', 'BuiltInRegistries.FEATURE'),
    (r'\bForgeRegistries\.Keys\.FLUID_TYPES\b', 'NeoForgeRegistries.Keys.FLUID_TYPES'),
    (r'\bForgeRegistries\.FLUID_TYPES\.get\(\)', 'NeoForgeRegistries.FLUID_TYPES'),
    (r'\bForgeRegistries\.Keys\.GLOBAL_LOOT_MODIFIER_SERIALIZERS\b', 'NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS'),
    (r'\bForgeRegistries\.Keys\.BIOME_MODIFIER_SERIALIZERS\b', 'NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS'),
    (r'\bForgeRegistries\.Keys\.', 'Registries.'),
    (r'\bnet\.minecraftforge\.registries\.', 'net.neoforged.neoforge.registries.'),
    # --- fluids ---
    (r'\bnet\.minecraftforge\.fluids\.ForgeFlowingFluid\b', 'net.neoforged.neoforge.fluids.BaseFlowingFluid'),
    (r'\bForgeFlowingFluid\b', 'BaseFlowingFluid'),
    # Forge fluid handler API -> Mantle's compat layer on the 26.3 transfer API (slimeknights.mantle.fluid.compat)
    (r'\bnet\.(?:minecraftforge|neoforged\.neoforge)\.fluids\.capability\.templates\.(FluidTank|EmptyFluidHandler)\b', r'slimeknights.mantle.fluid.compat.\1'),
    (r'\bnet\.(?:minecraftforge|neoforged\.neoforge)\.fluids\.capability\.(IFluidHandler|IFluidHandlerItem)\b', r'slimeknights.mantle.fluid.compat.\1'),
    (r'\bnet\.minecraftforge\.fluids\.', 'net.neoforged.neoforge.fluids.'),
    (r'\bnet\.minecraftforge\.client\.extensions\.common\.', 'net.neoforged.neoforge.client.extensions.common.'),
    # --- common / events / conditions ---
    # condition helpers of Forge's CraftingHelper -> slimeknights.mantle.util.CraftingHelper
    (r'\bnet\.(?:minecraftforge\.common|neoforged\.neoforge\.common)\.crafting\.CraftingHelper\b', 'slimeknights.mantle.util.CraftingHelper'),
    (r'\bnet\.minecraftforge\.common\.crafting\.conditions\.', 'net.neoforged.neoforge.common.conditions.'),
    (r'\bnet\.minecraftforge\.common\.util\.FakePlayer\b', 'net.neoforged.neoforge.common.util.FakePlayer'),
    (r'\bnet\.minecraftforge\.common\.ForgeConfigSpec\b', 'net.neoforged.neoforge.common.ModConfigSpec'),
    (r'\bForgeConfigSpec\b', 'ModConfigSpec'),
    (r'\bnet\.minecraftforge\.common\.Tags\b', 'net.neoforged.neoforge.common.Tags'),
    (r'\bnet\.minecraftforge\.common\.ForgeMod\b', 'net.neoforged.neoforge.common.NeoForgeMod'),
    (r'\bForgeMod\.', 'NeoForgeMod.'),
    (r'\bnet\.minecraftforge\.common\.ForgeI18n\b', 'net.neoforged.neoforge.common.I18nExtension'),
    (r'\bForgeI18n\b', 'I18nExtension'),
    (r'\bnet\.minecraftforge\.common\.ForgeHooks\b', 'net.neoforged.neoforge.common.CommonHooks'),
    (r'\bForgeHooks\.', 'CommonHooks.'),
    (r'\bnet\.minecraftforge\.event\.ForgeEventFactory\b', 'net.neoforged.neoforge.event.EventHooks'),
    (r'\bForgeEventFactory\.', 'EventHooks.'),
    (r'\bnet\.minecraftforge\.event\.', 'net.neoforged.neoforge.event.'),
    (r'\bnet\.minecraftforge\.common\.loot\.', 'net.neoforged.neoforge.common.loot.'),
    (r'\bnet\.minecraftforge\.common\.world\.', 'net.neoforged.neoforge.common.world.'),
    (r'\bnet\.minecraftforge\.common\.SoundActions?\b', lambda m: m.group(0).replace('net.minecraftforge.common', 'net.neoforged.neoforge.common')),
    (r'\bnet\.minecraftforge\.common\.util\.', 'net.neoforged.neoforge.common.util.'),
    (r'\bnet\.minecraftforge\.common\.extensions\.', 'net.neoforged.neoforge.common.extensions.'),
    (r'\bnet\.minecraftforge\.common\.crafting\.', 'net.neoforged.neoforge.common.crafting.'),
    # Forge SimpleChannel API -> Mantle's NetworkWrapper shims (slimeknights.mantle.network)
    (r'\bnet\.(?:minecraftforge|neoforged\.neoforge)\.network\.NetworkEvent\b', 'slimeknights.mantle.network.NetworkEvent'),
    (r'\bnet\.(?:minecraftforge|neoforged\.neoforge)\.network\.NetworkDirection\b', 'slimeknights.mantle.network.NetworkDirection'),
    (r'\bnet\.minecraftforge\.network\.', 'net.neoforged.neoforge.network.'),
    (r'\bnet\.minecraftforge\.client\.', 'net.neoforged.neoforge.client.'),
    (r'\bnet\.minecraftforge\.common\.', 'net.neoforged.neoforge.common.'),
    (r'\bnet\.minecraftforge\.entity\.', 'net.neoforged.neoforge.entity.'),
    (r'\bnet\.minecraftforge\.items\.', 'net.neoforged.neoforge.items.'),
    (r'\bnet\.minecraftforge\.data\.', 'net.neoforged.neoforge.data.'),
    # --- removed annotations / renamed classes of Minecraft ---
    (r'\n?import net\.minecraft\.MethodsReturnNonnullByDefault;', ''),
    (r'@MethodsReturnNonnullByDefault\s*', ''),
    (r'\bnet\.minecraft\.commands\.arguments\.ResourceLocationArgument\b', 'net.minecraft.commands.arguments.IdentifierArgument'),
    (r'\bResourceLocationArgument\b', 'IdentifierArgument'),
    # advancements.critereon was split into advancements.predicates(.entity) and advancements.triggers
    (r'\bnet\.minecraft\.advancements\.critereon\.(AnyBlockInteractionTrigger|BeeNestDestroyedTrigger|BredAnimalsTrigger|BrewedPotionTrigger|ChangeDimensionTrigger|ChanneledLightningTrigger|ConstructBeaconTrigger|ConsumeItemTrigger|CriteriaTriggers|Criterion|CriterionTrigger|CuredZombieVillagerTrigger|DefaultBlockInteractionTrigger|DistanceTrigger|EffectsChangedTrigger|EnchantedItemTrigger|EnterBlockTrigger|EntityHurtPlayerTrigger|FallAfterExplosionTrigger|FilledBucketTrigger|FishingRodHookedTrigger|ImpossibleTrigger|InventoryChangeTrigger|ItemDurabilityTrigger|ItemUsedOnLocationTrigger|KilledByArrowTrigger|KilledTrigger|LevitationTrigger|LightningStrikeTrigger|LootTableTrigger|PickedUpItemTrigger|PlayerHurtEntityTrigger|PlayerInteractTrigger|PlayerTrigger|RecipeCraftedTrigger|RecipeUnlockedTrigger|ShotCrossbowTrigger|SimpleCriterionTrigger|SlideDownBlockTrigger|SpearMobsTrigger|StartRidingTrigger|SummonedEntityTrigger|TameAnimalTrigger|TargetBlockTrigger|TradeTrigger|UsedEnderEyeTrigger|UsedTotemTrigger|UsingItemTrigger)\b', r'net.minecraft.advancements.triggers.\1'),
    (r'\bnet\.minecraft\.advancements\.critereon\.(CubeMobPredicate|DistanceToPlayerPredicate|EntityEffectsPredicate|EntityEquipmentPredicate|EntityExactDataComponentsPredicate|EntityFlagsPredicate|EntityLocationPredicate|EntityNbtPredicate|EntityPartialComponentsPredicate|EntityPredicate|EntitySlotsPredicate|EntitySubPredicate|EntitySubPredicates|EntityTagPredicate|EntityTypePredicate|FishingHookPredicate|LightningBoltPredicate|MovementAffectedByPredicate|MovementPredicate|PassengerPredicate|PeriodicEntityTickPredicate|PlayerPredicate|RaiderPredicate|SheepPredicate|SteppingOnPredicate|TargetedEntityPredicate|TeamPredicate|VehiclePredicate)\b', r'net.minecraft.advancements.predicates.entity.\1'),
    (r'\bnet\.minecraft\.advancements\.critere?on\.', 'net.minecraft.advancements.predicates.'),
    (r'\bnet\.minecraft\.ResourceLocationException\b', 'net.minecraft.IdentifierException'),
    (r'\bResourceLocationException\b', 'IdentifierException'),
    (r'\bnet\.(?:minecraftforge|neoforged\.neoforge)\.common\.ToolActions\b', 'net.neoforged.neoforge.common.ItemAbilities'),
    (r'\bnet\.(?:minecraftforge|neoforged\.neoforge)\.common\.ToolAction\b', 'net.neoforged.neoforge.common.ItemAbility'),
    (r'\bToolActions\b', 'ItemAbilities'),
    (r'\bToolAction\b', 'ItemAbility'),
    (r'\bnet\.neoforged\.neoforge\.client\.model\.data\.', 'net.neoforged.neoforge.model.data.'),
    (r'\bnet\.neoforged\.neoforge\.common\.util\.NonNullConsumer\b', 'java.util.function.Consumer'),
    (r'\bnet\.neoforged\.neoforge\.common\.util\.NonNullFunction\b', 'java.util.function.Function'),
    (r'\bnet\.neoforged\.neoforge\.common\.util\.NonNullSupplier\b', 'java.util.function.Supplier'),
    (r'\bNonNullConsumer\b', 'Consumer'),
    (r'\bNonNullFunction\b', 'Function'),
    (r'\bNonNullSupplier\b', 'Supplier'),
    # Gson based SimpleJsonResourceReloadListener of 1.20.1 -> Mantle shim
    (r'\bnet\.minecraft\.server\.packs\.resources\.SimpleJsonResourceReloadListener\b', 'slimeknights.mantle.data.listener.JsonReloadListener'),
    (r'\bextends SimpleJsonResourceReloadListener\b', 'extends JsonReloadListener'),
    # --- call patterns ---
    (r'\.isClientSide\b(?!\s*\()', '.isClientSide()'),
    (r'\.getOrThrow\(false,\s*[^()]*?\)', '.getOrThrow()'),
    (r'\bregistryOrThrow\(', 'lookupOrThrow('),
    (r'\bModLoader\.isLoadingStateValid\(\)', '!ModLoader.hasErrors()'),
    (r'\bFMLEnvironment\.dist\b', 'FMLEnvironment.getDist()'),
    (r'\bFMLEnvironment\.production\b', 'FMLEnvironment.isProduction()'),
    (r'\bScreen\.has(Shift|Control|Alt)Down\(\)', r'Minecraft.getInstance().has\1Down()'),
    # --- javax annotations: keep, jsr305 is on the 26.3 classpath? (checked by the compiler) ---
]


def convert(text: str) -> str:
    for pattern, repl in RULES:
        text = re.sub(pattern, repl, text)
    return text


def main(args):
    files = []
    for a in args:
        p = Path(a)
        files += sorted(p.rglob('*.java')) if p.is_dir() else [p]
    changed = 0
    for f in files:
        old = f.read_text(encoding='utf-8')
        new = convert(old)
        if new != old:
            f.write_text(new, encoding='utf-8')
            changed += 1
    print(f'{changed} of {len(files)} files changed')


if __name__ == '__main__':
    main(sys.argv[1:] or ['.'])
