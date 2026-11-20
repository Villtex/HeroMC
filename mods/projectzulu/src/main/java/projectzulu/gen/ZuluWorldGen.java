package projectzulu.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The world generation of Project Zulu 1.0.3.8 in the 1.5.2 core ({@code ProjectZuluGenerator}, registered by
 * ProjectZulu|Core): structures (pyramid, labyrinth, cemetery, oasis; {@code StructureManager}), sand over desert
 * water ({@code WorldGenReplaceTop}), aloe vera, night bloom, creeper blossom and palm saplings on the surface
 * ({@code WorldGenOnSurface}). No game classes (tools/expect.sh compiles this). The block states for the ids come
 * from the block classes of the mod (OldBlockStates).
 *
 * <p>Not seed-fixed in 1.5.2 and replaced by a Random from world seed and position: the maze of the pyramid
 * ({@code new Random()} of WorldGenPyramid) and everything of the cemetery ({@code world.rand}).
 */
public final class ZuluWorldGen {
    /** FML mod id of the mod that registered ProjectZuluGenerator. */
    public static final String FML_ID = "ProjectZulu|Core";

    /**
     * Told about every structure built ("Pyramid x y z"), for finding test chunks without the game; {@code null} in
     * the game.
     */
    public static volatile Consumer<String> built;

    private ZuluWorldGen() {
    }

    public static void register() {
        ZuluLoot.register();
        registerBlocks();
        OldTerrainGen.registerWorldGenerator(FML_ID, ZuluWorldGen::generate);
    }

    /**
     * OldBlockInfo of every Project Zulu block (the generators place or meet them): material, isOpaqueCube,
     * lightOpacity and renderAsNormalBlock of 1.5.2.
     */
    private static void registerBlocks() {
        block(ZuluIds.ALOE_VERA, Material.PLANTS, false, 0, false);
        block(ZuluIds.CAMPFIRE, Material.WOOD, false, 0, false);
        block(ZuluIds.COCONUT, Material.PLANTS, false, 0, false);
        block(ZuluIds.CREEPER_BLOSSOM, Material.TNT, false, 0, false);
        block(ZuluIds.JASPER, Material.ROCK, true, 255, true);
        block(ZuluIds.LIMITED_MOB_SPAWNER, Material.ROCK, false, 0, true);
        block(ZuluIds.MOB_SKULLS, Material.CIRCUITS, false, 0, false);
        block(ZuluIds.NIGHT_BLOOM, Material.PLANTS, false, 0, false);
        // BlockWoodSlab: opaque only as a double slab, setLightOpacity(0) for both
        block(ZuluIds.PALM_TREE_DOUBLE_SLAB, Material.WOOD, true, 0, true);
        if (!OldBlockInfo.known(ZuluIds.PALM_TREE_LEAVES)) {
            // BlockLeavesBase that overrides isOpaqueCube and renderAsNormalBlock with false, light opacity 1
            OldBlockInfo.registerLeaves(ZuluIds.PALM_TREE_LEAVES, false, false);
        }
        if (!OldBlockInfo.known(ZuluIds.PALM_TREE_LOG)) {
            OldBlockInfo.registerLog(ZuluIds.PALM_TREE_LOG);
        }
        block(ZuluIds.PALM_TREE_PLANK, Material.WOOD, true, 255, true);
        block(ZuluIds.PALM_TREE_SAPLING, Material.PLANTS, false, 0, false);
        block(ZuluIds.PALM_TREE_SLAB, Material.WOOD, false, 0, false);
        block(ZuluIds.PALM_TREE_STAIRS, Material.WOOD, false, 0, false);
        OldBlockInfo.registerStairs(ZuluIds.PALM_TREE_STAIRS);
        block(ZuluIds.QUICK_SAND, Material.SAND, true, 255, true);
        block(ZuluIds.SPIKES, Material.IRON, false, 0, false);
        block(ZuluIds.TOMBSTONE, Material.ROCK, false, 0, false);
        block(ZuluIds.TUMBLEWEED, Material.PLANTS, true, 255, true);
        block(ZuluIds.UNIVERSAL_FLOWER_POT, Material.WOOD, false, 0, false);
        block(ZuluIds.WATERED_DIRT, Material.SAND, false, 0, true);
    }

