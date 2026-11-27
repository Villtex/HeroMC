package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector3f;

@SuppressWarnings("WeakerAccess")
public class RenderingHelper {
  /* Rotation */

  /**
   * Applies horizontal rotation to the given TESR
   * @param matrices  Matrix stack
   * @param state     Block state, checked for {@link BlockStateProperties#HORIZONTAL_FACING}
   * @return  True if rotation was applied. Caller is expected to call {@link PoseStack#popPose()} if true
   */
  public static boolean applyRotation(PoseStack matrices, BlockState state) {
    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
      return applyRotation(matrices, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
    return false;
  }

  /**
   * Applies horizontal rotation to the given TESR
   * @param matrices  Matrix stack
   * @param facing    Direction of rotation
   * @return  True if rotation was applied. Caller is expected to call {@link PoseStack#popPose()} if true
   */
  public static boolean applyRotation(PoseStack matrices, Direction facing) {
    // south has a facing of 0, no rotation needed
    if (facing.getAxis().isHorizontal() && facing != Direction.SOUTH) {
      matrices.pushPose();
      matrices.translate(0.5, 0, 0.5);
      matrices.rotateDegrees(Axis.YP, (-90f * (facing.get2DDataValue())));
      matrices.translate(-0.5, 0, -0.5);
      return true;
    }
    return false;
  }


  /* Items */

  /**
   * Renders a single item in a TESR
   * @param matrices    Matrix stack inst ance
   * @param buffer      Buffer instance
   * @param item        Item to render
   * @param renderItem  Render item for render information
   * @param light       Model light
   */
  public static void renderItem(PoseStack matrices, SubmitNodeCollector buffer, ItemStack item, RenderItem renderItem, int light) {
    // if the item says skip, skip
    if (renderItem.isHidden()) return;
    // if no stack, skip
    if (item.isEmpty()) return;
    renderItem(matrices, buffer, resolveItem(item, renderItem, Minecraft.getInstance().level, 0), renderItem, light);
  }

  /**
   * Port (Teil 27c): resolves the item model for a render item, best done while extracting the block entity render state
   * @param seed  Model seed, vanilla uses the position hash
   */
  public static ItemStackRenderState resolveItem(ItemStack item, RenderItem renderItem, @javax.annotation.Nullable net.minecraft.world.level.Level level, int seed) {
    ItemStackRenderState state = new ItemStackRenderState();
    if (!renderItem.isHidden() && !item.isEmpty()) {
      Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, item, renderItem.getTransform(), level, null, seed);
    }
    return state;
  }

  /** Port: submit form of the original renderItem, with the item model resolved by {@link #resolveItem} */
  public static void renderItem(PoseStack matrices, SubmitNodeCollector buffer, ItemStackRenderState item, RenderItem renderItem, int light) {
    if (renderItem.isHidden() || item.isEmpty()) return;

    // start rendering
    matrices.pushPose();
    Vector3f center = renderItem.getCenterScaled();
    matrices.translate(center.x(), center.y(), center.z());

    // scale
    float scale = renderItem.getSizeScaled();
    matrices.scale(scale, scale, scale);

    // rotate X, then Y
    float x = renderItem.getX();
    if (x != 0) {
      matrices.rotateDegrees(Axis.XP, (x));
    }
    float y = renderItem.getY();
    if (y != 0) {
      matrices.rotateDegrees(Axis.YP, (y));
    }

    // render the actual item
    item.submit(matrices, buffer, light, OverlayTexture.NO_OVERLAY, 0);
    matrices.popPose();
  }

  /**
   * Renders faucet fluids at the relevant location
   * @param world       World instance
   * @param pos         Base position
   * @param direction   Direction to render
   * @param matrices    Matrix instance
   * @param buffer      Builder instance
   * @param still       Still fluid texture
   * @param flowing     Flowing fluid texture
   * @param color       Color to tint fluid
   * @param light       Fluid light value
   */
  public static void renderFaucetFluids(LevelAccessor world, BlockPos pos, Direction direction, PoseStack matrices, VertexConsumer buffer, TextureAtlasSprite still, TextureAtlasSprite flowing, int color, int light) {
    int i = 0;
    FaucetFluid faucetFluid;
    do {
      // get the faucet data for the block
      i++;
      faucetFluid = FaucetFluid.REGISTRY.get(world.getBlockState(pos.below(i)));
      // render all down cubes with the given offset
      matrices.pushPose();
      matrices.translate(0, -i, 0);
      for (FluidCuboid cube : faucetFluid.getFluids(direction)) {
        FluidRenderer.renderCuboid(matrices, buffer, cube, still, flowing, cube.getFromScaled(), cube.getToScaled(), color, light, false);
      }
      matrices.popPose();
    } while (faucetFluid.isContinued());
  }
}
