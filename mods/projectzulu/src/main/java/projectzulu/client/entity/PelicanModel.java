package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import projectzulu.entity.AerialAnimal.EntityState;

/**
 * ModelPelican: built like the eagle with a long neck of ten segments and a beak pouch. Grounded it folds its wings
 * and walks (legs, neck and head swing with the walk animation); in the air the wings flap with the world time,
 * attacking it pitches forward and stretches neck and legs.
 */
public class PelicanModel extends EntityModel<BirdRenderState> {
    private final ModelPart bodyrot;
    private final ModelPart tailbaserot;
    private final ModelPart tailfeather3rot;
    private final ModelPart tailfeather1rot;
    private final ModelPart tailfeather5rot;
    private final ModelPart tailfeather2rot;
    private final ModelPart tailfeather4rot;
    private final ModelPart neck1rot;
    private final ModelPart neck2rot;
    private final ModelPart neck3rot;
    private final ModelPart neck4rot;
    private final ModelPart neck5rot;
    private final ModelPart neck6rot;
    private final ModelPart neck7rot;
    private final ModelPart neck8rot;
    private final ModelPart neck9rot;
    private final ModelPart neck10rot;
    private final ModelPart headrot;
    private final ModelPart horn2rot;
    private final ModelPart winglefrot1;
    private final ModelPart winglefrot2;
    private final ModelPart winglefrot3;
    private final ModelPart winglefrot4;
    private final ModelPart winglefrot5;
    private final ModelPart winglefrot6;
    private final ModelPart winglefrot7;
    private final ModelPart winglefrot8;
    private final ModelPart winglefrot9;
    private final ModelPart winglefrot10;
    private final ModelPart winglefrot11;
    private final ModelPart winglefrot12;
    private final ModelPart wingrigrot1;
    private final ModelPart wingrigrot2;
    private final ModelPart wingrigrot3;
    private final ModelPart wingrigrot4;
    private final ModelPart wingrigrot5;
    private final ModelPart wingrigrot6;
    private final ModelPart wingrigrot7;
    private final ModelPart wingrigrot8;
    private final ModelPart wingrigrot9;
    private final ModelPart wingrigrot10;
    private final ModelPart wingrigrot11;
    private final ModelPart wingrigrot12;
    private final ModelPart legrigtoprot;
    private final ModelPart legrigbotrot;
    private final ModelPart footrig1rot;
    private final ModelPart footrig2rot;
    private final ModelPart footrig3rot;
    private final ModelPart legleftoprot;
    private final ModelPart leglefbotrot;
    private final ModelPart footlef1rot;
    private final ModelPart footlef2rot;
    private final ModelPart footlef3rot;

