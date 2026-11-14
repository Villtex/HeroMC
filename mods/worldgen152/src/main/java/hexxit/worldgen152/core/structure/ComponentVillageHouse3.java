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

public class ComponentVillageHouse3 extends ComponentVillage {
    private int averageGroundLevel = -1;

    public ComponentVillageHouse3(ComponentVillageStartPiece var1, int var2, Random var3, StructureBoundingBox var4, int var5) {
        super(var1, var2);
        this.coordBaseMode = var5;
        this.boundingBox = var4;
    }

    public static ComponentVillageHouse3 func_74921_a(ComponentVillageStartPiece var0, List var1, Random var2, int var3, int var4, int var5, int var6, int var7) {
        StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 9, 7, 12, var6);
        return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new ComponentVillageHouse3(var0, var7, var2, var8, var6) : null;
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        if (this.averageGroundLevel < 0) {
            this.averageGroundLevel = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLevel < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 7 - 1, 0);
        }

        this.fillWithBlocks(var1, var3, 1, 1, 1, 7, 4, 4, 0, 0, false);
        this.fillWithBlocks(var1, var3, 2, 1, 6, 8, 4, 10, 0, 0, false);
        this.fillWithBlocks(var1, var3, 2, 0, 5, 8, 0, 10, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 0, 1, 7, 0, 4, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 0, 0, 0, 0, 3, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 8, 0, 0, 8, 3, 10, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 1, 0, 0, 7, 2, 0, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 1, 0, 5, 2, 1, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 2, 0, 6, 2, 3, 10, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 3, 0, 10, 7, 3, 10, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 1, 2, 0, 7, 3, 0, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 2, 5, 2, 3, 5, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 0, 4, 1, 8, 4, 1, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 0, 4, 4, 3, 4, 4, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 0, 5, 2, 8, 5, 3, 5 /* planks */, 5 /* planks */, false);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 0, 4, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 0, 4, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 8, 4, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 8, 4, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 8, 4, 4, var3);
        int var4 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 3);
        int var5 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 2);

        for (int var6 = -1; var6 <= 2; var6++) {
            for (int var7 = 0; var7 <= 8; var7++) {
                this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var4, var7, 4 + var6, var6, var3);
                if ((var6 > -1 || var7 <= 1) && (var6 > 0 || var7 <= 3) && (var6 > 1 || var7 <= 4 || var7 >= 6)) {
                    this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var5, var7, 4 + var6, 5 - var6, var3);
                }
            }
        }

        this.fillWithBlocks(var1, var3, 3, 4, 5, 3, 4, 10, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 7, 4, 2, 7, 4, 10, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 4, 5, 4, 4, 5, 10, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 6, 5, 4, 6, 5, 10, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 5, 6, 3, 5, 6, 10, 5 /* planks */, 5 /* planks */, false);
        int var10 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 0);

        for (int var11 = 4; var11 >= 1; var11--) {
            this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, var11, 2 + var11, 7 - var11, var3);

            for (int var8 = 8 - var11; var8 <= 10; var8++) {
                this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var10, var11, 2 + var11, var8, var3);
            }
        }

        int var12 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 1);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 6, 6, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 7, 5, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var12, 6, 6, 4, var3);

        for (int var13 = 6; var13 <= 8; var13++) {
            for (int var9 = 5; var9 <= 10; var9++) {
                this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var12, var13, 12 - var13, var9, var3);
            }
        }

        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 0, 2, 1, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 0, 2, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 2, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 4, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 5, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 6, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 8, 2, 1, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 2, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 8, 2, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 8, 2, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 8, 2, 6, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 2, 7, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 2, 8, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 8, 2, 9, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 2, 2, 6, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 2, 2, 7, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 2, 2, 8, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 2, 2, 9, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 4, 4, 10, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 5, 4, 10, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 6, 4, 10, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 5, 5, 10, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 2, 1, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 2, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 50 /* torchWood */, 0, 2, 3, 1, var3);
        this.placeDoorAtCurrentPosition(var1, var3, var2, 2, 1, 0, this.getMetadataWithOffset(64 /* doorWood */, 1));
        this.fillWithBlocks(var1, var3, 1, 0, -1, 3, 2, -1, 0, 0, false);
        if (this.getBlockIdAtCurrentPosition(var1, 2, 0, -1, var3) == 0 && this.getBlockIdAtCurrentPosition(var1, 2, -1, -1, var3) != 0) {
            this.placeBlockAtCurrentPosition(var1, 67 /* stairsCobblestone */, this.getMetadataWithOffset(67 /* stairsCobblestone */, 3), 2, 0, -1, var3);
        }

        for (int var14 = 0; var14 < 5; var14++) {
            for (int var16 = 0; var16 < 9; var16++) {
                this.clearCurrentPositionBlocksUpwards(var1, var16, 7, var14, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 4 /* cobblestone */, 0, var16, -1, var14, var3);
            }
        }

        for (int var15 = 5; var15 < 11; var15++) {
            for (int var17 = 2; var17 < 9; var17++) {
                this.clearCurrentPositionBlocksUpwards(var1, var17, 7, var15, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 4 /* cobblestone */, 0, var17, -1, var15, var3);
            }
        }

        this.spawnVillagers(var1, var3, 4, 1, 2, 2);
        return true;
    }
}
