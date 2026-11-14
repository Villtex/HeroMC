package shadwdrgn.soulshards.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import shadwdrgn.soulshards.SoulShards;

/** JEI: no recipe types of its own; info pages for the soul rune, the shard and the cage. */
@JeiPlugin
public class SoulShardsJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(SoulShards.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(SoulShards.BLANK_SOUL_SHARD.get(), Component.translatable("jei.soulshards.blank_soul_shard"));
        registration.addIngredientInfo(SoulShards.SOUL_SHARD.get(), Component.translatable("jei.soulshards.soul_shard"));
        registration.addIngredientInfo(SoulShards.SOUL_CAGE_ITEM.get(), Component.translatable("jei.soulshards.soul_cage"));
    }
}
