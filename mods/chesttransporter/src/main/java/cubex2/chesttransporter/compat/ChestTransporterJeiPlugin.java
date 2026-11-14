package cubex2.chesttransporter.compat;

import cubex2.chesttransporter.ChestTransporter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** JEI: an info page on how the transporter is used. */
@JeiPlugin
public class ChestTransporterJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(ChestTransporter.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(ChestTransporter.CHEST_TRANSPORTER.get(), Component.translatable("jei.chesttransporter.chest_transporter"));
    }
}
