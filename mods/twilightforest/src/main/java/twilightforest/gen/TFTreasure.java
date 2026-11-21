package twilightforest.gen;

import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TFTreasure: the chests of the Twilight Forest. Each chest draws from its own random sequence seeded from the world
 * seed and its position, never from the chunk's. Item ids are those of 1.5.2: Twilight Forest items are their
 * TwilightForest.cfg id + 256, blocks their block id.
 *
 * <p>All 16 tables (24b: basement 9 and tree cache 14 of the decoration, 24d: the rest). Random enchantments use the
 * vanilla enchantments of 1.5.2 ({@link OldEnchanting}).
 */
public final class TFTreasure {
    /** TFCreatures.getSpawnerNameFor: the mob names of the pack (creatureCompatibility off). */
    public static final String SPAWNER_PREFIX = "TwilightForest.";

    static final int CHARM_OF_LIFE_1 = 7257 + 256;
    static final int CHARM_OF_KEEPING_1 = 7254 + 256;

    static final int IRONWOOD_SWORD = 7285 + 256;
    static final int STEELEAF_SWORD = 7319 + 256;

    public static final TFTreasure HILL1 = new TFTreasure(1);
    public static final TFTreasure HILL2 = new TFTreasure(2);
    public static final TFTreasure HILL3 = new TFTreasure(3);
    public static final TFTreasure HEDGEMAZE = new TFTreasure(4);
    public static final TFTreasure LABYRINTH_ROOM = new TFTreasure(5);
    public static final TFTreasure LABYRINTH_DEADEND = new TFTreasure(6);
    public static final TFTreasure TOWER_ROOM = new TFTreasure(7);
    public static final TFTreasure TOWER_LIBRARY = new TFTreasure(8);
    public static final TFTreasure BASEMENT = new TFTreasure(9);
    public static final TFTreasure LABYRINTH_VAULT = new TFTreasure(10);
    public static final TFTreasure DARKTOWER_CACHE = new TFTreasure(11);
    public static final TFTreasure DARKTOWER_KEY = new TFTreasure(12);
    public static final TFTreasure DARKTOWER_BOSS = new TFTreasure(13);
    public static final TFTreasure TREE_CACHE = new TFTreasure(14);
    public static final TFTreasure STRONGHOLD_CACHE = new TFTreasure(15);
    public static final TFTreasure STRONGHOLD_ROOM = new TFTreasure(16);

    private final Table useless = new Table();
    private final Table common = new Table();
    private final Table uncommon = new Table();
    private final Table rare = new Table();
    private final Table ultrarare = new Table();

    private TFTreasure(int type) {
        this.fill(type);
    }

    /** TFTreasure.generate: a chest at (x, y, z) with four common, two uncommon and one rare item. */
    public boolean generate(OldWorld world, int x, int y, int z) {
        boolean flag = true;
        Random rand = new Random(world.seed() * x + y ^ z);
        world.setBlock(x, y, z, 54, 0);
        OldTile.Chest chest = world.getTile(x, y, z) instanceof OldTile.Chest existing ? existing : null;
        if (chest == null && world.getBlockId(x, y, z) == 54) {
            chest = new OldTile.Chest();
            world.setTile(x, y, z, chest);
        }
        for (int i = 0; i < 4; i++) {
            flag &= addItemToChest(chest, rand, this.getCommonItem(rand));
        }
        for (int i = 0; i < 2; i++) {
            flag &= addItemToChest(chest, rand, this.uncommon.getRandomItem(rand));
        }
        flag &= addItemToChest(chest, rand, this.getRareItem(rand));
        return flag;
    }

    /**
     * The contents generate would put into a new chest at (x, y, z) of a world with this seed (the ur-ghast's chest,
     * made in the running game).
     */
    public OldTile.Chest contents(long seed, int x, int y, int z) {
        Random rand = new Random(seed * x + y ^ z);
        OldTile.Chest chest = new OldTile.Chest();
        for (int i = 0; i < 4; i++) {
            addItemToChest(chest, rand, this.getCommonItem(rand));
        }
        for (int i = 0; i < 2; i++) {
            addItemToChest(chest, rand, this.uncommon.getRandomItem(rand));
        }
        addItemToChest(chest, rand, this.getRareItem(rand));
        return chest;
    }

