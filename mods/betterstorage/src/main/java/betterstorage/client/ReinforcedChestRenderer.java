package betterstorage.client;

import betterstorage.BSUtil;
import betterstorage.BetterStorage;
import betterstorage.ChestMaterial;
import betterstorage.block.ReinforcedChestBlock;
import betterstorage.block.ReinforcedChestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityReinforcedChestRenderer: ModelChest or ModelLargeChest with the material's texture, drawn by the main
 * half only, and the lock (an item) on the front.
 */
public class ReinforcedChestRenderer implements BlockEntityRenderer<ReinforcedChestBlockEntity, ReinforcedChestRenderer.State> {
    private final BSModels.ChestModel chest;
    private final BSModels.ChestModel largeChest;
    private final ItemModelResolver items;

    public static class State extends BlockEntityRenderState {
        boolean draw;
        boolean large;
        ChestMaterial material = ChestMaterial.IRON;
        int rotation;
        float open;
        @Nullable ItemStackRenderState lock;
    }

    public ReinforcedChestRenderer(BlockEntityRendererProvider.Context context) {
        this.chest = new BSModels.ChestModel(context.bakeLayer(BSModels.CHEST));
        this.largeChest = new BSModels.ChestModel(context.bakeLayer(BSModels.LARGE_CHEST));
        this.items = context.itemModelResolver();
    }

    public static Identifier texture(ChestMaterial material, boolean large) {
        return BetterStorage.id("textures/model/" + (large ? "chest_large/" : "chest/") + material.name + ".png");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ReinforcedChestBlockEntity be, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, camera, breakProgress);
        state.large = be.isConnected();
        state.draw = !state.large || be.isMain();
        state.material = be.getBlockState().getBlock() instanceof ReinforcedChestBlock b ? b.material : ChestMaterial.IRON;
        Direction facing = be.orientation();
        state.rotation = BSUtil.rotation(facing);
        state.open = be.prevLidAngle + (be.lidAngle - be.prevLidAngle) * partialTicks;
        ItemStack lock = be.getLock();
        if (lock.isEmpty()) {
            state.lock = null;
        } else {
            state.lock = new ItemStackRenderState();
            this.items.updateForTopItem(state.lock, lock, ItemDisplayContext.NONE, be.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.draw) {
            return;
        }
        poseStack.pushPose();
        submitChest(poseStack, collector, state.large ? this.largeChest : this.chest, texture(state.material, state.large), state.large,
                state.rotation, state.open, state.lightCoords);
        if (state.lock != null) {
            // glTranslatef(large ? 1 : 0.5, 0.40625, 0.03125), glScalef(0.5, 0.5, 1.5), renderItemIn3d (turned to face out)
            poseStack.pushPose();
            poseStack.translate(state.large ? 1.0F : 0.5F, 0.40625F, 0.03125F);
            poseStack.scale(0.5F, 0.5F, 1.5F);
            poseStack.rotateDegrees(Axis.XP, 180.0F);
            state.lock.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    /** The transformation of renderTileEntityAt; leaves the pose in the chest's model space. */
    static void submitChest(PoseStack poseStack, SubmitNodeCollector collector, BSModels.ChestModel model, Identifier texture, boolean large,
            int rotation, float open, int light) {
        poseStack.translate(0.0F, 1.0F, 1.0F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.5F, 0.5F, 0.5F);
        if (rotation == 180 && large) {
            poseStack.translate(1.0F, 0.0F, 0.0F);
        }
        if (rotation == 270 && large) {
            poseStack.translate(0.0F, 0.0F, -1.0F);
        }
        poseStack.rotateDegrees(Axis.YP, rotation);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        collector.submitModel(model, open, poseStack, model.renderType(texture), light, OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    public AABB getRenderBoundingBox(ReinforcedChestBlockEntity be) {
        return be.renderBox();
    }
}
