package projectzulu.client;

import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import projectzulu.ZuluPlants;
import projectzulu.client.plant.UniversalFlowerPotRenderer;

import java.util.List;

/** Teil 11 – client registration of batch Plants. */
public final class PlantsClient {
    private PlantsClient() {
    }

    /** BlockPalmTreeLeaves.colorMultiplier: the biome foliage colour. */
    private static void colors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.foliage()), ZuluPlants.PALM_LEAVES.get());
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        // RenderCreeperBlossomPrimed drew the flower like primed TNT: the vanilla TNT renderer with the entity's block
        event.registerEntityRenderer(ZuluPlants.CREEPER_BLOSSOM_PRIMED.get(), TntRenderer::new);
        event.registerBlockEntityRenderer(ZuluPlants.UNIVERSAL_FLOWER_POT_ENTITY.get(), UniversalFlowerPotRenderer::new);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(PlantsClient::colors);
        modBus.addListener(PlantsClient::renderers);
    }
}
