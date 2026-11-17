package projectzulu.item.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import projectzulu.ZuluPlants;

/** ItemAloeVeraSeeds: on the top of dirt, sand or grass with air above, plants a young aloe vera (metadata 0). */
public class AloeVeraSeedsItem extends Item {
    public AloeVeraSeedsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player != null && (!player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())
                || !player.mayUseItemAt(pos.above(), context.getClickedFace(), context.getItemInHand()))) {
            return InteractionResult.FAIL;
        }
        BlockState soil = level.getBlockState(pos);
        if ((soil.is(Blocks.DIRT) || soil.is(Blocks.SAND) || soil.is(Blocks.GRASS_BLOCK)) && level.isEmptyBlock(pos.above())) {
            if (!level.isClientSide()) {
                level.setBlock(pos.above(), ZuluPlants.ALOE_VERA.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
