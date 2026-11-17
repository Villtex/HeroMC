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
import projectzulu.entity.ZuluBigAnimal;

import java.util.function.Function;

/**
 * ModelMammoth (heightToRais 18, drawn at four times the scale): three body blocks, four legs, the head with tusks,
 * ears and a nine-part trunk, and the saddle (always drawn; only the saddle textures paint it). While attacking with
 * animTime running, the head nods. Baby: head at 2.6 (1.3/2 x 4) moved by (0, 11, 9) x 0.65, body at 2 (4/2) moved
 * down 12.
 */
public class MammothModel extends EntityModel<BigAnimalRenderer.State> {
    private static final float HEIGHT_TO_RAIS = 18.0F;

    private final ModelPart headrot;
    private final ModelPart tuskRight;
    private final ModelPart tuskLeft;
    private final ModelPart[] nose = new ModelPart[9];
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public MammothModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> parts = root.createPartLookup();
        this.headrot = parts.apply("HEADROT");
        this.tuskRight = parts.apply("TUSKRIGROT");
        this.tuskLeft = parts.apply("TUSKLEFROT");
        for (int i = 0; i < 9; i++) {
            this.nose[i] = parts.apply("NOSEROT" + (i + 1));
        }
        this.leg1 = parts.apply("leg1");
        this.leg2 = parts.apply("leg2");
        this.leg3 = parts.apply("leg3");
        this.leg4 = parts.apply("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        return create(Transform.scale(4.0F), Transform.scale(4.0F));
    }

    public static LayerDefinition createBabyLayer() {
        return create(new Transform(2.6F, 0.0F, 11.0F * 0.65F, 9.0F * 0.65F), new Transform(2.0F, 0.0F, 12.0F, 0.0F));
    }

