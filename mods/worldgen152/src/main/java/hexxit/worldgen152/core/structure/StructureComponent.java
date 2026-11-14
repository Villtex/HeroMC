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

public abstract class StructureComponent {
    protected StructureBoundingBox boundingBox;
    protected int coordBaseMode;
    protected int componentType;

    /** Facing.oppositeSide. */
    static final int[] OPPOSITE_SIDE = {1, 0, 3, 2, 5, 4};
    /** Direction.rotateOpposite, offsetX, offsetZ. */
    static final int[] ROTATE_OPPOSITE = {2, 3, 0, 1};
    static final int[] OFFSET_X = {0, -1, 0, 1};
    static final int[] OFFSET_Z = {1, 0, -1, 0};

    /** MathHelper.getRandomIntegerInRange. */
    static int getRandomIntegerInRange(Random var0, int var1, int var2) {
        return var1 >= var2 ? var1 : var0.nextInt(var2 - var1 + 1) + var1;
    }

    protected StructureComponent(int var1) {
        this.componentType = var1;
        this.coordBaseMode = -1;
    }

    public void buildComponent(StructureComponent var1, List var2, Random var3) {
    }

    public abstract boolean addComponentParts(OldWorld var1, Random var2, StructureBoundingBox var3);

    public StructureBoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public int getComponentType() {
        return this.componentType;
    }

    public static StructureComponent findIntersecting(List<StructureComponent> var0, StructureBoundingBox var1) {
        for (StructureComponent var3 : var0) {
            if (var3.getBoundingBox() != null && var3.getBoundingBox().intersectsWith(var1)) {
                return var3;
            }
        }

        return null;
    }


    protected boolean isLiquidInStructureBoundingBox(OldWorld var1, StructureBoundingBox var2) {
        int var3 = Math.max(this.boundingBox.minX - 1, var2.minX);
        int var4 = Math.max(this.boundingBox.minY - 1, var2.minY);
        int var5 = Math.max(this.boundingBox.minZ - 1, var2.minZ);
        int var6 = Math.min(this.boundingBox.maxX + 1, var2.maxX);
        int var7 = Math.min(this.boundingBox.maxY + 1, var2.maxY);
        int var8 = Math.min(this.boundingBox.maxZ + 1, var2.maxZ);

        for (int var9 = var3; var9 <= var6; var9++) {
            for (int var10 = var5; var10 <= var8; var10++) {
                int var11 = var1.getBlockId(var9, var4, var10);
                if (var11 > 0 && OldBlockInfo.material(var11).liquid) {
                    return true;
                }

                var11 = var1.getBlockId(var9, var7, var10);
                if (var11 > 0 && OldBlockInfo.material(var11).liquid) {
                    return true;
                }
            }
        }

        for (int var12 = var3; var12 <= var6; var12++) {
            for (int var14 = var4; var14 <= var7; var14++) {
                int var17 = var1.getBlockId(var12, var14, var5);
                if (var17 > 0 && OldBlockInfo.material(var17).liquid) {
                    return true;
                }

                var17 = var1.getBlockId(var12, var14, var8);
                if (var17 > 0 && OldBlockInfo.material(var17).liquid) {
                    return true;
                }
            }
        }

        for (int var13 = var5; var13 <= var8; var13++) {
            for (int var15 = var4; var15 <= var7; var15++) {
                int var19 = var1.getBlockId(var3, var15, var13);
                if (var19 > 0 && OldBlockInfo.material(var19).liquid) {
                    return true;
                }

                var19 = var1.getBlockId(var6, var15, var13);
                if (var19 > 0 && OldBlockInfo.material(var19).liquid) {
                    return true;
                }
            }
        }

        return false;
    }

    protected int getXWithOffset(int var1, int var2) {
        switch (this.coordBaseMode) {
            case 0:
            case 2:
                return this.boundingBox.minX + var1;
            case 1:
                return this.boundingBox.maxX - var2;
            case 3:
                return this.boundingBox.minX + var2;
            default:
                return var1;
        }
    }

