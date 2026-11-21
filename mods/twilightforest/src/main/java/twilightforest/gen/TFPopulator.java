package twilightforest.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.populate.OldDecoration;
import hexxit.worldgen152.core.populate.OldDungeonGen;
import hexxit.worldgen152.core.populate.OldLakeGens;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import twilightforest.gen.structure.MapGenTFMajorFeature;

import java.util.Random;

/**
 * Population of a Twilight Forest chunk (ChunkProviderTwilightForest.populate), followed by the world generators of
 * mods (GameRegistry.generateWorld ran in every dimension; each generator checks the dimension itself).
 * The chunk provider fired no terrain events, so listeners of {@link OldTerrainGen} do not apply here.
 *
 * <p>The buildings of the major features come first and draw from the chunk's random numbers (Teil 24d; the hydra
 * lair, dark tower and knight stronghold follow with Teil 24e).
 */
public final class TFPopulator implements OldDecoration.ChunkPopulator {
    public static final int DIMENSION = 7;

    private final TFLayout layout;

    public TFPopulator(TFLayout layout) {
        this.layout = layout;
    }

    @Override
    public void populate(OldWorld world, int chunkX, int chunkZ) {
        long seed = world.seed();
        int mapX = chunkX * 16;
        int mapY = chunkZ * 16;
        OldBiome biome = world.getBiome(mapX + 16, mapY + 16);
        if (biome == TFBiomes.MAJOR_FEATURE || biome == TFBiomes.MINOR_FEATURE) {
            biome = world.getBiome(mapX + 17, mapY + 17);
        }

        Random rand = new Random(seed);
        long l1 = rand.nextLong() / 2L * 2L + 1L;
        long l2 = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed(chunkX * l1 + chunkZ * l2 ^ seed);
        boolean disableFeatures = false;
        disableFeatures |= MapGenTFMajorFeature.of(world, this.layout).generateStructuresInChunk(world, rand, chunkX, chunkZ);
        TFFeature nearFeature = TFFeature.getNearestFeature(chunkX, chunkZ, this.layout);
        world.chunkState(TFDecorator.NEAR_FEATURE, () -> nearFeature);
        disableFeatures |= !nearFeature.chunkDecorationsEnabled;
        if (!disableFeatures && rand.nextInt(4) == 0) {
            int i1 = mapX + rand.nextInt(16) + 8;
            int i2 = rand.nextInt(TFTerrain.WORLDHEIGHT);
            int i3 = mapY + rand.nextInt(16) + 8;
            new OldLakeGens.Lake(TFTerrain.WATER_STILL).generate(world, rand, i1, i2, i3);
        }

        if (!disableFeatures && rand.nextInt(32) == 0) {
            int j1 = mapX + rand.nextInt(16) + 8;
            int j2 = rand.nextInt(rand.nextInt(TFTerrain.WORLDHEIGHT - 8) + 8);
            int j3 = mapY + rand.nextInt(16) + 8;
            if (j2 < TFTerrain.SEALEVEL || rand.nextInt(10) == 0) {
                new OldLakeGens.Lake(11).generate(world, rand, j1, j2, j3);
            }
        }

        for (int k1 = 0; k1 < 8; k1++) {
            int k2 = mapX + rand.nextInt(16) + 8;
            int k3 = rand.nextInt(TFTerrain.WORLDHEIGHT);
            int l3 = mapY + rand.nextInt(16) + 8;
            new OldDungeonGen().generate(world, rand, k2, k3, l3);
        }

        biome.decoration.decorate(biome, world, rand, mapX, mapY);
        // SpawnerAnimals.performWorldGenSpawning: mobs only, after every block (Teil 24c)
        mapX += 8;
        mapY += 8;

        for (int i2 = 0; i2 < 16; i2++) {
            for (int j3 = 0; j3 < 16; j3++) {
                int j4 = world.getPrecipitationHeight(mapX + i2, mapY + j3);
                if (world.isBlockFreezable(i2 + mapX, j4 - 1, j3 + mapY)) {
                    world.setBlock(i2 + mapX, j4 - 1, j3 + mapY, TFTerrain.ICE, 0);
                }

                if (world.canSnowAt(i2 + mapX, j4, j3 + mapY)) {
                    world.setBlock(i2 + mapX, j4, j3 + mapY, 78, 0);
                }
            }
        }

        Random modRandom = new Random(seed);
        long modXMul = modRandom.nextLong() >> 3;
        long modZMul = modRandom.nextLong() >> 3;
        modRandom.setSeed(modXMul * chunkX + modZMul * chunkZ ^ seed);
        for (OldTerrainGen.WorldGenerator generator : OldTerrainGen.generators()) {
            generator.generate(modRandom, chunkX, chunkZ, world);
        }
    }
}
