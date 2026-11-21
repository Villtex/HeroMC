package twilightforest.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;


/** ModelTFKobold: its own setRotationAngles (arms forward, swaying); the jaw drops while it rises (render state flag). */
public class KoboldModel extends OldBiped {
    final ModelPart jaw;

    public KoboldModel(ModelPart root) {
        super(root);
        this.jaw = root.getChild("head").getChild("jaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -7.0F, -3.0F, 7.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        head.addOrReplaceChild("rightear", CubeListBuilder.create()
                .texOffs(48, 20).addBox(0.0F, -4.0F, 0.0F, 4.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(3.5F, -3.0F, -1.0F, 0.0F, (float) (Math.PI / 12), -0.3490659F));
        head.addOrReplaceChild("leftear", CubeListBuilder.create()
                .texOffs(48, 25).addBox(-4.0F, -4.0F, 0.0F, 4.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-3.5F, -3.0F, -1.0F, 0.0F, (float) (-Math.PI / 12), 0.3490659F));
        head.addOrReplaceChild("snout", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 2.0F, 3.0F),
                PartPose.offset(0.0F, -2.0F, -3.0F));
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(28, 5).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, -3.0F, 0.20944F, 0.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(12, 19).addBox(0.0F, 0.0F, 0.0F, 7.0F, 7.0F, 4.0F),
                PartPose.offset(-3.5F, 12.0F, -2.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(36, 17).addBox(-3.0F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(-3.5F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(36, 17).addBox(0.0F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(3.5F, 12.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(-2.0F, 19.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(2.0F, 19.0F, 0.0F));
        OldBiped.fill(root);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.HumanoidRenderState state) {
        super.setupAnim(state);
        float f = state.walkAnimationPos;
        float f1 = state.walkAnimationSpeed;
        float f2 = state.ageInTicks;
        this.resetPose();
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightArm.xRot = -0.47123894F;
        this.leftArm.xRot = -0.47123894F;
        this.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        this.leftLeg.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        this.rightArm.zRot += Mth.cos(f2 * 0.19F) * 0.15F + 0.05F;
        this.leftArm.zRot -= Mth.cos(f2 * 0.19F) * 0.15F + 0.05F;
        this.rightArm.xRot += Mth.sin(f2 * 0.267F) * 0.25F;
        this.leftArm.xRot -= Mth.sin(f2 * 0.267F) * 0.25F;
        this.jaw.xRot = state.isFallFlying ? 1.44F : 0.20944F;
    }
}
