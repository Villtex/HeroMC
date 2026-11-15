package eplus.network;

import eplus.EnchantingPlus;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client to server: the client's useMod setting (ConfigPacket of 1.5.2), sent on login and when it changes. */
public record UseModPayload(boolean useMod) implements CustomPacketPayload {
    public static final Type<UseModPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(EnchantingPlus.MOD_ID, "use_mod"));
    public static final StreamCodec<ByteBuf, UseModPayload> CODEC = ByteBufCodecs.BOOL.map(UseModPayload::new, UseModPayload::useMod);

    @Override
    public Type<UseModPayload> type() {
        return TYPE;
    }
}
