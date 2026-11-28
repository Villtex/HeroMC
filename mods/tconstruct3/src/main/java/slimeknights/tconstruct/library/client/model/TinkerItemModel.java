package slimeknights.tconstruct.library.client.model;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Port (Teil 27f2): shared parts of the Tinkers item models for 26.3.
 * <p>
 * In 1.20.1 the material and tool models were baked models whose item overrides baked a new model per material
 * combination. In 26.3 an item model definition ({@code assets/tconstruct3/items/<id>.json}) points to an
 * {@link net.minecraft.client.renderer.item.ItemModel}: {@code tconstruct3:material} and {@code tconstruct3:tool} name
 * the 1.20.1 model file (still loaded by the block model loaders {@code tconstruct3:material}/{@code tconstruct3:tool}
 * for its textures, display transforms and settings). The quads are built when a stack is first drawn, so the render
 * infos and the sprites on the atlases are always loaded; they are cached per material combination.
 */
public final class TinkerItemModel {
  private TinkerItemModel() {}

  /** Settings of the resolved 1.20.1 model needed when drawing */
  public record Settings(Identifier id, TextureSlots textures, ItemTransforms transforms, boolean usesBlockLight, Matrix4fc transform) {
    /** Reads the settings of a resolved model */
    public static Settings of(Identifier id, ResolvedModel model, Matrix4fc transform) {
      return new Settings(id, model.getTopTextureSlots(), model.getTopTransforms(), model.getTopGuiLight().lightLikeBlock(), transform);
    }

    /** Gets the material for the given texture name, missing texture if unset */
    public Material material(String name) {
      Material material = textures.getMaterial(name);
      return material != null ? material : new Material(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
    }
  }

  /** Finds the unbaked model of the given class in the model or its parents, null if missing */
  @Nullable
  public static <T extends UnbakedModel> T find(ResolvedModel model, Class<T> type) {
    for (ResolvedModel current = model; current != null; current = current.parent()) {
      if (type.isInstance(current.wrapped())) {
        return type.cast(current.wrapped());
      }
    }
    return null;
  }

  /** Quads of one variant of an item model in the different display contexts */
  public record Variant(ItemQuads right, ItemQuads left, ItemQuads small, ItemQuads gui, Material.Baked particle, Supplier<Vector3fc[]> extents, boolean animated) {
    /** Makes a variant that looks the same everywhere, GUI gets only the front faces like 1.20.1 */
    public static Variant of(List<BakedQuad> quads, TextureAtlasSprite particle) {
      ItemQuads all = ItemQuads.split(quads);
      return new Variant(all, all, all, ItemQuads.split(front(quads)), new Material.Baked(particle, false), TinkerItemModel.extents(quads), TinkerItemModel.animated(quads));
    }

    /** Gets the quads for the given display context */
    public ItemQuads get(ItemDisplayContext context, boolean useSmall) {
      if (context == ItemDisplayContext.GUI) {
        return gui;
      }
      if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
        return left;
      }
      return useSmall ? small : right;
    }
  }

  /** Filters the quads facing the viewer in GUIs */
  public static List<BakedQuad> front(List<BakedQuad> quads) {
    List<BakedQuad> front = new ArrayList<>();
    for (BakedQuad quad : quads) {
      if (quad.direction() == Direction.SOUTH) {
        front.add(quad);
      }
    }
    return front;
  }

  /** Lazily computed extents of the quads (used for the GUI and the item frame size) */
  public static Supplier<Vector3fc[]> extents(List<BakedQuad> quads) {
    List<BakedQuad> copy = List.copyOf(quads);
    return com.google.common.base.Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(copy));
  }

  /** Checks if any quad has an animated sprite */
  public static boolean animated(List<BakedQuad> quads) {
    for (BakedQuad quad : quads) {
      if (quad.materialInfo().sprite().contents().isAnimated()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds a layer for the variant to the render state, like the vanilla {@code minecraft:model} item model
   * @param output    Render state
   * @param stack     Stack being drawn
   * @param context   Display context
   * @param settings  Model settings
   * @param variant   Quads to draw
   * @param useSmall  If true, uses the small quads outside of hands and GUIs
   * @param identity  Object identifying the variant, the GUI item cache of 26.3 keys on it
   * @return  Layer, to add tints
   */
  public static ItemStackRenderState.LayerRenderState addLayer(ItemStackRenderState output, ItemStack stack, ItemDisplayContext context, Settings settings, Variant variant, boolean useSmall, Object identity) {
    output.appendModelIdentityElement(identity);
    ItemStackRenderState.LayerRenderState layer = output.newLayer();
    if (stack.hasFoil()) {
      layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
      output.setAnimated();
      output.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
    }
    layer.setExtents(variant.extents());
    layer.setLocalTransform(settings.transform());
    layer.setUsesBlockLight(settings.usesBlockLight());
    layer.setParticleMaterial(variant.particle());
    layer.setItemTransform(settings.transforms().getTransform(context));
    layer.setQuads(variant.get(context, useSmall));
    if (variant.animated()) {
      output.setAnimated();
    }
    return layer;
  }

  /** Adds tints to the layer and the identity of the render state */
  public static void addTints(ItemStackRenderState output, ItemStackRenderState.LayerRenderState layer, int[] tints) {
    if (tints.length > 0) {
      IntList list = layer.tintLayers();
      for (int tint : tints) {
        list.add(tint);
        output.appendModelIdentityElement(tint);
      }
    }
  }
}
