package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import slimeknights.mantle.Mantle;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderer for an inventory block entity, the item locations come from {@link RenderItem#STATE_REGISTRY}.
 * Port (Teil 27c): 26.3 block entity renderers extract a render state first and submit it afterwards.
 */
public class InventoryBlockEntityRenderer<T extends BlockEntity & Container> implements BlockEntityRenderer<T, InventoryBlockEntityRenderer.State> {
  private static boolean loggedError = false;

  public InventoryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(T inventory, State state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
    BlockEntityRenderer.super.extractRenderState(inventory, state, partialTicks, cameraPos, crumbling);
    state.items.clear();
    state.blockState = inventory.getBlockState();
    state.renderItems = inventory.isEmpty() ? List.of() : RenderItem.STATE_REGISTRY.get(state.blockState, List.of());
    int seed = (int) inventory.getBlockPos().asLong();
    for (int i = 0; i < state.renderItems.size() && i < inventory.getContainerSize(); i++) {
      state.items.add(RenderingHelper.resolveItem(inventory.getItem(i), state.renderItems.get(i), inventory.getLevel(), seed + i));
    }
  }

  @Override
  public void submit(State state, PoseStack matrices, SubmitNodeCollector buffer, CameraRenderState camera) {
    if (state.items.isEmpty()) return;
    try {
      // if the block is rotatable, rotate item display
      boolean isRotated = RenderingHelper.applyRotation(matrices, state.blockState);
      // render items
      for (int i = 0; i < state.items.size(); i++) {
        RenderingHelper.renderItem(matrices, buffer, state.items.get(i), state.renderItems.get(i), state.lightCoords);
      }
      // pop back rotation
      if (isRotated) {
        matrices.popPose();
      }
    } catch (RuntimeException e) {
      if (!loggedError) {
        loggedError = true;
        Mantle.logger.error("Failed to render inventory block entity", e);
      }
    }
  }

  @Override
  public boolean shouldRenderOffScreen() {
    return true;
  }

  /** Render state: the block state and the resolved item models */
  public static class State extends BlockEntityRenderState {
    public BlockState blockState;
    public List<RenderItem> renderItems = List.of();
    public final List<ItemStackRenderState> items = new ArrayList<>();
  }
}
