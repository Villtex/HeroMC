package twilightforest.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/** ModelTFHelmetCrab: a crab under a horned goblin helmet, five walking legs (spider gait) and one big claw. */
public class HelmetCrabModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart body;
    private final ModelPart leg8;
    private final ModelPart leg6;
    private final ModelPart leg4;
    private final ModelPart rightArm;
    private final ModelPart leg5;
    private final ModelPart leg3;

    public HelmetCrabModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.leg8 = root.getChild("Leg8");
        this.leg6 = root.getChild("Leg6");
        this.leg4 = root.getChild("Leg4");
        this.rightArm = root.getChild("rightArm");
        this.leg5 = root.getChild("Leg5");
        this.leg3 = root.getChild("Leg3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(32, 4).addBox(-2.5F, -2.5F, -5.0F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 19.0F, 0.0F));
        body.addOrReplaceChild("righteye", CubeListBuilder.create()
                .texOffs(10, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -4.0F, (float) (Math.PI / 4), 0.0F, (float) (-Math.PI / 4)));
        body.addOrReplaceChild("lefteye", CubeListBuilder.create()
                .texOffs(10, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(1.0F, -1.0F, -4.0F, (float) (Math.PI / 4), 0.0F, (float) (Math.PI / 4)));
        PartDefinition helmetBase = root.addOrReplaceChild("helmetBase", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, (float) -Math.PI * 5.0F / 9.0F, (float) (-Math.PI / 6), 0.0F));
        helmetBase.addOrReplaceChild("helmet", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-3.5F, -11.0F, -3.5F, 7.0F, 11.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F));
        PartDefinition righthorn1 = helmetBase.addOrReplaceChild("righthorn1", CubeListBuilder.create()
                .texOffs(28, 14).addBox(-6.0F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-3.5F, -9.0F, 0.0F, 0.0F, (float) (-Math.PI / 12), 0.17453294F));
        righthorn1.addOrReplaceChild("righthorn2", CubeListBuilder.create()
                .texOffs(28, 20).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-5.5F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 12), 0.17453294F));
        PartDefinition lefthorn1 = helmetBase.addOrReplaceChild("lefthorn1", CubeListBuilder.create().mirror()
                .texOffs(28, 14).addBox(-1.0F, -1.5F, -1.5F, 7.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(3.5F, -9.0F, 0.0F, 0.0F, (float) (Math.PI / 12), -0.17453294F));
        lefthorn1.addOrReplaceChild("lefthorn2", CubeListBuilder.create()
                .texOffs(28, 20).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(5.5F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 12), -0.17453294F));
        root.addOrReplaceChild("Leg8", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-1.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 20.0F, -3.0F, 0.0F, 0.5759587F, 0.1919862F));
        root.addOrReplaceChild("Leg6", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-1.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 20.0F, -2.0F, 0.0F, 0.2792527F, 0.1919862F));
        root.addOrReplaceChild("Leg4", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-1.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 20.0F, -1.0F, 0.0F, -0.2792527F, 0.1919862F));
        PartDefinition rightArm = root.addOrReplaceChild("rightArm", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-7.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 20.0F, -3.0F, 0.0F, -1.319531F, -0.1919862F));
        PartDefinition clawbase = rightArm.addOrReplaceChild("clawbase", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-6.0F, 0.0F, -0.5F, 0.0F, (float) (Math.PI / 2), 0.0F));
        clawbase.addOrReplaceChild("clawtop", CubeListBuilder.create()
                .texOffs(0, 5).addBox(0.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.1858931F));
        clawbase.addOrReplaceChild("clawbottom", CubeListBuilder.create()
                .texOffs(0, 8).addBox(0.0F, -0.5F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2602503F));
        root.addOrReplaceChild("Leg5", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-7.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 20.0F, -2.0F, 0.0F, -0.2792527F, -0.1919862F));
        root.addOrReplaceChild("Leg3", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-7.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 20.0F, -1.0F, 0.0F, 0.2792527F, -0.1919862F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float f = state.walkAnimationPos;
        float f1 = state.walkAnimationSpeed;
        this.body.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.body.xRot = state.xRot * Mth.DEG_TO_RAD;
        float f6 = (float) (Math.PI / 4);
        this.leg3.zRot = -f6 * 0.74F;
        this.leg4.zRot = f6 * 0.74F;
        this.leg5.zRot = -f6 * 0.74F;
        this.leg6.zRot = f6 * 0.74F;
        this.leg8.zRot = f6;
        float f7 = -0.0F;
        float f8 = (float) (Math.PI / 8);
        this.leg3.yRot = f8 * 1.0F + f7;
        this.leg4.yRot = -f8 * 1.0F - f7;
        this.leg5.yRot = -f8 * 1.0F + f7;
        this.leg6.yRot = f8 * 1.0F - f7;
        this.leg8.yRot = f8 * 2.0F - f7;
        float f10 = -(Mth.cos(f * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * f1;
        float f11 = -(Mth.cos(f * 0.6662F * 2.0F + (float) (Math.PI / 2)) * 0.4F) * f1;
        float f12 = -(Mth.cos(f * 0.6662F * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * f1;
        float f14 = Math.abs(Mth.sin(f * 0.6662F + (float) Math.PI) * 0.4F) * f1;
        float f15 = Math.abs(Mth.sin(f * 0.6662F + (float) (Math.PI / 2)) * 0.4F) * f1;
        float f16 = Math.abs(Mth.sin(f * 0.6662F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * f1;
        this.leg3.yRot += f10;
        this.leg4.yRot += -f10;
        this.leg5.yRot += f11;
        this.leg6.yRot += -f11;
        this.leg8.yRot += -f12;
        this.leg3.zRot += f14;
        this.leg4.zRot += -f14;
        this.leg5.zRot += f15;
        this.leg6.zRot += -f15;
        this.leg8.zRot += -f16;
        this.rightArm.yRot = -1.319531F + Mth.cos(f * 0.6662F + (float) Math.PI) * 2.0F * f1 * 0.5F;
    }
}
