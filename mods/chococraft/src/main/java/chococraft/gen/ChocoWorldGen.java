package chococraft.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * The world generation of ChocoCraft 2.8.8 (WorldGenGysahls, addDungeonChestHooks), without game classes so that
 * tools/expect.sh can run it. Ids of the pack's chococraft.cfg.
 */
public final class ChocoWorldGen {
    /** FML mod id of the original, the last mod of {@code OldTerrainGen.LOAD_ORDER}. */
    public static final String FML_ID = "chococraft";
    public static final int GYSAHL_GREEN = 1101;
    public static final int GYSAHL_STEM = 1102;
    public static final int STRAW = 1103;
    /** chocopediaItem.id 5487 + 256. */
    public static final int CHOCOPEDIA = 5487 + 256;
    /** gysahlGreenWorldGenerationRate of chocobo_config.txt: per mille of the chunks. */
    static final int GYSAHL_WORLD_GEN_RATE = 160;

    private ChocoWorldGen() {
    }

    public static void register() {
        // BlockFlower: Material.plants, not opaque, crossed squares (renderType 1)
        OldBlockInfo.register(GYSAHL_GREEN, OldBlockInfo.Material.PLANTS, false, 0, false);
        OldBlockInfo.register(GYSAHL_STEM, OldBlockInfo.Material.PLANTS, false, 0, false);
        OldBlockInfo.register(STRAW, OldBlockInfo.Material.CIRCUITS, false, 0, false);
        OldTerrainGen.registerWorldGenerator(FML_ID, "chococraft:gysahls", ChocoWorldGen::gysahls);
        // chocopediaInDungeons (true): weight 2, one Chocopedia
        OldLoot.Category[] categories = {OldLoot.DUNGEON_CHEST, OldLoot.MINESHAFT_CORRIDOR, OldLoot.PYRAMID_DESERT_CHEST,
                OldLoot.PYRAMID_JUNGLE_CHEST, OldLoot.STRONGHOLD_CORRIDOR, OldLoot.STRONGHOLD_CROSSING, OldLoot.STRONGHOLD_LIBRARY};
        for (OldLoot.Category category : categories) {
            category.add(FML_ID, new OldStack(CHOCOPEDIA, 0, 1), 1, 1, 2);
        }
    }

    /**
     * WorldGenGysahls.generate: in the overworld 161 of 1000 chunks get a WorldGenFlowers patch of gysahl greens. The
     * original asked the height at (x, x) instead of (x, z): the patch starts at the surface height of another column.
     */
    static void gysahls(Random random, int chunkX, int chunkZ, OldWorld world) {
        if (world.dimension() != 0 || random.nextInt(1000) > GYSAHL_WORLD_GEN_RATE) {
            return;
        }
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = world.getTopSolidOrLiquidBlock(x, x);
        new OldPlantGens.Flowers(GYSAHL_GREEN).generate(world, random, x, y, z);
    }
}
