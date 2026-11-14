package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

public class ComponentScatteredFeatureSwampHut extends ComponentScatteredFeature {
    private boolean hasWitch;

    public ComponentScatteredFeatureSwampHut(Random var1, int var2, int var3) {
        super(var1, var2, 64, var3, 7, 5, 9);
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        if (!this.func_74935_a(var1, var3, 0)) {
            return false;
        }

        this.fillWithMetadataBlocks(var1, var3, 1, 1, 1, 5, 1, 7, 5 /* planks */, 1, 5 /* planks */, 1, false);
        this.fillWithMetadataBlocks(var1, var3, 1, 4, 2, 5, 4, 7, 5 /* planks */, 1, 5 /* planks */, 1, false);
        this.fillWithMetadataBlocks(var1, var3, 2, 1, 0, 4, 1, 0, 5 /* planks */, 1, 5 /* planks */, 1, false);
        this.fillWithMetadataBlocks(var1, var3, 2, 2, 2, 3, 3, 2, 5 /* planks */, 1, 5 /* planks */, 1, false);
        this.fillWithMetadataBlocks(var1, var3, 1, 2, 3, 1, 3, 6, 5 /* planks */, 1, 5 /* planks */, 1, false);
        this.fillWithMetadataBlocks(var1, var3, 5, 2, 3, 5, 3, 6, 5 /* planks */, 1, 5 /* planks */, 1, false);
        this.fillWithMetadataBlocks(var1, var3, 2, 2, 7, 4, 3, 7, 5 /* planks */, 1, 5 /* planks */, 1, false);
        this.fillWithBlocks(var1, var3, 1, 0, 2, 1, 3, 2, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(var1, var3, 5, 0, 2, 5, 3, 2, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(var1, var3, 1, 0, 7, 1, 3, 7, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(var1, var3, 5, 0, 7, 5, 3, 7, 17 /* wood */, 17 /* wood */, false);
        this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 2, 3, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 3, 3, 7, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 1, 3, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 5, 3, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 5, 3, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 140 /* flowerPot */, 7, 1, 3, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 58 /* workbench */, 0, 3, 2, 6, var3);
        this.placeBlockAtCurrentPosition(var1, 118 /* cauldron */, 0, 4, 2, 6, var3);
        this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 1, 2, 1, var3);
        this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 5, 2, 1, var3);
        int var4 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 3);
        int var5 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 1);
        int var6 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 0);
        int var7 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 2);
        this.fillWithMetadataBlocks(var1, var3, 0, 4, 1, 6, 4, 1, 134 /* stairsWoodSpruce */, var4, 134 /* stairsWoodSpruce */, var4, false);
        this.fillWithMetadataBlocks(var1, var3, 0, 4, 2, 0, 4, 7, 134 /* stairsWoodSpruce */, var6, 134 /* stairsWoodSpruce */, var6, false);
        this.fillWithMetadataBlocks(var1, var3, 6, 4, 2, 6, 4, 7, 134 /* stairsWoodSpruce */, var5, 134 /* stairsWoodSpruce */, var5, false);
        this.fillWithMetadataBlocks(var1, var3, 0, 4, 8, 6, 4, 8, 134 /* stairsWoodSpruce */, var7, 134 /* stairsWoodSpruce */, var7, false);

        for (byte var8 = 2; var8 <= 7; var8 += 5) {
            for (byte var9 = 1; var9 <= 5; var9 += 4) {
                this.fillCurrentPositionBlocksDownwards(var1, 17 /* wood */, 0, var9, -1, var8, var3);
            }
        }

        if (!this.hasWitch) {
            int var12 = this.getXWithOffset(2, 5);
            int var13 = this.getYWithOffset(2);
            int var10 = this.getZWithOffset(2, 5);
            if (var3.isVecInside(var12, var13, var10)) {
                this.hasWitch = true;
                var1.spawnEntity(new OldEntity.Witch(var12 + 0.5, var13, var10 + 0.5));
            }
        }

        return true;
    }
}