    private static void block(int id, Material material, boolean opaqueCube, int lightOpacity, boolean normalRender) {
        if (!OldBlockInfo.known(id)) {
            OldBlockInfo.register(id, material, opaqueCube, lightOpacity, normalRender);
        }
    }

    /** ProjectZuluGenerator.generate: the switch has no break, so the Nether (-1) falls through into the surface. */
    private static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        switch (world.dimension()) {
            case -1:
                // generateNether is empty
            case 0:
                generateSurface(world, random, chunkX * 16, chunkZ * 16);
                break;
            default:
                break;
        }
    }

    /** WorldGenReplaceTop(sand, {still water, flowing water}, desert, desertHills). */
    private static final int[] WATER = {9, 8};
    private static final OldBiome[] DESERTS = {OldBiomes.DESERT, OldBiomes.DESERT_HILLS};

    private static void generateSurface(OldWorld world, Random random, int blockX, int blockZ) {
        // ProjectZulu|World is loaded in the pack
        int x = blockX + random.nextInt(16);
        int z = blockZ + random.nextInt(16);
        int y = world.getTopSolidOrLiquidBlock(x, z);
        if (Structure.tryToGenerateStructure(world, blockX, blockZ, x, y, z, random)) {
            return;
        }
        random.nextInt(60); // Ycoord of WorldGenReplaceTop, which it does not use
        ZuluSurface.replaceTop(world, 12, WATER, DESERTS, blockX, blockZ);
        // ProjectZulu|Blocks is loaded, all four plants exist
        plant(world, random, blockX, blockZ, ZuluIds.ALOE_VERA, 3, 5);
        plant(world, random, blockX, blockZ, ZuluIds.NIGHT_BLOOM, 2, 85);
        plant(world, random, blockX, blockZ, ZuluIds.CREEPER_BLOSSOM, 1, 5);
        plant(world, random, blockX, blockZ, ZuluIds.PALM_TREE_SAPLING, 6, 5);
    }

    /** x, y (unused by WorldGenOnSurface), z drawn in this order, then the generator. */
    private static void plant(OldWorld world, Random random, int blockX, int blockZ, int id, int number, int chance) {
        int x = blockX + random.nextInt(16);
        random.nextInt(60);
        int z = blockZ + random.nextInt(16);
        ZuluSurface.onSurface(world, random, id, number, chance, x, z);
    }

    // ---------------------------------------------------------------- StructureManager

    /**
     * StructureManager with the values of the pack configuration: minDistance, chunksPerSpawn from
     * ProjectZuluConfig.cfg ("world generation controls", the defaults), the biomes set to true in
     * ProjectZuluStructureBiomeConfig.cfg. The biomes are matched by name (1.5.2 compared the biome objects of
     * BiomeGenBase.biomeList, whose names are unique in the pack).
     */
    enum Structure {
        Pyramid(9, 100, Set.of("Desert", "Mountainous Desert")) {
            @Override
            boolean checkToGenerate(OldWorld world, int x, int y, int z, Random random) {
                return world.material(x, y, z) != Material.WATER && doesTerrainFluctuateTooMuch(world, x, y, z, 5, 12);
            }

            @Override
            boolean generate(OldWorld world, int x, int y, int z, Random random) {
                return ZuluPyramid.generate(world, random, x, y, z, ZuluPyramid.classRandom(world.seed(), x, y, z));
            }
        },
        Labyrinth(9, 100, Set.of("Birch Forest", "Desert", "DesertHills", "Extreme Hills", "Extreme Hills Edge", "Forest", "ForestHills",
                "Forested Hills", "Forested Island", "Green Hills", "Ice Mountains", "Ice Plains", "Jungle", "JungleHills", "Lush Redwoods",
                "Mountain Taiga", "Mountainous Desert", "Pine Forest", "Plains", "Rainforest", "Redwood Forest", "River", "Snow Forest",
                "Snowy Rainforest", "Swampland", "Taiga", "TaigaHills", "Temperate Rainforest", "Woodlands")) {
            @Override
            boolean checkToGenerate(OldWorld world, int x, int y, int z, Random random) {
                return world.material(x, y, z) != Material.WATER && doesTerrainFluctuateTooMuch(world, x, y, z, 5, 12);
            }

            @Override
            boolean generate(OldWorld world, int x, int y, int z, Random random) {
                return ZuluMaze.generate(new ZuluMaze.BuildingManagerLabyrinth(world), 1, 4, 3, 24, 1, 1, world, random, x, y - 11, z);
            }
        },
        Cemetary(3, 100, Set.of("Birch Forest", "DesertHills", "Extreme Hills", "Extreme Hills Edge", "Forest", "ForestHills", "Forested Hills",
                "Forested Island", "Green Hills", "Ice Mountains", "Ice Plains", "Jungle", "JungleHills", "Lush Redwoods", "Mountain Taiga",
                "Pine Forest", "Plains", "Rainforest", "Redwood Forest", "Snow Forest", "Snowy Rainforest", "Swampland", "Taiga", "TaigaHills",
                "Temperate Rainforest", "Woodlands")) {
            @Override
            boolean checkToGenerate(OldWorld world, int x, int y, int z, Random random) {
                return world.material(x, y - 1, z) != Material.ICE && world.material(x, y, z) != Material.WATER
                        && world.material(x, y - 1, z) != Material.SAND && doesTerrainFluctuateTooMuch(world, x, y, z, 2, 6);
            }

            @Override
            boolean generate(OldWorld world, int x, int y, int z, Random random) {
                // world.rand in the original
                return ZuluMaze.generate(new ZuluMaze.BuildingManagerCemetary(world), 1, 4, 3, 11, 1, 1, world, worldRand(world.seed(), x, y, z),
                        x, y - 1, z);
            }
        },
        Oasis(3, 400, Set.of("Desert", "DesertHills")) {
            @Override
            boolean checkToGenerate(OldWorld world, int x, int y, int z, Random random) {
                // palm sapling, log and leaves exist in the pack
                return doesTerrainFluctuateTooMuch(world, x, y, z, 3, 6);
            }

            @Override
            boolean generate(OldWorld world, int x, int y, int z, Random random) {
                return ZuluOasis.generate(world, random, x, y - 1, z);
            }
        };

        final int minDistance;
        final int chunksPerSpawn;
        /** biomesToSpawn: the biomes whose name is set to true in ProjectZuluStructureBiomeConfig.cfg. */
        final Set<String> biomes;

        Structure(int minDistance, int chunksPerSpawn, Set<String> biomes) {
            this.minDistance = minDistance;
            this.chunksPerSpawn = chunksPerSpawn;
            this.biomes = biomes;
        }

        abstract boolean checkToGenerate(OldWorld world, int x, int y, int z, Random random);

        abstract boolean generate(OldWorld world, int x, int y, int z, Random random);

        /**
         * StructureManager.tryToGenerateStructure ({@code blockX}/{@code blockZ} are the chunk's block coordinates, as
         * the original passed them). It keeps no state between chunks: "minDistance" only picks the chunk rows and
         * columns whose index is a multiple of it, and a biome that does not fit ends the search for this chunk.
         */
        static boolean tryToGenerateStructure(OldWorld world, int blockX, int blockZ, int x, int y, int z, Random random) {
            for (Structure structure : values()) {
                if ((blockX / 16 % structure.minDistance == 0 || blockZ / 16 % structure.minDistance == 0)
                        && random.nextInt(structure.chunksPerSpawn) == 0) {
                    if (!structure.biomes.contains(world.getBiome(x, z).name)) {
                        return false;
                    }
                    if (structure.checkToGenerate(world, x, y, z, random) && structure.generate(world, x, y, z, random)) {
                        Consumer<String> report = built;
                        if (report != null) {
                            report.accept(structure.name() + " " + x + " " + y + " " + z);
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        private static boolean doesTerrainFluctuateTooMuch(OldWorld world, int x, int y, int z, int maxDifference, int squareLength) {
            return Math.abs(world.getTopSolidOrLiquidBlock(x + squareLength, z + squareLength) - y) < maxDifference
                    && Math.abs(world.getTopSolidOrLiquidBlock(x - squareLength, z - squareLength) - y) < maxDifference
                    && Math.abs(world.getTopSolidOrLiquidBlock(x - squareLength, z + squareLength) - y) < maxDifference
                    && Math.abs(world.getTopSolidOrLiquidBlock(x + squareLength, z - squareLength) - y) < maxDifference;
        }
    }

    /** Fixed replacement for world.rand of the cemetery (not seed-fixed in 1.5.2): world seed and position. */
    static Random worldRand(long seed, int x, int y, int z) {
        return new Random(seed ^ x * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL ^ y * 0x165667B19E3779F9L ^ 0x43454D45L);
    }
}
