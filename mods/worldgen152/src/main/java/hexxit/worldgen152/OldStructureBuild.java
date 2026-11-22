package hexxit.worldgen152;

import com.mojang.logging.LogUtils;
import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * A 1.5.2 structure builder ({@link OldTerrainGen#registerFeature}, like {@code "battletowers:tower"}) run once for
 * the start chunk of an {@link OldStructure}, on the terrain of today as it is before any feature: the chunk
 * generator builds noise, surface and carvers of every chunk the builder touches into a scratch chunk (without
 * terrain adaptation of other structures). What the builder changed is kept, per chunk, until the chunks are
 * decorated; the result is the same each time it is computed, so it is only cached (a few dozen starts), not saved.
 *
 * <p>Old and real coordinates are the same (x, y and z); the old world is 256 high (real y 0 to 255).
 */
final class OldStructureBuild {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
    private static final int MAX_RESULTS = 48;
    private static final int MAX_TERRAIN = 160;

    /** What a builder put into one chunk: positions (index into the chunk: x, z, y), final block states, tile data. */
    static final class ChunkChanges {
        final List<BlockPos> positions = new ArrayList<>();
        final List<BlockState> states = new ArrayList<>();
        final List<OldTile> tiles = new ArrayList<>();
        final List<OldEntity> entities = new ArrayList<>();
    }

    /** The outcome of one build; {@code box == null} when the builder built nothing. */
    record Result(BoundingBox box, BlockPos locate, Map<Long, ChunkChanges> chunks) {
        static final Result NONE = new Result(null, null, Map.of());
    }

    /** One chunk of terrain: cells 256 high, index (x * 16 + z) * 256 + y, 1.5.2 biome ids, surface heights. */
    record Terrain(char[] cells, byte[] biomes, int[] surface) {
    }

    private record Key(Object generator, long seed, String name, int chunkX, int chunkZ) {
    }

    private record TerrainKey(Object generator, long seed, int chunkX, int chunkZ) {
    }

    private static final Map<Key, CompletableFuture<Result>> RESULTS = new ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentLinkedQueue<Key> RESULT_ORDER = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private static final Map<TerrainKey, Terrain> TERRAIN = java.util.Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<TerrainKey, Terrain> eldest) {
            return this.size() > MAX_TERRAIN;
        }
    });

    /** Beardifier input without the structures of the world: the scratch terrain is the same whenever it is built. */
    private static final StructureManager NO_STRUCTURES = new StructureManager(null, null, null) {
        @Override
        public List<net.minecraft.world.level.levelgen.structure.StructureStart> startsForStructure(int x, int z, Predicate<Structure> matcher) {
            return List.of();
        }
    };

    private OldStructureBuild() {
    }

    /** Everything a build needs from the world; the same in findGenerationPoint and in postProcess. */
    record Context(ChunkGenerator generator, RandomState randomState, LevelHeightAccessor height, RegistryAccess registries, long seed) {
    }

    static void clear() {
        RESULTS.clear();
        RESULT_ORDER.clear();
        TERRAIN.clear();
    }

    /** The terrain of a chunk before features, as 1.5.2 cells (cached). */
    static Terrain terrain(Context context, int chunkX, int chunkZ) {
        TerrainKey key = new TerrainKey(context.generator(), context.seed(), chunkX, chunkZ);
        Terrain terrain = TERRAIN.get(key);
        if (terrain == null) {
            terrain = buildTerrain(context, chunkX, chunkZ);
            TERRAIN.put(key, terrain);
        }
        return terrain;
    }

    private static Terrain buildTerrain(Context context, int chunkX, int chunkZ) {
        ChunkGenerator generator = context.generator();
        ProtoChunk chunk = new ProtoChunk(new ChunkPos(chunkX, chunkZ), UpgradeData.EMPTY, context.height(),
                PalettedContainerFactory.create(context.registries()), null);
        generator.createBiomes(context.randomState(), Blender.empty(), NO_STRUCTURES, chunk).join();
        net.minecraft.world.level.biome.BiomeResolver resolver = generator.getBiomeSource().createUncachedResolver(context.randomState());
        BiomeManager biomes = new BiomeManager(resolver,
                BiomeManager.obfuscateSeed(context.seed()));
        generator.buildTerrain(chunk, Blender.empty(), context.randomState(), NO_STRUCTURES, biomes, null,
                generator.getBiomeSource().possibleBiomes()).join();
        char[] cells = new char[256 * 256];
        byte[] oldBiomes = new byte[256];
        int[] surface = new int[256];
        int minY = Math.max(0, chunk.getMinY());
        int maxY = Math.min(255, chunk.getMaxY());
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int column = (lx * 16 + lz) * 256;
                for (int y = minY; y <= maxY; y++) {
                    LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
                    if (section.hasOnlyAir()) {
                        y += 15 - (y & 15);
                        continue;
                    }
                    cells[column + y] = (char) OldGeneratorFeature.cell(section.getBlockState(lx, y & 15, lz));
                }
                int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, lx, lz) + 1;
                surface[lx + lz * 16] = top;
                int qy = QuartPos.fromBlock(Math.clamp(top, chunk.getMinY(), chunk.getMaxY()));
                oldBiomes[lx + lz * 16] = (byte) OldBiomeTags.oldId(resolver.getNoiseBiome(QuartPos.fromBlock(chunkX * 16 + lx), qy,
                        QuartPos.fromBlock(chunkZ * 16 + lz)), false);
            }
        }
        return new Terrain(cells, oldBiomes, surface);
    }

    /** The result for the start chunk, computed on first use. */
    static Result result(Context context, String name, int chunkX, int chunkZ) {
        Key key = new Key(context.generator(), context.seed(), name, chunkX, chunkZ);
        CompletableFuture<Result> future = RESULTS.get(key);
        if (future != null) {
            return future.join();
        }
        CompletableFuture<Result> mine = new CompletableFuture<>();
        future = RESULTS.putIfAbsent(key, mine);
        if (future != null) {
            return future.join();
        }
        RESULT_ORDER.add(key);
        while (RESULT_ORDER.size() > MAX_RESULTS) {
            Key old = RESULT_ORDER.poll();
            if (old != null) {
                RESULTS.remove(old);
            }
        }
        Result result = Result.NONE;
        try {
            result = build(context, name, chunkX, chunkZ);
        } catch (RuntimeException e) {
            if (REPORTED.add(name)) {
                LOGGER.error("1.5.2 structure {} failed in chunk {} {}", name, chunkX, chunkZ, e);
            }
        } finally {
            mine.complete(result);
        }
        return result;
    }

    private static Result build(Context context, String name, int chunkX, int chunkZ) {
        OldTerrainGen.WorldGenerator gen = OldTerrainGen.generator(name);
        if (gen == null) {
            if (REPORTED.add(name)) {
                LOGGER.warn("Unknown 1.5.2 structure builder {}", name);
            }
            return Result.NONE;
        }
        Map<Long, char[]> before = new HashMap<>();
        OldWorld.TerrainSource source = new OldWorld.TerrainSource() {
            @Override
            public OldChunk chunk(int ocx, int ocz) {
                Terrain terrain = terrain(context, ocx, ocz);
                before.put(ChunkPos.pack(ocx, ocz), terrain.cells());
                char[] low = new char[256 * OldChunk.HEIGHT];
                for (int column = 0; column < 256; column++) {
                    System.arraycopy(terrain.cells(), column * 256, low, column * OldChunk.HEIGHT, OldChunk.HEIGHT);
                }
                return OldChunk.wide(ocx, ocz, low, terrain.biomes().clone());
            }

            @Override
            public char[] upper(int ocx, int ocz) {
                char[] cells = terrain(context, ocx, ocz).cells();
                char[] high = new char[256 * 128];
                for (int column = 0; column < 256; column++) {
                    System.arraycopy(cells, column * 256 + 128, high, column * 128, 128);
                }
                return high;
            }
        };
        long seed = context.seed();
        OldWorld world = new OldWorld(seed, source);
        world.beginChunk();
        long genSeed = seed ^ (long) name.hashCode() * 0x9E3779B97F4A7C15L;
        Random random = new Random(genSeed);
        long xMul = random.nextLong() >> 3;
        long zMul = random.nextLong() >> 3;
        random.setSeed(xMul * chunkX + zMul * chunkZ ^ genSeed);
        gen.generate(random, chunkX, chunkZ, world);

        Map<Long, ChunkChanges> chunks = new HashMap<>();
        int[] box = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        int minY = Math.max(0, context.height().getMinY());
        int maxY = Math.min(OldWorld.HEIGHT - 1, context.height().getMaxY());
        BlockState air = Blocks.AIR.defaultBlockState();
        for (Map.Entry<Long, char[]> entry : before.entrySet()) {
            int ocx = ChunkPos.getX(entry.getKey());
            int ocz = ChunkPos.getZ(entry.getKey());
            char[] old = entry.getValue();
            ChunkChanges changes = null;
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int x = ocx * 16 + lx;
                    int z = ocz * 16 + lz;
                    int column = (lx * 16 + lz) * 256;
                    for (int y = minY; y <= maxY; y++) {
                        int id = world.getBlockId(x, y, z);
                        int cell = id << 4 | world.getBlockMetadata(x, y, z);
                        if (cell == old[column + y]) {
                            continue;
                        }
                        BlockState state = id == 0 ? air : OldBlockStates.get(id, cell & 15);
                        state = OldGeneratorFeature.shape(world, state, x, y, z);
                        if (changes == null) {
                            changes = chunks.computeIfAbsent(entry.getKey(), k -> new ChunkChanges());
                        }
                        changes.positions.add(new BlockPos(x, y, z));
                        changes.states.add(state);
                        changes.tiles.add(world.getTile(x, y, z));
                        grow(box, x, y, z);
                    }
                }
            }
            for (OldEntity entity : world.entities(ocx, ocz)) {
                int x = (int) Math.floor(entity.x());
                int y = (int) Math.floor(entity.y());
                int z = (int) Math.floor(entity.z());
                chunks.computeIfAbsent(ChunkPos.pack(x >> 4, z >> 4), k -> new ChunkChanges()).entities.add(entity);
                grow(box, x, Math.clamp(y, minY, maxY), z);
            }
        }
        if (box[0] == Integer.MAX_VALUE) {
            return Result.NONE;
        }
        BoundingBox bounds = new BoundingBox(box[0], box[1], box[2], box[3], box[4], box[5]);
        // located at the middle of the start chunk, on its surface (the biome there decides whether it may stand)
        Terrain start = terrain(context, chunkX, chunkZ);
        BlockPos locate = new BlockPos(chunkX * 16 + 8, start.surface()[8 + 8 * 16], chunkZ * 16 + 8);
        return new Result(bounds, locate, chunks);
    }

    private static void grow(int[] box, int x, int y, int z) {
        box[0] = Math.min(box[0], x);
        box[1] = Math.min(box[1], y);
        box[2] = Math.min(box[2], z);
        box[3] = Math.max(box[3], x);
        box[4] = Math.max(box[4], y);
        box[5] = Math.max(box[5], z);
    }
}
