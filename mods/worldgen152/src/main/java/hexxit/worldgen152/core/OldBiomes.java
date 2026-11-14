package hexxit.worldgen152.core;

import hexxit.worldgen152.core.populate.OldDecorator;
import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldLakeGens;
import hexxit.worldgen152.core.populate.OldOreGens;
import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldTreeGens;

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

    static {
        // Decoration settings of the BiomeGen* classes.
        PLAINS.decorator.treesPerChunk = -999;
        PLAINS.decorator.flowersPerChunk = 4;
        PLAINS.decorator.grassPerChunk = 10;
        for (OldBiome desert : new OldBiome[] {DESERT, DESERT_HILLS}) {
            desert.decorator.treesPerChunk = -999;
            desert.decorator.deadBushPerChunk = 2;
            desert.decorator.reedsPerChunk = 50;
            desert.decorator.cactiPerChunk = 10;
            desert.afterDecoration((biome, world, random, x, z) -> {
                if (random.nextInt(1000) == 0) {
                    int bx = x + random.nextInt(16) + 8;
                    int bz = z + random.nextInt(16) + 8;
                    new OldLakeGens.DesertWell().generate(world, random, bx, world.getHeightValue(bx, bz) + 1, bz);
                }
            });
        }
        OldGen silverfish = new OldOreGens.Minable(97, 8);
        for (OldBiome hills : new OldBiome[] {EXTREME_HILLS, EXTREME_HILLS_EDGE}) {
            hills.afterDecoration((biome, world, random, x, z) -> {
                int emeralds = 3 + random.nextInt(6);
                for (int i = 0; i < emeralds; i++) {
                    int bx = x + random.nextInt(16);
                    int by = random.nextInt(28) + 4;
                    int bz = z + random.nextInt(16);
                    if (world.getBlockId(bx, by, bz) == 1) {
                        world.setBlock(bx, by, bz, 129, 0);
                    }
                }
                for (int i = 0; i < 7; i++) {
                    int bx = x + random.nextInt(16);
                    int by = random.nextInt(64);
                    int bz = z + random.nextInt(16);
                    silverfish.generate(world, random, bx, by, bz);
                }
            });
        }
        for (OldBiome forest : new OldBiome[] {FOREST, FOREST_HILLS}) {
            forest.decorator.treesPerChunk = 10;
            forest.decorator.grassPerChunk = 2;
            forest.trees = (world, random) -> {
                if (random.nextInt(5) == 0) {
                    return OldBiome.BIRCH;
                }
                return random.nextInt(10) == 0 ? world.bigTree() : OldBiome.OAK;
            };
        }
        for (OldBiome taiga : new OldBiome[] {TAIGA, TAIGA_HILLS}) {
            taiga.decorator.treesPerChunk = 10;
            taiga.decorator.grassPerChunk = 1;
            taiga.trees = (world, random) -> random.nextInt(3) == 0 ? new OldTreeGens.Pine() : new OldTreeGens.Spruce();
        }
        OldDecorator swamp = SWAMPLAND.decorator;
        swamp.treesPerChunk = 2;
        swamp.flowersPerChunk = -999;
        swamp.deadBushPerChunk = 1;
        swamp.mushroomsPerChunk = 8;
        swamp.reedsPerChunk = 10;
        swamp.clayPerChunk = 1;
        swamp.waterlilyPerChunk = 4;
        SWAMPLAND.trees = (world, random) -> OldBiome.SWAMP_OAK;
        for (OldBiome jungle : new OldBiome[] {JUNGLE, JUNGLE_HILLS}) {
            jungle.decorator.treesPerChunk = 50;
            jungle.decorator.grassPerChunk = 25;
            jungle.decorator.flowersPerChunk = 4;
            jungle.trees = (world, random) -> {
                if (random.nextInt(10) == 0) {
                    return world.bigTree();
                } else if (random.nextInt(2) == 0) {
                    return new OldTreeGens.Shrub(3, 0);
                }
                return random.nextInt(3) == 0 ? new OldTreeGens.Huge(10 + random.nextInt(20), 3, 3)
                        : new OldTreeGens.Trees(4 + random.nextInt(7), 3, 3, true);
            };
            jungle.grass = random -> random.nextInt(4) == 0 ? new OldPlantGens.TallGrass(2) : new OldPlantGens.TallGrass(1);
            jungle.afterDecoration((biome, world, random, x, z) -> {
                OldGen vines = new OldPlantGens.Vines();
                for (int i = 0; i < 50; i++) {
                    int bx = x + random.nextInt(16) + 8;
                    int bz = z + random.nextInt(16) + 8;
                    vines.generate(world, random, bx, 64, bz);
                }
            });
        }
        for (OldBiome mushroom : new OldBiome[] {MUSHROOM_ISLAND, MUSHROOM_ISLAND_SHORE}) {
            mushroom.decorator.treesPerChunk = -100;
            mushroom.decorator.flowersPerChunk = -100;
            mushroom.decorator.grassPerChunk = -100;
            mushroom.decorator.mushroomsPerChunk = 1;
            mushroom.decorator.bigMushroomsPerChunk = 1;
        }
        BEACH.decorator.treesPerChunk = -999;
        BEACH.decorator.deadBushPerChunk = 0;
        BEACH.decorator.reedsPerChunk = 0;
        BEACH.decorator.cactiPerChunk = 0;
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
