package slimeknights.tconstruct.library.utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Stand-in for Forge's {@code slimeknights.tconstruct.library.utils.LazyOptional}, which NeoForge 26.3 no longer has
 * (capabilities return the value or null, entity data is an attachment). Tinkers 3.12.1 passes these around in
 * modifier code (entity data, tool capabilities), so the port keeps the type: a lazily resolved, invalidatable value.
 */
public final class LazyOptional<T> {
  private static final LazyOptional<Void> EMPTY = new LazyOptional<>(null);

  @Nullable
  private Supplier<? extends T> supplier;
  @Nullable
  private T resolved;
  private boolean isValid = true;
  private List<Consumer<LazyOptional<T>>> listeners;

  private LazyOptional(@Nullable Supplier<? extends T> supplier) {
    this.supplier = supplier;
  }

  /** Creates a lazy optional from the given supplier; the supplier must not return null */
  public static <T> LazyOptional<T> of(@Nullable Supplier<T> supplier) {
    return supplier == null ? empty() : new LazyOptional<>(supplier);
  }

  /** Creates a lazy optional of a value that may be null */
  public static <T> LazyOptional<T> ofNullable(@Nullable T value) {
    return value == null ? empty() : new LazyOptional<>(() -> value);
  }

  /** Empty instance */
  @SuppressWarnings("unchecked")
  public static <T> LazyOptional<T> empty() {
    return (LazyOptional<T>) EMPTY;
  }

  /** Casts the contents, for capability style generics */
  @SuppressWarnings("unchecked")
  public <X> LazyOptional<X> cast() {
    return (LazyOptional<X>) this;
  }

  @Nullable
  private T getValue() {
    if (!isValid || supplier == null) {
      return null;
    }
    if (resolved == null) {
      resolved = Objects.requireNonNull(supplier.get(), "Supplier of LazyOptional returned null");
    }
    return resolved;
  }

  /** If true, the value is present */
  public boolean isPresent() {
    return supplier != null && isValid;
  }

  /** Runs the consumer if the value is present */
  public void ifPresent(Consumer<? super T> consumer) {
    T value = getValue();
    if (value != null) {
      consumer.accept(value);
    }
  }

  /** Maps the value lazily */
  public <U> LazyOptional<U> lazyMap(Function<? super T, ? extends U> mapper) {
    return isPresent() ? of(() -> mapper.apply(getValue())) : empty();
  }

  /** Maps the value right away */
  public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
    return resolve().map(mapper);
  }

  /** Filters the value */
  public Optional<T> filter(Predicate<? super T> predicate) {
    return resolve().filter(predicate);
  }

  /** Resolves this to a regular optional */
  public Optional<T> resolve() {
    return Optional.ofNullable(getValue());
  }

  /** Gets the value or the fallback */
  public T orElse(T other) {
    T value = getValue();
    return value != null ? value : other;
  }

  /** Gets the value or the fallback */
  public T orElseGet(Supplier<? extends T> other) {
    T value = getValue();
    return value != null ? value : other.get();
  }

  /** Gets the value or throws */
  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    T value = getValue();
    if (value != null) {
      return value;
    }
    throw exceptionSupplier.get();
  }

  /** Gets the value or throws */
  public T orElseThrow() {
    return resolve().orElseThrow();
  }

  /** Adds a listener for invalidation */
  public void addListener(Consumer<LazyOptional<T>> listener) {
    if (isPresent()) {
      if (listeners == null) {
        listeners = new ArrayList<>();
      }
      listeners.add(listener);
    } else {
      listener.accept(this);
    }
  }

  /** Invalidates this optional, notifying listeners */
  public void invalidate() {
    if (isValid) {
      isValid = false;
      if (listeners != null) {
        for (Consumer<LazyOptional<T>> listener : listeners) {
          listener.accept(this);
        }
        listeners = null;
      }
    }
  }
}
