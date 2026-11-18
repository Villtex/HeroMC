package betterdungeons.gen;

import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BuilderChunkSwamp: the swamp chunk caves (y 34 to 53 in every chunk whose corner lies in "Swampland"): a flat
 * cellular automaton over the chunk and its eight neighbours; still water in the lower eleven layers, air above,
 * the cave narrowing layer by layer.
 */
final class BuilderChunkSwamp {
    private int[][] map;
    private int[][] map2;
    private int[][] map3;
    private final int initChance = 65;
    private final int size = 48;
    private OldWorld world;

    void generate(Random random, OldWorld world, int i, int j, int k) {
        BuilderHelper b = new BuilderHelper(world);
        b.fillMap(world, random, this.getCave(i, k, world.seed(), world), i, 34, k, -1, 0);
    }

    int[][][] getCave(int x, int y, long seed, OldWorld world) {
        this.world = world;
        this.generatechunk(x, y, seed);
        this.iterate(4, 6);
        this.iterate(4, 6);
        this.iterate(4, 7);
        int[][][] chunk = new int[16][20][16];
        this.map3 = new int[this.size][this.size];
        for (int a = 0; a < this.map.length; a++) {
            System.arraycopy(this.map[a], 0, this.map3[a], 0, this.map[0].length);
        }
        for (int h = 11; h < chunk[0].length; h++) {
            for (int cx = 0; cx < chunk.length; cx++) {
                for (int cz = 0; cz < chunk[0][0].length; cz++) {
                    chunk[cx][h][cz] = this.map[16 + cx][16 + cz];
                }
            }
            this.iterate(h % 2, 1 + h % 2);
        }
        for (int a = 0; a < this.map.length; a++) {
            System.arraycopy(this.map3[a], 0, this.map[a], 0, this.map[0].length);
        }
        for (int h = 10; h >= 0; h--) {
            for (int cx = 0; cx < chunk.length; cx++) {
                for (int cz = 0; cz < chunk[0][0].length; cz++) {
                    chunk[cx][h][cz] = this.map[16 + cx][16 + cz] == 0 ? 9 : 1;
                }
            }
            this.iterate(1, 1 + h % 2);
        }
        for (int[][] plane : chunk) {
            for (int[] row : plane) {
                for (int c = 0; c < row.length; c++) {
                    if (row[c] == 1) {
                        row[c] = -1;
                    }
                }
            }
        }
        return chunk;
    }

    private void iterate(int rule, int rule2) {
        for (int x = 0; x < this.map.length; x++) {
            for (int y = 0; y < this.map[0].length; y++) {
                if (this.map[x][y] == 1) {
                    this.map2[x][y] = this.checkNeightbours(x, y) >= rule ? 1 : 0;
                }
                if (this.map[x][y] == 0) {
                    this.map2[x][y] = this.checkNeightbours(x, y) >= rule2 ? 1 : 0;
                }
            }
        }
        for (int x = 0; x < this.map.length; x++) {
            System.arraycopy(this.map2[x], 0, this.map[x], 0, this.map[0].length);
        }
    }

    private int checkNeightbours(int x, int y) {
        int count = 0;
        count += this.checkNumAt(x - 1, y);
        count += this.checkNumAt(x + 1, y);
        count += this.checkNumAt(x, y + 1);
        count += this.checkNumAt(x, y - 1);
        count += this.checkNumAt(x + 1, y + 1);
        count += this.checkNumAt(x + 1, y - 1);
        count += this.checkNumAt(x - 1, y + 1);
        return count + this.checkNumAt(x - 1, y - 1);
    }

    private int checkNumAt(int x, int y) {
        if (x <= 0 || y <= 0 || x >= this.map.length - 1 || y >= this.map.length - 1) {
            return 1;
        }
        return this.map[x][y];
    }

    private void generatechunk(int cx, int cy, long seed) {
        this.map = new int[this.size][this.size];
        this.map2 = new int[this.size][this.size];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int[][] chunk = this.generateNoiseAt(cx + (x - 1) * 16, cy + (y - 1) * 16, seed);
                for (int bx = 0; bx < 16; bx++) {
                    System.arraycopy(chunk[bx], 0, this.map[x * 16 + bx], y * 16, 16);
                }
            }
        }
    }

    private boolean swamp(int x, int z) {
        return this.world.getBiome(x, z) == OldBiomes.SWAMPLAND;
    }

    private int[][] generateNoiseAt(int x, int y, long seed) {
        Random random = new Random();
        random.setSeed(this.world.seed() + x * 34904 + y * 34184);
        int[][] chunk = new int[16][16];
        if (this.swamp(x, y) && this.swamp(x + 1, y) && this.swamp(x - 1, y) && this.swamp(x, y + 1) && this.swamp(x, y - 1)) {
            int chance = random.nextInt() % 8 == 0 ? 30 : this.initChance;
            for (int a = 0; a < 16; a++) {
                for (int b = 0; b < 16; b++) {
                    chunk[a][b] = random.nextInt(100) > chance ? 1 : 0;
                }
            }
        } else {
            for (int[] row : chunk) {
                java.util.Arrays.fill(row, 1);
            }
        }
        return chunk;
    }
}
