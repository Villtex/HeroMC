package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.stream.Stream;

/**
 * Port (found in Teil 27c): an ingredient matching nothing ({@code mantle:empty}). 26.3 refuses empty vanilla
 * ingredients ("Ingredients can't be empty"), but Mantle and Tinkers use empty ingredients as "not set".
 */
public final class EmptyIngredient implements ICustomIngredient {
  public static final EmptyIngredient INSTANCE = new EmptyIngredient();
  public static final MapCodec<EmptyIngredient> CODEC = MapCodec.unit(INSTANCE);
  public static final StreamCodec<ByteBuf, EmptyIngredient> STREAM_CODEC = StreamCodec.unit(INSTANCE);

  private EmptyIngredient() {}

  /** New empty ingredient */
  public static Ingredient ingredient() {
    return new Ingredient(INSTANCE);
  }

  @Override
  public boolean test(ItemStack stack) {
    return false;
  }

  @Override
  public Stream<Holder<Item>> items() {
    return Stream.empty();
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public SlotDisplay display() {
    return SlotDisplay.Empty.INSTANCE;
  }

  @Override
  public IngredientType<?> getType() {
    return MantleRecipes.EMPTY_INGREDIENT.get();
  }
}
