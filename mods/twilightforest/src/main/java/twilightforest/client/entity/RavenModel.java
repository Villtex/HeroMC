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


/** ModelTFRaven: tilts its head when it looks aside. */
public class RavenModel extends EntityModel<TFRenderStates.Bird> {
    final ModelPart head;
    final ModelPart rightleg;
    final ModelPart leftleg;
    final ModelPart rightarm;
    final ModelPart leftarm;

    public RavenModel(ModelPart root) {
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
                .texOffs(0, 0).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        head.addOrReplaceChild("beak1", CubeListBuilder.create()
                .texOffs(12, 0).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, (float) (Math.PI / 12), 0.0F, 0.0F));
        head.addOrReplaceChild("beak2", CubeListBuilder.create()
                .texOffs(12, 0).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, (float) (-Math.PI / 12), 0.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 6).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 17.0F, 1.0F, (float) (-Math.PI / 6), 0.0F, 0.0F));
        root.addOrReplaceChild("rightarm", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-1.0F, 0.0F, -1.5F, 1.0F, 3.0F, 6.0F),
                PartPose.offset(-1.5F, 18.0F, 1.0F));
        root.addOrReplaceChild("leftarm", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, 0.0F, -1.5F, 1.0F, 3.0F, 6.0F),
                PartPose.offset(1.5F, 18.0F, 1.0F));
        PartDefinition rightleg = root.addOrReplaceChild("rightleg", CubeListBuilder.create()
                .texOffs(14, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(-1.5F, 21.0F, 1.0F));
        rightleg.addOrReplaceChild("rightfoot", CubeListBuilder.create()
                .texOffs(14, 20).addBox(0.0F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, 1.0F, (float) (Math.PI / 6), 0.0F, 0.0F));
        PartDefinition leftleg = root.addOrReplaceChild("leftleg", CubeListBuilder.create()
                .texOffs(14, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.5F, 21.0F, 1.0F));
        leftleg.addOrReplaceChild("leftfoot", CubeListBuilder.create()
                .texOffs(14, 20).addBox(0.0F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, 1.0F, (float) (Math.PI / 6), 0.0F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 21.0F, 4.0F, (float) (-Math.PI / 6), 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(TFRenderStates.Bird state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.zRot = state.yRot > 5.0F ? -Mth.PI / 12.0F : 0.0F;
        this.rightleg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        this.leftleg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.4F * state.walkAnimationSpeed;
        this.rightarm.zRot = state.wing;
        this.leftarm.zRot = -state.wing;
        this.rightleg.y = state.landed ? 21.0F : 20.0F;
        this.leftleg.y = state.landed ? 21.0F : 20.0F;
    }
}
