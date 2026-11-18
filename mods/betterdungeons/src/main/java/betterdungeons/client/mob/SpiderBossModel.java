package betterdungeons.client.mob;

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
 * ModelSpiderBoss (the spider layout of 64x32 with eight two-piece legs bent down at the knee and two mandibles that
 * close with the swing): legs spread and stepping like ModelSpider, the head looks.
 */
public class SpiderBossModel extends EntityModel<SpiderBossModel.State> {
    private final ModelPart head;
    private final ModelPart[] legs = new ModelPart[8];
    private final ModelPart mouthLeft;
    private final ModelPart mouthRight;

    public static class State extends LivingEntityRenderState {
        /** swingProgress. */
        public float swing;
    }

    public SpiderBossModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        for (int i = 0; i < 8; i++) {
            this.legs[i] = root.getChild("leg" + i);
        }
        this.mouthLeft = this.head.getChild("mouth_left");
        this.mouthRight = this.head.getChild("mouth_right");
    }

    public static LayerDefinition createBodyLayer() {
        int i = 15;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, i, -3.0F));
        head.addOrReplaceChild("mouth_left", CubeListBuilder.create().texOffs(44, 20).addBox(0.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offset(-3.0F, 3.0F, -8.0F));
        head.addOrReplaceChild("mouth_right", CubeListBuilder.create().texOffs(44, 20).addBox(0.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offset(1.0F, 3.0F, -8.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, i, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F),
                PartPose.offset(0.0F, i, 9.0F));
        float[] z = {2.0F, 2.0F, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F, -1.0F};
        for (int h = 0; h < 8; h++) {
            boolean right = h % 2 == 0;
            PartDefinition leg = root.addOrReplaceChild("leg" + h, CubeListBuilder.create().texOffs(18, 0)
                    .addBox(right ? -15.0F : -1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F), PartPose.offset(right ? -4.0F : 4.0F, i, z[h]));
            leg.addOrReplaceChild("part", CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F),
                    PartPose.offsetAndRotation(right ? -15.0F : 15.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.6F));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(State s) {
        super.setupAnim(s);
        float f = s.walkAnimationPos;
        float f1 = s.walkAnimationSpeed;
        this.head.yRot = s.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = s.xRot * Mth.DEG_TO_RAD;
        float f6 = 2.66F;
        float f7 = 3.0F;
        float f8 = (float) (Math.PI / 8);
        float[] yRot = {f8 * 2.0F + f7, -f8 * 2.0F - f7, f8 + f7, -f8 - f7, -f8 + f7, f8 - f7, -f8 * 2.0F + f7, f8 * 2.0F - f7};
        float f9 = -(Mth.cos(f * 0.6662F * 2.0F) * 0.4F) * f1;
        float f10 = -(Mth.cos(f * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * f1;
        float f11 = -(Mth.cos(f * 0.6662F * 2.0F + (float) (Math.PI / 2)) * 0.4F) * f1;
        float f12 = -(Mth.cos(f * 0.6662F * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * f1;
        float f13 = Math.abs(Mth.sin(f * 0.6662F) * 0.4F) * f1;
        float f14 = Math.abs(Mth.sin(f * 0.6662F + (float) Math.PI) * 0.4F) * f1;
        float f15 = Math.abs(Mth.sin(f * 0.6662F + (float) (Math.PI / 2)) * 0.4F) * f1;
        float f16 = Math.abs(Mth.sin(f * 0.6662F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * f1;
        float[] swingY = {f9, f10, f11, f12};
        float[] swingZ = {f13, f14, f15, f16};
        for (int i = 0; i < 8; i++) {
            float sign = i % 2 == 0 ? 1.0F : -1.0F;
            this.legs[i].zRot = -f6 * sign + swingZ[i / 2] * sign;
            this.legs[i].yRot = yRot[i] + swingY[i / 2] * sign;
        }
        this.mouthLeft.yRot = 0.370796F - s.swing;
        this.mouthRight.yRot = -0.370796F + s.swing;
    }
}
