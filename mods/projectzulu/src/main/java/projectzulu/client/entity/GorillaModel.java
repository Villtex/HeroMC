package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import projectzulu.client.entity.BigModelParts.Transform;

import java.util.function.Function;

/**
 * ModelGorilla: hind legs and a hunched body carrying the head and the long front legs. The baby is the whole model at
 * half size (the head transform of the original was pushed and popped without drawing anything).
 */
public class GorillaModel extends EntityModel<BigAnimalRenderer.State> {
    private final ModelPart headrot;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public GorillaModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> parts = root.createPartLookup();
        this.headrot = parts.apply("HEADROT");
        this.leg1 = parts.apply("LEG1TOPROT");
        this.leg2 = parts.apply("LEG2TOPROT");
        this.leg3 = parts.apply("LEG3ROT");
        this.leg4 = parts.apply("LEG4ROT");
    }

    public static LayerDefinition createBodyLayer() {
        return create(Transform.scale(1.0F));
    }

    public static LayerDefinition createBabyLayer() {
        return create(Transform.HALF_BODY);
    }

    private static LayerDefinition create(Transform transform) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition main = BigModelParts.group(root, "main_group", transform);
        main.addOrReplaceChild("LEG4ROT", CubeListBuilder.create().mirror()
                .texOffs(38, 52).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(3.0F, 16.0F, 8.0F));
        main.addOrReplaceChild("LEG3ROT", CubeListBuilder.create().mirror()
                .texOffs(38, 39).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F),
                PartPose.offset(-3.0F, 16.0F, 8.0F));
        PartDefinition BODY2ROT = main.addOrReplaceChild("BODY2ROT", CubeListBuilder.create().mirror()
                .texOffs(0, 22).addBox(-5.0F, -7.0F, -7.0F, 10.0F, 8.0F, 10.0F),
                PartPose.offset(0.0F, 15.0F, 8.0F));
        BODY2ROT.addOrReplaceChild("BODY1ROT", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-6.0F, -15.0F, -3.0F, 12.0F, 12.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 1.178097F, 0.0F, 0.0F));
        PartDefinition BODY1OTHROT = BODY2ROT.addOrReplaceChild("BODY1OTHROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, -3.0F));
        BODY1OTHROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(38, 17).addBox(-3.5F, -7.0F, -3.0F, 7.0F, 8.0F, 6.0F)
                .texOffs(0, 0).addBox(-4.5F, -6.0F, -1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(0, 4).addBox(3.5F, -6.0F, -1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(47, 32).addBox(-2.0F, -3.0F, -4.0F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, -8.0F, -12.0F));
        PartDefinition LEG1TOPROT = BODY1OTHROT.addOrReplaceChild("LEG1TOPROT", CubeListBuilder.create().mirror()
                .texOffs(0, 41).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 8.0F, 4.0F),
                PartPose.offsetAndRotation(-5.0F, -9.0F, -10.0F, 0.0F, 0.0F, 0.0174533F));
        LEG1TOPROT.addOrReplaceChild("LEG1BOTROT", CubeListBuilder.create().mirror()
                .texOffs(0, 49).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 10.0F, 4.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition LEG2TOPROT = BODY1OTHROT.addOrReplaceChild("LEG2TOPROT", CubeListBuilder.create().mirror()
                .texOffs(19, 41).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 8.0F, 4.0F),
                PartPose.offset(5.0F, -9.0F, -10.0F));
        LEG2TOPROT.addOrReplaceChild("LEG2BOTROT", CubeListBuilder.create().mirror()
                .texOffs(19, 49).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 10.0F, 4.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(BigAnimalRenderer.State state) {
        super.setupAnim(state);
        float swing = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.8F * BigModelParts.logSwing(state.walkAnimationSpeed);
        this.leg1.xRot = swing;
        this.leg3.xRot = swing;
        this.leg2.xRot = -swing;
        this.leg4.xRot = -swing;
        this.headrot.xRot = BigModelParts.clampDeg(state.xRot, -90.0F, 90.0F);
        this.headrot.yRot = BigModelParts.clampDeg(state.yRot, -45.0F, 45.0F);
    }
}
