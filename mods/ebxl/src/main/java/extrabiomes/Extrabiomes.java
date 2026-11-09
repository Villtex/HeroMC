package extrabiomes;

import hexxit.worldgen152.OldBlockStates;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * ExtrabiomesXL 3.13.4 for NeoForge.
 *
 * <p>Ported so far: the 28 biomes as part of the 1.5.2 world layout, and the two terrain blocks (red rock in
 * its three forms, cracked sand) with their recipes. Trees, plants and the remaining blocks follow.
 */
@Mod(Extrabiomes.MOD_ID)
public final class Extrabiomes {
    public static final String MOD_ID = "extrabiomesxl";

    public Extrabiomes(IEventBus modBus) {
        EbxlBlocks.register(modBus);
        EbxlBiomes.register();
        OldBlockStates.register(EbxlBiomes.RED_ROCK_ID, () -> EbxlBlocks.RED_ROCK.get().defaultBlockState());
        OldBlockStates.register(EbxlBiomes.CRACKED_SAND_ID, () -> EbxlBlocks.CRACKED_SAND.get().defaultBlockState());
    }
}
