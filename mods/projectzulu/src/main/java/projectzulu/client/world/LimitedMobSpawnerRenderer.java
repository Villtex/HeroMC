package projectzulu.client.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import projectzulu.world.LimitedMobSpawnerBlockEntity;

/**
 * TileEntityLimitedMobSpawnerRenderer: the spawner's mob at 0.4375 scale in the cage, tilted by 30 degrees and turning
 * with the spawner's yaw (times 10), as the vanilla spawner of 1.5.2 drew it.
 */
public class LimitedMobSpawnerRenderer implements BlockEntityRenderer<LimitedMobSpawnerBlockEntity, SpawnerRenderState> {
    private final EntityRenderDispatcher entityRenderer;

    public LimitedMobSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public SpawnerRenderState createRenderState() {
        return new SpawnerRenderState();
    }

    @Override
    public void extractRenderState(LimitedMobSpawnerBlockEntity spawner, SpawnerRenderState state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(spawner, state, partialTicks, cameraPosition, breakProgress);
        state.displayEntity = null;
        Entity entity = spawner.getMobEntity();
        if (entity != null) {
            state.displayEntity = this.entityRenderer.extractEntity(entity, partialTicks);
            state.displayEntity.lightCoords = state.lightCoords;
            state.spin = (float) (spawner.yaw2 + (spawner.yaw - spawner.yaw2) * partialTicks) * 10.0F;
            state.scale = 0.4375F;
        }
    }

    @Override
    public void submit(SpawnerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.displayEntity == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.translate(0.0F, 0.4F, 0.0F);
        poseStack.rotateDegrees(Axis.YP, state.spin);
        poseStack.rotateDegrees(Axis.XP, -30.0F);
        poseStack.translate(0.0F, -0.4F, 0.0F);
        poseStack.scale(state.scale, state.scale, state.scale);
        this.entityRenderer.submit(state.displayEntity, camera, 0.0, 0.0, 0.0, poseStack, collector);
        poseStack.popPose();
    }
}
