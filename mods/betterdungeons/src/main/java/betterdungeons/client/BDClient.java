package betterdungeons.client;

import betterdungeons.BDEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** ClientProxy.registerRenderInformation for the projectiles (the mob renderers come with the mobs). */
public final class BDClient {
    private BDClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerEntityRenderer(BDEntities.MAGIC_ARROW.get(), MagicArrowRenderer::new);
            event.registerEntityRenderer(BDEntities.PROJECTILE.get(), BallRenderer::new);
            event.registerEntityRenderer(BDEntities.BLOCK_PROJECTILE.get(), BlockProjectileRenderer::new);
        });
    }
}
