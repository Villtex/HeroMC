package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

class StructureScatteredFeatureStones extends StructurePieceBlockSelector {
    StructureScatteredFeatureStones() {
    }

    @Override
    public void selectBlocks(Random var1, int var2, int var3, int var4, boolean var5) {
        if (var1.nextFloat() < 0.4F) {
            this.selectedBlockId = 4 /* cobblestone */;
        } else {
            this.selectedBlockId = 48 /* cobblestoneMossy */;
        }
    }
}
