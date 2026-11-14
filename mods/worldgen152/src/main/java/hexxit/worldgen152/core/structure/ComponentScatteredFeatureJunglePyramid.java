package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

public class ComponentScatteredFeatureJunglePyramid extends ComponentScatteredFeature {
    private boolean field_74947_h;
    private boolean field_74948_i;
    private boolean field_74945_j;
    private boolean field_74946_k;
    private static StructureScatteredFeatureStones junglePyramidsRandomScatteredStones = new StructureScatteredFeatureStones();

    /** BlockLever.invertMetadata. */
    private static int invertLever(int var0) {
        return switch (var0) {
            case 0 -> 0;
            case 1 -> 5;
            case 2 -> 4;
            case 3 -> 3;
            case 4 -> 2;
            case 5 -> 1;
            default -> -1;
        };
    }

    public ComponentScatteredFeatureJunglePyramid(Random par1Random, int par2, int par3) {
        super(par1Random, par2, 64, par3, 12, 10, 15);
    }

    @Override
    public boolean addComponentParts(OldWorld par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
        if (!this.func_74935_a(par1World, par3StructureBoundingBox, 0)) {
            return false;
        }

        int i = this.getMetadataWithOffset(67 /* stairsCobblestone */, 3);
        int j = this.getMetadataWithOffset(67 /* stairsCobblestone */, 2);
        int k = this.getMetadataWithOffset(67 /* stairsCobblestone */, 0);
        int l = this.getMetadataWithOffset(67 /* stairsCobblestone */, 1);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 0, -4, 0, this.scatteredFeatureSizeX - 1, 0, this.scatteredFeatureSizeZ - 1, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, 1, 2, 9, 2, 2, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, 1, 12, 9, 2, 12, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, 1, 3, 2, 2, 11, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 9, 1, 3, 9, 2, 11, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 3, 1, 10, 6, 1, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 3, 13, 10, 6, 13, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, 3, 2, 1, 6, 12, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 10, 3, 2, 10, 6, 12, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, 3, 2, 9, 3, 12, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, 6, 2, 9, 6, 12, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 3, 7, 3, 8, 7, 11, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 4, 8, 4, 7, 8, 10, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithAir(par1World, par3StructureBoundingBox, 3, 1, 3, 8, 2, 11);
        this.fillWithAir(par1World, par3StructureBoundingBox, 4, 3, 6, 7, 3, 9);
        this.fillWithAir(par1World, par3StructureBoundingBox, 2, 4, 2, 9, 5, 12);
        this.fillWithAir(par1World, par3StructureBoundingBox, 4, 6, 5, 7, 6, 9);
        this.fillWithAir(par1World, par3StructureBoundingBox, 5, 7, 6, 6, 7, 8);
        this.fillWithAir(par1World, par3StructureBoundingBox, 5, 1, 2, 6, 2, 2);
        this.fillWithAir(par1World, par3StructureBoundingBox, 5, 2, 12, 6, 2, 12);
        this.fillWithAir(par1World, par3StructureBoundingBox, 5, 5, 1, 6, 5, 1);
        this.fillWithAir(par1World, par3StructureBoundingBox, 5, 5, 13, 6, 5, 13);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 1, 5, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 10, 5, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 1, 5, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 10, 5, 9, par3StructureBoundingBox);

