package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.List;
import java.util.Random;

public class ComponentNetherBridgeThrone extends ComponentNetherBridgePiece {
    private boolean hasSpawner;

    public ComponentNetherBridgeThrone(int var1, Random var2, StructureBoundingBox var3, int var4) {
        super(var1);
        this.coordBaseMode = var4;
        this.boundingBox = var3;
    }

    public static ComponentNetherBridgeThrone createValidComponent(List var0, Random var1, int var2, int var3, int var4, int var5, int var6) {
        StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -2, 0, 0, 7, 8, 9, var5);
        return isAboveGround(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new ComponentNetherBridgeThrone(var6, var1, var7, var5) : null;
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        this.fillWithBlocks(var1, var3, 0, 2, 0, 6, 7, 7, 0, 0, false);
        this.fillWithBlocks(var1, var3, 1, 0, 0, 5, 1, 7, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 1, 2, 1, 5, 2, 7, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 1, 3, 2, 5, 3, 7, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 1, 4, 3, 5, 4, 7, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 1, 2, 0, 1, 4, 2, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 5, 2, 0, 5, 4, 2, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 1, 5, 2, 1, 5, 3, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 5, 5, 2, 5, 5, 3, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 0, 5, 3, 0, 5, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 6, 5, 3, 6, 5, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.fillWithBlocks(var1, var3, 1, 5, 8, 5, 5, 8, 112 /* netherBrick */, 112 /* netherBrick */, false);
        this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, 1, 6, 3, var3);
        this.placeBlockAtCurrentPosition(var1, 113 /* netherFence */, 0, 5, 6, 3, var3);
        this.fillWithBlocks(var1, var3, 0, 6, 3, 0, 6, 8, 113 /* netherFence */, 113 /* netherFence */, false);
        this.fillWithBlocks(var1, var3, 6, 6, 3, 6, 6, 8, 113 /* netherFence */, 113 /* netherFence */, false);
        this.fillWithBlocks(var1, var3, 1, 6, 8, 5, 7, 8, 113 /* netherFence */, 113 /* netherFence */, false);
        this.fillWithBlocks(var1, var3, 2, 8, 8, 4, 8, 8, 113 /* netherFence */, 113 /* netherFence */, false);
        if (!this.hasSpawner) {
            int var4 = this.getYWithOffset(5);
            int var5 = this.getXWithOffset(3, 5);
            int var6 = this.getZWithOffset(3, 5);
            if (var3.isVecInside(var5, var4, var6)) {
                this.hasSpawner = true;
                var1.setBlock(var5, var4, var6, 52 /* mobSpawner */, 0);
                var1.setTile(var5, var4, var6, new OldTile.Spawner("Blaze"));
            }
        }

        for (int var8 = 0; var8 <= 6; var8++) {
            for (int var9 = 0; var9 <= 6; var9++) {
                this.fillCurrentPositionBlocksDownwards(var1, 112 /* netherBrick */, 0, var8, -1, var9, var3);
            }
        }

        return true;
    }
}
