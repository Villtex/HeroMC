package harkenscythe.client;

import harkenscythe.altar.AltarRecipe;
import harkenscythe.altar.Altars;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

/** The altar recipes the server sent (Altars asks for them in OnDatapackSyncEvent), for JEI. */
public final class AltarRecipesClient {
    private static List<RecipeHolder<AltarRecipe>> recipes = List.of();

    private AltarRecipesClient() {
    }

    public static List<RecipeHolder<AltarRecipe>> recipes() {
        return recipes;
    }

    static void register() {
        // before JEI, which starts on the same event
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (RecipesReceivedEvent event) ->
                recipes = List.copyOf(event.getRecipeMap().byType(Altars.ALTAR_TYPE.get())));
    }
}
