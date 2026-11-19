package bibliocraft.printing.client;

import bibliocraft.BiblioCraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * The ModelBase models of the printing group, boxes copied with tools/modelbase_to_layer.py. Their units: the desk
 * 1/32 (draw with scale 0.5), the press, its books and the ink plate 1/20 (0.8), the typesetting table 1/48 (1/3).
 * The press plate ("press") and the ink rollers are turned by the renderer (rotateAngleX), so their layers have no
 * rotation of their own.
 */
public final class PrintingModels {
    public static final ModelLayerLocation WRITING_DESK = new ModelLayerLocation(BiblioCraft.id("desk"), "main");
    public static final ModelLayerLocation PRINT_PRESS = new ModelLayerLocation(BiblioCraft.id("press"), "main");
    public static final ModelLayerLocation PRINT_PRESS_BOOKS = new ModelLayerLocation(BiblioCraft.id("press_books"), "main");
    public static final ModelLayerLocation INK_PLATE = new ModelLayerLocation(BiblioCraft.id("ink_plate"), "main");
    public static final ModelLayerLocation TYPESET = new ModelLayerLocation(BiblioCraft.id("typeset"), "main");
    public static final ModelLayerLocation TYPESET_BASE = new ModelLayerLocation(BiblioCraft.id("typeset_base"), "main");
    public static final ModelLayerLocation TYPESET_BOOK = new ModelLayerLocation(BiblioCraft.id("typeset_book"), "main");

    private PrintingModels() {
    }

