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
 * ModelPenguin: an upright body carrying head with beak, tail, two flippers and two legs with three-toed feet.
 * Walking it waddles (the body rolls), the flippers spread and the tail wags.
 */
public class PenguinModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart bodyrot;
    private final ModelPart headrot;
    private final ModelPart tailrot;
    private final ModelPart wingrigrot;
    private final ModelPart winglefrot;
    private final ModelPart leglefrot;
    private final ModelPart legrigrot;

    public PenguinModel(ModelPart root) {
        super(root);
        this.bodyrot = root.getChild("bodyrot");
        this.headrot = this.bodyrot.getChild("headrot");
        this.tailrot = this.bodyrot.getChild("tailrot");
        this.wingrigrot = this.bodyrot.getChild("wingrigrot");
        this.winglefrot = this.bodyrot.getChild("winglefrot");
        this.leglefrot = this.bodyrot.getChild("leglefrot");
        this.legrigrot = this.bodyrot.getChild("legrigrot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bodyrot = root.addOrReplaceChild("bodyrot", CubeListBuilder.create().mirror()
                        .texOffs(0, 13).addBox(-4.5F, -10.0F, -4.0F, 9, 13, 6)
                        .texOffs(37, 0).addBox(-3.0F, -7.5F, -5.0F, 6, 9, 1),
                PartPose.offset(0.0F, 17.0F, 0.0F));
        bodyrot.addOrReplaceChild("headrot", CubeListBuilder.create().mirror()
                        .texOffs(0, 0).addBox(-3.0F, -5.0F, -3.0F, 6, 5, 6)
                        .texOffs(24, 0).addBox(-1.0F, -2.5F, -7.0F, 2, 1, 4)
                        .texOffs(24, 5).addBox(-0.5F, -1.5F, -6.0F, 1, 1, 3),
                PartPose.offset(0.0F, -10.0F, -1.5F));
        bodyrot.addOrReplaceChild("tailrot", CubeListBuilder.create().mirror()
                        .texOffs(50, 11).addBox(-1.0F, 1.0F, 2.0F, 2, 1, 3)
                        .texOffs(40, 11).addBox(-1.5F, 0.0F, 0.0F, 3, 2, 2)
                        .texOffs(30, 11).addBox(-2.0F, -1.0F, -1.0F, 4, 3, 1),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        bodyrot.addOrReplaceChild("wingrigrot", CubeListBuilder.create().mirror()
                .texOffs(44, 15).addBox(0.0F, 0.0F, -3.0F, 1, 11, 6), PartPose.offset(4.5F, -10.0F, -1.0F));
        bodyrot.addOrReplaceChild("winglefrot", CubeListBuilder.create().mirror()
                .texOffs(30, 15).addBox(-1.0F, 0.0F, -3.0F, 1, 11, 6), PartPose.offset(-4.5F, -10.0F, -1.0F));
        leg(bodyrot, "lef", -3.0F, Mth.PI / 18.0F, 0.0F);
        leg(bodyrot, "rig", 3.0F, 0.0F, -Mth.PI / 18.0F);
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** LEGxxxROT -> FOOTxxxROT (no box) -> TOExxx2ROT, TOExxx1ROT, TOExxx3ROT; toe 1 and toe 3 turned as set each frame. */
    private static void leg(PartDefinition body, String side, float x, float toe1Yaw, float toe3Yaw) {
        PartDefinition leg = body.addOrReplaceChild("leg" + side + "rot", CubeListBuilder.create().mirror()
                .texOffs(52, 0).addBox(-1.5F, 0.0F, -1.5F, 3, 4, 3), PartPose.offset(x, 3.0F, -0.5F));
        PartDefinition foot = leg.addOrReplaceChild("foot" + side + "rot", CubeListBuilder.create(), PartPose.offset(0.0F, 3.5F, -1.0F));
        CubeListBuilder toe = CubeListBuilder.create().mirror()
                .texOffs(56, 7).addBox(-1.0F, -0.5F, -2.0F, 2, 1, 2)
                .texOffs(60, 19).addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1)
                .texOffs(58, 16).addBox(-0.5F, -1.5F, -2.0F, 1, 1, 2);
        foot.addOrReplaceChild("toe" + side + "2rot", toe, PartPose.offset(0.0F, 0.0F, -0.5F));
        foot.addOrReplaceChild("toe" + side + "1rot", toe, PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0F, toe1Yaw, 0.0F));
        foot.addOrReplaceChild("toe" + side + "3rot", toe, PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0F, toe3Yaw, 0.0F));
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float swing = state.walkAnimationPos * 0.6662F * 2.0F;
        float amount = state.walkAnimationSpeed;
        this.legrigrot.xRot = Mth.cos(swing) * 1.8F * amount;
        this.leglefrot.xRot = Mth.cos(swing + Mth.PI) * 1.8F * amount;
        this.bodyrot.zRot = Mth.cos(swing) * 0.1F * amount;
        this.tailrot.xRot = -Mth.PI / 9.0F + Mth.cos(swing) * 0.5F * amount;
        this.wingrigrot.zRot = -Math.abs(Mth.cos(swing) * amount);
        this.winglefrot.zRot = Math.abs(Mth.cos(swing) * amount);
        this.headrot.xRot = Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD;
        this.headrot.yRot = Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
    }
}