    protected int getYWithOffset(int var1) {
        return this.coordBaseMode == -1 ? var1 : var1 + this.boundingBox.minY;
    }

    protected int getZWithOffset(int var1, int var2) {
        switch (this.coordBaseMode) {
            case 0:
                return this.boundingBox.minZ + var2;
            case 1:
            case 3:
                return this.boundingBox.minZ + var1;
            case 2:
                return this.boundingBox.maxZ - var2;
            default:
                return var2;
        }
    }

    protected int getMetadataWithOffset(int var1, int var2) {
        if (var1 == 66 /* rail */) {
            if (this.coordBaseMode == 1 || this.coordBaseMode == 3) {
                if (var2 == 1) {
                    return 0;
                }

                return 1;
            }
        } else if (var1 != 64 /* doorWood */ && var1 != 71 /* doorIron */) {
            if (var1 != 67 /* stairsCobblestone */
                && var1 != 53 /* stairsWoodOak */
                && var1 != 114 /* stairsNetherBrick */
                && var1 != 109 /* stairsStoneBrick */
                && var1 != 128 /* stairsSandStone */) {
                if (var1 == 65 /* ladder */) {
                    if (this.coordBaseMode == 0) {
                        if (var2 == 2) {
                            return 3;
                        }

                        if (var2 == 3) {
                            return 2;
                        }
                    } else if (this.coordBaseMode == 1) {
                        if (var2 == 2) {
                            return 4;
                        }

                        if (var2 == 3) {
                            return 5;
                        }

                        if (var2 == 4) {
                            return 2;
                        }

                        if (var2 == 5) {
                            return 3;
                        }
                    } else if (this.coordBaseMode == 3) {
                        if (var2 == 2) {
                            return 5;
                        }

                        if (var2 == 3) {
                            return 4;
                        }

                        if (var2 == 4) {
                            return 2;
                        }

                        if (var2 == 5) {
                            return 3;
                        }
                    }
                } else if (var1 == 77 /* stoneButton */) {
                    if (this.coordBaseMode == 0) {
                        if (var2 == 3) {
                            return 4;
                        }

                        if (var2 == 4) {
                            return 3;
                        }
                    } else if (this.coordBaseMode == 1) {
                        if (var2 == 3) {
                            return 1;
                        }

                        if (var2 == 4) {
                            return 2;
                        }

                        if (var2 == 2) {
                            return 3;
                        }

                        if (var2 == 1) {
                            return 4;
                        }
                    } else if (this.coordBaseMode == 3) {
                        if (var2 == 3) {
                            return 2;
                        }

                        if (var2 == 4) {
                            return 1;
                        }

                        if (var2 == 2) {
                            return 3;
                        }

                        if (var2 == 1) {
                            return 4;
                        }
                    }
                } else if (var1 != 131 /* tripWireSource */
                    && !isDirectional(var1)) {
                    if (var1 == 33 /* pistonBase */
                        || var1 == 29 /* pistonStickyBase */
                        || var1 == 69 /* lever */
                        || var1 == 23 /* dispenser */) {
                        if (this.coordBaseMode == 0) {
                            if (var2 == 2 || var2 == 3) {
                                return StructureComponent.OPPOSITE_SIDE[var2];
                            }
                        } else if (this.coordBaseMode == 1) {
                            if (var2 == 2) {
                                return 4;
                            }

                            if (var2 == 3) {
                                return 5;
                            }

                            if (var2 == 4) {
                                return 2;
                            }

                            if (var2 == 5) {
                                return 3;
                            }
                        } else if (this.coordBaseMode == 3) {
                            if (var2 == 2) {
                                return 5;
                            }

                            if (var2 == 3) {
                                return 4;
                            }

                            if (var2 == 4) {
                                return 2;
                            }

                            if (var2 == 5) {
                                return 3;
                            }
                        }
                    }
                } else if (this.coordBaseMode == 0) {
                    if (var2 == 0 || var2 == 2) {
                        return StructureComponent.ROTATE_OPPOSITE[var2];
                    }
                } else if (this.coordBaseMode == 1) {
                    if (var2 == 2) {
                        return 1;
                    }

                    if (var2 == 0) {
                        return 3;
                    }

                    if (var2 == 1) {
                        return 2;
                    }

                    if (var2 == 3) {
                        return 0;
                    }
                } else if (this.coordBaseMode == 3) {
                    if (var2 == 2) {
                        return 3;
                    }

                    if (var2 == 0) {
                        return 1;
                    }

                    if (var2 == 1) {
                        return 2;
                    }

                    if (var2 == 3) {
                        return 0;
                    }
                }
            } else if (this.coordBaseMode == 0) {
                if (var2 == 2) {
                    return 3;
                }

                if (var2 == 3) {
                    return 2;
                }
            } else if (this.coordBaseMode == 1) {
                if (var2 == 0) {
                    return 2;
                }

                if (var2 == 1) {
                    return 3;
                }

                if (var2 == 2) {
                    return 0;
                }

                if (var2 == 3) {
                    return 1;
                }
            } else if (this.coordBaseMode == 3) {
                if (var2 == 0) {
                    return 2;
                }

                if (var2 == 1) {
                    return 3;
                }

                if (var2 == 2) {
                    return 1;
                }

                if (var2 == 3) {
                    return 0;
                }
            }
        } else if (this.coordBaseMode == 0) {
            if (var2 == 0) {
                return 2;
            }

            if (var2 == 2) {
                return 0;
            }
        } else {
            if (this.coordBaseMode == 1) {
                return var2 + 1 & 3;
            }

            if (this.coordBaseMode == 3) {
                return var2 + 3 & 3;
            }
        }

        return var2;
    }

