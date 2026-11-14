package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.List;
import java.util.Random;

public class StructureMineshaftPieces {

    private static StructureComponent getRandomComponent(List var0, Random var1, int var2, int var3, int var4, int var5, int var6) {
        int var7 = var1.nextInt(100);
        if (var7 >= 80) {
            StructureBoundingBox var8 = ComponentMineshaftCross.findValidPlacement(var0, var1, var2, var3, var4, var5);
            if (var8 != null) {
                return new ComponentMineshaftCross(var6, var1, var8, var5);
            }
        } else if (var7 >= 70) {
            StructureBoundingBox var9 = ComponentMineshaftStairs.findValidPlacement(var0, var1, var2, var3, var4, var5);
            if (var9 != null) {
                return new ComponentMineshaftStairs(var6, var1, var9, var5);
            }
        } else {
            StructureBoundingBox var10 = ComponentMineshaftCorridor.findValidPlacement(var0, var1, var2, var3, var4, var5);
            if (var10 != null) {
                return new ComponentMineshaftCorridor(var6, var1, var10, var5);
            }
        }

        return null;
    }

    static StructureComponent getNextMineShaftComponent(StructureComponent var0, List var1, Random var2, int var3, int var4, int var5, int var6, int var7) {
        if (var7 > 8) {
            return null;
        }

        if (Math.abs(var3 - var0.getBoundingBox().minX) <= 80 && Math.abs(var5 - var0.getBoundingBox().minZ) <= 80) {
            StructureComponent var8 = getRandomComponent(var1, var2, var3, var4, var5, var6, var7 + 1);
            if (var8 != null) {
                var1.add(var8);
                var8.buildComponent(var0, var1, var2);
            }

            return var8;
        } else {
            return null;
        }
    }
}
