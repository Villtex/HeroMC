package artifice.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * DispenserBehaviorSledge: a dispenser holding a sledge smashes the block in front of it into its parts (no loss
 * chance) and the sledge loses one durability. As in 1.5.2 only blocks registered with a metadata value count:
 * the "wild" registrations were looked up with the wrong key and never matched.
 */
public class DispenseSledge extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        BlockPos target = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
        BlockState state = level.getBlockState(target);
        SledgeParts.Entry entry = SledgeParts.get(state);
        if (entry != null && entry.exact()) {
            level.removeBlock(target, false);
            stack.hurtAndBreak(1, level, null, item -> {
            });
            RandomSource random = level.getRandom();
            for (ItemStack part : entry.parts()) {
                ItemEntity item = new ItemEntity(level, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, part.copy());
                item.setDeltaMovement(-0.1 + 0.2 * random.nextDouble(), 0.1, -0.1 + 0.2 * random.nextDouble());
                level.addFreshEntity(item);
            }
        }
        return stack;
    }

    @Override
    protected void playSound(BlockSource source) {
        // playAuxSFX 1002: the bow sound of 1.5.2, today the dispenser launch sound
        source.level().levelEvent(1002, source.pos(), 0);
    }
}
