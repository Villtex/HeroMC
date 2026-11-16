package asgardshield.network;

import asgardshield.AsgardShield;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server to client: the guard state of the player (Guard Gauge, broken guard, Vanguard Count, HUD flags). Takes the
 * place of the "ASPacket" channel of 1.5.2, which went the other way (the client ran the gauge and told the server
 * when the guard broke).
 */
public record GuardStatePayload(int gauge, boolean broken, int vanguardCount, byte flags) implements CustomPacketPayload {
    public static final Type<GuardStatePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(AsgardShield.MOD_ID, "guard_state"));
    public static final StreamCodec<ByteBuf, GuardStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, GuardStatePayload::gauge,
            ByteBufCodecs.BOOL, GuardStatePayload::broken,
            ByteBufCodecs.VAR_INT, GuardStatePayload::vanguardCount,
            ByteBufCodecs.BYTE, GuardStatePayload::flags,
            GuardStatePayload::new);

    @Override
    public Type<GuardStatePayload> type() {
        return TYPE;
    }
}
