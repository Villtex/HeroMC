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


/** ModelTFBunny. */
public class BunnyModel extends EntityModel<TFRenderStates.Variant> {
    final ModelPart leg1;
    final ModelPart leg2;
    final ModelPart leg3;
    final ModelPart leg4;
    final ModelPart head;

    public BunnyModel(ModelPart root) {
        super(root);
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 20.0F, 3.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 8).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 3.0F, 5.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-2.0F, 23.0F, 2.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(1.0F, 23.0F, 2.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-2.0F, 23.0F, -2.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(1.0F, 23.0F, -2.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-2.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(16, 0).addBox(-2.5F, -8.0F, -0.5F, 2.0F, 4.0F, 1.0F)
                .texOffs(16, 0).addBox(0.5F, -8.0F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 22.0F, -1.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(TFRenderStates.Variant state) {
        super.setupAnim(state);
        float f = state.walkAnimationPos;
        float f1 = state.walkAnimationSpeed;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.leg1.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        this.leg2.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
        this.leg3.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
        this.leg4.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
    }
}
