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

public class ComponentStrongholdPrison extends ComponentStronghold {
    protected final EnumDoor doorType;

    public ComponentStrongholdPrison(int var1, Random var2, StructureBoundingBox var3, int var4) {
        super(var1);
        this.coordBaseMode = var4;
        this.doorType = this.getRandomDoor(var2);
        this.boundingBox = var3;
    }

    @Override
    public void buildComponent(StructureComponent var1, List var2, Random var3) {
        this.getNextComponentNormal((ComponentStrongholdStairs2)var1, var2, var3, 1, 1);
    }

    public static ComponentStrongholdPrison findValidPlacement(List var0, Random var1, int var2, int var3, int var4, int var5, int var6) {
        StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -1, 0, 9, 5, 11, var5);
        return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new ComponentStrongholdPrison(var6, var1, var7, var5) : null;
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
        }

        this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 8, 4, 10, true, var2, StructureStrongholdPieces.getStrongholdStones());
        this.placeDoor(var1, var2, var3, this.doorType, 1, 1, 0);
        this.fillWithBlocks(var1, var3, 1, 1, 10, 3, 3, 10, 0, 0, false);
        this.fillWithRandomizedBlocks(var1, var3, 4, 1, 1, 4, 3, 1, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 4, 1, 3, 4, 3, 3, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 4, 1, 7, 4, 3, 7, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 4, 1, 9, 4, 3, 9, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithBlocks(var1, var3, 4, 1, 4, 4, 3, 6, 101 /* fenceIron */, 101 /* fenceIron */, false);
        this.fillWithBlocks(var1, var3, 5, 1, 5, 7, 3, 5, 101 /* fenceIron */, 101 /* fenceIron */, false);
        this.placeBlockAtCurrentPosition(var1, 101 /* fenceIron */, 0, 4, 3, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 101 /* fenceIron */, 0, 4, 3, 8, var3);
        this.placeBlockAtCurrentPosition(var1, 71 /* doorIron */, this.getMetadataWithOffset(71 /* doorIron */, 3), 4, 1, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 71 /* doorIron */, this.getMetadataWithOffset(71 /* doorIron */, 3) + 8, 4, 2, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 71 /* doorIron */, this.getMetadataWithOffset(71 /* doorIron */, 3), 4, 1, 8, var3);
        this.placeBlockAtCurrentPosition(var1, 71 /* doorIron */, this.getMetadataWithOffset(71 /* doorIron */, 3) + 8, 4, 2, 8, var3);
        return true;
    }
}
