package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * {@code new EntityPainting(world, x, y, z, direction)} of 1.5.2 and its onValidSurface: every motive (EnumArt, in
 * its order) that fits there (no block collision box in its bounding box, solid blocks behind all of it, no other
 * painting or item frame in its box), one of them picked with the entity's own random. The bounding boxes are the
 * float arithmetic of EntityHanging.setDirection, including its shrinking by 1/32, which leaves the box inverted
 * across the wall.
 *
 * <p>Block collision boxes: full cubes, doors and the partial blocks a dungeon room holds; blocks without one (air,
 * plants, torches, webs, ...) by their material.
 */
final class BDPainting {
    private static final String[] TITLES = {"Kebab", "Aztec", "Alban", "Aztec2", "Bomb", "Plant", "Wasteland", "Pool", "Courbet",
            "Sea", "Sunset", "Creebet", "Wanderer", "Graham", "Match", "Bust", "Stage", "Void", "SkullAndRoses", "Wither", "Fighters",
            "Pointer", "Pigscene", "BurningSkull", "Skeleton", "DonkeyKong"};
    private static final int[] WIDTH = {16, 16, 16, 16, 16, 16, 16, 32, 32, 32, 32, 32, 16, 16, 32, 32, 32, 32, 32, 32, 64, 64, 64, 64, 64, 64};
    private static final int[] HEIGHT = {16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 32, 32, 32, 32, 32, 32, 32, 32, 32, 64, 64, 64, 48, 48};

    private BDPainting() {
    }

    /** EntityHanging after setDirection: its position and bounding box. */
    private record Hanging(int tileX, int tileY, int tileZ, int direction, int width, int height, double posX, double posY, double posZ,
                           double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        static Hanging of(int x, int y, int z, int direction, int width, int height) {
            float var2 = width;
            float var3 = height;
            float var4 = width;
            if (direction != 2 && direction != 0) {
                var2 = 0.5F;
            } else {
                var4 = 0.5F;
            }
            var2 /= 32.0F;
            var3 /= 32.0F;
            var4 /= 32.0F;
            float var5 = x + 0.5F;
            float var6 = y + 0.5F;
            float var7 = z + 0.5F;
            float var8 = 0.5625F;
            if (direction == 2) {
                var7 -= var8;
            }
            if (direction == 1) {
                var5 -= var8;
            }
            if (direction == 0) {
                var7 += var8;
            }
            if (direction == 3) {
                var5 += var8;
            }
            if (direction == 2) {
                var5 -= offset(width);
            }
            if (direction == 1) {
                var7 += offset(width);
            }
            if (direction == 0) {
                var5 += offset(width);
            }
            if (direction == 3) {
                var7 -= offset(width);
            }
            var6 += offset(height);
            float var9 = -0.03125F;
            return new Hanging(x, y, z, direction, width, height, var5, var6, var7, var5 - var2 - var9, var6 - var3 - var9,
                    var7 - var4 - var9, var5 + var2 + var9, var6 + var3 + var9, var7 + var4 + var9);
        }

        private static float offset(int size) {
            return size == 32 || size == 64 ? 0.5F : 0.0F;
        }

        /** AxisAlignedBB.intersectsWith. */
        boolean intersects(double oMinX, double oMinY, double oMinZ, double oMaxX, double oMaxY, double oMaxZ) {
            return oMaxX > this.minX && oMinX < this.maxX && oMaxY > this.minY && oMinY < this.maxY && oMaxZ > this.minZ && oMinZ < this.maxZ;
        }

        boolean intersects(Hanging o) {
            return this.intersects(o.minX, o.minY, o.minZ, o.maxX, o.maxY, o.maxZ);
        }
    }

