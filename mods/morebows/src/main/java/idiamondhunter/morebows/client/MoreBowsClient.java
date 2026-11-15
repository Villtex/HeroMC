package idiamondhunter.morebows.client;

import idiamondhunter.morebows.FrostArrow;
import idiamondhunter.morebows.MoreBows;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client side: the frost arrow renderer. */
public final class MoreBowsClient {
    private MoreBowsClient() {
    }

    /**
     * RenderFrostArrow drew the shape of the vanilla arrow with the vanilla texture (/item/arrows.png); the arrow
     * renderer of today does the same, including the shake when it hits.
     */
    public static final class FrostArrowRenderer extends ArrowRenderer<FrostArrow, ArrowRenderState> {
        public FrostArrowRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        protected Identifier getTextureLocation(ArrowRenderState state) {
            return TippableArrowRenderer.NORMAL_ARROW_LOCATION;
        }

        @Override
        public ArrowRenderState createRenderState() {
            return new ArrowRenderState();
        }
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MoreBows.FROST_ARROW.get(), FrostArrowRenderer::new);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(MoreBowsClient::renderers);
    }
}
