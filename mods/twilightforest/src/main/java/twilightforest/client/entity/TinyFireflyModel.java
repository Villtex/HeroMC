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


/** ModelTFTinyFirefly: one 10 x 10 quad (UV 20, 0 of the 64 x 32 texture), drawn facing the camera. */
public class TinyFireflyModel extends EntityModel<net.minecraft.client.renderer.entity.state.LivingEntityRenderState> {

    public TinyFireflyModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("glow1", CubeListBuilder.create()
                .texOffs(20, 0).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        super.setupAnim(state);
    }
}
