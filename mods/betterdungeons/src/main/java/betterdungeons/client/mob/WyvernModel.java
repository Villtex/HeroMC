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
 * ModelDragonQuadruped (the wyvern, 128x64): three body blocks (the outer two turned by 45 radians, as written), four
 * three-piece legs (tucked in while it flies), two folding wings (flapping in the air), a neck of four blocks curving
 * to the head, a tail of ten thick and ten thin pieces with a fin, waving with time and walk. The chains are placed
 * one piece after the other as ModelDragonQuadruped.render did with one ModelRenderer.
 */
public class WyvernModel extends EntityModel<WyvernModel.State> {
    private final ModelPart rightWing;
    private final ModelPart rightWingPart;
    private final ModelPart leftWing;
    private final ModelPart leftWingPart;
    private final ModelPart head;
    private final ModelPart[] leg = new ModelPart[4];
    private final ModelPart[] leg1 = new ModelPart[4];
    private final ModelPart[] foot = new ModelPart[4];
    private final ModelPart[] neck = new ModelPart[4];
    private final ModelPart[] tail = new ModelPart[10];
    private final ModelPart[] tail2 = new ModelPart[10];
    private final ModelPart tailEnd;

    public static class State extends LivingEntityRenderState {
        public boolean onGround;
    }

