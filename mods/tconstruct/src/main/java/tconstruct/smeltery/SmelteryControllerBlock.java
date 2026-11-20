package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import tconstruct.TSmeltery;

/**
 * SmelteryBlock metadata 0: the smeltery controller. Its front faces the player who placed it (setDirection); it lights
 * up (9) and smokes while the structure is valid, and opens the smeltery GUI unless sneaking.
 */
public class SmelteryControllerBlock extends SearedBrickBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public SmelteryControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmelteryBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, TSmeltery.SMELTERY_ENTITY.get(), SmelteryBlockEntity::serverTick);
    }

    /** onBlockPlacedElsewhere: look for the structure right away. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SmelteryBlockEntity smeltery) {
            smeltery.checkValidPlacement();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof SmelteryBlockEntity smeltery) {
            smeltery.openMenu(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    /** randomDisplayTick: smoke and flames at the front while active. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5 + random.nextFloat() * 6.0F / 16.0F;
        double z = pos.getZ() + 0.5;
        double out = 0.52;
        double along = random.nextFloat() * 0.6F - 0.3F;
        Direction face = state.getValue(FACING);
        double px = x + face.getStepX() * out + (face.getAxis() == Direction.Axis.Z ? along : 0);
        double pz = z + face.getStepZ() * out + (face.getAxis() == Direction.Axis.X ? along : 0);
        level.addParticle(ParticleTypes.SMOKE, px, y, pz, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, px, y, pz, 0, 0, 0);
    }
}
