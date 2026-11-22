package extrabiomes.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldDecorator;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldLakeGens;
import hexxit.worldgen152.core.populate.OldOreGens;
import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldTreeGens;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static extrabiomes.gen.EbxlIds.*;

/**
 * Decoration of the ExtrabiomesXL biomes as in 3.13.4 with the Hexxit configuration: per-biome decorator
 * counts ({@code DecorationSettings}), the weighted tree and grass choices ({@code BiomeManagerImpl}) and the
 * mod's world generators. No game classes, so tools/expect.sh predicts it too.
 */
public final class EbxlWorldGen {
    private EbxlWorldGen() {
    }

    /** A generator from a weighted list; {@code fixed} is null for the ones that need the chunk or the random. */
    private record Weighted(Function<OldWorld, OldGen> gen, int weight) {
    }

    private static final class Choices {
        final List<Weighted> entries = new ArrayList<>();

        Choices add(OldGen gen, int weight) {
            this.entries.add(new Weighted(w -> gen, weight));
            return this;
        }

        Choices add(Function<OldWorld, OldGen> gen, int weight) {
            this.entries.add(new Weighted(gen, weight));
            return this;
        }

        /** WeightedRandomChooser.getRandomItem: one draw over the total weight. */
        OldGen pick(OldWorld world, Random random) {
            int total = 0;
            for (Weighted entry : this.entries) {
                total += entry.weight;
            }
            int choice = random.nextInt(total);
            for (Weighted entry : this.entries) {
                choice -= entry.weight;
                if (choice < 0) {
                    return entry.gen.apply(world);
                }
            }
            throw new IllegalStateException();
        }
    }

    // BiomeManagerImpl: one instance each for the whole session
    private static final OldGen ACACIA = new EbxlTreeGens.Acacia();
    private static final OldGen ALT_TAIGA = new OldTreeGens.Spruce();
    private static final OldGen BIG_FIR = new EbxlTreeGens.FirHuge();
    private static final Function<OldWorld, OldGen> BIG_OAK = OldWorld::bigTree;
    private static final OldGen BIRCH = new OldTreeGens.Birch();
    private static final OldGen CUSTOM_SWAMP = new EbxlTreeGens.CustomSwamp();
    private static final OldGen FERN = new OldPlantGens.TallGrass(2);
    private static final OldGen FIR = new EbxlTreeGens.Fir();
    private static final OldGen GRASS = new OldPlantGens.TallGrass(1);
    private static final OldGen OAK = new OldTreeGens.Trees();
    private static final OldGen REDWOOD = new EbxlTreeGens.Redwood();
    private static final OldGen SHRUB = new OldTreeGens.Shrub(3, 0);
    private static final OldGen SWAMP = new OldTreeGens.Swamp();
    private static final OldGen TAIGA = new OldTreeGens.Pine();

    /** The two jungle trees of the Extreme Jungle, built with a height drawn from the decorator's random. */
    private static final class Jungle extends OldGen {
        private final boolean huge;

