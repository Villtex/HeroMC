package extrabiomes.client;

import extrabiomes.EbxlBlocks;
import extrabiomes.Extrabiomes;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

/** Client side: leaf colours and the scarecrow renderer. */
public final class EbxlClient {
    public static final ModelLayerLocation SCARECROW_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Extrabiomes.MOD_ID, "scarecrow"), "main");
    /** ColorizerFoliage.getFoliageColorPine of 1.5.2. */
    public static final int FIR_COLOR = 0x619961;
    /** ColorizerFoliage.getFoliageColor(0.9, 0.1): the dry, yellow end of the foliage colour map. */
    public static final int ACACIA_COLOR = 0xAEA42A;

    private EbxlClient() {
    }

    private static void colors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.constant(FIR_COLOR)), EbxlBlocks.FIR_LEAVES.get());
        event.register(List.of(BlockTintSources.constant(ACACIA_COLOR)), EbxlBlocks.ACACIA_LEAVES.get());
        // redwood leaves and leaf piles take the colour of the biome
        event.register(List.of(BlockTintSources.foliage()), EbxlBlocks.REDWOOD_LEAVES.get(), EbxlBlocks.LEAF_PILE.get());
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SCARECROW_LAYER, ScarecrowModel::createBodyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EbxlBlocks.SCARECROW.get(), ScarecrowRenderer::new);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EbxlClient::colors);
        modBus.addListener(EbxlClient::layers);
        modBus.addListener(EbxlClient::renderers);
    }
}
