package twilightforest.gen;

import hexxit.worldgen152.core.OctaveNoise;
import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.OldMath;

import java.util.Random;

/**
 * The terrain of the Twilight Forest (ChunkProviderTwilightForest.provideChunk): a low landscape of 64 blocks with
 * the sea at 32, glaciers, the hills and courtyards of the major features, the canopy of the dark forest, biome
 * surface layers, caves and ravines. A chunk is a pure function of seed and position; one instance serves all threads.
 *
 * <p>The original kept block ids (up to 4095) and metadata in separate arrays; they are joined into
 * {@code id << 4 | metadata} cells at the end, dropping metadata of air as Chunk storage did.
 */
public final class TFTerrain {
    public static final int SEALEVEL = 32;
    public static final int WORLDHEIGHT = 128;

    public static final int STONE = 1;
    public static final int GRASS = 2;
    public static final int DIRT = 3;
    public static final int BEDROCK = 7;
    public static final int WATER_MOVING = 8;
    public static final int WATER_STILL = 9;
    public static final int SAND = 12;
    public static final int GRAVEL = 13;
    public static final int SANDSTONE = 24;
    public static final int ICE = 79;
    /** Hedge of the pack's TwilightForest.cfg; metadata 1 is the darkwood leaves of the canopy. */
    public static final int HEDGE = 1356;

    private static final float[] SQUARE_TABLE = new float[25];

    static {
        for (int k1 = -2; k1 <= 2; k1++) {
            for (int l1 = -2; l1 <= 2; l1++) {
                SQUARE_TABLE[k1 + 2 + (l1 + 2) * 5] = 10.0F / OldMath.sqrt(k1 * k1 + l1 * l1 + 0.2F);
            }
        }
    }

    private final long seed;
    private final TFLayout layout;
    private final OctaveNoise noiseGen1;
    private final OctaveNoise noiseGen2;
    private final OctaveNoise noiseGen3;
    private final OctaveNoise noiseGen4;
    private final OctaveNoise noiseGen6;
    private final TFCaves caves;
    private final TFRavine ravines;

    public TFTerrain(TFLayout layout) {
        this.seed = layout.seed();
        this.layout = layout;
        Random random = new Random(this.seed);
        this.noiseGen1 = new OctaveNoise(random, 16);
        this.noiseGen2 = new OctaveNoise(random, 16);
        this.noiseGen3 = new OctaveNoise(random, 8);
        this.noiseGen4 = new OctaveNoise(random, 4);
        new OctaveNoise(random, 10); // noiseGen5, sampled but never used
        this.noiseGen6 = new OctaveNoise(random, 16);
        this.caves = new TFCaves(this.seed, layout);
        this.ravines = new TFRavine(this.seed, layout);
    }

    public TFLayout layout() {
        return this.layout;
    }

    /** provideChunk. */
    public OldChunk generate(int cx, int cz) {
        Random rand = new Random(cx * 341873128712L + cz * 132897987541L);
        short[] blockStorage = new short[32768];
        byte[] metaStorage = new byte[32768];
        this.generateTerrain(cx, cz, blockStorage);
        byte[] biomeIds = this.layout.chunkIds(cx, cz);
        OldBiome[] biomes = new OldBiome[256];
        for (int i = 0; i < 256; i++) {
            biomes[i] = OldBiomes.byId(biomeIds[i] & 0xFF);
        }
        double[] stoneNoise = this.noiseGen4.generate(cx * 16, cz * 16, 0, 16, 16, 1, 0.0625, 0.0625, 0.0625);
        addGlaciers(blockStorage, biomes);
        this.raiseHills(cx, cz, blockStorage);
        addDarkForestCanopy(blockStorage, metaStorage, biomes, stoneNoise);
        replaceBlocksForBiome(blockStorage, biomes, stoneNoise, rand);
        this.addFeatureMarker(cx, cz, blockStorage, metaStorage);
        this.caves.generate(cx, cz, blockStorage);
        this.ravines.generate(cx, cz, blockStorage);
        char[] cells = new char[32768];
        for (int i = 0; i < cells.length; i++) {
            int id = blockStorage[i] & 4095;
            if (id != 0) {
                cells[i] = (char) (id << 4 | metaStorage[i] & 15);
            }
        }
        return OldChunk.wide(cx, cz, cells, biomeIds);
    }

