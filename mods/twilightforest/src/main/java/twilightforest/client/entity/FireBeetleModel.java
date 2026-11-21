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


/** ModelTFFireBeetle: six legs scuttling in pairs. */
public class FireBeetleModel extends EntityModel<net.minecraft.client.renderer.entity.state.LivingEntityRenderState> {
    final ModelPart head;
    final ModelPart leg1;
    final ModelPart leg2;
    final ModelPart leg3;
    final ModelPart leg4;
    final ModelPart leg5;
    final ModelPart leg6;

    public FireBeetleModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.leg5 = root.getChild("leg5");
        this.leg6 = root.getChild("leg6");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("thorax", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-4.5F, -4.0F, 0.0F, 9.0F, 8.0F, 2.0F),
                PartPose.offset(0.0F, 18.0F, -4.5F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 19.0F, -5.0F));
        PartDefinition jaw1a = head.addOrReplaceChild("jaw1a", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, -6.0F, 0.3490659F, 0.0F, 0.0F));
        jaw1a.addOrReplaceChild("jaw1b", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.0F, 1.570796F, 0.0F));
        PartDefinition jaw2a = head.addOrReplaceChild("jaw2a", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, -6.0F, 0.3490659F, 0.0F, 0.0F));
        jaw2a.addOrReplaceChild("jaw2b", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.0F, 1.570796F, 0.0F));
        head.addOrReplaceChild("antenna1", CubeListBuilder.create()
                .texOffs(42, 4).addBox(0.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(1.0F, -3.0F, -5.0F, 0.0F, 1.047198F, -0.296706F));
        head.addOrReplaceChild("antenna2", CubeListBuilder.create()
                .texOffs(42, 4).addBox(0.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-1.0F, -3.0F, -5.0F, 0.0F, 2.094395F, 0.296706F));
        head.addOrReplaceChild("eye1", CubeListBuilder.create()
                .texOffs(15, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(-3.0F, -2.0F, -5.0F));
        head.addOrReplaceChild("eye2", CubeListBuilder.create()
                .texOffs(15, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(3.0F, -2.0F, -5.0F));
        root.addOrReplaceChild("connector2", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 6.0F, 1.0F),
                PartPose.offset(0.0F, 18.0F, -4.0F));
        root.addOrReplaceChild("rear_end", CubeListBuilder.create()
                .texOffs(22, 9).addBox(-6.0F, -9.0F, -4.0F, 12.0F, 14.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 7.0F, 1.570796F, 0.0F, 0.0F));
        root.addOrReplaceChild("leg6", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, 21.0F, -4.0F, 0.0F, 0.2792527F, 0.3490659F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, 21.0F, -1.0F, 0.0F, -0.2792527F, 0.3490659F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, 21.0F, 4.0F, 0.0F, (float) (-Math.PI * 2.0 / 9.0), 0.3490659F));
        root.addOrReplaceChild("leg5", CubeListBuilder.create().mirror()
                .texOffs(40, 0).addBox(-9.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, 21.0F, -4.0F, 0.0F, -0.2792527F, -0.3490659F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().mirror()
                .texOffs(40, 0).addBox(-9.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, 21.0F, -1.0F, 0.0F, 0.2792527F, -0.3490659F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().mirror()
                .texOffs(40, 0).addBox(-9.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, 21.0F, 4.0F, 0.0F, (float) (Math.PI * 2.0 / 9.0), -0.3490659F));
        root.addOrReplaceChild("connector1", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 1.0F),
                PartPose.offset(0.0F, 18.0F, -3.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        BeetleLegs.animate(state.walkAnimationPos, state.walkAnimationSpeed, this.leg1, this.leg2, this.leg3, this.leg4, this.leg5, this.leg6);
    }
}
