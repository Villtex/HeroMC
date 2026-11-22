package qcraft.gen;

import cofh.gen.CoFHWorldGen;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldOreGens;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * The world generation of qCraft 1.01 in the 1.5.2 core, without game classes so that tools/expect.sh can run it.
 *
 * <p>Two sources of quantum ore (block id 2373 of the pack's QuantumCraft.cfg):
 * <ul>
 * <li>{@code QuantumOreGenerator} (IWorldGenerator of "QuantumCraft"): 4 veins of 5 blocks below y 24 in every world
 * that is not the Nether. Registered only with {@code enableWorldGen}, which the pack's config turns off; the port
 * registers it always and leaves it before the first random draw while the setting is off ({@link #enableWorldGen}),
 * which is the same for the shared random sequence.</li>
 * <li>The template {@code QOre} of the pack's {@code config/cofh/WorldCustomGen.txt} ({@code UNIFORM QOre 2373 0 5 4 1
 * 24 true}): CoFH World generates it whenever the block exists, so the ore does appear in the pack.</li>
 * </ul>
 */
public final class QCraftWorldGen {
    /** FML mod id of the original, for the generator order ({@code OldTerrainGen.LOAD_ORDER}). */
    public static final String FML_ID = "QuantumCraft";
    /** quantumOreBlockID and quantumOreGlowingBlockID of the pack. */
    public static final int QUANTUM_ORE = 2373;
    public static final int QUANTUM_ORE_GLOWING = 2374;

    private static final int STONE = 1;

    /** {@code enableWorldGen} of QuantumCraft.cfg (pack: false); set from the mod config before worlds load. */
    public static volatile boolean enableWorldGen = false;

    private static boolean registered;

    private QCraftWorldGen() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        // BlockQuantumOre: Material.rock cubes
        OldBlockInfo.registerCube(QUANTUM_ORE, OldBlockInfo.Material.ROCK);
        OldBlockInfo.registerCube(QUANTUM_ORE_GLOWING, OldBlockInfo.Material.ROCK);
        // CoFH World's vanilla features come first; register() is a no-op when CoFH Core already did it
        CoFHWorldGen.register();
        CoFHWorldGen.addCustomFeature("QOre", QUANTUM_ORE, 0, 5, 4, 1, 24, false);
        OldTerrainGen.registerWorldGenerator(FML_ID, "quantumcraft:world", QCraftWorldGen::generate);
    }

    /** QuantumOreGenerator.generate: not in the Nether (a flat world never reaches the 1.5.2 generator). */
    static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        if (!enableWorldGen || world.isHellWorld()) {
            return;
        }
        OldOreGens.Minable oreGen = new OldOreGens.Minable(QUANTUM_ORE, 0, 5, STONE);
        int x0 = chunkX * 16;
        int z0 = chunkZ * 16;
        for (int k = 0; k < 4; k++) {
            int x = x0 + random.nextInt(16);
            int y = random.nextInt(24);
            int z = z0 + random.nextInt(16);
            oreGen.generate(world, random, x, y, z);
        }
    }
}
