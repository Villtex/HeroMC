package slimeknights.mantle.registration.deferred;

import slimeknights.mantle.registration.RegistrationContext;
import slimeknights.mantle.registration.RegistryObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.registration.RegistryObject;

import java.util.function.Supplier;

/** Deferred register instance that synchronizes register calls */
@RequiredArgsConstructor(staticName = "create")
public class SynchronizedDeferredRegister<T> {
  private final DeferredRegister<T> internal;

  /** Creates a new instance for the given resource key */
  public static <T> SynchronizedDeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modid) {
    return create(DeferredRegister.create(key, modid));
  }

  /** Creates a new instance for the given forge registry */
  public static <B> SynchronizedDeferredRegister<B> create(Registry<B> registry, String modid) {
    return create(DeferredRegister.create(registry, modid));
  }

  /** Registers the given object, synchronized over the internal register */
  public <I extends T> RegistryObject<I> register(final String name, final Supplier<? extends I> sup) {
    return register(name, sup, false);
  }

  /**
   * Registers the given object, synchronized over the internal register.
   * Port: the supplier runs inside {@link RegistrationContext}, so item and block properties get the registry key
   * 26.3 needs ({@code setId}) without the callers setting it.
   * @param blockItem  If true, the entry is the item of a block (block description ID, as in 1.20.1)
   */
  public <I extends T> RegistryObject<I> register(final String name, final Supplier<? extends I> sup, boolean blockItem) {
    synchronized (internal) {
      ResourceKey<? extends Registry<T>> registry = internal.getRegistryKey();
      return RegistryObject.of(internal.<I>register(name, id -> RegistrationContext.create(ResourceKey.create(registry, id), blockItem, sup::get)));
    }
  }

  /**
   * Registers the internal register with the event bus
   */
  public void register(IEventBus bus) {
    internal.register(bus);
  }
}