    public PelicanModel(ModelPart root) {
        super(root);
        this.bodyrot = root.getChild("bodyrot");
        this.tailbaserot = this.bodyrot.getChild("tailbaserot");
        this.tailfeather3rot = this.tailbaserot.getChild("tailfeather3rot");
        this.tailfeather1rot = this.tailbaserot.getChild("tailfeather1rot");
        this.tailfeather5rot = this.tailbaserot.getChild("tailfeather5rot");
        this.tailfeather2rot = this.tailbaserot.getChild("tailfeather2rot");
        this.tailfeather4rot = this.tailbaserot.getChild("tailfeather4rot");
        this.neck1rot = this.bodyrot.getChild("neck1rot");
        this.neck2rot = this.neck1rot.getChild("neck2rot");
        this.neck3rot = this.neck2rot.getChild("neck3rot");
        this.neck4rot = this.neck3rot.getChild("neck4rot");
        this.neck5rot = this.neck4rot.getChild("neck5rot");
        this.neck6rot = this.neck5rot.getChild("neck6rot");
        this.neck7rot = this.neck6rot.getChild("neck7rot");
        this.neck8rot = this.neck7rot.getChild("neck8rot");
        this.neck9rot = this.neck8rot.getChild("neck9rot");
        this.neck10rot = this.neck9rot.getChild("neck10rot");
        this.headrot = this.neck10rot.getChild("headrot");
        this.horn2rot = this.headrot.getChild("horn2rot");
        this.winglefrot1 = this.bodyrot.getChild("winglefrot1");
        this.winglefrot2 = this.winglefrot1.getChild("winglefrot2");
        this.winglefrot3 = this.winglefrot2.getChild("winglefrot3");
        this.winglefrot4 = this.winglefrot3.getChild("winglefrot4");
        this.winglefrot5 = this.winglefrot4.getChild("winglefrot5");
        this.winglefrot6 = this.winglefrot5.getChild("winglefrot6");
        this.winglefrot7 = this.winglefrot6.getChild("winglefrot7");
        this.winglefrot8 = this.winglefrot7.getChild("winglefrot8");
        this.winglefrot9 = this.winglefrot8.getChild("winglefrot9");
        this.winglefrot10 = this.winglefrot9.getChild("winglefrot10");
        this.winglefrot11 = this.winglefrot10.getChild("winglefrot11");
        this.winglefrot12 = this.winglefrot11.getChild("winglefrot12");
        this.wingrigrot1 = this.bodyrot.getChild("wingrigrot1");
        this.wingrigrot2 = this.wingrigrot1.getChild("wingrigrot2");
        this.wingrigrot3 = this.wingrigrot2.getChild("wingrigrot3");
        this.wingrigrot4 = this.wingrigrot3.getChild("wingrigrot4");
        this.wingrigrot5 = this.wingrigrot4.getChild("wingrigrot5");
        this.wingrigrot6 = this.wingrigrot5.getChild("wingrigrot6");
        this.wingrigrot7 = this.wingrigrot6.getChild("wingrigrot7");
        this.wingrigrot8 = this.wingrigrot7.getChild("wingrigrot8");
        this.wingrigrot9 = this.wingrigrot8.getChild("wingrigrot9");
        this.wingrigrot10 = this.wingrigrot9.getChild("wingrigrot10");
        this.wingrigrot11 = this.wingrigrot10.getChild("wingrigrot11");
        this.wingrigrot12 = this.wingrigrot11.getChild("wingrigrot12");
        this.legrigtoprot = this.bodyrot.getChild("legrigtoprot");
        this.legrigbotrot = this.legrigtoprot.getChild("legrigbotrot");
        this.footrig1rot = this.legrigbotrot.getChild("footrig1rot");
        this.footrig2rot = this.legrigbotrot.getChild("footrig2rot");
        this.footrig3rot = this.legrigbotrot.getChild("footrig3rot");
        this.legleftoprot = this.bodyrot.getChild("legleftoprot");
        this.leglefbotrot = this.legleftoprot.getChild("leglefbotrot");
        this.footlef1rot = this.leglefbotrot.getChild("footlef1rot");
        this.footlef2rot = this.leglefbotrot.getChild("footlef2rot");
        this.footlef3rot = this.leglefbotrot.getChild("footlef3rot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bodyrot = root.addOrReplaceChild("bodyrot", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-2.0F, -5.0F, -5.5F, 4, 5, 7).texOffs(0, 14).addBox(-2.0F, -5.0F, -6.5F, 4, 4, 1).texOffs(0, 3).addBox(-2.0F, -5.0F, 1.5F, 4, 4, 1).texOffs(0, 8).addBox(-1.5F, -5.0F, 2.5F, 3, 3, 1),
                PartPose.offset(0.0F, 8.0F, 2.5F));
        PartDefinition tailbaserot = bodyrot.addOrReplaceChild("tailbaserot", CubeListBuilder.create().texOffs(8, 8).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 2, 1),
                PartPose.offset(0.0F, -3.5F, 3.5F));
        tailbaserot.addOrReplaceChild("tailfeather3rot", CubeListBuilder.create().texOffs(9, 5).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6),
                PartPose.offset(0.0F, 0.0F, 1.0F));
        tailbaserot.addOrReplaceChild("tailfeather1rot", CubeListBuilder.create().texOffs(9, 5).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6),
                PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.0F, 0.7853981633974483F, 0.0F));
        tailbaserot.addOrReplaceChild("tailfeather5rot", CubeListBuilder.create().texOffs(9, 5).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 1.0F, 0.0F, -0.7853981633974483F, 0.0F));
        tailbaserot.addOrReplaceChild("tailfeather2rot", CubeListBuilder.create().texOffs(9, 5).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6),
                PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.5235987755982988F, 0.0F, 0.0F));
        tailbaserot.addOrReplaceChild("tailfeather4rot", CubeListBuilder.create().texOffs(9, 5).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6),
                PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition neck1rot = bodyrot.addOrReplaceChild("neck1rot", CubeListBuilder.create().texOffs(41, 0).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 3, 1),
                PartPose.offset(0.0F, -3.5F, -6.5F));
        PartDefinition neck2rot = neck1rot.addOrReplaceChild("neck2rot", CubeListBuilder.create().texOffs(37, 4).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck3rot = neck2rot.addOrReplaceChild("neck3rot", CubeListBuilder.create().texOffs(38, 4).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck4rot = neck3rot.addOrReplaceChild("neck4rot", CubeListBuilder.create().texOffs(39, 4).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck5rot = neck4rot.addOrReplaceChild("neck5rot", CubeListBuilder.create().texOffs(40, 4).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck6rot = neck5rot.addOrReplaceChild("neck6rot", CubeListBuilder.create().texOffs(37, 4).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck7rot = neck6rot.addOrReplaceChild("neck7rot", CubeListBuilder.create().texOffs(38, 4).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck8rot = neck7rot.addOrReplaceChild("neck8rot", CubeListBuilder.create().texOffs(39, 4).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck9rot = neck8rot.addOrReplaceChild("neck9rot", CubeListBuilder.create().texOffs(40, 4).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck10rot = neck9rot.addOrReplaceChild("neck10rot", CubeListBuilder.create().texOffs(37, 4).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition headrot = neck10rot.addOrReplaceChild("headrot", CubeListBuilder.create().texOffs(10, 12).mirror().addBox(-2.0F, -1.0F, -2.0F, 4, 4, 3).texOffs(46, 8).addBox(-1.5F, -0.5F, -8.0F, 3, 1, 6).texOffs(56, 26).addBox(-0.5F, 2.5F, -5.0F, 1, 1, 3).texOffs(48, 15).addBox(-1.5F, 0.5F, -7.0F, 3, 1, 5).texOffs(52, 21).addBox(-1.0F, 1.5F, -6.0F, 2, 1, 4),
                PartPose.offset(0.0F, -1.0F, -2.0F));
        headrot.addOrReplaceChild("horn2rot", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -2.5F, -3.0F, -0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition winglefrot1 = bodyrot.addOrReplaceChild("winglefrot1", CubeListBuilder.create().texOffs(22, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-2.5F, -3.5F, -5.5F));
        PartDefinition winglefrot2 = winglefrot1.addOrReplaceChild("winglefrot2", CubeListBuilder.create().texOffs(23, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot3 = winglefrot2.addOrReplaceChild("winglefrot3", CubeListBuilder.create().texOffs(24, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot4 = winglefrot3.addOrReplaceChild("winglefrot4", CubeListBuilder.create().texOffs(25, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F));
        PartDefinition winglefrot5 = winglefrot4.addOrReplaceChild("winglefrot5", CubeListBuilder.create().texOffs(22, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot6 = winglefrot5.addOrReplaceChild("winglefrot6", CubeListBuilder.create().texOffs(23, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot7 = winglefrot6.addOrReplaceChild("winglefrot7", CubeListBuilder.create().texOffs(24, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot8 = winglefrot7.addOrReplaceChild("winglefrot8", CubeListBuilder.create().texOffs(25, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot9 = winglefrot8.addOrReplaceChild("winglefrot9", CubeListBuilder.create().texOffs(23, 6).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot10 = winglefrot9.addOrReplaceChild("winglefrot10", CubeListBuilder.create().texOffs(23, 14).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot11 = winglefrot10.addOrReplaceChild("winglefrot11", CubeListBuilder.create().texOffs(23, 6).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        winglefrot11.addOrReplaceChild("winglefrot12", CubeListBuilder.create().texOffs(23, 14).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot1 = bodyrot.addOrReplaceChild("wingrigrot1", CubeListBuilder.create().texOffs(22, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(2.5F, -3.5F, -5.5F));
        PartDefinition wingrigrot2 = wingrigrot1.addOrReplaceChild("wingrigrot2", CubeListBuilder.create().texOffs(23, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot3 = wingrigrot2.addOrReplaceChild("wingrigrot3", CubeListBuilder.create().texOffs(24, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot4 = wingrigrot3.addOrReplaceChild("wingrigrot4", CubeListBuilder.create().texOffs(25, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offsetAndRotation(1.0F, 0.0F, 0.0F, 0.0F, 0.0698132F, 0.0F));
        PartDefinition wingrigrot5 = wingrigrot4.addOrReplaceChild("wingrigrot5", CubeListBuilder.create().texOffs(22, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot6 = wingrigrot5.addOrReplaceChild("wingrigrot6", CubeListBuilder.create().texOffs(23, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot7 = wingrigrot6.addOrReplaceChild("wingrigrot7", CubeListBuilder.create().texOffs(24, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot8 = wingrigrot7.addOrReplaceChild("wingrigrot8", CubeListBuilder.create().texOffs(25, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot9 = wingrigrot8.addOrReplaceChild("wingrigrot9", CubeListBuilder.create().texOffs(23, 6).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot10 = wingrigrot9.addOrReplaceChild("wingrigrot10", CubeListBuilder.create().texOffs(23, 14).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot11 = wingrigrot10.addOrReplaceChild("wingrigrot11", CubeListBuilder.create().texOffs(23, 6).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        wingrigrot11.addOrReplaceChild("wingrigrot12", CubeListBuilder.create().texOffs(23, 14).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition legrigtoprot = bodyrot.addOrReplaceChild("legrigtoprot", CubeListBuilder.create().texOffs(28, 0).mirror().addBox(-0.5F, -1.0F, -0.5F, 1, 3, 1),
                PartPose.offset(1.5F, 1.0F, 0.0F));
        PartDefinition legrigbotrot = legrigtoprot.addOrReplaceChild("legrigbotrot", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        legrigbotrot.addOrReplaceChild("footrig1rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -1.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 2.5F, -0.5F, 0.0F, 0.4363323F, 0.0F));
        legrigbotrot.addOrReplaceChild("footrig2rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -1.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 2.5F, -0.5F, 0.0F, -0.4363323F, 0.0F));
        legrigbotrot.addOrReplaceChild("footrig3rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, 2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, 1.0F, 1, 1, 1),
                PartPose.offset(0.0F, 2.5F, 0.5F));
        PartDefinition legleftoprot = bodyrot.addOrReplaceChild("legleftoprot", CubeListBuilder.create().texOffs(28, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1),
                PartPose.offset(-1.5F, 0.0F, 0.0F));
        PartDefinition leglefbotrot = legleftoprot.addOrReplaceChild("leglefbotrot", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        leglefbotrot.addOrReplaceChild("footlef1rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -1.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 2.5F, -0.5F, 0.0F, 0.4363323F, 0.0F));
        leglefbotrot.addOrReplaceChild("footlef2rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -1.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 2.5F, -0.5F, 0.0F, -0.4363323F, 0.0F));
        leglefbotrot.addOrReplaceChild("footlef3rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, 2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, 1.0F, 1, 1, 1),
                PartPose.offset(0.0F, 2.5F, 0.5F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** setLivingAnimations followed by setRotationAngles of the original. */
    @Override
    public void setupAnim(BirdRenderState state) {
        super.setupAnim(state);
        if (state.grounded) {
            this.headrot.xRot = (float)((Math.PI / 6) - 20.0F * state.walkAnimationSpeed * Math.PI / 180.0 * Mth.sin(state.walkAnimationPos * 0.6662F * 1.0F));
            this.bodyrot.xRot = (float) (-Math.PI / 6);
            this.winglefrot1.xRot = (float) (Math.PI / 18);
            this.wingrigrot1.xRot = (float) (Math.PI / 18);
            this.neck1rot.xRot = (float)((Math.PI / 18) + 20.0F * state.walkAnimationSpeed * Math.PI / 180.0 * Mth.sin(state.walkAnimationPos * 0.6662F * 1.0F));
            this.neck2rot.xRot = (float) (-Math.PI * 2.0 / 9.0);
            this.neck3rot.xRot = (float) (-Math.PI * 2.0 / 9.0);
            this.neck4rot.xRot = (float) (-Math.PI / 6);
            this.neck5rot.xRot = (float) (Math.PI / 18);
            this.neck6rot.xRot = (float) (Math.PI / 18);
            this.neck7rot.xRot = 0.43633232F;
            this.neck8rot.xRot = 0.61086524F;
            this.neck9rot.xRot = (float) (Math.PI / 4);
            this.neck10rot.xRot = 0.0F;
            this.legleftoprot.xRot = (float)((Math.PI / 18) + 40.0F * state.walkAnimationSpeed * Math.PI / 180.0 * Mth.sin(state.walkAnimationPos * 0.6662F * 1.0F));
            this.legleftoprot.yRot = 0.0F;
            this.leglefbotrot.xRot = (float)((Math.PI / 9) + 20.0F * state.walkAnimationSpeed * Math.PI / 180.0 * Mth.sin(state.walkAnimationPos * 0.6662F * 1.0F));
            this.footlef1rot.xRot = 0.0F;
            this.footlef2rot.xRot = 0.0F;
            this.footlef3rot.xRot = 0.0F;
            this.legrigtoprot.xRot = (float)(
                (Math.PI / 18) + 20.0F * state.walkAnimationSpeed * Math.PI / 180.0 * Mth.sin((float)(state.walkAnimationPos * 0.6662F * 1.0F + Math.PI))
            );
            this.legrigtoprot.yRot = 0.0F;
            this.legrigbotrot.xRot = (float)(
                (Math.PI / 9) + 10.0F * state.walkAnimationSpeed * Math.PI / 180.0 * Mth.sin((float)(state.walkAnimationPos * 0.6662F * 1.0F + Math.PI))
            );
            this.footrig1rot.xRot = 0.0F;
            this.footrig2rot.xRot = 0.0F;
            this.footrig3rot.xRot = 0.0F;
            this.winglefrot1.zRot = (float) (Math.PI / 2);
            this.winglefrot2.zRot = 0.0F;
            this.winglefrot3.zRot = -0.43633232F;
            this.winglefrot4.zRot = -0.43633232F;
            this.winglefrot5.zRot = -0.43633232F;
            this.winglefrot6.zRot = (float) (-Math.PI / 3);
            this.winglefrot7.zRot = (float) (-Math.PI / 3);
            this.winglefrot8.zRot = (float) (-Math.PI / 3);
            this.winglefrot8.zRot = 0.0F;
            this.winglefrot10.zRot = 0.0F;
            this.winglefrot11.zRot = 0.0F;
            this.winglefrot12.zRot = 0.0F;
            this.winglefrot1.xRot = 0.08726646F;
            this.winglefrot2.xRot = 0.08726646F;
            this.winglefrot3.xRot = 0.08726646F;
            this.winglefrot4.xRot = 0.08726646F;
            this.winglefrot5.xRot = 0.08726646F;
            this.winglefrot6.xRot = 0.08726646F;
            this.winglefrot7.xRot = 0.08726646F;
            this.winglefrot8.xRot = 0.08726646F;
            this.winglefrot8.xRot = 0.08726646F;
            this.winglefrot10.xRot = 0.08726646F;
            this.winglefrot11.xRot = 0.08726646F;
            this.winglefrot12.xRot = 0.08726646F;
            this.winglefrot5.yRot = 0.0F;
            this.winglefrot8.yRot = 0.0F;
            this.winglefrot11.yRot = 0.0F;
            this.wingrigrot1.zRot = (float) (-Math.PI / 2);
            this.wingrigrot2.zRot = 0.0F;
            this.wingrigrot3.zRot = 0.43633232F;
            this.wingrigrot4.zRot = 0.43633232F;
            this.wingrigrot5.zRot = 0.43633232F;
            this.wingrigrot6.zRot = (float) (Math.PI / 3);
            this.wingrigrot7.zRot = (float) (Math.PI / 3);
            this.wingrigrot8.zRot = (float) (Math.PI / 3);
            this.wingrigrot8.zRot = 0.0F;
            this.wingrigrot10.zRot = 0.0F;
            this.wingrigrot11.zRot = 0.0F;
            this.wingrigrot12.zRot = 0.0F;
            this.wingrigrot1.xRot = 0.08726646F;
            this.wingrigrot2.xRot = 0.08726646F;
            this.wingrigrot3.xRot = 0.08726646F;
            this.wingrigrot4.xRot = 0.08726646F;
            this.wingrigrot5.xRot = 0.08726646F;
            this.wingrigrot6.xRot = 0.08726646F;
            this.wingrigrot7.xRot = 0.08726646F;
            this.wingrigrot8.xRot = 0.08726646F;
            this.wingrigrot8.xRot = 0.08726646F;
            this.wingrigrot10.xRot = 0.08726646F;
            this.wingrigrot11.xRot = 0.08726646F;
            this.wingrigrot12.xRot = 0.08726646F;
            this.wingrigrot1.yRot = 0.0F;
            this.wingrigrot5.yRot = 0.0F;
            this.wingrigrot8.yRot = 0.0F;
            this.wingrigrot11.yRot = 0.0F;
            this.tailfeather1rot.yRot = (float)((Math.PI / 6) - (Math.PI / 18) * state.walkAnimationSpeed);
            this.tailfeather5rot.yRot = (float)((-Math.PI / 6) + (Math.PI / 18) * state.walkAnimationSpeed);
        } else if (state.entityState == EntityState.ATTACKING) {
            this.bodyrot.xRot = (float) (-Math.PI / 6);
            this.winglefrot1.xRot = (float) (Math.PI / 18);
            this.wingrigrot1.xRot = (float) (Math.PI / 18);
            this.neck1rot.xRot = (float) (Math.PI / 12);
            this.neck2rot.xRot = (float) (Math.PI / 12);
            this.neck3rot.xRot = (float) (Math.PI / 12);
            this.neck4rot.xRot = (float) (Math.PI / 12);
            this.neck5rot.xRot = (float) (-Math.PI / 12);
            this.neck6rot.xRot = (float) (-Math.PI / 12);
            this.neck7rot.xRot = 0.0F;
            this.neck8rot.xRot = 0.0F;
            this.neck9rot.xRot = 0.0F;
            this.neck10rot.xRot = 0.0F;
            this.legleftoprot.xRot = (float) (-Math.PI / 9);
            this.legleftoprot.yRot = 0.0F;
            this.leglefbotrot.xRot = (float) (Math.PI / 3);
            this.footlef1rot.xRot = (float)(
                (Math.PI / 4) + (Math.PI / 18) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.footlef2rot.xRot = (float)(
                (Math.PI / 4) + (Math.PI / 18) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.footlef3rot.xRot = (float)(
                (-Math.PI / 4) - (Math.PI / 18) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.legrigtoprot.xRot = (float) (-Math.PI / 9);
            this.legrigtoprot.yRot = 0.0F;
            this.legrigbotrot.xRot = (float) (Math.PI / 3);
            this.footrig1rot.xRot = (float)(
                (Math.PI / 4) + (Math.PI / 18) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.footrig2rot.xRot = (float)(
                (Math.PI / 4) + (Math.PI / 18) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.footrig3rot.xRot = (float)(
                (-Math.PI / 4) - (Math.PI / 18) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.winglefrot1.zRot = (float)(
                (Math.PI * 2.0 / 9.0) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.winglefrot2.zRot = 0.0F;
            this.winglefrot3.zRot = 0.0F;
            this.winglefrot4.zRot = 0.0F;
            this.winglefrot5.zRot = 0.0F;
            this.winglefrot6.zRot = (float)(
                (-Math.PI / 9)
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot7.zRot = 0.0F;
            this.winglefrot8.zRot = 0.0F;
            this.winglefrot8.zRot = 0.0F;
            this.winglefrot10.zRot = (float)(
                -0.4363323129985824
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot11.zRot = 0.0F;
            this.winglefrot12.zRot = 0.0F;
            this.winglefrot1.xRot = 0.0F;
            this.winglefrot2.xRot = 0.0F;
            this.winglefrot3.xRot = 0.0F;
            this.winglefrot4.xRot = 0.0F;
            this.winglefrot5.xRot = 0.0F;
            this.winglefrot6.xRot = 0.0F;
            this.winglefrot7.xRot = 0.0F;
            this.winglefrot8.xRot = 0.0F;
            this.winglefrot8.xRot = 0.0F;
            this.winglefrot10.xRot = 0.0F;
            this.winglefrot11.xRot = 0.0F;
            this.winglefrot12.xRot = 0.0F;
            this.winglefrot1.yRot = (float)(
                -0.06981317007977318
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot5.yRot = (float)(
                -0.08726646259971647
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot8.yRot = (float)(
                -0.10471975511965977
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot11.yRot = (float)(
                -0.12217304763960307
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot1.zRot = (float)(
                (-Math.PI * 2.0 / 9.0) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.wingrigrot2.zRot = 0.0F;
            this.wingrigrot3.zRot = 0.0F;
            this.wingrigrot4.zRot = 0.0F;
            this.wingrigrot5.zRot = 0.0F;
            this.wingrigrot6.zRot = (float)(
                (Math.PI / 9)
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot7.zRot = 0.0F;
            this.wingrigrot8.zRot = 0.0F;
            this.wingrigrot8.zRot = 0.0F;
            this.wingrigrot10.zRot = (float)(
                0.4363323129985824
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot11.zRot = 0.0F;
            this.wingrigrot12.zRot = 0.0F;
            this.wingrigrot1.xRot = 0.0F;
            this.wingrigrot2.xRot = 0.0F;
            this.wingrigrot3.xRot = 0.0F;
            this.wingrigrot4.xRot = 0.0F;
            this.wingrigrot5.xRot = 0.0F;
            this.wingrigrot6.xRot = 0.0F;
            this.wingrigrot7.xRot = 0.0F;
            this.wingrigrot8.xRot = 0.0F;
            this.wingrigrot8.xRot = 0.0F;
            this.wingrigrot10.xRot = 0.0F;
            this.wingrigrot11.xRot = 0.0F;
            this.wingrigrot12.xRot = 0.0F;
            this.wingrigrot1.yRot = (float)(
                0.06981317007977318
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot5.yRot = (float)(
                0.08726646259971647
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot8.yRot = (float)(
                0.10471975511965977
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot11.yRot = (float)(
                0.12217304763960307
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.tailfeather1rot.yRot = (float)((Math.PI / 4) - (Math.PI / 18) * state.walkAnimationSpeed);
            this.tailfeather5rot.yRot = (float)((-Math.PI / 4) + (Math.PI / 18) * state.walkAnimationSpeed);
        } else {
            this.bodyrot.xRot = 0.0F;
            this.winglefrot1.xRot = 0.0F;
            this.wingrigrot1.xRot = 0.0F;
            this.neck1rot.xRot = (float) (-Math.PI / 9);
            this.neck2rot.xRot = -0.87266463F;
            this.neck3rot.xRot = -0.87266463F;
            this.neck4rot.xRot = (float) (-Math.PI * 2.0 / 9.0);
            this.neck5rot.xRot = (float) (Math.PI / 9);
            this.neck6rot.xRot = (float) (Math.PI / 9);
            this.neck7rot.xRot = 0.61086524F;
            this.neck8rot.xRot = (float) (Math.PI / 4);
            this.neck9rot.xRot = 0.9599311F;
            this.neck10rot.xRot = 0.0F;
            this.legleftoprot.xRot = (float) (Math.PI / 3);
            this.legleftoprot.yRot = 0.08726646F;
            this.leglefbotrot.xRot = 0.87266463F;
            this.footlef1rot.xRot = (float) (Math.PI * 5.0 / 12.0);
            this.footlef2rot.xRot = (float) (Math.PI * 5.0 / 12.0);
            this.footlef3rot.xRot = (float) (-Math.PI * 5.0 / 12.0);
            this.legrigtoprot.xRot = (float) (Math.PI / 3);
            this.legrigtoprot.yRot = -0.08726646F;
            this.legrigbotrot.xRot = 0.87266463F;
            this.footrig1rot.xRot = (float) (Math.PI * 5.0 / 12.0);
            this.footrig2rot.xRot = (float) (Math.PI * 5.0 / 12.0);
            this.footrig3rot.xRot = (float) (-Math.PI * 5.0 / 12.0);
            this.winglefrot1.zRot = (float)(
                (Math.PI * 2.0 / 9.0) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.winglefrot2.zRot = 0.0F;
            this.winglefrot3.zRot = 0.0F;
            this.winglefrot4.zRot = 0.0F;
            this.winglefrot5.zRot = 0.0F;
            this.winglefrot6.zRot = (float)(
                (-Math.PI / 9)
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot7.zRot = 0.0F;
            this.winglefrot8.zRot = 0.0F;
            this.winglefrot8.zRot = 0.0F;
            this.winglefrot10.zRot = (float)(
                -0.4363323129985824
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot11.zRot = 0.0F;
            this.winglefrot12.zRot = 0.0F;
            this.winglefrot1.xRot = 0.0F;
            this.winglefrot2.xRot = 0.0F;
            this.winglefrot3.xRot = 0.0F;
            this.winglefrot4.xRot = 0.0F;
            this.winglefrot5.xRot = 0.0F;
            this.winglefrot6.xRot = 0.0F;
            this.winglefrot7.xRot = 0.0F;
            this.winglefrot8.xRot = 0.0F;
            this.winglefrot8.xRot = 0.0F;
            this.winglefrot10.xRot = 0.0F;
            this.winglefrot11.xRot = 0.0F;
            this.winglefrot12.xRot = 0.0F;
            this.winglefrot1.yRot = (float)(
                -0.06981317007977318
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot5.yRot = (float)(
                -0.08726646259971647
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot8.yRot = (float)(
                -0.10471975511965977
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot11.yRot = (float)(
                -0.12217304763960307
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot1.zRot = (float)(
                (-Math.PI * 2.0 / 9.0) * Mth.sin((float)(state.worldTime * 0.6662F * 0.5F + 0.0))
            );
            this.wingrigrot2.zRot = 0.0F;
            this.wingrigrot3.zRot = 0.0F;
            this.wingrigrot4.zRot = 0.0F;
            this.wingrigrot5.zRot = 0.0F;
            this.wingrigrot6.zRot = (float)(
                (Math.PI / 9)
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot7.zRot = 0.0F;
            this.wingrigrot8.zRot = 0.0F;
            this.wingrigrot8.zRot = 0.0F;
            this.wingrigrot10.zRot = (float)(
                0.4363323129985824
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot11.zRot = 0.0F;
            this.wingrigrot12.zRot = 0.0F;
            this.wingrigrot1.xRot = 0.0F;
            this.wingrigrot2.xRot = 0.0F;
            this.wingrigrot3.xRot = 0.0F;
            this.wingrigrot4.xRot = 0.0F;
            this.wingrigrot5.xRot = 0.0F;
            this.wingrigrot6.xRot = 0.0F;
            this.wingrigrot7.xRot = 0.0F;
            this.wingrigrot8.xRot = 0.0F;
            this.wingrigrot8.xRot = 0.0F;
            this.wingrigrot10.xRot = 0.0F;
            this.wingrigrot11.xRot = 0.0F;
            this.wingrigrot12.xRot = 0.0F;
            this.wingrigrot1.yRot = (float)(
                0.06981317007977318
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot5.yRot = (float)(
                0.08726646259971647
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot8.yRot = (float)(
                0.10471975511965977
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot11.yRot = (float)(
                0.12217304763960307
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.tailfeather1rot.yRot = (float)((Math.PI / 4) - (Math.PI / 18) * state.walkAnimationSpeed);
            this.tailfeather5rot.yRot = (float)((-Math.PI / 4) + (Math.PI / 18) * state.walkAnimationSpeed);
        }

        float lookingDirX = Math.min(Math.max(state.xRot, -30.0F), 30.0F) * (float) (Math.PI / 180.0);
        if (state.grounded) {
            this.headrot.xRot = (float)((Math.PI / 6) - 20.0F * state.walkAnimationSpeed * Math.PI / 180.0 * Mth.sin(state.walkAnimationPos * 0.6662F * 1.0F)) + lookingDirX;
        } else if (state.entityState == EntityState.ATTACKING) {
            this.headrot.xRot = (float) (Math.PI / 6) + lookingDirX;
        } else {
            this.headrot.xRot = 0.0F + lookingDirX;
        }

        this.headrot.yRot = Math.min(Math.max(state.yRot, -30.0F), 30.0F) * (float) (Math.PI / 180.0);
    }
}
