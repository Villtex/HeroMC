package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * ModelVulture: feet group and body with two four-segment wings, a three-joint neck with head and beak, and a tail.
 * On the ground it folds the wings and lifts the neck; following or attacking it beats the wings (ping-pong between
 * the limits) with the feet half stretched; idle it glides. The angles ease per frame (EntityModelRotation); the
 * renderer steps them per entity and hands them over in {@link State}.
 */
public class VultureModel extends EntityModel<VultureModel.State> {
    /** The eased angles (eFEETROT, eNECKROT1-3, eLEFTWING, eRIGTHWING, eBODYROT) and the head pitch base. */
    public static class State extends BirdRenderState {
        public float feetX;
        public float neck1X;
        public float neck2X;
        public float neck3X;
        public float leftWingZ;
        public float rightWingZ;
        public float bodyX;
        public float headBaseX;
    }

    private final ModelPart feetrot;
    private final ModelPart rightfootfronttal2;
    private final ModelPart rightfootfronttal1;
    private final ModelPart leftfootfronttal2;
    private final ModelPart leftfootfronttal1;
    private final ModelPart leftfootbacktal;
    private final ModelPart rightfoottoe2;
    private final ModelPart leftfoottoe2;
    private final ModelPart leftfootbase;
    private final ModelPart leftfoottoe1;
    private final ModelPart rigfootbacktal;
    private final ModelPart rightfoottoe1;
    private final ModelPart rightfootbase;
    private final ModelPart rightleg;
    private final ModelPart leftleg;
    private final ModelPart bodyrot;
    private final ModelPart leftwing;
    private final ModelPart wingleft4;
    private final ModelPart wingleft3;
    private final ModelPart wingleft2;
    private final ModelPart wingleft1;
    private final ModelPart rigthwing;
    private final ModelPart wingrig4;
    private final ModelPart wingrig3;
    private final ModelPart wingrig2;
    private final ModelPart wingrig1;
    private final ModelPart neckrot1;
    private final ModelPart neckrot2;
    private final ModelPart neck2;
    private final ModelPart neckrot3;
    private final ModelPart headrot;
    private final ModelPart mouthbot;
    private final ModelPart mouthtal;
    private final ModelPart mouthtop;
    private final ModelPart head;
    private final ModelPart neck3;
    private final ModelPart neck1;
    private final ModelPart tailrot;
    private final ModelPart tailbase1;
    private final ModelPart tailbase2;
    private final ModelPart neckfrill;
    private final ModelPart chestpuff;
    private final ModelPart body;

