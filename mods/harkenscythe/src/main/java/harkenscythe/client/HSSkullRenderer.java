package harkenscythe.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import harkenscythe.HarkenScythe;
import harkenscythe.block.HSSkullBlock;
import harkenscythe.block.HSSkullBlockEntity;
import harkenscythe.item.SkullType;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * RenderHSSkull: the 8x8x8 head of ModelHSSkull with the texture and scale of the kind, on the floor turned in 16
 * steps or against a wall (raised by 0.25 and pushed to the wall like vanilla skulls). The ender dragon skull gets its
 * snout, two nostrils and two horns (enderDragonAcc), each a scaled head cube with its own texture.
 */
public class HSSkullRenderer implements BlockEntityRenderer<HSSkullBlockEntity, HSSkullRenderer.State> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(HarkenScythe.id("skull"), "main");
    private static final Identifier SNOUT = texture("enderdragonsnout");
    private static final Identifier NOSE = texture("enderdragonnose");
    private static final Identifier HORN = texture("enderdragonhorn");
    private final ModelPart head;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.UP;
        int rotation;
        SkullType type = SkullType.SPIDER;
    }

    public HSSkullRenderer(BlockEntityRendererProvider.Context context) {
        this.head = context.bakeLayer(LAYER).getChild("head");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static Identifier texture(String name) {
        return HarkenScythe.id("textures/entity/skull/" + name + ".png");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HSSkullBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getBlockState().getValue(HSSkullBlock.FACING);
        state.rotation = entity.getRotation();
        state.type = SkullType.values()[entity.getSkullType()];
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        boolean wall = state.facing != Direction.UP;
        poseStack.pushPose();
        if (wall) {
            poseStack.translate(0.5F - state.facing.getStepX() * 0.25F, 0.25F, 0.5F - state.facing.getStepZ() * 0.25F);
            poseStack.rotateDegrees(Axis.YP, -state.facing.getOpposite().toYRot());
        } else {
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.rotateDegrees(Axis.YP, -state.rotation * 22.5F);
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.part(poseStack, collector, state, HarkenScythe.id("textures/entity/equipment/humanoid/skull_" + state.type.id() + ".png"), 0, 0, 0, state.type.scaleX, state.type.scaleYZ);
        if (state.type == SkullType.ENDER_DRAGON) {
            // positions relative to the head in its own (mirrored) frame, from the translations of enderDragonAcc
            this.part(poseStack, collector, state, SNOUT, 0.0F, wall ? 0.24F : 0.0F, wall ? -1.0F : -0.76F, 1.2F, 1.2F);
            this.part(poseStack, collector, state, NOSE, -0.15F, wall ? -0.3F : -0.55F, wall ? -1.1F : -0.86F, 0.3F, 0.3F);
            this.part(poseStack, collector, state, NOSE, 0.15F, wall ? -0.3F : -0.55F, wall ? -1.1F : -0.86F, 0.3F, 0.3F);
            this.part(poseStack, collector, state, HORN, -0.2F, wall ? -0.75F : -1.0F, wall ? -0.31F : 0.0F, 0.4F, 0.4F);
            this.part(poseStack, collector, state, HORN, 0.2F, wall ? -0.75F : -1.0F, wall ? -0.31F : 0.0F, 0.4F, 0.4F);
        }
        poseStack.popPose();
    }

    private void part(PoseStack poseStack, SubmitNodeCollector collector, State state, Identifier texture, float x, float y, float z, float scaleX,
            float scaleYZ) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scaleX, scaleYZ, scaleYZ);
        collector.submitModelPart(this.head, poseStack, RenderTypes.entityCutout(texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }
}