    /** Blocks of class BlockDirectional: bed, repeater, comparator, pumpkin, fence gate, cocoa. */
    private static boolean isDirectional(int var0) {
        return var0 == 26 || var0 == 93 || var0 == 94 || var0 == 149 || var0 == 150 || var0 == 86 || var0 == 91 || var0 == 107
            || var0 == 127;
    }

    protected void placeBlockAtCurrentPosition(OldWorld var1, int var2, int var3, int var4, int var5, int var6, StructureBoundingBox var7) {
        int var8 = this.getXWithOffset(var4, var6);
        int var9 = this.getYWithOffset(var5);
        int var10 = this.getZWithOffset(var4, var6);
        if (var7.isVecInside(var8, var9, var10)) {
            var1.setBlock(var8, var9, var10, var2, var3);
        }
    }

    protected int getBlockIdAtCurrentPosition(OldWorld var1, int var2, int var3, int var4, StructureBoundingBox var5) {
        int var6 = this.getXWithOffset(var2, var4);
        int var7 = this.getYWithOffset(var3);
        int var8 = this.getZWithOffset(var2, var4);
        return !var5.isVecInside(var6, var7, var8) ? 0 : var1.getBlockId(var6, var7, var8);
    }

    protected void fillWithAir(OldWorld var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8) {
        for (int var9 = var4; var9 <= var7; var9++) {
            for (int var10 = var3; var10 <= var6; var10++) {
                for (int var11 = var5; var11 <= var8; var11++) {
                    this.placeBlockAtCurrentPosition(var1, 0, 0, var10, var9, var11, var2);
                }
            }
        }
    }

    protected void fillWithBlocks(
        OldWorld var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, boolean var11
    ) {
        for (int var12 = var4; var12 <= var7; var12++) {
            for (int var13 = var3; var13 <= var6; var13++) {
                for (int var14 = var5; var14 <= var8; var14++) {
                    if (!var11 || this.getBlockIdAtCurrentPosition(var1, var13, var12, var14, var2) != 0) {
                        if (var12 != var4 && var12 != var7 && var13 != var3 && var13 != var6 && var14 != var5 && var14 != var8) {
                            this.placeBlockAtCurrentPosition(var1, var10, 0, var13, var12, var14, var2);
                        } else {
                            this.placeBlockAtCurrentPosition(var1, var9, 0, var13, var12, var14, var2);
                        }
                    }
                }
            }
        }
    }

