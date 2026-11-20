package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;

/**
 * SearedBlock metadata 0 (casting table) and 2 (casting basin). Right-click puts one of the held item in as the cast
 * when both slots are empty, else hands out the result, else the cast; nothing while liquid is in it.
 */
public class CastingBlock extends BaseEntityBlock {
    private static final VoxelShape TABLE = Shapes.or(Block.box(0, 12, 0, 16, 16, 16), Block.box(0, 0, 0, 4, 12, 4), Block.box(12, 0, 0, 16, 12, 4),
            Block.box(0, 0, 12, 4, 12, 16), Block.box(12, 0, 12, 16, 12, 16));
    private static final VoxelShape BASIN = Shapes.join(Shapes.block(), Block.box(2, 4, 2, 14, 16, 14), net.minecraft.world.phys.shapes.BooleanOp.ONLY_FIRST);

    public final boolean basin;

    public CastingBlock(boolean basin, Properties properties) {
        super(properties);
        this.basin = basin;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.basin ? BASIN : TABLE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CastingBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TSmeltery.CASTING_ENTITY.get(), level.isClientSide() ? CastingBlockEntity::clientTick : CastingBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return this.activate(level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return this.activate(level, pos, player);
    }

    private InteractionResult activate(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CastingBlockEntity cast && !cast.hasLiquid()) {
            if (cast.getItem(0).isEmpty() && cast.getItem(1).isEmpty()) {
                ItemStack held = player.getMainHandItem();
                if (!held.isEmpty()) {
                    cast.setItem(0, held.split(1));
                }
            } else if (!cast.getItem(1).isEmpty()) {
                give(player, cast.removeItem(1, 1));
            } else {
                give(player, cast.removeItem(0, 1));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** AbilityHelper.spawnItemAtPlayer. */
    static void give(Player player, ItemStack stack) {
        if (!stack.isEmpty() && !player.getInventory().add(stack)) {
            player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
    }
}
