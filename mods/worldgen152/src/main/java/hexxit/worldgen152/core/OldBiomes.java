package hexxit.worldgen152.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Biome table of 1.5.2 (BiomeGenBase.biomeList) plus the list the overworld layout picks land biomes from
 * (WorldType.DEFAULT). Mods that added biomes back then add them here in the same order.
 */
public final class OldBiomes {
    private static final OldBiome[] BY_ID = new OldBiome[256];
    private static final List<OldBiome> OVERWORLD = new ArrayList<>();

    public static final OldBiome OCEAN = vanilla(0, "Ocean", "ocean").height(-1.0F, 0.4F);
    public static final OldBiome PLAINS = vanilla(1, "Plains", "plains").climate(0.8F, 0.4F);
    public static final OldBiome DESERT = vanilla(2, "Desert", "desert").noRain().climate(2.0F, 0.0F).height(0.1F, 0.2F)
            .surface(OldBlocks.SAND, OldBlocks.SAND);
    public static final OldBiome EXTREME_HILLS = vanilla(3, "Extreme Hills", "extreme_hills").height(0.3F, 1.5F).climate(0.2F, 0.3F);
    public static final OldBiome FOREST = vanilla(4, "Forest", "forest").climate(0.7F, 0.8F);
    public static final OldBiome TAIGA = vanilla(5, "Taiga", "taiga").snow().climate(0.05F, 0.8F).height(0.1F, 0.4F);
    public static final OldBiome SWAMPLAND = vanilla(6, "Swampland", "swampland").height(-0.2F, 0.1F).climate(0.8F, 0.9F);
    public static final OldBiome RIVER = vanilla(7, "River", "river").height(-0.5F, 0.0F);
    // Nether and End biome: they keep their ids, but never appear in the overworld layout.
    public static final OldBiome HELL = register(new OldBiome(8, "Hell", null)).noRain().climate(2.0F, 0.0F);
    public static final OldBiome SKY = register(new OldBiome(9, "Sky", null)).noRain().surface(OldBlocks.DIRT, OldBlocks.DIRT);
    public static final OldBiome FROZEN_OCEAN = vanilla(10, "FrozenOcean", "frozen_ocean").snow().height(-1.0F, 0.5F).climate(0.0F, 0.5F);
    public static final OldBiome FROZEN_RIVER = vanilla(11, "FrozenRiver", "frozen_river").snow().height(-0.5F, 0.0F).climate(0.0F, 0.5F);
    public static final OldBiome ICE_PLAINS = vanilla(12, "Ice Plains", "ice_plains").snow().climate(0.0F, 0.5F);
    public static final OldBiome ICE_MOUNTAINS = vanilla(13, "Ice Mountains", "ice_mountains").snow().height(0.3F, 1.3F).climate(0.0F, 0.5F);
    public static final OldBiome MUSHROOM_ISLAND = vanilla(14, "MushroomIsland", "mushroom_island").climate(0.9F, 1.0F).height(0.2F, 1.0F)
            .surface(OldBlocks.MYCELIUM, OldBlocks.DIRT);
    public static final OldBiome MUSHROOM_ISLAND_SHORE = vanilla(15, "MushroomIslandShore", "mushroom_island_shore").climate(0.9F, 1.0F)
            .height(-1.0F, 0.1F).surface(OldBlocks.MYCELIUM, OldBlocks.DIRT);
    public static final OldBiome BEACH = vanilla(16, "Beach", "beach").climate(0.8F, 0.4F).height(0.0F, 0.1F)
            .surface(OldBlocks.SAND, OldBlocks.SAND);
    public static final OldBiome DESERT_HILLS = vanilla(17, "DesertHills", "desert_hills").noRain().climate(2.0F, 0.0F).height(0.3F, 0.8F)
            .surface(OldBlocks.SAND, OldBlocks.SAND);
    public static final OldBiome FOREST_HILLS = vanilla(18, "ForestHills", "forest_hills").climate(0.7F, 0.8F).height(0.3F, 0.7F);
    public static final OldBiome TAIGA_HILLS = vanilla(19, "TaigaHills", "taiga_hills").snow().climate(0.05F, 0.8F).height(0.3F, 0.8F);
    public static final OldBiome EXTREME_HILLS_EDGE = vanilla(20, "Extreme Hills Edge", "extreme_hills_edge").height(0.2F, 0.8F)
            .climate(0.2F, 0.3F);
    public static final OldBiome JUNGLE = vanilla(21, "Jungle", "jungle").climate(1.2F, 0.9F).height(0.2F, 0.4F);
    public static final OldBiome JUNGLE_HILLS = vanilla(22, "JungleHills", "jungle_hills").climate(1.2F, 0.9F).height(1.8F, 0.5F);

    static {
        // WorldType.base12Biomes, in this order.
        Collections.addAll(OVERWORLD, DESERT, FOREST, EXTREME_HILLS, SWAMPLAND, PLAINS, TAIGA, JUNGLE);
    }

    private OldBiomes() {
    }

    private static OldBiome vanilla(int id, String name, String path) {
        return register(new OldBiome(id, name, "worldgen152:" + path));
    }

    /** Claims a biome id, like constructing a BiomeGenBase did. */
    public static synchronized OldBiome register(OldBiome biome) {
        if (BY_ID[biome.id] != null) {
            throw new IllegalArgumentException("Biome id " + biome.id + " is already used by " + BY_ID[biome.id] + " when adding " + biome);
        }
        BY_ID[biome.id] = biome;
        return biome;
    }

    /** WorldType.addNewBiome: appends to the land biomes of the overworld layout (no duplicates, order kept). */
    public static synchronized void addToOverworld(OldBiome biome) {
        if (!OVERWORLD.contains(biome)) {
            OVERWORLD.add(biome);
        }
    }

    /** WorldType.removeBiome. */
    public static synchronized void removeFromOverworld(OldBiome biome) {
        OVERWORLD.remove(biome);
    }

    public static OldBiome byId(int id) {
        return BY_ID[id];
    }

    /** Snapshot of the land biome list, in layout order. */
    public static synchronized OldBiome[] overworld() {
        return OVERWORLD.toArray(new OldBiome[0]);
    }

    /** Every registered biome, ordered by id. */
    public static synchronized List<OldBiome> all() {
        List<OldBiome> out = new ArrayList<>();
        for (OldBiome biome : BY_ID) {
            if (biome != null) {
                out.add(biome);
            }
        }
        return out;
    }
}
