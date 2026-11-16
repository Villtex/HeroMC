package natura;

import natura.item.BoneBagItem;
import natura.item.KamaItem;
import natura.item.SeedBagItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Items of Natura. 1.5.2 kept many items as metadata of one id (plantItem, seeds, berries, sticks, bowls, stews);
 * here each is an item of its own. Left out: spawn eggs, boats and imp meat (they come with the entities).
 */
public final class NaturaItems {
    private static DeferredItem<Item> item(String name, Function<Item.Properties, Item> factory, UnaryOperator<Item.Properties> properties) {
        DeferredItem<Item> item = NaturaBlocks.ITEMS.registerItem(name, factory, properties);
        NaturaBlocks.TAB_ITEMS.add(() -> new ItemStack(item.get()));
        return item;
    }

    private static DeferredItem<Item> simple(String name) {
        return item(name, Item::new, p -> p);
    }

    private static FoodProperties food(int nutrition, float saturation, boolean alwaysEdible) {
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
        return (alwaysEdible ? builder.alwaysEdible() : builder).build();
    }

    private static Consumable eat(float seconds, ApplyStatusEffectsConsumeEffect... effects) {
        Consumable.Builder builder = Consumables.defaultFood().consumeSeconds(seconds);
        for (ApplyStatusEffectsConsumeEffect effect : effects) {
            builder.onConsume(effect);
        }
        return builder.build();
    }

