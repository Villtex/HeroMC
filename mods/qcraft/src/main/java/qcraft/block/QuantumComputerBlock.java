package qcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import qcraft.item.QuantumComputerItem;

/**
 * BlockQuantumComputer "Quantum Computer" / "Entangled Quantum Computer" (iron, hardness 5, resistance 10, metal
 * sound). Faces the player who placed it (BlockDirectional). Right click (not sneaking) opens the GUI. Broken by a
 * player not in creative mode it drops itself with its frequency and stored area; explosions drop nothing.
 */
public class QuantumComputerBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public QuantumComputerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // ((floor(yaw * 4 / 360 + 0.5) & 3) + 2) % 4: the front looks at the player
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuantumComputerBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // ItemQuantumComputer.placeBlockAt
        if (level.getBlockEntity(pos) instanceof QuantumComputerBlockEntity computer) {
            computer.setEntanglementFrequency(QuantumComputerItem.getEntanglementFrequency(stack));
            computer.setStoredData(QuantumComputerItem.getStoredData(stack));
        }
    }

    /** getBlockDropped: the computer with its frequency and stored area. */
    public static ItemStack drop(QuantumComputerBlockEntity computer) {
        ItemStack stack = QuantumComputerItem.create(computer.getEntanglementFrequency(), 1);
        QuantumComputerItem.setStoredData(stack, computer.getStoredData());
        return stack;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof QuantumComputerBlockEntity computer) {
            popResource(level, pos, drop(computer));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        if (level.getBlockEntity(pos) instanceof QuantumComputerBlockEntity computer) {
            return drop(computer);
        }
        return QuantumComputerItem.create(-1, 1);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof QuantumComputerBlockEntity) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new QuantumComputerMenu(id, inv, pos),
                    Component.translatable("block.quantumcraft.quantum_computer")), buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }
}
