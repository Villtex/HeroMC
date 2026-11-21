package twilightforest.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;


/** ModelTFSlimeBeetle: a beetle carrying a slime on its curled tail; the clear slime cube is {@link Cube}. */
public class SlimeBeetleModel extends EntityModel<net.minecraft.client.renderer.entity.state.LivingEntityRenderState> {
    final ModelPart head;
    final ModelPart leg1;
    final ModelPart leg2;
    final ModelPart leg3;
    final ModelPart leg4;
    final ModelPart leg5;
    final ModelPart leg6;
    final ModelPart tail1;
    final ModelPart tail2;
    final ModelPart slimeCenter;

    private final ModelPart slimeCube;
    private final boolean pass;

    public SlimeBeetleModel(ModelPart root) {
        this(root, false);
    }

    /** pass: the render pass model (only the tail and the clear slime cube, drawn translucent). */
    public SlimeBeetleModel(ModelPart root, boolean pass) {
        super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityTranslucent);
        this.pass = pass;
        this.head = root.getChild("head");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.leg5 = root.getChild("leg5");
        this.leg6 = root.getChild("leg6");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail1").getChild("tail2");
        this.slimeCenter = root.getChild("tail1").getChild("tail2").getChild("slimeCenter");
        this.slimeCube = root.getChild("tail1").getChild("tail2").getChild("slimeCube");
        for (var child : java.util.List.of("head", "rear_end", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "connector1")) {
            root.getChild(child).visible = !pass;
        }
        this.slimeCenter.visible = !pass;
        this.slimeCube.visible = pass;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition tail1 = root.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 19.0F, 9.0F));
        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 2.0F));
        tail2.addOrReplaceChild("slimeCenter", CubeListBuilder.create()
                .texOffs(32, 24).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -6.0F, 0.0F));
        tail2.addOrReplaceChild("slimeCube", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-6.0F, -12.0F, -9.0F, 12.0F, 12.0F, 12.0F),
                PartPose.offset(0.0F, -6.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 19.0F, -5.0F));
        head.addOrReplaceChild("antenna1", CubeListBuilder.create()
                .texOffs(38, 4).addBox(0.0F, -0.5F, -0.5F, 12.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(1.0F, -3.0F, -5.0F, 0.0F, 1.047198F, -0.296706F));
        head.addOrReplaceChild("antenna2", CubeListBuilder.create()
                .texOffs(38, 4).addBox(0.0F, -0.5F, -0.5F, 12.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-1.0F, -3.0F, -5.0F, 0.0F, 2.094395F, 0.296706F));
        head.addOrReplaceChild("eye1", CubeListBuilder.create()
                .texOffs(15, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(-3.0F, -2.0F, -5.0F));
        head.addOrReplaceChild("eye2", CubeListBuilder.create()
                .texOffs(15, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(3.0F, -2.0F, -5.0F));
        head.addOrReplaceChild("mouth", CubeListBuilder.create()
                .texOffs(17, 12).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 1.0F, -6.0F));
        root.addOrReplaceChild("rear_end", CubeListBuilder.create()
                .texOffs(31, 6).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 18.0F, 7.0F, 1.570796F, 0.0F, 0.0F));
        root.addOrReplaceChild("leg6", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(2.0F, 21.0F, -4.0F, 0.0F, 0.2792527F, 0.3490659F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(2.0F, 21.0F, -1.0F, 0.0F, -0.2792527F, 0.3490659F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(2.0F, 21.0F, 4.0F, 0.0F, (float) (-Math.PI * 2.0 / 9.0), 0.3490659F));
        root.addOrReplaceChild("leg5", CubeListBuilder.create().mirror()
                .texOffs(40, 0).addBox(-9.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, 21.0F, -4.0F, 0.0F, -0.2792527F, -0.3490659F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().mirror()
                .texOffs(40, 0).addBox(-9.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, 21.0F, -1.0F, 0.0F, 0.2792527F, -0.3490659F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().mirror()
                .texOffs(40, 0).addBox(-9.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, 21.0F, 4.0F, 0.0F, (float) (Math.PI * 2.0 / 9.0), -0.3490659F));
        root.addOrReplaceChild("connector1", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 6.0F, 1.0F),
                PartPose.offset(0.0F, 19.0F, -4.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        BeetleLegs.animate(state.walkAnimationPos, state.walkAnimationSpeed, this.leg1, this.leg2, this.leg3, this.leg4, this.leg5, this.leg6);
        float f2 = state.ageInTicks;
        this.tail1.xRot = Mth.cos(f2 * 0.3335F) * 0.15F;
        this.tail2.xRot = Mth.cos(f2 * 0.4445F) * 0.2F;
        this.slimeCenter.xRot = Mth.cos(f2 * 0.5555F + 0.25F) * 0.25F;
        this.slimeCube.xRot = Mth.cos(f2 * 0.5555F) * 0.25F;
    }
}
