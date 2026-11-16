package artifice;

import artifice.block.LotusBlock;
import artifice.block.LotusLilyBlock;
import artifice.gen.ArtificeIds;
import hexxit.worldgen152.OldBlockStates;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Artifice 1.1.2 (Hexxit 1.0.11) for NeoForge: basalt and marble, flowers and lotus lilies, steel, frames, blast and
 * glass walls, scaffolding, the detector, sledges, sickles, boxes, coins and tool upgrades. Only the compat module
 * {@code ArtificeCompat|Vanilla} of the pack is ported (sledge parts in {@link artifice.item.SledgeParts}); the other
 * modules need mods that are not in the pack. The world generation is in {@link artifice.gen.ArtificeWorldGen}.
 */
@Mod(Artifice.MOD_ID)
public final class Artifice {
    public static final String MOD_ID = "artifice";

    public Artifice(IEventBus modBus) {
        ArtificeBlocks.register(modBus);
        ArtificeRecipes.register(modBus);
        modBus.addListener(ArtificeEvents::commonSetup);
        NeoForge.EVENT_BUS.register(ArtificeEvents.class);
        artifice.gen.ArtificeWorldGen.register();
        registerOldBlocks();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            artifice.client.ArtificeClient.register(modBus);
        }
    }

    private static BlockState flora(int meta) {
        return switch (meta) {
            case 1 -> ArtificeBlocks.ORCHID.get().defaultBlockState();
            case 2 -> ArtificeBlocks.IRIS.get().defaultBlockState();
            case 3 -> ArtificeBlocks.LOTUS.get().defaultBlockState().setValue(LotusBlock.OPEN, true);
            case 4 -> ArtificeBlocks.LOTUS.get().defaultBlockState().setValue(LotusBlock.OPEN, false);
            default -> ArtificeBlocks.BLUEBELL.get().defaultBlockState();
        };
    }

    /** The ids the 1.5.2 world generation of Artifice writes (config/Artifice.cfg of the pack). */
    private static void registerOldBlocks() {
        OldBlockStates.register(ArtificeIds.BASALT, () -> ArtificeBlocks.BASALT.get().defaultBlockState());
        OldBlockStates.register(ArtificeIds.MARBLE, () -> ArtificeBlocks.MARBLE.get().defaultBlockState());
        OldBlockStates.registerMeta(ArtificeIds.FLORA, Artifice::flora);
        OldBlockStates.registerMeta(ArtificeIds.LOTUS, meta -> ArtificeBlocks.LOTUS_LILY.get().defaultBlockState()
                .setValue(LotusLilyBlock.OPEN, meta != 1));
    }
}