    /** ModelWritingDesk (256 x 128). */
    public static LayerDefinition writingDesk() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("top", CubeListBuilder.create()
                .texOffs(122, 0).addBox(-16.0F, 0.0F, -16.0F, 32.0F, 4.0F, 32.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rightside", CubeListBuilder.create()
                .texOffs(57, 0).addBox(11.0F, 4.0F, -14.0F, 3.0F, 25.0F, 28.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rightfoot", CubeListBuilder.create()
                .texOffs(1, 31).addBox(10.0F, 29.0F, -15.0F, 5.0F, 3.0F, 30.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("leftfoot", CubeListBuilder.create()
                .texOffs(1, 31).addBox(-15.0F, 29.0F, -15.0F, 5.0F, 3.0F, 30.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("leftside", CubeListBuilder.create()
                .texOffs(57, 0).addBox(-14.0F, 4.0F, -14.0F, 3.0F, 25.0F, 28.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("back", CubeListBuilder.create()
                .texOffs(161, 64).addBox(-11.0F, 4.0F, 11.0F, 22.0F, 10.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("shelf", CubeListBuilder.create()
                .texOffs(138, 37).addBox(-11.0F, 11.0F, -13.0F, 22.0F, 1.0F, 24.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("wBookBack", CubeListBuilder.create()
                .texOffs(3, 114).addBox(-9.0F, -1.0F, -14.0F, 18.0F, 1.0F, 12.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("wBookRight", CubeListBuilder.create()
                .texOffs(105, 114).addBox(0.2F, -2.0F, -13.0F, 8.0F, 1.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("wBookLeft", CubeListBuilder.create()
                .texOffs(66, 114).addBox(-8.2F, -2.0F, -13.0F, 8.0F, 1.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rbook1", CubeListBuilder.create()
                .texOffs(1, 83).addBox(2.0F, -4.0F, 3.5F, 12.0F, 4.0F, 11.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("lbook1", CubeListBuilder.create()
                .texOffs(1, 99).addBox(-13.0F, -3.0F, 3.0F, 13.0F, 3.0F, 11.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.122173F, 0.0F));
        root.addOrReplaceChild("rbook2", CubeListBuilder.create()
                .texOffs(48, 83).addBox(1.0F, -7.0F, 4.0F, 12.0F, 3.0F, 11.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F));
        root.addOrReplaceChild("rbook3", CubeListBuilder.create()
                .texOffs(95, 84).addBox(2.0F, -10.0F, 4.0F, 12.0F, 3.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.0349066F, 0.0F));
        root.addOrReplaceChild("rbook4", CubeListBuilder.create()
                .texOffs(139, 84).addBox(1.0F, -14.0F, 4.0F, 13.0F, 4.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0349066F, 0.0F));
        root.addOrReplaceChild("lbook2", CubeListBuilder.create()
                .texOffs(50, 99).addBox(-14.0F, -7.0F, 2.866667F, 12.0F, 4.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0174533F, 0.0F));
        root.addOrReplaceChild("lbook3", CubeListBuilder.create()
                .texOffs(95, 99).addBox(-12.6F, -10.0F, 4.0F, 12.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1047198F, 0.0F));
        root.addOrReplaceChild("lbook4", CubeListBuilder.create()
                .texOffs(141, 99).addBox(-14.0F, -13.0F, 3.0F, 12.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0523599F, 0.0F));
        root.addOrReplaceChild("candlestick", CubeListBuilder.create()
                .texOffs(192, 95).addBox(-11.5F, -6.0F, -3.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("candleholder", CubeListBuilder.create()
                .texOffs(189, 102).addBox(-13.0F, -1.0F, -5.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("inkwell", CubeListBuilder.create()
                .texOffs(228, 101).addBox(11.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("pen", CubeListBuilder.create()
                .texOffs(230, 95).addBox(10.8F, -9.0F, -2.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3148822F));
        root.addOrReplaceChild("ring", CubeListBuilder.create()
                .texOffs(203, 97).addBox(-15.0F, -4.0F, -3.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    /** ModelPrintPress (128 x 64). */
    public static LayerDefinition printPress() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("LLeg", CubeListBuilder.create()
                .texOffs(31, 28).addBox(-9.0F, 4.0F, -8.0F, 3.0F, 19.0F, 16.0F),
                PartPose.offset(0.0F, 0.0F, -3.0F));
        root.addOrReplaceChild("RLeg", CubeListBuilder.create()
                .texOffs(31, 28).addBox(6.0F, 4.0F, -8.0F, 3.0F, 19.0F, 16.0F),
                PartPose.offset(0.0F, 0.0F, -3.0F));
        root.addOrReplaceChild("frontbrace", CubeListBuilder.create()
                .texOffs(95, 34).addBox(-6.0F, 8.0F, -10.0F, 12.0F, 11.0F, 4.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("bottombrace", CubeListBuilder.create()
                .texOffs(89, 50).addBox(-6.0F, 12.0F, -2.0F, 12.0F, 7.0F, 7.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("backbrace", CubeListBuilder.create()
                .texOffs(62, 34).addBox(-6.0F, 10.0F, 1.0F, 12.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("inkplateINK", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, 0.0F, 0.5F, 7.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2792527F, 0.0F, 0.0F));
        root.addOrReplaceChild("inkplate", CubeListBuilder.create()
                .texOffs(0, 9).addBox(-3.5F, 0.0F, 0.5F, 7.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2792527F, 0.0F, 0.0F));
        root.addOrReplaceChild("pback", CubeListBuilder.create()
                .texOffs(73, 0).addBox(-5.0F, 2.0F, 0.0F, 10.0F, 12.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2792527F, 0.0F, 0.0F));
        root.addOrReplaceChild("pibrace", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-1.0F, 1.0F, 1.0F, 2.0F, 6.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2792527F, 0.0F, 0.0F));
        root.addOrReplaceChild("table", CubeListBuilder.create()
                .texOffs(70, 24).addBox(-10.0F, 0.0F, -13.0F, 20.0F, 1.0F, 8.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        root.addOrReplaceChild("plate", CubeListBuilder.create()
                .texOffs(77, 14).addBox(-4.0F, 3.0F, -1.0F, 8.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2792527F, 0.0F, 0.0F));
        root.addOrReplaceChild("irshaft", CubeListBuilder.create()
                .texOffs(2, 42).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 4.0F, 5.0F));
        root.addOrReplaceChild("Bbook1", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-9.0F, -2.0F, -14.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0872665F, 0.0F));
        root.addOrReplaceChild("Bbook2", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-10.0F, -4.0F, -13.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, -0.0174533F, 0.0F));
        root.addOrReplaceChild("Bbook3", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-11.0F, -6.0F, -13.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, -0.122173F, 0.0F));
        root.addOrReplaceChild("Bbook4", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-9.0F, -8.0F, -14.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0872665F, 0.0F));
        root.addOrReplaceChild("Fbook", CubeListBuilder.create()
                .texOffs(96, 12).addBox(3.0F, -2.0F, -14.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, -0.0349066F, 0.0F));
        root.addOrReplaceChild("inkRollers", CubeListBuilder.create().mirror()
                .texOffs(9, 22).addBox(-6.0F, -7.0F, -2.5F, 1.0F, 1.0F, 4.0F)
                .texOffs(9, 22).addBox(5.0F, -7.0F, -2.5F, 1.0F, 1.0F, 4.0F)
                .texOffs(2, 22).addBox(5.0F, -6.0F, -1.5F, 1.0F, 10.0F, 2.0F)
                .texOffs(2, 22).addBox(-6.0F, -6.0F, -1.5F, 1.0F, 10.0F, 2.0F)
                .texOffs(1, 35).addBox(-5.0F, 2.0F, -2.0F, 10.0F, 3.0F, 3.0F)
                .texOffs(3, 19).addBox(-5.0F, -7.0F, 0.0F, 10.0F, 1.0F, 1.0F)
                .texOffs(3, 19).addBox(-5.0F, -7.0F, -2.0F, 10.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 4.0F, 6.0F));
        root.addOrReplaceChild("press", CubeListBuilder.create().mirror()
                .texOffs(50, 0).addBox(-5.0F, -14.0F, 0.5F, 10.0F, 10.0F, 1.0F)
                .texOffs(50, 12).addBox(-4.0F, -12.0F, -2.5F, 8.0F, 8.0F, 3.0F),
                PartPose.offset(0.0F, 15.5F, 1.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelPrintPressBooks (32 x 32). */
    public static LayerDefinition printPressBooks() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("Bbook1", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9.0F, -2.0F, -14.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0872665F, 0.0F));
        root.addOrReplaceChild("Bbook2", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-10.0F, -4.0F, -13.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, -0.0174533F, 0.0F));
        root.addOrReplaceChild("Bbook3", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11.0F, -6.0F, -13.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, -0.122173F, 0.0F));
        root.addOrReplaceChild("Bbook4", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9.0F, -8.0F, -14.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0872665F, 0.0F));
        root.addOrReplaceChild("Fbook", CubeListBuilder.create()
                .texOffs(0, 12).addBox(3.0F, -2.0F, -14.0F, 6.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, -0.0349066F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** ModelInkPlate (32 x 32). */
    public static LayerDefinition inkPlate() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("inkplate", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -0.0F, 0.5F, 7.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2792527F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** ModelTypeset (128 x 64). */
    public static LayerDefinition typeset() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("blPlate", CubeListBuilder.create()
                .texOffs(50, 0).addBox(-19.0F, 0.0F, -18.0F, 14.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-24.0F, 2.0F, -23.0F, 48.0F, 42.0F, 48.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("tlc1", CubeListBuilder.create()
                .texOffs(51, 20).addBox(-7.0F, 0.0F, 10.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("trc1", CubeListBuilder.create()
                .texOffs(51, 20).addBox(5.0F, 0.0F, 10.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("tbc1", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-5.0F, 0.0F, 10.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("ttc1", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-5.0F, 0.0F, 18.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("ttc2", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-6.0F, -2.0F, 19.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F));
        root.addOrReplaceChild("tlc2", CubeListBuilder.create()
                .texOffs(51, 20).addBox(-8.0F, -2.0F, 11.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F));
        root.addOrReplaceChild("tbc2", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-6.0F, -2.0F, 11.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F));
        root.addOrReplaceChild("trc2", CubeListBuilder.create()
                .texOffs(51, 20).addBox(4.0F, -2.0F, 11.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F));
        root.addOrReplaceChild("ttc3", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-4.0F, -4.0F, 19.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.0349066F, 0.0F));
        root.addOrReplaceChild("trc3", CubeListBuilder.create()
                .texOffs(51, 20).addBox(6.0F, -4.0F, 11.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.0349066F, 0.0F));
        root.addOrReplaceChild("tbc3", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-4.0F, -4.0F, 11.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.0349066F, 0.0F));
        root.addOrReplaceChild("tlc3", CubeListBuilder.create()
                .texOffs(51, 20).addBox(-6.0F, -4.0F, 11.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.0349066F, 0.0F));
        root.addOrReplaceChild("ttc4", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-5.0F, -6.0F, 18.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0174533F, 0.0F));
        root.addOrReplaceChild("trc4", CubeListBuilder.create()
                .texOffs(51, 20).addBox(5.0F, -6.0F, 10.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0174533F, 0.0F));
        root.addOrReplaceChild("tbc4", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-5.0F, -6.0F, 10.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0174533F, 0.0F));
        root.addOrReplaceChild("tlc4", CubeListBuilder.create()
                .texOffs(51, 20).addBox(-7.0F, -6.0F, 10.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0174533F, 0.0F));
        root.addOrReplaceChild("topedge", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-24.0F, 0.0F, 24.0F, 48.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("buttomedge", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-24.0F, 0.0F, -23.0F, 48.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("mid1edge", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-23.0F, 0.0F, -4.0F, 46.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("mid2edge", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-23.0F, 0.0F, 5.0F, 46.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("leftedge", CubeListBuilder.create()
                .texOffs(0, 8).addBox(-24.0F, 0.0F, -22.0F, 1.0F, 2.0F, 46.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rightedge", CubeListBuilder.create()
                .texOffs(0, 8).addBox(23.0F, 0.0F, -22.0F, 1.0F, 2.0F, 46.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("bcedge", CubeListBuilder.create()
                .texOffs(4, 22).addBox(-0.5F, 0.0F, -22.0F, 1.0F, 2.0F, 18.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("m2edge", CubeListBuilder.create()
                .texOffs(74, 42).addBox(-3.0F, 0.0F, -3.0F, 1.0F, 2.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("m3edge", CubeListBuilder.create()
                .texOffs(74, 42).addBox(2.0F, 0.0F, -3.0F, 1.0F, 2.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("m4edge", CubeListBuilder.create()
                .texOffs(74, 42).addBox(7.0F, 0.0F, -3.0F, 1.0F, 2.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("m1edge", CubeListBuilder.create()
                .texOffs(74, 42).addBox(-8.0F, 0.0F, -3.0F, 1.0F, 2.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("trcedge", CubeListBuilder.create()
                .texOffs(4, 22).addBox(9.0F, 0.0F, 6.0F, 1.0F, 2.0F, 18.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("tlcedge", CubeListBuilder.create()
                .texOffs(4, 22).addBox(-10.0F, 0.0F, 6.0F, 1.0F, 2.0F, 18.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("mcb1edge", CubeListBuilder.create()
                .texOffs(7, 47).addBox(-23.0F, 0.0F, -1.0F, 15.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("mcb2edge", CubeListBuilder.create()
                .texOffs(7, 47).addBox(-23.0F, 0.0F, 2.0F, 15.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("mcb3edge", CubeListBuilder.create()
                .texOffs(7, 47).addBox(8.0F, 0.0F, -1.0F, 15.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("mcb4edge", CubeListBuilder.create()
                .texOffs(7, 47).addBox(8.0F, 0.0F, 2.0F, 15.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("tl1edge", CubeListBuilder.create()
                .texOffs(9, 43).addBox(-23.0F, 0.0F, 20.0F, 13.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("tl2edge", CubeListBuilder.create()
                .texOffs(9, 43).addBox(-23.0F, 0.0F, 16.0F, 13.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("tr1edge", CubeListBuilder.create()
                .texOffs(9, 43).addBox(10.0F, 0.0F, 20.0F, 13.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("tr2edge", CubeListBuilder.create()
                .texOffs(9, 43).addBox(10.0F, 0.0F, 16.0F, 13.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("tlmc2edge", CubeListBuilder.create()
                .texOffs(50, 40).addBox(-15.0F, 0.0F, 6.0F, 1.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("tlmc1edge", CubeListBuilder.create()
                .texOffs(50, 40).addBox(-19.0F, 0.0F, 6.0F, 1.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("trmc2edge", CubeListBuilder.create()
                .texOffs(50, 40).addBox(18.0F, 0.0F, 6.0F, 1.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("trmc1edge", CubeListBuilder.create()
                .texOffs(50, 40).addBox(14.0F, 0.0F, 6.0F, 1.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("book", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-21.0F, -2.0F, -19.0F, 18.0F, 4.0F, 12.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("brPlate", CubeListBuilder.create()
                .texOffs(50, 0).addBox(5.0F, 0.0F, -18.0F, 14.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("blcRight", CubeListBuilder.create()
                .texOffs(51, 20).addBox(-7.0F, 0.0F, -18.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("blcLeft", CubeListBuilder.create()
                .texOffs(51, 20).addBox(-19.0F, 0.0F, -18.0F, 2.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("blcTop", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-17.0F, 0.0F, -10.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("blcBottom", CubeListBuilder.create()
                .texOffs(74, 26).addBox(-17.0F, 0.0F, -18.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelTypesetBase (256 x 128). */
    public static LayerDefinition typesetBase() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-24.0F, 2.0F, -23.0F, 48.0F, 42.0F, 48.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    /** ModelTypesetBook (128 x 32). */
    public static LayerDefinition typesetBook() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("book", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-22.5F, -3.0F, -20.5F, 21.0F, 5.0F, 15.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 32);
    }
}
