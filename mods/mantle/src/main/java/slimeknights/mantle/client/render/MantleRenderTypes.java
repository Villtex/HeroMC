package slimeknights.mantle.client.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;

/**
 * Render types of Mantle.
 * Port (Teil 27c): 26.3 builds render types from render pipelines; Mantle's own shaders (fluid fog fix, fullbright
 * block) are gone, the vanilla types with the same look are used instead.
 */
public final class MantleRenderTypes {
  private MantleRenderTypes() {}

  /** Render type used for rendering fluids in block entities (translucent, block atlas, lightmap) */
  public static final RenderType FLUID = RenderTypes.translucentMovingBlock();

  /** Translucent block atlas without lightmap (structure previews in books) */
  public static final RenderType TRANSLUCENT_FULLBRIGHT = RenderTypes.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS);
}
