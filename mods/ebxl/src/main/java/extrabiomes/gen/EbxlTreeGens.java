package extrabiomes.gen;

import hexxit.worldgen152.core.OldMath;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

import static extrabiomes.gen.EbxlIds.*;

/**
 * The tree generators of ExtrabiomesXL 3.13.4 ({@code extrabiomes.module.summa.worldgen.WorldGen*}), line for
 * line, with the blocks the pack configuration gave them. Used for world generation and by the saplings.
 */
public final class EbxlTreeGens {
    private static final int DIRT = 3;
    private static final int OAK_LOG = 17;
    private static final int OAK_LEAVES = 18;
    private static final int VINE = 106;

    private EbxlTreeGens() {
    }

    /** TreeSoilRegistry: grass, dirt, farmland. */
    static boolean validSoil(int id) {
        return id == 2 || id == 3 || id == 60;
    }

    static boolean isLeaves(int id) {
        return id != 0 && OldBlockInfo.leaves(id);
    }

    static boolean isWood(int id) {
        return id != 0 && OldBlockInfo.wood(id);
    }

    /** {@code block == null || block.canBeReplacedByLeaves(...)}. */
    static boolean replaceable(int id) {
        return id == 0 || OldBlockInfo.replacedByLeaves(id);
    }

    /** WorldGenAcacia. */
    public static final class Acacia extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int height = rand.nextInt(4) + 6;
            boolean canGrow = true;
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            for (int y1 = y; y1 <= y + 1 + height; y1++) {
                int clearance = 1;
                if (y1 == y) {
                    clearance = 0;
                }
                if (y1 >= y + 1 + height - 2) {
                    clearance = 2;
                }
                for (int x1 = x - clearance; x1 <= x + clearance && canGrow; x1++) {
                    for (int z1 = z - clearance; z1 <= z + clearance && canGrow; z1++) {
                        if (y1 >= 0 && y1 < 256) {
                            int id = world.getBlockId(x1, y1, z1);
                            if (id != 0 && !isLeaves(id) && id != 2 && id != DIRT && !isWood(id)) {
                                canGrow = false;
                            }
                        } else {
                            canGrow = false;
                        }
                    }
                }
            }
            if (!canGrow) {
                return false;
            }
            if (!validSoil(world.getBlockId(x, y - 1, z)) || y >= 256 - height - 1) {
                return false;
            }
            world.setBlock(x, y - 1, z, DIRT);
            for (int y1 = y - 3 + height; y1 <= y + height; y1++) {
                int distanceFromTop = y1 - (y + height);
                int canopyRadius = 1 - distanceFromTop;
                for (int x1 = x - canopyRadius; x1 <= x + canopyRadius; x1++) {
                    int xOnRadius = x1 - x;
                    for (int z1 = z - canopyRadius; z1 <= z + canopyRadius; z1++) {
                        int zOnRadius = z1 - z;
                        if ((Math.abs(xOnRadius) != canopyRadius || Math.abs(zOnRadius) != canopyRadius || rand.nextInt(2) != 0 && distanceFromTop != 0)
                                && replaceable(world.getBlockId(x1, y1, z1))) {
                            world.setBlock(x1, y1, z1, GREEN_LEAVES, LEAVES_ACACIA);
                        }
                    }
                }
            }
            for (int y1 = 0; y1 < height; y1++) {
                int id = world.getBlockId(x, y + y1, z);
                if (id == 0 || isLeaves(id)) {
                    world.setBlock(x, y + y1, z, CUSTOM_LOG, LOG_ACACIA);
                }
            }
            return true;
        }
    }

    /** WorldGenFirTree. */
    public static final class Fir extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int below = world.getBlockId(x, y - 1, z);
            int height = rand.nextInt(8) + 24;
            if (!validSoil(below) || y >= 256 - height - 1) {
                return false;
            }
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            int bare = 1 + rand.nextInt(12);
            int maxRadius = 2 + rand.nextInt(6);
            for (int y1 = y; y1 <= y + 1 + height; y1++) {
                if (y1 < 0 || y1 >= 256) {
                    return false;
                }
                int r = y1 - y < bare ? 0 : maxRadius;
                for (int x1 = x - r; x1 <= x + r; x1++) {
                    for (int z1 = z - r; z1 <= z + r; z1++) {
                        int id = world.getBlockId(x1, y1, z1);
                        if (id != 0 && !isLeaves(id)) {
                            return false;
                        }
                    }
                }
            }
            world.setBlock(x, y - 1, z, DIRT);
            int radius = rand.nextInt(2);
            int limit = 1;
            boolean flag = false;
            for (int i = 0; i <= height - bare; i++) {
                int y1 = y + height - i;
                for (int x1 = x - radius; x1 <= x + radius; x1++) {
                    int dx = x1 - x;
                    for (int z1 = z - radius; z1 <= z + radius; z1++) {
                        int dz = z1 - z;
                        if ((Math.abs(dx) != radius || Math.abs(dz) != radius || radius <= 0) && replaceable(world.getBlockId(x1, y1, z1))) {
                            world.setBlock(x1, y1, z1, GREEN_LEAVES, LEAVES_FIR);
                        }
                    }
                }
                if (radius >= limit) {
                    radius = flag ? 1 : 0;
                    flag = true;
                    if (++limit > maxRadius) {
                        limit = maxRadius;
                    }
                } else {
                    radius++;
                }
            }
            int shorter = rand.nextInt(3);
            for (int i = 0; i < height - shorter; i++) {
                int id = world.getBlockId(x, y + i, z);
                if (id == 0 || isLeaves(id)) {
                    world.setBlock(x, y + i, z, CUSTOM_LOG, LOG_FIR);
                }
            }
            return true;
        }
    }

    /** The 2x2 trunk of WorldGenRedwood and WorldGenFirTreeHuge: quarter logs around the corner x-1/z-1..x/z. */
    private static void quarterTrunk(OldWorld world, int x, int y, int z, int meta) {
        world.setBlock(x, y, z, QUARTER_LOG_SE, meta);
        world.setBlock(x - 1, y, z, QUARTER_LOG_SW, meta);
        world.setBlock(x, y, z - 1, QUARTER_LOG_NE, meta);
        world.setBlock(x - 1, y, z - 1, QUARTER_LOG_NW, meta);
    }

    private static void dirtBelow2x2(OldWorld world, int x, int y, int z) {
        world.setBlock(x, y - 1, z, DIRT);
        world.setBlock(x - 1, y - 1, z, DIRT);
        world.setBlock(x, y - 1, z - 1, DIRT);
        world.setBlock(x - 1, y - 1, z - 1, DIRT);
    }

    /** Leaves at x/z and its three neighbours towards -x/-z, where leaves may go. */
    private static void leaves2x2(OldWorld world, int x, int y, int z, int meta) {
        for (int[] d : new int[][] {{0, 0}, {-1, 0}, {0, -1}, {-1, -1}}) {
            if (replaceable(world.getBlockId(x + d[0], y, z + d[1]))) {
                world.setBlock(x + d[0], y, z + d[1], GREEN_LEAVES, meta);
            }
        }
    }

    /** The cone of the redwood and the huge fir, built from the top down. */
    private static void cone2x2(OldWorld world, Random rand, int x, int y, int z, int height, int leafRows, int maxRadius, int leavesMeta) {
        int radius = rand.nextInt(2);
        int limit = 1;
        boolean flag = false;
        for (int i = 0; i <= leafRows; i++) {
            int y1 = y + height - i;
            for (int x1 = x - radius; x1 <= x + radius; x1++) {
                int dx = x1 - x;
                for (int z1 = z - radius; z1 <= z + radius; z1++) {
                    int dz = z1 - z;
                    if (Math.abs(dx) != radius || Math.abs(dz) != radius || radius <= 0) {
                        leaves2x2(world, x1, y1, z1, leavesMeta);
                    }
                }
            }
            if (radius >= limit) {
                radius = flag ? 1 : 0;
                flag = true;
                if (++limit > maxRadius) {
                    limit = maxRadius;
                }
            } else {
                radius++;
            }
        }
    }

    /** WorldGenRedwood. */
    public static final class Redwood extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int height = rand.nextInt(30) + 32;
            int bare = 1 + rand.nextInt(12);
            int leafRows = height - bare;
            int maxRadius = 2 + rand.nextInt(6);
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            if (!validSoil(world.getBlockId(x, y - 1, z)) || y >= 256 - height - 1) {
                return false;
            }
            for (int y1 = y; y1 <= y + 1 + height; y1++) {
                int r = y1 - y < bare ? 0 : maxRadius;
                for (int x1 = x - r; x1 <= x + r; x1++) {
                    for (int z1 = z - r; z1 <= z + r; z1++) {
                        if (y1 < 0 || y1 >= 256) {
                            return false;
                        }
                        // the original stops at leaves, not at solid blocks
                        if (isLeaves(world.getBlockId(x1, y1, z1))) {
                            return false;
                        }
                    }
                }
            }
            dirtBelow2x2(world, x, y, z);
            cone2x2(world, rand, x, y, z, height, leafRows, maxRadius, LEAVES_REDWOOD);
            int shorter = rand.nextInt(3);
            for (int i = 0; i < height - shorter; i++) {
                int id = world.getBlockId(x, y + i, z);
                if (id == 0 || isLeaves(id)) {
                    quarterTrunk(world, x, y + i, z, QUARTER_REDWOOD);
                }
            }
            return true;
        }
    }

    /** WorldGenFirTreeHuge. The original also gave up next to chunks that were not loaded; here all are. */
    public static final class FirHuge extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int height = rand.nextInt(16) + 32;
            int bare = 1 + rand.nextInt(12);
            int leafRows = height - bare;
            int maxRadius = 2 + rand.nextInt(9);
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            for (int y1 = y; y1 <= y + 1 + height; y1++) {
                int r = y1 - y < bare ? 0 : maxRadius;
                for (int x1 = x - r; x1 <= x + r; x1++) {
                    for (int z1 = z - r; z1 <= z + r; z1++) {
                        int id = world.getBlockId(x1, y1, z1);
                        if (id != 0 && !isLeaves(id)) {
                            return false;
                        }
                    }
                }
            }
            if (!validSoil(world.getBlockId(x, y - 1, z)) || y >= 256 - height - 1) {
                return false;
            }
            dirtBelow2x2(world, x, y, z);
            cone2x2(world, rand, x, y, z, height, leafRows, maxRadius, LEAVES_FIR);
            int shorter = rand.nextInt(3);
            for (int i = 0; i < height - shorter; i++) {
                int id = world.getBlockId(x, y + i, z);
                if (id == 0 || isLeaves(id)) {
                    quarterTrunk(world, x, y + i, z, QUARTER_FIR);
                }
            }
            return true;
        }
    }

    /** WorldGenAutumnTree. Its trunk stayed the vanilla spruce log: setTrunkBlock was never called. */
    public static final class Autumn extends OldGen {
        final int leavesMeta;

        public Autumn(int leavesMeta) {
            this.leavesMeta = leavesMeta;
        }

        private static boolean isRoomToGrow(OldWorld world, int x, int y, int z, int height) {
            for (int i = y; i <= y + 1 + height; i++) {
                if (i < 0 || i >= 256) {
                    return false;
                }
                int radius = 1;
                if (i == y) {
                    radius = 0;
                }
                if (i >= y + 1 + height - 2) {
                    radius = 2;
                }
                for (int x1 = x - radius; x1 <= x + radius; x1++) {
                    for (int z1 = z - radius; z1 <= z + radius; z1++) {
                        int id = world.getBlockId(x1, i, z1);
                        if (id != 0 && !isLeaves(id) && id != 2 && !isWood(id)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            int height = rand.nextInt(3) + 4;
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            if (!validSoil(world.getBlockId(x, y - 1, z)) || !isRoomToGrow(world, x, y, z, height)) {
                return false;
            }
            world.setBlock(x, y - 1, z, DIRT);
            for (int y1 = y - 3 + height; y1 <= y + height; y1++) {
                int canopyRow = y1 - (y + height);
                int radius = 1 - canopyRow / 2;
                for (int x1 = x - radius; x1 <= x + radius; x1++) {
                    int dx = x1 - x;
                    for (int z1 = z - radius; z1 <= z + radius; z1++) {
                        int dz = z1 - z;
                        if ((Math.abs(dx) != radius || Math.abs(dz) != radius || rand.nextInt(2) != 0 && canopyRow != 0)
                                && replaceable(world.getBlockId(x1, y1, z1))) {
                            world.setBlock(x1, y1, z1, AUTUMN_LEAVES, this.leavesMeta);
                        }
                    }
                }
            }
            for (int i = 0; i < height; i++) {
                int id = world.getBlockId(x, y + i, z);
                if (id == 0 || isLeaves(id)) {
                    world.setBlock(x, y + i, z, OAK_LOG, 1);
                }
            }
            return true;
        }
    }

    /**
     * WorldGenBigAutumnTree: the big oak with autumn leaves on an oak trunk. Like the vanilla big oak it keeps
     * its height limit between trees; the decorator therefore takes a fresh one per chunk
     * ({@link OldWorld#chunkState}).
     */
    public static final class BigAutumn extends OldGen {
        private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};
        private final int leavesMeta;
        private final Random rand = new Random();
        private OldWorld world;
        private final int[] basePos = {0, 0, 0};
        private int heightLimit = 0;
        private int height;
        private double scaleWidth = 1.0;
        private double leafDensity = 1.0;
        private int heightLimitLimit = 12;
        private int leafDistanceLimit = 4;
        private int[][] leafNodes;

        public BigAutumn(int leavesMeta) {
            this.leavesMeta = leavesMeta;
        }

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
                if (id != 0 && !isLeaves(id)) {
                    break;
                }
            }
            return i == end ? -1 : Math.abs(i);
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
            for (int[] node : this.leafNodes) {
                for (int by = node[1]; by < node[1] + this.leafDistanceLimit; by++) {
                    this.genTreeLayer(node[0], by, node[2], this.leafSize(by - node[1]));
                }
            }
            this.placeBlockLine(new int[] {x, y, z}, new int[] {x, y + this.height, z});
            int[] base = {x, y, z};
            for (int[] node : this.leafNodes) {
                base[1] = node[3];
                if (base[1] - this.basePos[1] >= this.heightLimit * 0.2) {
                    this.placeBlockLine(base, new int[] {node[0], node[1], node[2]});
                }
            }
            return true;
        }

        private void generateLeafNodeList() {
            this.height = (int) (this.heightLimit * 0.618);
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
                float size = this.layerSize(layer);
                if (size >= 0.0F) {
                    for (int n = 0; n < perLayer; n++) {
                        double distance = this.scaleWidth * size * (this.rand.nextFloat() + 0.328);
                        double angle = this.rand.nextFloat() * 2.0 * Math.PI;
                        int nx = OldMath.floor(distance * Math.sin(angle) + this.basePos[0] + 0.5);
                        int nz = OldMath.floor(distance * Math.cos(angle) + this.basePos[2] + 0.5);
                        int[] node = {nx, y, nz};
                        int[] above = {nx, y + this.leafDistanceLimit, nz};
                        if (this.checkBlockLine(node, above) == -1) {
                            int[] base = {this.basePos[0], this.basePos[1], this.basePos[2]};
                            double horizontal = Math.sqrt(Math.pow(Math.abs(this.basePos[0] - node[0]), 2.0)
                                    + Math.pow(Math.abs(this.basePos[2] - node[2]), 2.0));
                            double drop = horizontal * 0.381;
                            base[1] = node[1] - drop > trunkTop ? trunkTop : (int) (node[1] - drop);
                            if (this.checkBlockLine(base, node) == -1) {
                                nodes[count][0] = nx;
                                nodes[count][1] = y;
                                nodes[count][2] = nz;
                                nodes[count][3] = base[1];
                                count++;
                            }
                        }
                    }
                }
                y--;
                layer--;
            }
            this.leafNodes = new int[count][4];
            System.arraycopy(nodes, 0, this.leafNodes, 0, count);
        }

        private void genTreeLayer(int x, int y, int z, float size) {
            int reach = (int) (size + 0.618);
            int[] center = {x, y, z};
            int[] pos = {0, 0, 0};
            pos[1] = center[1];
            for (int da = -reach; da <= reach; da++) {
                pos[0] = center[0] + da;
                for (int db = -reach; db <= reach; db++) {
                    double distance = Math.pow(Math.abs(da) + 0.5, 2.0) + Math.pow(Math.abs(db) + 0.5, 2.0);
                    if (distance > size * size) {
                        continue;
                    }
                    pos[2] = center[2] + db;
                    int id = this.world.getBlockId(pos[0], pos[1], pos[2]);
                    if (id != 0 && !isLeaves(id)) {
                        continue;
                    }
                    if (replaceable(id)) {
                        this.world.setBlock(pos[0], pos[1], pos[2], AUTUMN_LEAVES, this.leavesMeta);
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

        private void placeBlockLine(int[] from, int[] to) {
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
                this.world.setBlock(pos[0], pos[1], pos[2], OAK_LOG, meta);
            }
        }

        private boolean validTreeLocation() {
            int[] bottom = {this.basePos[0], this.basePos[1], this.basePos[2]};
            int[] top = {this.basePos[0], this.basePos[1] + this.heightLimit - 1, this.basePos[2]};
            int soil = this.world.getBlockId(this.basePos[0], this.basePos[1] - 1, this.basePos[2]);
            if (soil != 2 && soil != 3) {
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
    }

    /** WorldGenCustomSwamp: a taller swamp oak that may stand in water, with vines. */
    public static final class CustomSwamp extends OldGen {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            while (world.material(x, y - 1, z) == OldBlockInfo.Material.WATER) {
                y--;
            }
            int height = rand.nextInt(4) + 10;
            if (y < 1 || y + height + 1 > 256) {
                return false;
            }
            for (int y1 = y; y1 <= y + 1 + height; y1++) {
                int clearance = 1;
                if (y1 == y) {
                    clearance = 0;
                }
                if (y1 >= y + 1 + height - 2) {
                    clearance = 3;
                }
                for (int x1 = x - clearance; x1 <= x + clearance; x1++) {
                    for (int z1 = z - clearance; z1 <= z + clearance; z1++) {
                        int id = world.getBlockId(x1, y1, z1);
                        if (id != 0 && !isLeaves(id)) {
                            if (id != 9 && id != 8) {
                                return false;
                            }
                            if (y1 > y) {
                                return false;
                            }
                        }
                    }
                }
            }
            int soil = world.getBlockId(x, y - 1, z);
            if (soil != 2 && soil != 3 || y >= 256 - height - 1) {
                return false;
            }
            world.setBlock(x, y - 1, z, DIRT);
            for (int y1 = y - 3 + height; y1 <= y + height; y1++) {
                int posTrunk = y1 - (y + height);
                int radius = 2 - posTrunk / 2;
                for (int x1 = x - radius; x1 <= x + radius; x1++) {
                    int dx = x1 - x;
                    for (int z1 = z - radius; z1 <= z + radius; z1++) {
                        int dz = z1 - z;
                        if ((Math.abs(dx) != radius || Math.abs(dz) != radius || rand.nextInt(2) != 0 && posTrunk != 0)
                                && replaceable(world.getBlockId(x1, y1, z1))) {
                            world.setBlock(x1, y1, z1, OAK_LEAVES);
                        }
                    }
                }
            }
            for (int i = 0; i < height; i++) {
                int id = world.getBlockId(x, y + i, z);
                if (id == 0 || isLeaves(id) || id == 8 || id == 9) {
                    world.setBlock(x, y + i, z, OAK_LOG);
                }
            }
            for (int y1 = y - 3 + height; y1 <= y + height; y1++) {
                int posTrunk = y1 - (y + height);
                int radius = 2 - posTrunk / 2;
                for (int x1 = x - radius; x1 <= x + radius; x1++) {
                    for (int z1 = z - radius; z1 <= z + radius; z1++) {
                        if (isLeaves(world.getBlockId(x1, y1, z1))) {
                            if (rand.nextInt(4) == 0 && world.getBlockId(x1 - 1, y1, z1) == 0) {
                                vines(world, x1 - 1, y1, z1, 8);
                            }
                            if (rand.nextInt(4) == 0 && world.getBlockId(x1 + 1, y1, z1) == 0) {
                                vines(world, x1 + 1, y1, z1, 2);
                            }
                            if (rand.nextInt(4) == 0 && world.getBlockId(x1, y1, z1 - 1) == 0) {
                                vines(world, x1, y1, z1 - 1, 1);
                            }
                            if (rand.nextInt(4) == 0 && world.getBlockId(x1, y1, z1 + 1) == 0) {
                                vines(world, x1, y1, z1 + 1, 4);
                            }
                        }
                    }
                }
            }
            return true;
        }

        private static void vines(OldWorld world, int x, int y, int z, int meta) {
            world.setBlock(x, y, z, VINE, meta);
            for (int i = 4; world.getBlockId(x, --y, z) == 0 && i > 0; i--) {
                world.setBlock(x, y, z, VINE, meta);
            }
        }
    }

    /** WorldGenLegendOak: a 2x2 oak trunk with long winding branches. */
    public static final class LegendOak extends OldGen {
        private enum Bend { LEFT, STRAIGHT, RIGHT }

        private enum Acuteness { LOOSE, TIGHTER, TIGHT }

        @Override
        public boolean generate(OldWorld world, Random random, int x, int y, int z) {
            if (!validSoil(world.getBlockId(x, y - 1, z))) {
                return false;
            }
            int height = random.nextInt(4) + 3;
            int size = 15 + random.nextInt(25);
            dirtBelow2x2(world, x, y, z);
            for (int i = 0; i < height + 1; i++) {
                quarterTrunk(world, x, y + i, z, QUARTER_OAK);
            }
            for (Bend xDir : Bend.values()) {
                for (Bend zDir : Bend.values()) {
                    if (xDir != Bend.STRAIGHT || zDir != Bend.STRAIGHT) {
                        primary(world, random, size, xDir, zDir, x, y + height, z);
                        inside(world, random, size, xDir, zDir, x, y + height, z, 2 * size / 3, size / 3, 3);
                        inside(world, random, size, xDir, zDir, x, y + height, z, size / 3, size / 6, 6);
                    }
                }
            }
            return true;
        }

        private static void leaf(OldWorld world, int x, int y, int z) {
            if (world.getBlockId(x, y, z) == 0) {
                world.setBlock(x, y, z, OAK_LEAVES);
            }
        }

        private static void leafNode(OldWorld world, int x, int y, int z) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    int ax = Math.abs(dx);
                    int az = Math.abs(dz);
                    if ((ax != 3 || az != 3) && (ax != 3 || az != 2) && (ax != 2 || az != 3) && (dx != 0 || dz != 0)) {
                        leaf(world, x + dx, y, z + dz);
                    }
                    if (ax < 3 && az < 3 && (ax != 2 || az != 2)) {
                        leaf(world, x + dx, y - 1, z + dz);
                        leaf(world, x + dx, y + 1, z + dz);
                    }
                }
            }
        }

        private static int step(Random random, Bend direction) {
            switch (direction) {
                case STRAIGHT:
                    return random.nextInt(3) - 1;
                case RIGHT:
                    return random.nextInt(2);
                default:
                    return -random.nextInt(2);
            }
        }

        /** inside() and insideSmall(): {@code length} branch blocks, side branches after {@code start}. */
        private static void inside(OldWorld world, Random random, int size, Bend xDir, Bend zDir, int x, int y, int z,
                int length, int start, int divisor) {
            for (int i = 0; i < length; i++) {
                world.setBlock(x, y, z, OAK_LOG);
                if (random.nextInt(3) == 0 || i == length - 1) {
                    leafNode(world, x, y, z);
                }
                x += step(random, xDir);
                z += step(random, zDir);
                if (random.nextInt(6) == 0 && i > start) {
                    secondary(world, random, size / divisor - i / divisor, xDir, zDir, x, y, z);
                }
                y++;
            }
        }

        private static void primary(OldWorld world, Random random, int size, Bend xDir, Bend zDir, int x, int y, int z) {
            Acuteness acuteness = Acuteness.LOOSE;
            if (xDir == Bend.RIGHT) {
                x += 2;
            }
            if (xDir == Bend.LEFT) {
                x -= 2;
            }
            if (zDir == Bend.RIGHT) {
                z += 2;
            }
            if (zDir == Bend.LEFT) {
                z -= 2;
            }
            for (int length = 0; length < size; length++) {
                switch (acuteness) {
                    case LOOSE:
                        if (random.nextInt(4) == 0) {
                            y++;
                        }
                        break;
                    case TIGHTER:
                        if (random.nextInt(2) == 0) {
                            y++;
                        }
                        break;
                    case TIGHT:
                        y++;
                        break;
                }
                world.setBlock(x, y, z, OAK_LOG);
                if (random.nextInt(3) == 0 || length == size - 1) {
                    leafNode(world, x, y, z);
                }
                x += step(random, xDir);
                z += step(random, zDir);
                if (length == size / 3) {
                    acuteness = Acuteness.TIGHTER;
                }
                if (length == 2 * size / 3) {
                    acuteness = Acuteness.TIGHT;
                }
                if (random.nextInt(4) == 0) {
                    secondary(world, random, size / 2 - length / 2, xDir, zDir, x, y, z);
                }
            }
        }

        /** Two side branches in the original; its counter was not reset, so only the first one ever grew. */
        private static void secondary(OldWorld world, Random random, int size, Bend xDir, Bend zDir, int x, int y, int z) {
            int length = 0;
            for (int branch = 0; branch < 2; branch++) {
                int x1 = x;
                int y1 = y;
                int z1 = z;
                while (length < size) {
                    if (random.nextInt(2) == 0) {
                        y1++;
                    }
                    world.setBlock(x1, y1, z1, OAK_LOG);
                    if (random.nextInt(4) == 0 || length == size - 1) {
                        leafNode(world, x1, y1, z1);
                    }
                    if (zDir == Bend.STRAIGHT) {
                        x1 += xDir == Bend.RIGHT ? random.nextInt(2) : -random.nextInt(2);
                        z1 += branch == 0 ? random.nextInt(2) : -random.nextInt(2);
                    }
                    if (xDir == Bend.STRAIGHT) {
                        z1 += zDir == Bend.RIGHT ? random.nextInt(2) : -random.nextInt(2);
                        x1 += branch == 0 ? random.nextInt(2) : -random.nextInt(2);
                    }
                    if (xDir == Bend.RIGHT) {
                        if (zDir == Bend.RIGHT) {
                            if (branch == 0) {
                                z1 += random.nextInt(2);
                            } else {
                                x1 += random.nextInt(2);
                            }
                        }
                        if (zDir == Bend.LEFT) {
                            if (branch == 0) {
                                z1 -= random.nextInt(2);
                            } else {
                                x1 += random.nextInt(2);
                            }
                        }
                    }
                    if (xDir == Bend.LEFT) {
                        if (zDir == Bend.RIGHT) {
                            if (branch == 0) {
                                z1 += random.nextInt(2);
                            } else {
                                x1 -= random.nextInt(2);
                            }
                        }
                        if (zDir == Bend.LEFT) {
                            if (branch == 0) {
                                z1 -= random.nextInt(2);
                            } else {
                                x1 -= random.nextInt(2);
                            }
                        }
                    }
                    length++;
                }
            }
        }
    }
}
