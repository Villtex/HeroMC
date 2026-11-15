package sct.hexxitgear.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import sct.hexxitgear.HexxitGear;

/** JEI: only crafting recipes, which JEI shows by itself; info pages for the hexbiscus and the set bonuses. */
@JeiPlugin
public class HexxitGearJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(HexxitGear.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(HexxitGear.HEXBISCUS_ITEM.get(), Component.translatable("jei.hexxitgear.hexbiscus"));
        registration.addIngredientInfo(HexxitGear.HEXICAL_ESSENCE.get(), Component.translatable("jei.hexxitgear.hexbiscus"));
        info(registration, "jei.hexxitgear.tribal", HexxitGear.TRIBAL_HELMET.get(), HexxitGear.TRIBAL_CHEST.get(),
                HexxitGear.TRIBAL_LEGGINGS.get(), HexxitGear.TRIBAL_BOOTS.get());
        info(registration, "jei.hexxitgear.scale", HexxitGear.SCALE_HELMET.get(), HexxitGear.SCALE_CHEST.get(),
                HexxitGear.SCALE_LEGGINGS.get(), HexxitGear.SCALE_BOOTS.get());
        info(registration, "jei.hexxitgear.thief", HexxitGear.THIEF_HELMET.get(), HexxitGear.THIEF_CHEST.get(),
                HexxitGear.THIEF_LEGGINGS.get(), HexxitGear.THIEF_BOOTS.get());
    }

    private static void info(IRecipeRegistration registration, String key, Item... items) {
        for (Item item : items) {
            registration.addIngredientInfo(item, Component.translatable(key));
        }
    }
}
