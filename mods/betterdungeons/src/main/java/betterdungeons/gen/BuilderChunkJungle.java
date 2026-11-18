package betterdungeons.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BuilderChunkJungle: the jungle chunk caves (y 35 to 54 in every chunk whose corner lies in "Jungle"). A cellular
 * automaton over the chunk and its eight neighbours, each seeded from the world seed and its position, so the caves
 * run on across chunk borders; then four to eight big mushrooms in the cave.
 */
final class BuilderChunkJungle {
    private boolean[][][] map;
    private boolean[][][] map2;
    private int initChance = 45;
    private final int size = 48;
    private final int height = 20;
    private OldWorld world;
    private final OldBiome biome = OldBiomes.JUNGLE;

    void generate(Random random, OldWorld world, int i, int j, int k) {
        BuilderHelper b = new BuilderHelper(world);
        b.fillMap(world, random, this.getCave(i, k, world), i, 35, k, -1, 0);
        for (int v = random.nextInt(5) + 4; v > 0; v--) {
            int x = random.nextInt(12) + 2;
            int z = random.nextInt(12) + 2;
            boolean m = false;
            for (int y = 35; !m && y < 50; y++) {
                if (world.getBlockId(i + x, y, k + z) == 0) {
                    BuilderHelper.fertilizeMushroom(world, random.nextBoolean() ? 40 : 39, i + x, y, k + z, random);
                    m = true;
                }
            }
        }
    }

    int[][][] getCave(int x, int y, OldWorld world) {
        this.world = world;
        this.generatechunk(x, y);
        this.iterate(12, 14);
        this.iterate(17, 20);
        // clean(map, 1) and clean(map, 2) of the original only set cells to false that already were: no effect
        this.iterate(17, 18);
        int[][][] chunk = new int[16][this.height][16];
        for (int cx = 0; cx < chunk.length; cx++) {
            for (int z = 0; z < chunk[0][0].length; z++) {
                for (int cy = 0; cy < chunk[0].length; cy++) {
                    chunk[cx][cy][z] = this.map[16 + cx][cy][16 + z] ? 0 : -1;
                }
            }
        }
        for (int cx = 0; cx < chunk.length; cx++) {
            for (int z = 0; z < chunk[0][0].length; z++) {
                for (int cy = 0; cy < chunk[0].length - 4; cy++) {
                    if (chunk[cx][cy][z] == -1 && chunk[cx][cy + 1][z] == 0) {
                        chunk[cx][cy][z] = 2;
                    }
                }
            }
        }
        return chunk;
    }

    private void iterate(int rule, int rule2) {
        for (int y = 0; y < this.map[0].length; y++) {
            for (int x = 0; x < this.map.length; x++) {
                for (int z = 0; z < this.map.length; z++) {
                    if (this.map[x][y][z]) {
                        this.map2[x][y][z] = this.checkNeightbours(x, y, z) >= rule;
                    } else {
                        this.map2[x][y][z] = this.checkNeightbours(x, y, z) >= rule2;
                    }
                }
            }
        }
        this.pushBack();
    }

    private int checkNeightbours(int x, int y, int z) {
        int count = 0;
        count += this.checkNumAt(x - 1, y, z) * 2;
        count += this.checkNumAt(x + 1, y, z) * 2;
        count += this.checkNumAt(x, y, z + 1) * 2;
        count += this.checkNumAt(x, y, z - 1) * 2;
        count += this.checkNumAt(x + 1, y, z + 1) * 2;
        count += this.checkNumAt(x + 1, y, z - 1) * 2;
        count += this.checkNumAt(x - 1, y, z + 1) * 2;
        count += this.checkNumAt(x - 1, y, z - 1) * 2;
        count += this.checkNumAt(x, y + 1, z);
        count += this.checkNumAt(x, y - 1, z);
        count += this.checkNumAt(x - 1, y + 1, z);
        count += this.checkNumAt(x + 1, y + 1, z);
        count += this.checkNumAt(x, y + 1, z + 1);
        count += this.checkNumAt(x, y + 1, z - 1);
        count += this.checkNumAt(x - 1, y - 1, z);
        count += this.checkNumAt(x + 1, y - 1, z);
        count += this.checkNumAt(x, y - 1, z + 1);
        return count + this.checkNumAt(x, y - 1, z - 1);
    }

    private int checkNumAt(int x, int y, int z) {
        if (x <= 0) {
            return 1;
        } else if (y <= 0) {
            return 0;
        } else if (z <= 0) {
            return 1;
        } else if (x >= this.map.length - 1) {
            return 1;
        } else if (z >= this.map.length - 1) {
            return 1;
        } else if (y >= this.map[0].length - 1) {
            return 0;
        }
        return this.map[x][y][z] ? 1 : 0;
    }

    private void pushBack() {
        boolean[][][] swap = this.map;
        this.map = this.map2;
        this.map2 = swap;
    }

    private void generatechunk(int cx, int cy) {
        this.map = new boolean[this.size][this.height][this.size];
        this.map2 = new boolean[this.size][this.height][this.size];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                boolean[][][] chunk = this.generateNoiseAt(cx + (x - 1) * 16, cy + (y - 1) * 16);
                for (int bx = 0; bx < 16; bx++) {
                    for (int bz = 0; bz < 16; bz++) {
                        for (int by = 0; by < this.height; by++) {
                            this.map[x * 16 + bx][by][y * 16 + bz] = chunk[bx][by][bz];
                        }
                    }
                }
            }
        }
    }

    private boolean jungle(int x, int z) {
        OldBiome b = this.world.getBiome(x, z);
        return b == this.biome || b == OldBiomes.JUNGLE_HILLS;
    }

    private boolean[][][] generateNoiseAt(int x, int z) {
        Random random = new Random();
        random.setSeed(this.world.seed() + x * x + x + z * z + z * 34952);
        boolean[][][] chunk = new boolean[16][this.height][16];
        this.initChance = 25 + random.nextInt(40);
        if (this.jungle(x, z) && this.jungle(x + 1, z) && this.jungle(x - 1, z) && this.jungle(x, z + 1) && this.jungle(x, z - 1)) {
            for (int y = 0; y < this.height; y++) {
                for (int bx = 0; bx < 16; bx++) {
                    for (int bz = 0; bz < 16; bz++) {
                        chunk[bx][y][bz] = random.nextInt(100) > this.initChance;
                    }
                }
            }
        }
        return chunk;
    }
}
