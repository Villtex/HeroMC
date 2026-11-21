package tconstruct.recipe;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Detailing (TConstruct.chiselDetailing): what the chisel makes of a block, in the world (right click) and in the
 * crafting grid (chisel + block). The order of TContent.addCraftingRecipes; the blocks of later parts (multi bricks,
 * speed blocks: 25f; seared blocks: 25c) add themselves with {@link #add}.
 */
public final class Detailing {
    /** One step: the input block and the output state (the meta of 1.5.2 is the block of today). */
    public record Detail(Supplier<Block> input, Supplier<BlockState> output) {
    }

    private static final List<Detail> DETAILS = new ArrayList<>();

    static {
        add(() -> Blocks.STONE, () -> Blocks.STONE_BRICKS.defaultBlockState());
        // sandStone 0 -> 2 (smooth = cut today) -> 1 (chiseled)
        add(() -> Blocks.SANDSTONE, () -> Blocks.CUT_SANDSTONE.defaultBlockState());
        add(() -> Blocks.CUT_SANDSTONE, () -> Blocks.CHISELED_SANDSTONE.defaultBlockState());
        // the seared blocks (part 25c): stone -> paver -> chiseled -> bricks -> road -> fancy -> square
        int[][] seared = {{1, 3}, {3, 8}, {8, 0}, {0, 5}, {5, 6}, {6, 7}};
        for (int[] step : seared) {
            add(() -> tconstruct.TSmeltery.SEARED.get(step[0]).get(), () -> tconstruct.TSmeltery.SEARED.get(step[1]).get().defaultBlockState());
        }
    }

    private Detailing() {
    }

    public static void add(Supplier<Block> input, Supplier<BlockState> output) {
        DETAILS.add(new Detail(input, output));
    }

    /** getDetailing: the first step for this block, or null. */
    public static @Nullable Detail get(BlockState state) {
        for (Detail detail : DETAILS) {
            if (state.is(detail.input().get())) {
                return detail;
            }
        }
        return null;
    }

    public static @Nullable Detail get(Block block) {
        return get(block.defaultBlockState());
    }

    public static List<Detail> all() {
        return Collections.unmodifiableList(DETAILS);
    }
}
