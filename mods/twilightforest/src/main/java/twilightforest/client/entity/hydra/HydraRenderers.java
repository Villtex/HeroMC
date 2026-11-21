package twilightforest.client.entity.hydra;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import twilightforest.TFHydra;
import twilightforest.TwilightForest;

/**
 * The renderers and model layers of the hydra (RenderTFHydra, RenderTFHydraHead with the necks, RenderTFHydraMortar)
 * and the large flame particle of its breath (TFClientProxy.spawnParticle "largeflame").
 */
public final class HydraRenderers {
    static final ModelLayerLocation HYDRA = layer("hydra");
    static final ModelLayerLocation HYDRA_HEAD = layer("hydra_head");
    static final ModelLayerLocation HYDRA_NECK = layer("hydra_neck");
    static final ModelLayerLocation HYDRA_MORTAR = layer("hydra_mortar");

    private HydraRenderers() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(TwilightForest.id(name), "main");
    }

    public static void register(IEventBus modBus) {
        // TileEntityTFTrophyRenderer.renderHydraHead: a quarter size, the head turned by the rotation once more, mouth open on walls
        twilightforest.client.TrophyRenderer.HYDRA_HEAD = context -> {
            HydraModels.Head model = new HydraModels.Head(context.bakeLayer(HYDRA_HEAD));
            HydraModels.PartState state = new HydraModels.PartState();
            return (pose, collector, light, rotation, onGround) -> {
                pose.scale(0.25F, 0.25F, 0.25F);
                pose.scale(1.0F, -1.0F, -1.0F);
                pose.rotateDegrees(com.mojang.math.Axis.YP, rotation);
                pose.rotateDegrees(com.mojang.math.Axis.YP, 180.0F);
                pose.translate(0.0F, onGround ? 1.0F : 0.0F, 1.5F);
                state.visible = true;
                state.yaw = rotation;
                state.pitch = 0.0F;
                state.mouthOpen = onGround ? 0.0F : 0.25F;
                state.lightCoords = light;
                collector.submitModel(model, state, pose, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(HydraRenderer.TEXTURE), light,
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, -1);
            };
        };
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, event -> {
            event.registerLayerDefinition(HYDRA, HydraModels.Body::createBodyLayer);
            event.registerLayerDefinition(HYDRA_HEAD, HydraModels.Head::createBodyLayer);
            event.registerLayerDefinition(HYDRA_NECK, HydraModels.Neck::createBodyLayer);
            event.registerLayerDefinition(HYDRA_MORTAR, HydraModels.Mortar::createBodyLayer);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerEntityRenderer(TFHydra.HYDRA.get(), HydraRenderer::new);
            event.registerEntityRenderer(TFHydra.HYDRA_HEAD.get(), HydraHeadRenderer::new);
            event.registerEntityRenderer(TFHydra.HYDRA_MORTAR.get(), HydraMortarRenderer::new);
        });
        modBus.addListener(RegisterParticleProvidersEvent.class,
                event -> event.registerSpriteSet(TFHydra.LARGE_FLAME.get(), LargeFlameParticle.Provider::new));
    }
}
