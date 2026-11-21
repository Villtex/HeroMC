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


/** ModelTFMinotaur: a biped with bull horns and snout. */
public class MinotaurModel extends OldBiped {

    public MinotaurModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition horn1a = head.addOrReplaceChild("horn1a", CubeListBuilder.create()
                .texOffs(24, 0).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 12), 0.0F));
        horn1a.addOrReplaceChild("horn1b", CubeListBuilder.create()
                .texOffs(24, 2).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(2.75F, 0.0F, 0.0F, (float) (Math.PI / 6), (float) (-Math.PI / 6), 0.0F));
        PartDefinition horn2a = head.addOrReplaceChild("horn2a", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 12), 0.0F));
        horn2a.addOrReplaceChild("horn2b", CubeListBuilder.create()
                .texOffs(24, 2).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(-2.75F, 0.0F, 0.0F, (float) (Math.PI / 6), (float) (Math.PI / 6), 0.0F));
        head.addOrReplaceChild("snout", CubeListBuilder.create()
                .texOffs(9, 12).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, -4.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
                .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        OldBiped.fill(root);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.HumanoidRenderState state) {
        super.setupAnim(state);
    }
}
