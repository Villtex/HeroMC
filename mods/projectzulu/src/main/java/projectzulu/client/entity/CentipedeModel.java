package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import projectzulu.entity.CentipedeEntity;

/**
 * ModelCentipede (the head: body plates, two jointed antennae, four two-piece legs) plus nine ModelFollower body
 * segments (the same without antennae, every second one drawn at 98 %). The segments were separate entities in the
 * original; here they are groups of this model placed at the part positions of {@link CentipedeEntity} (relative to
 * the head, computed by the renderer). Antennae wave and legs swing with the walk.
 */
public class CentipedeModel extends EntityModel<CentipedeModel.State> {
    private final ModelPart antenlefrot2;
    private final ModelPart antenlefrot3;
    private final ModelPart antenrigrot2;
    private final ModelPart antenrigrot3;
    private final ModelPart[] legs = new ModelPart[4];
    private final ModelPart[] segments = new ModelPart[CentipedeEntity.SEGMENTS];
    private final ModelPart[][] segmentLegs = new ModelPart[CentipedeEntity.SEGMENTS][4];

    public static class State extends LivingEntityRenderState {
        /** Segment positions in model space (pixels) and yaw relative to the head (radians). */
        public final float[] segX = new float[CentipedeEntity.SEGMENTS];
        public final float[] segY = new float[CentipedeEntity.SEGMENTS];
        public final float[] segZ = new float[CentipedeEntity.SEGMENTS];
        public final float[] segYaw = new float[CentipedeEntity.SEGMENTS];
        public boolean hasSegments;
    }

