package slimeknights.mantle.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import slimeknights.mantle.loot.condition.ILootModifierCondition;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Loot modifier to inject an additional loot entry into an existing table */
public class AddEntryLootModifier extends LootModifier {
  public static final MapCodec<AddEntryLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(inst.group(
    ILootModifierCondition.LOADER.codec().listOf().fieldOf("post_conditions").forGetter(m -> m.modifierConditions),
    LootPoolEntries.CODEC.fieldOf("entry").forGetter(m -> m.entry),
    LootItemFunctions.CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(m -> m.functions))
  ).apply(inst, AddEntryLootModifier::new));

  /** Additional conditions that can consider the previously generated loot */
  private final List<ILootModifierCondition> modifierConditions;
  /** Entry for generating loot */
	private final LootPoolEntryContainer entry;
  /** Functions to apply to the entry, allows adding functions to parented loot entries such as alternatives */
	private final List<Holder<LootItemFunction>> functions;
  /** Functions merged into a single function for ease of use */
	private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;

	protected AddEntryLootModifier(Optional<Holder<LootItemCondition>> condition, int priority, List<ILootModifierCondition> modifierConditions, LootPoolEntryContainer entry, List<Holder<LootItemFunction>> functions) {
    super(condition, priority);
    this.modifierConditions = modifierConditions;
    this.entry = entry;
		this.functions = functions;
		this.combinedFunctions = compose(functions);
	}

  /** Port: 26.3 loot functions are holders; applied in order */
  static BiFunction<ItemStack, LootContext, ItemStack> compose(List<Holder<LootItemFunction>> functions) {
    return (stack, context) -> {
      for (Holder<LootItemFunction> function : functions) {
        stack = function.value().apply(stack, context);
      }
      return stack;
    };
  }

  @Nonnull
	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    // if any condition fails, exit immediately
    for (ILootModifierCondition modifierCondition : modifierConditions) {
      if (!modifierCondition.test(generatedLoot, context)) {
        return generatedLoot;
      }
    }
    // generate the actual entry
    Consumer<ItemStack> consumer = stack -> generatedLoot.add(this.combinedFunctions.apply(stack, context));
    entry.expand(context, generator -> generator.createItemStack(consumer, context));
		return generatedLoot;
	}

  @Override
  public MapCodec<AddEntryLootModifier> codec() {
    return CODEC;
  }
}
