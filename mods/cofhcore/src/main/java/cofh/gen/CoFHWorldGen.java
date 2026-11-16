package cofh.gen;

import hexxit.worldgen152.core.populate.OldOreGens;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CoFH World 1.5.2.5 ({@code CoFHWorld}, {@code WorldHandler}, {@code FeatureOreGen*}, {@code WorldGenMinableCluster})
 * in the 1.5.2 core, without game classes so that tools/expect.sh can run it.
 *
 * <p>Settings of the Hexxit pack ({@code config/cofh/World.cfg}): {@code ReplaceVanillaGeneration} and
 * {@code FlatBedrock} are on. CoFH World then denies the eight ore events of the biome decorator and generates the
 * ores itself with the values of the {@code world.vanilla} section, which the pack raised well above vanilla
 * (iron 40 veins, gold 8, redstone 64, diamond 8, all of them up to y 128). Retro generation is off.
 *
 * <p>{@code WorldCustomGen.txt} of the pack adds one template: QOre of qCraft (id 2373). {@code AllowCustomGeneration}
 * is off, but CoFH World 1.5.2.5 never reads that flag: the template is generated whenever the block exists. qCraft
 * (part 17) calls {@link #addCustomFeature} for it.
 */
public final class CoFHWorldGen {
    /** FML mod id of the original, for the generator order ({@code OldTerrainGen.LOAD_ORDER}). */
    public static final String FML_ID = "CoFHWorld";

    private static final int STONE = 1;
    private static final int BEDROCK = 7;
    private static final int NETHERRACK = 87;

    /** One {@code IFeatureGenerator} of {@code WorldHandler.features}, in registration order. */
    private record Feature(String name, OldOreGens.Minable gen, int count, int a, int b, boolean normal) {
        void generate(OldWorld world, Random random, int chunkX, int chunkZ) {
            int blockX = chunkX * 16;
            int blockZ = chunkZ * 16;
            for (int i = 0; i < this.count; i++) {
                int x = blockX + random.nextInt(16);
                // FeatureOreGenUniform: minY + nextInt(maxY - minY); FeatureOreGenNormal: two draws around meanY
                int y = this.normal ? random.nextInt(this.b) + random.nextInt(this.b) + this.a - this.b
                        : this.a + random.nextInt(this.b - this.a);
                int z = blockZ + random.nextInt(16);
                this.gen.generate(world, random, x, y, z);
            }
        }
    }

    private static final List<Feature> FEATURES = new ArrayList<>();
    private static volatile List<Feature> features = List.of();

    private CoFHWorldGen() {
    }

    public static synchronized void register() {
        if (!FEATURES.isEmpty()) {
            return;
        }
        // CoFHWorld.loadWorldGeneration with the values of the pack's World.cfg (world.vanilla)
        addUniform("dirt", 3, 0, 32, 20, 0, 128);
        addUniform("gravel", 13, 0, 32, 10, 0, 128);
        addUniform("coal", 16, 0, 16, 20, 0, 128);
        addUniform("iron", 15, 0, 8, 40, 0, 128);
        addUniform("gold", 14, 0, 8, 8, 0, 128);
        addUniform("redstone", 73, 0, 7, 64, 0, 128);
        addUniform("diamond", 56, 0, 7, 8, 0, 128);
        addNormal("lapis", 21, 0, 6, 8, 64, 64);

        // WorldHandler.handleOreGenEvent: ReplaceVanillaGeneration denies all eight vanilla ore events
        OldTerrainGen.addListener(FML_ID, new OldTerrainGen.Listener() {
            @Override
            public boolean generateOre(OldWorld world, Random random, int x, int z, OldTerrainGen.Ore type) {
                return false;
            }
        });
        OldTerrainGen.registerWorldGenerator(FML_ID, CoFHWorldGen::generate);
    }

    /** A {@code UNIFORM} feature: {@code count} veins of {@code size} blocks between {@code minY} and {@code maxY}. */
    public static void addUniform(String name, int blockId, int meta, int size, int count, int minY, int maxY) {
        add(new Feature(name, new OldOreGens.Minable(blockId, meta, size, STONE), count, minY, maxY, false));
    }

    /** A {@code NORMAL} feature: {@code count} veins of {@code size} blocks around {@code meanY}. */
    public static void addNormal(String name, int blockId, int meta, int size, int count, int meanY, int maxVar) {
        add(new Feature(name, new OldOreGens.Minable(blockId, meta, size, STONE), count, meanY, maxVar, true));
    }

    /**
     * A template of {@code WorldCustomGen.txt} (CoFHWorld.addCustomFeature), registered after the vanilla
     * features as in CoFH World's post-init. {@code normal}: NORMAL instead of UNIFORM ({@code a}/{@code b} =
     * meanY/maxVar instead of minY/maxY).
     */
    public static void addCustomFeature(String name, int blockId, int meta, int size, int count, int a, int b, boolean normal) {
        add(new Feature(name, new OldOreGens.Minable(blockId, meta, size, STONE), count, a, b, normal));
    }

    private static synchronized void add(Feature feature) {
        // WorldHandler.registerFeature: a second feature with the same name is ignored
        for (Feature f : FEATURES) {
            if (f.name().equalsIgnoreCase(feature.name())) {
                return;
            }
        }
        FEATURES.add(feature);
        features = List.copyOf(FEATURES);
    }

    /** WorldHandler.generateWorld with newGen = true. */
    static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        replaceBedrock(world, chunkX, chunkZ);
        if (world.dimension() != 1 && world.dimension() != -1) {
            for (Feature feature : features) {
                feature.generate(world, random, chunkX, chunkZ);
            }
        }
    }

    /**
     * WorldHandler.replaceBedrock (FlatBedrock): bedrock in y 1-5 becomes stone, in the Nether also in y 122-126 and
     * netherrack. The original asked the biome at the block coordinates (chunkX, chunkZ) whether it is "hell":
     * true everywhere in the Nether and nowhere in the overworld.
     */
    static void replaceBedrock(OldWorld world, int chunkX, int chunkZ) {
        boolean nether = world.dimension() == -1;
        int replacement = nether ? NETHERRACK : STONE;
        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                int x = chunkX * 16 + bx;
                int z = chunkZ * 16 + bz;
                if (nether) {
                    for (int y = 126; y > 121; y--) {
                        if (world.getBlockId(x, y, z) == BEDROCK) {
                            world.setBlock(x, y, z, replacement, 0);
                        }
                    }
                }
                for (int y = 5; y > 0; y--) {
                    if (world.getBlockId(x, y, z) == BEDROCK) {
                        world.setBlock(x, y, z, replacement, 0);
                    }
                }
            }
        }
    }
}
