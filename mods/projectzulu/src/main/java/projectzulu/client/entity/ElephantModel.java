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
 * ModelElephant (heightToRaise 12, drawn at twice the scale): body, tail, four two-part legs and the head with ears,
 * tusks and a seven-part trunk that curls with the walk. Baby: head at 0.85 (1/2 x 1.7) moved by 0.5, body at 0.8
 * (0.8/2 x 2) moved by 14 x 0.4.
 */
public class ElephantModel extends EntityModel<BigAnimalRenderer.State> {
    private static final float HEIGHT_TO_RAISE = 12.0F;

    private final ModelPart headrot;
    private final ModelPart leg1Top;
    private final ModelPart leg2Top;
    private final ModelPart leg3Top;
    private final ModelPart leg4Top;
    private final ModelPart leg1Bot;
    private final ModelPart leg2Bot;
    private final ModelPart leg3Bot;
    private final ModelPart leg4Bot;
    private final ModelPart tail;
    private final ModelPart earLeft;
    private final ModelPart earRight;
    private final ModelPart[] trunk = new ModelPart[7];

    public ElephantModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> parts = root.createPartLookup();
        this.headrot = parts.apply("HEADROT");
        this.leg1Top = parts.apply("LEG1TOPROT");
        this.leg2Top = parts.apply("LEG2TOPROT");
        this.leg3Top = parts.apply("LEG3TOPROT");
        this.leg4Top = parts.apply("LEG4TOPROT");
        this.leg1Bot = parts.apply("LEG1BOTROT");
        this.leg2Bot = parts.apply("LEG2BOTROT");
        this.leg3Bot = parts.apply("LEG3BOTROT");
        this.leg4Bot = parts.apply("LEG4BOTROT");
        this.tail = parts.apply("tail");
        this.earLeft = parts.apply("EARLEFROT");
        this.earRight = parts.apply("EARRIGROT");
        for (int i = 0; i < 7; i++) {
            this.trunk[i] = parts.apply("TRUNK" + (i + 1) + "ROT");
        }
    }

    public static LayerDefinition createBodyLayer() {
        return create(Transform.scale(2.0F), Transform.scale(2.0F));
    }

    public static LayerDefinition createBabyLayer() {
        return create(new Transform(0.85F, 0.0F, 0.5F, 0.0F), new Transform(0.8F, 0.0F, 14.0F * 0.4F, 0.0F));
    }

    private static LayerDefinition create(Transform headTransform, Transform bodyTransform) {
        float heightToRaise = HEIGHT_TO_RAISE;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = BigModelParts.group(root, "head_group", headTransform);
        PartDefinition body = BigModelParts.group(root, "body_group", bodyTransform);
        PartDefinition HEADROT = head.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(92, 0).addBox(-6.0F, -7.0F, -6.0F, 12.0F, 14.0F, 6.0F)
                .texOffs(77, 20).addBox(-6.0F, -3.0F, -8.0F, 12.0F, 9.0F, 2.0F)
                .texOffs(77, 33).addBox(-5.0F, -2.0F, -9.0F, 10.0F, 7.0F, 1.0F)
                .texOffs(102, 33).addBox(-4.0F, 0.0F, -10.0F, 8.0F, 7.0F, 1.0F)
                .texOffs(113, 43).addBox(-3.0F, 2.0F, -11.0F, 6.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, 6.0F - heightToRaise, -12.0F));
        HEADROT.addOrReplaceChild("EARLEFROT", CubeListBuilder.create().mirror()
                .texOffs(55, 41).addBox(-11.0F, -5.5F, 0.0F, 11.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-7.0F, -0.5F, -4.0F, 0.0F, 1.22173F, 0.0F));
        HEADROT.addOrReplaceChild("EARRIGROT", CubeListBuilder.create().mirror()
                .texOffs(55, 41).addBox(0.0F, -5.5F, 0.0F, 11.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(7.0F, -0.5F, -4.0F, 0.0F, -1.22173F, 0.0F));
        PartDefinition TRUNK1ROT = HEADROT.addOrReplaceChild("TRUNK1ROT", CubeListBuilder.create().mirror()
                .texOffs(97, 53).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 4.0F, -10.5F));
        PartDefinition TRUNK2ROT = TRUNK1ROT.addOrReplaceChild("TRUNK2ROT", CubeListBuilder.create().mirror()
                .texOffs(97, 51).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition TRUNK3ROT = TRUNK2ROT.addOrReplaceChild("TRUNK3ROT", CubeListBuilder.create().mirror()
                .texOffs(97, 49).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition TRUNK4ROT = TRUNK3ROT.addOrReplaceChild("TRUNK4ROT", CubeListBuilder.create().mirror()
                .texOffs(97, 47).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition TRUNK5ROT = TRUNK4ROT.addOrReplaceChild("TRUNK5ROT", CubeListBuilder.create().mirror()
                .texOffs(97, 45).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition TRUNK6ROT = TRUNK5ROT.addOrReplaceChild("TRUNK6ROT", CubeListBuilder.create().mirror()
                .texOffs(97, 43).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        TRUNK6ROT.addOrReplaceChild("TRUNK7ROT", CubeListBuilder.create().mirror()
                .texOffs(97, 41).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        HEADROT.addOrReplaceChild("TUSKRIGROT", CubeListBuilder.create().mirror()
                .texOffs(60, 55).addBox(0.0F, 0.0F, -8.0F, 1.0F, 1.0F, 8.0F),
                PartPose.offsetAndRotation(4.5F, 2.0F, -8.0F, 0.8028515F, 0.0F, 0.0F));
        HEADROT.addOrReplaceChild("TUSKLEFROT", CubeListBuilder.create().mirror()
                .texOffs(60, 55).addBox(0.0F, 0.0F, -8.0F, 1.0F, 1.0F, 8.0F),
                PartPose.offsetAndRotation(-5.5F, 2.0F, -8.0F, 0.8028515F, 0.0F, 0.0F));
        body.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 12.0F, 24.0F),
                PartPose.offset(-7.0F, 0.0F - heightToRaise, -12.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(46, 46).addBox(0.0F, 0.0F, 0.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offset(-1.0F, 4.0F - heightToRaise, 11.0F));
        PartDefinition LEG4TOPROT = body.addOrReplaceChild("LEG4TOPROT", CubeListBuilder.create().mirror()
                .texOffs(0, 40).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(4.0F, 12.0F - heightToRaise, 9.0F));
        LEG4TOPROT.addOrReplaceChild("LEG4BOTROT", CubeListBuilder.create().mirror()
                .texOffs(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition LEG3TOPROT = body.addOrReplaceChild("LEG3TOPROT", CubeListBuilder.create().mirror()
                .texOffs(0, 40).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(-4.0F, 12.0F - heightToRaise, 9.0F));
        LEG3TOPROT.addOrReplaceChild("LEG3BOTROT", CubeListBuilder.create().mirror()
                .texOffs(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition LEG1TOPROT = body.addOrReplaceChild("LEG1TOPROT", CubeListBuilder.create().mirror()
                .texOffs(0, 40).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(-4.0F, 12.0F - heightToRaise, -9.0F));
        LEG1TOPROT.addOrReplaceChild("LEG1BOTROT", CubeListBuilder.create().mirror()
                .texOffs(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition LEG2TOPROT = body.addOrReplaceChild("LEG2TOPROT", CubeListBuilder.create().mirror()
                .texOffs(0, 40).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(4.0F, 12.0F - heightToRaise, -9.0F));
        LEG2TOPROT.addOrReplaceChild("LEG2BOTROT", CubeListBuilder.create().mirror()
                .texOffs(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(BigAnimalRenderer.State state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float amount = BigModelParts.logSwing(state.walkAnimationSpeed);
        float front = Mth.cos(pos * 0.6662F + Mth.PI) * 1.8F * amount;
        float back = Mth.cos(pos * 0.6662F) * 1.8F * amount;
        float frontBend = Math.abs(Mth.cos(pos * 0.6662F / 2.0F + Mth.PI) * 1.8F * amount);
        float backBend = Math.abs(Mth.cos(pos * 0.6662F / 2.0F) * 1.8F * amount);
        this.leg1Top.xRot = front;
        this.leg2Top.xRot = back;
        this.leg1Bot.xRot = frontBend;
        this.leg2Bot.xRot = backBend;
        this.leg3Top.xRot = front;
        this.leg4Top.xRot = back;
        this.leg3Bot.xRot = frontBend;
        this.leg4Bot.xRot = backBend;
        this.tail.zRot = front;
        float ear = Mth.cos(pos * 0.6662F + Mth.PI) * 1.2F * amount;
        this.earLeft.yRot = (float) (1.2217304763960306 + ear);
        this.earRight.yRot = (float) (-1.2217304763960306 - ear);
        this.trunk[0].xRot = 0.0F;
        this.trunk[1].xRot = 0.0F;
        this.trunk[2].xRot = 0.0F;
        float curl = (float) Math.abs(Math.PI / 4.0 * Mth.cos(pos * 0.6662F / 4.0F + Mth.HALF_PI));
        for (int i = 3; i < 7; i++) {
            this.trunk[i].xRot = curl;
        }
        this.headrot.xRot = BigModelParts.clampDeg(state.xRot, -15.0F, 15.0F);
        this.headrot.yRot = BigModelParts.clampDeg(state.yRot, -30.0F, 30.0F);
    }
}
