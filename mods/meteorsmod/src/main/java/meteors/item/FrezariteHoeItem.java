package meteors.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

/** ItemFrezariteHoe: tills like a hoe, but the farmland is wet at once (setBlock(tilledField, 14)). */
public class FrezariteHoeItem extends Item {
    public FrezariteHoeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = super.useOn(context);
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (result.consumesAction() && !level.isClientSide() && state.is(Blocks.FARMLAND)) {
            level.setBlock(context.getClickedPos(), state.setValue(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE), 3);
        }
        return result;
    }
}
