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

public class ComponentStrongholdChestCorridor extends ComponentStronghold {
    private final EnumDoor doorType;
    private boolean hasMadeChest;

    public ComponentStrongholdChestCorridor(int par1, Random par2Random, StructureBoundingBox par3StructureBoundingBox, int par4) {
        super(par1);
        this.coordBaseMode = par4;
        this.doorType = this.getRandomDoor(par2Random);
        this.boundingBox = par3StructureBoundingBox;
    }

    @Override
    public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
        this.getNextComponentNormal((ComponentStrongholdStairs2)par1StructureComponent, par2List, par3Random, 1, 1);
    }

    public static ComponentStrongholdChestCorridor findValidPlacement(List par0List, Random par1Random, int par2, int par3, int par4, int par5, int par6) {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(par2, par3, par4, -1, -1, 0, 5, 5, 7, par5);
        return canStrongholdGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(par0List, structureboundingbox) == null
            ? new ComponentStrongholdChestCorridor(par6, par1Random, structureboundingbox, par5)
            : null;
    }

    @Override
    public boolean addComponentParts(OldWorld par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
        if (this.isLiquidInStructureBoundingBox(par1World, par3StructureBoundingBox)) {
            return false;
        }

        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 4, 4, 6, true, par2Random, StructureStrongholdPieces.getStrongholdStones());
        this.placeDoor(par1World, par2Random, par3StructureBoundingBox, this.doorType, 1, 1, 0);
        this.placeDoor(par1World, par2Random, par3StructureBoundingBox, EnumDoor.OPENING, 1, 1, 6);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 3, 1, 2, 3, 1, 4, 98 /* stoneBrick */, 98 /* stoneBrick */, false);
        this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 5, 3, 1, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 5, 3, 1, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 5, 3, 2, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 5, 3, 2, 4, par3StructureBoundingBox);

        for (int i = 2; i <= 4; i++) {
            this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 5, 2, 1, i, par3StructureBoundingBox);
        }

        if (!this.hasMadeChest) {
            int var7 = this.getYWithOffset(2);
            int j = this.getXWithOffset(3, 3);
            int k = this.getZWithOffset(3, 3);
            if (par3StructureBoundingBox.isVecInside(j, var7, k)) {
                this.hasMadeChest = true;
                this.generateStructureChestContents(
                    par1World,
                    par3StructureBoundingBox,
                    par2Random,
                    3,
                    2,
                    3,
                    OldLoot.STRONGHOLD_CORRIDOR.items(par2Random),
                    OldLoot.STRONGHOLD_CORRIDOR.count(par2Random)
                );
            }
        }

        return true;
    }
}
