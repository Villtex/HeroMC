package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.Lazy;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Capabilities of tool stacks: asks the registered providers (modifier capabilities such as tanks, inventories, energy
 * or block providers) in order and returns the first result.
 * <p>26.3: NeoForge item capabilities are registered per item in {@link RegisterCapabilitiesEvent}; tool items call
 * {@link #registerItems(RegisterCapabilitiesEvent, ItemLike...)}, providers name the capabilities they offer with
 * {@link #registerCapability(ItemCapability)}. Providers are built for each lookup, the tool stack is lazy.
 */
public class ToolCapabilityProvider {
  private static final List<BiFunction<ItemStack,Supplier<? extends IToolStackView>,IToolCapabilityProvider>> PROVIDER_CONSTRUCTORS = new ArrayList<>();
  private static final Set<ItemCapability<?,?>> CAPABILITIES = new LinkedHashSet<>();

  private ToolCapabilityProvider() {}

  /** Registers a tool capability provider constructor. It may return null for stacks it does not handle */
  public static void register(BiFunction<ItemStack,Supplier<? extends IToolStackView>,IToolCapabilityProvider> constructor) {
    PROVIDER_CONSTRUCTORS.add(constructor);
  }

  /** Registers a capability that tools may provide through a provider */
  public static void registerCapability(ItemCapability<?,?> capability) {
    CAPABILITIES.add(capability);
  }

  /** Registers all tool capabilities for the given tool items */
  public static void registerItems(RegisterCapabilitiesEvent event, ItemLike... items) {
    for (ItemCapability<?,?> capability : CAPABILITIES) {
      registerItems(event, capability, items);
    }
  }

  private static <T,C> void registerItems(RegisterCapabilitiesEvent event, ItemCapability<T,C> capability, ItemLike... items) {
    event.registerItem(capability, (stack, context) -> getCapability(stack, capability, context), items);
  }

  /** Gets the capability of a tool stack from the providers, null if none provides it */
  @Nullable
  public static <T,C> T getCapability(ItemStack stack, ItemCapability<T,C> capability, C context) {
    Lazy<ToolStack> tool = Lazy.of(() -> ToolStack.from(stack));
    for (BiFunction<ItemStack,Supplier<? extends IToolStackView>,IToolCapabilityProvider> constructor : PROVIDER_CONSTRUCTORS) {
      IToolCapabilityProvider provider = constructor.apply(stack, tool);
      if (provider != null) {
        T result = provider.getCapability(tool.get(), capability, context);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /** Interface to get a capability on a tool */
  @FunctionalInterface
  public interface IToolCapabilityProvider {
    /** Gets a capability on the given tool, or null if this provider does not provide it */
    @Nullable
    <T,C> T getCapability(IToolStackView tool, ItemCapability<T,C> capability, C context);

    /** Called to clear the cache of the provider */
    default void clearCache() {}
  }
}
