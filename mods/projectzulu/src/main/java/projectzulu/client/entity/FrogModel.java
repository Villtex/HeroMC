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
 * ModelFrog: body with head (eyes, crest, two-part mouth) and four legs of three segments each. The original drew
 * the whole model at half scale (BODYROT.render(0.5F * f5)) with the pivot lowered by 30 (heightToRaise), which
 * here is the 0.5 scale of the root part. On the ground the frog sits up with folded legs, in the air (hopping) it
 * stretches out.
 */
public class FrogModel extends EntityModel<FrogModel.State> {
    private static final float HEIGHT_TO_RAISE = -30.0F;

    private final ModelPart bodyrot;
    private final ModelPart headrot;
    private final ModelPart leg1toprot;
    private final ModelPart leg1botrot;
    private final ModelPart leg2toprot;
    private final ModelPart leg2botrot;
    private final ModelPart leg3toprot;
    private final ModelPart leg3botrot;
    private final ModelPart leg4toprot;
    private final ModelPart leg4botrot;

    public static class State extends LivingEntityRenderState {
        public boolean onGround = true;
    }

    public FrogModel(ModelPart root) {
        super(root);
        this.bodyrot = root.getChild("bodyrot");
        this.headrot = this.bodyrot.getChild("headrot");
        this.leg1toprot = this.bodyrot.getChild("leg1toprot");
        this.leg1botrot = this.leg1toprot.getChild("leg1botrot");
        this.leg2toprot = this.bodyrot.getChild("leg2toprot");
        this.leg2botrot = this.leg2toprot.getChild("leg2botrot");
        this.leg3toprot = this.bodyrot.getChild("leg3toprot");
        this.leg3botrot = this.leg3toprot.getChild("leg3botrot");
        this.leg4toprot = this.bodyrot.getChild("leg4toprot");
        this.leg4botrot = this.leg4toprot.getChild("leg4botrot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bodyrot = root.addOrReplaceChild("bodyrot", CubeListBuilder.create().mirror()
                        .texOffs(0, 18).addBox(-3.5F, -3.0F, -9.0F, 7, 5, 9)
                        .texOffs(0, 12).addBox(-2.5F, -4.0F, -7.0F, 5, 1, 5),
                PartPose.offset(0.0F, 15.0F - HEIGHT_TO_RAISE, 7.0F));
        leg(bodyrot, 2, 4.0F, -2.0F);
        leg(bodyrot, 1, -4.0F, -2.0F);
        leg(bodyrot, 3, 3.0F, -8.0F);
        PartDefinition headrot = bodyrot.addOrReplaceChild("headrot", CubeListBuilder.create().mirror()
                        .texOffs(0, 0).addBox(-2.5F, -5.0F, -2.0F, 1, 3, 2)
                        .texOffs(6, 0).addBox(1.5F, -5.0F, -2.0F, 1, 3, 2)
                        .texOffs(28, 0).addBox(-0.5F, -4.0F, -2.0F, 1, 2, 4)
                        .texOffs(12, 0).addBox(-1.5F, -4.5F, -2.0F, 1, 3, 3)
                        .texOffs(24, 7).addBox(-2.5F, -3.0F, 0.0F, 5, 1, 2)
                        .texOffs(20, 0).addBox(0.5F, -4.5F, -2.0F, 1, 3, 3),
                PartPose.offset(0.0F, -1.0F, -9.0F));
        headrot.addOrReplaceChild("mouthtoprot", CubeListBuilder.create().mirror()
                        .texOffs(46, 0).addBox(-3.5F, -2.0F, -2.0F, 7, 2, 2)
                        .texOffs(50, 4).addBox(-3.0F, -1.5F, -3.0F, 6, 3, 1)
                        .texOffs(52, 8).addBox(-2.5F, -1.0F, -4.0F, 5, 2, 1)
                        .texOffs(54, 11).addBox(-2.0F, -0.5F, -5.0F, 4, 2, 1)
                        .texOffs(56, 14).addBox(-1.5F, 0.0F, -6.0F, 3, 1, 1),
                PartPose.ZERO);
        headrot.addOrReplaceChild("mouthbotrot", CubeListBuilder.create().mirror()
                        .texOffs(46, 16).addBox(-3.5F, 0.0F, -2.0F, 7, 3, 2)
                        .texOffs(50, 21).addBox(-3.0F, 1.0F, -3.0F, 6, 2, 1)
                        .texOffs(52, 24).addBox(-2.5F, 1.0F, -4.0F, 5, 2, 1)
                        .texOffs(54, 27).addBox(-2.0F, 1.0F, -5.0F, 4, 2, 1)
                        .texOffs(46, 29).addBox(-1.5F, 1.0F, -6.0F, 3, 2, 1),
                PartPose.ZERO);
        leg(bodyrot, 4, -3.0F, -8.0F);
        // the whole frog at half scale
        return LayerDefinition.create(mesh, 64, 32).apply(m -> m.transformed(pose -> pose.scaled(0.5F)));
    }