    /** generateTerrain: stone below the density surface, water up to y 32; only the lower 64 blocks. */
    private void generateTerrain(int par1, int par2, short[] blocks) {
        byte byte0 = 4;
        byte byte1 = 8;
        int byte2 = SEALEVEL;
        int i = byte0 + 1;
        byte byte3 = 9;
        int j = byte0 + 1;
        int[] quarterIds = this.layout.quarterIds(par1 * 4 - 2, par2 * 4 - 2, i + 5, j + 5);
        double[] landMap = this.makeLandPerBiome(quarterIds, par1 * byte0, 0, par2 * byte0, i, byte3, j);

        for (int k = 0; k < byte0; k++) {
            for (int l = 0; l < byte0; l++) {
                for (int i1 = 0; i1 < byte1; i1++) {
                    double d = 0.125;
                    double d1 = landMap[((k + 0) * j + l + 0) * byte3 + i1 + 0];
                    double d2 = landMap[((k + 0) * j + l + 1) * byte3 + i1 + 0];
                    double d3 = landMap[((k + 1) * j + l + 0) * byte3 + i1 + 0];
                    double d4 = landMap[((k + 1) * j + l + 1) * byte3 + i1 + 0];
                    double d5 = (landMap[((k + 0) * j + l + 0) * byte3 + i1 + 1] - d1) * d;
                    double d6 = (landMap[((k + 0) * j + l + 1) * byte3 + i1 + 1] - d2) * d;
                    double d7 = (landMap[((k + 1) * j + l + 0) * byte3 + i1 + 1] - d3) * d;
                    double d8 = (landMap[((k + 1) * j + l + 1) * byte3 + i1 + 1] - d4) * d;

                    for (int j1 = 0; j1 < 8; j1++) {
                        double d9 = 0.25;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int k1 = 0; k1 < 4; k1++) {
                            int l1 = k1 + k * 4 << 11 | 0 + l * 4 << 7 | i1 * 8 + j1;
                            char c = 128;
                            l1 -= c;
                            double d14 = 0.25;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;
                            d15 -= d16;

                            for (int i2 = 0; i2 < 4; i2++) {
                                if ((d15 += d16) > 0.0) {
                                    blocks[l1 += c] = STONE;
                                } else if (i1 * 8 + j1 < byte2) {
                                    blocks[l1 += c] = WATER_STILL;
                                } else {
                                    blocks[l1 += c] = 0;
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    /** makeLandPerBiome: 5x9x5 density samples shaped by the biome height ranges. */
    private double[] makeLandPerBiome(int[] quarterIds, int xx, int zero, int zz, int l, int i1, int j1) {
        double[] ad = new double[l * i1 * j1];
        double d = 684.412;
        double d1 = 684.412;
        double[] noise6 = this.noiseGen6.generate(xx, zz, l, j1, 200.0, 200.0);
        double[] noise3 = this.noiseGen3.generate(xx, zero, zz, l, i1, j1, d / 80.0, d1 / 160.0, d / 80.0);
        double[] noise1 = this.noiseGen1.generate(xx, zero, zz, l, i1, j1, d, d1, d);
        double[] noise2 = this.noiseGen2.generate(xx, zero, zz, l, i1, j1, d, d1, d);
        int i2 = 0;
        int j2 = 0;

        for (int k2 = 0; k2 < l; k2++) {
            for (int l2 = 0; l2 < j1; l2++) {
                float f1 = 0.0F;
                float f2 = 0.0F;
                float f3 = 0.0F;
                byte byte0 = 2;
                OldBiome biomegenbase = biome(quarterIds[k2 + 2 + (l2 + 2) * (l + 5)]);

                for (int i3 = -byte0; i3 <= byte0; i3++) {
                    for (int j3 = -byte0; j3 <= byte0; j3++) {
                        OldBiome biomegenbase1 = biome(quarterIds[k2 + i3 + 2 + (l2 + j3 + 2) * (l + 5)]);
                        float f4 = SQUARE_TABLE[i3 + 2 + (j3 + 2) * 5] / (biomegenbase1.minHeight + 2.0F);
                        if (biomegenbase1.minHeight > biomegenbase.minHeight) {
                            f4 /= 2.0F;
                        }

                        f1 += biomegenbase1.maxHeight * f4;
                        f2 += biomegenbase1.minHeight * f4;
                        f3 += f4;
                    }
                }

                f1 /= f3;
                f2 /= f3;
                f1 = f1 * 0.9F + 0.1F;
                f2 = (f2 * 4.0F - 1.0F) / 8.0F;
                double d2 = noise6[j2] / 8000.0;
                if (d2 < 0.0) {
                    d2 = -d2 * 0.3;
                }

                d2 = d2 * 3.0 - 2.0;
                if (d2 < 0.0) {
                    d2 /= 2.0;
                    if (d2 < -1.0) {
                        d2 = -1.0;
                    }

                    d2 /= 1.4;
                    d2 /= 2.0;
                } else {
                    if (d2 > 1.0) {
                        d2 = 1.0;
                    }

                    d2 /= 8.0;
                }

                j2++;

                for (int k3 = 0; k3 < i1; k3++) {
                    double d3 = f2;
                    double d4 = f1;
                    d3 += d2 * 0.2;
                    d3 = d3 * i1 / 16.0;
                    double d5 = i1 / 2.0 + d3 * 4.0;
                    double d6;
                    double d7 = (k3 - d5) * 12.0 * 128.0 / WORLDHEIGHT / d4;
                    if (d7 < 0.0) {
                        d7 *= 4.0;
                    }

                    double d8 = noise1[i2] / 512.0;
                    double d9 = noise2[i2] / 512.0;
                    double d10 = (noise3[i2] / 10.0 + 1.0) / 2.0;
                    if (d10 < 0.0) {
                        d6 = d8;
                    } else if (d10 > 1.0) {
                        d6 = d9;
                    } else {
                        d6 = d8 + (d9 - d8) * d10;
                    }

                    d6 -= d7;
                    if (k3 > i1 - 4) {
                        double d11 = (k3 - (i1 - 4)) / 3.0F;
                        d6 = d6 * (1.0 - d11) + -10.0 * d11;
                    }

                    ad[i2] = d6;
                    i2++;
                }
            }
        }

        return ad;
    }

    /** getBiomesForGeneration turned negative ids into Twilight Forest. */
    private static OldBiome biome(int id) {
        return id >= 0 ? OldBiomes.byId(id) : TFBiomes.TWILIGHT_FOREST;
    }

    /** replaceBlocksForBiome. Unlike the overworld, air does not end a stone run here. */
    private static void replaceBlocksForBiome(short[] blockArray4096, OldBiome[] biomes, double[] stoneNoise, Random rand) {
        int sealevel = SEALEVEL;

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                OldBiome biomegenbase = biomes[j + i * 16];
                if (biomegenbase == null) {
                    biomegenbase = TFBiomes.TWILIGHT_FOREST;
                }

                float f = biomegenbase.temperature;
                int k = (int) (stoneNoise[i + j * 16] / 3.0 + 3.0 + rand.nextDouble() * 0.25);
                int l = -1;
                short top = biomegenbase.topBlock;
                short filler = biomegenbase.fillerBlock;

                for (int i1 = 127; i1 >= 0; i1--) {
                    int j1 = (j * 16 + i) * 128 + i1;
                    if (i1 <= 0 + rand.nextInt(5)) {
                        blockArray4096[j1] = BEDROCK;
                    } else {
                        short currentBlock = blockArray4096[j1];
                        if (currentBlock == STONE) {
                            if (l == -1) {
                                if (k <= 0) {
                                    top = 0;
                                    filler = STONE;
                                } else if (i1 >= sealevel - 4 && i1 <= sealevel + 1) {
                                    top = biomegenbase.topBlock;
                                    filler = biomegenbase.fillerBlock;
                                }

                                if (i1 < sealevel && top == 0) {
                                    if (f < 0.15F) {
                                        top = ICE;
                                    } else {
                                        top = WATER_STILL;
                                    }
                                }

                                l = k;
                                if (i1 >= sealevel - 1) {
                                    blockArray4096[j1] = top;
                                } else {
                                    blockArray4096[j1] = filler;
                                }
                            } else if (l > 0) {
                                l--;
                                blockArray4096[j1] = filler;
                                if (l == 0 && filler == SAND) {
                                    l = rand.nextInt(4);
                                    filler = SANDSTONE;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** raiseHills: hollow hills and the hydra lair rise, hedge mazes and courtyards are flattened. */
    private void raiseHills(int cx, int cz, short[] storage) {
        TFFeature nearFeature = TFFeature.getNearestFeature(cx, cz, this.layout);
        if (nearFeature == TFFeature.NOTHING) {
            return;
        }
        int[] nearCenter = TFFeature.getNearestCenter(cx, cz, this.layout);
        double hdiam = (nearFeature.size * 2 + 1) * 16.0;
        int hx = nearCenter[0];
        int hz = nearCenter[1];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int oldGround;
                int newGround = -1;
                int dx = x - hx;
                int dz = z - hz;
                int dist = (int) Math.sqrt(dx * dx + dz * dz);
                int hheight = (int) (Math.cos(dist / hdiam * Math.PI) * (hdiam / 3.0));
                if (nearFeature == TFFeature.HILL1 || nearFeature == TFFeature.HILL2 || nearFeature == TFFeature.HILL3
                        || nearFeature == TFFeature.HYDRA_LAIR) {
                    for (int y = 0; y <= 127; y++) {
                        int index = (x * 16 + z) * WORLDHEIGHT + y;
                        int currentTerrain = storage[index];
                        if (currentTerrain == 0 || currentTerrain == ICE) {
                            if (newGround == -1) {
                                oldGround = y;
                                newGround = oldGround + hheight;
                            }

                            if (y <= newGround) {
                                storage[index] = STONE;
                            }
                        }
                    }

                    int hollow = hheight - 4 - nearFeature.size;
                    if (nearFeature == TFFeature.HYDRA_LAIR) {
                        int mx = dx + 16;
                        int mz = dz + 16;
                        int mdist = (int) Math.sqrt(mx * mx + mz * mz);
                        int mheight = (int) (Math.cos(mdist / (hdiam / 1.5) * Math.PI) * (hdiam / 1.5));
                        hollow = Math.max(mheight - 4, hollow);
                    }

                    if (hollow < 0) {
                        hollow = 0;
                    }

                    for (int y = 0; y <= 127; y++) {
                        int index = (x * 16 + z) * WORLDHEIGHT + y;
                        if (hheight > 0 && y < SEALEVEL && storage[index] != STONE) {
                            storage[index] = STONE;
                        }

                        int hollowFloor = SEALEVEL - 3 - hollow / 8;
                        if (nearFeature == TFFeature.HYDRA_LAIR) {
                            hollowFloor = SEALEVEL;
                        }

                        if (y > hollowFloor && y < hollowFloor + hollow) {
                            storage[index] = 0;
                        }
                    }
                }

                if (nearFeature == TFFeature.HEDGE_MAZE || nearFeature == TFFeature.NAGA_LAIR || nearFeature == TFFeature.QUEST_GROVE) {
                    float squishfactor = 0.0F;
                    int mazeheight = SEALEVEL + 1;
                    int featureBoundary = (nearFeature.size * 2 + 1) * 8 - 8;
                    if (dx <= -featureBoundary) {
                        squishfactor = (-dx - featureBoundary) / 8.0F;
                    }

                    if (dx >= featureBoundary) {
                        squishfactor = (dx - featureBoundary) / 8.0F;
                    }

                    if (dz <= -featureBoundary) {
                        squishfactor = Math.max(squishfactor, (-dz - featureBoundary) / 8.0F);
                    }

                    if (dz >= featureBoundary) {
                        squishfactor = Math.max(squishfactor, (dz - featureBoundary) / 8.0F);
                    }

                    if (squishfactor > 0.0F) {
                        newGround = -1;

                        for (int y = 0; y <= 127; y++) {
                            int index = (x * 16 + z) * WORLDHEIGHT + y;
                            int currentTerrain = storage[index];
                            if (currentTerrain != STONE && newGround == -1) {
                                oldGround = y;
                                mazeheight = (int) (mazeheight + (oldGround - mazeheight) * squishfactor);
                                newGround = oldGround;
                            }
                        }
                    }

                    for (int y = 0; y <= 127; y++) {
                        int index = (x * 16 + z) * WORLDHEIGHT + y;
                        if (y < mazeheight && (storage[index] == 0 || storage[index] == WATER_STILL)) {
                            storage[index] = STONE;
                        }

                        if (y >= mazeheight && storage[index] != WATER_STILL) {
                            storage[index] = 0;
                        }
                    }
                }
            }
        }
    }

    /** addFeatureMarker: bedrock at the bottom of the first column, its metadata naming the feature. */
    private void addFeatureMarker(int cx, int cz, short[] blockStorage, byte[] metaStorage) {
        TFFeature feature = TFFeature.getFeatureDirectlyAt(cx, cz, this.layout);
        if (feature != TFFeature.NOTHING) {
            blockStorage[0] = BEDROCK;
            blockStorage[1] = BEDROCK;
            blockStorage[2] = BEDROCK;
            metaStorage[0] = (byte) (feature.featureID & 15);
            metaStorage[1] = (byte) ((feature.featureID & 240) >> 4);
            metaStorage[2] = 1;
        }
    }

    /** addGlaciers: gravel on the ground and 33 blocks of ice above it. */
    private static void addGlaciers(short[] blocks, OldBiome[] biomes) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                OldBiome biome = biomes[z + x * 16];
                if (biome == TFBiomes.GLACIER) {
                    int topLevel = -1;

                    for (int y = 127; y >= 0; y--) {
                        int index = (z * 16 + x) * 128 + y;
                        if (blocks[index] == STONE) {
                            topLevel = y;
                            blocks[index] = GRAVEL;
                            break;
                        }
                    }

                    int gHeight = 32;
                    int gTop = topLevel + gHeight + 1;

                    for (int y = topLevel + 1; y <= gTop && y < 128; y++) {
                        blocks[(z * 16 + x) * 128 + y] = ICE;
                    }
                }
            }
        }
    }

    /** addDarkForestCanopy: a layer of darkwood leaves (hedge, metadata 1) some blocks above the dark forest. */
    private static void addDarkForestCanopy(short[] blocks, byte[] meta, OldBiome[] biomes, double[] stoneNoise) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                OldBiome biome = biomes[z + x * 16];
                boolean generateForest = biome == TFBiomes.DARK_FOREST;
                if ((biome == TFBiomes.MAJOR_FEATURE || biome == TFBiomes.MINOR_FEATURE) && z + 1 + x * 16 < biomes.length) {
                    generateForest = biomes[z + 1 + x * 16] == TFBiomes.DARK_FOREST;
                }

                if (generateForest) {
                    int topLevel = -1;

                    for (int y = 127; y >= 0; y--) {
                        short currentBlock = blocks[(z * 16 + x) * 128 + y];
                        if (currentBlock == WATER_STILL) {
                            break;
                        }

                        if (currentBlock == STONE) {
                            topLevel = y;
                            break;
                        }
                    }

                    if (topLevel != -1) {
                        int noise = (int) (stoneNoise[x + z * 16] / 2.0);
                        int treeBottom = topLevel + 7 - noise;
                        int treeTop = treeBottom + 6 + noise;

                        for (int y = treeBottom; y <= treeTop; y++) {
                            int index = (z * 16 + x) * 128 + y;
                            blocks[index] = (short) HEDGE;
                            meta[index] = 1;
                        }
                    }
                }
            }
        }
    }
}
