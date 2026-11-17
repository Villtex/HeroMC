package projectzulu.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockUniversalFlowerPot: a flower pot that holds any one item (a block is drawn as a small block, anything else as an
 * item). The original opened a one-slot GUI; here a right click with an item puts one in, a right click with an empty
 * hand takes it out again (sneaking does nothing, as in the original). Looks like the vanilla pot and drops a vanilla
 * flower pot; its content drops as well. With "Replace Default Flower Pot"=true the vanilla flower pot item places
 * this block (see ZuluPlants).
 */
public class UniversalFlowerPotBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.column(6.0, 0.0, 6.0);

    public UniversalFlowerPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UniversalFlowerPotBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof UniversalFlowerPotBlockEntity pot)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!pot.getStack().isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            pot.setStack(stack.copyWithCount(1));
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof UniversalFlowerPotBlockEntity pot) || pot.getStack().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack content = pot.getStack();
            pot.setStack(ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(content, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(Items.FLOWER_POT);
    }
}
