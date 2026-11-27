package slimeknights.mantle.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.model.util.QuadRetexture;
import slimeknights.mantle.client.model.util.QuadRetexture.StatedElement;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model that dynamically retextures a list of textures based on data from {@link RetexturedHelper}
 * ({@code "loader": "mantle:retextured"}, {@code "retextured": ["name", ...]}).
 * Port (Teil 27c): the model file bakes like a normal (coloured) model with the default textures. In the world, the
 * block state model {@code "type": "mantle:retextured"} (added by the asset import of the build) reads the texture
 * block from the block entity model data and swaps the sprite of the retextured faces.
 */
public class RetexturedModel extends ColoredBlockModel {
  public static final UnbakedModelLoader<RetexturedModel> LOADER = RetexturedModel::deserialize;
  public static final Identifier ID = Mantle.getResource("retextured");

  private final Set<String> retextured;

  protected RetexturedModel(CuboidModel delegate, ColoredGeometry geometry, Set<String> retextured) {
    super(delegate, geometry);
    this.retextured = retextured;
  }

  /** Finds the retextured model in the parent chain of a resolved model */
  @Nullable
  public static RetexturedModel find(@Nullable ResolvedModel model) {
    for (int i = 0; model != null && i < 64; i++, model = model.parent()) {
      if (model.wrapped() instanceof RetexturedModel retextured) {
        return retextured;
      }
    }
    return null;
  }

  public static RetexturedModel deserialize(JsonObject json, JsonDeserializationContext context) {
    CuboidModel model = readVanilla(json, context);
    return new RetexturedModel(model, readGeometry(model, json), getRetexturedNames(json));
  }

  /**
   * Gets a list of retextured textures from the model
   * @param json  Model json
   * @return  List of textures
   */
  public static Set<String> getRetexturedNames(JsonObject json) {
    if (json.has("retextured")) {
      // if an array, set from each texture in array
      JsonElement retextured = json.get("retextured");
      if (retextured.isJsonArray()) {
        JsonArray array = retextured.getAsJsonArray();
        if (array.isEmpty()) {
          throw new JsonSyntaxException("Must have at least one texture in retextured");
        }
        List<String> builder = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
          builder.add(GsonHelper.convertToString(array.get(i), "retextured[" + i + "]"));
        }
        return Set.copyOf(builder);
      }
      // if string, single texture
      if (retextured.isJsonPrimitive()) {
        return Set.of(retextured.getAsString());
      }
    }
    // if neither or missing, error
    throw new JsonSyntaxException("Missing retextured, expected to find a String or a JsonArray");
  }

  /** Gets the sprite used to retexture a model for the given block (its particle texture) */
  @Nullable
  public static TextureAtlasSprite getSprite(Block block) {
    Identifier texture = ModelHelper.getParticleTexture(block);
    if (MissingTextureAtlasSprite.getLocation().equals(texture)) {
      return null;
    }
    return Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, texture));
  }

  /** World aware block state model of a retextured model */
  public static class BlockModel implements DynamicBlockStateModel {
    private final BlockStateModelPart plain;
    private final QuadCollection staticQuads;
    private final QuadCollection retexturedQuads;
    private final Map<Block,BlockStateModelPart> cache = new ConcurrentHashMap<>();

    private BlockModel(BlockStateModelPart plain, QuadCollection staticQuads, QuadCollection retexturedQuads) {
      this.plain = plain;
      this.staticQuads = staticQuads;
      this.retexturedQuads = retexturedQuads;
    }

    /** Model part retextured with the given block */
    public BlockStateModelPart getPart(@Nullable Block block) {
      if (block == null) {
        return plain;
      }
      return cache.computeIfAbsent(block, b -> {
        TextureAtlasSprite sprite = getSprite(b);
        if (sprite == null) {
          return plain;
        }
        QuadCollection.Builder builder = new QuadCollection.Builder().addAll(staticQuads);
        QuadRetexture.addSwapped(builder, retexturedQuads, old -> sprite);
        return new SimpleModelWrapper(builder.build(), plain.useAmbientOcclusion(), plain.particleMaterial());
      });
    }

    @Nullable
    private static Block getBlock(BlockAndTintGetter level, BlockPos pos) {
      if (level == BlockAndTintGetter.EMPTY) {
        return null;
      }
      ModelData data = level.getModelData(pos);
      return data.get(RetexturedHelper.BLOCK_PROPERTY);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
      parts.add(getPart(getBlock(level, pos)));
    }

    @Nullable
    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
      Block block = getBlock(level, pos);
      return block == null ? this : new GeometryKey(this, block);
    }

    private record GeometryKey(BlockModel model, Block block) {}

    @Override
    public Material.Baked particleMaterial() {
      return plain.particleMaterial();
    }

    @Override
    public int materialFlags() {
      return plain.materialFlags();
    }
  }

  /** Block state model {@code "type": "mantle:retextured"}, same fields as a vanilla variant */
  public record Unbaked(Variant variant) implements CustomUnbakedBlockStateModel {
    public static final MapCodec<Unbaked> MAP_CODEC = Variant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
      return MAP_CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
      variant.resolveDependencies(resolver);
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
      BlockStateModelPart plain = variant.bake(baker);
      ResolvedModel resolved = baker.getModel(variant.modelLocation());
      RetexturedModel model = find(resolved);
      ModelState state = variant.modelState().asModelState();
      List<StatedElement> elements = model == null ? null : QuadRetexture.elements(resolved, state);
      if (model == null || elements == null) {
        return new SingleVariant(plain);
      }
      TextureSlots slots = resolved.getTopTextureSlots();
      List<TextureSlots.Data> chain = QuadRetexture.textureChain(resolved);
      List<CuboidModelElement> plainElements = new ArrayList<>();
      List<CuboidModelElement> retexturedElements = new ArrayList<>();
      QuadCollection.Builder staticQuads = new QuadCollection.Builder();
      QuadCollection.Builder retexturedQuads = new QuadCollection.Builder();
      for (StatedElement stated : elements) {
        CuboidModelElement element = stated.element();
        Map<Direction,CuboidFace> plainFaces = new EnumMap<>(Direction.class);
        Map<Direction,CuboidFace> retexturedFaces = new EnumMap<>(Direction.class);
        for (Map.Entry<Direction,CuboidFace> entry : element.faces().entrySet()) {
          String texture = QuadRetexture.findTextureName(entry.getValue().texture(), chain, model.retextured::contains);
          (texture.isEmpty() ? plainFaces : retexturedFaces).put(entry.getKey(), entry.getValue());
        }
        if (!plainFaces.isEmpty()) {
          staticQuads.addAll(QuadRetexture.bake(List.of(QuadRetexture.withFaces(element, plainFaces)), slots, baker, stated.state(), resolved));
        }
        if (!retexturedFaces.isEmpty()) {
          retexturedQuads.addAll(QuadRetexture.bake(List.of(QuadRetexture.withFaces(element, retexturedFaces)), slots, baker, stated.state(), resolved));
        }
      }
      return new BlockModel(plain, staticQuads.build(), retexturedQuads.build());
    }
  }
}