        Jungle(boolean huge) {
            this.huge = huge;
        }

        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            OldGen gen = this.huge ? new OldTreeGens.Huge(10 + rand.nextInt(20), 3, 3) : new OldTreeGens.Trees(4 + rand.nextInt(7), 3, 3, true);
            return gen.generate(world, rand, x, y, z);
        }
    }

    /** The shrubland's shrub with leaves of a random kind. */
    private static final OldGen RANDOM_SHRUB = new OldGen() {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            return new OldTreeGens.Shrub(3, rand.nextInt(3)).generate(world, rand, x, y, z);
        }
    };

    private static final OldGen NOTHING = new OldGen() {
        @Override
        public boolean generate(OldWorld world, Random rand, int x, int y, int z) {
            return false;
        }
    };

    /** Big autumn trees keep state between trees like the big oak: a fresh one per chunk and colour. */
    private static Function<OldWorld, OldGen> bigAutumn(int meta) {
        return world -> world.chunkState("ebxl:big_autumn_" + meta, () -> new EbxlTreeGens.BigAutumn(meta));
    }

    private static OldBiome biome(int id) {
        return OldBiomes.byId(id);
    }

    /** Blocks the generators place, described for the 1.5.2 world. */
    private static void registerBlocks() {
        OldBlockInfo.registerLeaves(AUTUMN_LEAVES);
        OldBlockInfo.registerLeaves(GREEN_LEAVES);
        OldBlockInfo.registerLog(CUSTOM_LOG);
        for (int id = QUARTER_LOG_NW; id <= QUARTER_LOG_SE; id++) {
            OldBlockInfo.registerLog(id);
        }
        OldBlockInfo.register(CATTAIL, Material.PLANTS, false, 0, false);
        OldBlockInfo.register(FLOWER, Material.PLANTS, false, 0, false);
        OldBlockInfo.register(SAPLING, Material.PLANTS, false, 0, false);
        OldBlockInfo.register(GRASS_ID, Material.VINE, false, 0, false);
        OldBlockInfo.register(LEAF_PILE, Material.VINE, false, 0, false);
        OldBlockInfo.registerCube(QUICKSAND, Material.SAND);
        OldBlockInfo.registerCube(PLANKS, Material.WOOD);
    }

    private static final int GRASS_ID = EbxlIds.GRASS;

    /** DecorationSettings of 3.13.4 (the pack's extrabiomes.cfg repeats them). */
    private static void decorator(int biomeId, Integer waterlily, Integer trees, Integer flowers, Integer grass, Integer deadBush,
            Integer mushrooms, Integer reeds, Integer cacti, Integer sand, Integer sand2, Integer clay) {
        OldDecorator d = new OldDecorator();
        if (waterlily != null) d.waterlilyPerChunk = waterlily;
        if (trees != null) d.treesPerChunk = trees;
        if (flowers != null) d.flowersPerChunk = flowers;
        if (grass != null) d.grassPerChunk = grass;
        if (deadBush != null) d.deadBushPerChunk = deadBush;
        if (mushrooms != null) d.mushroomsPerChunk = mushrooms;
        if (reeds != null) d.reedsPerChunk = reeds;
        if (cacti != null) d.cactiPerChunk = cacti;
        // CustomBiomeDecorator.Builder took both sand counts only when both were set
        if (sand != null && sand2 != null) {
            d.sandPerChunk = sand;
            d.sandPerChunk2 = sand2;
        }
        if (clay != null) d.clayPerChunk = clay;
        biome(biomeId).decorator = d;
    }

    private static void decorators() {
        //              id  lily  trees flowers grass dead  mush  reeds cacti sand  sand2 clay
        decorator(32, null, 7, 0, 0, null, null, null, null, null, null, null);            // alpine
        decorator(33, null, 9, null, 6, null, 3, null, null, null, null, null);            // autumn woods
        decorator(34, null, 7, null, 1, null, null, null, null, null, null, null);         // birch forest
        decorator(35, null, 50, 4, 25, null, null, null, null, null, null, null);          // extreme jungle
        decorator(36, null, 7, 1, 5, null, null, null, null, null, null, null);            // forested hills
        decorator(37, null, 7, null, 3, null, null, null, null, null, null, null);         // forested island
        // 38 glacier: no decorator of its own, the vanilla BiomeDecorator
        decorator(39, null, 1, null, null, null, null, null, null, null, null, null);      // green hills
        decorator(40, 4, 4, 0, null, 0, 8, 10, null, 0, 0, 1);                             // green swamplands
        decorator(41, null, 0, null, null, null, null, null, null, null, null, null);      // ice wasteland
        decorator(42, null, 0, null, 999, null, null, 10, null, null, null, null);         // marsh
        decorator(43, null, 0, 9, 12, null, null, null, null, null, null, null);           // meadow
        decorator(44, 12, 15, 5, null, null, 2, 70, null, null, null, 3);                  // mini jungle
        decorator(45, null, 0, null, null, 2, null, 50, 10, null, null, null);             // mountainous desert
        decorator(46, null, 0, null, 12, null, null, null, null, null, null, null);        // mountain ridge
        decorator(47, null, 10, null, 1, null, null, null, null, null, null, null);        // mountain taiga
        decorator(48, null, 10, null, 1, null, null, null, null, null, null, null);        // pine forest
        decorator(49, null, 7, 2, 4, null, null, null, null, null, null, null);            // rainforest
        decorator(50, null, 17, null, null, null, null, null, null, null, null, null);     // redwood forest
        decorator(51, null, 17, null, null, null, null, null, null, null, null, null);     // lush redwoods
        decorator(52, null, 0, 1, 17, null, null, null, null, null, null, null);           // savanna
        decorator(53, null, 0, 3, 1, null, null, null, null, null, null, null);            // shrubland
        decorator(54, null, 8, 1, 4, null, null, null, null, null, null, null);            // snow forest
        decorator(55, null, 17, null, 16, null, 2, null, null, null, null, null);          // snowy rainforest
        decorator(56, null, 17, null, 16, null, 2, null, null, null, null, null);          // temperate rainforest
        decorator(57, null, null, -999, -999, null, null, null, null, 0, 0, null);         // tundra
        decorator(58, null, null, null, 1, 3, null, null, null, null, null, null);         // wasteland
        decorator(59, null, 8, null, 3, null, null, null, null, null, null, null);         // woodlands
    }

    private static void trees(int biomeId, Choices choices) {
        biome(biomeId).trees = choices::pick;
    }

    private static Choices defaultTrees() {
        return new Choices().add(OAK, 90).add(BIG_OAK, 10);
    }

    private static Choices taigaTrees() {
        return new Choices().add(TAIGA, 50).add(ALT_TAIGA, 100);
    }

    private static Choices temperateRainforestTrees() {
        return new Choices().add(BIG_FIR, 200).add(FIR, 100);
    }

    /** BiomeManagerImpl.buildWeightedBiomeTreeList. */
    private static void treeChoices() {
        trees(32, new Choices().add(FIR, 100));
        Choices autumn = new Choices();
        for (int meta = AUTUMN_BROWN; meta <= AUTUMN_YELLOW; meta++) {
            autumn.add(new EbxlTreeGens.Autumn(meta), 90).add(bigAutumn(meta), 10);
        }
        trees(33, autumn.add(OAK, 90).add(BIG_OAK, 10));
        trees(34, new Choices().add(OAK, 99).add(BIG_OAK, 1).add(BIRCH, 9900));
        trees(35, new Choices().add(BIG_OAK, 2).add(SHRUB, 9).add(new Jungle(true), 3).add(new Jungle(false), 6));
        for (int id : new int[] {36, 37, 38, 39, 41, 42, 43, 45, 46, 54, 57, 58, 59}) {
            trees(id, defaultTrees());
        }
        trees(40, new Choices().add(SWAMP, 20).add(CUSTOM_SWAMP, 80));
        trees(44, new Choices().add(SWAMP, 100).add(OAK, 1).add(BIG_OAK, 99));
        trees(47, taigaTrees());
        trees(48, taigaTrees());
        trees(49, new Choices().add(BIRCH, 2).add(BIG_OAK, 49999).add(OAK, 149997));
        trees(50, new Choices().add(REDWOOD, 100));
        trees(51, new Choices().add(REDWOOD, 50).add(FIR, 50));
        trees(52, new Choices().add(ACACIA, 100));
        trees(53, new Choices().add(RANDOM_SHRUB, 200).add(NOTHING, 100));
        trees(55, temperateRainforestTrees());
        trees(56, temperateRainforestTrees());
    }

    /** BlockHandler.createGrass, then BiomeManagerImpl.buildWeightedBiomeGrassList. */
    private static void grassChoices() {
        for (int id = 32; id <= 59; id++) {
            Choices choices = new Choices();
            if (id == 46) {         // mountain ridge
                choices.add(new EbxlPlantGens.TallGrass(GRASS_BROWN), 100).add(new EbxlPlantGens.TallGrass(GRASS_SHORT_BROWN), 100);
            } else if (id == 58) {  // wasteland
                choices.add(new EbxlPlantGens.TallGrass(GRASS_DEAD), 90).add(new EbxlPlantGens.TallGrass(GRASS_DEAD_YELLOW), 90)
                        .add(new EbxlPlantGens.TallGrass(GRASS_DEAD_TALL), 35);
            }
            boolean ferns = id == 35 || id == 44 || id == 49 || id == 50 || id == 51 || id == 56;
            if (ferns) {
                choices.add(FERN, 25).add(GRASS, 75);
            } else {
                choices.add(GRASS, 100);
            }
            biome(id).grass = random -> choices.pick(null, random);
        }
    }

    // ---- the IWorldGenerators, in the order the mod registered them ----

    private static final OldGen CATTAIL_GEN = new EbxlPlantGens.CatTail();
    private static final OldGen AUTUMN_SHRUB_GEN = new EbxlPlantGens.Flowers(FLOWER_AUTUMN_SHRUB);
    private static final OldGen HYDRANGEA_GEN = new EbxlPlantGens.Flowers(FLOWER_HYDRANGEA);
    private static final OldGen ORANGE_GEN = new EbxlPlantGens.Flowers(FLOWER_ORANGE);
    private static final OldGen PURPLE_GEN = new EbxlPlantGens.Flowers(FLOWER_PURPLE);
    private static final OldGen ROOT_GEN = new EbxlPlantGens.Root();
    private static final OldGen TINY_CACTUS_GEN = new EbxlPlantGens.TinyCactus();
    private static final OldGen TOADSTOOL_GEN = new EbxlPlantGens.Flowers(FLOWER_TOADSTOOL);
    private static final OldGen WHITE_GEN = new EbxlPlantGens.Flowers(FLOWER_WHITE);
    private static final OldGen LEAF_PILE_GEN = new EbxlPlantGens.LeafPile();
    private static final OldGen MARSH_GRASS_GEN = new EbxlPlantGens.Marsh(true);
    private static final OldGen MARSH_DIRT_GEN = new EbxlPlantGens.Marsh(false);
    private static final OldGen OASIS_GEN = new EbxlPlantGens.Oasis();
    private static final OldGen SILVERFISH_GEN = new OldOreGens.Minable(97, 8);
    private static final OldGen VINE_GEN = new OldPlantGens.Vines();
    private static final OldGen LEGEND_OAK_GEN = new EbxlTreeGens.LegendOak();
    private static final OldGen QUICKSAND_GEN = new EbxlPlantGens.Quicksand();

    private static boolean is(OldBiome biome, int id) {
        return biome.id == id;
    }

    /** {@code n} times a generator at a random spot of the chunk, y random below 128. */
    private static void scatter(OldWorld world, Random rand, int x, int z, int n, OldGen gen) {
        for (int i = 0; i < n; i++) {
            int x1 = x + rand.nextInt(16) + 8;
            int y1 = rand.nextInt(128);
            int z1 = z + rand.nextInt(16) + 8;
            gen.generate(world, rand, x1, y1, z1);
        }
    }

    private static void catTails(Random rand, int chunkX, int chunkZ, OldWorld world) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        // the original looked the biome up at (x, x)
        if (is(world.getBiome(x, x), 40)) {
            for (int i = 0; i < 20; i++) {
                int x1 = x + rand.nextInt(16) + 8;
                int z1 = z + rand.nextInt(16) + 8;
                CATTAIL_GEN.generate(world, rand, x1, world.getHeightValue(x1, z1), z1);
            }
        }
    }

    private static void flowers(Random rand, int chunkX, int chunkZ, OldWorld world) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        OldBiome biome = world.getBiome(x, z);
        if (is(biome, 33)) {
            scatter(world, rand, x, z, 2, AUTUMN_SHRUB_GEN);
        }
        if (is(biome, 33) || is(biome, 55) || is(biome, 56) || is(biome, 57)) {
            scatter(world, rand, x, z, 2, TOADSTOOL_GEN);
        }
        if (is(biome, 39)) {
            for (int i = 0; i < 3; i++) {
                scatter(world, rand, x, z, 1, ORANGE_GEN);
                scatter(world, rand, x, z, 1, WHITE_GEN);
            }
        }
        if (is(biome, 40)) {
            scatter(world, rand, x, z, 1, HYDRANGEA_GEN);
        }
        if (is(biome, 40) || is(biome, 51)) {
            scatter(world, rand, x, z, 5, ROOT_GEN);
        }
        if (is(biome, 46)) {
            scatter(world, rand, x, z, 50, TINY_CACTUS_GEN);
            scatter(world, rand, x, z, 12, TINY_CACTUS_GEN);
        }
        if (is(biome, 52)) {
            scatter(world, rand, x, z, 15, PURPLE_GEN);
        }
    }

    private static void leafPiles(Random rand, int chunkX, int chunkZ, OldWorld world) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        OldBiome biome = world.getBiome(x, z);
        if (is(biome, 40) || is(biome, 46) || is(biome, 51) || is(biome, 59)) {
            scatter(world, rand, x, z, 2, LEAF_PILE_GEN);
        }
    }

    private static void marsh(Random rand, int chunkX, int chunkZ, OldWorld world) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        if (is(world.getBiome(x, z), 42)) {
            for (int i = 0; i < 127; i++) {
                int x1 = x + rand.nextInt(16) + 8;
                int z1 = z + rand.nextInt(16) + 8;
                MARSH_GRASS_GEN.generate(world, rand, x1, 0, z1);
            }
            for (int i = 0; i < 256; i++) {
                int x1 = x + rand.nextInt(1) + 8;
                int z1 = z + rand.nextInt(1) + 8;
                MARSH_DIRT_GEN.generate(world, rand, x1, 0, z1);
            }
        }
    }

    private static void mountainDesert(Random rand, int chunkX, int chunkZ, OldWorld world) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        if (is(world.getBiome(x, z), 45) && rand.nextInt(1000) == 0) {
            int x1 = x + rand.nextInt(16) + 8;
            int z1 = z + rand.nextInt(16) + 8;
            new OldLakeGens.DesertWell().generate(world, rand, x1, world.getHeightValue(x1, z1) + 1, z1);
        }
    }

    private static void mountainRidge(Random rand, int chunkX, int chunkZ, OldWorld world) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        if (!is(world.getBiome(x, z), 46)) {
            return;
        }
        for (int i = 0; i < 1000; i++) {
            int x1 = x + rand.nextInt(16) + 8;
            int z1 = z + rand.nextInt(16) + 8;
            OASIS_GEN.generate(world, rand, x1, world.getTopSolidOrLiquidBlock(x1, z1), z1);
        }
        int veins = 3 + rand.nextInt(6);
        for (int i = 0; i < veins; i++) {
            int x1 = x + rand.nextInt(16);
            int y1 = rand.nextInt(28) + 4;
            int z1 = z + rand.nextInt(16);
            if (world.getBlockId(x1, y1, z1) == 1) {
                world.setBlock(x1, y1, z1, 129);
            }
        }
        for (int i = 0; i < 7; i++) {
            int x1 = x + rand.nextInt(16);
            int y1 = rand.nextInt(64);
            int z1 = z + rand.nextInt(16);
            SILVERFISH_GEN.generate(world, rand, x1, y1, z1);
        }
    }

    private static void vanillaFlora(Random rand, int chunkX, int chunkZ, OldWorld world) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        OldBiome biome = world.getBiome(x, z);
        boolean extremeJungle = is(biome, 35);
        if (extremeJungle) {
            for (int i = 0; i < 50; i++) {
                int x1 = x + rand.nextInt(16) + 8;
                int z1 = z + rand.nextInt(16) + 8;
                VINE_GEN.generate(world, rand, x1, 64, z1);
            }
        }
        if ((extremeJungle || is(biome, 44) || is(biome, 56) || biome == OldBiomes.JUNGLE || biome == OldBiomes.JUNGLE_HILLS)
                && rand.nextInt(48) == 0) {
            int x1 = x + rand.nextInt(16) + 8;
            int y1 = rand.nextInt(128);
            int z1 = z + rand.nextInt(16) + 8;
            new EbxlPlantGens.Melon().generate(world, rand, x1, y1, z1);
        }
    }

    private static void legendOak(Random rand, int chunkX, int chunkZ, OldWorld world) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        int rarity = 0;
        // the original looked the biome up at (x, x)
        OldBiome biome = world.getBiome(x, x);
        if (biome == OldBiomes.FOREST || biome == OldBiomes.FOREST_HILLS || is(biome, 36) || is(biome, 37) || is(biome, 49)) {
            rarity = 100;
        }
        if (biome == OldBiomes.PLAINS || biome == OldBiomes.EXTREME_HILLS_EDGE) {
            rarity = 1000;
        }
        if (rarity != 0 && rand.nextInt(rarity) == 0) {
            int x1 = x + rand.nextInt(16) + 8;
            int z1 = z + rand.nextInt(16) + 8;
            LEGEND_OAK_GEN.generate(world, rand, x1, world.getHeightValue(x1, z1), z1);
        }
    }

    private static void quicksand(Random rand, int chunkX, int chunkZ, OldWorld world) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        if (is(world.getBiome(x, z), 44)) {
            int attempts = rand.nextInt(1) + rand.nextInt(1) + 1;
            for (int i = 0; i < attempts; i++) {
                if (rand.nextInt(1) == 0) {
                    int x1 = x + rand.nextInt(16) + 8;
                    int z1 = z + rand.nextInt(16) + 8;
                    QUICKSAND_GEN.generate(world, rand, x1, world.getHeightValue(x1, z1) + 1, z1);
                }
            }
        }
    }

    /** FML mod id of the original, for the generator order ({@code OldTerrainGen.LOAD_ORDER}). */
    public static final String FML_ID = "ExtrabiomesXL";

    /** Decoration and generators of all ExtrabiomesXL biomes. Called once, after the biomes are registered. */
    public static void register() {
        registerBlocks();
        decorators();
        treeChoices();
        grassChoices();
        // BlockHandler.createBlocks (cattail, flower, leaf pile), BiomeHandler.registerWorldGenerators, then
        // the cautia module. Forge kept them in a HashSet, so their order changed from start to start.
        OldTerrainGen.registerWorldGenerator(FML_ID, "extrabiomesxl:cattails", EbxlWorldGen::catTails);
        OldTerrainGen.registerWorldGenerator(FML_ID, "extrabiomesxl:flowers", EbxlWorldGen::flowers);
        OldTerrainGen.registerWorldGenerator(FML_ID, "extrabiomesxl:leaf_piles", EbxlWorldGen::leafPiles);
        OldTerrainGen.registerWorldGenerator(FML_ID, "extrabiomesxl:marsh", EbxlWorldGen::marsh);
        OldTerrainGen.registerWorldGenerator(FML_ID, "extrabiomesxl:mountain_desert", EbxlWorldGen::mountainDesert);
        OldTerrainGen.registerWorldGenerator(FML_ID, "extrabiomesxl:mountain_ridge", EbxlWorldGen::mountainRidge);
        OldTerrainGen.registerWorldGenerator(FML_ID, "extrabiomesxl:vanilla_flora", EbxlWorldGen::vanillaFlora);
        OldTerrainGen.registerWorldGenerator(FML_ID, "extrabiomesxl:legend_oak", EbxlWorldGen::legendOak);
        OldTerrainGen.registerWorldGenerator(FML_ID, "extrabiomesxl:quicksand", EbxlWorldGen::quicksand);
    }

    // ---- saplings ----

    /** The tree a sapling grows into, as BlockCustomSapling.growTree chose it; null if it needs a 2x2 of saplings. */
    public static OldGen saplingTree(int meta, boolean huge, Random rand) {
        return switch (meta) {
            case SAPLING_BROWN, SAPLING_ORANGE, SAPLING_PURPLE, SAPLING_YELLOW ->
                    rand.nextInt(20) == 0 ? new EbxlTreeGens.BigAutumn(meta) : new EbxlTreeGens.Autumn(meta);
            case SAPLING_ACACIA -> new EbxlTreeGens.Acacia();
            case SAPLING_FIR -> huge ? new EbxlTreeGens.FirHuge() : new EbxlTreeGens.Fir();
            case SAPLING_REDWOOD -> huge ? new EbxlTreeGens.Redwood() : null;
            default -> null;
        };
    }
}
