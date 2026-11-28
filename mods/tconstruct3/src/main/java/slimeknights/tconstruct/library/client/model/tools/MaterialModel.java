package slimeknights.tconstruct.library.client.model.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.TinkerSprites;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo.TintedSprite;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.client.model.TinkerItemModel;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Model for an item with material texture variants, such as tool parts. Used only for single material items, {@link ToolModel} is used for multi-material items.
 * <p>
 * Port (Teil 27f2): this class is the block model loader {@code tconstruct3:material} (keeps the settings of the
 * 1.20.1 JSON and bakes the untextured part as a plain fallback), {@link Item} is the 26.3 item model
 * {@code tconstruct3:material} that swaps in the material texture of the stack, see {@link TinkerItemModel}.
 */
public class MaterialModel extends DelegateUnbakedModel {
  /** Shared loader instance */
  public static final UnbakedModelLoader<MaterialModel> LOADER = MaterialModel::deserialize;

  /** If null, uses dynamic material */
  @Nullable
  private final MaterialVariantId material;
  /** Tint index and index of part in tool */
  private final int index;
  /** Transform matrix to apply to child parts */
  private final Vec2 offset;

  protected MaterialModel(CuboidModel delegate, @Nullable MaterialVariantId material, int index, Vec2 offset) {
    super(delegate);
    this.material = material;
    this.index = index;
    this.offset = offset;
  }

  @Override
  public UnbakedGeometry geometry() {
    // fallback when used as a plain model: the base texture
    Transformation transform = offsetTransform(offset);
    return (ExtendedUnbakedGeometry) (TextureSlots slots, ModelBaker baker, ModelState state, ModelDebugName name, ContextMap properties) -> {
      TextureAtlasSprite sprite = baker.materials().resolveSlot(slots, "texture", name).sprite();
      QuadCollection.Builder builder = new QuadCollection.Builder();
      MantleItemLayerModel.getQuadsForSprite(-1, -1, sprite, transform, 0).forEach(builder::addUnculledFace);
      return builder.build();
    };
  }

  /**
   * Gets the tinted sprite info for the given material
   * @param spriteGetter  Sprite getter instance
   * @param texture       Base texture
   * @param material      Material variant
   * @return  Tinted sprite or fallback
   */
  @SuppressWarnings("OptionalIsPresent")
  public static TintedSprite getMaterialSprite(Function<Material, TextureAtlasSprite> spriteGetter, Material texture, MaterialVariantId material) {
    // if the base material is non-null, try to find the sprite for that material
    // first, find a render info
    Optional<MaterialRenderInfo> optional = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(material);
    if (optional.isPresent()) {
      return optional.get().getSprite(texture, spriteGetter);
    }
    return new TintedSprite(spriteGetter.apply(texture), -1, 0);
  }

  /**
   * Gets quads for the given material variant of the texture
   * @param spriteGetter    Sprite getter instance
   * @param texture         Base texture
   * @param material        Material variant
   * @param tintIndex       Tint index for quads
   * @param transformation  Transformation to apply
   * @param pixels          Pixels to prevent z-fighting for multiple layers
   * @return  Quad list
   */
  public static List<BakedQuad> getQuadsForMaterial(Function<Material, TextureAtlasSprite> spriteGetter, Material texture, MaterialVariantId material, int tintIndex, Transformation transformation, @Nullable ItemLayerPixels pixels) {
    TintedSprite sprite = getMaterialSprite(spriteGetter, texture, material);
    return MantleItemLayerModel.getQuadsForSprite(sprite.color(), tintIndex, sprite.sprite(), transformation, sprite.emissivity(), pixels);
  }

  /** Makes the transformation for a pixel offset */
  private static Transformation offsetTransform(Vec2 offset) {
    if (Vec2.ZERO.equals(offset)) {
      return Transformation.IDENTITY;
    }
    // divide by 16 to convert from pixels to base values
    // negate Y as positive is up for transforms but down for pixels
    return new Transformation(new Vector3f(offset.x / 16, -offset.y / 16, 0), null, null, null);
  }


