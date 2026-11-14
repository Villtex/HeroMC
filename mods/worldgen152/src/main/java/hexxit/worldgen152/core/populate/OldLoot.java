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
        private synchronized Entry[] roll(Random random) {
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

        /** Fills a chest: WeightedRandomChestContent.generateChestContents with this category's items and count. */
        public void fill(Random random, OldTile.Chest chest) {
            Entry[] items = this.roll(random);
            int count = this.countMin < this.countMax ? this.countMin + random.nextInt(this.countMax - this.countMin) : this.countMin;
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
                        chest.slots()[random.nextInt(chest.slots().length)] = chosen.stack.withCount(1);
                    }
                } else {
                    chest.slots()[random.nextInt(chest.slots().length)] = chosen.stack.withCount(size);
                }
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
        // of the items above only these do not stack; mods adding unstackable loot with max > 1 are not known
        return itemId == 329 || itemId == 325 || itemId == 2256 || itemId == 2257 || itemId == ENCHANTED_BOOK ? 1 : 64;
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
