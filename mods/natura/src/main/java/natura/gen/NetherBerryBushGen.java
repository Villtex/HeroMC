package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * NetherBerryBushGen: the same patches as {@link BerryBushGen} with the Nether berry bush, on netherrack, soul
 * sand or tainted soil. (The shrub shape still asks for dirt or grass, as in the original.)
 */
public class NetherBerryBushGen extends BerryBushGen {
    public NetherBerryBushGen(int meta) {
        super(meta, 0);
    }

    @Override
    int bushId() {
        return NaturaIds.NETHER_BERRY_BUSH;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int height = this.findGround(world, x, y, z);
        if (height != -1) {
            this.generateNode(world, random, x, height, z);
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@code id == netherrack || block.canSustainPlant(..., netherBerryBush)}: the bush is a Nether plant, which
     * soul sand and tainted soil carry.
     */
    private static boolean soil(int id) {
        return netherSoil(id);
    }

    @Override
    int findGround(OldWorld world, int x, int y, int z) {
        int returnHeight = -1;
        int blockID = world.getBlockId(x, y - 1, z);
        if (blockID == 0 || opaqueCube(world.getBlockId(x, y, z)) || blockID != NETHERRACK && !soil(blockID)) {
            int height = y;
            do {
                int bID = world.getBlockId(x, height, z);
                if (bID != 0 && (bID == NETHERRACK || soil(bID))) {
                    if (!opaqueCube(world.getBlockId(x, height + 1, z))) {
                        returnHeight = height + 1;
                    }
                    break;
                }
            } while (--height > 0);
            return returnHeight;
        } else {
            return y;
        }
    }
}
