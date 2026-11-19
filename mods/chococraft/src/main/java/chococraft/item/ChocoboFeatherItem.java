package chococraft.item;

import chococraft.ChocoCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** The chocobo feather; right-clicked on a bookshelf it turns the shelf into a Chocopedia (onFeatherRightClick). */
public class ChocoboFeatherItem extends Item {
    public ChocoboFeatherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.BOOKSHELF)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.removeBlock(pos, false);
            context.getItemInHand().shrink(1);
            // PacketChocoboSpawnItem: the item appears at the block's corner
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ChocoCraft.CHOCOPEDIA.get())));
        }
        return InteractionResult.SUCCESS;
    }
}
