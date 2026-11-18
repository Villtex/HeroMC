package betterdungeons.client.mob;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * ModelTurtle (the shell: a 16 x 7 x 16 block with a plate and four ridges on top, 64x32, drawn three times its size)
 * and ModelTurtlePart (one 4 x 4 x 4 cube, the head at 0, 0 of the texture or a leg at 0, 24, also tripled and 0.2
 * blocks lower). No animation.
 */
public final class TurtleModel {
    private TurtleModel() {
    }

    public static LayerDefinition createShellLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 7.0F, 16.0F), PartPose.ZERO);
        root.addOrReplaceChild("Shape1", CubeListBuilder.create().texOffs(18, 2).addBox(1.0F, -2.0F, 0.0F, 5.0F, 1.0F, 6.0F), PartPose.ZERO);
        root.addOrReplaceChild("Shape2", CubeListBuilder.create().texOffs(18, 9).addBox(0.0F, -2.0F, -6.0F, 6.0F, 1.0F, 5.0F), PartPose.ZERO);
        root.addOrReplaceChild("Shape3", CubeListBuilder.create().texOffs(13, 2).addBox(-6.0F, -2.0F, 1.0F, 6.0F, 1.0F, 5.0F), PartPose.ZERO);
        root.addOrReplaceChild("Shape4", CubeListBuilder.create().texOffs(12, 7).addBox(-6.0F, -2.0F, -6.0F, 5.0F, 1.0F, 6.0F), PartPose.ZERO);
        root.addOrReplaceChild("Shape5", CubeListBuilder.create().texOffs(2, 1).addBox(-7.0F, -1.0F, -7.0F, 14.0F, 1.0F, 14.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createPartLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 24).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** One part as the renderer draws it (submitted models are posed later, so each needs its own state). */
    public static class PartState extends EntityRenderState {
        public boolean head;
        public boolean visible;
        public double dx;
        public double dy;
        public double dz;
        public float yaw;
    }

    /** The shell: render() pushed glScalef(3, 3, 3). */
    public static class Shell extends EntityModel<EntityRenderState> {
        public Shell(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(EntityRenderState state) {
            super.setupAnim(state);
            this.root.xScale = 3.0F;
            this.root.yScale = 3.0F;
            this.root.zScale = 3.0F;
        }
    }

    /** A part: glTranslatef(0, 0.2, 0), glScalef(3, 3, 3), then the head or the leg cube. */
    public static class Part extends EntityModel<PartState> {
        private final ModelPart leg;
        private final ModelPart head;

        public Part(ModelPart root) {
            super(root);
            this.leg = root.getChild("leg");
            this.head = root.getChild("head");
        }

        @Override
        public void setupAnim(PartState state) {
            super.setupAnim(state);
            this.root.y = 3.2F;
            this.root.xScale = 3.0F;
            this.root.yScale = 3.0F;
            this.root.zScale = 3.0F;
            this.head.visible = state.head;
            this.leg.visible = !state.head;
        }
    }
}
