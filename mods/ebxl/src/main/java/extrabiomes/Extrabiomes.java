package extrabiomes;

import extrabiomes.block.QuarterLogBlock;
import extrabiomes.gen.EbxlIds;
import hexxit.worldgen152.OldBlockStates;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.function.Supplier;

/**
 * ExtrabiomesXL 3.13.4 for NeoForge: the 28 biomes with their own decoration and world generators, the trees
 * (fir, redwood, acacia, autumn, legend oak) with logs, planks, leaves and saplings, flowers, grasses,
 * cattails, leaf piles, quicksand, red rock, stairs, slabs, a wall, the log turner, cactus paste and the
 * scarecrow.
 */
@Mod(Extrabiomes.MOD_ID)
public final class Extrabiomes {
    public static final String MOD_ID = "extrabiomesxl";

    public Extrabiomes(IEventBus modBus) {
        EbxlBlocks.register(modBus);
        EbxlBiomes.register();
        registerOldBlocks();
        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) ->
                event.enqueueWork(extrabiomes.integrated.EbxlRegion::register));
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            extrabiomes.client.EbxlClient.register(modBus);
        }
    }

    /** 1.5.2 orientation bits of a log (metadata 4/8) to the axis of today. */
    private static Direction.Axis axis(int meta) {
        return switch (meta & 12) {
            case 4 -> Direction.Axis.X;
            case 8 -> Direction.Axis.Z;
            default -> Direction.Axis.Y;
        };
    }

    /** Block of a metadata value from a list of sub-blocks (values beyond the list fall back to the first). */
    private static BlockState byMeta(List<? extends Supplier<? extends Block>> blocks, int meta) {
        return (meta < blocks.size() ? blocks.get(meta) : blocks.get(0)).get().defaultBlockState();
    }

    private static void quarterLog(int id, QuarterLogBlock.Bark bark) {
        List<DeferredBlock<QuarterLogBlock>> woods = List.of(EbxlBlocks.REDWOOD_QUARTER_LOG, EbxlBlocks.FIR_QUARTER_LOG, EbxlBlocks.OAK_QUARTER_LOG);
        OldBlockStates.registerMeta(id, meta -> byMeta(woods, meta & 3).setValue(QuarterLogBlock.BARK, bark)
                .setValue(RotatedPillarBlock.AXIS, axis(meta)));
    }

    /** The ids the 1.5.2 generation writes (and the other ExtrabiomesXL ids of the pack). */
    private static void registerOldBlocks() {
        OldBlockStates.register(EbxlIds.RED_ROCK, () -> EbxlBlocks.RED_ROCK.get().defaultBlockState());
        OldBlockStates.register(EbxlIds.CRACKED_SAND, () -> EbxlBlocks.CRACKED_SAND.get().defaultBlockState());
        OldBlockStates.registerMeta(EbxlIds.AUTUMN_LEAVES, meta -> byMeta(List.of(EbxlBlocks.BROWN_AUTUMN_LEAVES, EbxlBlocks.ORANGE_AUTUMN_LEAVES,
                EbxlBlocks.PURPLE_AUTUMN_LEAVES, EbxlBlocks.YELLOW_AUTUMN_LEAVES), meta & 3));
        OldBlockStates.registerMeta(EbxlIds.GREEN_LEAVES, meta -> byMeta(List.of(EbxlBlocks.FIR_LEAVES, EbxlBlocks.REDWOOD_LEAVES,
                EbxlBlocks.ACACIA_LEAVES), meta & 3));
        OldBlockStates.register(EbxlIds.CATTAIL, () -> EbxlBlocks.CATTAIL.get().defaultBlockState());
        OldBlockStates.registerMeta(EbxlIds.CUSTOM_LOG, meta -> byMeta(List.of(EbxlBlocks.FIR_LOG, EbxlBlocks.ACACIA_LOG), meta & 3)
                .setValue(RotatedPillarBlock.AXIS, axis(meta)));
        OldBlockStates.registerMeta(EbxlIds.FLOWER, meta -> byMeta(List.of(EbxlBlocks.AUTUMN_SHRUB, EbxlBlocks.HYDRANGEA, EbxlBlocks.BUTTERCUPS,
                EbxlBlocks.PURPLE_FLOWER, EbxlBlocks.TINY_CACTUS, EbxlBlocks.ROOT, EbxlBlocks.TOADSTOOL, EbxlBlocks.CALLA_LILIES), meta));
        OldBlockStates.registerMeta(EbxlIds.GRASS, meta -> byMeta(List.of(EbxlBlocks.BROWN_GRASS, EbxlBlocks.SHORT_BROWN_GRASS, EbxlBlocks.DEAD_GRASS,
                EbxlBlocks.TALL_DEAD_GRASS, EbxlBlocks.DEAD_YELLOW_GRASS), meta));
        OldBlockStates.register(EbxlIds.LEAF_PILE, () -> EbxlBlocks.LEAF_PILE.get().defaultBlockState());
        OldBlockStates.registerMeta(EbxlIds.PLANKS, meta -> byMeta(List.of(EbxlBlocks.REDWOOD_PLANKS, EbxlBlocks.FIR_PLANKS, EbxlBlocks.ACACIA_PLANKS), meta));
        quarterLog(EbxlIds.QUARTER_LOG_NW, QuarterLogBlock.Bark.NW);
        quarterLog(EbxlIds.QUARTER_LOG_NE, QuarterLogBlock.Bark.NE);
        quarterLog(EbxlIds.QUARTER_LOG_SW, QuarterLogBlock.Bark.SW);
        quarterLog(EbxlIds.QUARTER_LOG_SE, QuarterLogBlock.Bark.SE);
        OldBlockStates.register(EbxlIds.QUICKSAND, () -> EbxlBlocks.QUICKSAND.get().defaultBlockState());
        OldBlockStates.registerMeta(EbxlIds.SAPLING, meta -> byMeta(List.of(EbxlBlocks.BROWN_AUTUMN_SAPLING, EbxlBlocks.ORANGE_AUTUMN_SAPLING,
                EbxlBlocks.PURPLE_AUTUMN_SAPLING, EbxlBlocks.YELLOW_AUTUMN_SAPLING, EbxlBlocks.FIR_SAPLING, EbxlBlocks.REDWOOD_SAPLING,
                EbxlBlocks.ACACIA_SAPLING), meta & 7));
    }
}
