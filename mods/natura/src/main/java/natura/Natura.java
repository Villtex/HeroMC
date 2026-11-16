package natura;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Natura 2.1.5.1 for NeoForge. The 1.5.2 Nether of Natura (Netherite chunk provider and the Nether parts of the
 * world generators) is ported in part 7b; blocks, items and mobs of the Nether are here.
 */
@Mod(Natura.MOD_ID)
public final class Natura {
    public static final String MOD_ID = "natura";

    public Natura(IEventBus modBus) {
        NaturaBlocks.register(modBus);
        natura.gen.NaturaWorldGen.register();
        // registerOldBlocks();   (ids of the pack -> block states)
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            natura.client.NaturaClient.register(modBus);
        }
    }
}
