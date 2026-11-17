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
 * ModelCrocodile: upper and lower jaw, a body of six segments and a tail of nine that sway sideways while walking;
 * the upper jaw snaps with the walk. Baby: jaws at 0.85 moved by (0, 6, 3.4) * 0.85, body at half size.
 */
public class AlligatorModel extends EntityModel<BigAnimalRenderer.State> {
    private final ModelPart mouthTop;
    private final ModelPart mouthBottom;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    /** BODY1ROT..BODY4ROT (phase 0), BODY5ROT, BODY6ROT, TAIL1ROT, TAIL2ROT (phase pi), TAIL3ROT..TAILROT6 (2 pi), TAILROT7..9 (3 pi). */
    private final ModelPart[] spine;
    private static final float[] SPINE_PHASE = {0.0F, 0.0F, 0.0F, 0.0F, Mth.PI, Mth.PI, Mth.PI, Mth.PI,
            Mth.TWO_PI, Mth.TWO_PI, Mth.TWO_PI, Mth.TWO_PI, 3.0F * Mth.PI, 3.0F * Mth.PI, 3.0F * Mth.PI};

    public AlligatorModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> parts = root.createPartLookup();
        this.mouthTop = parts.apply("MOUTHTOPROT");
        this.mouthBottom = parts.apply("MOUTHBOTROT");
        this.leg1 = parts.apply("LEG1TOPROT");
        this.leg2 = parts.apply("LEG2TOPROT");
        this.leg3 = parts.apply("LEG3TOPROT");
        this.leg4 = parts.apply("LEG4TOPROT");
        String[] names = {"BODY1ROT", "BODY2ROT", "BODY3ROT", "BODY4ROT", "BODY5ROT", "BODY6ROT", "TAIL1ROT", "TAIL2ROT",
                "TAIL3ROT", "TAILROT4", "TAILROT5", "TAILROT6", "TAILROT7", "TAILROT8", "TAILROT9"};
        this.spine = new ModelPart[names.length];
        for (int i = 0; i < names.length; i++) {
            this.spine[i] = parts.apply(names[i]);
        }
    }

    public static LayerDefinition createBodyLayer() {
        return create(Transform.scale(1.0F), Transform.scale(1.0F));
    }

    public static LayerDefinition createBabyLayer() {
        return create(new Transform(0.85F, 0.0F, 6.0F * 0.85F, 3.4F * 0.85F), Transform.HALF_BODY);
    }

    private static LayerDefinition create(Transform headTransform, Transform bodyTransform) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = BigModelParts.group(root, "head_group", headTransform);
        PartDefinition body = BigModelParts.group(root, "body_group", bodyTransform);
        head.addOrReplaceChild("MOUTHBOTROT", CubeListBuilder.create().mirror()
                .texOffs(98, 0).addBox(-4.0F, 0.0F, -7.0F, 8.0F, 3.0F, 7.0F)
                .texOffs(109, 18).addBox(-1.0F, 0.0F, -16.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(107, 10).addBox(-3.0F, 0.0F, -12.0F, 6.0F, 3.0F, 5.0F)
                .texOffs(115, 18).addBox(-2.0F, 0.0F, -15.0F, 4.0F, 2.0F, 3.0F)
                .texOffs(24, 0).addBox(3.0F, -1.0F, -5.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(24, 0).addBox(2.0F, -1.0F, -8.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(24, 0).addBox(1.0F, -1.0F, -14.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(24, 0).addBox(2.0F, -1.0F, -10.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(24, 0).addBox(-4.0F, -1.0F, -5.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(24, 0).addBox(-3.0F, -1.0F, -8.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(24, 0).addBox(-3.0F, -1.0F, -10.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(24, 0).addBox(-2.0F, -1.0F, -14.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 18.0F, -7.0F));
        PartDefinition MOUTHTOPROT = head.addOrReplaceChild("MOUTHTOPROT", CubeListBuilder.create().mirror()
                .texOffs(98, 23).addBox(-4.5F, -4.0F, -7.0F, 9.0F, 4.0F, 7.0F)
                .texOffs(107, 34).addBox(-3.5F, -3.0F, -12.0F, 7.0F, 3.0F, 5.0F)
                .texOffs(113, 42).addBox(-2.5F, -2.0F, -15.0F, 5.0F, 2.0F, 3.0F)
                .texOffs(94, 23).addBox(-1.5F, -5.0F, -3.5F, 3.0F, 1.0F, 2.0F)
                .texOffs(105, 42).addBox(-1.5F, -1.0F, -16.0F, 3.0F, 1.0F, 1.0F)
                .texOffs(24, 2).addBox(-4.5F, 0.0F, -4.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 0).addBox(-2.5F, 0.0F, -14.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(24, 2).addBox(-4.5F, 0.0F, -6.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 2).addBox(-3.5F, 0.0F, -9.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 2).addBox(-3.5F, 0.0F, -11.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 2).addBox(-4.5F, 0.0F, -2.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 2).addBox(3.5F, 0.0F, -2.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 2).addBox(3.5F, 0.0F, -4.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 2).addBox(3.5F, 0.0F, -6.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 2).addBox(2.5F, 0.0F, -9.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 2).addBox(2.5F, 0.0F, -11.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(24, 0).addBox(1.5F, 0.0F, -14.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 18.0F, -7.0F));
        MOUTHTOPROT.addOrReplaceChild("EYEBALLLEFROT", CubeListBuilder.create().mirror()
                .texOffs(98, 19).addBox(-1.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F)
                .texOffs(98, 21).addBox(-1.0F, -0.5F, 0.5F, 2.0F, 1.0F, 1.0F)
                .texOffs(98, 21).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 1.0F, 1.0F)
                .texOffs(98, 21).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(-2.5F, -4.5F, -2.5F));
        MOUTHTOPROT.addOrReplaceChild("EYEBALLRIGROT", CubeListBuilder.create().mirror()
                .texOffs(98, 19).addBox(-0.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F)
                .texOffs(98, 21).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 1.0F, 1.0F)
                .texOffs(98, 21).addBox(-1.0F, -0.5F, 0.5F, 2.0F, 1.0F, 1.0F)
                .texOffs(98, 21).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(2.5F, -4.5F, -2.5F));
        PartDefinition BODY1ROT = body.addOrReplaceChild("BODY1ROT", CubeListBuilder.create().mirror()
                .texOffs(62, 0).addBox(-7.0F, -4.0F, 0.0F, 14.0F, 8.0F, 4.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 17.0F, -7.0F));
        PartDefinition BODY2ROT = BODY1ROT.addOrReplaceChild("BODY2ROT", CubeListBuilder.create().mirror()
                .texOffs(62, 0).addBox(-7.0F, -4.0F, 0.0F, 14.0F, 8.0F, 4.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition LEG2TOPROT = BODY2ROT.addOrReplaceChild("LEG2TOPROT", CubeListBuilder.create().mirror()
                .texOffs(20, 12).addBox(0.0F, -2.0F, -2.0F, 6.0F, 4.0F, 4.0F)
                .texOffs(16, 8).addBox(0.0F, -3.0F, -1.5F, 5.0F, 1.0F, 3.0F),
                PartPose.offset(7.0F, 1.0F, 2.0F));
        LEG2TOPROT.addOrReplaceChild("LEG2BOTROT", CubeListBuilder.create().mirror()
                .texOffs(16, 20).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(4.0F, 2.0F, 0.0F));
        PartDefinition LEG1TOPROT = BODY2ROT.addOrReplaceChild("LEG1TOPROT", CubeListBuilder.create().mirror()
                .texOffs(0, 12).addBox(-5.0F, -2.0F, -2.0F, 6.0F, 4.0F, 4.0F)
                .texOffs(0, 8).addBox(-4.0F, -3.0F, -1.5F, 5.0F, 1.0F, 3.0F),
                PartPose.offset(-8.0F, 1.0F, 2.0F));
        LEG1TOPROT.addOrReplaceChild("LEG1BOTROT", CubeListBuilder.create().mirror()
                .texOffs(0, 20).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(-3.0F, 2.0F, 0.0F));
        PartDefinition BODY3ROT = BODY2ROT.addOrReplaceChild("BODY3ROT", CubeListBuilder.create().mirror()
                .texOffs(62, 0).addBox(-7.0F, -4.0F, 0.0F, 14.0F, 8.0F, 4.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition BODY4ROT = BODY3ROT.addOrReplaceChild("BODY4ROT", CubeListBuilder.create().mirror()
                .texOffs(62, 0).addBox(-7.0F, -4.0F, 0.0F, 14.0F, 8.0F, 4.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition BODY5ROT = BODY4ROT.addOrReplaceChild("BODY5ROT", CubeListBuilder.create().mirror()
                .texOffs(62, 0).addBox(-7.0F, -4.0F, 0.0F, 14.0F, 8.0F, 4.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition BODY6ROT = BODY5ROT.addOrReplaceChild("BODY6ROT", CubeListBuilder.create().mirror()
                .texOffs(62, 0).addBox(-7.0F, -4.0F, 0.0F, 14.0F, 8.0F, 4.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -5.0F, 2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.5F, -5.0F, 3.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition TAIL1ROT = BODY6ROT.addOrReplaceChild("TAIL1ROT", CubeListBuilder.create().mirror()
                .texOffs(62, 12).addBox(-6.0F, -3.5F, 0.0F, 12.0F, 7.0F, 3.0F)
                .texOffs(0, 0).addBox(-2.0F, -4.5F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -4.5F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-5.0F, -4.5F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(4.0F, -4.5F, 1.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition TAIL2ROT = TAIL1ROT.addOrReplaceChild("TAIL2ROT", CubeListBuilder.create().mirror()
                .texOffs(62, 22).addBox(-5.5F, -2.5F, 0.0F, 11.0F, 6.0F, 3.0F)
                .texOffs(0, 0).addBox(-4.5F, -3.5F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(3.5F, -3.5F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -3.5F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -3.5F, 0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        PartDefinition TAIL3ROT = TAIL2ROT.addOrReplaceChild("TAIL3ROT", CubeListBuilder.create().mirror()
                .texOffs(62, 31).addBox(-4.5F, -1.5F, 0.0F, 9.0F, 5.0F, 4.0F)
                .texOffs(0, 0).addBox(2.5F, -2.5F, 1.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(0.5F, -2.5F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-1.5F, -2.5F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-4.0F, -2.5F, 1.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        PartDefinition TAILROT4 = TAIL3ROT.addOrReplaceChild("TAILROT4", CubeListBuilder.create().mirror()
                .texOffs(62, 40).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 4.0F, 4.0F)
                .texOffs(0, 0).addBox(2.0F, -3.0F, 1.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-0.5F, -3.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-3.0F, -3.0F, 1.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 1.5F, 4.0F));
        PartDefinition TAILROT5 = TAILROT4.addOrReplaceChild("TAILROT5", CubeListBuilder.create().mirror()
                .texOffs(62, 48).addBox(-3.0F, -1.5F, 0.0F, 6.0F, 3.0F, 5.0F)
                .texOffs(0, 0).addBox(-2.0F, -2.5F, 1.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -2.5F, 3.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(1.0F, -2.5F, 1.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-2.0F, -2.5F, 3.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.5F, 4.0F));
        PartDefinition TAILROT6 = TAILROT5.addOrReplaceChild("TAILROT6", CubeListBuilder.create().mirror()
                .texOffs(84, 49).addBox(-2.5F, -1.0F, 0.0F, 5.0F, 2.0F, 5.0F)
                .texOffs(0, 0).addBox(-1.5F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(0.5F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(0.5F, -2.0F, 3.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-1.5F, -2.0F, 3.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.5F, 5.0F));
        PartDefinition TAILROT7 = TAILROT6.addOrReplaceChild("TAILROT7", CubeListBuilder.create().mirror()
                .texOffs(90, 44).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 3.0F)
                .texOffs(0, 0).addBox(0.5F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-1.5F, -2.0F, 1.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 5.0F));
        PartDefinition TAILROT8 = TAILROT7.addOrReplaceChild("TAILROT8", CubeListBuilder.create().mirror()
                .texOffs(94, 40).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 2.0F)
                .texOffs(0, 0).addBox(0.5F, -2.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 0).addBox(-1.5F, -2.0F, 0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        TAILROT8.addOrReplaceChild("TAILROT9", CubeListBuilder.create().mirror()
                .texOffs(98, 37).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        PartDefinition LEG4TOPROT = BODY6ROT.addOrReplaceChild("LEG4TOPROT", CubeListBuilder.create().mirror()
                .texOffs(20, 12).addBox(0.0F, -2.0F, -2.0F, 6.0F, 4.0F, 4.0F)
                .texOffs(16, 8).addBox(0.0F, -3.0F, -1.5F, 5.0F, 1.0F, 3.0F),
                PartPose.offset(7.0F, 1.0F, 0.0F));
        LEG4TOPROT.addOrReplaceChild("LEG4BOTROT", CubeListBuilder.create().mirror()
                .texOffs(16, 20).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(4.0F, 2.0F, 0.0F));
        PartDefinition LEG3TOPROT = BODY6ROT.addOrReplaceChild("LEG3TOPROT", CubeListBuilder.create().mirror()
                .texOffs(0, 12).addBox(-5.0F, -2.0F, -2.0F, 6.0F, 4.0F, 4.0F)
                .texOffs(0, 8).addBox(-4.0F, -3.0F, -1.5F, 5.0F, 1.0F, 3.0F),
                PartPose.offset(-8.0F, 1.0F, 0.0F));
        LEG3TOPROT.addOrReplaceChild("LEG3BOTROT", CubeListBuilder.create().mirror()
                .texOffs(0, 20).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(-3.0F, 2.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(BigAnimalRenderer.State state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float amount = state.walkAnimationSpeed;
        float swing = pos * 0.6662F * 2.0F;
        this.leg2.xRot = Mth.cos(swing) * 1.8F * amount;
        this.leg4.xRot = Mth.cos(swing) * 1.8F * amount;
        this.leg1.xRot = Mth.cos(swing + Mth.PI) * 1.8F * amount;
        this.leg3.xRot = Mth.cos(swing + Mth.PI) * 1.8F * amount;
        for (int i = 0; i < this.spine.length; i++) {
            this.spine[i].yRot = Mth.cos(swing + SPINE_PHASE[i]) * 0.1F * amount;
        }
        float pitch = BigModelParts.clampDeg(state.xRot, -15.0F, 15.0F);
        float yaw = BigModelParts.clampDeg(state.yRot, -45.0F, 45.0F);
        this.mouthTop.xRot = -Math.abs(Mth.cos(pos * 0.6662F) * amount) + pitch;
        this.mouthTop.yRot = yaw;
        this.mouthBottom.xRot = pitch;
        this.mouthBottom.yRot = yaw;
    }
}