    private static ApplyStatusEffectsConsumeEffect effect(Holder<net.minecraft.world.effect.MobEffect> effect, int ticks, float chance) {
        return new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(effect, ticks, 0), chance);
    }

    // ---- crops and plant items (seeds 5877:0-1, plantItem "Food Items" 5899:0-8) ----
    public static final DeferredItem<Item> BARLEY_SEEDS = item("barley_seeds", p -> new BlockItem(NaturaBlocks.BARLEY_CROP.get(), p),
            Item.Properties::useItemDescriptionPrefix);
    public static final DeferredItem<Item> COTTON_SEEDS = item("cotton_seeds", p -> new BlockItem(NaturaBlocks.COTTON_CROP.get(), p),
            Item.Properties::useItemDescriptionPrefix);
    public static final DeferredItem<Item> BARLEY = simple("barley");
    public static final DeferredItem<Item> BARLEY_FLOUR = simple("barley_flour");
    public static final DeferredItem<Item> WHEAT_FLOUR = simple("wheat_flour");
    public static final DeferredItem<Item> COTTON = simple("cotton");
    public static final DeferredItem<Item> SULFUR = simple("sulfur");
    public static final DeferredItem<Item> GHOSTWOOD_FLETCHING = simple("ghostwood_fletching");
    public static final DeferredItem<Item> IMP_LEATHER = simple("imp_leather");
    public static final DeferredItem<Item> FLAMESTRING = simple("flamestring");
    public static final DeferredItem<Item> BLUE_DYE = simple("blue_dye");

    // ---- seed bags 5930, 5878, 5920, 5889, 5918, 5890, bone meal bag 5887 ----
    public static final DeferredItem<Item> WHEAT_SEED_BAG = item("wheat_seed_bag", p -> new SeedBagItem(() -> Blocks.WHEAT, p), p -> p);
    public static final DeferredItem<Item> BARLEY_SEED_BAG = item("barley_seed_bag", p -> new SeedBagItem(NaturaBlocks.BARLEY_CROP, p), p -> p);
    public static final DeferredItem<Item> POTATO_BAG = item("potato_bag", p -> new SeedBagItem(() -> Blocks.POTATOES, p), p -> p);
    public static final DeferredItem<Item> CARROT_BAG = item("carrot_bag", p -> new SeedBagItem(() -> Blocks.CARROTS, p), p -> p);
    public static final DeferredItem<Item> NETHER_WART_BAG = item("nether_wart_bag", p -> new SeedBagItem(() -> Blocks.NETHER_WART, p), p -> p);
    public static final DeferredItem<Item> COTTON_SEED_BAG = item("cotton_seed_bag", p -> new SeedBagItem(NaturaBlocks.COTTON_CROP, p), p -> p);
    public static final DeferredItem<Item> BONEMEAL_BAG = item("bonemeal_bag", BoneBagItem::new, p -> p);

    // ---- food ----
    /** CactusJuice 5888: 1 food, 0.1 saturation, 12 ticks. */
    public static final DeferredItem<Item> CACTUS_JUICE = item("cactus_juice", Item::new, p -> p.food(food(1, 0.1F, false), eat(0.6F)));
    /** SeedFood 5926: 3 food, 0.3 saturation; planted on sand it is a saguaro sprout. */
    public static final DeferredItem<Item> SAGUARO_FRUIT = item("saguaro_fruit", p -> new BlockItem(NaturaBlocks.SAGUARO_SPROUT.get(), p),
            p -> p.useItemDescriptionPrefix().food(food(3, 0.3F, false)));
    // BerryItem 5879:0-3: 1 food, 0.4 saturation, always edible, 16 ticks
    public static final DeferredItem<Item> RASPBERRY = berry("raspberry");
    public static final DeferredItem<Item> BLUEBERRY = berry("blueberry");
    public static final DeferredItem<Item> BLACKBERRY = berry("blackberry");
    public static final DeferredItem<Item> MALOBERRY = berry("maloberry");
    // NetherBerryItem 5917:0-3: 1 food, 0.4 saturation, 24 ticks, an effect and (75 %) a side effect
    public static final DeferredItem<Item> BLIGHTBERRY = netherBerry("blightberry", effect(MobEffects.REGENERATION, 160, 1.0F),
            effect(MobEffects.POISON, 100, 0.75F), effect(MobEffects.WITHER, 100, 0.15F));
    public static final DeferredItem<Item> DUSKBERRY = netherBerry("duskberry", effect(MobEffects.NIGHT_VISION, 300, 1.0F),
            effect(MobEffects.BLINDNESS, 60, 0.75F));
    public static final DeferredItem<Item> SKYBERRY = netherBerry("skyberry", effect(MobEffects.JUMP_BOOST, 160, 1.0F),
            effect(MobEffects.SLOWNESS, 60, 0.75F));
    public static final DeferredItem<Item> STINGBERRY = netherBerry("stingberry", effect(MobEffects.STRENGTH, 200, 1.0F),
            effect(MobEffects.MINING_FATIGUE, 200, 0.75F));
    /** BerryMedley 5880: 5 food, 1.4 saturation, always edible, 32 ticks, gives the bowl back. */
    public static final DeferredItem<Item> BERRY_MEDLEY = item("berry_medley", Item::new,
            p -> p.food(food(5, 1.4F, true), eat(1.6F)).usingConvertsTo(Items.BOWL));
    /** NetherFoodItem 5919:0: 4 food, 0.4 saturation, poison (75 %). */
    public static final DeferredItem<Item> POTASH_APPLE = item("potash_apple", Item::new,
            p -> p.food(food(4, 0.4F, false), eat(1.6F, effect(MobEffects.POISON, 50, 0.75F))));

    private static DeferredItem<Item> berry(String name) {
        return item(name, Item::new, p -> p.food(food(1, 0.4F, true), eat(0.8F)));
    }

    private static DeferredItem<Item> netherBerry(String name, ApplyStatusEffectsConsumeEffect... effects) {
        return item(name, Item::new, p -> p.food(food(1, 0.4F, false), eat(1.2F, effects)));
    }

    // ---- sticks 5929:0-12 ----
    public static final Map<Wood, DeferredItem<Item>> STICKS = perWood(w -> simple(w.id + "_stick"));

    // ---- bowls 5898 and stews 5928 (only the nether woods have bowls) ----
    public static final List<Wood> BOWL_WOODS = List.of(Wood.GHOSTWOOD, Wood.BLOODWOOD, Wood.DARKWOOD, Wood.FUSEWOOD);
    public static final Map<Wood, DeferredItem<Item>> BOWLS = bowlWoods(w -> simple(w.id + "_bowl"));
    /** BowlStew 1-13: mushroom stew in a Natura bowl (6 food, 0.6 saturation). */
    public static final Map<Wood, DeferredItem<Item>> MUSHROOM_STEWS = bowlWoods(w -> stew(w.id + "_mushroom_stew", BOWLS.get(w), false));
    /** BowlStew 14: glowshroom stew in a wooden bowl. */
    public static final DeferredItem<Item> GLOWSHROOM_STEW = item("glowshroom_stew", Item::new,
            p -> p.food(food(6, 0.6F, false), glowshroomStew()).usingConvertsTo(Items.BOWL));
    /** BowlStew 15-27: glowshroom stew in a Natura bowl. */
    public static final Map<Wood, DeferredItem<Item>> GLOWSHROOM_STEWS = bowlWoods(w -> stew(w.id + "_glowshroom_stew", BOWLS.get(w), true));

    /** Night vision 1125, weakness 400, mining fatigue 200 ticks. */
    private static Consumable glowshroomStew() {
        return eat(1.6F, effect(MobEffects.NIGHT_VISION, 1125, 1.0F), effect(MobEffects.WEAKNESS, 400, 1.0F),
                effect(MobEffects.MINING_FATIGUE, 200, 1.0F));
    }

    private static DeferredItem<Item> stew(String name, DeferredItem<Item> bowl, boolean glowshroom) {
        return item(name, p -> new Item(p.usingConvertsTo(bowl.get())), p -> p.food(food(6, 0.6F, false), glowshroom ? glowshroomStew() : eat(1.6F)));
    }

    // ---- tools ----
    /** EnumHelper.addToolMaterial("Bloodwood", 2, 350, 7.0F, 3, 24). */
    public static final ToolMaterial BLOODWOOD_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 350, 7.0F, 3.0F, 24,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Natura.MOD_ID, "bloodwood_tool_materials")));

    /** Sword, pickaxe, shovel, hatchet and kama of one material. */
    public record ToolSet(DeferredItem<Item> sword, DeferredItem<Item> pickaxe, DeferredItem<Item> shovel, DeferredItem<Item> hatchet,
            DeferredItem<Item> kama) {
    }

    // 1.5.2 damage: sword 4, pickaxe 2, shovel 1, axe 3 (+ material); the values below add the player's 1
    private static ToolSet tools(String prefix, ToolMaterial material) {
        return new ToolSet(
                item(prefix + "_sword", Item::new, p -> fuel(material, p.sword(material, 3.0F, -2.4F))),
                item(prefix + "_pickaxe", Item::new, p -> fuel(material, p.pickaxe(material, 1.0F, -2.8F))),
                item(prefix + "_shovel", Item::new, p -> fuel(material, p.shovel(material, 0.0F, -3.0F))),
                item(prefix + "_hatchet", Item::new, p -> fuel(material, p.axe(material, 2.0F, -3.0F))),
                item(prefix + "_kama", KamaItem::new, p -> fuel(material, p.sword(material, 3.0F, -2.4F)
                        .component(DataComponents.TOOL, ShearsItem.createToolProperties()))));
    }

    /** TileEntityFurnace: tools and swords of the WOOD material burn 200 ticks. */
    private static Item.Properties fuel(ToolMaterial material, Item.Properties properties) {
        return material == ToolMaterial.WOOD ? properties.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE) : properties;
    }

    public static final ToolSet GHOSTWOOD_TOOLS = tools("ghostwood", ToolMaterial.WOOD);
    public static final ToolSet BLOODWOOD_TOOLS = tools("bloodwood", BLOODWOOD_MATERIAL);
    public static final ToolSet DARKWOOD_TOOLS = tools("darkwood", ToolMaterial.STONE);
    public static final ToolSet FUSEWOOD_TOOLS = tools("fusewood", ToolMaterial.IRON);
    public static final ToolSet QUARTZ_TOOLS = tools("quartz", ToolMaterial.STONE);

    // NaturaBow: durability 384, 1501, 162, 28 (the fusewood bow shoots normal arrows, as in the pack)
    public static final DeferredItem<Item> GHOSTWOOD_BOW = item("ghostwood_bow", BowItem::new, p -> p.durability(384).enchantable(1));
    public static final DeferredItem<Item> BLOODWOOD_BOW = item("bloodwood_bow", BowItem::new, p -> p.durability(1501).enchantable(1));
    public static final DeferredItem<Item> DARKWOOD_BOW = item("darkwood_bow", BowItem::new, p -> p.durability(162).enchantable(1));
    public static final DeferredItem<Item> FUSEWOOD_BOW = item("fusewood_bow", BowItem::new, p -> p.durability(28).enchantable(1));

    // ---- imp armour: EnumHelper.addArmorMaterial("Imp", 33, {1, 3, 2, 1}, 15) ----
    public static final ResourceKey<EquipmentAsset> IMP_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(Natura.MOD_ID, "imp"));
    public static final ArmorMaterial IMP_MATERIAL = new ArmorMaterial(33, impDefense(), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Natura.MOD_ID, "repairs_imp_armor")), IMP_ASSET);

    private static Map<ArmorType, Integer> impDefense() {
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.HELMET, 1);
        defense.put(ArmorType.CHESTPLATE, 3);
        defense.put(ArmorType.LEGGINGS, 2);
        defense.put(ArmorType.BOOTS, 1);
        defense.put(ArmorType.BODY, 3);
        return defense;
    }

    // the creative tab shows them enchanted, as crafted (see addEnchantedArmor)
    public static final DeferredItem<Item> IMP_HELMET = NaturaBlocks.ITEMS.registerItem("imp_helmet", Item::new,
            p -> p.humanoidArmor(IMP_MATERIAL, ArmorType.HELMET));
    public static final DeferredItem<Item> IMP_JERKIN = NaturaBlocks.ITEMS.registerItem("imp_jerkin", Item::new,
            p -> p.humanoidArmor(IMP_MATERIAL, ArmorType.CHESTPLATE));
    public static final DeferredItem<Item> IMP_LEGGINGS = NaturaBlocks.ITEMS.registerItem("imp_leggings", Item::new,
            p -> p.humanoidArmor(IMP_MATERIAL, ArmorType.LEGGINGS));
    public static final DeferredItem<Item> IMP_BOOTS = NaturaBlocks.ITEMS.registerItem("imp_boots", Item::new,
            p -> p.humanoidArmor(IMP_MATERIAL, ArmorType.BOOTS));

    /** NContent: the imp armour is crafted with fire protection and protection, blast, projectile protection or feather falling. */
    static void addEnchantedArmor(HolderLookup.Provider holders, CreativeModeTab.Output output) {
        HolderLookup.RegistryLookup<Enchantment> enchantments = holders.lookupOrThrow(Registries.ENCHANTMENT);
        output.accept(enchanted(IMP_HELMET.get(), enchantments, Enchantments.PROTECTION));
        output.accept(enchanted(IMP_JERKIN.get(), enchantments, Enchantments.BLAST_PROTECTION));
        output.accept(enchanted(IMP_LEGGINGS.get(), enchantments, Enchantments.PROJECTILE_PROTECTION));
        output.accept(enchanted(IMP_BOOTS.get(), enchantments, Enchantments.FEATHER_FALLING));
    }

    private static ItemStack enchanted(Item item, HolderLookup.RegistryLookup<Enchantment> enchantments, ResourceKey<Enchantment> first) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(enchantments.getOrThrow(first), 1);
        stack.enchant(enchantments.getOrThrow(Enchantments.FIRE_PROTECTION), 1);
        return stack;
    }

    private static <T> Map<Wood, T> perWood(Function<Wood, T> factory) {
        Map<Wood, T> map = new EnumMap<>(Wood.class);
        for (Wood wood : Wood.values()) {
            map.put(wood, factory.apply(wood));
        }
        return Collections.unmodifiableMap(map);
    }

    private static <T> Map<Wood, T> bowlWoods(Function<Wood, T> factory) {
        Map<Wood, T> map = new EnumMap<>(Wood.class);
        for (Wood wood : BOWL_WOODS) {
            map.put(wood, factory.apply(wood));
        }
        return Collections.unmodifiableMap(map);
    }

    private NaturaItems() {
    }

    /** Loads the class, so that its items are registered. */
    static void init() {
    }
}
