package chococraft.client;

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
 * ModelChicobo (64 x 32): head, body, two legs. As in the original the head pitch is inverted and the left leg
 * swings about its y axis instead of x.
 */
public class ChicoboModel extends EntityModel<ChicoboModel.State> {
    public static class State extends LivingEntityRenderState {
        public String texture = "";
    }

    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ChicoboModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightLeg = root.getChild("rightleg");
        this.leftLeg = root.getChild("leftleg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 18.0F, -2.5F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 6).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("rightleg", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(-1.0F, 22.0F, 0.5F));
        root.addOrReplaceChild("leftleg", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(1.0F, 22.0F, 0.5F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        this.head.xRot = -(state.xRot * Mth.DEG_TO_RAD);
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.rightLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        this.leftLeg.yRot = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.4F * state.walkAnimationSpeed;
    }
}
