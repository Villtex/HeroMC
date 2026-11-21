package twilightforest.client.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** ModelTFBlockGoblin: a small biped under a pointed helmet, both arms raised over its head (ModelBiped angles + PI). */
public class BlockGoblinModel extends HumanoidModel<TFRenderStates.BlockGoblin> {
    public BlockGoblinModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 11.0F, 0.0F));
        PartDefinition hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 11.0F, 0.0F));
        hat.addOrReplaceChild("helmet", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-2.5F, -9.0F, -2.5F, 5.0F, 9.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 7.0F, 4.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-3.0F, -1.0F, -2.0F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(-3.5F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(52, 0).addBox(0.0F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offset(3.5F, 12.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(-2.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(2.0F, 18.0F, 0.0F));
        OldBiped.fill(root);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(TFRenderStates.BlockGoblin state) {
        this.resetPose();
        OldBiped.angles(this, state);
        this.head.y = 11.0F;
        this.body.y = 11.0F;
        this.rightLeg.y = 18.0F;
        this.leftLeg.y = 18.0F;
        this.rightArm.setPos(-3.5F, 12.0F, 0.0F);
        this.rightArm.xRot += Mth.PI;
        this.leftArm.setPos(3.5F, 12.0F, 0.0F);
        this.leftArm.xRot += Mth.PI;
    }
}
