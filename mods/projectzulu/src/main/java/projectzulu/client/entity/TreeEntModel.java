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
 * ModelTreeEnt (heightToRaise 8, drawn at 1.5 times the scale): a mossy trunk with six little trees on its back, a
 * face, two two-part arms with fingers and two legs with toes. Arms swing with the walk while idle or looking, are
 * held forward while attacking and swing a club-like blow over animTime. The original's final "else if
 * (maxAnimTime > 0)" branch is always taken once animTime is at or below 12, so it overrides the walking arm swing;
 * that is kept. Baby: whole model at half size.
 */
public class TreeEntModel extends EntityModel<BigAnimalRenderer.State> {
    private static final float HEIGHT_TO_RAISE = 8.0F;
    private static final float A5_12 = (float) (Math.PI * 5.0 / 12.0);
    private static final float A4_9 = (float) (Math.PI * 4.0 / 9.0);
    private static final float A5DEG = 0.08726646F;
    private static final float A110DEG = 1.9198622F;

    private final ModelPart headrot;
    private final ModelPart[] backTrees = new ModelPart[6];
    private final ModelPart[] toesRight = new ModelPart[6];
    private final ModelPart[] toesLeft = new ModelPart[6];
    private final ModelPart legRightTop;
    private final ModelPart legLeftTop;
    private final ModelPart legRightBot;
    private final ModelPart legLeftBot;
    private final ModelPart armLeftTop;
    private final ModelPart armRightTop;
    private final ModelPart armLeftBot;
    private final ModelPart armRightBot;