    public CentipedeModel(ModelPart root) {
        super(root);
        ModelPart antenlefrot1 = root.getChild("antenlefrot1");
        this.antenlefrot2 = antenlefrot1.getChild("antenlefrot2");
        this.antenlefrot3 = this.antenlefrot2.getChild("antenlefrot3");
        ModelPart antenrigrot1 = root.getChild("antenrigrot1");
        this.antenrigrot2 = antenrigrot1.getChild("antenrigrot2");
        this.antenrigrot3 = this.antenrigrot2.getChild("antenrigrot3");
        for (int leg = 0; leg < 4; leg++) {
            this.legs[leg] = root.getChild("leg" + (leg + 1) + "1rot");
        }
        for (int i = 0; i < CentipedeEntity.SEGMENTS; i++) {
            this.segments[i] = root.getChild("segment" + i);
            for (int leg = 0; leg < 4; leg++) {
                this.segmentLegs[i][leg] = this.segments[i].getChild("leg" + (leg + 1) + "1rot");
            }
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("sidebody2", CubeListBuilder.create().texOffs(42, 17).addBox(0.0F, -2.5F, -5.0F, 1, 5, 10),
                PartPose.offset(4.5F, 18.5F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -3.0F, -12.0F, 9, 6, 12),
                PartPose.offset(0.0F, 18.5F, 6.0F));
        root.addOrReplaceChild("sidebody3", CubeListBuilder.create().texOffs(0, 2).addBox(-3.0F, 0.0F, -5.5F, 6, 1, 11),
                PartPose.offset(0.0F, 21.5F, 0.0F));
        root.addOrReplaceChild("sidebody1", CubeListBuilder.create().texOffs(42, 17).addBox(-1.0F, -2.5F, -5.0F, 1, 5, 10),
                PartPose.offset(-4.5F, 18.5F, 0.0F));
        PartDefinition antenlefrot1 = root.addOrReplaceChild("antenlefrot1", CubeListBuilder.create().texOffs(54, 0).mirror().addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4).mirror(false),
                PartPose.offsetAndRotation(-3.0F, 17.5F, -6.0F, (float) (-Math.PI / 6), (float) (Math.PI / 6), 0.0F));
        PartDefinition antenlefrot2 = antenlefrot1.addOrReplaceChild("antenlefrot2", CubeListBuilder.create().texOffs(50, 0).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, (float) (-Math.PI / 6), 0.0F, 0.0F));
        antenlefrot2.addOrReplaceChild("antenlefrot3", CubeListBuilder.create().texOffs(47, 0).mirror().addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, (float) (-Math.PI / 6), 0.0F, 0.0F));
        PartDefinition antenrigrot1 = root.addOrReplaceChild("antenrigrot1", CubeListBuilder.create().texOffs(54, 0).mirror().addBox(-0.5F, -0.5F, -4.0F, 1, 1, 4).mirror(false),
                PartPose.offsetAndRotation(3.0F, 17.5F, -6.0F, (float) (-Math.PI / 6), (float) (-Math.PI / 6), 0.0F));
        PartDefinition antenrigrot2 = antenrigrot1.addOrReplaceChild("antenrigrot2", CubeListBuilder.create().texOffs(50, 0).mirror().addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, (float) (-Math.PI / 6), 0.0F, 0.0F));
        antenrigrot2.addOrReplaceChild("antenrigrot3", CubeListBuilder.create().texOffs(47, 0).mirror().addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, (float) (-Math.PI / 6), 0.0F, 0.0F));
        PartDefinition leg11rot = root.addOrReplaceChild("leg11rot", CubeListBuilder.create().texOffs(34, 0).mirror().addBox(-4.0F, -0.5F, -1.0F, 4, 1, 2).mirror(false),
                PartPose.offsetAndRotation(-3.0F, 22.0F, -3.0F, 0.0F, 0.0F, -0.1745329F));
        leg11rot.addOrReplaceChild("leg12rot", CubeListBuilder.create().texOffs(34, 3).mirror().addBox(-2.0F, -0.5F, -1.0F, 2, 1, 2).mirror(false),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 4)));
        PartDefinition leg21rot = root.addOrReplaceChild("leg21rot", CubeListBuilder.create().texOffs(34, 0).mirror().addBox(0.0F, -0.5F, -1.0F, 4, 1, 2).mirror(false),
                PartPose.offsetAndRotation(3.0F, 22.0F, -3.0F, 0.0F, 0.0F, 0.1745329F));
        leg21rot.addOrReplaceChild("leg22rot", CubeListBuilder.create().texOffs(34, 3).mirror().addBox(0.0F, -0.5F, -1.0F, 2, 1, 2).mirror(false),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4)));
        PartDefinition leg31rot = root.addOrReplaceChild("leg31rot", CubeListBuilder.create().texOffs(34, 0).mirror().addBox(-4.0F, -0.5F, -1.0F, 4, 1, 2).mirror(false),
                PartPose.offsetAndRotation(-3.0F, 22.0F, 3.0F, 0.0F, 0.0F, -0.1745329F));
        leg31rot.addOrReplaceChild("leg32rot", CubeListBuilder.create().texOffs(34, 3).mirror().addBox(-2.0F, -0.5F, -1.0F, 2, 1, 2).mirror(false),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 4)));
        PartDefinition leg41rot = root.addOrReplaceChild("leg41rot", CubeListBuilder.create().texOffs(34, 0).mirror().addBox(0.0F, -0.5F, -1.0F, 4, 1, 2).mirror(false),
                PartPose.offsetAndRotation(3.0F, 22.0F, 3.0F, 0.0F, 0.0F, 0.1745329F));
        leg41rot.addOrReplaceChild("leg42rot", CubeListBuilder.create().texOffs(34, 3).mirror().addBox(0.0F, -0.5F, -1.0F, 2, 1, 2).mirror(false),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4)));
        for (int i = 0; i < CentipedeEntity.SEGMENTS; i++) {
            // the group sits at the follower's feet (model y 24); its parts keep ModelFollower's points shifted by -24
            PartPose pose = i % 2 == 0 ? PartPose.offset(0.0F, 24.0F, 0.0F).withScale(0.98F) : PartPose.offset(0.0F, 24.0F, 0.0F);
            addFollower(root.addOrReplaceChild("segment" + i, CubeListBuilder.create(), pose));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelFollower. */
    private static void addFollower(PartDefinition segment) {
        segment.addOrReplaceChild("sidebody2", CubeListBuilder.create().texOffs(42, 17).addBox(0.0F, -2.5F, -5.0F, 1, 5, 10),
                PartPose.offset(4.5F, -5.5F, 0.0F));
        segment.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -3.0F, -12.0F, 9, 6, 12),
                PartPose.offset(0.0F, -5.5F, 6.0F));
        segment.addOrReplaceChild("sidebody3", CubeListBuilder.create().texOffs(0, 2).addBox(-3.0F, 0.0F, -5.5F, 6, 1, 11),
                PartPose.offset(0.0F, -2.5F, 0.0F));
        segment.addOrReplaceChild("sidebody1", CubeListBuilder.create().texOffs(42, 17).addBox(-1.0F, -2.5F, -5.0F, 1, 5, 10),
                PartPose.offset(-4.5F, -5.5F, 0.0F));
        PartDefinition leg11rot = segment.addOrReplaceChild("leg11rot", CubeListBuilder.create().texOffs(34, 0).mirror().addBox(-4.0F, -0.5F, -1.0F, 4, 1, 2).mirror(false),
                PartPose.offsetAndRotation(-3.0F, -2.0F, -3.0F, 0.0F, 0.0F, -0.1745329F));
        leg11rot.addOrReplaceChild("leg12rot", CubeListBuilder.create().texOffs(34, 3).mirror().addBox(-2.0F, -0.5F, -1.0F, 2, 1, 2).mirror(false),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 4)));
        PartDefinition leg21rot = segment.addOrReplaceChild("leg21rot", CubeListBuilder.create().texOffs(34, 0).mirror().addBox(0.0F, -0.5F, -1.0F, 4, 1, 2).mirror(false),
                PartPose.offsetAndRotation(3.0F, -2.0F, -3.0F, 0.0F, 0.0F, 0.1745329F));
        leg21rot.addOrReplaceChild("leg22rot", CubeListBuilder.create().texOffs(34, 3).mirror().addBox(0.0F, -0.5F, -1.0F, 2, 1, 2).mirror(false),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4)));
        PartDefinition leg31rot = segment.addOrReplaceChild("leg31rot", CubeListBuilder.create().texOffs(34, 0).mirror().addBox(-4.0F, -0.5F, -1.0F, 4, 1, 2).mirror(false),
                PartPose.offsetAndRotation(-3.0F, -2.0F, 3.0F, 0.0F, 0.0F, -0.1745329F));
        leg31rot.addOrReplaceChild("leg32rot", CubeListBuilder.create().texOffs(34, 3).mirror().addBox(-2.0F, -0.5F, -1.0F, 2, 1, 2).mirror(false),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 4)));
        PartDefinition leg41rot = segment.addOrReplaceChild("leg41rot", CubeListBuilder.create().texOffs(34, 0).mirror().addBox(0.0F, -0.5F, -1.0F, 4, 1, 2).mirror(false),
                PartPose.offsetAndRotation(3.0F, -2.0F, 3.0F, 0.0F, 0.0F, 0.1745329F));
        leg41rot.addOrReplaceChild("leg42rot", CubeListBuilder.create().texOffs(34, 3).mirror().addBox(0.0F, -0.5F, -1.0F, 2, 1, 2).mirror(false),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4)));
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos * 0.6662F;
        float speed = state.walkAnimationSpeed;
        float antenna = -Mth.PI / 12.0F + Mth.PI / 9.0F * Mth.cos(pos + Mth.PI);
        this.antenlefrot2.xRot = antenna;
        this.antenlefrot3.xRot = antenna;
        this.antenrigrot2.xRot = antenna;
        this.antenrigrot3.xRot = antenna;
        float swingA = Mth.cos(pos + Mth.PI) * 1.4F * speed;
        float swingB = Mth.cos(pos) * 1.4F * speed;
        this.legs[0].yRot = swingA;
        this.legs[1].yRot = swingB;
        this.legs[2].yRot = swingA;
        this.legs[3].yRot = swingB;
        for (int i = 0; i < CentipedeEntity.SEGMENTS; i++) {
            ModelPart segment = this.segments[i];
            segment.visible = state.hasSegments;
            segment.x = state.segX[i];
            segment.y = state.segY[i];
            segment.z = state.segZ[i];
            segment.yRot = state.segYaw[i];
            this.segmentLegs[i][0].yRot = swingA;
            this.segmentLegs[i][1].yRot = swingB;
            this.segmentLegs[i][2].yRot = swingA;
            this.segmentLegs[i][3].yRot = swingB;
        }
    }
}
