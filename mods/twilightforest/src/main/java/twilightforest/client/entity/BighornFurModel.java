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


/** ModelTFBighornFur: ModelSheep2, the fleece. */
public class BighornFurModel extends net.minecraft.client.model.animal.sheep.SheepFurModel {

    public BighornFurModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.6F)),
                PartPose.offset(0.0F, 6.0F, -8.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(28, 8).addBox(-4.0F, -9.0F, -7.0F, 8.0F, 15.0F, 6.0F, new CubeDeformation(0.5F)),
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, Mth.HALF_PI, 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.4F)),
                PartPose.offset(-3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.4F)),
                PartPose.offset(3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.4F)),
                PartPose.offset(-3.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.4F)),
                PartPose.offset(3.0F, 12.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.SheepRenderState state) {
        super.setupAnim(state);
    }
}
