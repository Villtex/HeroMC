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

public class ComponentMineshaftStairs extends StructureComponent {
    public ComponentMineshaftStairs(int var1, Random var2, StructureBoundingBox var3, int var4) {
        super(var1);
        this.coordBaseMode = var4;
        this.boundingBox = var3;
    }

    public static StructureBoundingBox findValidPlacement(List var0, Random var1, int var2, int var3, int var4, int var5) {
        StructureBoundingBox var6 = new StructureBoundingBox(var2, var3 - 5, var4, var2, var3 + 2, var4);
        switch (var5) {
            case 0:
                var6.maxX = var2 + 2;
                var6.maxZ = var4 + 8;
                break;
            case 1:
                var6.minX = var2 - 8;
                var6.maxZ = var4 + 2;
                break;
            case 2:
                var6.maxX = var2 + 2;
                var6.minZ = var4 - 8;
                break;
            case 3:
                var6.maxX = var2 + 8;
                var6.maxZ = var4 + 2;
        }

        return StructureComponent.findIntersecting(var0, var6) != null ? null : var6;
    }

    @Override
    public void buildComponent(StructureComponent var1, List var2, Random var3) {
        int var4 = this.getComponentType();
        switch (this.coordBaseMode) {
            case 0:
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, var4
                );
                break;
            case 1:
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, 1, var4
                );
                break;
            case 2:
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, var4
                );
                break;
            case 3:
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, 3, var4
                );
        }
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
        }

        this.fillWithBlocks(var1, var3, 0, 5, 0, 2, 7, 1, 0, 0, false);
        this.fillWithBlocks(var1, var3, 0, 0, 7, 2, 2, 8, 0, 0, false);

        for (int var4 = 0; var4 < 5; var4++) {
            this.fillWithBlocks(var1, var3, 0, 5 - var4 - (var4 < 4 ? 1 : 0), 2 + var4, 2, 7 - var4, 2 + var4, 0, 0, false);
        }

        return true;
    }
}