    protected void fillWithMetadataBlocks(
        OldWorld var1,
        StructureBoundingBox var2,
        int var3,
        int var4,
        int var5,
        int var6,
        int var7,
        int var8,
        int var9,
        int var10,
        int var11,
        int var12,
        boolean var13
    ) {
        for (int var14 = var4; var14 <= var7; var14++) {
            for (int var15 = var3; var15 <= var6; var15++) {
                for (int var16 = var5; var16 <= var8; var16++) {
                    if (!var13 || this.getBlockIdAtCurrentPosition(var1, var15, var14, var16, var2) != 0) {
                        if (var14 != var4 && var14 != var7 && var15 != var3 && var15 != var6 && var16 != var5 && var16 != var8) {
                            this.placeBlockAtCurrentPosition(var1, var11, var12, var15, var14, var16, var2);
                        } else {
                            this.placeBlockAtCurrentPosition(var1, var9, var10, var15, var14, var16, var2);
                        }
                    }
                }
            }
        }
    }

    protected void fillWithRandomizedBlocks(
        OldWorld var1,
        StructureBoundingBox var2,
        int var3,
        int var4,
        int var5,
        int var6,
        int var7,
        int var8,
        boolean var9,
        Random var10,
        StructurePieceBlockSelector var11
    ) {
        for (int var12 = var4; var12 <= var7; var12++) {
            for (int var13 = var3; var13 <= var6; var13++) {
                for (int var14 = var5; var14 <= var8; var14++) {
                    if (!var9 || this.getBlockIdAtCurrentPosition(var1, var13, var12, var14, var2) != 0) {
                        var11.selectBlocks(
                            var10, var13, var12, var14, var12 == var4 || var12 == var7 || var13 == var3 || var13 == var6 || var14 == var5 || var14 == var8
                        );
                        this.placeBlockAtCurrentPosition(var1, var11.getSelectedBlockId(), var11.getSelectedBlockMetaData(), var13, var12, var14, var2);
                    }
                }
            }
        }
    }

    protected void randomlyFillWithBlocks(
        OldWorld var1,
        StructureBoundingBox var2,
        Random var3,
        float var4,
        int var5,
        int var6,
        int var7,
        int var8,
        int var9,
        int var10,
        int var11,
        int var12,
        boolean var13
    ) {
        for (int var14 = var6; var14 <= var9; var14++) {
            for (int var15 = var5; var15 <= var8; var15++) {
                for (int var16 = var7; var16 <= var10; var16++) {
                    if (!(var3.nextFloat() > var4) && (!var13 || this.getBlockIdAtCurrentPosition(var1, var15, var14, var16, var2) != 0)) {
                        if (var14 != var6 && var14 != var9 && var15 != var5 && var15 != var8 && var16 != var7 && var16 != var10) {
                            this.placeBlockAtCurrentPosition(var1, var12, 0, var15, var14, var16, var2);
                        } else {
                            this.placeBlockAtCurrentPosition(var1, var11, 0, var15, var14, var16, var2);
                        }
                    }
                }
            }
        }
    }

    protected void randomlyPlaceBlock(OldWorld var1, StructureBoundingBox var2, Random var3, float var4, int var5, int var6, int var7, int var8, int var9) {
        if (var3.nextFloat() < var4) {
            this.placeBlockAtCurrentPosition(var1, var8, var9, var5, var6, var7, var2);
        }
    }

