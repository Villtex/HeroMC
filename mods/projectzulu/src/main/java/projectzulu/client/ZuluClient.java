package projectzulu.client;

import net.neoforged.bus.api.IEventBus;
import projectzulu.client.entity.ZuluEntityRenderers;

/** Client-only registration of Project Zulu (mob renderers and model layers). */
public final class ZuluClient {
    private ZuluClient() {
    }

    public static void register(IEventBus modBus) {
        ZuluEntityRenderers.register(modBus);
        projectzulu.client.entity.BigAnimalRenderers.register(modBus);
        projectzulu.client.entity.MonsterRenderers.register(modBus);
        projectzulu.client.entity.HorseRenderers.register(modBus);
        PlantsClient.register(modBus);
        GearClient.register(modBus);
        WorldClient.register(modBus);
        PotionsClient.register(modBus);
    }
}
