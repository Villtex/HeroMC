package twilightforest.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * TFGenerator: the drawing helpers every Twilight Forest generator uses (Bresenham lines, circles, blobs, area
 * checks). {@code setBlockAndMetadata} of WorldGenerator is {@link OldWorld#setBlock(int, int, int, int, int)}.
 * Generators keep no state between calls; per-call values (the mushroom cap block) are passed along.
 */
public abstract class TFGen extends OldGen {
    /** Block ids of the pack's TwilightForest.cfg. */
    public static final int BOSS_SPAWNER = 1351;
    public static final int CICADA = 1352;
    public static final int FIREFLY = 1353;
    public static final int FIRE_JET = 1354;
    public static final int FIREFLY_JAR = 1355;
    public static final int HEDGE = 1356;
    public static final int LEAVES = 1357;
    public static final int LOG = 1358;
    public static final int MAGIC_LEAVES = 1359;
    public static final int MAGIC_LOG = 1360;
    public static final int MAGIC_LOG_SPECIAL = 1361;
    public static final int MAZESTONE = 1362;
    public static final int MOONWORM = 1363;
    public static final int NAGASTONE = 1364;
    public static final int PLANT = 1365;
    public static final int ROOT = 1367;
    public static final int SAPLING = 1368;
    public static final int TOWER_WOOD = 1372;

    /** TFWorld.MAXHEIGHT. */
    public static final int MAXHEIGHT = 256;

    protected static boolean putBlock(OldWorld world, int x, int y, int z, int id, boolean priority) {
        return putBlockAndMetadata(world, x, y, z, id, 0, priority);
    }

    protected static boolean putBlockAndMetadata(OldWorld world, int x, int y, int z, int id, int meta, boolean priority) {
        if (!priority && world.getBlockId(x, y, z) != 0) {
            return false;
        }
        world.setBlock(x, y, z, id, meta);
        return true;
    }

    public static int[] translate(int sx, int sy, int sz, double distance, double angle, double tilt) {
        int[] dest = {sx, sy, sz};
        double rangle = angle * 2.0 * Math.PI;
        double rtilt = tilt * Math.PI;
        dest[0] = (int) (dest[0] + Math.round(Math.sin(rangle) * Math.sin(rtilt) * distance));
        dest[1] = (int) (dest[1] + Math.round(Math.cos(rtilt) * distance));
        dest[2] = (int) (dest[2] + Math.round(Math.cos(rangle) * Math.sin(rtilt) * distance));
        return dest;
    }

    protected static void drawBresehnam(OldWorld world, int x1, int y1, int z1, int x2, int y2, int z2, int id, int meta, boolean priority) {
        int[] line = getBresehnamArray(x1, y1, z1, x2, y2, z2);
        for (int i = 0; i < line.length; i += 3) {
            putBlockAndMetadata(world, line[i], line[i + 1], line[i + 2], id, meta, priority);
        }
    }

    public static int[] getBresehnamArray(int x1, int y1, int z1, int x2, int y2, int z2) {
        int[] pixel = {x1, y1, z1};
        int dx = x2 - x1;
        int dy = y2 - y1;
        int dz = z2 - z1;
        int xInc = dx < 0 ? -1 : 1;
        int l = Math.abs(dx);
        int yInc = dy < 0 ? -1 : 1;
        int m = Math.abs(dy);
        int zInc = dz < 0 ? -1 : 1;
        int n = Math.abs(dz);
        int dx2 = l << 1;
        int dy2 = m << 1;
        int dz2 = n << 1;
        int[] line;
        if (l >= m && l >= n) {
            int err1 = dy2 - l;
            int err2 = dz2 - l;
            line = new int[3 * l + 3];
            for (int i = 0; i < l * 3; i += 3) {
                line[i] = pixel[0];
                line[i + 1] = pixel[1];
                line[i + 2] = pixel[2];
                if (err1 > 0) {
                    pixel[1] += yInc;
                    err1 -= dx2;
                }
                if (err2 > 0) {
                    pixel[2] += zInc;
                    err2 -= dx2;
                }
                err1 += dy2;
                err2 += dz2;
                pixel[0] += xInc;
            }
        } else if (m >= l && m >= n) {
            int err1 = dx2 - m;
            int err2 = dz2 - m;
            line = new int[3 * m + 3];
            for (int i = 0; i < m * 3; i += 3) {
                line[i] = pixel[0];
                line[i + 1] = pixel[1];
                line[i + 2] = pixel[2];
                if (err1 > 0) {
                    pixel[0] += xInc;
                    err1 -= dy2;
                }
                if (err2 > 0) {
                    pixel[2] += zInc;
                    err2 -= dy2;
                }
                err1 += dx2;
                err2 += dz2;
                pixel[1] += yInc;
            }
        } else {
            int err1 = dy2 - n;
            int err2 = dx2 - n;
            line = new int[3 * n + 3];
            for (int i = 0; i < n * 3; i += 3) {
                line[i] = pixel[0];
                line[i + 1] = pixel[1];
                line[i + 2] = pixel[2];
                if (err1 > 0) {
                    pixel[1] += yInc;
                    err1 -= dz2;
                }
                if (err2 > 0) {
                    pixel[0] += xInc;
                    err2 -= dz2;
                }
                err1 += dy2;
                err2 += dx2;
                pixel[2] += zInc;
            }
        }
        line[line.length - 3] = pixel[0];
        line[line.length - 2] = pixel[1];
        line[line.length - 1] = pixel[2];
        return line;
    }

    public static void drawCircle(OldWorld world, int sx, int sy, int sz, int rad, int id, int meta, boolean priority) {
        for (byte dx = 0; dx <= rad; dx++) {
            for (byte dz = 0; dz <= rad; dz++) {
                int dist = Math.max(dx, dz) + (Math.min(dx, dz) >> 1);
                if (dx == 3 && dz == 3) {
                    dist = 6;
                }
                if (dist <= rad) {
                    putBlockAndMetadata(world, sx + dx, sy, sz + dz, id, meta, priority);
                    putBlockAndMetadata(world, sx + dx, sy, sz - dz, id, meta, priority);
                    putBlockAndMetadata(world, sx - dx, sy, sz + dz, id, meta, priority);
                    putBlockAndMetadata(world, sx - dx, sy, sz - dz, id, meta, priority);
                }
            }
        }
    }

    public static void drawBlob(OldWorld world, int sx, int sy, int sz, int rad, int id, int meta, boolean priority) {
        for (byte dx = 0; dx <= rad; dx++) {
            for (byte dy = 0; dy <= rad; dy++) {
                for (byte dz = 0; dz <= rad; dz++) {
                    int dist;
                    if (dx >= dy && dx >= dz) {
                        dist = dx + (Math.max(dy, dz) >> 1) + (Math.min(dy, dz) >> 2);
                    } else if (dy >= dx && dy >= dz) {
                        dist = dy + (Math.max(dx, dz) >> 1) + (Math.min(dx, dz) >> 2);
                    } else {
                        dist = dz + (Math.max(dx, dy) >> 1) + (Math.min(dx, dy) >> 2);
                    }
                    if (dist <= rad) {
                        putBlockAndMetadata(world, sx + dx, sy + dy, sz + dz, id, meta, priority);
                        putBlockAndMetadata(world, sx + dx, sy + dy, sz - dz, id, meta, priority);
                        putBlockAndMetadata(world, sx - dx, sy + dy, sz + dz, id, meta, priority);
                        putBlockAndMetadata(world, sx - dx, sy + dy, sz - dz, id, meta, priority);
                        putBlockAndMetadata(world, sx + dx, sy - dy, sz + dz, id, meta, priority);
                        putBlockAndMetadata(world, sx + dx, sy - dy, sz - dz, id, meta, priority);
                        putBlockAndMetadata(world, sx - dx, sy - dy, sz + dz, id, meta, priority);
                        putBlockAndMetadata(world, sx - dx, sy - dy, sz - dz, id, meta, priority);
                    }
                }
            }
        }
    }

    /** Cobblestone, or mossy cobblestone 1 in {@code howMuch}. */
    protected static int randStone(Random rand, int howMuch) {
        return rand.nextInt(howMuch) >= 1 ? 4 : 48;
    }

    /**
     * The ground under {@code dx} × {@code dy} columns (dirt, grass or rock) and the space above them, {@code dz} high,
     * empty. The loop bounds use the parameters as the original did (z runs to {@code dy}, y to {@code dz}).
     */
    protected static boolean isAreaClear(OldWorld world, int x, int y, int z, int dx, int dy, int dz) {
        boolean flag = true;
        for (int cx = 0; cx < dx; cx++) {
            for (int cz = 0; cz < dy; cz++) {
                Material m = world.material(x + cx, y - 1, z + cz);
                if (m != Material.GROUND && m != Material.GRASS && m != Material.ROCK) {
                    flag = false;
                }
                for (int cy = 0; cy < dz; cy++) {
                    if (!world.isAirBlock(x + cx, y + cy, z + cz)) {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    protected static boolean hasAirAround(OldWorld world, int x, int y, int z) {
        return world.getBlockId(x + 1, y, z) == 0 || world.getBlockId(x - 1, y, z) == 0 || world.getBlockId(x, y, z + 1) == 0
                || world.getBlockId(x, y, z - 1) == 0 || world.getBlockId(x, y + 1, z) == 0;
    }

    protected static boolean isNearSolid(OldWorld world, int x, int y, int z) {
        return world.material(x + 1, y, z).solid || world.material(x - 1, y, z).solid || world.material(x, y, z + 1).solid
                || world.material(x, y, z - 1).solid;
    }

    /** BlockTFCritter.canPlaceAt. */
    public static boolean critterCanPlaceAt(OldWorld world, int x, int y, int z) {
        Material m = world.material(x, y, z);
        return world.isBlockNormalCube(x, y, z) || m == Material.LEAVES || m == Material.CACTUS;
    }

    /** BlockTFCritter.canPlaceBlockAt. */
    public static boolean critterCanPlaceBlockAt(OldWorld world, int x, int y, int z) {
        return critterCanPlaceAt(world, x - 1, y, z) || critterCanPlaceAt(world, x + 1, y, z) || critterCanPlaceAt(world, x, y, z - 1)
                || critterCanPlaceAt(world, x, y, z + 1) || critterCanPlaceAt(world, x, y - 1, z) || critterCanPlaceAt(world, x, y + 1, z);
    }

    /**
     * BlockTFCritter.onBlockAdded: a critter of metadata 0 turns to the first block it can hold on to; one that can
     * hold on nowhere drops as an item and goes (the moonworm without an item).
     */
    static void critterAdded(OldWorld world, int x, int y, int z) {
        if (world.getBlockMetadata(x, y, z) == 0) {
            if (critterCanPlaceAt(world, x - 1, y, z)) {
                world.setMetadata(x, y, z, 1);
            } else if (critterCanPlaceAt(world, x + 1, y, z)) {
                world.setMetadata(x, y, z, 2);
            } else if (critterCanPlaceAt(world, x, y, z - 1)) {
                world.setMetadata(x, y, z, 3);
            } else if (critterCanPlaceAt(world, x, y, z + 1)) {
                world.setMetadata(x, y, z, 4);
            } else if (critterCanPlaceAt(world, x, y - 1, z)) {
                world.setMetadata(x, y, z, 5);
            } else if (critterCanPlaceAt(world, x, y + 1, z)) {
                world.setMetadata(x, y, z, 6);
            }
        }
        if (!critterCanPlaceBlockAt(world, x, y, z)) {
            world.setBlockToAir(x, y, z);
        }
    }

    /** BlockTFPlant.canPlaceRootBelow: dirt or grass, a root strand or a plain root block at (x, y, z). */
    public static boolean canPlaceRootBelow(OldWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        if (id != 0) {
            Material m = world.material(x, y, z);
            if (m == Material.GROUND || m == Material.GRASS) {
                return true;
            }
        }
        int meta = world.getBlockMetadata(x, y, z);
        return id == PLANT && meta == 14 || id == ROOT && meta == 0;
    }
}
