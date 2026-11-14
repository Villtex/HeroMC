package extrabiomes.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** ModelScarecrow: a pole, a sack body, a head and a crossbar for arms. It does not move. */
public class ScarecrowModel extends EntityModel<LivingEntityRenderState> {
    public ScarecrowModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, -29.0F, -1.0F, 2, 30, 2),
                PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8),
                PartPose.offset(0.0F, 5.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 52).mirror().addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        root.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-15.0F, -1.0F, -1.0F, 31, 2, 2),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.1F));
        return LayerDefinition.create(mesh, 128, 64);
    }
}
