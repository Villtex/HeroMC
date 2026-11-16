package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.List;
import java.util.Random;

public class ComponentNetherBridgeNetherStalkRoom extends ComponentNetherBridgePiece {
    public ComponentNetherBridgeNetherStalkRoom(int var1, Random var2, StructureBoundingBox var3, int var4) {
        super(var1);
        this.coordBaseMode = var4;
        this.boundingBox = var3;
    }

    @Override
    public void buildComponent(StructureComponent var1, List var2, Random var3) {
        this.getNextComponentNormal((ComponentNetherBridgeStartPiece)var1, var2, var3, 5, 3, true);
        this.getNextComponentNormal((ComponentNetherBridgeStartPiece)var1, var2, var3, 5, 11, true);
    }

    public static ComponentNetherBridgeNetherStalkRoom createValidComponent(List var0, Random var1, int var2, int var3, int var4, int var5, int var6) {
        StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -5, -3, 0, 13, 14, 13, var5);
        return isAboveGround(var7) && StructureComponent.findIntersecting(var0, var7) == null
            ? new ComponentNetherBridgeNetherStalkRoom(var6, var1, var7, var5)
            : null;
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        this.fillWithBlocks(var1, var3, 0, 3, 0, 12, 4, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 5, 0, 12, 13, 12, 0, 0, false);
        this.fillWithBlocks(var1, var3, 0, 5, 0, 1, 12, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 11, 5, 0, 12, 12, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 2, 5, 11, 4, 12, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 8, 5, 11, 10, 12, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 5, 9, 11, 7, 12, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 2, 5, 0, 4, 12, 1, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 8, 5, 0, 10, 12, 1, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 5, 9, 0, 7, 12, 1, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 2, 11, 2, 10, 12, 10, 112 /* netherBrick */, 112 /* netherBrick */, false);

        for (byte var4 = 1; var4 <= 11; var4 += 2) {
            this.fillWithBlocks(var1, var3, var4, 10, 0, var4, 11, 0, 113 /* netherFence */, 113 /* netherFence */, false);
            this.fillWithBlocks(var1, var3, var4, 10, 12, var4, 11, 12, 113 /* netherFence */, 113 /* netherFence */, false);
            this.fillWithBlocks(var1, var3, 0, 10, var4, 0, 11, var4, 113 /* netherFence */, 113 /* netherFence */, false);
            this.fillWithBlocks(var1, var3, 12, 10, var4, 12, 11, var4, 113 /* netherFence */, 113 /* netherFence */, false);
            this.placeBlockAtCurrentPosition(var1, 112 /* netherBrick */, 0, var4, 13, 0, var3);
            this.placeBlockAtCurrentPosition(var1, 112 /* netherBrick */, 0, var4, 13, 12, var3);
            this.placeBlockAtCurrentPosition(var1, 112 /* netherBrick */, 0, 0, 13, var4, var3);
            this.placeBlockAtCurrentPosition(var1, 112 /* netherBrick */, 0, 12, 13, var4, var3);
            this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, var4 + 1, 13, 0, var3);
            this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, var4 + 1, 13, 12, var3);
            this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, 0, 13, var4 + 1, var3);
            this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, 12, 13, var4 + 1, var3);
        }

        this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, 0, 13, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, 0, 13, 12, var3);
        this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, 0, 13, 0, var3);
        this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, 12, 13, 0, var3);

        for (byte var9 = 3; var9 <= 9; var9 += 2) {
            this.fillWithBlocks(var1, var3, 1, 7, var9, 1, 8, var9, 113 /* netherFence */, 113 /* netherFence */, false);
            this.fillWithBlocks(var1, var3, 11, 7, var9, 11, 8, var9, 113 /* netherFence */, 113 /* netherFence */, false);
        }

        int var10 = this.getMetadataWithOffset(114 /* stairsNetherBrick */, 3);

        for (int var5 = 0; var5 <= 6; var5++) {
            int var6 = var5 + 4;

            for (int var7 = 5; var7 <= 7; var7++) {
                this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var10, var7, 5 + var5, var6, var3);
            }

            if (var6 >= 5 && var6 <= 8) {
                this.fillWithBlocks(var1, var3, 5, 5, var6, 7, var5 + 4, var6, 112 /* netherBrick */, 112 /* netherBrick */, false);
            } else if (var6 >= 9 && var6 <= 10) {
                this.fillWithBlocks(var1, var3, 5, 8, var6, 7, var5 + 4, var6, 112 /* netherBrick */, 112 /* netherBrick */, false);
            }

            if (var5 >= 1) {
                this.fillWithBlocks(var1, var3, 5, 6 + var5, var6, 7, 9 + var5, var6, 0, 0, false);
            }
        }

        for (int var11 = 5; var11 <= 7; var11++) {
            this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var10, var11, 12, 11, var3);
        }

        this.fillWithBlocks(var1, var3, 5, 6, 7, 5, 7, 7, 113 /* netherFence */, 113 /* netherFence */, false);
        this.fillWithBlocks(var1, var3, 7, 6, 7, 7, 7, 7, 113 /* netherFence */, 113 /* netherFence */, false);
        this.fillWithBlocks(var1, var3, 5, 13, 12, 7, 13, 12, 0, 0, false);
        this.fillWithBlocks(var1, var3, 2, 5, 2, 3, 5, 3, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 2, 5, 9, 3, 5, 10, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 2, 5, 4, 2, 5, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 9, 5, 2, 10, 5, 3, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 9, 5, 9, 10, 5, 10, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 10, 5, 4, 10, 5, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);
        int var12 = this.getMetadataWithOffset(114 /* stairsNetherBrick */, 0);
        int var13 = this.getMetadataWithOffset(114 /* stairsNetherBrick */, 1);
        this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var13, 4, 5, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var13, 4, 5, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var13, 4, 5, 9, var3);
        this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var13, 4, 5, 10, var3);
        this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var12, 8, 5, 2, var3);
        this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var12, 8, 5, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var12, 8, 5, 9, var3);
        this.placeBlockAtCurrentPosition(var1, 114 /* stairsNetherBrick */, var12, 8, 5, 10, var3);
        this.fillWithBlocks(var1, var3, 3, 4, 4, 4, 4, 8, 88 /* slowSand */, 88 /* slowSand */, false);
        this.fillWithBlocks(var1, var3, 8, 4, 4, 9, 4, 8, 88 /* slowSand */, 88 /* slowSand */, false);
        this.fillWithBlocks(var1, var3, 3, 5, 4, 4, 5, 8, 115 /* netherStalk */, 115 /* netherStalk */, false);
        this.fillWithBlocks(var1, var3, 8, 5, 4, 9, 5, 8, 115 /* netherStalk */, 115 /* netherStalk */, false);
        this.fillWithBlocks(var1, var3, 4, 2, 0, 8, 2, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 2, 4, 12, 2, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 4, 0, 0, 8, 1, 3, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 4, 0, 9, 8, 1, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 0, 4, 3, 1, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 9, 0, 4, 12, 1, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);

        for (int var14 = 4; var14 <= 8; var14++) {
            for (int var8 = 0; var8 <= 2; var8++) {
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, var14, -1, var8, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, var14, -1, 12 - var8, var3);
            }
        }

        for (int var15 = 0; var15 <= 2; var15++) {
            for (int var16 = 4; var16 <= 8; var16++) {
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, var15, -1, var16, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, 12 - var15, -1, var16, var3);
            }
        }

        return true;
    }
}
