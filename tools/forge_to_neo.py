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
    (r'\bnew Identifier\.Serializer\(\)', 'slimeknights.mantle.data.gson.ResourceLocationSerializer.resourceLocation("minecraft")'),
    (r'\bTextColor\.parseColor\(([^()]*)\)(?!\.result)', r'TextColor.parseColor(\1).result().orElse(null)'),
    (r'\bimport org\.lwjgl\.glfw\.GLFW;', 'import com.mojang.blaze3d.platform.InputConstants;'),  # 26.3 input is SDL
    (r'\bGLFW\.GLFW_KEY_', 'InputConstants.KEY_'),
    (r'\bnet\.neoforged\.neoforge\.common\.conditions\.TrueCondition\b', 'net.neoforged.neoforge.common.conditions.AlwaysCondition'),
    (r'\bTrueCondition\b', 'AlwaysCondition'),
    (r'\bnet\.minecraft\.util\.FastColor\b', 'net.minecraft.util.ARGB'),
    (r'\bFastColor\.ARGB32\.', 'ARGB.'),
    (r'\bimport net\.neoforged\.neoforge\.common\.crafting\.IShapedRecipe;', 'import net.minecraft.world.item.crafting.ShapedRecipe;'),
    (r'\bIShapedRecipe<\?>', 'ShapedRecipe'),
    (r'\.getRecipeWidth\(\)', '.getWidth()'),
    (r'\.getRecipeHeight\(\)', '.getHeight()'),
    (r'\bMinecraft\.getInstance\(\)\.setScreen\(', 'Minecraft.getInstance().gui.setScreen('),
    (r'\bMinecraft\.getInstance\(\)\.screen\b', 'Minecraft.getInstance().gui.screen()'),
    (r'\b(\w+)\.readItem\(\)', r'ItemStack.OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf) \1)'),
    (r'\b(\w+)\.writeItem\(([^;]+)\);', r'ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf) \1, \2);'),
    (r'\bInventoryMenu\.BLOCK_ATLAS\b', 'net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS'),
    (r'\bInteractionResult\.sidedSuccess\([^()]*(?:\([^()]*\))?\)', 'InteractionResult.SUCCESS'),
    (r'\bnet\.minecraft\.client\.renderer\.RenderType\b', 'net.minecraft.client.renderer.rendertype.RenderType'),
    (r'\bRenderType\.(entitySolid|entityCutout|entityCutoutNoCull|entityTranslucent|entityTranslucentCull|itemEntityTranslucentCull|translucent|cutout|solid)\(', r'net.minecraft.client.renderer.rendertype.RenderTypes.\1('),
    (r'\bMinecraft\.getInstance\(\)\.getModelManager\(\)\.getAtlas\(([^()]+)\)\.getSprite\(([^()]+)\)', r'Minecraft.getInstance().getAtlasManager().get(new net.minecraft.client.resources.model.sprite.SpriteId(\1, \2))'),
    (r'\bnet\.minecraft\.world\.level\.BlockAndTintGetter\b', 'net.minecraft.client.renderer.block.BlockAndTintGetter'),
    (r'\.mulPose\(Axis\.(\w+)\.rotationDegrees\(', r'.rotateDegrees(Axis.\1, ('),  # 26.3 PoseStack; closing parens stay balanced
    (r'\.mulPose\(Axis\.(\w+)\.rotation\(', r'.rotate(Axis.\1, ('),
    # --- event bus / fml ---
    (r'\bRegisterClientReloadListenersEvent\b', 'AddClientReloadListenersEvent'),  # registerReloadListener(x) -> addListener(id, x) by hand
    (r'\bnet\.minecraftforge\.eventbus\.api\.', 'net.neoforged.bus.api.'),
    (r'\bnet\.minecraftforge\.fml\.common\.Mod\.EventBusSubscriber\b', 'net.neoforged.fml.common.EventBusSubscriber'),
    (r'\bnet\.minecraftforge\.fml\.', 'net.neoforged.fml.'),
    (r'\bnet\.minecraftforge\.api\.distmarker\.', 'net.neoforged.api.distmarker.'),
    (r'\bnet\.minecraftforge\.common\.MinecraftForge\b', 'net.neoforged.neoforge.common.NeoForge'),
    (r'\bMinecraftForge\.EVENT_BUS\b', 'NeoForge.EVENT_BUS'),
    # --- registries ---
    (r'\bnet\.minecraftforge\.registries\.RegistryObject\b', 'slimeknights.mantle.registration.RegistryObject'),
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
    # typed NBT lookups of 1.20.1 (gone in 26.3) -> Mantle TagHelper (Teil 27d); receiver must be a simple expression; Tinkers mod data views (modData, persistentData ...) keep their own contains/getList
    (r'\b(?!(?:modData|persistentData|persistent|volatileData|toolData)\b)([A-Za-z_][\w.]*(?:\(\))?)\.contains\(([^,()]+), (Tag\.TAG_[A-Z_]+|\d+)\)', r'slimeknights.mantle.util.TagHelper.contains(\1, \2, \3)'),
    (r'\b(?!(?:modData|persistentData|persistent|volatileData|toolData)\b)([A-Za-z_][\w.]*(?:\(\))?)\.getList\(([^,()]+), (Tag\.TAG_[A-Z_]+|\d+)\)', r'slimeknights.mantle.util.TagHelper.getList(\1, \2, \3)'),
    (r'\bTag\.TAG_ANY_NUMERIC\b', 'slimeknights.mantle.util.TagHelper.TAG_ANY_NUMERIC'),
    # Entity sync flags: push() sets needsSync (1.20.1 hasImpulse), markHurt() sets syncVelocity (1.20.1 hurtMarked)
    (r'\.hasImpulse\b', '.needsSync'),
    (r'\.hurtMarked\b', '.syncVelocity'),
    (r'\bLivingTickEvent\b', 'EntityTickEvent.Pre'),
    (r'\bimport net\.neoforged\.neoforge\.event\.entity\.living\.LivingEvent\.EntityTickEvent\.Pre;', 'import net.neoforged.neoforge.event.tick.EntityTickEvent;'),
    # 26.3 renames (Teil 27d)
    (r'\bimport net\.minecraft\.world\.item\.ArmorItem\.Type;', 'import net.minecraft.world.item.equipment.ArmorType;'),
    (r'\bArmorItem\.Type\b', 'ArmorType'),
    (r'\bimport net\.minecraft\.world\.entity\.projectile\.AbstractArrow;', 'import net.minecraft.world.entity.projectile.arrow.AbstractArrow;'),
    (r'\bimport net\.minecraft\.world\.item\.UseAnim;', 'import net.minecraft.world.item.ItemUseAnimation;'),
    (r'\bUseAnim\b', 'ItemUseAnimation'),
    (r'\bimport net\.neoforged\.neoforge\.event\.AddReloadListenerEvent;', 'import net.neoforged.neoforge.event.AddServerReloadListenersEvent;'),
    (r'\bAddReloadListenerEvent\b', 'AddServerReloadListenersEvent'),
    (r'\bimport net\.neoforged\.neoforge\.event\.entity\.living\.MobSpawnEvent\.FinalizeSpawn;', 'import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;'),
    (r'\bMobSpawnEvent\.FinalizeSpawn\b', 'FinalizeSpawnEvent'),
    (r'\bFinalizeSpawn\b(?!Event)', 'FinalizeSpawnEvent'),
    (r'\bimport net\.minecraft\.world\.level\.storage\.loot\.parameters\.LootContextParamSet;', 'import net.minecraft.util.context.ContextKeySet;'),
    (r'\bLootContextParamSet\b', 'ContextKeySet'),
    (r'\bimport net\.minecraft\.world\.level\.storage\.loot\.parameters\.LootContextParam;', 'import net.minecraft.util.context.ContextKey;'),
    (r'\bLootContextParam<', 'ContextKey<'),
    (r'\bEntityType\.([A-Z][A-Z_0-9]+)\b', r'EntityTypes.\1'),  # vanilla entity type constants moved to EntityTypes
    (r'\bFMLLoader\.isProduction\(\)', 'net.neoforged.fml.loading.FMLEnvironment.isProduction()'),
    (r'\bModLoader\.get\(\)\.', 'ModLoader.'),
    # harvest tiers: Tinkers 3 keeps its own Tier/TierSortingRegistry (26.3 has neither)
    (r'\bimport net\.minecraft\.world\.item\.Tiers?;', 'import slimeknights.tconstruct.library.utils.Tier;'),
    (r'\bimport net\.neoforged\.neoforge\.common\.TierSortingRegistry;', 'import slimeknights.tconstruct.library.utils.TierSortingRegistry;'),
    (r'\bTiers\.([A-Z]+)\b', r'Tier.\1'),
    (r'\bimport net\.minecraft\.advancements\.CriteriaTriggers;', 'import net.minecraft.advancements.triggers.CriteriaTriggers;'),
    (r'\.getFilterFlag\(\)', '.getId()'),  # EquipmentSlot: same numbers 0-5, 26.3 adds BODY 6 and SADDLE 7
    # attribute modifiers (26.3: ids instead of UUIDs, renamed operations)
    (r'\bOperation\.ADDITION\b', 'Operation.ADD_VALUE'),
    (r'\bOperation\.MULTIPLY_BASE\b', 'Operation.ADD_MULTIPLIED_BASE'),
    (r'\bOperation\.MULTIPLY_TOTAL\b', 'Operation.ADD_MULTIPLIED_TOTAL'),
    (r'\bItem\.BASE_ATTACK_DAMAGE_UUID\b', 'Item.BASE_ATTACK_DAMAGE_ID'),
    (r'\bItem\.BASE_ATTACK_SPEED_UUID\b', 'Item.BASE_ATTACK_SPEED_ID'),
    (r'\bItemStack\.ATTRIBUTE_MODIFIER_FORMAT\b', 'net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT'),
    (r'\boperation\.toValue\(\)', 'operation.id()'),
    (r'\bItemAbilities\.SHIELD_BLOCK\b', 'ItemAbilities.SHIELD_BLOCK'),
    # LazyOptional is gone in NeoForge 26.3; Tinkers 3 keeps it as its own stand-in (Teil 27d)
    (r'\bnet\.minecraftforge\.common\.util\.LazyOptional\b', 'slimeknights.tconstruct.library.utils.LazyOptional'),
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
    # Forge item handler API -> Mantle's compat layer on the 26.3 transfer API (slimeknights.mantle.inventory.compat)
    (r'\bnet\.(?:minecraftforge|neoforged\.neoforge)\.items\.(?:wrapper\.)?(IItemHandler|IItemHandlerModifiable|ItemStackHandler|ItemHandlerHelper|SlotItemHandler|InvWrapper|SidedInvWrapper|EmptyHandler)\b', r'slimeknights.mantle.inventory.compat.\1'),
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
    # --- block properties and block constructors of 26.3 ---
    (r'\b(BlockBehaviour\.)?Properties\.copy\(', r'\1Properties.ofFullCopy('),
    (r'\bPushReaction\.DESTROY\b', 'PushReaction.POPPED'),
    (r'\bPushReaction\.BLOCK\b', 'PushReaction.IMMOVEABLE'),
    (r'\.noCollission\(\)', '.noCollision()'),
    (r'\.dropsLike\(([\w.]+)\)', r'.overrideLootTable(\1.getLootTable())'),
    (r'\bnew StairBlock\(\(\) -> ([\w.()]+?)\.defaultBlockState\(\),', r'new StairBlock(\1.defaultBlockState(),'),
    (r'\bnew StairBlock\((\w+)::defaultBlockState,', r'new StairBlock(\1.defaultBlockState(),'),
    # block methods with new 26.3 signatures (bodies using the removed parameters are left for the compiler)
    (r'public InteractionResult use\(BlockState (\w+), Level (\w+), BlockPos (\w+), Player (\w+), InteractionHand \w+, BlockHitResult (\w+)\)',
     r'protected InteractionResult useWithoutItem(BlockState \1, Level \2, BlockPos \3, Player \4, BlockHitResult \5)'),
    (r'public BlockState updateShape\(BlockState (\w+), Direction (\w+), BlockState (\w+), LevelAccessor (\w+), BlockPos (\w+), BlockPos (\w+)\)',
     r'protected BlockState updateShape(BlockState \1, LevelReader \4, ScheduledTickAccess tickAccess, BlockPos \5, Direction \2, BlockPos \6, BlockState \3, RandomSource random)'),
    (r'\bpublic (VoxelShape get(?:Collision|Visual)?Shape|boolean canSurvive|RenderShape getRenderShape|BlockState rotate|BlockState mirror|boolean isSignalSource|int getSignal|int getDirectSignal|boolean hasAnalogOutputSignal|int getAnalogOutputSignal|void tick|void randomTick|boolean isRandomlyTicking|FluidState getFluidState)\(',
     r'protected \1('),
    # chat: action bar / system messages
    (r'\.displayClientMessage\(((?:[^()]|\((?:[^()]|\([^()]*\))*\))*), true\)', r'.sendOverlayMessage(\1)'),
    (r'\.displayClientMessage\(((?:[^()]|\((?:[^()]|\([^()]*\))*\))*), false\)', r'.sendSystemMessage(\1)'),
    (r'\b(fluid\w*|\w*[fF]luid|stack|transferred)\.getDisplayName\(\)', r'\1.getHoverName()'),
    # registry tags and lookups of 26.3
    (r'\.getTagNames\(\)', '.getTags().map(net.minecraft.core.HolderSet.Named::key)'),
    (r'\b(registry|result|reg)\.getTag\(', r'\1.get('),
    (r'(registryAccess\(\)\s*\.)registry\(', r'\1lookup('),
    (r'\bResourceKey::location\b', 'ResourceKey::identifier'),
    (r'\bHolder::getTagKeys\b', 'Holder::tags'),
    # Forge pick block -> NeoForge 26.3 IBlockExtension#getCloneItemStack
    (r'public ItemStack getCloneItemStack\(BlockState (\w+), HitResult \w+, (?:BlockGetter|LevelReader) (\w+), BlockPos (\w+), Player (\w+)\)',
     r'public ItemStack getCloneItemStack(LevelReader \2, BlockPos \3, BlockState \1, boolean includeData, Player \4)'),
    # block entity types: no builder and no datafixer type in 26.3
    (r'\bBlockEntityType\.Builder\.(<\w+>)?of\(([^;]*?)\)\.build\([^()]*(?:\([^()]*\))?\)', r'new BlockEntityType\1(\2)'),
    (r'\bnew BlockEntityType<>\((\w+), ([^;]*?), getType\(name\)\)', r'new BlockEntityType<>(\1, java.util.Set.<Block>copyOf(\2))'),
    (r'\.lootFrom\(([\w.]+)\)', r'.overrideLootTable(\1.get().getLootTable())'),
    # --- javax annotations: keep, jsr305 is on the 26.3 classpath? (checked by the compiler) ---
]


