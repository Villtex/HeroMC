package wildcaves.gen;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldOreGens;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * The world generation of Wild Caves 3 0.4.2 (WorldGenWildCaves, the Generation* and Generate* classes, Utils and
 * the fossil generator EventManager), without game classes so that tools/expect.sh can run it. Values are those
 * of the pack configuration (WildCaves3.cfg), block ids too.
 * <p>
 * The original made many of its choices (which decoration, which stalactite piece, the kind of an iceshroom) with
 * {@code new Random()}: a fresh, unseeded generator per choice, so the same seed gave different caves on every
 * start. Here those draws come from one generator per chunk seeded from the world seed and the chunk
 * ({@link #choices}); the draws of the shared chunk random are those of the original.
 */
public final class WildCavesGen {
    /** FML mod id of the original, for the generator order ({@code OldTerrainGen.LOAD_ORDER}). */
    public static final String FML_ID = "WildCaves3";

    public static final int DECORATIONS = 1550;
    public static final int FLORA = 1551;
    public static final int FOSSILS = 1552;
    public static final int SANDSTONE_STALACTITE = 1553;
    public static final int STALACTITE = 1554;

    // general
    static final int TIMES_PER_CHUNK = 40;
    static final int MAX_GEN_HEIGHT = 80;
    static final int MAX_LENGTH = 8;
    static final int FOSSIL_CHANCES = 5;
    // biome specific
    static final float GLOWING_ICE_MUSHROOMS = 0.3F;
    static final float GLOWCAPS_HUMID = 0.3F;
    static final float ICICLES = 0.6F;
    static final float LESS_ARID = 0.5F;
    static final float MORE_WATER = 0.1F;
    static final float SANDSTONE_STALACTITES = 0.5F;
    static final float VINES_JUNGLE = 0.5F;
    // non biome specific
    static final float STALACTITES = 0.5F;
    /** "Probability of glowing mushrooms on caves" 0.1 never reached GenerationNormal, whose field stayed 0. */
    static final float GLOWCAPS_NORMAL = 0.0F;
    static final float SKULLS = 0.01F;
    static final float SPIDER_WEBS = 0.15F;
    static final float VINES = 0.1F;
    /** permissions."Block white list": blocks a cave decoration may hang from. */
    static final int[] WHITE_LIST = {1, 2, 3, 4, 13, 14, 15, 16, 21, 24, 56, 73, 74, 79, 80, 82, 97, 129};

    private WildCavesGen() {
    }

    public static void register() {
        OldBlockInfo.register(DECORATIONS, OldBlockInfo.Material.ROCK, false, 0, false);
        OldBlockInfo.register(FLORA, OldBlockInfo.Material.PLANTS, false, 0, false);
        OldBlockInfo.registerCube(FOSSILS, OldBlockInfo.Material.ROCK);
        OldBlockInfo.register(SANDSTONE_STALACTITE, OldBlockInfo.Material.ROCK, false, 0, false);
        OldBlockInfo.register(STALACTITE, OldBlockInfo.Material.ROCK, false, 0, false);
        // WildCaves.load: the cave generator, the skull loot, then the fossil generator
        OldTerrainGen.registerWorldGenerator(FML_ID, WildCavesGen::caves);
        for (OldLoot.Category category : new OldLoot.Category[] {OldLoot.DUNGEON_CHEST, OldLoot.MINESHAFT_CORRIDOR, OldLoot.STRONGHOLD_CORRIDOR}) {
            for (int kind : new int[] {0, 2, 3, 4}) {
                category.add(FML_ID, new OldStack(397, kind, 1), 1, 2, 50);
            }
        }
        OldTerrainGen.registerWorldGenerator(FML_ID, WildCavesGen::fossils);
    }

    /** Stands in for the unseeded {@code new Random()} of the original, one per chunk. */
    static Random choices(OldWorld world, int chunkX, int chunkZ) {
        return new Random(world.seed() ^ (chunkX * 341873128712L + chunkZ * 132897987541L) ^ 0x57494C4443415645L);
    }

    // ---- WorldGenWildCaves ----

    static void caves(Random random, int chunkX, int chunkZ, OldWorld world) {
        Random choices = choices(world, chunkX, chunkZ);
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        for (int i = 0; i < TIMES_PER_CHUNK; i++) {
            int x = blockX + random.nextInt(16);
            int y = random.nextInt(MAX_GEN_HEIGHT);
            int z = blockZ + random.nextInt(16);
            while ((!contains(WHITE_LIST, world.getBlockId(x, y + 1, z)) || !world.isAirBlock(x, y, z)) && y > 10) {
                y--;
            }
            if (y > 10) {
                OldBiome biome = world.getBiome(blockX, blockZ);
                boolean humid = biome.rainfall > 0.85F;
                if (biome.temperature <= 0.1F) {
                    frozen(world, random, choices, x, y, z);
                } else if (biome.temperature > 1.5F && biome.rainfall < 0.1F) {
                    arid(world, random, choices, x, y, z);
                } else if (humid && biome.temperature > 1.0F) {
                    jungle(world, random, choices, x, y, z);
                } else if (!humid && biome != OldBiomes.OCEAN && biome != OldBiomes.RIVER) {
                    normal(world, random, choices, x, y, z);
                } else {
                    humid(world, random, choices, x, y, z);
                }
            }
        }
    }

    static void frozen(OldWorld world, Random random, Random choices, int x, int y, int z) {
        switch (weightedChoice(choices, GLOWING_ICE_MUSHROOMS, SPIDER_WEBS, ICICLES, SKULLS, STALACTITES, 0.0F)) {
            case 1 -> iceshrooms(world, random, choices, x, y - emptyBlocks(world, x, y, z) + 1, z);
            case 2 -> world.setBlock(x, y, z, 30);
            case 3 -> icicles(world, random, choices, x, y, z, emptyBlocks(world, x, y, z));
            case 4 -> skulls(world, random, x, y, z, emptyBlocks(world, x, y, z));
            default -> stoneStalactite(world, random, choices, x, y, z, emptyBlocks(world, x, y, z));
        }
    }

    static void arid(OldWorld world, Random random, Random choices, int x, int y, int z) {
        switch (weightedChoice(choices, SANDSTONE_STALACTITES, SPIDER_WEBS, LESS_ARID, SKULLS, STALACTITES, 0.0F)) {
            case 1 -> sandstoneStalactite(world, random, choices, x, y, z, emptyBlocks(world, x, y, z));
            case 2 -> world.setBlock(x, y, z, 30);
            case 3 -> {
            }
            case 4 -> skulls(world, random, x, y, z, emptyBlocks(world, x, y, z));
            default -> stoneStalactite(world, random, choices, x, y, z, emptyBlocks(world, x, y, z));
        }
    }

    /** GenerationJungle: its glowcap probability was set to the one of jungle vines. */
    static void jungle(OldWorld world, Random random, Random choices, int x, int y, int z) {
        switch (weightedChoice(choices, VINES_JUNGLE, VINES_JUNGLE, SPIDER_WEBS, SKULLS, STALACTITES, 0.0F)) {
            case 1 -> glowcaps(world, random, x, y - emptyBlocks(world, x, y, z) + 1, z);
            case 2 -> vines(world, random, x, y, z);
            case 3 -> world.setBlock(x, y, z, 30);
            case 4 -> skulls(world, random, x, y, z, emptyBlocks(world, x, y, z));
            default -> stoneStalactite(world, random, choices, x, y, z, emptyBlocks(world, x, y, z));
        }
    }

    static void normal(OldWorld world, Random random, Random choices, int x, int y, int z) {
        switch (weightedChoice(choices, VINES, SPIDER_WEBS, STALACTITES, GLOWCAPS_NORMAL, SKULLS, 0.0F)) {
            case 1 -> vines(world, random, x, y, z);
            case 2 -> world.setBlock(x, y, z, 30);
            case 4 -> glowcaps(world, random, x, y - emptyBlocks(world, x, y, z) + 1, z);
            case 5 -> skulls(world, random, x, y, z, emptyBlocks(world, x, y, z));
            default -> stoneStalactite(world, random, choices, x, y, z, emptyBlocks(world, x, y, z));
        }
    }

    /** GenerationHumid: the fifth weight is the stalactite probability, but its case makes skulls. */
    static void humid(OldWorld world, Random random, Random choices, int x, int y, int z) {
        switch (weightedChoice(choices, GLOWCAPS_HUMID, MORE_WATER, VINES, SPIDER_WEBS, STALACTITES, 0.0F)) {
            case 1 -> glowcaps(world, random, x, y - emptyBlocks(world, x, y, z) + 1, z);
            case 2 -> world.setBlock(x, y + 1, z, 8);
            case 3 -> vines(world, random, x, y, z);
            case 4 -> world.setBlock(x, y - emptyBlocks(world, x, y, z) + 1, z, 30);
            case 5 -> skulls(world, random, x, y, z, emptyBlocks(world, x, y, z));
            default -> stoneStalactite(world, random, choices, x, y, z, emptyBlocks(world, x, y, z));
        }
    }

    // ---- structureGen ----

    /** GenerateGlowcaps: one of glowcaps 0 to 3, or the two-high glowcap (4 below, 5 above) where there is room. */
    static void glowcaps(OldWorld world, Random random, int x, int y, int z) {
        if (!world.isAirBlock(x, y - 1, z)) {
            int kind = world.isAirBlock(x, y + 1, z) ? random.nextInt(5) : random.nextInt(4);
            world.setBlock(x, y, z, FLORA, kind);
            if (kind == 4) {
                world.setBlock(x, y + 1, z, FLORA, kind + 1);
            }
        }
    }

    static void iceshrooms(OldWorld world, Random random, Random choices, int x, int y, int z) {
        if (!world.isAirBlock(x, y - 1, z)) {
            if (!world.material(x, y, z).liquid) {
                world.setBlock(x, y - 1, z, 79);
                world.setBlock(x, y, z, FLORA, randomChoice(choices, 6, 7, 8, 9));
            }
            convertToFrozen(world, random, choices, x, y, z);
        }
    }

    static void icicles(OldWorld world, Random random, Random choices, int x, int y, int z, int distance) {
        int botY = y - distance + 1;
        world.setBlock(x, y + 1, z, 79);
        world.setBlock(x, y, z, DECORATIONS, randomChoice(choices, 0, 1, 2));
        convertToFrozen(world, random, choices, x, y, z);
        if (!world.material(x, botY, z).liquid) {
            convertToFrozen(world, random, choices, x, botY, z);
        }
    }

    /** GenerateSkulls: a skeleton skull on the floor below, turned by random.nextInt(360). */
    static void skulls(OldWorld world, Random random, int x, int y, int z, int emptyBlocks) {
        if (emptyBlocks > 0) {
            int floorY = y - emptyBlocks + 1;
            world.setBlock(x, floorY, z, 144, 1);
            world.setTile(x, floorY, z, new OldTile.Skull(0, random.nextInt(360)));
        }
    }

    /** GenerateVines: a vine from the ceiling down, on a random side, at most to the floor. */
    static void vines(OldWorld world, Random random, int x, int y, int z) {
        int distance = emptyBlocks(world, x, y, z);
        int length = distance > 5 ? random.nextInt(distance - 5) + 5 : distance;
        int side = random.nextInt(4) + 2;
        // Direction.facingToDirection {-1, -1, 2, 0, 1, 3}
        int meta = 1 << new int[] {-1, -1, 2, 0, 1, 3}[side];
        for (int i = 0; i < length && !world.material(x, y - i, z).liquid; i++) {
            world.setBlock(x, y - i, z, 106, meta);
        }
    }

    static void stoneStalactite(OldWorld world, Random random, Random choices, int x, int y, int z, int distance) {
        stalactite(world, random, choices, x, y, z, distance, STALACTITE, false);
    }

    static void sandstoneStalactite(OldWorld world, Random random, Random choices, int x, int y, int z, int distance) {
        if (world.getBlockId(x, y + 1, z) == 1) {
            world.setBlock(x, y + 1, z, SANDSTONE_STALACTITE, 0);
        }
        stalactite(world, random, choices, x, y, z, distance, SANDSTONE_STALACTITE, true);
    }

    /**
     * GenerateStoneStalactite and GenerateSandstoneStalactites: a stalactite from the ceiling and maybe a stalagmite
     * from the floor, growing towards each other. A gap of one block gets the single piece 0; the stone version wrote
     * it to (x, y, y) by mistake, a block far away in another chunk, which is left out here.
     */
    private static void stalactite(OldWorld world, Random random, Random choices, int x, int y, int z, int distance, int id, boolean sand) {
        boolean stalagmite = false;
        if (distance == 1) {
            if (sand) {
                world.setBlock(x, y, z, id, 0);
            }
            return;
        }
        int k = 0;
        int j = 0;
        int topY = y;
        int botY = y - distance + 1;
        world.setBlock(x, topY, z, id, randomChoice(choices, 1, 2, 3, 3));
        if (sand) {
            convertToSand(world, random, choices, x, topY, z);
        }
        j++;
        if (!world.material(x, botY, z).liquid && !world.isAirBlock(x, botY - 1, z)) {
            int aux = randomChoice(choices, -1, 8, 9, 10);
            if (aux != -1) {
                if (sand && world.getBlockId(x, botY - 1, z) == 1) {
                    world.setBlock(x, botY - 1, z, 24, 0);
                }
                world.setBlock(x, botY, z, id, aux);
                j++;
                stalagmite = true;
                if (sand) {
                    convertToSand(world, random, choices, x, botY, z);
                }
            }
        }
        if (distance > 2) {
            while (k < MAX_LENGTH && topY >= botY && j < distance && !world.material(x, topY - 1, z).liquid) {
                k++;
                int topMeta = world.getBlockMetadata(x, topY, z);
                int bottomMeta = world.getBlockMetadata(x, botY, z);
                topY--;
                botY++;
                if (world.isAirBlock(x, topY, z) && topMeta > 2 && topMeta < 6) {
                    if (random.nextInt(5) != 4) {
                        world.setBlock(x, topY, z, id, randomChoice(choices, 4, 5, 7, 11));
                    } else {
                        world.setBlock(x, topY, z, id, randomChoice(choices, 7, 11));
                    }
                    j++;
                }
                if (world.isAirBlock(x, botY, z) && (bottomMeta > 3 && bottomMeta < 5 || bottomMeta == 8) && j < distance && stalagmite) {
                    if (random.nextInt(5) != 4) {
                        world.setBlock(x, botY, z, id, randomChoice(choices, 4, 5, 6, 12));
                    } else {
                        world.setBlock(x, botY, z, id, randomChoice(choices, 12, 6));
                    }
                    j++;
                }
            }
        }
    }

    // ---- Utils ----

    /** Utils.weightedChoise: 1 to 6 by weight; the sixth weight counts in the total but has no range of its own. */
    static int weightedChoice(Random choices, float p1, float p2, float p3, float p4, float p5, float p6) {
        float total = p1 + p2 + p3 + p4 + p5 + p6;
        float value = choices.nextFloat();
        p1 /= total;
        p2 /= total;
        p3 /= total;
        p4 /= total;
        p5 /= total;
        if (value < p1) {
            return 1;
        }
        float previous = p1;
        if (value < p2 + previous) {
            return 2;
        }
        previous += p2;
        if (value < p3 + previous) {
            return 3;
        }
        previous += p3;
        if (value < p4 + previous) {
            return 4;
        }
        previous += p4;
        return value < p5 + previous ? 5 : 6;
    }

    /** Utils.randomChoise. */
    static int randomChoice(Random choices, int... values) {
        return values[choices.nextInt(values.length)];
    }

    /** Utils.getNumEmptyBlocks: how far down to the next normal cube (or tile entity), counting this block. */
    static int emptyBlocks(OldWorld world, int x, int y, int z) {
        int distance = 0;
        while (!world.isBlockNormalCube(x, y, z) && y > 5 && world.getTile(x, y, z) == null) {
            y--;
            distance++;
        }
        return distance;
    }

    /** Utils.convertToFrozenType: 80 % of stone, dirt, gravel and grass in a small box around the spot become ice. */
    static void convertToFrozen(OldWorld world, Random random, Random choices, int x, int y, int z) {
        int height = random.nextInt(5) + 3;
        int length = random.nextInt(5) + 3;
        int width = random.nextInt(5) + 3;
        int newX = x - length / 2;
        int newY = y + height / 2;
        int newZ = z - width / 2;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < width; k++) {
                    if (weightedChoice(choices, 0.8F, 0.2F, 0.0F, 0.0F, 0.0F, 0.0F) == 1) {
                        int id = world.getBlockId(newX + j, newY - i, newZ + k);
                        if (id == 1 || id == 3 || id == 13 || id == 2) {
                            world.setBlock(newX + j, newY - i, newZ + k, 79);
                        }
                    }
                }
            }
        }
    }

    /** Utils.convertToSandType: 70 % of stone becomes sandstone, of dirt and gravel sand. */
    static void convertToSand(OldWorld world, Random random, Random choices, int x, int y, int z) {
        int height = random.nextInt(5) + 3;
        int length = random.nextInt(5) + 3;
        int width = random.nextInt(5) + 3;
        int newX = x - length / 2;
        int newY = y + height / 2;
        int newZ = z - width / 2;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < width; k++) {
                    if (weightedChoice(choices, 0.7F, 0.3F, 0.0F, 0.0F, 0.0F, 0.0F) == 1) {
                        int id = world.getBlockId(newX + j, newY - i, newZ + k);
                        if (id == 1) {
                            world.setBlock(newX + j, newY - i, newZ + k, 24, 0);
                        } else if (id == 3 || id == 13) {
                            world.setBlock(newX + j, newY - i, newZ + k, 12, 0);
                        }
                    }
                }
            }
        }
    }

    static boolean contains(int[] array, int value) {
        for (int v : array) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }

    // ---- EventManager ----

    /** EventManager.generateSurface: five fossil veins of 4 to 6 blocks in stone, y 1 to 89. */
    static void fossils(Random random, int chunkX, int chunkZ, OldWorld world) {
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        int veinSize = 4 + random.nextInt(3);
        int minY = 1;
        int maxY = 90;
        for (int i = 0; i < FOSSIL_CHANCES; i++) {
            int x = blockX + random.nextInt(16);
            int y = minY + random.nextInt(maxY - minY);
            int z = blockZ + random.nextInt(16);
            new OldOreGens.Minable(FOSSILS, veinSize).generate(world, random, x, y, z);
        }
    }
}
