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
 * ModelArmadillo: an armour shell of four plates (body1 fixed, body2-4 on the folding joints rear1-3), a head piece
 * with nose and chin, four stub legs and a tail. Covering or rolling it curls up: the whole body pitches over, the
 * rear joints fold, tail and hind legs tuck in. The eased values come from the render state (ArmadilloRenderer).
 */
public class ArmadilloModel extends EntityModel<ArmadilloModel.State> {
    private final ModelPart whole;
    private final ModelPart headpiece;
    private final ModelPart rear1;
    private final ModelPart rear2;
    private final ModelPart rear3;
    private final ModelPart tail;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    /** The eased part angles, stepped per frame in the renderer (EntityModelRotation of the original). */
    public static class State extends LivingEntityRenderState {
        public float wholeX;
        public float rearX;
        public float tailX;
        public float tailZ = 4.0F;
        public float rearLegZ = 5.0F;
        public float headX;
        public boolean curled;
    }

    public ArmadilloModel(ModelPart root) {
        super(root);
        this.whole = root.getChild("whole");
        this.headpiece = this.whole.getChild("headpiece");
        this.rear1 = this.whole.getChild("rear1");
        this.rear2 = this.rear1.getChild("rear2");
        this.rear3 = this.rear2.getChild("rear3");
        this.tail = this.rear3.getChild("tail");
        this.leg1 = this.whole.getChild("leg1");
        this.leg2 = this.whole.getChild("leg2");
        this.leg3 = this.whole.getChild("leg3");
        this.leg4 = this.whole.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition whole = root.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.0F));
        whole.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(32, 0).addBox(-4.5F, -4.0F, -2.0F, 9, 8, 4),
                PartPose.offset(0.0F, 0.0F, -4.0F));
        PartDefinition rear1 = whole.addOrReplaceChild("rear1", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -2.0F));
        rear1.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(32, 0).addBox(-4.5F, -4.0F, -2.0F, 9, 8, 4),
                PartPose.offset(0.0F, 3.0F, 2.0F));
        PartDefinition rear2 = rear1.addOrReplaceChild("rear2", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 4.0F));
        rear2.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(32, 0).addBox(-4.5F, -4.0F, -2.0F, 9, 8, 4),
                PartPose.offset(0.0F, 4.0F, 2.0F));
        PartDefinition rear3 = rear2.addOrReplaceChild("rear3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 4.0F));
        rear3.addOrReplaceChild("body4", CubeListBuilder.create().texOffs(32, 0).addBox(-4.5F, -4.0F, -2.0F, 9, 8, 4),
                PartPose.offset(0.0F, 4.0F, 2.0F));
        rear3.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(8, 14).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6),
                PartPose.offset(0.0F, 6.0F, 4.0F));
        PartDefinition headpiece = whole.addOrReplaceChild("headpiece", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -6.0F));
        headpiece.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(12, 9).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2),
                PartPose.offset(0.0F, 2.0F, -5.0F));
        headpiece.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.5F, -1.5F, 5, 5, 3),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        headpiece.addOrReplaceChild("chin", CubeListBuilder.create().texOffs(0, 8).addBox(-1.5F, -1.5F, -2.5F, 3, 3, 3),
                PartPose.offset(0.0F, 1.0F, -3.0F));
        whole.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-3.0F, 4.0F, -4.0F));
        whole.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2),
                PartPose.offset(3.0F, 4.0F, -4.0F));
        whole.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2),
                PartPose.offset(-3.0F, 4.0F, 5.0F));
        whole.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2),
                PartPose.offset(3.0F, 4.0F, 5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        this.whole.xRot = state.wholeX;
        this.rear1.xRot = state.rearX;
        this.rear2.xRot = state.rearX;
        this.rear3.xRot = state.rearX;
        this.tail.xRot = state.tailX;
        this.tail.z = state.tailZ;
        this.leg3.z = state.rearLegZ;
        this.leg4.z = state.rearLegZ;
        if (state.curled) {
            this.headpiece.xRot = state.headX;
        } else {
            this.headpiece.xRot = Mth.clamp(state.xRot, -180.0F, 180.0F) * Mth.DEG_TO_RAD;
            this.headpiece.yRot = Mth.clamp(state.yRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD;
            float pos = state.walkAnimationPos;
            float speed = state.walkAnimationSpeed;
            this.leg1.xRot = Mth.cos(4.0F * pos * 0.6662F + Mth.PI) * 1.4F * speed;
            this.leg2.xRot = Mth.cos(4.0F * pos * 0.6662F) * 1.4F * speed;
            this.leg3.xRot = Mth.cos(4.0F * pos * 0.6662F + Mth.PI) * 1.4F * speed;
            this.leg4.xRot = Mth.cos(4.0F * pos * 0.6662F) * 1.4F * speed;
        }
    }
}
