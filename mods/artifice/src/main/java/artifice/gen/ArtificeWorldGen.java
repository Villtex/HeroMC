package artifice.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldBlockInfo.Material;
import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The world generation of Artifice 1.1.2 ({@code ArtificeWorldGen} and its {@code WorldGen*} classes) in the 1.5.2
 * core, without game classes so that tools/expect.sh can run it.
 *
 * <p>Settings of the Hexxit pack ({@code config/Artifice.cfg}): flowers (frequency 1), lotus lilies (frequency 1),
 * the basalt layer (y -2..7) and marble clusters (4 per chunk, size 400, below y 128) are on; marble layer, basalt
 * clusters and both cave generators are off. Only {@code ArtificeCompat|Vanilla} registers anything (the other
 * compat modules need mods that are not in the pack): it blacklists the Nether and the End, makes stone the only
 * stone type, keeps flowers out of Swampland and the mushroom islands and puts lotus lilies only into Swampland.
 */
public final class ArtificeWorldGen {
    /** FML mod id of the original, for the generator order ({@code OldTerrainGen.LOAD_ORDER}). */
    public static final String FML_ID = "Artifice";

    private static final int STONE = 1;
    private static final int SNOW = 78;
    /** {@code ArtificeRegistry.getStoneTypes()}: only {@code Block.stone} (from ArtificeCompat|Vanilla). */
    private static final Set<Integer> STONE_TYPES = Set.of(STONE);
    private static final Set<String> FLORA_BLACKLIST = Set.of("Swampland", "MushroomIsland", "MushroomIslandShore");
    private static final Set<String> LOTUS_WHITELIST = Set.of("Swampland");

    private static final int FLORA_FREQUENCY = 1;
    private static final int LOTUS_FREQUENCY = 1;
    private static final int BASALT_LAYER_MIN = -2;
    private static final int BASALT_LAYER_MAX = 7;
    private static final int MARBLE_CLUSTER_FREQUENCY = 4;
    private static final int MARBLE_CLUSTER_SIZE = 400;
    private static final int MARBLE_CLUSTER_HEIGHT = 128;

    private ArtificeWorldGen() {
    }

    public static void register() {
        // BlockBasalt, BlockMarble: Material.rock cubes; BlockFlora (BlockFlower) and BlockLotus (BlockLilyPad): plants
        OldBlockInfo.registerCube(ArtificeIds.BASALT, Material.ROCK);
        OldBlockInfo.registerCube(ArtificeIds.MARBLE, Material.ROCK);
        OldBlockInfo.register(ArtificeIds.FLORA, Material.PLANTS, false, 0, false);
        OldBlockInfo.register(ArtificeIds.LOTUS, Material.PLANTS, false, 0, false);
        OldTerrainGen.registerWorldGenerator(FML_ID, "artifice:world", ArtificeWorldGen::generate);
    }

