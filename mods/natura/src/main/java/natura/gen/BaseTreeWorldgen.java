package natura.gen;

import hexxit.worldgen152.core.populate.OldGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BaseTreeWorldgen, the third IWorldGenerator of Natura: sakura, eucalyptus, hopseed bushes, willows, saguaros
 * and the rare trees in the overworld, the Nether trees, thornvines and glowshrooms in the Nether.
 *
 * <p>The original picked by {@code biomeName}, mostly with {@code ==}. Every biome name of 1.5.2 that the Hexxit
 * overworld knows is a string literal handed to {@code setBiomeName} (vanilla, and every ExtrabiomesXL biome class:
 * an {@code ldc} before {@code func_76735_a}), so it is the interned constant and {@code ==} with a literal is
 * true exactly when the names are equal. The comparisons are therefore written with {@code equals}. Natura's
 * names without spaces never match the ExtrabiomesXL biomes ("Autumn Woods", "Birch Forest", "Pine Forest",
 * "Temperate Rainforest", "Forested Hills", "Green Hills"); "Rainforest", "Woodlands" and "Extreme Jungle" do.
 *
 * <p>The Nether branch ("Hell" and the Nether biomes of other mods) never matches in the overworld and draws no
 * random numbers there; it stays for the Nether of Natura (part 7b).
 */
public final class BaseTreeWorldgen {
    private static final BloodTreeLargeGen GEN_BLOOD = new BloodTreeLargeGen(3, 2);
    private static final BushTreeGen BUSH = new BushTreeGen(false, 2, 3, 2);
    private static final EucalyptusTreeGenShort EUCALYPTUS_SHORT = new EucalyptusTreeGenShort(0, 1);
    private static final DarkwoodGen DARKWOOD = new DarkwoodGen(false, 3, 0);
    private static final FusewoodGen FUSEWOOD = new FusewoodGen(false, 3, 1);
    private static final SaguaroGen SAGUARO = new SaguaroGen(NaturaIds.SAGUARO, 0, false);
    private static final GlowshroomGenBlueGreen BLUE_GREEN_GLOWSHROOM = new GlowshroomGenBlueGreen(false);
    private static final GlowshroomGenPurple PURPLE_GLOWSHROOM = new GlowshroomGenPurple(false);
    private static final RareTreeGen SILVERBELL = new RareTreeGen(false, 4, 2, 1, 1);
    private static final RareTreeGen PURPLEHEART = new RareTreeGen(false, 9, 8, 2, 2);
    private static final RareTreeGen TIGER = new RareTreeGen(false, 6, 4, 3, 3);
    private static final RareTreeGen MAPLE = new RareTreeGen(false, 4, 2, 0, 0);
    private static final WillowGen WILLOW = new WillowGen(false);
    // genRedwood of the original is created but never used here: redwoods only grow from saplings.

    /** Keys of the per-chunk instances, see {@link #pinkSakura}. */
    private static final Object PINK_SAKURA = new Object();
    private static final Object WHITE_SAKURA = new Object();

    private BaseTreeWorldgen() {
    }

    /**
     * The original kept one SakuraTreeGen for the whole session. Like the big oak of 1.5.2 it keeps its height
     * limit from one tree to the next (set by the first tree, shortened by obstacles), so the trees depended on
     * the order chunks were generated in. Here one instance lives for one chunk ({@link OldWorld#chunkState}).
     */
    private static SakuraTreeGen pinkSakura(OldWorld world) {
        return world.chunkState(PINK_SAKURA, () -> new SakuraTreeGen(false, 1, 0));
    }

    private static WhiteTreeGen whiteSakura(OldWorld world) {
        return world.chunkState(WHITE_SAKURA, () -> new WhiteTreeGen(false, 2, 1));
    }

