package twilightforest;

import hexxit.worldgen152.OldLiveGen;
import hexxit.worldgen152.core.populate.OldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import twilightforest.gen.TFTrees;

import java.util.Random;

/**
 * BlockTFSapling.growTree: the 1.5.2 generator of each sapling grows through {@link OldLiveGen}. The bounds are
 * what the generators reach (measured over 300 trees on flat ground, plus a block): radius to the sides, depth
 * below and height above the sapling.
 */
public final class TFSaplings {
    private TFSaplings() {
    }

    private record Grower(OldGen gen, int radius, int depth, int height) {
    }

    private static Grower grower(int meta, Random random) {
        return switch (meta) {
            case 1 -> new Grower(new TFTrees.Canopy(), 10, 10, 30);
            case 2 -> new Grower(new TFTrees.Mangrove(false), 10, 8, 16);
            case 3 -> new Grower(new TFTrees.DarkCanopy(), 12, 10, 13);
            case 4 -> new Grower(new TFTrees.Hollow(), 30, 11, 124);
            case 5 -> new Grower(new TFTrees.TreeOfTime(), 10, 20, 16);
            case 6 -> new Grower(new TFTrees.Transformation(), 10, 10, 14);
            case 7 -> new Grower(new TFTrees.Miners(), 7, 11, 12);
            case 8 -> new Grower(new TFTrees.Sorting(), 2, 2, 5);
            // the rainbow oak: 1 in 7 the large one
            case 9 -> random.nextInt(7) == 0 ? new Grower(TFTrees.largeRainboak(), 9, 2, 18) : new Grower(new TFTrees.SmallOak(3), 3, 2, 8);
            default -> new Grower(new TFTrees.SmallOak(0), 3, 2, 8);
        };
    }

    /** Grows the tree of a sapling of metadata {@code meta} at {@code pos}; false if it did not fit. */
    public static boolean grow(ServerLevel level, BlockPos pos, int meta, Random random) {
        Grower g = grower(meta, random);
        return OldLiveGen.generate(level, g.gen(), random, pos.getX(), pos.getY(), pos.getZ(), g.radius(), g.depth(), g.height());
    }
}
