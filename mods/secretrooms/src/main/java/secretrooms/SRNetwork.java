package secretrooms;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** PacketSRM1ToggleShow (server -> client, /srm-show) and PacketSRM2Key (client -> server, the one-way facing key). */
public final class SRNetwork {
    private SRNetwork() {
    }

    public record ToggleShow() implements CustomPacketPayload {
        public static final Type<ToggleShow> TYPE = new Type<>(SecretRooms.id("toggle_show"));
        public static final StreamCodec<ByteBuf, ToggleShow> CODEC = StreamCodec.unit(new ToggleShow());

        @Override
        public Type<ToggleShow> type() {
            return TYPE;
        }
    }

    public record Key() implements CustomPacketPayload {
        public static final Type<Key> TYPE = new Type<>(SecretRooms.id("key"));
        public static final StreamCodec<ByteBuf, Key> CODEC = StreamCodec.unit(new Key());

        @Override
        public Type<Key> type() {
            return TYPE;
        }
    }

    /** Set by the client side: what to do with a ToggleShow. */
    public static Runnable clientToggle = () -> {
    };

    static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(ToggleShow.TYPE, ToggleShow.CODEC, (payload, ctx) -> clientToggle.run());
        registrar.playToServer(Key.TYPE, Key.CODEC, SRNetwork::key);
    }

    private static void key(Key payload, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            SecretRooms.toggleFacing(player);
        }
    }
}
