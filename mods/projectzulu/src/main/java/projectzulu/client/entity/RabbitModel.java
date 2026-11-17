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
 * ModelRabbit (32 x 32 texture): body, tail, neck, two front legs, two hind legs (thigh and foot) and the head with
 * nose and two slanted ears. Legs swing only on the ground; as in the original the right hind leg never moves
 * (setLivingAnimations set the left one twice).
 */
public class RabbitModel extends EntityModel<RabbitModel.State> {
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leglefrot;
    private final ModelPart headrot;

    public static class State extends LivingEntityRenderState {
        public boolean onGround = true;
    }

    public RabbitModel(ModelPart root) {
        super(root);
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leglefrot = root.getChild("leglefrot");
        this.headrot = root.getChild("headrot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -4.0F, 4, 4, 5),
                PartPose.offset(0.0F, 21.0F, 3.0F));
        root.addOrReplaceChild("queue", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 1),
                PartPose.offset(0.0F, 22.0F, 4.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 11).addBox(-2.0F, -2.0F, -6.0F, 4, 2, 2),
                PartPose.offset(0.0F, 21.0F, 3.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 28).addBox(-2.0F, 0.0F, -1.0F, 1, 3, 1),
                PartPose.offset(0.0F, 21.0F, -2.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(4, 28).addBox(1.0F, 0.0F, -1.0F, 1, 3, 1),
                PartPose.offset(0.0F, 21.0F, -2.0F));
        root.addOrReplaceChild("legrigrot", CubeListBuilder.create().mirror()
                        .texOffs(22, 18).addBox(-0.5F, -1.0F, -2.5F, 1, 3, 4)
                        .texOffs(20, 26).addBox(-0.5F, 2.0F, -3.5F, 1, 1, 5),
                PartPose.offset(2.5F, 21.0F, 2.5F));
        root.addOrReplaceChild("leglefrot", CubeListBuilder.create().mirror()
                        .texOffs(8, 18).addBox(-0.5F, -1.0F, -2.5F, 1, 3, 4)
                        .texOffs(8, 26).addBox(-0.5F, 2.0F, -3.5F, 1, 1, 5),
                PartPose.offset(-2.5F, 21.0F, 2.5F));
        PartDefinition headrot = root.addOrReplaceChild("headrot", CubeListBuilder.create().mirror()
                        .texOffs(19, 5).addBox(-1.5F, -3.0F, -1.0F, 3, 3, 3)
                        .texOffs(23, 11).addBox(-0.5F, -1.0F, -1.5F, 1, 1, 1),
                PartPose.offset(0.0F, 19.0F, -2.5F));
        headrot.addOrReplaceChild("earlefrot", CubeListBuilder.create().mirror()
                .texOffs(27, 1).addBox(-0.5F, -6.0F, -0.5F, 1, 3, 1), PartPose.rotation(-0.5585054F, -0.4363323F, 0.0F));
        headrot.addOrReplaceChild("earrigrot", CubeListBuilder.create().mirror()
                .texOffs(19, 1).addBox(-0.6F, -6.0F, -0.5F, 1, 3, 1), PartPose.rotation(-0.5585054F, 0.4363323F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        if (state.onGround) {
            float swing = state.walkAnimationPos * 0.6662F * 2.0F;
            float amount = state.walkAnimationSpeed;
            this.leg1.xRot = Mth.cos(swing) * 1.8F * amount;
            this.leg2.xRot = Mth.cos(swing + Mth.PI) * 1.8F * amount;
            this.leglefrot.xRot = Mth.cos(swing) * 1.8F * amount;
        }
        this.headrot.xRot = Mth.clamp(state.xRot, -15.0F, 15.0F) * Mth.DEG_TO_RAD;
        this.headrot.yRot = Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
    }
}
