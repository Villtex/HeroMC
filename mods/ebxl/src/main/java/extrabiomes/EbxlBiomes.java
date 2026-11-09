package extrabiomes;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.OldBlocks;

/**
 * The ExtrabiomesXL biomes as the 1.5.2 terrain generator saw them: ids, height ranges, climate and surface
 * blocks, with the ids and settings of the Hexxit pack configuration (all biomes enabled, ids 32-59).
 */
public final class EbxlBiomes {
    // Terrain block ids from the pack's extrabiomes.cfg; both had to stay below 256 for the generator.
    static final int RED_ROCK_ID = 254;
    static final int CRACKED_SAND_ID = 255;

    private EbxlBiomes() {
    }

    private static OldBiome biome(int id, String name, String path) {
        return OldBiomes.register(new OldBiome(id, name, Extrabiomes.MOD_ID + ":" + path));
    }

    /** Adds the biomes to the 1.5.2 biome table and to the overworld layout. Uses no game classes. */
    public static void register() {
        float forestTemp = OldBiomes.FOREST.temperature;
        float forestRain = OldBiomes.FOREST.rainfall;
        float swampTemp = OldBiomes.SWAMPLAND.temperature;
        float swampRain = OldBiomes.SWAMPLAND.rainfall;
        float jungleTemp = OldBiomes.JUNGLE.temperature;
        float jungleRain = OldBiomes.JUNGLE.rainfall;
        float desertTemp = OldBiomes.DESERT.temperature;
        float desertRain = OldBiomes.DESERT.rainfall;
        float taigaHillsTemp = OldBiomes.TAIGA_HILLS.temperature;
        float taigaHillsRain = OldBiomes.TAIGA_HILLS.rainfall;

        // Declaration order = BiomeSettings order of the original = order in the land biome list.
        OldBiome[] biomes = {
                biome(32, "Alpine", "alpine").surface(OldBlocks.STONE, OldBlocks.STONE).snow().climate(0.0F, 0.1F).height(1.3F, 2.1F),
                biome(33, "Autumn Woods", "autumn_woods").climate(forestTemp, forestRain).height(0.2F, 0.8F),
                biome(34, "Birch Forest", "birch_forest").climate(0.4F, 0.7F).height(0.0F, 0.4F),
                biome(35, "Extreme Jungle", "extreme_jungle").climate(jungleTemp, jungleRain).height(2.1F, 2.3F),
                biome(36, "Forested Hills", "forested_hills").climate(forestTemp - 0.1F, forestRain).height(0.2F, 1.8F),
                biome(37, "Forested Island", "forested_island").climate(forestTemp + 0.1F, forestRain).height(-0.8F, 0.8F),
                biome(38, "Glacier", "glacier").surface(OldBlocks.SNOW_BLOCK, OldBlocks.ICE).snow().climate(0.0F, 0.0F).height(1.4F, 2.1F),
                biome(39, "Green Hills", "green_hills").climate(forestTemp - 0.1F, forestRain + 0.1F).height(0.6F, 1.2F),
                biome(40, "Green Swamplands", "green_swamp").climate(swampTemp - 0.1F, swampRain).height(-0.2F, 0.1F),
                biome(41, "Ice Wasteland", "ice_wasteland").surface(OldBlocks.SNOW_BLOCK, OldBlocks.SNOW_BLOCK).snow().climate(0.0F, 0.1F)
                        .height(0.3F, 0.4F),
                biome(42, "Marsh", "marsh").climate(swampTemp, swampRain).height(-0.4F, 0.0F),
                biome(43, "Meadow", "meadow").height(0.0F, 0.0F),
                biome(44, "Mini Jungle", "mini_jungle").climate(jungleTemp, jungleRain).height(0.2F, 0.6F),
                biome(45, "Mountainous Desert", "mountain_desert").climate(OldBiomes.DESERT_HILLS.temperature, OldBiomes.DESERT_HILLS.rainfall)
                        .height(0.4F, 1.4F).surface(OldBlocks.SAND, OldBlocks.SAND).noRain(),
                biome(46, "Mountain Ridge", "mountain_ridge").climate(desertTemp, desertRain).height(1.7F, 1.7F).noRain()
                        .surface((byte) RED_ROCK_ID, (byte) RED_ROCK_ID),
                biome(47, "Mountain Taiga", "mountain_taiga").climate(0.0F, taigaHillsRain).height(0.3F, 1.2F),
                biome(48, "Pine Forest", "pine_forest").climate(forestTemp, forestRain).height(0.1F, 0.3F),
                biome(49, "Rainforest", "rainforest").climate(1.1F, 1.4F).height(0.4F, 1.3F),
                biome(50, "Redwood Forest", "redwood_forest").climate(1.1F, 1.4F).height(0.9F, 1.5F),
                biome(51, "Lush Redwoods", "redwood_lush").climate(1.1F, 1.4F).height(0.9F, 1.5F),
                biome(52, "Savanna", "savanna").climate(desertTemp, desertRain).height(0.0F, 0.1F),
                biome(53, "Shrubland", "shrubland").climate(0.4F, 0.6F).height(0.1F, 0.3F),
                biome(54, "Snow Forest", "snowy_forest").climate(taigaHillsTemp, taigaHillsRain).height(0.1F, 0.5F).snow(),
                biome(55, "Snowy Rainforest", "snowy_rainforest").climate(taigaHillsTemp, 1.3F).height(0.4F, 1.5F).snow(),
                biome(56, "Temperate Rainforest", "temperate_rainforest").climate(0.6F, 0.9F).height(0.4F, 1.5F),
                biome(57, "Tundra", "tundra").climate(0.0F, 0.0F).height(0.0F, 0.2F),
                biome(58, "Wasteland", "wasteland").climate(desertTemp, desertRain).height(0.0F, 0.0F).noRain()
                        .surface((byte) CRACKED_SAND_ID, (byte) CRACKED_SAND_ID),
                biome(59, "Woodlands", "woodlands").climate(forestTemp, forestRain).height(0.2F, 0.4F),
        };
        for (OldBiome biome : biomes) {
            OldBiomes.addToOverworld(biome);
        }
    }
}