    protected void randomlyRareFillWithBlocks(OldWorld var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, boolean var10) {
        float var11 = var6 - var3 + 1;
        float var12 = var7 - var4 + 1;
        float var13 = var8 - var5 + 1;
        float var14 = var3 + var11 / 2.0F;
        float var15 = var5 + var13 / 2.0F;

        for (int var16 = var4; var16 <= var7; var16++) {
            float var17 = (var16 - var4) / var12;

            for (int var18 = var3; var18 <= var6; var18++) {
                float var19 = (var18 - var14) / (var11 * 0.5F);

                for (int var20 = var5; var20 <= var8; var20++) {
                    float var21 = (var20 - var15) / (var13 * 0.5F);
                    if (!var10 || this.getBlockIdAtCurrentPosition(var1, var18, var16, var20, var2) != 0) {
                        float var22 = var19 * var19 + var17 * var17 + var21 * var21;
                        if (var22 <= 1.05F) {
                            this.placeBlockAtCurrentPosition(var1, var9, 0, var18, var16, var20, var2);
                        }
                    }
                }
            }
        }
    }

    protected void clearCurrentPositionBlocksUpwards(OldWorld var1, int var2, int var3, int var4, StructureBoundingBox var5) {
        int var6 = this.getXWithOffset(var2, var4);
        int var7 = this.getYWithOffset(var3);
        int var8 = this.getZWithOffset(var2, var4);
        if (var5.isVecInside(var6, var7, var8)) {
            while (!var1.isAirBlock(var6, var7, var8) && var7 < 255) {
                var1.setBlock(var6, var7, var8, 0, 0);
                var7++;
            }
        }
    }

    protected void fillCurrentPositionBlocksDownwards(OldWorld var1, int var2, int var3, int var4, int var5, int var6, StructureBoundingBox var7) {
        int var8 = this.getXWithOffset(var4, var6);
        int var9 = this.getYWithOffset(var5);
        int var10 = this.getZWithOffset(var4, var6);
        if (var7.isVecInside(var8, var9, var10)) {
            while ((var1.isAirBlock(var8, var9, var10) || var1.material(var8, var9, var10).liquid) && var9 > 1) {
                var1.setBlock(var8, var9, var10, var2, var3);
                var9--;
            }
        }
    }

    protected boolean generateStructureChestContents(OldWorld var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, OldLoot.Entry[] var7, int var8) {
        int var9 = this.getXWithOffset(var4, var6);
        int var10 = this.getYWithOffset(var5);
        int var11 = this.getZWithOffset(var4, var6);
        if (var2.isVecInside(var9, var10, var11) && var1.getBlockId(var9, var10, var11) != 54 /* chest */) {
            var1.setBlock(var9, var10, var11, 54 /* chest */, 0);
            if (var1.getBlockId(var9, var10, var11) == 54) {
                OldTile.Chest var12 = new OldTile.Chest();
                var1.setTile(var9, var10, var11, var12);
                OldLoot.generate(var3, var7, var12.slots(), var8);
            }

            return true;
        } else {
            return false;
        }
    }

    protected boolean generateStructureDispenserContents(
        OldWorld var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, int var7, OldLoot.Entry[] var8, int var9
    ) {
        int var10 = this.getXWithOffset(var4, var6);
        int var11 = this.getYWithOffset(var5);
        int var12 = this.getZWithOffset(var4, var6);
        if (var2.isVecInside(var10, var11, var12) && var1.getBlockId(var10, var11, var12) != 23 /* dispenser */) {
            var1.setBlock(var10, var11, var12, 23 /* dispenser */, this.getMetadataWithOffset(23 /* dispenser */, var7));
            if (var1.getBlockId(var10, var11, var12) == 23) {
                OldTile.Dispenser var13 = new OldTile.Dispenser();
                var1.setTile(var10, var11, var12, var13);
                OldLoot.generate(var3, var8, var13.slots(), var9);
            }

            return true;
        } else {
            return false;
        }
    }

    protected void placeDoorAtCurrentPosition(OldWorld var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, int var7) {
        int var8 = this.getXWithOffset(var4, var6);
        int var9 = this.getYWithOffset(var5);
        int var10 = this.getZWithOffset(var4, var6);
        if (var2.isVecInside(var8, var9, var10)) {
            var1.placeDoor(var8, var9, var10, var7, 64 /* doorWood */);
        }
    }
}
