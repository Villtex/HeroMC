package twilightforest.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * ModelTFTowerGolem: the carminite golem; legs and arms swing on a triangle wave, both arms slam down while the attack
 * timer runs (setLivingAnimations; the timer is in {@link TFRenderStates.Flags#value}).
 */
public class TowerGolemModel extends EntityModel<TFRenderStates.Flags> {
    private final ModelPart head;
    private final ModelPart rightarm;
    private final ModelPart leftarm;
    private final ModelPart rightleg;
    private final ModelPart leftleg;

    public TowerGolemModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightarm = root.getChild("rightarm");
        this.leftarm = root.getChild("leftarm");
        this.rightleg = root.getChild("rightleg");
        this.leftleg = root.getChild("leftleg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -10.0F, -3.0F, 7.0F, 8.0F, 6.0F)
                .texOffs(0, 14).addBox(-4.0F, -6.0F, -3.5F, 8.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, -11.0F, -2.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-8.0F, 0.0F, -5.0F, 16.0F, 10.0F, 10.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F));
        root.addOrReplaceChild("rightarm", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-5.0F, -2.0F, -1.5F, 3.0F, 14.0F, 3.0F)
                .texOffs(52, 17).addBox(-7.0F, 12.0F, -3.0F, 6.0F, 12.0F, 6.0F)
                .texOffs(52, 36).addBox(-7.0F, -3.0F, -3.5F, 7.0F, 2.0F, 7.0F)
                .texOffs(52, 45).addBox(-7.0F, -1.0F, -3.5F, 7.0F, 5.0F, 2.0F)
                .texOffs(52, 45).addBox(-7.0F, -1.0F, 1.5F, 7.0F, 5.0F, 2.0F)
                .texOffs(52, 54).addBox(-2.0F, -1.0F, -2.0F, 2.0F, 5.0F, 3.0F),
                PartPose.offset(-8.0F, -12.0F, 0.0F));
        root.addOrReplaceChild("leftarm", CubeListBuilder.create().mirror()
                .texOffs(52, 0).addBox(2.0F, -2.0F, -1.5F, 3.0F, 14.0F, 3.0F)
                .texOffs(52, 17).addBox(1.0F, 12.0F, -3.0F, 6.0F, 12.0F, 6.0F)
                .texOffs(52, 36).addBox(0.0F, -3.0F, -3.5F, 7.0F, 2.0F, 7.0F)
                .texOffs(52, 45).addBox(0.0F, -1.0F, -3.5F, 7.0F, 5.0F, 2.0F)
                .texOffs(52, 45).addBox(0.0F, -1.0F, 1.5F, 7.0F, 5.0F, 2.0F)
                .texOffs(52, 54).addBox(0.0F, -1.0F, -2.0F, 2.0F, 5.0F, 3.0F),
                PartPose.offset(8.0F, -12.0F, 0.0F));
        root.addOrReplaceChild("rightleg", CubeListBuilder.create()
                .texOffs(84, 32).addBox(-3.0F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F)
                .texOffs(84, 43).addBox(-5.5F, 8.0F, -4.0F, 6.0F, 14.0F, 7.0F),
                PartPose.offset(-1.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("leftleg", CubeListBuilder.create().mirror()
                .texOffs(84, 32).addBox(0.0F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F)
                .texOffs(84, 43).addBox(-0.5F, 8.0F, -4.0F, 6.0F, 14.0F, 7.0F),
                PartPose.offset(1.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("ribs", CubeListBuilder.create()
                .texOffs(0, 46).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        root.addOrReplaceChild("hips", CubeListBuilder.create()
                .texOffs(84, 25).addBox(-5.0F, 0.0F, -2.0F, 10.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        root.addOrReplaceChild("spine", CubeListBuilder.create()
                .texOffs(84, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** func_78172_a of 1.5.2 (IronGolem's triangle wave). */
    static float triangle(float f, float period) {
        return (Math.abs(f % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
    }

    @Override
    public void setupAnim(TFRenderStates.Flags state) {
        super.setupAnim(state);
        float f = state.walkAnimationPos;
        float f1 = state.walkAnimationSpeed;
        // setLivingAnimations
        int timer = (int) state.value;
        if (timer > 0) {
            this.rightarm.xRot = -2.0F + 1.5F * triangle(timer - state.partialTick, 10.0F);
            this.leftarm.xRot = -2.0F + 1.5F * triangle(timer - state.partialTick, 10.0F);
        } else {
            this.rightarm.xRot = (-0.2F + 1.5F * triangle(f, 25.0F)) * f1;
            this.leftarm.xRot = (-0.2F - 1.5F * triangle(f, 25.0F)) * f1;
        }
        // setRotationAngles
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.leftleg.xRot = -1.5F * triangle(f, 13.0F) * f1;
        this.rightleg.xRot = 1.5F * triangle(f, 13.0F) * f1;
        this.leftleg.yRot = 0.0F;
        this.rightleg.yRot = 0.0F;
        this.rightarm.zRot = Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.leftarm.zRot = -Mth.cos(state.ageInTicks * 0.09F) * 0.05F - 0.05F;
    }
}
