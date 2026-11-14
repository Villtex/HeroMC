package hexxit.worldgen152.core.populate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Chest loot and dungeon spawner tables as Forge 7.8 kept them (ChestGenHooks, DungeonHooks), with the
 * random draws of the original. Mods add their entries like they did through those hooks.
 */
public final class OldLoot {
    /** WeightedRandomChestContent. */
    public record Entry(OldStack stack, int min, int max, int weight) {
    }

    /** One chest category of ChestGenHooks. */
    public static final class Category {
        private final List<Entry> entries = new ArrayList<>();
        private final int countMin;
        private final int countMax;

        Category(int countMin, int countMax) {
            this.countMin = countMin;
            this.countMax = countMax;
        }

        public synchronized void add(OldStack stack, int min, int max, int weight) {
            this.entries.add(new Entry(stack, min, max, weight));
        }

        public synchronized void remove(int itemId, int damage) {
            this.entries.removeIf(entry -> entry.stack.itemId() == itemId && (damage == 32767 || entry.stack.damage() == damage));
        }

        /** ChestGenHooks.getItems: enchanted books pick their enchantment here, once per chest. */
        public synchronized Entry[] items(Random random) {
            Entry[] rolled = new Entry[this.entries.size()];
            for (int i = 0; i < rolled.length; i++) {
                Entry entry = this.entries.get(i);
                if (entry.stack.itemId() == ENCHANTED_BOOK) {
                    int[] enchantment = BOOK_ENCHANTMENTS[random.nextInt(BOOK_ENCHANTMENTS.length)];
                    int level = 1 >= enchantment[1] ? 1 : random.nextInt(enchantment[1]) + 1;
                    entry = new Entry(new OldStack(ENCHANTED_BOOK, 0, 1, enchantment[0], level), entry.min, entry.max, entry.weight);
                }
                rolled[i] = entry;
            }
            return rolled;
        }

        /** ChestGenHooks.getCount: how many draws a container of this category gets. */
        public int count(Random random) {
            return this.countMin < this.countMax ? this.countMin + random.nextInt(this.countMax - this.countMin) : this.countMin;
        }

        /** Fills a chest: WeightedRandomChestContent.generateChestContents with this category's items and count. */
        public void fill(Random random, OldTile.Chest chest) {
            Entry[] items = this.items(random);
            generate(random, items, chest.slots(), this.count(random));
        }
    }

    /**
     * WeightedRandomChestContent.generateChestContents / generateDispenserContents: {@code count} weighted draws,
     * each put into a random slot of the container (a later draw may overwrite an earlier one).
     */
    public static void generate(Random random, Entry[] items, OldStack[] slots, int count) {
        int total = 0;
        for (Entry item : items) {
            total += item.weight;
        }
        for (int i = 0; i < count; i++) {
            int pick = random.nextInt(total);
            Entry chosen = null;
            for (Entry item : items) {
                pick -= item.weight;
                if (pick < 0) {
                    chosen = item;
                    break;
                }
            }
            random.nextInt(chosen.max - chosen.min + 1); // the original drew the count twice and used the second
            int size = chosen.min + random.nextInt(chosen.max - chosen.min + 1);
            int limit = stackLimit(chosen.stack.itemId());
            if (size > limit) {
                for (int n = 0; n < size; n++) {
                    slots[random.nextInt(slots.length)] = chosen.stack.withCount(1);
                }
            } else {
                slots[random.nextInt(slots.length)] = chosen.stack.withCount(size);
            }
        }
    }

    public static final int ENCHANTED_BOOK = 403;

    /** Enchantments a generated book can carry: {id, highest level}, the 22 of vanilla 1.5.2 in id order. */
    private static final int[][] BOOK_ENCHANTMENTS = {
            {0, 4}, {1, 4}, {2, 4}, {3, 4}, {4, 4}, {5, 3}, {6, 1}, {7, 3},
            {16, 5}, {17, 5}, {18, 5}, {19, 2}, {20, 2}, {21, 3},
            {32, 5}, {33, 1}, {34, 3}, {35, 3},
            {48, 5}, {49, 2}, {50, 1}, {51, 1}};

