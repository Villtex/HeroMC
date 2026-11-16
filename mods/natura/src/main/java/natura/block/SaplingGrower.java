package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Random;

/**
 * Hook for everything that grows with a 1.5.2 generator of Natura (saplings, saguaro sprouts, bone-mealed
 * glowshrooms). The generators live in {@code natura.gen}; the integrator sets the grower with
 * {@link NaturaTree#setGrower(SaplingGrower)}. Until then nothing grows (the sapling stays marked).
 */
@FunctionalInterface
public interface SaplingGrower {
    /**
     * Runs the original generator of {@code tree} at {@code pos} (x, y, z of the 1.5.2 call).
     *
     * @return whether the generator succeeded ({@code WorldGenerator.generate} returned true)
     */
    boolean grow(ServerLevel level, BlockPos pos, NaturaTree tree, Random random);
}