    /** DecoratorRoom.placePainting after the side was turned into the wall and direction: spawns the painting if one fits. */
    static void place(OldWorld world, int x, int y, int z, int direction) {
        List<Integer> fitting = new ArrayList<>();
        for (int art = 0; art < TITLES.length; art++) {
            if (onValidSurface(world, Hanging.of(x, y, z, direction, WIDTH[art], HEIGHT[art]))) {
                fitting.add(art);
            }
        }
        if (fitting.isEmpty()) {
            // the last motive stays chosen, and it did not fit: the painting is not spawned
            return;
        }
        // Entity.rand of the painting was an unseeded new Random()
        Random rand = BuilderHelper.positionRandom(world, x, y, z, 3);
        int art = fitting.get(rand.nextInt(fitting.size()));
        world.spawnEntity(new OldEntity.Painting(x, y, z, direction, TITLES[art]));
    }

    /** EntityHanging.onValidSurface. */
    private static boolean onValidSurface(OldWorld world, Hanging h) {
        if (collides(world, h)) {
            return false;
        }
        int var1 = Math.max(1, h.width / 16);
        int var2 = Math.max(1, h.height / 16);
        int var3 = h.tileX;
        int var5 = h.tileZ;
        if (h.direction == 2 || h.direction == 0) {
            var3 = floor(h.posX - h.width / 32.0F);
        }
        if (h.direction == 1 || h.direction == 3) {
            var5 = floor(h.posZ - h.width / 32.0F);
        }
        int var4 = floor(h.posY - h.height / 32.0F);
        for (int var6 = 0; var6 < var1; var6++) {
            for (int var7 = 0; var7 < var2; var7++) {
                OldBlockInfo.Material material;
                if (h.direction != 2 && h.direction != 0) {
                    material = world.material(h.tileX, var4 + var7, var5 + var6);
                } else {
                    material = world.material(var3 + var6, var4 + var7, h.tileZ);
                }
                if (!material.solid) {
                    return false;
                }
            }
        }
        // getEntitiesWithinAABBExcludingEntity: the chunks within 2 blocks of the box
        for (int cx = floor((h.minX - 2.0) / 16.0); cx <= floor((h.maxX + 2.0) / 16.0); cx++) {
            for (int cz = floor((h.minZ - 2.0) / 16.0); cz <= floor((h.maxZ + 2.0) / 16.0); cz++) {
                for (OldEntity e : world.entities(cx, cz)) {
                    Hanging other = null;
                    if (e instanceof OldEntity.Painting p) {
                        int art = java.util.Arrays.asList(TITLES).indexOf(p.motive());
                        if (art >= 0) {
                            other = Hanging.of(p.tileX(), p.tileY(), p.tileZ(), p.direction(), WIDTH[art], HEIGHT[art]);
                        }
                    } else if (e instanceof OldEntity.ItemFrame f) {
                        other = Hanging.of(f.tileX(), f.tileY(), f.tileZ(), f.direction(), 9, 9);
                    }
                    if (other != null && other.intersects(h)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** World.getCollidingBoundingBoxes is not empty: a block collision box intersects the bounding box. */
    private static boolean collides(OldWorld world, Hanging h) {
        int i = floor(h.minX);
        int j = floor(h.maxX + 1.0);
        int k = floor(h.minY);
        int l = floor(h.maxY + 1.0);
        int i1 = floor(h.minZ);
        int j1 = floor(h.maxZ + 1.0);
        for (int x = i; x < j; x++) {
            for (int z = i1; z < j1; z++) {
                for (int y = k - 1; y < l; y++) {
                    for (double[] box : collisionBoxes(world, x, y, z)) {
                        if (h.intersects(x + box[0], y + box[1], z + box[2], x + box[3], y + box[4], z + box[5])) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static final double[][] NONE = {};
    private static final double[][] CUBE = {{0, 0, 0, 1, 1, 1}};

    /** Block.addCollisionBoxesToList, relative to the block. */
    private static double[][] collisionBoxes(OldWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        float f = 0.0625F;
        switch (id) {
            case 0:
                return NONE;
            case 64, 71: {
                // BlockDoor.setDoorRotation with getFullMetadata
                boolean top = (meta & 8) != 0;
                int lower = top ? world.getBlockMetadata(x, y - 1, z) : meta;
                int upper = top ? meta : world.getBlockMetadata(x, y + 1, z);
                int full = lower & 7 | (top ? 8 : 0) | ((upper & 1) != 0 ? 16 : 0);
                return new double[][] {door(full)};
            }
            case 26:
                return new double[][] {{0, 0, 0, 1, 0.5625F, 1}};
            case 92: {
                float bite = (1 + meta * 2) / 16.0F;
                return new double[][] {{bite, 0, f, 1 - f, 0.5F - f, 1 - f}};
            }
            case 140:
                return new double[][] {{0.3125F, 0, 0.3125F, 0.6875F, 0.375F, 0.6875F}};
            case 54, 146, 130:
                return new double[][] {{f, 0, f, 1 - f, 0.875F, 1 - f}};
            case 116:
                return new double[][] {{0, 0, 0, 1, 0.75F, 1}};
            case 117:
                return new double[][] {{0.4375F, 0, 0.4375F, 0.5625F, 0.875F, 0.5625F}, {0, 0, 0, 1, 0.125F, 1}};
            case 118:
                return new double[][] {{0, 0, 0, 1, 0.3125F, 1}, {0, 0, 0, 0.125F, 1, 1}, {0, 0, 0, 1, 1, 0.125F},
                        {1 - 0.125F, 0, 0, 1, 1, 1}, {0, 0, 1 - 0.125F, 1, 1, 1}};
            case 120:
                return (meta & 4) != 0 ? new double[][] {{0, 0, 0, 1, 0.8125F, 1}, {0.3125F, 0.8125F, 0.3125F, 0.6875F, 1, 0.6875F}}
                        : new double[][] {{0, 0, 0, 1, 0.8125F, 1}};
            case 145:
                return (meta & 3) == 3 || (meta & 3) == 1 ? new double[][] {{0, 0, 0.125F, 1, 1, 0.875F}} : new double[][] {{0.125F, 0, 0, 0.875F, 1, 1}};
            case 44, 126:
                return (meta & 8) != 0 ? new double[][] {{0, 0.5F, 0, 1, 1, 1}} : new double[][] {{0, 0, 0, 1, 0.5F, 1}};
            case 78:
                return (meta & 7) >= 3 ? new double[][] {{0, 0, 0, 1, 0.5F, 1}} : NONE;
            case 85, 113, 139, 107:
                // fences and walls: 1.5 high (their connections are not followed here)
                return new double[][] {{0, 0, 0, 1, 1.5F, 1}};
            default:
                return world.material(x, y, z).blocksMovement ? CUBE : NONE;
        }
    }

    private static double[] door(int full) {
        float f = 0.1875F;
        int j = full & 3;
        boolean open = (full & 4) != 0;
        boolean hinge = (full & 16) != 0;
        if (j == 0) {
            return open ? !hinge ? box(0, 0, 0, 1, 1, f) : box(0, 0, 1 - f, 1, 1, 1) : box(0, 0, 0, f, 1, 1);
        } else if (j == 1) {
            return open ? !hinge ? box(1 - f, 0, 0, 1, 1, 1) : box(0, 0, 0, f, 1, 1) : box(0, 0, 0, 1, 1, f);
        } else if (j == 2) {
            return open ? !hinge ? box(0, 0, 1 - f, 1, 1, 1) : box(0, 0, 0, 1, 1, f) : box(1 - f, 0, 0, 1, 1, 1);
        }
        return open ? !hinge ? box(0, 0, 0, f, 1, 1) : box(1 - f, 0, 0, 1, 1, 1) : box(0, 0, 1 - f, 1, 1, 1);
    }

    private static double[] box(float x0, float y0, float z0, float x1, float y1, float z1) {
        return new double[] {x0, y0, z0, x1, y1, z1};
    }

    private static int floor(double d) {
        int i = (int) d;
        return d < i ? i - 1 : i;
    }
}
