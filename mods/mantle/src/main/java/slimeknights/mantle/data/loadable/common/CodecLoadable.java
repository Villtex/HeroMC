package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import slimeknights.mantle.data.loadable.field.ContextKey;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Implementation of a loadable using a codec. Note this will be inefficient when reading from and writing to the network
 *
 * <p>Port: JSON is read with registry ops ({@link ContextKey#registryOps(TypedMap)}), as many 26.3 codecs need registry
 * access. On a {@link RegistryFriendlyByteBuf} the buffer is written through the registry aware NBT ops.
 */
public record CodecLoadable<T>(DynamicOps<Tag> ops, Codec<T> codec) implements Loadable<T> {
  public CodecLoadable(Codec<T> codec) {
    this(NbtOps.INSTANCE, codec);
  }

  @Override
  public T convert(JsonElement element, String key, TypedMap context) {
    return codec.parse(ContextKey.registryOps(context), element).getOrThrow(message -> new JsonSyntaxException("Failed to parse '" + key + "': " + message));
  }

  @Override
  public JsonElement serialize(T object) {
    return codec.encodeStart(ContextKey.registryOps(TypedMap.EMPTY), object).getOrThrow();
  }

  @Override
  public T decode(FriendlyByteBuf buffer, TypedMap context) {
    if (buffer instanceof RegistryFriendlyByteBuf registryBuffer) {
      return ByteBufCodecs.fromCodecWithRegistries(codec).decode(registryBuffer);
    }
    return ByteBufCodecs.fromCodec(codec, NbtAccounter::unlimitedHeap).decode(buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, T object) {
    if (buffer instanceof RegistryFriendlyByteBuf registryBuffer) {
      ByteBufCodecs.fromCodecWithRegistries(codec).encode(registryBuffer, object);
    } else {
      ByteBufCodecs.fromCodec(codec, NbtAccounter::unlimitedHeap).encode(buffer, object);
    }
  }
}
