package twilightforest.client.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * ModelTFGoblinKnightUpper: the upper goblin knight with horned helmet, spear in the right hand, the shield on the left
 * arm (hidden when broken) and the breastplate while armoured; the spear arm swings back during the heavy spear attack.
 */
public class GoblinKnightUpperModel extends HumanoidModel<TFRenderStates.GoblinKnight> {
    private final ModelPart shield;
    private final ModelPart breastplate;

    public GoblinKnightUpperModel(ModelPart root) {
        super(root);
        this.shield = this.leftArm.getChild("shield");
        this.breastplate = root.getChild("breastplate");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));
        hat.addOrReplaceChild("helmet", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -11.0F, -3.5F, 7.0F, 11.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F));
        PartDefinition righthorn1 = hat.addOrReplaceChild("righthorn1", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-6.0F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-3.5F, -9.0F, 0.0F, 0.0F, (float) (Math.PI / 12), 0.17453294F));
        righthorn1.addOrReplaceChild("righthorn2", CubeListBuilder.create()
                .texOffs(28, 6).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-5.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.17453294F));
        PartDefinition lefthorn1 = hat.addOrReplaceChild("lefthorn1", CubeListBuilder.create().mirror()
                .texOffs(28, 0).addBox(-1.0F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(3.5F, -9.0F, 0.0F, 0.0F, (float) (-Math.PI / 12), -0.17453294F));
        lefthorn1.addOrReplaceChild("lefthorn2", CubeListBuilder.create()
                .texOffs(28, 6).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(5.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.17453294F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-5.5F, 0.0F, -2.0F, 11.0F, 8.0F, 4.0F)
                .texOffs(30, 24).addBox(-6.5F, 0.0F, -2.0F, 1.0F, 4.0F, 4.0F)
                .texOffs(30, 24).addBox(5.5F, 0.0F, -2.0F, 1.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(44, 16).addBox(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-6.5F, 14.0F, 0.0F));
        rightArm.addOrReplaceChild("spear", CubeListBuilder.create()
                .texOffs(108, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 30.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, 8.5F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
                .texOffs(44, 16).addBox(0.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(6.5F, 14.0F, 0.0F));
        leftArm.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(63, 36).addBox(-6.0F, -6.0F, -2.0F, 12.0F, 20.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(30, 16).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 4.0F, 4.0F),
                PartPose.offset(-4.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror()
                .texOffs(30, 16).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 4.0F, 4.0F),
                PartPose.offset(4.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("breastplate", CubeListBuilder.create()
                .texOffs(64, 0).addBox(-6.5F, 0.0F, -3.0F, 13.0F, 12.0F, 6.0F),
                PartPose.offset(0.0F, 11.5F, 0.0F));
        OldBiped.fill(root);
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** RenderTFGoblinKnightUpper.getPitchForAttack: the body leans into the heavy spear thrust. */
    static float pitchForAttack(float t) {
        if (t <= 10.0F) {
            return t * 3.0F;
        } else if (t <= 30.0F) {
            return 30.0F;
        } else if (t <= 33.0F) {
            return (t - 30.0F) * -25.0F + 30.0F;
        } else if (t <= 50.0F) {
            return -45.0F;
        }
        return t <= 60.0F ? (10.0F - (t - 50.0F)) * -4.5F : 0.0F;
    }

    private static float armRotationDuringSwing(float t) {
        if (t <= 10.0F) {
            return t;
        } else if (t <= 30.0F) {
            return 10.0F;
        } else if (t <= 33.0F) {
            return (t - 30.0F) * -8.0F + 10.0F;
        } else if (t <= 50.0F) {
            return -15.0F;
        }
        return t <= 60.0F ? (10.0F - (t - 50.0F)) * -1.5F : 0.0F;
    }

    @Override
    public void setupAnim(TFRenderStates.GoblinKnight s) {
        this.resetPose();
        float f = s.walkAnimationPos;
        float f1 = s.walkAnimationSpeed;
        float f2 = s.ageInTicks;
        this.shield.visible = s.hasShield;
        this.breastplate.visible = s.hasArmor;
        this.head.yRot = s.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = s.xRot * Mth.DEG_TO_RAD;
        this.head.zRot = 0.0F;
        this.rightArm.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 2.0F * f1 * 0.5F;
        float leftConstraint = s.hasShield ? 0.2F : f1;
        this.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * leftConstraint * 0.5F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        this.leftLeg.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        if (s.isPassenger) {
            this.rightArm.xRot += -Mth.PI / 5.0F;
            this.leftArm.xRot += -Mth.PI / 5.0F;
            this.rightLeg.xRot = 0.0F;
            this.leftLeg.xRot = 0.0F;
        }
        // heldItemRight is forced to 1
        this.rightArm.xRot = this.rightArm.xRot * 0.5F - Mth.PI / 10.0F;
        this.rightArm.xRot = (float) (this.rightArm.xRot - 2.0734511513692637);
        if (s.heavySpearTimer > 0) {
            // 1.5.2 passes the model scale (0.0625) here, not the partial tick
            this.rightArm.xRot -= armRotationDuringSwing(60 - s.heavySpearTimer + 0.0625F) * Mth.DEG_TO_RAD;
        }
        this.rightArm.yRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        this.rightArm.zRot += Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(f2 * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(f2 * 0.067F) * 0.05F;
        this.leftArm.zRot = -this.leftArm.zRot;
        this.shield.xRot = (float) (Math.PI * 2 - this.leftArm.xRot);
    }
}
