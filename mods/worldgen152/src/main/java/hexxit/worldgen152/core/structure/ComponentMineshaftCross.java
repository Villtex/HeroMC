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

public class ComponentMineshaftCross extends StructureComponent {
    private final int corridorDirection;
    private final boolean isMultipleFloors;

    public ComponentMineshaftCross(int var1, Random var2, StructureBoundingBox var3, int var4) {
        super(var1);
        this.corridorDirection = var4;
        this.boundingBox = var3;
        this.isMultipleFloors = var3.getYSize() > 3;
    }

    public static StructureBoundingBox findValidPlacement(List var0, Random var1, int var2, int var3, int var4, int var5) {
        StructureBoundingBox var6 = new StructureBoundingBox(var2, var3, var4, var2, var3 + 2, var4);
        if (var1.nextInt(4) == 0) {
            var6.maxY += 4;
        }

        switch (var5) {
            case 0:
                var6.minX = var2 - 1;
                var6.maxX = var2 + 3;
                var6.maxZ = var4 + 4;
                break;
            case 1:
                var6.minX = var2 - 4;
                var6.minZ = var4 - 1;
                var6.maxZ = var4 + 3;
                break;
            case 2:
                var6.minX = var2 - 1;
                var6.maxX = var2 + 3;
                var6.minZ = var4 - 4;
                break;
            case 3:
                var6.maxX = var2 + 4;
                var6.minZ = var4 - 1;
                var6.maxZ = var4 + 3;
        }

        return StructureComponent.findIntersecting(var0, var6) != null ? null : var6;
    }

    @Override
    public void buildComponent(StructureComponent var1, List var2, Random var3) {
        int var4 = this.getComponentType();
        switch (this.corridorDirection) {
            case 0:
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, var4
                );
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 1, var4
                );
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 3, var4
                );
                break;
            case 1:
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, var4
                );
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, var4
                );
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 1, var4
                );
                break;
            case 2:
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, var4
                );
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 1, var4
                );
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 3, var4
                );
                break;
            case 3:
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, var4
                );
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, var4
                );
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, 3, var4
                );
        }

        if (this.isMultipleFloors) {
            if (var3.nextBoolean()) {
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ - 1, 2, var4
                );
            }

            if (var3.nextBoolean()) {
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, 1, var4
                );
            }

            if (var3.nextBoolean()) {
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, 3, var4
                );
            }

            if (var3.nextBoolean()) {
                StructureMineshaftPieces.getNextMineShaftComponent(
                    var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.maxZ + 1, 0, var4
                );
            }
        }
    }

    @Override
    public boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3) {
        if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
        }

        if (this.isMultipleFloors) {
            this.fillWithBlocks(
                var1,
                var3,
                this.boundingBox.minX + 1,
                this.boundingBox.minY,
                this.boundingBox.minZ,
                this.boundingBox.maxX - 1,
                this.boundingBox.minY + 3 - 1,
                this.boundingBox.maxZ,
                0,
                0,
                false
            );
            this.fillWithBlocks(
                var1,
                var3,
                this.boundingBox.minX,
                this.boundingBox.minY,
                this.boundingBox.minZ + 1,
                this.boundingBox.maxX,
                this.boundingBox.minY + 3 - 1,
                this.boundingBox.maxZ - 1,
                0,
                0,
                false
            );
            this.fillWithBlocks(
                var1,
                var3,
                this.boundingBox.minX + 1,
                this.boundingBox.maxY - 2,
                this.boundingBox.minZ,
                this.boundingBox.maxX - 1,
                this.boundingBox.maxY,
                this.boundingBox.maxZ,
                0,
                0,
                false
            );
            this.fillWithBlocks(
                var1,
                var3,
                this.boundingBox.minX,
                this.boundingBox.maxY - 2,
                this.boundingBox.minZ + 1,
                this.boundingBox.maxX,
                this.boundingBox.maxY,
                this.boundingBox.maxZ - 1,
                0,
                0,
                false
            );
            this.fillWithBlocks(
                var1,
                var3,
                this.boundingBox.minX + 1,
                this.boundingBox.minY + 3,
                this.boundingBox.minZ + 1,
                this.boundingBox.maxX - 1,
                this.boundingBox.minY + 3,
                this.boundingBox.maxZ - 1,
                0,
                0,
                false
            );
        } else {
            this.fillWithBlocks(
                var1,
                var3,
                this.boundingBox.minX + 1,
                this.boundingBox.minY,
                this.boundingBox.minZ,
                this.boundingBox.maxX - 1,
                this.boundingBox.maxY,
                this.boundingBox.maxZ,
                0,
                0,
                false
            );
            this.fillWithBlocks(
                var1,
                var3,
                this.boundingBox.minX,
                this.boundingBox.minY,
                this.boundingBox.minZ + 1,
                this.boundingBox.maxX,
                this.boundingBox.maxY,
                this.boundingBox.maxZ - 1,
                0,
                0,
                false
            );
        }

        this.fillWithBlocks(
            var1,
            var3,
            this.boundingBox.minX + 1,
            this.boundingBox.minY,
            this.boundingBox.minZ + 1,
            this.boundingBox.minX + 1,
            this.boundingBox.maxY,
            this.boundingBox.minZ + 1,
            5 /* planks */,
            0,
            false
        );
        this.fillWithBlocks(
            var1,
            var3,
            this.boundingBox.minX + 1,
            this.boundingBox.minY,
            this.boundingBox.maxZ - 1,
            this.boundingBox.minX + 1,
            this.boundingBox.maxY,
            this.boundingBox.maxZ - 1,
            5 /* planks */,
            0,
            false
        );
        this.fillWithBlocks(
            var1,
            var3,
            this.boundingBox.maxX - 1,
            this.boundingBox.minY,
            this.boundingBox.minZ + 1,
            this.boundingBox.maxX - 1,
            this.boundingBox.maxY,
            this.boundingBox.minZ + 1,
            5 /* planks */,
            0,
            false
        );
        this.fillWithBlocks(
            var1,
            var3,
            this.boundingBox.maxX - 1,
            this.boundingBox.minY,
            this.boundingBox.maxZ - 1,
            this.boundingBox.maxX - 1,
            this.boundingBox.maxY,
            this.boundingBox.maxZ - 1,
            5 /* planks */,
            0,
            false
        );

        for (int var4 = this.boundingBox.minX; var4 <= this.boundingBox.maxX; var4++) {
            for (int var5 = this.boundingBox.minZ; var5 <= this.boundingBox.maxZ; var5++) {
                int var6 = this.getBlockIdAtCurrentPosition(var1, var4, this.boundingBox.minY - 1, var5, var3);
                if (var6 == 0) {
                    this.placeBlockAtCurrentPosition(var1, 5 /* planks */, 0, var4, this.boundingBox.minY - 1, var5, var3);
                }
            }
        }

        return true;
    }
}
