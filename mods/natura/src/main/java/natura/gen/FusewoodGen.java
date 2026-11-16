package natura.gen;

import java.util.Random;

/** FusewoodGen: {@link DarkwoodGen} with fusewood leaves (the two classes of the original differ only there). */
public class FusewoodGen extends DarkwoodGen {
    public FusewoodGen(boolean notify) {
        this(notify, 4, 0);
    }

    public FusewoodGen(boolean notify, int treeHeight, int woodMeta) {
        super(notify, treeHeight, woodMeta);
    }

    @Override
    int leafMeta(Random random) {
        return NaturaIds.DARK_LEAVES_FUSEWOOD;
    }
}
