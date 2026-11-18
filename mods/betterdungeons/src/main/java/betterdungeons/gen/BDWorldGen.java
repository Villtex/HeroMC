package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * WorldGeneratorNew of Better Dungeons with the values of the pack's Chocolate/config.prop: dungeonSeparation 5,
 * underworld caves and mineshafts off, jungle and swamp chunk caves on (part 13 of the port), nether mountains on
 * (but only generateNether builds them, and nothing calls it).
 *
 * <p>Every chunk whose coordinates are both multiples of the separation may get one dungeon: the dungeon types are
 * tried in turn, each whose biome list names the biome at the chunk corner draws once with its chance, the last hit
 * wins. The generator has its own random (seeded from the world seed and the chunk), so it does not touch the random
 * sequence the other generators share.
 */
public final class BDWorldGen {
    public static final String FML_ID = "BetterDungeons";
    static final int DUNGEON_SEPARATION = 5;

    private BDWorldGen() {
    }

    public static void register() {
        // the mod's spawner block: BlockContainer of Material.rock, not an opaque cube
        OldBlockInfo.register(BuilderHelper.SPAWNER_BLOCK, OldBlockInfo.Material.ROCK, false, 0, true);
        BDLoot.load();
        OldTerrainGen.registerWorldGenerator(FML_ID, (random, chunkX, chunkZ, world) -> generate(world, chunkX, chunkZ));
    }

    /** IWorldGenerator.generate. */
    static void generate(OldWorld world, int chunkX, int chunkZ) {
        Random rnd = new Random();
        rnd.setSeed(world.seed() + chunkX * 34904 + chunkZ * 34184);
        generateSurface(world, rnd, chunkX * 16, chunkZ * 16);
    }

    private static void generateSurface(OldWorld world, Random random, int i, int k) {
        if (world.dimension() == 0) {
            // underworld caves off; the jungle caves (BuilderChunkJungle, y 40 in "Jungle") and swamp caves
            // (BuilderChunkSwamp, y 40 in "Swampland") follow in part 13: they draw from this random, which the big
            // dungeon below seeds anew. The mineshafts are off: mineshafts = 0 became Integer.MAX_VALUE, doubled to
            // -2 in BuilderChunkMineshafts, so they never start.
            if (k / 16 == -500) {
                // the great wall at z = -8000 with a castle every 10 chunks: part 13 of the port
            }
        }
        if (i / 16 % DUNGEON_SEPARATION == 0 && k / 16 % DUNGEON_SEPARATION == 0) {
            generateBigDungeon(world, random, i, k, true);
        }
    }

    static void generateBigDungeon(OldWorld world, Random random, int i, int k, boolean addDungeon) {
        random.setSeed(world.seed() + i / 16 * 34904 + k / 16 * 34184);
        String biome = world.getBiome(i, k).name;
        BDDungeons.Dungeon dungeon = null;
        for (BDDungeons.Dungeon d : BDDungeons.list()) {
            for (String st : d.biomes()) {
                if (d.chance() > 0 && d.dimensionID() == world.dimension() && (biome.equals(st) || st != null && addDungeon && st.equals("*"))
                        && random.nextInt(d.chance()) == 0) {
                    dungeon = d;
                }
            }
        }
        if (dungeon != null) {
            random.setSeed(world.seed() + i / 16 * 34904 + k / 16 * 34184);
            BDDungeons.Builder builder = dungeon.builder().get();
            int idMob = dungeon.mobID();
            if (idMob == BDMobs.BASED_ON_DISTANCE) {
                idMob = BDMobs.randomIndex(random);
            }
            builder.generate(random, world, i, k, idMob);
        }
    }

    /**
     * Which dungeon type a chunk of the overworld gets (the name of its DungeonConfig file), or {@code null}; {@code biome}
     * is the name of the biome at the chunk corner. For the search in tools/expect.sh.
     */
    public static String dungeonAt(long seed, String biome, int chunkX, int chunkZ) {
        if (chunkX % DUNGEON_SEPARATION != 0 || chunkZ % DUNGEON_SEPARATION != 0) {
            return null;
        }
        Random random = new Random(seed + chunkX * 34904 + chunkZ * 34184);
        BDDungeons.Dungeon dungeon = null;
        for (BDDungeons.Dungeon d : BDDungeons.list()) {
            for (String st : d.biomes()) {
                if (d.chance() > 0 && d.dimensionID() == 0 && (biome.equals(st) || st != null && st.equals("*"))
                        && random.nextInt(d.chance()) == 0) {
                    dungeon = d;
                }
            }
        }
        return dungeon == null ? null : dungeon.name() + " (" + dungeon.type() + ")";
    }
}
