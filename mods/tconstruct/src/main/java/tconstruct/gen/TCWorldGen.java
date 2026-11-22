package tconstruct.gen;

import hexxit.worldgen152.core.OldMath;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldOreGens;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.structure.OldStructures;

import java.util.Random;

/**
 * The world generation of Tinkers' Construct 1.4.3 without game classes, so that tools/expect.sh can run it:
 * {@code TBaseWorldGenerator} (ores, ore berry bushes, Nether ores), {@code SlimeIslandGen} (both registered with
 * GameRegistry, in that order), {@code TerrainGenEventHandler} (surface ores at the SAND decoration event), the two
 * village pieces and the chest loot. Ids and values of the pack's TinkersWorkshop.txt.
 */
public final class TCWorldGen {
    public static final String FML_ID = "TConstruct";

    // block ids (TinkersWorkshop.txt)
    public static final int ORE_BERRY = 1317;
    public static final int ORE_BERRY_SECOND = 1318;
    public static final int ORE_GRAVEL = 1319;
    public static final int ORE_SLAG = 1320;
    public static final int SEARED_TABLE = 1321;
    public static final int SMELTERY = 1322;
    public static final int CRAFTED_SOIL = 1323;
    public static final int TOOL_STATION_WOOD = 1326;
    public static final int SLIME_GEL = 1345;
    public static final int SLIME_POOL = 1350;
    public static final int SLIME_GRASS = 2384;
    public static final int SLIME_LEAVES = 2385;
    public static final int SLIME_TALL_GRASS = 2386;
    public static final int SLIME_SAPLING = 2387;
    /** Natura's tainted soil, which counts as netherrack for ore veins (isGenMineableReplaceable). */
    static final int TAINTED_SOIL = 253;

    // item ids (config id + 256)
    static final int HEART_CANISTER = 6027 + 256;
    static final int WOOD_PATTERN = 6043 + 256;

    // PHConstruct, values of the pack
    static final int SEA_LEVEL = 64;
    static final int COPPER_U_DENSITY = 2, COPPER_U_MIN_Y = 20, COPPER_U_MAX_Y = 60;
    static final int TIN_U_DENSITY = 2, TIN_U_MIN_Y = 0, TIN_U_MAX_Y = 40;
    static final int ALUMINUM_U_DENSITY = 3, ALUMINUM_U_MIN_Y = 0, ALUMINUM_U_MAX_Y = 64;
    static final int NETHER_DENSITY = 8;
    static final int IRON_BUSH_RARITY = 5, IRON_BUSH_DENSITY = 1;
    static final int GOLD_BUSH_RARITY = 8, GOLD_BUSH_DENSITY = 1;
    static final int COPPER_BUSH_RARITY = 3, COPPER_BUSH_DENSITY = 2, COPPER_BUSH_MIN_Y = 20, COPPER_BUSH_MAX_Y = 60;
    static final int TIN_BUSH_RARITY = 3, TIN_BUSH_DENSITY = 2, TIN_BUSH_MIN_Y = 0, TIN_BUSH_MAX_Y = 40;
    static final int ALUMINUM_BUSH_RARITY = 2, ALUMINUM_BUSH_DENSITY = 2, ALUMINUM_BUSH_MIN_Y = 0, ALUMINUM_BUSH_MAX_Y = 60;
    static final int ESSENCE_BUSH_RARITY = 6, SILVER_BUSH_DENSITY = 1;
    static final int IRON_S_RARITY = 400, GOLD_S_RARITY = 900, COPPER_S_RARITY = 100, TIN_S_RARITY = 100, ALUMINUM_S_RARITY = 50;
    static final int ISLAND_RARITY = 450;