        for (int i1 = 0; i1 <= 14; i1 += 14) {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, 4, i1, 2, 5, i1, false, par2Random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 4, 4, i1, 4, 5, i1, false, par2Random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 7, 4, i1, 7, 5, i1, false, par2Random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 9, 4, i1, 9, 5, i1, false, par2Random, junglePyramidsRandomScatteredStones);
        }

        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 5, 6, 0, 6, 6, 0, false, par2Random, junglePyramidsRandomScatteredStones);

        for (int var11 = 0; var11 <= 11; var11 += 11) {
            for (int j1 = 2; j1 <= 12; j1 += 2) {
                this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, var11, 4, j1, var11, 5, j1, false, par2Random, junglePyramidsRandomScatteredStones);
            }

            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, var11, 6, 5, var11, 6, 5, false, par2Random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, var11, 6, 9, var11, 6, 9, false, par2Random, junglePyramidsRandomScatteredStones);
        }

        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, 7, 2, 2, 9, 2, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 9, 7, 2, 9, 9, 2, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, 7, 12, 2, 9, 12, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 9, 7, 12, 9, 9, 12, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 4, 9, 4, 4, 9, 4, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 7, 9, 4, 7, 9, 4, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 4, 9, 10, 4, 9, 10, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 7, 9, 10, 7, 9, 10, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 5, 9, 7, 6, 9, 7, false, par2Random, junglePyramidsRandomScatteredStones);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 5, 9, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 6, 9, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, j, 5, 9, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, j, 6, 9, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 4, 0, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 5, 0, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 6, 0, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 7, 0, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 4, 1, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 4, 2, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 4, 3, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 7, 1, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 7, 2, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, i, 7, 3, 10, par3StructureBoundingBox);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 4, 1, 9, 4, 1, 9, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 7, 1, 9, 7, 1, 9, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 4, 1, 10, 7, 2, 10, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 5, 4, 5, 6, 4, 5, false, par2Random, junglePyramidsRandomScatteredStones);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, k, 4, 4, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, l, 7, 4, 5, par3StructureBoundingBox);

        for (int var12 = 0; var12 < 4; var12++) {
            this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, j, 5, 0 - var12, 6 + var12, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 67 /* stairsCobblestone */, j, 6, 0 - var12, 6 + var12, par3StructureBoundingBox);
            this.fillWithAir(par1World, par3StructureBoundingBox, 5, 0 - var12, 7 + var12, 6, 0 - var12, 9 + var12);
        }

        this.fillWithAir(par1World, par3StructureBoundingBox, 1, -3, 12, 10, -1, 13);
        this.fillWithAir(par1World, par3StructureBoundingBox, 1, -3, 1, 3, -1, 13);
        this.fillWithAir(par1World, par3StructureBoundingBox, 1, -3, 1, 9, -1, 5);

        for (int var13 = 1; var13 <= 13; var13 += 2) {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, -3, var13, 1, -2, var13, false, par2Random, junglePyramidsRandomScatteredStones);
        }

        for (int var14 = 2; var14 <= 12; var14 += 2) {
            this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 1, -1, var14, 3, -1, var14, false, par2Random, junglePyramidsRandomScatteredStones);
        }

        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 2, -2, 1, 5, -2, 1, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 7, -2, 1, 9, -2, 1, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 6, -3, 1, 6, -3, 1, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 6, -1, 1, 6, -1, 1, false, par2Random, junglePyramidsRandomScatteredStones);
        this.placeBlockAtCurrentPosition(
            par1World, 131 /* tripWireSource */, this.getMetadataWithOffset(131 /* tripWireSource */, 3) | 4, 1, -3, 8, par3StructureBoundingBox
        );
        this.placeBlockAtCurrentPosition(
            par1World, 131 /* tripWireSource */, this.getMetadataWithOffset(131 /* tripWireSource */, 1) | 4, 4, -3, 8, par3StructureBoundingBox
        );
        this.placeBlockAtCurrentPosition(par1World, 132 /* tripWire */, 4, 2, -3, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 132 /* tripWire */, 4, 3, -3, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 5, -3, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 5, -3, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 5, -3, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 5, -3, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 5, -3, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 5, -3, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 5, -3, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 4, -3, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 3, -3, 1, par3StructureBoundingBox);
        OldLoot.Category dispenser = OldLoot.PYRAMID_JUNGLE_DISPENSER;
        OldLoot.Category chest = OldLoot.PYRAMID_JUNGLE_CHEST;
        if (!this.field_74945_j) {
            this.field_74945_j = this.generateStructureDispenserContents(
                par1World, par3StructureBoundingBox, par2Random, 3, -2, 1, 2, dispenser.items(par2Random), dispenser.count(par2Random)
            );
        }

        this.placeBlockAtCurrentPosition(par1World, 106 /* vine */, 15, 3, -2, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(
            par1World, 131 /* tripWireSource */, this.getMetadataWithOffset(131 /* tripWireSource */, 2) | 4, 7, -3, 1, par3StructureBoundingBox
        );
        this.placeBlockAtCurrentPosition(
            par1World, 131 /* tripWireSource */, this.getMetadataWithOffset(131 /* tripWireSource */, 0) | 4, 7, -3, 5, par3StructureBoundingBox
        );
        this.placeBlockAtCurrentPosition(par1World, 132 /* tripWire */, 4, 7, -3, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 132 /* tripWire */, 4, 7, -3, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 132 /* tripWire */, 4, 7, -3, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 8, -3, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 9, -3, 6, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 9, -3, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 9, -3, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 9, -2, 4, par3StructureBoundingBox);
        if (!this.field_74946_k) {
            this.field_74946_k = this.generateStructureDispenserContents(
                par1World, par3StructureBoundingBox, par2Random, 9, -2, 3, 4, dispenser.items(par2Random), dispenser.count(par2Random)
            );
        }

        this.placeBlockAtCurrentPosition(par1World, 106 /* vine */, 15, 8, -1, 3, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 106 /* vine */, 15, 8, -2, 3, par3StructureBoundingBox);
        if (!this.field_74947_h) {
            this.field_74947_h = this.generateStructureChestContents(
                par1World, par3StructureBoundingBox, par2Random, 8, -3, 3, chest.items(par2Random), chest.count(par2Random)
            );
        }

        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 9, -3, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 8, -3, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 4, -3, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 5, -2, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 5, -1, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 6, -3, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 7, -2, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 7, -1, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 8, -3, 5, par3StructureBoundingBox);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 9, -1, 1, 9, -1, 5, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithAir(par1World, par3StructureBoundingBox, 8, -3, 8, 10, -1, 10);
        this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 3, 8, -2, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 3, 9, -2, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 98 /* stoneBrick */, 3, 10, -2, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(
            par1World,
            69 /* lever */,
            invertLever(this.getMetadataWithOffset(69 /* lever */, 2)),
            8,
            -2,
            12,
            par3StructureBoundingBox
        );
        this.placeBlockAtCurrentPosition(
            par1World,
            69 /* lever */,
            invertLever(this.getMetadataWithOffset(69 /* lever */, 2)),
            9,
            -2,
            12,
            par3StructureBoundingBox
        );
        this.placeBlockAtCurrentPosition(
            par1World,
            69 /* lever */,
            invertLever(this.getMetadataWithOffset(69 /* lever */, 2)),
            10,
            -2,
            12,
            par3StructureBoundingBox
        );
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 8, -3, 8, 8, -3, 10, false, par2Random, junglePyramidsRandomScatteredStones);
        this.fillWithRandomizedBlocks(par1World, par3StructureBoundingBox, 10, -3, 8, 10, -3, 10, false, par2Random, junglePyramidsRandomScatteredStones);
        this.placeBlockAtCurrentPosition(par1World, 48 /* cobblestoneMossy */, 0, 10, -2, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 8, -2, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 8, -2, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 55 /* redstoneWire */, 0, 10, -1, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 29 /* pistonStickyBase */, 1, 9, -2, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(
            par1World, 29 /* pistonStickyBase */, this.getMetadataWithOffset(29 /* pistonStickyBase */, 4), 10, -2, 8, par3StructureBoundingBox
        );
        this.placeBlockAtCurrentPosition(
            par1World, 29 /* pistonStickyBase */, this.getMetadataWithOffset(29 /* pistonStickyBase */, 4), 10, -1, 8, par3StructureBoundingBox
        );
        this.placeBlockAtCurrentPosition(
            par1World, 93 /* redstoneRepeaterIdle */, this.getMetadataWithOffset(93 /* redstoneRepeaterIdle */, 2), 10, -2, 10, par3StructureBoundingBox
        );
        if (!this.field_74948_i) {
            this.field_74948_i = this.generateStructureChestContents(
                par1World, par3StructureBoundingBox, par2Random, 9, -3, 10, chest.items(par2Random), chest.count(par2Random)
            );
        }

        return true;
    }
}
