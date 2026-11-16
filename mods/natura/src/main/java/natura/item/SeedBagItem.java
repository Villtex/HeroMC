package natura.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** SeedBag: plants its crop on every free soil block of the 3x3 around the clicked block (one bag per use). */
public class SeedBagItem extends Item {
    private final Supplier<? extends Block> crop;

    public SeedBagItem(Supplier<? extends Block> crop, Properties properties) {
        super(properties);
        this.crop = crop;
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
        BlockState plant = this.crop.get().defaultBlockState();
        boolean planted = false;
        for (BlockPos soil : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
            BlockPos above = soil.above();
            if (player != null && (!player.mayUseItemAt(soil, Direction.UP, stack) || !player.mayUseItemAt(above, Direction.UP, stack))) {
                continue;
            }
            if (level.isEmptyBlock(above) && plant.canSurvive(level, above)) {
                if (!level.isClientSide()) {
                    level.setBlock(above, plant, Block.UPDATE_ALL);
                }
                planted = true;
            }
        }
        if (!planted) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.levelEvent(2001, center, Block.getId(plant));
        }
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
