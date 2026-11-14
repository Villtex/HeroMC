package myrathi.obsidiplates.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import myrathi.obsidiplates.ObsidiPlates;
import myrathi.obsidiplates.PlayerMobPlateBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** JEI: all eight plates are called "Pressure Plate" as in the original; info pages tell who presses them. */
@JeiPlugin
public class ObsidiPlatesJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(ObsidiPlates.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ObsidiPlates.ITEMS_IN_ORDER.forEach(item -> registration.addIngredientInfo(item.get(), Component.translatable(
                ((PlayerMobPlateBlock) item.get().getBlock()).playersOnly() ? "jei.obsidiplates.players" : "jei.obsidiplates.mobs")));
    }
}
