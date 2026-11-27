package slimeknights.mantle.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.condition.TagCombinationCondition;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.PotionDisplayIngredient;
import slimeknights.mantle.recipe.ingredient.PotionIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/**
 * Handles any custom recipes added by Mantle
 *
 * <p>Port: the cooking result recipes and the shaped fallback recipe are gone (no data or code of Tinkers 3.12.1 uses
 * them). Load conditions are registered here as NeoForge condition codecs.
 */
public class MantleRecipes {
  private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Mantle.modId);
  private static final DeferredRegister<IngredientType<?>> INGREDIENTS = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Mantle.modId);
  private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Mantle.modId);

  private MantleRecipes() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    RECIPES.register(bus);
    CONDITIONS.register(bus);
    INGREDIENTS.register(bus);
  }

  // crafting
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedRetexturedRecipe>> CRAFTING_SHAPED_RETEXTURED = RECIPES.register("crafting_shaped_retextured", () -> ShapedRetexturedRecipe.SERIALIZER);

  // ingredients
  public static final DeferredHolder<IngredientType<?>, IngredientType<PotionIngredient>> POTION_INGREDIENT = INGREDIENTS.register("potion", () -> new IngredientType<>(PotionIngredient.CODEC, PotionIngredient.STREAM_CODEC));
  public static final DeferredHolder<IngredientType<?>, IngredientType<PotionDisplayIngredient>> POTION_DISPLAY_INGREDIENT = INGREDIENTS.register("potion_display", () -> new IngredientType<>(PotionDisplayIngredient.CODEC, PotionDisplayIngredient.STREAM_CODEC));
  public static final DeferredHolder<IngredientType<?>, IngredientType<slimeknights.mantle.recipe.ingredient.EmptyIngredient>> EMPTY_INGREDIENT = INGREDIENTS.register("empty", () -> new IngredientType<>(slimeknights.mantle.recipe.ingredient.EmptyIngredient.CODEC, slimeknights.mantle.recipe.ingredient.EmptyIngredient.STREAM_CODEC));
  public static final DeferredHolder<IngredientType<?>, IngredientType<FluidContainerIngredient>> FLUID_CONTAINER_INGREDIENT = INGREDIENTS.register("fluid_container", () -> new IngredientType<>(FluidContainerIngredient.CODEC, FluidContainerIngredient.STREAM_CODEC));

  // conditions
  public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagEmptyCondition<?>>> TAG_EMPTY = CONDITIONS.register("tag_empty", () -> TagEmptyCondition.CODEC);
  public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagFilledCondition<?>>> TAG_FILLED = CONDITIONS.register("tag_filled", () -> TagFilledCondition.CODEC);
  public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagCombinationCondition<?>>> TAG_COMBINATION_FILLED = CONDITIONS.register("tag_combination_filled", () -> TagCombinationCondition.CODEC);
}
