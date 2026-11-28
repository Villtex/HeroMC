package slimeknights.tconstruct.library.modifiers.modules.interaction.edible;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.EdibleEffectHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module for making eating a tool remove all effects using an item. */
public record EdibleCureEffectsModule(slimeknights.mantle.recipe.helper.ItemOutput curativeItemOutput, IJsonPredicate<LivingEntity> holder, ModifierCondition<IToolStackView> condition) implements ModifierModule, EdibleEffectHook, ConditionalModule<IToolStackView> {
  /** Port (Teil 27e): the stack is made on first use, 26.3 has no item components while data loads */
  public ItemStack curativeItem() {
    return curativeItemOutput.get();
  }

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<EdibleCureEffectsModule>defaultHooks(ModifierHooks.EDIBLE_EFFECT);
  public static final RecordLoadable<EdibleCureEffectsModule> LOADER = RecordLoadable.create(
    slimeknights.mantle.recipe.helper.ItemOutput.Loadable.REQUIRED_ITEM.requiredField("curative_item", EdibleCureEffectsModule::curativeItemOutput),
    LivingEntityPredicate.LOADER.defaultField("holder", EdibleCureEffectsModule::holder),
    ModifierCondition.TOOL_FIELD, EdibleCureEffectsModule::new);

  public EdibleCureEffectsModule(ItemLike item) {
    this(slimeknights.mantle.recipe.helper.ItemOutput.fromItem(item), LivingEntityPredicate.ANY, ModifierCondition.ANY_TOOL);
  }

  @Override
  public RecordLoadable<EdibleCureEffectsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onToolEaten(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot eatenSlot, int hunger, float saturation, List<ItemStack> representativeItems) {
    if (condition.matches(tool, modifier) && holder.matches(player)) {
      slimeknights.tconstruct.library.utils.EffectCures.cure(player, curativeItem());
    }
  }
}
