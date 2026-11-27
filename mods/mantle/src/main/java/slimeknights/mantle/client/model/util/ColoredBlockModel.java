package slimeknights.mantle.client.model.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.common.ColorLoadable;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.LogicHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Block model with a colour, light level and UV lock per element ({@code "colors": [...]}, one entry per element).
 * Port (Teil 27c): 26.3 models are a vanilla {@link CuboidModel}; the colour and light become NeoForge's
 * {@link ExtraFaceData} of the element, an element with its own UV lock is baked with the matching model state.
 */
public class ColoredBlockModel extends DelegateUnbakedModel {
  /** Loader for {@code mantle:colored_block} */
  public static final UnbakedModelLoader<ColoredBlockModel> LOADER = ColoredBlockModel::deserialize;

  private final ColoredGeometry geometry;

  protected ColoredBlockModel(UnbakedModel delegate, ColoredGeometry geometry) {
    super(delegate);
    this.geometry = geometry;
  }

  /** Colour data of the elements */
  public List<ColorData> getColorData() {
    return geometry.colors();
  }

  @Nullable
  @Override
  public UnbakedGeometry geometry() {
    return geometry.elements().isEmpty() ? super.geometry() : geometry;
  }

  /** Elements with colours applied, in model order (empty if the model inherits its elements) */
  public List<CuboidModelElement> getElements() {
    return geometry.elements();
  }

  /** Geometry of a coloured model */
  public record ColoredGeometry(List<CuboidModelElement> elements, List<ColorData> colors) implements ExtendedUnbakedGeometry {
    /** Element with the colour data of its index applied */
    public CuboidModelElement element(int index) {
      return colors.get(index).apply(elements.get(index));
    }

    /** Model state for the element at the index, with its UV lock override */
    public ModelState state(int index, ModelState state) {
      return LogicHelper.getOrDefault(colors, index, ColorData.DEFAULT).state(state);
    }

    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
      QuadCollection.Builder builder = new QuadCollection.Builder();
      for (int i = 0; i < elements.size(); i++) {
        builder.addAll(UnbakedCuboidGeometry.bake(List.of(element(i)), textureSlots, baker, state(i, state), debugName));
      }
      return builder.build();
    }
  }

  public record ColorData(int color, @Deprecated int luminosity, @Nullable Boolean uvlock) {
    public static final ColorData DEFAULT = new ColorData(-1, -1, null);
    public static final RecordLoadable<ColorData> LOADABLE = RecordLoadable.create(
      ColorLoadable.ALPHA.defaultField("color", false, ColorData::color),
      IntLoadable.range(-1, 15).defaultField("luminosity", -1, ColorData::luminosity),
      BooleanLoadable.INSTANCE.nullableField("uvlock", ColorData::uvlock),
      ColorData::new);
    public static final Loadable<List<ColorData>> LIST_LOADABLE = LOADABLE.list(0);

    public boolean isUvLock(boolean defaultLock) {
      if (uvlock == null) {
        return defaultLock;
      }
      return uvlock;
    }

    /** Applies colour and light to the element (as NeoForge face data) */
    public CuboidModelElement apply(CuboidModelElement element) {
      if (color == -1 && luminosity == -1) {
        return element;
      }
      ExtraFaceData base = element.faceData() == null ? ExtraFaceData.DEFAULT : element.faceData();
      ExtraFaceData data = new ExtraFaceData(color == -1 ? base.color() : color, luminosity == -1 ? base.lightEmission() : luminosity, base.ambientOcclusion());
      return new CuboidModelElement(element.from(), element.to(), element.faces(), element.rotation(), element.shadeDirectionOverride(),
        luminosity == -1 ? element.lightEmission() : luminosity, data);
    }

    /** Model state with the UV lock of this element */
    public ModelState state(ModelState state) {
      if (uvlock == null) {
        return state;
      }
      if (uvlock && state instanceof BlockModelRotation rotation) {
        return rotation.withUvLock();
      }
      if (!uvlock && state instanceof BlockModelRotation.WithUvLock locked) {
        return locked.parent();
      }
      return state;
    }
  }

  /* Deserializing */

  /** Reads the vanilla part of a model JSON (everything but the loader) */
  public static CuboidModel readVanilla(JsonObject json, JsonDeserializationContext context) {
    JsonObject copy = json.deepCopy();
    copy.remove("loader");
    return context.deserialize(copy, CuboidModel.class);
  }

  /** Reads the colours of a model JSON */
  public static ColoredGeometry readGeometry(CuboidModel model, JsonObject json) {
    List<ColorData> colorData = ColorData.LIST_LOADABLE.getOrDefault(json, "colors", List.of());
    List<CuboidModelElement> elements = model.geometry() instanceof UnbakedCuboidGeometry cuboid ? cuboid.elements() : List.of();
    List<ColorData> padded = new ArrayList<>(elements.size());
    for (int i = 0; i < elements.size(); i++) {
      padded.add(LogicHelper.getOrDefault(colorData, i, ColorData.DEFAULT));
    }
    return new ColoredGeometry(elements, List.copyOf(padded));
  }

  public static ColoredBlockModel deserialize(JsonObject json, JsonDeserializationContext context) {
    CuboidModel model = readVanilla(json, context);
    return new ColoredBlockModel(model, readGeometry(model, json));
  }
}
