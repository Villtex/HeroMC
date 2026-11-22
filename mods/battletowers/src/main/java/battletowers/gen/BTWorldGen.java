package battletowers.gen;

import hexxit.worldgen152.core.BiomeLayout;
import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * The world generation of Battle Towers 1.3.4 (WorldGenHandler) without game classes, so that tools/expect.sh can run
 * it. The pack allows the biomes of config/BattleTowers.cfg and has 15 % underground towers.
 *
 * <p>The original kept the towers of the running server in a set and built one at the first chunk of an allowed
 * biome that lay at least 850 blocks (the pack's "Minimum Distance") from all of them - which tower stood where
 * depended on the order chunks were loaded in and on when the server had last restarted. Here the world is cut into
 * cells of {@value #SPACING} by {@value #SPACING} chunks; each cell has one candidate chunk, chosen from the seed at
 * least {@value #SEPARATION} chunks from the cell's far edges, and the tower is tried there with the checks of the
 * original. Two candidates are therefore at least {@value #SEPARATION} + 1 chunks (880 blocks) apart in x or in z, so
 * the original's minimum distance of 850 blocks holds. The cell size (1,024 blocks) gives about as many towers as
 * the original had: one per square of about 1,000 blocks, which is how densely towers 850 blocks apart fill the
 * world when they are placed one by one, and nearly all tries succeed (12 of 12 with tools/expect.sh).
 */
public final class BTWorldGen {
    public static final String FML_ID = "BattleTowers";
    /** Cell edge in chunks. */
    static final int SPACING = 64;
    /** Candidates keep this many chunks off the far edges of their cell, so two towers are at least that far apart. */
    static final int SEPARATION = 54;
    /** Told of every tower built ("x y z type underground"), for tools/expect.sh; {@code null} in the game. */
    public static volatile java.util.function.Consumer<String> built;

    private BTWorldGen() {
    }

    public static void register() {
        OldTerrainGen.registerWorldGenerator(FML_ID, "battletowers:towers", BTWorldGen::generate);
    }

    /** The entity name of the golem of a tower type (AS_EntityGolem.towerID, the ordinal of its tower type). */
    public static String golemName(int towerType) {
        return "BattleTowers.Battletower Golem#" + towerType;
    }

    private static long cellSeed(long seed, int cellX, int cellZ) {
        return cellX * 341873128712L + cellZ * 132897987541L + seed + 0x4254L;
    }

    /** Whether chunk (chunkX, chunkZ) is the candidate of its cell. */
    public static boolean isCandidate(long seed, int chunkX, int chunkZ) {
        int cellX = Math.floorDiv(chunkX, SPACING);
        int cellZ = Math.floorDiv(chunkZ, SPACING);
        Random r = new Random(cellSeed(seed, cellX, cellZ));
        return chunkX == cellX * SPACING + r.nextInt(SPACING - SEPARATION) && chunkZ == cellZ * SPACING + r.nextInt(SPACING - SEPARATION);
    }

    /** Whether a tower is tried in this chunk: the candidate of its cell, in an allowed biome (looked up at block x = chunkX). */
    public static boolean triedAt(long seed, BiomeLayout layout, int chunkX, int chunkZ) {
        return isCandidate(seed, chunkX, chunkZ) && biomeAllowed(layout.biomeAt(chunkX, chunkZ));
    }

    private static boolean biomeAllowed(OldBiome target) {
        return target != OldBiomes.HELL && BTConfig.get().biomeAllowed(target.name);
    }

    private static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        // the original asked for the biome at the block coordinates chunkX, chunkZ
        if (!isCandidate(world.seed(), chunkX, chunkZ) || !biomeAllowed(world.getBiome(chunkX, chunkZ))) {
            return;
        }
        int x = chunkX * 16;
        int z = chunkZ * 16;
        int h = 50;
        while (world.getBlockId(x, ++h, z) != 0) {
        }
        int y = h - 1;
        // y is never 49 (the loop starts at 51): the original's check for it never failed
        BTTower tower = new BTTower(BTConfig.get(), new Random(world.seed() ^ chunkX * 0x5DEECE66DL ^ chunkZ * 0xB16B00B5L));
        if (tower.generate(world, random, x, y, z)) {
            java.util.function.Consumer<String> report = built;
            if (report != null) {
                report.accept(x + " " + y + " " + z + " " + tower.wasUnderground());
            }
        }
    }
}
