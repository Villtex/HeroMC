package hexxit.worldgen152;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hexxit.worldgen152.core.populate.OldEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A 1.5.2 structure (a Battle Tower, a dungeon of Better Dungeons, a pyramid of Project Zulu) as a structure of
 * today, for the world type Hexxit Integrated. Where it may start comes from its structure set (spacing and
 * separation like the rarity of the original), so {@code /locate}, explorer maps and the spacing rules of other
 * structure mods work with it. The builder named {@code generator} runs once for the start chunk on the terrain of
 * today ({@link OldStructureBuild}); the start covers exactly what it built, and every chunk the box touches writes
 * its part when it is decorated. Blocks of today are replaced as by the feature bridge
 * ({@link OldGeneratorFeature#replaceable}).
 *
 * <p>{@code avoid_radius}: no start within that many chunks of a possible start of another surface structure (vanilla
 * or of another mod, not of these) whose biome fits there. 0 turns the check off.
 */
public final class OldStructure extends Structure {
    public static final MapCodec<OldStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            Codec.STRING.fieldOf("generator").forGetter(s -> s.generator),
            Codec.intRange(0, 8).optionalFieldOf("avoid_radius", 3).forGetter(s -> s.avoidRadius))
            .apply(instance, OldStructure::new));

    /** Set by worldgen152 when it registers the type. */
    static Supplier<StructureType<OldStructure>> TYPE;
    static Supplier<StructurePieceType> PIECE;

    /** The structure state of each chunk generator in use (for the check against other structures). */
    private static final Map<ChunkGenerator, ChunkGeneratorStructureState> STATES = new ConcurrentHashMap<>();

    final String generator;
    final int avoidRadius;

    OldStructure(StructureSettings settings, String generator, int avoidRadius) {
        super(settings);
        this.generator = generator;
        this.avoidRadius = avoidRadius;
    }

    static void levelLoaded(ServerLevel level) {
        STATES.put(level.getChunkSource().getGenerator(), level.getChunkSource().getGeneratorState());
    }

    static void serverStopped() {
        STATES.clear();
        OldStructureBuild.clear();
    }

    @Override
    public StructureType<?> type() {
        return TYPE.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (context.chunkGenerator() instanceof OldChunkGenerator || context.chunkGenerator() instanceof OldNetherChunkGenerator) {
            return Optional.empty();
        }
        if (!IntegratedConfig.enabled(this.generator)) {
            return Optional.empty();
        }
        ChunkPos pos = context.chunkPos();
        if (!hexxit.worldgen152.core.populate.OldTerrainGen.mayStart(this.generator, context.seed(), pos.x(), pos.z())) {
            return Optional.empty();
        }
        // cheap first: may a fitting biome be anywhere in the column at all (as vanilla structures check)
        if (!context.couldValidBiomeExistOnTopOfChunkCenter()) {
            return Optional.empty();
        }
        OldStructureBuild.Context build = new OldStructureBuild.Context(context.chunkGenerator(), context.randomState(), context.heightAccessor(),
                context.registryAccess(), context.seed());
        // the biome on the surface in the middle of the start chunk, before anything is built
        int surface = OldStructureBuild.terrain(build, pos.x(), pos.z()).surface()[8 + 8 * 16];
        BlockPos middle = new BlockPos(pos.getMiddleBlockX(), surface, pos.getMiddleBlockZ());
        if (!context.validBiome().test(context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(middle.getX()),
                QuartPos.fromBlock(middle.getY()), QuartPos.fromBlock(middle.getZ())))) {
            return Optional.empty();
        }
        if (this.avoidRadius > 0 && this.nearOtherStructure(context, pos)) {
            return Optional.empty();
        }
        OldStructureBuild.Result result = OldStructureBuild.result(build, this.generator, pos.x(), pos.z());
        if (result.box() == null) {
            return Optional.empty();
        }
        return Optional.of(new GenerationStub(middle, builder -> builder.addPiece(new Piece(this.generator, pos.x(), pos.z(), result.box()))));
    }

    /** Whether another surface structure may start within {@link #avoidRadius} chunks (its placement and its biome fit). */
    private boolean nearOtherStructure(GenerationContext context, ChunkPos pos) {
        ChunkGeneratorStructureState state = STATES.get(context.chunkGenerator());
        if (state == null) {
            return false;
        }
        int r = this.avoidRadius;
        for (Holder<StructureSet> set : state.possibleStructureSets()) {
            boolean surface = false;
            boolean own = false;
            for (StructureSet.StructureSelectionEntry entry : set.value().structures()) {
                Structure structure = entry.structure().value();
                own |= structure instanceof OldStructure;
                surface |= structure.step() == GenerationStep.Decoration.SURFACE_STRUCTURES;
            }
            if (own || !surface) {
                continue;
            }
            StructurePlacement placement = set.value().placement();
            if (placement instanceof RandomSpreadStructurePlacement spread) {
                Set<Long> seen = new HashSet<>();
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        ChunkPos candidate = spread.getPotentialStructureChunk(state.getLevelSeed(), pos.x() + dx, pos.z() + dz);
                        if (Math.abs(candidate.x() - pos.x()) > r || Math.abs(candidate.z() - pos.z()) > r || !seen.add(candidate.pack())) {
                            continue;
                        }
                        if (spread.isStructureChunk(state, candidate.x(), candidate.z()) && fits(context, set.value(), candidate)) {
                            return true;
                        }
                    }
                }
            } else if (state.hasStructureChunkInRange(set, pos.x(), pos.z(), r)) {
                return true;
            }
        }
        return false;
    }

    /** Whether one of the structures of the set may stand in the biome at the middle of the chunk (at sea level). */
    private static boolean fits(GenerationContext context, StructureSet set, ChunkPos chunk) {
        Holder<Biome> biome = context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(chunk.getMiddleBlockX()), QuartPos.fromBlock(64),
                QuartPos.fromBlock(chunk.getMiddleBlockZ()));
        for (StructureSet.StructureSelectionEntry entry : set.structures()) {
            if (entry.structure().value().biomes().contains(biome)) {
                return true;
            }
        }
        return false;
    }

    /** The one piece of an old structure: the box of everything the builder built. */
    public static final class Piece extends StructurePiece {
        private final String generator;
        private final int startX;
        private final int startZ;

        Piece(String generator, int startX, int startZ, BoundingBox box) {
            super(PIECE.get(), 0, box);
            this.generator = generator;
            this.startX = startX;
            this.startZ = startZ;
        }

        Piece(CompoundTag tag) {
            super(PIECE.get(), tag);
            this.generator = tag.getStringOr("Generator", "");
            this.startX = tag.getIntOr("StartX", 0);
            this.startZ = tag.getIntOr("StartZ", 0);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putString("Generator", this.generator);
            tag.putInt("StartX", this.startX);
            tag.putInt("StartZ", this.startZ);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random,
                                BoundingBox chunkBox, ChunkPos chunkPos, BlockPos referencePos) {
            ServerLevel serverLevel = level.getLevel();
            OldStructureBuild.Context build = new OldStructureBuild.Context(chunkGenerator, serverLevel.getChunkSource().randomState(), serverLevel,
                    level.registryAccess(), level.getSeed());
            OldStructureBuild.Result result = OldStructureBuild.result(build, this.generator, this.startX, this.startZ);
            OldStructureBuild.ChunkChanges changes = result.chunks().get(chunkPos.pack());
            if (changes == null) {
                return;
            }
            ChunkAccess chunk = level.getChunk(chunkPos.x(), chunkPos.z());
            for (int i = 0; i < changes.positions.size(); i++) {
                BlockPos at = changes.positions.get(i);
                if (!chunkBox.isInside(at)) {
                    continue;
                }
                BlockState real = level.getBlockState(at);
                if (!OldGeneratorFeature.replaceable(real)) {
                    continue;
                }
                BlockState state = changes.states.get(i);
                if (real.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
                    state = OldGeneratorFeature.deepslate(state);
                }
                level.setBlock(at, state, Block.UPDATE_CLIENTS);
                if (!state.getFluidState().isEmpty() || OldChunkWriter.connects(state)) {
                    chunk.markPosForPostProcessing(at);
                }
                if (state.getBlock() instanceof EntityBlock && changes.tiles.get(i) != null) {
                    BlockEntity blockEntity = level.getBlockEntity(at);
                    if (blockEntity != null) {
                        OldChunkWriter.fillTile(blockEntity, changes.tiles.get(i), level.registryAccess(), at);
                        blockEntity.setChanged();
                    }
                }
            }
            for (OldEntity old : changes.entities) {
                Entity entity = OldChunkWriter.entity(old, serverLevel, level.registryAccess(), 0, 0);
                if (entity != null) {
                    if (!(entity instanceof HangingEntity)) {
                        entity.snapTo(old.x(), old.y(), old.z(), old instanceof OldEntity.Mob mob ? mob.yaw() : 0.0F, 0.0F);
                    }
                    level.addFreshEntity(entity);
                }
            }
        }
    }
}
