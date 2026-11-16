package harkenscythe;

import harkenscythe.item.AmuletItem;
import harkenscythe.item.BookItem;
import harkenscythe.item.GlaiveItem;
import harkenscythe.item.KeeperItem;
import harkenscythe.item.NecronomiconItem;
import harkenscythe.item.ScytheItem;
import harkenscythe.item.SeedItem;
import harkenscythe.item.SkullItem;
import harkenscythe.item.SkullType;
import harkenscythe.item.SpectralArmorItem;
import harkenscythe.item.TalismanItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * The items of mod_HarkenScythe. Tool materials as in 1.5.2: livingmetal (iron, 250 uses, enchantability 20),
 * biomass (iron, 150 uses, 20), the Soul Reaper (iron, 131 uses, 30; repaired with bones).
 */
public final class HSItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HarkenScythe.MOD_ID);

    public static final TagKey<Item> LIVINGMETAL_REPAIR = tag("repairs/livingmetal");
    public static final TagKey<Item> BIOMASS_REPAIR = tag("repairs/biomass");
    public static final TagKey<Item> SOUL_REAPER_REPAIR = tag("repairs/soul_reaper");
    public static final TagKey<Item> SOULWEAVE_REPAIR = tag("repairs/soulweave");
    public static final TagKey<Item> BLOODWEAVE_REPAIR = tag("repairs/bloodweave");

    public static final ToolMaterial LIVINGMETAL_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0F, 2.0F, 20, LIVINGMETAL_REPAIR);
    public static final ToolMaterial BIOMASS_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 150, 6.0F, 2.0F, 20, BIOMASS_REPAIR);
    public static final ToolMaterial SOUL_REAPER_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 131, 6.0F, 2.0F, 30, SOUL_REAPER_REPAIR);

    /** EnumHelper.addArmorMaterial("Livingmetal", 15, {2, 6, 5, 2}, 20). */
    public static final ArmorMaterial LIVINGMETAL_ARMOR = armor(15, 2, 6, 5, 2, 20, SoundEvents.ARMOR_EQUIP_IRON, LIVINGMETAL_REPAIR, "livingmetal");
    /** EnumHelper.addArmorMaterial("Biomass", 10, {2, 5, 4, 1}, 20). */
    public static final ArmorMaterial BIOMASS_ARMOR = armor(10, 2, 5, 4, 1, 20, SoundEvents.ARMOR_EQUIP_IRON, BIOMASS_REPAIR, "biomass");
    /** EnumArmorMaterial.CLOTH (5, {1, 3, 2, 1}, 15); the texture follows the dye colour (client). */
    public static final ArmorMaterial SOULWEAVE_ARMOR = armor(5, 1, 3, 2, 1, 15, SoundEvents.ARMOR_EQUIP_LEATHER, SOULWEAVE_REPAIR, "soulweave");
    public static final ArmorMaterial BLOODWEAVE_ARMOR = armor(5, 1, 3, 2, 1, 15, SoundEvents.ARMOR_EQUIP_LEATHER, BLOODWEAVE_REPAIR, "bloodweave");
    /** Leather durability and armour points, the enchantability of chain (12); see {@link SpectralArmorItem}. */
    public static final ArmorMaterial SPECTRAL_LEATHER_ARMOR = armor(5, 1, 3, 2, 1, 12, SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR, "spectral");
    public static final ArmorMaterial SPECTRAL_SOULWEAVE_ARMOR = armor(5, 1, 3, 2, 1, 12, SoundEvents.ARMOR_EQUIP_LEATHER, SOULWEAVE_REPAIR, "spectral");
    public static final ArmorMaterial SPECTRAL_BLOODWEAVE_ARMOR = armor(5, 1, 3, 2, 1, 12, SoundEvents.ARMOR_EQUIP_LEATHER, BLOODWEAVE_REPAIR, "spectral");

    private static final Map<String, List<DeferredItem<? extends Item>>> TABS = new java.util.LinkedHashMap<>();

    // scythes (tabCombat)
    public static final DeferredItem<ScytheItem> WOODEN_SCYTHE = scythe("wooden_scythe", ToolMaterial.WOOD, false);
    public static final DeferredItem<ScytheItem> STONE_SCYTHE = scythe("stone_scythe", ToolMaterial.STONE, false);
    public static final DeferredItem<ScytheItem> IRON_SCYTHE = scythe("iron_scythe", ToolMaterial.IRON, false);
    public static final DeferredItem<ScytheItem> DIAMOND_SCYTHE = scythe("diamond_scythe", ToolMaterial.DIAMOND, false);
    public static final DeferredItem<ScytheItem> GOLDEN_SCYTHE = scythe("golden_scythe", ToolMaterial.GOLD, false);
    public static final DeferredItem<ScytheItem> LIVINGMETAL_SCYTHE = scythe("livingmetal_scythe", LIVINGMETAL_TOOL, false);
    public static final DeferredItem<ScytheItem> BIOMASS_SCYTHE = scythe("biomass_scythe", BIOMASS_TOOL, false);
    public static final DeferredItem<ScytheItem> SOUL_REAPER = scythe("soul_reaper", SOUL_REAPER_TOOL, true);
    // glaives (tabCombat); the number is bloodPoolChance
    public static final DeferredItem<GlaiveItem> WOODEN_GLAIVE = glaive("wooden_glaive", ToolMaterial.WOOD, 0);
    public static final DeferredItem<GlaiveItem> STONE_GLAIVE = glaive("stone_glaive", ToolMaterial.STONE, 1);
    public static final DeferredItem<GlaiveItem> IRON_GLAIVE = glaive("iron_glaive", ToolMaterial.IRON, 2);
    public static final DeferredItem<GlaiveItem> DIAMOND_GLAIVE = glaive("diamond_glaive", ToolMaterial.DIAMOND, 3);
    public static final DeferredItem<GlaiveItem> GOLDEN_GLAIVE = glaive("golden_glaive", ToolMaterial.GOLD, 1);
    public static final DeferredItem<GlaiveItem> LIVINGMETAL_GLAIVE = glaive("livingmetal_glaive", LIVINGMETAL_TOOL, 2);
    public static final DeferredItem<GlaiveItem> BIOMASS_GLAIVE = glaive("biomass_glaive", BIOMASS_TOOL, 2);
    // keepers (tabMisc)
    public static final DeferredItem<KeeperItem> ESSENCE_KEEPER = keeper("essence_keeper", KeeperItem.Kind.ESSENCE_KEEPER);
    public static final DeferredItem<KeeperItem> SOULKEEPER = keeper("soulkeeper", KeeperItem.Kind.SOULKEEPER);
    public static final DeferredItem<KeeperItem> BLOODKEEPER = keeper("bloodkeeper", KeeperItem.Kind.BLOODKEEPER);
    public static final DeferredItem<KeeperItem> ESSENCE_VESSEL = keeper("essence_vessel", KeeperItem.Kind.ESSENCE_VESSEL);
    public static final DeferredItem<KeeperItem> SOUL_VESSEL = keeper("soul_vessel", KeeperItem.Kind.SOUL_VESSEL);
    public static final DeferredItem<KeeperItem> BLOOD_VESSEL = keeper("blood_vessel", KeeperItem.Kind.BLOOD_VESSEL);
    // books (tabMisc)
    public static final DeferredItem<BookItem> BOOK_OF_SHADOWS = item("book_of_shadows", "misc", p -> new BookItem(0, p), BookItem::properties);
    public static final DeferredItem<BookItem> BOOK_OF_CARNAGE = item("book_of_carnage", "misc", p -> new BookItem(1, p), BookItem::properties);
    public static final DeferredItem<NecronomiconItem> NECRONOMICON = item("necronomicon", "misc", NecronomiconItem::new,
            p -> BookItem.properties(p).durability(32).setNoCombineRepair());
    public static final DeferredItem<BookItem> NECRONOMICON_PAGE = item("necronomicon_page", "misc", p -> new BookItem(3, p),
            p -> p.stacksTo(5).rarity(Rarity.EPIC));
    // skulls (tabDecorations)
    public static final Map<SkullType, DeferredItem<SkullItem>> SKULLS = skulls();
    // talismans (tabMisc), amulets (tabTransport)
    public static final DeferredItem<TalismanItem> TALISMAN = item("talisman", "misc", TalismanItem::new,
            p -> p.stacksTo(1).durability(1).setNoCombineRepair().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<TalismanItem> ETHEREAL_TALISMAN = item("ethereal_talisman", "misc", TalismanItem::new,
            p -> p.stacksTo(1).durability(5).setNoCombineRepair().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<AmuletItem> OVERWORLD_AMULET = amulet("overworld_amulet", AmuletItem.Kind.OVERWORLD);
    public static final DeferredItem<AmuletItem> NETHER_AMULET = amulet("nether_amulet", AmuletItem.Kind.NETHER);
    public static final DeferredItem<AmuletItem> ETHEREAL_AMULET = amulet("ethereal_amulet", AmuletItem.Kind.ETHEREAL);
    // materials (tabMaterials; the creepball in tabMisc)
    public static final DeferredItem<Item> LIVINGMETAL_INGOT = item("livingmetal_ingot", "materials", Item::new, HSItems::glint);
    public static final DeferredItem<Item> BIOMASS = item("biomass", "materials", Item::new, HSItems::glint);
    public static final DeferredItem<Item> ECTOPLASM = item("ectoplasm", "materials", Item::new, HSItems::glint);
    public static final DeferredItem<Item> ABYSS_FRAGMENT = item("abyss_fragment", "materials", Item::new, p -> p.rarity(Rarity.EPIC));
    public static final DeferredItem<Item> SPECTRAL_DYE = item("spectral_dye", "materials", Item::new, HSItems::glint);
    public static final DeferredItem<SeedItem> CREEPBALL = item("creepball", "misc", p -> new SeedItem(1, p), UnaryOperator.identity());
    public static final DeferredItem<SeedItem> BIOMASS_SEED = item("biomass_seed", "materials", p -> new SeedItem(0, p), UnaryOperator.identity());
    public static final DeferredItem<SeedItem> GERMINATED_BIOMASS_SEED = item("germinated_biomass_seed", "materials", p -> new SeedItem(2, p),
            HSItems::glint);
    // livingmetal and biomass equipment (sword: tabCombat, the rest tabTools); 1.5.2 values of sword, shovel and
    // pickaxe, the axe and hoe of today's iron tools
    public static final DeferredItem<Item> LIVINGMETAL_SWORD = item("livingmetal_sword", "combat", Item::new, p -> p.sword(LIVINGMETAL_TOOL, 3.0F, -2.4F));
    public static final DeferredItem<Item> LIVINGMETAL_SHOVEL = item("livingmetal_shovel", "tools", Item::new, p -> p.shovel(LIVINGMETAL_TOOL, 1.5F, -3.0F));
    public static final DeferredItem<Item> LIVINGMETAL_PICKAXE = item("livingmetal_pickaxe", "tools", Item::new, p -> p.pickaxe(LIVINGMETAL_TOOL, 1.0F, -2.8F));
    public static final DeferredItem<Item> LIVINGMETAL_AXE = item("livingmetal_axe", "tools", Item::new, p -> p.axe(LIVINGMETAL_TOOL, 6.0F, -3.1F));
    public static final DeferredItem<Item> LIVINGMETAL_HOE = item("livingmetal_hoe", "tools", Item::new, p -> p.hoe(LIVINGMETAL_TOOL, -2.0F, -1.0F));
    public static final DeferredItem<Item> BIOMASS_SWORD = item("biomass_sword", "combat", Item::new, p -> p.sword(BIOMASS_TOOL, 3.0F, -2.4F));
    public static final DeferredItem<Item> BIOMASS_SHOVEL = item("biomass_shovel", "tools", Item::new, p -> p.shovel(BIOMASS_TOOL, 1.5F, -3.0F));
    public static final DeferredItem<Item> BIOMASS_PICKAXE = item("biomass_pickaxe", "tools", Item::new, p -> p.pickaxe(BIOMASS_TOOL, 1.0F, -2.8F));
    public static final DeferredItem<Item> BIOMASS_AXE = item("biomass_axe", "tools", Item::new, p -> p.axe(BIOMASS_TOOL, 6.0F, -3.1F));
    public static final DeferredItem<Item> BIOMASS_HOE = item("biomass_hoe", "tools", Item::new, p -> p.hoe(BIOMASS_TOOL, -2.0F, -1.0F));
    // armour (tabCombat)
    public static final DeferredItem<Item> SOULWEAVE_HOOD = armor("soulweave_hood", SOULWEAVE_ARMOR, ArmorType.HELMET);
    public static final DeferredItem<Item> SOULWEAVE_ROBE = armor("soulweave_robe", SOULWEAVE_ARMOR, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> SOULWEAVE_PANTS = armor("soulweave_pants", SOULWEAVE_ARMOR, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> SOULWEAVE_SHOES = armor("soulweave_shoes", SOULWEAVE_ARMOR, ArmorType.BOOTS);
    public static final DeferredItem<Item> BLOODWEAVE_HOOD = armor("bloodweave_hood", BLOODWEAVE_ARMOR, ArmorType.HELMET);
    public static final DeferredItem<Item> BLOODWEAVE_ROBE = armor("bloodweave_robe", BLOODWEAVE_ARMOR, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> BLOODWEAVE_PANTS = armor("bloodweave_pants", BLOODWEAVE_ARMOR, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> BLOODWEAVE_SHOES = armor("bloodweave_shoes", BLOODWEAVE_ARMOR, ArmorType.BOOTS);
    public static final DeferredItem<Item> LIVINGMETAL_HELMET = armor("livingmetal_helmet", LIVINGMETAL_ARMOR, ArmorType.HELMET);
    public static final DeferredItem<Item> LIVINGMETAL_CHESTPLATE = armor("livingmetal_chestplate", LIVINGMETAL_ARMOR, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> LIVINGMETAL_LEGGINGS = armor("livingmetal_leggings", LIVINGMETAL_ARMOR, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> LIVINGMETAL_BOOTS = armor("livingmetal_boots", LIVINGMETAL_ARMOR, ArmorType.BOOTS);
    public static final DeferredItem<Item> BIOMASS_HELMET = armor("biomass_helmet", BIOMASS_ARMOR, ArmorType.HELMET);
    public static final DeferredItem<Item> BIOMASS_CHESTPLATE = armor("biomass_chestplate", BIOMASS_ARMOR, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> BIOMASS_LEGGINGS = armor("biomass_leggings", BIOMASS_ARMOR, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> BIOMASS_BOOTS = armor("biomass_boots", BIOMASS_ARMOR, ArmorType.BOOTS);
    // spectral armour (no creative tab, made with spectral dye)
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_LEATHER_CAP = spectral("spectral_leather_cap", SPECTRAL_LEATHER_ARMOR, ArmorType.HELMET);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_LEATHER_TUNIC = spectral("spectral_leather_tunic", SPECTRAL_LEATHER_ARMOR, ArmorType.CHESTPLATE);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_LEATHER_PANTS = spectral("spectral_leather_pants", SPECTRAL_LEATHER_ARMOR, ArmorType.LEGGINGS);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_LEATHER_SHOES = spectral("spectral_leather_shoes", SPECTRAL_LEATHER_ARMOR, ArmorType.BOOTS);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_SOULWEAVE_HOOD = spectral("spectral_soulweave_hood", SPECTRAL_SOULWEAVE_ARMOR, ArmorType.HELMET);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_SOULWEAVE_ROBE = spectral("spectral_soulweave_robe", SPECTRAL_SOULWEAVE_ARMOR, ArmorType.CHESTPLATE);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_SOULWEAVE_PANTS = spectral("spectral_soulweave_pants", SPECTRAL_SOULWEAVE_ARMOR, ArmorType.LEGGINGS);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_SOULWEAVE_SHOES = spectral("spectral_soulweave_shoes", SPECTRAL_SOULWEAVE_ARMOR, ArmorType.BOOTS);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_BLOODWEAVE_HOOD = spectral("spectral_bloodweave_hood", SPECTRAL_BLOODWEAVE_ARMOR, ArmorType.HELMET);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_BLOODWEAVE_ROBE = spectral("spectral_bloodweave_robe", SPECTRAL_BLOODWEAVE_ARMOR, ArmorType.CHESTPLATE);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_BLOODWEAVE_PANTS = spectral("spectral_bloodweave_pants", SPECTRAL_BLOODWEAVE_ARMOR, ArmorType.LEGGINGS);
    public static final DeferredItem<SpectralArmorItem> SPECTRAL_BLOODWEAVE_SHOES = spectral("spectral_bloodweave_shoes", SPECTRAL_BLOODWEAVE_ARMOR, ArmorType.BOOTS);
    // tabTools
    public static final DeferredItem<Item> LUNAR_CLOCK = item("lunar_clock", "tools", Item::new,
            p -> p.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    // crucibles (tabDecorations)
    public static final DeferredItem<BlockItem> SOUL_CRUCIBLE = blockItem(HSBlocks.SOUL_CRUCIBLE, "decorations");
    public static final DeferredItem<BlockItem> BLOOD_CRUCIBLE = blockItem(HSBlocks.BLOOD_CRUCIBLE, "decorations");
    // altars (tabDecorations)
    public static final DeferredItem<BlockItem> ALTAR_OF_SOULS = blockItem(HSBlocks.ALTAR_OF_SOULS, "decorations");
    public static final DeferredItem<BlockItem> ALTAR_OF_BLOOD = blockItem(HSBlocks.ALTAR_OF_BLOOD, "decorations");
    // blocks (tabBlock)
    public static final DeferredItem<BlockItem> SOULWEAVE_CLOTH = blockItem(HSBlocks.SOULWEAVE_CLOTH, "blocks");
    public static final DeferredItem<BlockItem> BLOODWEAVE_CLOTH = blockItem(HSBlocks.BLOODWEAVE_CLOTH, "blocks");
    public static final DeferredItem<BlockItem> LIVINGMETAL_BLOCK = blockItem(HSBlocks.LIVINGMETAL_BLOCK, "blocks");
    public static final DeferredItem<BlockItem> BIOMASS_BLOCK = blockItem(HSBlocks.BIOMASS_BLOCK, "blocks");
    public static final DeferredItem<BlockItem> SPECTRAL_WOOL = blockItem(HSBlocks.SPECTRAL_WOOL, "blocks");
    public static final DeferredItem<BlockItem> CREEP_BLOCK = blockItem(HSBlocks.CREEP_BLOCK, "blocks");

    private HSItems() {
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, HarkenScythe.id(path));
    }

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, HarkenScythe.id(name));
    }

    private static ArmorMaterial armor(int durability, int helmet, int chest, int legs, int boots, int enchantability,
            net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound, TagKey<Item> repair, String asset) {
        return new ArmorMaterial(durability, Map.of(ArmorType.HELMET, helmet, ArmorType.CHESTPLATE, chest, ArmorType.LEGGINGS, legs,
                ArmorType.BOOTS, boots, ArmorType.BODY, chest), enchantability, sound, 0.0F, 0.0F, repair, asset(asset));
    }

    private static Item.Properties glint(Item.Properties p) {
        return p.rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private static <I extends Item> DeferredItem<I> item(String name, String tab, Function<Item.Properties, I> factory, UnaryOperator<Item.Properties> props) {
        DeferredItem<I> item = ITEMS.registerItem(name, factory, props);
        if (tab != null) {
            TABS.computeIfAbsent(tab, t -> new ArrayList<>()).add(item);
        }
        return item;
    }

    private static DeferredItem<ScytheItem> scythe(String name, ToolMaterial material, boolean soulReaper) {
        return item(name, "combat", p -> new ScytheItem(material, p), p -> ScytheItem.properties(p, material, soulReaper));
    }

    private static DeferredItem<GlaiveItem> glaive(String name, ToolMaterial material, int bloodPoolChance) {
        return item(name, "combat", p -> new GlaiveItem(material, bloodPoolChance, p), p -> GlaiveItem.properties(p, material));
    }

    private static DeferredItem<KeeperItem> keeper(String name, KeeperItem.Kind kind) {
        return item(name, "misc", p -> new KeeperItem(kind, p), p -> KeeperItem.properties(p, kind));
    }

    private static DeferredItem<AmuletItem> amulet(String name, AmuletItem.Kind kind) {
        return item(name, "transport", p -> new AmuletItem(kind, p), p -> p.stacksTo(1).durability(22).setNoCombineRepair().rarity(Rarity.UNCOMMON));
    }

    private static DeferredItem<Item> armor(String name, ArmorMaterial material, ArmorType type) {
        return item(name, "combat", Item::new, p -> p.humanoidArmor(material, type));
    }

    private static DeferredItem<SpectralArmorItem> spectral(String name, ArmorMaterial material, ArmorType type) {
        return item(name, null, SpectralArmorItem::new, p -> p.humanoidArmor(material, type));
    }

    private static DeferredItem<BlockItem> blockItem(net.neoforged.neoforge.registries.DeferredBlock<? extends Block> block, String tab) {
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(block);
        TABS.computeIfAbsent(tab, t -> new ArrayList<>()).add(item);
        return item;
    }

    /** ItemHSSkull: one item per kind (1.5.2 used damage values), worn on the head with the skull texture, no armour. */
    private static Map<SkullType, DeferredItem<SkullItem>> skulls() {
        Map<SkullType, DeferredItem<SkullItem>> map = new EnumMap<>(SkullType.class);
        for (SkullType type : SkullType.values()) {
            map.put(type, item(type.id() + "_skull", "decorations", p -> new SkullItem(type, p),
                    p -> p.stacksTo(64).component(DataComponents.EQUIPPABLE,
                            Equippable.builder(EquipmentSlot.HEAD).setAsset(asset("skull_" + type.id())).setSwappable(false).build())));
        }
        return map;
    }

    static void register(IEventBus modBus) {
        modBus.addListener(HSItems::tabs);
    }

    private static void tabs(BuildCreativeModeTabContentsEvent event) {
        String tab = null;
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            tab = "combat";
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            accept(event, "tools");
            accept(event, "transport");
            tab = "misc";
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            tab = "materials";
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            tab = "decorations";
        } else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            tab = "blocks";
        }
        if (tab != null) {
            accept(event, tab);
        }
    }

    private static void accept(BuildCreativeModeTabContentsEvent event, String tab) {
        for (DeferredItem<? extends Item> item : TABS.getOrDefault(tab, List.of())) {
            event.accept(item.get());
        }
    }
}
