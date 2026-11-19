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
 * ModelChocobo (128 x 128): head with two feathers, neck, body with saddle bags and pack bag (always drawn, the
 * textures without bags leave them empty), wings, three tail feathers, thighs, shins and three talons per leg. The
 * head follows the look, the legs swing; in the air (the chicken-style wing value of RenderChocobo) the wings spread
 * (or flap with chocoboWingFlutter) and both legs are drawn up.
 */
public class ChocoboModel extends EntityModel<ChocoboModel.State> {
    private static final float PI = Mth.PI;

    public static class State extends LivingEntityRenderState {
        /** handleRotationFloat: (sin(wingRotation) + 1) * destPos. */
        public float wing;
        public boolean flutter;
        public String texture = "";
    }

    private final ModelPart head;
    private final ModelPart feather;
    private final ModelPart feather2;
    private final ModelPart neck;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightThigh;
    private final ModelPart leftThigh;
    private final ModelPart rightShin;
    private final ModelPart leftShin;
    private final ModelPart talonRB;
    private final ModelPart talonRR;
    private final ModelPart talonRL;
    private final ModelPart talonLL;
    private final ModelPart talonLR;
    private final ModelPart talonLB;

    public ChocoboModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.feather = root.getChild("feather");
        this.feather2 = root.getChild("feather_2");
        this.neck = root.getChild("neck");
        this.rightWing = root.getChild("rightWing");
        this.leftWing = root.getChild("leftWing");
        this.rightThigh = root.getChild("rightThigh");
        this.leftThigh = root.getChild("leftThigh");
        this.rightShin = root.getChild("rightShin");
        this.leftShin = root.getChild("leftShin");
        this.talonRB = root.getChild("talonRB");
        this.talonRR = root.getChild("talonRR");
        this.talonRL = root.getChild("talonRL");
        this.talonLL = root.getChild("talonLL");
        this.talonLR = root.getChild("talonLR");
        this.talonLB = root.getChild("talonLB");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(87, 2).addBox(-3.0F, -12.0F, -9.0F, 6.0F, 6.0F, 12.0F),
                PartPose.offset(1.0F, -8.0F, -5.0F));
        root.addOrReplaceChild("feather", CubeListBuilder.create()
                .texOffs(25, 18).addBox(-3.0F, -12.0F, 3.0F, 6.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(1.0F, -8.0F, -5.0F, 0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("feather_2", CubeListBuilder.create()
                .texOffs(45, 21).addBox(-3.0F, -12.0F, 3.0F, 6.0F, 6.0F, 3.0F),
                PartPose.offset(1.0F, -8.0F, -5.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(20, 45).addBox(-2.0F, -9.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offset(1.0F, -8.0F, -5.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(77, 31).addBox(-3.0F, -4.0F, -3.0F, 12.0F, 20.0F, 11.0F),
                PartPose.offsetAndRotation(-2.0F, 1.0F, -2.0F, 1.37881F, 0.0F, 0.0F));
        root.addOrReplaceChild("leftSaddleBag", CubeListBuilder.create()
                .texOffs(2, 96).addBox(7.8F, 10.1F, 0.5F, 2.0F, 6.0F, 8.0F),
                PartPose.offsetAndRotation(-2.0F, 1.0F, -2.0F, 1.37881F, 0.0F, 0.0F));
        root.addOrReplaceChild("rightSaddleBag", CubeListBuilder.create()
                .texOffs(2, 72).addBox(-4.0F, 10.1F, 0.5F, 2.0F, 6.0F, 8.0F),
                PartPose.offsetAndRotation(-2.0F, 1.0F, -2.0F, 1.37881F, 0.0F, 0.0F));
        root.addOrReplaceChild("packBag", CubeListBuilder.create()
                .texOffs(50, 66).addBox(-2.0F, 3.0F, 8.0F, 10.0F, 12.0F, 6.0F),
                PartPose.offsetAndRotation(-2.0F, 1.0F, -2.0F, 1.37881F, 0.0F, 0.0F));
        root.addOrReplaceChild("rightWing", CubeListBuilder.create()
                .texOffs(40, 37).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 10.0F, 16.0F),
                PartPose.offsetAndRotation(-5.0F, -7.0F, 0.0F, 0.0F, -0.0174533F, 0.0F));
        root.addOrReplaceChild("leftWing", CubeListBuilder.create()
                .texOffs(40, 37).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 10.0F, 16.0F),
                PartPose.offsetAndRotation(8.0F, -7.0F, 0.0F, 0.0F, 0.0174533F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(59, 2).addBox(0.0F, 0.0F, 0.0F, 12.0F, 14.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, -11.0F, 26.6F, -0.9948377F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail_2", CubeListBuilder.create()
                .texOffs(31, 2).addBox(0.0F, 0.0F, 0.0F, 12.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 23.0F, -1.308997F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail_3", CubeListBuilder.create()
                .texOffs(31, 2).addBox(0.0F, 0.0F, 0.0F, 12.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, -1.0F, 23.0F, -1.37881F, 0.0F, 0.0F));
        root.addOrReplaceChild("rightThigh", CubeListBuilder.create()
                .texOffs(20, 27).addBox(-1.0F, 0.0F, -3.0F, 4.0F, 9.0F, 4.0F),
                PartPose.offset(-4.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("leftThigh", CubeListBuilder.create()
                .texOffs(20, 27).addBox(-1.0F, 0.0F, -3.0F, 4.0F, 9.0F, 4.0F),
                PartPose.offset(4.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("rightShin", CubeListBuilder.create()
                .texOffs(17, 2).addBox(-1.0F, 8.0F, 1.0F, 3.0F, 9.0F, 3.0F),
                PartPose.offset(-4.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("leftShin", CubeListBuilder.create()
                .texOffs(17, 2).addBox(0.0F, 8.0F, 1.0F, 3.0F, 9.0F, 3.0F),
                PartPose.offset(4.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("talonRB", CubeListBuilder.create()
                .texOffs(111, 22).addBox(-0.5F, 13.0F, 9.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offset(-4.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("talonRR", CubeListBuilder.create()
                .texOffs(67, 20).addBox(-1.0F, 14.0F, -14.0F, 2.0F, 2.0F, 7.0F),
                PartPose.offset(-4.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("talonRL", CubeListBuilder.create()
                .texOffs(67, 20).addBox(0.5F, 14.0F, -14.0F, 2.0F, 2.0F, 7.0F),
                PartPose.offset(-4.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("talonLL", CubeListBuilder.create()
                .texOffs(67, 20).addBox(1.5F, 14.0F, -14.0F, 2.0F, 2.0F, 7.0F),
                PartPose.offset(4.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("talonLR", CubeListBuilder.create()
                .texOffs(67, 20).addBox(0.0F, 14.0F, -14.0F, 2.0F, 2.0F, 7.0F),
                PartPose.offset(4.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("talonLB", CubeListBuilder.create()
                .texOffs(111, 22).addBox(0.5F, 13.0F, 9.0F, 2.0F, 2.0F, 4.0F),
                PartPose.offset(4.0F, 4.0F, 5.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        float f = state.walkAnimationPos;
        float f1 = state.walkAnimationSpeed;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.neck.xRot = 0.0F;
        this.neck.yRot = this.head.yRot;
        this.feather.xRot = this.head.xRot + 0.1745329F;
        this.feather.yRot = this.head.yRot;
        this.feather2.xRot = this.head.xRot;
        this.feather2.yRot = this.head.yRot;
        this.rightLeg(Mth.cos(f * 0.6662F) * 1.4F * f1);
        this.leftLeg(Mth.cos(f * 0.6662F + PI) * 1.4F * f1);
        if (state.wing > 0.001F) {
            if (state.flutter) {
                rotate(this.rightWing, PI / 2.0F - PI / 12.0F, -0.0174533F, -state.wing);
                rotate(this.leftWing, PI / 2.0F - PI / 12.0F, 0.0174533F, state.wing);
            } else {
                rotate(this.rightWing, PI / 2.0F, -0.0174533F, -1.2F);
                rotate(this.leftWing, PI / 2.0F, 0.0174533F, 1.2F);
            }
            this.leftLeg(0.6F);
            this.rightLeg(0.6F);
        } else {
            rotate(this.rightWing, 0.0F, -0.0174533F, 0.0F);
            rotate(this.leftWing, 0.0F, 0.0174533F, 0.0F);
        }
    }

    private static void rotate(ModelPart part, float x, float y, float z) {
        part.xRot = x;
        part.yRot = y;
        part.zRot = z;
    }

    private void leftLeg(float dx) {
        rotate(this.leftThigh, 0.2094395F + dx, 0.0F, 0.0F);
        rotate(this.leftShin, -0.1919862F + dx, 0.0F, 0.0F);
        rotate(this.talonLR, 0.3490659F + dx, 0.1570796F, 0.0F);
        rotate(this.talonLL, 0.3490659F + dx, -0.1919862F, 0.0F);
        rotate(this.talonLB, (float) (-Math.PI / 6) + dx, 0.0F, 0.0F);
    }

    private void rightLeg(float dx) {
        rotate(this.rightThigh, 0.2094395F + dx, 0.0F, 0.0F);
        rotate(this.rightShin, -0.1919862F + dx, 0.0F, 0.0F);
        rotate(this.talonRR, 0.3490659F + dx, 0.1919862F, 0.0F);
        rotate(this.talonRL, 0.3490659F + dx, -0.1570796F, 0.0F);
        rotate(this.talonRB, (float) (-Math.PI / 6) + dx, 0.0F, 0.0F);
    }
}
