package artifice;

import artifice.item.BoxItem;
import artifice.item.SickleItem;
import artifice.item.SledgeItem;
import artifice.item.Upgrade;
import artifice.item.UpgradeItem;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Items of Artifice. 1.5.2 kept several items as metadata of one id (steel 5256:0-1, dye 5258:0-3, nugget 5264:0-2,
 * coin 5265:0-3, upgrade 5266:0-12); here each is an item of its own.
 */
public final class ArtificeItems {
    /** The blocks a sledge mines quickly and can harvest: those of {@link artifice.item.SledgeParts}. */
    public static final TagKey<Block> SLEDGE_BREAKABLE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Artifice.MOD_ID,
            "sledge_breakable"));

    private static <I extends Item> DeferredItem<I> item(String name, Function<Item.Properties, I> factory, UnaryOperator<Item.Properties> properties) {
        DeferredItem<I> item = ArtificeBlocks.ITEMS.registerItem(name, factory, properties);
        ArtificeBlocks.TAB_ITEMS.add(() -> new ItemStack(item.get()));
        return item;
    }

    private static DeferredItem<Item> simple(String name) {
        return item(name, Item::new, p -> p);
    }

    /** TileEntityFurnace: tools of the WOOD material burn 200 ticks. */
    private static Item.Properties fuel(ToolMaterial material, Item.Properties properties) {
        return material == ToolMaterial.WOOD ? properties.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE) : properties;
    }

    /**
     * ItemSledge: ItemTool(2, material) - damage 2 + material bonus (1 is the player's), a quarter of the uses;
     * fast on the registered blocks only and no durability for mining (that is charged in SledgeItem.smash).
     */
    private static Item.Properties sledge(Item.Properties p, ToolMaterial material) {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return fuel(material, p.tool(material, SLEDGE_BREAKABLE, 1.0F, -2.8F, 0.0F)
                .component(DataComponents.TOOL, new Tool(List.of(Tool.Rule.minesAndDrops(blocks.getOrThrow(SLEDGE_BREAKABLE), material.speed())),
                        1.0F, 0, true))
                .durability(material.durability() / 4));
    }

    /** ItemSickle: ItemTool(3, material), half the uses, getStrVsBlock 1 everywhere; ItemTool damages on every hard block. */
    private static Item.Properties sickle(Item.Properties p, ToolMaterial material) {
        return fuel(material, p.tool(material, SLEDGE_BREAKABLE, 2.0F, -2.8F, 0.0F)
                .component(DataComponents.TOOL, new Tool(List.of(), 1.0F, 1, true))
                .durability(material.durability() / 2));
    }

    // ---- sledges 5251-5255 (EnumToolMaterial WOOD, STONE, IRON, GOLD, EMERALD), loss chance 50/40/30/25/20 % ----
    public static final DeferredItem<SledgeItem> WOODEN_SLEDGE = item("wooden_sledge", p -> new SledgeItem(p, 50), p -> sledge(p, ToolMaterial.WOOD));
    public static final DeferredItem<SledgeItem> STONE_SLEDGE = item("stone_sledge", p -> new SledgeItem(p, 40), p -> sledge(p, ToolMaterial.STONE));
    public static final DeferredItem<SledgeItem> IRON_SLEDGE = item("iron_sledge", p -> new SledgeItem(p, 30), p -> sledge(p, ToolMaterial.IRON));
    public static final DeferredItem<SledgeItem> GOLDEN_SLEDGE = item("golden_sledge", p -> new SledgeItem(p, 25), p -> sledge(p, ToolMaterial.GOLD));
    public static final DeferredItem<SledgeItem> DIAMOND_SLEDGE = item("diamond_sledge", p -> new SledgeItem(p, 20), p -> sledge(p, ToolMaterial.DIAMOND));
    public static final List<DeferredItem<SledgeItem>> SLEDGES = List.of(WOODEN_SLEDGE, STONE_SLEDGE, IRON_SLEDGE, GOLDEN_SLEDGE, DIAMOND_SLEDGE);

    // ---- steel 5256: 0 ingot, 1 dust (the dust has no recipe without "Alternate Steel Recipe") ----
    public static final DeferredItem<Item> STEEL_INGOT = simple("steel_ingot");
    public static final DeferredItem<Item> STEEL_DUST = simple("steel_dust");

    // ---- box 5257 ----
    public static final DeferredItem<BoxItem> BOX = item("box", BoxItem::new, p -> p);

    // ---- dye 5258: 0 blue, 1 black, 2 brown, 3 white (oredict dyeBlue ...; here real dyes) ----
    public static final DeferredItem<DyeItem> BLUE_DYE = dye("blue_dye", DyeColor.BLUE);
    public static final DeferredItem<DyeItem> BLACK_DYE = dye("black_dye", DyeColor.BLACK);
    public static final DeferredItem<DyeItem> BROWN_DYE = dye("brown_dye", DyeColor.BROWN);
    public static final DeferredItem<DyeItem> WHITE_DYE = dye("white_dye", DyeColor.WHITE);

    private static DeferredItem<DyeItem> dye(String name, DyeColor color) {
        return item(name, DyeItem::new, p -> p.component(DataComponents.DYE, color));
    }

    // ---- sickles 5259-5263, radius 1-5 ----
    public static final DeferredItem<SickleItem> WOODEN_SICKLE = item("wooden_sickle", p -> new SickleItem(p, 1), p -> sickle(p, ToolMaterial.WOOD));
    public static final DeferredItem<SickleItem> STONE_SICKLE = item("stone_sickle", p -> new SickleItem(p, 2), p -> sickle(p, ToolMaterial.STONE));
    public static final DeferredItem<SickleItem> IRON_SICKLE = item("iron_sickle", p -> new SickleItem(p, 3), p -> sickle(p, ToolMaterial.IRON));
    public static final DeferredItem<SickleItem> GOLDEN_SICKLE = item("golden_sickle", p -> new SickleItem(p, 4), p -> sickle(p, ToolMaterial.GOLD));
    public static final DeferredItem<SickleItem> DIAMOND_SICKLE = item("diamond_sickle", p -> new SickleItem(p, 5), p -> sickle(p, ToolMaterial.DIAMOND));
    public static final List<DeferredItem<SickleItem>> SICKLES = List.of(WOODEN_SICKLE, STONE_SICKLE, IRON_SICKLE, GOLDEN_SICKLE, DIAMOND_SICKLE);

    // ---- nugget 5264: 0 copper, 1 silver, 2 platinum; coin 5265: 0 shilling, 1 florin, 2 sovereign, 3 marque ----
    public static final DeferredItem<Item> COPPER_NUGGET = simple("copper_nugget");
    public static final DeferredItem<Item> SILVER_NUGGET = simple("silver_nugget");
    public static final DeferredItem<Item> PLATINUM_NUGGET = simple("platinum_nugget");
    public static final DeferredItem<Item> SHILLING = simple("shilling");
    public static final DeferredItem<Item> FLORIN = simple("florin");
    public static final DeferredItem<Item> SOVEREIGN = simple("sovereign");
    public static final DeferredItem<Item> MARQUE = simple("marque");

    // ---- upgrade 5266:0-12 ----
    public static final Map<Upgrade, DeferredItem<UpgradeItem>> UPGRADES = upgrades();

    private static Map<Upgrade, DeferredItem<UpgradeItem>> upgrades() {
        Map<Upgrade, DeferredItem<UpgradeItem>> map = new EnumMap<>(Upgrade.class);
        for (Upgrade upgrade : Upgrade.values()) {
            map.put(upgrade, item(upgrade.id, p -> new UpgradeItem(p, upgrade), p -> p));
        }
        return map;
    }

    /** All upgrade items in the order of EnumUpgrades. */
    public static List<DeferredItem<UpgradeItem>> upgradeList() {
        return new ArrayList<>(UPGRADES.values());
    }

    private ArtificeItems() {
    }

    static void init() {
        // loads the class: the items above register in ArtificeBlocks.ITEMS
    }
}
