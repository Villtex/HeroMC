package twilightforest.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldChunkWriter;
import hexxit.worldgen152.core.OldChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
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
import twilightforest.gen.TFFeature;
import twilightforest.gen.TFTerrain;
import twilightforest.gen.TFWorld;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Chunk generator of the Twilight Forest (ChunkProviderTwilightForest): {@link twilightforest.gen.TFTerrain} shapes the
 * land, {@link twilightforest.gen.TFPopulator} populates it; this class writes both into the chunk.
 */
public final class TFChunkGenerator extends ChunkGenerator {
    public static final MapCodec<TFChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource))
            .apply(instance, TFChunkGenerator::new));

    private volatile TFWorld world;

    public TFChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /** Fixes the world seed. Called once per server start, before chunks are generated. */
    public synchronized void bind(long seed) {
        TFWorld current = this.world;
        if (current == null || current.seed() != seed) {
            current = new TFWorld(seed);
            if (this.biomeSource instanceof TFBiomeSource source) {
                source.bind(current.layout());
            }
            this.world = current;
        }
    }

    public TFWorld world() {
        TFWorld current = this.world;
        if (current == null) {
            throw new IllegalStateException("Twilight Forest used before the world seed was known");
        }
        return current;
    }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState,
                                                      StructureManager structureManager, BiomeManager biomeManager,
                                                      WorldGenRegion region, Set<Holder<Biome>> biomes) {
        this.bind(region.getSeed());
        ChunkPos pos = chunk.getPos();
        OldChunk old = this.world().terrain(pos.x(), pos.z());
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < OldChunk.HEIGHT; y++) {
                    BlockState state = state(old.cell(x, y, z));
                    if (state != null) {
                        chunk.setBlockState(cursor.set(x, y, z), state);
                        oceanFloor.update(x, y, z, state);
                        worldSurface.update(x, y, z, state);
                        if (old.blockId(x, y, z) == TFTerrain.WATER_MOVING) {
                            // the cave water below y 10 was flowing water: let it run once the chunk is in the world
                            chunk.markPosForPostProcessing(cursor.offset(pos.getMinBlockX(), 0, pos.getMinBlockZ()));
                        }
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private static BlockState state(int cell) {
        return cell == 0 ? null : OldBlockStates.get(cell >> 4, cell & 15);
    }

    /** The feature step: the population of the Twilight Forest chunk. */
    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        this.bind(level.getSeed());
        ChunkPos pos = chunk.getPos();
        OldChunkWriter.write(level, chunk, this.world().terrain(pos.x(), pos.z()), this.world().populated(pos.x(), pos.z()));
    }

    /** No structures of today; the Twilight Forest's own follow in Teil 24d/24e. */
    @Override
    public void createStructures(RegistryAccess registries, ChunkGeneratorStructureState structureState, StructureManager structureManager,
                                 ChunkAccess chunk, StructureTemplateManager templates, ResourceKey<Level> level) {
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        OldChunk old = this.world().terrain(x >> 4, z >> 4);
        for (int y = OldChunk.HEIGHT - 1; y >= 0; y--) {
            BlockState state = state(old.cell(x & 15, y, z & 15));
            if (state != null && type.isOpaque().test(state)) {
                return y + 1;
            }
        }
        return level.getMinY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        OldChunk old = this.world().terrain(x >> 4, z >> 4);
        BlockState[] column = new BlockState[level.getHeight()];
        for (int i = 0; i < column.length; i++) {
            int y = level.getMinY() + i;
            BlockState state = y >= 0 && y < OldChunk.HEIGHT ? state(old.cell(x & 15, y, z & 15)) : null;
            column[i] = state != null ? state : Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(level.getMinY(), column);
    }

    /** Animals of a new chunk come with Teil 24c (SpawnerAnimals.performWorldGenSpawning with the biome lists). */
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos pos, SamplerContext context) {
        TFWorld current = this.world;
        if (current != null) {
            TFFeature feature = TFFeature.getNearestFeature(pos.getX() >> 4, pos.getZ() >> 4, current.layout());
            lines.add("Twilight Forest of 1.5.2, " + current.layout().biomeAt(pos.getX(), pos.getZ()).name
                    + (feature != TFFeature.NOTHING ? ", near " + feature : ""));
        }
    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return TFTerrain.SEALEVEL;
    }

    @Override
    public int getMinY() {
        return 0;
    }
}
