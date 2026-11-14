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

public class ComponentStrongholdPortalRoom extends ComponentStronghold {
    private boolean hasSpawner;

    public ComponentStrongholdPortalRoom(int var1, Random var2, StructureBoundingBox var3, int var4) {
        super(var1);
        this.coordBaseMode = var4;
        this.boundingBox = var3;
    }

    @Override
    public void buildComponent(StructureComponent var1, List var2, Random var3) {
        if (var1 != null) {
            ((ComponentStrongholdStairs2)var1).strongholdPortalRoom = this;
        }
    }

    public static ComponentStrongholdPortalRoom findValidPlacement(List var0, Random var1, int var2, int var3, int var4, int var5, int var6) {
        StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -4, -1, 0, 11, 8, 16, var5);
        return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new ComponentStrongholdPortalRoom(var6, var1, var7, var5) : null;
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 10, 7, 15, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.placeDoor(var1, var2, var3, EnumDoor.GRATES, 4, 1, 0);
        int var4 = 6;
        this.fillWithRandomizedBlocks(var1, var3, 1, var4, 1, 1, var4, 14, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 9, var4, 1, 9, var4, 14, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 2, var4, 1, 8, var4, 2, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 2, var4, 14, 8, var4, 14, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 1, 1, 1, 2, 1, 4, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 8, 1, 1, 9, 1, 4, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithBlocks(var1, var3, 1, 1, 1, 1, 1, 3, 10 /* lavaMoving */, 10 /* lavaMoving */, false);
        this.fillWithBlocks(var1, var3, 9, 1, 1, 9, 1, 3, 10 /* lavaMoving */, 10 /* lavaMoving */, false);
        this.fillWithRandomizedBlocks(var1, var3, 3, 1, 8, 7, 1, 12, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithBlocks(var1, var3, 4, 1, 9, 6, 1, 11, 10 /* lavaMoving */, 10 /* lavaMoving */, false);

        for (byte var5 = 3; var5 < 14; var5 += 2) {
            this.fillWithBlocks(var1, var3, 0, 3, var5, 0, 4, var5, 101 /* fenceIron */, 101 /* fenceIron */, false);
            this.fillWithBlocks(var1, var3, 10, 3, var5, 10, 4, var5, 101 /* fenceIron */, 101 /* fenceIron */, false);
        }

        for (byte var14 = 2; var14 < 9; var14 += 2) {
            this.fillWithBlocks(var1, var3, var14, 3, 15, var14, 4, 15, 101 /* fenceIron */, 101 /* fenceIron */, false);
        }

        int var15 = this.getMetadataWithOffset(109 /* stairsStoneBrick */, 3);
        this.fillWithRandomizedBlocks(var1, var3, 4, 1, 5, 6, 1, 7, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 4, 2, 6, 6, 2, 7, false, var2, StructureStrongholdPieces.getStrongholdStones());
        this.fillWithRandomizedBlocks(var1, var3, 4, 3, 7, 6, 3, 7, false, var2, StructureStrongholdPieces.getStrongholdStones());

        for (int var6 = 4; var6 <= 6; var6++) {
            this.placeBlockAtCurrentPosition(var1, 109 /* stairsStoneBrick */, var15, var6, 1, 4, var3);
            this.placeBlockAtCurrentPosition(var1, 109 /* stairsStoneBrick */, var15, var6, 2, 5, var3);
            this.placeBlockAtCurrentPosition(var1, 109 /* stairsStoneBrick */, var15, var6, 3, 6, var3);
        }

        byte var16 = 2;
        byte var7 = 0;
        byte var8 = 3;
        byte var9 = 1;
        switch (this.coordBaseMode) {
            case 0:
                var16 = 0;
                var7 = 2;
                break;
            case 1:
                var16 = 1;
                var7 = 3;
                var8 = 0;
                var9 = 2;
            case 2:
            default:
                break;
            case 3:
                var16 = 3;
                var7 = 1;
                var8 = 0;
                var9 = 2;
        }

        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var16 + (var2.nextFloat() > 0.9F ? 4 : 0), 4, 3, 8, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var16 + (var2.nextFloat() > 0.9F ? 4 : 0), 5, 3, 8, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var16 + (var2.nextFloat() > 0.9F ? 4 : 0), 6, 3, 8, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var7 + (var2.nextFloat() > 0.9F ? 4 : 0), 4, 3, 12, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var7 + (var2.nextFloat() > 0.9F ? 4 : 0), 5, 3, 12, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var7 + (var2.nextFloat() > 0.9F ? 4 : 0), 6, 3, 12, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var8 + (var2.nextFloat() > 0.9F ? 4 : 0), 3, 3, 9, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var8 + (var2.nextFloat() > 0.9F ? 4 : 0), 3, 3, 10, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var8 + (var2.nextFloat() > 0.9F ? 4 : 0), 3, 3, 11, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var9 + (var2.nextFloat() > 0.9F ? 4 : 0), 7, 3, 9, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var9 + (var2.nextFloat() > 0.9F ? 4 : 0), 7, 3, 10, var3);
        this.placeBlockAtCurrentPosition(var1, 120 /* endPortalFrame */, var9 + (var2.nextFloat() > 0.9F ? 4 : 0), 7, 3, 11, var3);
        if (!this.hasSpawner) {
            var4 = this.getYWithOffset(3);
            int var10 = this.getXWithOffset(5, 6);
            int var11 = this.getZWithOffset(5, 6);
            if (var3.isVecInside(var10, var4, var11)) {
                this.hasSpawner = true;
                var1.setBlock(var10, var4, var11, 52 /* mobSpawner */, 0);
                var1.setTile(var10, var4, var11, new OldTile.Spawner("Silverfish"));
            }
        }

        return true;
    }
}
