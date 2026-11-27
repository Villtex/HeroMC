package hexxit.worldgen152;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Which world type the loaded world has: Hexxit Legacy (the overworld runs the 1.5.2 generator,
 * {@link OldChunkGenerator}) or another one (Hexxit Integrated, vanilla presets). Mods with content that belongs to
 * only one of them ask here (Teil 27: Tinkers' Construct 1.4.3 only in Legacy, 3.12.1 only in Integrated); data
 * uses the load condition {@link WorldTypeCondition} instead.
 *
 * <p>Server side the answer comes from the overworld's generator. The server tells every client on login
 * ({@link Payload}); until then, and after leaving a world, {@link #clientLegacy()} is {@code true} (Hexxit Legacy is
 * the pack's world type). Client code that caches something per world type (creative tabs, JEI) registers a
 * listener with {@link #onClientChange}.
 */
public final class HexxitWorldType {
    private HexxitWorldType() {
    }

    private static volatile boolean clientLegacy = true;
    private static volatile MinecraftServer cachedServer;
    private static volatile boolean cachedLegacy;
    private static final List<Consumer<Boolean>> CLIENT_LISTENERS = new CopyOnWriteArrayList<>();

    /** Hexxit Legacy on this server: the overworld runs the 1.5.2 generator. */
    public static boolean isLegacy(MinecraftServer server) {
        if (server != cachedServer) {
            cachedLegacy = server.overworld().getChunkSource().getGenerator() instanceof OldChunkGenerator;
            cachedServer = server;
        }
        return cachedLegacy;
    }

    /** Hexxit Legacy for the world this level belongs to (client levels: what the server sent). */
    public static boolean isLegacy(Level level) {
        MinecraftServer server = level.getServer();
        return level.isClientSide() || server == null ? clientLegacy : isLegacy(server);
    }

    /** Client side: the world type of the world the player is in (true before the server said anything). */
    public static boolean clientLegacy() {
        return clientLegacy;
    }

    /** Client side: called with the new value whenever the world type known to the client changes. */
    public static void onClientChange(Consumer<Boolean> listener) {
        CLIENT_LISTENERS.add(listener);
    }

    /** Internal (payload handler, client logout): sets what the client knows. */
    public static void setClient(boolean legacy) {
        if (legacy != clientLegacy) {
            clientLegacy = legacy;
            CLIENT_LISTENERS.forEach(l -> l.accept(legacy));
        }
    }

    /** Server to client on login: the world type of the server's world. */
    public record Payload(boolean legacy) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WorldGen152.MOD_ID, "world_type"));
        public static final StreamCodec<ByteBuf, Payload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Payload::legacy, Payload::new);

        @Override
        public Type<Payload> type() {
            return TYPE;
        }
    }

    static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1").optional()
                .playToClient(Payload.TYPE, Payload.CODEC, (payload, context) -> setClient(payload.legacy())));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new Payload(isLegacy(player.level().getServer())));
            }
        });
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> cachedServer = null);
    }
}
