package meteors.client;

import meteors.MeteorsMod;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

/**
 * ClientProxy.registerRenderers: the falling meteor, the alien creeper (creeper model, meteorcreeper.png, always charged)
 * and the comet kitty (cat model with cometKitty.png; the space helmet of ModelCometKitty is left out), the thrown
 * summoner as its item; the dust and shield particles and the detector needles.
 */
public final class MeteorsClient {
    private static final Identifier CREEPER = MeteorsMod.id("textures/entity/meteorcreeper.png");
    private static final Identifier KITTY = MeteorsMod.id("textures/entity/comet_kitty.png");
    /** Colours of the shield particles per meteor type (row of particles.png) and for the mixed row 5. */
    private static final float[][] SHIELD = {{0.6F, 0.25F, 0.1F}, {0.3F, 0.9F, 1.0F}, {1.0F, 0.4F, 0.0F}, {0.6F, 0.2F, 0.9F}, {1.0F, 0.6F, 0.8F},
            {1.0F, 1.0F, 1.0F}};

    private MeteorsClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            e.registerEntityRenderer(MeteorsMod.FALLING_METEOR.get(), FallingMeteorRenderer::new);
            e.registerEntityRenderer(MeteorsMod.METEOR_SUMMONER_ENTITY.get(), ThrownItemRenderer::new);
            e.registerEntityRenderer(MeteorsMod.ALIEN_CREEPER.get(), c -> new CreeperRenderer(c) {
                @Override
                public Identifier getTextureLocation(CreeperRenderState state) {
                    return CREEPER;
                }
            });
            e.registerEntityRenderer(MeteorsMod.COMET_KITTY.get(), c -> new CatRenderer(c) {
                @Override
                public Identifier getTextureLocation(CatRenderState state) {
                    return KITTY;
                }
            });
        });
        modBus.addListener(RegisterParticleProvidersEvent.class, e -> {
            e.registerSpriteSet(MeteorsMod.METEOR_DUST.get(), s -> new MeteorDustParticle.Provider(s, 0.09F, 0.09F, 0.09F));
            e.registerSpriteSet(MeteorsMod.FREZA_DUST.get(), s -> new MeteorDustParticle.Provider(s, 0.2F, 1.0F, 0.8F));
            for (int i = 0; i < MeteorsMod.SHIELD_PARTICLES.size(); i++) {
                float[] c = SHIELD[i];
                e.registerSpriteSet(MeteorsMod.SHIELD_PARTICLES.get(i).get(), s -> new MeteorDustParticle.Provider(s, c[0], c[1], c[2]));
            }
        });
        modBus.addListener(RegisterRangeSelectItemModelPropertyEvent.class, e -> e.register(MeteorsMod.id("detector"), DetectorAngle.MAP_CODEC));
    }
}
