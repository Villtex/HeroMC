package xreliquary.recipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * AlkahestMap: the alkahest points of each item (by item id in 1.5.2, so every damage value of wood, wool, saplings
 * and coal counts the same; here the 1.5.2 variants: the four logs, every wool, every sapling, coal and charcoal).
 * 2304 points are a lapis block, 256 a lapis lazuli, 64 a redstone dust.
 */
public final class AlkahestValues {
    private static final Map<Item, Integer> VALUES = new HashMap<>();

    static {
        put(1, Items.DIRT, Items.COBBLESTONE, Items.SAND, Items.NETHERRACK, Items.NETHER_BRICKS, Items.END_STONE, Items.STRING, Items.WHEAT_SEEDS,
                Items.SUGAR_CANE, Items.SUGAR, Items.PAPER);
        put(4, Items.GRAVEL, Items.SANDSTONE, Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE, Items.SOUL_SAND, Items.SPIDER_EYE, Items.FLINT,
                Items.CARROT, Items.POTATO, Items.ROTTEN_FLESH, Items.EGG);
        for (Item wool : Items.WOOL.asList()) {
            put(4, wool);
        }
        put(8, Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG, Items.CLAY, Items.OBSIDIAN, Items.MELON, Items.PUMPKIN,
                Items.COAL, Items.CHARCOAL, Items.BLAZE_POWDER, Items.SLIME_BALL, Items.COD, Items.COOKED_COD, Items.PORKCHOP, Items.COOKED_PORKCHOP,
                Items.BEEF, Items.COOKED_BEEF, Items.CHICKEN, Items.COOKED_CHICKEN, Items.BONE, Items.FEATHER, Items.LEATHER);
        put(2, Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING, Items.JUNGLE_SAPLING, Items.NETHER_WART, Items.PUMPKIN_SEEDS,
                Items.MELON_SEEDS, Items.WHEAT);
        put(16, Items.BLAZE_ROD, Items.MAGMA_CREAM);
        put(4608, Items.DIAMOND);
        put(1152, Items.GOLD_INGOT, Items.EMERALD);
        put(512, Items.IRON_INGOT);
        put(128, Items.GUNPOWDER);
    }

    private AlkahestValues() {
    }

    private static void put(int value, Item... items) {
        for (Item item : items) {
            VALUES.put(item, value);
        }
    }

    public static int value(Item item) {
        return VALUES.getOrDefault(item, 0);
    }

    public static Map<Item, Integer> all() {
        return VALUES;
    }
}
