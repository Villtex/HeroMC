package twilightforest.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.populate.OldDecorator;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldLakeGens;
import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * TFBiomeDecorator: before the vanilla decoration a hollow tree (1 in 24), one of the small features (1 in 6),
 * the canopy trees and mangroves; after it roots, cave water and torchberries underground, extra lakes, lava pools
 * and mycelium. Near a feature whose chunks are not decorated only the underground and the ores remain.
 */
public class TFDecorator extends OldDecorator {
    /** Key of the feature nearest to the chunk being populated in {@link OldWorld#chunkState}, set by TFPopulator. */
    static final Object NEAR_FEATURE = new Object();

    private static final OldGen CANOPY = new TFTrees.Canopy();
    private static final OldGen CANOPY_MUSHROOM = new TFTrees.CanopyMushroom();
    private static final OldGen HUGE_CANOPY = new TFTrees.HugeCanopy();
    private static final OldGen HOLLOW_TREE = new TFTrees.Hollow();
    private static final OldGen MANGROVE = new TFTrees.Mangrove(true);
    private static final OldGen MYCELIUM_BLOB = new TFPlants.MyceliumBlob(5);
    private static final OldGen EXTRA_LAKE = new OldLakeGens.Lake(9);
    private static final OldGen EXTRA_LAVA_POOL = new OldLakeGens.Lake(11);
    private static final OldGen PLANT_ROOTS = new TFPlants.PlantRoots();
    private static final OldGen WOOD_ROOTS = new TFPlants.WoodRoots();
    private static final OldGen CAVE_WATER = new OldLakeGens.Spring(8);
    private static final OldGen HANG_BERRIES = new TFPlants.HangBerries();

    /** ruinList with the weights of WeightedRandom. */
    private static final OldGen[] RUINS = {new TFRuins.StoneCircle(), new TFRuins.Well(), new TFRuins.WitchHut(), new TFRuins.OutsideStalagmite(),
            new TFRuins.Foundation(), new TFRuins.Monolith(), new TFRuins.GroveRuins(), new TFTrees.HollowStump(), new TFRuins.FallenHollowLog(),
            new TFRuins.FallenSmallLog()};
    private static final int[] RUIN_WEIGHTS = {10, 10, 5, 12, 10, 10, 5, 12, 10, 10};

    public int canopyPerChunk = 1;
    public float chanceCanopyIsMushroom = 0.0F;
    public int myceliumPerChunk = 0;
    public int mangrovesPerChunk = 0;
    public int lakesPerChunk = 0;
    public float lavaPoolChance = 0.0F;

    /** The feature nearest to the chunk (TFFeature.getNearestFeature), as TFPopulator found it. */
    static TFFeature nearFeature(OldWorld world) {
        return world.chunkState(NEAR_FEATURE, () -> TFFeature.NOTHING);
    }

