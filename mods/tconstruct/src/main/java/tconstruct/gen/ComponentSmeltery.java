package tconstruct.gen;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.structure.ComponentVillage;
import hexxit.worldgen152.core.structure.ComponentVillageStartPiece;
import hexxit.worldgen152.core.structure.OldStructures;
import hexxit.worldgen152.core.structure.StructureBoundingBox;
import hexxit.worldgen152.core.structure.StructureComponent;
import hexxit.worldgen152.core.structure.StructureVillagePieceWeight;

import java.util.List;
import java.util.Random;

/** ComponentSmeltery and VillageSmelteryHandler of Tinkers' Construct 1.4.3: a smeltery basin of seared bricks. */
public class ComponentSmeltery extends ComponentVillage {
    private int averageGroundLevel = -1;

    public ComponentSmeltery(ComponentVillageStartPiece par1ComponentVillageStartPiece, int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2);
        this.coordBaseMode = par5;
        this.boundingBox = par4StructureBoundingBox;
    }

    public static ComponentSmeltery buildComponent(ComponentVillageStartPiece villagePiece, List<StructureComponent> pieces, Random random, int p1, int p2, int p3, int p4, int p5) {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 9, 3, 7, p4);
        return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(pieces, structureboundingbox) == null
                ? new ComponentSmeltery(villagePiece, p5, random, structureboundingbox, p4)
                : null;
    }

    /** For a village of today (Hexxit Integrated): the box already stands on the street, no ground level search. */
    public ComponentSmeltery grounded() {
        this.averageGroundLevel = 0;
        return this;
    }

    @Override
    public boolean addComponentParts(OldWorld world, Random random, StructureBoundingBox sbb) {
        if (this.averageGroundLevel < 0) {
            this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);
            if (this.averageGroundLevel < 0) {
                return true;
            }
            this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 2, 0);
        }

        this.fillWithBlocks(world, sbb, 1, 0, 0, 7, 0, 6, 98 /* stoneBrick */, 98 /* stoneBrick */, false);
        this.fillWithBlocks(world, sbb, 0, 0, 1, 0, 0, 5, 98 /* stoneBrick */, 98 /* stoneBrick */, false);
        this.fillWithBlocks(world, sbb, 8, 0, 1, 8, 0, 5, 98 /* stoneBrick */, 98 /* stoneBrick */, false);
        this.fillWithBlocks(world, sbb, 0, 1, 0, 9, 3, 7, 0, 0, false);
        this.fillWithMetaBlocks(world, sbb, 2, 0, 1, 6, 2, 5, TCWorldGen.SMELTERY, 2, TCWorldGen.SMELTERY, 2, false);
        this.fillWithBlocks(world, sbb, 3, 1, 2, 5, 2, 4, 0, 0, false);
        this.placeBlockAtCurrentPosition(world, TCWorldGen.SEARED_TABLE, 0, 1, 1, 2, sbb);
        this.placeBlockAtCurrentPosition(world, TCWorldGen.SEARED_TABLE, 2, 1, 1, 4, sbb);
        this.placeBlockAtCurrentPosition(world, TCWorldGen.SEARED_TABLE, 0, 7, 1, 2, sbb);
        this.placeBlockAtCurrentPosition(world, TCWorldGen.SEARED_TABLE, 2, 7, 1, 4, sbb);

        for (int l = 1; l < 6; l++) {
            for (int i1 = 0; i1 < 9; i1++) {
                this.clearCurrentPositionBlocksUpwards(world, i1, 9, l, sbb);
                this.fillCurrentPositionBlocksDownwards(world, 98 /* stoneBrick */, 0, i1, -1, l, sbb);
            }
        }

        for (int l = 0; l < 7; l++) {
            for (int i1 = 1; i1 < 8; i1++) {
                this.clearCurrentPositionBlocksUpwards(world, i1, 9, l, sbb);
                this.fillCurrentPositionBlocksDownwards(world, 98 /* stoneBrick */, 0, i1, -1, l, sbb);
            }
        }
        return true;
    }

    /** ComponentSmeltery.fillWithMetaBlocks: the village's biome blocks, then StructureComponent.fillWithMetadataBlocks. */
    protected void fillWithMetaBlocks(OldWorld world, StructureBoundingBox sbb, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                      int placeBlockID, int placeBlockMeta, int replaceBlockID, int replaceBlockMeta, boolean alwaysReplace) {
        int i2 = this.getBiomeSpecificBlock(placeBlockID, placeBlockMeta);
        int j2 = this.getBiomeSpecificBlockMetadata(placeBlockID, placeBlockMeta);
        int k2 = this.getBiomeSpecificBlock(replaceBlockID, replaceBlockMeta);
        int l2 = this.getBiomeSpecificBlockMetadata(replaceBlockID, replaceBlockMeta);
        super.fillWithMetadataBlocks(world, sbb, minX, minY, minZ, maxX, maxY, maxZ, i2, j2, k2, l2, alwaysReplace);
    }

    /** VillageSmelteryHandler: weight 9, limit 1 only when {@code i + random.nextInt(10) == 0}. */
    static final class Handler implements OldStructures.VillageHandler {
        @Override
        public StructureVillagePieceWeight getVillagePieceWeight(Random random, int i) {
            return new StructureVillagePieceWeight(ComponentSmeltery.class, 9, i + random.nextInt(10) == 0 ? 1 : 0);
        }

        @Override
        public Class<?> getComponentClass() {
            return ComponentSmeltery.class;
        }

        @Override
        public ComponentVillage buildComponent(StructureVillagePieceWeight weight, ComponentVillageStartPiece start, List<StructureComponent> pieces,
                                               Random random, int x, int y, int z, int direction, int type) {
            return ComponentSmeltery.buildComponent(start, pieces, random, x, y, z, direction, type);
        }
    }
}
