package bibliocraft.client;

import bibliocraft.BiblioCraft;
import bibliocraft.bookcase.client.BookcasesClient;
import bibliocraft.decor.client.DecorClient;
import bibliocraft.display.client.DisplaysClient;
import bibliocraft.printing.client.PrintingClient;
import bibliocraft.shelf.client.ShelvesClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

/** ClientProxy: block entity renderers, item renderers and screens of every group. */
public final class BCClient {
    private BCClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterSpecialModelRendererEvent.class,
                e -> e.register(BiblioCraft.id("block"), BlockItemRenderers.Unbaked.MAP_CODEC));
        BookcasesClient.init(modBus);
        ShelvesClient.init(modBus);
        DisplaysClient.init(modBus);
        DecorClient.init(modBus);
        PrintingClient.init(modBus);
    }
}
