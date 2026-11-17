package projectzulu.item.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import projectzulu.ZuluPlants;

/**
 * ItemCoconutSeed: on a side of a palm log, places a young coconut in the air block in front of it, hanging on that
 * log. The original placed it even where it could not stay (it then dropped again at once); here it is only placed
 * where it can stay.
 */
public class CoconutSeedItem extends Item {
    public CoconutSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos log = context.getClickedPos();
        Direction face = context.getClickedFace();
        if (!level.getBlockState(log).is(ZuluPlants.PALM_LOG.get()) || face.getAxis() == Direction.Axis.Y) {
            return InteractionResult.PASS;
        }
        BlockPos pos = log.relative(face);
        BlockState coconut = ZuluPlants.COCONUT.get().defaultBlockState().setValue(CocoaBlock.FACING, face.getOpposite());
        if (level.isEmptyBlock(pos) && coconut.canSurvive(level, pos)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, coconut, Block.UPDATE_ALL);
            }
            Player player = context.getPlayer();
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