    /** ChestGenHooks "dungeonChest": always 8 draws. */
    public static final Category DUNGEON_CHEST = new Category(8, 8);

    public static final Category MINESHAFT_CORRIDOR = category(3, 7, new int[][] {
            {265, 0, 1, 5, 10}, {266, 0, 1, 3, 5}, {331, 0, 4, 9, 5}, {351, 4, 4, 9, 5}, {264, 0, 1, 2, 3}, {263, 0, 3, 8, 10},
            {297, 0, 1, 3, 15}, {257, 0, 1, 1, 1}, {66, 0, 4, 8, 1}, {362, 0, 2, 4, 10}, {361, 0, 2, 4, 10}, {ENCHANTED_BOOK, 0, 1, 1, 1}});
    public static final Category PYRAMID_DESERT_CHEST = category(2, 7, new int[][] {
            {264, 0, 1, 3, 3}, {265, 0, 1, 5, 10}, {266, 0, 2, 7, 15}, {388, 0, 1, 3, 2}, {352, 0, 4, 6, 20}, {367, 0, 3, 7, 16},
            {ENCHANTED_BOOK, 0, 1, 1, 1}});
    public static final Category PYRAMID_JUNGLE_CHEST = category(2, 7, new int[][] {
            {264, 0, 1, 3, 3}, {265, 0, 1, 5, 10}, {266, 0, 2, 7, 15}, {388, 0, 1, 3, 2}, {352, 0, 4, 6, 20}, {367, 0, 3, 7, 16},
            {ENCHANTED_BOOK, 0, 1, 1, 1}});
    public static final Category PYRAMID_JUNGLE_DISPENSER = category(2, 2, new int[][] {{262, 0, 2, 7, 30}});
    public static final Category STRONGHOLD_CORRIDOR = category(2, 4, new int[][] {
            {368, 0, 1, 1, 10}, {264, 0, 1, 3, 3}, {265, 0, 1, 5, 10}, {266, 0, 1, 3, 5}, {331, 0, 4, 9, 5}, {297, 0, 1, 3, 15},
            {260, 0, 1, 3, 15}, {257, 0, 1, 1, 5}, {267, 0, 1, 1, 5}, {307, 0, 1, 1, 5}, {306, 0, 1, 1, 5}, {308, 0, 1, 1, 5},
            {309, 0, 1, 1, 5}, {322, 0, 1, 1, 1}, {ENCHANTED_BOOK, 0, 1, 1, 1}});
    public static final Category STRONGHOLD_LIBRARY = category(1, 5, new int[][] {
            {340, 0, 1, 3, 20}, {339, 0, 2, 7, 20}, {395, 0, 1, 1, 1}, {345, 0, 1, 1, 1}, {ENCHANTED_BOOK, 0, 1, 5, 2}});
    public static final Category STRONGHOLD_CROSSING = category(1, 5, new int[][] {
            {265, 0, 1, 5, 10}, {266, 0, 1, 3, 5}, {331, 0, 4, 9, 5}, {263, 0, 3, 8, 10}, {297, 0, 1, 3, 15}, {260, 0, 1, 3, 15},
            {257, 0, 1, 1, 1}, {ENCHANTED_BOOK, 0, 1, 1, 1}});
    public static final Category VILLAGE_BLACKSMITH = category(3, 9, new int[][] {
            {264, 0, 1, 3, 3}, {265, 0, 1, 5, 10}, {266, 0, 1, 3, 5}, {297, 0, 1, 3, 15}, {260, 0, 1, 3, 15}, {257, 0, 1, 1, 5},
            {267, 0, 1, 1, 5}, {307, 0, 1, 1, 5}, {306, 0, 1, 1, 5}, {308, 0, 1, 1, 5}, {309, 0, 1, 1, 5}, {49, 0, 3, 7, 5},
            {6, 0, 3, 7, 5}});

