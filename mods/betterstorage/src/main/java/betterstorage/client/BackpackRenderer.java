package betterstorage.client;

import betterstorage.BSUtil;
import betterstorage.BetterStorage;
import betterstorage.block.BackpackBlock;
import betterstorage.block.BackpackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityBackpackRenderer: ModelBackpack with the armor textures of the backpack item, the first pass in its colour
 * (leather brown when undyed), the overlay pass white. The ender backpack has one texture.
 */
public class BackpackRenderer implements BlockEntityRenderer<BackpackBlockEntity, BackpackRenderer.State> {
    static final Identifier TEXTURE = BetterStorage.id("textures/entity/equipment/humanoid/backpack.png");
    static final Identifier OVERLAY = BetterStorage.id("textures/entity/equipment/humanoid/backpack_overlay.png");
    static final Identifier ENDER = BetterStorage.id("textures/entity/equipment/humanoid/ender_backpack.png");
    /** ItemArmor.getColor of CLOTH without a colour. */
    public static final int DEFAULT_COLOR = 0xA06540;
    private final BSModels.BackpackModel model;

    public static class State extends BlockEntityRenderState {
        boolean ender;
        int color = DEFAULT_COLOR;
        int rotation;
        float open;
    }

    public BackpackRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new BSModels.BackpackModel(context.bakeLayer(BSModels.BACKPACK));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BackpackBlockEntity be, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, camera, breakProgress);
        state.ender = be.getBlockState().getBlock() instanceof BackpackBlock b && b.ender;
        state.color = color(be.stack);
        state.rotation = BSUtil.rotation(be.getBlockState().getValue(BackpackBlock.FACING));
        state.open = be.prevLidAngle + (be.lidAngle - be.prevLidAngle) * partialTicks;
    }

    public static int color(ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, DEFAULT_COLOR);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        submitBackpack(poseStack, collector, this.model, state.ender, state.color, state.rotation, state.open, state.lightCoords);
        poseStack.popPose();
    }

    static void submitBackpack(PoseStack poseStack, SubmitNodeCollector collector, BSModels.BackpackModel model, boolean ender, int color,
            float rotation, float open, int light) {
        poseStack.translate(0.0F, 2.0F, 1.0F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, rotation);
        if (ender) {
            collector.submitModel(model, open, poseStack, model.renderType(ENDER), light, OverlayTexture.NO_OVERLAY, -1, null, 0);
        } else {
            collector.submitModel(model, open, poseStack, model.renderType(TEXTURE), light, OverlayTexture.NO_OVERLAY, ARGB.opaque(color), null, 0);
            collector.submitModel(model, open, poseStack, model.renderType(OVERLAY), light, OverlayTexture.NO_OVERLAY, -1, null, 0);
        }
    }
}
