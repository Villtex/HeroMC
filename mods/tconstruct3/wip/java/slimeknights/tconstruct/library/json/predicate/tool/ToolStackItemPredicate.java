package slimeknights.tconstruct.library.json.predicate.tool;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.data.loadable.LoadableCodec;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags.Items;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * Item predicate matching Tinker tools with a {@link ToolStackPredicate}. 26.3: an item sub-predicate
 * ({@code "predicates": {"tconstruct3:tool_stack": <tool predicate>}} in item predicates), registered as
 * data component predicate type; 1.20.1 extended {@code ItemPredicate}, which is final now.
 */
public record ToolStackItemPredicate(IJsonPredicate<IToolStackView> predicate) implements DataComponentPredicate {
  public static final Identifier ID = TConstruct.getResource("tool_stack");
  public static final Codec<ToolStackItemPredicate> CODEC = new LoadableCodec<>(ToolStackPredicate.LOADER).xmap(ToolStackItemPredicate::new, ToolStackItemPredicate::predicate);

  private static final DeferredRegister<DataComponentPredicate.Type<?>> TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_PREDICATE_TYPE, TConstruct.MOD_ID);
  /** Predicate type {@code tconstruct3:tool_stack} */
  public static final DeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<ToolStackItemPredicate>> TYPE = TYPES.register(ID.getPath(), () -> new DataComponentPredicate.ConcreteType<>(CODEC));

  /** Registers the predicate type */
  public static void register(IEventBus modBus) {
    TYPES.register(modBus);
  }

  /** Creates a predicate from a tool stack predicate */
  public static ToolStackItemPredicate ofTool(IJsonPredicate<IToolStackView> predicate) {
    return new ToolStackItemPredicate(predicate);
  }

  /** Creates a predicate from a tool context predicate */
  public static ToolStackItemPredicate ofContext(IJsonPredicate<IToolContext> predicate) {
    return new ToolStackItemPredicate(ToolStackPredicate.context(predicate));
  }

  @Override
  public boolean matches(DataComponentGetter components) {
    // tag check is important to prevent accidentally modifying the data of non-tools; only item stacks are tools
    return components instanceof ItemStack stack && stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.copyFrom(stack));
  }
}
