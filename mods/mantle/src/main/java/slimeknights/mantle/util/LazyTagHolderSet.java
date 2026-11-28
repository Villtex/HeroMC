package slimeknights.mantle.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Port: holder set of a tag that looks the tag up on each use. In 1.20.1 {@code Ingredient.of(TagKey)} was lazy; in
 * 26.3 a built-in registry throws for tags before they are bound, so ingredients in static fields (defaults of
 * recipe loaders) use {@link #ingredient(TagKey)} instead. Encodes as the tag. Empty while tags are not bound.
 */
public final class LazyTagHolderSet<T> implements HolderSet<T> {
  private final Registry<T> registry;
  private final TagKey<T> tag;

  public LazyTagHolderSet(Registry<T> registry, TagKey<T> tag) {
    this.registry = registry;
    this.tag = tag;
  }

  /** Lazy item ingredient for the tag */
  public static Ingredient ingredient(TagKey<Item> tag) {
    return Ingredient.of(new LazyTagHolderSet<>(BuiltInRegistries.ITEM, tag));
  }

  /** Bound tag, or empty if tags are not bound yet */
  private HolderSet<T> delegate() {
    try {
      return registry.get(tag).<HolderSet<T>>map(set -> set).orElse(HolderSet.empty());
    } catch (IllegalStateException e) {
      return HolderSet.empty();
    }
  }

  @Override
  public Stream<Holder<T>> stream() {
    return delegate().stream();
  }

  @Override
  public int size() {
    return delegate().size();
  }

  @Override
  public boolean isBound() {
    return delegate().isBound();
  }

  @Override
  public Either<TagKey<T>, List<Holder<T>>> unwrap() {
    return Either.left(tag);
  }

  @Override
  public Optional<Holder<T>> getRandomElement(RandomSource random) {
    return delegate().getRandomElement(random);
  }

  @Override
  public Holder<T> get(int index) {
    return delegate().get(index);
  }

  @Override
  public boolean contains(Holder<T> holder) {
    return holder.is(tag);
  }

  @Override
  public boolean canSerializeIn(HolderOwner<T> owner) {
    return true;
  }

  @Override
  public Optional<TagKey<T>> unwrapKey() {
    return Optional.of(tag);
  }

  @Override
  public Iterator<Holder<T>> iterator() {
    return stream().iterator();
  }

  @Override
  public boolean equals(Object other) {
    return this == other || other instanceof LazyTagHolderSet<?> set && set.tag.equals(tag);
  }

  @Override
  public int hashCode() {
    return tag.hashCode();
  }

  @Override
  public String toString() {
    return "LazyTag[" + tag.location() + "]";
  }
}