    /** IWorldGenerator.generate. */
    public static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        int xPos = chunkX * 16 + 8;
        int zPos = chunkZ * 16 + 8;
        String biomeName = world.getBiome(xPos, zPos).name;
        if (biomeName.equals("Forest")
                || biomeName.equals("AutumnWoods")
                || biomeName.equals("BirchForest")
                || biomeName.equals("PineForest")
                || biomeName.equals("Rainforest")
                || biomeName.equals("TemperateRainforest")
                || biomeName.equals("Woodlands")) {
            if (NaturaIds.GENERATE_SAKURA && random.nextInt(NaturaIds.SAKURA_SPAWN_RARITY * 5) == 0) {
                for (int iter = 0; iter < 3; iter++) {
                    int xSpawn = xPos + random.nextInt(16);
                    int ySpawn = random.nextInt(NaturaIds.SAKURA_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
                    int zSpawn = zPos + random.nextInt(16);
                    pinkSakura(world).generate(world, random, xSpawn, ySpawn, zSpawn);
                }
            }

            if (NaturaIds.GENERATE_SMALL_EUCALYPTUS && random.nextInt(NaturaIds.EUCALYPTUS_SHORT_SPAWN_RARITY) == 0) {
                int xSpawn = xPos + random.nextInt(16);
                int ySpawn = random.nextInt(NaturaIds.EUCALYPTUS_SHORT_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
                int zSpawn = zPos + random.nextInt(16);
                EUCALYPTUS_SHORT.generate(world, random, xSpawn, ySpawn, zSpawn);
            }
        }

        if (biomeName.equals("Extreme Hills") || biomeName.equals("Extreme Hills Edge") || biomeName.equals("ForestedHills")
                || biomeName.equals("GreenHills")) {
            if (NaturaIds.GENERATE_BUSH && random.nextInt(NaturaIds.BUSH_SPAWN_RARITY) == 0) {
                int xSpawn = xPos + random.nextInt(16) + 8;
                int ySpawn = random.nextInt(NaturaIds.BUSH_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
                int zSpawn = zPos + random.nextInt(16) + 8;
                BUSH.generate(world, random, xSpawn, ySpawn, zSpawn);
            }

            if (NaturaIds.GENERATE_SMALL_EUCALYPTUS && random.nextInt(NaturaIds.EUCALYPTUS_SHORT_SPAWN_RARITY) < 10) {
                int xSpawn = xPos + random.nextInt(16) + 8;
                int ySpawn = random.nextInt(NaturaIds.EUCALYPTUS_SHORT_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
                int zSpawn = zPos + random.nextInt(16) + 8;
                EUCALYPTUS_SHORT.generate(world, random, xSpawn, ySpawn, zSpawn);
            }
        }

        if (biomeName.equals("River")) {
            if (NaturaIds.GENERATE_SAKURA && random.nextInt(NaturaIds.SAKURA_SPAWN_RARITY) == 0) {
                for (int iter = 0; iter < 3; iter++) {
                    int xSpawn = xPos + random.nextInt(16);
                    int ySpawn = random.nextInt(NaturaIds.SAKURA_SPAWN_RANGE) + NaturaIds.SEA_LEVEL;
                    int zSpawn = zPos + random.nextInt(16);
                    pinkSakura(world).generate(world, random, xSpawn, ySpawn, zSpawn);
                }
            }

            if (NaturaIds.GENERATE_WILLOW && random.nextInt(NaturaIds.WILLOW_RARITY) == 0) {
                int xSpawn = xPos + random.nextInt(16);
                int zSpawn = zPos + random.nextInt(16);
                WILLOW.generate(world, random, xSpawn, NaturaIds.SEA_LEVEL + 16, zSpawn);
            }
        }

        if ((biomeName.equals("Desert") || biomeName.equals("DesertHills")) && NaturaIds.GENERATE_SAGUARO
                && random.nextInt(NaturaIds.SAGUARO_SPAWN_RARITY) == 0) {
            int xSpawn = xPos + random.nextInt(16);
            int ySpawn = random.nextInt(NaturaIds.SEA_LEVEL) + 16;
            int zSpawn = zPos + random.nextInt(16);
            SAGUARO.generate(world, random, xSpawn, ySpawn, zSpawn);
        }

        // no rarity: "Amaranth Tree Spawn Rarity" is read but not used, every jungle chunk tries one
        if ((biomeName.equals("Jungle") || biomeName.equals("JungleHills") || biomeName.equals("Extreme Jungle"))
                && NaturaIds.GENERATE_PURPLEHEART) {
            int xSpawn = xPos + random.nextInt(16);
            int zSpawn = zPos + random.nextInt(16);
            PURPLEHEART.generate(world, random, xSpawn, NaturaIds.SEA_LEVEL + 48, zSpawn);
        }

        if (biomeName.equals("Forest") || biomeName.equals("Woodlands") || biomeName.equals("AutumnWoods")) {
            if (NaturaIds.GENERATE_MAPLE && random.nextInt(NaturaIds.MAPLE_RARITY) == 0) {
                int xSpawn = xPos + random.nextInt(16);
                int zSpawn = zPos + random.nextInt(16);
                MAPLE.generate(world, random, xSpawn, NaturaIds.SEA_LEVEL + 48, zSpawn);
            }

            if (NaturaIds.GENERATE_SILVERBELL && random.nextInt(NaturaIds.SILVERBELL_RARITY) == 0) {
                int xSpawn = xPos + random.nextInt(16);
                int zSpawn = zPos + random.nextInt(16);
                SILVERBELL.generate(world, random, xSpawn, NaturaIds.SEA_LEVEL + 48, zSpawn);
            }
        }

        if ((biomeName.equals("Forest") || biomeName.equals("Rainforest") || biomeName.equals("TemperateRainforest"))
                && NaturaIds.GENERATE_TIGER
                && random.nextInt(NaturaIds.TIGER_RARITY) == 0) {
            int xSpawn = xPos + random.nextInt(16);
            int zSpawn = zPos + random.nextInt(16);
            TIGER.generate(world, random, xSpawn, NaturaIds.SEA_LEVEL + 48, zSpawn);
        }

        if ((biomeName.equals("Swampland") || biomeName.equals("ForestHills")) && NaturaIds.GENERATE_WILLOW
                && random.nextInt(NaturaIds.WILLOW_RARITY) == 0) {
            for (int i = 0; i < 3; i++) {
                int xSpawn = xPos + random.nextInt(16);
                int zSpawn = zPos + random.nextInt(16);
                WILLOW.generate(world, random, xSpawn, NaturaIds.SEA_LEVEL + 16, zSpawn);
            }
        }

        if (biomeName.equals("Hell")
                || biomeName.equals("Boneyard")
                || biomeName.equals("Phantasmagoric Inferno")
                || biomeName.equals("Corrupted Sands")) {
            nether(random, world, xPos, zPos);
        }
    }

    /** The Nether part of generate (part 7b). */
    private static void nether(Random random, OldWorld world, int xPos, int zPos) {
        if (NaturaIds.GENERATE_BLOODWOOD && random.nextInt(NaturaIds.BLOOD_SPAWN_RARITY) == 0) {
            int xSpawn = xPos + random.nextInt(16);
            int ySpawn = 72;
            int zSpawn = zPos + random.nextInt(16);
            GEN_BLOOD.generate(world, random, xSpawn, ySpawn, zSpawn);
        }

        if (NaturaIds.GENERATE_GHOST && random.nextInt(NaturaIds.GHOST_SPAWN_RARITY) == 0) {
            for (int iter = 0; iter < 3; iter++) {
                int ySpawn = random.nextInt(80) + 16;
                int xSpawn = xPos + random.nextInt(16);
                int zSpawn = zPos + random.nextInt(16);
                whiteSakura(world).generate(world, random, xSpawn, ySpawn, zSpawn);
            }
        }

        if (NaturaIds.GENERATE_DARKWOOD && random.nextInt(NaturaIds.DARK_SPAWN_RARITY) == 0) {
            int ySpawn = random.nextInt(64) + 32;
            int xSpawn = xPos + random.nextInt(16);
            int zSpawn = zPos + random.nextInt(16);
            DARKWOOD.generate(world, random, xSpawn, ySpawn, zSpawn);
        }

        if (NaturaIds.GENERATE_FUSEWOOD && random.nextInt(NaturaIds.FUSE_SPAWN_RARITY) == 0) {
            int ySpawn = random.nextInt(64) + 32;
            int xSpawn = xPos + random.nextInt(16);
            int zSpawn = zPos + random.nextInt(16);
            FUSEWOOD.generate(world, random, xSpawn, ySpawn, zSpawn);
        }

        if (NaturaIds.GENERATE_THORNVINES && random.nextInt(NaturaIds.THORN_SPAWN_RARITY) == 0) {
            int ySpawn = 108;
            for (int i = 0; i < 20; i++) {
                int vineMeta = random.nextInt(16);
                int xSpawn = xPos + random.nextInt(16);
                int zSpawn = zPos + random.nextInt(16);
                int size = random.nextInt(25) + 1;
                int height = ySpawn - (random.nextInt(size) + random.nextInt(size) + random.nextInt(size));
                for (int yHeight = ySpawn; yHeight > height; yHeight--) {
                    if (world.getBlockId(xSpawn, yHeight, zSpawn) == 0) {
                        world.setBlock(xSpawn, yHeight, zSpawn, NaturaIds.THORN_VINES, vineMeta);
                    }
                }
            }
        }

        if (random.nextInt(35) == 0) {
            for (int i = 0; i < 5; i++) {
                OldGen obj = random.nextInt(3) == 0 ? PURPLE_GLOWSHROOM : BLUE_GREEN_GLOWSHROOM;
                int xSpawn = xPos + random.nextInt(24) - 4;
                int zSpawn = zPos + random.nextInt(24) - 4;
                int ySpawn = findGround(world, xSpawn, random.nextInt(64) + 32, zSpawn);
                if (ySpawn != -1) {
                    obj.generate(world, random, xSpawn, ySpawn, zSpawn);
                }
            }
        }
    }

    static int findGround(OldWorld world, int x, int y, int z) {
        boolean foundGround = false;
        int height = y;
        do {
            int underID = world.getBlockId(x, --height, z);
            if (NaturaGen.netherSoil(underID) || height < 0) {
                foundGround = true;
            }
        } while (!foundGround);
        return height + 1;
    }
}
