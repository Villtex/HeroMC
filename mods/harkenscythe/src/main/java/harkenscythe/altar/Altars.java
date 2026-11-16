package harkenscythe.altar;

import harkenscythe.HarkenScythe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The Altar of Souls and the Altar of Blood (Teil 9, part 2): the menu, the recipe type {@code harkenscythe_core:altar}
 * (data/harkenscythe_core/recipe/altar/) and its sync to the clients (for JEI). The blocks and the block entity are in
 * {@link harkenscythe.HSBlocks}.
 */
public final class Altars {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, HarkenScythe.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, HarkenScythe.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, HarkenScythe.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<AltarMenu>> ALTAR_MENU = MENUS.register("altar",
            () -> IMenuTypeExtension.create(AltarMenu::fromNetwork));
    public static final DeferredHolder<RecipeType<?>, RecipeType<AltarRecipe>> ALTAR_TYPE = TYPES.register("altar",
            () -> RecipeType.simple(HarkenScythe.id("altar")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AltarRecipe>> ALTAR_SERIALIZER = SERIALIZERS.register("altar",
            () -> AltarRecipe.SERIALIZER);

    private Altars() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
        TYPES.register(modBus);
        SERIALIZERS.register(modBus);
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> event.sendRecipes(ALTAR_TYPE.get()));
    }
}
