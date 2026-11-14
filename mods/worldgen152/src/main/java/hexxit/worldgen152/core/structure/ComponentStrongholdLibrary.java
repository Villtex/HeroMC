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

public class ComponentStrongholdLibrary extends ComponentStronghold {
    protected final EnumDoor doorType;
    private final boolean isLargeRoom;

    public ComponentStrongholdLibrary(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
        super(par1);
        this.coordBaseMode = par4;
        this.doorType = this.getRandomDoor(par2Random);
        this.boundingBox = par3StructureBoundingBox;
        this.isLargeRoom = par3StructureBoundingBox.getYSize() > 6;
    }

    public static ComponentStrongholdLibrary findValidPlacement(List par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6) {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -4, -1, 0, 14, 11, 15, par5);
        if (!canStrongholdGoDeeper(structureboundingbox) || StructureComponent.findIntersecting(par0List, structureboundingbox) != null) {
            structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -4, -1, 0, 14, 6, 15, par5);
            if (!canStrongholdGoDeeper(structureboundingbox) || StructureComponent.findIntersecting(par0List, structureboundingbox) != null) {
                return null;
            }
        }

        return new ComponentStrongholdLibrary(par6, par1Random, structureboundingbox, par5);
    }

    @Override
    public boolean addComponentParts(OldWorld par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
        if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
        }

        byte b0 = 11;
        if (!this.isLargeRoom) {
            b0 = 6;
        }

        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 13, b0 - 1, 14, true, par2Random, StructureStrongholdPieces.getStrongholdStones());
        this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.doorType, 4, 1, 0);
        this.randomlyFillWithBlocks(
            par1World,
            par3StructureBoundingBox,
            par2Random,
            0.07F,
            2,
            1,
            1,
            11,
            4,
            13,
            30 /* web */,
            30 /* web */,
            false
        );

        for (int i = 1; i <= 13; i++) {
            if ((i - 1) % 4 == 0) {
                this.fillWithBlocks(
                    par1World, par3StructureBoundingBox, 1, 1, i, 1, 4, i, 5 /* planks */, 5 /* planks */, false
                );
                this.fillWithBlocks(
                    par1World, par3StructureBoundingBox, 12, 1, i, 12, 4, i, 5 /* planks */, 5 /* planks */, false
                );
                this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, 2, 3, i, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, 11, 3, i, par3StructureBoundingBox);
                if (this.isLargeRoom) {
                    this.fillWithBlocks(
                        par1World, par3StructureBoundingBox, 1, 6, i, 1, 9, i, 5 /* planks */, 5 /* planks */, false
                    );
                    this.fillWithBlocks(
                        par1World, par3StructureBoundingBox, 12, 6, i, 12, 9, i, 5 /* planks */, 5 /* planks */, false
                    );
                }
            } else {
                this.fillWithBlocks(
                    par1World, par3StructureBoundingBox, 1, 1, i, 1, 4, i, 47 /* bookShelf */, 47 /* bookShelf */, false
                );
                this.fillWithBlocks(
                    par1World, par3StructureBoundingBox, 12, 1, i, 12, 4, i, 47 /* bookShelf */, 47 /* bookShelf */, false
                );
                if (this.isLargeRoom) {
                    this.fillWithBlocks(
                        par1World, par3StructureBoundingBox, 1, 6, i, 1, 9, i, 47 /* bookShelf */, 47 /* bookShelf */, false
                    );
                    this.fillWithBlocks(
                        par1World, par3StructureBoundingBox, 12, 6, i, 12, 9, i, 47 /* bookShelf */, 47 /* bookShelf */, false
                    );
                }
            }
        }

        for (int var8 = 3; var8 < 12; var8 += 2) {
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 3, 1, var8, 4, 3, var8, 47 /* bookShelf */, 47 /* bookShelf */, false
            );
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 6, 1, var8, 7, 3, var8, 47 /* bookShelf */, 47 /* bookShelf */, false
            );
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 9, 1, var8, 10, 3, var8, 47 /* bookShelf */, 47 /* bookShelf */, false
            );
        }

        if (this.isLargeRoom) {
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 1, 5, 1, 3, 5, 13, 5 /* planks */, 5 /* planks */, false
            );
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 10, 5, 1, 12, 5, 13, 5 /* planks */, 5 /* planks */, false
            );
            this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 5, 1, 9, 5, 2, 5 /* planks */, 5 /* planks */, false);
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 4, 5, 12, 9, 5, 13, 5 /* planks */, 5 /* planks */, false
            );
            this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 9, 5, 11, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 8, 5, 11, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 9, 5, 10, par3StructureBoundingBox);
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 3, 6, 2, 3, 6, 12, 85 /* fence */, 85 /* fence */, false
            );
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 10, 6, 2, 10, 6, 10, 85 /* fence */, 85 /* fence */, false
            );
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 4, 6, 2, 9, 6, 2, 85 /* fence */, 85 /* fence */, false
            );
            this.fillWithBlocks(
                par1World, par3StructureBoundingBox, 4, 6, 12, 8, 6, 12, 85 /* fence */, 85 /* fence */, false
            );
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, 9, 6, 11, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, 8, 6, 11, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, 9, 6, 10, par3StructureBoundingBox);
            int var9 = this.getMetadataWithOffset(65 /* ladder */, 3);
            this.placeBlockAtCurrentPosition(par1World, 65 /* ladder */, var9, 10, 1, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 65 /* ladder */, var9, 10, 2, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 65 /* ladder */, var9, 10, 3, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 65 /* ladder */, var9, 10, 4, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 65 /* ladder */, var9, 10, 5, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 65 /* ladder */, var9, 10, 6, 13, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 65 /* ladder */, var9, 10, 7, 13, par3StructureBoundingBox);
            byte b1 = 7;
            byte b2 = 7;
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1 - 1, 9, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1, 9, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1 - 1, 8, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1, 8, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1 - 1, 7, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1, 7, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1 - 2, 7, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1 + 1, 7, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1 - 1, 7, b2 - 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1 - 1, 7, b2 + 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1, 7, b2 - 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 85 /* fence */, 0, b1, 7, b2 + 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, b1 - 2, 8, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, b1 + 1, 8, b2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, b1 - 1, 8, b2 - 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, b1 - 1, 8, b2 + 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, b1, 8, b2 - 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, b1, 8, b2 + 1, par3StructureBoundingBox);
        }

        OldLoot.Category info = OldLoot.STRONGHOLD_LIBRARY;
        this.generateStructureChestContents(par1World, par3StructureBoundingBox, par2Random, 3, 3, 5, info.items(par2Random), info.count(par2Random));
        if (this.isLargeRoom) {
            this.placeBlockAtCurrentPosition(par1World, 0, 0, 12, 9, 1, par3StructureBoundingBox);
            this.generateStructureChestContents(par1World, par3StructureBoundingBox, par2Random, 12, 8, 1, info.items(par2Random), info.count(par2Random));
        }

        return true;
    }
}
