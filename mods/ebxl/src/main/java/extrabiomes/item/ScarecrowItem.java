package extrabiomes.item;

import extrabiomes.EbxlBlocks;
import extrabiomes.entity.ScarecrowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** ItemScarecrow: sets a scarecrow on the clicked face; on top of a fence it stands half a block lower. */
public class ScarecrowItem extends Item {
    public ScarecrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos clicked = context.getClickedPos();
        boolean fence = context.getClickedFace() == Direction.UP && level.getBlockState(clicked).is(BlockTags.FENCES);
        BlockPos pos = clicked.relative(context.getClickedFace());
        ScarecrowEntity scarecrow = EbxlBlocks.SCARECROW.get().create(server, EntitySpawnReason.SPAWN_ITEM_USE);
        if (scarecrow == null) {
            return InteractionResult.FAIL;
        }
        // the original lifted it by 0.5 on fences; a fence of today is 1.5 high, the scarecrow stands on it
        scarecrow.snapTo(pos.getX() + 0.5, pos.getY() + (fence ? 0.5 : 0.0), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
        server.addFreshEntity(scarecrow);
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
