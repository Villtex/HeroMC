package projectzulu.gen;

import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;
import java.util.Random;

/**
 * The world generation of Project Zulu 1.0.3.8 in the 1.5.2 core ({@code ProjectZuluGenerator}, registered by
 * ProjectZulu|Core): structures (pyramid, labyrinth, cemetery, oasis), sand over desert water, aloe vera, night
 * bloom, creeper blossom and palm saplings on the surface. No game classes (tools/expect.sh compiles this). The block
 * states for the ids come from the block classes of the mod (OldBlockStates).
 */
public final class ZuluWorldGen {
    /** FML mod id of the mod that registered ProjectZuluGenerator. */
    public static final String FML_ID = "ProjectZulu|Core";

    private ZuluWorldGen() {
    }

    public static void register() {
        // TODO Teil 11, Batch C: OldBlockInfo of the generated blocks, generator below
        OldTerrainGen.registerWorldGenerator(FML_ID, ZuluWorldGen::generate);
    }

    private static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        // TODO Teil 11, Batch C: port of ProjectZuluGenerator.generate (structures, WorldGenReplaceTop, WorldGenOnSurface)
    }
}
