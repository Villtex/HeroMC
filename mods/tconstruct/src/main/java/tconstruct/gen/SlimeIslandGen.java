package tconstruct.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

import static tconstruct.gen.TCWorldGen.CRAFTED_SOIL;
import static tconstruct.gen.TCWorldGen.SLIME_GEL;
import static tconstruct.gen.TCWorldGen.SLIME_GRASS;
import static tconstruct.gen.TCWorldGen.SLIME_LEAVES;
import static tconstruct.gen.TCWorldGen.SLIME_POOL;
import static tconstruct.gen.TCWorldGen.SLIME_SAPLING;
import static tconstruct.gen.TCWorldGen.SLIME_TALL_GRASS;

/**
 * SlimeIslandGen (with liquid blue slime, gel metadata 0), SlimeTreeGen and PlantGen of Tinkers' Construct 1.4.3.
 *
 * <p>The original drew the shape of the island, its erosion and its trees from {@code new Random()} without a seed,
 * so islands were not the same for the same seed. Only the rarity check, the slime pool and the tall grass used the
 * shared random sequence. Here that second random is seeded from the world seed and the chunk.
 */
public final class SlimeIslandGen {
    private static final int BASE_ID = CRAFTED_SOIL;
    /** CraftedSoil metadata 5: blue slime dirt. */
    private static final int BASE_META = 5;
    private static final int TOP_ID = SLIME_GRASS;
    private static final int RANDOMNESS = 2;

    private SlimeIslandGen() {
    }

    /** The replacement for the unseeded {@code SlimeIslandGen.random}: one sequence per chunk from the world seed. */
    static Random islandRandom(long seed, int chunkX, int chunkZ) {
        return new Random(seed ^ chunkX * 341873128712L ^ chunkZ * 132897987541L ^ 0x7C0451E15L);
    }

