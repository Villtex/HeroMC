package natura.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/** ImpModel: a body lying flat, a big head, two legs and two short arms that swing like the legs. */
public class ImpModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public ImpModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-5.0F, 0.0F, -18.5F, 10, 5, 9),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.570796F, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -6.5F, -4.0F, 10, 8, 8),
                PartPose.offset(0.0F, 8.0F, 2.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4),
                PartPose.offset(-3.0F, 18.0F, 2.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4),
                PartPose.offset(3.0F, 18.0F, 2.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-1.5F, 0.0F, 1.0F, 2, 5, 2),
                PartPose.offset(6.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-0.5F, 0.0F, 1.0F, 2, 5, 2),
                PartPose.offset(-6.0F, 10.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        // the original turned the head by -pitch
        this.head.xRot = -state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.rightArm.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        this.leftArm.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed;
        this.rightLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        this.leftLeg.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed;
    }
}
