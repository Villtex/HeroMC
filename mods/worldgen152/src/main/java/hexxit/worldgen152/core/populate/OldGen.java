package hexxit.worldgen152.core.populate;

import java.util.Random;

/** A 1.5.2 {@code WorldGenerator}: places one feature at a position, drawing from the chunk's random sequence. */
public abstract class OldGen {
    public abstract boolean generate(OldWorld world, Random random, int x, int y, int z);

    /** WorldGenerator.setScale; only the big oak reacts to it. */
    public void setScale(double height, double width, double leafDensity) {
    }
}
