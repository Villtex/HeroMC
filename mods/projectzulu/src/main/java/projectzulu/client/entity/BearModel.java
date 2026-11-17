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
 * ModelBear with its three kinds: ModelBlackBear (heightToRaise 0, render scale 1), ModelBrownBear (7, 1.4) and
 * ModelPolarBear (12, 2.0). Babies: the head at its own scale and offset per kind, the body at 1/2, 1/2.5 or 1/3
 * (so brown and polar cubs float a little, as in the original).
 */
public class BearModel extends EntityModel<BigAnimalRenderer.State> {
    private final ModelPart headrot;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public BearModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> parts = root.createPartLookup();
        this.headrot = parts.apply("HEADROT");
        this.leg1 = parts.apply("LEG1ROT2");
        this.leg2 = parts.apply("LEG2ROT2");
        this.leg3 = parts.apply("LEG3ROT2");
        this.leg4 = parts.apply("LEG4ROT2");
    }

    public static LayerDefinition createBodyLayer(float heightToRaise, float scale) {
        return create(heightToRaise, Transform.scale(scale), Transform.scale(scale));
    }

    /**
     * The isChild branch of ModelBear.render, called with f5 * scale: head translated by (j, g, h) * scale and drawn
     * at scale * headScale; body scaled by 1 / bodyDivisor and moved down by 24 * scale pixels in that scaled space.
     */
    public static LayerDefinition createBabyLayer(float heightToRaise, float scale, float bodyDivisor, float headScale, float j, float g,
            float h) {
        return create(heightToRaise, new Transform(scale * headScale, j * scale, g * scale, h * scale),
                new Transform(scale / bodyDivisor, 0.0F, 24.0F * scale / bodyDivisor, 0.0F));
    }

    private static LayerDefinition create(float heightToRaise, Transform headTransform, Transform bodyTransform) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = BigModelParts.group(root, "head_group", headTransform);
        PartDefinition body = BigModelParts.group(root, "body_group", bodyTransform);
        head.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(58, 0).addBox(-3.5F, -3.0F, -5.0F, 7.0F, 6.0F, 5.0F)
                .texOffs(58, 16).addBox(-2.0F, 3.0F, -3.0F, 4.0F, 1.0F, 3.0F)
                .texOffs(69, 12).addBox(-4.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(75, 12).addBox(2.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(58, 12).addBox(-1.5F, 0.0F, -7.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(1.0F, 9.0F - heightToRaise, -6.0F));
        PartDefinition BODYROT = body.addOrReplaceChild("BODYROT", CubeListBuilder.create().mirror()
                .texOffs(2, 0).addBox(-5.0F, -5.0F, 1.0F, 12.0F, 12.0F, 16.0F),
                PartPose.offset(0.0F, 9.0F - heightToRaise, -7.0F));
        PartDefinition LEG1ROT2 = BODYROT.addOrReplaceChild("LEG1ROT2", CubeListBuilder.create().mirror()
                .texOffs(0, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(-3.0F, 7.0F, 15.0F));
        LEG1ROT2.addOrReplaceChild("LEG1ROT", CubeListBuilder.create().mirror()
                .texOffs(16, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition LEG2ROT2 = BODYROT.addOrReplaceChild("LEG2ROT2", CubeListBuilder.create().mirror()
                .texOffs(0, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(5.0F, 7.0F, 15.0F));
        LEG2ROT2.addOrReplaceChild("LEG2ROT", CubeListBuilder.create().mirror()
                .texOffs(16, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition LEG3ROT2 = BODYROT.addOrReplaceChild("LEG3ROT2", CubeListBuilder.create().mirror()
                .texOffs(0, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(-3.0F, 7.0F, 3.0F));
        LEG3ROT2.addOrReplaceChild("LEG3ROT", CubeListBuilder.create().mirror()
                .texOffs(16, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition LEG4ROT2 = BODYROT.addOrReplaceChild("LEG4ROT2", CubeListBuilder.create().mirror()
                .texOffs(0, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(5.0F, 7.0F, 3.0F));
        LEG4ROT2.addOrReplaceChild("LEG4ROT", CubeListBuilder.create().mirror()
                .texOffs(16, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        BODYROT.addOrReplaceChild("TAILROT", CubeListBuilder.create().mirror()
                .texOffs(32, 29).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 1.0F),
                PartPose.offset(1.0F, 2.0F, 17.0F));
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
        this.headrot.xRot = BigModelParts.clampDeg(state.xRot, -15.0F, 15.0F);
        this.headrot.yRot = BigModelParts.clampDeg(state.yRot, -45.0F, 45.0F);
    }
}
