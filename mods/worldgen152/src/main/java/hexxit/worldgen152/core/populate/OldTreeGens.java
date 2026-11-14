package hexxit.worldgen152.core.populate;

import hexxit.worldgen152.core.OldMath;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;

import java.util.Random;

/** The trees of 1.5.2. Log is id 17, leaves 18; metadata 0 oak, 1 spruce, 2 birch, 3 jungle. */
public final class OldTreeGens {
    private static final int LOG = 17;
    private static final int LEAVES = 18;
    private static final int VINE = 106;
    private static final int[] DIRECTION_X = {0, -1, 0, 1};
    private static final int[] DIRECTION_Z = {1, 0, -1, 0};
    private static final int[] OPPOSITE = {2, 3, 0, 1};

    private OldTreeGens() {
    }

    /** canSustainPlant for a sapling: grass, dirt or farmland. */
    static boolean saplingSoil(int id) {
        return id == 2 || id == 3 || id == 60;
    }

    /** Block.onPlantGrow: grass under a new tree becomes dirt. */
    static void plantGrow(OldWorld world, int x, int y, int z) {
        if (world.getBlockId(x, y, z) == 2) {
            world.setBlock(x, y, z, 3, 0);
        }
    }

    private static boolean airOrLeaves(int id) {
        return id == 0 || OldBlockInfo.leaves(id);
    }

    private static void hangVines(OldWorld world, int x, int y, int z, int meta) {
        world.setBlock(x, y, z, VINE, meta);
        for (int left = 4; world.getBlockId(x, --y, z) == 0 && left > 0; left--) {
            world.setBlock(x, y, z, VINE, meta);
        }
    }

    /** WorldGenTrees: the small oak, and with other settings the small jungle tree with vines and cocoa. */
    public static final class Trees extends OldGen {
        private final int minHeight;
        private final int woodMeta;
        private final int leavesMeta;
        private final boolean vines;

        public Trees() {
            this(4, 0, 0, false);
        }

