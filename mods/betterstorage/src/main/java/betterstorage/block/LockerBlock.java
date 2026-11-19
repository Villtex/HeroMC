package betterstorage.block;

import betterstorage.BSUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockLocker: opens only from the front (the door side). {@link #MIRROR} (the tile entity's mirror flag) puts the
 * hinge on the other side, chosen by where the player looked when placing it. Every side but the front is solid.
 */
public class LockerBlock extends ConnectableBlock {
    public static final BooleanProperty MIRROR = BooleanProperty.create("mirror");
    /** isBlockSolidOnSide: not the front. A notch in the middle of the front keeps that face from being sturdy. */
    private static final VoxelShape[] SUPPORT = new VoxelShape[6];

    static {
        for (Direction dir : Direction.values()) {
            double x = 8 + dir.getStepX() * 7.5, y = 8 + dir.getStepY() * 7.5, z = 8 + dir.getStepZ() * 7.5;
            VoxelShape notch = box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
            SUPPORT[dir.get3DDataValue()] = Shapes.join(Shapes.block(), notch, BooleanOp.ONLY_FIRST);
        }
    }

    public LockerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(MIRROR, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MIRROR);
    }

    /** onBlockPlacedBy: mirror if the player looked to the right of straight at the front. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        Player player = context.getPlayer();
        if (state == null || player == null) {
            return state;
        }
        double angle = BSUtil.rotation(state.getValue(FACING).getOpposite());
        double yaw = (player.getYRot() % 360.0F + 360.0F) % 360.0F;
        return state.setValue(MIRROR, BSUtil.angleDifference(angle, yaw) > 0.0);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LockerBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SUPPORT[state.getValue(FACING).get3DDataValue()];
    }

    /** onBlockActivated: from the front it opens; it always "uses" the click, so no item gets used on it. */
    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && hit.getDirection() == state.getValue(FACING) && level.getBlockEntity(pos) instanceof LockerBlockEntity locker
                && player instanceof ServerPlayer sp) {
            locker.openGui(sp);
        }
        return InteractionResult.SUCCESS;
    }
}