  /* 26.3 item model */

  /** Item model definition {@code {"type": "tconstruct3:material", "model": "<1.20.1 model>"}} */
  public record Unbaked(Identifier model) implements ItemModel.Unbaked {
    public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model)
    ).apply(inst, Unbaked::new));

    @Override
    public MapCodec<Unbaked> type() {
      return MAP_CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      resolver.markDependency(model);
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
      ResolvedModel resolved = context.blockModelBaker().getModel(model);
      MaterialModel config = TinkerItemModel.find(resolved, MaterialModel.class);
      TinkerItemModel.Settings settings = TinkerItemModel.Settings.of(model, resolved, transformation);
      if (config == null) {
        TConstruct.LOG.error("Item model {} needs a model with loader tconstruct3:material", model);
        return new Item(settings, null, Transformation.IDENTITY, 0);
      }
      return new Item(settings, config.material, offsetTransform(config.offset), config.index);
    }
  }

  /** Item model choosing the quads by the material of the stack */
  public static class Item implements ItemModel {
    private final TinkerItemModel.Settings settings;
    @Nullable
    private final MaterialVariantId material;
    private final Transformation transform;
    private final int index;
    /** contains all the variants since they'll never change, discarded with the model on resource reload */
    private final Map<MaterialVariantId, TinkerItemModel.Variant> cache = new ConcurrentHashMap<>();

    Item(TinkerItemModel.Settings settings, @Nullable MaterialVariantId material, Transformation transform, int index) {
      this.settings = settings;
      this.material = material;
      this.transform = transform;
      this.index = index;
    }

    /** Builds the quads for the given material */
    private TinkerItemModel.Variant bake(MaterialVariantId material) {
      TintedSprite sprite = getMaterialSprite(TinkerSprites.GETTER, settings.material("texture"), material);
      List<BakedQuad> quads = MantleItemLayerModel.getQuadsForSprite(sprite.color(), index, sprite.sprite(), transform, sprite.emissivity());
      return TinkerItemModel.Variant.of(quads, sprite.sprite());
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      MaterialVariantId variant = material != null ? material : Objects.requireNonNullElse(IMaterialItem.getMaterialFromStack(stack), MaterialId.UNKNOWN);
      TinkerItemModel.addLayer(output, stack, context, settings, cache.computeIfAbsent(variant, this::bake), false, variant);
    }
  }


  /* Helpers */

  /** Loads a material model from JSON */
  public static MaterialModel deserialize(JsonObject json, JsonDeserializationContext context) {
    // need tint index for tool models, doubles as part index
    int index = GsonHelper.getAsInt(json, "index", 0);

    // static material can be defined, if unset uses dynamic material
    MaterialVariantId material = null;
    if (json.has("material")) {
      material = MaterialVariantId.fromJson(json, "material");
    }

    Vec2 offset = Vec2.ZERO;
    if (json.has("offset")) {
      offset = getVec2(json, "offset");
    }

    return new MaterialModel(ColoredBlockModel.readVanilla(json, context), material, index, offset);
  }

  /**
   * Converts a JSON float array to the specified object
   * @param json    JSON object
   * @param name    Name of the array in the object to fetch
   * @return  Vector3f of data
   * @throws JsonParseException  If there is no array or the length is wrong
   */
  public static Vec2 getVec2(JsonObject json, String name) {
    JsonArray array = GsonHelper.getAsJsonArray(json, name);
    if (array.size() != 2) {
      throw new JsonParseException("Expected " + 2 + " " + name + " values, found: " + array.size());
    }
    float[] vec = new float[2];
    for(int i = 0; i < 2; ++i) {
      vec[i] = GsonHelper.convertToFloat(array.get(i), name + "[" + i + "]");
    }
    return new Vec2(vec[0], vec[1]);
  }
}
