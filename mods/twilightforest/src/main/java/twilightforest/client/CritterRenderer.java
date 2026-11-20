package twilightforest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.TwilightForest;
import twilightforest.block.CritterBlock;
import twilightforest.block.CritterBlockEntity;

/**
 * TileEntityTFFireflyRenderer, TileEntityTFCicadaRenderer and TileEntityTFMoonwormRenderer with ModelTFFirefly,
 * ModelTFCicada and ModelTFMoonworm: turned to the side the critter sits on, then by its yaw, then drawn up-side
 * down (glScalef(1, -1, -1)) as the originals did. The firefly's glow is drawn additively with its intensity.
 */
public class CritterRenderer implements BlockEntityRenderer<CritterBlockEntity, CritterRenderer.State> {
    public static final ModelLayerLocation FIREFLY = new ModelLayerLocation(TwilightForest.id("firefly"), "main");
    public static final ModelLayerLocation CICADA = new ModelLayerLocation(TwilightForest.id("cicada"), "main");
    public static final ModelLayerLocation MOONWORM = new ModelLayerLocation(TwilightForest.id("moonworm"), "main");
    /** firefly-tiny.png, also the sprite of the little firefly particle. */
    public static final Identifier FIREFLY_TEXTURE = TwilightForest.id("textures/particle/firefly.png");
    public static final Identifier CICADA_TEXTURE = TwilightForest.id("textures/entity/cicada.png");
    public static final Identifier MOONWORM_TEXTURE = TwilightForest.id("textures/entity/moonworm.png");

    private final ModelPart fireflyBody;
    private final ModelPart fireflyGlow;
    private final ModelPart cicada;
    private final MoonwormModel moonworm;

    public static class State extends BlockEntityRenderState {
        CritterBlock.Kind kind = CritterBlock.Kind.FIREFLY;
        int facing;
        float yaw;
        float glow;
        boolean wiggling;
        float wiggleTime;
    }

    public CritterRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart firefly = context.bakeLayer(FIREFLY);
        this.fireflyBody = firefly.getChild("body");
        this.fireflyGlow = firefly.getChild("glow");
        this.cicada = context.bakeLayer(CICADA);
        this.moonworm = new MoonwormModel(context.bakeLayer(MOONWORM));
    }

    /** ModelTFFirefly (64 x 32): legs, fat and skinny body; the glow is a flat 10 x 10 plane above them. */
    public static LayerDefinition firefly() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("legs", CubeListBuilder.create().texOffs(0, 21).addBox(-4.0F, 7.9F, -5.0F, 8.0F, 1.0F, 10.0F), PartPose.ZERO);
        body.addOrReplaceChild("fatbody", CubeListBuilder.create().texOffs(0, 11).addBox(-2.0F, 6.0F, -4.0F, 4.0F, 2.0F, 6.0F), PartPose.ZERO);
        body.addOrReplaceChild("skinnybody", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 7.0F, -5.0F, 2.0F, 1.0F, 8.0F), PartPose.ZERO);
        root.addOrReplaceChild("glow", CubeListBuilder.create().texOffs(20, 0).addBox(-5.0F, 5.9F, -9.0F, 10.0F, 0.0F, 10.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelTFCicada (64 x 32). */
    public static LayerDefinition cicada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("legs", CubeListBuilder.create().texOffs(0, 21).addBox(-4.0F, 7.9F, -5.0F, 8.0F, 1.0F, 9.0F), PartPose.ZERO);
        root.addOrReplaceChild("fatbody", CubeListBuilder.create().texOffs(0, 11).addBox(-2.0F, 6.0F, -4.0F, 4.0F, 2.0F, 6.0F), PartPose.ZERO);
        root.addOrReplaceChild("skinnybody", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 7.0F, -5.0F, 2.0F, 1.0F, 8.0F), PartPose.ZERO);
        root.addOrReplaceChild("eye1", CubeListBuilder.create().texOffs(20, 15).addBox(1.0F, 5.0F, 2.0F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(20, 15).addBox(-3.0F, 5.0F, 2.0F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("wings", CubeListBuilder.create().texOffs(20, 0).addBox(-4.0F, 5.0F, -7.0F, 8.0F, 1.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelTFMoonworm (32 x 32): four segments at y 7. */
    public static LayerDefinition moonworm() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 7.0F, 3.0F));
        root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 4.0F), PartPose.offset(3.0F, 7.0F, 0.0F));
        root.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(2.0F, 7.0F, -2.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(-3.0F, 7.0F, 2.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** ModelTFMoonworm.setLivingAnimations: while it turns, the segments lift one after the other. */
    static class MoonwormModel extends Model<State> {
        private final ModelPart head;
        private final ModelPart shape1;
        private final ModelPart shape2;
        private final ModelPart shape3;

        MoonwormModel(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.head = root.getChild("head");
            this.shape1 = root.getChild("shape1");
            this.shape2 = root.getChild("shape2");
            this.shape3 = root.getChild("shape3");
        }

        @Override
        public void setupAnim(State state) {
            float t = state.wiggleTime;
            this.head.y = 7.0F + (state.wiggling ? Math.min(0.0F, Mth.sin(t / 2.0F)) : 0.0F);
            this.shape1.y = 7.0F + (state.wiggling ? Math.min(0.0F, Mth.sin(t / 2.0F + 1.0F)) : 0.0F);
            this.shape2.y = 7.0F + (state.wiggling ? Math.min(0.0F, Mth.sin(t / 2.0F + 2.0F)) : 0.0F);
            this.shape3.y = 7.0F + (state.wiggling ? Math.min(0.0F, Mth.sin(t / 2.0F + 3.0F)) : 0.0F);
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CritterBlockEntity be, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, camera, breakProgress);
        state.kind = be.kind();
        state.facing = CritterBlock.meta(be.getBlockState().getValue(CritterBlock.FACING));
        state.yaw = be.currentYaw;
        state.glow = Mth.clamp(be.glowIntensity, 0.0F, 1.0F);
        state.wiggling = be.yawDelay == 0;
        state.wiggleTime = be.desiredYaw - be.currentYaw - partialTicks;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float rotX = 90.0F;
        float rotZ = 0.0F;
        switch (state.facing) {
            case 4 -> rotZ = 180.0F;
            case 1 -> rotZ = -90.0F;
            case 2 -> rotZ = 90.0F;
            case 5 -> rotX = 0.0F;
            case 6 -> rotX = 180.0F;
            default -> {
            }
        }
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.rotateDegrees(Axis.XP, rotX);
        pose.rotateDegrees(Axis.ZP, rotZ);
        pose.rotateDegrees(Axis.YP, state.yaw);
        pose.scale(1.0F, -1.0F, -1.0F);
        int light = state.lightCoords;
        switch (state.kind) {
            case FIREFLY -> {
                collector.submitModelPart(this.fireflyBody, pose, RenderTypes.entityCutout(FIREFLY_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
                if (state.glow > 0.0F) {
                    collector.submitModelPart(this.fireflyGlow, pose, RenderTypes.eyes(FIREFLY_TEXTURE), light, OverlayTexture.NO_OVERLAY, null,
                            ARGB.colorFromFloat(1.0F, state.glow, state.glow, state.glow));
                }
            }
            case CICADA -> collector.submitModelPart(this.cicada, pose, RenderTypes.entityCutout(CICADA_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
            case MOONWORM -> collector.submitModel(this.moonworm, state, pose, RenderTypes.entityCutout(MOONWORM_TEXTURE), light,
                    OverlayTexture.NO_OVERLAY, -1);
        }
        pose.popPose();
    }
}
