package projectzulu.gen;

import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;
import projectzulu.gen.ZuluMaze.MazeCell;

import java.util.Random;

/**
 * WorldGenPyramid of Project Zulu 1.0.3.8 (chancePerChunk 1, chanceOutOf 1): a sandstone step pyramid 50 blocks
 * wide and 25 high on a sandstone foundation, with a sandstone maze on every fourth level (mummy spawners, chests,
 * mimics) linked by stairs, and an entrance corridor on the -z side. Every setBlock is flag 3 ({@link ZuluUpdates}).
 *
 * <p>The maze used the generator's own {@code new Random()}, which was not seed-fixed; here it is a Random seeded
 * from the world seed and the position (see {@link #classRandom}). No game classes.
 */
final class ZuluPyramid {
    private static final int OUTSIDE_HEIGHT = 25;
    private static final int FLOOR_HEIGHT = 3;

    private final OldWorld world;
    private final Random classRandom;

    private ZuluPyramid(OldWorld world, Random classRandom) {
        this.world = world;
        this.classRandom = classRandom;
    }

    /** Replacement for the unseeded {@code new Random()} of the original: fixed by world seed and position. */
    static Random classRandom(long seed, int x, int y, int z) {
        return new Random(seed ^ x * 341873128712L ^ z * 132897987541L ^ y * 0x5DEECE66DL ^ 0x50594C4AL);
    }

    static boolean generate(OldWorld world, Random random, int x, int y, int z, Random classRandom) {
        return new ZuluPyramid(world, classRandom).generate(random, x, y, z);
    }

    private void set(int x, int y, int z, int id, int meta) {
        ZuluUpdates.set(this.world, x, y, z, id, meta);
    }

