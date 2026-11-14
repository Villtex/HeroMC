package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.List;
import java.util.Random;

public class ComponentMineshaftCorridor extends StructureComponent {
    private final boolean hasRails;
    private final boolean hasSpiders;
    private boolean spawnerPlaced;
    private int sectionCount;

    public ComponentMineshaftCorridor(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
        super(par1);
        this.coordBaseMode = par4;
        this.boundingBox = par3StructureBoundingBox;
        this.hasRails = par2Random.nextInt(3) == 0;
        this.hasSpiders = !this.hasRails && par2Random.nextInt(23) == 0;
        if (this.coordBaseMode != 2 && this.coordBaseMode != 0) {
            this.sectionCount = par3StructureBoundingBox.getXSize() / 5;
        } else {
            this.sectionCount = par3StructureBoundingBox.getZSize() / 5;
        }
    }

    public static StructureBoundingBox findValidPlacement(List par0List, Random par1Random, int par2, int par3, int par4, int par5) {
        StructureBoundingBox structureboundingbox = new StructureBoundingBox(par2, par3, par4, par2, par3 + 2, par4);

        int i1;
        for (i1 = par1Random.nextInt(3) + 2; i1 > 0; i1--) {
            int j1 = i1 * 5;
            switch (par5) {
                case 0:
                    structureboundingbox.maxX = par2 + 2;
                    structureboundingbox.maxZ = par4 + (j1 - 1);
                    break;
                case 1:
                    structureboundingbox.minX = par2 - (j1 - 1);
                    structureboundingbox.maxZ = par4 + 2;
                    break;
                case 2:
                    structureboundingbox.maxX = par2 + 2;
                    structureboundingbox.minZ = par4 - (j1 - 1);
                    break;
                case 3:
                    structureboundingbox.maxX = par2 + (j1 - 1);
                    structureboundingbox.maxZ = par4 + 2;
            }

            if (StructureComponent.findIntersecting(par0List, structureboundingbox) == null) {
                break;
            }
        }

        return i1 > 0 ? structureboundingbox : null;
    }

