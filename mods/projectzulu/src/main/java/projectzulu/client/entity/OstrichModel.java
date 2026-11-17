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

/**
 * ModelOstrich (64 x 64 texture): four flat tail plumes, the body with an eight-segment neck ending in the head,
 * two long legs of two segments with three toes, and two wings. Walking the neck bobs, the wings flap open and the
 * legs stride (thigh at half the step frequency, the shin kicking).
 */
public class OstrichModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart[] neck = new ModelPart[8];
    private final ModelPart headrot;
    private final ModelPart legtoprot1;
    private final ModelPart legbotrot1;
    private final ModelPart legtoprot2;
    private final ModelPart legbotrot2;
    private final ModelPart winglefrot;
    private final ModelPart wingrigrot;

    public OstrichModel(ModelPart root) {
        super(root);
        ModelPart bodyrot = root.getChild("bodyrot");
        ModelPart part = bodyrot;
        for (int i = 0; i < 8; i++) {
            part = part.getChild("neck" + (i + 1) + "rot");
            this.neck[i] = part;
        }
        this.headrot = part.getChild("headrot");
        this.legtoprot1 = bodyrot.getChild("legtoprot1");
        this.legbotrot1 = this.legtoprot1.getChild("legbotrot1");
        this.legtoprot2 = bodyrot.getChild("legtoprot2");
        this.legbotrot2 = this.legtoprot2.getChild("legbotrot2");
        this.winglefrot = root.getChild("winglefrot");
        this.wingrigrot = root.getChild("wingrigrot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(12, 8).addBox(-5.0F, -5.0F, 0.0F, 10, 5, 0),
                PartPose.offsetAndRotation(-4.0F, 2.0F, 10.0F, -0.1487144F, 1.041001F, 0.4461433F));
        root.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(12, 19).addBox(-5.0F, -5.0F, 0.0F, 10, 5, 0),
                PartPose.offsetAndRotation(-3.0F, 8.0F, 10.0F, 0.2059489F, 1.343904F, 0.1919862F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(12, 3).addBox(-5.0F, -5.0F, 0.0F, 10, 5, 0),
                PartPose.offsetAndRotation(4.0F, 2.0F, 10.0F, -0.1487144F, -1.041001F, -0.6320364F));
        root.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(12, 14).addBox(-5.0F, -5.0F, 0.0F, 10, 5, 0),
                PartPose.offsetAndRotation(3.0F, 8.0F, 10.0F, 0.2234021F, -1.343904F, -0.1919862F));
        PartDefinition bodyrot = root.addOrReplaceChild("bodyrot", CubeListBuilder.create().mirror()
                        .texOffs(12, 38).addBox(-5.0F, -5.0F, 0.0F, 10, 10, 16)
                        .texOffs(12, 30).addBox(-3.0F, -4.0F, -2.0F, 6, 6, 2)
                        .texOffs(12, 25).addBox(-1.5F, -3.5F, -4.0F, 3, 3, 2),
                PartPose.offset(0.0F, 4.0F, -7.0F));
        CubeListBuilder neckBox = CubeListBuilder.create().mirror().texOffs(22, 26).addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2);
        PartDefinition neck = bodyrot.addOrReplaceChild("neck1rot", neckBox, PartPose.offset(0.0F, -2.0F, -4.0F));
        for (int i = 2; i <= 8; i++) {
            neck = neck.addOrReplaceChild("neck" + i + "rot", neckBox, PartPose.offset(0.0F, 0.0F, -2.0F));
        }
        neck.addOrReplaceChild("headrot", CubeListBuilder.create().mirror()
                        .texOffs(48, 0).addBox(-2.0F, -2.5F, -4.0F, 4, 4, 4)
                        .texOffs(36, 0).addBox(-1.0F, -1.0F, -8.0F, 2, 1, 4)
                        .texOffs(38, 5).addBox(-1.0F, 0.0F, -7.0F, 2, 1, 3),
                PartPose.offset(0.0F, 0.0F, -2.0F));
        leg(bodyrot, 1, -4.0F);
        leg(bodyrot, 2, 4.0F);
        root.addOrReplaceChild("winglefrot", CubeListBuilder.create().mirror()
                .texOffs(32, 14).addBox(-1.0F, -5.0F, 0.0F, 1, 9, 15), PartPose.offset(-5.0F, 4.5F, -7.0F));
        root.addOrReplaceChild("wingrigrot", CubeListBuilder.create().mirror()
                .texOffs(32, 14).addBox(0.0F, -5.0F, 0.0F, 1, 9, 15), PartPose.offset(5.0F, 4.5F, -7.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** LEGTOPROTn -> LEGBOTROTn -> FOOTROTn (no box) with toes n2 (turned in), n3 (back) and n1 (turned out). */
    private static void leg(PartDefinition body, int n, float x) {
        PartDefinition top = body.addOrReplaceChild("legtoprot" + n, CubeListBuilder.create().mirror()
                .texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2, 7, 2), PartPose.offset(x, 5.0F, 9.0F));
        PartDefinition bot = top.addOrReplaceChild("legbotrot" + n, CubeListBuilder.create().mirror()
                .texOffs(0, 51).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2), PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition foot = bot.addOrReplaceChild("footrot" + n, CubeListBuilder.create(), PartPose.offset(0.0F, 7.5F, 0.0F));
        foot.addOrReplaceChild("toe" + n + "2rot", CubeListBuilder.create().mirror().texOffs(0, 61).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2),
                PartPose.offsetAndRotation(-0.5F, 0.0F, -1.0F, 0.0F, Mth.PI / 4.0F, 0.0F));
        foot.addOrReplaceChild("toe" + n + "3rot", CubeListBuilder.create().mirror().texOffs(6, 61).addBox(-0.5F, -0.5F, 0.0F, 1, 1, 2),
                PartPose.offset(0.0F, 0.0F, 1.0F));
        foot.addOrReplaceChild("toe" + n + "1rot", CubeListBuilder.create().mirror().texOffs(0, 61).addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2),
                PartPose.offsetAndRotation(0.5F, 0.0F, -1.0F, 0.0F, -Mth.PI / 4.0F, 0.0F));
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos;
        float amount = state.walkAnimationSpeed;
        float angle = pos * 0.6625F * 2.0F;
        float bob = Mth.cos(angle) + 1.0F;
        this.neck[0].xRot = (-22.5F + Mth.cos(0.0F)) * Mth.DEG_TO_RAD;
        this.neck[1].xRot = (-22.5F + bob) * Mth.DEG_TO_RAD;
        this.neck[2].xRot = (-22.5F + bob) * Mth.DEG_TO_RAD;
        this.neck[3].xRot = (-22.5F + bob) * Mth.DEG_TO_RAD;
        this.neck[4].xRot = (-5.0F + bob) * Mth.DEG_TO_RAD;
        this.neck[5].xRot = (-1.0F + bob) * Mth.DEG_TO_RAD;
        this.neck[6].xRot = (-1.0F + bob) * Mth.DEG_TO_RAD * 1.2F * amount;
        this.neck[7].xRot = (-1.0F + bob) * Mth.DEG_TO_RAD;
        this.winglefrot.yRot = -45.5F * Math.abs(Mth.cos(angle)) * Mth.DEG_TO_RAD;
        this.wingrigrot.yRot = 45.5F * Math.abs(Mth.cos(angle)) * Mth.DEG_TO_RAD;
        float stride = 2.4F * Math.abs((float) Math.log(amount + 1.0F));
        this.legtoprot1.xRot = Mth.cos(pos * 0.6662F / 2.0F + Mth.PI) * stride;
        this.legbotrot1.xRot = Math.abs(Mth.cos(pos * 0.6662F / 2.0F * 2.0F + Mth.HALF_PI) * stride);
        this.legtoprot2.xRot = Mth.cos(pos * 0.6662F / 2.0F) * stride;
        this.legbotrot2.xRot = Math.abs(Mth.cos(pos * 0.6662F / 2.0F * 2.0F) * stride);
        // the original fed the limb swing amount (f1) into the head bob
        this.headrot.xRot = (105.0F + Mth.cos(amount * 0.6625F * 2.0F)) * Mth.DEG_TO_RAD
                + Mth.clamp(state.xRot, -15.0F, 15.0F) * Mth.DEG_TO_RAD;
        this.headrot.yRot = Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
    }
}