        public Trees(int minHeight, int woodMeta, int leavesMeta, boolean vines) {
            this.minHeight = minHeight;
            this.woodMeta = woodMeta;
            this.leavesMeta = leavesMeta;
            this.vines = vines;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int height = random.nextInt(3) + this.minHeight;
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            for (int by = y; by <= y + 1 + height; by++) {
                int r = 1;
                if (by == y) {
                    r = 0;
                }
                if (by >= y + 1 + height - 2) {
                    r = 2;
                }
                for (int bx = x - r; bx <= x + r; bx++) {
                    for (int bz = z - r; bz <= z + r; bz++) {
                        if (by < 0 || by >= 256) {
                            return false;
                        }
                        int id = world.getBlockId(bx, by, bz);
                        if (id != 0 && !OldBlockInfo.leaves(id) && id != 2 && id != 3 && !OldBlockInfo.wood(id)) {
                            return false;
                        }
                    }
                }
            }
            if (!saplingSoil(world.getBlockId(x, y - 1, z)) || y >= 256 - height - 1) {
                return false;
            }
            plantGrow(world, x, y - 1, z);

            for (int by = y - 3 + height; by <= y + height; by++) {
                int fromTop = by - (y + height);
                int r = 1 - fromTop / 2;
                for (int bx = x - r; bx <= x + r; bx++) {
                    int dx = bx - x;
                    for (int bz = z - r; bz <= z + r; bz++) {
                        int dz = bz - z;
                        if (Math.abs(dx) != r || Math.abs(dz) != r || random.nextInt(2) != 0 && fromTop != 0) {
                            if (OldBlockInfo.replacedByLeaves(world.getBlockId(bx, by, bz))) {
                                world.setBlock(bx, by, bz, LEAVES, this.leavesMeta);
                            }
                        }
                    }
                }
            }

            for (int h = 0; h < height; h++) {
                if (airOrLeaves(world.getBlockId(x, y + h, z))) {
                    world.setBlock(x, y + h, z, LOG, this.woodMeta);
                    if (this.vines && h > 0) {
                        if (random.nextInt(3) > 0 && world.isAirBlock(x - 1, y + h, z)) {
                            world.setBlock(x - 1, y + h, z, VINE, 8);
                        }
                        if (random.nextInt(3) > 0 && world.isAirBlock(x + 1, y + h, z)) {
                            world.setBlock(x + 1, y + h, z, VINE, 2);
                        }
                        if (random.nextInt(3) > 0 && world.isAirBlock(x, y + h, z - 1)) {
                            world.setBlock(x, y + h, z - 1, VINE, 1);
                        }
                        if (random.nextInt(3) > 0 && world.isAirBlock(x, y + h, z + 1)) {
                            world.setBlock(x, y + h, z + 1, VINE, 4);
                        }
                    }
                }
            }

            if (this.vines) {
                for (int by = y - 3 + height; by <= y + height; by++) {
                    int fromTop = by - (y + height);
                    int r = 2 - fromTop / 2;
                    for (int bx = x - r; bx <= x + r; bx++) {
                        for (int bz = z - r; bz <= z + r; bz++) {
                            if (OldBlockInfo.leaves(world.getBlockId(bx, by, bz))) {
                                if (random.nextInt(4) == 0 && world.getBlockId(bx - 1, by, bz) == 0) {
                                    hangVines(world, bx - 1, by, bz, 8);
                                }
                                if (random.nextInt(4) == 0 && world.getBlockId(bx + 1, by, bz) == 0) {
                                    hangVines(world, bx + 1, by, bz, 2);
                                }
                                if (random.nextInt(4) == 0 && world.getBlockId(bx, by, bz - 1) == 0) {
                                    hangVines(world, bx, by, bz - 1, 1);
                                }
                                if (random.nextInt(4) == 0 && world.getBlockId(bx, by, bz + 1) == 0) {
                                    hangVines(world, bx, by, bz + 1, 4);
                                }
                            }
                        }
                    }
                }
                if (random.nextInt(5) == 0 && height > 5) {
                    for (int row = 0; row < 2; row++) {
                        for (int direction = 0; direction < 4; direction++) {
                            if (random.nextInt(4 - row) == 0) {
                                int age = random.nextInt(3);
                                world.setBlock(x + DIRECTION_X[OPPOSITE[direction]], y + height - 5 + row,
                                        z + DIRECTION_Z[OPPOSITE[direction]], 127, age << 2 | direction);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenForest: the birch. */
    public static final class Birch extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int height = random.nextInt(3) + 5;
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            for (int by = y; by <= y + 1 + height; by++) {
                int r = 1;
                if (by == y) {
                    r = 0;
                }
                if (by >= y + 1 + height - 2) {
                    r = 2;
                }
                for (int bx = x - r; bx <= x + r; bx++) {
                    for (int bz = z - r; bz <= z + r; bz++) {
                        if (by < 0 || by >= 256 || !airOrLeaves(world.getBlockId(bx, by, bz))) {
                            return false;
                        }
                    }
                }
            }
            if (!saplingSoil(world.getBlockId(x, y - 1, z)) || y >= 256 - height - 1) {
                return false;
            }
            plantGrow(world, x, y - 1, z);
            for (int by = y - 3 + height; by <= y + height; by++) {
                int fromTop = by - (y + height);
                int r = 1 - fromTop / 2;
                for (int bx = x - r; bx <= x + r; bx++) {
                    int dx = bx - x;
                    for (int bz = z - r; bz <= z + r; bz++) {
                        int dz = bz - z;
                        if (Math.abs(dx) != r || Math.abs(dz) != r || random.nextInt(2) != 0 && fromTop != 0) {
                            if (OldBlockInfo.replacedByLeaves(world.getBlockId(bx, by, bz))) {
                                world.setBlock(bx, by, bz, LEAVES, 2);
                            }
                        }
                    }
                }
            }
            for (int h = 0; h < height; h++) {
                if (airOrLeaves(world.getBlockId(x, y + h, z))) {
                    world.setBlock(x, y + h, z, LOG, 2);
                }
            }
            return true;
        }
    }

    /** WorldGenSwamp: the wide oak that stands in shallow water and is hung with vines. */
    public static final class Swamp extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int height = random.nextInt(4) + 5;
            while (world.material(x, y - 1, z) == Material.WATER) {
                y--;
            }
            if (y < 1 || y + height + 1 > 128) {
                return false;
            }
            for (int by = y; by <= y + 1 + height; by++) {
                int r = 1;
                if (by == y) {
                    r = 0;
                }
                if (by >= y + 1 + height - 2) {
                    r = 3;
                }
                for (int bx = x - r; bx <= x + r; bx++) {
                    for (int bz = z - r; bz <= z + r; bz++) {
                        if (by < 0 || by >= 128) {
                            return false;
                        }
                        int id = world.getBlockId(bx, by, bz);
                        if (!airOrLeaves(id)) {
                            if (id != 9 && id != 8) {
                                return false;
                            } else if (by > y) {
                                return false;
                            }
                        }
                    }
                }
            }
            int soil = world.getBlockId(x, y - 1, z);
            if (soil != 2 && soil != 3 || y >= 128 - height - 1) {
                return false;
            }
            world.setBlock(x, y - 1, z, 3, 0);
            for (int by = y - 3 + height; by <= y + height; by++) {
                int fromTop = by - (y + height);
                int r = 2 - fromTop / 2;
                for (int bx = x - r; bx <= x + r; bx++) {
                    int dx = bx - x;
                    for (int bz = z - r; bz <= z + r; bz++) {
                        int dz = bz - z;
                        int id = world.getBlockId(bx, by, bz);
                        if ((Math.abs(dx) != r || Math.abs(dz) != r || random.nextInt(2) != 0 && fromTop != 0)
                                && OldBlockInfo.replacedByLeaves(id)) {
                            world.setBlock(bx, by, bz, LEAVES, 0);
                        }
                    }
                }
            }
            for (int h = 0; h < height; h++) {
                int id = world.getBlockId(x, y + h, z);
                if (airOrLeaves(id) || id == 8 || id == 9) {
                    world.setBlock(x, y + h, z, LOG, 0);
                }
            }
            for (int by = y - 3 + height; by <= y + height; by++) {
                int fromTop = by - (y + height);
                int r = 2 - fromTop / 2;
                for (int bx = x - r; bx <= x + r; bx++) {
                    for (int bz = z - r; bz <= z + r; bz++) {
                        if (OldBlockInfo.leaves(world.getBlockId(bx, by, bz))) {
                            if (random.nextInt(4) == 0 && world.getBlockId(bx - 1, by, bz) == 0) {
                                hangVines(world, bx - 1, by, bz, 8);
                            }
                            if (random.nextInt(4) == 0 && world.getBlockId(bx + 1, by, bz) == 0) {
                                hangVines(world, bx + 1, by, bz, 2);
                            }
                            if (random.nextInt(4) == 0 && world.getBlockId(bx, by, bz - 1) == 0) {
                                hangVines(world, bx, by, bz - 1, 1);
                            }
                            if (random.nextInt(4) == 0 && world.getBlockId(bx, by, bz + 1) == 0) {
                                hangVines(world, bx, by, bz + 1, 4);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenTaiga1: the tall pine with leaves only near the top. */
    public static final class Pine extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int height = random.nextInt(5) + 7;
            int bare = height - random.nextInt(2) - 3;
            int crown = height - bare;
            int maxRadius = 1 + random.nextInt(crown + 1);
            if (y < 1 || y + height + 1 > 128) {
                return false;
            }
            for (int by = y; by <= y + 1 + height; by++) {
                int r = by - y < bare ? 0 : maxRadius;
                for (int bx = x - r; bx <= x + r; bx++) {
                    for (int bz = z - r; bz <= z + r; bz++) {
                        if (by < 0 || by >= 128 || !airOrLeaves(world.getBlockId(bx, by, bz))) {
                            return false;
                        }
                    }
                }
            }
            int soil = world.getBlockId(x, y - 1, z);
            if (soil != 2 && soil != 3 || y >= 128 - height - 1) {
                return false;
            }
            world.setBlock(x, y - 1, z, 3, 0);
            int r = 0;
            for (int by = y + height; by >= y + bare; by--) {
                for (int bx = x - r; bx <= x + r; bx++) {
                    int dx = bx - x;
                    for (int bz = z - r; bz <= z + r; bz++) {
                        int dz = bz - z;
                        if ((Math.abs(dx) != r || Math.abs(dz) != r || r <= 0)
                                && OldBlockInfo.replacedByLeaves(world.getBlockId(bx, by, bz))) {
                            world.setBlock(bx, by, bz, LEAVES, 1);
                        }
                    }
                }
                if (r >= 1 && by == y + bare + 1) {
                    r--;
                } else if (r < maxRadius) {
                    r++;
                }
            }
            for (int h = 0; h < height - 1; h++) {
                if (airOrLeaves(world.getBlockId(x, y + h, z))) {
                    world.setBlock(x, y + h, z, LOG, 1);
                }
            }
            return true;
        }
    }

    /** WorldGenTaiga2: the spruce with layered branches. */
    public static final class Spruce extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int height = random.nextInt(4) + 6;
            int bare = 1 + random.nextInt(2);
            int crown = height - bare;
            int maxRadius = 2 + random.nextInt(2);
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            for (int by = y; by <= y + 1 + height; by++) {
                int r = by - y < bare ? 0 : maxRadius;
                for (int bx = x - r; bx <= x + r; bx++) {
                    for (int bz = z - r; bz <= z + r; bz++) {
                        if (by < 0 || by >= 256 || !airOrLeaves(world.getBlockId(bx, by, bz))) {
                            return false;
                        }
                    }
                }
            }
            if (!saplingSoil(world.getBlockId(x, y - 1, z)) || y >= 256 - height - 1) {
                return false;
            }
            plantGrow(world, x, y - 1, z);
            int r = random.nextInt(2);
            int limit = 1;
            int reset = 0;
            for (int layer = 0; layer <= crown; layer++) {
                int by = y + height - layer;
                for (int bx = x - r; bx <= x + r; bx++) {
                    int dx = bx - x;
                    for (int bz = z - r; bz <= z + r; bz++) {
                        int dz = bz - z;
                        if ((Math.abs(dx) != r || Math.abs(dz) != r || r <= 0)
                                && OldBlockInfo.replacedByLeaves(world.getBlockId(bx, by, bz))) {
                            world.setBlock(bx, by, bz, LEAVES, 1);
                        }
                    }
                }
                if (r >= limit) {
                    r = reset;
                    reset = 1;
                    if (++limit > maxRadius) {
                        limit = maxRadius;
                    }
                } else {
                    r++;
                }
            }
            int topless = random.nextInt(3);
            for (int h = 0; h < height - topless; h++) {
                if (airOrLeaves(world.getBlockId(x, y + h, z))) {
                    world.setBlock(x, y + h, z, LOG, 1);
                }
            }
            return true;
        }
    }

    /** WorldGenShrub: one log with a small leaf cap, the jungle bush. */
    public static final class Shrub extends OldGen {
        private final int woodMeta;
        private final int leavesMeta;

        public Shrub(int woodMeta, int leavesMeta) {
            this.woodMeta = woodMeta;
            this.leavesMeta = leavesMeta;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            y = OldPlantGens.skipAirAndLeaves(world, x, y, z);
            int soil = world.getBlockId(x, y, z);
            if (soil == 3 || soil == 2) {
                y++;
                world.setBlock(x, y, z, LOG, this.woodMeta);
                for (int by = y; by <= y + 2; by++) {
                    int r = 2 - (by - y);
                    for (int bx = x - r; bx <= x + r; bx++) {
                        int dx = bx - x;
                        for (int bz = z - r; bz <= z + r; bz++) {
                            int dz = bz - z;
                            int id = world.getBlockId(bx, by, bz);
                            if ((Math.abs(dx) != r || Math.abs(dz) != r || random.nextInt(2) != 0) && OldBlockInfo.replacedByLeaves(id)) {
                                world.setBlock(bx, by, bz, LEAVES, this.leavesMeta);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /** WorldGenHugeTrees: the 2x2 jungle giant. */
    public static final class Huge extends OldGen {
        private final int baseHeight;
        private final int woodMeta;
        private final int leavesMeta;

        public Huge(int baseHeight, int woodMeta, int leavesMeta) {
            this.baseHeight = baseHeight;
            this.woodMeta = woodMeta;
            this.leavesMeta = leavesMeta;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int height = random.nextInt(3) + this.baseHeight;
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            for (int by = y; by <= y + 1 + height; by++) {
                int r = 2;
                if (by == y) {
                    r = 1;
                }
                if (by >= y + 1 + height - 2) {
                    r = 2;
                }
                for (int bx = x - r; bx <= x + r; bx++) {
                    for (int bz = z - r; bz <= z + r; bz++) {
                        if (by < 0 || by >= 256) {
                            return false;
                        }
                        int id = world.getBlockId(bx, by, bz);
                        if (id != 0 && !OldBlockInfo.leaves(id) && id != 2 && id != 3 && !OldBlockInfo.wood(id) && id != 6) {
                            return false;
                        }
                    }
                }
            }
            if (!saplingSoil(world.getBlockId(x, y - 1, z)) || y >= 256 - height - 1) {
                return false;
            }
            plantGrow(world, x, y - 1, z);
            plantGrow(world, x + 1, y - 1, z);
            plantGrow(world, x, y - 1, z + 1);
            plantGrow(world, x + 1, y - 1, z + 1);
            this.growLeaves(world, x, z, y + height, 2, random);

            for (int by = y + height - 2 - random.nextInt(4); by > y + height / 2; by -= 2 + random.nextInt(4)) {
                float angle = random.nextFloat() * (float) Math.PI * 2.0F;
                int bx = x + (int) (0.5F + OldMath.cos(angle) * 4.0F);
                int bz = z + (int) (0.5F + OldMath.sin(angle) * 4.0F);
                this.growLeaves(world, bx, bz, by, 0, random);
                for (int step = 0; step < 5; step++) {
                    bx = x + (int) (1.5F + OldMath.cos(angle) * step);
                    bz = z + (int) (1.5F + OldMath.sin(angle) * step);
                    world.setBlock(bx, by - 3 + step / 2, bz, LOG, this.woodMeta);
                }
            }

            for (int h = 0; h < height; h++) {
                if (airOrLeaves(world.getBlockId(x, y + h, z))) {
                    world.setBlock(x, y + h, z, LOG, this.woodMeta);
                    if (h > 0) {
                        if (random.nextInt(3) > 0 && world.isAirBlock(x - 1, y + h, z)) {
                            world.setBlock(x - 1, y + h, z, VINE, 8);
                        }
                        if (random.nextInt(3) > 0 && world.isAirBlock(x, y + h, z - 1)) {
                            world.setBlock(x, y + h, z - 1, VINE, 1);
                        }
                    }
                }
                if (h < height - 1) {
                    if (airOrLeaves(world.getBlockId(x + 1, y + h, z))) {
                        world.setBlock(x + 1, y + h, z, LOG, this.woodMeta);
                        if (h > 0) {
                            if (random.nextInt(3) > 0 && world.isAirBlock(x + 2, y + h, z)) {
                                world.setBlock(x + 2, y + h, z, VINE, 2);
                            }
                            if (random.nextInt(3) > 0 && world.isAirBlock(x + 1, y + h, z - 1)) {
                                world.setBlock(x + 1, y + h, z - 1, VINE, 1);
                            }
                        }
                    }
                    if (airOrLeaves(world.getBlockId(x + 1, y + h, z + 1))) {
                        world.setBlock(x + 1, y + h, z + 1, LOG, this.woodMeta);
                        if (h > 0) {
                            if (random.nextInt(3) > 0 && world.isAirBlock(x + 2, y + h, z + 1)) {
                                world.setBlock(x + 2, y + h, z + 1, VINE, 2);
                            }
                            if (random.nextInt(3) > 0 && world.isAirBlock(x + 1, y + h, z + 2)) {
                                world.setBlock(x + 1, y + h, z + 2, VINE, 4);
                            }
                        }
                    }
                    if (airOrLeaves(world.getBlockId(x, y + h, z + 1))) {
                        world.setBlock(x, y + h, z + 1, LOG, this.woodMeta);
                        if (h > 0) {
                            if (random.nextInt(3) > 0 && world.isAirBlock(x - 1, y + h, z + 1)) {
                                world.setBlock(x - 1, y + h, z + 1, VINE, 8);
                            }
                            if (random.nextInt(3) > 0 && world.isAirBlock(x, y + h, z + 2)) {
                                world.setBlock(x, y + h, z + 2, VINE, 4);
                            }
                        }
                    }
                }
            }
            return true;
        }

        private void growLeaves(OldWorld world, int x, int z, int top, int extra, Random random) {
            for (int by = top - 2; by <= top; by++) {
                int fromTop = by - top;
                int r = extra + 1 - fromTop;
                for (int bx = x - r; bx <= x + r + 1; bx++) {
                    int dx = bx - x;
                    for (int bz = z - r; bz <= z + r + 1; bz++) {
                        int dz = bz - z;
                        if ((dx >= 0 || dz >= 0 || dx * dx + dz * dz <= r * r)
                                && (dx <= 0 && dz <= 0 || dx * dx + dz * dz <= (r + 1) * (r + 1))
                                && (random.nextInt(4) != 0 || dx * dx + dz * dz <= (r - 1) * (r - 1))) {
                            if (OldBlockInfo.replacedByLeaves(world.getBlockId(bx, by, bz))) {
                                world.setBlock(bx, by, bz, LEAVES, this.leavesMeta);
                            }
                        }
                    }
                }
            }
        }
    }

    /** WorldGenBigMushroom: brown (block 99) or red (100); the metadata picks which faces show the cap. */
    public static final class BigMushroom extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            int type = random.nextInt(2);
            int height = random.nextInt(3) + 4;
            if (y < 1 || y + height + 1 >= 256) {
                return false;
            }
            for (int by = y; by <= y + 1 + height; by++) {
                int r = by <= y + 3 ? 0 : 3;
                for (int bx = x - r; bx <= x + r; bx++) {
                    for (int bz = z - r; bz <= z + r; bz++) {
                        if (by < 0 || by >= 256 || !airOrLeaves(world.getBlockId(bx, by, bz))) {
                            return false;
                        }
                    }
                }
            }
            int soil = world.getBlockId(x, y - 1, z);
            if (soil != 3 && soil != 2 && soil != 110) {
                return false;
            }
            int capBottom = y + height;
            if (type == 1) {
                capBottom = y + height - 3;
            }
            for (int by = capBottom; by <= y + height; by++) {
                int r = 1;
                if (by < y + height) {
                    r++;
                }
                if (type == 0) {
                    r = 3;
                }
                for (int bx = x - r; bx <= x + r; bx++) {
                    for (int bz = z - r; bz <= z + r; bz++) {
                        int meta = 5;
                        if (bx == x - r) {
                            meta--;
                        }
                        if (bx == x + r) {
                            meta++;
                        }
                        if (bz == z - r) {
                            meta -= 3;
                        }
                        if (bz == z + r) {
                            meta += 3;
                        }
                        if (type == 0 || by < y + height) {
                            if ((bx == x - r || bx == x + r) && (bz == z - r || bz == z + r)) {
                                continue;
                            }
                            if (bx == x - (r - 1) && bz == z - r) {
                                meta = 1;
                            }
                            if (bx == x - r && bz == z - (r - 1)) {
                                meta = 1;
                            }
                            if (bx == x + (r - 1) && bz == z - r) {
                                meta = 3;
                            }
                            if (bx == x + r && bz == z - (r - 1)) {
                                meta = 3;
                            }
                            if (bx == x - (r - 1) && bz == z + r) {
                                meta = 7;
                            }
                            if (bx == x - r && bz == z + (r - 1)) {
                                meta = 7;
                            }
                            if (bx == x + (r - 1) && bz == z + r) {
                                meta = 9;
                            }
                            if (bx == x + r && bz == z + (r - 1)) {
                                meta = 9;
                            }
                        }
                        if (meta == 5 && by < y + height) {
                            meta = 0;
                        }
                        if ((meta != 0 || y >= y + height - 1) && OldBlockInfo.replacedByLeaves(world.getBlockId(bx, by, bz))) {
                            world.setBlock(bx, by, bz, 99 + type, meta);
                        }
                    }
                }
            }
            for (int h = 0; h < height; h++) {
                if (OldBlockInfo.replacedByLeaves(world.getBlockId(x, y + h, z))) {
                    world.setBlock(x, y + h, z, 99 + type, 10);
                }
            }
            return true;
        }
    }

    /**
     * WorldGenBigTree: the branching oak. It keeps its height limit from one tree to the next, as the
     * original instance did; {@link OldWorld#bigTree()} hands out one instance per populated chunk.
     */
    public static final class BigTree extends OldGen {
        private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};

        private final Random rand = new Random();
        private OldWorld world;
        private final int[] basePos = {0, 0, 0};
        private int heightLimit = 0;
        private int height;
        private final double heightAttenuation = 0.618;
        private final double branchSlope = 0.381;
        private double scaleWidth = 1.0;
        private double leafDensity = 1.0;
        private int heightLimitLimit = 12;
        private int leafDistanceLimit = 4;
        private int[][] leafNodes;

        private void generateLeafNodeList() {
            this.height = (int) (this.heightLimit * this.heightAttenuation);
            if (this.height >= this.heightLimit) {
                this.height = this.heightLimit - 1;
            }
            int perLayer = (int) (1.382 + Math.pow(this.leafDensity * this.heightLimit / 13.0, 2.0));
            if (perLayer < 1) {
                perLayer = 1;
            }
            int[][] nodes = new int[perLayer * this.heightLimit][4];
            int y = this.basePos[1] + this.heightLimit - this.leafDistanceLimit;
            int count = 1;
            int trunkTop = this.basePos[1] + this.height;
            int layer = y - this.basePos[1];
            nodes[0][0] = this.basePos[0];
            nodes[0][1] = y;
            nodes[0][2] = this.basePos[2];
            nodes[0][3] = trunkTop;
            y--;

            while (layer >= 0) {
                int n = 0;
                float size = this.layerSize(layer);
                if (size < 0.0F) {
                    y--;
                    layer--;
                } else {
                    for (double half = 0.5; n < perLayer; n++) {
                        double distance = this.scaleWidth * size * (this.rand.nextFloat() + 0.328);
                        double angle = this.rand.nextFloat() * 2.0 * Math.PI;
                        int nx = OldMath.floor(distance * Math.sin(angle) + this.basePos[0] + half);
                        int nz = OldMath.floor(distance * Math.cos(angle) + this.basePos[2] + half);
                        int[] node = {nx, y, nz};
                        int[] above = {nx, y + this.leafDistanceLimit, nz};
                        if (this.checkBlockLine(node, above) == -1) {
                            int[] base = {this.basePos[0], this.basePos[1], this.basePos[2]};
                            double horizontal = Math.sqrt(Math.pow(Math.abs(this.basePos[0] - node[0]), 2.0)
                                    + Math.pow(Math.abs(this.basePos[2] - node[2]), 2.0));
                            double drop = horizontal * this.branchSlope;
                            if (node[1] - drop > trunkTop) {
                                base[1] = trunkTop;
                            } else {
                                base[1] = (int) (node[1] - drop);
                            }
                            if (this.checkBlockLine(base, node) == -1) {
                                nodes[count][0] = nx;
                                nodes[count][1] = y;
                                nodes[count][2] = nz;
                                nodes[count][3] = base[1];
                                count++;
                            }
                        }
                    }
                    y--;
                    layer--;
                }
            }
            this.leafNodes = new int[count][4];
            System.arraycopy(nodes, 0, this.leafNodes, 0, count);
        }

        private void genTreeLayer(int x, int y, int z, float radius, byte axis, int blockId) {
            int reach = (int) (radius + 0.618);
            byte a = OTHER_COORD_PAIRS[axis];
            byte b = OTHER_COORD_PAIRS[axis + 3];
            int[] center = {x, y, z};
            int[] pos = {0, 0, 0};
            pos[axis] = center[axis];
            for (int da = -reach; da <= reach; da++) {
                pos[a] = center[a] + da;
                for (int db = -reach; db <= reach; db++) {
                    double distance = Math.pow(Math.abs(da) + 0.5, 2.0) + Math.pow(Math.abs(db) + 0.5, 2.0);
                    if (distance <= radius * radius) {
                        pos[b] = center[b] + db;
                        int id = this.world.getBlockId(pos[0], pos[1], pos[2]);
                        if (id == 0 || id == LEAVES) {
                            this.world.setBlock(pos[0], pos[1], pos[2], blockId, 0);
                        }
                    }
                }
            }
        }

        private float layerSize(int layer) {
            if (layer < this.heightLimit * 0.3) {
                return -1.618F;
            }
            float half = this.heightLimit / 2.0F;
            float offset = this.heightLimit / 2.0F - layer;
            float size;
            if (offset == 0.0F) {
                size = half;
            } else if (Math.abs(offset) >= half) {
                size = 0.0F;
            } else {
                size = (float) Math.sqrt(Math.pow(Math.abs(half), 2.0) - Math.pow(Math.abs(offset), 2.0));
            }
            return size * 0.5F;
        }

        private float leafSize(int layer) {
            return layer >= 0 && layer < this.leafDistanceLimit ? (layer != 0 && layer != this.leafDistanceLimit - 1 ? 3.0F : 2.0F) : -1.0F;
        }

        private void generateLeafNode(int x, int y, int z) {
            for (int by = y; by < y + this.leafDistanceLimit; by++) {
                this.genTreeLayer(x, by, z, this.leafSize(by - y), (byte) 1, LEAVES);
            }
        }

        private void placeBlockLine(int[] from, int[] to, int blockId) {
            int[] delta = {0, 0, 0};
            byte longest = 0;
            for (byte i = 0; i < 3; i++) {
                delta[i] = to[i] - from[i];
                if (Math.abs(delta[i]) > Math.abs(delta[longest])) {
                    longest = i;
                }
            }
            if (delta[longest] == 0) {
                return;
            }
            byte a = OTHER_COORD_PAIRS[longest];
            byte b = OTHER_COORD_PAIRS[longest + 3];
            int step = delta[longest] > 0 ? 1 : -1;
            double slopeA = (double) delta[a] / delta[longest];
            double slopeB = (double) delta[b] / delta[longest];
            int[] pos = {0, 0, 0};
            for (int i = 0, end = delta[longest] + step; i != end; i += step) {
                pos[longest] = OldMath.floor(from[longest] + i + 0.5);
                pos[a] = OldMath.floor(from[a] + i * slopeA + 0.5);
                pos[b] = OldMath.floor(from[b] + i * slopeB + 0.5);
                int meta = 0;
                int dx = Math.abs(pos[0] - from[0]);
                int dz = Math.abs(pos[2] - from[2]);
                int max = Math.max(dx, dz);
                if (max > 0) {
                    if (dx == max) {
                        meta = 4;
                    } else if (dz == max) {
                        meta = 8;
                    }
                }
                this.world.setBlock(pos[0], pos[1], pos[2], blockId, meta);
            }
        }

        private void generateLeaves() {
            for (int[] node : this.leafNodes) {
                this.generateLeafNode(node[0], node[1], node[2]);
            }
        }

        private void generateTrunk() {
            int[] bottom = {this.basePos[0], this.basePos[1], this.basePos[2]};
            int[] top = {this.basePos[0], this.basePos[1] + this.height, this.basePos[2]};
            this.placeBlockLine(bottom, top, LOG);
        }

        private void generateLeafNodeBases() {
            int[] base = {this.basePos[0], this.basePos[1], this.basePos[2]};
            for (int[] node : this.leafNodes) {
                int[] end = {node[0], node[1], node[2]};
                base[1] = node[3];
                if (base[1] - this.basePos[1] >= this.heightLimit * 0.2) {
                    this.placeBlockLine(base, end, LOG);
                }
            }
        }

        /** -1 if the line is free (air or oak leaves), else the distance to the first obstacle. */
        private int checkBlockLine(int[] from, int[] to) {
            int[] delta = {0, 0, 0};
            byte longest = 0;
            for (byte i = 0; i < 3; i++) {
                delta[i] = to[i] - from[i];
                if (Math.abs(delta[i]) > Math.abs(delta[longest])) {
                    longest = i;
                }
            }
            if (delta[longest] == 0) {
                return -1;
            }
            byte a = OTHER_COORD_PAIRS[longest];
            byte b = OTHER_COORD_PAIRS[longest + 3];
            int step = delta[longest] > 0 ? 1 : -1;
            double slopeA = (double) delta[a] / delta[longest];
            double slopeB = (double) delta[b] / delta[longest];
            int[] pos = {0, 0, 0};
            int i = 0;
            int end = delta[longest] + step;
            for (; i != end; i += step) {
                pos[longest] = from[longest] + i;
                pos[a] = OldMath.floor(from[a] + i * slopeA);
                pos[b] = OldMath.floor(from[b] + i * slopeB);
                int id = this.world.getBlockId(pos[0], pos[1], pos[2]);
                if (id != 0 && id != LEAVES) {
                    break;
                }
            }
            return i == end ? -1 : Math.abs(i);
        }

        private boolean validTreeLocation() {
            int[] bottom = {this.basePos[0], this.basePos[1], this.basePos[2]};
            int[] top = {this.basePos[0], this.basePos[1] + this.heightLimit - 1, this.basePos[2]};
            if (!saplingSoil(this.world.getBlockId(this.basePos[0], this.basePos[1] - 1, this.basePos[2]))) {
                return false;
            }
            int free = this.checkBlockLine(bottom, top);
            if (free == -1) {
                return true;
            }
            if (free < 6) {
                return false;
            }
            this.heightLimit = free;
            return true;
        }

        @Override
        public void setScale(double height, double width, double leafDensity) {
            this.heightLimitLimit = (int) (height * 12.0);
            if (height > 0.5) {
                this.leafDistanceLimit = 5;
            }
            this.scaleWidth = width;
            this.leafDensity = leafDensity;
        }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            this.world = world;
            this.rand.setSeed(random.nextLong());
            this.basePos[0] = x;
            this.basePos[1] = y;
            this.basePos[2] = z;
            if (this.heightLimit == 0) {
                this.heightLimit = 5 + this.rand.nextInt(this.heightLimitLimit);
            }
            if (!this.validTreeLocation()) {
                return false;
            }
            this.generateLeafNodeList();
            this.generateLeaves();
            this.generateTrunk();
            this.generateLeafNodeBases();
            return true;
        }
    }
}
