package dimdoors.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dimdoors.DimDoors;
import dimdoors.entity.Monolith;
import dimdoors.gen.LimboTerrain;
import dimdoors.gen.RiftGen;
import hexxit.worldgen152.core.OldChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
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
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * LimboGenerator of Dimensional Doors 1.4.1: the terrain of {@link LimboTerrain}, nothing populated, and the
 * monoliths CommonTickHandler put into each new chunk.
 */
public final class LimboChunkGenerator extends ChunkGenerator {
    public static final MapCodec<LimboChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource))
            .apply(instance, LimboChunkGenerator::new));
    private static final LimboTerrain TERRAIN = new LimboTerrain();

    private final Map<Long, byte[]> cache = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
            return this.size() > 256;
        }
    };

    public LimboChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private byte[] blocks(int chunkX, int chunkZ) {
        long key = ChunkPos.pack(chunkX, chunkZ);
        synchronized (this.cache) {
            byte[] cached = this.cache.get(key);
            if (cached != null) {
                return cached;
            }
        }
        byte[] generated = TERRAIN.generate(chunkX, chunkZ);
        synchronized (this.cache) {
            this.cache.put(key, generated);
        }
        return generated;
    }

    private static BlockState state(int id) {
        if (id == RiftGen.UNRAVELED_FABRIC) {
            return DimDoors.UNRAVELED_FABRIC.get().defaultBlockState();
        }
        if (id == RiftGen.ETERNAL_FABRIC) {
            return DimDoors.ETERNAL_FABRIC.get().defaultBlockState();
        }
        return null;
    }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager,
                                                      BiomeManager biomeManager, WorldGenRegion region, Set<Holder<Biome>> biomes) {
        ChunkPos pos = chunk.getPos();
        byte[] blocks = this.blocks(pos.x(), pos.z());
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int base = (x * 16 + z) * OldChunk.HEIGHT;
                for (int y = 0; y < OldChunk.HEIGHT; y++) {
                    BlockState state = state(blocks[base + y] & 0xFF);
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

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
    }

    @Override
    public void createStructures(RegistryAccess registries, ChunkGeneratorStructureState structureState, StructureManager structureManager,
                                 ChunkAccess chunk, StructureTemplateManager templates, ResourceKey<Level> level) {
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
    }

    /** The monoliths of a new chunk (the original placed them at the end of the tick the chunk was made in). */
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        ChunkPos pos = region.getCenter();
        Monolith.placeInLimboChunk(region, pos.x(), pos.z(), RandomSource.create(region.getSeed() ^ pos.pack()));
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        byte[] blocks = this.blocks(x >> 4, z >> 4);
        int base = ((x & 15) * 16 + (z & 15)) * OldChunk.HEIGHT;
        for (int y = OldChunk.HEIGHT - 1; y >= 0; y--) {
            BlockState state = state(blocks[base + y] & 0xFF);
            if (state != null && type.isOpaque().test(state)) {
                return y + 1;
            }
        }
        return level.getMinY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        byte[] blocks = this.blocks(x >> 4, z >> 4);
        int base = ((x & 15) * 16 + (z & 15)) * OldChunk.HEIGHT;
        BlockState[] column = new BlockState[level.getHeight()];
        for (int i = 0; i < column.length; i++) {
            int y = level.getMinY() + i;
            BlockState state = y >= 0 && y < OldChunk.HEIGHT ? state(blocks[base + y] & 0xFF) : null;
            column[i] = state != null ? state : Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(level.getMinY(), column);
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos pos, SamplerContext context) {
        lines.add("Limbo of 1.5.2");
    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }
}
