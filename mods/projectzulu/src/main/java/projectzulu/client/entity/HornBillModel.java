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
 * ModelHornBill: built like the eagle (three tail feathers, the body raised with heightToRaise -10). Wings and
 * claws move with the world time; attacking it pitches forward and stretches the legs.
 */
public class HornBillModel extends EntityModel<BirdRenderState> {
    private final ModelPart bodyrot;
    private final ModelPart tailbaserot;
    private final ModelPart tailfeather3rot;
    private final ModelPart tailfeather1rot;
    private final ModelPart tailfeather5rot;
    private final ModelPart neck1rot;
    private final ModelPart neck2rot;
    private final ModelPart neck3rot;
    private final ModelPart neck4rot;
    private final ModelPart neck5rot;
    private final ModelPart neck6rot;
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
    private final ModelPart legleftoprot;
    private final ModelPart leglefbotrot;
    private final ModelPart footlef1rot;
    private final ModelPart footlef2rot;
    private final ModelPart footlef3rot;
    private final ModelPart legrigtoprot;
    private final ModelPart legrigbotrot;
    private final ModelPart footrig1rot;
    private final ModelPart footrig2rot;
    private final ModelPart footrig3rot;

    public HornBillModel(ModelPart root) {
        super(root);
        this.bodyrot = root.getChild("bodyrot");
        this.tailbaserot = this.bodyrot.getChild("tailbaserot");
        this.tailfeather3rot = this.tailbaserot.getChild("tailfeather3rot");
        this.tailfeather1rot = this.tailbaserot.getChild("tailfeather1rot");
        this.tailfeather5rot = this.tailbaserot.getChild("tailfeather5rot");
        this.neck1rot = this.bodyrot.getChild("neck1rot");
        this.neck2rot = this.neck1rot.getChild("neck2rot");
        this.neck3rot = this.neck2rot.getChild("neck3rot");
        this.neck4rot = this.neck3rot.getChild("neck4rot");
        this.neck5rot = this.neck4rot.getChild("neck5rot");
        this.neck6rot = this.neck5rot.getChild("neck6rot");
        this.headrot = this.neck6rot.getChild("headrot");
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
        this.legleftoprot = this.bodyrot.getChild("legleftoprot");
        this.leglefbotrot = this.legleftoprot.getChild("leglefbotrot");
        this.footlef1rot = this.leglefbotrot.getChild("footlef1rot");
        this.footlef2rot = this.leglefbotrot.getChild("footlef2rot");
        this.footlef3rot = this.leglefbotrot.getChild("footlef3rot");
        this.legrigtoprot = this.bodyrot.getChild("legrigtoprot");
        this.legrigbotrot = this.legrigtoprot.getChild("legrigbotrot");
        this.footrig1rot = this.legrigbotrot.getChild("footrig1rot");
        this.footrig2rot = this.legrigbotrot.getChild("footrig2rot");
        this.footrig3rot = this.legrigbotrot.getChild("footrig3rot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bodyrot = root.addOrReplaceChild("bodyrot", CubeListBuilder.create().texOffs(0, 21).mirror().addBox(-2.0F, -3.0F, -5.5F, 4, 4, 7).texOffs(0, 16).addBox(-2.0F, -3.0F, -6.5F, 4, 4, 1).texOffs(0, 5).addBox(-2.0F, -3.0F, 1.5F, 4, 4, 1).texOffs(0, 10).addBox(-1.5F, -3.0F, 2.5F, 3, 3, 1),
                PartPose.offset(0.0F, 29.0F, 2.5F));
        PartDefinition tailbaserot = bodyrot.addOrReplaceChild("tailbaserot", CubeListBuilder.create().texOffs(8, 10).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 2, 1),
                PartPose.offset(0.0F, -1.5F, 3.5F));
        tailbaserot.addOrReplaceChild("tailfeather3rot", CubeListBuilder.create().texOffs(9, 5).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 8),
                PartPose.offset(0.0F, -0.25F, 1.0F));
        tailbaserot.addOrReplaceChild("tailfeather1rot", CubeListBuilder.create().texOffs(9, 5).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 8),
                PartPose.offset(1.0F, 0.25F, 1.0F));
        tailbaserot.addOrReplaceChild("tailfeather5rot", CubeListBuilder.create().texOffs(9, 5).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 8),
                PartPose.offset(-1.0F, 0.5F, 1.0F));
        PartDefinition neck1rot = bodyrot.addOrReplaceChild("neck1rot", CubeListBuilder.create().texOffs(8, 0).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 3, 1),
                PartPose.offset(0.0F, -1.5F, -6.5F));
        PartDefinition neck2rot = neck1rot.addOrReplaceChild("neck2rot", CubeListBuilder.create().texOffs(21, 0).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck3rot = neck2rot.addOrReplaceChild("neck3rot", CubeListBuilder.create().texOffs(20, 0).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck4rot = neck3rot.addOrReplaceChild("neck4rot", CubeListBuilder.create().texOffs(19, 0).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck5rot = neck4rot.addOrReplaceChild("neck5rot", CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition neck6rot = neck5rot.addOrReplaceChild("neck6rot", CubeListBuilder.create().texOffs(17, 0).mirror().addBox(-1.5F, -1.5F, -1.0F, 3, 2, 1),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        PartDefinition headrot = neck6rot.addOrReplaceChild("headrot", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-1.5F, -3.0F, -3.5F, 3, 1, 5).texOffs(50, 6).addBox(-2.0F, -2.0F, -2.0F, 4, 4, 3).texOffs(48, 13).addBox(-1.0F, -1.5F, -8.0F, 2, 2, 6).texOffs(44, 14).addBox(-0.5F, 0.5F, -6.0F, 1, 1, 4),
                PartPose.offset(0.0F, -1.0F, -2.0F));
        headrot.addOrReplaceChild("horn2rot", CubeListBuilder.create().texOffs(42, 4).mirror().addBox(-1.0F, -0.5F, -1.5F, 2, 1, 1),
                PartPose.offsetAndRotation(0.0F, -2.5F, -3.0F, -0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition winglefrot1 = bodyrot.addOrReplaceChild("winglefrot1", CubeListBuilder.create().texOffs(22, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-2.5F, -2.5F, -5.5F));
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
        PartDefinition winglefrot9 = winglefrot8.addOrReplaceChild("winglefrot9", CubeListBuilder.create().texOffs(22, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot10 = winglefrot9.addOrReplaceChild("winglefrot10", CubeListBuilder.create().texOffs(23, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition winglefrot11 = winglefrot10.addOrReplaceChild("winglefrot11", CubeListBuilder.create().texOffs(24, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        winglefrot11.addOrReplaceChild("winglefrot12", CubeListBuilder.create().texOffs(24, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot1 = bodyrot.addOrReplaceChild("wingrigrot1", CubeListBuilder.create().texOffs(22, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(2.5F, -2.5F, -5.5F));
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
        PartDefinition wingrigrot9 = wingrigrot8.addOrReplaceChild("wingrigrot9", CubeListBuilder.create().texOffs(22, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot10 = wingrigrot9.addOrReplaceChild("wingrigrot10", CubeListBuilder.create().texOffs(23, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition wingrigrot11 = wingrigrot10.addOrReplaceChild("wingrigrot11", CubeListBuilder.create().texOffs(24, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        wingrigrot11.addOrReplaceChild("wingrigrot12", CubeListBuilder.create().texOffs(24, 24).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 7),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition legleftoprot = bodyrot.addOrReplaceChild("legleftoprot", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1),
                PartPose.offset(-1.5F, 1.0F, 0.0F));
        PartDefinition leglefbotrot = legleftoprot.addOrReplaceChild("leglefbotrot", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        leglefbotrot.addOrReplaceChild("footlef1rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -1.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 1.5F, -0.5F, 0.0F, 0.4363323F, 0.0F));
        leglefbotrot.addOrReplaceChild("footlef2rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -1.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 1.5F, -0.5F, 0.0F, -0.4363323F, 0.0F));
        leglefbotrot.addOrReplaceChild("footlef3rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, 2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, 1.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.5F, 0.5F));
        PartDefinition legrigtoprot = bodyrot.addOrReplaceChild("legrigtoprot", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1),
                PartPose.offset(1.5F, 1.0F, 0.0F));
        PartDefinition legrigbotrot = legrigtoprot.addOrReplaceChild("legrigbotrot", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        legrigbotrot.addOrReplaceChild("footrig1rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -1.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 1.5F, -0.5F, 0.0F, 0.4363323F, 0.0F));
        legrigbotrot.addOrReplaceChild("footrig2rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, -1.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 1.5F, -0.5F, 0.0F, -0.4363323F, 0.0F));
        legrigbotrot.addOrReplaceChild("footrig3rot", CubeListBuilder.create().texOffs(36, 2).mirror().addBox(-0.5F, -0.5F, 2.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 1).texOffs(36, 0).addBox(-0.5F, -0.5F, 1.0F, 1, 1, 1),
                PartPose.offset(0.0F, 1.5F, 0.5F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** setLivingAnimations followed by setRotationAngles of the original. */
    @Override
    public void setupAnim(BirdRenderState state) {
        super.setupAnim(state);
        if (state.entityState == EntityState.ATTACKING) {
            this.bodyrot.xRot = (float) (-Math.PI / 6);
            this.winglefrot1.xRot = (float) (Math.PI / 18);
            this.wingrigrot1.xRot = (float) (Math.PI / 18);
            this.legleftoprot.xRot = -0.9599311F;
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
            this.legrigtoprot.xRot = -0.9599311F;
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
            this.winglefrot6.zRot = (float)(
                (-Math.PI / 9)
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot10.zRot = (float)(
                -0.4363323129985824
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
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
            this.wingrigrot6.zRot = (float)(
                (Math.PI / 9)
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot10.zRot = (float)(
                0.4363323129985824
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
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
            this.winglefrot6.zRot = (float)(
                (-Math.PI / 9)
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.winglefrot10.zRot = (float)(
                -0.4363323129985824
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
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
            this.wingrigrot6.zRot = (float)(
                (Math.PI / 9)
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
            this.wingrigrot10.zRot = (float)(
                0.4363323129985824
                    * Math.abs(Mth.sin((float)(state.worldTime * 0.6662F * 0.5F / 2.0F + (Math.PI * 3.0 / 4.0))))
            );
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

        float lookingDirX = Math.min(Math.max(state.xRot, -15.0F), 15.0F) * (float) (Math.PI / 180.0);
        if (state.entityState == EntityState.ATTACKING) {
            this.headrot.xRot = 0.87266463F + lookingDirX;
        } else {
            this.headrot.xRot = 0.0F + lookingDirX;
        }

        this.headrot.yRot = Math.min(Math.max(state.yRot, -20.0F), 20.0F) * (float) (Math.PI / 180.0);
    }
}
