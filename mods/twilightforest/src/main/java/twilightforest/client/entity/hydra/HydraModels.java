package twilightforest.client.entity.hydra;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * The models of the hydra (texture hydra4.png, 512 x 256): ModelTFHydra (body, legs and tail; its three necks and
 * heads were built but never drawn), ModelTFHydraHead, ModelTFHydraNeck, and ModelTFHydraMortar (32 x 32).
 */
public final class HydraModels {
    private HydraModels() {
    }

    /** What a head or a neck segment needs: its yaw and pitch (degrees, absolute), the mouth and the death tilt. */
    public static class PartState extends EntityRenderState {
        public boolean visible;
        public double dx;
        public double dy;
        public double dz;
        public float yaw;
        public float pitch;
        public float mouthOpen;
        public boolean red;
        /** deathTime + partial tick of a dying head (RenderLiving.rotateCorpse), 0 otherwise. */
        public float deathTime;
    }

    /** The head entity: the head itself and the five neck segments it draws (RenderTFHydraHead). */
    public static class HeadState extends PartState {
        public final PartState[] necks = new PartState[5];

        public HeadState() {
            for (int i = 0; i < this.necks.length; i++) {
                this.necks[i] = new PartState();
            }
        }
    }

    /** The mortar: fuse - partialTick + 1 and the fuse itself (for the flashing). */
    public static class MortarState extends EntityRenderState {
        public float fuseTime;
        public int fuse;
    }

    // ------------------------------------------------------------------------------------------------ body

    /** ModelTFHydra.render: body, both legs (walking swing) and the four tail segments. */
    public static class Body extends EntityModel<LivingEntityRenderState> {
        private final ModelPart leg1;
        private final ModelPart leg2;

