package projectzulu.client.gear;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * The head models of blocks/heads (ModelXHead of 1.5.2): boxes, texture offsets, mirror, rotation points and the
 * addChild hierarchy copied with tools/modelbase_to_layer.py. The poses of setRotationAngles are set by
 * {@link MobHeadRenderer}. The stuffed finch uses the finch model of the creature (ModelFinch).
 */
@SuppressWarnings("unused")
final class MobHeadModels {
    private MobHeadModels() {
    }

    /** ModelArmadilloHead (64x32), root part CENTERROT. */
    static LayerDefinition armadillo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 21.5F, 0.0F));
        CENTERROT.addOrReplaceChild("HEADPIECE", CubeListBuilder.create().mirror()
                .texOffs(17, 0).addBox(-4.0F, -3.0F, -2.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(17, 0).addBox(2.0F, -3.0F, -2.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(12, 9).addBox(-0.5F, 1.5F, -7.0F, 1.0F, 1.0F, 2.0F)
                .texOffs(0, 8).addBox(-1.5F, -0.5F, -5.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(0, 0).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 2.5F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelBearHead (128x64), root part CENTERROT. */
    static LayerDefinition bear() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 20.0F, 0.0F));
        CENTERROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(58, 0).addBox(-3.5F, -3.0F, -5.0F, 7.0F, 6.0F, 5.0F)
                .texOffs(58, 16).addBox(-2.0F, 3.0F, -3.0F, 4.0F, 1.0F, 3.0F)
                .texOffs(69, 12).addBox(-4.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(75, 12).addBox(2.0F, -5.0F, -3.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(58, 12).addBox(-1.5F, 0.0F, -7.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelBeaverHead (64x32), root part CENTERROT. */
    static LayerDefinition beaver() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        CENTERROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(38, 14).addBox(-3.0F, -3.0F, -4.0F, 6.0F, 6.0F, 4.0F)
                .texOffs(38, 24).addBox(-1.5F, 0.0F, -5.0F, 3.0F, 2.0F, 1.0F)
                .texOffs(46, 24).addBox(0.0F, 2.0F, -5.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(46, 24).addBox(-1.0F, 2.0F, -5.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelBoarHead (64x32), root part CENTERROT. */
    static LayerDefinition boar() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        CENTERROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-4.0F, -3.5F, -5.0F, 8.0F, 7.0F, 6.0F)
                .texOffs(28, 0).addBox(-2.5F, -0.5F, -7.0F, 5.0F, 4.0F, 2.0F)
                .texOffs(42, 0).addBox(-2.0F, 0.5F, -9.0F, 4.0F, 3.0F, 2.0F)
                .texOffs(0, 29).addBox(-4.0F, 2.5F, -9.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(0, 27).addBox(-4.0F, 1.5F, -9.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 29).addBox(2.0F, 2.5F, -9.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(0, 27).addBox(3.0F, 1.5F, -9.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 29).addBox(-4.5F, 2.5F, -7.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(0, 27).addBox(-4.5F, 1.5F, -7.0F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 29).addBox(2.5F, 2.5F, -7.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(0, 27).addBox(3.5F, 1.5F, -7.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelCrocodileHead (128x64), root part CENTERROT. */
    static LayerDefinition crocodile() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        PartDefinition MOUTHTOPROT = CENTERROT.addOrReplaceChild("MOUTHTOPROT", CubeListBuilder.create().mirror()
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
                PartPose.offset(0.0F, 0.0F, 7.0F));
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
        CENTERROT.addOrReplaceChild("MOUTHBOTROT", CubeListBuilder.create().mirror()
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
                PartPose.offset(0.0F, 0.0F, 7.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelElephantHead (128x64), root part CENTERROT. */
    static LayerDefinition elephant() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition HEADROT = CENTERROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(92, 0).addBox(-6.0F, -7.0F, -6.0F, 12.0F, 14.0F, 6.0F)
                .texOffs(77, 20).addBox(-6.0F, -3.0F, -8.0F, 12.0F, 9.0F, 2.0F)
                .texOffs(77, 33).addBox(-5.0F, -2.0F, -9.0F, 10.0F, 7.0F, 1.0F)
                .texOffs(102, 33).addBox(-4.0F, 0.0F, -10.0F, 8.0F, 7.0F, 1.0F)
                .texOffs(113, 43).addBox(-3.0F, 2.0F, -11.0F, 6.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, -6.0F, 8.0F));
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
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelGiraffeHead (128x64), root part NECKROT2. */
    static LayerDefinition giraffe() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition NECKROT2 = root.addOrReplaceChild("NECKROT2", CubeListBuilder.create().mirror()
                .texOffs(103, 33).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition NECKROT3 = NECKROT2.addOrReplaceChild("NECKROT3", CubeListBuilder.create().mirror()
                .texOffs(103, 30).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition NECKROT4 = NECKROT3.addOrReplaceChild("NECKROT4", CubeListBuilder.create().mirror()
                .texOffs(103, 27).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition NECKROT5 = NECKROT4.addOrReplaceChild("NECKROT5", CubeListBuilder.create().mirror()
                .texOffs(103, 24).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition NECKROT6 = NECKROT5.addOrReplaceChild("NECKROT6", CubeListBuilder.create().mirror()
                .texOffs(103, 21).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition NECKROT7 = NECKROT6.addOrReplaceChild("NECKROT7", CubeListBuilder.create().mirror()
                .texOffs(103, 18).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        NECKROT7.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(81, 46).addBox(-3.5F, -6.0F, -9.0F, 7.0F, 6.0F, 12.0F)
                .texOffs(82, 52).addBox(-2.0F, -8.0F, 1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(115, 52).addBox(1.0F, -8.0F, 1.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -1.0F, 1.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelGorillaHead (64x64), root part HEADROT. */
    static LayerDefinition gorilla() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(38, 17).addBox(-3.5F, -7.0F, -3.0F, 7.0F, 8.0F, 6.0F)
                .texOffs(0, 0).addBox(-4.5F, -6.0F, -1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(0, 4).addBox(3.5F, -6.0F, -1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(47, 32).addBox(-2.0F, -3.0F, -4.0F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 23.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** ModelLizardHead (64x32), root part CENTERROT. */
    static LayerDefinition lizard() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        PartDefinition HEADBASE = CENTERROT.addOrReplaceChild("HEADBASE", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-3.0F, -3.0F, -8.0F, 6.0F, 6.0F, 8.0F)
                .texOffs(29, 22).addBox(3.0F, -2.0F, -3.0F, 3.0F, 5.0F, 1.0F)
                .texOffs(29, 22).addBox(-6.0F, -2.0F, -3.0F, 3.0F, 5.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        HEADBASE.addOrReplaceChild("FRILL3ROT", CubeListBuilder.create().mirror()
                .texOffs(37, 22).addBox(0.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(1.0F, -2.0F, -2.5F));
        HEADBASE.addOrReplaceChild("FRILL4ROT", CubeListBuilder.create().mirror()
                .texOffs(37, 22).addBox(-4.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(-1.0F, -2.0F, -2.5F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelMammothHead (128x64), root part CENTERROT. */
    static LayerDefinition mammoth() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        PartDefinition HEADROT = CENTERROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(58, 48).addBox(-2.5F, -6.0F, -3.5F, 5.0F, 2.0F, 3.0F)
                .texOffs(34, 48).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 4.0F)
                .texOffs(64, 53).addBox(-3.0F, -2.0F, -5.0F, 2.0F, 1.0F, 1.0F)
                .texOffs(64, 53).addBox(1.0F, -2.0F, -5.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 5.0F));
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
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelOstrichHead (64x64), root part BODYROT. */
    static LayerDefinition ostrich() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition BODYROT = root.addOrReplaceChild("BODYROT", CubeListBuilder.create().mirror()
                .texOffs(12, 25).addBox(-1.5F, -3.5F, -4.0F, 3.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 26.0F, -1.0F, -1.570796F, 0.0F, 0.0F));
        PartDefinition NECK1ROT = BODYROT.addOrReplaceChild("NECK1ROT", CubeListBuilder.create().mirror()
                .texOffs(22, 26).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, -4.0F));
        PartDefinition NECK3ROT = NECK1ROT.addOrReplaceChild("NECK3ROT", CubeListBuilder.create().mirror()
                .texOffs(22, 26).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, -2.0F));
        PartDefinition NECK4ROT = NECK3ROT.addOrReplaceChild("NECK4ROT", CubeListBuilder.create().mirror()
                .texOffs(22, 26).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, -2.0F));
        PartDefinition NECK5ROT = NECK4ROT.addOrReplaceChild("NECK5ROT", CubeListBuilder.create().mirror()
                .texOffs(22, 26).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, -2.0F));
        PartDefinition NECK6ROT = NECK5ROT.addOrReplaceChild("NECK6ROT", CubeListBuilder.create().mirror()
                .texOffs(22, 26).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, -2.0F));
        PartDefinition NECK7ROT = NECK6ROT.addOrReplaceChild("NECK7ROT", CubeListBuilder.create().mirror()
                .texOffs(22, 26).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, -2.0F));
        PartDefinition NECK8ROT = NECK7ROT.addOrReplaceChild("NECK8ROT", CubeListBuilder.create().mirror()
                .texOffs(22, 26).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, -2.0F));
        NECK8ROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(48, 0).addBox(-2.0F, -2.5F, -4.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(36, 0).addBox(-1.0F, -1.0F, -8.0F, 2.0F, 1.0F, 4.0F)
                .texOffs(38, 5).addBox(-1.0F, 0.0F, -7.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 1.570796F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** ModelPenguinHead (64x32), root part BODYROT. */
    static LayerDefinition penguin() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition BODYROT = root.addOrReplaceChild("BODYROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        BODYROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 5.0F, 6.0F)
                .texOffs(24, 0).addBox(-1.0F, -2.5F, -7.0F, 2.0F, 1.0F, 4.0F)
                .texOffs(24, 5).addBox(-0.5F, -1.5F, -6.0F, 1.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 1.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelRhinoHead (128x64), root part CENTERROT. */
    static LayerDefinition rhino() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        CENTERROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(102, 15).addBox(3.0F, -6.0F, -1.5F, 2.0F, 3.0F, 1.0F)
                .texOffs(96, 15).addBox(-5.0F, -6.0F, -1.5F, 2.0F, 3.0F, 1.0F)
                .texOffs(96, 0).addBox(-1.5F, -1.0F, -13.0F, 3.0F, 2.0F, 3.0F)
                .texOffs(96, 5).addBox(-1.0F, -4.0F, -13.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(96, 10).addBox(-0.5F, -8.0F, -13.0F, 1.0F, 4.0F, 1.0F)
                .texOffs(108, 0).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 10.0F, 2.0F)
                .texOffs(109, 12).addBox(-3.5F, -2.0F, -5.0F, 7.0F, 8.0F, 3.0F)
                .texOffs(109, 23).addBox(-3.0F, -1.0F, -9.0F, 6.0F, 7.0F, 4.0F)
                .texOffs(111, 34).addBox(-2.5F, 1.0F, -13.0F, 5.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 6.5F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelTreeEntHead (128x64), root part CENTERROT. */
    static LayerDefinition treeEnt() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition CENTERROT = root.addOrReplaceChild("CENTERROT", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition BODYROT = CENTERROT.addOrReplaceChild("BODYROT", CubeListBuilder.create().mirror()
                .texOffs(0, 28).addBox(-3.5F, -3.0F, 1.0F, 7.0F, 3.0F, 4.0F)
                .texOffs(0, 20).addBox(-4.5F, -3.0F, -3.0F, 9.0F, 3.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 2.5F));
        PartDefinition HEADROT = BODYROT.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(88, 0).addBox(-2.5F, -9.0F, -2.5F, 5.0F, 12.0F, 4.0F)
                .texOffs(98, 18).addBox(1.5F, -5.5F, -3.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(98, 18).addBox(-0.5F, -5.5F, -3.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(98, 18).addBox(-2.5F, -5.5F, -3.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(106, 17).addBox(-2.5F, -3.5F, -3.5F, 5.0F, 3.0F, 1.0F)
                .texOffs(106, 13).addBox(-2.5F, -7.5F, -3.5F, 5.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -3.0F, -4.5F));
        HEADROT.addOrReplaceChild("NOSEROT", CubeListBuilder.create().mirror()
                .texOffs(90, 16).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 1.0F, 2.0F)
                .texOffs(94, 20).addBox(-0.5F, 1.0F, -2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(90, 22).addBox(-1.0F, 2.0F, -3.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -3.0F, -3.0F));
        BODYROT.addOrReplaceChild("BACKTREESTEM5", CubeListBuilder.create().mirror()
                .texOffs(32, 28).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(23, 42).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(3.0F, -3.0F, 0.5F));
        BODYROT.addOrReplaceChild("BACKTREESTEM6", CubeListBuilder.create().mirror()
                .texOffs(23, 42).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 3.0F, 6.0F)
                .texOffs(32, 28).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offset(-3.0F, -3.0F, 0.5F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelVultureHead (64x32), root part BODYROT. */
    static LayerDefinition vulture() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition BODYROT = root.addOrReplaceChild("BODYROT", CubeListBuilder.create(),
                PartPose.offset(0.5F, 24.5F, 0.0F));
        PartDefinition NECKROT1 = BODYROT.addOrReplaceChild("NECKROT1", CubeListBuilder.create().mirror()
                .texOffs(8, 10).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 1.0F));
        PartDefinition NECKROT2 = NECKROT1.addOrReplaceChild("NECKROT2", CubeListBuilder.create().mirror()
                .texOffs(8, 10).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition NECKROT3 = NECKROT2.addOrReplaceChild("NECKROT3", CubeListBuilder.create().mirror()
                .texOffs(8, 10).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        NECKROT3.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(0, 25).addBox(-0.5F, 0.5F, -4.0F, 1.0F, 1.0F, 2.0F)
                .texOffs(6, 25).addBox(-1.0F, 0.5F, -5.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(0, 21).addBox(-1.0F, -0.5F, -5.0F, 2.0F, 1.0F, 3.0F)
                .texOffs(0, 16).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
}
