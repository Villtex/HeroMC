package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Random;

/**
 * What a sapling (or sprout, or small glowshroom) grows into, with the 1.5.2 generator call of the original.
 * For all entries except {@link #SAGUARO} the caller has already removed the sapling (set air) and puts it back
 * (marked) when {@link #grow} returns false, exactly as {@code NSaplingBlock.growTree} / {@code OverworldSapling.growTree}
 * / {@code Glowshroom.fertilizeMushroom} did.
 */
public enum NaturaTree {
    /** NSaplingBlock meta 0: {@code new RedwoodTreeGen(true, PHNatura.redwoodID)}. */
    REDWOOD("new RedwoodTreeGen(true, redwoodID)"),
    /** meta 1. */
    EUCALYPTUS("new EucalyptusTreeGenShort(0, 1)"),
    /** meta 2. */
    HOPSEED("new BushTreeGen(true, 2, 3, 2)"),
    /** meta 3. */
    SAKURA("new SakuraTreeGen(true, 1, 0)"),
    /** meta 4. */
    GHOSTWOOD("new WhiteTreeGen(true, 2, 1)"),
    /** meta 5 (hangs from the ceiling). */
    BLOODWOOD("new BloodTreeLargeGen(3, 2)"),
    /** meta 6. */
    DARKWOOD("new DarkwoodGen(true, 3, 0)"),
    /** meta 7. */
    FUSEWOOD("new FusewoodGen(true, 3, 1)"),
    /** OverworldSapling meta 0. */
    MAPLE("new RareTreeGen(true, 4, 2, 0, 0)"),
    /** meta 1. */
    SILVERBELL("new RareTreeGen(true, 4, 2, 1, 1)"),
    /** meta 2. */
    AMARANTH("new RareTreeGen(true, 9, 8, 2, 2)"),
    /** meta 3. */
    TIGER("new RareTreeGen(true, 6, 4, 3, 3)"),
    /** meta 4. */
    WILLOW("new WillowGen(true)"),
    /**
     * SaguaroBlock meta 2 (ripe sprout), random tick 1/200: {@code new SaguaroGen(saguaroID, 0, true).generate(world,
     * random, x, y, z)} at the sprout itself; the sprout is NOT removed first (the generator overwrites it).
     */
    SAGUARO("new SaguaroGen(saguaro, 0, true)"),
    /** Glowshroom meta 0: {@code new GlowshroomGenBlueGreen(true, 0)}. */
    GREEN_GLOWSHROOM("new GlowshroomGenBlueGreen(true, 0)"),
    /** Glowshroom meta 1. */
    PURPLE_GLOWSHROOM("new GlowshroomGenPurple(true)"),
    /** Glowshroom meta 2. */
    BLUE_GLOWSHROOM("new GlowshroomGenBlueGreen(true, 1)");

    /** The generator call of the original, for reference. */
    public final String original;

    private static volatile SaplingGrower grower;

    NaturaTree(String original) {
        this.original = original;
    }

    /** Called once by the integration (Natura constructor) with the generators of {@code natura.gen}. */
    public static void setGrower(SaplingGrower newGrower) {
        grower = newGrower;
    }

    /** Runs the generator; false when none is wired up. */
    public boolean grow(ServerLevel level, BlockPos pos, Random random) {
        SaplingGrower g = grower;
        return g != null && g.grow(level, pos, this, random);
    }
}
