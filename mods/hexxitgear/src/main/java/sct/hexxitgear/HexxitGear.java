package sct.hexxitgear;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import sct.hexxitgear.block.HexbiscusBlock;
import sct.hexxitgear.core.SetBonuses;
import sct.hexxitgear.gen.HGWorldGen;

import java.util.List;

/**
 * Hexxit Gear 1.0.23: hexical essence (from the hexbiscus flower) and hexical diamond, the Tribal, Scale and Thief
 * armor sets (diamond armor values, own helmet models) with their set bonuses, and the hexbiscus world generator.
 */
@Mod(HexxitGear.MOD_ID)
public final class HexxitGear {
    public static final String MOD_ID = "hexxitgear";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    /** EnumArmorMaterial.DIAMOND (durability factor 33, 3/8/6/3, enchantability 10, repaired with diamonds) with the set's textures. */
    private static ArmorMaterial diamond(String asset) {
        ArmorMaterial d = ArmorMaterials.DIAMOND;
        return new ArmorMaterial(d.durability(), d.defense(), d.enchantmentValue(), SoundEvents.ARMOR_EQUIP_DIAMOND, d.toughness(),
                d.knockbackResistance(), ItemTags.REPAIRS_DIAMOND_ARMOR, asset(asset));
    }

    public static final ArmorMaterial TRIBAL = diamond("tribal");
    public static final ArmorMaterial SCALE = diamond("scale");
    public static final ArmorMaterial THIEF = diamond("thief");
    /** The helmets render with their own models and textures (textures/maps of the original). */
    public static final ArmorMaterial SKULL_HELMET = diamond("skull_helmet");
    public static final ArmorMaterial SCALE_HELMET_MODEL = diamond("scale_helmet");
    public static final ArmorMaterial HOOD_HELMET = diamond("hood_helmet");

    /** BlockFlower: no collision, breaks instantly, grass sound. Drops hexical essence. */
    public static final DeferredBlock<HexbiscusBlock> HEXBISCUS = BLOCKS.registerBlock("hexbiscus", HexbiscusBlock::new,
            p -> p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED));
    public static final DeferredItem<BlockItem> HEXBISCUS_ITEM = ITEMS.registerSimpleBlockItem(HEXBISCUS);

    public static final DeferredItem<Item> HEXICAL_ESSENCE = ITEMS.registerSimpleItem("hexical_essence");
    public static final DeferredItem<Item> HEXICAL_DIAMOND = ITEMS.registerSimpleItem("hexical_diamond");

    public static final DeferredItem<Item> TRIBAL_HELMET = armor("tribal_helmet", SKULL_HELMET, ArmorType.HELMET);
    public static final DeferredItem<Item> TRIBAL_CHEST = armor("tribal_chest", TRIBAL, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> TRIBAL_LEGGINGS = armor("tribal_leggings", TRIBAL, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> TRIBAL_BOOTS = armor("tribal_boots", TRIBAL, ArmorType.BOOTS);
    public static final DeferredItem<Item> SCALE_HELMET = armor("scale_helmet", SCALE_HELMET_MODEL, ArmorType.HELMET);
    public static final DeferredItem<Item> SCALE_CHEST = armor("scale_chest", SCALE, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> SCALE_LEGGINGS = armor("scale_leggings", SCALE, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> SCALE_BOOTS = armor("scale_boots", SCALE, ArmorType.BOOTS);
    public static final DeferredItem<Item> THIEF_HELMET = armor("thief_helmet", HOOD_HELMET, ArmorType.HELMET);
    public static final DeferredItem<Item> THIEF_CHEST = armor("thief_chest", THIEF, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> THIEF_LEGGINGS = armor("thief_leggings", THIEF, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> THIEF_BOOTS = armor("thief_boots", THIEF, ArmorType.BOOTS);

    /** Item and block ids of the pack configuration (+256, as Item(id) added it) in the order of the creative tab. */
    private static final List<Object[]> OLD_IDS = List.of(
            new Object[] {HGWorldGen.HEXBISCUS, HEXBISCUS_ITEM},
            new Object[] {7451 + 256, HEXICAL_DIAMOND}, new Object[] {7452 + 256, HEXICAL_ESSENCE},
            new Object[] {7453 + 256, SCALE_BOOTS}, new Object[] {7454 + 256, SCALE_CHEST},
            new Object[] {7455 + 256, SCALE_HELMET}, new Object[] {7456 + 256, SCALE_LEGGINGS},
            new Object[] {7457 + 256, THIEF_BOOTS}, new Object[] {7458 + 256, THIEF_CHEST},
            new Object[] {7459 + 256, THIEF_HELMET}, new Object[] {7460 + 256, THIEF_LEGGINGS},
            new Object[] {7461 + 256, TRIBAL_CHEST}, new Object[] {7462 + 256, TRIBAL_HELMET},
            new Object[] {7463 + 256, TRIBAL_LEGGINGS}, new Object[] {7464 + 256, TRIBAL_BOOTS});

    /** HGCreativeTab "Hexxit Gear" with the tribal helmet as icon; items in id order like 1.5.2 listed them. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("hexxit_gear", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.hexxitgear"))
            .icon(() -> new ItemStack(TRIBAL_HELMET.get()))
            .displayItems((parameters, output) -> {
                for (Object[] entry : OLD_IDS) {
                    output.accept((DeferredItem<?>) entry[1]);
                }
            })
            .build());

    private static DeferredItem<Item> armor(String name, ArmorMaterial material, ArmorType type) {
        return ITEMS.registerItem(name, Item::new, p -> p.humanoidArmor(material, type));
    }

    public HexxitGear(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
        NeoForge.EVENT_BUS.register(SetBonuses.class);
        HGWorldGen.register();
        OldBlockStates.register(HGWorldGen.HEXBISCUS, () -> HEXBISCUS.get().defaultBlockState());
        // chest loot of other mods of the pack (Battle Towers, Infernal Mobs) names these items by their old ids
        for (Object[] entry : OLD_IDS) {
            OldGameIds.registerItem((Integer) entry[0], MOD_ID + ":" + ((DeferredItem<?>) entry[1]).getId().getPath());
        }
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            sct.hexxitgear.client.HexxitGearClient.register(modBus);
        }
    }
}
