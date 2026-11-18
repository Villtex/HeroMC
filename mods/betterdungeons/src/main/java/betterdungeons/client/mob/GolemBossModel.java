package betterdungeons.client.mob;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * ModelGolemBoss (ModelGolemBoss(0, -7): an iron-golem-like biped of 128x128 with long arms hung from one point, nose,
 * belt, short legs). RenderGolemBoss drew it 1 block down and scaled 2 x 1.8 x 1.9. Pose: head looks, arms and legs
 * swing (arms like the golem, both from y -7), the Monking's attack wind-up, arms raised 0.5, legs pulled in the slam.
 */
public class GolemBossModel extends EntityModel<MonkingModel.State> {
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GolemBossModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        float par2 = -7.0F;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F)
                .texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, par2, -2.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F)
                .texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, par2, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(-4.0F, 18.0F + par2, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(60, 0).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(5.0F, 18.0F + par2, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(MonkingModel.State s) {
        super.setupAnim(s);
        // RenderGolemBoss.renderModel: glTranslatef(0, -1, 0), glScalef(2, 1.8, 1.9)
        this.root.y = -16.0F;
        this.root.xScale = 2.0F;
        this.root.yScale = 1.8F;
        this.root.zScale = 1.9F;
        float par1 = s.walkAnimationPos;
        float par2 = s.walkAnimationSpeed;
        this.head.yRot = s.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = s.xRot * Mth.DEG_TO_RAD;
        this.rightArm.xRot = Mth.cos(par1 * 0.6662F + (float) Math.PI) * 2.0F * par2 * 0.5F;
        this.leftArm.xRot = Mth.cos(par1 * 0.6662F) * 2.0F * par2 * 0.5F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(par1 * 0.6662F) * 1.4F * par2;
        this.leftLeg.xRot = Mth.cos(par1 * 0.6662F + (float) Math.PI) * 1.4F * par2;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        MonkingModel.attackPose(this.rightArm, this.leftArm, s);
        this.rightArm.xRot += -0.5F;
        this.leftArm.xRot += -0.5F;
        MonkingModel.slamLegs(this.rightLeg, this.leftLeg, s);
    }
}
