package projectzulu.client.plant;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import projectzulu.block.plant.UniversalFlowerPotBlockEntity;

/**
 * TileEntityUniversalFlowerPotRenderer: a block in the pot is drawn as a small block (scaled so that it is 0.29 wide),
 * any other item as a dropped item at 0.6 size.
 */
public class UniversalFlowerPotRenderer implements BlockEntityRenderer<UniversalFlowerPotBlockEntity, UniversalFlowerPotRenderer.State> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        final BlockModelRenderState block = new BlockModelRenderState();
        final ItemStackRenderState item = new ItemStackRenderState();
        float blockScale;
        boolean isBlock;
    }

    public UniversalFlowerPotRenderer(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(UniversalFlowerPotBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        ItemStack stack = entity.getStack();
        state.isBlock = false;
        state.block.clear();
        state.item.clear();
        if (stack.isEmpty()) {
            return;
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            BlockState block = blockItem.getBlock().defaultBlockState();
            if (block.getRenderShape() != RenderShape.INVISIBLE) {
                AABB bounds = block.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty() ? new AABB(0, 0, 0, 1, 1, 1)
                        : block.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).bounds();
                double width = Math.max(bounds.maxX - bounds.minX, 0.1);
                state.blockScale = (float) (0.29 / width);
                this.blockModelResolver.update(state.block, block, BLOCK_DISPLAY_CONTEXT);
                state.isBlock = !state.block.isEmpty();
                if (state.isBlock) {
                    return;
                }
            }
        }
        this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.GROUND, entity.getLevel(), null,
                (int) entity.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.isBlock) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.25F, 0.5F);
            poseStack.scale(state.blockScale, state.blockScale, state.blockScale);
            poseStack.translate(-0.5F, 0.0F, -0.5F);
            state.block.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        } else if (!state.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.3F, 0.5F);
            poseStack.scale(0.6F, 0.6F, 0.6F);
            state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
