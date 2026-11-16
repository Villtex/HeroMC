package hexxit.worldgen152.core.populate;

import hexxit.worldgen152.core.nether.NetherTerrain;
import hexxit.worldgen152.core.populate.OldTerrainGen.Decorate;
import hexxit.worldgen152.core.populate.OldTerrainGen.Populate;

import java.util.Random;

/**
 * Population of one Nether chunk as in Hexxit: {@code NetheriteChunkProvider.populate} of Natura (the
 * fortress, lava springs, fire, glowstone, quartz, glowshrooms) followed by the world generators of mods
 * ({@code GameRegistry.generateWorld}, which ran in every dimension).
 *
 * <p>The original drew from the chunk provider's random generator in whatever state the last generated
 * chunk had left it, so the result depended on the order in which chunks loaded. Here a chunk is populated
 * with the state left by the terrain of the chunk at +1/+1: the chunk whose generation triggered the
 * population when chunks were generated row by row. Fortress layout and terrain do not depend on this.
 */
public final class OldNetherPopulator {
    /** Natura's small glowshroom ("Glowing Mushroom" of the pack configuration), metadata 0 to 2. */
    public static final int GLOWSHROOM = 1241;
    private static final int LAVA_MOVING = 10;
    private static final int QUARTZ_ORE = 153;

    private OldNetherPopulator() {
    }

    public static void populate(OldWorld world, int chunkX, int chunkZ) {
        world.beginChunk();
        Random hellRNG = NetherTerrain.randomAfter(chunkX + 1, chunkZ + 1);
        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.populatePre(world, hellRNG, chunkX, chunkZ);
        }
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        world.netherBridge().generateStructuresInChunk(world, hellRNG, chunkX, chunkZ);
        boolean doGen = OldTerrainGen.populate(world, hellRNG, chunkX, chunkZ, false, Populate.NETHER_LAVA);

        for (int i = 0; doGen && i < 8; i++) {
            int x = blockX + hellRNG.nextInt(16) + 8;
            int y = hellRNG.nextInt(120) + 4;
            int z = blockZ + hellRNG.nextInt(16) + 8;
            new OldNetherGens.HellLava(LAVA_MOVING, false).generate(world, hellRNG, x, y, z);
        }

        int count = hellRNG.nextInt(hellRNG.nextInt(10) + 1) + 1;
        doGen = OldTerrainGen.populate(world, hellRNG, chunkX, chunkZ, false, Populate.FIRE);

        for (int i = 0; doGen && i < count; i++) {
            int x = blockX + hellRNG.nextInt(16) + 8;
            int y = hellRNG.nextInt(120) + 4;
            int z = blockZ + hellRNG.nextInt(16) + 8;
            new OldNetherGens.Fire(NetherTerrain.TAINTED_SOIL).generate(world, hellRNG, x, y, z);
        }

        count = hellRNG.nextInt(hellRNG.nextInt(10) + 1);
        doGen = OldTerrainGen.populate(world, hellRNG, chunkX, chunkZ, false, Populate.GLOWSTONE);

        for (int i = 0; doGen && i < count; i++) {
            int x = blockX + hellRNG.nextInt(16) + 8;
            int y = hellRNG.nextInt(120) + 4;
            int z = blockZ + hellRNG.nextInt(16) + 8;
            new OldNetherGens.Glowstone().generate(world, hellRNG, x, y, z);
        }

        for (int i = 0; doGen && i < 10; i++) {
            int x = blockX + hellRNG.nextInt(16) + 8;
            int y = hellRNG.nextInt(128);
            int z = blockZ + hellRNG.nextInt(16) + 8;
            new OldNetherGens.Glowstone().generate(world, hellRNG, x, y, z);
        }

        // tainted soil lets itself be replaced where netherrack is the target (isGenMineableReplaceable)
        OldOreGens.Minable quartz = new OldOreGens.Minable(QUARTZ_ORE, 0, 13, NetherTerrain.NETHERRACK, NetherTerrain.TAINTED_SOIL);

        for (int i = 0; i < 16; i++) {
            int x = blockX + hellRNG.nextInt(16);
            int y = hellRNG.nextInt(108) + 10;
            int z = blockZ + hellRNG.nextInt(16);
            quartz.generate(world, hellRNG, x, y, z);
        }

        for (int i = 0; i < 16; i++) {
            int x = blockX + hellRNG.nextInt(16);
            int y = hellRNG.nextInt(108) + 10;
            int z = blockZ + hellRNG.nextInt(16);
            new OldNetherGens.HellLava(LAVA_MOVING, true).generate(world, hellRNG, x, y, z);
        }

        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.decoratePre(world, hellRNG, blockX, blockZ);
        }
        doGen = OldTerrainGen.decorate(world, hellRNG, blockX, blockZ, Decorate.SHROOM);
        if (doGen && hellRNG.nextInt(7) == 0) {
            int x = blockX + hellRNG.nextInt(16) + 8;
            int y = hellRNG.nextInt(128);
            int z = blockZ + hellRNG.nextInt(16) + 8;
            new OldNetherGens.Glowshroom(GLOWSHROOM, 0).generate(world, hellRNG, x, y, z);
        }

        if (doGen && hellRNG.nextInt(8) == 0) {
            int x = blockX + hellRNG.nextInt(16) + 8;
            int y = hellRNG.nextInt(128);
            int z = blockZ + hellRNG.nextInt(16) + 8;
            new OldNetherGens.Glowshroom(GLOWSHROOM, 1).generate(world, hellRNG, x, y, z);
        }

        if (doGen && hellRNG.nextInt(9) == 0) {
            int x = blockX + hellRNG.nextInt(16) + 8;
            int y = hellRNG.nextInt(128);
            int z = blockZ + hellRNG.nextInt(16) + 8;
            new OldNetherGens.Glowshroom(GLOWSHROOM, 2).generate(world, hellRNG, x, y, z);
        }

        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.decoratePost(world, hellRNG, blockX, blockZ);
        }
        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.populatePost(world, hellRNG, chunkX, chunkZ, false);
        }

        long seed = world.seed();
        Random modRandom = new Random(seed);
        long modXMul = modRandom.nextLong() >> 3;
        long modZMul = modRandom.nextLong() >> 3;
        modRandom.setSeed(modXMul * chunkX + modZMul * chunkZ ^ seed);
        for (OldTerrainGen.WorldGenerator generator : OldTerrainGen.generators()) {
            generator.generate(modRandom, chunkX, chunkZ, world);
        }
    }
}
