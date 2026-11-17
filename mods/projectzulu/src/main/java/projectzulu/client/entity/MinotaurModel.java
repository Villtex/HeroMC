package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * ModelMinotaur (128 x 64): a horned bull head on a broad torso with a belt, two-piece arms (the left one holds a
 * double axe) and hoofed legs. Walking swings arms and legs (the belt flaps follow the legs); while attacking the
 * arms are held forward, and during the attack animation (animTime 20 -> 10) the body leans in and both arms swing
 * the axe down from overhead.
 */
public class MinotaurModel extends EntityModel<MinotaurModel.State> {
    private final ModelPart bodyrot;
    private final ModelPart headrot;
    private final ModelPart armtoprigrot;
    private final ModelPart armbotrigrot;
    private final ModelPart armtoplefrot;
    private final ModelPart armbotlefrot;
    private final ModelPart hammerrot;
    private final ModelPart finrig1rot;
    private final ModelPart finrig2rot;
    private final ModelPart finlef1rot;
    private final ModelPart finlef2rot;
    private final ModelPart legleftoprot;
    private final ModelPart leglefbotrot;
    private final ModelPart legrigtoprot;
    private final ModelPart legrigbotrot;
    private final ModelPart backbelt2rot;
    private final ModelPart frontbelt2rot;

    public static class State extends LivingEntityRenderState {
        public boolean attacking;
        public int animTime;
    }

