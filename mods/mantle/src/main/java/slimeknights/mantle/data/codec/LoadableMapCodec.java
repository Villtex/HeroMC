package slimeknights.mantle.data.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.resources.RegistryOps;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Port (after Mantle 1.21): a {@link RecordLoadable} as a {@link MapCodec}, so loadables can back 26.3 codecs (recipe
 * serializers, ingredient types, conditions). The map is converted to JSON and read by the loadable; registry ops of
 * the caller go into {@link ContextKey#REGISTRY_OPS}.
 * @param loadable  Loadable to read and write
 * @param context   Adds context entries before each decode (for instance the recipe ID)
 */
public class LoadableMapCodec<T> extends MapCodec<T> {
  private final RecordLoadable<T> loadable;
  private final Consumer<TypedMapBuilder> context;

  public LoadableMapCodec(RecordLoadable<T> loadable, Consumer<TypedMapBuilder> context) {
    this.loadable = loadable;
    this.context = context;
  }

  public LoadableMapCodec(RecordLoadable<T> loadable) {
    this(loadable, builder -> {});
  }

  public RecordLoadable<T> loadable() {
    return loadable;
  }

  /** Builds the context for the given ops */
  private TypedMap buildContext(DynamicOps<?> ops) {
    TypedMapBuilder builder = TypedMapBuilder.builder();
    if (ops instanceof RegistryOps<?> registryOps) {
      builder.put(ContextKey.REGISTRY_OPS, registryOps);
    }
    context.accept(builder);
    return builder.build();
  }

  @Override
  public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
    try {
      JsonElement json = ops.convertMap(JsonOps.INSTANCE, ops.createMap(input.entries()));
      return DataResult.success(loadable.deserialize(json.getAsJsonObject(), buildContext(ops)));
    } catch (JsonParseException | IllegalArgumentException | IllegalStateException e) {
      return DataResult.error(() -> "Failed to load " + loadable + ": " + e.getMessage());
    }
  }

  @Override
  public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
    try {
      JsonObject json = new JsonObject();
      loadable.serialize(input, json);
      for (var entry : json.entrySet()) {
        prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
      }
      return prefix;
    } catch (RuntimeException e) {
      return prefix.withErrorsFrom(DataResult.error(() -> "Failed to save " + input + ": " + e.getMessage()));
    }
  }

  @Override
  public <O> Stream<O> keys(DynamicOps<O> ops) {
    return Stream.empty();
  }

  @Override
  public String toString() {
    return "LoadableMapCodec[" + loadable + "]";
  }
}
