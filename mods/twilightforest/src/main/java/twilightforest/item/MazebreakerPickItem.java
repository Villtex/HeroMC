package twilightforest.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.block.MazestoneBlock;

/** ItemTFMazebreakerPick: mines the maze stone (BlockTFMazestone) 16 times as fast. */
public class MazebreakerPickItem extends Item {
    public MazebreakerPickItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float speed = super.getDestroySpeed(stack, state);
        return state.getBlock() instanceof MazestoneBlock ? speed * 16.0F : speed;
    }
}