    @Override
    public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
        int i = this.getComponentType();
        int j = par3Random.nextInt(4);
        switch (this.coordBaseMode) {
            case 0:
                if (j <= 1) {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.minX,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.maxZ + 1,
                        this.coordBaseMode,
                        i
                    );
                } else if (j == 2) {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.minX - 1,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.maxZ - 3,
                        1,
                        i
                    );
                } else {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.maxX + 1,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.maxZ - 3,
                        3,
                        i
                    );
                }
                break;
            case 1:
                if (j <= 1) {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.minX - 1,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.minZ,
                        this.coordBaseMode,
                        i
                    );
                } else if (j == 2) {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.minX,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.minZ - 1,
                        2,
                        i
                    );
                } else {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.minX,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.maxZ + 1,
                        0,
                        i
                    );
                }
                break;
            case 2:
                if (j <= 1) {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.minX,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.minZ - 1,
                        this.coordBaseMode,
                        i
                    );
                } else if (j == 2) {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.minX - 1,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.minZ,
                        1,
                        i
                    );
                } else {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.maxX + 1,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.minZ,
                        3,
                        i
                    );
                }
                break;
            case 3:
                if (j <= 1) {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.maxX + 1,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.minZ,
                        this.coordBaseMode,
                        i
                    );
                } else if (j == 2) {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.maxX - 3,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.minZ - 1,
                        2,
                        i
                    );
                } else {
                    StructureMineshaftPieces.getNextMineShaftComponent(
                        par1StructureComponent,
                        par2List,
                        par3Random,
                        this.boundingBox.maxX - 3,
                        this.boundingBox.minY - 1 + par3Random.nextInt(3),
                        this.boundingBox.maxZ + 1,
                        0,
                        i
                    );
                }
        }

        if (i < 8) {
            if (this.coordBaseMode != 2 && this.coordBaseMode != 0) {
                for (int k = this.boundingBox.minX + 3; k + 3 <= this.boundingBox.maxX; k += 5) {
                    int l = par3Random.nextInt(5);
                    if (l == 0) {
                        StructureMineshaftPieces.getNextMineShaftComponent(
                            par1StructureComponent, par2List, par3Random, k, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, i + 1
                        );
                    } else if (l == 1) {
                        StructureMineshaftPieces.getNextMineShaftComponent(
                            par1StructureComponent, par2List, par3Random, k, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, i + 1
                        );
                    }
                }
            } else {
                for (int k = this.boundingBox.minZ + 3; k + 3 <= this.boundingBox.maxZ; k += 5) {
                    int l = par3Random.nextInt(5);
                    if (l == 0) {
                        StructureMineshaftPieces.getNextMineShaftComponent(
                            par1StructureComponent, par2List, par3Random, this.boundingBox.minX - 1, this.boundingBox.minY, k, 1, i + 1
                        );
                    } else if (l == 1) {
                        StructureMineshaftPieces.getNextMineShaftComponent(
                            par1StructureComponent, par2List, par3Random, this.boundingBox.maxX + 1, this.boundingBox.minY, k, 3, i + 1
                        );
                    }
                }
            }
        }
    }

    @Override
    protected boolean generateStructureChestContents(
        OldWorld par1World,
        StructureBoundingBox par2StructureBoundingBox,
        Random par3Random,
        int par4,
        int par5,
        int par6,
        OldLoot.Entry[] par7ArrayOfWeightedRandomChestContent,
        int par8
    ) {
        int i1 = this.getXWithOffset(par4, par6);
        int j1 = this.getYWithOffset(par5);
        int k1 = this.getZWithOffset(par4, par6);
        if (par2StructureBoundingBox.isVecInside(i1, j1, k1) && par1World.getBlockId(i1, j1, k1) == 0) {
            par1World.setBlock(i1, j1, k1, 66 /* rail */, this.getMetadataWithOffset(66 /* rail */, par3Random.nextBoolean() ? 1 : 0));
            OldEntity.MinecartChest entityminecartchest = new OldEntity.MinecartChest(i1 + 0.5F, j1 + 0.5F, k1 + 0.5F, new OldStack[27]);
            OldLoot.generate(par3Random, par7ArrayOfWeightedRandomChestContent, entityminecartchest.slots(), par8);
            par1World.spawnEntity(entityminecartchest);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addComponentParts(OldWorld par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
        if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
        }

        int i = this.sectionCount * 5 - 1;
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 2, 1, i, 0, 0, false);
        this.randomlyFillWithBlocks(par1World, par3StructureBoundingBox, par2Random, 0.8F, 0, 2, 0, 2, 2, i, 0, 0, false);
        if (this.hasSpiders) {
            this.randomlyFillWithBlocks(par1World, par3StructureBoundingBox, par2Random, 0.6F, 0, 0, 0, 2, 1, i, 30 /* web */, 0, false);
        }

        for (int j = 0; j < this.sectionCount; j++) {
            int k = 2 + j * 5;
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 0, k, 0, 1, k, 85 /* fence */, 0, false);
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 2, 0, k, 2, 1, k, 85 /* fence */, 0, false);
            if (par2Random.nextInt(4) == 0) {
                this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 2, k, 0, 2, k, 5 /* planks */, 0, false);
                this.fillWithBlocks(par1World, par3StructureBoundingBox, 2, 2, k, 2, 2, k, 5 /* planks */, 0, false);
            } else {
                this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 2, k, 2, 2, k, 5 /* planks */, 0, false);
            }

            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.1F, 0, 2, k - 1, 30 /* web */, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.1F, 2, 2, k - 1, 30 /* web */, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.1F, 0, 2, k + 1, 30 /* web */, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.1F, 2, 2, k + 1, 30 /* web */, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.05F, 0, 2, k - 2, 30 /* web */, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.05F, 2, 2, k - 2, 30 /* web */, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.05F, 0, 2, k + 2, 30 /* web */, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.05F, 2, 2, k + 2, 30 /* web */, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.05F, 1, 2, k - 1, 50 /* torchWood */, 0);
            this.randomlyPlaceBlock(par1World, par3StructureBoundingBox, par2Random, 0.05F, 1, 2, k + 1, 50 /* torchWood */, 0);
            OldLoot.Category info = OldLoot.MINESHAFT_CORRIDOR;
            if (par2Random.nextInt(100) == 0) {
                this.generateStructureChestContents(par1World, par3StructureBoundingBox, par2Random, 2, 0, k - 1, info.items(par2Random), info.count(par2Random));
            }

            if (par2Random.nextInt(100) == 0) {
                this.generateStructureChestContents(par1World, par3StructureBoundingBox, par2Random, 0, 0, k + 1, info.items(par2Random), info.count(par2Random));
            }

            if (this.hasSpiders && !this.spawnerPlaced) {
                int l = this.getYWithOffset(0);
                int i1 = k - 1 + par2Random.nextInt(3);
                int j1 = this.getXWithOffset(1, i1);
                i1 = this.getZWithOffset(1, i1);
                if (par3StructureBoundingBox.isVecInside(j1, l, i1)) {
                    this.spawnerPlaced = true;
                    par1World.setBlock(j1, l, i1, 52 /* mobSpawner */, 0);
                    par1World.setTile(j1, l, i1, new OldTile.Spawner("CaveSpider"));
                }
            }
        }

        for (int var12 = 0; var12 <= 2; var12++) {
            for (int k = 0; k <= i; k++) {
                int l = this.getBlockIdAtCurrentPosition(par1World, var12, -1, k, par3StructureBoundingBox);
                if (l == 0) {
                    this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, var12, -1, k, par3StructureBoundingBox);
                }
            }
        }

        if (this.hasRails) {
            for (int var13 = 0; var13 <= i; var13++) {
                int k = this.getBlockIdAtCurrentPosition(par1World, 1, -1, var13, par3StructureBoundingBox);
                if (k > 0 && OldBlockInfo.opaqueCube(k)) {
                    this.randomlyPlaceBlock(
                        par1World,
                        par3StructureBoundingBox,
                        par2Random,
                        0.7F,
                        1,
                        0,
                        var13,
                        66 /* rail */,
                        this.getMetadataWithOffset(66 /* rail */, 0)
                    );
                }
            }
        }

        return true;
    }
}
