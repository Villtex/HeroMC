package tconstruct.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tconstruct.TConstruct;
import tconstruct.menu.ToolStationMenu;

/** The packet of 1.5.2 that the GUI sent: 1 = the name typed in the tool station (the stencil table uses menu buttons). */
public final class TNetwork {
    private TNetwork() {
    }

    /** The tool's name. */
    public record ToolNamePayload(String name) implements CustomPacketPayload {
        public static final Type<ToolNamePayload> TYPE = new Type<>(TConstruct.id("tool_name"));
        public static final StreamCodec<ByteBuf, ToolNamePayload> CODEC = ByteBufCodecs.stringUtf8(40).map(ToolNamePayload::new, ToolNamePayload::name);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1")
                .playToServer(ToolNamePayload.TYPE, ToolNamePayload.CODEC, TNetwork::toolName));
    }

    private static void toolName(ToolNamePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof ToolStationMenu menu && menu.stillValid(player)) {
            menu.setToolName(payload.name());
        }
    }
}
