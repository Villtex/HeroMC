package betterstorage.client;

import betterstorage.BSUtil;
import betterstorage.BetterStorage;
import betterstorage.block.LockerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** TileEntityLockerRenderer: the Wavefront models locker.obj / locker_large.obj (box, door, handle), door turned open. */
public class LockerRenderer implements BlockEntityRenderer<LockerBlockEntity, LockerRenderer.State> {
    static final Identifier TEXTURE = BetterStorage.id("textures/model/locker.png");
    static final Identifier LARGE_TEXTURE = BetterStorage.id("textures/model/locker_large.png");
    private final ObjModel model;
    private final ObjModel largeModel;

    public static class State extends BlockEntityRenderState {
        boolean draw;
        boolean large;
        boolean mirror;
        int rotation;
        float open;
    }

    public LockerRenderer(BlockEntityRendererProvider.Context context) {
        this.model = ObjModel.load(BetterStorage.id("models/obj/locker.obj"));
        this.largeModel = ObjModel.load(BetterStorage.id("models/obj/locker_large.obj"));
    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LockerBlockEntity be, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, camera, breakProgress);
        state.large = be.isConnected();
        state.draw = !state.large || be.isMain();
        state.mirror = be.mirror();
        state.rotation = BSUtil.rotation(be.orientation());
        state.open = be.prevLidAngle + (be.lidAngle - be.prevLidAngle) * partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.draw) {
            poseStack.pushPose();
            submitLocker(poseStack, collector, state.large ? this.largeModel : this.model, state.large ? LARGE_TEXTURE : TEXTURE, state.mirror,
                    state.rotation, state.open, state.lightCoords);
            poseStack.popPose();
        }
    }

    /** renderTileEntityAt + ModelLocker.renderAll. */
    static void submitLocker(PoseStack poseStack, SubmitNodeCollector collector, ObjModel model, Identifier texture, boolean mirror, int rotation,
            float open, int light) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        poseStack.rotateDegrees(Axis.YP, -rotation);
        float angle = 1.0F - open;
        angle = 1.0F - angle * angle * angle;
        float doorAngle = angle * 90.0F;
        model.submit(poseStack, collector, texture, light, "box");
        if (doorAngle > 0.0F) {
            float px = mirror ? -7.0F : 7.0F;
            float pz = -7.0F;
            if (mirror) {
                doorAngle = -doorAngle;
            }
            poseStack.translate(-px, 0.0F, -pz);
            poseStack.rotateDegrees(Axis.YP, -doorAngle);
            poseStack.translate(px, 0.0F, pz);
        }
        model.submit(poseStack, collector, texture, light, "door");
        if (mirror) {
            poseStack.translate(-11.0F, 0.0F, 0.0F);
        }
        model.submit(poseStack, collector, texture, light, "handle");
    }

    @Override
    public AABB getRenderBoundingBox(LockerBlockEntity be) {
        return be.renderBox();
    }
}
