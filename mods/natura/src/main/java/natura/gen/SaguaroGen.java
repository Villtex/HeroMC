package natura.gen;

import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * SaguaroGen: a saguaro cactus, small with up to four arms or (one in twenty) the big candelabra.
 *
 * <p>The original set its blocks with {@code world.setBlock(x, y, z, id)}, that is with neighbour updates. Three
 * of them change the result and are reproduced: a saguaro block that stands on something other than saguaro,
 * sand or air breaks, a vanilla cactus next to a saguaro block breaks, and reed above a replaced reed breaks.
 * (Sand that would start to fall and water that would start to flow are not.)
 */
public class SaguaroGen extends NaturaGen {
    final boolean useHeight;

    public SaguaroGen(int blockID, int metadata, boolean notify) {
        super(true);
        this.useHeight = notify;
    }

    /** {@code block.canSustainPlant(..., saguaro)}: the saguaro is a desert plant, only sand carries it. */
    private static boolean soil(int id) {
        return id == SAND;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int yPos = this.findGround(world, x, y, z, this.useHeight);
        int currentID = world.getBlockId(x, yPos, z);
        if (currentID != 0) {
            if (currentID != NaturaIds.SAGUARO) {
                return false;
            }
            if (!soil(world.getBlockId(x, yPos - 1, z))) {
                return false;
            }
        } else {
            if (!soil(world.getBlockId(x, yPos - 1, z))) {
                return false;
            }
        }

        if (random.nextInt(20) == 0) {
            this.generateCactusTree(world, random, x, yPos, z);
        } else {
            this.generateSmallCactus(world, random, x, yPos, z);
        }
        return true;
    }

    void generateCactusTree(OldWorld world, Random random, int x, int y, int z) {
        for (int i = 0; i < 6; i++) {
            this.genBlock(world, x, y + i, z);
        }
        this.genBlock(world, x + 1, y + 2, z);
        this.genBlock(world, x - 1, y + 2, z);
        this.genBlock(world, x, y + 2, z + 1);
        this.genBlock(world, x, y + 2, z - 1);
        for (int height = 0; height < 2; height++) {
            this.genBlock(world, x + 2, y + height + 2, z);
            this.genBlock(world, x - 2, y + height + 2, z);
            this.genBlock(world, x, y + height + 2, z + 2);
            this.genBlock(world, x, y + height + 2, z - 2);
            this.genBlock(world, x + 3, y + height + 3, z);
            this.genBlock(world, x - 3, y + height + 3, z);
            this.genBlock(world, x, y + height + 3, z + 3);
            this.genBlock(world, x, y + height + 3, z - 3);
            this.genBlock(world, x + 1, y + height + 5, z);
            this.genBlock(world, x - 1, y + height + 5, z);
            this.genBlock(world, x, y + height + 5, z + 1);
            this.genBlock(world, x, y + height + 5, z - 1);
        }
    }

    void generateSmallCactus(OldWorld world, Random random, int x, int y, int z) {
        int height = random.nextInt(4) + 3;
        for (int iter = 0; iter < height; iter++) {
            this.genBlock(world, x, y + iter, z);
        }
        int branchY = height >= 5 ? 2 : 1;
        y += branchY;
        if (random.nextBoolean()) {
            int size = random.nextInt(height - branchY) + branchY - random.nextInt(3);
            for (int branch = 0; branch < size; branch++) {
                this.genBlock(world, x + 1, y + branch, z);
            }
        }
        if (random.nextBoolean()) {
            int size = random.nextInt(height - branchY) + branchY - random.nextInt(3);
            for (int branch = 0; branch < size; branch++) {
                this.genBlock(world, x - 1, y + branch, z);
            }
        }
        if (random.nextBoolean()) {
            int size = random.nextInt(height - branchY) + branchY - random.nextInt(3);
            for (int branch = 0; branch < size; branch++) {
                this.genBlock(world, x, y + branch, z + 1);
            }
        }
        if (random.nextBoolean()) {
            int size = random.nextInt(height - branchY) + branchY - random.nextInt(3);
            for (int branch = 0; branch < size; branch++) {
                this.genBlock(world, x, y + branch, z - 1);
            }
        }
    }

    void genBlock(OldWorld world, int x, int y, int z) {
        if (!opaqueCube(world.getBlockId(x, y, z))) {
            setWithUpdate(world, x, y, z, NaturaIds.SAGUARO);
        }
    }

    /** World.setBlock with flag 3: a block that changed tells its six neighbours. */
    private static void setWithUpdate(OldWorld world, int x, int y, int z, int id) {
        if (world.setBlock(x, y, z, id, 0)) {
            neighbourChanged(world, x - 1, y, z);
            neighbourChanged(world, x + 1, y, z);
            neighbourChanged(world, x, y - 1, z);
            neighbourChanged(world, x, y + 1, z);
            neighbourChanged(world, x, y, z - 1);
            neighbourChanged(world, x, y, z + 1);
        }
    }

    /** onNeighborBlockChange of SaguaroBlock and BlockCactus: what cannot stay drops. */
    private static void neighbourChanged(OldWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        if (id == NaturaIds.SAGUARO) {
            int below = world.getBlockId(x, y - 1, z);
            if (below != NaturaIds.SAGUARO && below != SAND && below != 0) {
                setWithUpdate(world, x, y, z, 0);
            }
        } else if (id == 81) {
            if (!OldPlantGens.cactusCanStay(world, x, y, z)) {
                setWithUpdate(world, x, y, z, 0);
            }
        } else if (id == 83) {
            // BlockReed: a saguaro block that replaced the reed below takes the rest of the stalk with it
            if (!OldPlantGens.reedCanStay(world, x, y, z)) {
                setWithUpdate(world, x, y, z, 0);
            }
        }
    }

    int findGround(OldWorld world, int x, int y, int z, boolean useHeight) {
        if (useHeight) {
            return y;
        }
        boolean foundGround = false;
        int height = NaturaIds.SEA_LEVEL + 64;
        do {
            int underID = world.getBlockId(x, --height, z);
            if (underID == DIRT || underID == GRASS || height < NaturaIds.SEA_LEVEL) {
                foundGround = true;
            }
        } while (!foundGround);
        return height + 1;
    }
}
