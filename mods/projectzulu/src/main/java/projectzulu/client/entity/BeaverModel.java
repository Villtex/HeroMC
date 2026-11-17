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

/** ModelBeaver: a round body on four stub legs, the head with nose and two teeth and the flat tail that flaps when walking. */
public class BeaverModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart headrot;
    private final ModelPart tailrot;

    public BeaverModel(ModelPart root) {
        super(root);
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.headrot = root.getChild("headrot");
        this.tailrot = root.getChild("tailrot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -4.0F, -10.0F, 9, 8, 10),
                PartPose.offset(0.0F, 18.0F, 7.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(8, 10).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2),
                PartPose.offset(3.5F, 22.0F, 6.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 10).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-3.5F, 22.0F, 6.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 10).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-3.5F, 22.0F, -2.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(8, 10).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2),
                PartPose.offset(3.5F, 22.0F, -2.0F));
        root.addOrReplaceChild("headrot", CubeListBuilder.create().mirror()
                        .texOffs(38, 14).addBox(-3.0F, -3.0F, -4.0F, 6, 6, 4)
                        .texOffs(38, 24).addBox(-1.5F, 1.0F, -5.0F, 3, 2, 1)
                        .texOffs(46, 24).addBox(0.0F, 3.0F, -5.0F, 1, 1, 1)
                        .texOffs(46, 24).addBox(-1.0F, 3.0F, -5.0F, 1, 1, 1),
                PartPose.offset(0.0F, 18.0F, -3.0F));
        root.addOrReplaceChild("tailrot", CubeListBuilder.create().mirror()
                        .texOffs(0, 0).addBox(-1.5F, -0.5F, 0.0F, 3, 1, 2)
                        .texOffs(0, 3).addBox(-2.0F, -0.5F, 2.0F, 4, 1, 1)
                        .texOffs(0, 5).addBox(-2.5F, -0.5F, 3.0F, 5, 1, 1)
                        .texOffs(12, 0).addBox(-3.0F, -0.5F, 4.0F, 6, 1, 4)
                        .texOffs(12, 5).addBox(-2.5F, -0.5F, 8.0F, 5, 1, 1),
                PartPose.offset(0.0F, 20.0F, 7.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float swing = state.walkAnimationPos * 0.6662F * 2.0F;
        float amount = state.walkAnimationSpeed;
        this.leg1.xRot = Mth.cos(swing) * 1.8F * amount;
        this.leg2.xRot = Mth.cos(swing) * 1.8F * amount;
        this.leg3.xRot = Mth.cos(swing + Mth.PI) * 1.8F * amount;
        this.leg4.xRot = Mth.cos(swing + Mth.PI) * 1.8F * amount;
        this.tailrot.xRot = Mth.cos(swing) * 1.8F * amount;
        this.headrot.xRot = Mth.clamp(state.xRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD;
        this.headrot.yRot = Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
    }
}
