package projectzulu.client.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import projectzulu.ProjectZulu;
import projectzulu.block.gear.TombstoneBlock;
import projectzulu.block.gear.TombstoneBlockEntity;

import java.util.List;

/**
 * TileEntityTombstoneRenderer: ModelTombstone (base, body, top and the two slanted top pieces) turned by 45 degrees
 * per rotation step, and the five text rows in black on its front.
 */
public class TombstoneRenderer implements BlockEntityRenderer<TombstoneBlockEntity, TombstoneRenderer.State> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "tombstone"), "main");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "textures/entity/tombstone.png");
    private static final List<String> BODY = List.of("body4", "topcenter", "body3", "body2", "body1", "baselef", "baserig");
    /** f5 of render1/render2. */
    private static final float MODEL_SCALE = 0.0459F * 16.0F;

    public static class State extends BlockEntityRenderState {
        float rotation;
        String[] lines = new String[0];
    }

    /** render1 (the body parts) or render2 (CENTERROT with the slanted top), each turned by the angle. */
    static final class TombstoneModel extends Model<Float> {
        private final boolean top;

        TombstoneModel(ModelPart root, boolean top) {
            super(root, RenderTypes::entityCutout);
            this.top = top;
        }

        @Override
        public void setupAnim(Float angle) {
            super.setupAnim(angle);
            this.resetPose();
            float yRot = (float) (angle * Math.PI / 180.0);
            for (String name : BODY) {
                ModelPart part = this.root.getChild(name);
                part.visible = !this.top;
                part.yRot = yRot;
            }
            ModelPart center = this.root.getChild("CENTERROT");
            center.visible = this.top;
            center.yRot = yRot;
        }
    }

    private final TombstoneModel body;
    private final TombstoneModel top;
    private final Font font;

    public TombstoneRenderer(BlockEntityRendererProvider.Context context) {
        this.body = new TombstoneModel(context.bakeLayer(LAYER), false);
        this.top = new TombstoneModel(context.bakeLayer(LAYER), true);
        this.font = context.font();
    }

    /** ModelTombstone (64x32). */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body4", CubeListBuilder.create()
                .texOffs(28, 15).addBox(0.0F, -18.0F, -4.5F, 9.0F, 8.0F, 9.0F),
                PartPose.offset(0.0F, 28.0F, 0.0F));
        root.addOrReplaceChild("topcenter", CubeListBuilder.create()
                .texOffs(28, 16).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 4.0F, 9.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("body3", CubeListBuilder.create()
                .texOffs(28, 15).addBox(-9.0F, -18.0F, -4.5F, 9.0F, 8.0F, 9.0F),
                PartPose.offset(0.0F, 28.0F, 0.0F));
        root.addOrReplaceChild("body2", CubeListBuilder.create()
                .texOffs(28, 15).addBox(0.0F, -18.0F, -4.5F, 9.0F, 8.0F, 9.0F),
                PartPose.offset(0.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("body1", CubeListBuilder.create()
                .texOffs(28, 15).addBox(-9.0F, -18.0F, -4.5F, 9.0F, 8.0F, 9.0F),
                PartPose.offset(0.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("baselef", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12.0F, -3.0F, -6.0F, 12.0F, 6.0F, 12.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        root.addOrReplaceChild("baserig", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, -3.0F, -6.0F, 12.0F, 6.0F, 12.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        PartDefinition center = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 1.2F, 0.0F));
        center.addOrReplaceChild("TOPLEFROT", CubeListBuilder.create().mirror()
                .texOffs(0, 17).addBox(-1.5F, -3.0F, -4.5F, 3.0F, 6.0F, 9.0F),
                PartPose.offsetAndRotation(-5.8F, 0.0F, 0.0F, 0.0F, 0.0F, 0.837758F));
        center.addOrReplaceChild("TOPRIGROT", CubeListBuilder.create().mirror()
                .texOffs(0, 17).addBox(-1.5F, -3.0F, -4.5F, 3.0F, 6.0F, 9.0F),
                PartPose.offsetAndRotation(5.8F, 0.0F, 0.0F, 0.0F, 0.0F, -0.837758F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TombstoneBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.rotation = entity.getBlockState().getValue(TombstoneBlock.ROTATION) * 360 / 8;
        state.lines = entity.getSignText();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.88F, 0.5F);
        poseStack.scale(-0.9F * MODEL_SCALE, -0.8F * MODEL_SCALE, 0.9F * MODEL_SCALE);
        collector.submitModel(this.body, -state.rotation, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords,
                OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.88F, 0.5F);
        poseStack.scale(-0.899F * MODEL_SCALE, -0.8F * MODEL_SCALE, 0.899F * MODEL_SCALE);
        collector.submitModel(this.top, -state.rotation, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords,
                OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        poseStack.pushPose();
        float size = 0.6666667F;
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, state.rotation);
        poseStack.translate(0.0F, 0.5F * size - 0.35F, 0.07F * size + 0.18F);
        float textScale = 0.016666668F * size;
        poseStack.scale(textScale, -textScale, textScale);
        for (int i = 0; i < state.lines.length; i++) {
            String line = state.lines[i];
            collector.submitText(poseStack, -this.font.width(line) / 2, i * 10 - state.lines.length * 5,
                    Component.literal(line).getVisualOrderText(), false, Font.DisplayMode.POLYGON_OFFSET, state.lightCoords, 0xFF000000, 0, 0);
        }
        poseStack.popPose();
    }
}
