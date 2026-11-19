package betterstorage.client;

import betterstorage.BSUtil;
import betterstorage.network.TeleportPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** ClientProxy: renderers, screens, colours and the client half of the packets. */
public final class BSClient {
    private BSClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(TeleportPayload.TYPE, BSClient::handleTeleport));
    }

    /** handleBackpackTeleport: 128 portal particles along the way. */
    private static void handleTeleport(TeleportPayload p, IPayloadContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        int amount = 128;
        for (int i = 0; i < amount; i++) {
            double a = (double) i / (amount - 1);
            double vX = BSUtil.getDouble(-0.3, 0.3);
            double vY = BSUtil.getDouble(-0.3, 0.3);
            double vZ = BSUtil.getDouble(-0.3, 0.3);
            double pX = p.sourceX() + (p.x() - p.sourceX()) * a + BSUtil.getDouble(0.3, 0.7);
            double pY = p.sourceY() + (p.y() - p.sourceY()) * a + BSUtil.getDouble(-0.5, 0.0) + a / 2.0;
            double pZ = p.sourceZ() + (p.z() - p.sourceZ()) * a + BSUtil.getDouble(0.3, 0.7);
            level.addParticle(ParticleTypes.PORTAL, pX, pY, pZ, vX, vY, vZ);
        }
    }
}
