package slimeknights.mantle.client;

import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import slimeknights.mantle.recipe.helper.RecipeHelper;

/** Port: client setup of the Mantle core (Teil 27b). The client library (models, books) follows in Teil 27c. */
public final class MantleClient {
  private MantleClient() {}

  /** Called from the Mantle constructor on the client */
  public static void onConstruct() {
    NeoForge.EVENT_BUS.addListener(RecipesReceivedEvent.class, event -> RecipeHelper.setClientRecipes(event.getRecipeMap()));
  }
}
