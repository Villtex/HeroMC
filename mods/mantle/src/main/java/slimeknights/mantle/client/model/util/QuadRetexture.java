package slimeknights.mantle.client.model.util;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Map;

/**
 * Port (Teil 27c): helpers for Mantle models that change textures at runtime (connected textures, retextured blocks).
 * 26.3 has no model baker after loading, so faces whose texture can change are baked on their own during model loading,
 * and at runtime only the sprite of those quads is swapped.
 */
public final class QuadRetexture {
  private QuadRetexture() {}

  /** One element face baked on its own, with the texture name it uses */
  public record BakedFace(String texture, Direction side, CuboidFace face, QuadCollection quads) {}

  /** Removes the leading '#' of a texture reference */
  public static String textureName(String texture) {
    return !texture.isEmpty() && texture.charAt(0) == '#' ? texture.substring(1) : texture;
  }

  /** An element of a model with the model state to bake it with (coloured elements may override the UV lock) */
  public record StatedElement(CuboidModelElement element, ModelState state) {}

  /**
   * Elements of the top geometry of a resolved model, or null if the geometry is not made of cuboid elements
   * @param state  Model state of the block state variant
   */
  @javax.annotation.Nullable
  public static List<StatedElement> elements(ResolvedModel model, ModelState state) {
    UnbakedGeometry geometry = model.getTopGeometry();
    if (geometry instanceof ColoredBlockModel.ColoredGeometry colored) {
      List<StatedElement> list = new java.util.ArrayList<>(colored.elements().size());
      for (int i = 0; i < colored.elements().size(); i++) {
        list.add(new StatedElement(colored.element(i), colored.state(i, state)));
      }
      return list;
    }
    if (geometry instanceof UnbakedCuboidGeometry cuboid) {
      return cuboid.elements().stream().map(element -> new StatedElement(element, state)).toList();
    }
    return null;
  }

  /** Texture data of a resolved model, from the model itself up to its last parent */
  public static List<TextureSlots.Data> textureChain(ResolvedModel model) {
    List<TextureSlots.Data> chain = new java.util.ArrayList<>();
    for (int i = 0; model != null && i < 64; i++, model = model.parent()) {
      chain.add(model.wrapped().textureSlots());
    }
    return chain;
  }

  /**
   * Follows a face texture through the texture references of the model until it reaches a name accepted by the
   * predicate (returned) or a texture value (empty string). Same as Mantle's {@code getConnectedName} of 1.20.1.
   */
  public static String findTextureName(String texture, List<TextureSlots.Data> chain, java.util.function.Predicate<String> isTarget) {
    String check = textureName(texture);
    for (int guard = 0; guard < 64; guard++) {
      if (isTarget.test(check)) {
        return check;
      }
      TextureSlots.SlotContents contents = null;
      for (TextureSlots.Data data : chain) {
        contents = data.values().get(check);
        if (contents != null) {
          break;
        }
      }
      if (contents instanceof TextureSlots.Reference reference) {
        check = reference.target();
      } else {
        return "";
      }
    }
    return "";
  }

  /** Copy of an element with only the given faces */
  public static CuboidModelElement withFaces(CuboidModelElement element, Map<Direction, CuboidFace> faces) {
    return new CuboidModelElement(element.from(), element.to(), faces, element.rotation(), element.shadeDirectionOverride(), element.lightEmission(), element.faceData());
  }

  /** Bakes the given elements */
  public static QuadCollection bake(List<CuboidModelElement> elements, TextureSlots slots, ModelBaker baker, ModelState state, ModelDebugName name) {
    return UnbakedCuboidGeometry.bake(elements, slots, baker, state, name);
  }

  /** Bakes a single face of an element */
  public static BakedFace bakeFace(CuboidModelElement element, Direction side, CuboidFace face, TextureSlots slots, ModelBaker baker, ModelState state, ModelDebugName name) {
    QuadCollection quads = bake(List.of(withFaces(element, Map.of(side, face))), slots, baker, state, name);
    return new BakedFace(textureName(face.texture()), side, face, quads);
  }

  /** Quad with the given sprite in place of its own, UVs moved to the same spot of the new sprite */
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
    BakedQuad.MaterialInfo swapped = new BakedQuad.MaterialInfo(sprite, info.layer(), info.itemRenderType(), info.itemGlintRenderType(),
      info.itemGlintSpecialRenderType(), info.tintIndex(), info.shadeDirectionOverride(), info.lightEmission(), info.ambientOcclusion());
    return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), uvs[0], uvs[1], uvs[2], uvs[3],
      quad.direction(), swapped, quad.bakedNormals(), quad.bakedColors());
  }

  /** Adds all quads of the collection to the builder, each with the sprite given by the function */
  public static void addSwapped(QuadCollection.Builder builder, QuadCollection quads, java.util.function.UnaryOperator<TextureAtlasSprite> sprites) {
    for (Direction side : Direction.values()) {
      for (BakedQuad quad : quads.getQuads(side)) {
        builder.addCulledFace(side, swapSprite(quad, sprites.apply(quad.materialInfo().sprite())));
      }
    }
    for (BakedQuad quad : quads.getQuads(null)) {
      builder.addUnculledFace(swapSprite(quad, sprites.apply(quad.materialInfo().sprite())));
    }
  }
}
