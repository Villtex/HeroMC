package natura.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.util.Mth;

/** ModelBoat of 1.5.2 (the boat textures of Natura are 64 x 32 in its layout): a bottom and four sides, no paddles. */
public class NaturaBoatModel extends EntityModel<BoatRenderState> {
    public NaturaBoatModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 8).addBox(-12.0F, -8.0F, -3.0F, 24, 16, 4),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, Mth.HALF_PI, 0.0F, 0.0F));
        CubeListBuilder side = CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -7.0F, -1.0F, 20, 6, 2);
        root.addOrReplaceChild("back", side, PartPose.offsetAndRotation(-11.0F, 4.0F, 0.0F, 0.0F, Mth.PI * 3.0F / 2.0F, 0.0F));
        root.addOrReplaceChild("front", side, PartPose.offsetAndRotation(11.0F, 4.0F, 0.0F, 0.0F, Mth.HALF_PI, 0.0F));
        root.addOrReplaceChild("right", side, PartPose.offsetAndRotation(0.0F, 4.0F, -9.0F, 0.0F, Mth.PI, 0.0F));
        root.addOrReplaceChild("left", side, PartPose.offset(0.0F, 4.0F, 9.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
}