    public TreeEntModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> parts = root.createPartLookup();
        this.headrot = parts.apply("HEADROT");
        for (int i = 0; i < 6; i++) {
            this.backTrees[i] = parts.apply("BACKTREESTEM" + (i + 1));
            this.toesRight[i] = parts.apply("toer" + (i + 1));
            this.toesLeft[i] = parts.apply("toel" + (i + 1));
        }
        this.legRightTop = parts.apply("LEGRIGTOPROT");
        this.legLeftTop = parts.apply("LEGLEFTTOPROT");
        this.legRightBot = parts.apply("LEGRIGBOTROT");
        this.legLeftBot = parts.apply("LEGLEFBOTROT");
        this.armLeftTop = parts.apply("ARMLEFTOPROT");
        this.armRightTop = parts.apply("ARMRIGTOPROT");
        this.armLeftBot = parts.apply("ARMLEFBOTROT");
        this.armRightBot = parts.apply("ARMRIGBOTROT");
    }

    public static LayerDefinition createBodyLayer() {
        return create(Transform.scale(1.5F));
    }

    public static LayerDefinition createBabyLayer() {
        return create(new Transform(0.75F, 0.0F, 12.0F, 0.0F));
    }

    private static LayerDefinition create(Transform transform) {
        float heightToRaise = HEIGHT_TO_RAISE;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition main = BigModelParts.group(root, "main_group", transform);
        PartDefinition BODYROT = main.addOrReplaceChild("BODYROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 4.0F - heightToRaise, 0.5F));
        BODYROT.addOrReplaceChild("mosscb3", CubeListBuilder.create()
                .texOffs(0, 36).addBox(0.0F, 0.0F, 0.0F, 7.0F, 11.0F, 3.0F),
                PartPose.offset(-3.5F, -12.0F, 3.0F));
        BODYROT.addOrReplaceChild("mosscf2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offset(-4.0F, -4.0F, -3.5F));
        BODYROT.addOrReplaceChild("mosscf1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 5.0F, 5.0F, 1.0F),
                PartPose.offset(1.0F, -8.0F, -3.5F));
        BODYROT.addOrReplaceChild("mosscf3", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offset(4.0F, -1.0F, -3.5F));
        BODYROT.addOrReplaceChild("mosscb2", CubeListBuilder.create()
                .texOffs(0, 28).addBox(0.0F, 0.0F, 0.0F, 7.0F, 3.0F, 4.0F),
                PartPose.offset(-3.5F, -15.0F, 1.0F));
        BODYROT.addOrReplaceChild("mosscb", CubeListBuilder.create()
                .texOffs(0, 20).addBox(0.0F, 0.0F, 0.0F, 9.0F, 3.0F, 4.0F),
                PartPose.offset(-4.5F, -15.0F, -3.0F));
        BODYROT.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 15.0F, 14.0F, 6.0F),
                PartPose.offset(-8.0F, -12.0F, -3.0F));
        PartDefinition ARMLEFTOPROT = BODYROT.addOrReplaceChild("ARMLEFTOPROT", CubeListBuilder.create(),
                PartPose.offset(-10.5F, -10.0F, 0.0F));
        ARMLEFTOPROT.addOrReplaceChild("mossual2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(-0.5F, 8.0F, -3.0F));
        ARMLEFTOPROT.addOrReplaceChild("mossual3", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 7.0F, -3.0F));
        ARMLEFTOPROT.addOrReplaceChild("mossual1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 6.0F, 1.0F),
                PartPose.offset(0.5F, 1.0F, -3.0F));
        ARMLEFTOPROT.addOrReplaceChild("mossual5", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 4.0F, 3.0F),
                PartPose.offset(-3.0F, 11.0F, -1.5F));
        ARMLEFTOPROT.addOrReplaceChild("mossual4", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F),
                PartPose.offset(-3.0F, 1.0F, -0.5F));
        ARMLEFTOPROT.addOrReplaceChild("mossual6", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 6.0F, 1.0F),
                PartPose.offset(0.0F, 7.0F, 2.333333F));
        ARMLEFTOPROT.addOrReplaceChild("mossual7", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 6.0F, 3.5F));
        ARMLEFTOPROT.addOrReplaceChild("mossual8", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(-0.5F, 0.0F, 3.0F));
        ARMLEFTOPROT.addOrReplaceChild("armleftop", CubeListBuilder.create()
                .texOffs(42, 0).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 16.0F, 5.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition ARMLEFBOTROT = ARMLEFTOPROT.addOrReplaceChild("ARMLEFBOTROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        ARMLEFBOTROT.addOrReplaceChild("fingerl1", CubeListBuilder.create()
                .texOffs(32, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(-2.0F, 14.0F, 0.0F));
        ARMLEFBOTROT.addOrReplaceChild("fingerl3", CubeListBuilder.create()
                .texOffs(32, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(1.0F, 14.0F, 2.0F));
        ARMLEFBOTROT.addOrReplaceChild("fingerl2", CubeListBuilder.create()
                .texOffs(32, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(1.0F, 14.0F, -2.0F));
        ARMLEFBOTROT.addOrReplaceChild("mosslal2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(0.5F, 4.0F, -3.0F));
        ARMLEFBOTROT.addOrReplaceChild("mosslal1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 3.0F, -3.0F));
        ARMLEFBOTROT.addOrReplaceChild("mosslal3", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F),
                PartPose.offset(-3.0F, 6.0F, -0.5F));
        ARMLEFBOTROT.addOrReplaceChild("mosslal5", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(0.5F, 4.0F, 3.0F));
        ARMLEFBOTROT.addOrReplaceChild("mosslal4", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 3.0F, 3.0F));
        ARMLEFBOTROT.addOrReplaceChild("armlefbot", CubeListBuilder.create()
                .texOffs(42, 21).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition ARMRIGTOPROT = BODYROT.addOrReplaceChild("ARMRIGTOPROT", CubeListBuilder.create(),
                PartPose.offset(9.5F, -10.0F, 0.0F));
        ARMRIGTOPROT.addOrReplaceChild("mossuar3", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(0.5F, 8.0F, -3.0F));
        ARMRIGTOPROT.addOrReplaceChild("mossuar2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 7.0F, -3.0F));
        ARMRIGTOPROT.addOrReplaceChild("mossuar1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 6.0F, 1.0F),
                PartPose.offset(0.5F, 1.0F, -3.0F));
        ARMRIGTOPROT.addOrReplaceChild("mossuar5", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 4.0F, 3.0F),
                PartPose.offset(3.0F, 11.0F, -1.0F));
        ARMRIGTOPROT.addOrReplaceChild("mossuar4", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F),
                PartPose.offset(3.0F, 1.0F, -0.5F));
        ARMRIGTOPROT.addOrReplaceChild("mossuar8", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(0.5F, 8.0F, 3.0F));
        ARMRIGTOPROT.addOrReplaceChild("mossuar6", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 6.0F, 1.0F),
                PartPose.offset(0.5F, 1.0F, 3.0F));
        ARMRIGTOPROT.addOrReplaceChild("mossuar7", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 4.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 7.0F, 3.0F));
        ARMRIGTOPROT.addOrReplaceChild("armrigtop", CubeListBuilder.create()
                .texOffs(42, 0).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 16.0F, 5.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition ARMRIGBOTROT = ARMRIGTOPROT.addOrReplaceChild("ARMRIGBOTROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        ARMRIGBOTROT.addOrReplaceChild("fingerr3", CubeListBuilder.create()
                .texOffs(32, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(-1.0F, 14.0F, 2.0F));
        ARMRIGBOTROT.addOrReplaceChild("fingerr1", CubeListBuilder.create()
                .texOffs(32, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(2.0F, 14.0F, 0.0F));
        ARMRIGBOTROT.addOrReplaceChild("fingerr2", CubeListBuilder.create()
                .texOffs(32, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(-1.0F, 14.0F, -2.0F));
        ARMRIGBOTROT.addOrReplaceChild("mosslar2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(0.5F, 4.0F, -3.0F));
        ARMRIGBOTROT.addOrReplaceChild("mosslar1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 3.0F, -3.0F));
        ARMRIGBOTROT.addOrReplaceChild("mosslar3", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F),
                PartPose.offset(3.0F, 6.0F, -0.5F));
        ARMRIGBOTROT.addOrReplaceChild("mosslar4", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 3.0F, 3.0F));
        ARMRIGBOTROT.addOrReplaceChild("mosslar5", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(0.5F, 4.0F, 3.0F));
        ARMRIGBOTROT.addOrReplaceChild("armrigbot", CubeListBuilder.create()
                .texOffs(42, 21).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition BACKTREESTEM6 = BODYROT.addOrReplaceChild("BACKTREESTEM6", CubeListBuilder.create(),
                PartPose.offset(-3.0F, -15.0F, 0.5F));
        BACKTREESTEM6.addOrReplaceChild("backtreesleaves6", CubeListBuilder.create()
                .texOffs(23, 42).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        BACKTREESTEM6.addOrReplaceChild("backtreestem6", CubeListBuilder.create()
                .texOffs(32, 28).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition BACKTREESTEM5 = BODYROT.addOrReplaceChild("BACKTREESTEM5", CubeListBuilder.create(),
                PartPose.offset(3.0F, -15.0F, 0.5F));
        BACKTREESTEM5.addOrReplaceChild("backtreestem5", CubeListBuilder.create()
                .texOffs(32, 28).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        BACKTREESTEM5.addOrReplaceChild("backtreesleaves5", CubeListBuilder.create()
                .texOffs(23, 42).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        PartDefinition BACKTREESTEM4 = BODYROT.addOrReplaceChild("BACKTREESTEM4", CubeListBuilder.create(),
                PartPose.offset(3.0F, -3.0F, 5.5F));
        BACKTREESTEM4.addOrReplaceChild("backtreesleaves4", CubeListBuilder.create()
                .texOffs(23, 33).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        BACKTREESTEM4.addOrReplaceChild("backtreestem4", CubeListBuilder.create()
                .texOffs(23, 29).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition BACKTREESTEM3 = BODYROT.addOrReplaceChild("BACKTREESTEM3", CubeListBuilder.create(),
                PartPose.offset(-3.0F, -3.0F, 5.5F));
        BACKTREESTEM3.addOrReplaceChild("backtreesleaves3", CubeListBuilder.create()
                .texOffs(23, 33).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        BACKTREESTEM3.addOrReplaceChild("backtreestem3", CubeListBuilder.create()
                .texOffs(23, 29).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition BACKTREESTEM2 = BODYROT.addOrReplaceChild("BACKTREESTEM2", CubeListBuilder.create(),
                PartPose.offset(3.0F, -11.0F, 5.5F));
        BACKTREESTEM2.addOrReplaceChild("backtreesleaves2", CubeListBuilder.create()
                .texOffs(23, 33).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        BACKTREESTEM2.addOrReplaceChild("backtreestem2", CubeListBuilder.create()
                .texOffs(23, 29).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition BACKTREESTEM1 = BODYROT.addOrReplaceChild("BACKTREESTEM1", CubeListBuilder.create(),
                PartPose.offset(-3.0F, -11.0F, 5.5F));
        BACKTREESTEM1.addOrReplaceChild("backtreesleaves1", CubeListBuilder.create()
                .texOffs(23, 33).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        BACKTREESTEM1.addOrReplaceChild("backtreestem1", CubeListBuilder.create()
                .texOffs(23, 29).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition HEADROT = BODYROT.addOrReplaceChild("HEADROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, -4.5F));
        HEADROT.addOrReplaceChild("brow4", CubeListBuilder.create()
                .texOffs(98, 18).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -5.0F, -3.0F));
        PartDefinition NOSEROT = HEADROT.addOrReplaceChild("NOSEROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, -3.0F));
        NOSEROT.addOrReplaceChild("nose2", CubeListBuilder.create()
                .texOffs(94, 20).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-0.5F, 1.0F, -2.5F));
        NOSEROT.addOrReplaceChild("nose1", CubeListBuilder.create()
                .texOffs(90, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offset(-0.5F, 0.0F, -2.5F));
        NOSEROT.addOrReplaceChild("nose3", CubeListBuilder.create()
                .texOffs(90, 22).addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(-1.0F, 2.0F, -3.0F));
        HEADROT.addOrReplaceChild("brow3", CubeListBuilder.create()
                .texOffs(98, 18).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(-2.0F, -5.0F, -3.0F));
        HEADROT.addOrReplaceChild("brow1", CubeListBuilder.create()
                .texOffs(106, 13).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -7.0F, -3.0F));
        HEADROT.addOrReplaceChild("brow5", CubeListBuilder.create()
                .texOffs(98, 18).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(2.0F, -5.0F, -3.0F));
        HEADROT.addOrReplaceChild("brow2", CubeListBuilder.create()
                .texOffs(106, 17).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, -3.0F, -3.0F));
        HEADROT.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(88, 0).addBox(0.0F, 0.0F, 0.0F, 5.0F, 12.0F, 4.0F),
                PartPose.offset(-2.5F, -9.0F, -2.5F));
        PartDefinition LEGRIGTOPROT = main.addOrReplaceChild("LEGRIGTOPROT", CubeListBuilder.create(),
                PartPose.offset(3.5F, 6.0F - heightToRaise, 0.5F));
        LEGRIGTOPROT.addOrReplaceChild("mossulr1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offset(0.5F, 4.0F, -3.5F));
        LEGRIGTOPROT.addOrReplaceChild("mossulr2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offset(0.5F, 4.0F, 3.5F));
        LEGRIGTOPROT.addOrReplaceChild("legrigtop", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-2.5F, 0.0F, -2.5F, 6.0F, 9.0F, 6.0F),
                PartPose.offset(-0.5F, 0.0F, -0.5F));
        PartDefinition LEGRIGBOTROT = LEGRIGTOPROT.addOrReplaceChild("LEGRIGBOTROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        LEGRIGBOTROT.addOrReplaceChild("mossllr2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offset(0.5F, 3.0F, 3.5F));
        LEGRIGBOTROT.addOrReplaceChild("mossllr1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offset(0.5F, 3.0F, -3.5F));
        LEGRIGBOTROT.addOrReplaceChild("toer6", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offset(2.0F, 8.5F, -2.5F));
        LEGRIGBOTROT.addOrReplaceChild("toer5", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 8.5F, -2.5F));
        LEGRIGBOTROT.addOrReplaceChild("toer4", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offset(-2.0F, 8.5F, -2.5F));
        LEGRIGBOTROT.addOrReplaceChild("toer3", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(2.0F, 8.5F, 2.5F));
        LEGRIGBOTROT.addOrReplaceChild("toer2", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 8.5F, 2.5F));
        LEGRIGBOTROT.addOrReplaceChild("toer1", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(-2.0F, 8.5F, 2.5F));
        LEGRIGBOTROT.addOrReplaceChild("legrigbot", CubeListBuilder.create()
                .texOffs(62, 15).addBox(0.0F, 0.0F, 0.0F, 7.0F, 9.0F, 6.0F),
                PartPose.offset(-3.5F, 0.0F, -3.0F));
        PartDefinition LEGLEFTTOPROT = main.addOrReplaceChild("LEGLEFTTOPROT", CubeListBuilder.create(),
                PartPose.offset(-4.5F, 6.0F - heightToRaise, 0.5F));
        LEGLEFTTOPROT.addOrReplaceChild("mossull2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(1.5F, 5.0F, -3.5F));
        LEGLEFTTOPROT.addOrReplaceChild("mossull1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(-0.5F, 2.0F, -3.5F));
        LEGLEFTTOPROT.addOrReplaceChild("mossull4", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(1.5F, 5.0F, 3.5F));
        LEGLEFTTOPROT.addOrReplaceChild("legleftop", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-2.5F, 0.0F, -2.5F, 6.0F, 9.0F, 6.0F),
                PartPose.offset(-0.5F, 0.0F, 0.0F));
        LEGLEFTTOPROT.addOrReplaceChild("mossull3", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(-0.5F, 2.0F, 3.5F));
        PartDefinition LEGLEFBOTROT = LEGLEFTTOPROT.addOrReplaceChild("LEGLEFBOTROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        LEGLEFBOTROT.addOrReplaceChild("toel4", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offset(-2.5F, 8.5F, -2.5F));
        LEGLEFBOTROT.addOrReplaceChild("toel6", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offset(1.5F, 8.5F, -2.5F));
        LEGLEFBOTROT.addOrReplaceChild("toel5", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F),
                PartPose.offset(-0.5F, 8.5F, -2.5F));
        LEGLEFBOTROT.addOrReplaceChild("toel3", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(1.5F, 8.5F, 2.5F));
        LEGLEFBOTROT.addOrReplaceChild("toel2", CubeListBuilder.create()
                .texOffs(62, 30).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(-0.5F, 8.5F, 2.5F));
        LEGLEFBOTROT.addOrReplaceChild("toel1", CubeListBuilder.create()
                .texOffs(62, 30).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(-3.0F, 8.0F, 2.5F));
        LEGLEFBOTROT.addOrReplaceChild("mosslll1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(-1.0F, 3.0F, -3.5F));
        LEGLEFBOTROT.addOrReplaceChild("mosslll2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -1.5F, -0.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(-1.0F, 3.0F, 3.5F));
        LEGLEFBOTROT.addOrReplaceChild("leglefbot", CubeListBuilder.create()
                .texOffs(62, 15).addBox(0.0F, 0.0F, 0.0F, 7.0F, 9.0F, 6.0F),
                PartPose.offset(-3.5F, 0.0F, -3.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(BigAnimalRenderer.State state) {
        super.setupAnim(state);
        this.backTrees[0].yRot = -0.43633232F;
        this.backTrees[1].yRot = 0.43633232F;
        this.backTrees[2].yRot = (float) (-Math.PI * 2.0 / 9.0);
        this.backTrees[3].yRot = (float) (Math.PI * 2.0 / 9.0);
        this.backTrees[4].xRot = -Mth.PI / 6.0F;
        this.backTrees[4].zRot = Mth.PI / 6.0F;
        this.backTrees[5].xRot = -Mth.PI / 6.0F;
        this.backTrees[5].zRot = -Mth.PI / 6.0F;
        float toe = Mth.PI / 12.0F;
        this.toesRight[0].yRot = -toe;
        this.toesRight[2].yRot = toe;
        this.toesRight[3].yRot = toe;
        this.toesRight[5].yRot = -toe;
        this.toesLeft[0].yRot = -toe;
        this.toesLeft[2].yRot = toe;
        this.toesLeft[3].yRot = toe;
        this.toesLeft[5].yRot = -toe;

        float pos = state.walkAnimationPos;
        float amount = BigModelParts.logSwing(state.walkAnimationSpeed);
        this.legRightTop.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.8F * amount;
        this.legLeftTop.xRot = Mth.cos(pos * 0.6662F) * 1.8F * amount;
        this.legRightBot.xRot = Math.abs(Mth.cos(pos * 0.6662F / 2.0F + Mth.PI) * 1.8F * amount);
        this.legLeftBot.xRot = Math.abs(Mth.cos(pos * 0.6662F / 2.0F) * 1.8F * amount);

        if (state.entityState == ZuluBigAnimal.STATE_IDLE || state.entityState == ZuluBigAnimal.STATE_LOOKING) {
            this.armLeftTop.xRot = Mth.cos(pos * 0.6662F + Mth.PI) * 1.8F * amount;
            this.armRightTop.xRot = Mth.cos(pos * 0.6662F) * 1.8F * amount;
            this.armLeftBot.xRot = -Math.abs(Mth.cos(pos * 0.6662F / 2.0F + Mth.PI) * 1.8F * amount);
            this.armRightBot.xRot = -Math.abs(Mth.cos(pos * 0.6662F / 2.0F) * 1.8F * amount);
        } else if (state.entityState == ZuluBigAnimal.STATE_ATTACKING) {
            this.armRightTop.zRot = -A5DEG;
            this.armRightTop.xRot = Mth.DEG_TO_RAD * (-5.0F + 8.0F * Mth.cos(pos * 0.6662F));
            this.armRightTop.yRot = 0.0F;
            this.armRightBot.xRot = Mth.DEG_TO_RAD * (-80.0F + 8.0F * Mth.cos(pos * 0.6662F));
            this.armLeftTop.zRot = A5DEG;
            this.armLeftTop.xRot = Mth.DEG_TO_RAD * (-5.0F + 8.0F * Mth.cos(pos * 0.6662F + Mth.PI));
            this.armLeftTop.yRot = 0.0F;
            this.armLeftBot.xRot = Mth.DEG_TO_RAD * (-80.0F + 8.0F * Mth.cos(pos * 0.6662F + Mth.PI));
        }

        // the blow; the int thresholds are maxAnimTime * 7 / 8 = 17, * 6 / 8 = 15, * 5 / 8 = 12 (integer division)
        int maxAnimTime = ZuluBigAnimal.MAX_ANIM_TIME;
        float time = state.animTime;
        if (state.animTime > maxAnimTime * 7 / 8) {
            float min = maxAnimTime * 7 / 8;
            float max = maxAnimTime;
            this.armRightTop.zRot = BigModelParts.map(time, min, max, -A5_12, -A5DEG);
            this.armRightTop.xRot = BigModelParts.map(time, min, max, Mth.PI / 12.0F, -A5DEG);
            this.armRightTop.yRot = 0.0F;
            this.armRightBot.xRot = BigModelParts.map(time, min, max, -A110DEG, -A4_9);
            this.armLeftTop.zRot = BigModelParts.map(time, min, max, A5_12, A5DEG);
            this.armLeftTop.xRot = BigModelParts.map(time, min, max, Mth.PI / 12.0F, -A5DEG);
            this.armLeftTop.yRot = 0.0F;
            this.armLeftBot.xRot = BigModelParts.map(time, min, max, -A110DEG, -A4_9);
        } else if (state.animTime > maxAnimTime * 6 / 8) {
            float max = maxAnimTime * 7 / 8;
            float min = maxAnimTime * 6 / 8;
            this.armRightTop.zRot = -A5_12;
            this.armRightTop.xRot = BigModelParts.map(time, min, max, -Mth.PI / 3.0F, Mth.PI / 12.0F);
            this.armRightTop.yRot = BigModelParts.map(time, min, max, Mth.PI / 6.0F, 0.0F);
            this.armRightBot.xRot = BigModelParts.map(time, min, max, -A4_9, -A110DEG);
            this.armLeftTop.zRot = A5_12;
            this.armLeftTop.xRot = BigModelParts.map(time, min, max, -Mth.PI / 3.0F, Mth.PI / 12.0F);
            this.armLeftTop.yRot = BigModelParts.map(time, min, max, -Mth.PI / 6.0F, 0.0F);
            this.armLeftBot.xRot = BigModelParts.map(time, min, max, -A4_9, -A110DEG);
        } else if (state.animTime > maxAnimTime * 5 / 8) {
            // minTime = maxAnimTime * 5 / 4 is the original's (sic)
            float max = maxAnimTime * 6 / 8;
            float min = maxAnimTime * 5 / 4;
            this.armRightTop.zRot = -A5_12;
            this.armRightTop.xRot = BigModelParts.map(time, min, max, Mth.PI / 12.0F, -Mth.PI / 3.0F);
            this.armRightTop.yRot = BigModelParts.map(time, min, max, 0.0F, Mth.PI / 6.0F);
            this.armRightBot.xRot = BigModelParts.map(time, min, max, -A110DEG, -A4_9);
            this.armLeftTop.zRot = A5_12;
            this.armLeftTop.xRot = BigModelParts.map(time, min, max, Mth.PI / 12.0F, -Mth.PI / 3.0F);
            this.armLeftTop.yRot = BigModelParts.map(time, min, max, 0.0F, -Mth.PI / 6.0F);
            this.armLeftBot.xRot = BigModelParts.map(time, min, max, -A110DEG, -A4_9);
        } else {
            float max = maxAnimTime * 5 / 4;
            float min = 0.0F;
            this.armRightTop.zRot = BigModelParts.map(time, min, max, -A5DEG, -A5_12);
            this.armRightTop.xRot = BigModelParts.map(time, min, max, -A5DEG, Mth.PI / 12.0F);
            this.armRightTop.yRot = 0.0F;
            this.armRightBot.xRot = BigModelParts.map(time, min, max, -A4_9, -A110DEG);
            this.armLeftTop.zRot = BigModelParts.map(time, min, max, A5DEG, A5_12);
            this.armLeftTop.xRot = BigModelParts.map(time, min, max, -A5DEG, Mth.PI / 12.0F);
            this.armLeftTop.yRot = 0.0F;
            this.armLeftBot.xRot = BigModelParts.map(time, min, max, -A4_9, -A110DEG);
        }

        this.headrot.xRot = BigModelParts.clampDeg(state.xRot, -15.0F, 15.0F);
        this.headrot.yRot = BigModelParts.clampDeg(state.yRot, -45.0F, 45.0F);
    }
}