# 26.3 moved these constructor arguments: class -> (old argument order -> new order), by argument count
CTOR_ORDER = {
    'DoorBlock': {2: (1, 0)},
    'TrapDoorBlock': {2: (1, 0)},
    'FenceGateBlock': {2: (1, 0)},
    'ButtonBlock': {4: (1, 2, 0)},
    'PressurePlateBlock': {3: (2, 1)},
    'StandingSignBlock': {2: (1, 0)},
    'WallSignBlock': {2: (1, 0)},
    'CeilingHangingSignBlock': {2: (1, 0)},
    'WallHangingSignBlock': {2: (1, 0)},
}


def _split_args(text: str, start: int):
    """Arguments of the call whose '(' is at start: (list of argument strings, index after ')') or None"""
    depth, args, cur, i = 0, [], [], start + 1
    while i < len(text):
        c = text[i]
        if c in '([{<' and not (c == '<' and depth == 0 and False):
            depth += c in '([{'
        if c in ')]}':
            if depth == 0:
                args.append(''.join(cur).strip())
                return args, i + 1
            depth -= 1
        if c == ',' and depth == 0:
            args.append(''.join(cur).strip())
            cur = []
        else:
            cur.append(c)
        i += 1
    return None


def reorder_constructors(text: str) -> str:
    """Puts the arguments of the constructors in CTOR_ORDER into their 26.3 order (only for the 1.20.1 signatures:
    BlockSetType/WoodType last; already reordered calls have it first and are left alone)."""
    for name, orders in CTOR_ORDER.items():
        pos = 0
        pattern = re.compile(r'\bnew ' + name + r'\(')
        while (m := pattern.search(text, pos)):
            res = _split_args(text, m.end() - 1)
            pos = m.end()
            if not res:
                continue
            args, end = res
            order = orders.get(len(args))
            # old form: properties first (props variable or a builder chain), type second
            if order is None or re.search(r'(Type|setType|woodType|Sensitivity)\b', args[0]):
                continue
            new = [args[i] for i in order]
            if name == 'PressurePlateBlock':
                pass  # sensitivity is part of BlockSetType in 26.3, dropped
            replacement = 'new ' + name + '(' + ', '.join(new) + ')'
            text = text[:m.start()] + replacement + text[end:]
            pos = m.start() + len(replacement)
    return text


