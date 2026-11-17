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
import java.util.function.UnaryOperator;

/**
 * ModelHorse (64x64): two stacked body boxes with the tail, four legs of two segments, and the neck carrying head and
 * ears. The original drew everything 1.5 times larger about the model origin (render(f5 * 1.5F)); here the mesh is
 * scaled the same way. Walking swings the legs (lower segments only forward), the tail sideways and nods the neck;
 * the head does not follow the look direction.
 * <p>
 * Child: neck drawn at 1.3/2, the rest at 1/2, each shifted forward and down (the GL11 translations of render);
 * shifted 3 px further down here, the original left the foal floating that much above the ground.
 */
public class ZuluHorseModel extends EntityModel<ZuluHorseModel.State> {
    private static final float RAISE = 8.0F;
    private static final float NECK_X_ROT = Mth.PI / 4.0F;

    private final ModelPart neck;
    private final ModelPart tail;
    private final ModelPart leg1top;
    private final ModelPart leg2top;
    private final ModelPart leg3top;
    private final ModelPart leg4top;
    private final ModelPart leg1bot;
    private final ModelPart leg2bot;
    private final ModelPart leg3bot;
    private final ModelPart leg4bot;

    public static class State extends LivingEntityRenderState {
        public boolean saddled;
    }

    public ZuluHorseModel(ModelPart root) {
        super(root);
        this.neck = root.getChild("neckrot");
        this.tail = root.getChild("tail");
        this.leg1top = root.getChild("leg1toprot");
        this.leg2top = root.getChild("leg2toprot");
        this.leg3top = root.getChild("leg3toprot");
        this.leg4top = root.getChild("leg4toprot");
        this.leg1bot = this.leg1top.getChild("leg1botrot");
        this.leg2bot = this.leg2top.getChild("leg2botrot");
        this.leg3bot = this.leg3top.getChild("leg3botrot");
        this.leg4bot = this.leg4top.getChild("leg4botrot");
    }

    /**
     * The mesh with every top-level part placed by {@code body} (body, tail, legs) or {@code neck}; the children follow
     * the scale of their parent part.
     */
    private static MeshDefinition createMesh(UnaryOperator<PartPose> body, UnaryOperator<PartPose> neck) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // body1, tail and body2 set mirror only after addBox: not mirrored
        root.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -2.0F, -10.0F, 7, 10, 15),
                body.apply(PartPose.offset(0.0F, 6.0F - RAISE, 3.0F)));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 25).addBox(-1.0F, -1.0F, 0.0F, 2, 10, 2),
                body.apply(PartPose.offset(0.0F, 6.0F - RAISE, 8.0F)));
        root.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(0, 25).addBox(-4.5F, -1.1F, -9.0F, 9, 8, 13),
                body.apply(PartPose.offset(0.0F, 6.0F - RAISE, 3.0F)));
        leg(root, body, 1, -2.0F, -4.5F, 0, -1.5F);
        leg(root, body, 2, 2.0F, -4.5F, 14, -1.5F);
        leg(root, body, 3, -2.0F, 5.5F, 28, -1.5F);
        leg(root, body, 4, 2.0F, 5.5F, 42, -1.0F);
        PartDefinition neckrot = root.addOrReplaceChild("neckrot", CubeListBuilder.create().mirror()
                        .texOffs(48, 0).addBox(-1.5F, -5.5F, -2.5F, 3, 9, 5)
                        .texOffs(46, 21).addBox(-1.0F, -5.5F, 2.5F, 2, 7, 1),
                neck.apply(PartPose.offsetAndRotation(0.0F, 3.0F - RAISE, -6.0F, NECK_X_ROT, 0.0F, 0.0F)));
        neckrot.addOrReplaceChild("earrot", CubeListBuilder.create().mirror()
                        .texOffs(53, 18).addBox(1.0F, -1.0F, -0.5F, 1, 2, 1)
                        .texOffs(58, 18).addBox(-2.0F, -1.0F, -0.5F, 1, 2, 1),
                PartPose.offsetAndRotation(0.0F, -5.5F, 2.5F, -Mth.PI / 4.0F, 0.0F, 0.0F));
        neckrot.addOrReplaceChild("headrot", CubeListBuilder.create().mirror()
                        .texOffs(33, 0).addBox(-2.0F, -1.0F, -2.0F, 4, 8, 3)
                        .texOffs(46, 17).addBox(-1.0F, -1.5F, -3.0F, 2, 3, 1),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -1.570796F, 0.0F, 0.0F));
        return mesh;
    }

    /** LEGnTOPROT with its LEGnBOTROT child; leg 4 has its boxes shifted by half a pixel ("-1.0F" corners). */
    private static void leg(PartDefinition root, UnaryOperator<PartPose> body, int n, float x, float z, int u, float corner) {
        PartDefinition top = root.addOrReplaceChild("leg" + n + "toprot", CubeListBuilder.create().mirror()
                .texOffs(u, 48).addBox(corner, 0.0F, corner, 3, 4, 3), body.apply(PartPose.offset(x, 14.0F - RAISE, z)));
        top.addOrReplaceChild("leg" + n + "botrot", CubeListBuilder.create().mirror()
                .texOffs(u, 53).addBox(corner, 0.0F, corner, 3, 6, 3), PartPose.offset(0.0F, 4.0F, 0.0F));
    }

    /** render(f5 * 1.5F): scaled about the model origin, which leaves the hooves on the ground. */
    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(createMesh(pose -> pose.scaled(1.5F), pose -> pose.scaled(1.5F)), 64, 64);
    }

    /**
     * The isChild branch of render: glScalef(1.3/2) + glTranslatef(0, 11, 9) for the neck, glScalef(1/2) +
     * glTranslatef(0, 12, 10) for the rest (both inside the 1.5 scale), plus 3 px down to the ground.
     */
    public static LayerDefinition createBabyLayer() {
        float neckScale = 1.5F * 1.3F / 2.0F;
        float bodyScale = 1.5F / 2.0F;
        return LayerDefinition.create(createMesh(
                pose -> pose.scaled(bodyScale).translated(0.0F, bodyScale * 12.0F + 3.0F, bodyScale * 10.0F),
                pose -> pose.scaled(neckScale).translated(0.0F, neckScale * 11.0F + 3.0F, neckScale * 9.0F)), 64, 64);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos * 0.6662F;
        float amount = 1.8F * (float) Math.abs(Math.log(state.walkAnimationSpeed + 1.0F));
        float back = Mth.cos(pos + Mth.PI) * amount;
        float front = Mth.cos(pos) * amount;
        this.neck.xRot = NECK_X_ROT + 0.12217305F * back;
        this.tail.zRot = 0.5F * back;
        this.leg1top.xRot = back;
        this.leg2top.xRot = front;
        this.leg3top.xRot = back;
        this.leg4top.xRot = front;
        float lowerBack = Math.abs(Mth.cos(pos / 2.0F + Mth.PI) * amount);
        float lowerFront = Math.abs(Mth.cos(pos / 2.0F) * amount);
        this.leg1bot.xRot = lowerBack;
        this.leg2bot.xRot = lowerFront;
        this.leg3bot.xRot = lowerBack;
        this.leg4bot.xRot = lowerFront;
    }
}