    private OldStack getCommonItem(Random rand) {
        return !this.useless.isEmpty() && rand.nextInt(4) == 0 ? this.useless.getRandomItem(rand) : this.common.getRandomItem(rand);
    }

    private OldStack getRareItem(Random rand) {
        return !this.ultrarare.isEmpty() && rand.nextInt(4) == 0 ? this.ultrarare.getRandomItem(rand) : this.rare.getRandomItem(rand);
    }

    private static boolean addItemToChest(OldTile.Chest chest, Random rand, OldStack stack) {
        if (chest != null) {
            for (int i = 0; i < 100; i++) {
                int slot = rand.nextInt(chest.slots().length);
                if (chest.slots()[slot] == null) {
                    chest.slots()[slot] = stack;
                    return true;
                }
            }
        }
        return false;
    }

    /** TFTreasure.fill (item ids of 1.5.2, Twilight Forest items with their TwilightForest.cfg id + 256). */
    private void fill(int type) {
        this.useless.add(38, 0, 4);
        this.useless.add(37, 0, 4);
        this.useless.add(288, 0, 3);
        this.useless.add(295, 0, 2);
        this.useless.add(318, 0, 2);
        this.useless.add(81, 0, 2);
        this.useless.add(338, 0, 4);
        this.useless.add(12, 0, 4);
        this.useless.add(390, 0, 1);
        this.useless.add(351, 0, 1);
        if (type == 1) {
            this.common.add(265, 0, 4);
            this.common.add(296, 0, 4);
            this.common.add(287, 0, 4);
            this.common.add(325, 0, 1);
            this.uncommon.add(297, 0, 1);
            this.uncommon.add(7557, 0, 1);
            this.uncommon.add(289, 0, 4);
            this.uncommon.add(262, 0, 12);
            this.uncommon.add(50, 0, 12);
            this.rare.add(266, 0, 3);
            this.rare.add(257, 0, 1);
            this.rare.add(7544, 0, 3);
            this.ultrarare.add(7578, 0, 12);
            this.ultrarare.add(264, 0, 1);
            this.ultrarare.add(7570, 0, 3);
        }
        if (type == 2) {
            this.common.add(265, 0, 4);
            this.common.add(391, 0, 4);
            this.common.add(65, 0, 6);
            this.common.add(325, 0, 1);
            this.uncommon.add(393, 0, 2);
            this.uncommon.add(7557, 0, 1);
            this.uncommon.add(7535, 0, 4);
            this.uncommon.add(262, 0, 12);
            this.uncommon.add(50, 0, 12);
            this.rare.add(7556, 0, 1);
            this.rare.add(1374, 0, 1);
            this.rare.add(7578, 0, 12);
            this.ultrarare.add(7560, 0, 1);
            this.ultrarare.add(388, 0, 6);
            this.ultrarare.add(264, 0, 1);
            this.ultrarare.add(7513, 0, 1);
        }
        if (type == 3) {
            this.common.add(371, 0, 9);
            this.common.add(392, 0, 4);
            this.common.add(349, 0, 4);
            this.common.add(7576, 0, 5);
            this.uncommon.add(400, 0, 1);
            this.uncommon.add(7557, 0, 1);
            this.uncommon.add(289, 0, 4);
            this.uncommon.add(262, 0, 12);
            this.uncommon.add(50, 0, 12);
            this.uncommon.add(7570, 0, 4);
            this.rare.add(7556, 0, 1);
            this.rare.addEnchanted(new OldStack(7537, 0, 1), 32, 1, 35, 1);
            this.rare.add(7578, 0, 12);
            this.ultrarare.add(7555, 0, 1);
            this.ultrarare.add(1368, 0, 1, 4);
            this.ultrarare.add(264, 0, 2);
            this.ultrarare.add(7513, 0, 1);
            this.ultrarare.add(7510, 0, 1);
        }
        if (type == 4) {
            this.common.add(5, 0, 4);
            this.common.add(39, 0, 4);
            this.common.add(40, 0, 4);
            this.common.add(296, 0, 4);
            this.common.add(287, 0, 4);
            this.common.add(280, 0, 6);
            this.uncommon.add(360, 0, 4);
            this.uncommon.add(362, 0, 4);
            this.uncommon.add(361, 0, 4);
            this.uncommon.add(262, 0, 12);
            this.uncommon.add(1353, 0, 4);
            this.rare.add(30, 0, 3);
            this.rare.add(359, 0, 1);
            this.rare.add(329, 0, 1);
            this.rare.add(261, 0, 1);
            this.rare.add(260, 0, 2);
            this.ultrarare.add(293, 0, 1);
            this.ultrarare.add(264, 0, 1);
            this.ultrarare.add(282, 0, 1);
            this.ultrarare.add(322, 0, 1);
        }
        if (type == 5) {
            this.useless.clear();
            this.common.add(265, 0, 4);
            this.common.add(7549, 0, 12);
            this.common.add(289, 0, 4);
            this.common.add(7535, 0, 4);
            this.common.add(1353, 0, 5);
            this.common.add(335, 0, 1);
            this.uncommon.add(7570, 0, 6);
            this.uncommon.add(7571, 0, 1);
            this.uncommon.add(7573, 0, 1);
            this.uncommon.add(7568, 0, 1);
            this.uncommon.add(7567, 0, 1);
            this.uncommon.add(7572, 0, 1);
            this.uncommon.add(7538, 0, 1);
            this.uncommon.add(7541, 0, 1);
            this.uncommon.add(7510, 0, 1);
            this.rare.add(7548, 0, 1);
            this.rare.add(46, 0, 3);
            this.rare.add(373, 16373, 1);
        }
        if (type == 6) {
            this.common.add(280, 0, 12);
            this.common.add(263, 0, 12);
            this.common.add(262, 0, 12);
            this.common.add(7549, 0, 9);
            this.common.add(339, 0, 12);
            this.common.add(334, 0, 4);
            this.common.add(282, 0, 1);
            this.uncommon.add(335, 0, 1);
            this.uncommon.add(339, 0, 5);
            this.uncommon.add(265, 0, 6);
            this.uncommon.add(7535, 0, 8);
            this.uncommon.add(1353, 0, 5);
            this.uncommon.add(7510, 0, 1);
            this.rare.add(7570, 0, 8);
            this.rare.add(322, 0, 1);
            this.rare.add(369, 0, 2);
        }
        if (type == 10) {
            this.useless.clear();
            this.common.add(265, 0, 9);
            this.common.add(388, 0, 5);
            this.common.add(7549, 0, 12);
            this.common.add(7535, 0, 9);
            this.common.add(373, 16369, 1);
            this.common.add(373, 16373, 1);
            this.common.add(373, 16370, 1);
            this.uncommon.addEnchanted(new OldStack(261, 0, 1), 51, 1, 49, 2);
            this.uncommon.addEnchanted(new OldStack(261, 0, 1), 48, 3, 50, 1);
            this.uncommon.addEnchanted(new OldStack(7574, 0, 1), 32, 4, 34, 2);
            this.uncommon.addEnchanted(new OldStack(7566, 0, 1), 32, 5);
            this.uncommon.add(7570, 0, 12);
            this.uncommon.addEnchanted(new OldStack(7573, 0, 1), 0, 3);
            this.uncommon.addEnchanted(new OldStack(7571, 0, 1), 1, 4);
            this.uncommon.addEnchanted(new OldStack(7567, 0, 1), 0, 2);
            this.uncommon.addEnchanted(new OldStack(7568, 0, 1), 5, 3);
            this.rare.add(133, 0, 1);
            this.rare.add(130, 0, 1);
            this.rare.addEnchanted(new OldStack(7572, 0, 1), 32, 4, 33, 1);
            this.rare.addEnchanted(new OldStack(7575, 0, 1), 16, 4, 19, 2);
            this.rare.addEnchanted(new OldStack(7575, 0, 1), 18, 5, 20, 2);
            this.rare.addEnchanted(new OldStack(7550, 0, 1), 32, 4, 34, 3, 35, 2);
        }
        if (type == 7) {
            this.common.add(374, 0, 6);
            this.common.add(373, 0, 1);
            this.common.add(353, 0, 5);
            this.common.add(375, 0, 3);
            this.common.add(370, 0, 1);
            this.common.add(378, 0, 2);
            this.common.add(376, 0, 1);
            this.common.add(382, 0, 2);
            this.common.add(377, 0, 3);
            this.common.add(339, 0, 6);
            this.uncommon.addRandomEnchanted(283, 10);
            this.uncommon.addRandomEnchanted(317, 7);
            this.uncommon.add(373, 16274, 1);
            this.uncommon.add(373, 16341, 1);
            this.uncommon.add(373, 16307, 1);
            this.uncommon.add(373, 16348, 1);
            this.rare.addRandomEnchanted(314, 18);
            this.rare.add(373, 16306, 1);
            this.rare.add(373, 16305, 1);
            this.rare.add(373, 32725, 1);
            this.rare.add(373, 32764, 1);
            this.rare.add(7578, 0, 12);
            this.rare.add(7513, 0, 1);
            this.rare.add(7510, 0, 1);
            this.ultrarare.addRandomEnchanted(286, 20);
            this.ultrarare.add(368, 0, 1);
            this.ultrarare.add(49, 0, 4);
            this.ultrarare.add(264, 0, 1);
            this.ultrarare.add(7555, 0, 1);
            this.ultrarare.add(7560, 0, 1);
        }
        if (type == 8) {
            this.common.add(374, 0, 6);
            this.common.add(373, 0, 1);
            this.common.add(65, 0, 6);
            this.common.add(339, 0, 6);
            this.common.add(352, 0, 6);
            this.common.add(371, 0, 6);
            this.common.add(337, 0, 12);
            this.uncommon.addRandomEnchanted(308, 5);
            this.uncommon.add(385, 0, 3);
            this.uncommon.add(340, 0, 5);
            this.uncommon.add(395, 0, 1);
            this.uncommon.add(373, 16, 1);
            this.uncommon.add(373, 16276, 1);
            this.uncommon.add(373, 16312, 1);
            this.rare.addRandomEnchanted(261, 5);
            this.rare.addRandomEnchanted(272, 10);
            this.rare.addRandomEnchanted(268, 15);
            this.rare.add(373, 32696, 1);
            this.rare.add(373, 16369, 1);
            this.rare.add(373, 16373, 1);
            this.rare.add(373, 16370, 1);
            this.rare.add(7578, 0, 12);
            this.rare.add(7510, 0, 1);
            this.ultrarare.addRandomEnchanted(285, 10);
            this.ultrarare.addRandomEnchanted(267, 20);
            this.ultrarare.addRandomEnchanted(261, 30);
            this.ultrarare.add(47, 0, 5);
            this.ultrarare.add(368, 0, 2);
            this.ultrarare.add(384, 0, 6);
        }
        if (type == 9) {
            this.common.add(373, 0, 1);
            this.common.add(367, 0, 6);
            this.common.add(394, 0, 2);
            this.common.add(296, 0, 6);
            this.common.add(392, 0, 6);
            this.common.add(391, 0, 6);
            this.common.add(360, 0, 6);
            this.common.add(326, 0, 1);
            this.common.add(50, 0, 12);
            this.common.add(282, 0, 1);
            this.common.add(335, 0, 1);
            this.common.add(362, 0, 5);
            this.uncommon.add(297, 0, 8);
            this.uncommon.add(364, 0, 6);
            this.uncommon.add(320, 0, 8);
            this.uncommon.add(393, 0, 8);
            this.uncommon.add(366, 0, 10);
            this.uncommon.add(350, 0, 8);
            this.rare.add(382, 0, 12);
            this.rare.add(260, 0, 12);
            this.rare.add(395, 0, 1);
            this.rare.add(7510, 0, 1);
            this.ultrarare.add(322, 0, 2);
            this.ultrarare.add(396, 0, 2);
            this.ultrarare.add(354, 0, 1);
            this.ultrarare.add(333, 0, 1);
            this.ultrarare.add(1368, 4, 1);
        }
        if (type == 11) {
            this.common.add(280, 0, 12);
            this.common.add(263, 1, 12);
            this.common.add(262, 0, 12);
            this.common.add(7519, 0, 9);
            this.common.add(35, 14, 1);
            this.common.add(331, 0, 6);
            this.uncommon.add(123, 0, 3);
            this.uncommon.add(265, 0, 6);
            this.uncommon.add(7535, 0, 8);
            this.uncommon.add(1353, 0, 5);
            this.uncommon.add(7510, 0, 1);
            this.rare.add(7570, 0, 8);
            this.rare.add(264, 0, 2);
        }
        if (type == 12) {
            this.useless.clear();
            this.common.add(265, 0, 4);
            this.common.add(7519, 0, 12);
            this.common.add(289, 0, 4);
            this.common.add(7535, 0, 4);
            this.common.add(1353, 0, 5);
            this.common.add(331, 0, 12);
            this.common.add(348, 0, 12);
            this.uncommon.add(7570, 0, 6);
            this.uncommon.add(7571, 0, 1);
            this.uncommon.add(7573, 0, 1);
            this.uncommon.add(7568, 0, 1);
            this.uncommon.add(7567, 0, 1);
            this.uncommon.add(7572, 0, 1);
            this.uncommon.add(7538, 0, 1);
            this.uncommon.add(7541, 0, 1);
            this.uncommon.add(7510, 0, 1);
            this.rare.add(7513, 0, 1);
            this.rare.add(new OldStack(403, 0, 1, 2, 3));
            this.rare.add(new OldStack(403, 0, 1, 19, 2));
            this.rare.add(new OldStack(403, 0, 1, 32, 3));
        }
        if (type == 13) {
            this.useless.clear();
            this.common.add(7509, 0, 3);
            this.uncommon.add(7521, 0, 5);
            this.rare.add(7530, 3, 1);
        }
        if (type == 14) {
            this.common.add(394, 0, 2);
            this.common.add(296, 0, 6);
            this.common.add(392, 0, 6);
            this.common.add(391, 0, 6);
            this.common.add(360, 0, 6);
            this.common.add(326, 0, 1);
            this.common.add(335, 0, 1);
            this.common.add(362, 0, 5);
            this.uncommon.add(1353, 0, 12);
            this.uncommon.add(1368, 0, 4);
            this.uncommon.add(1368, 1, 4);
            this.uncommon.add(1368, 2, 4);
            this.uncommon.add(1368, 3, 4);
            this.rare.add(400, 0, 12);
            this.rare.add(260, 0, 12);
            this.rare.add(7513, 0, 1);
            this.rare.add(7510, 0, 1);
            this.ultrarare.add(1368, 4, 1);
            this.ultrarare.add(1368, 5, 1);
            this.ultrarare.add(1368, 6, 1);
            this.ultrarare.add(1368, 7, 1);
            this.ultrarare.add(1368, 8, 1);
        }
        if (type == 15) {
            this.common.add(280, 0, 12);
            this.common.add(263, 0, 12);
            this.common.add(262, 0, 12);
            this.common.add(7549, 0, 9);
            this.common.add(35, 11, 1);
            this.common.add(265, 0, 2);
            this.uncommon.add(325, 0, 1);
            this.uncommon.add(339, 0, 5);
            this.uncommon.add(265, 0, 6);
            this.uncommon.add(7535, 0, 6);
            this.uncommon.add(1353, 0, 5);
            this.uncommon.add(7510, 0, 1);
            this.uncommon.add(7507, 0, 3);
            this.rare.add(7542, 0, 8);
            this.rare.addRandomEnchanted(261, 20);
            this.rare.addRandomEnchanted(267, 20);
            this.rare.addRandomEnchanted(7541, 15);
            this.rare.addRandomEnchanted(7575, 10);
        }
        if (type == 16) {
            this.useless.clear();
            this.common.add(265, 0, 4);
            this.common.add(7549, 0, 12);
            this.common.add(289, 0, 4);
            this.common.add(7535, 0, 4);
            this.common.add(1353, 0, 5);
            this.common.add(335, 0, 1);
            this.uncommon.add(7570, 0, 6);
            this.uncommon.add(7571, 0, 1);
            this.uncommon.add(7573, 0, 1);
            this.uncommon.add(7568, 0, 1);
            this.uncommon.add(7567, 0, 1);
            this.uncommon.add(7572, 0, 1);
            this.uncommon.add(7538, 0, 1);
            this.uncommon.add(7541, 0, 1);
            this.uncommon.add(7510, 0, 1);
            this.rare.add(7548, 0, 1);
            this.rare.addRandomEnchanted(261, 30);
            this.rare.addRandomEnchanted(267, 30);
            this.rare.addRandomEnchanted(7541, 25);
            this.rare.addRandomEnchanted(7575, 20);
            this.rare.addRandomEnchanted(276, 15);
        }
    }

