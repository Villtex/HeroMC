package slimeknights.mantle.registration;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.registration.RegistryObject;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Port: stand-in for Forge's {@code RegistryObject<T>} (one type parameter), on top of NeoForge's
 * {@link DeferredHolder DeferredHolder&lt;R,T&gt;}. Keeps the Mantle 1.20.1 API (registers, item and block objects)
 * that Tinkers' Construct 3.12.1 calls, without spelling out the registry type everywhere.
 * @param <T>  Registered value type
 */
public final class RegistryObject<T> implements Supplier<T> {
  private final DeferredHolder<?, ? extends T> holder;

  private RegistryObject(DeferredHolder<?, ? extends T> holder) {
    this.holder = holder;
  }

  /** Wraps a deferred holder */
  public static <R, T extends R> RegistryObject<T> of(DeferredHolder<R, T> holder) {
    return new RegistryObject<>(holder);
  }

  /** Same as Forge's {@code RegistryObject.create(name, registry)}: a lazy reference to an entry of the registry */
  public static <R, T extends R> RegistryObject<T> create(Identifier name, ResourceKey<? extends Registry<R>> registry) {
    return of(DeferredHolder.<R,T>create(registry, name));
  }

  /** Registered value, throws if not yet registered */
  @Override
  public T get() {
    return holder.get();
  }

  /** Registry name */
  public Identifier getId() {
    return holder.getId();
  }

  /** Registry key */
  @SuppressWarnings("unchecked")
  public <R> ResourceKey<R> getKey() {
    return (ResourceKey<R>) Objects.requireNonNull(holder.getKey());
  }

  /** True once the value is registered */
  public boolean isPresent() {
    return holder.isBound();
  }

  /** Value if registered */
  public Optional<T> asOptional() {
    return isPresent() ? Optional.of(get()) : Optional.empty();
  }

  /** The NeoForge holder (also a {@link Holder} of the registry type) */
  @SuppressWarnings("unchecked")
  public <R> DeferredHolder<R, ? extends T> holder() {
    return (DeferredHolder<R, ? extends T>) holder;
  }

  /** Same as Forge's {@code getHolder()} */
  public <R> Optional<Holder<R>> getHolder() {
    return Optional.of(this.<R>holder());
  }

  @Override
  public boolean equals(Object other) {
    return this == other || other instanceof RegistryObject<?> object && object.holder.equals(holder);
  }

  @Override
  public int hashCode() {
    return holder.hashCode();
  }

  @Override
  public String toString() {
    return "RegistryObject[" + getId() + "]";
  }
}
