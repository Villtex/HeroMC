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
 * ModelBull (a ModelQuadruped with a big body, a head with six horn pieces and four legs, 64x32): head turns with the
 * look, legs walk like ModelQuadruped. The original drew everything twice (super.render, then again); once is the
 * same picture.
 */
public class BullModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public BullModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, -8.0F));
        PartPose horn = PartPose.offsetAndRotation(0.0F, -1.0F, -3.0F, -0.0569039F, 0.0F, 0.0F);
        head.addOrReplaceChild("horn1", CubeListBuilder.create().texOffs(22, 0).addBox(-4.0F, -3.0F, -5.0F, 2.0F, 2.0F, 2.0F), horn);
        head.addOrReplaceChild("horn11", CubeListBuilder.create().texOffs(22, 0).addBox(-5.0F, -4.0F, -6.0F, 2.0F, 2.0F, 2.0F), horn);
        head.addOrReplaceChild("horn12", CubeListBuilder.create().texOffs(22, 0).addBox(-5.0F, -4.0F, -9.0F, 1.0F, 1.0F, 3.0F), horn);
        head.addOrReplaceChild("horn2", CubeListBuilder.create().texOffs(22, 0).addBox(2.0F, -3.0F, -5.0F, 2.0F, 2.0F, 2.0F), horn);
        head.addOrReplaceChild("horn21", CubeListBuilder.create().texOffs(22, 0).addBox(3.0F, -4.0F, -6.0F, 2.0F, 2.0F, 2.0F), horn);
        head.addOrReplaceChild("horn22", CubeListBuilder.create().texOffs(22, 0).addBox(4.0F, -4.0F, -9.0F, 1.0F, 1.0F, 3.0F), horn);
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(18, 4).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-3.0F, 8.0F, 7.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(3.0F, 8.0F, 7.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 0.0F, -3.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-3.0F, 8.0F, -5.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -3.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(3.0F, 8.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelQuadruped.setRotationAngles. */
    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float f = state.walkAnimationPos;
        float f1 = state.walkAnimationSpeed;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.body.xRot = (float) (Math.PI / 2);
        this.leg1.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        this.leg2.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * f1;
        this.leg3.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * f1;
        this.leg4.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
    }
}
