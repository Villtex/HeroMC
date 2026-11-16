package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.List;
import java.util.Random;

public class ComponentNetherBridgeEntrance extends ComponentNetherBridgePiece {
    public ComponentNetherBridgeEntrance(int var1, Random var2, StructureBoundingBox var3, int var4) {
        super(var1);
        this.coordBaseMode = var4;
        this.boundingBox = var3;
    }

    @Override
    public void buildComponent(StructureComponent var1, List var2, Random var3) {
        this.getNextComponentNormal((ComponentNetherBridgeStartPiece)var1, var2, var3, 5, 3, true);
    }

    public static ComponentNetherBridgeEntrance createValidComponent(List var0, Random var1, int var2, int var3, int var4, int var5, int var6) {
        StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -5, -3, 0, 13, 14, 13, var5);
        return isAboveGround(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new ComponentNetherBridgeEntrance(var6, var1, var7, var5) : null;
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
        this.fillWithBlocks(var1, var3, 5, 8, 0, 7, 8, 0, 113 /* netherFence */, 113 /* netherFence */, false);

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

        for (byte var7 = 3; var7 <= 9; var7 += 2) {
            this.fillWithBlocks(var1, var3, 1, 7, var7, 1, 8, var7, 113 /* netherFence */, 113 /* netherFence */, false);
            this.fillWithBlocks(var1, var3, 11, 7, var7, 11, 8, var7, 113 /* netherFence */, 113 /* netherFence */, false);
        }

        this.fillWithBlocks(var1, var3, 4, 2, 0, 8, 2, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 2, 4, 12, 2, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 4, 0, 0, 8, 1, 3, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 4, 0, 9, 8, 1, 12, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 0, 4, 3, 1, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 9, 0, 4, 12, 1, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);

        for (int var8 = 4; var8 <= 8; var8++) {
            for (int var5 = 0; var5 <= 2; var5++) {
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, var8, -1, var5, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, var8, -1, 12 - var5, var3);
            }
        }

        for (int var9 = 0; var9 <= 2; var9++) {
            for (int var11 = 4; var11 <= 8; var11++) {
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, var9, -1, var11, var3);
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, 12 - var9, -1, var11, var3);
            }
        }

        this.fillWithBlocks(var1, var3, 5, 5, 5, 7, 5, 7, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 6, 1, 6, 6, 4, 6, 0, 0, false);
        this.placeBlockAtCurrentPosition(var1, 112 /* netherBrick */, 0, 6, 0, 6, var3);
        this.placeBlockAtCurrentPosition(var1, 10 /* lavaMoving */, 0, 6, 5, 6, var3);
        int var10 = this.getXWithOffset(6, 6);
        int var12 = this.getYWithOffset(5);
        int var6 = this.getZWithOffset(6, 6);
        if (var3.isVecInside(var10, var12, var6)) {
            // scheduledUpdatesAreImmediate = true; Block.lavaMoving.updateTick(...)
            this.lavaUpdateTick(var1, var10, var12, var6);
        }

        return true;
    }

    /**
     * BlockFlowing.updateTick of the lava source just placed, in the Nether. A source (decay 0) turns
     * stationary and flows into the block below with decay 8; the random is not drawn for a source.
     * Flowing blocks are not ticked at once even with scheduledUpdatesAreImmediate
     * (BlockFlowing.func_82506_l is false), so the new lava below only gets a pending tick; the game
     * runs that once the chunk is live. The block below is the shaft this piece has just cleared, so the
     * sideways branch of updateTick (lava resting on a solid block) cannot happen here.
     */
    private void lavaUpdateTick(OldWorld var1, int var2, int var3, int var4) {
        int var5 = var1.getBlockId(var2, var3, var4) == 10 || var1.getBlockId(var2, var3, var4) == 11 ? var1.getBlockMetadata(var2, var3, var4) : -1;
        if (var5 != 0) {
            return;
        }

        // updateFlow: the source becomes stationary lava
        var1.setBlock(var2, var3, var4, 11 /* lavaStill */, var5);
        if (liquidCanDisplaceBlock(var1, var2, var3 - 1, var4)) {
            if (var1.material(var2, var3 - 1, var4) == OldBlockInfo.Material.WATER) {
                var1.setBlock(var2, var3 - 1, var4, 1 /* stone */, 0);
                notifyLiquidsOfNeighborChange(var1, var2, var3 - 1, var4);
                return;
            }

            // flowIntoBlock with decay 8 (what it replaces would only drop as an item); flag 3 notifies the neighbours
            var1.setBlock(var2, var3 - 1, var4, 10 /* lavaMoving */, 8);
            notifyLiquidsOfNeighborChange(var1, var2, var3 - 1, var4);
        }
    }

    /** BlockFlowing.liquidCanDisplaceBlock for lava, with blockBlocksFlow. */
    private static boolean liquidCanDisplaceBlock(OldWorld var0, int var1, int var2, int var3) {
        int var4 = var0.getBlockId(var1, var2, var3);
        if (var4 == 0) {
            return true;
        }

        if (var4 == 90 /* portal */ || var4 == 64 /* doorWood */ || var4 == 71 /* doorIron */ || var4 == 63 /* signPost */ || var4 == 65 /* ladder */
            || var4 == 83 /* reed */) {
            return false;
        }

        OldBlockInfo.Material var5 = OldBlockInfo.material(var4);
        return var5 != OldBlockInfo.Material.LAVA && !var5.blocksMovement;
    }

    /**
     * World.notifyBlocksOfNeighborChange, reduced to what reacts during generation: a still liquid next to
     * the changed block turns into a flowing one (BlockStationary.setNotStationary) and gets a pending tick.
     */
    private static void notifyLiquidsOfNeighborChange(OldWorld var0, int var1, int var2, int var3) {
        int[][] var4 = {{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
        for (int[] var8 : var4) {
            int var5 = var1 + var8[0];
            int var6 = var2 + var8[1];
            int var7 = var3 + var8[2];
            int var9 = var0.getBlockId(var5, var6, var7);
            if (var9 == 9 /* waterStill */ || var9 == 11 /* lavaStill */) {
                var0.setBlock(var5, var6, var7, var9 - 1, var0.getBlockMetadata(var5, var6, var7));
            }
        }
    }
}
