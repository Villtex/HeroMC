package idiamondhunter.morebows.compat;

import idiamondhunter.morebows.MoreBows;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** JEI: an info page per bow (draw time and what it does). */
@JeiPlugin
public class MoreBowsJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(MoreBows.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        MoreBows.BOWS.forEach(bow -> registration.addIngredientInfo(bow.get(),
                Component.translatable("jei.morebows." + BuiltInRegistries.ITEM.getKey(bow.get()).getPath())));
    }
}
