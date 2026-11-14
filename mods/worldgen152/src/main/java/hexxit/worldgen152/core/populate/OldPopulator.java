package hexxit.worldgen152.core.populate;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.populate.OldTerrainGen.Populate;

import java.util.Random;

/**
 * Population of one chunk as in 1.5.2: {@code ChunkProviderGenerate.populate} (structures, lakes, dungeons,
 * the biome's decoration, snow and ice) followed by the world generators of mods ({@code GameRegistry.generateWorld}).
 * It works on the 16x16 area shifted 8 blocks into the positive x and z direction of the chunk.
 *
 * <p>Not ported here: the animals spawned with a new chunk. They used the chunk's random sequence after
 * all blocks were set, so leaving them to the game changes no block.
 */
public final class OldPopulator {
    private OldPopulator() {
    }

    public static void populate(OldWorld world, int chunkX, int chunkZ) {
        world.beginChunk();
        long seed = world.seed();
        int x = chunkX * 16;
        int z = chunkZ * 16;
        OldBiome biome = world.getBiome(x + 16, z + 16);
        Random random = new Random(seed);
        long xMul = random.nextLong() / 2L * 2L + 1L;
        long zMul = random.nextLong() / 2L * 2L + 1L;
        random.setSeed(chunkX * xMul + chunkZ * zMul ^ seed);
        boolean village = false;

        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.populatePre(world, random, chunkX, chunkZ);
        }

        if (world.structures() != null) {
            village = world.structures().generateStructuresInChunk(world, random, chunkX, chunkZ);
        }

        if (OldTerrainGen.populate(world, random, chunkX, chunkZ, village, Populate.LAKE) && !village && random.nextInt(4) == 0) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            new OldLakeGens.Lake(9).generate(world, random, bx, by, bz);
        }

        if (OldTerrainGen.populate(world, random, chunkX, chunkZ, village, Populate.LAVA) && !village && random.nextInt(8) == 0) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(random.nextInt(120) + 8);
            int bz = z + random.nextInt(16) + 8;
            if (by < 63 || random.nextInt(10) == 0) {
                new OldLakeGens.Lake(11).generate(world, random, bx, by, bz);
            }
        }

        boolean doGen = OldTerrainGen.populate(world, random, chunkX, chunkZ, village, Populate.DUNGEON);
        for (int i = 0; doGen && i < 8; i++) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            new OldDungeonGen().generate(world, random, bx, by, bz);
        }

        biome.decoration.decorate(biome, world, random, x, z);

        doGen = OldTerrainGen.populate(world, random, chunkX, chunkZ, village, Populate.ICE);
        for (int dx = 0; doGen && dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int bx = x + 8 + dx;
                int bz = z + 8 + dz;
                int by = world.getPrecipitationHeight(bx, bz);
                if (world.isBlockFreezable(bx, by - 1, bz)) {
                    world.setBlock(bx, by - 1, bz, 79, 0);
                }
                if (world.canSnowAt(bx, by, bz)) {
                    world.setBlock(bx, by, bz, 78, 0);
                }
            }
        }

        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.populatePost(world, random, chunkX, chunkZ, village);
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
