package meteors.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldOreGens;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * The world generation of Falling Meteors 2.10.2 (MeteorsMod.generate as IWorldGenerator), without game classes so
 * that tools/expect.sh can run it: "Chunk Generation Checks" (4) veins of "Meteor Ore Gen Size" (6) underground
 * meteor ore at y 6..21, only for ChunkProviderGenerate (the overworld; the Nether draws no random numbers). Block
 * ids of the pack's Meteors.cfg.
 */
public final class MeteorsWorldGen {
    /** FML mod id of the original (@Mod modid). */
    public static final String FML_ID = "Meteors";
    public static final int RED_METEOR_GEM_BLOCK = 1201;
    public static final int FALLEN_METEOR = 1202;
    public static final int FREZARITE = 1203;
    public static final int KREKNORITE = 1204;
    public static final int METEOR_SHIELD = 1205;
    public static final int METEOR_TIMER = 1206;
    public static final int RARE_FALLEN_METEOR = 1207;
    public static final int TORCH_LIT = 1208;
    public static final int TORCH_UNLIT = 1209;
    /** "Underground Meteor ID": BlockMeteorOre (Material.iron). */
    public static final int METEOR_ORE = 1210;
    static final int CHUNK_CHECKS = 4;
    static final int ORE_GEN_SIZE = 6;

    private MeteorsWorldGen() {
    }

    public static void register() {
        OldBlockInfo.registerCube(METEOR_ORE, OldBlockInfo.Material.IRON);
        OldTerrainGen.registerWorldGenerator(FML_ID, "meteors:meteors", MeteorsWorldGen::generate);
    }

    static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        if (world.dimension() != 0) {
            return;
        }
        int x = chunkX << 4;
        int z = chunkZ << 4;
        for (int i = 0; i < CHUNK_CHECKS; i++) {
            int randX = x + random.nextInt(16);
            int randY = random.nextInt(16) + 6;
            int randZ = z + random.nextInt(16);
            new OldOreGens.Minable(METEOR_ORE, ORE_GEN_SIZE).generate(world, random, randX, randY, randZ);
        }
    }
}
