package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.List;
import java.util.Random;

public class ComponentNetherBridgeCrossing extends ComponentNetherBridgePiece {
    public ComponentNetherBridgeCrossing(int var1, Random var2, StructureBoundingBox var3, int var4) {
        super(var1);
        this.coordBaseMode = var4;
        this.boundingBox = var3;
    }

    @Override
    public void buildComponent(StructureComponent var1, List var2, Random var3) {
        this.getNextComponentNormal((ComponentNetherBridgeStartPiece)var1, var2, var3, 2, 0, false);
        this.getNextComponentX((ComponentNetherBridgeStartPiece)var1, var2, var3, 0, 2, false);
        this.getNextComponentZ((ComponentNetherBridgeStartPiece)var1, var2, var3, 0, 2, false);
    }

    public static ComponentNetherBridgeCrossing createValidComponent(List var0, Random var1, int var2, int var3, int var4, int var5, int var6) {
        StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -2, 0, 0, 7, 9, 7, var5);
        return isAboveGround(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new ComponentNetherBridgeCrossing(var6, var1, var7, var5) : null;
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        this.fillWithBlocks(var1, var3, 0, 0, 0, 6, 1, 6, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 2, 0, 6, 7, 6, 0, 0, false);
        this.fillWithBlocks(var1, var3, 0, 2, 0, 1, 6, 0, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 2, 6, 1, 6, 6, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 5, 2, 0, 6, 6, 0, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 5, 2, 6, 6, 6, 6, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 2, 0, 0, 6, 1, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 2, 5, 0, 6, 6, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 6, 2, 0, 6, 6, 1, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 6, 2, 5, 6, 6, 6, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 2, 6, 0, 4, 6, 0, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 2, 5, 0, 4, 5, 0, 113 /* netherFence */, 113 /* netherFence */, false);
        this.fillWithBlocks(var1, var3, 2, 6, 6, 4, 6, 6, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 2, 5, 6, 4, 5, 6, 113 /* netherFence */, 113 /* netherFence */, false);
        this.fillWithBlocks(var1, var3, 0, 6, 2, 0, 6, 4, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 5, 2, 0, 5, 4, 113 /* netherFence */, 113 /* netherFence */, false);
        this.fillWithBlocks(var1, var3, 6, 6, 2, 6, 6, 4, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 6, 5, 2, 6, 5, 4, 113 /* netherFence */, 113 /* netherFence */, false);

        for (int var4 = 0; var4 <= 6; var4++) {
            for (int var5 = 0; var5 <= 6; var5++) {
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, var4, -1, var5, var3);
            }
        }

        return true;
    }
}
