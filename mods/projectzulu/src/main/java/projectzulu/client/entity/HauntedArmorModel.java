package projectzulu.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import projectzulu.entity.HauntedArmorEntity;
import projectzulu.entity.ZuluMonster;

/**
 * ModelHauntedArmor: a biped made only of armour, drawn with the armour textures of what it wears (chestplate layer
 * 1 for head, body, arms and legs; leggings layer 2 for the inner legs at 85 %). Idle the pieces lie scattered on the
 * ground; on waking they fly together over 30 ticks; it walks with a swaying sword arm ("swordhand", an invisible
 * anchor for the held sword) and thrusts the sword forward during the attack animation. One mesh, two instances: the
 * main one hides the inner legs, the leggings one shows only them ({@link #legsOnly}).
 */
public class HauntedArmorModel extends EntityModel<HauntedArmorModel.State> implements ArmedModel<HauntedArmorModel.State> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart body2;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg2;
    private final ModelPart leftLeg2;
    private final ModelPart swordhand;
    private final boolean legsOnly;

    public static class State extends ArmedEntityRenderState {
        public boolean idle = true;
        public boolean attacking;
        public int wakeUpTimer;
        public int animTime;
        public Identifier chestTexture;
        public Identifier legsTexture;
    }

    public HauntedArmorModel(ModelPart root, boolean legsOnly) {
        super(root);
        this.legsOnly = legsOnly;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.body2 = root.getChild("body2");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        ModelPart legs2 = root.getChild("legs2");
        this.rightLeg2 = legs2.getChild("right_leg2");
        this.leftLeg2 = legs2.getChild("left_leg2");
        this.swordhand = root.getChild("swordhand");
        this.head.visible = !legsOnly;
        this.body.visible = !legsOnly;
        this.rightArm.visible = !legsOnly;
        this.leftArm.visible = !legsOnly;
        this.rightLeg.visible = !legsOnly;
        this.leftLeg.visible = !legsOnly;
        legs2.visible = legsOnly;
        // bipedBody2 was posed but never rendered
        this.body2.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4), PartPose.ZERO);
        root.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        // render(scale * 0.85): the inner legs scaled about the model origin
        PartDefinition legs2 = root.addOrReplaceChild("legs2", CubeListBuilder.create(), PartPose.ZERO.withScale(0.85F));
        legs2.addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        legs2.addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("swordhand", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, -7.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelHelper.mapValueofSet1ToSet2 (no clamping). */
    private static float map(float value, float min1, float max1, float min2, float max2) {
        return (value - min1) * ((max2 - min2) / (max1 - min1)) + min2;
    }

    private static void point(ModelPart part, float value, float min, float max, float x1, float y1, float z1, float x2, float y2, float z2) {
        part.x = map(value, min, max, x1, x2);
        part.y = map(value, min, max, y1, y2);
        part.z = map(value, min, max, z1, z2);
    }

    private static void rotation(ModelPart part, float value, float min, float max, float x1, float y1, float z1, float x2, float y2, float z2) {
        part.xRot = map(value, min, max, x1, x2);
        part.yRot = map(value, min, max, y1, y2);
        part.zRot = map(value, min, max, z1, z2);
    }

    private static void set(ModelPart part, float x, float y, float z, float xRot, float yRot, float zRot) {
        part.setPos(x, y, z);
        part.setRotation(xRot, yRot, zRot);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        if (state.idle) {
            set(this.head, -4.0F, 15.9F, 0.0F, 0.0F, 0.0F, Mth.HALF_PI);
            set(this.rightArm, -6.0F, 17.8F, 0.0F, -1.1519173F, 0.0F, 0.0F);
            set(this.leftArm, 7.0F, 23.0F, 4.0F, 0.0F, 0.0F, -Mth.HALF_PI);
            set(this.body, 0.0F, 22.0F, 9.5F, -Mth.HALF_PI, 0.0F, 0.0F);
            set(this.body2, 0.0F, 26.0F, 9.5F, -Mth.HALF_PI, 0.0F, 0.0F);
            set(this.rightLeg, -2.0F, 12.0F, -7.0F, 0.0F, 0.0F, 0.0F);
            set(this.rightLeg2, -9.0F, 26.0F, 0.0F, Mth.HALF_PI, -0.2268928F, 0.0F);
            set(this.leftLeg, 2.0F, 12.0F, -6.0F, 0.0F, 0.0F, 0.0F);
            set(this.leftLeg2, 7.0F, 26.0F, 0.0F, -Mth.HALF_PI, -0.034906585F, 0.0F);
            set(this.swordhand, 0.0F, 9.0F, -12.0F, Mth.HALF_PI, 0.0F, 0.0F);
        } else if (state.wakeUpTimer > 0) {
            float t = state.wakeUpTimer;
            float wake = HauntedArmorEntity.TIME_TO_WAKE_UP;
            point(this.head, t, wake, 0.0F, -6.0F, 17.8F, 0.0F, 0.0F, 0.0F, 0.0F);
            rotation(this.head, t, wake, 0.0F, 0.0F, 0.0F, Mth.HALF_PI, 0.0F, 0.0F, 0.0F);
            point(this.rightArm, t, wake, 0.0F, -6.0F, 17.8F, 0.0F, -5.0F, 2.0F, 0.0F);
            rotation(this.rightArm, t, wake, 0.0F, -1.1519173F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            point(this.leftArm, t, wake, 0.0F, 7.0F, 23.0F, 4.0F, 5.0F, 2.0F, 0.0F);
            rotation(this.leftArm, t, wake, 0.0F, 0.0F, 0.0F, -Mth.HALF_PI, 0.0F, 0.0F, 0.0F);
            point(this.body, t, wake, 0.0F, 0.0F, 22.0F, 9.5F, 0.0F, 0.0F, 0.0F);
            rotation(this.body, t, wake, 0.0F, -Mth.HALF_PI, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            point(this.rightLeg, t, wake, 0.0F, -2.0F, 12.0F, -7.0F, -1.9F, 12.0F, 0.0F);
            rotation(this.rightLeg, t, wake, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            point(this.rightLeg2, t, wake, 0.0F, -9.0F, 26.0F, 0.0F, -1.9F, 12.0F, 0.0F);
            rotation(this.rightLeg2, t, wake, 0.0F, Mth.HALF_PI, -0.2268928F, 0.0F, 0.0F, 0.0F, 0.0F);
            point(this.leftLeg, t, wake, 0.0F, 2.0F, 12.0F, -6.0F, 1.9F, 12.0F, 0.0F);
            rotation(this.leftLeg, t, wake, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            point(this.leftLeg2, t, wake, 0.0F, 7.0F, 26.0F, 0.0F, 1.9F, 12.0F, 0.0F);
            rotation(this.leftLeg2, t, wake, 0.0F, -Mth.HALF_PI, -0.034906585F, 0.0F, 0.0F, 0.0F, 0.0F);
            point(this.swordhand, t, wake, 0.0F, 0.0F, 9.0F, -12.0F, 0.0F, 9.0F, -12.0F);
            rotation(this.swordhand, t, wake, 0.0F, Mth.HALF_PI, 0.0F, 0.0F, -1.2217305F, 0.0F, 0.0F);
        } else {
            // the walking pose; in the original the attack branch only moved the sword and the rest kept the last
            // walking pose, so it is applied here first
            this.walk(state);
            if (state.animTime > 0 && state.attacking) {
                float max = ZuluMonster.MAX_ANIM_TIME;
                point(this.swordhand, state.animTime, max, 0.0F, 0.0F, 9.0F, -17.0F, 0.0F, 9.0F, -8.0F);
                rotation(this.swordhand, state.animTime, max, 0.0F, 0.0F, -Mth.PI / 18.0F, -Mth.PI / 6.0F,
                        -1.2217305F, Mth.PI / 18.0F, Mth.PI / 6.0F);
            }
        }
    }

    private void walk(State state) {
        float mapValue = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 30.0F;
        float start = 30.0F;
        float end = -30.0F;
        point(this.swordhand, mapValue, start, end, -4.0F, 9.0F, -8.0F, 6.0F, 9.0F, -4.0F);
        rotation(this.swordhand, mapValue, start, end, -Mth.PI * 2.0F / 9.0F, -Mth.PI / 18.0F, -Mth.PI / 6.0F,
                -1.2217305F, Mth.PI / 18.0F, Mth.PI / 6.0F);
        point(this.head, mapValue, start, end, 0.0F, -2.0F, 0.0F, 0.0F, 2.0F, 0.0F);
        rotation(this.head, mapValue, start, end, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.1F);
        point(this.rightArm, mapValue, start, end, -6.0F, 3.0F, 0.0F, -5.0F, 1.0F, 0.0F);
        rotation(this.rightArm, mapValue, start, end, -Mth.PI / 6.0F, 0.0F, 0.0F, Mth.PI / 6.0F, 0.0F, 0.0F);
        point(this.leftArm, mapValue, start, end, 6.0F, 3.0F, 0.0F, 5.0F, 1.0F, 0.0F);
        rotation(this.leftArm, mapValue, start, end, -Mth.PI / 6.0F, 0.0F, 0.05F, Mth.PI / 6.0F, 0.0F, 0.0F);
        point(this.body, mapValue, start, end, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F);
        rotation(this.body, mapValue, start, end, 0.0F, -0.05F, 0.0F, 0.0F, 0.05F, 0.0F);
        mapValue = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * state.walkAnimationSpeed * 45.0F;
        point(this.rightLeg, mapValue, start, end, -2.9F, 12.0F, 1.0F, -1.9F, 12.0F, 0.0F);
        rotation(this.rightLeg, mapValue, start, end, -Mth.PI / 6.0F, 0.0F, 0.05F, Mth.PI / 6.0F, 0.0F, 0.0F);
        point(this.rightLeg2, mapValue, start, end, -1.9F, 12.0F, 0.0F, -1.9F, 12.0F, 0.0F);
        rotation(this.rightLeg2, mapValue, start, end, -Mth.PI / 18.0F, 0.0F, 0.05F, Mth.PI / 18.0F, 0.0F, 0.0F);
        point(this.leftLeg, mapValue, start, end, 2.9F, 12.0F, -1.0F, 1.9F, 12.0F, 0.0F);
        rotation(this.leftLeg, mapValue, start, end, Mth.PI / 6.0F, 0.0F, 0.05F, -Mth.PI / 6.0F, 0.0F, 0.0F);
        point(this.leftLeg2, mapValue, start, end, 1.9F, 12.0F, 0.0F, 1.9F, 12.0F, 0.0F);
        rotation(this.leftLeg2, mapValue, start, end, Mth.PI / 18.0F, 0.0F, 0.05F, -Mth.PI / 18.0F, 0.0F, 0.0F);
    }

    /**
     * RenderEquipment drew the held item right at the swordhand anchor; vanilla's ItemInHandLayer adds the offset of
     * 1.5.2 RenderBiped (-1, 7, 1)/16 from an arm, so that offset is taken back here.
     */
    @Override
    public void translateToHand(State state, HumanoidArm arm, PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
        this.swordhand.translateAndRotate(poseStack);
        poseStack.translate(1.0F / 16.0F, -7.0F / 16.0F, -1.0F / 16.0F);
    }
}
