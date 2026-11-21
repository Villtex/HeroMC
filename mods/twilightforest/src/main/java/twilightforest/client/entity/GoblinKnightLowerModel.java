package twilightforest.client.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** ModelTFGoblinKnightLower: the lower goblin knight, the tunic while armoured; the head stays still under a rider. */
public class GoblinKnightLowerModel extends HumanoidModel<TFRenderStates.GoblinKnight> {
    private final ModelPart tunic;

    public GoblinKnightLowerModel(ModelPart root) {
        super(root);
        this.tunic = root.getChild("tunic");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-2.5F, -5.0F, -3.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 10.0F, 1.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 8.0F, 4.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 48).addBox(-2.0F, -2.0F, -1.5F, 2.0F, 8.0F, 3.0F),
                PartPose.offset(-3.5F, 10.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
                .texOffs(40, 48).addBox(0.0F, -2.0F, -1.5F, 2.0F, 8.0F, 3.0F),
                PartPose.offset(3.5F, 10.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-3.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(-2.5F, 16.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror()
                .texOffs(0, 48).addBox(-1.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(2.5F, 16.0F, 0.0F));
        root.addOrReplaceChild("tunic", CubeListBuilder.create()
                .texOffs(64, 19).addBox(-6.0F, 0.0F, -3.0F, 12.0F, 9.0F, 6.0F),
                PartPose.offset(0.0F, 7.5F, 0.0F));
        OldBiped.fill(root);
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(TFRenderStates.GoblinKnight s) {
        this.resetPose();
        float f = s.walkAnimationPos;
        float f1 = s.walkAnimationSpeed;
        float f2 = s.ageInTicks;
        this.tunic.visible = s.hasArmor;
        this.head.yRot = s.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = s.xRot * Mth.DEG_TO_RAD;
        this.rightArm.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 2.0F * f1 * 0.5F;
        this.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
        this.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        this.leftLeg.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
        if (s.isVehicle) {
            this.head.yRot = 0.0F;
            this.head.xRot = 0.0F;
        }
        this.rightArm.zRot = Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot = -(Mth.cos(f2 * 0.09F) * 0.05F + 0.05F);
        this.rightArm.xRot += Mth.sin(f2 * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(f2 * 0.067F) * 0.05F;
    }
}
