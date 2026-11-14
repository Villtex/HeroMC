package hexxit.worldgen152.core.populate;

/**
 * What the 1.5.2 world generators asked a block about: its material, whether it is an opaque cube, how much
 * light it swallows. Indexed by the numeric block id. Mods register the blocks they generate with
 * {@link #register}; asking about an unknown id is an error, so a missing registration shows up at once.
 */
public final class OldBlockInfo {
    /** Material of 1.5.2, reduced to the properties world generation reads. */
    public enum Material {
        AIR(false, false, false, true, false),
        GRASS, GROUND, WOOD, ROCK, IRON, SPONGE, CLOTH, SAND, CRAFTED_SNOW, CLAY, PUMPKIN, PISTON,
        WATER(false, false, true, true, true),
        LAVA(false, false, true, true, true),
        LEAVES(true, true, false, false, false),
        PLANTS(false, false, false, false, true),
        VINE(false, false, false, true, true),
        FIRE(false, false, false, true, false),
        CIRCUITS(false, false, false, false, true),
        GLASS(true, true, false, false, false),
        TNT(true, true, false, false, false),
        ICE(true, true, false, false, false),
        SNOW(false, false, false, true, false),
        CACTUS(true, true, false, false, false),
        WEB(true, false, false, false, true);

        public final boolean solid;
        public final boolean blocksMovement;
        public final boolean liquid;
        public final boolean replaceable;
        /** Material.isOpaque: not translucent and blocks movement. */
        public final boolean opaque;

        Material() {
            this(true, true, false, false, true);
        }

        Material(boolean solid, boolean blocksMovement, boolean liquid, boolean replaceable, boolean opaque) {
            this.solid = solid;
            this.blocksMovement = blocksMovement;
            this.liquid = liquid;
            this.replaceable = replaceable;
            this.opaque = opaque && blocksMovement;
        }
    }

    public static final int MAX_ID = 4096;

    private static final Material[] MATERIAL = new Material[MAX_ID];
    private static final boolean[] OPAQUE_CUBE = new boolean[MAX_ID];
    private static final boolean[] NORMAL_RENDER = new boolean[MAX_ID];
    private static final int[] LIGHT_OPACITY = new int[MAX_ID];
    private static final boolean[] LEAVES = new boolean[MAX_ID];
    private static final boolean[] WOOD = new boolean[MAX_ID];

    static {
        MATERIAL[0] = Material.AIR;
        cube(Material.ROCK, 1, 4, 7, 14, 15, 16, 21, 22, 23, 24, 43, 45, 48, 49, 56, 61, 62, 73, 74, 87, 98, 112, 121, 129, 153, 155, 158);
        cube(Material.GRASS, 2, 110);
        cube(Material.GROUND, 3);
        cube(Material.WOOD, 5, 17, 25, 47, 58, 84, 99, 100, 125);
        cube(Material.SAND, 12, 13, 88);
        cube(Material.SPONGE, 19);
        cube(Material.CLOTH, 35);
        cube(Material.IRON, 41, 42, 57, 133, 152);
        cube(Material.TNT, 46);
        cube(Material.CRAFTED_SNOW, 80);
        cube(Material.CLAY, 82, 97);
        cube(Material.PUMPKIN, 86, 91, 103);
        cube(Material.GLASS, 89);
        WOOD[17] = true;

        // liquids: water dims light by 3, lava blocks it
        register(8, Material.WATER, false, 3, false);
        register(9, Material.WATER, false, 3, false);
        register(10, Material.LAVA, false, 255, false);
        register(11, Material.LAVA, false, 255, false);

        register(18, Material.LEAVES, false, 1, true);
        LEAVES[18] = true;
        register(20, Material.GLASS, false, 0, false);
        register(30, Material.WEB, false, 1, false);
        register(52, Material.ROCK, false, 0, true);
        register(54, Material.WOOD, false, 0, false);
        register(79, Material.ICE, false, 3, true);
        register(81, Material.CACTUS, false, 0, false);
        register(78, Material.SNOW, false, 0, false);
        register(51, Material.FIRE, false, 0, false);
        // slabs, stairs, farmland: not opaque cubes, but they stop light completely
        for (int id : new int[] {44, 53, 60, 67, 108, 109, 114, 126, 128, 134, 135, 136, 156}) {
            register(id, id == 60 ? Material.GROUND : id == 53 || id == 126 || id >= 134 && id <= 136 ? Material.WOOD : Material.ROCK, false, 255, false);
        }
        for (int id : new int[] {85, 107}) {
            register(id, Material.WOOD, false, 0, false);
        }
        for (int id : new int[] {101, 113, 139}) {
            register(id, id == 101 ? Material.IRON : Material.ROCK, false, 0, false);
        }
        register(102, Material.GLASS, false, 0, false);
        for (int id : new int[] {6, 37, 38, 39, 40, 59, 83, 104, 105, 111, 115, 127, 141, 142}) {
            register(id, Material.PLANTS, false, 0, false);
        }
        for (int id : new int[] {31, 32, 106}) {
            register(id, Material.VINE, false, 0, false);
        }
        for (int id : new int[] {27, 28, 50, 55, 63, 65, 66, 68, 69, 70, 72, 75, 76, 77, 93, 94, 131, 132, 140, 143, 157}) {
            register(id, Material.CIRCUITS, false, 0, false);
        }
        for (int id : new int[] {64, 96}) {
            register(id, Material.WOOD, false, 0, false);
        }
        register(71, Material.IRON, false, 0, false);
    }

