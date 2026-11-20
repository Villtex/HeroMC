package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * BlockTorchLever: looks like a torch (light 14) and works like a lever: right click toggles it; on it gives 15 to
 * every side and strongly powers the block it is fixed to. Standing and wall form as for vanilla torches.
 */
public final class TorchLeverBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private TorchLeverBlock() {
    }

    static InteractionResult toggle(BlockState state, Level level, BlockPos pos, Direction attached) {
        if (!level.isClientSide()) {
            boolean was = state.getValue(POWERED);
            level.setBlock(pos, state.setValue(POWERED, !was), 2);
            CamoRedstone.click(level, pos, 0.5, was ? 0.5F : 0.6F);
            level.updateNeighborsAt(pos, state.getBlock());
            level.updateNeighborsAt(pos.relative(attached), state.getBlock());
        }
        return InteractionResult.SUCCESS;
    }

    public static class Standing extends TorchBlock {
        public Standing(Properties properties) {
            super(ParticleTypes.FLAME, properties);
            this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(POWERED);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            return toggle(state, level, pos, Direction.DOWN);
        }

        @Override
        protected boolean isSignalSource(BlockState state) {
            return true;
        }

        @Override
        protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return state.getValue(POWERED) ? 15 : 0;
        }

        @Override
        protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return state.getValue(POWERED) && direction == Direction.UP ? 15 : 0;
        }

        @Override
        protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
            if (state.getValue(POWERED)) {
                level.updateNeighborsAt(pos, this);
                level.updateNeighborsAt(pos.below(), this);
            }
        }
    }

    public static class Wall extends WallTorchBlock {
        public Wall(Properties properties) {
            super(ParticleTypes.FLAME, properties);
            this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(POWERED);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            return toggle(state, level, pos, state.getValue(FACING).getOpposite());
        }

        @Override
        protected boolean isSignalSource(BlockState state) {
            return true;
        }

        @Override
        protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return state.getValue(POWERED) ? 15 : 0;
        }

        @Override
        protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return state.getValue(POWERED) && direction == state.getValue(FACING) ? 15 : 0;
        }

        @Override
        protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
            if (state.getValue(POWERED)) {
                level.updateNeighborsAt(pos, this);
                level.updateNeighborsAt(pos.relative(state.getValue(FACING).getOpposite()), this);
            }
        }
    }
}
