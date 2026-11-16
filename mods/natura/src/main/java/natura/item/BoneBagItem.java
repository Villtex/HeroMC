package natura.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** BoneBag: bone meal on every block of the 3x3 around the clicked block (one bag per use). */
public class BoneBagItem extends Item {
    public BoneBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos center = context.getClickedPos();
        boolean grown = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
            BlockPos target = pos.immutable();
            if (player != null && (!player.mayUseItemAt(target, Direction.UP, stack) || !player.mayUseItemAt(target.above(), Direction.UP, stack))) {
                continue;
            }
            if (BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, target, player)) {
                grown = true;
                if (!level.isClientSide()) {
                    level.levelEvent(1505, target, 15);
                }
            }
        }
        if (!grown) {
            return InteractionResult.PASS;
        }
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
