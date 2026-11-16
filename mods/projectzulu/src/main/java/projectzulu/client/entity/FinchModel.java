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
 * ModelFinch: a small bird whose two wings and tail flap with the world time unless it sits still (posture).
 * The head follows the look direction.
 */
public class FinchModel extends EntityModel<BirdRenderState> {
    private final ModelPart body;
    private final ModelPart wingrig;
    private final ModelPart winglef;
    private final ModelPart tail;
    private final ModelPart legrigtoprot;
    private final ModelPart legrigbotrot;
    private final ModelPart legleftoprot;
    private final ModelPart leglefbotrot;
    private final ModelPart headrot;
    private final ModelPart beakrot;

    public FinchModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.wingrig = root.getChild("wingrig");
        this.winglef = root.getChild("winglef");
        this.tail = root.getChild("tail");
        this.legrigtoprot = root.getChild("legrigtoprot");
        this.legrigbotrot = this.legrigtoprot.getChild("legrigbotrot");
        this.legleftoprot = root.getChild("legleftoprot");
        this.leglefbotrot = this.legleftoprot.getChild("leglefbotrot");
        this.headrot = root.getChild("headrot");
        this.beakrot = this.headrot.getChild("beakrot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -2.0F, 2, 2, 4),
                PartPose.offset(0.0F, 21.3F, 0.0F));
        root.addOrReplaceChild("wingrig", CubeListBuilder.create().texOffs(13, 0).addBox(0.0F, 0.0F, -1.5F, 1, 1, 3),
                PartPose.offset(1.0F, 20.3F, 0.0F));
        root.addOrReplaceChild("winglef", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, 0.0F, -1.5F, 1, 1, 3),
                PartPose.offset(-1.0F, 20.3F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(14, 8).addBox(-0.4666667F, 0.0F, 0.0F, 1, 1, 3),
                PartPose.offsetAndRotation(0.0F, 20.8F, 1.0F, 0.3346075F, 0.0F, 0.0F));
        PartDefinition legrigtoprot = root.addOrReplaceChild("legrigtoprot", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-0.5F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(0.5F, 22.3F, 1.0F, -0.6684611F, 0.0F, 0.0F));
        legrigtoprot.addOrReplaceChild("legrigbotrot", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-0.5F, -0.4F, -2.3F, 1, 1, 2),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.6702064F, 0.0F, 0.0F));
        PartDefinition legleftoprot = root.addOrReplaceChild("legleftoprot", CubeListBuilder.create().texOffs(3, 12).mirror().addBox(-0.5F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offsetAndRotation(-0.5F, 22.3F, 1.0F, -0.6702064F, 0.0F, 0.0F));
        legleftoprot.addOrReplaceChild("leglefbotrot", CubeListBuilder.create().texOffs(7, 15).mirror().addBox(-0.5F, -0.4F, -2.3F, 1, 1, 2),
                PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.6702064F, 0.0F, 0.0F));
        PartDefinition headrot = root.addOrReplaceChild("headrot", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2),
                PartPose.offset(0.0F, 19.8F, -2.0F));
        headrot.addOrReplaceChild("beakrot", CubeListBuilder.create().texOffs(9, 9).mirror().addBox(-0.5333334F, 0.3F, -0.9F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7801622F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** setLivingAnimations followed by setRotationAngles of the original. */
    @Override
    public void setupAnim(BirdRenderState state) {
        super.setupAnim(state);
        if (state.entityState != EntityState.POSTURE) {
            this.winglef.zRot = Mth.cos(state.worldTime * 0.6662F * 2.0F) * 1.8F * 0.5F;
            this.wingrig.zRot = Mth.cos(state.worldTime * 0.6662F * 2.0F + (float) Math.PI) * 1.8F * 0.5F;
            this.tail.xRot = Mth.cos(state.worldTime * 0.6662F * 2.0F + (float) Math.PI) * 0.3F;
        }

        this.headrot.xRot = Math.min(Math.max(state.xRot, -15.0F), 15.0F) * (float) (Math.PI / 180.0);
        this.headrot.yRot = Math.min(Math.max(state.yRot, -45.0F), 45.0F) * (float) (Math.PI / 180.0);
    }
}
