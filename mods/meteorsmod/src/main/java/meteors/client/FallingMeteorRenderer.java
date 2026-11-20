package meteors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import meteors.MeteorType;
import meteors.MeteorsMod;
import meteors.entity.FallingMeteor;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;

/**
 * RenderMeteor with ModelMeteor: size 1 one 16-pixel cube, size 2 the eight cubes around the centre, size 3 six more
 * on the faces; the whole model scaled by the size, the texture of the meteor type (fallingMeteor, frezaMeteor, ...).
 */
public class FallingMeteorRenderer extends EntityRenderer<FallingMeteor, FallingMeteorRenderer.State> {
    private static final float[][] RING = {{8, 16, 8}, {8, 0, 8}, {-8, 0, -8}, {8, 0, -8}, {-8, 16, -8}, {8, 16, -8}, {-8, 0, 8}, {-8, 16, 8}};
    private static final float[][] OUTER = {{0, 8, 16}, {0, 8, -16}, {0, -8, 0}, {0, 24, 0}, {16, 8, 0}, {-16, 8, 0}};
    private final ModelPart small;
    private final ModelPart ring;
    private final ModelPart outer;

    public static class State extends EntityRenderState {
        int size;
        MeteorType type = MeteorType.METEORITE;
    }

    public FallingMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.small = bake(new float[][]{{0, 16, 0}});
        this.ring = bake(RING);
        this.outer = bake(OUTER);
        this.shadowRadius = 0.5F;
    }

    private static ModelPart bake(float[][] offsets) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int i = 0; i < offsets.length; i++) {
            root.addOrReplaceChild("b" + i, CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                    PartPose.offset(offsets[i][0], offsets[i][1], offsets[i][2]));
        }
        return LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected int getBlockLightLevel(FallingMeteor entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void extractRenderState(FallingMeteor entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = entity.getSize();
        state.type = entity.getMeteorType();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        float s = Math.max(1, state.size);
        poseStack.scale(s, s, s);
        var type = RenderTypes.entityCutout(MeteorsMod.id("textures/entity/" + state.type.texture + ".png"));
        if (state.size <= 1) {
            collector.submitModelPart(this.small, poseStack, type, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        } else {
            collector.submitModelPart(this.ring, poseStack, type, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
            if (state.size >= 3) {
                collector.submitModelPart(this.outer, poseStack, type, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
            }
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
