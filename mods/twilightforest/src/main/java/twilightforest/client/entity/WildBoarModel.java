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


/** ModelTFBoar: a ModelQuadruped (head and leg swing of today's QuadrupedModel are the same), child head offset 4/4. */
public class WildBoarModel extends net.minecraft.client.model.QuadrupedModel<net.minecraft.client.renderer.entity.state.LivingEntityRenderState> {

    public WildBoarModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -2.0F, -6.0F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(28, 0).addBox(-3.0F, 1.0F, -9.0F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(17, 17).addBox(3.0F, 2.0F, -9.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(17, 17).addBox(-4.0F, 2.0F, -9.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.0F, -6.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(28, 10).addBox(-5.0F, -8.0F, -7.0F, 10.0F, 14.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, 1.570796F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 18.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 18.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 18.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 18.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        super.setupAnim(state);
    }
}
