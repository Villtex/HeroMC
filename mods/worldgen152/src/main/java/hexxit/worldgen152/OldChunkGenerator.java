package hexxit.worldgen152;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hexxit.worldgen152.core.BiomeLayout;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.OldTerrain;
import hexxit.worldgen152.core.populate.OldDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

import com.mojang.datafixers.util.Pair;
import hexxit.worldgen152.core.structure.OldStructures;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Chunk generator that lays down the 1.5.2 overworld: 128 blocks of terrain from y=0, sea level 63.
 * All shaping (surface layers, caves, ravines) happens in {@link OldTerrain} and the population of chunks
 * (structures, lakes, ores, trees, plants, snow) in {@link OldDecoration}; this class only writes the results into the
 * chunk - bare terrain in the noise step, population in the feature step - and answers height queries from
 * the bare terrain.
 */
public final class OldChunkGenerator extends ChunkGenerator {
    public static final MapCodec<OldChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource))
            .apply(instance, OldChunkGenerator::new));

    private static final int SEA_LEVEL = 63;
    private static final int CACHE_SIZE = 1024;

    private final OldBiomeSource oldBiomes;
    private volatile OldTerrain terrain;
    private volatile OldDecoration decoration;
    private final Map<Long, OldChunk> chunkCache = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, OldChunk> eldest) {
            return this.size() > CACHE_SIZE;
        }
    };

    public OldChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
        if (!(biomeSource instanceof OldBiomeSource source)) {
            throw new IllegalArgumentException("worldgen152:overworld needs the worldgen152:overworld biome source");
        }
        this.oldBiomes = source;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private OldTerrain terrain() {
        BiomeLayout layout = this.oldBiomes.layout();
        OldTerrain current = this.terrain;
        if (current == null || current.layout() != layout) {
            synchronized (this.chunkCache) {
                current = this.terrain;
                if (current == null || current.layout() != layout) {
                    this.chunkCache.clear();
                    current = new OldTerrain(layout);
                    this.decoration = new OldDecoration(layout.seed(), this::oldChunk, layout);
                    this.terrain = current;
                }
            }
        }
        return current;
    }

    private OldChunk oldChunk(int chunkX, int chunkZ) {
        OldTerrain generator = this.terrain();
        long key = ChunkPos.pack(chunkX, chunkZ);
        synchronized (this.chunkCache) {
            OldChunk cached = this.chunkCache.get(key);
            if (cached != null) {
                return cached;
            }
        }
        OldChunk generated = generator.generate(chunkX, chunkZ);
        synchronized (this.chunkCache) {
            this.chunkCache.put(key, generated);
        }
        return generated;
    }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState,
                                                      StructureManager structureManager, BiomeManager biomeManager,
                                                      WorldGenRegion region, Set<Holder<Biome>> biomes) {
        this.oldBiomes.bind(region.getSeed());
        ChunkPos pos = chunk.getPos();
        OldChunk old = this.oldChunk(pos.x(), pos.z());
        byte[] blocks = old.blocks();
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = chunk.getMinY();
        int maxY = Math.min(OldChunk.HEIGHT, minY + chunk.getHeight());

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int base = (x * 16 + z) * OldChunk.HEIGHT;
                for (int y = Math.max(0, minY); y < maxY; y++) {
                    BlockState state = OldBlockStates.get(blocks[base + y] & 0xFF);
                    if (state != null) {
                        chunk.setBlockState(cursor.set(x, y, z), state);
                        oceanFloor.update(x, y, z, state);
                        worldSurface.update(x, y, z, state);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    /** The feature step: what 1.5.2 did when it populated chunks, then whatever today's biomes and structures add. */
    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        this.oldBiomes.bind(level.getSeed());
        ChunkPos pos = chunk.getPos();
        OldChunk bare = this.oldChunk(pos.x(), pos.z());
        OldChunkWriter.write(level, chunk, bare, this.decoration.chunk(pos.x(), pos.z()));
        super.applyBiomeDecoration(level, chunk, structureManager);
    }

    /** The world spawn of 1.5.2 for this seed, on the ground. */
    public BlockPos spawnPoint() {
        BiomeLayout layout = this.oldBiomes.layout();
        this.terrain();
        int[] spawn = OldStructures.spawnPoint(layout.seed(), layout, this::oldChunk);
        OldChunk old = this.oldChunk(spawn[0] >> 4, spawn[1] >> 4);
        int y = OldChunk.HEIGHT - 1;
        while (y > 0 && old.blockId(spawn[0] & 15, y, spawn[1] & 15) == 0) {
            y--;
        }
        return new BlockPos(spawn[0], y + 1, spawn[1]);
    }

    /** Eyes of ender and /locate find the strongholds of 1.5.2; other structures of today do not exist here. */
    @Override
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel level, HolderSet<Structure> structures, BlockPos pos,
                                                                     int maxSearchRadius, boolean createReference) {
        Holder<Structure> stronghold = null;
        for (Holder<Structure> holder : structures) {
            if (holder.is(BuiltinStructures.STRONGHOLD)) {
                stronghold = holder;
            }
        }
        if (stronghold == null) {
            return null;
        }
        this.oldBiomes.bind(level.getSeed());
        BiomeLayout layout = this.oldBiomes.layout();
        BlockPos nearest = null;
        for (int[] room : new OldStructures(layout.seed(), layout).strongholdPortalRooms()) {
            BlockPos candidate = new BlockPos(room[0], room[1], room[2]);
            if (nearest == null || candidate.distSqr(pos) < nearest.distSqr(pos)) {
                nearest = candidate;
            }
        }
        return nearest == null ? null : Pair.of(nearest, stronghold);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        OldChunk old = this.oldChunk(x >> 4, z >> 4);
        int base = ((x & 15) * 16 + (z & 15)) * OldChunk.HEIGHT;
        for (int y = OldChunk.HEIGHT - 1; y >= 0; y--) {
            BlockState state = OldBlockStates.get(old.blocks()[base + y] & 0xFF);
            if (state != null && type.isOpaque().test(state)) {
                return y + 1;
            }
        }
        return level.getMinY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        OldChunk old = this.oldChunk(x >> 4, z >> 4);
        int base = ((x & 15) * 16 + (z & 15)) * OldChunk.HEIGHT;
        BlockState[] column = new BlockState[level.getHeight()];
        for (int i = 0; i < column.length; i++) {
            int y = level.getMinY() + i;
            BlockState state = y >= 0 && y < OldChunk.HEIGHT ? OldBlockStates.get(old.blocks()[base + y] & 0xFF) : null;
            column[i] = state != null ? state : Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(level.getMinY(), column);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        ChunkPos center = region.getCenter();
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        random.setDecorationSeed(region.getSeed(), center.getMinBlockX(), center.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(region, center.getWorldPosition().atY(region.getMaxY()), center, random);
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos pos, SamplerContext context) {
        BiomeLayout layout = this.oldBiomes.layout();
        lines.add("1.5.2 biome: " + layout.biomeAt(pos.getX(), pos.getZ()).name + " (land biomes: " + OldBiomes.overworld().length + ")");
    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return 0;
    }
}
