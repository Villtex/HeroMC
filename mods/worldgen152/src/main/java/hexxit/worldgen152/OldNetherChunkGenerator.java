package hexxit.worldgen152;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.nether.NetherTerrain;
import hexxit.worldgen152.core.populate.OldDecoration;
import hexxit.worldgen152.core.structure.MapGenNetherBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Chunk generator for the Nether of Hexxit 1.0.11: the 1.5.2 Nether as Natura's "Override Nether" made it
 * (netherrack, tainted soil and heat sand in 128 blocks from y=0, lava sea below 32, nether fortresses).
 * {@link NetherTerrain} shapes the terrain and {@link OldDecoration#nether} populates it; this class writes
 * the results into the chunk, as {@link OldChunkGenerator} does for the overworld.
 */
public final class OldNetherChunkGenerator extends ChunkGenerator {
    public static final MapCodec<OldNetherChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource))
            .apply(instance, OldNetherChunkGenerator::new));

    private static final int LAVA_LEVEL = 32;
    private static final int CACHE_SIZE = 1024;

    /** MapGenNetherBridge.spawnList. A skeleton of 1.5.2 that spawned in the Nether became a wither skeleton four times out of five. */
    private static final WeightedList<MobSpawnSettings.SpawnerData> FORTRESS_MONSTERS = WeightedList.<MobSpawnSettings.SpawnerData>builder()
            .add(new MobSpawnSettings.SpawnerData(EntityTypes.BLAZE, UniformInt.of(2, 3)), 10)
            .add(new MobSpawnSettings.SpawnerData(EntityTypes.ZOMBIFIED_PIGLIN, ConstantInt.of(4)), 5)
            .add(new MobSpawnSettings.SpawnerData(EntityTypes.WITHER_SKELETON, ConstantInt.of(4)), 8)
            .add(new MobSpawnSettings.SpawnerData(EntityTypes.SKELETON, ConstantInt.of(4)), 2)
            .add(new MobSpawnSettings.SpawnerData(EntityTypes.MAGMA_CUBE, ConstantInt.of(4)), 3)
            .build();

    /** Everything that depends on the world seed. */
    private record World(long seed, NetherTerrain terrain, OldDecoration decoration, MapGenNetherBridge fortresses) {
    }

    private volatile World world;
    private final Map<Long, OldChunk> chunkCache = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, OldChunk> eldest) {
            return this.size() > CACHE_SIZE;
        }
    };

    public OldNetherChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /** Fixes the world seed. Called once per server start, before chunks are generated. */
    public void bind(long seed) {
        World current = this.world;
        if (current == null || current.seed != seed) {
            synchronized (this.chunkCache) {
                current = this.world;
                if (current == null || current.seed != seed) {
                    this.chunkCache.clear();
                    this.world = new World(seed, new NetherTerrain(seed), OldDecoration.nether(seed, this::oldChunk), new MapGenNetherBridge(seed));
                }
            }
        }
    }

    private World world() {
        World current = this.world;
        if (current == null) {
            throw new IllegalStateException("1.5.2 Nether used before the world seed was known");
        }
        return current;
    }

    private OldChunk oldChunk(int chunkX, int chunkZ) {
        World current = this.world();
        long key = ChunkPos.pack(chunkX, chunkZ);
        synchronized (this.chunkCache) {
            OldChunk cached = this.chunkCache.get(key);
            if (cached != null) {
                return cached;
            }
        }
        OldChunk generated = current.terrain.generate(chunkX, chunkZ);
        synchronized (this.chunkCache) {
            this.chunkCache.put(key, generated);
        }
        return generated;
    }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState,
                                                      StructureManager structureManager, BiomeManager biomeManager,
                                                      WorldGenRegion region, Set<Holder<Biome>> biomes) {
        this.bind(region.getSeed());
        ChunkPos pos = chunk.getPos();
        byte[] blocks = this.oldChunk(pos.x(), pos.z()).blocks();
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

    /** The feature step: what Hexxit did when it populated Nether chunks. */
    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        this.bind(level.getSeed());
        ChunkPos pos = chunk.getPos();
        OldChunk bare = this.oldChunk(pos.x(), pos.z());
        OldChunkWriter.write(level, chunk, bare, this.world().decoration.chunk(pos.x(), pos.z()));
        super.applyBiomeDecoration(level, chunk, structureManager);
    }

    /**
     * No structures of today: the biome is tagged as a Nether biome (for the mob spawns of mods), which would
     * otherwise bring the fortresses, bastions and ruined portals of 26.3 on top of the fortresses of 1.5.2.
     */
    @Override
    public void createStructures(RegistryAccess registries, ChunkGeneratorStructureState structureState, StructureManager structureManager,
                                 ChunkAccess chunk, StructureTemplateManager templates, ResourceKey<Level> level) {
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
    }

    /** In a fortress its own monsters spawn (ChunkProviderHell.getPossibleCreatures). */
    @Override
    public WeightedList<MobSpawnSettings.SpawnerData> getMobsAt(Level level, StructureManager structureManager, MobCategory category, BlockPos pos) {
        World current = this.world;
        if (category == MobCategory.MONSTER && current != null && current.fortresses.hasStructureAt(pos.getX(), pos.getY(), pos.getZ())) {
            return FORTRESS_MONSTERS;
        }
        return super.getMobsAt(level, structureManager, category, pos);
    }

    /** /locate finds the fortresses of 1.5.2; other structures of today do not exist here. */
    @Override
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel level, HolderSet<Structure> structures, BlockPos pos,
                                                                     int maxSearchRadius, boolean createReference) {
        Holder<Structure> fortress = null;
        for (Holder<Structure> holder : structures) {
            if (holder.is(BuiltinStructures.FORTRESS)) {
                fortress = holder;
            }
        }
        if (fortress == null) {
            return null;
        }
        this.bind(level.getSeed());
        // a fresh instance: the one for spawning is asked from the server thread all the time
        MapGenNetherBridge fortresses = new MapGenNetherBridge(level.getSeed());
        int reach = Math.max(Math.abs(pos.getX() >> 4), Math.abs(pos.getZ() >> 4)) + 64;
        BlockPos nearest = null;
        for (MapGenNetherBridge.Found found : fortresses.find(reach)) {
            BlockPos candidate = new BlockPos(found.x(), found.y(), found.z());
            if (nearest == null || candidate.distSqr(pos) < nearest.distSqr(pos)) {
                nearest = candidate;
            }
        }
        return nearest == null ? null : Pair.of(nearest, fortress);
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

    /** 1.5.2 spawned no mobs with a new Nether chunk. */
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos pos, SamplerContext context) {
        World current = this.world;
        if (current != null) {
            lines.add("1.5.2 Nether" + (current.fortresses.hasStructureAt(pos.getX(), pos.getY(), pos.getZ()) ? ", in a fortress" : ""));
        }
    }

    @Override
    public int getGenDepth() {
        return 128;
    }

    @Override
    public int getSeaLevel() {
        return LAVA_LEVEL;
    }

    @Override
    public int getMinY() {
        return 0;
    }
}
