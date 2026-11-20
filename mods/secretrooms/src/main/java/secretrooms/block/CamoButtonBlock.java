package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * BlockCamoButton (stone: damage 0, wood: damage 1): pressed it gives 15 to every side for 30 ticks (tickRate read
 * the already pressed state, so both kinds wait 30). The wooden one is also pressed by arrows stuck in it.
 */
public class CamoButtonBlock extends CamoFullBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private final boolean wood;

    public CamoButtonBlock(Properties properties, boolean wood) {
        super(properties);
        this.wood = wood;
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(POWERED)) {
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            this.press(level, pos, state, 0.5F);
        }
        return InteractionResult.SUCCESS;
    }

    private void press(Level level, BlockPos pos, BlockState state, float pitch) {
        BlockState now = state.setValue(POWERED, true);
        level.setBlock(pos, now, 3);
        CamoRedstone.notifyAround(level, pos, now);
        CamoRedstone.click(level, pos, 0.5, pitch);
        level.scheduleTick(pos, this, 30);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) {
            return;
        }
        if (this.wood && this.arrows(level, pos)) {
            level.scheduleTick(pos, this, 30);
            return;
        }
        BlockState now = state.setValue(POWERED, false);
        level.setBlock(pos, now, 3);
        CamoRedstone.notifyAround(level, pos, now);
        CamoRedstone.click(level, pos, 0.5, 0.5F);
    }

    private boolean arrows(Level level, BlockPos pos) {
        return !level.getEntitiesOfClass(AbstractArrow.class, new AABB(pos)).isEmpty();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean precise) {
        if (this.wood && !level.isClientSide() && !state.getValue(POWERED) && entity instanceof AbstractArrow) {
            this.press(level, pos, state, 0.6F);
        }
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
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        CamoRedstone.notifyAround(level, pos, state);
    }
}
