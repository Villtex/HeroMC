package dimdoors.gen;

import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * DDLoot of Dimensional Doors 1.4.1: the "dimensionalDungeonChest" of the dungeon pockets. Its items are those of
 * seven vanilla categories merged by item id (the smallest weight wins, dungeonChest weights divided by 10 first),
 * the enchanted book at weight 3, then the mod's own items as far as the pack's DimDoors.cfg allows them. Five draws.
 *
 * <p>The merge divided the dungeonChest weights in place, which changed the dungeon chests of the whole pack
 * ({@link OldLoot.Category#divideWeights}).
 */
public final class DDLoot {
    /** Item ids of the pack's DimDoors.cfg, plus 256 like every item id of 1.5.2. */
    public static final int DIMENSIONAL_DOOR_ITEM = 5526 + 256;
    public static final int RIFT_BLADE = 5527 + 256;
    public static final int RIFT_REMOVER = 5528 + 256;
    public static final int RIFT_SIGNATURE = 5529 + 256;
    public static final int STABILIZED_RIFT_SIGNATURE = 5530 + 256;
    public static final int STABLE_FABRIC = 5531 + 256;
    public static final int UNSTABLE_DOOR_ITEM = 5532 + 256;
    public static final int WARP_DOOR_ITEM = 5533 + 256;

    private static final int CHEST_SIZE = 5;
    private static List<OldLoot.Entry> items;

    private DDLoot() {
    }

    static void register() {
        OldLoot.DUNGEON_CHEST.divideWeights(10);
    }

    /** DDLoot.registerInfo, at the first use: by then every mod has added its loot to the vanilla categories. */
    public static synchronized List<OldLoot.Entry> items() {
        if (items == null) {
            OldLoot.Category[] categories = {OldLoot.MINESHAFT_CORRIDOR, OldLoot.PYRAMID_DESERT_CHEST, OldLoot.PYRAMID_JUNGLE_CHEST,
                    OldLoot.STRONGHOLD_CORRIDOR, OldLoot.STRONGHOLD_CROSSING, OldLoot.VILLAGE_BLACKSMITH, OldLoot.DUNGEON_CHEST};
            // a HashMap of Integer keys iterates as in Java 8, the Java of the pack's servers
            Map<Integer, OldLoot.Entry> container = new HashMap<>();
            Random unused = new Random(0L);
            for (OldLoot.Category category : categories) {
                for (OldLoot.Entry item : category.items(unused)) {
                    if (category == OldLoot.DUNGEON_CHEST && item.stack().itemId() == OldLoot.ENCHANTED_BOOK) {
                        // the fresh book entry of dungeonChest is divided here as well (the stored one is not)
                        item = new OldLoot.Entry(item.stack(), item.min(), item.max(), Math.max(1, item.weight() / 10));
                    }
                    int id = item.stack().itemId();
                    OldLoot.Entry other = container.get(id);
                    if (other == null || item.weight() < other.weight()) {
                        container.put(id, item);
                    }
                }
            }
            List<OldLoot.Entry> merged = new ArrayList<>();
            boolean book = false;
            for (OldLoot.Entry item : container.values()) {
                if (!book && item.stack().itemId() == OldLoot.ENCHANTED_BOOK) {
                    item = new OldLoot.Entry(item.stack(), item.min(), item.max(), 3);
                    book = true;
                }
                merged.add(item);
            }
            // the pack's loot switches: fabric of reality, dimensional door, rift signature, rift remover and rift blade
            merged.add(new OldLoot.Entry(new OldStack(RiftGen.FABRIC, 0, 1), 8, 32, 9));
            merged.add(new OldLoot.Entry(new OldStack(DIMENSIONAL_DOOR_ITEM, 0, 1), 1, 1, 4));
            merged.add(new OldLoot.Entry(new OldStack(RIFT_SIGNATURE, 0, 1), 1, 1, 4));
            merged.add(new OldLoot.Entry(new OldStack(RIFT_REMOVER, 0, 1), 1, 1, 4));
            merged.add(new OldLoot.Entry(new OldStack(RIFT_BLADE, 0, 1), 1, 1, 1));
            items = merged;
        }
        return items;
    }

    /** WeightedRandomChestContent.generateChestContents with ChestGenHooks.getItems / getCount of the category. */
    public static void fill(Random random, OldTile.Chest chest) {
        List<OldLoot.Entry> stored = items();
        OldLoot.Entry[] rolled = new OldLoot.Entry[stored.size()];
        for (int i = 0; i < rolled.length; i++) {
            OldLoot.Entry entry = stored.get(i);
            if (entry.stack().itemId() == OldLoot.ENCHANTED_BOOK) {
                entry = new OldLoot.Entry(OldLoot.randomBook(random), entry.min(), entry.max(), entry.weight());
            }
            rolled[i] = entry;
        }
        OldLoot.generate(random, rolled, chest.slots(), CHEST_SIZE);
    }
}
