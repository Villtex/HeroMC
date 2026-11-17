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

/** ModelBoar: a head with snout and four tusks, a long body carrying the four legs. */
public class BoarModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart headrot;
    private final ModelPart legrot1;
    private final ModelPart legrot2;
    private final ModelPart legrot3;
    private final ModelPart legrot4;

    public BoarModel(ModelPart root) {
        super(root);
        this.headrot = root.getChild("headrot");
        ModelPart bodyrot = root.getChild("bodyrot");
        this.legrot1 = bodyrot.getChild("legrot1");
        this.legrot2 = bodyrot.getChild("legrot2");
        this.legrot3 = bodyrot.getChild("legrot3");
        this.legrot4 = bodyrot.getChild("legrot4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("headrot", CubeListBuilder.create().mirror()
                        .texOffs(0, 0).addBox(-4.0F, -3.5F, -5.0F, 8, 7, 6)
                        .texOffs(28, 0).addBox(-2.5F, -0.5F, -7.0F, 5, 4, 2)
                        .texOffs(42, 0).addBox(-2.0F, 0.5F, -9.0F, 4, 3, 2)
                        .texOffs(0, 29).addBox(-4.0F, 2.5F, -9.0F, 2, 1, 1)
                        .texOffs(0, 27).addBox(-4.0F, 1.5F, -9.0F, 1, 1, 1)
                        .texOffs(0, 29).addBox(2.0F, 2.5F, -9.0F, 2, 1, 1)
                        .texOffs(0, 27).addBox(3.0F, 1.5F, -9.0F, 1, 1, 1)
                        .texOffs(0, 29).addBox(-4.5F, 2.5F, -7.0F, 2, 1, 1)
                        .texOffs(0, 27).addBox(-4.5F, 1.5F, -7.0F, 1, 1, 1)
                        .texOffs(0, 29).addBox(2.5F, 2.5F, -7.0F, 2, 1, 1)
                        .texOffs(0, 27).addBox(3.5F, 1.5F, -7.0F, 1, 1, 1),
                PartPose.offset(0.0F, 14.0F, -5.0F));
        PartDefinition bodyrot = root.addOrReplaceChild("bodyrot", CubeListBuilder.create().mirror()
                .texOffs(12, 8).addBox(-5.0F, -4.5F, 0.0F, 10, 8, 16), PartPose.offset(0.0F, 14.5F, -4.0F));
        CubeListBuilder leg = CubeListBuilder.create().mirror().texOffs(0, 13).addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4);
        bodyrot.addOrReplaceChild("legrot1", leg, PartPose.offset(-3.0F, 3.5F, 14.0F));
        bodyrot.addOrReplaceChild("legrot2", leg, PartPose.offset(3.0F, 3.5F, 14.0F));
        bodyrot.addOrReplaceChild("legrot3", leg, PartPose.offset(-3.0F, 3.5F, 2.0F));
        bodyrot.addOrReplaceChild("legrot4", leg, PartPose.offset(3.0F, 3.5F, 2.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float swing = state.walkAnimationPos * 0.6662F * 2.0F;
        float amount = state.walkAnimationSpeed;
        this.legrot1.xRot = Mth.cos(swing) * 1.8F * amount;
        this.legrot3.xRot = Mth.cos(swing) * 1.8F * amount;
        this.legrot2.xRot = Mth.cos(swing + Mth.PI) * 1.8F * amount;
        this.legrot4.xRot = Mth.cos(swing + Mth.PI) * 1.8F * amount;
        this.headrot.xRot = Mth.clamp(state.xRot, -15.0F, 15.0F) * Mth.DEG_TO_RAD;
        this.headrot.yRot = Mth.clamp(state.yRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD;
    }
}
