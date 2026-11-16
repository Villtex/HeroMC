package artifice;

import artifice.recipe.BoxRecipe;
import artifice.recipe.UpgradeRecipe;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The data component of the box and the two special crafting recipes (RecipeBox, RecipeUpgrade). */
public final class ArtificeRecipes {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,
            Artifice.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Artifice.MOD_ID);

    /**
     * What a box holds: the item with its components, the count is the amount (the NBT "id"/"meta"/"nbt" and the
     * item damage of the 1.5.2 box).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStackTemplate>> BOX_CONTENTS = COMPONENTS.registerComponentType(
            "box_contents", b -> b.persistent(ItemStackTemplate.CODEC).networkSynchronized(ItemStackTemplate.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BoxRecipe>> BOX = SERIALIZERS.register("box",
            () -> BoxRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<UpgradeRecipe>> UPGRADE = SERIALIZERS.register("upgrade",
            () -> UpgradeRecipe.SERIALIZER);

    private ArtificeRecipes() {
    }

    static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        SERIALIZERS.register(modBus);
    }
}
