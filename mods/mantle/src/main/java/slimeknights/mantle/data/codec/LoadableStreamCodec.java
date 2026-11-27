package slimeknights.mantle.data.codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Consumer;

/**
 * Port (after Mantle 1.21): a {@link Loadable} as a {@link StreamCodec}.
 * @param loadable  Loadable to read and write
 * @param context   Adds context entries before each decode
 */
public record LoadableStreamCodec<T>(Loadable<T> loadable, Consumer<TypedMapBuilder> context) implements StreamCodec<RegistryFriendlyByteBuf,T> {
  public LoadableStreamCodec(Loadable<T> loadable) {
    this(loadable, builder -> {});
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer) {
    TypedMapBuilder builder = TypedMapBuilder.builder();
    context.accept(builder);
    return loadable.decode(buffer, builder.build());
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T value) {
    loadable.encode(buffer, value);
  }
}
