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

public class ComponentVillageHouse1 extends ComponentVillage {
    private int averageGroundLevel = -1;

    public ComponentVillageHouse1(ComponentVillageStartPiece var1, int var2, Random var3, StructureBoundingBox var4, int var5) {
        super(var1, var2);
        this.coordBaseMode = var5;
        this.boundingBox = var4;
    }

    public static ComponentVillageHouse1 func_74898_a(ComponentVillageStartPiece var0, List var1, Random var2, int var3, int var4, int var5, int var6, int var7) {
        StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 9, 9, 6, var6);
        return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new ComponentVillageHouse1(var0, var7, var2, var8, var6) : null;
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        if (this.averageGroundLevel < 0) {
            this.averageGroundLevel = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLevel < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 9 - 1, 0);
        }

        this.fillWithBlocks(var1, var3, 1, 1, 1, 7, 5, 4, 0, 0, false);
        this.fillWithBlocks(var1, var3, 0, 0, 0, 8, 0, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 0, 5, 0, 8, 5, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 0, 6, 1, 8, 6, 4, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 0, 7, 2, 8, 7, 3, 4 /* cobblestone */, 4 /* cobblestone */, false);
        int var4 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 3);
        int var5 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 2);

        for (int var6 = -1; var6 <= 2; var6++) {
            for (int var7 = 0; var7 <= 8; var7++) {
                this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var4, var7, 6 + var6, var6, var3);
                this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var5, var7, 6 + var6, 5 - var6, var3);
            }
        }

        this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 1, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 1, 1, 5, 8, 1, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 8, 1, 0, 8, 1, 4, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 2, 1, 0, 7, 1, 0, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 0, 2, 0, 0, 4, 0, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 0, 2, 5, 0, 4, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 8, 2, 5, 8, 4, 5, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 8, 2, 0, 8, 4, 0, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 0, 2, 1, 0, 4, 4, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 2, 5, 7, 4, 5, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 8, 2, 1, 8, 4, 4, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 2, 0, 7, 4, 0, 5 /* planks */, 5 /* planks */, false);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 4, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 5, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 6, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 4, 3, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 5, 3, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 6, 3, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 2, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 3, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 3, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 2, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 3, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 8, 3, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 2, 2, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 3, 2, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 5, 2, 5, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 6, 2, 5, var3);
        this.fillWithBlocks(var1, var3, 1, 4, 1, 7, 4, 1, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 4, 4, 7, 4, 4, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 3, 4, 7, 3, 4, 47 /* bookShelf */, 47 /* bookShelf */, false);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 7, 1, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, this.getMetadataWithOffset(53 /* stairsWoodOak */, 0), 7, 1, 3, var3);
        int var9 = this.getMetadataWithOffset(53 /* stairsWoodOak */, 3);
        this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var9, 6, 1, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var9, 5, 1, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var9, 4, 1, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 53 /* stairsWoodOak */, var9, 3, 1, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 6, 1, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 72 /* pressurePlatePlanks */, 0, 6, 2, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 4, 1, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 72 /* pressurePlatePlanks */, 0, 4, 2, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 58 /* workbench */, 0, 7, 1, 1, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 1, 1, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 1, 2, 0, var3);
        this.placeDoorAtCurrentPosition(var1, var3, var2, 1, 1, 0, this.getMetadataWithOffset(64 /* doorWood */, 1));
        if (this.getBlockIdAtCurrentPosition(var1, 1, 0, -1, var3) == 0 && this.getBlockIdAtCurrentPosition(var1, 1, -1, -1, var3) != 0) {
            this.placeBlockAtCurrentPosition(var1, 67 /* stairsCobblestone */, this.getMetadataWithOffset(67 /* stairsCobblestone */, 3), 1, 0, -1, var3);
        }

        for (int var10 = 0; var10 < 6; var10++) {
            for (int var8 = 0; var8 < 9; var8++) {
                this.clearCurrentPositionBlocksUpwards(var1, var8, 9, var10, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 4 /* cobblestone */, 0, var8, -1, var10, var3);
            }
        }

        this.spawnVillagers(var1, var3, 2, 1, 2, 1);
        return true;
    }

    @Override
    protected int getVillagerType(int var1) {
        return 1;
    }
}
