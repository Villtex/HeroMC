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

public class ComponentVillageWoodHut extends ComponentVillage {
    private int averageGroundLevel = -1;
    private final boolean isTallHouse;
    private final int tablePosition;

    public ComponentVillageWoodHut(ComponentVillageStartPiece var1, int var2, Random var3, StructureBoundingBox var4, int var5) {
        super(var1, var2);
        this.coordBaseMode = var5;
        this.boundingBox = var4;
        this.isTallHouse = var3.nextBoolean();
        this.tablePosition = var3.nextInt(3);
    }

    public static ComponentVillageWoodHut func_74908_a(ComponentVillageStartPiece var0, List var1, Random var2, int var3, int var4, int var5, int var6, int var7) {
        StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 4, 6, 5, var6);
        return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new ComponentVillageWoodHut(var0, var7, var2, var8, var6) : null;
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

        this.fillWithBlocks(var1, var3, 1, 1, 1, 3, 5, 4, 0, 0, false);
        this.fillWithBlocks(var1, var3, 0, 0, 0, 3, 0, 4, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(var1, var3, 1, 0, 1, 2, 0, 3, 3 /* dirt */, 3 /* dirt */, false);
        if (this.isTallHouse) {
            this.fillWithBlocks(var1, var3, 1, 4, 1, 2, 4, 3, 17 /* wood */, 17 /* wood */, false);
        } else {
            this.fillWithBlocks(var1, var3, 1, 5, 1, 2, 5, 3, 17 /* wood */, 17 /* wood */, false);
        }

        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 1, 4, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 2, 4, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 1, 4, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 2, 4, 4, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 0, 4, 1, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 0, 4, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 0, 4, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 3, 4, 1, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 3, 4, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 17 /* wood */, 0, 3, 4, 3, var3);
        this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 3, 0, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(var1, var3, 3, 1, 0, 3, 3, 0, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(var1, var3, 0, 1, 4, 0, 3, 4, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(var1, var3, 3, 1, 4, 3, 3, 4, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 3, 3, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 3, 1, 1, 3, 3, 3, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 1, 0, 2, 3, 0, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(var1, var3, 1, 1, 4, 2, 3, 4, 5 /* planks */, 5 /* planks */, false);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 0, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 102 /* thinGlass */, 0, 3, 2, 2, var3);
        if (this.tablePosition > 0) {
            this.placeBlockAtCurrentPosition(var1, 85 /* fence */, 0, this.tablePosition, 1, 3, var3);
            this.placeBlockAtCurrentPosition(var1, 72 /* pressurePlatePlanks */, 0, this.tablePosition, 2, 3, var3);
        }

        this.placeBlockAtCurrentPosition(var1, 0, 0, 1, 1, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 0, 0, 1, 2, 0, var3);
        this.placeDoorAtCurrentPosition(var1, var3, var2, 1, 1, 0, this.getMetadataWithOffset(64 /* doorWood */, 1));
        if (this.getBlockIdAtCurrentPosition(var1, 1, 0, -1, var3) == 0 && this.getBlockIdAtCurrentPosition(var1, 1, -1, -1, var3) != 0) {
            this.placeBlockAtCurrentPosition(var1, 67 /* stairsCobblestone */, this.getMetadataWithOffset(67 /* stairsCobblestone */, 3), 1, 0, -1, var3);
        }

        for (int var4 = 0; var4 < 5; var4++) {
            for (int var5 = 0; var5 < 4; var5++) {
                this.clearCurrentPositionBlocksUpwards(var1, var5, 6, var4, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 4 /* cobblestone */, 0, var5, -1, var4, var3);
            }
        }

        this.spawnVillagers(var1, var3, 1, 1, 2, 1);
        return true;
    }
}
