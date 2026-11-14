package extrabiomes.compat;

import extrabiomes.EbxlBlocks;
import extrabiomes.Extrabiomes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * JEI: ExtrabiomesXL has only crafting and smelting recipes, which JEI shows by itself. The plugin adds info
 * pages for what no recipe explains (the log turner, the 2x2 saplings, quicksand, the scarecrow).
 * Loaded by JEI only when JEI is installed; the mod does not need it.
 */
@JeiPlugin
public class EbxlJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Extrabiomes.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(EbxlBlocks.LOG_TURNER.get(), Component.translatable("jei.extrabiomesxl.log_turner"));
        registration.addIngredientInfo(EbxlBlocks.FIR_SAPLING.get(), Component.translatable("jei.extrabiomesxl.fir_sapling"));
        registration.addIngredientInfo(EbxlBlocks.REDWOOD_SAPLING.get(), Component.translatable("jei.extrabiomesxl.redwood_sapling"));
        registration.addIngredientInfo(EbxlBlocks.QUICKSAND.get(), Component.translatable("jei.extrabiomesxl.quicksand"));
        registration.addIngredientInfo(EbxlBlocks.SCARECROW_ITEM.get(), Component.translatable("jei.extrabiomesxl.scarecrow"));
    }
}