    /** IWorldGenerator.generate: the overworld and dimensions from 2 on (the Twilight Forest). */
    static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        int dim = world.dimension();
        if ((dim == 0 || dim >= 2) && random.nextInt(TCWorldGen.ISLAND_RARITY) == 0) {
            generateIsland(world, random, islandRandom(world.seed(), chunkX, chunkZ), chunkX * 16, chunkZ * 16);
        }
    }

    /** java.awt.geom.Ellipse2D.Double(0, 0, w, h).contains(x, y). */
    private static boolean ellipse(double w, double h, double x, double y) {
        if (w <= 0.0) {
            return false;
        }
        double normx = x / w - 0.5;
        if (h <= 0.0) {
            return false;
        }
        double normy = y / h - 0.5;
        return normx * normx + normy * normy < 0.25;
    }

    static void generateIsland(OldWorld world, Random rand, Random own, int xChunk, int zChunk) {
        int xRange = own.nextInt(13) + 20;
        int yCenter = 100 + own.nextInt(41);
        int zRange = own.nextInt(13) + 20;
        int height = 12;
        int initialHeight = height;

        for (int x = 0; x <= xRange; x++) {
            for (int z = 0; z <= zRange; z++) {
                for (int y = 0; y <= height; y++) {
                    if (ellipse(xRange, zRange, x, z)) {
                        world.setBlock(x + xChunk, y + yCenter, z + zChunk, BASE_ID, BASE_META);
                    }
                }
            }
        }

        int depth = 8;
        for (int x = 0; x <= xRange; x++) {
            for (int z = 0; z <= zRange; z++) {
                for (int y = 0; y >= -depth; y--) {
                    int xPos = x + xChunk;
                    int yPos = y + yCenter + depth;
                    int zPos = z + zChunk;
                    if (world.getBlockId(xPos - 1, yPos + 1, zPos) != BASE_ID
                            || world.getBlockId(xPos + 1, yPos + 1, zPos) != BASE_ID
                            || world.getBlockId(xPos, yPos + 1, zPos - 1) != BASE_ID
                            || world.getBlockId(xPos - 1, yPos + 1, zPos + 1) != BASE_ID
                            || own.nextInt(100) <= RANDOMNESS) {
                        world.setBlock(xPos, yPos, zPos, 0, 0);
                    }
                }
            }
        }

        depth = 3;
        for (int x = 0; x <= xRange; x++) {
            for (int z = 0; z <= zRange; z++) {
                for (int y = 1; y <= depth; y++) {
                    int xPos = x + xChunk;
                    int yPos = y + yCenter + initialHeight - depth + 1;
                    int zPos = z + zChunk;
                    if (world.getBlockId(xPos - 1, yPos - 1, zPos) != BASE_ID
                            || world.getBlockId(xPos + 1, yPos - 1, zPos) != BASE_ID
                            || world.getBlockId(xPos, yPos - 1, zPos - 1) != BASE_ID
                            || world.getBlockId(xPos - 1, yPos - 1, zPos + 1) != BASE_ID) {
                        world.setBlock(xPos, yPos, zPos, 0, 0);
                    }
                }
            }
        }

        for (int x = 0; x <= xRange; x++) {
            for (int z = 0; z <= zRange; z++) {
                for (int y = 0; y <= depth; y++) {
                    int xPos = x + xChunk;
                    int yPos = y + yCenter + initialHeight - depth;
                    int zPos = z + zChunk;
                    if (world.getBlockId(xPos, yPos, zPos) == BASE_ID && world.getBlockId(xPos, yPos + 1, zPos) == 0) {
                        world.setBlock(xPos, yPos, zPos, TOP_ID, 0);
                    }
                }
            }
        }

        slimePool(world, rand, xChunk + xRange / 2, yCenter + initialHeight, zChunk + zRange / 2);
        tallGrass(world, rand, xChunk, yCenter + initialHeight + 1, zChunk, xRange, zRange);

        for (int i = 0; i < 3; i++) {
            int x = xChunk + own.nextInt(xRange);
            int y = yCenter + initialHeight + 3;
            int z = zChunk + own.nextInt(zRange);
            slimeTree(world, own, x, y, z, true);
        }
    }

    /** SlimeIslandGen.generate(World, Random, x, y, z): a lake of liquid slime with gel around it (WorldGenLakes). */
    static boolean slimePool(OldWorld world, Random rand, int x, int y, int z) {
        x -= 8;
        z -= 8;
        y -= 4;
        boolean[] valid = new boolean[2048];
        int blobs = rand.nextInt(4) + 4;
        for (int i = 0; i < blobs; i++) {
            double noise1 = rand.nextDouble() * 6.0 + 3.0;
            double noise2 = rand.nextDouble() * 4.0 + 2.0;
            double noise3 = rand.nextDouble() * 6.0 + 3.0;
            double noise4 = rand.nextDouble() * (16.0 - noise1 - 2.0) + 1.0 + noise1 / 2.0;
            double noise5 = rand.nextDouble() * (8.0 - noise2 - 4.0) + 2.0 + noise2 / 2.0;
            double noise6 = rand.nextDouble() * (16.0 - noise3 - 2.0) + 1.0 + noise3 / 2.0;
            for (int xi = 1; xi < 15; xi++) {
                for (int zi = 1; zi < 15; zi++) {
                    for (int yi = 1; yi < 7; yi++) {
                        double a = (xi - noise4) / (noise1 / 2.0);
                        double b = (yi - noise5) / (noise2 / 2.0);
                        double c = (zi - noise6) / (noise3 / 2.0);
                        if (a * a + b * b + c * c < 1.0) {
                            valid[(xi * 16 + zi) * 8 + yi] = true;
                        }
                    }
                }
            }
        }

        for (int xi = 0; xi < 16; xi++) {
            for (int zi = 0; zi < 16; zi++) {
                for (int yi = 0; yi < 8; yi++) {
                    if (border(valid, xi, zi, yi)) {
                        Material material = world.material(x + xi, y + yi, z + zi);
                        if (yi >= 4 && material.liquid) {
                            return false;
                        }
                        if (yi < 4 && !material.solid && world.getBlockId(x + xi, y + yi, z + zi) != SLIME_POOL) {
                            return false;
                        }
                    }
                }
            }
        }

        for (int xi = 0; xi < 16; xi++) {
            for (int zi = 0; zi < 16; zi++) {
                for (int yi = 0; yi < 8; yi++) {
                    if (valid[(xi * 16 + zi) * 8 + yi]) {
                        world.setBlock(x + xi, y + yi, z + zi, yi >= 4 ? 0 : SLIME_POOL);
                    }
                }
            }
        }

        for (int xi = 0; xi < 16; xi++) {
            for (int zi = 0; zi < 16; zi++) {
                for (int yi = 4; yi < 8; yi++) {
                    if (valid[(xi * 16 + zi) * 8 + yi]
                            && world.getBlockId(x + xi, y + yi - 1, z + zi) == BASE_ID
                            && world.skyLight(x + xi, y + yi, z + zi) > 0) {
                        world.setBlock(x + xi, y + yi - 1, z + zi, TOP_ID, 0);
                    }
                }
            }
        }

        // liquid slime has Material.water
        for (int xi = 0; xi < 16; xi++) {
            for (int zi = 0; zi < 16; zi++) {
                for (int yi = 0; yi < 8; yi++) {
                    if (border(valid, xi, zi, yi)
                            && (yi < 4 || rand.nextInt(2) != 0)
                            && world.material(x + xi, y + yi, z + zi).solid
                            && world.material(x + xi, y + yi + 1, z + zi) != Material.WATER) {
                        world.setBlock(x + xi, y + yi, z + zi, SLIME_GEL, 0);
                    }
                }
            }
        }
        return true;
    }

    private static boolean border(boolean[] valid, int xi, int zi, int yi) {
        return !valid[(xi * 16 + zi) * 8 + yi]
                && (xi < 15 && valid[((xi + 1) * 16 + zi) * 8 + yi]
                || xi > 0 && valid[((xi - 1) * 16 + zi) * 8 + yi]
                || zi < 15 && valid[(xi * 16 + zi + 1) * 8 + yi]
                || zi > 0 && valid[(xi * 16 + (zi - 1)) * 8 + yi]
                || yi < 7 && valid[(xi * 16 + zi) * 8 + yi + 1]
                || yi > 0 && valid[(xi * 16 + zi) * 8 + (yi - 1)]);
    }

    /** PlantGen(slimeTallGrass, 0, 128, xRange, 1, zRange, clumped = false). */
    static void tallGrass(OldWorld world, Random random, int x, int y, int z, int xSize, int zSize) {
        for (int iter = 0; iter < 128; iter++) {
            int posX = x + random.nextInt(xSize);
            int posY = y + random.nextInt(1);
            int posZ = z + random.nextInt(zSize);
            if (world.isAirBlock(posX, posY, posZ) && (world.dimension() != -1 || posY < 127)
                    && tallGrassStays(world, posX, posY, posZ)) {
                world.setBlock(posX, posY, posZ, SLIME_TALL_GRASS, 0);
            }
        }
    }

    /**
     * BlockFlower.canBlockStay (Forge): light 8 or the sky, and a soil that takes a Plains plant: grass and dirt, and
     * slime grass (SlimeGrass.canSustainPlant: Plains, not vanilla tall grass).
     */
    private static boolean tallGrassStays(OldWorld world, int x, int y, int z) {
        if (world.skyLight(x, y, z) < 8 && !world.canBlockSeeTheSky(x, y, z)) {
            return false;
        }
        int soil = world.getBlockId(x, y - 1, z);
        return soil == SLIME_GRASS || OldBlockInfo.plainsSoil(soil);
    }

    /**
     * SlimeTreeGen(notify, 5, 4, 1, 0): gel trunk (metadata 1) under a slime leaf canopy. {@code seekHeight} is
     * {@code !notify}: the island looks for its ground, the sapling grows where it stands.
     */
    public static boolean slimeTree(OldWorld world, Random random, int x, int y, int z, boolean seekHeight) {
        int height = random.nextInt(4) + 5;
        if (seekHeight) {
            y = findGround(world, x, y, z);
            if (y == -1) {
                return false;
            }
        }
        if (y >= 1 && y + height + 1 <= 256) {
            int soil = world.getBlockId(x, y - 1, z);
            // canSustainPlant(..., slimeSapling): vanilla grass and dirt (Plains), slime grass; not the slime soil
            if (soil == SLIME_GRASS || OldBlockInfo.plainsSoil(soil)) {
                if (!checkClear(world, x, y, z, height)) {
                    return false;
                }
                if (soil == SLIME_GRASS) {
                    // SlimeGrass.onPlantGrow
                    world.setBlock(x, y - 1, z, CRAFTED_SOIL, BASE_META);
                } else if (soil == 2) {
                    // Block.onPlantGrow of grass
                    world.setBlock(x, y - 1, z, 3, 0);
                }
                placeCanopy(world, x, y, z, height);
                placeTrunk(world, x, y, z, height);
                return true;
            }
        }
        return false;
    }

    /** Checks one block per layer only (the loops of the original run from {@code range} to {@code range}). */
    private static boolean checkClear(OldWorld world, int x, int y, int z, int treeHeight) {
        for (int yPos = 0; yPos < treeHeight + 1; yPos++) {
            int range = 1;
            if (yPos == 0) {
                range = 0;
            } else if (yPos >= treeHeight - 1) {
                range = 2;
            }
            int id = world.getBlockId(x + range, y + yPos, z + range);
            if (id != 0 && id != SLIME_SAPLING && !OldBlockInfo.leaves(id)) {
                return false;
            }
        }
        return true;
    }

    private static int findGround(OldWorld world, int x, int y, int z) {
        int height = y;
        do {
            int id = world.getBlockId(x, height, z);
            if ((id == CRAFTED_SOIL || id == SLIME_GRASS) && !OldBlockInfo.opaqueCube(world.getBlockId(x, height + 1, z))) {
                return height + 1;
            }
        } while (--height > 0);
        return -1;
    }

    private static void placeCanopy(OldWorld world, int x, int y, int z, int height) {
        for (int i = 0; i < 4; i++) {
            diamondLayer(world, x, y + height - i, z, i + 1);
        }
        int top = y + height;
        world.setBlock(x + 4, top - 3, z, 0, 0);
        world.setBlock(x - 4, top - 3, z, 0, 0);
        world.setBlock(x, top - 3, z + 4, 0, 0);
        world.setBlock(x, top - 3, z - 4, 0, 0);
        world.setBlock(x + 1, top - 3, z + 1, 0, 0);
        world.setBlock(x + 1, top - 3, z - 1, 0, 0);
        world.setBlock(x - 1, top - 3, z + 1, 0, 0);
        world.setBlock(x - 1, top - 3, z - 1, 0, 0);
        for (int dy = 4; dy <= 5; dy++) {
            world.setBlock(x + 3, top - dy, z, SLIME_LEAVES, 0);
            world.setBlock(x - 3, top - dy, z, SLIME_LEAVES, 0);
            world.setBlock(x, top - dy, z - 3, SLIME_LEAVES, 0);
            world.setBlock(x, top - dy, z + 3, SLIME_LEAVES, 0);
            world.setBlock(x + 2, top - dy, z + 2, SLIME_LEAVES, 0);
            world.setBlock(x + 2, top - dy, z - 2, SLIME_LEAVES, 0);
            world.setBlock(x - 2, top - dy, z + 2, SLIME_LEAVES, 0);
            world.setBlock(x - 2, top - dy, z - 2, SLIME_LEAVES, 0);
        }
    }

    private static void diamondLayer(OldWorld world, int x, int y, int z, int range) {
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= range) {
                    world.setBlock(dx + x, y, dz + z, SLIME_LEAVES, 0);
                }
            }
        }
    }

    private static void placeTrunk(OldWorld world, int x, int y, int z, int height) {
        for (int h = 0; h < height; h++) {
            int id = world.getBlockId(x, y + h, z);
            if (id == 0 || OldBlockInfo.leaves(id)) {
                world.setBlock(x, y + h, z, SLIME_GEL, 1);
            }
        }
    }
}