    private boolean generate(Random random, int x, int y, int z) {
        for (int c = 0; c < 1; c++) {
            if (1 - random.nextInt(1) >= 0) {
                int sx = x;
                int sy = y - 1;
                int sz = z;
                for (int j = 0; j >= -(OUTSIDE_HEIGHT / 2 - 1); j--) {
                    for (int i = -(OUTSIDE_HEIGHT + j * 2); i < OUTSIDE_HEIGHT + j * 2; i++) {
                        for (int k = -(OUTSIDE_HEIGHT + j * 2); k < OUTSIDE_HEIGHT + j * 2; k++) {
                            if (this.world.isAirBlock(sx + i, sy + j, sz + k)) {
                                this.set(sx + i, sy + j, sz + k, 24, 0);
                            }
                        }
                    }
                }
                for (int j = 0; j <= OUTSIDE_HEIGHT; j++) {
                    if (j == 0 || j % (FLOOR_HEIGHT + 1) == 0) {
                        for (int i = -(OUTSIDE_HEIGHT - j); i < OUTSIDE_HEIGHT - j; i++) {
                            for (int k = -(OUTSIDE_HEIGHT - j); k < OUTSIDE_HEIGHT - j; k++) {
                                this.set(sx + i, sy + j, sz + k, 24, 0);
                            }
                        }
                        int low = -(OUTSIDE_HEIGHT - j - 3);
                        int high = OUTSIDE_HEIGHT - j - 3;
                        this.generateMaze(sx, sy, sz, j, FLOOR_HEIGHT, low, high, low, high);
                        if (j != 0) {
                            this.generateStairs(sx, sy, sz, j, FLOOR_HEIGHT, low, high, low, high);
                        }
                    }
                }
                for (int j = 1; j <= OUTSIDE_HEIGHT; j++) {
                    for (int i = -(OUTSIDE_HEIGHT - j); i <= OUTSIDE_HEIGHT - j; i++) {
                        this.set(sx + i, sy + j, sz - (OUTSIDE_HEIGHT - j), 24, 0);
                        this.set(sx + i, sy + j, sz + OUTSIDE_HEIGHT - j, 24, 0);
                    }
                    for (int k = -(OUTSIDE_HEIGHT - j); k <= OUTSIDE_HEIGHT - j; k++) {
                        this.set(sx - (OUTSIDE_HEIGHT - j), sy + j, sz + k, 24, 0);
                        this.set(sx + OUTSIDE_HEIGHT - j, sy + j, sz + k, 24, 0);
                    }
                }
                for (int i = -2; i <= 2; i++) {
                    for (int k = -(OUTSIDE_HEIGHT - 8); k > -OUTSIDE_HEIGHT; k--) {
                        for (int j = 1; j < 5; j++) {
                            if (j != 4 && i != -2 && i != 2) {
                                this.set(sx + i, sy + j, sz + k, 0, 0);
                            } else {
                                this.set(sx + i, sy + j, sz + k, 24, 0);
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void generateMaze(int sx, int sy, int sz, int floorYcoord, int floorHeight, int lowerX, int upperX, int lowerZ, int upperZ) {
        int cellWidth = 2;
        int numCellsX = (Math.abs(lowerX - upperX) + 1) / cellWidth;
        int numCellsZ = (Math.abs(lowerZ - upperZ) + 1) / cellWidth;
        int mazeStartX = 4;
        int mazeStartZ = 4;
        int mazeFinishX = numCellsX - 4;
        int mazeFinishZ = numCellsZ - 4;
        MazeCell[][] allCells = new MazeCell[numCellsZ][numCellsX];
        for (int i = 0; i < numCellsZ; i++) {
            for (int k = 0; k < numCellsX; k++) {
                allCells[i][k] = new MazeCell(cellWidth);
            }
        }
        int xCell = 0;
        int xCounter = 1;
        for (int i = lowerX; i < upperX; i++) {
            int zCell = 0;
            int zCounter = 1;
            for (int k = lowerZ; k < upperZ; k++) {
                if (xCell < numCellsX && zCell < numCellsZ) {
                    allCells[xCell][zCell].addLocation(sx + i, sy + floorYcoord + 1, sz + k);
                }
                if (zCounter % cellWidth == 0) {
                    zCell++;
                }
                zCounter++;
            }
            if (xCounter % cellWidth == 0) {
                xCell++;
            }
            xCounter++;
        }
        for (int i = 0; i < numCellsZ; i++) {
            allCells[i][0].setCellType(1);
            allCells[i][numCellsZ - 1].setCellType(1);
        }
        for (int k = 0; k < numCellsX; k++) {
            allCells[0][k].setCellType(1);
            allCells[numCellsX - 1][k].setCellType(1);
        }
        int[] directionsX = {0, 1, 0, -1};
        int[] directionsZ = {1, 0, -1, 0};
        for (int counter = 0; counter < 8000; counter++) {
            int randLength = this.classRandom.nextInt(1) + 1;
            int randDirection = this.classRandom.nextInt(4);
            int selectedX = this.classRandom.nextInt(numCellsX);
            int selectedZ;
            for (selectedZ = this.classRandom.nextInt(numCellsZ); allCells[selectedX][selectedZ].getCellType() != 1;
                    selectedZ = this.classRandom.nextInt(numCellsZ)) {
                selectedX = this.classRandom.nextInt(numCellsX);
            }
            if (!ZuluMaze.isCellTouching(allCells, selectedX, selectedZ, randDirection, randLength, numCellsX, numCellsZ, mazeStartX, mazeStartZ,
                    mazeFinishX, mazeFinishZ)) {
                for (int i = 1; i <= randLength; i++) {
                    allCells[selectedX + directionsX[randDirection] * i][selectedZ + directionsZ[randDirection] * i].setCellType(1);
                }
            }
        }
        for (int i = 0; i < numCellsX; i++) {
            for (int k = 0; k < numCellsZ; k++) {
                if (allCells[i][k].getCellType() == 0 && ZuluMaze.BuildingManager.isDeadEnd(allCells, i, k, numCellsX, numCellsZ)) {
                    allCells[i][k].setCellType(2);
                } else if (allCells[i][k].getCellType() == 0) {
                    int holdRand = this.classRandom.nextInt(100);
                    if (1 - holdRand >= 0) {
                        allCells[i][k].setCellType(4);
                    }
                }
            }
        }
        for (int i = 0; i < numCellsX; i++) {
            for (int k = 0; k < numCellsZ; k++) {
                if (allCells[i][k].getCellType() == 0) {
                    allCells[i][k].setCellType(3);
                }
            }
        }
        for (int i = 0; i < numCellsX; i++) {
            for (int k = 0; k < numCellsZ; k++) {
                MazeCell cell = allCells[i][k];
                for (int m = 0; m < cellWidth * cellWidth; m++) {
                    int[] p = cell.getLocation(m);
                    if (p != null && cell.getCellType() == 1) {
                        for (int j = 0; j < floorHeight; j++) {
                            int meta;
                            if (5 - this.classRandom.nextInt(100) >= 0) {
                                meta = 3;
                            } else if (5 - this.classRandom.nextInt(100) >= 0) {
                                meta = 2;
                            } else if (5 - this.classRandom.nextInt(100) >= 0) {
                                meta = 1;
                            } else {
                                meta = 0;
                            }
                            this.set(p[0], p[1] + j, p[2], 24, meta);
                        }
                    }
                    if (p != null && cell.getCellType() == 2) {
                        for (int j = 0; j < floorHeight; j++) {
                            if (j == 0 && 20 - this.classRandom.nextInt(100) >= 0) {
                                this.classRandom.nextInt(2); // holdRand, unused
                                if (this.classRandom.nextInt(5) == 0) {
                                    this.set(p[0], p[1] + j, p[2], 54, 0);
                                    OldTile.Chest chest = new OldTile.Chest();
                                    for (int slot = 0; slot < chest.slots().length; slot++) {
                                        if (15 - this.classRandom.nextInt(100) >= 0) {
                                            chest.slots()[slot] = OldLoot.DUNGEON_CHEST.oneItem(this.classRandom);
                                        }
                                    }
                                    this.world.setTile(p[0], p[1] + j, p[2], chest);
                                } else {
                                    this.set(p[0], p[1] + j, p[2], 0, 0);
                                    this.world.spawnEntity(ZuluStructureContent.mimic(p[0] + 0.5, p[1] + j, p[2] + 0.5));
                                }
                            } else {
                                this.set(p[0], p[1] + j, p[2], 0, 0);
                            }
                        }
                    }
                    if (p != null && cell.getCellType() == 3) {
                        for (int j = 0; j < floorHeight; j++) {
                            this.set(p[0], p[1] + j, p[2], 0, 0);
                        }
                    }
                    if (p != null && cell.getCellType() == 4) {
                        for (int j = 0; j < floorHeight; j++) {
                            if (j == 0 && m == 0) {
                                // setMobID("ProjectZulu|Core.Entity Mummy"): no entity has that name (the mummy is
                                // "ProjectZulu|Core.Mummy"), so these spawners never spawned anything; an empty
                                // spawner (no tile data) does the same today
                                this.set(p[0], p[1] + j, p[2], 52, 0);
                            } else {
                                this.set(p[0], p[1] + j, p[2], 0, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateStairs(int sx, int sy, int sz, int floorYcoord, int floorHeight, int lowerX, int upperX, int lowerZ, int upperZ) {
        int[][] ref = {{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, 1}, {1, 2}};
        int placeX = 0;
        int placeZ = 0;
        boolean valid = false;
        for (int i = lowerX; i < upperX; i++) {
            for (int k = lowerZ; k < upperZ; k++) {
                valid = true;
                for (int[] l : ref) {
                    int tempX = sx + i + l[0];
                    int tempY = sy + floorYcoord;
                    int tempZ = sz + k + l[1];
                    if (i + l[0] <= lowerX || i + l[0] > upperX || k + l[1] > upperZ || k + l[1] <= lowerZ) {
                        valid = false;
                    }
                    for (int j = -3; j <= 3; j++) {
                        if (j != 0 && this.world.getBlockId(tempX, tempY + j, tempZ) == 0) {
                            valid = false;
                        }
                    }
                    if (!valid) {
                        break;
                    }
                }
                if (valid) {
                    placeX = i;
                    placeZ = k;
                    break;
                }
            }
            if (valid) {
                break;
            }
        }
        if (!valid) {
            return;
        }
        for (int tx = 0; tx < 4; tx++) {
            for (int tz = 0; tz < 4; tz++) {
                for (int ty = 0; ty <= floorHeight * 2; ty++) {
                    if (ty != floorHeight) {
                        this.set(sx + placeX + tx, sy + floorYcoord - floorHeight + ty, sz + placeZ + tz, 0, 0);
                    }
                }
            }
        }
        for (int j = 0; j < floorHeight + 1; j++) {
            int y = sy + floorYcoord - floorHeight + j;
            for (int i = ref.length - 1 - j; i >= ref.length / 2; i--) {
                if (i == ref.length - 1 - j) {
                    this.set(sx + placeX + ref[i][0], y, sz + placeZ + ref[i][1], 128, 3);
                } else {
                    this.set(sx + placeX + ref[i][0], y, sz + placeZ + ref[i][1], 24, 0);
                }
            }
            if (j == floorHeight) {
                for (int[] r : ref) {
                    this.set(sx + placeX + r[0], y, sz + placeZ + r[1], 0, 0);
                }
                for (int i = 0; i < ref.length / 2; i++) {
                    if (i == 0) {
                        this.set(sx + placeX + ref[i][0], y, sz + placeZ + ref[i][1], 128, 1);
                    } else {
                        this.set(sx + placeX + ref[i][0], y, sz + placeZ + ref[i][1], 24, 0);
                    }
                }
            }
        }
    }
}
