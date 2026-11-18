package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BuilderJungleCave ("jungleCave"): a cave of {@code size} x {@code height} x {@code size} from y 15, grown by a
 * cellular automaton, with a grass floor, plants, lanterns, chests and spawners on it, one of the swampCave text
 * templates in the middle, vines from the ceiling and big mushrooms.
 *
 * <p>The original drew the cave from an unseeded {@code new Random()}: here from a random seeded with the world seed
 * and the position.
 */
final class BuilderJungleCave implements BDDungeons.Builder {
    private boolean[][][] map;
    private boolean[][][] map2;
    private final int posY = 15;
    private final int size;
    private final int height;
    private final int floorBlock;
    private final boolean vines;
    private final boolean vegetation;
    private OldWorld world;

    BuilderJungleCave(int size, int height, int floorBlock, boolean vines, boolean vegetation) {
        this.size = size;
        this.height = height;
        this.floorBlock = floorBlock;
        this.vines = vines;
        this.vegetation = vegetation;
    }

    @Override
    public void generate(Random random, OldWorld world, int x, int z, int mob) {
        this.generate(random, world, x, this.posY, z, mob);
    }

    void generate(Random random, OldWorld world, int i, int j, int k, int idMob) {
        BuilderHelper b = new BuilderHelper(world);
        int[][][] map = this.getCave(world, BuilderHelper.positionRandom(world, i, j, k, 1));
        if (this.vegetation) {
            this.findAndReplaceFloor(random, map);
        }
        b.fillMap(world, random, map, i, j, k, 1, idMob);
        int center = this.size / 2 - 8;
        this.addPremadeDungeon(i + center, j, k + center, random, b);
        if (this.vines) {
            this.placeVinesAtWorld(world, random, map, i, j, k);
        }
        if (this.vegetation) {
            for (int v = random.nextInt(5) + 3; v > 0; v--) {
                int x = random.nextInt(this.size - 8) + 4;
                int z = random.nextInt(this.size - 8) + 4;
                boolean m = false;
                for (int y = 35; !m && y < 50; y++) {
                    if (world.getBlockId(i + x, y, k + z) == 0) {
                        BuilderHelper.fertilizeMushroom(world, random.nextBoolean() ? 40 : 39, i + x, y, k + z, random);
                        m = true;
                    }
                }
            }
        }
    }

    private int[][][] getCave(OldWorld world, Random noise) {
        this.world = world;
        this.generateNoise(noise);
        this.iterate(12, 14);
        this.iterate(17, 20);
        // clean(map, 1) and clean(map, 3) of the original only set cells to false that already were: no effect
        this.iterate(17, 18);
        int[][][] chunk = new int[48][this.height][48];
        for (int x = 0; x < chunk.length; x++) {
            for (int z = 0; z < chunk[0][0].length; z++) {
                for (int y = 0; y < chunk[0].length; y++) {
                    chunk[x][y][z] = this.map[x][y][z] ? 0 : -1;
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
        boolean[][][] swap = this.map;
        this.map = this.map2;
        this.map2 = swap;
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
        if (x <= 0 || y <= 0 || z <= 0 || x >= this.map.length - 1 || z >= this.map.length - 1 || y >= this.map[0].length - 1) {
            return 0;
        }
        return this.map[x][y][z] ? 1 : 0;
    }

    private void generateNoise(Random random) {
        this.map = new boolean[this.size][this.height][this.size];
        this.map2 = new boolean[this.size][this.height][this.size];
        int initChance = 35;
        for (int y = 0; y < this.map[0].length; y++) {
            for (int x = 0; x < this.map.length; x++) {
                for (int z = 0; z < this.map.length; z++) {
                    int chance = initChance;
                    if (x < 4 || x > this.map.length - 5 || z < 4 || z > this.map.length - 5 || y < 4 || y > this.map[0].length - 5) {
                        chance += 45;
                    }
                    if (y == 15 || y == 20 || y == 25 || y == 30) {
                        chance += 30;
                    }
                    this.map[x][y][z] = random.nextInt(100) > chance;
                }
            }
        }
    }

    private void findAndReplaceFloor(Random rnd, int[][][] map) {
        for (int x = 0; x < map.length - 1; x++) {
            for (int z = 0; z < map.length - 1; z++) {
                boolean inCave = false;
                for (int y = map[0].length - 2; y >= 0; y--) {
                    if (map[x][y][z] == 0 && !inCave) {
                        inCave = true;
                        if (rnd.nextInt(200) == 0) {
                            map[x][y][z] = 91;
                        }
                    } else if (inCave && map[x][y][z] != 0) {
                        map[x][y][z] = this.floorBlock;
                        inCave = false;
                        int i = rnd.nextInt(3500);
                        if (i < 5) {
                            map[x][y + 1][z] = 54;
                        }
                        if (i < 10) {
                            map[x][y + 1][z] = 91;
                        } else if (i < 70) {
                            map[x][y + 1][z] = 40;
                        } else if (i < 130) {
                            map[x][y + 1][z] = 39;
                        } else if (i < 140) {
                            map[x][y + 1][z] = 37;
                        } else if (i < 150) {
                            map[x][y + 1][z] = 38;
                        } else if (i < 156) {
                            map[x][y + 2][z] = 52;
                            map[x][y + 1][z] = 54;
                        }
                    }
                }
            }
        }
    }

    /** Vines from the ceiling; {@code inCave} and {@code vines} run on from one column to the next, as in the original. */
    private void placeVinesAtWorld(OldWorld world, Random random, int[][][] map3, int x, int y, int z) {
        boolean inCave = true;
        boolean vines = false;
        for (int x1 = 0; x1 < map3.length; x1++) {
            for (int z1 = 0; z1 < map3.length; z1++) {
                for (int y1 = map3[0].length; y1 > 3; y1--) {
                    if (!inCave) {
                        if (world.getBlockId(x + x1, y + y1, z + z1) == 0) {
                            int blockID = world.getBlockId(x + x1, y + y1 + 1, z + z1);
                            if (blockID == 1 || blockID == 3 || blockID == 2) {
                                inCave = true;
                                if (random.nextInt(5) == 0) {
                                    vines = true;
                                    world.setBlock(x1 + x, y1 + y, z1 + z, 106, 15);
                                } else if (random.nextInt(3) == 0) {
                                    world.setBlock(x1 + x, y1 + y, z1 + z, 106, 0);
                                }
                            }
                        }
                    } else if (world.getBlockId(x + x1, y + y1, z + z1) != 0) {
                        inCave = false;
                    } else if (vines) {
                        world.setBlock(x1 + x, y1 + y, z1 + z, 106, 15);
                        if (random.nextBoolean()) {
                            vines = false;
                        }
                    }
                }
            }
        }
    }

    /** One of the swampCave text templates in the middle, with goblins (mob 5) and spawners of the mob of distance (1). */
    private void addPremadeDungeon(int posx, int posy, int posz, Random rnd, BuilderHelper b) {
        float[][][] map = BDTemplate.randomMap("swampCave", rnd);
        b.fillMap(this.world, rnd, map, posx, posy, posz, 5, 1);
    }
}
