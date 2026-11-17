package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import projectzulu.entity.ZuluBigAnimal;

import java.util.function.Function;

/**
 * ModelGiraffe: body, long legs, tail and a neck of seven segments up to the head. While attacking (animTime) the front
 * legs kick up and out. ModelGiraffe had no isChild branch, so a young giraffe renders at full size (as in 1.5.2).
 */
public class GiraffeModel extends EntityModel<BigAnimalRenderer.State> {
    private final ModelPart headrot;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart tail;

    public GiraffeModel(ModelPart root) {
        super(root);
        Function<String, ModelPart> parts = root.createPartLookup();
        this.headrot = parts.apply("HEADROT");
        this.leg1 = parts.apply("leg1");
        this.leg2 = parts.apply("leg2");
        this.leg3 = parts.apply("leg3");
        this.leg4 = parts.apply("leg4");
        this.tail = parts.apply("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-7.0F, -16.0F, -23.0F, 14.0F, 16.0F, 24.0F),
                PartPose.offset(0.0F, 5.0F, 11.0F));
        root.addOrReplaceChild("body2", CubeListBuilder.create()
                .texOffs(56, 12).addBox(-5.0F, -28.0F, 2.0F, 10.0F, 21.0F, 12.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 11.0F, 1.047198F, 0.0F, 0.0F));
        root.addOrReplaceChild("body3", CubeListBuilder.create()
                .texOffs(92, 0).addBox(-4.5F, -26.0F, -23.0F, 11.0F, 10.0F, 6.0F),
                PartPose.offset(-1.0F, 5.0F, 11.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 19.0F, 4.0F),
                PartPose.offset(-5.0F, 5.0F, -9.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 19.0F, 4.0F),
                PartPose.offset(5.0F, 5.0F, -9.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create()
                .texOffs(36, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 19.0F, 4.0F),
                PartPose.offset(-5.0F, 5.0F, 9.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 19.0F, 4.0F),
                PartPose.offset(5.0F, 5.0F, 9.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 17.0F, 2.0F),
                PartPose.offset(0.0F, -8.0F, 12.0F));
        PartDefinition NECKROT1 = root.addOrReplaceChild("NECKROT1", CubeListBuilder.create().mirror()
                .texOffs(103, 36).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -21.0F, -9.0F));
        PartDefinition NECKROT2 = NECKROT1.addOrReplaceChild("NECKROT2", CubeListBuilder.create().mirror()
                .texOffs(103, 33).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition NECKROT3 = NECKROT2.addOrReplaceChild("NECKROT3", CubeListBuilder.create().mirror()
                .texOffs(103, 30).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition NECKROT4 = NECKROT3.addOrReplaceChild("NECKROT4", CubeListBuilder.create().mirror()
                .texOffs(103, 27).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition NECKROT5 = NECKROT4.addOrReplaceChild("NECKROT5", CubeListBuilder.create().mirror()
                .texOffs(103, 24).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition NECKROT6 = NECKROT5.addOrReplaceChild("NECKROT6", CubeListBuilder.create().mirror()
                .texOffs(103, 21).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition NECKROT7 = NECKROT6.addOrReplaceChild("NECKROT7", CubeListBuilder.create().mirror()
                .texOffs(103, 18).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        NECKROT7.addOrReplaceChild("HEADROT", CubeListBuilder.create().mirror()
                .texOffs(81, 46).addBox(-3.5F, -6.0F, -9.0F, 7.0F, 6.0F, 12.0F)
                .texOffs(82, 52).addBox(-2.0F, -8.0F, 1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(115, 52).addBox(1.0F, -8.0F, 1.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -1.0F, 1.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(BigAnimalRenderer.State state) {
        super.setupAnim(state);
        float swing = Mth.cos(state.walkAnimationPos * 0.6662F + Mth.PI) * 1.8F * BigModelParts.logSwing(state.walkAnimationSpeed);
        this.tail.zRot = swing;
        this.leg1.xRot = swing;
        this.leg1.zRot = 0.0F;
        this.leg3.xRot = swing;
        this.leg2.xRot = -swing;
        this.leg2.zRot = 0.0F;
        this.leg4.xRot = -swing;
        if (state.animTime > 0) {
            float time = state.animTime;
            float max = ZuluBigAnimal.MAX_ANIM_TIME;
            this.leg1.xRot = BigModelParts.map(time, 0.0F, max, 0.0F, -2.268928F);
            this.leg1.zRot = BigModelParts.map(time, 0.0F, max, 0.0F, -Mth.PI / 6.0F);
            this.leg2.xRot = BigModelParts.map(time, 0.0F, max, 0.0F, -2.268928F);
            this.leg2.zRot = BigModelParts.map(time, 0.0F, max, 0.0F, Mth.PI / 6.0F);
        }
        this.headrot.xRot = BigModelParts.clampDeg(state.xRot, -45.0F, 30.0F);
        this.headrot.yRot = BigModelParts.clampDeg(state.yRot, -45.0F, 45.0F);
    }
}
