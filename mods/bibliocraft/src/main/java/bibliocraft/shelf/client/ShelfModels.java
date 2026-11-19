package bibliocraft.shelf.client;

import bibliocraft.BiblioCraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * ModelPotionShelf (128 x 64, drawn at scale 1/32), ModelGenericShelf (64 x 32) and ModelToolRack (64 x 64), boxes
 * copied with tools/modelbase_to_layer.py (setTextureSize and mirror came after addBox and changed nothing).
 */
public final class ShelfModels {
    public static final ModelLayerLocation POTION_SHELF = new ModelLayerLocation(BiblioCraft.id("potion_shelf"), "main");
    public static final ModelLayerLocation SHELF = new ModelLayerLocation(BiblioCraft.id("shelf"), "main");
    public static final ModelLayerLocation TOOL_RACK = new ModelLayerLocation(BiblioCraft.id("tool_rack"), "main");

    private ShelfModels() {
    }

    /** ModelPotionShelf.renderShelf: all five parts. */
    public static LayerDefinition potionShelf() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shelfTop", CubeListBuilder.create()
                .texOffs(39, 3).addBox(-15.0F, -8.0F, 2.0F, 30.0F, 1.0F, 13.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("shelfMiddle", CubeListBuilder.create()
                .texOffs(39, 19).addBox(-15.0F, 3.0F, 2.0F, 30.0F, 1.0F, 13.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("shelfBottom", CubeListBuilder.create()
                .texOffs(39, 35).addBox(-15.0F, 14.0F, 2.0F, 30.0F, 1.0F, 13.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("leftbig", CubeListBuilder.create()
                .texOffs(0, 3).addBox(15.0F, -17.0F, 0.0F, 1.0F, 32.0F, 16.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rightbig", CubeListBuilder.create()
                .texOffs(0, 3).addBox(-16.0F, -17.0F, 0.0F, 1.0F, 32.0F, 16.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** ModelGenericShelf: renderShelf = topshelf, bottomshelf, centerBottom; renderTop = centerTop. */
    public static LayerDefinition shelf() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("topshelf", CubeListBuilder.create()
                .texOffs(15, 1).addBox(-8.0F, 7.0F, 0.0F, 16.0F, 1.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("bottomshelf", CubeListBuilder.create()
                .texOffs(15, 12).addBox(-8.0F, 15.0F, 0.0F, 16.0F, 1.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("centerBottom", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, 8.0F, 0.0F, 1.0F, 7.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("centerTop", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 7.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelToolRack.renderRack: all parts. */
    public static LayerDefinition toolRack() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right", CubeListBuilder.create()
                .texOffs(13, 1).addBox(0.0F, 0.0F, -8.0F, 8.0F, 16.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("leg", CubeListBuilder.create()
                .texOffs(13, 1).addBox(0.0F, 0.0F, 7.0F, 8.0F, 16.0F, 1.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("panel", CubeListBuilder.create()
                .texOffs(1, 35).addBox(2.0F, -6.0F, -7.0F, 6.0F, 14.0F, 14.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("top", CubeListBuilder.create()
                .texOffs(0, 19).addBox(0.0F, 0.0F, -7.0F, 8.0F, 1.0F, 14.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("bottom", CubeListBuilder.create()
                .texOffs(0, 19).addBox(0.0F, 0.0F, -7.0F, 8.0F, 1.0F, 14.0F),
                PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("peg0", CubeListBuilder.create()
                .texOffs(4, 8).addBox(1.0F, -0.5F, 4.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("peg1", CubeListBuilder.create()
                .texOffs(4, 8).addBox(1.0F, -2.4F, 1.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("peg2", CubeListBuilder.create()
                .texOffs(4, 8).addBox(1.0F, -0.5F, -2.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("peg3", CubeListBuilder.create()
                .texOffs(4, 8).addBox(1.0F, -2.4F, -4.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("peg4", CubeListBuilder.create()
                .texOffs(4, 8).addBox(1.0F, 5.5F, 4.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("peg5", CubeListBuilder.create()
                .texOffs(4, 8).addBox(1.0F, 3.6F, 1.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("peg6", CubeListBuilder.create()
                .texOffs(4, 8).addBox(1.0F, 5.5F, -2.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("peg7", CubeListBuilder.create()
                .texOffs(4, 8).addBox(1.0F, 3.6F, -4.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