    private static final OldOreGens.Minable COPPER = new OldOreGens.Minable(ORE_SLAG, 3, 8, 1);
    private static final OldOreGens.Minable TIN = new OldOreGens.Minable(ORE_SLAG, 4, 8, 1);
    private static final OldOreGens.Minable ALUMINUM = new OldOreGens.Minable(ORE_SLAG, 5, 6, 1);
    private static final OldOreGens.Minable COBALT = new OldOreGens.Minable(ORE_SLAG, 1, 3, 87, TAINTED_SOIL);
    private static final OldOreGens.Minable ARDITE = new OldOreGens.Minable(ORE_SLAG, 2, 3, 87, TAINTED_SOIL);
    private static final OreberryBushGen IRON_BUSH = new OreberryBushGen(ORE_BERRY, 12, 12);
    private static final OreberryBushGen GOLD_BUSH = new OreberryBushGen(ORE_BERRY, 13, 6);
    private static final OreberryBushGen COPPER_BUSH = new OreberryBushGen(ORE_BERRY, 14, 12);
    private static final OreberryBushGen TIN_BUSH = new OreberryBushGen(ORE_BERRY, 15, 12);
    private static final OreberryBushGen ALUMINUM_BUSH = new OreberryBushGen(ORE_BERRY_SECOND, 12, 14);
    private static final OreberryBushGen SILVER_BUSH = new OreberryBushGen(ORE_BERRY_SECOND, 13, 8);
    private static final SurfaceOreGen IRON_SURFACE = new SurfaceOreGen(ORE_GRAVEL, 0, 12);
    private static final SurfaceOreGen GOLD_SURFACE = new SurfaceOreGen(ORE_GRAVEL, 1, 20);
    private static final SurfaceOreGen COPPER_SURFACE = new SurfaceOreGen(ORE_GRAVEL, 2, 12);
    private static final SurfaceOreGen TIN_SURFACE = new SurfaceOreGen(ORE_GRAVEL, 3, 12);
    private static final SurfaceOreGen ALUMINUM_SURFACE = new SurfaceOreGen(ORE_GRAVEL, 4, 12);

    /** TContent.tinkerHouseChest ("TinkerHouse", 3 to 27 draws). */
    public static final OldLoot.Category TINKER_HOUSE_CHEST = OldLoot.newCategory(3, 27);
    /** TContent.tinkerHousePatterns ("TinkerPatterns", 5 to 30 draws). */
    public static final OldLoot.Category TINKER_HOUSE_PATTERNS = OldLoot.newCategory(5, 30);

    private static boolean registered;

