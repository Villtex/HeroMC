package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Arrays;
import java.util.Random;

/**
 * BuilderVolcano ("volcano"): a cone of 100 x 120 x 100 blocks from y 5, centred on the chunk corner, grown downward
 * from a 5 x 5 top with a crater grown the same way, lava on and in it, ores in its walls, a netherrack path
 * spiralling down the crater, and a dungeon of 25 rooms (dungeons/dungeonRooms, the field "folder" of the original
 * never changed to "volcano") climbing from y 4 east of it, reached by a brick corridor.
 *
 * <p>As in the original: the four top layers of the map (y 121 to 124) are air over the whole square and its bottom
 * layer (y 5) is flowing lava wherever nothing else is there; the configured "oreChance" is read from "size".
 * The original drew the cone, the path and the dungeon from unseeded {@code new Random()}: here from randoms seeded
 * with the world seed and the position.
 */
final class BuilderVolcano implements BDDungeons.Builder {
    private static final int VACIO = -1;
    private static final int[] PATTERN = {11, 11, 7, 10, 6, 9, 1, 20, 4, 11, 5, 8, 2, 9, 20, 11, 7, 10, 6, 9, 1, 20, 4, 11, 5, 8, 2, 9};
    private static final byte[] MOVE = {3, 3, 2, 2, 1, 1, 0, 2, 3, 3, 0, 0, 1, 1, 3, 3, 2, 2, 1, 1, 0, 2, 3, 3, 0, 0, 1, 1};
    private static final int SX = 100;
    private static final int SY = 130;
    private static final int SZ = 100;

    private final int blockWalls;
    private final int blockPath;
    private final int oreChance;
    private final String folder = "dungeons";
    private final int dungeonRooms;
    private final boolean generatePath;
    private final boolean generateDungeon;
    /** The static "filer" of the original (shared by all volcanoes, here one per builder). */
    private int filer = 1;

    BuilderVolcano(int blockWalls, int blockPath, int oreChance, boolean generatePath, boolean generateDungeon, int dungeonRooms) {
        this.blockWalls = blockWalls;
        this.blockPath = blockPath;
        this.oreChance = oreChance;
        this.dungeonRooms = dungeonRooms;
        this.generatePath = generatePath;
        this.generateDungeon = generateDungeon;
    }

    @Override
    public void generate(Random random, OldWorld world, int x, int z, int mob) {
        this.generate(random, world, x, 5, z, mob);
    }