    public VultureModel(ModelPart root) {
        super(root);
        this.feetrot = root.getChild("feetrot");
        this.rightfootfronttal2 = this.feetrot.getChild("rightfootfronttal2");
        this.rightfootfronttal1 = this.feetrot.getChild("rightfootfronttal1");
        this.leftfootfronttal2 = this.feetrot.getChild("leftfootfronttal2");
        this.leftfootfronttal1 = this.feetrot.getChild("leftfootfronttal1");
        this.leftfootbacktal = this.feetrot.getChild("leftfootbacktal");
        this.rightfoottoe2 = this.feetrot.getChild("rightfoottoe2");
        this.leftfoottoe2 = this.feetrot.getChild("leftfoottoe2");
        this.leftfootbase = this.feetrot.getChild("leftfootbase");
        this.leftfoottoe1 = this.feetrot.getChild("leftfoottoe1");
        this.rigfootbacktal = this.feetrot.getChild("rigfootbacktal");
        this.rightfoottoe1 = this.feetrot.getChild("rightfoottoe1");
        this.rightfootbase = this.feetrot.getChild("rightfootbase");
        this.rightleg = this.feetrot.getChild("rightleg");
        this.leftleg = this.feetrot.getChild("leftleg");
        this.bodyrot = root.getChild("bodyrot");
        this.leftwing = this.bodyrot.getChild("leftwing");
        this.wingleft4 = this.leftwing.getChild("wingleft4");
        this.wingleft3 = this.leftwing.getChild("wingleft3");
        this.wingleft2 = this.leftwing.getChild("wingleft2");
        this.wingleft1 = this.leftwing.getChild("wingleft1");
        this.rigthwing = this.bodyrot.getChild("rigthwing");
        this.wingrig4 = this.rigthwing.getChild("wingrig4");
        this.wingrig3 = this.rigthwing.getChild("wingrig3");
        this.wingrig2 = this.rigthwing.getChild("wingrig2");
        this.wingrig1 = this.rigthwing.getChild("wingrig1");
        this.neckrot1 = this.bodyrot.getChild("neckrot1");
        this.neckrot2 = this.neckrot1.getChild("neckrot2");
        this.neck2 = this.neckrot2.getChild("neck2");
        this.neckrot3 = this.neckrot2.getChild("neckrot3");
        this.headrot = this.neckrot3.getChild("headrot");
        this.mouthbot = this.headrot.getChild("mouthbot");
        this.mouthtal = this.headrot.getChild("mouthtal");
        this.mouthtop = this.headrot.getChild("mouthtop");
        this.head = this.headrot.getChild("head");
        this.neck3 = this.neckrot3.getChild("neck3");
        this.neck1 = this.neckrot1.getChild("neck1");
        this.tailrot = this.bodyrot.getChild("tailrot");
        this.tailbase1 = this.tailrot.getChild("tailbase1");
        this.tailbase2 = this.tailrot.getChild("tailbase2");
        this.neckfrill = this.bodyrot.getChild("neckfrill");
        this.chestpuff = this.bodyrot.getChild("chestpuff");
        this.body = this.bodyrot.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition feetrot = root.addOrReplaceChild("feetrot", CubeListBuilder.create(),
                PartPose.offset(1.5F, 18.0F, -6.0F));
        feetrot.addOrReplaceChild("rightfootfronttal2", CubeListBuilder.create().texOffs(39, 4).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(2.5F, 12.0F, -1.0F));
        feetrot.addOrReplaceChild("rightfootfronttal1", CubeListBuilder.create().texOffs(39, 4).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(4.5F, 12.0F, -1.0F));
        feetrot.addOrReplaceChild("leftfootfronttal2", CubeListBuilder.create().texOffs(39, 4).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(-1.5F, 12.0F, -1.0F));
        feetrot.addOrReplaceChild("leftfootfronttal1", CubeListBuilder.create().texOffs(39, 4).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(0.5F, 12.0F, -1.0F));
        feetrot.addOrReplaceChild("leftfootbacktal", CubeListBuilder.create().texOffs(39, 4).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(-0.5F, 11.0F, 2.0F));
        feetrot.addOrReplaceChild("rightfoottoe2", CubeListBuilder.create().texOffs(32, 3).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(2.5F, 11.0F, -1.0F));
        feetrot.addOrReplaceChild("leftfoottoe2", CubeListBuilder.create().texOffs(32, 3).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(-1.5F, 11.0F, -1.0F));
        feetrot.addOrReplaceChild("leftfootbase", CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, -7.0F, -1.5F, 3, 1, 2),
                PartPose.offset(-1.5F, 11.0F, 0.0F));
        feetrot.addOrReplaceChild("leftfoottoe1", CubeListBuilder.create().texOffs(32, 3).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(0.5F, 11.0F, -1.0F));
        feetrot.addOrReplaceChild("rigfootbacktal", CubeListBuilder.create().texOffs(39, 4).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(3.5F, 11.0F, 2.0F));
        feetrot.addOrReplaceChild("rightfoottoe1", CubeListBuilder.create().texOffs(32, 3).addBox(-2.0F, -7.0F, -1.5F, 1, 1, 1),
                PartPose.offset(4.5F, 11.0F, -1.0F));
        feetrot.addOrReplaceChild("rightfootbase", CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, -7.0F, -1.5F, 3, 1, 2),
                PartPose.offset(2.5F, 11.0F, 0.0F));
        feetrot.addOrReplaceChild("rightleg", CubeListBuilder.create().texOffs(20, 27).addBox(-2.0F, -7.0F, -1.5F, 1, 4, 1),
                PartPose.offset(3.5F, 7.0F, 1.0F));
        feetrot.addOrReplaceChild("leftleg", CubeListBuilder.create().texOffs(16, 27).addBox(-2.0F, -7.0F, -1.5F, 1, 4, 1),
                PartPose.offset(-0.5F, 7.0F, 1.0F));
        PartDefinition bodyrot = root.addOrReplaceChild("bodyrot", CubeListBuilder.create(),
                PartPose.offset(1.5F, 17.5F, -6.0F));
        PartDefinition leftwing = bodyrot.addOrReplaceChild("leftwing", CubeListBuilder.create(),
                PartPose.offset(-1.5F, -6.0F, 0.0F));
        leftwing.addOrReplaceChild("wingleft4", CubeListBuilder.create().texOffs(16, 18).addBox(-2.0F, -7.0F, -1.5F, 1, 2, 6),
                PartPose.offset(0.0F, 12.5F, 0.0F));
        leftwing.addOrReplaceChild("wingleft3", CubeListBuilder.create().texOffs(16, 11).addBox(-2.0F, -7.0F, -1.5F, 1, 2, 5),
                PartPose.offset(0.0F, 10.5F, 0.0F));
        leftwing.addOrReplaceChild("wingleft2", CubeListBuilder.create().texOffs(16, 5).addBox(-2.0F, -7.0F, -1.5F, 1, 2, 4),
                PartPose.offset(0.0F, 8.5F, 0.0F));
        leftwing.addOrReplaceChild("wingleft1", CubeListBuilder.create().texOffs(16, 0).addBox(-2.0F, -7.0F, -1.5F, 1, 2, 3),
                PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition rigthwing = bodyrot.addOrReplaceChild("rigthwing", CubeListBuilder.create(),
                PartPose.offset(2.0F, -6.0F, 0.0F));
        rigthwing.addOrReplaceChild("wingrig4", CubeListBuilder.create().texOffs(16, 18).addBox(-2.0F, -7.0F, -1.5F, 1, 2, 6),
                PartPose.offset(2.5F, 12.5F, 0.0F));
        rigthwing.addOrReplaceChild("wingrig3", CubeListBuilder.create().texOffs(16, 11).addBox(-2.0F, -7.0F, -1.5F, 1, 2, 5),
                PartPose.offset(2.5F, 10.5F, 0.0F));
        rigthwing.addOrReplaceChild("wingrig2", CubeListBuilder.create().texOffs(16, 5).addBox(-2.0F, -7.0F, -1.5F, 1, 2, 4),
                PartPose.offset(2.5F, 8.5F, 0.0F));
        rigthwing.addOrReplaceChild("wingrig1", CubeListBuilder.create().texOffs(16, 0).addBox(-2.0F, -7.0F, -1.5F, 1, 2, 3),
                PartPose.offset(2.5F, 6.5F, 0.0F));
        PartDefinition neckrot1 = bodyrot.addOrReplaceChild("neckrot1", CubeListBuilder.create(),
                PartPose.offset(0.0F, -7.5F, 0.0F));
        PartDefinition neckrot2 = neckrot1.addOrReplaceChild("neckrot2", CubeListBuilder.create(),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        neckrot2.addOrReplaceChild("neck2", CubeListBuilder.create().texOffs(8, 10).addBox(-0.5F, -3.0F, -0.5F, 1, 2, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition neckrot3 = neckrot2.addOrReplaceChild("neckrot3", CubeListBuilder.create(),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition headrot = neckrot3.addOrReplaceChild("headrot", CubeListBuilder.create(),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        headrot.addOrReplaceChild("mouthbot", CubeListBuilder.create().texOffs(0, 25).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2),
                PartPose.offset(0.0F, 1.0F, -2.0F));
        headrot.addOrReplaceChild("mouthtal", CubeListBuilder.create().texOffs(6, 25).addBox(-1.0F, 0.0F, -0.5F, 2, 2, 1),
                PartPose.offset(0.0F, 0.5F, -4.5F));
        headrot.addOrReplaceChild("mouthtop", CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -1.5F, -3.0F, 2, 1, 3),
                PartPose.offset(0.0F, 1.0F, -2.0F));
        headrot.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, -1.5F, -2.0F, 3, 3, 2),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        neckrot3.addOrReplaceChild("neck3", CubeListBuilder.create().texOffs(8, 10).addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        neckrot1.addOrReplaceChild("neck1", CubeListBuilder.create().texOffs(8, 10).addBox(-0.5F, -3.0F, -0.5F, 1, 2, 1),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition tailrot = bodyrot.addOrReplaceChild("tailrot", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 1.0F));
        tailrot.addOrReplaceChild("tailbase1", CubeListBuilder.create().texOffs(32, 8).addBox(-1.0F, -0.5F, 0.0F, 2, 1, 2),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tailrot.addOrReplaceChild("tailbase2", CubeListBuilder.create().texOffs(32, 11).addBox(-1.5F, -0.5F, 0.0F, 3, 1, 2),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        bodyrot.addOrReplaceChild("neckfrill", CubeListBuilder.create().texOffs(0, 28).addBox(0.0F, 0.0F, 0.0F, 2, 1, 2),
                PartPose.offset(-1.0F, -7.5F, -1.0F));
        bodyrot.addOrReplaceChild("chestpuff", CubeListBuilder.create().texOffs(0, 10).addBox(-2.0F, -7.0F, -1.5F, 3, 5, 1),
                PartPose.offset(0.5F, 0.5F, -1.0F));
        bodyrot.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -7.0F, -1.5F, 5, 7, 3),
                PartPose.offset(-0.5F, 0.5F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** setLivingAnimations followed by setRotationAngles of the original. */
    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        this.feetrot.xRot = state.feetX;
        this.neckrot1.xRot = state.neck1X;
        this.neckrot2.xRot = state.neck2X;
        this.neckrot3.xRot = state.neck3X;
        this.leftwing.zRot = state.leftWingZ;
        this.rigthwing.zRot = state.rightWingZ;
        this.bodyrot.xRot = state.bodyX;
        float lookingDirectionX = Math.min(Math.max(state.xRot, -15.0F), 15.0F) * (float) (Math.PI / 180.0);
        this.headrot.xRot = state.headBaseX + lookingDirectionX;
        this.headrot.yRot = Math.min(Math.max(state.yRot, -45.0F), 45.0F) * (float) (Math.PI / 180.0);
    }
}
