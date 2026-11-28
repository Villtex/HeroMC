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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Shaped recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient} and
 * {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 * <p>Port: on the 26.3 shaped recipe; JSON as in 1.20.1 ({@code "parts"} is a string of key characters). Saving to JSON
 * is not supported (no datagen).
 */
public class ShapedMaterialsRecipe extends ShapedRecipe implements MaterialsCraftingTableRecipe {
  /** Codec for the extra materials */
  static final MapCodec<List<MaterialVariantId>> EXTRA_MATERIALS = MaterialVariantId.CODEC.listOf().optionalFieldOf("extra_materials", List.of());
  /** Network codec for the extra materials */
  static final StreamCodec<RegistryFriendlyByteBuf,List<MaterialVariantId>> EXTRA_MATERIALS_STREAM = ByteBufCodecs.fromCodecWithRegistries(MaterialVariantId.CODEC).apply(ByteBufCodecs.list());

  /** Fields of a 26.3 shaped recipe, with the key kept for {@code "parts"} */
  record Shaped(Recipe.CommonInfo common, CraftingRecipe.CraftingBookInfo book, ShapedRecipePattern.Data data, ItemStackTemplate result) {
    static final MapCodec<Shaped> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Recipe.CommonInfo.MAP_CODEC.forGetter(Shaped::common),
      CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(Shaped::book),
      ShapedRecipePattern.Data.MAP_CODEC.forGetter(Shaped::data),
      ItemStackTemplate.CODEC.fieldOf("result").forGetter(Shaped::result)
    ).apply(inst, Shaped::new));

    ShapedRecipePattern pattern() {
      return ShapedRecipePattern.of(data.key(), data.pattern());
    }
  }

  /** Codec instance */
  public static final MapCodec<ShapedMaterialsRecipe> CODEC = RecordCodecBuilder.<Raw>mapCodec(inst -> inst.group(
    Shaped.CODEC.forGetter(Raw::shaped),
    Codec.STRING.fieldOf("parts").forGetter(Raw::parts),
    EXTRA_MATERIALS.forGetter(Raw::extra)
  ).apply(inst, Raw::new)).flatXmap(ShapedMaterialsRecipe::resolve, recipe -> DataResult.error(() -> "Saving tconstruct3:crafting_shaped_materials is not supported"));

  /** Network codec */
  public static final StreamCodec<RegistryFriendlyByteBuf,ShapedMaterialsRecipe> STREAM_CODEC = StreamCodec.of(
    (buffer, recipe) -> {
      Recipe.CommonInfo.STREAM_CODEC.encode(buffer, new Recipe.CommonInfo(recipe.showNotification()));
      CraftingRecipe.CraftingBookInfo.STREAM_CODEC.encode(buffer, new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()));
      ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
      ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
      Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.parts);
      EXTRA_MATERIALS_STREAM.encode(buffer, recipe.extraMaterials);
    },
    buffer -> new ShapedMaterialsRecipe(
      Recipe.CommonInfo.STREAM_CODEC.decode(buffer), CraftingRecipe.CraftingBookInfo.STREAM_CODEC.decode(buffer),
      ShapedRecipePattern.STREAM_CODEC.decode(buffer), ItemStackTemplate.STREAM_CODEC.decode(buffer),
      List.copyOf(Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer)), EXTRA_MATERIALS_STREAM.decode(buffer)));

  /** Decoded JSON before resolving the parts */
  private record Raw(Shaped shaped, String parts, List<MaterialVariantId> extra) {}

  /** Builds the recipe from the JSON parts */
  private static DataResult<ShapedMaterialsRecipe> resolve(Raw raw) {
    Shaped shaped = raw.shaped();
    String partPattern = raw.parts();
    // specific to shaped part recipe, map from a pattern string to the ingredients for each character
    List<Ingredient> parts = new ArrayList<>();
    for (int i = 0; i < partPattern.length(); i++) {
      char sym = partPattern.charAt(i);
      Ingredient ingredient = shaped.data().key().get(sym);
      if (ingredient == null) {
        return DataResult.error(() -> "Parts references symbol '" + sym + "' but it's not defined in the key");
      }
      parts.add(ingredient);
    }
    try {
      return DataResult.success(new ShapedMaterialsRecipe(shaped.common(), shaped.book(), shaped.pattern(), shaped.result(), List.copyOf(parts), raw.extra()));
    } catch (RuntimeException e) {
      return DataResult.error(e::getMessage);
    }
  }

  /** List of tool parts to search for in the final recipe */
  @Getter
  private final List<Ingredient> parts;
  /**
   * If true, a part may show up multiple times in the inputs, and all copies should match.
   * If false, only the first instance of a part is checked for each input, allowing a tool with the same part multiple times.
   */
  private final boolean checkRepeats;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;
  private final ItemStackTemplate result;
  public ShapedMaterialsRecipe(Recipe.CommonInfo common, CraftingRecipe.CraftingBookInfo book, ShapedRecipePattern pattern, ItemStackTemplate result, List<Ingredient> parts, List<MaterialVariantId> extraMaterials) {
    super(common, book, pattern, result);
    this.result = result;
    this.parts = parts;
    this.checkRepeats = parts.stream().unordered().distinct().count() == parts.size();
    this.extraMaterials = extraMaterials;
  }

  @Override
  public int getPartCount() {
    return parts.size();
  }

  /**
   * Finds materials for each of the parts
   * @return Array of all matched materials. Array will have no null entries, though the array may be null if no match was found.
   */
  @Nullable
  static MaterialVariantId[] findMaterials(CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats) {
    // want one material for each
    MaterialVariantId[] materials = new MaterialVariantId[partCount];
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        for (int p = 0; p < partCount; p++) {
          MaterialVariantId current = materials[p];
          // if we have not found the material yet, or repeats are considered the same material, test the ingredient
          if ((current == null || checkRepeats) && parts.get(p).test(stack)) {
            MaterialVariantId matched = MaterialRecipeCache.getMaterial(stack);
            // first occurrence? thats our material
            if (current == null) {
              materials[p] = matched;
              break;
            } else if (!current.matchesVariant(matched)) {
              // if same material but different variants, just discard the variant
              if (current.getId().equals(matched.getId())) {
                materials[p] = current.getId();
                break;
              } else {
                // if different materials, no match
                return null;
              }
            }
          }
        }
      }
    }
    // ensure we found all materials needed
    for (int p = 0; p < partCount; p++) {
      if (materials[p] == null) {
        return null;
      }
    }
    return materials;
  }

  @Override
  public boolean matches(CraftingInput inventory, Level level) {
    if (!super.matches(inventory, level)) {
      return false;
    }
    // ensure all part materials matched and we found all parts
    return findMaterials(inventory, parts, parts.size(), checkRepeats) != null;
  }

  /** Common logic to this and {@link ShapedMaterialsRecipe} */
  public static void setMaterial(ItemStack stack, MaterialVariantId material, List<MaterialVariantId> extraMaterials) {
    if (extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
      materialItem.setMaterial(stack, material);
    } else {
      MaterialNBT.Builder builder = MaterialNBT.builder();
      builder.add(material);
      for (MaterialVariantId extraMaterial : extraMaterials) {
        builder.add(extraMaterial);
      }
      ToolStack.from(stack).setMaterials(builder.build());
    }
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    setMaterial(stack, material, extraMaterials);
  }

  /** Assembles the item with material information */
  static ItemStack assemble(ItemStack stack, CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats, List<MaterialVariantId> extraMaterials) {
    MaterialVariantId[] materials = findMaterials(inventory, parts, partCount, checkRepeats);
    if (materials != null) {
      // if the result is a tool part, and we only have the one material, set its material
      if (materials.length == 1 && extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
        return materialItem.setMaterial(stack, materials[0]);
      }
      MaterialNBT.Builder builder = MaterialNBT.builder();
      // add each material
      for (MaterialVariantId material : materials) {
        builder.add(material);
      }
      // add extra materials
      builder.add(extraMaterials);
      ToolStack.from(stack).setMaterials(builder.build());
    }
    return stack;
  }

  @Override
  public ItemStack assemble(CraftingInput inventory) {
    return assemble(super.assemble(inventory), inventory, parts, parts.size(), checkRepeats, extraMaterials);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer) TinkerTables.shapedMaterialsRecipeSerializer.get();
  }
}
