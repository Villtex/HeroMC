package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBlocks;

/**
 * BlockTFFireJet. Metadata 0 smoker (smokes forever), 1/2 encased smoker (smokes while powered), 8/9/10 fire jet
 * idle/popping/flame (an idle jet takes a still lava source below it on a random tick), 11/12/13 encased fire jet
 * (redstone starts it). Popping lasts 80 ticks, the flame 60, then the jet is idle again
 * ({@link FireJetBlockEntity}, TileEntityTFPoppingJet / TileEntityTFFlameJet).
 */
public class FireJetBlock extends Block implements EntityBlock {
    public enum Kind {
        SMOKER, FIRE_JET, ENCASED_SMOKER, ENCASED_FIRE_JET;

        boolean jet() {
            return this == FIRE_JET || this == ENCASED_FIRE_JET;
        }
    }

    public enum Phase implements StringRepresentable {
        IDLE("idle"), POPPING("popping"), FLAME("flame");

        private final String name;

        Phase(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<Phase> PHASE = EnumProperty.create("state", Phase.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    /** The kind of the block under construction (createBlockStateDefinition runs inside the Block constructor). */
    private static final ThreadLocal<Kind> CONSTRUCTING = new ThreadLocal<>();

    private final Kind kind;

    public FireJetBlock(Kind kind, Properties properties) {
        super(remember(kind, properties));
        this.kind = kind;
        CONSTRUCTING.remove();
        if (kind.jet()) {
            this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, Phase.IDLE));
        } else if (kind == Kind.ENCASED_SMOKER) {
            this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
        }
    }

    private static Properties remember(Kind kind, Properties properties) {
        CONSTRUCTING.set(kind);
        return properties;
    }

    public Kind kind() {
        return this.kind;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        Kind kind = CONSTRUCTING.get();
        if (kind != null && kind.jet()) {
            builder.add(PHASE);
        } else if (kind == Kind.ENCASED_SMOKER) {
            builder.add(ACTIVE);
        }
    }

    /** The state of a 1.5.2 metadata value (unused values are an idle fire jet, as they looked). */
    public static BlockState fromMeta(int meta) {
        return switch (meta) {
            case 0 -> TFBlocks.SMOKER.get().defaultBlockState();
            case 1, 2 -> TFBlocks.ENCASED_SMOKER.get().defaultBlockState().setValue(ACTIVE, meta == 2);
            case 9 -> TFBlocks.FIRE_JET.get().defaultBlockState().setValue(PHASE, Phase.POPPING);
            case 10 -> TFBlocks.FIRE_JET.get().defaultBlockState().setValue(PHASE, Phase.FLAME);
            case 11 -> TFBlocks.ENCASED_FIRE_JET.get().defaultBlockState();
            case 12 -> TFBlocks.ENCASED_FIRE_JET.get().defaultBlockState().setValue(PHASE, Phase.POPPING);
            case 13 -> TFBlocks.ENCASED_FIRE_JET.get().defaultBlockState().setValue(PHASE, Phase.FLAME);
            default -> TFBlocks.FIRE_JET.get().defaultBlockState();
        };
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return this.kind == Kind.FIRE_JET && state.getValue(PHASE) == Phase.IDLE;
    }

    /** updateTick: an idle jet takes a still lava source below it (or next to that) and starts popping. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.kind != Kind.FIRE_JET || state.getValue(PHASE) != Phase.IDLE) {
            return;
        }
        BlockPos lava = findLavaAround(level, pos.below(), random);
        if (isLava(level, lava)) {
            level.setBlock(lava, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(pos, state.setValue(PHASE, Phase.POPPING), Block.UPDATE_CLIENTS);
        }
    }

    private static BlockPos findLavaAround(Level level, BlockPos pos, RandomSource random) {
        if (isLava(level, pos)) {
            return pos;
        }
        for (int i = 0; i < 3; i++) {
            BlockPos r = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
            if (isLava(level, r)) {
                return r;
            }
        }
        return pos;
    }

    /** Material.lava with metadata 0: a lava source. */
    private static boolean isLava(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.LAVA) && state.getFluidState().isSource();
    }

    /** onNeighborBlockChange: the encased smoker follows the redstone, the encased jet starts popping. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        BlockState next = null;
        if (this.kind == Kind.ENCASED_SMOKER && state.getValue(ACTIVE) != powered) {
            next = state.setValue(ACTIVE, powered);
        } else if (this.kind == Kind.ENCASED_FIRE_JET && powered && state.getValue(PHASE) == Phase.IDLE) {
            next = state.setValue(PHASE, Phase.POPPING);
        }
        if (next != null) {
            level.setBlock(pos, next, Block.UPDATE_ALL);
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.6F);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FireJetBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TFBlocks.FIRE_JET_ENTITY.get() ? (l, p, s, be) -> ((FireJetBlockEntity) be).tick(l, p, s) : null;
    }
}