# types the rules above introduce: simple name -> import
IMPORTS = {
    'ScheduledTickAccess': 'net.minecraft.world.level.ScheduledTickAccess',
    'RandomSource': 'net.minecraft.util.RandomSource',
    'LevelReader': 'net.minecraft.world.level.LevelReader',
    'ServerLevel': 'net.minecraft.server.level.ServerLevel',
    'BlockEntityTypes': 'net.minecraft.world.level.block.entity.BlockEntityTypes',
    'Prediction': 'net.minecraft.util.Prediction',
    'EntityTypes': 'net.minecraft.world.entity.EntityTypes',
    'ArmorType': 'net.minecraft.world.item.equipment.ArmorType',
}


def add_imports(text: str) -> str:
    """Adds imports for the types in IMPORTS that the file uses but does not import"""
    m = re.search(r'^package [\w.]+;\n', text, re.M)
    if not m:
        return text
    missing = []
    for name, full in IMPORTS.items():
        if re.search(r'\b' + name + r'\b', text) and not re.search(r'^import ' + re.escape(full) + ';', text, re.M) \
                and not re.search(r'^import [\w.]+\.' + name + ';', text, re.M):
            missing.append(f'import {full};\n')
    if missing:
        text = text[:m.end()] + '\n' + ''.join(missing) + text[m.end():]
    return text


VERTEX_CHAIN = [
    (r'\.vertex\(', '.addVertex('), (r'\.color\(', '.setColor('), (r'\.uv\(', '.setUv('), (r'\.uv2\(', '.setUv2('),
    (r'\.overlayCoords\(', '.setOverlay('), (r'\.normal\(', '.setNormal('), (r'\.endVertex\(\)', ''),
]


def vertex_chains(text: str) -> str:
    """1.20.1 vertex builder chains (vertex(...).color(...).uv(...).endVertex()) -> 26.3 (addVertex(...).setColor(...))"""
    out = []
    for line in text.split('\n'):
        if re.search(r'\.vertex\([^)]*\)\.(?:color|uv|normal|overlayCoords)\(', line) or ('.endVertex()' in line and '.vertex(' in line):
            for pattern, repl in VERTEX_CHAIN:
                line = re.sub(pattern, repl, line)
        out.append(line)
    return '\n'.join(out)


def convert(text: str) -> str:
    for pattern, repl in RULES:
        text = re.sub(pattern, repl, text)
    return add_imports(reorder_constructors(vertex_chains(text)))


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
