package slimeknights.mantle.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import slimeknights.mantle.Mantle;
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
  }

  private static void registerListeners(AddClientReloadListenersEvent event) {
    event.addListener(Mantle.getResource("books"), new BookLoader());
    ResourceColorManager.init(event);
    FluidTooltipHandler.init(event);
  }

  private static void clientSetup(FMLClientSetupEvent event) {
    BookLoader.registerBook(Mantle.getResource("test"), new FileRepository(Mantle.getResource("books/test")));
    MantleClientCommand.init();
  }
}
