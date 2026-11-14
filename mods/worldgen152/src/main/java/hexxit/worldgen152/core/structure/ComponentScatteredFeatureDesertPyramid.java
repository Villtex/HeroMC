package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

public class ComponentScatteredFeatureDesertPyramid extends ComponentScatteredFeature {
    private boolean[] field_74940_h = new boolean[4];

    public ComponentScatteredFeatureDesertPyramid(Random par1Random, int par2, int par3) {
        super(par1Random, par2, 64, par3, 21, 15, 21);
    }

    @Override
    public boolean addComponentParts(OldWorld par1World, Random par2Random, StructureBoundingBox par3StructureBoundingBox) {
        this.fillWithBlocks(
            par1World,
            par3StructureBoundingBox,
            0,
            -4,
            0,
            this.scatteredFeatureSizeX - 1,
            0,
            this.scatteredFeatureSizeZ - 1,
            24 /* sandStone */,
            24 /* sandStone */,
            false
        );

        for (int i = 1; i <= 9; i++) {
            this.fillWithBlocks(
                par1World,
                par3StructureBoundingBox,
                i,
                i,
                i,
                this.scatteredFeatureSizeX - 1 - i,
                i,
                this.scatteredFeatureSizeZ - 1 - i,
                24 /* sandStone */,
                24 /* sandStone */,
                false
            );
            this.fillWithBlocks(par1World, par3StructureBoundingBox, i + 1, i, i + 1, this.scatteredFeatureSizeX - 2 - i, i, this.scatteredFeatureSizeZ - 2 - i, 0, 0, false);
        }

        for (int var14 = 0; var14 < this.scatteredFeatureSizeX; var14++) {
            for (int j = 0; j < this.scatteredFeatureSizeZ; j++) {
                this.fillCurrentPositionBlocksDownwards(par1World, 24 /* sandStone */, 0, var14, -5, j, par3StructureBoundingBox);
            }
        }

        int var15 = this.getMetadataWithOffset(128 /* stairsSandStone */, 3);
        int j = this.getMetadataWithOffset(128 /* stairsSandStone */, 2);
        int k = this.getMetadataWithOffset(128 /* stairsSandStone */, 0);
        int l = this.getMetadataWithOffset(128 /* stairsSandStone */, 1);
        byte b0 = 1;
        byte b1 = 11;
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 0, 0, 0, 4, 9, 4, 24 /* sandStone */, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 10, 1, 3, 10, 3, 24 /* sandStone */, 24 /* sandStone */, false);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, var15, 2, 10, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, j, 2, 10, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, k, 0, 10, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, l, 4, 10, 2, par3StructureBoundingBox);
        this.fillWithBlocks(
            par1World, par3StructureBoundingBox, this.scatteredFeatureSizeX - 5, 0, 0, this.scatteredFeatureSizeX - 1, 9, 4, 24 /* sandStone */, 0, false
        );
        this.fillWithBlocks(
            par1World,
            par3StructureBoundingBox,
            this.scatteredFeatureSizeX - 4,
            10,
            1,
            this.scatteredFeatureSizeX - 2,
            10,
            3,
            24 /* sandStone */,
            24 /* sandStone */,
            false
        );
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, var15, this.scatteredFeatureSizeX - 3, 10, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, j, this.scatteredFeatureSizeX - 3, 10, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, k, this.scatteredFeatureSizeX - 5, 10, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, l, this.scatteredFeatureSizeX - 1, 10, 2, par3StructureBoundingBox);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 8, 0, 0, 12, 4, 4, 24 /* sandStone */, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 9, 1, 0, 11, 3, 4, 0, 0, false);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 9, 1, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 9, 2, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 9, 3, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 10, 3, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 11, 3, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 11, 2, 1, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 11, 1, 1, par3StructureBoundingBox);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 1, 8, 3, 3, 24 /* sandStone */, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 1, 2, 8, 2, 2, 0, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 12, 1, 1, 16, 3, 3, 24 /* sandStone */, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 12, 1, 2, 16, 2, 2, 0, 0, false);
        this.fillWithBlocks(
            par1World,
            par3StructureBoundingBox,
            5,
            4,
            5,
            this.scatteredFeatureSizeX - 6,
            4,
            this.scatteredFeatureSizeZ - 6,
            24 /* sandStone */,
            24 /* sandStone */,
            false
        );
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 9, 4, 9, 11, 4, 11, 0, 0, false);
        this.fillWithMetadataBlocks(
            par1World, par3StructureBoundingBox, 8, 1, 8, 8, 3, 8, 24 /* sandStone */, 2, 24 /* sandStone */, 2, false
        );
        this.fillWithMetadataBlocks(
            par1World, par3StructureBoundingBox, 12, 1, 8, 12, 3, 8, 24 /* sandStone */, 2, 24 /* sandStone */, 2, false
        );
        this.fillWithMetadataBlocks(
            par1World, par3StructureBoundingBox, 8, 1, 12, 8, 3, 12, 24 /* sandStone */, 2, 24 /* sandStone */, 2, false
        );
        this.fillWithMetadataBlocks(
            par1World, par3StructureBoundingBox, 12, 1, 12, 12, 3, 12, 24 /* sandStone */, 2, 24 /* sandStone */, 2, false
        );
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 1, 5, 4, 4, 11, 24 /* sandStone */, 24 /* sandStone */, false);
        this.fillWithBlocks(
            par1World,
            par3StructureBoundingBox,
            this.scatteredFeatureSizeX - 5,
            1,
            5,
            this.scatteredFeatureSizeX - 2,
            4,
            11,
            24 /* sandStone */,
            24 /* sandStone */,
            false
        );
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 6, 7, 9, 6, 7, 11, 24 /* sandStone */, 24 /* sandStone */, false);
        this.fillWithBlocks(
            par1World,
            par3StructureBoundingBox,
            this.scatteredFeatureSizeX - 7,
            7,
            9,
            this.scatteredFeatureSizeX - 7,
            7,
            11,
            24 /* sandStone */,
            24 /* sandStone */,
            false
        );
        this.fillWithMetadataBlocks(
            par1World, par3StructureBoundingBox, 5, 5, 9, 5, 7, 11, 24 /* sandStone */, 2, 24 /* sandStone */, 2, false
        );
        this.fillWithMetadataBlocks(
            par1World,
            par3StructureBoundingBox,
            this.scatteredFeatureSizeX - 6,
            5,
            9,
            this.scatteredFeatureSizeX - 6,
            7,
            11,
            24 /* sandStone */,
            2,
            24 /* sandStone */,
            2,
            false
        );
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 5, 5, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 5, 6, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 6, 6, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, this.scatteredFeatureSizeX - 6, 5, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, this.scatteredFeatureSizeX - 6, 6, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, this.scatteredFeatureSizeX - 7, 6, 10, par3StructureBoundingBox);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 2, 4, 4, 2, 6, 4, 0, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, this.scatteredFeatureSizeX - 3, 4, 4, this.scatteredFeatureSizeX - 3, 6, 4, 0, 0, false);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, var15, 2, 4, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, var15, 2, 3, 4, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, var15, this.scatteredFeatureSizeX - 3, 4, 5, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, var15, this.scatteredFeatureSizeX - 3, 3, 4, par3StructureBoundingBox);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 1, 1, 3, 2, 2, 3, 24 /* sandStone */, 24 /* sandStone */, false);
        this.fillWithBlocks(
            par1World,
            par3StructureBoundingBox,
            this.scatteredFeatureSizeX - 3,
            1,
            3,
            this.scatteredFeatureSizeX - 2,
            2,
            3,
            24 /* sandStone */,
            24 /* sandStone */,
            false
        );
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, 0, 1, 1, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, 0, this.scatteredFeatureSizeX - 2, 1, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 1, 1, 2, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 44 /* stoneSingleSlab */, 1, this.scatteredFeatureSizeX - 2, 2, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, l, 2, 1, 2, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 128 /* stairsSandStone */, k, this.scatteredFeatureSizeX - 3, 1, 2, par3StructureBoundingBox);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 4, 3, 5, 4, 3, 18, 24 /* sandStone */, 24 /* sandStone */, false);
        this.fillWithBlocks(
            par1World,
            par3StructureBoundingBox,
            this.scatteredFeatureSizeX - 5,
            3,
            5,
            this.scatteredFeatureSizeX - 5,
            3,
            17,
            24 /* sandStone */,
            24 /* sandStone */,
            false
        );
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 3, 1, 5, 4, 2, 16, 0, 0, false);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, this.scatteredFeatureSizeX - 6, 1, 5, this.scatteredFeatureSizeX - 5, 2, 16, 0, 0, false);

        for (int i1 = 5; i1 <= 17; i1 += 2) {
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 4, 1, i1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, 4, 2, i1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, this.scatteredFeatureSizeX - 5, 1, i1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, this.scatteredFeatureSizeX - 5, 2, i1, par3StructureBoundingBox);
        }

        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 10, 0, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 10, 0, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 9, 0, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 11, 0, 9, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 8, 0, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 12, 0, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 7, 0, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 13, 0, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 9, 0, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 11, 0, 11, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 10, 0, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 10, 0, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b1, 10, 0, 10, par3StructureBoundingBox);

        for (int var17 = 0; var17 <= this.scatteredFeatureSizeX - 1; var17 += this.scatteredFeatureSizeX - 1) {
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var17, 2, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 2, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var17, 2, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var17, 3, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 3, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var17, 3, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 4, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, var17, 4, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 4, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var17, 5, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 5, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var17, 5, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 6, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, var17, 6, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 6, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 7, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 7, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var17, 7, 3, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var17, 8, 1, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var17, 8, 2, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var17, 8, 3, par3StructureBoundingBox);
        }

        for (int var18 = 2; var18 <= this.scatteredFeatureSizeX - 3; var18 += this.scatteredFeatureSizeX - 3 - 2) {
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var18 - 1, 2, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18, 2, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var18 + 1, 2, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var18 - 1, 3, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18, 3, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var18 + 1, 3, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18 - 1, 4, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, var18, 4, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18 + 1, 4, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var18 - 1, 5, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18, 5, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var18 + 1, 5, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18 - 1, 6, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, var18, 6, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18 + 1, 6, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18 - 1, 7, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18, 7, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, var18 + 1, 7, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var18 - 1, 8, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var18, 8, 0, par3StructureBoundingBox);
            this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, var18 + 1, 8, 0, par3StructureBoundingBox);
        }

        this.fillWithMetadataBlocks(
            par1World, par3StructureBoundingBox, 8, 4, 0, 12, 6, 0, 24 /* sandStone */, 2, 24 /* sandStone */, 2, false
        );
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 8, 6, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 12, 6, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 9, 5, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, 10, 5, 0, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 35 /* cloth */, b0, 11, 5, 0, par3StructureBoundingBox);
        this.fillWithMetadataBlocks(
            par1World, par3StructureBoundingBox, 8, -14, 8, 12, -11, 12, 24 /* sandStone */, 2, 24 /* sandStone */, 2, false
        );
        this.fillWithMetadataBlocks(
            par1World, par3StructureBoundingBox, 8, -10, 8, 12, -10, 12, 24 /* sandStone */, 1, 24 /* sandStone */, 1, false
        );
        this.fillWithMetadataBlocks(
            par1World, par3StructureBoundingBox, 8, -9, 8, 12, -9, 12, 24 /* sandStone */, 2, 24 /* sandStone */, 2, false
        );
        this.fillWithBlocks(
            par1World, par3StructureBoundingBox, 8, -8, 8, 12, -1, 12, 24 /* sandStone */, 24 /* sandStone */, false
        );
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 9, -11, 9, 11, -1, 11, 0, 0, false);
        this.placeBlockAtCurrentPosition(par1World, 70 /* pressurePlateStone */, 0, 10, -11, 10, par3StructureBoundingBox);
        this.fillWithBlocks(par1World, par3StructureBoundingBox, 9, -13, 9, 11, -13, 11, 46 /* tnt */, 0, false);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 8, -11, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 8, -10, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, 7, -10, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 7, -11, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 12, -11, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 12, -10, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, 13, -10, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 13, -11, 10, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 10, -11, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 10, -10, 8, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, 10, -10, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 10, -11, 7, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 10, -11, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 0, 0, 10, -10, 12, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 1, 10, -10, 13, par3StructureBoundingBox);
        this.placeBlockAtCurrentPosition(par1World, 24 /* sandStone */, 2, 10, -11, 13, par3StructureBoundingBox);
        OldLoot.Category info = OldLoot.PYRAMID_DESERT_CHEST;

        for (int var19 = 0; var19 < 4; var19++) {
            if (!this.field_74940_h[var19]) {
                int j1 = StructureComponent.OFFSET_X[var19] * 2;
                int k1 = StructureComponent.OFFSET_Z[var19] * 2;
                this.field_74940_h[var19] = this.generateStructureChestContents(
                    par1World, par3StructureBoundingBox, par2Random, 10 + j1, -11, 10 + k1, info.items(par2Random), info.count(par2Random)
                );
            }
        }

        return true;
    }
}
