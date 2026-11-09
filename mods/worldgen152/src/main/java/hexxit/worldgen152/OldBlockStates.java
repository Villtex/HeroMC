package hexxit.worldgen152;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Translates the numeric block ids the 1.5.2 terrain generator writes into block states.
 * Mods whose blocks were part of the old terrain (ids below 256) add theirs with {@link #register}.
 */
public final class OldBlockStates {
    @SuppressWarnings("unchecked")
    private static final Supplier<BlockState>[] SUPPLIERS = new Supplier[256];
    private static final BlockState[] RESOLVED = new BlockState[256];

    static {
        register(1, () -> Blocks.STONE.defaultBlockState());
        register(2, () -> Blocks.GRASS_BLOCK.defaultBlockState());
        register(3, () -> Blocks.DIRT.defaultBlockState());
        register(7, () -> Blocks.BEDROCK.defaultBlockState());
        register(8, () -> Blocks.WATER.defaultBlockState());
        register(9, () -> Blocks.WATER.defaultBlockState());
        register(10, () -> Blocks.LAVA.defaultBlockState());
        register(11, () -> Blocks.LAVA.defaultBlockState());
        register(12, () -> Blocks.SAND.defaultBlockState());
        register(13, () -> Blocks.GRAVEL.defaultBlockState());
        register(24, () -> Blocks.SANDSTONE.defaultBlockState());
        register(79, () -> Blocks.ICE.defaultBlockState());
        register(80, () -> Blocks.SNOW_BLOCK.defaultBlockState());
        register(110, () -> Blocks.MYCELIUM.defaultBlockState());
    }

    private OldBlockStates() {
    }

    public static synchronized void register(int oldId, Supplier<BlockState> state) {
        if (oldId <= 0 || oldId > 255) {
            throw new IllegalArgumentException("Terrain block ids must be 1..255, got " + oldId);
        }
        if (SUPPLIERS[oldId] != null) {
            throw new IllegalArgumentException("Terrain block id " + oldId + " is already mapped");
        }
        SUPPLIERS[oldId] = state;
    }

    /** State for an old block id, or {@code null} for air. */
    public static BlockState get(int oldId) {
        if (oldId == 0) {
            return null;
        }
        BlockState state = RESOLVED[oldId];
        if (state == null) {
            Supplier<BlockState> supplier = SUPPLIERS[oldId];
            if (supplier == null) {
                throw new IllegalStateException("1.5.2 terrain uses block id " + oldId + ", which no mod has mapped to a block");
            }
            state = supplier.get();
            RESOLVED[oldId] = state;
        }
        return state;
    }
}
