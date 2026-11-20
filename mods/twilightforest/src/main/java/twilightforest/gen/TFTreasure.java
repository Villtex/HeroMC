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
 * <p>Teil 24b fills the tables of the decoration (basement 9: wells and foundations; tree cache 14: leaf dungeons of
 * the hollow trees). The tables of the buildings follow with Teil 24d/24e.
 */
public final class TFTreasure {
    /** TFCreatures.getSpawnerNameFor: the mob names of the pack (creatureCompatibility off). */
    public static final String SPAWNER_PREFIX = "TwilightForest.";

    static final int CHARM_OF_LIFE_1 = 7257 + 256;
    static final int CHARM_OF_KEEPING_1 = 7254 + 256;

    public static final TFTreasure BASEMENT = new TFTreasure(9);
    public static final TFTreasure TREE_CACHE = new TFTreasure(14);

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
            this.rare.add(CHARM_OF_KEEPING_1, 0, 1);
            this.ultrarare.add(322, 0, 2);
            this.ultrarare.add(396, 0, 2);
            this.ultrarare.add(354, 0, 1);
            this.ultrarare.add(333, 0, 1);
            this.ultrarare.add(TFGen.SAPLING, 4, 1);
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
            this.uncommon.add(TFGen.FIREFLY, 0, 12);
            this.uncommon.add(TFGen.SAPLING, 0, 4);
            this.uncommon.add(TFGen.SAPLING, 1, 4);
            this.uncommon.add(TFGen.SAPLING, 2, 4);
            this.uncommon.add(TFGen.SAPLING, 3, 4);
            this.rare.add(400, 0, 12);
            this.rare.add(260, 0, 12);
            this.rare.add(CHARM_OF_LIFE_1, 0, 1);
            this.rare.add(CHARM_OF_KEEPING_1, 0, 1);
            this.ultrarare.add(TFGen.SAPLING, 4, 1);
            this.ultrarare.add(TFGen.SAPLING, 5, 1);
            this.ultrarare.add(TFGen.SAPLING, 6, 1);
            this.ultrarare.add(TFGen.SAPLING, 7, 1);
            this.ultrarare.add(TFGen.SAPLING, 8, 1);
        }
    }

    /** TFTreasureTable with TFTreasureItem: every entry of rarity 10, the count drawn from 1 to its quantity. */
    private static final class Table {
        private final List<OldStack> list = new ArrayList<>();
        private final List<Integer> rarity = new ArrayList<>();

        void add(int itemId, int damage, int quantity) {
            this.add(itemId, damage, quantity, 10);
        }

        void add(int itemId, int damage, int quantity, int weight) {
            this.list.add(new OldStack(itemId, damage, quantity));
            this.rarity.add(weight);
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
                    return stack.withCount(rand.nextInt(stack.count()) + 1);
                }
                value -= r;
            }
            return null;
        }
    }
}
