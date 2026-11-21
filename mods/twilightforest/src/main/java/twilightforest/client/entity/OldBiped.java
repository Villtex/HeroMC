package twilightforest.client.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

/** ModelBiped of 1.5.2 on today's HumanoidModel: the part names and setRotationAngles. */
public class OldBiped extends HumanoidModel<HumanoidRenderState> {
    public static final java.util.Map<String, String> NAMES = java.util.Map.of("bipedHead", "head", "bipedHeadwear", "hat",
            "bipedBody", "body", "bipedRightArm", "right_arm", "bipedLeftArm", "left_arm", "bipedRightLeg", "right_leg", "bipedLeftLeg", "left_leg");

    public OldBiped(ModelPart root) {
        super(root);
    }

    /**
     * Empty parts for the humanoid parts a model of 1.5.2 does not have (HumanoidModel needs all seven). HumanoidModel
     * reads the hat as a child of the head: a headwear at the root is moved under the head (relative to its pivot), it
     * then follows the head as bipedHeadwear did in 1.5.2 (rotation copied from the head).
     */
    public static void fill(PartDefinition root) {
        for (String name : new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"}) {
            if (!has(root, name)) {
                root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
            }
        }
        PartDefinition head = root.getChild("head");
        if (has(head, "hat")) {
            return;
        }
        if (!has(root, "hat")) {
            head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
            return;
        }
        PartPose[] headPose = new PartPose[1];
        head.transformed(p -> headPose[0] = p);
        PartPose h = headPose[0];
        PartDefinition hat = root.getChild("hat").transformed(p -> new PartPose(p.x() - h.x(), p.y() - h.y(), p.z() - h.z(), p.xRot(), p.yRot(),
                p.zRot(), p.xScale(), p.yScale(), p.zScale()));
        root.clearChild("hat");
        head.addOrReplaceChild("hat", hat);
    }

    private static boolean has(PartDefinition part, String name) {
        return part.getChildren().stream().anyMatch(e -> e.getKey().equals(name));
    }

    @Override
    public void setupAnim(HumanoidRenderState s) {
        this.resetPose();
        angles(this, s);
    }

    /** ModelBiped.setRotationAngles (f = walk position, f1 = walk speed, f2 = age in ticks). */
    public static void angles(HumanoidModel<?> m, HumanoidRenderState s) {
        float f = s.walkAnimationPos;
        float f1 = s.walkAnimationSpeed;
        float f2 = s.ageInTicks;
        m.head.yRot = s.yRot * Mth.DEG_TO_RAD;
        m.head.xRot = s.xRot * Mth.DEG_TO_RAD;
        m.rightArm.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 2.0F * f1 * 0.5F;
        m.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
        m.rightArm.zRot = 0.0F;
        m.leftArm.zRot = 0.0F;
        m.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        m.leftLeg.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
        m.rightLeg.yRot = 0.0F;
        m.leftLeg.yRot = 0.0F;
        if (s.isPassenger) {
            m.rightArm.xRot += -Mth.PI / 5.0F;
            m.leftArm.xRot += -Mth.PI / 5.0F;
            m.rightLeg.xRot = -Mth.PI * 2.0F / 5.0F;
            m.leftLeg.xRot = -Mth.PI * 2.0F / 5.0F;
            m.rightLeg.yRot = Mth.PI / 10.0F;
            m.leftLeg.yRot = -Mth.PI / 10.0F;
        }
        if (!s.rightHandItemStack.isEmpty()) {
            m.rightArm.xRot = m.rightArm.xRot * 0.5F - Mth.PI / 10.0F;
        }
        m.rightArm.yRot = 0.0F;
        m.leftArm.yRot = 0.0F;
        float swing = s.swingAnimation;
        m.body.yRot = Mth.sin(Mth.sqrt(swing) * Mth.PI * 2.0F) * 0.2F;
        m.rightArm.z = Mth.sin(m.body.yRot) * 5.0F;
        m.rightArm.x = -Mth.cos(m.body.yRot) * 5.0F;
        m.leftArm.z = -Mth.sin(m.body.yRot) * 5.0F;
        m.leftArm.x = Mth.cos(m.body.yRot) * 5.0F;
        m.rightArm.yRot += m.body.yRot;
        m.leftArm.yRot += m.body.yRot;
        m.leftArm.xRot += m.body.yRot;
        float var8 = 1.0F - swing;
        var8 *= var8;
        var8 *= var8;
        var8 = 1.0F - var8;
        float var9 = Mth.sin(var8 * Mth.PI);
        float var10 = Mth.sin(swing * Mth.PI) * -(m.head.xRot - 0.7F) * 0.75F;
        m.rightArm.xRot = (float) (m.rightArm.xRot - (var9 * 1.2 + var10));
        m.rightArm.yRot += m.body.yRot * 2.0F;
        m.rightArm.zRot = Mth.sin(swing * Mth.PI) * -0.4F;
        m.body.xRot = 0.0F;
        m.rightLeg.z = 0.1F;
        m.leftLeg.z = 0.1F;
        m.rightLeg.y = 12.0F;
        m.leftLeg.y = 12.0F;
        m.head.y = 0.0F;
        m.rightArm.zRot += Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        m.leftArm.zRot -= Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
        m.rightArm.xRot += Mth.sin(f2 * 0.067F) * 0.05F;
        m.leftArm.xRot -= Mth.sin(f2 * 0.067F) * 0.05F;
    }
}
