package slimeknights.mantle.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;
import slimeknights.mantle.loot.entry.TagPreferenceLootEntry;
import slimeknights.mantle.loot.function.SetFluidLootFunction;
import slimeknights.mantle.loot.AddEntryLootModifier;
import slimeknights.mantle.loot.ReplaceItemLootModifier;
import slimeknights.mantle.loot.condition.AndLootModifierCondition;
import slimeknights.mantle.loot.condition.ContainsItemModifierLootCondition;
import slimeknights.mantle.loot.condition.ContextLootModifierCondition;
import slimeknights.mantle.loot.condition.EmptyModifierLootCondition;
import slimeknights.mantle.loot.condition.ILootModifierCondition;
import slimeknights.mantle.loot.condition.InvertedModifierLootCondition;
import slimeknights.mantle.loot.condition.OrLootModifierCondition;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;

import java.util.Objects;

/** Handles any loot table registration */
public class MantleLoot {
  private static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Mantle.modId);
  private static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Mantle.modId);
  private static final DeferredRegister<MapCodec<? extends LootPoolEntryContainer>> LOOT_ENTRIES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, Mantle.modId);
  private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Mantle.modId);


  private MantleLoot() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    LOOT_CONDITIONS.register(bus);
    LOOT_FUNCTIONS.register(bus);
    LOOT_ENTRIES.register(bus);
    LOOT_MODIFIERS.register(bus);
    // loot modifier conditions
    ILootModifierCondition.LOADER.register(Mantle.getResource("inverted"), InvertedModifierLootCondition.CODEC);
    ILootModifierCondition.LOADER.register(Mantle.getResource("and"), AndLootModifierCondition.CODEC);
    ILootModifierCondition.LOADER.register(Mantle.getResource("or"), OrLootModifierCondition.CODEC);
    ILootModifierCondition.LOADER.register(Mantle.getResource("context"), ContextLootModifierCondition.CODEC);
    ILootModifierCondition.LOADER.register(Mantle.getResource("contains_item"), ContainsItemModifierLootCondition.CODEC);
    ILootModifierCondition.LOADER.register(Mantle.getResource("empty"), EmptyModifierLootCondition.CODEC);
  }

  /** Port: in 26.3 loot types are their map codecs */
  public static final Holder<MapCodec<? extends IGlobalLootModifier>> ADD_ENTRY = LOOT_MODIFIERS.register("add_entry", () -> AddEntryLootModifier.CODEC);
  public static final Holder<MapCodec<? extends IGlobalLootModifier>> REPLACE_ITEM = LOOT_MODIFIERS.register("replace_item", () -> ReplaceItemLootModifier.CODEC);

  /** Matches if the passed tag is empty */
  public static final Holder<MapCodec<? extends LootItemCondition>> TAG_EMPTY = LOOT_CONDITIONS.register("tag_empty", () -> TagEmptyCondition.CODEC);
  /** Matches if the passed tag is filled */
  public static final Holder<MapCodec<? extends LootItemCondition>> TAG_FILLED = LOOT_CONDITIONS.register("tag_filled", () -> slimeknights.mantle.recipe.condition.TagFilledCondition.CODEC);
  /** Condition to match a block tag and property predicate */
  public static final Holder<MapCodec<? extends LootItemCondition>> BLOCK_TAG_CONDITION = LOOT_CONDITIONS.register("block_tag", () -> BlockTagLootCondition.CODEC);
  /** Condition for global loot modifiers that ensures a context set is present. Useful to check if we are in a specific context like entity. */
  public static final Holder<MapCodec<? extends LootItemCondition>> HAS_CONTEXT_SET = LOOT_CONDITIONS.register("has_context_set", () -> HasLootContextSetCondition.CODEC);

  // PORT-TODO(27b block): fill_retextured_block (RetexturedLootFunction, needs IRetexturedBlockEntity)
  /** Function to add a fluid to an item fluid capability */
  public static final Holder<MapCodec<? extends LootItemFunction>> SET_FLUID_FUNCTION = LOOT_FUNCTIONS.register("set_fluid", () -> SetFluidLootFunction.CODEC);

  /** Entry to pull a value from a tag preference */
  public static final Holder<MapCodec<? extends LootPoolEntryContainer>> TAG_PREFERENCE = LOOT_ENTRIES.register("tag_preference", () -> TagPreferenceLootEntry.CODEC);

}
