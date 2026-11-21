package tconstruct.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiConsumer;

/**
 * BarricadeBlock (trap.barricade.oak/spruce/birch/jungle): logs stacked up to four (meta % 4; right click with another
 * one, not sneaking, adds one), turned to the placer (meta / 4). It collides as a full block. Breaking takes one log off
 * at a time; an explosion takes off power = size * 2 / |dx + dy + dz| logs and removes the barricade when none are left.
 */
public class BarricadeBlock extends Block {
    public static final IntegerProperty LOGS = IntegerProperty.create("logs", 1, 4);

    public BarricadeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH).setValue(LOGS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING, LOGS);
    }

    /** BarricadeItem: rotation = floor(yaw * 4 / 360 + 0.5) & 3, 0 = south as the player looks. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        int logs = state.getValue(LOGS);
        if (stack.is(this.asItem()) && !player.isShiftKeyDown() && logs < 4) {
            level.setBlock(pos, state.setValue(LOGS, logs + 1), 3);
            SoundType sound = state.getSoundType();
            level.playSound(null, pos, sound.getStepSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /** harvestBlock: one log comes off; the rest stays (the loot table drops one barricade). */
    @Override
    public void playerDestroy(ServerLevel level, net.minecraft.server.level.ServerPlayer player, BlockPos pos, BlockState state,
                              BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        int logs = state.getValue(LOGS);
        if (logs > 1 && level.getBlockState(pos).isAir()) {
            level.setBlock(pos, state.setValue(LOGS, logs - 1), 3);
        }
    }

    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> drops) {
        if (explosion.getBlockInteraction() == Explosion.BlockInteraction.KEEP) {
            return;
        }
        double distance = Math.abs(pos.getX() - explosion.center().x + (pos.getY() - explosion.center().y) + (pos.getZ() - explosion.center().z));
        double power = explosion.radius() * 2.0F / distance;
        int left = (int) (state.getValue(LOGS) - 1 - power);
        if (left < 0) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(LOGS, left + 1), 3);
        }
    }
}
