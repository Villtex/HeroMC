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


/** ModelTFDeer: a ModelQuadruped with antlers and a neck on the body, child head offset 10/4. */
public class WildDeerModel extends net.minecraft.client.model.QuadrupedModel<net.minecraft.client.renderer.entity.state.LivingEntityRenderState> {

    public WildDeerModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 5).addBox(-2.0F, -8.0F, -6.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(52, 0).addBox(-1.5F, -5.0F, -9.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-3.0F, -10.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-3.0F, -10.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-4.0F, -10.0F, -1.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-5.0F, -11.0F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-5.0F, -14.0F, 2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-6.0F, -17.0F, 3.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-6.0F, -13.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-6.0F, -14.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-7.0F, -15.0F, -6.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-6.0F, -16.0F, -9.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-7.0F, -18.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-6.0F, -19.0F, -6.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(1.0F, -10.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(3.0F, -10.0F, -1.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(4.0F, -11.0F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(4.0F, -14.0F, 2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(5.0F, -17.0F, 3.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(5.0F, -13.0F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(5.0F, -14.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(6.0F, -15.0F, -6.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(5.0F, -16.0F, -9.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(6.0F, -18.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(5.0F, -19.0F, -6.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 4.0F, -7.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(36, 6).addBox(-4.0F, -10.0F, -7.0F, 6.0F, 18.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 5.0F, 2.0F, 1.570796F, 0.0F, 0.0F));
        body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(10, 19).addBox(-2.5F, -8.0F, -11.0F, 3.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 4.974188F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-3.0F, 0.0F, -2.0F, 2.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.0F, 9.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 12.0F, 9.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-3.0F, 0.0F, -3.0F, 2.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 12.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        super.setupAnim(state);
    }
}
