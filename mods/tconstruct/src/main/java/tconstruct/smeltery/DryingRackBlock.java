package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;

/**
 * DryingRack: a wooden bar. Placed on a wall it hangs at the top of the block against that wall (metadata 2..5 =
 * the side clicked), placed on a floor or ceiling it lies at the bottom along x (0) or z (1) by the player's yaw.
 * Right-click hangs one held item on it or takes the item off.
 */
public class DryingRackBlock extends BaseEntityBlock {
    /** The metadata of 1.5.2. */
    public static final IntegerProperty META = IntegerProperty.create("meta", 0, 5);
    private static final VoxelShape[] SHAPES = {Block.box(0, 0, 6, 16, 4, 10), Block.box(6, 0, 0, 10, 4, 16), Block.box(0, 12, 12, 16, 16, 16),
            Block.box(0, 12, 0, 16, 16, 4), Block.box(12, 12, 0, 16, 16, 16), Block.box(0, 12, 0, 4, 16, 16)};

    public DryingRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(META, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(META);
    }

    /** onBlockPlaced (side > 1 keeps the side) and onBlockPlacedBy (yaw quarter odd: 1). */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (side.getAxis() != Direction.Axis.Y) {
            return this.defaultBlockState().setValue(META, side.get3DDataValue());
        }
        int quarter = net.minecraft.util.Mth.floor(context.getRotation() * 4.0F / 360.0F + 0.5) & 3;
        return this.defaultBlockState().setValue(META, quarter % 2);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(META)];
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, TSmeltery.DRYING_RACK_ENTITY.get(), DryingRackBlockEntity::serverTick);
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
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack) {
            if (rack.getItem(0).isEmpty()) {
                ItemStack held = player.getMainHandItem();
                if (!held.isEmpty()) {
                    rack.setItem(0, held.split(1));
                }
            } else {
                CastingBlock.give(player, rack.removeItem(0, 1));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