    @Override
    public void decorate(OldWorld world, Random rand, OldBiome biome, int mapX, int mapZ) {
        if (!nearFeature(world).chunkDecorationsEnabled) {
            this.decorateUnderground(world, rand, mapX, mapZ);
            this.generateOres(world, rand, mapX, mapZ);
            return;
        }
        if (rand.nextInt(24) == 0) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            HOLLOW_TREE.generate(world, rand, rx, world.getHeightValue(rx, rz), rz);
        }
        if (rand.nextInt(6) == 0) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            int ry = world.getHeightValue(rx, rz);
            randomFeature(rand).generate(world, rand, rx, ry, rz);
        }
        int nc = this.canopyPerChunk + rand.nextInt(2);
        for (int i = 0; i < nc; i++) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            int ry = world.getHeightValue(rx, rz);
            if (this.chanceCanopyIsMushroom > 0.0F && rand.nextFloat() <= this.chanceCanopyIsMushroom) {
                CANOPY_MUSHROOM.generate(world, rand, rx, ry, rz);
            } else if (rand.nextInt(24) > 0) {
                CANOPY.generate(world, rand, rx, ry, rz);
            } else {
                HUGE_CANOPY.generate(world, rand, rx, ry, rz);
            }
        }
        for (int i = 0; i < this.mangrovesPerChunk; i++) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            MANGROVE.generate(world, rand, rx, world.getHeightValue(rx, rz), rz);
        }
        super.decorate(world, rand, biome, mapX, mapZ);
        // TFBiomeDecorator.decorate(): after the vanilla decoration (and its DecorateBiomeEvent.Post)
        this.decorateUnderground(world, rand, mapX, mapZ);
        for (int i = 0; i < this.lakesPerChunk; i++) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            EXTRA_LAKE.generate(world, rand, rx, world.getHeightValue(rx, rz), rz);
        }
        if (rand.nextFloat() <= this.lavaPoolChance) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            EXTRA_LAVA_POOL.generate(world, rand, rx, world.getHeightValue(rx, rz), rz);
        }
        for (int i = 0; i < this.myceliumPerChunk; i++) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            MYCELIUM_BLOB.generate(world, rand, rx, world.getHeightValue(rx, rz), rz);
        }
    }

    protected void decorateUnderground(OldWorld world, Random rand, int mapX, int mapZ) {
        for (int i = 0; i < 12; i++) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            PLANT_ROOTS.generate(world, rand, rx, 64, rz);
        }
        for (int i = 0; i < 20; i++) {
            int rx = mapX + rand.nextInt(16) + 8;
            int ry = rand.nextInt(64);
            int rz = mapZ + rand.nextInt(16) + 8;
            WOOD_ROOTS.generate(world, rand, rx, ry, rz);
        }
        for (int i = 0; i < 50; i++) {
            int rx = mapX + rand.nextInt(16) + 8;
            int ry = rand.nextInt(24) + 4;
            int rz = mapZ + rand.nextInt(16) + 8;
            CAVE_WATER.generate(world, rand, rx, ry, rz);
        }
        for (int i = 0; i < 3; i++) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            HANG_BERRIES.generate(world, rand, rx, 64, rz);
        }
    }

    private static OldGen randomFeature(Random rand) {
        int total = 0;
        for (int w : RUIN_WEIGHTS) {
            total += w;
        }
        int value = rand.nextInt(total);
        for (int i = 0; i < RUINS.length; i++) {
            value -= RUIN_WEIGHTS[i];
            if (value < 0) {
                return RUINS[i];
            }
        }
        return RUINS[RUINS.length - 1];
    }

    /**
     * TFDarkForestBiomeDecorator: dark canopy trees, the biome's trees on the ground level, dead bushes, mushrooms and
     * mushgloom instead of the vanilla decoration, then the underground and the ores.
     */
    public static final class DarkForest extends TFDecorator {
        private static final OldGen DARK_CANOPY = new TFTrees.DarkCanopy();

        @Override
        public void decorate(OldWorld world, Random rand, OldBiome biome, int mapX, int mapZ) {
            if (nearFeature(world).chunkDecorationsEnabled) {
                int nc = this.canopyPerChunk + rand.nextInt(2);
                for (int i = 0; i < nc; i++) {
                    int rx = mapX + rand.nextInt(16) + 8;
                    int rz = mapZ + rand.nextInt(16) + 8;
                    DARK_CANOPY.generate(world, rand, rx, world.getHeightValue(rx, rz), rz);
                }
                for (int i = 0; i < this.treesPerChunk; i++) {
                    int rx = mapX + rand.nextInt(16) + 8;
                    int rz = mapZ + rand.nextInt(16) + 8;
                    int ry = groundLevel(world, rx, rz);
                    OldGen tree = biome.trees.pick(world, rand);
                    tree.setScale(1.0, 1.0, 1.0);
                    tree.generate(world, rand, rx, ry, rz);
                }
                for (int i = 0; i < this.deadBushPerChunk; i++) {
                    int rx = mapX + rand.nextInt(16) + 8;
                    int rz = mapZ + rand.nextInt(16) + 8;
                    int ry = rand.nextInt(128);
                    new OldPlantGens.DeadBush().generate(world, rand, rx, ry, rz);
                }
                for (int i = 0; i < this.mushroomsPerChunk; i++) {
                    if (rand.nextInt(8) == 0) {
                        int rx = mapX + rand.nextInt(16) + 8;
                        int rz = mapZ + rand.nextInt(16) + 8;
                        this.mushroomBrownGen.generate(world, rand, rx, groundLevel(world, rx, rz), rz);
                    }
                    if (rand.nextInt(16) == 0) {
                        int rx = mapX + rand.nextInt(16) + 8;
                        int rz = mapZ + rand.nextInt(16) + 8;
                        this.mushroomRedGen.generate(world, rand, rx, groundLevel(world, rx, rz), rz);
                    }
                    if (rand.nextInt(24) == 0) {
                        int rx = mapX + rand.nextInt(16) + 8;
                        int rz = mapZ + rand.nextInt(16) + 8;
                        new TFPlants.TallPlant(9).generate(world, rand, rx, groundLevel(world, rx, rz), rz);
                    }
                }
                if (rand.nextInt(4) == 0) {
                    int rx = mapX + rand.nextInt(16) + 8;
                    int rz = mapZ + rand.nextInt(16) + 8;
                    int ry = rand.nextInt(128);
                    this.mushroomBrownGen.generate(world, rand, rx, ry, rz);
                }
                if (rand.nextInt(8) == 0) {
                    int rx = mapX + rand.nextInt(16) + 8;
                    int rz = mapZ + rand.nextInt(16) + 8;
                    int ry = rand.nextInt(128);
                    this.mushroomRedGen.generate(world, rand, rx, ry, rz);
                }
            }
            this.decorateUnderground(world, rand, mapX, mapZ);
            this.generateOres(world, rand, mapX, mapZ);
        }

        /** getGroundLevel: one above the lowest grass from sea level up, or sea level. */
        private static int groundLevel(OldWorld world, int x, int z) {
            for (int y = TFTerrain.SEALEVEL; y < TFTerrain.WORLDHEIGHT - 1; y++) {
                if (world.getBlockId(x, y, z) == 2) {
                    return y + 1;
                }
            }
            return TFTerrain.SEALEVEL;
        }
    }
}
