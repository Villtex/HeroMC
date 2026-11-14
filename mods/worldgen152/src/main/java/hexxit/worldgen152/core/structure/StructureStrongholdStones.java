package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

class StructureStrongholdStones extends StructurePieceBlockSelector {
    StructureStrongholdStones() {
    }

    @Override
    public void selectBlocks(Random var1, int var2, int var3, int var4, boolean var5) {
        if (var5) {
            this.selectedBlockId = 98 /* stoneBrick */;
            float var6 = var1.nextFloat();
            if (var6 < 0.2F) {
                this.selectedBlockMetaData = 2;
            } else if (var6 < 0.5F) {
                this.selectedBlockMetaData = 1;
            } else if (var6 < 0.55F) {
                this.selectedBlockId = 97 /* silverfish */;
                this.selectedBlockMetaData = 2;
            } else {
                this.selectedBlockMetaData = 0;
            }
        } else {
            this.selectedBlockId = 0;
            this.selectedBlockMetaData = 0;
        }
    }
}
