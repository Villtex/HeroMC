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

public class ComponentVillageHouse4_Garden extends ComponentVillage {
    private int averageGroundLevel = -1;
    private final boolean isRoofAccessible;

    public ComponentVillageHouse4_Garden(ComponentVillageStartPiece var1, int var2, Random var3, StructureBoundingBox var4, int var5) {
        super(var1, var2);
        this.coordBaseMode = var5;
        this.boundingBox = var4;
        this.isRoofAccessible = var3.nextBoolean();
    }

    public static ComponentVillageHouse4_Garden func_74912_a(
        ComponentVillageStartPiece var0, List var1, Random var2, int var3, int var4, int var5, int var6, int var7
    ) {
        StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 5, 6, 5, var6);
        return StructureComponent.findIntersecting(var1, var8) != null ? null : new ComponentVillageHouse4_Garden(var0, var7, var2, var8, var6);
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        if (this.averageGroundLevel < 0) {
            this.averageGroundLevel = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLevel < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 6 - 1, 0);
        }

        this.fillWithBlocks(var1, var3, 0, 0, 0, 4, 0, 4, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 0, 4, 0, 4, 4, 4, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(var1, var3, 1, 4, 1, 3, 4, 3, 5 /* planks */, 5 /* planks */, false);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 0, 1, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 0, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 0, 3, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 4, 1, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 4, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 4, 3, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 0, 1, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 0, 2, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 0, 3, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 4, 1, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 4, 2, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 4 /* cobblestone */, 0, 4, 3, 4, var3);
        this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 3, 3, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 4, 1, 1, 4, 3, 3, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 1, 4, 3, 3, 4, 5 /* planks */, 5 /* planks */, false);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 2, 2, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 4, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 1, 1, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 1, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 1, 3, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 2, 3, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 3, 3, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 3, 2, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, 3, 1, 0, var3);
        if (this.getBlockIdAtCurrentPosition(var1, 2, 0, -1, var3) == 0 && this.getBlockIdAtCurrentPosition(var1, 2, -1, -1, var3) != 0) {
            this.placeBlockAtCurrentPosition(var1, 67 /* stairsCobblestone */, this.getMetadataWithOffset(67 /* stairsCobblestone */, 3), 2, 0, -1, var3);
        }

        this.fillWithBlocks(var1, var3, 1, 1, 1, 3, 3, 3, 0, 0, false);
        if (this.isRoofAccessible) {
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 0, 5, 0, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 1, 5, 0, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 2, 5, 0, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 3, 5, 0, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 4, 5, 0, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 0, 5, 4, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 1, 5, 4, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 2, 5, 4, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 3, 5, 4, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 4, 5, 4, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 4, 5, 1, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 4, 5, 2, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 4, 5, 3, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 0, 5, 1, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 0, 5, 2, var3);
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, 0, 5, 3, var3);
        }

        if (this.isRoofAccessible) {
            int var4 = this.getMetadataWithOffset(65 /* ladder */, 3);
            this.placeBlockAtCurrentPosition(var1, 65 /* ladder */, var4, 3, 1, 3, var3);
            this.placeBlockAtCurrentPosition(var1, 65 /* ladder */, var4, 3, 2, 3, var3);
            this.placeBlockAtCurrentPosition(var1, 65 /* ladder */, var4, 3, 3, 3, var3);
            this.placeBlockAtCurrentPosition(var1, 65 /* ladder */, var4, 3, 4, 3, var3);
        }

        this.placeBlockAtCurrentPosition(var1, 50 /* torchWood */, 0, 2, 3, 1, var3);

        for (int var6 = 0; var6 < 5; var6++) {
            for (int var5 = 0; var5 < 5; var5++) {
                this.clearCurrentPositionBlocksUpwards(var1, var5, 6, var6, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 4 /* cobblestone */, 0, var5, -1, var6, var3);
            }
        }

        this.spawnVillagers(var1, var3, 1, 1, 2, 1);
        return true;
    }
}
