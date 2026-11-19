package dimdoors.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * BlockDimWall: Fabric of Reality (metadata 0) and Ancient Fabric (metadata 1, hardness 10^13). Gives nothing when
 * broken, except with silk touch. Right-clicked with a full block, Fabric of Reality turns into that block.
 */
public class FabricBlock extends Block {
    private final boolean ancient;

    public FabricBlock(boolean ancient, Properties properties) {
        super(properties);
        this.ancient = ancient;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hitResult) {
        if (this.ancient || !(stack.getItem() instanceof BlockItem item)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        Block block = item.getBlock();
        BlockState placed = block.defaultBlockState();
        if (!placed.isRedstoneConductor(level, pos) || !placed.isCollisionShapeFullBlock(level, pos) || block instanceof EntityBlock
                || block instanceof FabricBlock) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.setBlock(pos, placed, Block.UPDATE_CLIENTS);
        }
        return InteractionResult.SUCCESS;
    }
}
