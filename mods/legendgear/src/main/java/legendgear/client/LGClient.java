package legendgear.client;

import legendgear.LGEntities;
import legendgear.LGParticles;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * ClientProxy.registerRenderers: the thrown medallions, the boomerang, bombs and the hook as their item; the effect
 * entities draw nothing. The particles of the proxy are {@link LGParticle}.
 */
public final class LGClient {
    private LGClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterParticleProvidersEvent.class, e -> {
            e.registerSpriteSet(LGParticles.RUNE.get(), LGParticle.Provider::new);
            e.registerSpriteSet(LGParticles.SCRAMBLE.get(), LGParticle.Provider::new);
            e.registerSpriteSet(LGParticles.FLARE.get(), LGParticle.Provider::new);
            e.registerSpriteSet(LGParticles.SPARKLE.get(), LGParticle.Provider::new);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            e.registerEntityRenderer(LGEntities.MEDALLION.get(), ThrownItemRenderer::new);
            e.registerEntityRenderer(LGEntities.MAGIC_BOOMERANG.get(), ThrownItemRenderer::new);
            e.registerEntityRenderer(LGEntities.HOOKSHOT_HOOK.get(), ThrownItemRenderer::new);
            e.registerEntityRenderer(LGEntities.BOMB.get(), ThrownItemRenderer::new);
            e.registerEntityRenderer(LGEntities.QUAKE.get(), NoopRenderer::new);
            e.registerEntityRenderer(LGEntities.ARROW_STORM.get(), NoopRenderer::new);
            e.registerEntityRenderer(LGEntities.FIREBLAST.get(), NoopRenderer::new);
            e.registerEntityRenderer(LGEntities.BOMB_BLAST.get(), NoopRenderer::new);
            e.registerEntityRenderer(LGEntities.WHIRLWIND.get(), NoopRenderer::new);
            e.registerEntityRenderer(LGEntities.GRIND_STAR.get(), NoopRenderer::new);
            e.registerEntityRenderer(LGEntities.FALLING_STAR.get(), FallingStarRenderer::new);
            e.registerEntityRenderer(LGEntities.ENDER_BOMB.get(), NoopRenderer::new);
        });
    }
}
