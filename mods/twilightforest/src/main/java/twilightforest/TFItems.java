package twilightforest;

import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.item.FieryPickItem;
import twilightforest.item.MazebreakerPickItem;
import twilightforest.item.MinotaurAxeItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * TFItems (Teil 24f): the materials, tools, armour and food of 1.5.2 with the item ids of the pack's TwilightForest.cfg
 * (+ 256). The special items (scepters, maps, charms ...) are in their own classes of {@code twilightforest.item}.
 * The tools and armour are crafted with the enchantments of TFRecipes.addEnchantedRecipe (recipe results with the
 * enchantment component), and show them in the creative tab (getSubItems).
 */
public final class TFItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TwilightForest.MOD_ID);
    /** The creative tab stacks (getSubItems: the tools and armour enchanted as crafted). */
    static final List<Function<HolderLookup.Provider, ItemStack>> TAB_ITEMS = new ArrayList<>();
    private static final List<Object[]> OLD_ITEMS = new ArrayList<>();

    private TFItems() {
    }

    // ---------------------------------------------------------------- materials (EnumHelper.addToolMaterial / addArmorMaterial)
    public static final TagKey<Item> IRONWOOD_REPAIR = tag("repairs_ironwood");
    public static final TagKey<Item> STEELEAF_REPAIR = tag("repairs_steeleaf");
    public static final TagKey<Item> FIERY_REPAIR = tag("repairs_fiery");
    public static final TagKey<Item> NAGA_REPAIR = tag("repairs_naga_scale");

    /** ("IRONWOOD", 2, 512, 6.5F, 2, 25), ("FIERY", 4, 1024, 9.0F, 4, 10), ("STEELEAF", 3, 131, 8.0F, 3, 9). */
    public static final ToolMaterial IRONWOOD_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 512, 6.5F, 2.0F, 25, IRONWOOD_REPAIR);
    public static final ToolMaterial FIERY_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1024, 9.0F, 4.0F, 10, FIERY_REPAIR);
    public static final ToolMaterial STEELEAF_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 131, 8.0F, 3.0F, 9, STEELEAF_REPAIR);
    /** EnumToolMaterial.EMERALD (3, 1561, 8.0F, 3, 10), repaired with diamonds. */
    public static final ToolMaterial EMERALD_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.0F, 3.0F, 10,
            ItemTags.DIAMOND_TOOL_MATERIALS);

    /** NAGA_SCALE (21, {2, 7, 6, 3}, 15), IRONWOOD (20, {2, 7, 5, 2}, 15), FIERY (25, {4, 9, 7, 4}, 10). */
    public static final ArmorMaterial NAGA_ARMOR = armor("naga_scale", 21, 2, 7, 6, 3, 15, NAGA_REPAIR);
    public static final ArmorMaterial IRONWOOD_ARMOR = armor("ironwood", 20, 2, 7, 5, 2, 15, IRONWOOD_REPAIR);
    public static final ArmorMaterial FIERY_ARMOR = armor("fiery", 25, 4, 9, 7, 4, 10, FIERY_REPAIR);
    /** The steeleaf armour of TFItems is made of ARMOR_IRONWOOD (ARMOR_STEELEAF is never used); its own look and repair item. */
    public static final ArmorMaterial STEELEAF_ARMOR = armor("steeleaf", 20, 2, 7, 5, 2, 15, STEELEAF_REPAIR);

    // ---------------------------------------------------------------- simple items (ItemTF: uncommon, makeRare: rare)
    public static final DeferredItem<Item> NAGA_SCALE = simple("naga_scale", 7300, Rarity.UNCOMMON);
    public static final DeferredItem<Item> IRONWOOD_RAW = simple("raw_ironwood", 7283, Rarity.UNCOMMON);
    public static final DeferredItem<Item> IRONWOOD_INGOT = simple("ironwood_ingot", 7279, Rarity.UNCOMMON);
    public static final DeferredItem<Item> FIERY_BLOOD = simple("fiery_blood", 7265, Rarity.RARE);
    public static final DeferredItem<Item> FIERY_INGOT = simple("fiery_ingot", 7268, Rarity.RARE);
    public static final DeferredItem<Item> STEELEAF_INGOT = simple("steeleaf_ingot", 7314, Rarity.UNCOMMON);
    public static final DeferredItem<Item> CHARM_OF_LIFE_2 = simple("charm_of_life_2", 7258, Rarity.UNCOMMON);
    public static final DeferredItem<Item> CHARM_OF_KEEPING_2 = simple("charm_of_keeping_2", 7255, Rarity.UNCOMMON);
    public static final DeferredItem<Item> CHARM_OF_KEEPING_3 = simple("charm_of_keeping_3", 7256, Rarity.UNCOMMON);
    /** armorShards and knightMetal: items of 1.5.2 without a name (never registered by TFItems), shards smelt to the metal. */
    public static final DeferredItem<Item> ARMOR_SHARDS = simple("armor_shards", 7251, Rarity.UNCOMMON);
    public static final DeferredItem<Item> KNIGHT_METAL = simple("knight_metal", 7286, Rarity.UNCOMMON);

    // ---------------------------------------------------------------- food (ItemTFFood)
    /** hydraChop: 18, 2.0F, wolf food, regeneration (5 ticks) always; looks like a cooked porkchop. */
    public static final DeferredItem<Item> HYDRA_CHOP = food("hydra_chop", 7273, p -> p.food(food(18, 2.0F),
            Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 5, 0), 1.0F)).build()));
    /** meefStroganoff: ItemTFFood(8) = ItemFood(8, 0.6F) as soup (stacks to 1, gives the bowl back). */
    public static final DeferredItem<Item> MEEF_STROGANOFF = food("meef_stroganoff", 7297,
            p -> p.food(food(8, 0.6F)).stacksTo(1).usingConvertsTo(Items.BOWL));
    public static final DeferredItem<Item> MAZE_WAFER = food("maze_wafer", 7293, p -> p.food(food(4, 0.6F)));
    public static final DeferredItem<Item> EXPERIMENT_115 = food("experiment_115", 7263, p -> p.food(food(4, 0.3F)));

    // ---------------------------------------------------------------- naga armour (ItemTFNagaArmor: common, no sub items)
    public static final DeferredItem<Item> NAGA_CHESTPLATE = armor("naga_chestplate", 7305, NAGA_ARMOR, ArmorType.CHESTPLATE, Rarity.COMMON, false,
            Enchantments.FIRE_PROTECTION, 3);
    public static final DeferredItem<Item> NAGA_LEGGINGS = armor("naga_leggings", 7287, NAGA_ARMOR, ArmorType.LEGGINGS, Rarity.COMMON, false,
            Enchantments.PROTECTION, 3);

    // ---------------------------------------------------------------- ironwood
    public static final DeferredItem<Item> IRONWOOD_HELMET = armor("ironwood_helmet", 7277, IRONWOOD_ARMOR, ArmorType.HELMET, Rarity.UNCOMMON, true,
            Enchantments.AQUA_AFFINITY, 1);
    public static final DeferredItem<Item> IRONWOOD_CHESTPLATE = armor("ironwood_chestplate", 7282, IRONWOOD_ARMOR, ArmorType.CHESTPLATE,
            Rarity.UNCOMMON, true, Enchantments.PROTECTION, 1);
    public static final DeferredItem<Item> IRONWOOD_LEGGINGS = armor("ironwood_leggings", 7280, IRONWOOD_ARMOR, ArmorType.LEGGINGS, Rarity.UNCOMMON,
            true, Enchantments.PROTECTION, 1);
    public static final DeferredItem<Item> IRONWOOD_BOOTS = armor("ironwood_boots", 7276, IRONWOOD_ARMOR, ArmorType.BOOTS, Rarity.UNCOMMON, true,
            Enchantments.FEATHER_FALLING, 1);
    /** ItemSword: 4 + damage; ItemSpade 1 +, ItemPickaxe 2 +, ItemAxe 3 + damage; ItemHoe the fist (1). */
    public static final DeferredItem<Item> IRONWOOD_SWORD = tool("ironwood_sword", 7285, Item::new, p -> p.sword(IRONWOOD_TOOL, 3.0F, -2.4F),
            Rarity.COMMON, Enchantments.KNOCKBACK, 1);
    public static final DeferredItem<Item> IRONWOOD_SHOVEL = tool("ironwood_shovel", 7284, Item::new, p -> p.shovel(IRONWOOD_TOOL, 0.0F, -3.0F),
            Rarity.COMMON, Enchantments.UNBREAKING, 1);
    public static final DeferredItem<Item> IRONWOOD_PICKAXE = tool("ironwood_pickaxe", 7281, Item::new, p -> p.pickaxe(IRONWOOD_TOOL, 1.0F, -2.8F),
            Rarity.COMMON, Enchantments.EFFICIENCY, 1);
    public static final DeferredItem<Item> IRONWOOD_AXE = tool("ironwood_axe", 7275, Item::new, p -> p.axe(IRONWOOD_TOOL, 2.0F, -3.0F),
            Rarity.COMMON, Enchantments.FORTUNE, 1);
    public static final DeferredItem<Item> IRONWOOD_HOE = tool("ironwood_hoe", 7278, Item::new, p -> p.hoe(IRONWOOD_TOOL, -2.0F, 0.0F),
            Rarity.COMMON, null, 0);

    // ---------------------------------------------------------------- fiery (rare)
    public static final DeferredItem<Item> FIERY_HELMET = armor("fiery_helmet", 7267, FIERY_ARMOR, ArmorType.HELMET, Rarity.RARE, true, TFEnchantment.FIERY_AURA, 2);
    public static final DeferredItem<Item> FIERY_CHESTPLATE = armor("fiery_chestplate", 7271, FIERY_ARMOR, ArmorType.CHESTPLATE, Rarity.RARE, true,
            TFEnchantment.FIERY_AURA, 2);
    public static final DeferredItem<Item> FIERY_LEGGINGS = armor("fiery_leggings", 7269, FIERY_ARMOR, ArmorType.LEGGINGS, Rarity.RARE, true,
            TFEnchantment.FIERY_AURA, 2);
    public static final DeferredItem<Item> FIERY_BOOTS = armor("fiery_boots", 7266, FIERY_ARMOR, ArmorType.BOOTS, Rarity.RARE, true, TFEnchantment.FIERY_AURA, 2);
    /** The recipe gives fire aspect II, the creative tab fire aspect I (getSubItems). */
    public static final DeferredItem<Item> FIERY_SWORD = tool("fiery_sword", 7272, Item::new, p -> p.sword(FIERY_TOOL, 3.0F, -2.4F).fireResistant(),
            Rarity.RARE, Enchantments.FIRE_ASPECT, 1);
    /** ItemTFFieryPick: smelts what it mines, sets what it hits on fire, harvests obsidian. */
    public static final DeferredItem<Item> FIERY_PICKAXE = tool("fiery_pickaxe", 7270, FieryPickItem::new,
            p -> p.pickaxe(FIERY_TOOL, 1.0F, -2.8F).fireResistant(), Rarity.RARE, null, 0);

    // ---------------------------------------------------------------- steeleaf
    public static final DeferredItem<Item> STEELEAF_HELMET = armor("steeleaf_helmet", 7312, STEELEAF_ARMOR, ArmorType.HELMET, Rarity.UNCOMMON, true,
            Enchantments.PROJECTILE_PROTECTION, 2);
    public static final DeferredItem<Item> STEELEAF_CHESTPLATE = armor("steeleaf_chestplate", 7317, STEELEAF_ARMOR, ArmorType.CHESTPLATE,
            Rarity.UNCOMMON, true, Enchantments.BLAST_PROTECTION, 2);
    public static final DeferredItem<Item> STEELEAF_LEGGINGS = armor("steeleaf_leggings", 7315, STEELEAF_ARMOR, ArmorType.LEGGINGS, Rarity.UNCOMMON,
            true, Enchantments.FIRE_PROTECTION, 2);
    public static final DeferredItem<Item> STEELEAF_BOOTS = armor("steeleaf_boots", 7311, STEELEAF_ARMOR, ArmorType.BOOTS, Rarity.UNCOMMON, true,
            Enchantments.FEATHER_FALLING, 2);
    public static final DeferredItem<Item> STEELEAF_SWORD = tool("steeleaf_sword", 7319, Item::new, p -> p.sword(STEELEAF_TOOL, 3.0F, -2.4F),
            Rarity.COMMON, Enchantments.LOOTING, 2);
    public static final DeferredItem<Item> STEELEAF_SHOVEL = tool("steeleaf_shovel", 7318, Item::new, p -> p.shovel(STEELEAF_TOOL, 0.0F, -3.0F),
            Rarity.COMMON, Enchantments.EFFICIENCY, 2);
    public static final DeferredItem<Item> STEELEAF_PICKAXE = tool("steeleaf_pickaxe", 7316, Item::new, p -> p.pickaxe(STEELEAF_TOOL, 1.0F, -2.8F),
            Rarity.COMMON, Enchantments.FORTUNE, 2);
    public static final DeferredItem<Item> STEELEAF_AXE = tool("steeleaf_axe", 7310, Item::new, p -> p.axe(STEELEAF_TOOL, 2.0F, -3.0F),
            Rarity.COMMON, Enchantments.EFFICIENCY, 2);
    public static final DeferredItem<Item> STEELEAF_HOE = tool("steeleaf_hoe", 7313, Item::new, p -> p.hoe(STEELEAF_TOOL, -3.0F, 0.0F),
            Rarity.COMMON, null, 0);

    // ---------------------------------------------------------------- the labyrinth's tools (EMERALD)
    /** ItemTFMinotaurAxe: 4 + 3 damage, + 7 when sprinting, enchantability of gold (22). */
    public static final DeferredItem<Item> MINOTAUR_AXE = tool("minotaur_axe", 7298, MinotaurAxeItem::new,
            p -> p.axe(EMERALD_TOOL, 3.0F, -3.0F).enchantable(22), Rarity.COMMON, null, 0);
    /** ItemTFMazebreakerPick: 16 times as fast on maze stone; efficiency IV, unbreaking III, fortune II in the creative tab. */
    public static final DeferredItem<Item> MAZEBREAKER_PICKAXE = tool("mazebreaker_pickaxe", 7294, MazebreakerPickItem::new,
            p -> p.pickaxe(EMERALD_TOOL, 1.0F, -2.8F), Rarity.COMMON, null, -1);

    // ---------------------------------------------------------------- the special items (twilightforest.item)
    public static final DeferredItem<Item> TWILIGHT_SCEPTER = register("twilight_scepter", 7307, twilightforest.item.TwilightScepterItem::new,
            p -> p.stacksTo(1).durability(99).rarity(Rarity.RARE));
    public static final DeferredItem<Item> LIFEDRAIN_SCEPTER = register("lifedrain_scepter", 7306, twilightforest.item.LifedrainScepterItem::new,
            p -> p.stacksTo(1).durability(99).rarity(Rarity.RARE));
    public static final DeferredItem<Item> ZOMBIE_SCEPTER = register("zombie_scepter", 7308, twilightforest.item.ZombieScepterItem::new,
            p -> p.stacksTo(1).durability(9).rarity(Rarity.RARE));
    public static final DeferredItem<Item> ORE_METER = register("ore_meter", 7303, twilightforest.item.OreMeterItem::new,
            p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> ORE_MAGNET = register("ore_magnet", 7301, twilightforest.item.OreMagnetItem::new,
            p -> p.durability(12).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> CRUMBLE_HORN = register("crumble_horn", 7259, twilightforest.item.CrumbleHornItem::new,
            p -> p.durability(1024).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PEACOCK_FAN = register("peacock_fan", 7304, twilightforest.item.PeacockFanItem::new,
            p -> p.durability(1024).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MOONWORM_QUEEN = register("moonworm_queen", 7299, twilightforest.item.MoonwormQueenItem::new,
            p -> p.durability(256).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> TRANSFORM_POWDER = register("transform_powder", 7322, twilightforest.item.TransformPowderItem::new,
            p -> p.rarity(Rarity.RARE));

    static {
        TAB_ITEMS.add(lookup -> {
            ItemStack stack = new ItemStack(MAZEBREAKER_PICKAXE.get());
            enchant(stack, lookup, Enchantments.EFFICIENCY, 4);
            enchant(stack, lookup, Enchantments.UNBREAKING, 3);
            enchant(stack, lookup, Enchantments.FORTUNE, 2);
            return stack;
        });
    }

    // ---------------------------------------------------------------- helpers

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, TwilightForest.id(name));
    }

    private static ArmorMaterial armor(String name, int durability, int helmet, int chest, int legs, int boots, int enchantability, TagKey<Item> repair) {
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.HELMET, helmet);
        defense.put(ArmorType.CHESTPLATE, chest);
        defense.put(ArmorType.LEGGINGS, legs);
        defense.put(ArmorType.BOOTS, boots);
        defense.put(ArmorType.BODY, chest);
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, TwilightForest.id(name));
        return new ArmorMaterial(durability, defense, enchantability, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, repair, asset);
    }

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    static void old(int oldId, DeferredItem<?> item) {
        OLD_ITEMS.add(new Object[]{oldId + 256, item});
    }

    static <I extends Item> DeferredItem<I> register(String name, int oldId, Function<Item.Properties, I> factory, UnaryOperator<Item.Properties> props) {
        DeferredItem<I> item = ITEMS.registerItem(name, factory, props);
        TAB_ITEMS.add(lookup -> new ItemStack(item.get()));
        if (oldId > 0) {
            old(oldId, item);
        }
        return item;
    }

    private static DeferredItem<Item> simple(String name, int oldId, Rarity rarity) {
        return register(name, oldId, Item::new, p -> p.rarity(rarity));
    }

    private static DeferredItem<Item> food(String name, int oldId, UnaryOperator<Item.Properties> props) {
        return register(name, oldId, Item::new, props);
    }

    private static DeferredItem<Item> armor(String name, int oldId, ArmorMaterial material, ArmorType type, Rarity rarity, boolean enchantedInTab,
            ResourceKey<Enchantment> enchantment, int level) {
        DeferredItem<Item> item = ITEMS.registerItem(name, Item::new, p -> p.humanoidArmor(material, type).rarity(rarity));
        tab(item, enchantedInTab ? enchantment : null, level);
        old(oldId, item);
        return item;
    }

    private static <I extends Item> DeferredItem<I> tool(String name, int oldId, Function<Item.Properties, I> factory, UnaryOperator<Item.Properties> props,
            Rarity rarity, ResourceKey<Enchantment> enchantment, int level) {
        DeferredItem<I> item = ITEMS.registerItem(name, factory, p -> props.apply(p).rarity(rarity));
        if (enchantment != null || level >= 0) {
            tab(item, enchantment, level);
        }
        old(oldId, item);
        return item;
    }

    private static void tab(DeferredItem<? extends Item> item, ResourceKey<Enchantment> enchantment, int level) {
        TAB_ITEMS.add(lookup -> {
            ItemStack stack = new ItemStack(item.get());
            if (enchantment != null) {
                enchant(stack, lookup, enchantment, level);
            }
            return stack;
        });
    }

    public static void enchant(ItemStack stack, HolderLookup.Provider lookup, ResourceKey<Enchantment> enchantment, int level) {
        lookup.lookupOrThrow(Registries.ENCHANTMENT).get(enchantment).ifPresent(h -> stack.enchant((Holder<Enchantment>) h, level));
    }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    static void registerOld() {
        for (Object[] old : OLD_ITEMS) {
            OldGameIds.registerItem((Integer) old[0], ((DeferredItem<?>) old[1]).getId().toString());
        }
    }
}
