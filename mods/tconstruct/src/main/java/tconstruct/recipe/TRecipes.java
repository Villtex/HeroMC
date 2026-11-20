package tconstruct.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import tconstruct.TConstruct;

/** The recipe types of code: the chisel in the crafting grid. */
public final class TRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TConstruct.MOD_ID);

    static {
        SERIALIZERS.register("chisel_detailing", () -> ChiselRecipe.SERIALIZER);
    }

    private TRecipes() {
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
