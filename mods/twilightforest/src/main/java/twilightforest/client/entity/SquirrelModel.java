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
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** ModelTFSquirrel: the bushy tail wags harder when it runs. */
public class SquirrelModel extends EntityModel<LivingEntityRenderState> {
    final ModelPart leg1;
    final ModelPart leg2;
    final ModelPart leg3;
    final ModelPart leg4;
    final ModelPart head;
    final ModelPart tail;
    final ModelPart fluff2;
    final ModelPart fluff3;

    public SquirrelModel(ModelPart root) {
        super(root);
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
        this.fluff2 = root.getChild("tail").getChild("fluff1").getChild("fluff2");
        this.fluff3 = root.getChild("tail").getChild("fluff1").getChild("fluff2").getChild("fluff3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 8).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 3.0F, 5.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-2.0F, 23.0F, 2.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(1.0F, 23.0F, 2.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-2.0F, 23.0F, -2.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(1.0F, 23.0F, -2.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -5.0F, -3.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(16, 0).addBox(-2.0F, -6.0F, -0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(16, 0).addBox(1.0F, -6.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 22.0F, -2.0F));
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-0.5F, -1.5F, 0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 21.0F, 2.0F));
        PartDefinition fluff1 = tail.addOrReplaceChild("fluff1", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-1.5F, -4.0F, 1.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition fluff2 = fluff1.addOrReplaceChild("fluff2", CubeListBuilder.create()
                .texOffs(0, 20).addBox(0.0F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(-1.5F, -4.0F, 2.5F));
        fluff2.addOrReplaceChild("fluff3", CubeListBuilder.create()
                .texOffs(0, 26).addBox(1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offset(-1.5F, -3.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float f = state.walkAnimationPos;
        float f1 = state.walkAnimationSpeed;
        float f2 = state.ageInTicks;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.leg1.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        this.leg2.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
        this.leg3.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
        this.leg4.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        if (f1 > 0.2F) {
            float wiggle = Math.min(f1, 0.6F);
            this.tail.xRot = (Mth.cos(f2 * 0.6662F) * 1.0F - Mth.PI / 3.0F) * wiggle;
            this.fluff2.xRot = Mth.cos(f2 * 0.7774F) * 1.2F * wiggle;
            this.fluff3.xRot = Mth.cos(f2 * 0.8886F + Mth.PI / 2.0F) * 1.4F * wiggle;
        } else {
            this.tail.xRot = 0.2F + Mth.cos(f2 * 0.3335F) * 0.15F;
            this.fluff2.xRot = 0.1F + Mth.cos(f2 * 0.4445F) * 0.2F;
            this.fluff3.xRot = 0.1F + Mth.cos(f2 * 0.5555F) * 0.25F;
        }
    }
}
