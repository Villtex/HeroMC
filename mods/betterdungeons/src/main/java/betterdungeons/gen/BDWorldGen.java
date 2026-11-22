package betterdungeons.gen;

import hexxit.worldgen152.core.BiomeLayout;
import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * WorldGeneratorNew of Better Dungeons with the values of the pack's Chocolate/config.prop: dungeonSeparation 5,
 * underworld caves and mineshafts off, jungle chunk caves ("jungleChunck") and swamp chunk caves ("swampCaves", read
 * into the field swampCave) on, nether mountains on (but only generateNether builds them, and nothing calls it).
 *
 * <p>Per chunk of the overworld: the jungle cave if the biome at the chunk corner is "Jungle" (not JungleHills),
 * the swamp cave if it is "Swampland", the great wall in the chunk row z = -500. Then every chunk whose
 * coordinates are both multiples of the separation may get one dungeon: the dungeon types are tried in turn, each
 * whose biome list names the biome at the chunk corner draws once with its chance, the last hit wins. The generator
 * has its own random (seeded from the world seed and the chunk), so it does not touch the random sequence the other
 * generators share; the chunk caves and the wall draw from it, the dungeon seeds it anew.
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
        OldTerrainGen.registerWorldGenerator(FML_ID, "betterdungeons:dungeons", (random, chunkX, chunkZ, world) -> generate(world, chunkX, chunkZ));
        OldTerrainGen.registerReach(FML_ID, 8, BDWorldGen::reach);
        // Hexxit Integrated: the dungeons and the great wall as structures, the chunk caves as a feature
        OldTerrainGen.registerStructure("betterdungeons:dungeon", (random, chunkX, chunkZ, world) ->
                generateBigDungeon(world, new Random(), chunkX * 16, chunkZ * 16, true));
        OldTerrainGen.registerStructure("betterdungeons:great_wall", (random, chunkX, chunkZ, world) -> {
            Random rnd = new Random(world.seed() + chunkX * 34904 + chunkZ * 34184);
            BuilderWall.generate(rnd, world, chunkX * 16, chunkZ * 16);
        }, (seed, chunkX, chunkZ) -> chunkZ == BuilderWall.WALL_CHUNK_Z);
        OldTerrainGen.registerFeature("betterdungeons:chunk_caves", (random, chunkX, chunkZ, world) -> {
            Random rnd = new Random(world.seed() + chunkX * 34904 + chunkZ * 34184);
            chunkCaves(world, rnd, chunkX * 16, chunkZ * 16);
        });
    }

    /**
     * The chunks the big builders write into beyond the usual margin of a populated tile (measured): the ice cave 100
     * blocks from the chunk corner towards +x/+z, the volcano from 50 blocks before the corner to its dungeon 85 blocks
     * east, the castles and ships of the wall 70 blocks north of it.
     */
    static OldTerrainGen.Reach reach(long seed, BiomeLayout layout, int chunkX, int chunkZ) {
        if (chunkZ == BuilderWall.WALL_CHUNK_Z && chunkX % 10 == 0) {
            return new OldTerrainGen.Reach(0, -5, 4, 0);
        }
        if (chunkX % DUNGEON_SEPARATION != 0 || chunkZ % DUNGEON_SEPARATION != 0) {
            return null;
        }
        BDDungeons.Dungeon dungeon = dungeonFor(seed, OldBiomes.byId(layout.chunkIds(chunkX, chunkZ)[0] & 0xFF).name, chunkX, chunkZ);
        if (dungeon == null) {
            return null;
        }
        return switch (dungeon.type()) {
            case "iceCave" -> new OldTerrainGen.Reach(0, 0, 6, 6);
            case "volcano" -> new OldTerrainGen.Reach(-4, -4, 5, 3);
            default -> null;
        };
    }

    /** IWorldGenerator.generate. */
    static void generate(OldWorld world, int chunkX, int chunkZ) {
        Random rnd = new Random();
        rnd.setSeed(world.seed() + chunkX * 34904 + chunkZ * 34184);
        generateSurface(world, rnd, chunkX * 16, chunkZ * 16);
    }

    private static void generateSurface(OldWorld world, Random random, int i, int k) {
        if (world.dimension() == 0) {
            chunkCaves(world, random, i, k);
            // BuilderChunkMineshafts runs, but does nothing: mineshafts = 0 became Integer.MAX_VALUE, doubled to -2,
            // and it starts only for a chance above 0
            if (k / 16 == BuilderWall.WALL_CHUNK_Z) {
                BuilderWall.generate(random, world, i, k);
            }
        }
        if (i / 16 % DUNGEON_SEPARATION == 0 && k / 16 % DUNGEON_SEPARATION == 0) {
            generateBigDungeon(world, random, i, k, true);
        }
    }

    /** The jungle or swamp cave of a chunk (biome at its corner); underworld caves are off (BuilderChunkCaveBottom). */
    private static void chunkCaves(OldWorld world, Random random, int i, int k) {
        OldBiome biome = world.getBiome(i, k);
        if (biome == OldBiomes.JUNGLE) {
            new BuilderChunkJungle().generate(random, world, i, 40, k);
        }
        if (biome == OldBiomes.SWAMPLAND) {
            new BuilderChunkSwamp().generate(random, world, i, 40, k);
        }
    }

    static void generateBigDungeon(OldWorld world, Random random, int i, int k, boolean addDungeon) {
        random.setSeed(world.seed() + i / 16 * 34904 + k / 16 * 34184);
        BDDungeons.Dungeon dungeon = choose(random, world.getBiome(i, k).name, world.dimension(), addDungeon);
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
        BDDungeons.Dungeon dungeon = dungeonFor(seed, biome, chunkX, chunkZ);
        return dungeon == null ? null : dungeon.name() + " (" + dungeon.type() + ")";
    }

    private static BDDungeons.Dungeon dungeonFor(long seed, String biome, int chunkX, int chunkZ) {
        return choose(new Random(seed + chunkX * 34904 + chunkZ * 34184), biome, 0, true);
    }

    /** The dungeon type of generateBigDungeon, drawn from {@code random}. */
    private static BDDungeons.Dungeon choose(Random random, String biome, int dimension, boolean addDungeon) {
        BDDungeons.Dungeon dungeon = null;
        for (BDDungeons.Dungeon d : BDDungeons.list()) {
            for (String st : d.biomes()) {
                if (d.chance() > 0 && d.dimensionID() == dimension && (biome.equals(st) || st != null && addDungeon && st.equals("*"))
                        && random.nextInt(d.chance()) == 0) {
                    dungeon = d;
                }
            }
        }
        return dungeon;
    }

    /**
     * What the great wall builds 70 blocks north of chunk {@code chunkX} ("castle", "snow castle", "ship") given the
     * biome there, or {@code null}; for the search in tools/expect.sh.
     */
    public static String wallCastleAt(OldBiome biome, int chunkX) {
        return chunkX % 10 != 0 ? null : BuilderWall.castleKind(biome);
    }

    /** Whether the chunk gets a jungle (1) or swamp (2) chunk cave or neither (0), for the search in tools/expect.sh. */
    public static int chunkCaveAt(OldBiome biomeAtCorner) {
        return biomeAtCorner == OldBiomes.JUNGLE ? 1 : biomeAtCorner == OldBiomes.SWAMPLAND ? 2 : 0;
    }
}
