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

public class ComponentVillageHouse2 extends ComponentVillage {
    private int averageGroundLevel = -1;
    private boolean hasMadeChest;

    public ComponentVillageHouse2(
        ComponentVillageStartPiece par1ComponentVillageStartPiece, int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5
    ) {
        super(par1ComponentVillageStartPiece, par2);
        this.coordBaseMode = par5;
        this.boundingBox = par4StructureBoundingBox;
    }

    public static ComponentVillageHouse2 func_74915_a(
        ComponentVillageStartPiece par0ComponentVillageStartPiece, List par1List, Random par2Random, int par3, int par4, int par5, int par6, int par7
    ) {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 10, 6, 7, par6);
        return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par1List, structureboundingbox) == null
            ? new ComponentVillageHouse2(par0ComponentVillageStartPiece, par7, par2Random, structureboundingbox, par6)
            : null;
    }

    @Override
    public boolean addComponentParts(OldWorld par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
        if (this.averageGroundLevel < 0) {
            this.averageGroundLevel = this.getAverageGroundLevel(par1World, par3StructureBoundingBox);
            if (this.averageGroundLevel < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 6 - 1, 0);
        }

        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 0, 9, 4, 6, 0, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 9, 0, 6, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 4, 0, 9, 4, 6, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 5, 0, 9, 5, 6, 44 /* stoneSingleSlab */, 44 /* stoneSingleSlab */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 5, 1, 8, 5, 5, 0, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 1, 0, 2, 3, 0, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 0, 0, 4, 0, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 3, 1, 0, 3, 4, 0, 17 /* wood */, 17 /* wood */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 6, 0, 4, 6, 17 /* wood */, 17 /* wood */, false);
        this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 3, 3, 1, par3StructureBoundingBox);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 3, 1, 2, 3, 3, 2, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 3, 5, 3, 3, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 1, 0, 3, 5, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 1, 6, 5, 3, 6, 5 /* planks */, 5 /* planks */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 5, 1, 0, 5, 3, 0, 85 /* fence */, 85 /* fence */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 9, 1, 0, 9, 3, 0, 85 /* fence */, 85 /* fence */, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 6, 1, 4, 9, 4, 6, 4 /* cobblestone */, 4 /* cobblestone */, false);
        this.placeBlockAtCurrentPosition(par1World, 10 /* lavaMoving */, 0, 7, 1, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 10 /* lavaMoving */, 0, 8, 1, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 101 /* fenceIron */, 0, 9, 2, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 101 /* fenceIron */, 0, 9, 2, 4, par3StructureBoundingBox);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 7, 2, 4, 8, 2, 5, 0, 0, false);
        this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 6, 1, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 61 /* furnaceIdle */, 0, 6, 2, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 61 /* furnaceIdle */, 0, 6, 3, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 43 /* stoneDoubleSlab */, 0, 8, 1, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 102 /* thinGlass */, 0, 0, 2, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 102 /* thinGlass */, 0, 0, 2, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 102 /* thinGlass */, 0, 2, 2, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 102 /* thinGlass */, 0, 4, 2, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, 2, 1, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 72 /* pressurePlatePlanks */, 0, 2, 2, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 1, 1, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(
            par1World, 53 /* stairsWoodOak */, this.getMetadataWithOffset(53 /* stairsWoodOak */, 3), 2, 1, 5, par3StructureBoundingBox
        );
        this.placeBlockAtCurrentPosition(
            par1World, 53 /* stairsWoodOak */, this.getMetadataWithOffset(53 /* stairsWoodOak */, 1), 1, 1, 4, par3StructureBoundingBox
        );
        if (!this.hasMadeChest) {
            int i = this.getYWithOffset(1);
            int j = this.getXWithOffset(5, 5);
            int k = this.getZWithOffset(5, 5);
            if (par3StructureBoundingBox.isVecInside(j, i, k)) {
                this.hasMadeChest = true;
                this.generateStructureChestContents(
                    par1World,
                    par3StructureBoundingBox,
                    par2Random,
                    5,
                    1,
                    5,
                    OldLoot.VILLAGE_BLACKSMITH.items(par2Random),
                    OldLoot.VILLAGE_BLACKSMITH.count(par2Random)
                );
            }
        }

        for (int i = 6; i <= 8; i++) {
            if (this.getBlockIdAtCurrentPosition(par1World, i, 0, -1, par3StructureBoundingBox) == 0 && this.getBlockIdAtCurrentPosition(par1World, i, -1, -1, par3StructureBoundingBox) != 0) {
                this.placeBlockAtCurrentPosition(
                    par1World, 67 /* stairsCobblestone */, this.getMetadataWithOffset(67 /* stairsCobblestone */, 3), i, 0, -1, par3StructureBoundingBox
                );
            }
        }

        for (int var8 = 0; var8 < 7; var8++) {
            for (int j = 0; j < 10; j++) {
                this.clearCurrentPositionBlocksUpwards(par1World, j, 6, var8, par3StructureBoundingBox);
                this.fillCurrentPositionBlocksDownwards(par1World, 4 /* cobblestone */, 0, j, -1, var8, par3StructureBoundingBox);
            }
        }

        this.spawnVillagers(par1World, par3StructureBoundingBox, 7, 1, 1, 1);
        return true;
    }

    @Override
    protected int getVillagerType(int par1) {
        return 3;
    }
}
