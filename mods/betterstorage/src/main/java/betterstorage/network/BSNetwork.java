package betterstorage.network;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * PacketHandler of 1.5.2. Only the teleport effect is left as a packet: openGui is done by the menu types, and
 * backpackHasItems is the synced attachment {@link betterstorage.BS#BACKPACK_STATE}. The client handler is
 * registered in {@link betterstorage.client.BSClient}.
 */
public final class BSNetwork {
    private BSNetwork() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1").playToClient(TeleportPayload.TYPE, TeleportPayload.CODEC));
    }

    /** sendToAllNear(px, py, pz, 512): every player within 512 blocks of the target. */
    public static void sendTeleport(ServerLevel level, double sourceX, double sourceY, double sourceZ, int x, int y, int z) {
        PacketDistributor.sendToPlayersNear(level, null, x + 0.5, y + 0.5, z + 0.5, 512.0,
                new TeleportPayload(sourceX, sourceY, sourceZ, x, y, z));
    }
}
