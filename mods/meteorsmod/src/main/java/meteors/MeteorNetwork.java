package meteors;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The server-to-client packets of ClientHandler (MetGhostAdd/Rem, MetNewCrash, MetNewTime) as one sync payload: the
 * announced meteors, the last crash and the meteor that falls soonest. The detectors point at them ({@link #ghosts} etc.
 * are the client's copy). MetSettings and MetChunk (shield chunks) were only used by the client's tooltips and are
 * left out.
 */
public final class MeteorNetwork {
    /** Client copy: x/z of the announced meteors. */
    public static volatile List<BlockPos> ghosts = List.of();
    public static volatile Optional<BlockPos> lastCrash = Optional.empty();
    public static volatile Optional<BlockPos> nearestTime = Optional.empty();
    public static volatile int nearestTimeTicks;
    public static volatile long syncedAt;

    private MeteorNetwork() {
    }

    public record Sync(List<BlockPos> ghosts, Optional<BlockPos> lastCrash, Optional<BlockPos> nearestTime, int nearestTimeTicks)
            implements CustomPacketPayload {
        public static final Type<Sync> TYPE = new Type<>(MeteorsMod.id("sync"));
        public static final StreamCodec<ByteBuf, Sync> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), Sync::ghosts,
                ByteBufCodecs.optional(BlockPos.STREAM_CODEC), Sync::lastCrash,
                ByteBufCodecs.optional(BlockPos.STREAM_CODEC), Sync::nearestTime,
                ByteBufCodecs.VAR_INT, Sync::nearestTimeTicks, Sync::new);

        @Override
        public Type<Sync> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Sync.TYPE, Sync.CODEC, MeteorNetwork::handle);
    }

    private static void handle(Sync payload, IPayloadContext ctx) {
        ghosts = payload.ghosts();
        lastCrash = payload.lastCrash();
        nearestTime = payload.nearestTime();
        nearestTimeTicks = payload.nearestTimeTicks();
        syncedAt = System.currentTimeMillis();
    }

    private static Sync build(MinecraftServer server) {
        MeteorData data = MeteorData.get(server);
        List<BlockPos> list = new ArrayList<>();
        for (MeteorData.Ghost ghost : data.ghosts()) {
            list.add(new BlockPos(ghost.x, 0, ghost.z));
        }
        MeteorData.Ghost time = data.nearestTimeMeteor();
        return new Sync(list, Optional.ofNullable(data.lastCrash()), time == null ? Optional.empty() : Optional.of(new BlockPos(time.x, 0, time.z)),
                time == null ? 0 : time.remainingTicks());
    }

    public static void sendToAll(MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(build(server));
    }

    public static void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, build(player.level().getServer()));
    }
}
