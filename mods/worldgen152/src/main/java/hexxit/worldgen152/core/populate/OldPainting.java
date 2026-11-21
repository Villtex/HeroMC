package hexxit.worldgen152.core.populate;

import java.util.Arrays;

/**
 * Paintings of 1.5.2 for generators that place them (Twilight Forest towers): the motives of EnumArt, the bounding box of
 * EntityHanging.setDirection and World.getCollidingBoundingBoxes against blocks. Same arithmetic as Better Dungeons'
 * BDPainting (betterdungeons.gen), which keeps its own copy.
 */
public final class OldPainting {
    /** EnumArt in its order: title, width and height in pixels. */
    public static final String[] TITLES = {"Kebab", "Aztec", "Alban", "Aztec2", "Bomb", "Plant", "Wasteland", "Pool", "Courbet",
            "Sea", "Sunset", "Creebet", "Wanderer", "Graham", "Match", "Bust", "Stage", "Void", "SkullAndRoses", "Wither", "Fighters",
            "Pointer", "Pigscene", "BurningSkull", "Skeleton", "DonkeyKong"};
    public static final int[] WIDTH = {16, 16, 16, 16, 16, 16, 16, 32, 32, 32, 32, 32, 16, 16, 32, 32, 32, 32, 32, 32, 64, 64, 64, 64, 64, 64};
    public static final int[] HEIGHT = {16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 32, 32, 32, 32, 32, 32, 32, 32, 32, 64, 64, 64, 48, 48};

    private OldPainting() {
    }

    /** EntityHanging after setDirection: its position and bounding box. */
    public record Hanging(int tileX, int tileY, int tileZ, int direction, int width, int height, double posX, double posY, double posZ,
                           double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public static Hanging of(int x, int y, int z, int direction, int width, int height) {
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
        public boolean intersects(double oMinX, double oMinY, double oMinZ, double oMaxX, double oMaxY, double oMaxZ) {
            return oMaxX > this.minX && oMinX < this.maxX && oMaxY > this.minY && oMinY < this.maxY && oMaxZ > this.minZ && oMinZ < this.maxZ;
        }

        public boolean intersects(Hanging o) {
            return this.intersects(o.minX, o.minY, o.minZ, o.maxX, o.maxY, o.maxZ);
        }
    }

    /** The bounding box of a painting or item frame already spawned, or null for other entities. */
    public static Hanging of(OldEntity e) {
        if (e instanceof OldEntity.Painting p) {
            int art = Arrays.asList(TITLES).indexOf(p.motive());
            return art < 0 ? null : Hanging.of(p.tileX(), p.tileY(), p.tileZ(), p.direction(), WIDTH[art], HEIGHT[art]);
        }
        if (e instanceof OldEntity.ItemFrame f) {
            return Hanging.of(f.tileX(), f.tileY(), f.tileZ(), f.direction(), 9, 9);
        }
        return null;
    }

    /** getEntitiesWithinAABBExcludingEntity holds an EntityHanging: the chunks within 2 blocks of the box are searched. */
    public static boolean hangingWithin(OldWorld world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        for (int cx = floor((minX - 2.0) / 16.0); cx <= floor((maxX + 2.0) / 16.0); cx++) {
            for (int cz = floor((minZ - 2.0) / 16.0); cz <= floor((maxZ + 2.0) / 16.0); cz++) {
                for (OldEntity e : world.entities(cx, cz)) {
                    Hanging other = of(e);
                    if (other != null && other.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** World.getCollidingBoundingBoxes is not empty: a block collision box intersects the bounding box. */
    public static boolean collides(OldWorld world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        Hanging h = new Hanging(0, 0, 0, 0, 0, 0, 0, 0, 0, minX, minY, minZ, maxX, maxY, maxZ);
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
            case 53, 67, 108, 109, 114, 128, 134, 135, 136: {
                // stairs: the half slab and the half block behind it (the corner shapes of BlockStairs are not followed)
                double y0 = (meta & 4) != 0 ? 0.5 : 0;
                double[] slab = {0, y0, 0, 1, y0 + 0.5, 1};
                double y1 = (meta & 4) != 0 ? 0 : 0.5;
                double[] step = switch (meta & 3) {
                    case 0 -> new double[] {0.5, y1, 0, 1, y1 + 0.5, 1};
                    case 1 -> new double[] {0, y1, 0, 0.5, y1 + 0.5, 1};
                    case 2 -> new double[] {0, y1, 0.5, 1, y1 + 0.5, 1};
                    default -> new double[] {0, y1, 0, 1, y1 + 0.5, 0.5};
                };
                return new double[][] {slab, step};
            }
            case 50, 75, 76, 30, 31, 32, 37, 38, 39, 40, 6, 106, 65, 69, 77, 55, 63, 68:
                return NONE;
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
