package slimeknights.tconstruct.library.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import org.joml.Matrix4fc;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Fluid container items (buckets of Tinkers fluids, copper can).
 * Port (Teil 27f2): 1.20.1 extended Forge's dynamic fluid container model with baked tints and fluid stack sensitive
 * models. NeoForge 26.3 ships the same as the item model {@code neoforge:fluid_container} (tint and light from the fluid
 * model, contained fluid read from the stack), so this class only keeps the 1.20.1 model file as block model loader
 * {@code tconstruct3:fluid_container} and {@link Unbaked} hands its fluid and textures to NeoForge's model. Fluids of
 * later parts that are not registered yet give an empty model instead of a load error.
 */
public class FluidContainerModel extends DelegateUnbakedModel {
  public static final UnbakedModelLoader<FluidContainerModel> LOADER = FluidContainerModel::deserialize;

  /** Fixed fluid, null for the fluid of the stack */
  @Nullable
  private final Identifier fluid;
  private final boolean flipGas;

  protected FluidContainerModel(CuboidModel delegate, @Nullable Identifier fluid, boolean flipGas) {
    super(delegate);
    this.fluid = fluid;
    this.flipGas = flipGas;
  }

  /** Deserializes this model from JSON */
  public static FluidContainerModel deserialize(JsonObject json, JsonDeserializationContext context) {
    Identifier fluid = null;
    if (json.has("fluid")) {
      JsonElement element = json.get("fluid");
      // the 1.20.1 fluid NBT is not used by any data of 3.12.1
      String name = element.isJsonObject() ? GsonHelper.getAsString(element.getAsJsonObject(), "name") : GsonHelper.convertToString(element, "fluid");
      fluid = Identifier.parse(name);
    }
    boolean flipGas = GsonHelper.getAsBoolean(json, "flip_gas", true);
    return new FluidContainerModel(ColoredBlockModel.readVanilla(json, context), fluid, flipGas);
  }

  /** Item model definition {@code {"type": "tconstruct3:fluid_container", "model": "<1.20.1 model>"}} */
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
      FluidContainerModel config = TinkerItemModel.find(resolved, FluidContainerModel.class);
      Fluid fluid = Fluids.EMPTY;
      boolean flipGas = true;
      if (config != null) {
        flipGas = config.flipGas;
        if (config.fluid != null) {
          Optional<Fluid> found = BuiltInRegistries.FLUID.getOptional(config.fluid);
          if (found.isEmpty()) {
            // fluid of a later part (27g/27i), the item does not exist either
            return context.missingItemModel(transformation);
          }
          fluid = found.get();
        }
      } else {
        TConstruct.LOG.error("Item model {} needs a model with loader tconstruct3:fluid_container", model);
      }
      TextureSlots slots = resolved.getTopTextureSlots();
      DynamicFluidContainerModel.Textures textures = new DynamicFluidContainerModel.Textures(
        Optional.ofNullable(slots.getMaterial("particle")), Optional.ofNullable(slots.getMaterial("base")),
        Optional.ofNullable(slots.getMaterial("fluid")), Optional.ofNullable(slots.getMaterial("cover")));
      // 1.20.1 had no cover, the mask is the fluid texture
      return new DynamicFluidContainerModel.Unbaked(textures, fluid, flipGas, false, true).bake(context, transformation);
    }
  }
}
