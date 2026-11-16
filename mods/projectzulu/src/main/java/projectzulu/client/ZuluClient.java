package projectzulu.client;

import net.neoforged.bus.api.IEventBus;
import projectzulu.client.entity.ZuluEntityRenderers;

/** Client-only registration of Project Zulu (mob renderers and model layers). */
public final class ZuluClient {
    private ZuluClient() {
    }

    public static void register(IEventBus modBus) {
        ZuluEntityRenderers.register(modBus);
    }
}
