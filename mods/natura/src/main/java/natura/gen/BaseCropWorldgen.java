package natura.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BaseCropWorldgen, the first of the three IWorldGenerator of Natura: barley, cotton and bluebells, the four
 * overworld berry bushes and, in the Nether, the four Nether berry bushes. Random calls exactly as in the
 * original, with the settings of the pack's Natura.txt ({@link NaturaIds}).
 */
public final class BaseCropWorldgen {
    private static final BerryBushGen RASPGEN = new BerryBushGen(NaturaIds.BERRY_RASPBERRY, NaturaIds.SEA_LEVEL + NaturaIds.RASP_SPAWN_RANGE);
    private static final BerryBushGen BLUEGEN = new BerryBushGen(NaturaIds.BERRY_BLUEBERRY, NaturaIds.SEA_LEVEL + NaturaIds.BLUE_SPAWN_RANGE);
    private static final BerryBushGen BLACKGEN = new BerryBushGen(NaturaIds.BERRY_BLACKBERRY, NaturaIds.SEA_LEVEL + NaturaIds.BLACK_SPAWN_RANGE);
    private static final BerryBushGen MALOGEN = new BerryBushGen(NaturaIds.BERRY_MALOBERRY, NaturaIds.SEA_LEVEL + NaturaIds.GEO_SPAWN_RANGE);
    private static final NetherBerryBushGen BLIGHTGEN = new NetherBerryBushGen(NaturaIds.NETHER_BERRY_BLIGHTBERRY);
    private static final NetherBerryBushGen DUSKGEN = new NetherBerryBushGen(NaturaIds.NETHER_BERRY_DUSKBERRY);
    private static final NetherBerryBushGen SKYGEN = new NetherBerryBushGen(NaturaIds.NETHER_BERRY_SKYBERRY);
    private static final NetherBerryBushGen STINGGEN = new NetherBerryBushGen(NaturaIds.NETHER_BERRY_STINGBERRY);

    private BaseCropWorldgen() {
    }

    /**
     * {@code world.provider.isHellWorld}. The 1.5.2 world of the core is the overworld, so the Nether branch draws
     * no random numbers there; it stays for the Nether of Natura (part 7b).
     */
    static boolean isHellWorld(OldWorld world) {
        return false;
    }

    /** IWorldGenerator.generate. */
    public static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        int xChunk = chunkX * 16 + 8;
        int zChunk = chunkZ * 16 + 8;
        int xCh = chunkX * 16 + random.nextInt(16);
        int yCh = random.nextInt(128);
        int zCh = chunkZ * 16 + random.nextInt(16);
        OldBiome biome = world.getBiome(chunkX * 16, chunkZ * 16);
        if (NaturaIds.GENERATE_BARLEY && random.nextInt(5) == 0 && goodClimate(biome, 0.11F, 1.0F, 0.11F, 2.0F)) {
            generateBarley(world, random, xCh, yCh + NaturaIds.SEA_LEVEL, zCh);
            generateBarley(world, random, xCh, yCh + NaturaIds.SEA_LEVEL, zCh);
        }

        if (NaturaIds.GENERATE_COTTON && random.nextInt(12) == 0 && goodClimate(biome, 0.11F, 1.0F, 0.11F, 2.0F)) {
            generateCotton(world, random, xCh, yCh + NaturaIds.SEA_LEVEL, zCh);
            generateCotton(world, random, xCh, yCh + NaturaIds.SEA_LEVEL, zCh);
        }

        if (NaturaIds.GENERATE_BLUEBELLS && random.nextInt(12) == 0) {
            generateBluebells(world, random, xCh, yCh + NaturaIds.SEA_LEVEL, zCh);
        }