    /** ArtificeWorldGen.generateWorld with newGen = true. */
    static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        // dimension blacklist 1 and -1; the world type blacklist ("flat") never applies to the 1.5.2 generator
        if (world.dimension() != 0) {
            return;
        }
        for (int i = 0; i < FLORA_FREQUENCY; i++) {
            flowers(world, random, chunkX, chunkZ);
        }
        for (int i = 0; i < LOTUS_FREQUENCY; i++) {
            lily(world, random, chunkX, chunkZ);
        }
        layer(world, ArtificeIds.BASALT, BASALT_LAYER_MIN, BASALT_LAYER_MAX, chunkX, chunkZ);
        for (int i = 0; i < MARBLE_CLUSTER_FREQUENCY; i++) {
            int size = MARBLE_CLUSTER_SIZE + random.nextInt(MARBLE_CLUSTER_SIZE) / 2 - random.nextInt(MARBLE_CLUSTER_SIZE) / 2;
            cluster(world, random, ArtificeIds.MARBLE, MARBLE_CLUSTER_HEIGHT, size, chunkX, chunkZ);
        }
    }

    private static boolean inChunk(int chunkX, int chunkZ, int x, int z) {
        return chunkX == x >> 4 && chunkZ == z >> 4;
    }

    /**
     * WorldGenFlowers. The lotus (type 3) was set closed when {@code world.getWorldTime() > 12000}; the time is
     * not part of the seed, the port sets it open (time 0, as in a new world). It opens and closes by light anyway.
     */
    static void flowers(OldWorld world, Random rand, int chunkX, int chunkZ) {
        int type = rand.nextInt(4);
        int startX = (chunkX << 4) + rand.nextInt(16);
        int startZ = (chunkZ << 4) + rand.nextInt(16);
        OldBiome biome = world.getBiome(startX, startZ);
        if (FLORA_BLACKLIST.contains(biome.name)) {
            return;
        }
        if (rand.nextInt(10) > 6) {
            return;
        }
        int tries = rand.nextInt(16) + 8;
        for (int l = 0; l < tries; l++) {
            int x = startX + rand.nextInt(8) - rand.nextInt(8);
            int z = startZ + rand.nextInt(8) - rand.nextInt(8);
            int y = world.getHeightValue(x, z);
            // the && chain draws the last random number only when everything before it holds
            if (inChunk(chunkX, chunkZ, x, z)
                    && (world.isAirBlock(x, y, z) || world.getBlockId(x, y, z) == SNOW)
                    && OldPlantGens.flowerCanStay(world, x, y, z)
                    && rand.nextInt(10) <= 5) {
                world.setBlock(x, y, z, ArtificeIds.FLORA, type);
            }
        }
    }

    /** WorldGenLily. Set open for the same reason as the lotus flower. */
    static void lily(OldWorld world, Random rand, int chunkX, int chunkZ) {
        int startX = (chunkX << 4) + rand.nextInt(16);
        int startZ = (chunkZ << 4) + rand.nextInt(16);
        if (!LOTUS_WHITELIST.contains(world.getBiome(startX, startZ).name)) {
            return;
        }
        if (rand.nextInt(10) > 4) {
            return;
        }
        int tries = rand.nextInt(16) + 8;
        for (int l = 0; l < tries; l++) {
            int x = startX + rand.nextInt(8) - rand.nextInt(8);
            int z = startZ + rand.nextInt(8) - rand.nextInt(8);
            int y = world.getHeightValue(x, z);
            // BlockLotus.canPlaceBlockAt = BlockFlower: free (air here) and BlockLilyPad.canBlockStay
            if (inChunk(chunkX, chunkZ, x, z) && world.isAirBlock(x, y, z) && y >= 0 && y < OldWorld.HEIGHT
                    && world.material(x, y - 1, z) == Material.WATER && world.getBlockMetadata(x, y - 1, z) == 0) {
                world.setBlock(x, y, z, ArtificeIds.LOTUS, 0);
            }
        }
    }

    /**
     * WorldGenLayer. Its random source is a new {@link XSRandom} seeded with the world seed for every chunk, so
     * every chunk gets the same pattern of layer heights.
     */
    static void layer(OldWorld world, int id, int minHeight, int maxHeight, int chunkX, int chunkZ) {
        Random rand = new XSRandom(world.seed());
        int x0 = chunkX * 16;
        int z0 = chunkZ * 16;
        for (int i = x0; i < x0 + 16; i++) {
            for (int j = z0; j < z0 + 16; j++) {
                int min = minHeight + rand.nextInt(2) - rand.nextInt(2);
                int max = maxHeight + rand.nextInt(2) - rand.nextInt(2);
                if (min < 0) {
                    min = 0;
                }
                if (max > 256) {
                    max = 256;
                }
                for (int t = min; t < max; t++) {
                    if (STONE_TYPES.contains(world.getBlockId(i, t, j))) {
                        world.setBlock(i, t, j, id, 0);
                    }
                }
            }
        }
    }

    /** {@code BlockCoord} as WorldGenCluster used it: equality by position (for {@code List.remove(Object)}). */
    private record Pos(int x, int y, int z) {
        /** BlockCoord.getAdjacent: down, up, north, south, west, east. */
        Pos[] adjacent() {
            return new Pos[] {new Pos(x, y - 1, z), new Pos(x, y + 1, z), new Pos(x, y, z - 1), new Pos(x, y, z + 1),
                    new Pos(x - 1, y, z), new Pos(x + 1, y, z)};
        }
    }

    /** WorldGenCluster.canGenHere: inside the chunk, not air and a stone type. */
    private static boolean canGenHere(OldWorld world, int chunkX, int chunkZ, Pos b) {
        if (!inChunk(chunkX, chunkZ, b.x, b.z)) {
            return false;
        }
        int id = world.getBlockId(b.x, b.y, b.z);
        return id != 0 && STONE_TYPES.contains(id);
    }

    /**
     * WorldGenCluster.generate. The list of candidates is never cleared and keeps positions that were added more
     * than once or already turned; {@code remove} takes out the first equal entry, as {@code ArrayList.remove} did.
     */
    static void cluster(OldWorld world, Random rand, int id, int maxHeight, int size, int chunkX, int chunkZ) {
        List<Pos> blocks = new ArrayList<>();
        boolean doGen = false;
        int threshold = size / 8;
        int tries = rand.nextInt(16) + 4;
        Pos c = null;
        for (int i = 0; i < tries; i++) {
            int x = (chunkX << 4) + rand.nextInt(16);
            int y = rand.nextInt(maxHeight);
            int z = (chunkZ << 4) + rand.nextInt(16);
            c = new Pos(x, y, z);
            if (canGenHere(world, chunkX, chunkZ, c)) {
                doGen = true;
                break;
            }
        }
        if (!doGen) {
            return;
        }
        int genned = 0;
        while (genned < size) {
            for (Pos t : c.adjacent()) {
                if (canGenHere(world, chunkX, chunkZ, t)) {
                    blocks.add(t);
                }
            }
            if (blocks.isEmpty()) {
                break;
            }
            c = blocks.get(rand.nextInt(blocks.size()));
            blocks.remove(c);
            world.setBlock(c.x, c.y, c.z, id, 0);
            genned++;
            while (blocks.size() > threshold && genned < size && !blocks.isEmpty()) {
                c = blocks.get(rand.nextInt(blocks.size()));
                blocks.remove(c);
                world.setBlock(c.x, c.y, c.z, id, 0);
                genned++;
            }
        }
    }

    /** Artifice's XSRandom: a xorshift generator behind {@code java.util.Random}'s {@code nextInt}. */
    static final class XSRandom extends Random {
        private long state;

        XSRandom(long seed) {
            this.state = seed;
        }

        @Override
        protected int next(int nbits) {
            long x = this.state;
            x ^= x << 21;
            x ^= x >>> 35;
            x ^= x << 4;
            this.state = x;
            x &= (1L << nbits) - 1L;
            return (int) x;
        }
    }
}