    void generate(Random random, OldWorld world, int i, int j, int k, int mob) {
        BuilderHelper b = new BuilderHelper(world);
        int despX = -50;
        int despZ = -50;
        Random fillRandom = BuilderHelper.positionRandom(world, i, j, k, 1);
        b.fillMap(world, fillRandom, this.createVolcan(BuilderHelper.positionRandom(world, i, j, k, 2)), i + despX, 5, k + despZ, this.blockWalls, mob);
        despZ += 60;
        despX += 90;
        if (this.generateDungeon) {
            for (int z = 0; z < 5; z++) {
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 25; x++) {
                        if (y != 0 && y != 7 && (x % 4 != 0 || z != 0 && z != 4)) {
                            world.setBlock(i + despX - x, y + 5, k + despZ - z - 6, 0, 0);
                        } else {
                            world.setBlock(i + despX - x, y + 5, k + despZ - z - 6, 45, 0);
                        }
                    }
                }
            }
            this.generateReversedDungeon(BuilderHelper.positionRandom(world, i, j, k, 3), world, i + despX, 4, k + despZ, b, mob);
        }
    }

    /** The map [x][height][z] of the original as one array. */
    private static int at(int x, int y, int z) {
        return (x * SY + y) * SZ + z;
    }

    private int[][][] createVolcan(Random rand) {
        this.filer = 1;
        int[] map = new int[SX * SY * SZ];
        int[] next = new int[SX * SY * SZ];
        Arrays.fill(map, VACIO);
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                map[at(48 + x, 128, 48 + y)] = this.filer;
            }
        }
        for (int i = 128; i > 0; i--) {
            this.iterate(i, map, next, rand, 0.3F);
            int[] swap = map;
            map = next;
            next = swap;
        }
        this.filer = 0;
        int entranceSize = rand.nextInt(10) + 4;
        int e = 50 - entranceSize / 2;
        for (int x = 0; x < entranceSize; x++) {
            for (int y = 0; y < entranceSize; y++) {
                map[at(e + x, 118, e + y)] = this.filer;
            }
        }
        for (int i = 118; i > 0; i--) {
            this.iterate(i, map, next, rand, 0.22F);
            int[] swap = map;
            map = next;
            next = swap;
        }
        int[][][] map2 = new int[SX][SY - 10][SZ];
        for (int z = SY - 15; z > 0; z--) {
            for (int x = 0; x < SX; x++) {
                for (int y = 0; y < SZ; y++) {
                    map2[x][z][y] = map[at(x, z, y)];
                }
            }
        }
        for (int x = 0; x < map2.length; x++) {
            for (int y = 0; y < map2[0][0].length; y++) {
                boolean topLayer = true;
                boolean inwall = false;
                for (int z = map2[0].length - 1; z >= 0; z--) {
                    if (topLayer && map2[x][z][y] == 1) {
                        if (rand.nextInt(150) == 0) {
                            map2[x][z + 1][y] = 10;
                        }
                        topLayer = false;
                        inwall = true;
                    }
                    if (inwall) {
                        if (map2[x][z][y] == 0) {
                            inwall = false;
                            if (rand.nextInt(450) == 0) {
                                map2[x][z + 1][y] = 10;
                            }
                        }
                        if (this.oreChance >= 1 && rand.nextInt(this.oreChance) == 0) {
                            int r = rand.nextInt(100);
                            if (r < 65) {
                                map2[x][z][y] = 16;
                            } else if (r < 88) {
                                map2[x][z][y] = 15;
                            } else if (r < 95) {
                                map2[x][z][y] = 14;
                            } else if (r < 98) {
                                map2[x][z][y] = 129;
                            } else if (r < 99) {
                                map2[x][z][y] = 21;
                            } else {
                                map2[x][z][y] = 56;
                            }
                        }
                    }
                    if (z == 0 && map2[x][0][y] == 0) {
                        map2[x][z][y] = 10;
                    }
                }
            }
        }
        if (this.generatePath) {
            this.generatePath(rand, map2);
        }
        return map2;
    }

    private boolean pathCell(int[][][] map2, int i, int y, int k) {
        return map2[i][y][k] == 0 || map2[i][y][k] == 10;
    }

    private void pathBlock(Random rand, int[][][] map2, int i, int y, int k) {
        map2[i][y][k] = this.blockPath;
        if (rand.nextInt(800) == 0) {
            map2[i][y + 1][k] = 52;
        } else if (rand.nextInt(700) == 0) {
            map2[i][y + 1][k] = 54;
            map2[i][y + 2][k] = 52;
        }
    }

    private void generatePath(Random rand, int[][][] map2) {
        int x = 40;
        int y = 65;
        int inc = 0;
        for (boolean bot = false; !bot; inc++) {
            if (y == 0) {
                bot = true;
            }
            int dist = 0;
            for (int f = 40 - inc; f < map2[0][0].length; f++) {
                if (map2[x][y][f] == 0 || map2[f][y][60] == 10) {
                    dist++;
                }
            }
            dist = dist + 40 - inc;
            for (int k = 40 - inc; k < dist; k++) {
                for (int i = 40 - inc; i > 0 && this.pathCell(map2, i, y, k); i--) {
                    this.pathBlock(rand, map2, i, y, k);
                }
                if (rand.nextInt(10) == 1 && k < 60 + inc && y > 0) {
                    y--;
                }
            }
            dist = 0;
            for (int f = 40 - inc; f < map2.length; f++) {
                if (map2[f][y][60] == 0 || map2[f][y][60] == 10) {
                    dist++;
                }
            }
            dist = dist + 40 - inc;
            for (int i = 40 - inc; i < dist; i++) {
                for (int k = 60 + inc; k < 100 && this.pathCell(map2, i, y, k); k++) {
                    this.pathBlock(rand, map2, i, y, k);
                }
                if (rand.nextInt(10) == 1 && i < 60 + inc && y > 0) {
                    y--;
                }
            }
            dist = 0;
            for (int f = 60 + inc; f > 0; f--) {
                if (map2[x][y][f] == 0 || map2[f][y][60] == 10) {
                    dist++;
                }
            }
            dist = dist + 40 - inc;
            for (int k = 60 + inc; k > 100 - dist; k--) {
                for (int i = 60 + inc; i < 100 && this.pathCell(map2, i, y, k); i++) {
                    this.pathBlock(rand, map2, i, y, k);
                }
                if (rand.nextInt(10) == 1 && k < 60 + inc && y > 0) {
                    y--;
                }
            }
            dist = 0;
            for (int f = 60 + inc; f > 0; f--) {
                if (map2[f][y][60] == 0 || map2[f][y][60] == 10) {
                    dist++;
                }
            }
            dist = dist + 40 - inc;
            for (int i = 60 + inc; i > 100 - dist; i--) {
                for (int k = 40 - inc; k > 0 && this.pathCell(map2, i, y, k); k--) {
                    this.pathBlock(rand, map2, i, y, k);
                }
                if (rand.nextInt(10) == 1 && i > 40 - inc && y > 0) {
                    y--;
                }
            }
        }
    }

    /**
     * One growth step at height {@code h}: every cell of {@code filer} spreads one down (the copy into the fresh map
     * of the original, whose bottom layer stays 0), and at {@code h} a cell next to one of {@code filer} above takes it
     * with chance {@code chance}.
     */
    private void iterate(int h, int[] map, int[] map2, Random rand, float chance) {
        int filer = this.filer;
        for (int x = 0; x < SX; x++) {
            for (int z = SY - 1; z > 0; z--) {
                int k = z == SY - 1 ? 0 : 1;
                int src = at(x, z, 0);
                int above = at(x, z + k, 0);
                for (int y = 0; y < SZ; y++) {
                    int v = map[src + y];
                    map2[src + y] = map[above + y] == filer ? filer : v;
                }
            }
            Arrays.fill(map2, at(x, 0, 0), at(x, 0, 0) + SZ, 0);
        }
        for (int x = 1; x < SX - 1; x++) {
            for (int y = 1; y < SZ - 1; y++) {
                boolean spread = map[at(x - 1, h + 1, y)] == filer || map[at(x + 1, h + 1, y)] == filer
                        || map[at(x, h + 1, y + 1)] == filer || map[at(x, h + 1, y - 1)] == filer;
                if (spread && rand.nextFloat() < chance) {
                    map2[at(x, h, y)] = filer;
                }
            }
        }
    }

    private void generateReversedDungeon(Random random, OldWorld world, int x, int y, int z, BuilderHelper b, int mobID) {
        z -= 15;
        String fName = this.folder + "/";
        for (int i = 0; i < PATTERN.length && i < this.dungeonRooms; i++) {
            Random piece = BuilderHelper.positionRandom(world, x, y, z, 4);
            if (PATTERN[i] < 20) {
                b.fillMap(world, piece, BDTemplate.random(fName + "dungeonRooms/" + PATTERN[i], random), x, y, z, mobID);
            } else {
                b.fillMap(world, piece, BDTemplate.map("Building/" + fName + "stairs" + MOVE[i - 1]), x, y, z, mobID, 0);
                y += 10;
            }
            switch (MOVE[i]) {
                case 0 -> z += 15;
                case 1 -> x -= 15;
                case 2 -> z -= 15;
                case 3 -> x += 15;
                default -> {
                }
            }
        }
        b.fillMap(world, BuilderHelper.positionRandom(world, x, y, z, 4), BDTemplate.map("Building/" + fName + "bossRoom"), x, y, z, mobID, 0);
    }
}
