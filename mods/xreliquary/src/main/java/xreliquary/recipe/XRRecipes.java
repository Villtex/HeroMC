package xreliquary.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import xreliquary.Reliquary;

/** The code recipe of the Reliquary: the void satchel (VoidSatchelHandler). */
public final class XRRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Reliquary.MOD_ID);

    static {
        SERIALIZERS.register("void_satchel", () -> SatchelRecipe.SERIALIZER);
    }

    private XRRecipes() {
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
