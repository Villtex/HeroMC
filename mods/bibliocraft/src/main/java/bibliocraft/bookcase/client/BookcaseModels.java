package bibliocraft.bookcase.client;

import bibliocraft.BiblioCraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * ModelBookcaseEmpty (128 x 64) and ModelBookcaseBooks (128 x 128, parts Book1..Book16), boxes copied with
 * tools/modelbase_to_layer.py. Their unit is half a pixel (rendered at scale 1/32): draw with an extra scale of 0.5.
 */
public final class BookcaseModels {
    public static final ModelLayerLocation SHELF = new ModelLayerLocation(BiblioCraft.id("bookcase"), "shelf");
    public static final ModelLayerLocation BOOKS = new ModelLayerLocation(BiblioCraft.id("bookcase"), "books");

    private BookcaseModels() {
    }

    public static LayerDefinition shelf() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("back", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-30.0F, -30.0F, -32.0F, 30.0F, 30.0F, 1.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("topshelf", CubeListBuilder.create()
                .texOffs(-1, 31).addBox(-30.0F, -31.0F, -32.0F, 30.0F, 1.0F, 16.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("midshelf", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-30.0F, -15.0F, -31.0F, 30.0F, 1.0F, 15.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("bottomshelf", CubeListBuilder.create()
                .texOffs(-1, 31).addBox(-30.0F, 0.0F, -32.0F, 30.0F, 1.0F, 16.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("leftside", CubeListBuilder.create()
                .texOffs(93, 0).addBox(0.0F, -31.0F, -32.0F, 1.0F, 32.0F, 16.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("rightside", CubeListBuilder.create()
                .texOffs(93, 0).addBox(-31.0F, -31.0F, -32.0F, 1.0F, 32.0F, 16.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    public static LayerDefinition books() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("Book1", CubeListBuilder.create()
                .texOffs(0, 2).addBox(-30.0F, -14.0F, -30.0F, 4.0F, 13.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book2", CubeListBuilder.create()
                .texOffs(32, 2).addBox(-26.0F, -14.0F, -31.0F, 3.0F, 12.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book3", CubeListBuilder.create()
                .texOffs(62, 2).addBox(-23.0F, -14.0F, -30.0F, 4.0F, 12.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book4", CubeListBuilder.create()
                .texOffs(94, 2).addBox(-19.0F, -14.0F, -30.0F, 4.0F, 13.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book5", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-15.0F, -14.0F, -29.0F, 4.0F, 13.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book6", CubeListBuilder.create()
                .texOffs(32, 30).addBox(-11.0F, -14.0F, -30.0F, 4.0F, 12.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book7", CubeListBuilder.create()
                .texOffs(64, 30).addBox(-7.0F, -14.0F, -29.0F, 3.0F, 13.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book8", CubeListBuilder.create()
                .texOffs(94, 30).addBox(-4.0F, -14.0F, -30.0F, 4.0F, 13.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book9", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-30.0F, -30.0F, -30.0F, 4.0F, 13.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book10", CubeListBuilder.create()
                .texOffs(32, 58).addBox(-26.0F, -30.0F, -30.0F, 4.0F, 14.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book11", CubeListBuilder.create()
                .texOffs(64, 58).addBox(-22.0F, -30.0F, -31.0F, 3.0F, 13.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book12", CubeListBuilder.create()
                .texOffs(94, 58).addBox(-19.0F, -30.0F, -30.0F, 3.0F, 12.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book13", CubeListBuilder.create()
                .texOffs(0, 87).addBox(-16.0F, -30.0F, -29.0F, 4.0F, 14.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book14", CubeListBuilder.create()
                .texOffs(32, 87).addBox(-12.0F, -30.0F, -30.0F, 4.0F, 13.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book15", CubeListBuilder.create()
                .texOffs(64, 87).addBox(-8.0F, -30.0F, -29.0F, 4.0F, 12.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        root.addOrReplaceChild("Book16", CubeListBuilder.create()
                .texOffs(96, 87).addBox(-4.0F, -30.0F, -30.0F, 4.0F, 13.0F, 12.0F),
                PartPose.offset(15.0F, 15.0F, 16.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
