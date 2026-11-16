package natura.gen;

import hexxit.worldgen152.core.populate.OldPlantGens;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * FlowerGen: a patch of a flower-like block. The world generators of the overworld create none (the fields in
 * {@code BaseCropWorldgen} stay empty); the Nether of Natura scatters the small glowshrooms with it.
 */
public class FlowerGen extends NaturaGen {
    /** {@code Block.blocksList[plantID].canBlockStay}. */
    public interface CanStay {
        boolean test(OldWorld world, int x, int y, int z);
    }

    public final int plantID;
    public final int metadata;
    public int chances = 64;
    private final CanStay canStay;
    /** {@code world.provider.hasNoSky}: in the Nether nothing is set at y 127 and above. */
    private final boolean hasNoSky;

    /** A flower of the overworld (BlockFlower.canBlockStay). */
    public FlowerGen(int id, int meta) {
        this(id, meta, 64, OldPlantGens::flowerCanStay, false);
    }

    public FlowerGen(int id, int meta, int count) {
        this(id, meta, count, OldPlantGens::flowerCanStay, false);
    }

    public FlowerGen(int id, int meta, int count, CanStay canStay, boolean hasNoSky) {
        this.plantID = id;
        this.metadata = meta;
        this.chances = count;
        this.canStay = canStay;
        this.hasNoSky = hasNoSky;
    }

    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        for (int iter = 0; iter < this.chances; iter++) {
            int posX = x + random.nextInt(8) - random.nextInt(8);
            int posY = y + random.nextInt(4) - random.nextInt(4);
            int posZ = z + random.nextInt(8) - random.nextInt(8);
            if (world.isAirBlock(posX, posY, posZ) && (!this.hasNoSky || posY < 127) && this.canStay.test(world, posX, posY, posZ)) {
                world.setBlock(posX, posY, posZ, this.plantID, this.metadata);
            }
        }
        return true;
    }
}