    public MinotaurModel(ModelPart root) {
        super(root);
        this.bodyrot = root.getChild("bodyrot");
        this.headrot = this.bodyrot.getChild("headrot");
        this.armtoprigrot = this.bodyrot.getChild("armtoprigrot");
        this.armbotrigrot = this.armtoprigrot.getChild("armbotrigrot");
        ModelPart handrot = this.armbotrigrot.getChild("handrot");
        this.finrig1rot = handrot.getChild("finrig1rot");
        this.finrig2rot = handrot.getChild("finrig2rot");
        this.armtoplefrot = this.bodyrot.getChild("armtoplefrot");
        this.armbotlefrot = this.armtoplefrot.getChild("armbotlefrot");
        this.hammerrot = this.armbotlefrot.getChild("hammerrot");
        ModelPart handlefrot = this.armbotlefrot.getChild("handlefrot");
        this.finlef1rot = handlefrot.getChild("finlef1rot");
        this.finlef2rot = handlefrot.getChild("finlef2rot");
        this.legleftoprot = root.getChild("legleftoprot");
        this.leglefbotrot = this.legleftoprot.getChild("leglefbotrot");
        this.legrigtoprot = root.getChild("legrigtoprot");
        this.legrigbotrot = this.legrigtoprot.getChild("legrigbotrot");
        this.backbelt2rot = root.getChild("backbelt2rot");
        this.frontbelt2rot = root.getChild("frontbelt2rot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bodyrot = root.addOrReplaceChild("bodyrot", CubeListBuilder.create().texOffs(0, 28).mirror().addBox(-5.5F, -14.0F, -3.5F, 11, 14, 7).mirror(false).texOffs(14, 0).mirror().addBox(-5.5F, -2.0F, -4.5F, 11, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(5.5F, -2.0F, -4.5F, 1, 2, 9).mirror(false).texOffs(0, 0).mirror().addBox(-6.5F, -2.0F, -4.5F, 1, 2, 9).mirror(false).texOffs(14, 0).mirror().addBox(-5.5F, -2.0F, 3.5F, 11, 2, 1).mirror(false),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition headrot = bodyrot.addOrReplaceChild("headrot", CubeListBuilder.create().texOffs(0, 49).mirror().addBox(-4.0F, -7.0F, -3.0F, 8, 8, 7).mirror(false).texOffs(30, 49).mirror().addBox(-2.5F, -3.5F, -6.0F, 5, 4, 3).mirror(false).texOffs(30, 56).mirror().addBox(-2.0F, -2.5F, -8.0F, 4, 3, 2).mirror(false),
                PartPose.offset(0.0F, -15.0F, -1.0F));
        PartDefinition hornlef1rot = headrot.addOrReplaceChild("hornlef1rot", CubeListBuilder.create().texOffs(54, 58).mirror().addBox(-2.0F, -1.5F, -1.5F, 2, 3, 3).mirror(false),
                PartPose.offset(-4.0F, -3.0F, 0.5F));
        PartDefinition hornlef2rot = hornlef1rot.addOrReplaceChild("hornlef2rot", CubeListBuilder.create().texOffs(50, 53).mirror().addBox(-4.0F, -2.0F, -1.5F, 4, 2, 3).mirror(false),
                PartPose.offsetAndRotation(-2.0F, 1.5F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4)));
        hornlef2rot.addOrReplaceChild("hornlef3rot", CubeListBuilder.create().texOffs(42, 60).mirror().addBox(-4.0F, -2.0F, -1.0F, 4, 2, 2).mirror(false),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7504916F));
        PartDefinition hornrig1rot = headrot.addOrReplaceChild("hornrig1rot", CubeListBuilder.create().texOffs(54, 58).mirror().addBox(0.0F, -1.5F, -1.5F, 2, 3, 3).mirror(false),
                PartPose.offset(4.0F, -3.0F, 0.5F));
        PartDefinition hornrig2rot = hornrig1rot.addOrReplaceChild("hornrig2rot", CubeListBuilder.create().texOffs(50, 53).mirror().addBox(0.0F, -2.0F, -1.5F, 4, 2, 3).mirror(false),
                PartPose.offsetAndRotation(2.0F, 1.5F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 4)));
        hornrig2rot.addOrReplaceChild("hornrig3rot", CubeListBuilder.create().texOffs(42, 60).mirror().addBox(0.0F, -2.0F, -1.0F, 4, 2, 2).mirror(false),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7504916F));
        PartDefinition armtoprigrot = bodyrot.addOrReplaceChild("armtoprigrot", CubeListBuilder.create().texOffs(36, 22).mirror().addBox(-2.5F, -2.0F, -2.5F, 5, 9, 5).mirror(false),
                PartPose.offset(8.0F, -11.0F, 0.0F));
        PartDefinition armbotrigrot = armtoprigrot.addOrReplaceChild("armbotrigrot", CubeListBuilder.create().texOffs(36, 36).mirror().addBox(-2.5F, 0.0F, -2.5F, 5, 8, 5).mirror(false),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition handrot = armbotrigrot.addOrReplaceChild("handrot", CubeListBuilder.create().texOffs(38, 15).mirror().addBox(-1.0F, 0.0F, -2.5F, 2, 2, 5).mirror(false),
                PartPose.offset(1.5F, 8.0F, 0.0F));
        handrot.addOrReplaceChild("finrig2rot", CubeListBuilder.create().texOffs(56, 22).mirror().addBox(-1.0F, -1.0F, 0.5F, 2, 3, 2).mirror(false),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        handrot.addOrReplaceChild("finrig1rot", CubeListBuilder.create().texOffs(56, 22).mirror().addBox(-1.0F, -1.0F, -2.5F, 2, 3, 2).mirror(false),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        armbotrigrot.addOrReplaceChild("finrigrot3", CubeListBuilder.create().texOffs(54, 16).mirror().addBox(-0.5F, 0.0F, -2.0F, 1, 2, 4).mirror(false),
                PartPose.offset(-2.0F, 8.0F, 0.0F));
        PartDefinition armtoplefrot = bodyrot.addOrReplaceChild("armtoplefrot", CubeListBuilder.create().texOffs(36, 22).mirror().addBox(-2.5F, -2.0F, -2.5F, 5, 9, 5).mirror(false).texOffs(44, 8).mirror().addBox(-3.5F, -2.0F, -3.5F, 6, 4, 1).mirror(false).texOffs(31, 7).mirror().addBox(-3.5F, -2.0F, -2.5F, 1, 4, 5).mirror(false).texOffs(44, 8).mirror().addBox(-3.5F, -2.0F, 2.5F, 6, 4, 1).mirror(false).texOffs(38, 0).mirror().addBox(-3.5F, -3.0F, -3.5F, 6, 1, 7).mirror(false),
                PartPose.offset(-8.0F, -11.0F, 0.0F));
        PartDefinition armbotlefrot = armtoplefrot.addOrReplaceChild("armbotlefrot", CubeListBuilder.create().texOffs(36, 36).mirror().addBox(-2.5F, 0.0F, -2.5F, 5, 8, 5).mirror(false),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition hammerrot = armbotlefrot.addOrReplaceChild("hammerrot", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-1.5F, -3.0F, -15.5F, 3, 2, 5).mirror(false).texOffs(64, 7).mirror().addBox(-1.0F, 1.0F, -15.5F, 2, 2, 5).mirror(false).texOffs(64, 14).mirror().addBox(-1.5F, 3.0F, -16.0F, 3, 2, 6).mirror(false).texOffs(67, 22).mirror().addBox(-0.5F, 5.0F, -18.0F, 1, 2, 10).mirror(false).texOffs(43, 30).mirror().addBox(-1.0F, -1.0F, -16.0F, 2, 2, 20).mirror(false),
                PartPose.offset(0.5F, 9.0F, 0.0F));
        hammerrot.addOrReplaceChild("axeblade2", CubeListBuilder.create().texOffs(67, 34).mirror().addBox(-0.5F, -1.5F, -4.0F, 1, 2, 4).mirror(false),
                PartPose.offsetAndRotation(0.0F, 6.5F, -18.0F, (float) (-Math.PI / 6), 0.0F, 0.0F));
        hammerrot.addOrReplaceChild("axeblade3", CubeListBuilder.create().texOffs(67, 34).mirror().addBox(-0.5F, -1.5F, 0.0F, 1, 2, 4).mirror(false),
                PartPose.offsetAndRotation(0.0F, 6.5F, -8.5F, (float) (Math.PI / 6), 0.0F, 0.0F));
        PartDefinition handlefrot = armbotlefrot.addOrReplaceChild("handlefrot", CubeListBuilder.create().texOffs(38, 15).mirror().addBox(-1.0F, 0.0F, -2.5F, 2, 2, 5).mirror(false),
                PartPose.offset(-1.5F, 8.0F, 0.0F));
        handlefrot.addOrReplaceChild("finlef2rot", CubeListBuilder.create().texOffs(56, 22).mirror().addBox(-1.0F, -1.0F, 0.5F, 2, 3, 2).mirror(false),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        handlefrot.addOrReplaceChild("finlef1rot", CubeListBuilder.create().texOffs(56, 22).mirror().addBox(-1.0F, -1.0F, -2.5F, 2, 3, 2).mirror(false),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        armbotlefrot.addOrReplaceChild("finlefrot3", CubeListBuilder.create().texOffs(54, 16).mirror().addBox(-0.5F, 0.0F, -2.0F, 1, 2, 4).mirror(false),
                PartPose.offset(2.0F, 8.0F, 0.0F));
        PartDefinition legleftoprot = root.addOrReplaceChild("legleftoprot", CubeListBuilder.create().texOffs(0, 17).mirror().addBox(-2.5F, 0.0F, -2.5F, 5, 6, 5).mirror(false),
                PartPose.offset(-3.0F, 10.0F, 0.0F));
        legleftoprot.addOrReplaceChild("leglefbotrot", CubeListBuilder.create().texOffs(0, 11).mirror().addBox(-2.5F, 7.0F, -4.5F, 1, 1, 2).mirror(false).texOffs(20, 20).mirror().addBox(-2.5F, 6.0F, -2.5F, 5, 2, 5).mirror(false).texOffs(0, 17).mirror().addBox(-2.5F, 0.0F, -2.5F, 5, 6, 5).mirror(false).texOffs(3, 12).mirror().addBox(-1.5F, 6.0F, -5.0F, 1, 2, 3).mirror(false).texOffs(3, 12).mirror().addBox(0.5F, 6.0F, -5.0F, 1, 2, 3).mirror(false).texOffs(0, 11).mirror().addBox(1.5F, 7.0F, -4.5F, 1, 1, 2).mirror(false).texOffs(11, 14).mirror().addBox(-1.5F, 7.0F, 3.5F, 3, 1, 1).mirror(false).texOffs(8, 11).mirror().addBox(-2.0F, 6.0F, 2.5F, 4, 2, 1).mirror(false),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition backbelt2rot = root.addOrReplaceChild("backbelt2rot", CubeListBuilder.create().texOffs(16, 3).mirror().addBox(-4.5F, 0.0F, -0.5F, 9, 2, 1).mirror(false),
                PartPose.offset(0.0F, 10.0F, 4.0F));
        backbelt2rot.addOrReplaceChild("backbelt3rot", CubeListBuilder.create().texOffs(18, 6).mirror().addBox(-3.5F, 0.0F, -0.5F, 7, 2, 1).mirror(false),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition frontbelt2rot = root.addOrReplaceChild("frontbelt2rot", CubeListBuilder.create().texOffs(16, 3).mirror().addBox(-4.5F, 0.0F, -0.5F, 9, 2, 1).mirror(false),
                PartPose.offset(0.0F, 10.0F, -4.0F));
        PartDefinition frontbelt3rot = frontbelt2rot.addOrReplaceChild("frontbelt3rot", CubeListBuilder.create().texOffs(18, 6).mirror().addBox(-3.5F, 0.0F, -0.5F, 7, 2, 1).mirror(false),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        frontbelt3rot.addOrReplaceChild("frontbelt4rot", CubeListBuilder.create().texOffs(20, 9).mirror().addBox(-2.5F, 0.0F, -0.5F, 5, 2, 1).mirror(false),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition legrigtoprot = root.addOrReplaceChild("legrigtoprot", CubeListBuilder.create().texOffs(0, 17).mirror().addBox(-2.5F, 0.0F, -2.5F, 5, 6, 5).mirror(false),
                PartPose.offset(3.0F, 10.0F, 0.0F));
        legrigtoprot.addOrReplaceChild("legrigbotrot", CubeListBuilder.create().texOffs(0, 11).mirror().addBox(-2.5F, 7.0F, -4.5F, 1, 1, 2).mirror(false).texOffs(20, 20).mirror().addBox(-2.5F, 6.0F, -2.5F, 5, 2, 5).mirror(false).texOffs(0, 17).mirror().addBox(-2.5F, 0.0F, -2.5F, 5, 6, 5).mirror(false).texOffs(3, 12).mirror().addBox(-1.5F, 6.0F, -5.0F, 1, 2, 3).mirror(false).texOffs(3, 12).mirror().addBox(0.5F, 6.0F, -5.0F, 1, 2, 3).mirror(false).texOffs(0, 11).mirror().addBox(1.5F, 7.0F, -4.5F, 1, 1, 2).mirror(false).texOffs(11, 14).mirror().addBox(-1.5F, 7.0F, 3.5F, 3, 1, 1).mirror(false).texOffs(8, 11).mirror().addBox(-2.0F, 6.0F, 2.5F, 4, 2, 1).mirror(false),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelHelper.mapValueWithClamp. */
    private static float mapClamp(float value, float min1, float max1, float min2, float max2) {
        float mapped = (value - min1) * ((max2 - min2) / (max1 - min1)) + min2;
        return Mth.clamp(mapped, min2, max2);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos * 0.6662F;
        float speed = state.walkAnimationSpeed;
        float step = 1.8F * (float) Math.max(Math.log(speed + 1.0F), 0.0);
        this.legleftoprot.xRot = Mth.cos(pos + Mth.PI) * step;
        this.leglefbotrot.xRot = Math.abs(Mth.cos(pos / 2.0F + Mth.PI) * step);
        this.legrigtoprot.xRot = Mth.cos(pos) * step;
        this.legrigbotrot.xRot = Math.abs(Mth.cos(pos / 2.0F) * step);
        this.frontbelt2rot.xRot = Math.min(Math.min(this.legleftoprot.xRot, this.legrigtoprot.xRot), 0.0F);
        this.backbelt2rot.xRot = Math.max(Math.max(this.legleftoprot.xRot, this.legrigtoprot.xRot), 0.0F);
        this.finrig2rot.zRot = Mth.HALF_PI;
        this.finrig1rot.zRot = Mth.HALF_PI;
        this.finlef1rot.zRot = -Mth.HALF_PI;
        this.finlef2rot.zRot = -Mth.HALF_PI;
        this.bodyrot.xRot = 0.08726646F;
        if (state.attacking) {
            this.armtoplefrot.xRot = -Mth.HALF_PI * Mth.cos(pos + Mth.PI) * speed;
            this.armtoplefrot.zRot = 0.10471976F;
            this.armbotlefrot.xRot = -Mth.PI * 2.0F / 9.0F;
            this.armtoprigrot.xRot = -Mth.HALF_PI * Mth.cos(pos) * speed;
            this.armtoprigrot.zRot = -Mth.PI / 18.0F;
            this.armbotrigrot.xRot = -Mth.PI / 6.0F;
        } else {
            this.armtoplefrot.xRot = Mth.HALF_PI * Mth.cos(pos + Mth.PI) * speed;
            this.armtoplefrot.zRot = Mth.PI / 18.0F;
            this.armbotlefrot.xRot = -Mth.PI / 6.0F;
            this.armtoprigrot.xRot = Mth.HALF_PI * Mth.cos(pos) * speed;
            this.armtoprigrot.zRot = -Mth.PI / 18.0F;
            this.armbotrigrot.xRot = -Mth.PI / 6.0F;
        }
        this.armtoplefrot.yRot = 0.0F;
        this.armbotlefrot.yRot = 0.0F;
        this.armbotlefrot.zRot = 0.0F;
        this.armtoprigrot.yRot = 0.0F;
        this.armbotrigrot.yRot = 0.0F;
        this.armbotrigrot.zRot = 0.0F;
        if (state.animTime > 0 && state.attacking) {
            float max = projectzulu.entity.ZuluMonster.MAX_ANIM_TIME;
            this.bodyrot.xRot = mapClamp(state.animTime, max, 10.0F, 0.08726646F, Mth.PI / 9.0F);
            this.armtoplefrot.xRot = mapClamp(state.animTime, max, 10.0F, Mth.PI * 11.0F / 12.0F, 6.1086526F);
            this.armtoplefrot.zRot = 0.08726646F;
            this.armbotlefrot.zRot = -Mth.PI / 6.0F;
            this.armbotlefrot.xRot = 0.0F;
            this.armtoprigrot.xRot = mapClamp(state.animTime, max, 10.0F, Mth.PI * 11.0F / 12.0F, 6.1086526F);
            this.armtoprigrot.zRot = -0.08726646F;
            this.armbotrigrot.xRot = 0.0F;
            this.armbotrigrot.zRot = Mth.PI / 6.0F;
            this.hammerrot.xRot = 0.0F;
            this.hammerrot.yRot = 0.0F;
            this.hammerrot.zRot = Mth.PI / 9.0F;
        }
        this.headrot.xRot = Mth.clamp(state.xRot, -15.0F, 15.0F) * Mth.DEG_TO_RAD;
        this.headrot.yRot = Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
    }
}
