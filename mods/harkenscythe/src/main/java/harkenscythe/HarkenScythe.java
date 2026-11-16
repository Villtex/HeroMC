package harkenscythe;

import harkenscythe.altar.Altars;
import harkenscythe.event.HSEvents;
import harkenscythe.gen.HSWorldGen;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import com.mojang.serialization.Codec;
import harkenscythe.recipe.SpectralDyeRecipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Harken Scythe 2.1.6 (mod id HarkenScythe_Core): scythes reap the souls of slain mobs, glaives spill their blood;
 * keepers and vessels hold both, and talismans, amulets, the Necronomicon, the crucibles and the augments use them.
 * Livingmetal and biomass gear repairs itself from souls and blood.
 * <p>
 * The altars ({@link harkenscythe.altar}) work with the souls and the blood of the crucibles. The spectral mobs, the Harbinger and the essence slimes (part 3) follow later; see
 * {@link harkenscythe.entity.SpectralPets} and the NOTES files of the project.
 */
@Mod(HarkenScythe.MOD_ID)
public final class HarkenScythe {
    public static final String MOD_ID = "harkenscythe_core";
    /** FML mod id of the original, for the chest loot order of worldgen152. */
    public static final String ORIGINAL_MOD_ID = "HarkenScythe_Core";

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    /** ItemHSKeeper.flaskDrinkCount: drinks taken from a keeper since the drinking started (the HUD shows it). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DRINK_COUNT = COMPONENTS.registerComponentType("drink_count",
            b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SpectralDyeRecipe>> SPECTRAL_DYE = SERIALIZERS.register("spectral_dye",
            () -> SpectralDyeRecipe.SERIALIZER);

    // augments (data-driven enchantments)
    public static final ResourceKey<Enchantment> SOUL_ATTUNED = enchantment("soul_attuned");
    public static final ResourceKey<Enchantment> BLOOD_ATTUNED = enchantment("blood_attuned");
    public static final ResourceKey<Enchantment> SOULSTEAL = enchantment("soulsteal");
    public static final ResourceKey<Enchantment> BLOODLETTING = enchantment("bloodletting");
    public static final ResourceKey<Enchantment> BLIGHT = enchantment("blight");
    public static final ResourceKey<Enchantment> HEMORRHAGE = enchantment("hemorrhage");
    public static final ResourceKey<Enchantment> VITALITY = enchantment("vitality");
    public static final ResourceKey<Enchantment> EXUDE = enchantment("exude");
    public static final ResourceKey<Enchantment> AFTERLIFE = enchantment("afterlife");
    public static final ResourceKey<Enchantment> DECAPITATE = enchantment("decapitate");
    public static final ResourceKey<Enchantment> WARD = enchantment("ward");
    public static final ResourceKey<Enchantment> SANGUINARY = enchantment("sanguinary");

    /** SpecialTierAbilities: what souls (livingmetal) and blood (biomass) repair in the inventory and in the armour slots. */
    public static final TagKey<Item> LIVINGMETAL_EQUIPMENT = itemTag("livingmetal_equipment");
    public static final TagKey<Item> LIVINGMETAL_ARMOR = itemTag("livingmetal_armor");
    public static final TagKey<Item> BIOMASS_EQUIPMENT = itemTag("biomass_equipment");
    public static final TagKey<Item> BIOMASS_ARMOR = itemTag("biomass_armor");
    /** Mobs of the later parts (filled by Teil 9, part 3). */
    public static final TagKey<EntityType<?>> SPECTRAL_PETS = entityTag("spectral_pets");
    public static final TagKey<EntityType<?>> HARBINGERS = entityTag("harbingers");
    /** Mobs that leave no soul: the essence slimes and the evil spectral miner. */
    public static final TagKey<EntityType<?>> SOULLESS = entityTag("soulless");

    public HarkenScythe(IEventBus modBus) {
        HSBlocks.BLOCKS.register(modBus);
        HSBlocks.BLOCK_ENTITIES.register(modBus);
        HSItems.ITEMS.register(modBus);
        HSEntities.ENTITIES.register(modBus);
        SERIALIZERS.register(modBus);
        COMPONENTS.register(modBus);
        HSItems.register(modBus);
        HSEntities.register(modBus);
        harkenscythe.mob.HSMobs.register(modBus);
        Altars.register(modBus);
        NeoForge.EVENT_BUS.register(HSEvents.class);
        // ChestGenHooks: the Necronomicon page in the chests of the 1.5.2 structures (generated by worldgen152)
        HSWorldGen.register();
        OldGameIds.registerItem(HSWorldGen.NECRONOMICON_PAGE, MOD_ID + ":necronomicon_page");
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            harkenscythe.client.HSClient.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static ResourceKey<Enchantment> enchantment(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, id(path));
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, id(path));
    }

    private static TagKey<EntityType<?>> entityTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, id(path));
    }
}
