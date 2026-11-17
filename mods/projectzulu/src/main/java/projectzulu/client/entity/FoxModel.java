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
 * ModelFox: body with tail and four legs, a separate head with nose and two ears. Sitting it rears up on the folded
 * hind legs and the head is drawn 1.1 times larger (render(f5 * 1.1F) of the original).
 */
public class FoxModel extends EntityModel<FoxModel.State> {
    private static final float HEAD_Y = 17.5F;
    private static final float HEAD_Z = -5.0F;

    private final ModelPart bodyrot;
    private final ModelPart headrot;
    private final ModelPart tailrot;
    private final ModelPart leg1rot;
    private final ModelPart leg2rot;
    private final ModelPart leg3rot;
    private final ModelPart leg4rot;

    public static class State extends LivingEntityRenderState {
        public boolean sitting;
    }

    public FoxModel(ModelPart root) {
        super(root);
        this.bodyrot = root.getChild("bodyrot");
        this.headrot = root.getChild("headrot");
        this.tailrot = this.bodyrot.getChild("tailrot");
        this.leg1rot = this.bodyrot.getChild("leg1rot");
        this.leg2rot = this.bodyrot.getChild("leg2rot");
        this.leg3rot = this.bodyrot.getChild("leg3rot");
        this.leg4rot = this.bodyrot.getChild("leg4rot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bodyrot = root.addOrReplaceChild("bodyrot", CubeListBuilder.create().mirror()
                .texOffs(0, 9).addBox(-2.5F, -2.5F, 0.0F, 5, 5, 8), PartPose.offset(0.0F, 17.5F, -5.0F));
        bodyrot.addOrReplaceChild("tailrot", CubeListBuilder.create().mirror()
                .texOffs(0, 22).addBox(-1.0F, -1.0F, 0.0F, 2, 2, 7), PartPose.offset(0.0F, -1.0F, 7.5F));
        bodyrot.addOrReplaceChild("leg4rot", CubeListBuilder.create().mirror()
                .texOffs(42, 22).addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2), PartPose.offset(1.5F, 2.5F, 0.0F));
        bodyrot.addOrReplaceChild("leg3rot", CubeListBuilder.create().mirror()
                .texOffs(34, 22).addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2), PartPose.offset(-1.5F, 2.5F, 0.0F));
        bodyrot.addOrReplaceChild("leg1rot", CubeListBuilder.create().mirror()
                .texOffs(18, 22).addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2), PartPose.offset(-1.5F, 2.5F, 7.0F));
        bodyrot.addOrReplaceChild("leg2rot", CubeListBuilder.create().mirror()
                .texOffs(26, 22).addBox(-1.0F, 0.0F, -1.0F, 2, 4, 2), PartPose.offset(1.5F, 2.5F, 7.0F));
        PartDefinition headrot = root.addOrReplaceChild("headrot", CubeListBuilder.create().mirror()
                        .texOffs(0, 0).addBox(-2.5F, -2.5F, -3.0F, 5, 5, 3)
                        .texOffs(16, 0).addBox(-1.0F, 0.0F, -5.0F, 2, 2, 2),
                PartPose.offset(0.0F, HEAD_Y, HEAD_Z));
        headrot.addOrReplaceChild("earrotl", CubeListBuilder.create().mirror()
                .texOffs(22, 4).addBox(-2.0F, -3.0F, -0.5F, 2, 3, 1), PartPose.offset(-0.5F, -2.5F, -2.0F));
        headrot.addOrReplaceChild("earrotr", CubeListBuilder.create().mirror()
                .texOffs(16, 4).addBox(0.0F, -3.0F, -0.5F, 2, 3, 1), PartPose.offset(0.5F, -2.5F, -2.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        if (state.sitting) {
            this.tailrot.xRot = Mth.PI / 18.0F;
            this.leg1rot.xRot = -0.9599311F;
            this.leg1rot.x = -1.4F;
            this.leg2rot.xRot = -0.9599311F;
            this.leg2rot.x = 1.4F;
            this.leg3rot.xRot = Mth.PI / 9.0F;
            this.leg3rot.y = 3.0F;
            this.leg3rot.z = 1.0F;
            this.leg4rot.xRot = Mth.PI / 9.0F;
            this.leg4rot.y = 3.0F;
            this.leg4rot.z = 1.0F;
            this.bodyrot.xRot = -0.61086524F;
            this.bodyrot.y = 17.0F;
            // the head is drawn at 1.1 scale, its pivot scaled with it
            this.headrot.y = HEAD_Y * 1.1F;
            this.headrot.z = HEAD_Z * 1.1F;
            this.headrot.xScale = 1.1F;
            this.headrot.yScale = 1.1F;
            this.headrot.zScale = 1.1F;
        } else {
            this.tailrot.xRot = -0.40142572F;
            float pos = state.walkAnimationPos * 0.6662F;
            float amount = 1.8F * Math.abs((float) Math.log(state.walkAnimationSpeed + 1.0F));
            this.leg1rot.xRot = Mth.cos(pos) * amount;
            this.leg2rot.xRot = Mth.cos(pos + Mth.PI) * amount;
            this.leg3rot.xRot = Mth.cos(pos) * amount;
            this.leg4rot.xRot = Mth.cos(pos + Mth.PI) * amount;
        }
        this.headrot.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.headrot.yRot = state.yRot * Mth.DEG_TO_RAD;
    }
}
