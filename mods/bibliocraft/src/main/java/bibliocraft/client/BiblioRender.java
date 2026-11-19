package bibliocraft.client;

import bibliocraft.common.BCConfig;
import bibliocraft.reading.Reading;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/** Helpers of the block entity renderers: the reading-glasses look, item names on blocks, items in the world. */
public final class BiblioRender {
    private BiblioRender() {
    }

    /**
     * What HighlightHandler.DrawBlockHighlightEvent got: the player wears reading glasses and looks at this block.
     * hitX/Y/Z inside the block, rounded to 1/100 like the original; {@code close} = within 3 blocks on every axis.
     */
    public record Look(double hitX, double hitY, double hitZ, Direction side, boolean close) {
    }

    /** The look of the local player at {@code pos}, or null (not looking at it, or no glasses). Use in extractRenderState. */
    public static @Nullable Look look(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        HitResult hit = mc.hitResult;
        if (player == null || !(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK || !bhr.getBlockPos().equals(pos)
                || !Reading.canRead(player)) {
            return null;
        }
        double hx = Math.round((bhr.getLocation().x - pos.getX()) * 100.0) / 100.0;
        double hy = Math.round((bhr.getLocation().y - pos.getY()) * 100.0) / 100.0;
        double hz = Math.round((bhr.getLocation().z - pos.getZ()) * 100.0) / 100.0;
        boolean close = Math.abs((int) (player.getX() - pos.getX())) < 3 && Math.abs((int) (player.getY() - pos.getY())) < 3
                && Math.abs((int) (player.getZ() - pos.getZ())) < 3;
        return new Look(hx, hy, hz, bhr.getDirection(), close);
    }

    /** "count name" as the renderers showed it (count only above 1). */
    public static String label(ItemStack stack) {
        String name = stack.getHoverName().getString();
        return stack.getCount() > 1 ? stack.getCount() + " " + name : name;
    }

    /**
     * renderText of the renderers: scale 0.006, turned to face the front, centred, in Config.color (shadow if
     * RenderTextShadow). Call with the pose of the original at glScalef(0.006...), {@code x/y} in font pixels after the
     * translate of the original (e.g. -adjust, 0).
     */
    public static void text(PoseStack poseStack, SubmitNodeCollector collector, Font font, String text, double xAdjust, double yAdjust,
            double zAdjust, int light) {
        poseStack.pushPose();
        poseStack.scale(0.006F, 0.006F, 0.006F);
        poseStack.rotateDegrees(Axis.YP, 180.0F);
        poseStack.rotateDegrees(Axis.ZP, 180.0F);
        poseStack.translate(xAdjust, -8.0 + yAdjust, -1.0 + zAdjust);
        drawCentered(poseStack, collector, font, text, light);
        poseStack.popPose();
    }

    /** fontRender.drawString(text, -width/2, 0, Config.color, Config.textshadow) at the current pose. */
    public static void drawCentered(PoseStack poseStack, SubmitNodeCollector collector, Font font, String text, int light) {
        FormattedCharSequence seq = Component.literal(text).getVisualOrderText();
        float x = -(font.width(seq) / 2);
        collector.submitText(poseStack, x, 0.0F, seq, BCConfig.textShadow(), Font.DisplayMode.POLYGON_OFFSET, light, BCConfig.textColor(), 0, 0);
    }

    /** An item model for drawing in the world (extract side), like ShelfRenderer does. Empty stack -> null. */
    public static @Nullable ItemStackRenderState item(ItemModelResolver resolver, ItemStack stack, ItemDisplayContext context, BlockEntity owner,
            int seed) {
        if (stack.isEmpty()) {
            return null;
        }
        ItemStackRenderState state = new ItemStackRenderState();
        resolver.updateForTopItem(state, stack, context, owner.getLevel(), null, seed);
        return state;
    }
}
