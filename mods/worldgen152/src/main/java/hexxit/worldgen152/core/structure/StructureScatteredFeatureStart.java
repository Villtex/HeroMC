package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

public class StructureScatteredFeatureStart extends StructureStart {
    public StructureScatteredFeatureStart(OldStructures var1, Random var2, int var3, int var4) {
        OldBiome var5 = var1.getBiomeGenAt(var3 * 16 + 8, var4 * 16 + 8);
        if (var5 == OldBiomes.JUNGLE || var5 == OldBiomes.JUNGLE_HILLS) {
            ComponentScatteredFeatureJunglePyramid var8 = new ComponentScatteredFeatureJunglePyramid(var2, var3 * 16, var4 * 16);
            this.components.add(var8);
        } else if (var5 == OldBiomes.SWAMPLAND) {
            ComponentScatteredFeatureSwampHut var6 = new ComponentScatteredFeatureSwampHut(var2, var3 * 16, var4 * 16);
            this.components.add(var6);
        } else {
            ComponentScatteredFeatureDesertPyramid var7 = new ComponentScatteredFeatureDesertPyramid(var2, var3 * 16, var4 * 16);
            this.components.add(var7);
        }

        this.updateBoundingBox();
    }
}