    private static LayerDefinition create(Transform headTransform, Transform bodyTransform) {
        float heightToRais = HEIGHT_TO_RAIS;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = BigModelParts.group(root, "head_group", headTransform);
        PartDefinition body = BigModelParts.group(root, "body_group", bodyTransform);
        PartDefinition HEADROT = head.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(58, 48).addBox(-2.5F, -6.0F, -3.5F, 5.0F, 2.0F, 3.0F)
                .texOffs(34, 48).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 4.0F)
                .texOffs(64, 53).addBox(-3.0F, -2.0F, -5.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(64, 53).addBox(1.0F, -2.0F, -5.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 13.0F - heightToRais, -8.0F));
        HEADROT.addOrReplaceChild("TUSKLEFROT", CubeListBuilder.create().mirror()
                .texOffs(44, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                .texOffs(52, 14).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(44, 21).addBox(-1.0F, 2.0F, -6.0F, 2.0F, 2.0F, 5.0F),
                PartPose.offset(-3.0F, 4.0F, -2.0F));
        HEADROT.addOrReplaceChild("TUSKRIGROT", CubeListBuilder.create().mirror()
                .texOffs(44, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                .texOffs(44, 21).addBox(-1.0F, 2.0F, -6.0F, 2.0F, 2.0F, 5.0F)
                .texOffs(52, 14).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(3.0F, 4.0F, -2.0F));
        PartDefinition NOSEROT1 = HEADROT.addOrReplaceChild("NOSEROT1", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 3.0F, -4.0F));
        PartDefinition NOSEROT2 = NOSEROT1.addOrReplaceChild("NOSEROT2", CubeListBuilder.create().mirror()
                .texOffs(0, 3).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition NOSEROT3 = NOSEROT2.addOrReplaceChild("NOSEROT3", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition NOSEROT4 = NOSEROT3.addOrReplaceChild("NOSEROT4", CubeListBuilder.create().mirror()
                .texOffs(0, 3).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition NOSEROT5 = NOSEROT4.addOrReplaceChild("NOSEROT5", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition NOSEROT6 = NOSEROT5.addOrReplaceChild("NOSEROT6", CubeListBuilder.create().mirror()
                .texOffs(0, 3).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition NOSEROT7 = NOSEROT6.addOrReplaceChild("NOSEROT7", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition NOSEROT8 = NOSEROT7.addOrReplaceChild("NOSEROT8", CubeListBuilder.create().mirror()
                .texOffs(0, 3).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        NOSEROT8.addOrReplaceChild("NOSEROT9", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        HEADROT.addOrReplaceChild("EARRIGROT", CubeListBuilder.create().mirror()
                .texOffs(58, 53).addBox(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offset(4.0F, -4.0F, -2.0F));
        HEADROT.addOrReplaceChild("EARLEFROT", CubeListBuilder.create().mirror()
                .texOffs(58, 53).addBox(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offset(-5.0F, -4.0F, -2.0F));
        body.addOrReplaceChild("body3", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-4.0F, 0.0F, -2.0F, 13.0F, 10.0F, 4.0F),
                PartPose.offset(-2.5F, 10.0F - heightToRais, 3.0F));
        body.addOrReplaceChild("body2", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-5.5F, 0.0F, -2.0F, 15.0F, 11.0F, 5.0F),
                PartPose.offset(-2.0F, 9.0F - heightToRais, -2.0F));
        body.addOrReplaceChild("body1", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-2.0F, 0.0F, -2.0F, 16.0F, 11.0F, 4.0F),
                PartPose.offset(-6.0F, 8.0F - heightToRais, -6.0F));
        body.addOrReplaceChild("leg1", CubeListBuilder.create()
                .texOffs(40, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(-6.0F, 19.0F - heightToRais, -6.0F));
        body.addOrReplaceChild("leg2", CubeListBuilder.create()
                .texOffs(40, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F),
                PartPose.offset(6.0F, 19.0F - heightToRais, -6.0F));
        body.addOrReplaceChild("leg3", CubeListBuilder.create()
                .texOffs(56, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(-4.5F, 20.0F - heightToRais, 3.0F, 0.0F, 0.0F, 0.0174533F));
        body.addOrReplaceChild("leg4", CubeListBuilder.create()
                .texOffs(56, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(4.5F, 20.0F - heightToRais, 3.0F, 0.0F, 0.0F, 0.0174533F));
        body.addOrReplaceChild("SADDLEROT", CubeListBuilder.create().mirror()
                .texOffs(74, 7).addBox(4.0F, -4.0F, -4.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(-5.0F, -4.0F, -4.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(-5.0F, -4.0F, -2.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(-5.0F, -4.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(-5.0F, -4.0F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(-2.0F, -4.0F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(1.0F, -4.0F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(4.0F, -4.0F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(4.0F, -4.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(4.0F, -4.0F, -2.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 17).addBox(-3.0F, -7.0F, 0.5F, 6.0F, 1.0F, 1.0F)
                .texOffs(74, 7).addBox(-0.5F, -4.0F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(-3.5F, -4.0F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 7).addBox(2.5F, -4.0F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(74, 0).addBox(-5.0F, -1.0F, -4.0F, 10.0F, 1.0F, 6.0F)
                .texOffs(74, 7).addBox(-5.0F, -5.0F, -4.5F, 1.0F, 1.0F, 5.0F)
                .texOffs(74, 15).addBox(-4.0F, -6.0F, 0.5F, 8.0F, 1.0F, 1.0F)
                .texOffs(74, 7).addBox(4.0F, -5.0F, -4.5F, 1.0F, 1.0F, 5.0F)
                .texOffs(114, 9).addBox(-8.5F, 1.0F, -3.5F, 1.0F, 11.0F, 3.0F)
                .texOffs(74, 19).addBox(-8.5F, 0.0F, -3.5F, 17.0F, 1.0F, 3.0F)
                .texOffs(114, 9).addBox(7.5F, 1.0F, -3.5F, 1.0F, 11.0F, 3.0F)
                .texOffs(74, 19).addBox(-8.5F, 12.0F, -3.5F, 17.0F, 1.0F, 3.0F)
                .texOffs(96, 7).addBox(-6.0F, -1.0F, -5.0F, 12.0F, 1.0F, 1.0F)
                .texOffs(96, 9).addBox(-4.0F, 1.0F, 1.0F, 8.0F, 1.0F, 1.0F)
                .texOffs(96, 11).addBox(-5.0F, 0.0F, 1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(96, 11).addBox(4.0F, 0.0F, 1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(74, 13).addBox(-5.0F, -5.0F, 0.5F, 10.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F - heightToRais, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(BigAnimalRenderer.State state) {
        super.setupAnim(state);
        this.tuskRight.yRot = -Mth.PI / 8.0F;
        this.tuskLeft.yRot = Mth.PI / 8.0F;
        for (int i = 0; i < 9; i++) {
            this.nose[i].xRot = i < 4 ? Mth.PI / 8.0F : 0.0F;
        }
        float swing = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.8F * BigModelParts.logSwing(state.walkAnimationSpeed);
        this.leg1.xRot = swing;
        this.leg3.xRot = swing;
        this.leg2.xRot = -swing;
        this.leg4.xRot = -swing;
        float pitch = BigModelParts.clampDeg(state.xRot, -15.0F, 15.0F);
        if (state.entityState == ZuluBigAnimal.STATE_ATTACKING && state.animTime > 0) {
            // the original fed the walk amount (f1), not the walk position, into this nod
            pitch += (float) (Mth.cos(state.walkAnimationSpeed * 0.6662F / 2.0F + Mth.PI) * Math.PI / 180.0 * 20.0);
        }
        this.headrot.xRot = pitch;
        this.headrot.yRot = BigModelParts.clampDeg(state.yRot, -45.0F, 45.0F);
    }
}
