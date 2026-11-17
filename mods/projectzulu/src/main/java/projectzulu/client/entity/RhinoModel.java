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
 * ModelRhino (heightToRaise 9, renderScale 1.6). Baby: head at full scale moved by (0, 8, 6), body at half size.
 */
public class RhinoModel extends EntityModel<BigAnimalRenderer.State> {
    private static final float HEIGHT_TO_RAISE = 9.0F;
    private static final float RENDER_SCALE = 1.6F;

    private final ModelPart headrot;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart tail;

    public RhinoModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> parts = root.createPartLookup();
        this.headrot = parts.apply("HEADROT");
        this.leg1 = parts.apply("LEG1ROT");
        this.leg2 = parts.apply("LEG2ROT");
        this.leg3 = parts.apply("LEG3ROT");
        this.leg4 = parts.apply("LEG4ROT");
        this.tail = parts.apply("TAILROT");
    }

    public static LayerDefinition createBodyLayer() {
        return create(Transform.scale(RENDER_SCALE), Transform.scale(RENDER_SCALE));
    }

    public static LayerDefinition createBabyLayer() {
        return create(new Transform(RENDER_SCALE, 0.0F, 8.0F, 6.0F), new Transform(RENDER_SCALE / 2.0F, 0.0F, 12.0F, 0.0F));
    }

    private static LayerDefinition create(Transform headTransform, Transform bodyTransform) {
        float heightToRaise = HEIGHT_TO_RAISE;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = BigModelParts.group(root, "head_group", headTransform);
        PartDefinition body = BigModelParts.group(root, "body_group", bodyTransform);
        head.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(102, 15).addBox(3.0F, -6.0F, -1.5F, 2.0F, 3.0F, 1.0F)
                .texOffs(96, 15).addBox(-5.0F, -6.0F, -1.5F, 2.0F, 3.0F, 1.0F)
                .texOffs(96, 0).addBox(-1.5F, -1.0F, -13.0F, 3.0F, 2.0F, 3.0F)
                .texOffs(96, 5).addBox(-1.0F, -4.0F, -13.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(96, 10).addBox(-0.5F, -8.0F, -13.0F, 1.0F, 4.0F, 1.0F)
                .texOffs(108, 0).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 10.0F, 2.0F)
                .texOffs(109, 12).addBox(-3.5F, -2.0F, -5.0F, 7.0F, 8.0F, 3.0F)
                .texOffs(109, 23).addBox(-3.0F, -1.0F, -9.0F, 6.0F, 7.0F, 4.0F)
                .texOffs(111, 34).addBox(-2.5F, 1.0F, -13.0F, 5.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 9.0F - heightToRaise, -8.0F));
        body.addOrReplaceChild("LEG3ROT", CubeListBuilder.create().mirror()
                .texOffs(48, 13).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F)
                .texOffs(0, 0).addBox(-2.5F, 8.0F, -3.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-0.5F, 8.0F, -3.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.5F, 8.0F, -3.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-4.5F, 15.0F - heightToRaise, 9.5F));
        body.addOrReplaceChild("LEG4ROT", CubeListBuilder.create().mirror()
                .texOffs(68, 13).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F)
                .texOffs(0, 0).addBox(-2.5F, 8.0F, -3.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-0.5F, 8.0F, -3.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.5F, 8.0F, -3.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(4.5F, 15.0F - heightToRaise, 9.5F));
        body.addOrReplaceChild("LEG2ROT", CubeListBuilder.create().mirror()
                .texOffs(68, 0).addBox(-2.5F, 1.0F, -2.5F, 5.0F, 8.0F, 5.0F)
                .texOffs(0, 0).addBox(-2.5F, 8.0F, -3.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-0.5F, 8.0F, -3.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.5F, 8.0F, -3.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(4.0F, 15.0F - heightToRaise, -5.0F));
        body.addOrReplaceChild("LEG1ROT", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-2.5F, 8.0F, -3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-0.5F, 8.0F, -3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.5F, 8.0F, -3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(48, 0).addBox(-2.5F, 1.0F, -2.0F, 5.0F, 8.0F, 5.0F),
                PartPose.offset(-4.0F, 15.0F - heightToRaise, -5.0F));
        body.addOrReplaceChild("TAILROT", CubeListBuilder.create().mirror()
                .texOffs(0, 25).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offset(0.0F, 9.0F - heightToRaise, 12.0F));
        body.addOrReplaceChild("BODYROT", CubeListBuilder.create().mirror()
                .texOffs(20, 25).addBox(-7.0F, 5.0F, 0.0F, 6.0F, 1.0F, 6.0F)
                .texOffs(20, 25).addBox(1.0F, 5.0F, 0.0F, 6.0F, 1.0F, 6.0F)
                .texOffs(0, 32).addBox(-7.0F, -7.0F, 0.0F, 14.0F, 12.0F, 20.0F),
                PartPose.offset(0.0F, 10.0F - heightToRaise, -8.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(BigAnimalRenderer.State state) {
        super.setupAnim(state);
        float swing = state.walkAnimationPos * 0.6662F * 2.0F;
        float amount = state.walkAnimationSpeed;
        this.leg1.xRot = Mth.cos(swing) * 1.2F * amount;
        this.leg3.xRot = Mth.cos(swing) * 1.2F * amount;
        this.leg2.xRot = Mth.cos(swing + Mth.PI) * 1.2F * amount;
        this.leg4.xRot = Mth.cos(swing + Mth.PI) * 1.2F * amount;
        this.tail.xRot = -Mth.HALF_PI;
        this.headrot.xRot = BigModelParts.clampDeg(state.xRot, -14.0F, 15.0F);
        this.headrot.yRot = BigModelParts.clampDeg(state.yRot, -15.0F, 15.0F);
    }
}
