package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;
import secretrooms.SRBlocks;

/**
 * BlockCamoGate: powered it pushes out a wall of up to 10 camo dummies in its direction (breaking what is in the way
 * unless that side is solid), unpowered or broken it takes them back. The direction: up or down when placed from
 * within 2 blocks above or below, else away from the player.
 */
public class CamoGateBlock extends CamoFullBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public CamoGateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction dir = Direction.DOWN;
        if (player != null) {
            int l = Mth.floor(player.getYRot() * 4.0F / 360.0F + 0.5) & 3;
            double d = player.getY() + 1.82;
            dir = switch (l) {
                case 0 -> Direction.NORTH;
                case 1 -> Direction.EAST;
                case 2 -> Direction.SOUTH;
                default -> Direction.WEST;
            };
            if (Math.abs((float) player.getX() - pos.getX()) < 2.0F && Math.abs((float) player.getZ() - pos.getZ()) < 2.0F) {
                if (d - pos.getY() > 2.0) {
                    dir = Direction.UP;
                } else if (pos.getY() - d > 0.0) {
                    dir = Direction.DOWN;
                }
            }
        }
        return this.defaultBlockState().setValue(FACING, dir);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moved) {
        if (level.isClientSide()) {
            return;
        }
        boolean powered = powered(level, pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), 4);
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (powered(level, pos)) {
            Direction dir = state.getValue(FACING);
            for (int i = 1; i <= 10; i++) {
                BlockPos p = pos.relative(dir, i);
                if (level.getBlockState(p).isFaceSturdy(level, p, dir.getOpposite())) {
                    break;
                }
                level.destroyBlock(p, true);
                level.setBlock(p, SRBlocks.CAMO_DUMMY.get().defaultBlockState(), 3);
            }
        } else {
            destroyGate(level, pos, state);
        }
    }

    private static void destroyGate(Level level, BlockPos pos, BlockState state) {
        Direction dir = state.getValue(FACING);
        for (int i = 1; i <= 10; i++) {
            BlockPos p = pos.relative(dir, i);
            if (level.getBlockState(p).is(SRBlocks.CAMO_DUMMY.get())) {
                level.removeBlock(p, false);
            }
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        destroyGate(level, pos, state);
    }
}
