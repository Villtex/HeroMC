package legendgear.recipe;

import legendgear.LegendGear;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The two code recipes of LegendGear.load: emptying a quiver or bomb bag, and putting a medallion into a sword. */
public final class LGRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, LegendGear.MOD_ID);

    static {
        SERIALIZERS.register("empty_ammo_bag", () -> EmptyAmmoBagRecipe.SERIALIZER);
        SERIALIZERS.register("medallion_sword", () -> MedallionSwordRecipe.SERIALIZER);
    }

    private LGRecipes() {
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
