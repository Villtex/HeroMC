package twilightforest.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;


/** ModelTFPenguin: the wings flap with RenderTFBird's wing angle. */
public class PenguinModel extends EntityModel<TFRenderStates.Bird> {
    final ModelPart head;
    final ModelPart rightleg;
    final ModelPart leftleg;
    final ModelPart rightarm;
    final ModelPart leftarm;

    public PenguinModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightleg = root.getChild("rightleg");
        this.leftleg = root.getChild("leftleg");
        this.rightarm = root.getChild("rightarm");
        this.leftarm = root.getChild("leftarm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -4.0F, -3.5F, 7.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        head.addOrReplaceChild("beak", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, -1.0F, -4.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 9.0F, 8.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        root.addOrReplaceChild("rightleg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -5.0F, 4.0F, 1.0F, 8.0F),
                PartPose.offset(-2.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("leftleg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -5.0F, 4.0F, 1.0F, 8.0F),
                PartPose.offset(2.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("rightarm", CubeListBuilder.create()
                .texOffs(34, 18).addBox(-1.0F, -1.0F, -2.0F, 1.0F, 8.0F, 4.0F),
                PartPose.offset(-4.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("leftarm", CubeListBuilder.create()
                .texOffs(24, 18).addBox(0.0F, -1.0F, -2.0F, 1.0F, 8.0F, 4.0F),
                PartPose.offset(4.0F, 15.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(TFRenderStates.Bird state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.rightleg.xRot = Mth.cos(state.walkAnimationPos) * 0.7F * state.walkAnimationSpeed;
        this.leftleg.xRot = Mth.cos(state.walkAnimationPos + Mth.PI) * 0.7F * state.walkAnimationSpeed;
        this.rightarm.zRot = state.wing;
        this.leftarm.zRot = -state.wing;
    }
}
