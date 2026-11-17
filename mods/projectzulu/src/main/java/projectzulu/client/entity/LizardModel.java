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
 * ModelLizard: a flat body with four splayed legs (swinging sideways), a three-piece tail and a head with four
 * frills. The frills lie flat against the head and flare up and flutter while the lizard prepares to spit.
 */
public class LizardModel extends EntityModel<LizardModel.State> {
    private final ModelPart headbase;
    private final ModelPart frill1;
    private final ModelPart frill2;
    private final ModelPart frill3;
    private final ModelPart frill4;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public static class State extends LivingEntityRenderState {
        public boolean prepareToSpit;
    }

    public LizardModel(ModelPart root) {
        super(root);
        ModelPart front = root.getChild("frontrot");
        this.headbase = front.getChild("headbase");
        this.frill1 = this.headbase.getChild("frill1");
        this.frill2 = this.headbase.getChild("frill2");
        this.frill3 = this.headbase.getChild("frill3");
        this.frill4 = this.headbase.getChild("frill4");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = front.getChild("leg3");
        this.leg4 = front.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition frontrot = root.addOrReplaceChild("frontrot", CubeListBuilder.create(),
                PartPose.offset(0.0F, 19.0F, 6.0F));
        PartDefinition headbase = frontrot.addOrReplaceChild("headbase", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, -13.0F));
        headbase.addOrReplaceChild("frill2", CubeListBuilder.create().texOffs(29, 22).addBox(-3.0F, -3.0F, 0.0F, 3, 5, 1),
                PartPose.offsetAndRotation(-3.0F, 1.0F, -3.0F, 0.0F, 0.0371786F, 0.0F));
        headbase.addOrReplaceChild("frill4", CubeListBuilder.create().texOffs(37, 22).addBox(-4.0F, -2.0F, 0.0F, 4, 4, 1),
                PartPose.offset(-1.0F, -2.0F, -3.0F));
        headbase.addOrReplaceChild("frill1", CubeListBuilder.create().texOffs(29, 22).addBox(0.0F, -3.0F, 0.0F, 3, 5, 1),
                PartPose.offsetAndRotation(3.0F, 1.0F, -3.0F, 0.0F, 0.0371786F, 0.0F));
        headbase.addOrReplaceChild("frill3", CubeListBuilder.create().texOffs(37, 22).addBox(0.0F, -2.0F, 0.0F, 4, 4, 1),
                PartPose.offset(1.0F, -2.0F, -3.0F));
        headbase.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -9.0F, 6, 6, 8),
                PartPose.offset(1.0F, 1.0F, 1.0F));
        frontrot.addOrReplaceChild("leg3u", CubeListBuilder.create().texOffs(14, 22).addBox(-1.0F, 0.0F, -1.0F, 1, 2, 2),
                PartPose.offset(-4.0F, -1.0F, -12.0F));
        frontrot.addOrReplaceChild("body", CubeListBuilder.create().texOffs(10, 0).addBox(-4.0F, -2.0F, -9.0F, 8, 4, 18),
                PartPose.offset(0.0F, 0.0F, -6.0F));
        frontrot.addOrReplaceChild("leg4u", CubeListBuilder.create().texOffs(14, 22).addBox(0.0F, 0.0F, -1.0F, 1, 2, 2),
                PartPose.offset(4.0F, -1.0F, -12.0F));
        frontrot.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(20, 22).addBox(1.0F, 0.0F, -1.0F, 2, 6, 2),
                PartPose.offset(4.0F, -1.0F, -12.0F));
        frontrot.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(20, 22).addBox(-3.0F, 0.0F, -1.0F, 2, 6, 2),
                PartPose.offset(-4.0F, -1.0F, -12.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(20, 22).addBox(-3.0F, 0.0F, -1.0F, 2, 6, 2),
                PartPose.offset(-4.0F, 18.0F, 6.0F));
        root.addOrReplaceChild("leg1u", CubeListBuilder.create().texOffs(14, 22).addBox(-1.0F, 0.0F, -1.0F, 1, 2, 2),
                PartPose.offset(-4.0F, 18.0F, 6.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(20, 22).addBox(1.0F, 0.0F, -1.0F, 2, 6, 2),
                PartPose.offset(4.0F, 18.0F, 6.0F));
        root.addOrReplaceChild("leg2u", CubeListBuilder.create().texOffs(14, 22).addBox(0.0F, 0.0F, -1.0F, 1, 2, 2),
                PartPose.offset(4.0F, 18.0F, 6.0F));
        PartDefinition tailrot = root.addOrReplaceChild("tailrot", CubeListBuilder.create(),
                PartPose.offset(0.0F, 20.0F, 9.0F));
        tailrot.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, 0.0F, 2, 1, 5),
                PartPose.offset(0.0F, -1.0F, 10.0F));
        tailrot.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, 0.0F, 2, 1, 5),
                PartPose.offset(0.0F, -1.0F, 5.0F));
        tailrot.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, 0.0F, 2, 1, 5),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        float pos = 4.0F * state.walkAnimationPos * 0.6662F;
        float speed = state.walkAnimationSpeed;
        if (state.prepareToSpit) {
            this.frill1.yRot = Mth.cos(pos) * speed;
            this.frill2.yRot = Mth.cos(pos - Mth.PI) * speed;
            this.frill3.zRot = -Mth.PI / 4.0F;
            this.frill3.yRot = Mth.cos(pos) * 0.5F * speed;
            this.frill3.xRot = Mth.cos(pos) * 0.5F * speed;
            this.frill4.zRot = Mth.PI / 4.0F;
            this.frill4.yRot = Mth.cos(pos - Mth.PI) * 0.5F * speed;
            this.frill4.xRot = Mth.cos(pos - Mth.PI) * 0.5F * speed;
        } else {
            this.frill1.zRot = -Mth.HALF_PI;
            this.frill2.zRot = Mth.HALF_PI;
            this.frill3.yRot = -Mth.HALF_PI;
            this.frill4.yRot = Mth.HALF_PI;
        }
        this.leg1.yRot = Mth.cos(pos + Mth.PI) * 1.4F * speed;
        this.leg2.yRot = Mth.cos(pos) * 1.4F * speed;
        this.leg3.yRot = Mth.cos(pos + Mth.PI) * 1.4F * speed;
        this.leg4.yRot = Mth.cos(pos) * 1.4F * speed;
        this.headbase.xRot = Mth.clamp(state.xRot, -15.0F, 15.0F) * Mth.DEG_TO_RAD;
        this.headbase.yRot = Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
    }
}
