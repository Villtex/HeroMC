package offlawn.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * The world generation of offLawn 1.5.1 (SunflowerWorldGen), without game classes so that tools/expect.sh can run
 * it. Block ids and the biome lists are those of the pack configuration (OffLawn.cfg).
 */
public final class OffLawnGen {
    /** FML mod id of the original, for the generator order ({@code OldTerrainGen.LOAD_ORDER}). */
    public static final String FML_ID = "OffLawn";

    public static final int BEANSTALK = 1281;
    public static final int LAWN = 1289;
    public static final int SUNFLOWER = 1290;

    /** general.FrequentSpawnBiomes: one patch in 10 chunks. */
    static final List<String> FREQUENT = split("Plains, Green Hills, Meadow, Shrubland, Field, Garden, Grassland, Meadow, Orchard, "
            + "Prairie, Promised Land, Shrubland, Heathland, Marsh, Moorland");
    /** general.NormalSpawnBiomes: one patch in 32 chunks. */
    static final List<String> NORMAL = split("Extreme Hills, Forest, ForestHills, Extreme Hills Edge, Autumn Woods, Birch Forest, "
            + "Forested Hills, Forested Island, Woodlands, Birch Forest, Boreal Forest, Chaparral, Grove, Highland, Jade Cliffs, "
            + "Maple Woods, Moor, Mountain, Origin Valley, Seasonal Forest, Deciduous Forest, Woodland, Prairie Forest, Foothills, Hollow");

    private OffLawnGen() {
    }

    private static List<String> split(String names) {
        return Arrays.stream(names.split(",")).map(String::trim).toList();
    }

    public static void register() {
        // Material.plants, not opaque, renderType 1; metadata 0 = lower half, 1 = upper half
        OldBlockInfo.register(SUNFLOWER, OldBlockInfo.Material.PLANTS, false, 0, false);
        OldTerrainGen.registerWorldGenerator(FML_ID, OffLawnGen::sunflowers);
    }

    /** SunflowerWorldGen.generate. */
    static void sunflowers(Random random, int chunkX, int chunkZ, OldWorld world) {
        String biome = world.getBiome(chunkX * 16, chunkZ * 16).name;
        int odds;
        if (FREQUENT.contains(biome)) {
            odds = 10;
        } else if (NORMAL.contains(biome)) {
            odds = 32;
        } else {
            return;
        }
        if (random.nextInt(odds) == 0) {
            int x = chunkX * 16 + 8 + random.nextInt(16);
            int y = random.nextInt(128);
            int z = chunkZ * 16 + 8 + random.nextInt(16);
            for (int i = 0; i < 64; i++) {
                int sx = x + random.nextInt(8) - random.nextInt(8);
                int sy = y + random.nextInt(4) - random.nextInt(4);
                int sz = z + random.nextInt(8) - random.nextInt(8);
                if (world.isAirBlock(sx, sy, sz) && world.isAirBlock(sx, sy + 1, sz) && world.getBlockId(sx, sy - 1, sz) == 2
                        && canSunflowerStay(world, sx, sy, sz)) {
                    world.setBlock(sx, sy, sz, SUNFLOWER, 0);
                    world.setBlock(sx, sy + 1, sz, SUNFLOWER, 1);
                }
            }
        }
    }

    /** SunflowerBlock.canBlockStay for the lower half (the block there is air, metadata 0). */
    static boolean canSunflowerStay(OldWorld world, int x, int y, int z) {
        if (world.skyLight(x, y, z) < 8 && !world.canBlockSeeTheSky(x, y, z)) {
            return false;
        }
        int ground = world.getBlockId(x, y - 1, z);
        return ground == 3 || ground == 2 || ground == 60 || ground == LAWN || ground == BEANSTALK;
    }
}
