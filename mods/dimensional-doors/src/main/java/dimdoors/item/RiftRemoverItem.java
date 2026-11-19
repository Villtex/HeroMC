package dimdoors.item;

import dimdoors.DimDoors;
import dimdoors.block.RiftBlockEntity;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/** itemRiftRemover: aimed at a rift (or next to one), it closes the rift and the rifts near it. Five uses. */
public class RiftRemoverItem extends Item {
    public RiftRemoverItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        BlockPos hit = DDItems.target(level, player, 6.0);
        ItemStack stack = player.getItemInHand(hand);
        if (hit != null && removeRift(level, hit, 1, player, stack, hand)) {
            level.playSound(null, player, DimDoors.RIFT_CLOSE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        return InteractionResult.PASS;
    }

    /** dimHelper.removeRift. */
    static boolean removeRift(Level level, BlockPos pos, int range, Player player, ItemStack stack, InteractionHand hand) {
        LinkData nearest = null;
        float distance = range + 1;
        int dim = DDWorld.dimId(level, pos);
        for (int i = -range; i < range; i++) {
            for (int j = -range; j < range; j++) {
                for (int k = -range; k < range; k++) {
                    BlockPos at = pos.offset(i, j, k);
                    int d = Math.abs(i) + Math.abs(j) + Math.abs(k);
                    if (level.getBlockState(at).is(DimDoors.RIFT.get()) && d < distance && (d != 0 || range == 1)) {
                        nearest = DDWorld.link(dim, at.getX(), at.getY(), at.getZ());
                        distance = d;
                    }
                }
            }
        }
        if (nearest != null && level.getBlockEntity(new BlockPos(nearest.locX, nearest.locY, nearest.locZ)) instanceof RiftBlockEntity rift) {
            rift.close();
            stack.hurtAndBreak(1, player, hand);
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Use near exposed rift"));
        tooltip.accept(Component.literal("to remove it and"));
        tooltip.accept(Component.literal("any nearby rifts"));
    }
}