    /** A category from rows of {item id, damage, min, max, weight}. */
    private static Category category(int countMin, int countMax, int[][] rows) {
        Category category = new Category(countMin, countMax);
        for (int[] row : rows) {
            category.add(new OldStack(row[0], row[1], 1), row[2], row[3], row[4]);
        }
        return category;
    }

    private static final List<String> DUNGEON_MOBS = new ArrayList<>();
    private static final List<Integer> DUNGEON_MOB_WEIGHTS = new ArrayList<>();

    static {
        DUNGEON_CHEST.add(new OldStack(329, 0, 1), 1, 1, 100); // saddle
        DUNGEON_CHEST.add(new OldStack(265, 0, 1), 1, 4, 100); // iron ingot
        DUNGEON_CHEST.add(new OldStack(297, 0, 1), 1, 1, 100); // bread
        DUNGEON_CHEST.add(new OldStack(296, 0, 1), 1, 4, 100); // wheat
        DUNGEON_CHEST.add(new OldStack(289, 0, 1), 1, 4, 100); // gunpowder
        DUNGEON_CHEST.add(new OldStack(287, 0, 1), 1, 4, 100); // string
        DUNGEON_CHEST.add(new OldStack(325, 0, 1), 1, 1, 100); // bucket
        DUNGEON_CHEST.add(new OldStack(322, 0, 1), 1, 1, 1); // golden apple
        DUNGEON_CHEST.add(new OldStack(331, 0, 1), 1, 4, 50); // redstone
        DUNGEON_CHEST.add(new OldStack(2256, 0, 1), 1, 1, 5); // record "13"
        DUNGEON_CHEST.add(new OldStack(2257, 0, 1), 1, 1, 5); // record "cat"
        DUNGEON_CHEST.add(new OldStack(351, 3, 1), 1, 1, 100); // cocoa beans
        DUNGEON_CHEST.add(new OldStack(ENCHANTED_BOOK, 0, 1), 1, 1, 100);
        addDungeonMob("Skeleton", 100);
        addDungeonMob("Zombie", 200);
        addDungeonMob("Spider", 100);
    }

    private OldLoot() {
    }

    private static int stackLimit(int itemId) {
        // of the vanilla loot only these do not stack; mods adding unstackable loot with max > 1 are not known
        if (itemId == 368) {
            return 16; // ender pearl
        }
        return switch (itemId) {
            case 329, 325, 2256, 2257, ENCHANTED_BOOK, 257, 267, 306, 307, 308, 309 -> 1;
            default -> 64;
        };
    }

    /** DungeonHooks.addDungeonMob: a known name gets the extra weight. */
    public static synchronized void addDungeonMob(String name, int weight) {
        int index = DUNGEON_MOBS.indexOf(name);
        if (index >= 0) {
            DUNGEON_MOB_WEIGHTS.set(index, DUNGEON_MOB_WEIGHTS.get(index) + weight);
        } else {
            DUNGEON_MOBS.add(name);
            DUNGEON_MOB_WEIGHTS.add(weight);
        }
    }

    /** DungeonHooks.removeDungeonMob. */
    public static synchronized void removeDungeonMob(String name) {
        int index = DUNGEON_MOBS.indexOf(name);
        if (index >= 0) {
            DUNGEON_MOBS.remove(index);
            DUNGEON_MOB_WEIGHTS.remove(index);
        }
    }

    /** DungeonHooks.getRandomDungeonMob. */
    public static synchronized String randomDungeonMob(Random random) {
        int total = 0;
        for (int weight : DUNGEON_MOB_WEIGHTS) {
            total += weight;
        }
        int pick = random.nextInt(total);
        for (int i = 0; i < DUNGEON_MOBS.size(); i++) {
            pick -= DUNGEON_MOB_WEIGHTS.get(i);
            if (pick < 0) {
                return DUNGEON_MOBS.get(i);
            }
        }
        return "";
    }
}
