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

public class ComponentVillageHall extends ComponentVillage {
    private int averageGroundLevel = -1;

    public ComponentVillageHall(ComponentVillageStartPiece var1, int var2, Random var3, StructureBoundingBox var4, int var5) {
        super(var1, var2);
        this.coordBaseMode = var5;
        this.boundingBox = var4;
    }

    public static ComponentVillageHall func_74906_a(ComponentVillageStartPiece var0, List var1, Random var2, int var3, int var4, int var5, int var6, int var7) {
        StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 9, 7, 11, var6);
        return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new ComponentVillageHall(var0, var7, var2, var8, var6) : null;
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
        this.fillWithBlocks(var1, var3, 2, 0, 6, 8, 0, 10, 3 /* dirt */, 3 /* dirt */, false);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 6, 0, 6, var3);
        this.fillWithBlocks(var1, var3, 2, 1, 6, 2, 1, 10, 85 /* fence */, 85 /* fence */, false);
        this.fillWithBlocks(var1, var3, 8, 1, 6, 8, 1, 10, 85 /* fence */, 85 /* fence */, false);
        this.fillWithBlocks(var1, var3, 3, 1, 10, 7, 1, 10, 85 /* fence */, 85 /* fence */, false);
        this.fillWithBlocks(var1, var3, 1, 0, 1, 7, 0, 4, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 0, 0, 0, 0, 3, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 8, 0, 0, 8, 3, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 1, 0, 0, 7, 1, 0, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 1, 0, 5, 7, 1, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 1, 2, 0, 7, 3, 0, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 2, 5, 7, 3, 5, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 0, 4, 1, 8, 4, 1, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 0, 4, 4, 8, 4, 4, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 0, 5, 2, 8, 5, 3, 5 /* planks */, 5 /* planks */, false);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 0, 4, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 0, 4, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 8, 4, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 8, 4, 3, var3);
        int var4 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 3);
        int var5 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 2);

        for (int var6 = -1; var6 <= 2; var6++) {
            for (int var7 = 0; var7 <= 8; var7++) {
                this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var4, var7, 4 + var6, var6, var3);
                this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var5, var7, 4 + var6, 5 - var6, var3);
            }
        }

        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 0, 2, 1, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 0, 2, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 8, 2, 1, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 8, 2, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 2, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 2, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 2, 2, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 3, 2, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 5, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 6, 2, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 2, 1, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 72 /* pressurePlatePlanks */, 0, 2, 2, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 1, 1, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, this.getMetadataWithOffset(53 /* stairsWoodOak */, 3), 2, 1, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, this.getMetadataWithOffset(53 /* stairsWoodOak */, 1), 1, 1, 3, var3);
        this.fillWithBlocks(var1, var3, 5, 0, 1, 7, 0, 3, 43 /* stoneDoubleSlab */, 43 /* stoneDoubleSlab */, false);
        this.placeBlockAtCurrentPosition(var1, 43 /* stoneDoubleSlab */, 0, 6, 1, 1, var3);
        this.placeBlockAtCurrentPosition(var1, 43 /* stoneDoubleSlab */, 0, 6, 1, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 2, 1, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 2, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 50 /* torchWood */, 0, 2, 3, 1, var3);
        this.placeDoorAtCurrentPosition(var1, var3, var2, 2, 1, 0, this.getMetadataWithOffset(64 /* doorWood */, 1));
        if (this.getBlockIdAtCurrentPosition(var1, 2, 0, -1, var3) == 0 && this.getBlockIdAtCurrentPosition(var1, 2, -1, -1, var3) != 0) {
            this.placeBlockAtCurrentPosition(var1, 67 /* stairsCobblestone */, this.getMetadataWithOffset(67 /* stairsCobblestone */, 3), 2, 0, -1, var3);
        }

        this.placeBlockAtCurrentPosition(var1, 0, 0, 6, 1, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 6, 2, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 50 /* torchWood */, 0, 6, 3, 4, var3);
        this.placeDoorAtCurrentPosition(var1, var3, var2, 6, 1, 5, this.getMetadataWithOffset(64 /* doorWood */, 1));

        for (int var8 = 0; var8 < 5; var8++) {
            for (int var9 = 0; var9 < 9; var9++) {
                this.clearCurrentPositionBlocksUpwards(var1, var9, 7, var8, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 4 /* cobblestone */, 0, var9, -1, var8, var3);
            }
        }

        this.spawnVillagers(var1, var3, 4, 1, 2, 2);
        return true;
    }

    @Override
    protected int getVillagerType(int var1) {
        return var1 == 0 ? 4 : 0;
    }
}
