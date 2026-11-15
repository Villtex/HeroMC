package offlawn;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/** SunflowerItem: a food (2 hunger points) that plants a sunflower when used on the top of a block. */
public class SunflowerSeedItem extends Item {
    public SunflowerSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().above();
        Player player = context.getPlayer();
        BlockState flower = OffLawn.SUNFLOWER.get().defaultBlockState();
        if (player != null && (!player.mayUseItemAt(pos, Direction.UP, context.getItemInHand())
                || !player.mayUseItemAt(pos.above(), Direction.UP, context.getItemInHand()))) {
            return InteractionResult.PASS;
        }
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above()) || !flower.canSurvive(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            DoublePlantBlock.placeAt(level, flower, pos, Block.UPDATE_ALL);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
            context.getItemInHand().consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }
}
