package betterdungeons;

import betterdungeons.block.CapsuleStationBlock;
import betterdungeons.item.BagItem;
import betterdungeons.item.BigSwordItem;
import betterdungeons.item.CapsuleItem;
import betterdungeons.item.DaggerItem;
import betterdungeons.item.EggItem;
import betterdungeons.item.GuardItem;
import betterdungeons.item.HookShotItem;
import betterdungeons.item.MiscItems;
import betterdungeons.item.RangedItems;
import betterdungeons.item.SpearItem;
import betterdungeons.item.StaffItem;
import betterdungeons.projectile.ProjectileType;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * The items of BetterDungeons.registerItems (itemID 7351 of the pack's config.prop + offset, + 256 as Item(id) added)
 * and the capsule stations (blockID 2001). Every damage value of a 1.5.2 item with sub-items is an item of its own
 * (essences, grenades, arrows, materials, shields, banners, eggs, stations); {@link #OLD_IDS} names them all with their
 * old id and damage, for the dungeon chests (BDLoot) and the creative tabs. Values (damage, uses, enchantability,
 * armour) as in 1.5.2: ItemSword hits for 4 + material bonus (iron 2, diamond 3), daggers 2 less, spears 1 less; the
 * attack damage modifiers subtract the player's base damage of 1.
 */
public final class BDItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BetterDungeons.MOD_ID);
    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BetterDungeons.MOD_ID);

    /** First item id of the mod as the chests and NBT of 1.5.2 know it (7351 + 256). */
    public static final int ITEM_ID = 7351 + 256;
    /** blockID of config.prop: the capsule station (the spawner is 2002). */
    public static final int CAPSULE_STATION_ID = 2001;

    /** One entry: the old id, its damage value (-1: any) and the item; in the order of the 1.5.2 creative tabs. */
    public record OldId(int id, int damage, DeferredItem<? extends Item> item, boolean dungeonTab) {
    }

    public static final List<OldId> OLD_IDS = new ArrayList<>();

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, path));
    }

    /** Repair items of the end and defensive weapons: the material's item or the magic essence (getIsRepairable). */
    static final TagKey<Item> REPAIRS_DIAMOND_WEAPONS = tag("repairs/diamond_weapons");
    static final TagKey<Item> REPAIRS_IRON_WEAPONS = tag("repairs/iron_weapons");
    static final TagKey<Item> REPAIRS_CLOUD_BOOTS = tag("repairs/cloud_boots");

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, name));
    }

    private static Map<ArmorType, Integer> defense(int boots, int legs, int chest, int helmet) {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, boots);
        map.put(ArmorType.LEGGINGS, legs);
        map.put(ArmorType.CHESTPLATE, chest);
        map.put(ArmorType.HELMET, helmet);
        map.put(ArmorType.BODY, chest);
        return map;
    }

    /** ItemArmorTurtle: EnumArmorMaterial.IRON (2/6/5/2, enchantability 9, iron repairs), textures turtle_1/turtle_2. */
    static final ArmorMaterial TURTLE = new ArmorMaterial(15, defense(2, 5, 6, 2), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F,
            ItemTags.REPAIRS_IRON_ARMOR, asset("turtle"));
    /** Scouter and cloud boots: EnumArmorMaterial.CLOTH (1 point, enchantability 15, leather repairs), texture cloud_1. */
    static final ArmorMaterial CLOUD = new ArmorMaterial(5, defense(1, 2, 3, 1), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F,
            ItemTags.REPAIRS_LEATHER_ARMOR, asset("cloud"));
    static final ArmorMaterial CLOUD_BOOTS_MATERIAL = new ArmorMaterial(5, defense(1, 2, 3, 1), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F,
            REPAIRS_CLOUD_BOOTS, asset("cloud"));

    private static <I extends Item> DeferredItem<I> add(int offset, int damage, String name, Function<Item.Properties, ? extends I> factory,
            UnaryOperator<Item.Properties> properties) {
        DeferredItem<I> item = ITEMS.registerItem(name, factory, properties);
        OLD_IDS.add(new OldId(ITEM_ID + offset, damage, item, offset == 0));
        return item;
    }

    private static DeferredItem<Item> simple(int offset, int damage, String name, UnaryOperator<Item.Properties> properties) {
        return add(offset, damage, name, Item::new, properties);
    }

    /** ItemSword of 1.5.2: the vanilla material with the given total damage and 2 uses per block (WEAPON 1 per hit). */
    private static Item.Properties sword(Item.Properties p, ToolMaterial material, int damage, int durability, int usesPerHit, TagKey<Item> repair) {
        return p.sword(material, damage - 1.0F - material.attackDamageBonus(), -2.4F).durability(durability).repairable(repair)
                .component(DataComponents.WEAPON, new Weapon(usesPerHit));
    }

    /** EnumAction.block of 1.5.2: blocking halves the damage, (1 + damage) / 2 is taken, from every side. */
    static Item.Properties blocking(Item.Properties p) {
        return p.component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0.0F, 1.0F,
                List.of(new BlocksAttacks.DamageReduction(180.0F, Optional.empty(), -0.5F, 0.5F)),
                new BlocksAttacks.ItemDamageFunction(1.0E9F, 0.0F, 0.0F), Optional.empty(), Optional.empty(), Optional.empty()));
    }

    /** getDamageVsEntity of a plain item (staffs, the magic pickaxe): attack damage in the main hand. */
    private static Item.Properties damage(Item.Properties p, int damage) {
        return p.attributes(ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage - 1.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build());
    }

    /** ItemBigSword.onUpdate: landMovementFactor - 0.01 while held, on top of the sword's damage. */
    private static Item.Properties bigSword(Item.Properties p, ToolMaterial material, int damage, int durability, TagKey<Item> repair) {
        Item.Properties props = sword(p, material, damage, durability, 1, repair);
        return props.attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage - 1.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.MOVEMENT_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "big_sword"), -0.01,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
    }

    // ---------------------------------------------------------------- items in the order of registerItems

    public static final String[] ELEMENTS = {"water", "poison", "ice", "fire", "dark", "electric", "wind", "life"};

    public static final DeferredItem<BagItem> BAG = add(1, -1, "bag", p -> new BagItem(p, false), p -> p.stacksTo(1));
    public static final DeferredItem<BagItem> QUIVER = add(2, -1, "quiver", p -> new BagItem(p, true), p -> p.stacksTo(1));
    /** ItemTotem: the essences 0..7 (elements) and 8 magic essence. */
    public static final List<DeferredItem<Item>> ESSENCES = new ArrayList<>();
    public static final List<DeferredItem<RangedItems.Grenade>> GRENADES = new ArrayList<>();

    static {
        for (int i = 0; i < 8; i++) {
            ESSENCES.add(simple(3, i, ELEMENTS[i] + "_essence", p -> p));
        }
        ESSENCES.add(simple(3, 8, "magic_essence", p -> p));
        for (int i = 0; i < 8; i++) {
            ProjectileType type = ProjectileType.byId(i);
            GRENADES.add(add(4, i, ELEMENTS[i] + "_grenade", p -> new RangedItems.Grenade(p, type), p -> p));
        }
    }

    /** ItemBowBase: an ItemBow with 500 uses. */
    public static final DeferredItem<BowItem> LONG_BOW = add(5, -1, "long_bow", BowItem::new, p -> p.durability(500).enchantable(1));
    public static final List<DeferredItem<Item>> MAGIC_ARROWS = new ArrayList<>();

    static {
        for (int i = 0; i < 8; i++) {
            MAGIC_ARROWS.add(simple(6, i, ELEMENTS[i] + "_arrow", p -> p));
        }
    }

    public static final DeferredItem<RangedItems.Pistol> REVOLVER = add(7, -1, "revolver", RangedItems.Pistol::new,
            p -> p.stacksTo(1).durability(RangedItems.Pistol.COOLDOWN).rarity(Rarity.RARE));
    public static final DeferredItem<Item> BULLET = simple(8, -1, "bullet", p -> p);
    public static final DeferredItem<CapsuleItem> CAPSULE = add(9, -1, "capsule", CapsuleItem::new, p -> p.stacksTo(1));

    private static DeferredItem<StaffItem> staff(int offset, String name, StaffItem.Element element, int damage, int durability) {
        return add(offset, -1, name, p -> new StaffItem(p, element), p -> damage(p.stacksTo(1).durability(durability).rarity(Rarity.RARE), damage));
    }

    // getMaxDamage() overrides beat setMaxDamage: fire / poison / life 1024 (ice 1024 set), water 400, wind 150, dark and electric 100
    public static final DeferredItem<StaffItem> STAFF_LIFE = staff(10, "staff_life", StaffItem.Element.LIFE, 2, 1024);
    public static final DeferredItem<StaffItem> STAFF_WATER = staff(11, "staff_water", StaffItem.Element.WATER, 4, 400);
    public static final DeferredItem<StaffItem> STAFF_POISON = staff(12, "staff_poison", StaffItem.Element.POISON, 2, 1024);
    public static final DeferredItem<StaffItem> STAFF_ICE = staff(13, "staff_ice", StaffItem.Element.ICE, 4, 1024);
    public static final DeferredItem<StaffItem> STAFF_FIRE = staff(14, "staff_fire", StaffItem.Element.FIRE, 2, 1024);
    public static final DeferredItem<StaffItem> STAFF_DARK = staff(15, "staff_dark", StaffItem.Element.DARK, 4, 100);
    public static final DeferredItem<StaffItem> STAFF_ELECTRIC = staff(16, "staff_electric", StaffItem.Element.ELECTRIC, 4, 100);
    public static final DeferredItem<StaffItem> STAFF_WIND = staff(17, "staff_wind", StaffItem.Element.WIND, 4, 150);

    /** ItemMaterial: 0 staff, 1 turtle scale, 2 handle. */
    public static final DeferredItem<Item> STAFF = simple(18, 0, "staff", p -> p);
    public static final DeferredItem<Item> TURTLE_SCALE = simple(18, 1, "turtle_scale", p -> p);
    public static final DeferredItem<Item> HANDLE = simple(18, 2, "handle", p -> p);

    public static final DeferredItem<DaggerItem.Ninja> NINJA_DAGGER = add(19, -1, "ninja_dagger", p -> new DaggerItem.Ninja(p, 5),
            p -> sword(p, ToolMaterial.DIAMOND, 5, 2048, 1, REPAIRS_DIAMOND_WEAPONS).rarity(Rarity.EPIC));
    public static final DeferredItem<GuardItem> WALKER_SWORD = add(20, -1, "walker_sword",
            p -> new GuardItem(p, MobEffects.RESISTANCE, 2, 0, false, true),
            p -> blocking(sword(p, ToolMaterial.DIAMOND, 7, 2024, 1, REPAIRS_DIAMOND_WEAPONS).rarity(Rarity.EPIC)));
    public static final DeferredItem<BigSwordItem.Monking> MONKING_SWORD = add(21, -1, "monking_sword", p -> new BigSwordItem.Monking(p, 6),
            p -> bigSword(p, ToolMaterial.DIAMOND, 6, 2048, ItemTags.DIAMOND_TOOL_MATERIALS));
    public static final DeferredItem<MiscItems.MagicPickaxe> MAGIC_PICKAXE = add(22, -1, "magic_pickaxe", MiscItems.MagicPickaxe::new,
            p -> damage(p.stacksTo(1).durability(2024), 10000).component(DataComponents.TOOL, new Tool(List.of(
                    Tool.Rule.overrideSpeed(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK).getOrThrow(BlockTags.MINEABLE_WITH_PICKAXE),
                            15.0F)), 8.0F, 1, true)));
    public static final DeferredItem<MiscItems.GoldenFeather> GOLDEN_FEATHER = add(23, -1, "golden_feather", MiscItems.GoldenFeather::new,
            p -> p.stacksTo(1).durability(385));
    public static final DeferredItem<MiscItems.CursedBone> CURSED_BONE = add(24, -1, "cursed_bone", MiscItems.CursedBone::new,
            p -> p.stacksTo(1).durability(8));
    public static final DeferredItem<HookShotItem> HOOKSHOT = add(25, -1, "hookshot", p -> new HookShotItem(p, 0), p -> p.stacksTo(1));
    public static final DeferredItem<HookShotItem> LONGSHOT = add(26, -1, "longshot", p -> new HookShotItem(p, 1), p -> p.stacksTo(1));
    public static final DeferredItem<HookShotItem> MANUAL_HOOK = add(27, -1, "manual_hook", p -> new HookShotItem(p, 2), p -> p.stacksTo(1));
    public static final DeferredItem<MiscItems.ScrollOfEscape> SCROLL_OF_ESCAPE = add(28, -1, "scroll_of_escape", MiscItems.ScrollOfEscape::new, p -> p);
    public static final DeferredItem<Item> SCOUTER = add(29, -1, "scouter", Item::new, p -> p.humanoidArmor(CLOUD, ArmorType.HELMET).durability(650));
    public static final DeferredItem<Item> CLOUD_BOOTS = add(30, -1, "cloud_boots", Item::new,
            p -> p.humanoidArmor(CLOUD_BOOTS_MATERIAL, ArmorType.BOOTS).durability(350));
    // EnumArmorMaterial.DIAMOND.getDurability(type) + 100: 363, 528, 495, 429 plus 100
    public static final DeferredItem<Item> TURTLE_HELMET = add(31, -1, "turtle_helmet", Item::new, p -> p.humanoidArmor(TURTLE, ArmorType.HELMET).durability(463));
    public static final DeferredItem<Item> TURTLE_PLATE = add(32, -1, "turtle_plate", Item::new, p -> p.humanoidArmor(TURTLE, ArmorType.CHESTPLATE).durability(628));
    public static final DeferredItem<Item> TURTLE_PANTS = add(33, -1, "turtle_pants", Item::new, p -> p.humanoidArmor(TURTLE, ArmorType.LEGGINGS).durability(595));
    public static final DeferredItem<Item> TURTLE_BOOTS = add(34, -1, "turtle_boots", Item::new, p -> p.humanoidArmor(TURTLE, ArmorType.BOOTS).durability(529));
    public static final DeferredItem<HookShotItem> SPIDER_HOOK = add(35, -1, "spider_hook", p -> new HookShotItem(p, 3), p -> p.stacksTo(1));

    /** ItemShied / ItemBanner: 13 kinds each (texture s0..s12); getSubItems listed shields 0..13 and banners 0..1. */
    public static final String[] KINDS = {"end", "nether", "dwarf", "zombie", "wooden", "pirate", "shadows", "round_wood", "green", "ensaimada",
            "squid", "minotaur", "colorful"};
    public static final List<DeferredItem<GuardItem>> SHIELDS = new ArrayList<>();
    public static final List<DeferredItem<GuardItem>> BANNERS = new ArrayList<>();

    static {
        for (int i = 0; i < KINDS.length; i++) {
            SHIELDS.add(add(36, i, KINDS[i] + "_shield", p -> new GuardItem(p, MobEffects.RESISTANCE, 2, 1, true, false), BDItems::blocking));
        }
        for (int i = 0; i < KINDS.length; i++) {
            BANNERS.add(add(37, i, KINDS[i] + "_banner", p -> new GuardItem(p, MobEffects.SPEED, 2, 1, false, false), BDItems::blocking));
        }
    }

    public static final DeferredItem<SpearItem.Dwarf> DWARF_SPEAR = add(38, -1, "dwarf_spear", p -> new SpearItem.Dwarf(p, 6),
            p -> sword(p, ToolMaterial.DIAMOND, 6, 2048, 2, ItemTags.DIAMOND_TOOL_MATERIALS).rarity(Rarity.EPIC));
    public static final DeferredItem<DaggerItem> IRON_DAGGER = add(39, -1, "iron_dagger", p -> new DaggerItem(p, 4),
            p -> sword(p, ToolMaterial.IRON, 4, 250, 1, ItemTags.IRON_TOOL_MATERIALS));
    public static final DeferredItem<DaggerItem> DIAMOND_DAGGER = add(40, -1, "diamond_dagger", p -> new DaggerItem(p, 5),
            p -> sword(p, ToolMaterial.DIAMOND, 5, 1561, 1, ItemTags.DIAMOND_TOOL_MATERIALS));
    public static final DeferredItem<SpearItem> IRON_SPEAR = add(41, -1, "iron_spear", p -> new SpearItem(p, 5),
            p -> sword(p, ToolMaterial.IRON, 5, 250, 2, ItemTags.IRON_TOOL_MATERIALS));
    public static final DeferredItem<SpearItem> DIAMOND_SPEAR = add(42, -1, "diamond_spear", p -> new SpearItem(p, 6),
            p -> sword(p, ToolMaterial.DIAMOND, 6, 1561, 2, ItemTags.DIAMOND_TOOL_MATERIALS));
    public static final DeferredItem<BigSwordItem> IRON_BIGSWORD = add(43, -1, "iron_bigsword", p -> new BigSwordItem(p, 6),
            p -> bigSword(p, ToolMaterial.IRON, 6, 250, ItemTags.IRON_TOOL_MATERIALS));
    public static final DeferredItem<BigSwordItem> DIAMOND_BIGSWORD = add(44, -1, "diamond_bigsword", p -> new BigSwordItem(p, 7),
            p -> bigSword(p, ToolMaterial.DIAMOND, 7, 1561, ItemTags.DIAMOND_TOOL_MATERIALS));
    public static final DeferredItem<BigSwordItem.Earth> EARTH_SWORD = add(45, -1, "earth_sword", p -> new BigSwordItem.Earth(p, 7),
            p -> bigSword(p, ToolMaterial.DIAMOND, 7, 2048, ItemTags.DIAMOND_TOOL_MATERIALS));
    public static final DeferredItem<GuardItem> MOON_SWORD = add(46, -1, "moon_sword", p -> new GuardItem(p, MobEffects.RESISTANCE, 2, 0, false, false),
            p -> blocking(sword(p, ToolMaterial.IRON, 6, 2048, 1, REPAIRS_IRON_WEAPONS).rarity(Rarity.RARE)));
    public static final DeferredItem<GuardItem> DEFENSE_SWORD = add(47, -1, "defense_sword",
            p -> new GuardItem(p, MobEffects.RESISTANCE, 2, 0, false, false),
            p -> blocking(sword(p, ToolMaterial.DIAMOND, 7, 2048, 1, REPAIRS_DIAMOND_WEAPONS).rarity(Rarity.RARE)));
    public static final DeferredItem<DaggerItem.Trickster> TRICKSTER_DAGGER = add(48, -1, "trickster_dagger", p -> new DaggerItem.Trickster(p, 5),
            p -> sword(p, ToolMaterial.DIAMOND, 5, 2048, 1, REPAIRS_DIAMOND_WEAPONS).rarity(Rarity.EPIC));
    public static final DeferredItem<MiscItems.Controller> CONTROLLER = add(49, -1, "controller", MiscItems.Controller::new, p -> p);
    public static final DeferredItem<MiscItems.PathMarker> PATH_MARKER = add(50, -1, "path_marker", MiscItems.PathMarker::new, p -> p);

    // ---------------------------------------------------------------- spawn eggs (ItemEggBD, offset 0)

    /** damage value, 1.5.2 entity name, item name; 514 (the wyvern) was not in the creative tab. */
    static final Object[][] EGG_TABLE = {
            {100, "ArmoredSkeleton"}, {101, "ArmoredZombie"}, {102, "Pirate"}, {103, "Specter"}, {104, "Dwarf"}, {105, "Walker"},
            {106, "Goblin"}, {107, "SnowGoblin"}, {108, "ArmoredPigZombie"}, {109, "ArmoredEnderman"}, {110, "Monkey"}, {111, "Triton"},
            {112, "Gremlin"}, {113, "GolemSmall"}, {114, "OgreArmored"}, {115, "MinotaurArmored"}, {116, "FriendlyHuman"},
            {500, "SpiderBoss"}, {501, "Monking"}, {502, "TurtleBoss"}, {503, "IceBull"}, {504, "GolemBoss"}, {505, "PirateBoss"},
            {506, "WalkerBoss"}, {507, "Necromancer"}, {508, "Liche"}, {509, "PigMage"}, {510, "SpecterBoss"}, {511, "GremlinBoss"},
            {512, "MinotaurBoss"}, {513, "goblinBoss"}, {514, "Wyvern"}};
    public static final List<DeferredItem<EggItem>> EGGS = new ArrayList<>();

    static {
        for (Object[] egg : EGG_TABLE) {
            String mob = (String) egg[1];
            String path = betterdungeons.api.BDEntityIds.NAMES.get(mob);
            EGGS.add(add(0, (Integer) egg[0], "egg_" + path, p -> new EggItem(p, mob), p -> p));
        }
    }

    // ---------------------------------------------------------------- capsule stations (block 2001, metadata 0..4)

    public static final String[] STATIONS = {"editable", "storage", "small", "medium", "big"};
    public static final List<DeferredBlock<CapsuleStationBlock>> STATION_BLOCKS = new ArrayList<>();
    public static final List<DeferredItem<BlockItem>> STATION_ITEMS = new ArrayList<>();

    static {
        for (int i = 0; i < STATIONS.length; i++) {
            int kind = i;
            // Material.cake (pistons break it), hardness 3.5, resistance 2.0 (explosion resistance 2.0 * 3 / 5)
            DeferredBlock<CapsuleStationBlock> block = BetterDungeons.BLOCKS.registerBlock("capsule_station_" + STATIONS[i],
                    p -> new CapsuleStationBlock(p, kind), p -> p.mapColor(MapColor.NONE).strength(3.5F, 1.2F).sound(SoundType.STONE)
                            .pushReaction(PushReaction.POPPED));
            STATION_BLOCKS.add(block);
            DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(block);
            STATION_ITEMS.add(item);
            OLD_IDS.add(new OldId(CAPSULE_STATION_ID, i, item, false));
        }
    }

    // ---------------------------------------------------------------- creative tabs

    /** DungeonsItemsTab "adventureItems" (icon: the Monking sword) with the capsule stations of CreativeTabs.tabTransport. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS_TAB = TABS.register("adventure_items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.adventureItems"))
            .icon(() -> new ItemStack(MONKING_SWORD.get()))
            .displayItems((parameters, output) -> {
                for (OldId entry : OLD_IDS) {
                    // getSubItems of ItemBanner listed only the first two banners
                    if (!entry.dungeonTab() && !(BANNERS.contains(entry.item()) && BANNERS.indexOf(entry.item()) > 1)) {
                        output.accept(entry.item().get());
                    }
                }
            })
            .build());
    /** DungeonsTab "dungeonGenerator": the spawn eggs (1.5.2 showed the dungeon eggs first, with a dungeon as icon). */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EGG_TAB = TABS.register("dungeon_generator", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dungeonGenerator"))
            .icon(() -> new ItemStack(EGGS.get(0).get()))
            .displayItems((parameters, output) -> {
                for (DeferredItem<EggItem> egg : EGGS) {
                    if (!egg.get().mob().equals("Wyvern")) {
                        output.accept(egg.get());
                    }
                }
            })
            .build());

    private BDItems() {
    }

    public static boolean isMagicArrow(ItemStack stack) {
        for (DeferredItem<Item> arrow : MAGIC_ARROWS) {
            if (stack.is(arrow.get())) {
                return true;
            }
        }
        return false;
    }

    /** The element (0..7, ItemArrowMagic damage) of a magic arrow, -1 for others. */
    public static int arrowType(ItemStack stack) {
        for (int i = 0; i < MAGIC_ARROWS.size(); i++) {
            if (stack.is(MAGIC_ARROWS.get(i).get())) {
                return i;
            }
        }
        return -1;
    }

    /** new ItemStack(BetterDungeons.shied, 1, kind) of the mobs. */
    public static ItemStack shield(int kind) {
        return new ItemStack(SHIELDS.get(kind >= 0 && kind < SHIELDS.size() ? kind : 0).get());
    }

    /** new ItemStack(BetterDungeons.banner, 1, kind) of the mobs. */
    public static ItemStack banner(int kind) {
        return new ItemStack(BANNERS.get(kind >= 0 && kind < BANNERS.size() ? kind : 0).get());
    }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
        for (OldId entry : OLD_IDS) {
            String id = BetterDungeons.MOD_ID + ":" + entry.item().getId().getPath();
            if (entry.damage() < 0) {
                OldGameIds.registerItem(entry.id(), id);
            } else {
                OldGameIds.registerItem(entry.id(), entry.damage(), id);
            }
        }
    }
}