        public Body(ModelPart root) {
            super(root);
            this.leg1 = root.getChild("leg1");
            this.leg2 = root.getChild("leg2");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("body", CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-48.0F, 0.0F, 0.0F, 96.0F, 96.0F, 40.0F),
                    PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 1.22173F, 0.0F, 0.0F));
            root.addOrReplaceChild("leg1", CubeListBuilder.create()
                    .texOffs(0, 136).addBox(-16.0F, 0.0F, -16.0F, 32.0F, 48.0F, 32.0F)
                    .texOffs(184, 200).addBox(-20.0F, 40.0F, -20.0F, 8.0F, 8.0F, 8.0F)
                    .texOffs(184, 200).addBox(-4.0F, 40.0F, -22.0F, 8.0F, 8.0F, 8.0F)
                    .texOffs(184, 200).addBox(12.0F, 40.0F, -20.0F, 8.0F, 8.0F, 8.0F),
                    PartPose.offset(48.0F, -24.0F, 0.0F));
            root.addOrReplaceChild("leg2", CubeListBuilder.create().mirror()
                    .texOffs(0, 136).addBox(-16.0F, 0.0F, -16.0F, 32.0F, 48.0F, 32.0F)
                    .texOffs(184, 200).addBox(-20.0F, 40.0F, -20.0F, 8.0F, 8.0F, 8.0F)
                    .texOffs(184, 200).addBox(-4.0F, 40.0F, -22.0F, 8.0F, 8.0F, 8.0F)
                    .texOffs(184, 200).addBox(12.0F, 40.0F, -20.0F, 8.0F, 8.0F, 8.0F),
                    PartPose.offset(-48.0F, -24.0F, 0.0F));
            float[][] tails = {{6.0F, 108.0F}, {7.0F, 142.0F}, {8.0F, 176.0F}, {8.0F, 210.0F}};
            for (int i = 0; i < tails.length; i++) {
                root.addOrReplaceChild("tail" + (i + 1), CubeListBuilder.create()
                        .texOffs(128, 136).addBox(-16.0F, -16.0F, -16.0F, 32.0F, 32.0F, 32.0F)
                        .texOffs(128, 200).addBox(-2.0F, -28.0F, -11.0F, 4.0F, 24.0F, 24.0F),
                        PartPose.offset(0.0F, tails[i][0], tails[i][1]));
            }
            return LayerDefinition.create(mesh, 512, 256);
        }

        @Override
        public void setupAnim(LivingEntityRenderState state) {
            super.setupAnim(state);
            float f = state.walkAnimationPos;
            float f1 = state.walkAnimationSpeed;
            this.leg1.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
            this.leg2.xRot = Mth.cos(f * 0.6662F + 3.141593F) * 1.4F * f1;
            this.leg1.yRot = 0.0F;
            this.leg2.yRot = 0.0F;
        }
    }

    // ------------------------------------------------------------------------------------------------ head

    /** ModelTFHydraHead.setLivingAnimations: the world yaw and pitch of the head; the open mouth tips it up. */
    public static class Head extends EntityModel<PartState> {
        private final ModelPart head;
        private final ModelPart jaw;

        public Head(ModelPart root) {
            super(root);
            this.head = root.getChild("head");
            this.jaw = this.head.getChild("jaw");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                    .texOffs(272, 0).addBox(-16.0F, -14.0F, -32.0F, 32.0F, 24.0F, 32.0F)
                    .texOffs(272, 56).addBox(-15.0F, -2.0F, -56.0F, 30.0F, 12.0F, 24.0F)
                    .texOffs(272, 132).addBox(-15.0F, 10.0F, -20.0F, 30.0F, 8.0F, 16.0F)
                    .texOffs(128, 200).addBox(-2.0F, -30.0F, -12.0F, 4.0F, 24.0F, 24.0F)
                    .texOffs(272, 156).addBox(-12.0F, 10.0F, -49.0F, 2.0F, 5.0F, 2.0F)
                    .texOffs(272, 156).addBox(10.0F, 10.0F, -49.0F, 2.0F, 5.0F, 2.0F)
                    .texOffs(280, 156).addBox(-8.0F, 9.0F, -49.0F, 16.0F, 2.0F, 2.0F)
                    .texOffs(280, 160).addBox(-10.0F, 9.0F, -45.0F, 2.0F, 2.0F, 16.0F)
                    .texOffs(280, 160).addBox(8.0F, 9.0F, -45.0F, 2.0F, 2.0F, 16.0F),
                    PartPose.offset(0.0F, 0.0F, 0.0F));
            head.addOrReplaceChild("jaw", CubeListBuilder.create()
                    .texOffs(272, 92).addBox(-15.0F, 0.0F, -32.0F, 30.0F, 8.0F, 32.0F)
                    .texOffs(272, 156).addBox(-10.0F, -5.0F, -29.0F, 2.0F, 5.0F, 2.0F)
                    .texOffs(272, 156).addBox(8.0F, -5.0F, -29.0F, 2.0F, 5.0F, 2.0F)
                    .texOffs(280, 156).addBox(-8.0F, -1.0F, -29.0F, 16.0F, 2.0F, 2.0F)
                    .texOffs(280, 160).addBox(-10.0F, -1.0F, -25.0F, 2.0F, 2.0F, 16.0F)
                    .texOffs(280, 160).addBox(8.0F, -1.0F, -25.0F, 2.0F, 2.0F, 16.0F),
                    PartPose.offset(0.0F, 10.0F, -20.0F));
            head.addOrReplaceChild("frill", CubeListBuilder.create()
                    .texOffs(272, 200).addBox(-24.0F, -40.0F, 0.0F, 48.0F, 48.0F, 4.0F),
                    PartPose.offsetAndRotation(0.0F, 0.0F, -14.0F, (float) (-Math.PI / 6), 0.0F, 0.0F));
            return LayerDefinition.create(mesh, 512, 256);
        }

        @Override
        public void setupAnim(PartState state) {
            super.setupAnim(state);
            this.head.yRot = state.yaw * Mth.DEG_TO_RAD;
            this.head.xRot = state.pitch * Mth.DEG_TO_RAD - (float) (state.mouthOpen * (Math.PI / 12));
            this.jaw.xRot = (float) (state.mouthOpen * (Math.PI / 3));
        }
    }

    // ------------------------------------------------------------------------------------------------ neck

    /** ModelTFHydraNeck: one segment, turned by the segment's yaw and pitch. */
    public static class Neck extends EntityModel<PartState> {
        private final ModelPart neck;

        public Neck(ModelPart root) {
            super(root);
            this.neck = root.getChild("neck");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("neck", CubeListBuilder.create()
                    .texOffs(128, 136).addBox(-16.0F, -16.0F, -16.0F, 32.0F, 32.0F, 32.0F)
                    .texOffs(128, 200).addBox(-2.0F, -23.0F, 0.0F, 4.0F, 24.0F, 24.0F),
                    PartPose.offset(0.0F, 0.0F, 0.0F));
            return LayerDefinition.create(mesh, 512, 256);
        }

        @Override
        public void setupAnim(PartState state) {
            super.setupAnim(state);
            this.neck.yRot = state.yaw * Mth.DEG_TO_RAD;
            this.neck.xRot = state.pitch * Mth.DEG_TO_RAD;
        }
    }

    // ------------------------------------------------------------------------------------------------ mortar

    /** ModelTFHydraMortar: an 8³ box standing on its origin. */
    public static class Mortar extends EntityModel<MortarState> {
        public Mortar(ModelPart root) {
            super(root);
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("box", CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                    PartPose.offset(0.0F, 0.0F, 0.0F));
            return LayerDefinition.create(mesh, 32, 32);
        }
    }
}
