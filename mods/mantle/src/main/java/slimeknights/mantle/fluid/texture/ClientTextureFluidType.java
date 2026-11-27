package slimeknights.mantle.fluid.texture;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.BlockPos;
import org.joml.Vector4f;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nullable;

/** Implementation of {@link IClientFluidTypeExtensions} using {@link FluidTexture} */
@RequiredArgsConstructor
public class ClientTextureFluidType implements IClientFluidTypeExtensions {
  protected final FluidType type;

  // Port: still/flowing/overlay textures and the tint are a FluidModel in 26.3, see FluidTextureManager#registerModels

  @Nullable
  @Override
  public Identifier getRenderOverlayTexture(Minecraft mc) {
    return FluidTextureManager.getCameraTexture(type);
  }

  /** Port: 26.3 extracts the camera overlay; same as the default but with the opacity and colour of the fluid texture */
  @Override
  public void extractOverlay(Minecraft minecraft, LocalPlayer player, PlayerRenderState playerRenderState, BlockPos eyePos, float brightness) {
    FluidTexture data = FluidTextureManager.getData(type);
    Identifier camera = data.camera();
    if (camera != null) {
      int color = data.color();
      float opacity = data.cameraOpacity();
      int argb = color != -1
        ? ARGB.colorFromFloat(opacity * (color >>> 24) / 255f, brightness * (color >> 16 & 255) / 255f, brightness * (color >> 8 & 255) / 255f, brightness * (color & 255) / 255f)
        : ARGB.colorFromFloat(opacity, brightness, brightness, brightness);
      PlayerRenderState.WaterOverlay overlay = new PlayerRenderState.WaterOverlay(argb, -player.getYRot() / 64.0F, player.getXRot() / 64.0F);
      playerRenderState.customFluidOverlayRenderer = (a, b, submitNodeCollector, poseStack, c, d, e) -> {
        ScreenEffectRenderer.submitFluid(overlay, poseStack, submitNodeCollector, camera);
        return true;
      };
    }
  }

  @Override
  public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
    // nothing to do if fog color is white
    int fluidColor = FluidTextureManager.getData(type).fogColor();
    if (fluidColor != -1) {
      fluidFogColor.x *= ARGB.red(fluidColor) / 255f;
      fluidFogColor.y *= ARGB.green(fluidColor) / 255f;
      fluidFogColor.z *= ARGB.blue(fluidColor) / 255f;
    }
  }

  @Override
  public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
    FluidTexture data = FluidTextureManager.getData(type);
    if (data.fogShape() != null) {
      fogData.environmentalStart = Math.min(fogData.environmentalStart, data.fogStart());
      fogData.environmentalEnd = Math.min(fogData.environmentalEnd, data.fogEnd());
    }
  }
}
