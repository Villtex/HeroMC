package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * ModelSandWorm: a head with a four-part beak (top, bottom, left, right jaws of stacked teeth) and a tail of seven
 * segments, drawn at twice the size (render(2 * scale), scaled about the model origin as in 1.5.2). While buried
 * (idle) everything is still; otherwise the jaws snap open and shut and the segments snake with the walk.
 */
public class SandwormModel extends EntityModel<SandwormModel.State> {
    private final ModelPart topmorot;
    private final ModelPart botmorot;
    private final ModelPart lefmorot;
    private final ModelPart rigmorot;
    private final ModelPart[] rear = new ModelPart[7];

    public static class State extends net.minecraft.client.renderer.entity.state.LivingEntityRenderState {
        public boolean hidden;
    }

    public SandwormModel(ModelPart root) {
        super(root);
        ModelPart headbase = root.getChild("headbase");
        this.topmorot = headbase.getChild("topmorot");
        this.botmorot = headbase.getChild("botmorot");
        this.lefmorot = headbase.getChild("lefmorot");
        this.rigmorot = headbase.getChild("rigmorot");
        ModelPart part = root.getChild("rearrot");
        this.rear[0] = part;
        for (int i = 1; i < 7; i++) {
            part = part.getChild("rearrot" + (i + 1));
            this.rear[i] = part;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition headbase = root.addOrReplaceChild("headbase", CubeListBuilder.create(),
                PartPose.offset(0.0F, 8.0F, -6.0F));
        headbase.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 4),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition lefmorot = headbase.addOrReplaceChild("lefmorot", CubeListBuilder.create(),
                PartPose.offset(-3.0F, 0.0F, -4.0F));
        lefmorot.addOrReplaceChild("lefmo8", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, 0.0F, 0.0F, 1, 8, 1),
                PartPose.offset(-1.0F, -4.0F, -1.0F));
        lefmorot.addOrReplaceChild("lefmo6", CubeListBuilder.create().texOffs(4, 10).addBox(0.0F, 0.0F, 0.0F, 1, 6, 1),
                PartPose.offset(0.0F, -3.0F, -1.0F));
        lefmorot.addOrReplaceChild("lefmo4", CubeListBuilder.create().texOffs(8, 10).addBox(0.0F, 0.0F, 0.0F, 1, 4, 1),
                PartPose.offset(1.0F, -2.0F, -1.0F));
        lefmorot.addOrReplaceChild("lefmo2", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offset(2.0F, -1.0F, 0.0F));
        lefmorot.addOrReplaceChild("lefmot2", CubeListBuilder.create().texOffs(12, 13).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(1.0F, -2.0F, 0.0F));
        lefmorot.addOrReplaceChild("lefmot1", CubeListBuilder.create().texOffs(12, 13).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(1.0F, 1.0F, 0.0F));
        PartDefinition rigmorot = headbase.addOrReplaceChild("rigmorot", CubeListBuilder.create(),
                PartPose.offset(3.0F, 0.0F, -4.0F));
        rigmorot.addOrReplaceChild("rigmo8", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, 0.0F, 0.0F, 1, 8, 1),
                PartPose.offset(0.0F, -4.0F, -1.0F));
        rigmorot.addOrReplaceChild("rigmo6", CubeListBuilder.create().texOffs(4, 10).addBox(0.0F, 0.0F, 0.0F, 1, 6, 1),
                PartPose.offset(-1.0F, -3.0F, -1.0F));
        rigmorot.addOrReplaceChild("rigmo4", CubeListBuilder.create().texOffs(8, 10).addBox(0.0F, 0.0F, 0.0F, 1, 4, 1),
                PartPose.offset(-2.0F, -2.0F, -1.0F));
        rigmorot.addOrReplaceChild("rigmo2", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1),
                PartPose.offset(-3.0F, -1.0F, -1.0F));
        rigmorot.addOrReplaceChild("rigmot1", CubeListBuilder.create().texOffs(12, 13).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-2.0F, -2.0F, 0.0F));
        rigmorot.addOrReplaceChild("rigmot2", CubeListBuilder.create().texOffs(12, 13).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-2.0F, 1.0F, 0.0F));
        PartDefinition topmorot = headbase.addOrReplaceChild("topmorot", CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, -4.0F));
        topmorot.addOrReplaceChild("topmo8", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, -1.0F, 0.0F, 8, 1, 1),
                PartPose.offset(-4.0F, 0.0F, -1.0F));
        topmorot.addOrReplaceChild("topmo6", CubeListBuilder.create().texOffs(0, 21).addBox(0.0F, 0.0F, 0.0F, 6, 1, 1),
                PartPose.offset(-3.0F, 0.0F, -1.0F));
        topmorot.addOrReplaceChild("topmo4", CubeListBuilder.create().texOffs(0, 23).addBox(0.0F, 0.0F, 0.0F, 4, 1, 1),
                PartPose.offset(-2.0F, 1.0F, -1.0F));
        topmorot.addOrReplaceChild("topmo2", CubeListBuilder.create().texOffs(0, 25).addBox(0.0F, 0.0F, 0.0F, 2, 1, 1),
                PartPose.offset(-1.0F, 2.0F, -1.0F));
        topmorot.addOrReplaceChild("topmot1", CubeListBuilder.create().texOffs(7, 25).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-2.0F, 1.0F, 0.0F));
        topmorot.addOrReplaceChild("topmot2", CubeListBuilder.create().texOffs(7, 25).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(1.0F, 1.0F, 0.0F));
        PartDefinition botmorot = headbase.addOrReplaceChild("botmorot", CubeListBuilder.create(),
                PartPose.offset(0.0F, 3.0F, -4.0F));
        botmorot.addOrReplaceChild("botmo4", CubeListBuilder.create().texOffs(0, 23).addBox(0.0F, 0.0F, 0.0F, 4, 1, 1),
                PartPose.offset(-2.0F, -2.0F, -1.0F));
        botmorot.addOrReplaceChild("botmo6", CubeListBuilder.create().texOffs(0, 21).addBox(0.0F, 0.0F, 0.0F, 6, 1, 1),
                PartPose.offset(-3.0F, -1.0F, -1.0F));
        botmorot.addOrReplaceChild("botmo2", CubeListBuilder.create().texOffs(0, 25).addBox(0.0F, 0.0F, 0.0F, 2, 1, 1),
                PartPose.offset(-1.0F, -3.0F, -1.0F));
        botmorot.addOrReplaceChild("botmo8", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 0.0F, 8, 1, 1),
                PartPose.offset(-4.0F, 0.0F, -1.0F));
        botmorot.addOrReplaceChild("botmot1", CubeListBuilder.create().texOffs(7, 25).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(-2.0F, -2.0F, 0.0F));
        botmorot.addOrReplaceChild("botmot2", CubeListBuilder.create().texOffs(7, 25).addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(1.0F, -2.0F, 0.0F));
        PartDefinition rearrot = root.addOrReplaceChild("rearrot", CubeListBuilder.create(),
                PartPose.offset(0.0F, 8.0F, -6.0F));
        rearrot.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(14, 20).addBox(-4.0F, -4.0F, -2.0F, 8, 8, 4),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        PartDefinition rearrot2 = rearrot.addOrReplaceChild("rearrot2", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        rearrot2.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(14, 20).addBox(-4.0F, -4.0F, -2.0F, 8, 8, 4),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        PartDefinition rearrot3 = rearrot2.addOrReplaceChild("rearrot3", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        rearrot3.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(14, 20).addBox(-4.0F, -4.0F, -2.0F, 8, 8, 4),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        PartDefinition rearrot4 = rearrot3.addOrReplaceChild("rearrot4", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        rearrot4.addOrReplaceChild("body4", CubeListBuilder.create().texOffs(14, 20).addBox(-4.0F, -4.0F, -2.0F, 8, 8, 4),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        PartDefinition rearrot5 = rearrot4.addOrReplaceChild("rearrot5", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        rearrot5.addOrReplaceChild("body5", CubeListBuilder.create().texOffs(39, 20).addBox(-4.0F, -4.0F, -2.0F, 8, 8, 4),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        PartDefinition rearrot6 = rearrot5.addOrReplaceChild("rearrot6", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        rearrot6.addOrReplaceChild("body6", CubeListBuilder.create().texOffs(39, 20).addBox(-4.0F, -4.0F, -2.0F, 8, 8, 4),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        PartDefinition rearrot7 = rearrot6.addOrReplaceChild("rearrot7", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        rearrot7.addOrReplaceChild("body7", CubeListBuilder.create().texOffs(39, 20).addBox(-4.0F, -4.0F, -2.0F, 8, 8, 4),
                PartPose.offset(0.0F, 0.0F, 2.0F));
        return LayerDefinition.create(mesh, 64, 32).apply(m -> m.transformed(pose -> pose.scaled(2.0F)));
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        if (state.hidden) {
            return;
        }
        float pos = state.walkAnimationPos * 0.6662F;
        float speed = state.walkAnimationSpeed;
        this.topmorot.xRot = Mth.cos(pos - Mth.PI) * 2.0F * speed - Mth.HALF_PI;
        this.lefmorot.yRot = Mth.cos(pos - Mth.PI) * 2.0F * speed + Mth.HALF_PI;
        this.rigmorot.yRot = Mth.cos(pos) * 2.0F * speed - Mth.HALF_PI;
        this.botmorot.xRot = Mth.cos(pos) * 2.0F * speed + Mth.HALF_PI;
        for (int i = 0; i < 7; i++) {
            this.rear[i].yRot = Mth.cos(pos - i * Mth.PI / 3.0F) * 0.5F * speed;
        }
    }
}
