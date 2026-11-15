package eplus.network;

import eplus.EnchantingPlus;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server to client: "/eplus useMod|hasParticles" changed a client setting (ConfigurationHandler.set of 1.5.2). */
public record ClientOptionPayload(String option, boolean value) implements CustomPacketPayload {
    public static final String USE_MOD = "useMod";
    public static final String HAS_PARTICLES = "hasParticles";
    public static final Type<ClientOptionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(EnchantingPlus.MOD_ID, "client_option"));
    public static final StreamCodec<ByteBuf, ClientOptionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientOptionPayload::option, ByteBufCodecs.BOOL, ClientOptionPayload::value, ClientOptionPayload::new);

    @Override
    public Type<ClientOptionPayload> type() {
        return TYPE;
    }
}
