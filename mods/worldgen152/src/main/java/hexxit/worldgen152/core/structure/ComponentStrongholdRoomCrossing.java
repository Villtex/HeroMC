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

public class ComponentStrongholdRoomCrossing extends ComponentStronghold {
    protected final EnumDoor doorType;
    protected final int roomType;

    public ComponentStrongholdRoomCrossing(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
        super(par1);
        this.coordBaseMode = par4;
        this.doorType = this.getRandomDoor(par2Random);
        this.boundingBox = par3StructureBoundingBox;
        this.roomType = par2Random.nextInt(5);
    }

    @Override
    public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
        this.getNextComponentNormal((ComponentStrongholdStairs2)par1StructureComponent, par2List, par3Random, 4, 1);
        this.getNextComponentX((ComponentStrongholdStairs2)par1StructureComponent, par2List, par3Random, 1, 4);
        this.getNextComponentZ((ComponentStrongholdStairs2)par1StructureComponent, par2List, par3Random, 1, 4);
    }

    public static ComponentStrongholdRoomCrossing findValidPlacement(List par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6) {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -4, -1, 0, 11, 7, 11, par5);
        return canStrongholdGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new ComponentStrongholdRoomCrossing(par6, par1Random, structureboundingbox, par5)
            : null;
    }

    @Override
    public boolean addComponentParts(OldWorld par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
        if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
        }

        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 10, 6, 10, true, par2Random, StructureStrongholdPieces.getStrongholdStones());
        this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.doorType, 4, 1, 0);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 10, 6, 3, 10, 0, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 1, 4, 0, 3, 6, 0, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 10, 1, 4, 10, 3, 6, 0, 0, false);
        switch (this.roomType) {
            case 0:
                this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 5, 1, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 5, 2, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 5, 3, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, 4, 3, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, 6, 3, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, 5, 3, 4, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, 5, 3, 6, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 0, 4, 1, 4, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 0, 4, 1, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 0, 4, 1, 6, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 0, 6, 1, 4, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 0, 6, 1, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 0, 6, 1, 6, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 0, 5, 1, 4, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 0, 5, 1, 6, par3StructureBoundingBox);
                break;
            case 1:
                for (int i = 0; i < 5; i++) {
                    this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 3, 1, 3 + i, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 7, 1, 3 + i, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 3 + i, 1, 3, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 3 + i, 1, 7, par3StructureBoundingBox);
                }

                this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 5, 1, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 5, 2, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 0, 5, 3, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 8 /* waterMoving */, 0, 5, 4, 5, par3StructureBoundingBox);
                break;
            case 2:
                for (int i = 1; i <= 9; i++) {
                    this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 1, 3, i, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 9, 3, i, par3StructureBoundingBox);
                }

                for (int var5 = 1; var5 <= 9; var5++) {
                    this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, var5, 3, 1, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, var5, 3, 9, par3StructureBoundingBox);
                }

                this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 5, 1, 4, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 5, 1, 6, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 5, 3, 4, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 5, 3, 6, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 4, 1, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 6, 1, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 4, 3, 5, par3StructureBoundingBox);
                this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 6, 3, 5, par3StructureBoundingBox);

                for (int var6 = 1; var6 <= 3; var6++) {
                    this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 4, var6, 4, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 6, var6, 4, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 4, var6, 6, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 4 /* cobblestone */, 0, 6, var6, 6, par3StructureBoundingBox);
                }

                this.placeBlockAtCurrentPosition(par1World, 50 /* torchWood */, 0, 5, 3, 5, par3StructureBoundingBox);

                for (int var7 = 2; var7 <= 8; var7++) {
                    this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 2, 3, var7, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 3, 3, var7, par3StructureBoundingBox);
                    if (var7 <= 3 || var7 >= 7) {
                        this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 4, 3, var7, par3StructureBoundingBox);
                        this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 5, 3, var7, par3StructureBoundingBox);
                        this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 6, 3, var7, par3StructureBoundingBox);
                    }

                    this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 7, 3, var7, par3StructureBoundingBox);
                    this.placeBlockAtCurrentPosition(par1World, 5 /* planks */, 0, 8, 3, var7, par3StructureBoundingBox);
                }

                this.placeBlockAtCurrentPosition(
                    par1World, 65 /* ladder */, this.getMetadataWithOffset(65 /* ladder */, 4), 9, 1, 3, par3StructureBoundingBox
                );
                this.placeBlockAtCurrentPosition(
                    par1World, 65 /* ladder */, this.getMetadataWithOffset(65 /* ladder */, 4), 9, 2, 3, par3StructureBoundingBox
                );
                this.placeBlockAtCurrentPosition(
                    par1World, 65 /* ladder */, this.getMetadataWithOffset(65 /* ladder */, 4), 9, 3, 3, par3StructureBoundingBox
                );
                this.generateStructureChestContents(
                    par1World,
                    par3StructureBoundingBox,
                    par2Random,
                    3,
                    4,
                    8,
                    OldLoot.STRONGHOLD_CROSSING.items(par2Random),
                    OldLoot.STRONGHOLD_CROSSING.count(par2Random)
                );
        }

        return true;
    }
}
