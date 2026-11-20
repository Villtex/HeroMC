package tconstruct.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import tconstruct.entity.ThrownDagger;
import tconstruct.entity.TinkerArrow;

/**
 * ArrowRender / DaggerRender: the arrow or dagger item (its layered tool model) along the flight direction, tilted by
 * 45 degrees, 1.35 times the item size, shaking when it hits. The dagger also turns about its own axis. Client check open.
 */
public class ToolProjectileRenderer<T extends AbstractArrow> extends EntityRenderer<T, ToolProjectileRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public ToolProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    public static class State extends EntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        float yRot;
        float xRot;
        float shake;
        boolean dagger;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.shake = entity.shakeTime - partialTicks;
        state.dagger = entity instanceof ThrownDagger;
        ItemStack stack = entity instanceof TinkerArrow arrow ? arrow.getArrow() : entity instanceof ThrownDagger dagger ? dagger.getDagger() : ItemStack.EMPTY;
        this.itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.NONE, entity);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, state.yRot - 90.0F);
        poseStack.rotateDegrees(Axis.ZP, state.xRot - 45.0F);
        if (state.dagger) {
            poseStack.rotateDegrees(Axis.ZP, state.yRot + 90.0F);
            poseStack.rotateDegrees(Axis.ZP, state.xRot * 15.0F);
        }
        if (state.shake > 0.0F) {
            poseStack.rotateDegrees(Axis.ZP, -Mth.sin(state.shake * 3.0F) * state.shake);
        }
        poseStack.scale(1.35F, 1.35F, 1.35F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
