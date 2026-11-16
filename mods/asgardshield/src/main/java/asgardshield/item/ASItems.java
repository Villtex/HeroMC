package asgardshield.item;

import asgardshield.AsgardShield;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * The items of the core mod and the helpers the addons use for theirs. Values as in the pack
 * ("Override Equipment Duriablity"=1): shield durability = uses of the tool material (wood x2, diamond / 2, ender / 2,
 * patchwork x2), gilded +128; giant swords 1.5 x the uses. Attack damage as in 1.5.2 (the shield 1-2, the giant sword
 * 6 + material bonus); the modifiers subtract the player's base damage of 1.
 */
public final class ASItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AsgardShield.MOD_ID);
    /** The items in the order of the creative tab (CreativeTabs.tabCombat). */
    public static final List<DeferredItem<? extends Item>> TAB = new ArrayList<>();

    private ASItems() {
    }

    public static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
    }

    private static ToolMaterial material(int durability, int enchantability, TagKey<Item> repair) {
        return new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, durability, 1.0F, 0.0F, enchantability, repair);
    }

    /**
     * ItemAsgardShield: getStrVsBlock 2 on dirt and grass, 1.5 on plants, leaves and pumpkins, 1 on anything else; two
     * points of durability per block with hardness; one per hit. No attack cooldown in 1.5.2: the sword's -2.4 is
     * used as for every ported sword.
     */
    public static Item.Properties shield(Item.Properties p, int durability, int attackDamage, int enchantability, TagKey<Item> repair) {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return p.sword(material(durability, enchantability, repair), attackDamage - 1.0F, -2.4F)
                .component(DataComponents.TOOL, new Tool(List.of(
                        Tool.Rule.overrideSpeed(HolderSet.direct(Blocks.DIRT.builtInRegistryHolder(), Blocks.GRASS_BLOCK.builtInRegistryHolder()), 2.0F),
                        Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)), 1.0F, 2, true));
    }

    /** ItemZedSword (an ItemSword): 15 on cobweb (and harvests it), 1.5 on anything else, two points per block. */
    public static Item.Properties giantSword(Item.Properties p, int durability, int attackDamage, int enchantability, TagKey<Item> repair,
            boolean wood) {
        Item.Properties props = p.sword(material(durability, enchantability, repair), attackDamage - 1.0F, -2.4F)
                .component(DataComponents.TOOL, new Tool(List.of(Tool.Rule.minesAndDrops(HolderSet.direct((Holder<Block>) Blocks.COBWEB
                        .builtInRegistryHolder()), 15.0F)), 1.5F, 2, false));
        // TileEntityFurnace: swords of the WOOD material burn 200 ticks
        return wood ? props.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE) : props;
    }

    private static TagKey<Item> shieldRepair(String material) {
        return itemTag(AsgardShield.MOD_ID, "repairs/shield/" + material);
    }

    private static TagKey<Item> swordRepair(String material) {
        return itemTag(AsgardShield.MOD_ID, "repairs/giant_sword/" + material);
    }

    private static <I extends Item> DeferredItem<I> add(DeferredItem<I> item) {
        TAB.add(item);
        return item;
    }

    private static DeferredItem<ItemAsgardShield> shield(String name, int perk, boolean gilded, GuardSound sound, float multiplier,
            float knockback, String material, int durability, int damage, int enchantability, DeferredItem<ItemAsgardShield> gildedVariant) {
        return add(ITEMS.registerItem(name, p -> new ItemAsgardShield(p, perk, gilded, sound, multiplier, knockback, Special.NONE, material,
                gildedVariant), p -> shield(p, durability, damage, enchantability, shieldRepair(material))));
    }

    private static DeferredItem<ItemZedSword> sword(String name, GuardSound sound, float multiplier, String material, int durability, int damage,
            int enchantability) {
        return ITEMS.registerItem(name, p -> new ItemZedSword(p, sound, multiplier, Special.NONE),
                p -> giantSword(p, durability, damage, enchantability, swordRepair(material), material.equals("wood")));
    }

    // gilded shields: +128 durability, +1 damage, +2 enchantability, -0.05 damage multiplier, -0.1 knockback
    public static final DeferredItem<ItemAsgardShield> GILDED_WOODEN_SHIELD;
    public static final DeferredItem<ItemAsgardShield> GILDED_STONE_SHIELD;
    public static final DeferredItem<ItemAsgardShield> GILDED_IRON_SHIELD;
    public static final DeferredItem<ItemAsgardShield> GILDED_DIAMOND_SHIELD;
    public static final DeferredItem<ItemAsgardShield> WOODEN_SHIELD;
    public static final DeferredItem<ItemAsgardShield> STONE_SHIELD;
    public static final DeferredItem<ItemAsgardShield> IRON_SHIELD;
    public static final DeferredItem<ItemAsgardShield> DIAMOND_SHIELD;
    public static final DeferredItem<ItemZedSword> WOODEN_GIANT_SWORD;
    public static final DeferredItem<ItemZedSword> STONE_GIANT_SWORD;
    public static final DeferredItem<ItemZedSword> IRON_GIANT_SWORD;
    public static final DeferredItem<ItemZedSword> GOLDEN_GIANT_SWORD;
    public static final DeferredItem<ItemZedSword> DIAMOND_GIANT_SWORD;

    static {
        // registration order = order of the creative tab (ids 24500-24512 of the mod)
        GILDED_WOODEN_SHIELD = ITEMS.registerItem("gilded_wooden_shield", p -> new ItemAsgardShield(p, 1, true, GuardSound.WOOD, 0.9F, -0.2F,
                Special.NONE, "wood", null), p -> shield(p, 118 + 128, 2, 7, shieldRepair("wood")));
        GILDED_STONE_SHIELD = ITEMS.registerItem("gilded_stone_shield", p -> new ItemAsgardShield(p, 2, true, GuardSound.STONE, 0.85F, -0.3F,
                Special.NONE, "stone", null), p -> shield(p, 131 + 128, 3, 3, shieldRepair("stone")));
        GILDED_IRON_SHIELD = ITEMS.registerItem("gilded_iron_shield", p -> new ItemAsgardShield(p, 3, true, GuardSound.METAL, 0.8F, -0.2F,
                Special.NONE, "iron", null), p -> shield(p, 250 + 128, 2, 6, shieldRepair("iron")));
        GILDED_DIAMOND_SHIELD = ITEMS.registerItem("gilded_diamond_shield", p -> new ItemAsgardShield(p, 4, true, GuardSound.CRYSTAL, 0.75F,
                -0.2F, Special.NONE, "diamond", null), p -> shield(p, 780 + 128, 2, 5, shieldRepair("diamond")));
        WOODEN_SHIELD = shield("wooden_shield", 1, false, GuardSound.WOOD, 0.95F, -0.1F, "wood", 118, 1, 5, GILDED_WOODEN_SHIELD);
        STONE_SHIELD = shield("stone_shield", 2, false, GuardSound.STONE, 0.9F, -0.2F, "stone", 131, 2, 1, GILDED_STONE_SHIELD);
        IRON_SHIELD = shield("iron_shield", 3, false, GuardSound.METAL, 0.85F, -0.1F, "iron", 250, 1, 4, GILDED_IRON_SHIELD);
        DIAMOND_SHIELD = shield("diamond_shield", 4, false, GuardSound.CRYSTAL, 0.8F, -0.1F, "diamond", 780, 1, 3, GILDED_DIAMOND_SHIELD);
        TAB.add(GILDED_WOODEN_SHIELD);
        TAB.add(GILDED_STONE_SHIELD);
        TAB.add(GILDED_IRON_SHIELD);
        TAB.add(GILDED_DIAMOND_SHIELD);
        WOODEN_GIANT_SWORD = add(sword("wooden_giant_sword", GuardSound.WOOD, 0.95F, "wood", 88, 6, 15));
        STONE_GIANT_SWORD = add(sword("stone_giant_sword", GuardSound.STONE, 0.9F, "stone", 196, 7, 5));
        IRON_GIANT_SWORD = add(sword("iron_giant_sword", GuardSound.METAL, 0.85F, "iron", 375, 8, 14));
        GOLDEN_GIANT_SWORD = add(sword("golden_giant_sword", GuardSound.METAL, 0.95F, "gold", 48, 6, 22));
        DIAMOND_GIANT_SWORD = add(sword("diamond_giant_sword", GuardSound.CRYSTAL, 0.8F, "diamond", 2341, 9, 10));
    }
}
