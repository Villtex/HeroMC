package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** CloudGen: a drifting line of small cloud lumps. */
public class CloudGen extends NaturaGen {
    private final int bID;
    private final int meta;
    private final int numberOfBlocks;
    private final boolean flatCloud;

    public CloudGen(int blockID, int metadata, int size, boolean flag) {
        this.bID = blockID;
        this.meta = metadata;
        this.numberOfBlocks = size;
        this.flatCloud = flag;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int l = random.nextInt(3) - 1;
        int i1 = random.nextInt(3) - 1;
        for (int j1 = 0; j1 < this.numberOfBlocks; j1++) {
            x += random.nextInt(3) - 1 + l;
            z += random.nextInt(3) - 1 + i1;
            if (random.nextBoolean() && !this.flatCloud || this.flatCloud && random.nextInt(10) == 0) {
                y += random.nextInt(3) - 1;
            }
            // the loop bounds draw a random number every time they are checked
            for (int xIter = x; xIter < x + random.nextInt(4) + 3 * (this.flatCloud ? 3 : 1); xIter++) {
                for (int yIter = y; yIter < y + random.nextInt(1) + 2; yIter++) {
                    for (int zIter = z; zIter < z + random.nextInt(4) + 3 * (this.flatCloud ? 3 : 1); zIter++) {
                        if (world.getBlockId(xIter, yIter, zIter) == 0
                                && Math.abs(xIter - x) + Math.abs(yIter - y) + Math.abs(zIter - z) < 4 * (this.flatCloud ? 3 : 1) + random.nextInt(2)) {
                            this.setBlockAndMetadata(world, xIter, yIter, zIter, this.bID, this.meta);
                        }
                    }
                }
            }
        }
        return true;
    }
}
