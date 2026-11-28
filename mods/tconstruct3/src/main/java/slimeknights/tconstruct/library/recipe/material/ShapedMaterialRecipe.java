package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.helper.RecipeIdContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Shaped recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the material of the result.
 * <p>Port: on the 26.3 shaped recipe, saving to JSON is not supported.
 * @deprecated use {@link ShapedMaterialsRecipe}, which requires specifying the ingredients for each part.
 */
@Deprecated
public class ShapedMaterialRecipe extends ShapedRecipe {
  /** Codec instance */
  public static final MapCodec<ShapedMaterialRecipe> CODEC = RecordCodecBuilder.<ShapedMaterialRecipe>mapCodec(inst -> inst.group(
    Recipe.CommonInfo.MAP_CODEC.forGetter(r -> new Recipe.CommonInfo(r.showNotification())),
    CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> new CraftingRecipe.CraftingBookInfo(r.category(), r.group())),
    ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
    ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
    ShapedMaterialsRecipe.EXTRA_MATERIALS.forGetter(r -> r.extraMaterials)
  ).apply(inst, ShapedMaterialRecipe::new)).validate(recipe -> {
    // ensure the material is valid, since we have all the needed information to check; better now than at runtime
    if (recipe.getMaterial() == null) {
      return DataResult.error(() -> "Invalid material ingredients for shaped material recipe " + recipe.id);
    }
    return DataResult.success(recipe);
  });

  /** Network codec */
  public static final StreamCodec<RegistryFriendlyByteBuf,ShapedMaterialRecipe> STREAM_CODEC = StreamCodec.of(
    (buffer, recipe) -> {
      Recipe.CommonInfo.STREAM_CODEC.encode(buffer, new Recipe.CommonInfo(recipe.showNotification()));
      CraftingRecipe.CraftingBookInfo.STREAM_CODEC.encode(buffer, new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()));
      ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
      ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
      ShapedMaterialsRecipe.EXTRA_MATERIALS_STREAM.encode(buffer, recipe.extraMaterials);
    },
    buffer -> new ShapedMaterialRecipe(
      Recipe.CommonInfo.STREAM_CODEC.decode(buffer), CraftingRecipe.CraftingBookInfo.STREAM_CODEC.decode(buffer),
      ShapedRecipePattern.STREAM_CODEC.decode(buffer), ItemStackTemplate.STREAM_CODEC.decode(buffer), ShapedMaterialsRecipe.EXTRA_MATERIALS_STREAM.decode(buffer)));

  private final Identifier id;
  private final ItemStackTemplate result;
  private MaterialValueIngredient material;
  private final List<MaterialVariantId> extraMaterials;
  public ShapedMaterialRecipe(Recipe.CommonInfo common, CraftingRecipe.CraftingBookInfo book, ShapedRecipePattern pattern, ItemStackTemplate result, List<MaterialVariantId> extraMaterials) {
    super(common, book, pattern, result);
    this.id = RecipeIdContext.getOrUnknown();
    this.result = result;
    this.extraMaterials = extraMaterials;
  }

  /** Gets the material to match */
  @Nullable
  public MaterialValueIngredient getMaterial() {
    if (material == null) {
      // assume all material ingredients match the same stat type
      for (Ingredient ingredient : pattern.ingredients().stream().flatMap(java.util.Optional::stream).toList()) {
        // collect all ingredients that match
        if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof MaterialValueIngredient materialValue) {
          if (material == null) {
            material = materialValue;
          } else {
            // ensure the stat type matches, and expand the range
            material = material.merge(materialValue);
          }
        }
      }
      // if we found no materials, that is also an issue
      if (material == null) {
        TConstruct.LOG.error("No material ingredient found for material shaped recipe {}, this indicates a broken recipe", id);
      }
    }
    return material;
  }

  @Nullable
  private MaterialVariantId findMaterial(CraftingInput inventory) {
    MaterialValueIngredient material = getMaterial();
    if (material == null) {
      return null;
    }
    // ensure same material in all slots
    MaterialVariantId firstMaterial = null;
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        // ignore anything that does not meet our requirements
        MaterialVariantId matchedMaterial = material.getMaterial(stack);
        if (matchedMaterial != null) {
          // first match is set
          if (firstMaterial == null) {
            firstMaterial = matchedMaterial;
          } else if (!firstMaterial.matchesVariant(matchedMaterial)) {
            // if same material but different variants, just discard the variant
            if (firstMaterial.getId().equals(matchedMaterial.getId())) {
              firstMaterial = firstMaterial.getId();
            } else {
              // if different materials, no match
              return null;
            }
          }
        }
      }
    }
    return firstMaterial;
  }

  @Override
  public boolean matches(CraftingInput inventory, Level level) {
    if (!super.matches(inventory, level)) {
      return false;
    }

    // must have a material to match, no mixing
    return findMaterial(inventory) != null;
  }

  /** Sets the material for the given stack */
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    ShapedMaterialsRecipe.setMaterial(stack, material, extraMaterials);
  }

  @Override
  public ItemStack assemble(CraftingInput inventory) {
    ItemStack stack = super.assemble(inventory);
    MaterialVariantId material = findMaterial(inventory);
    if (material != null) {
      setMaterial(stack, material);
    }
    return stack;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer) TinkerTables.shapedMaterialRecipeSerializer.get();
  }
}
