package slimeknights.mantle.fluid.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;

import com.google.gson.JsonElement;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import slimeknights.mantle.data.listener.JsonReloadListener;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.listener.IEarlySafeManagerReloadListener;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Manager for handling fluid textures */
public class FluidTextureManager implements IEarlySafeManagerReloadListener {
  /** Folder containing the logic */
  public static final String FOLDER = "mantle/fluid_texture";

  /* Instance data */
  private static final FluidTextureManager INSTANCE = new FluidTextureManager();
  /** Map of fluid type to texture */
  private Map<FluidType,FluidTexture> textures = Collections.emptyMap();
  /** Fallback texture instance */
  private static final FluidTexture FALLBACK = new FluidTexture(Identifier.parse("block/water_still"), Identifier.parse("block/water_flow"), null, null, 0, -1, -1, false, null, 0, 0);

  private FluidTextureManager() {}


  /**
   * Initializes this manager, registering it with the resource manager
   */
  public static void init(AddClientReloadListenersEvent event) {
    event.addListener(Mantle.getResource("fluid_textures"), INSTANCE);
  }

  /**
   * Port (Teil 27c): 26.3 bakes fluid textures and tint into a {@link FluidModel} per fluid. The texture JSONs are read
   * here too (the event runs during model loading, possibly before the reload listener above), then every fluid whose
   * type has a fluid texture gets a model.
   */
  public static void registerModels(RegisterFluidModelsEvent event) {
    INSTANCE.onReloadSafe(Minecraft.getInstance().getResourceManager());
    Map<FluidType,FluidTexture> textures = INSTANCE.textures;
    for (Fluid fluid : BuiltInRegistries.FLUID) {
      if (fluid == Fluids.EMPTY) {
        continue;
      }
      FluidType type = fluid.getFluidType();
      FluidTexture texture = textures.get(type);
      if (texture == null) {
        continue;
      }
      Identifier flowing = texture.flowing();
      if (type instanceof slimeknights.mantle.fluid.InvertedFluidType) {
        flowing = flowing.withSuffix("_inverted");
      }
      int color = texture.color();
      FluidTintSource tint = state -> color;
      try {
        event.register(new FluidModel.Unbaked(new Material(texture.still()), new Material(flowing),
          texture.overlay() == null ? null : new Material(texture.overlay()), tint), fluid);
      } catch (IllegalStateException e) {
        Mantle.logger.debug("Fluid {} already has a fluid model, keeping it", BuiltInRegistries.FLUID.getKey(fluid));
      }
    }
  }

  @Override
  public void onReloadSafe(ResourceManager resourceManager) {
    long time = System.nanoTime();
    // fetch JSONs
    Map<Identifier,JsonElement> jsons = new HashMap<>();
    slimeknights.mantle.data.listener.JsonReloadListener.scanDirectory(resourceManager, FOLDER, JsonHelper.DEFAULT_GSON, jsons);

    // start building fluid type map
    Map<FluidType, FluidTexture> map = new HashMap<>();
    Registry<FluidType> fluidTypeRegistry = net.neoforged.neoforge.registries.NeoForgeRegistries.FLUID_TYPES;


    for (Map.Entry<Identifier,JsonElement> entry : jsons.entrySet()) {
      Identifier id = entry.getKey();
      // first step is to find the matching fluid type, if there is none ignore the file
      FluidType type = fluidTypeRegistry.getValue(id);
      if (type == null || !id.equals(fluidTypeRegistry.getKey(type))) {
        Mantle.logger.debug("Ignoring fluid texture {} as no fluid type exists with that name", id);
      } else {
        // parse it if valid
        map.put(type, FluidTexture.deserialize(GsonHelper.convertToJsonObject(entry.getValue(), "fluid_texture")));
      }
    }
    this.textures = map;
    Mantle.logger.info("Loaded {} fluid textures in {} ms", map.size(), (System.nanoTime() - time) / 1000000f);
  }

  /** Gets the texture for the given fluid */
  public static FluidTexture getData(FluidType fluid) {
    return INSTANCE.textures.getOrDefault(fluid, FALLBACK);
  }

  /** Gets the still texture for the given fluid */
  public static Identifier getStillTexture(FluidType fluid) {
    return getData(fluid).still();
  }

  /** Gets the flowing texture for the given fluid */
  public static Identifier getFlowingTexture(FluidType fluid) {
    return getData(fluid).flowing();
  }

  /** Gets the overlay texture for the given fluid */
  @Nullable
  public static Identifier getOverlayTexture(FluidType fluid) {
    return getData(fluid).overlay();
  }

  /** Gets the camera texture for the given fluid */
  @Nullable
  public static Identifier getCameraTexture(FluidType fluid) {
    return getData(fluid).camera();
  }

  /** Gets the still texture for the given fluid */
  public static int getColor(FluidType fluid) {
    return getData(fluid).color();
  }
}
