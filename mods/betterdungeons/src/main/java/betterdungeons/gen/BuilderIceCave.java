package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BuilderIceCave ("iceCave"): a flat cave of {@code size} x 10 x {@code size} grown by a cellular automaton from
 * its floor up, an ice floor, ten chests with spawners on them, and the ice bull (IceBull) in the middle. The
 * walls keep the blocks that were there. The original spawned the bull whatever {@code spawnBoss} said.
 *
 * <p>The original drew the cave and the places of the chests from two unseeded {@code new Random()}: here from
 * randoms seeded with the world seed and the position.
 */
final class BuilderIceCave implements BDDungeons.Builder {
    private int[][][] map;
    private int[][] map2;
    private int globalY = 0;
    private final int posY;
    private final int heightVariation;
    private final int initChance;
    private final int size;
    private final int floorBlock;

    BuilderIceCave(int size, int caveSpread, int floorBlock, boolean boss, int posY, int heightVariation) {
        this.size = size;
        this.initChance = caveSpread;
        this.floorBlock = floorBlock;
        this.posY = posY;
        this.heightVariation = heightVariation;
    }

    @Override
    public void generate(Random random, OldWorld world, int x, int z, int mob) {
        int y = this.posY;
        if (this.heightVariation > 1) {
            y += random.nextInt(this.heightVariation);
        }
        this.generate(random, world, x, y, z, mob);
    }

    void generate(Random random, OldWorld world, int i, int j, int k, int mob) {
        this.getCave(1, BuilderHelper.positionRandom(world, i, j, k, 1), BuilderHelper.positionRandom(world, i, j, k, 2));
        BuilderHelper b = new BuilderHelper(world);
        b.fillMap(world, random, this.map, i, j, k, -1, mob);
        world.spawnEntity(new OldEntity.Mob("BetterDungeons.IceBull", i + this.size / 2, j + 1, k + this.size / 2, 0.0F, null));
    }

    /** {@code rnd}: the random of restartMap, {@code random} the one of getCave (both {@code new Random()} in the original). */
    private int[][][] getCave(int mainBlock, Random rnd, Random random) {
        this.restartMap(rnd);
        int y = 0;
        int center = this.size / 2 - 5;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                this.map[center + i][y][center + j] = 0;
            }
        }
        this.iterate(4, 6);
        this.pushBack(0);
        this.iterate(4, 6);
        this.pushBack(0);
        this.iterate(4, 7);
        this.pushBack(0);
        for (int i = 1; i < this.map[0].length; this.globalY = i++) {
            this.iterate(4, 7 - i);
            this.pushBack(i);
        }
        for (int i = 0; i < this.map.length; i++) {
            for (int j = 0; j < this.map.length; j++) {
                if (this.map[i][0][j] == 0) {
                    this.map[i][0][j] = this.floorBlock;
                }
            }
        }
        for (int i = 0; i < this.size / 10; i++) {
            int z = random.nextInt(this.map.length - 10) + 5;
            int x = random.nextInt(this.map.length - 10) + 5;
            if (this.map[x][1][z] == 0) {
                this.map[x][1][z] = 54;
                this.map[x][2][z] = 52;
            } else if (random.nextInt(5) != 0) {
                i--;
            }
        }
        for (int[][] plane : this.map) {
            for (int[] row : plane) {
                for (int c = 0; c < row.length; c++) {
                    if (row[c] == 1) {
                        row[c] = mainBlock;
                    }
                }
            }
        }
        return this.map;
    }

    private void iterate(int rule, int rule2) {
        for (int x = 0; x < this.map.length; x++) {
            for (int z = 0; z < this.map.length; z++) {
                if (this.map[x][this.globalY][z] == 1) {
                    this.map2[x][z] = this.checkNeightbours(x, z) >= rule ? 1 : 0;
                }
                if (this.map[x][this.globalY][z] == 0) {
                    this.map2[x][z] = this.checkNeightbours(x, z) >= rule2 ? 1 : 0;
                }
            }
        }
    }

    private int checkNeightbours(int x, int z) {
        int count = 0;
        count += this.checkNumAt(x - 1, z);
        count += this.checkNumAt(x + 1, z);
        count += this.checkNumAt(x, z + 1);
        count += this.checkNumAt(x, z - 1);
        count += this.checkNumAt(x + 1, z + 1);
        count += this.checkNumAt(x + 1, z - 1);
        count += this.checkNumAt(x - 1, z + 1);
        return count + this.checkNumAt(x - 1, z - 1);
    }

    private int checkNumAt(int x, int z) {
        if (x <= 0 || z <= 0 || x >= this.map.length - 1 || z >= this.map.length - 1) {
            return 1;
        }
        return this.map[x][this.globalY][z];
    }

    private void pushBack(int y) {
        if (y > this.map[0].length) {
            y = this.map[0].length;
        }
        for (int x = 0; x < this.map.length; x++) {
            for (int z = 0; z < this.map.length; z++) {
                this.map[x][y][z] = this.map2[x][z];
            }
        }
    }

    private void restartMap(Random rnd) {
        int s = this.size;
        this.map = new int[s][10][s];
        this.map2 = new int[s][s];
        for (int x = 0; x < this.map.length; x++) {
            for (int z = 0; z < this.map.length; z++) {
                this.map[x][0][z] = rnd.nextInt(100) > this.initChance ? 1 : 0;
            }
        }
    }
}
