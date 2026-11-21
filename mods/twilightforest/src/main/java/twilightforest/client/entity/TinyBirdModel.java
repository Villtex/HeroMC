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


/** ModelTFTinyBird: legs tucked in while flying. */
public class TinyBirdModel extends EntityModel<TFRenderStates.Bird> {
    final ModelPart head;
    final ModelPart rightleg;
    final ModelPart leftleg;
    final ModelPart rightarm;
    final ModelPart leftarm;

    public TinyBirdModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightleg = root.getChild("rightleg");
        this.leftleg = root.getChild("leftleg");
        this.rightarm = root.getChild("rightarm");
        this.leftarm = root.getChild("leftarm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 20.5F, -0.5F));
        head.addOrReplaceChild("beak", CubeListBuilder.create()
                .texOffs(12, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.5F, -2.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 6).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("rightleg", CubeListBuilder.create()
                .texOffs(0, 12).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-1.5F, 23.0F, 0.0F));
        root.addOrReplaceChild("leftleg", CubeListBuilder.create()
                .texOffs(0, 12).addBox(0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("rightarm", CubeListBuilder.create()
                .texOffs(12, 2).addBox(-1.0F, 0.0F, -1.5F, 1.0F, 2.0F, 3.0F),
                PartPose.offset(-1.5F, 20.5F, 1.0F));
        root.addOrReplaceChild("leftarm", CubeListBuilder.create()
                .texOffs(12, 2).addBox(0.0F, 0.0F, -1.5F, 1.0F, 2.0F, 3.0F),
                PartPose.offset(1.5F, 20.5F, 1.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 22.0F, 2.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(TFRenderStates.Bird state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.rightleg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        this.leftleg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.4F * state.walkAnimationSpeed;
        this.rightarm.zRot = state.wing;
        this.leftarm.zRot = -state.wing;
        this.rightleg.y = state.landed ? 23.0F : 22.5F;
        this.leftleg.y = state.landed ? 23.0F : 22.5F;
    }
}
