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


/** ModelTFRedcap: a short biped with a pointed cap and goblin ears that follow the head. */
public class RedcapModel extends OldBiped {
    final ModelPart goblinRightEar;
    final ModelPart goblinLeftEar;

    public RedcapModel(ModelPart root) {
        super(root);
        this.goblinRightEar = root.getChild("goblinRightEar");
        this.goblinLeftEar = root.getChild("goblinLeftEar");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.4F, 1.0F, -4.0F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(12, 19).addBox(-4.0F, 6.0F, -2.0F, 8.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(36, 17).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(36, 17).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-2.0F, 2.0F, -1.0F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-1.0F, 3.0F, -1.0F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("goblinRightEar", CubeListBuilder.create()
                .texOffs(48, 20).addBox(3.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("goblinLeftEar", CubeListBuilder.create()
                .texOffs(48, 24).addBox(-5.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        OldBiped.fill(root);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.HumanoidRenderState state) {
        super.setupAnim(state);
        this.goblinRightEar.xRot = this.head.xRot;
        this.goblinRightEar.yRot = this.head.yRot;
        this.goblinRightEar.zRot = this.head.zRot;
        this.goblinLeftEar.xRot = this.head.xRot;
        this.goblinLeftEar.yRot = this.head.yRot;
        this.goblinLeftEar.zRot = this.head.zRot;
    }
}
