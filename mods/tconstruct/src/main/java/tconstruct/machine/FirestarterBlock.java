package tconstruct.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/**
 * RedstoneMachine 1, the igniter (FirestarterLogic): faces the player who put it down. On every neighbour change it
 * takes the redstone state (powered, or redstone dust next to it with power above 0) and one tick later lights fire
 * in front of it (only into air) or puts that fire out. Its top burns forever (isFireSource: tag infiniburn).
 */
public class FirestarterBlock extends DirectionalBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public FirestarterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    /** setDirection(yaw, pitch): towards the player. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, facing(context));
    }

    /** IFacingLogic.setDirection: pitch over 45 up, under -45 down, else the horizontal side towards the player. */
    public static Direction facing(BlockPlaceContext context) {
        float pitch = context.getPlayer() == null ? 0.0F : context.getPlayer().getXRot();
        if (pitch > 45.0F) {
            return Direction.UP;
        }
        if (pitch < -45.0F) {
            return Direction.DOWN;
        }
        return context.getHorizontalDirection().getOpposite();
    }

    /** RedstoneMachine.activeNearbyRedstone: redstone dust with power beside it (not above or below) counts too. */
    public static boolean powered(Level level, BlockPos pos) {
        if (level.hasNeighborSignal(pos)) {
            return true;
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockState wire = level.getBlockState(pos.relative(d));
            if (wire.is(Blocks.REDSTONE_WIRE) && wire.getValue(BlockStateProperties.POWER) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moving) {
        if (!level.isClientSide()) {
            boolean active = powered(level, pos);
            if (active != state.getValue(ACTIVE)) {
                level.setBlock(pos, state.setValue(ACTIVE, active), 2);
            }
            level.scheduleTick(pos, this, 1);
        }
    }

    /** setFire. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos at = pos.relative(state.getValue(FACING));
        BlockState there = level.getBlockState(at);
        if (state.getValue(ACTIVE)) {
            if (there.isAir()) {
                level.playSound(null, at, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
                level.setBlock(at, BaseFireBlock.getState(level, at), 3);
            }
        } else if (there.getBlock() instanceof BaseFireBlock) {
            level.playSound(null, at, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
            // shouldActivate = true: once more on the next tick
            level.scheduleTick(pos, this, 1);
        }
    }
}
