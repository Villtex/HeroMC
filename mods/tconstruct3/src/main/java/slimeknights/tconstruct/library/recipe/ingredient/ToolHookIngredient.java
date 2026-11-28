package slimeknights.tconstruct.library.recipe.ingredient;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Ingredient that only matches tools with a specific hook.
 * <p>Port: {@link ICustomIngredient}; an empty tag shows nothing (1.20.1 showed a named barrier).
 */
public class ToolHookIngredient implements ICustomIngredient {
  public static final Identifier ID = TConstruct.getResource("tool_hook");
  /** Loader instance */
  public static final RecordLoadable<ToolHookIngredient> LOADABLE = RecordLoadable.create(
    Loadables.ITEM_TAG.defaultField("tag", TinkerTags.Items.MODIFIABLE, i -> i.tag),
    ToolHooks.LOADER.requiredField("hook", i -> i.hook),
    ToolHookIngredient::new);

  private final TagKey<Item> tag;
  private final ModuleHook<?> hook;

  protected ToolHookIngredient(TagKey<Item> tag, ModuleHook<?> hook) {
    this.tag = tag;
    this.hook = hook;
  }

  public static ToolHookIngredient of(TagKey<Item> tag, ModuleHook<?> hook) {
    return new ToolHookIngredient(tag, hook);
  }

  public static ToolHookIngredient of(ModuleHook<?> hook) {
    return of(TinkerTags.Items.MODIFIABLE, hook);
  }

  /** Checks if the item has the hook */
  private boolean hasHook(Item item) {
    return item instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook);
  }

  @Override
  public boolean test(ItemStack stack) {
    return stack.is(tag) && hasHook(stack.getItem());
  }

  @Override
  public Stream<Holder<Item>> items() {
    return StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false).filter(holder -> hasHook(holder.value()));
  }

  /** Port: not simple, tool definitions (and their hooks) are data and change on reload */
  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredients.TOOL_HOOK.get();
  }
}