        if (NaturaIds.GENERATE_RASPBERRIES && random.nextInt(NaturaIds.RASP_SPAWN_RARITY) == 0 && goodClimate(biome, 0.6F, 2.0F, 0.2F, 0.93F)) {
            xCh = xChunk + random.nextInt(16);
            yCh = random.nextInt(NaturaIds.RASP_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
            zCh = zChunk + random.nextInt(16);
            RASPGEN.generate(world, random, xCh, yCh, zCh);
        }

        if (NaturaIds.GENERATE_BLUEBERRIES && random.nextInt(NaturaIds.BLUE_SPAWN_RARITY) == 0 && goodClimate(biome, 0.3F, 0.81F, 0.3F, 0.8F)) {
            xCh = xChunk + random.nextInt(16);
            yCh = random.nextInt(NaturaIds.BLUE_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
            zCh = zChunk + random.nextInt(16);
            BLUEGEN.generate(world, random, xCh, yCh, zCh);
        }

        if (NaturaIds.GENERATE_BLACKBERRIES && random.nextInt(NaturaIds.BLACK_SPAWN_RARITY) == 0 && goodClimate(biome, 0.5F, 5.0F, 0.6F, 3.0F)) {
            xCh = xChunk + random.nextInt(16);
            yCh = random.nextInt(NaturaIds.BLACK_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
            zCh = zChunk + random.nextInt(16);
            BLACKGEN.generate(world, random, xCh, yCh, zCh);
        }

        // biome == BiomeGenBase.swampland: the vanilla swamp only, not the swamps of ExtrabiomesXL
        if (NaturaIds.GENERATE_BLACKBERRIES && random.nextInt(NaturaIds.BLACK_SPAWN_RARITY / 3) == 0 && biome == OldBiomes.SWAMPLAND) {
            xCh = xChunk + random.nextInt(16);
            yCh = random.nextInt(NaturaIds.BLACK_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
            zCh = zChunk + random.nextInt(16);
            BLACKGEN.generate(world, random, xCh, yCh, zCh);
        }

        if (NaturaIds.GENERATE_MALOBERRIES && random.nextInt(NaturaIds.GEO_SPAWN_RARITY) == 0 && goodClimate(biome, 0.0F, 0.3F, 0.0F, 5.0F)) {
            xCh = xChunk + random.nextInt(16);
            yCh = random.nextInt(NaturaIds.GEO_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
            zCh = zChunk + random.nextInt(16);
            MALOGEN.generate(world, random, xCh, yCh, zCh);
        }

        if (isHellWorld(world)) {
            if (NaturaIds.GENERATE_BLIGHTBERRIES && random.nextInt(NaturaIds.BLIGHT_SPAWN_RARITY) == 0) {
                xCh = xChunk + random.nextInt(16);
                yCh = random.nextInt(NaturaIds.BLIGHT_SPAWN_RANGE) + 16;
                zCh = zChunk + random.nextInt(16);
                BLIGHTGEN.generate(world, random, xCh, yCh, zCh);
            }

            if (NaturaIds.GENERATE_DUSKBERRIES && random.nextInt(NaturaIds.DUSK_SPAWN_RARITY) == 0) {
                xCh = xChunk + random.nextInt(16);
                yCh = random.nextInt(NaturaIds.DUSK_SPAWN_RANGE) + 16;
                zCh = zChunk + random.nextInt(16);
                DUSKGEN.generate(world, random, xCh, yCh, zCh);
            }

            if (NaturaIds.GENERATE_SKYBERRIES && random.nextInt(NaturaIds.SKY_SPAWN_RARITY) == 0) {
                xCh = xChunk + random.nextInt(16);
                yCh = random.nextInt(NaturaIds.SKY_SPAWN_RANGE) + 16;
                zCh = zChunk + random.nextInt(16);
                SKYGEN.generate(world, random, xCh, yCh, zCh);
            }

            if (NaturaIds.GENERATE_STINGBERRIES && random.nextInt(NaturaIds.STING_SPAWN_RARITY) == 0) {
                xCh = xChunk + random.nextInt(16);
                yCh = random.nextInt(NaturaIds.STING_SPAWN_RANGE) + 16;
                zCh = zChunk + random.nextInt(16);
                STINGGEN.generate(world, random, xCh, yCh, zCh);
            }
        }
    }

    /**
     * {@code ((BlockFlower) Block.plantYellow).canBlockStay}: light 8 or open sky, on grass, dirt or farmland.
     * (The core knows only sky light; torches and lava in caves do not count.)
     */
    private static boolean flowerCanStay(OldWorld world, int x, int y, int z) {
        return OldPlantGens.flowerCanStay(world, x, y, z);
    }

    static boolean generateBarley(OldWorld world, Random random, int x, int y, int z) {
        for (int tries = 0; tries < 64; tries++) {
            int i1 = x + random.nextInt(8) - random.nextInt(8);
            int j1 = y + random.nextInt(4) - random.nextInt(4);
            int k1 = z + random.nextInt(8) - random.nextInt(8);
            if (world.isAirBlock(i1, j1, k1) && flowerCanStay(world, i1, j1, k1)) {
                world.setBlock(i1, j1, k1, NaturaIds.CROPS, NaturaIds.CROPS_BARLEY_GROWN);
            }
        }
        return true;
    }

    static boolean generateCotton(OldWorld world, Random random, int x, int y, int z) {
        for (int tries = 0; tries < 64; tries++) {
            int i1 = x + random.nextInt(8) - random.nextInt(8);
            int j1 = y + random.nextInt(4) - random.nextInt(4);
            int k1 = z + random.nextInt(8) - random.nextInt(8);
            if (world.isAirBlock(i1, j1, k1) && flowerCanStay(world, i1, j1, k1)) {
                world.setBlock(i1, j1, k1, NaturaIds.CROPS, NaturaIds.CROPS_COTTON_GROWN);
            }
        }
        return true;
    }

    static boolean generateBluebells(OldWorld world, Random random, int x, int y, int z) {
        for (int tries = 0; tries < 40; tries++) {
            int i1 = x + random.nextInt(8) - random.nextInt(8);
            int j1 = y + random.nextInt(8) - random.nextInt(8);
            int k1 = z + random.nextInt(8) - random.nextInt(8);
            if (world.isAirBlock(i1, j1, k1) && flowerCanStay(world, i1, j1, k1)) {
                world.setBlock(i1, j1, k1, NaturaIds.BLUEBELLS, 0);
            }
        }
        return true;
    }

    static boolean goodClimate(OldBiome biome, float minTemp, float maxTemp, float minRain, float maxRain) {
        float temp = biome.temperature;
        float rain = biome.rainfall;
        return minTemp <= temp && temp <= maxTemp && minRain <= rain && rain <= maxRain;
    }
}