    /** LEGnTOPROT -> LEGnBOTROT -> FOOTROTn with the back foot and three toes. */
    private static void leg(PartDefinition body, int n, float x, float z) {
        PartDefinition top = body.addOrReplaceChild("leg" + n + "toprot", CubeListBuilder.create().mirror()
                .texOffs(32, 18).addBox(-0.5F, 0.0F, -1.0F, 1, 4, 2), PartPose.offset(x, 0.0F, z));
        PartDefinition bot = top.addOrReplaceChild("leg" + n + "botrot", CubeListBuilder.create().mirror()
                .texOffs(32, 24).addBox(-0.5F, 0.0F, -1.0F, 1, 4, 2), PartPose.offset(0.0F, 4.0F, 0.0F));
        bot.addOrReplaceChild("footrot" + n, CubeListBuilder.create().mirror()
                        .texOffs(38, 18).addBox(-1.0F, -0.5F, 0.0F, 2, 1, 2)
                        .texOffs(38, 21).addBox(-2.0F, -0.5F, -0.5F, 2, 1, 1)
                        .texOffs(38, 23).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2)
                        .texOffs(38, 21).addBox(0.0F, -0.5F, -0.5F, 2, 1, 1),
                PartPose.offset(0.0F, 4.5F, 0.0F));
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        if (!state.onGround) {
            this.bodyrot.xRot = 0.0F;
            this.leg2toprot.xRot = Mth.PI / 3.0F;
            this.leg2toprot.yRot = 0.0F;
            this.leg2toprot.zRot = Mth.PI / 3.0F;
            this.leg2botrot.xRot = Mth.PI / 4.0F;
            this.leg2botrot.yRot = 0.0F;
            this.leg1toprot.xRot = Mth.PI / 3.0F;
            this.leg1toprot.yRot = 0.0F;
            this.leg1toprot.zRot = Mth.PI / 3.0F;
            this.leg1botrot.xRot = Mth.PI / 4.0F;
            this.leg1botrot.yRot = 0.0F;
            this.leg3toprot.xRot = -Mth.PI / 3.0F;
            this.leg3botrot.xRot = 0.0F;
            this.leg4toprot.xRot = -Mth.PI / 3.0F;
            this.leg4botrot.xRot = 0.0F;
            this.headrot.xRot = 0.0F;
        } else {
            this.bodyrot.xRot = -Mth.PI / 4.0F;
            this.leg2toprot.xRot = -1.134464F;
            this.leg2toprot.yRot = -Mth.PI / 6.0F;
            this.leg2toprot.zRot = 0.0F;
            this.leg2botrot.xRot = 1.9198622F;
            this.leg2botrot.yRot = 0.43633232F;
            this.leg1toprot.xRot = -1.134464F;
            this.leg1toprot.yRot = Mth.PI / 6.0F;
            this.leg1toprot.zRot = 0.0F;
            this.leg1botrot.xRot = 1.9198622F;
            this.leg1botrot.yRot = -0.43633232F;
            this.leg3toprot.xRot = Mth.PI / 18.0F;
            this.leg3botrot.xRot = 0.61086524F;
            this.leg4toprot.xRot = Mth.PI / 18.0F;
            this.leg4botrot.xRot = 0.61086524F;
            // the original multiplied the look pitch by 0, so the head never follows the view
            this.headrot.xRot = Mth.PI / 4.0F;
        }
    }
}