    private OldBlockInfo() {
    }

    private static void cube(Material material, int... ids) {
        for (int id : ids) {
            register(id, material, true, 255, true);
        }
    }

    /**
     * Describes a block for the generators.
     *
     * @param opaqueCube   Block.isOpaqueCube
     * @param lightOpacity Block.lightOpacity (255 for opaque cubes, 0 for most others)
     * @param normalRender Block.renderAsNormalBlock
     */
    public static synchronized void register(int id, Material material, boolean opaqueCube, int lightOpacity, boolean normalRender) {
        if (id <= 0 || id >= MAX_ID) {
            throw new IllegalArgumentException("Block ids must be 1.." + (MAX_ID - 1) + ", got " + id);
        }
        if (MATERIAL[id] != null) {
            throw new IllegalArgumentException("Block id " + id + " is already described");
        }
        MATERIAL[id] = material;
        OPAQUE_CUBE[id] = opaqueCube;
        LIGHT_OPACITY[id] = lightOpacity;
        NORMAL_RENDER[id] = normalRender;
    }

    /** An ordinary full block. */
    public static void registerCube(int id, Material material) {
        register(id, material, true, 255, true);
    }

    /** Block.isLeaves: leaves are see-through for tree checks, hold snow and get a decay distance. */
    public static void registerLeaves(int id) {
        register(id, Material.LEAVES, false, 1, true);
        LEAVES[id] = true;
    }

    /** Block.isWood: a log, which trees may grow through and leaves hang on. */
    public static void registerLog(int id) {
        registerCube(id, Material.WOOD);
        WOOD[id] = true;
    }

    public static boolean known(int id) {
        return id >= 0 && id < MAX_ID && MATERIAL[id] != null;
    }

    public static Material material(int id) {
        Material material = MATERIAL[id];
        if (material == null) {
            throw new IllegalStateException("1.5.2 world generation met block id " + id + ", which no mod has described (OldBlockInfo.register)");
        }
        return material;
    }

    public static boolean opaqueCube(int id) {
        return OPAQUE_CUBE[id];
    }

    public static int lightOpacity(int id) {
        return LIGHT_OPACITY[id];
    }

    public static boolean leaves(int id) {
        return LEAVES[id];
    }

    public static boolean wood(int id) {
        return WOOD[id];
    }

    /** Block.isBlockNormalCube. */
    public static boolean normalCube(int id) {
        return id != 0 && material(id).opaque && NORMAL_RENDER[id];
    }

    /** What vines hold on to (BlockVine.canBePlacedOn). */
    public static boolean holdsVines(int id) {
        return id != 0 && NORMAL_RENDER[id] && material(id).blocksMovement;
    }

    /** Block.canBeReplacedByLeaves. */
    public static boolean replacedByLeaves(int id) {
        return !OPAQUE_CUBE[id];
    }

    /** Soil for saplings, flowers and grass (canSustainPlant, plant type Plains). */
    public static boolean plainsSoil(int id) {
        return id == 2 || id == 3;
    }
}
