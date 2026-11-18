package betterdungeons.client.human;

import betterdungeons.BetterDungeons;
import betterdungeons.mob.MageEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * RenderMage with client.model.ModelMage (a villager-like caster with folded arms and a long robe, 64 x 64), scaled
 * 0.9375; no held items. The texture by the kind (necromancer, liche, pigMage). Client check open.
 */
public class MageRenderer extends MobRenderer<MageEntity, MageRenderer.State, MageRenderer.Model> {
    private final Identifier texture;

    public static class State extends LivingEntityRenderState {
    }

    public MageRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, String texture) {
        super(context, new Model(context.bakeLayer(layer)), 0.5F);
        this.texture = Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "textures/entity/" + texture + ".png");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return this.texture;
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    /** ModelMage. */
    public static class Model extends EntityModel<State> {
        final ModelPart head;
        final ModelPart arms;
        final ModelPart rightLeg;
        final ModelPart leftLeg;

        public Model(ModelPart root) {
            super(root);
            this.head = root.getChild("head");
            this.arms = root.getChild("arms");
            this.rightLeg = root.getChild("right_leg");
            this.leftLeg = root.getChild("left_leg");
        }

        public static LayerDefinition createBodyLayer() {
            CubeDeformation g = CubeDeformation.NONE;
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, g)
                    .texOffs(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F, g), PartPose.ZERO);
            root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, g)
                    .texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, g.extend(0.5F)), PartPose.ZERO);
            // setRotationAngles always put the folded arms at (0, 3, -1), tilted -0.75
            root.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, g)
                    .texOffs(44, 22).addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, g).texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, g),
                    PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
            root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                    PartPose.offset(-2.0F, 12.0F, 0.0F));
            root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, g),
                    PartPose.offset(2.0F, 12.0F, 0.0F));
            return LayerDefinition.create(mesh, 64, 64);
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
            this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
            float f = state.walkAnimationPos;
            float f1 = state.walkAnimationSpeed;
            this.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1 * 0.5F;
            this.leftLeg.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1 * 0.5F;
        }
    }
}
