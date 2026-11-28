package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.List;

/**
 * Shapeless recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient} and
 * {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 * <p>Port: on the 26.3 shapeless recipe, saving to JSON is not supported.
 */
public class ShapelessMaterialsRecipe extends ShapelessRecipe implements MaterialsCraftingTableRecipe {
  /** Codec instance */
  public static final MapCodec<ShapelessMaterialsRecipe> CODEC = RecordCodecBuilder.<ShapelessMaterialsRecipe>mapCodec(inst -> inst.group(
    Recipe.CommonInfo.MAP_CODEC.forGetter(r -> new Recipe.CommonInfo(r.showNotification())),
    CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> new CraftingRecipe.CraftingBookInfo(r.category(), r.group())),
    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ShapelessRecipe::result),
    Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(r -> r.ingredients),
    Codec.INT.fieldOf("parts").forGetter(r -> r.partCount),
    ShapedMaterialsRecipe.EXTRA_MATERIALS.forGetter(r -> r.extraMaterials)
  ).apply(inst, ShapelessMaterialsRecipe::new)).validate(recipe -> {
    if (recipe.partCount < 1 || recipe.partCount > recipe.ingredients.size()) {
      return DataResult.error(() -> "Parts must be between 1 and the number of ingredients " + recipe.ingredients.size());
    }
    return DataResult.success(recipe);
  });

  /** Network codec */
  public static final StreamCodec<RegistryFriendlyByteBuf,ShapelessMaterialsRecipe> STREAM_CODEC = StreamCodec.of(
    (buffer, recipe) -> {
      Recipe.CommonInfo.STREAM_CODEC.encode(buffer, new Recipe.CommonInfo(recipe.showNotification()));
      CraftingRecipe.CraftingBookInfo.STREAM_CODEC.encode(buffer, new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()));
      ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result());
      Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.ingredients);
      buffer.writeByte(recipe.partCount);
      ShapedMaterialsRecipe.EXTRA_MATERIALS_STREAM.encode(buffer, recipe.extraMaterials);
    },
    buffer -> new ShapelessMaterialsRecipe(
      Recipe.CommonInfo.STREAM_CODEC.decode(buffer), CraftingRecipe.CraftingBookInfo.STREAM_CODEC.decode(buffer),
      ItemStackTemplate.STREAM_CODEC.decode(buffer), Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
      buffer.readByte(), ShapedMaterialsRecipe.EXTRA_MATERIALS_STREAM.decode(buffer)));

  /** Ingredients, the first {@link #partCount} are the parts */
  private final List<Ingredient> ingredients;
  /** Number of parts to match */
  @Getter
  private final int partCount;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;

  public ShapelessMaterialsRecipe(Recipe.CommonInfo common, CraftingRecipe.CraftingBookInfo book, ItemStackTemplate result, List<Ingredient> ingredients, int partCount, List<MaterialVariantId> extraMaterials) {
    super(common, book, result, ingredients);
    this.ingredients = List.copyOf(ingredients);
    this.partCount = partCount;
    this.extraMaterials = extraMaterials;
  }

  @Override
  public List<Ingredient> getParts() {
    return ingredients;
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    ShapedMaterialsRecipe.setMaterial(stack, material, extraMaterials);
  }

  @Override
  public ItemStack assemble(CraftingInput inventory) {
    return ShapedMaterialsRecipe.assemble(super.assemble(inventory), inventory, ingredients, partCount, false, extraMaterials);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public RecipeSerializer<ShapelessRecipe> getSerializer() {
    return (RecipeSerializer) TinkerTables.shapelessMaterialsRecipeSerializer.get();
  }
}
