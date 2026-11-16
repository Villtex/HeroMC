package natura.gen;

import hexxit.worldgen152.core.populate.OldWorld;

/**
 * WhiteTreeGen: the ghostwood of the Nether. The same generator as {@link SakuraTreeGen} (the two classes of the
 * original differ in nothing else), on netherrack, soul sand or tainted soil, looking for ground from y 104 down.
 */
public class WhiteTreeGen extends SakuraTreeGen {
    public WhiteTreeGen(boolean notify, int mdwood, int mdleaves) {
        super(notify, mdwood, mdleaves);
    }

    @Override
    boolean validSoil(int id) {
        return netherSoil(id);
    }

    @Override
    int findGround(OldWorld world, int x, int y, int z) {
        boolean foundGround = false;
        int height = 104;
        do {
            int underID = world.getBlockId(x, --height, z);
            if (netherSoil(underID) || height < 0) {
                foundGround = true;
            }
        } while (!foundGround);
        return height + 1;
    }
}