    /** TFTreasureTable with TFTreasureItem: every entry of rarity 10, the count drawn from 1 to its quantity. */
    private static final class Table {
        private final List<OldStack> list = new ArrayList<>();
        private final List<Integer> rarity = new ArrayList<>();
        private final List<Integer> randomLevel = new ArrayList<>();

        void add(int itemId, int damage, int quantity) {
            this.add(itemId, damage, quantity, 10);
        }

        void add(int itemId, int damage, int quantity, int weight) {
            this.add(new OldStack(itemId, damage, quantity), weight, 0);
        }

        void add(OldStack stack) {
            this.add(stack, 10, 0);
        }

        private void add(OldStack stack, int weight, int level) {
            this.list.add(stack);
            this.rarity.add(weight);
            this.randomLevel.add(level);
        }

        /** addEnchanted: ItemStack.addEnchantment for each pair of enchantment id and level. */
        void addEnchanted(OldStack stack, int... pairs) {
            for (int i = 0; i < pairs.length; i += 2) {
                stack = stack.withEnchantment(pairs[i], pairs[i + 1]);
            }
            this.add(stack);
        }

        /** addRandomEnchanted: one item, enchanted at random with this level when drawn. */
        void addRandomEnchanted(int itemId, int level) {
            this.add(new OldStack(itemId, 0, 1), 10, level);
        }

        void clear() {
            this.list.clear();
            this.rarity.clear();
            this.randomLevel.clear();
        }

        boolean isEmpty() {
            return this.list.isEmpty();
        }

        OldStack getRandomItem(Random rand) {
            int total = 0;
            for (int r : this.rarity) {
                total += r;
            }
            int value = rand.nextInt(total);
            for (int i = 0; i < this.list.size(); i++) {
                int r = this.rarity.get(i);
                if (r > value) {
                    OldStack stack = this.list.get(i);
                    stack = stack.withCount(rand.nextInt(stack.count()) + 1);
                    // isItemEnchantable: an item that is not enchanted yet, with a stack size of 1
                    if (this.randomLevel.get(i) > 0 && stack.applied().length == 0) {
                        stack = OldEnchanting.addRandomEnchantment(rand, stack, this.randomLevel.get(i));
                    }
                    return stack;
                }
                value -= r;
            }
            return null;
        }
    }
}