    private TCWorldGen() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        registerBlocks();
        OldTerrainGen.registerWorldGenerator(FML_ID, "tconstruct:world", TCWorldGen::baseGenerate);
        OldTerrainGen.addListener(FML_ID, new OldTerrainGen.Listener() {
            @Override
            public boolean decorate(OldWorld world, Random random, int x, int z, OldTerrainGen.Decorate type) {
                if (type == OldTerrainGen.Decorate.SAND) {
                    surfaceOres(world, random, x, z);
                }
                return true;
            }
        });
        OldTerrainGen.registerWorldGenerator(FML_ID, "tconstruct:slime_islands", SlimeIslandGen::generate);
        registerIntegrated();
        // TConstruct.preInit: registerVillageCreationHandler (tool workshop, then smeltery)
        OldStructures.VILLAGE_HANDLERS.add(new ComponentToolWorkshop.Handler());
        OldStructures.VILLAGE_HANDLERS.add(new ComponentSmeltery.Handler());
        addLoot();
    }

    /** What the generators place or read, with the properties of the 1.5.2 blocks. */
    private static void registerBlocks() {
        // OreberryBush: BlockLeavesBase, Material.leaves, not opaque, isLeaves false
        OldBlockInfo.register(ORE_BERRY, Material.LEAVES, false, 0, false);
        OldBlockInfo.register(ORE_BERRY_SECOND, Material.LEAVES, false, 0, false);
        OldBlockInfo.registerCube(ORE_GRAVEL, Material.CRAFTED_SNOW);   // GravelOre extends BlockSand, Material.craftedSnow
        OldBlockInfo.registerCube(ORE_SLAG, Material.IRON);
        OldBlockInfo.register(SEARED_TABLE, Material.ROCK, false, 0, false);
        OldBlockInfo.registerCube(SMELTERY, Material.ROCK);
        OldBlockInfo.registerCube(CRAFTED_SOIL, Material.GROUND);
        OldBlockInfo.register(TOOL_STATION_WOOD, Material.WOOD, false, 0, false);
        OldBlockInfo.registerCube(SLIME_GEL, Material.SPONGE);
        OldBlockInfo.register(SLIME_POOL, Material.WATER, false, 0, false);
        OldBlockInfo.registerCube(SLIME_GRASS, Material.GRASS);
        // SlimeLeaves extends BlockLeaves: opaqueCubeLookup true (see OldBlockInfo.registerLeaves), setLightOpacity(1)
        OldBlockInfo.registerLeaves(SLIME_LEAVES, true);
        OldBlockInfo.register(SLIME_TALL_GRASS, Material.VINE, false, 0, false);
        OldBlockInfo.register(SLIME_SAPLING, Material.PLANTS, false, 0, false);
    }

    /** TContent.addLoot. */
    private static void addLoot() {
        OldLoot.DUNGEON_CHEST.add(FML_ID, new OldStack(HEART_CANISTER, 1, 1), 1, 1, 5);
        OldLoot.PYRAMID_DESERT_CHEST.add(FML_ID, new OldStack(HEART_CANISTER, 1, 1), 1, 1, 10);
        OldLoot.PYRAMID_JUNGLE_CHEST.add(FML_ID, new OldStack(HEART_CANISTER, 1, 1), 1, 1, 10);
        TINKER_HOUSE_CHEST.add(FML_ID, new OldStack(HEART_CANISTER, 1, 1), 1, 1, 1);
        int[] validTypes = {0, 1, 2, 3, 4, 5, 6, 8, 9, 13, 14, 17};
        // pickaxeHead, shovelHead, hatchetHead, binding, swordBlade, wideGuard, handGuard, crossbar, knifeBlade, frypanHead,
        // signHead, chiselHead
        int[] partIds = {6053, 6054, 6044, 6057, 6056, 6050, 6052, 6046, 6049, 6047, 6055, 6045};
        for (int part : partIds) {
            for (int type : validTypes) {
                TINKER_HOUSE_CHEST.add(FML_ID, new OldStack(part + 256, type, 1), 1, 1, 15);
            }
        }
        for (int i = 0; i < 13; i++) {
            TINKER_HOUSE_PATTERNS.add(FML_ID, new OldStack(WOOD_PATTERN, i + 1, 1), 1, 3, 20);
        }
        TINKER_HOUSE_PATTERNS.add(FML_ID, new OldStack(WOOD_PATTERN, 22, 1), 1, 3, 40);
    }

    // ---------------------------------------------------------------- world type Hexxit Integrated

    /**
     * The pieces of TBaseWorldGenerator and of the SAND event handler as features of their own (world type Hexxit
     * Integrated), so that each ore can be switched off or replaced. The underground ores run with {@code y_offset}
     * -64 and {@link OldTerrainGen#deepY}: the heights of 1.5.2 stretched over the deeper world. Copper ore is left
     * out: vanilla has its own (the ore berries and the ore gravel of copper stay).
     */
    private static void registerIntegrated() {
        OldTerrainGen.registerFeature("tconstruct:tin_ore", (random, chunkX, chunkZ, world) ->
                deepOre(world, random, chunkX * 16, chunkZ * 16, TIN, TIN_U_DENSITY, TIN_U_MIN_Y, TIN_U_MAX_Y));
        OldTerrainGen.registerFeature("tconstruct:aluminum_ore", (random, chunkX, chunkZ, world) ->
                deepOre(world, random, chunkX * 16, chunkZ * 16, ALUMINUM, ALUMINUM_U_DENSITY, ALUMINUM_U_MIN_Y, ALUMINUM_U_MAX_Y));
        OldTerrainGen.registerFeature("tconstruct:oreberry_bushes", (random, chunkX, chunkZ, world) -> {
            if (world.dimension() == 0) {
                generateOreBushes(random, chunkX * 16, chunkZ * 16, world);
            }
        });
        OldTerrainGen.registerFeature("tconstruct:surface_ores", (random, chunkX, chunkZ, world) -> {
            if (world.dimension() == 0) {
                surfaceOres(world, random, chunkX * 16, chunkZ * 16);
            }
        });
        OldTerrainGen.registerFeature("tconstruct:cobalt_ore", (random, chunkX, chunkZ, world) -> netherOre(world, random, chunkX * 16, chunkZ * 16, COBALT));
        OldTerrainGen.registerFeature("tconstruct:ardite_ore", (random, chunkX, chunkZ, world) -> netherOre(world, random, chunkX * 16, chunkZ * 16, ARDITE));
    }

    /** generateUndergroundOres for one ore (twice in the Extreme Hills), heights through deepY. */
    static void deepOre(OldWorld world, Random random, int xChunk, int zChunk, OldOreGens.Minable ore, int density, int minY, int maxY) {
        if (world.dimension() != 0) {
            return;
        }
        String biomeName = world.getBiome(xChunk, zChunk).name;
        int rounds = "Extreme Hills Edge".equals(biomeName) || "Extreme Hills".equals(biomeName) ? 2 : 1;
        for (int round = 0; round < rounds; round++) {
            for (int q = 0; q <= density; q++) {
                int x = xChunk + random.nextInt(16);
                int y = OldTerrainGen.deepY(minY + random.nextInt(maxY - minY));
                int z = zChunk + random.nextInt(16);
                ore.generate(world, random, x, y, z);
            }
        }
    }

    /** generateNether for one of the two ores: both loops of the original. */
    static void netherOre(OldWorld world, Random random, int xChunk, int zChunk, OldOreGens.Minable ore) {
        if (!world.isHellWorld()) {
            return;
        }
        for (int i = 0; i < NETHER_DENSITY; i++) {
            ore.generate(world, random, xChunk + random.nextInt(16), random.nextInt(64) + 32, zChunk + random.nextInt(16));
        }
        for (int i = 0; i < NETHER_DENSITY; i++) {
            ore.generate(world, random, xChunk + random.nextInt(16), random.nextInt(128), zChunk + random.nextInt(16));
        }
    }

    // ---------------------------------------------------------------- TBaseWorldGenerator

    static void baseGenerate(Random random, int chunkX, int chunkZ, OldWorld world) {
        if (world.isHellWorld()) {
            generateNether(random, chunkX * 16, chunkZ * 16, world);
        } else {
            // terrainType is never FLAT in the pack
            generateSurface(random, chunkX * 16, chunkZ * 16, world);
            if (world.dimension() == 0) {
                generateOreBushes(random, chunkX * 16, chunkZ * 16, world);
            }
        }
        // superfun and the world border are off
    }

    static void generateSurface(Random random, int xChunk, int zChunk, OldWorld world) {
        String biomeName = world.getBiome(xChunk, zChunk).name;
        generateUndergroundOres(random, xChunk, zChunk, world);
        if ("Extreme Hills Edge".equals(biomeName) || "Extreme Hills".equals(biomeName)) {
            generateUndergroundOres(random, xChunk, zChunk, world);
        }
    }

    static void generateUndergroundOres(Random random, int xChunk, int zChunk, OldWorld world) {
        for (int q = 0; q <= COPPER_U_DENSITY; q++) {
            int x = xChunk + random.nextInt(16);
            int y = COPPER_U_MIN_Y + random.nextInt(COPPER_U_MAX_Y - COPPER_U_MIN_Y);
            int z = zChunk + random.nextInt(16);
            COPPER.generate(world, random, x, y, z);
        }
        for (int q = 0; q <= TIN_U_DENSITY; q++) {
            int x = xChunk + random.nextInt(16);
            int y = TIN_U_MIN_Y + random.nextInt(TIN_U_MAX_Y - TIN_U_MIN_Y);
            int z = zChunk + random.nextInt(16);
            TIN.generate(world, random, x, y, z);
        }
        for (int q = 0; q <= ALUMINUM_U_DENSITY; q++) {
            int x = xChunk + random.nextInt(16);
            int y = ALUMINUM_U_MIN_Y + random.nextInt(ALUMINUM_U_MAX_Y - ALUMINUM_U_MIN_Y);
            int z = zChunk + random.nextInt(16);
            ALUMINUM.generate(world, random, x, y, z);
        }
    }

    static void generateOreBushes(Random random, int xChunk, int zChunk, OldWorld world) {
        if (random.nextInt(IRON_BUSH_RARITY + 1) == 0) {
            for (int i = 0; i < IRON_BUSH_DENSITY; i++) {
                int x = xChunk + random.nextInt(16);
                int y = SEA_LEVEL - 32;
                int z = zChunk + random.nextInt(16);
                y = findAdequateLocation(world, x, y, z, SEA_LEVEL, 0);
                if (y != -1) {
                    IRON_BUSH.generate(world, random, x, y, z);
                }
            }
        }
        if (random.nextInt(GOLD_BUSH_RARITY + 1) == 0) {
            for (int i = 0; i < GOLD_BUSH_DENSITY; i++) {
                int x = xChunk + random.nextInt(16);
                int y = 16;
                int z = zChunk + random.nextInt(16);
                y = findAdequateLocation(world, x, y, z, 32, 0);
                if (y != -1) {
                    GOLD_BUSH.generate(world, random, x, y, z);
                }
            }
        }
        if (random.nextInt(COPPER_BUSH_RARITY + 1) == 0) {
            for (int i = 0; i < COPPER_BUSH_DENSITY; i++) {
                int x = xChunk + random.nextInt(16);
                int y = (COPPER_BUSH_MAX_Y + COPPER_BUSH_MIN_Y) / 2;
                int z = zChunk + random.nextInt(16);
                y = findAdequateLocation(world, x, y, z, COPPER_BUSH_MAX_Y, COPPER_BUSH_MIN_Y);
                if (y != -1) {
                    COPPER_BUSH.generate(world, random, x, y, z);
                }
            }
        }
        if (random.nextInt(TIN_BUSH_RARITY + 1) == 0) {
            for (int i = 0; i < TIN_BUSH_DENSITY; i++) {
                int x = xChunk + random.nextInt(16);
                int y = (TIN_BUSH_MAX_Y + TIN_BUSH_MIN_Y) / 2;
                int z = zChunk + random.nextInt(16);
                y = findAdequateLocation(world, x, y, z, TIN_BUSH_MAX_Y, TIN_BUSH_MIN_Y);
                if (y != -1) {
                    TIN_BUSH.generate(world, random, x, y, z);
                }
            }
        }
        if (random.nextInt(ALUMINUM_BUSH_RARITY + 1) == 0) {
            for (int i = 0; i < ALUMINUM_BUSH_DENSITY; i++) {
                int x = xChunk + random.nextInt(16);
                int y = (ALUMINUM_BUSH_MAX_Y + ALUMINUM_BUSH_MIN_Y) / 2;
                int z = zChunk + random.nextInt(16);
                y = findAdequateLocation(world, x, y, z, ALUMINUM_BUSH_MAX_Y, ALUMINUM_BUSH_MIN_Y);
                if (y != -1) {
                    ALUMINUM_BUSH.generate(world, random, x, y, z);
                }
            }
        }
        // generateEssenceBush: no "+ 1" here in the original
        if (random.nextInt(ESSENCE_BUSH_RARITY) == 0) {
            for (int i = 0; i < SILVER_BUSH_DENSITY; i++) {
                int x = xChunk + random.nextInt(16);
                int y = SEA_LEVEL - 16;
                int z = zChunk + random.nextInt(16);
                y = findAdequateLocation(world, x, y, z, 32, 0);
                if (y != -1) {
                    SILVER_BUSH.generate(world, random, x, y, z);
                }
            }
        }
    }

    static void generateNether(Random random, int xChunk, int zChunk, OldWorld world) {
        for (int i = 0; i < NETHER_DENSITY; i++) {
            int x = xChunk + random.nextInt(16);
            int y = random.nextInt(64) + 32;
            int z = zChunk + random.nextInt(16);
            COBALT.generate(world, random, x, y, z);
            x = xChunk + random.nextInt(16);
            y = random.nextInt(64) + 32;
            z = zChunk + random.nextInt(16);
            ARDITE.generate(world, random, x, y, z);
        }
        for (int i = 0; i < NETHER_DENSITY; i++) {
            int x = xChunk + random.nextInt(16);
            int y = random.nextInt(128);
            int z = zChunk + random.nextInt(16);
            COBALT.generate(world, random, x, y, z);
            x = xChunk + random.nextInt(16);
            y = random.nextInt(128);
            z = zChunk + random.nextInt(16);
            ARDITE.generate(world, random, x, y, z);
        }
    }

    /** An air block with a block above it (a cave ceiling), first upwards, then downwards. */
    static int findAdequateLocation(OldWorld world, int x, int y, int z, int heightLimit, int depthLimit) {
        int height = y;
        while (world.getBlockId(x, height, z) != 0 || world.getBlockId(x, height + 1, z) == 0) {
            if (++height >= heightLimit) {
                height = y;
                while (world.getBlockId(x, height, z) != 0 || world.getBlockId(x, height - 1, z) == 0) {
                    if (--height <= depthLimit) {
                        return -1;
                    }
                }
                return height - 1;
            }
        }
        return height + 1;
    }

    // ---------------------------------------------------------------- TerrainGenEventHandler

    /** Decorate SAND: once, twice in the Extreme Hills (BiomeGenBase.extremeHills, extremeHillsEdge). */
    static void surfaceOres(OldWorld world, Random random, int x, int z) {
        int biome = world.getBiome(x, z).id;
        int iterations = biome == 3 || biome == 20 ? 2 : 1;
        for (int i = 0; i < iterations; i++) {
            generateSurfaceOres(random, x, z, world);
        }
    }

    private static void generateSurfaceOres(Random random, int xChunk, int zChunk, OldWorld world) {
        if (random.nextInt(IRON_S_RARITY) == 0) {
            int x = xChunk + random.nextInt(16);
            int y = 64 + SEA_LEVEL;
            int z = zChunk + random.nextInt(16);
            IRON_SURFACE.generate(world, random, x, y, z);
        }
        if (random.nextInt(GOLD_S_RARITY) == 0) {
            int x = xChunk + random.nextInt(16);
            int y = 64 + SEA_LEVEL;
            int z = zChunk + random.nextInt(16);
            GOLD_SURFACE.generate(world, random, x, y, z);
        }
        if (random.nextInt(COPPER_S_RARITY) == 0) {
            int x = xChunk + random.nextInt(16);
            int y = 64 + SEA_LEVEL;
            int z = zChunk + random.nextInt(16);
            COPPER_SURFACE.generate(world, random, x, y, z);
        }
        if (random.nextInt(TIN_S_RARITY) == 0) {
            int x = xChunk + random.nextInt(16);
            int y = 64 + SEA_LEVEL;
            int z = zChunk + random.nextInt(16);
            TIN_SURFACE.generate(world, random, x, y, z);
        }
        if (random.nextInt(ALUMINUM_S_RARITY) == 0) {
            int x = xChunk + random.nextInt(16);
            int y = 64 + SEA_LEVEL;
            int z = zChunk + random.nextInt(16);
            ALUMINUM_SURFACE.generate(world, random, x, y, z);
        }
        // generateCobaltSurface is never set (false): no draw
    }

    /** Block.isGenMineableReplaceable of the 1.5.2 blocks: the same id. */
    static boolean replaceable(int id, int[] targets) {
        for (int target : targets) {
            if (id == target) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- generators

    /** OreberryBushGen, built with the varargs constructor and no targets: replaces only blocks that are not opaque. */
    static final class OreberryBushGen {
        private final int blockId;
        private final int metadata;
        private final int chance;

        OreberryBushGen(int blockId, int meta, int chance) {
            this.blockId = blockId;
            this.metadata = meta;
            this.chance = chance;
        }

        void generate(OldWorld world, Random random, int x, int y, int z) {
            int type = random.nextInt(this.chance);
            if (type == 11) {
                this.mediumNode(world, random, x, y, z);
            } else if (type >= 5) {
                this.smallNode(world, random, x, y, z);
            } else {
                this.tinyNode(world, random, x, y, z);
            }
        }

        private void mediumNode(OldWorld world, Random random, int x, int y, int z) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (random.nextInt(4) == 0) {
                            this.berry(world, x + dx, y + dy, z + dz);
                        }
                    }
                }
            }
            this.smallNode(world, random, x, y, z);
        }

        private void smallNode(OldWorld world, Random random, int x, int y, int z) {
            this.berry(world, x, y, z);
            if (random.nextBoolean()) {
                this.berry(world, x + 1, y, z);
            }
            if (random.nextBoolean()) {
                this.berry(world, x - 1, y, z);
            }
            if (random.nextBoolean()) {
                this.berry(world, x, y, z + 1);
            }
            if (random.nextBoolean()) {
                this.berry(world, x, y, z - 1);
            }
            if (random.nextBoolean()) {
                this.berry(world, x, y + 1, z);
            }
            if (random.nextBoolean()) {
                this.berry(world, x, y + 1, z);
            }
        }

        private void tinyNode(OldWorld world, Random random, int x, int y, int z) {
            this.berry(world, x, y, z);
            if (random.nextInt(4) == 0) {
                this.berry(world, x + 1, y, z);
            }
            if (random.nextInt(4) == 0) {
                this.berry(world, x - 1, y, z);
            }
            if (random.nextInt(4) == 0) {
                this.berry(world, x, y, z + 1);
            }
            if (random.nextInt(4) == 0) {
                this.berry(world, x, y, z - 1);
            }
            if (random.nextInt(4) == 0) {
                this.berry(world, x, y + 1, z);
            }
            if (random.nextInt(4) == 0) {
                this.berry(world, x, y + 1, z);
            }
        }

        private void berry(OldWorld world, int x, int y, int z) {
            int id = world.getBlockId(x, y, z);
            // an existing block that is the end portal frame or opaque would need a target in the empty list
            if (id == 0 || id != 120 && !OldBlockInfo.opaqueCube(id)) {
                world.setBlock(x, y, z, this.blockId, this.metadata);
            }
        }
    }

    /** SurfaceOreGen with changeSize = true and the default targets. */
    static final class SurfaceOreGen {
        private static final int[] TARGETS = {1, 2, 3, 9, 12, 13, 78};
        private final int blockId;
        private final int meta;
        private final int numberOfBlocks;

        SurfaceOreGen(int id, int meta, int number) {
            this.blockId = id;
            this.meta = meta;
            this.numberOfBlocks = number;
        }

        private static int findGround(OldWorld world, int x, int y, int z) {
            int returnHeight = -1;
            int below = world.getBlockId(x, y - 1, z);
            if (OldBlockInfo.opaqueCube(world.getBlockId(x, y, z)) || below != 3 && below != 2) {
                int height = SEA_LEVEL + 64;
                while (height >= SEA_LEVEL - 30) {
                    int id = world.getBlockId(x, height, z);
                    if (id == 3 || id == 2) {
                        if (!OldBlockInfo.opaqueCube(world.getBlockId(x, height + 1, z))) {
                            returnHeight = height + 1;
                        }
                        break;
                    }
                    if (--height <= 0) {
                        break;
                    }
                }
                return returnHeight;
            }
            return y;
        }

        boolean generate(OldWorld world, Random random, int startX, int startY, int startZ) {
            startY = findGround(world, startX, startY, startZ);
            if (startY == -1) {
                return false;
            }
            float f = random.nextFloat() * (float) Math.PI;
            int blockNumber = this.numberOfBlocks * 2 / 5 + random.nextInt(this.numberOfBlocks * 3 / 5);
            double d0 = startX + 8 + OldMath.sin(f) * blockNumber / 8.0F;
            double d1 = startX + 8 - OldMath.sin(f) * blockNumber / 8.0F;
            double d2 = startZ + 8 + OldMath.cos(f) * blockNumber / 8.0F;
            double d3 = startZ + 8 - OldMath.cos(f) * blockNumber / 8.0F;
            double d4 = startY + random.nextInt(3) - 2;
            double d5 = startY + random.nextInt(3) - 2;
            for (int l = 0; l <= blockNumber; l++) {
                double d6 = d0 + (d1 - d0) * l / blockNumber;
                double d7 = d4 + (d5 - d4) * l / blockNumber;
                double d8 = d2 + (d3 - d2) * l / blockNumber;
                double d9 = random.nextDouble() * blockNumber / 16.0;
                double d10 = (OldMath.sin(l * (float) Math.PI / blockNumber) + 1.0F) * d9 + 1.0;
                double d11 = (OldMath.sin(l * (float) Math.PI / blockNumber) + 1.0F) * d9 + 1.0;
                int i1 = OldMath.floor(d6 - d10 / 2.0);
                int j1 = OldMath.floor(d7 - d11 / 2.0);
                int k1 = OldMath.floor(d8 - d10 / 2.0);
                int l1 = OldMath.floor(d6 + d10 / 2.0);
                int i2 = OldMath.floor(d7 + d11 / 2.0);
                int j2 = OldMath.floor(d8 + d10 / 2.0);
                for (int k2 = i1; k2 <= l1; k2++) {
                    double d12 = (k2 + 0.5 - d6) / (d10 / 2.0);
                    if (d12 * d12 < 1.0) {
                        for (int l2 = j1; l2 <= i2; l2++) {
                            double d13 = (l2 + 0.5 - d7) / (d11 / 2.0);
                            if (d12 * d12 + d13 * d13 < 1.0) {
                                for (int i3 = k1; i3 <= j2; i3++) {
                                    double d14 = (i3 + 0.5 - d8) / (d10 / 2.0);
                                    if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0) {
                                        int id = world.getBlockId(k2, l2, i3);
                                        if (id != 0 && OldBlockInfo.opaqueCube(id)) {
                                            if (replaceable(id, TARGETS)) {
                                                world.setBlock(k2, l2, i3, this.blockId, this.meta);
                                            }
                                        } else {
                                            world.setBlock(k2, l2, i3, this.blockId, this.meta);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
    }
}
