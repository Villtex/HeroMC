package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

public abstract class StructurePieceBlockSelector {
    protected int selectedBlockId;
    protected int selectedBlockMetaData;

    protected StructurePieceBlockSelector() {
    }

    public abstract void selectBlocks(Random var1, int var2, int var3, int var4, boolean var5);

    public int getSelectedBlockId() {
        return this.selectedBlockId;
    }

    public int getSelectedBlockMetaData() {
        return this.selectedBlockMetaData;
    }
}
