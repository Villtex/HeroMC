package slimeknights.mantle.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import slimeknights.mantle.recipe.helper.ItemOutput;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;

/** Loot modifier to replace an item with another */
public class ReplaceItemLootModifier extends LootModifier {
  public static final MapCodec<ReplaceItemLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(inst.group(
    new slimeknights.mantle.data.loadable.LoadableCodec<>(slimeknights.mantle.data.loadable.common.IngredientLoadable.DISALLOW_EMPTY).fieldOf("original").forGetter(m -> m.original),
    ItemOutput.REQUIRED_STACK_CODEC.fieldOf("replacement").forGetter(m -> m.replacement),
    LootItemFunctions.CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(m -> m.functions)
  )).apply(inst, ReplaceItemLootModifier::new));

  /** Ingredient to test for the original item */
  private final Ingredient original;
  /** Item for the replacement */
  private final ItemOutput replacement;
  /** Functions to apply to the replacement */
  private final List<Holder<LootItemFunction>> functions;
  /** Functions merged into a single function for ease of use */
  private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;

  protected ReplaceItemLootModifier(Optional<Holder<LootItemCondition>> condition, int priority, Ingredient original, ItemOutput replacement, List<Holder<LootItemFunction>> functions) {
    super(condition, priority);
    this.original = original;
    this.replacement = replacement;
    this.functions = functions;
    this.combinedFunctions = AddEntryLootModifier.compose(functions);
  }

  @Nonnull
  @Override
  protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    ListIterator<ItemStack> iterator = generatedLoot.listIterator();
    while (iterator.hasNext()) {
      ItemStack stack = iterator.next();
      if (original.test(stack)) {
        ItemStack replacement = this.replacement.get();
        iterator.set(combinedFunctions.apply(replacement.copyWithCount(replacement.getCount() * stack.getCount()), context));
      }
    }
    return generatedLoot;
  }

  @Override
  public MapCodec<ReplaceItemLootModifier> codec() {
    return CODEC;
  }
}
