package betterdungeons.client.mob;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * ModelGiant (the Monking: a ModelBiped with forearms, ears, a muzzle and a 15 piece tail, 64x32, drawn twice the size
 * and 0.6 blocks up, always crouched). The pose is ModelBiped.setRotationAngles of 1.5.2 with isSneak and no swing,
 * then the giant's own: bent body, swinging forearms, the arm wind-up of the attacks, legs moved back (and pulled up in
 * the body slam). The headwear of ModelBiped is drawn too (texture at 32, 0). The tail pieces are placed one after the
 * other along the waving chain.
 */
public class MonkingModel extends EntityModel<MonkingModel.State> {
    private static final int TAIL = 15;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightArm1;
    private final ModelPart leftArm1;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart[] tail = new ModelPart[TAIL];

    /** setLivingAnimations of the giant (and the golem boss). */
    public static class State extends LivingEntityRenderState {
        public boolean attacking;
        public int attackAnim;
        public int attackAnimTime;
        public int attackType;
        /** Golem boss: health / max health and whether to draw the sparks. */
        public float renderHeal = 1.0F;
        public boolean powersource;
        public int entityId;
        public int tickCount;
    }

    public MonkingModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.hat = root.getChild("hat");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightArm1 = this.rightArm.getChild("rightarm1");
        this.leftArm1 = this.leftArm.getChild("leftarm1");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        for (int i = 0; i < TAIL; i++) {
            this.tail[i] = root.getChild("tail" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        head.addOrReplaceChild("earR", CubeListBuilder.create().texOffs(0, 0).addBox(4.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("earL", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(25, 0).addBox(-2.0F, -3.0F, -6.0F, 4.0F, 3.0F, 3.0F), PartPose.ZERO);
        // ModelBiped's own headwear, which the giant kept
        root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("rightarm1", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), PartPose.offset(-1.0F, 10.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("leftarm1", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), PartPose.offset(1.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        for (int i = 0; i < TAIL; i++) {
            root.addOrReplaceChild("tail" + i, CubeListBuilder.create().texOffs(0, 20).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                    PartPose.offset(0.0F, -3.0F, 14.0F));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(State s) {
        super.setupAnim(s);
        float f = s.walkAnimationPos;
        float f1 = s.walkAnimationSpeed;
        float f2 = s.ageInTicks;
        // render(): glScalef(2, 2, 2) then glTranslatef(0, -0.6, 0)
        this.root.y = -19.2F;
        this.root.xScale = 2.0F;
        this.root.yScale = 2.0F;
        this.root.zScale = 2.0F;
        bipedPose(this.head, this.hat, this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, s);
        // ModelGiant.setRotationAngles
        this.body.xRot += 0.2F;
        this.rightArm1.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 2.0F * f1 * 0.5F;
        this.rightArm1.zRot = Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        this.rightArm1.xRot += Mth.sin(f2 * 0.067F) * 0.05F;
        this.leftArm1.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 2.0F * f1 * 0.5F;
        attackPose(this.rightArm, this.leftArm, s);
        this.rightArm.xRot += -0.5F;
        this.leftArm.xRot += -0.5F;
        this.leftLeg.x = 2.0F;
        this.leftLeg.y = 9.0F;
        this.leftLeg.z = 5.0F;
        this.rightLeg.x = -2.0F;
        this.rightLeg.y = 9.0F;
        this.rightLeg.z = 5.0F;
        slamLegs(this.rightLeg, this.leftLeg, s);
        // the tail chain
        float px = -0.5F;
        float py = 8.0F;
        float pz = 9.5F;
        for (int i = 0; i < TAIL; i++) {
            ModelPart t = this.tail[i];
            float f10 = (float) Math.cos((15.0 - i / 15.0 + f / 10.0F) * Math.PI * 2.0);
            float animOnTime = (float) Math.cos((i / 20.0 + f2 / 50.0F) * Math.PI * 2.0);
            t.xRot = 3.86F + f10 / 10.0F;
            t.yRot = animOnTime;
            t.zRot = 0.0F;
            t.x = px;
            t.y = py;
            t.z = pz;
            px = (float) (px - Math.sin(t.yRot) * Math.cos(t.xRot) * 0.8);
            py = (float) (py + Math.sin(t.xRot) * 0.8);
            pz = (float) (pz - Math.cos(t.yRot) * Math.cos(t.xRot) * 0.8);
        }
    }

    /** ModelBiped.setRotationAngles of 1.5.2 with isSneak, nothing held, no swing (onGround 0). */
    static void bipedPose(ModelPart head, ModelPart hat, ModelPart body, ModelPart rightArm, ModelPart leftArm, ModelPart rightLeg, ModelPart leftLeg,
            LivingEntityRenderState s) {
        float f = s.walkAnimationPos;
        float f1 = s.walkAnimationSpeed;
        float f2 = s.ageInTicks;
        head.yRot = s.yRot * Mth.DEG_TO_RAD;
        head.xRot = s.xRot * Mth.DEG_TO_RAD;
        hat.yRot = head.yRot;
        hat.xRot = head.xRot;
        rightArm.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 2.0F * f1 * 0.5F;
        leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
        rightArm.zRot = 0.0F;
        leftArm.zRot = 0.0F;
        rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        leftLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * f1;
        rightLeg.yRot = 0.0F;
        leftLeg.yRot = 0.0F;
        rightArm.yRot = 0.0F;
        leftArm.yRot = 0.0F;
        // onGround (swing) 0: body straight, arms at their sides
        body.yRot = 0.0F;
        rightArm.z = 0.0F;
        rightArm.x = -5.0F;
        leftArm.z = 0.0F;
        leftArm.x = 5.0F;
        // isSneak
        body.xRot = 0.5F;
        rightArm.xRot += 0.4F;
        leftArm.xRot += 0.4F;
        rightLeg.z = 4.0F;
        leftLeg.z = 4.0F;
        rightLeg.y = 9.0F;
        leftLeg.y = 9.0F;
        head.y = 1.0F;
        hat.y = 1.0F;
        rightArm.zRot += Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        leftArm.zRot -= Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        rightArm.xRot += Mth.sin(f2 * 0.067F) * 0.05F;
        leftArm.xRot -= Mth.sin(f2 * 0.067F) * 0.05F;
    }

    /** The arm wind-up of the attack types (ModelGiant and ModelGolemBoss alike). */
    static void attackPose(ModelPart rightArm, ModelPart leftArm, State s) {
        if (!s.attacking) {
            return;
        }
        int attackAnim = s.attackAnim;
        int attackAnimTime = s.attackAnimTime;
        int attackType = s.attackType;
        boolean firstHalf = attackAnim < attackAnimTime / 2 + 2;
        if (attackType == 0 || attackType == 2) {
            rightArm.xRot += firstHalf ? -attackAnim / 6.0F : (-attackAnimTime - 4 + attackAnim) / 4.0F;
        }
        if (attackType == 1 || attackType == 2) {
            leftArm.xRot += firstHalf ? -attackAnim / 6.0F : (-attackAnimTime - 4 + attackAnim) / 4.0F;
        }
        if (attackType == 4 || attackType == 6) {
            rightArm.xRot -= firstHalf ? -attackAnim / 15.0F : (-attackAnimTime + 8 + attackAnim) / 4.0F;
        }
        if (attackType == 5 || attackType == 6 || attackType == 7 || attackType == 8) {
            leftArm.xRot -= firstHalf ? -attackAnim / 15.0F : (-attackAnimTime + 8 + attackAnim) / 4.0F;
        }
    }

    /** The legs pulled up in the body slam (attack type 3). */
    static void slamLegs(ModelPart rightLeg, ModelPart leftLeg, State s) {
        if (s.attackType == 3 && s.attacking) {
            rightLeg.xRot = (float) (-Math.PI / 4);
            leftLeg.xRot = (float) (-Math.PI / 4);
            rightLeg.yRot = 0.07853982F;
            leftLeg.yRot = -0.07853982F;
        }
    }
}
