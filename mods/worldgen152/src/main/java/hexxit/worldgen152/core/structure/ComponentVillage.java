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

public abstract class ComponentVillage extends StructureComponent {
    private int villagersSpawned;
    protected ComponentVillageStartPiece startPiece;

    protected ComponentVillage(ComponentVillageStartPiece par1ComponentVillageStartPiece, int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    protected StructureComponent getNextComponentNN(ComponentVillageStartPiece par1ComponentVillageStartPiece, List par2List, Random par3Random, int par4, int par5) {
        switch (this.coordBaseMode) {
            case 0:
                return StructureVillagePieces.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX - 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    1,
                    this.getComponentType()
                );
            case 1:
                return StructureVillagePieces.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ - 1,
                    2,
                    this.getComponentType()
                );
            case 2:
                return StructureVillagePieces.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX - 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    1,
                    this.getComponentType()
                );
            case 3:
                return StructureVillagePieces.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ - 1,
                    2,
                    this.getComponentType()
                );
            default:
                return null;
        }
    }

    protected StructureComponent getNextComponentPP(ComponentVillageStartPiece par1ComponentVillageStartPiece, List par2List, Random par3Random, int par4, int par5) {
        switch (this.coordBaseMode) {
            case 0:
                return StructureVillagePieces.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.maxX + 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    3,
                    this.getComponentType()
                );
            case 1:
                return StructureVillagePieces.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.maxZ + 1,
                    0,
                    this.getComponentType()
                );
            case 2:
                return StructureVillagePieces.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.maxX + 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    3,
                    this.getComponentType()
                );
            case 3:
                return StructureVillagePieces.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.maxZ + 1,
                    0,
                    this.getComponentType()
                );
            default:
                return null;
        }
    }

    protected int getAverageGroundLevel(OldWorld par1World, StructureBoundingBox par2StructureBoundingBox) {
        int i = 0;
        int j = 0;

        for (int k = this.boundingBox.minZ; k <= this.boundingBox.maxZ; k++) {
            for (int l = this.boundingBox.minX; l <= this.boundingBox.maxX; l++) {
                if (par2StructureBoundingBox.isVecInside(l, 64, k)) {
                    i += Math.max(par1World.getTopSolidOrLiquidBlock(l, k), 64);
                    j++;
                }
            }
        }

        return j == 0 ? -1 : i / j;
    }

    protected static boolean canVillageGoDeeper(StructureBoundingBox par0StructureBoundingBox) {
        return par0StructureBoundingBox != null && par0StructureBoundingBox.minY > 10;
    }

    protected void spawnVillagers(OldWorld par1World, StructureBoundingBox par2StructureBoundingBox, int par3, int par4, int par5, int par6) {
        if (this.villagersSpawned < par6) {
            for (int i1 = this.villagersSpawned; i1 < par6; i1++) {
                int j1 = this.getXWithOffset(par3 + i1, par5);
                int k1 = this.getYWithOffset(par4);
                int l1 = this.getZWithOffset(par3 + i1, par5);
                if (!par2StructureBoundingBox.isVecInside(j1, k1, l1)) {
                    break;
                }

                this.villagersSpawned++;
                par1World.spawnEntity(new OldEntity.Villager(j1 + 0.5, k1, l1 + 0.5, this.getVillagerType(i1)));
            }
        }
    }

    protected int getVillagerType(int par1) {
        return 0;
    }

    protected int getBiomeSpecificBlock(int par1, int par2) {
        if (OldStructures.villageBlocks != null) {
            int replacement = OldStructures.villageBlocks.blockId(this.startPiece.biome, par1, par2);
            if (replacement >= 0) {
                return replacement;
            }
        }

        if (this.startPiece.inDesert) {
            if (par1 == 17 /* wood */) {
                return 24 /* sandStone */;
            }

            if (par1 == 4 /* cobblestone */) {
                return 24 /* sandStone */;
            }

            if (par1 == 5 /* planks */) {
                return 24 /* sandStone */;
            }

            if (par1 == 53 /* stairsWoodOak */) {
                return 128 /* stairsSandStone */;
            }

            if (par1 == 67 /* stairsCobblestone */) {
                return 128 /* stairsSandStone */;
            }

            if (par1 == 13 /* gravel */) {
                return 24 /* sandStone */;
            }
        }

        return par1;
    }

    protected int getBiomeSpecificBlockMetadata(int par1, int par2) {
        if (OldStructures.villageBlocks != null) {
            int replacement = OldStructures.villageBlocks.metadata(this.startPiece.biome, par1, par2);
            if (replacement >= 0) {
                return replacement;
            }
        }

        if (this.startPiece.inDesert) {
            if (par1 == 17 /* wood */) {
                return 0;
            }

            if (par1 == 4 /* cobblestone */) {
                return 0;
            }

            if (par1 == 5 /* planks */) {
                return 2;
            }
        }

        return par2;
    }

    @Override
    protected void placeBlockAtCurrentPosition(OldWorld par1World, int par2, int par3, int par4, int par5, int par6, StructureBoundingBox par7StructureBoundingBox) {
        int j1 = this.getBiomeSpecificBlock(par2, par3);
        int k1 = this.getBiomeSpecificBlockMetadata(par2, par3);
        super.placeBlockAtCurrentPosition(par1World, j1, k1, par4, par5, par6, par7StructureBoundingBox);
    }

    @Override
    protected void fillWithBlocks(
        OldWorld par1World,
        StructureBoundingBox par2StructureBoundingBox,
        int par3,
        int par4,
        int par5,
        int par6,
        int par7,
        int par8,
        int par9,
        int par10,
        boolean par11
    ) {
        int i2 = this.getBiomeSpecificBlock(par9, 0);
        int j2 = this.getBiomeSpecificBlockMetadata(par9, 0);
        int k2 = this.getBiomeSpecificBlock(par10, 0);
        int l2 = this.getBiomeSpecificBlockMetadata(par10, 0);
        super.fillWithMetadataBlocks(par1World, par2StructureBoundingBox, par3, par4, par5, par6, par7, par8, i2, j2, k2, l2, par11);
    }

    @Override
    protected void fillCurrentPositionBlocksDownwards(OldWorld par1World, int par2, int par3, int par4, int par5, int par6, StructureBoundingBox par7StructureBoundingBox) {
        int j1 = this.getBiomeSpecificBlock(par2, par3);
        int k1 = this.getBiomeSpecificBlockMetadata(par2, par3);
        super.fillCurrentPositionBlocksDownwards(par1World, j1, k1, par4, par5, par6, par7StructureBoundingBox);
    }
}
