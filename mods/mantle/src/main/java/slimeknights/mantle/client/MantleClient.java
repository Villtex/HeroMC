package slimeknights.mantle.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.model.NBTKeyModel;
import slimeknights.mantle.client.model.RetexturedModel;
import slimeknights.mantle.client.model.connected.ConnectedBlockStateModel;
import slimeknights.mantle.client.model.connected.ConnectedModel;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.model.TextureColorHelper;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.RenderItem;
import slimeknights.mantle.fluid.TextureFluidType;
import slimeknights.mantle.fluid.texture.ClientTextureFluidType;
import slimeknights.mantle.fluid.texture.FluidTextureManager;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.mantle.command.client.MantleClientCommand;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.mantle.recipe.helper.RecipeHelper;

/**
 * Port: client setup of Mantle (was {@code ClientEvents}, an {@code EventBusSubscriber}). Teil 27b: recipes received
 * from the server; Teil 27c: reload listeners, books, client commands, renderers and model loaders.
 */
public final class MantleClient {
  private MantleClient() {}

  /** Called from the Mantle constructor on the client */
  public static void onConstruct(IEventBus bus) {
    NeoForge.EVENT_BUS.addListener(RecipesReceivedEvent.class, event -> RecipeHelper.setClientRecipes(event.getRecipeMap()));
    bus.addListener(AddClientReloadListenersEvent.class, MantleClient::registerListeners);
    bus.addListener(FMLClientSetupEvent.class, MantleClient::clientSetup);
    bus.addListener(RegisterFluidModelsEvent.class, FluidTextureManager::registerModels);
    bus.addListener(RegisterClientExtensionsEvent.class, MantleClient::registerClientExtensions);
    bus.addListener(ModelEvent.RegisterLoaders.class, MantleClient::registerModelLoaders);
    MantleHud.register(bus);
    bus.addListener(RegisterBlockStateModels.class, MantleClient::registerBlockStateModels);
  }

  /** Model loaders (model JSON {@code "loader"}) */
  private static void registerModelLoaders(ModelEvent.RegisterLoaders event) {
    // standard models - useful in resource packs for any model
    event.register(Mantle.getResource("connected"), ConnectedModel.LOADER);
    event.register(Mantle.getResource("item_layer"), MantleItemLayerModel.LOADER);
    event.register(Mantle.getResource("colored_block"), ColoredBlockModel.LOADER);
    // NBT dynamic models - require specific data defined in the block/item to use
    event.register(Mantle.getResource("nbt_key"), NBTKeyModel.LOADER);
    event.register(Mantle.getResource("retextured"), RetexturedModel.LOADER);
    // Port: "fallback" (first model of a list whose mod is loaded) is gone, no Tinkers model uses it
  }

  /** Port: world aware parts of the connected and retextured models (block state JSON {@code "type"}) */
  private static void registerBlockStateModels(RegisterBlockStateModels event) {
    event.registerModel(ConnectedBlockStateModel.ID, ConnectedBlockStateModel.Unbaked.MAP_CODEC);
    event.registerModel(RetexturedModel.ID, RetexturedModel.Unbaked.MAP_CODEC);
  }

  /** Port: was {@code TextureFluidType#initializeClient}; camera overlay and fog of the fluid texture */
  private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
    for (FluidType type : NeoForgeRegistries.FLUID_TYPES) {
      if (type instanceof TextureFluidType) {
        event.registerFluidType(new ClientTextureFluidType(type), type);
      }
    }
  }

  private static void registerListeners(AddClientReloadListenersEvent event) {
    event.addListener(Mantle.getResource("books"), new BookLoader());
    ResourceColorManager.init(event);
    FluidTooltipHandler.init(event);
    FluidTextureManager.init(event);
    event.addListener(Mantle.getResource("model_helper"), ModelHelper.LISTENER);
    event.addListener(Mantle.getResource("block_fluids"), FluidCuboid.REGISTRY);
    event.addListener(Mantle.getResource("render_items"), RenderItem.REGISTRY);
    event.addListener(Mantle.getResource("render_item_states"), RenderItem.STATE_REGISTRY);
    event.addListener(Mantle.getResource("texture_colors"), TextureColorHelper.RELOAD_LISTENER);
  }

  private static void clientSetup(FMLClientSetupEvent event) {
    BookLoader.registerBook(Mantle.getResource("test"), new FileRepository(Mantle.getResource("books/test")));
    MantleClientCommand.init();
  }
}
