package sct.hexxitgear.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * HGWorldGen of Hexxit Gear 1.0.23: up to one hexbiscus per chunk, on the surface near a random spot of the
 * chunk. No game classes, so tools/expect.sh predicts it too.
 */
public final class HGWorldGen {
    /** ID.HexbiscusFlower of the pack's hexxitgear.cfg. */
    public static final int HEXBISCUS = 2011;
    /** Block.snow (the thin snow layer). */
    private static final int SNOW = 78;

    private HGWorldGen() {
    }

    /** Registers the hexbiscus block and the generator (FML mod id "hexxitgear"). */
    public static void register() {
        // BlockFlower: Material.plants, not opaque, no light dimming, not a normal cube
        OldBlockInfo.register(HEXBISCUS, OldBlockInfo.Material.PLANTS, false, 0, false);
        OldTerrainGen.registerWorldGenerator("hexxitgear", "hexxitgear:plants", HGWorldGen::generate);
    }

    /**
     * HGWorldGen.generate. The dimension blacklist of the pack is empty and the generator only runs in the
     * overworld of worldgen152 (never in a flat world), so both checks of the original always pass.
     */
    static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        int xMin = chunkX << 4;
        int zMin = chunkZ << 4;
        int startX = xMin + random.nextInt(16);
        int startZ = zMin + random.nextInt(16);
        int tries = random.nextInt(2);
        for (int i = 0; i < tries; i++) {
            int x = startX + random.nextInt(8) - random.nextInt(8);
            int z = startZ + random.nextInt(8) - random.nextInt(8);
            int y = world.getHeightValue(x, z);
            // the last draw only happens when the flower could stand there (short-circuit as in the original)
            if ((world.isAirBlock(x, y, z) || world.getBlockId(x, y, z) == SNOW)
                    && OldPlantGens.flowerCanStay(world, x, y, z)
                    && random.nextInt(50) <= 1) {
                world.setBlock(x, y, z, HEXBISCUS, 0);
            }
        }
    }
}
