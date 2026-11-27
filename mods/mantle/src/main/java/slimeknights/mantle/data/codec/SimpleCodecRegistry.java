package slimeknights.mantle.data.codec;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

/**
 * Simple registry for registering map codecs for a dispatch field, for handling JSON or NBT parsing with a resource location type field.
 * @param <T>  Type of the serializable object
 * @see GenericLoaderRegistry GenericLoaderRegistry for an alternative that is limited to JSON but also supports network friendly byte buffers
 */
@SuppressWarnings("unused")  // API
@Accessors(fluent = true)
public class SimpleCodecRegistry<T> {
  /** Map of all registered codecs */
  private final BiMap<Identifier, MapCodec<? extends T>> codecs = HashBiMap.create();
  /** Codec for parsing the type field */
  @Getter
  private final Codec<MapCodec<? extends T>> typeCodec;
  /** Codec for parsing a condition */
  @Getter
  private final Codec<T> codec;

  /**
   * Creates a new registry
   * @param name         Name of the result object.
   * @param codecGetter  Getter to fetch a codec from the given object.
   */
  public SimpleCodecRegistry(String name, Function<? super T, MapCodec<? extends T>> codecGetter) {
    typeCodec = Identifier.CODEC.flatXmap(id -> {
      MapCodec<? extends T> codec = codecs.get(id);
      return codec != null ? DataResult.success(codec) : DataResult.error(() -> "Unknown " + name + ": " + id);
    }, codec -> {
      Identifier key = codecs.inverse().get(codec);
      return key != null ? DataResult.success(key) : DataResult.error(() -> name + " codec is not registered: " + codec);
    });
    codec = typeCodec.dispatch(codecGetter, Function.identity());
  }

  /** Registers the map codec with the given ID. */
  public synchronized MapCodec<? extends T> register(Identifier id, MapCodec<? extends T> codec) {
    if (codecs.putIfAbsent(id, codec) != null) {
      throw new IllegalArgumentException("Duplicate registration " + id);
    }
    return codec;
  }

  /** Gets the codec registered under the given name. */
  @Nullable
  public MapCodec<? extends T> getCodec(Identifier id) {
    return codecs.get(id);
  }

  /** Gets a collection of all keys with registered codecs. */
  public Collection<Identifier> getKeys() {
    return codecs.keySet();
  }

  /** Gets a collection of all registered codecs. */
  public Collection<MapCodec<? extends T>> getAllCodecs() {
    return codecs.values();
  }
}
