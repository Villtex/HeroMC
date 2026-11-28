package slimeknights.tconstruct.library.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import com.mojang.math.Transformation;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.mantle.util.ItemLayerPixels;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Port (Teil 27f2): sprite lookup for the Tinkers item models.
 * In 1.20.1 the models fetched sprites with a {@code Function<Material,TextureAtlasSprite>} while baking, which also
 * requested them for the block atlas. In 26.3 every texture under {@code textures/item} is on the item atlas already
 * (directory source), and the dynamic models resolve sprites lazily when a stack is first drawn. This getter looks a
 * texture up on the item atlas, then the block atlas (fluids, block textures), without logging missing ones.
 */
public final class TinkerSprites {
  /** Sprite getter for the model code */
  public static final Function<Material, TextureAtlasSprite> GETTER = TinkerSprites::get;

  private TinkerSprites() {}

  /** Finds the sprite on the item or block atlas, null if missing */
  @Nullable
  public static TextureAtlasSprite find(Identifier texture) {
    if (MissingTextureAtlasSprite.getLocation().equals(texture)) {
      return null;
    }
    var manager = Minecraft.getInstance().getAtlasManager();
    for (Identifier atlasId : new Identifier[] { TextureAtlas.LOCATION_ITEMS, TextureAtlas.LOCATION_BLOCKS }) {
      TextureAtlas atlas = manager.getAtlasOrThrow(atlasId);
      TextureAtlasSprite sprite = atlas.getSprite(texture);
      if (sprite != atlas.missingSprite()) {
        return sprite;
      }
    }
    return null;
  }

  /** Gets the sprite for the material, or the missing sprite of the item atlas */
  public static TextureAtlasSprite get(Material material) {
    TextureAtlasSprite sprite = find(material.sprite());
    return sprite != null ? sprite : missing();
  }

  /** Gets the sprite for the texture, or the missing sprite of the item atlas */
  public static TextureAtlasSprite get(Identifier texture) {
    TextureAtlasSprite sprite = find(texture);
    return sprite != null ? sprite : missing();
  }

  /** Checks if the material exists on one of the atlases */
  public static boolean exists(Material material) {
    return find(material.sprite()) != null;
  }

  /** Missing sprite of the item atlas */
  public static TextureAtlasSprite missing() {
    return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_ITEMS).missingSprite();
  }

  /**
   * Swaps the sprite of the quad, keeping the relative UVs. Unlike Mantle's {@code QuadRetexture.swapSprite} this picks
   * the item render type for the atlas of the new sprite (fluid sprites are on the block atlas, part sprites on the item atlas).
   */
  public static BakedQuad swapSprite(BakedQuad quad, TextureAtlasSprite sprite) {
    TextureAtlasSprite old = quad.materialInfo().sprite();
    if (old == sprite) {
      return quad;
    }
    float oldU = old.getU1() - old.getU0(), oldV = old.getV1() - old.getV0();
    long[] uvs = new long[4];
    for (int i = 0; i < 4; i++) {
      long packed = quad.packedUV(i);
      float u = (UVPair.unpackU(packed) - old.getU0()) / oldU;
      float v = (UVPair.unpackV(packed) - old.getV0()) / oldV;
      uvs[i] = UVPair.pack(sprite.getU(u), sprite.getV(v));
    }
    BakedQuad.MaterialInfo info = quad.materialInfo();
    BakedQuad.MaterialInfo swapped = BakedQuad.MaterialInfo.of(new Material.Baked(sprite, false), sprite.transparency(),
      info.tintIndex(), info.shadeDirectionOverride(), info.lightEmission(), info.ambientOcclusion());
    return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), uvs[0], uvs[1], uvs[2], uvs[3],
      quad.direction(), swapped, quad.bakedNormals(), quad.bakedColors());
  }

  /** Makes fluids a bit thicker than the mask (scaled around the item center), 1.20.1 {@code FluidContainerModel.FLUID_TRANSFORM} */
  public static final Transformation FLUID_TRANSFORM = new Transformation(
    new org.joml.Matrix4f().translate(0, 0, 0.5f).scale(1, 1, 1.002f).translate(0, 0, -0.5f));

  /**
   * Builds the quads of a fluid in the shape of a mask sprite, replacing the 1.20.1 {@code UnbakedGeometryHelper} item
   * mask (that needs a model baker in 26.3). Used by fluid modifier models and the fluid container model.
   * @param fluid      Fluid to draw, color, light and still texture come from Mantle's fluid models
   * @param mask       Sprite whose opaque pixels make the shape
   * @param transform  Transformation for the quads
   * @param pixels     Pixels to prevent z-fighting, or null
   */
  public static List<BakedQuad> fluidQuads(FluidStack fluid, TextureAtlasSprite mask, Transformation transform, @Nullable ItemLayerPixels pixels) {
    TextureAtlasSprite still = FluidRenderer.getStillSprite(fluid);
    int color = FluidRenderer.getColor(fluid);
    int light = fluid.getFluid().getFluidType().getLightLevel(fluid);
    List<BakedQuad> quads = MantleItemLayerModel.getQuadsForSprite(color, -1, mask, transform.compose(FLUID_TRANSFORM), light, pixels);
    return quads.stream().map(quad -> swapSprite(quad, still)).toList();
  }

  /** Checks if the sprite is a missing sprite */
  public static boolean isMissing(TextureAtlasSprite sprite) {
    return MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
  }
}
