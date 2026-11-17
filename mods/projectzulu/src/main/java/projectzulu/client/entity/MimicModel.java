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
 * ModelMimic: a chest (lid with teeth, bottom with teeth) on four two-piece legs, 64 x 64. Idle it sits on the
 * ground as a closed chest with the legs folded flat (everything 9 pixels lower); awake it stands on splayed legs
 * and the lid chomps with every step. In the original the chest boxes were drawn with the vanilla chest texture;
 * mimicchest.png holds the same chest layout plus the legs and teeth, so the whole model uses it here.
 */
public class MimicModel extends EntityModel<MimicModel.State> {
    private final ModelPart botchest;
    private final ModelPart botteethrot;
    private final ModelPart topchestrot;
    private final ModelPart leg1top;
    private final ModelPart leg2top;
    private final ModelPart leg3top;
    private final ModelPart leg4top;
    private final ModelPart leg1bot;
    private final ModelPart leg2bot;
    private final ModelPart leg3bot;
    private final ModelPart leg4bot;

    public static class State extends LivingEntityRenderState {
        public boolean idle = true;
    }

    public MimicModel(ModelPart root) {
        super(root);
        this.botchest = root.getChild("botchest");
        this.botteethrot = root.getChild("botteethrot");
        this.topchestrot = root.getChild("topchestrot");
        this.leg1top = root.getChild("leg1toprot");
        this.leg2top = root.getChild("leg2toprot");
        this.leg3top = root.getChild("leg3toprot");
        this.leg4top = root.getChild("leg4toprot");
        this.leg1bot = this.leg1top.getChild("leg1botrot");
        this.leg2bot = this.leg2top.getChild("leg2botrot");
        this.leg3bot = this.leg3top.getChild("leg3botrot");
        this.leg4bot = this.leg4top.getChild("leg4botrot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition leg1toprot = root.addOrReplaceChild("leg1toprot", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2).mirror(false),
                PartPose.offset(4.0F, 14.5F, -5.0F));
        leg1toprot.addOrReplaceChild("leg1botrot", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2).mirror(false),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition leg2toprot = root.addOrReplaceChild("leg2toprot", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2).mirror(false),
                PartPose.offset(-5.0F, 14.5F, -5.0F));
        leg2toprot.addOrReplaceChild("leg2botrot", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2).mirror(false),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0174533F, 0.0F));
        PartDefinition leg4toprot = root.addOrReplaceChild("leg4toprot", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2).mirror(false),
                PartPose.offset(-5.0F, 14.5F, 4.0F));
        leg4toprot.addOrReplaceChild("leg4botrot", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2).mirror(false),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition leg3toprot = root.addOrReplaceChild("leg3toprot", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 3, 2).mirror(false),
                PartPose.offset(4.0F, 14.5F, 4.0F));
        leg3toprot.addOrReplaceChild("leg3botrot", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2).mirror(false),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("botchest", CubeListBuilder.create().texOffs(0, 19).addBox(-7.5F, -9.5F, -7.5F, 14, 10, 14),
                PartPose.offset(0.0F, 14.53333F, 0.0F));
        root.addOrReplaceChild("botteethrot", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, 3.0F, -13.0F, 2, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(-4.5F, 3.0F, -13.0F, 1, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(3.0F, 3.0F, -13.0F, 1, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(4.5F, 3.0F, -11.0F, 1, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(-6.5F, 4.0F, -7.0F, 1, 1, 1).mirror(false).texOffs(0, 0).mirror().addBox(4.5F, 4.0F, -7.0F, 1, 1, 1).mirror(false).texOffs(0, 0).mirror().addBox(-6.5F, 3.0F, -11.0F, 1, 2, 1).mirror(false),
                PartPose.offset(0.0F, 0.5F, 6.5F));
        root.addOrReplaceChild("topchestrot", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.5F, -5.0F, -14.0F, 14, 5, 14).mirror(false).texOffs(0, 0).mirror().addBox(-1.0F, -1.5F, -15.5F, 2, 3, 1).mirror(false).texOffs(0, 0).mirror().addBox(4.5F, 0.0F, -13.0F, 1, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(-6.5F, 0.0F, -13.0F, 1, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(1.5F, 0.0F, -13.0F, 1, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(-2.5F, 0.0F, -13.0F, 1, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(4.5F, 0.0F, -9.0F, 1, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(4.5F, 0.0F, -5.0F, 1, 1, 1).mirror(false).texOffs(0, 0).mirror().addBox(-6.5F, 0.0F, -9.0F, 1, 2, 1).mirror(false).texOffs(0, 0).mirror().addBox(-6.5F, 0.0F, -5.0F, 1, 1, 1).mirror(false),
                PartPose.offset(0.0F, 5.0F, 6.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        if (!state.idle) {
            float pos = state.walkAnimationPos * 0.6662F;
            float speed = state.walkAnimationSpeed;
            // ModelHelper.abs: negative values become 0
            float step = 1.8F * (float) Math.max(Math.log(speed + 1.0F), 0.0);
            this.botchest.y = 14.5333F;
            this.botteethrot.y = 0.5F;
            this.topchestrot.y = 5.0F;
            this.leg1top.y = 14.5F;
            this.leg2top.y = 14.5F;
            this.leg3top.y = 14.5F;
            this.leg4top.y = 14.5F;
            this.leg1top.xRot = -Mth.PI / 18.0F + Mth.cos(pos) * step;
            this.leg1top.zRot = -0.87266463F;
            this.leg1bot.zRot = 0.87266463F;
            this.leg3top.xRot = Mth.PI / 18.0F + Mth.cos(pos) * step;
            this.leg3top.zRot = -0.87266463F;
            this.leg3bot.zRot = 0.87266463F;
            this.leg2top.xRot = -Mth.PI / 18.0F + Mth.cos(pos + Mth.PI) * step;
            this.leg2top.zRot = 0.87266463F;
            this.leg2bot.zRot = -0.87266463F;
            this.leg4top.xRot = Mth.PI / 18.0F + Mth.cos(pos + Mth.PI) * step;
            this.leg4top.zRot = 0.87266463F;
            this.leg4bot.zRot = -0.87266463F;
            this.topchestrot.xRot = Math.abs(Mth.cos(pos)) * -60.0F * Mth.DEG_TO_RAD * speed;
        } else {
            float heightToRaise = -9.0F;
            this.botchest.y = 14.5333F - heightToRaise;
            this.botteethrot.y = 0.5F - heightToRaise;
            this.topchestrot.y = 5.0F - (heightToRaise - 1.0F);
            this.leg1top.y = 14.5F - heightToRaise;
            this.leg2top.y = 14.5F - heightToRaise;
            this.leg3top.y = 14.5F - heightToRaise;
            this.leg4top.y = 14.5F - heightToRaise;
            this.leg1top.xRot = Mth.PI / 6.0F;
            this.leg1top.zRot = Mth.HALF_PI;
            this.leg3top.xRot = -Mth.PI / 6.0F;
            this.leg3top.zRot = Mth.HALF_PI;
            this.leg2top.xRot = Mth.PI / 6.0F;
            this.leg2top.zRot = -Mth.HALF_PI;
            this.leg4top.xRot = -Mth.PI / 6.0F;
            this.leg4top.zRot = -Mth.HALF_PI;
            this.topchestrot.xRot = 0.0F;
        }
    }
}
