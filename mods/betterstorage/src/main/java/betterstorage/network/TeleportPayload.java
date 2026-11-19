package betterstorage.network;

import betterstorage.BetterStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** PacketHandler.backpackTeleport: the trail of portal particles from where an ender backpack was to where it landed. */
public record TeleportPayload(double sourceX, double sourceY, double sourceZ, int x, int y, int z) implements CustomPacketPayload {
    public static final Type<TeleportPayload> TYPE = new Type<>(BetterStorage.id("backpack_teleport"));
    public static final StreamCodec<ByteBuf, TeleportPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, TeleportPayload::sourceX, ByteBufCodecs.DOUBLE, TeleportPayload::sourceY,
            ByteBufCodecs.DOUBLE, TeleportPayload::sourceZ, ByteBufCodecs.VAR_INT, TeleportPayload::x,
            ByteBufCodecs.VAR_INT, TeleportPayload::y, ByteBufCodecs.VAR_INT, TeleportPayload::z, TeleportPayload::new);

    @Override
    public Type<TeleportPayload> type() {
        return TYPE;
    }
}
