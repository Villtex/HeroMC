package twilightforest.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.BlockGoblin;

/**
 * RenderTFBlockGoblin (RenderBiped) and, as the parts are no entities here, RenderTFSpikeBlock for its spiked ball and
 * three chain links: placed as EntityTFBlockGoblin.onUpdate placed them, drawn turned 180 - yaw and flipped.
 */
public class BlockGoblinRenderer extends HumanoidMobRenderer<BlockGoblin, TFRenderStates.BlockGoblin, BlockGoblinModel> {
    private static final Identifier TEXTURE = TFMobRenderer.texture("blockgoblin");
    /** EntityTFSpikeBlock.height. */
    private static final double BLOCK_HEIGHT = 0.75;
    private static final float[] CHAIN_STEPS = {0.4F, 0.5F, 0.6F};
    private final EntityModel<EntityRenderState> block;
    private final EntityModel<EntityRenderState> chain;

    public BlockGoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new BlockGoblinModel(context.bakeLayer(TFEntityRenderers.BLOCK_GOBLIN)), 0.625F);
        this.block = new SpikeBlockModel(context.bakeLayer(TFEntityRenderers.SPIKE_BLOCK));
        this.chain = new SpikeBlockModel(context.bakeLayer(TFEntityRenderers.GOBLIN_CHAIN));
    }

    @Override
    public TFRenderStates.BlockGoblin createRenderState() {
        return new TFRenderStates.BlockGoblin();
    }

    @Override
    public void extractRenderState(BlockGoblin entity, TFRenderStates.BlockGoblin state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // the synced angle, run back to the last tick as the parts' interpolation did (16 degrees per tick)
        float angle = entity.getChainAngle() - 16.0F * (1.0F - partialTick);
        Vec3 pos = entity.chainPosition(angle, entity.getChainLength());
        state.blockX = pos.x - entity.getX();
        state.blockY = pos.y - entity.getY();
        state.blockZ = pos.z - entity.getZ();
        state.blockYaw = angle;
        double sy = entity.getBbHeight() - 0.1;
        double ox = -state.blockX;
        double oy = sy - state.blockY - BLOCK_HEIGHT / 3.0;
        double oz = -state.blockZ;
        for (int i = 0; i < 3; i++) {
            state.chainX[i] = -ox * CHAIN_STEPS[i];
            state.chainY[i] = sy - oy * CHAIN_STEPS[i];
            state.chainZ[i] = -oz * CHAIN_STEPS[i];
        }
    }

    @Override
    public void submit(TFRenderStates.BlockGoblin state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.isInvisible) {
            return;
        }
        this.part(this.block, state, poseStack, collector, state.blockX, state.blockY, state.blockZ, state.blockYaw);
        for (int i = 0; i < 3; i++) {
            this.part(this.chain, state, poseStack, collector, state.chainX[i], state.chainY[i], state.chainZ[i], 0.0F);
        }
    }

    private void part(EntityModel<EntityRenderState> model, TFRenderStates.BlockGoblin state, PoseStack poseStack, SubmitNodeCollector collector,
            double x, double y, double z, float yaw) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.rotateDegrees(Axis.YP, 180.0F - Mth.wrapDegrees(yaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        collector.submitModel(model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    /** The ball swings up to 1.3 blocks out. */
    @Override
    protected AABB getBoundingBoxForCulling(BlockGoblin entity, float partialTick) {
        return super.getBoundingBoxForCulling(entity, partialTick).inflate(1.5);
    }

    @Override
    public Identifier getTextureLocation(TFRenderStates.BlockGoblin state) {
        return TEXTURE;
    }
}
