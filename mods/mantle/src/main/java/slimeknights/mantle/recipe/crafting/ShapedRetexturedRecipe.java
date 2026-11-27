package slimeknights.mantle.recipe.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.RetexturedHelper;

/**
 * Recipe which sets the texture for a retextured block based on an ingredient input.
 *
 * <p>Port: on the 26.3 shaped recipe (pattern + item stack template). {@code "texture"} is either a key of the pattern
 * (one character) or an ingredient, as in 1.20.1.
 */
@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  /** Ingredient used to determine the texture on the output */
  @Getter
  private final Ingredient texture;
  private final boolean matchAll;
  private final ItemStackTemplate resultTemplate;
  /** Original JSON form of the texture, kept for saving */
  private final Either<String,Ingredient> textureSource;

  public ShapedRetexturedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result, Either<String,Ingredient> texture, Ingredient resolved, boolean matchAll) {
    super(commonInfo, bookInfo, pattern, result);
    this.resultTemplate = result;
    this.textureSource = texture;
    this.texture = resolved;
    this.matchAll = matchAll;
  }

  /** Codec reading the shaped recipe and the texture */
  public static final MapCodec<ShapedRetexturedRecipe> CODEC = RecordCodecBuilder.<Raw>mapCodec(inst -> inst.group(
    ShapedRecipe.MAP_CODEC.forGetter(Raw::shaped),
    // read a second time for the key characters (the pattern keeps them private)
    ShapedRecipePattern.Data.MAP_CODEC.forGetter(raw -> null),
    Codec.either(Codec.STRING, Ingredient.CODEC).fieldOf("texture").forGetter(Raw::texture),
    Codec.BOOL.optionalFieldOf("match_all", false).forGetter(Raw::matchAll)
  ).apply(inst, Raw::new)).flatXmap(Raw::resolve, recipe -> DataResult.error(() -> "Saving mantle:crafting_shaped_retextured is not supported"));

  /** Network codec */
  public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRetexturedRecipe> STREAM_CODEC = StreamCodec.of(
    (buffer, recipe) -> {
      ShapedRecipe.STREAM_CODEC.encode(buffer, recipe);
      Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.texture);
      buffer.writeBoolean(recipe.matchAll);
    },
    buffer -> {
      ShapedRecipe shaped = ShapedRecipe.STREAM_CODEC.decode(buffer);
      Ingredient texture = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
      boolean matchAll = buffer.readBoolean();
      return copy(shaped, Either.right(texture), texture, matchAll);
    });

  /** Serializer instance */
  public static final RecipeSerializer<ShapedRetexturedRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

  /** Builds the recipe from a shaped recipe */
  private static ShapedRetexturedRecipe copy(ShapedRecipe shaped, Either<String,Ingredient> source, Ingredient texture, boolean matchAll) {
    ItemStackTemplate result = ItemStackTemplate.fromNonEmptyStack(shaped.assemble(CraftingInput.EMPTY));
    return new ShapedRetexturedRecipe(new Recipe.CommonInfo(shaped.showNotification()), new CraftingRecipe.CraftingBookInfo(shaped.category(), shaped.group()), shaped.pattern, result, source, texture, matchAll);
  }

  /** Decoded form before resolving the texture key */
  private record Raw(ShapedRecipe shaped, ShapedRecipePattern.Data data, Either<String,Ingredient> texture, boolean matchAll) {
    DataResult<ShapedRetexturedRecipe> resolve() {
      Ingredient resolved;
      if (texture.left().isPresent()) {
        String key = texture.left().get();
        if (key.length() != 1) {
          return DataResult.error(() -> "Invalid texture key: '" + key + "' is an invalid symbol (must be 1 character only).");
        }
        resolved = data.key().get(key.charAt(0));
        if (resolved == null) {
          return DataResult.error(() -> "Texture ingredient references symbol '" + key + "' but it's not defined in the key");
        }
      } else {
        resolved = texture.right().orElseThrow();
      }
      return DataResult.success(copy(shaped, texture, resolved, matchAll));
    }
  }

  /**
   * Gets the output using the given texture
   * @param texture  Texture to use
   * @return  Output with texture. Will be blank if the input is not a block
   */
  public ItemStack getResultItem(Item texture) {
    return RetexturedHelper.setTexture(resultTemplate.create(), Block.byItem(texture));
  }

  @Override
  public ItemStack assemble(CraftingInput craftMatrix) {
    ItemStack result = super.assemble(craftMatrix);
    Block currentTexture = null;
    for (int i = 0; i < craftMatrix.size(); i++) {
      ItemStack stack = craftMatrix.getItem(i);
      if (!stack.isEmpty() && texture.test(stack)) {
        // fetch texture from the block if it has one
        Block block = RetexturedHelper.getTexture(stack);
        // assuming it does not, use the block itself as the texture (provided it is not the result that is)
        if (block == Blocks.AIR && stack.getItem() != result.getItem()) {
          block = Block.byItem(stack.getItem());
        }
        // if no texture, skip
        if (block == Blocks.AIR) {
          continue;
        }
        // if we have not found a texture yet, store the found block
        if (currentTexture == null) {
          currentTexture = block;
          // match all means we must check the rest. If not match all, we can be done
          if (!matchAll) {
            break;
          }
          // if we found a texture before, must match or we do no texture
        } else if (currentTexture != block) {
          currentTexture = null;
          break;
        }
      }
    }
    // set the texture if found. No texture will use the fallback
    if (currentTexture != null) {
      return RetexturedHelper.setTexture(result, currentTexture);
    }
    return result;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public RecipeSerializer<ShapedRecipe> getSerializer() {
    return (RecipeSerializer) MantleRecipes.CRAFTING_SHAPED_RETEXTURED.get();
  }
}
