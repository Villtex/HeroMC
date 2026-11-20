package xreliquary.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import xreliquary.XRBlocks;
import xreliquary.XRData;
import xreliquary.XRFx;

import java.util.function.Consumer;

/**
 * ItemWraithEye: a salamander's eye that binds to a wraith node ("nodeX/Y/Z") and, for an ender pearl, teleports its
 * holder next to it (the last free two-block spot of the 5 x 3 x 5 around the node, as the loop of 1.5.2 ended).
 */
public class WraithEyeItem extends SalamanderEyeItem {
    public WraithEyeItem(Properties properties) {
        super(properties, 2);
    }

    private static BlockPos node(ItemStack stack) {
        return new BlockPos(XRData.getShort(stack, "nodeX"), XRData.getShort(stack, "nodeY"), XRData.getShort(stack, "nodeZ"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        Level level = context.level();
        BlockPos node = node(stack);
        if (level == null || !level.getBlockState(node).is(XRBlocks.WRAITH_NODE.get())) {
            tooltip.accept(Component.translatable("item.xreliquary.wraith_eye.tooltip.nowhere"));
        } else {
            tooltip.accept(Component.translatable("item.xreliquary.wraith_eye.tooltip.bound", node.getX(), node.getY(), node.getZ()));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        BlockPos node = node(player.getItemInHand(hand));
        if (!level.isClientSide() && level.getBlockState(node).is(XRBlocks.WRAITH_NODE.get())
                && (XRFx.creative(player) || XRFx.consumeMain(player, Items.ENDER_PEARL))) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        BlockPos p = node.offset(dx, dy, dz);
                        if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) {
                            player.teleportTo(p.getX(), p.getY(), p.getZ());
                            for (int i = 0; i < 2; i++) {
                                XRFx.particle(level, ParticleTypes.PORTAL, player.getX(), player.getY(), player.getZ(), level.getRandom().nextGaussian(),
                                        level.getRandom().nextGaussian(), level.getRandom().nextGaussian());
                            }
                        }
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos()).is(XRBlocks.WRAITH_NODE.get())) {
            BlockPos pos = context.getClickedPos();
            ItemStack stack = context.getItemInHand();
            XRData.setShort(stack, "nodeX", pos.getX());
            XRData.setShort(stack, "nodeY", pos.getY());
            XRData.setShort(stack, "nodeZ", pos.getZ());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
