package offlawn.client;

import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import offlawn.OffLawn;

import java.util.List;

/** Client side: the lawn takes the grass colour of the biome (LawnBlock.colorMultiplier averaged 3x3 like grass today). */
public final class OffLawnClient {
    private OffLawnClient() {
    }

    private static void colors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.grass()), OffLawn.LAWN.get());
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(OffLawnClient::colors);
    }
}