    public WyvernModel(ModelPart root) {
        super(root);
        this.rightWing = root.getChild("right_wing");
        this.rightWingPart = this.rightWing.getChild("right_wing_part");
        this.leftWing = root.getChild("left_wing");
        this.leftWingPart = this.leftWing.getChild("left_wing_part");
        this.head = root.getChild("head");
        for (int i = 0; i < 4; i++) {
            this.leg[i] = root.getChild("leg" + i);
            this.leg1[i] = this.leg[i].getChild("leg1");
            this.foot[i] = this.leg1[i].getChild("foot");
            this.neck[i] = root.getChild("neck" + i);
        }
        for (int i = 0; i < 10; i++) {
            this.tail[i] = root.getChild("tail" + i);
            this.tail2[i] = root.getChild("tail2_" + i);
        }
        this.tailEnd = root.getChild("tail_end");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -7.0F, -7.0F, 14.0F, 14.0F, 14.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -10.0F, 45.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(25, 40).addBox(-5.0F, -4.0F, -8.0F, 10.0F, 8.0F, 16.0F), PartPose.ZERO);
        root.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(0, 28).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 10.0F, 45.0F, 0.0F, 0.0F));
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("neck" + i, CubeListBuilder.create().texOffs(0, 51).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                    PartPose.offset(0.0F, -6.0F, -14.0F));
        }
        for (int i = 0; i < 10; i++) {
            root.addOrReplaceChild("tail" + i, CubeListBuilder.create().texOffs(40, 28).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                    PartPose.offset(0.0F, -3.0F, 14.0F));
            root.addOrReplaceChild("tail2_" + i, CubeListBuilder.create().texOffs(42, 30).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                    PartPose.offset(0.0F, -3.0F, 14.0F));
        }
        root.addOrReplaceChild("tail_end", CubeListBuilder.create().texOffs(44, 0).addBox(0.0F, -2.0F, -2.0F, 0.0F, 4.0F, 4.0F), PartPose.ZERO);
        float[][] legAt = {{8.0F, 5.0F, -10.0F}, {-10.0F, 5.0F, -10.0F}, {6.0F, 5.0F, 10.0F}, {-8.0F, 5.0F, 10.0F}};
        for (int i = 0; i < 4; i++) {
            PartDefinition leg = root.addOrReplaceChild("leg" + i, CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -4.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                    PartPose.offset(legAt[i][0], legAt[i][1], legAt[i][2]));
            PartDefinition leg1 = leg.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(56, 19).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 10.0F, 3.0F),
                    PartPose.offset(1.0F, 7.0F, -1.0F));
            leg1.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(52, 32).addBox(-1.5F, -1.0F, -4.0F, 3.0F, 2.0F, 5.0F),
                    PartPose.offset(0.0F, 11.0F, 0.0F));
        }
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(74, 0).addBox(-2.5F, -3.0F, -4.0F, 5.0F, 5.0F, 8.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("mouth_up", CubeListBuilder.create().texOffs(106, 0).addBox(-2.0F, -2.0F, -11.0F, 4.0F, 3.0F, 7.0F), PartPose.ZERO);
        head.addOrReplaceChild("mouth_down", CubeListBuilder.create().texOffs(92, 0).addBox(-2.0F, 1.0F, -10.0F, 4.0F, 1.0F, 6.0F), PartPose.ZERO);
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(107, 11).addBox(-2.0F, -3.0F, -11.0F, 4.0F, 1.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("horn_left", CubeListBuilder.create().texOffs(94, 8).addBox(1.5F, -2.0F, 3.0F, 1.0F, 1.0F, 8.0F),
                PartPose.rotation(0.5F, 0.098F, 0.0F));
        head.addOrReplaceChild("horn_right", CubeListBuilder.create().texOffs(94, 8).addBox(-2.5F, -2.0F, 3.0F, 1.0F, 1.0F, 8.0F),
                PartPose.rotation(0.5F, -0.298F, 0.0F));
        for (String side : new String[] {"right", "left"}) {
            PartDefinition wing = root.addOrReplaceChild(side + "_wing", CubeListBuilder.create().texOffs(58, 43).addBox(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 21.0F),
                    PartPose.offset(side.equals("right") ? 6.0F : -6.0F, -4.0F, -10.0F));
            PartDefinition part = wing.addOrReplaceChild(side + "_wing_part", CubeListBuilder.create().texOffs(46, 21)
                    .addBox(0.0F, 0.0F, 0.0F, 30.0F, 0.0F, 22.0F), PartPose.offset(14.0F, 0.0F, 0.0F));
            wing.addOrReplaceChild(side + "_wing_arm", CubeListBuilder.create().texOffs(82, 17).addBox(0.0F, -1.0F, 0.0F, 14.0F, 2.0F, 2.0F), PartPose.ZERO);
            part.addOrReplaceChild(side + "_wing_arm_part", CubeListBuilder.create().texOffs(64, 18).addBox(0.0F, -1.0F, 0.0F, 30.0F, 1.0F, 1.0F),
                    PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(State s) {
        super.setupAnim(s);
        float par1 = s.walkAnimationPos;
        float par2 = s.walkAnimationSpeed;
        float f2 = s.ageInTicks;
        // setRotationAngles
        float headX = s.xRot * Mth.DEG_TO_RAD;
        float headY = s.yRot * Mth.DEG_TO_RAD;
        float rx = Mth.cos(par1 * 0.6662F) * 1.4F * par2;
        for (int i = 0; i < 4; i++) {
            float sign = i % 2 == 0 ? 1.0F : -1.0F;
            this.leg[i].xRot = rx * sign;
            this.leg1[i].xRot = -0.436F - rx * 2.0F * sign;
            this.foot[i].xRot = 0.576F - rx * -0.3F * sign;
        }
        this.rightWing.zRot = -1.3F;
        this.rightWingPart.yRot = -1.5F;
        this.rightWingPart.zRot = 0.0F;
        if (!s.onGround) {
            for (int i = 0; i < 4; i++) {
                this.leg[i].xRot = -0.9F;
                this.leg1[i].xRot = 2.4F;
            }
            float wingRotation = Mth.cos(par1 * 0.26F) * par2 * 0.85F;
            this.rightWing.zRot = wingRotation;
            this.rightWingPart.zRot = wingRotation / 2.0F;
            this.rightWingPart.yRot = -0.25F;
        }
        this.leftWing.zRot = 3.1416F - this.rightWing.zRot;
        this.leftWingPart.zRot = -this.rightWingPart.zRot;
        this.leftWingPart.yRot = this.rightWingPart.yRot;
        // render(): the neck chain, the head at its end, the two tails
        float px = 0.0F;
        float py = -6.0F;
        float pz = -14.0F;
        float neckY = 0.0F;
        for (int i = 0; i < 4; i++) {
            ModelPart n = this.neck[i];
            n.xRot = i / 4.0F - 0.4F + headX;
            n.yRot = headY * (i / 1.8F);
            n.zRot = headY / 10.0F;
            n.x = px;
            n.y = py;
            n.z = pz;
            px = (float) (px - Math.sin(n.yRot) * Math.cos(n.xRot) * 6.0);
            py = (float) (py + Math.sin(n.xRot) * 6.0);
            pz = (float) (pz - Math.cos(n.yRot) * Math.cos(n.xRot) * 6.0);
            neckY = n.yRot;
        }
        this.head.x = px;
        this.head.y = py - 0.4F;
        this.head.z = pz;
        this.head.xRot = headX;
        this.head.yRot = neckY;
        px = 0.0F;
        py = -3.0F;
        pz = 14.0F;
        float lastTailX = 0.0F;
        for (int i = 0; i < 10; i++) {
            ModelPart t = this.tail[i];
            float rotXVar = (float) Math.cos((i / 10.0 + par1 / 10.0F) * Math.PI * 2.0) * 0.5F;
            float animOnTime = (float) Math.cos((i / 10.0 + f2 / 50.0F) * Math.PI * 2.0);
            t.xRot = 3.16F + rotXVar;
            t.yRot = animOnTime;
            t.zRot = 0.0F;
            t.x = px;
            t.y = py;
            t.z = pz;
            px = (float) (px - Math.sin(t.yRot) * Math.cos(t.xRot) * 4.0);
            py = (float) (py + Math.sin(t.xRot) * 4.0);
            pz = (float) (pz - Math.cos(t.yRot) * Math.cos(t.xRot) * 4.0);
            lastTailX = t.xRot;
        }
        px += 2.0F;
        pz -= 2.0F;
        for (int i = 0; i < 10; i++) {
            ModelPart t = this.tail2[i];
            float f10 = (float) Math.cos((i / 10.0 + par1 / 10.0F) * Math.PI * 2.0);
            float animOnTime = (float) Math.cos((i / 10.0 + f2 / 40.0F) * Math.PI * 2.0);
            t.xRot = 3.16F + f10;
            t.yRot = animOnTime;
            t.zRot = 0.0F;
            t.x = px;
            t.y = py;
            t.z = pz;
            // (the original used the thick tail's last pitch here)
            px = (float) (px - Math.sin(t.yRot) * Math.cos(lastTailX) * 2.0);
            py = (float) (py + Math.sin(t.xRot) * 2.0);
            pz = (float) (pz - Math.cos(t.yRot) * Math.cos(lastTailX) * 2.0);
        }
        this.tailEnd.x = px;
        this.tailEnd.y = py;
        this.tailEnd.z = pz;
    }
}
