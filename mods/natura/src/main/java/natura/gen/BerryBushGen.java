package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** BerryBushGen: a patch of one of the four overworld berry bushes. */
public class BerryBushGen extends NaturaGen {
    private final int metadata;
    private final int spawnHeight;

    public BerryBushGen(int meta, int range) {
        this.metadata = meta;
        this.spawnHeight = range;
    }

    /** The block the bushes are made of. */
    int bushId() {
        return NaturaIds.BERRY_BUSH;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int height = this.findGround(world, x, y, z);
        if (height != -1) {
            this.generateNode(world, random, x, height, z);
        }
        return true;
    }

    final void generateNode(OldWorld world, Random random, int x, int height, int z) {
        int type = random.nextInt(10);
        if (type == 9) {
            this.generateLargeNode(world, random, x, height, z);
        } else if (type >= 7) {
            this.generateShrub(world, random, x, height, z);
        } else if (type >= 3) {
            this.generateSmallNode(world, random, x, height, z);
        } else {
            this.generateTinyNode(world, random, x, height, z);
        }
    }

    int findGround(OldWorld world, int x, int y, int z) {
        int returnHeight = -1;
        int blockID = world.getBlockId(x, y - 1, z);
        if (opaqueCube(world.getBlockId(x, y, z)) || blockID != DIRT && blockID != GRASS) {
            int height = this.spawnHeight;
            while (height >= NaturaIds.SEA_LEVEL) {
                int j1 = world.getBlockId(x, height, z);
                if (j1 == DIRT || j1 == GRASS) {
                    if (!opaqueCube(world.getBlockId(x, height + 1, z))) {
                        returnHeight = height + 1;
                    }
                    break;
                }
                if (--height <= 0) {
                    break;
                }
            }
            return returnHeight;
        } else {
            return y;
        }
    }

    public void generateLargeNode(OldWorld world, Random random, int x, int y, int z) {
        for (int iterX = x - 2; iterX <= x + 2; iterX++) {
            for (int iterZ = z - 1; iterZ <= z + 1; iterZ++) {
                for (int iterY = y - 1; iterY <= y; iterY++) {
                    this.generateBerryBlock(world, iterX, iterY, iterZ, random);
                }
            }
        }
        for (int iterX = x - 1; iterX <= x + 1; iterX++) {
            for (int iterZ = z - 2; iterZ <= z - 2; iterZ++) {
                for (int iterY = y - 1; iterY <= y; iterY++) {
                    this.generateBerryBlock(world, iterX, iterY, iterZ, random);
                }
            }
        }
        for (int iterX = x - 1; iterX <= x + 1; iterX++) {
            for (int iterZ = z + 2; iterZ <= z + 2; iterZ++) {
                for (int iterY = y - 1; iterY <= y; iterY++) {
                    this.generateBerryBlock(world, iterX, iterY, iterZ, random);
                }
            }
        }
        for (int iterX = x - 1; iterX <= x + 1; iterX++) {
            for (int iterZ = z - 1; iterZ <= z + 1; iterZ++) {
                int yPos = y + 1;
                this.generateBerryBlock(world, iterX, yPos, iterZ, random);
                yPos = y - 2;
                this.generateBerryBlock(world, iterX, yPos, iterZ, random);
            }
        }
    }

    public void generateShrub(OldWorld world, Random random, int x, int y, int z) {
        int id;
        do {
            id = world.getBlockId(x, y, z);
        } while ((id == 0 || isLeaves(id)) && --y > 0);

        int i1 = world.getBlockId(x, y, z);
        if (i1 == DIRT || i1 == GRASS) {
            for (int yPos = ++y; yPos <= y + 2; yPos++) {
                int k1 = yPos - y;
                int l1 = 2 - k1;
                for (int xPos = x - l1; xPos <= x + l1; xPos++) {
                    int j2 = xPos - x;
                    for (int zPos = z - l1; zPos <= z + l1; zPos++) {
                        int l2 = zPos - z;
                        int block = world.getBlockId(xPos, yPos, zPos);
                        if ((Math.abs(j2) != l1 || Math.abs(l2) != l1 || random.nextInt(2) != 0) && replaceable(block)) {
                            this.generateBerryBlock(world, xPos, yPos, zPos, random);
                        }
                    }
                }
            }
        }
    }

    public void generateSmallNode(OldWorld world, Random random, int x, int y, int z) {
        this.generateBerryBlock(world, x, y, z, random);
        if (random.nextBoolean()) {
            this.generateBerryBush(world, x + 1, y, z, random);
        }
        if (random.nextBoolean()) {
            this.generateBerryBush(world, x - 1, y, z, random);
        }
        if (random.nextBoolean()) {
            this.generateBerryBush(world, x, y, z + 1, random);
        }
        if (random.nextBoolean()) {
            this.generateBerryBush(world, x, y, z - 1, random);
        }
    }

    public void generateTinyNode(OldWorld world, Random random, int x, int y, int z) {
        this.generateBerryBush(world, x, y, z, random);
    }

    void generateBerryBlock(OldWorld world, int x, int y, int z, Random random) {
        if (!opaqueCube(world.getBlockId(x, y, z))) {
            int metaOffset = random.nextInt(5) == 0 ? 1 : 0;
            this.setBlockAndMetadata(world, x, y, z, this.bushId(), this.metadata + 8 + metaOffset * 4);
        }
    }

    void generateBerryBush(OldWorld world, int x, int y, int z, Random random) {
        if (!opaqueCube(world.getBlockId(x, y, z))) {
            int metaOffset = random.nextInt(4);
            this.setBlockAndMetadata(world, x, y, z, this.bushId(), this.metadata + metaOffset * 4);
        }
    }
}
