package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeRoomVault extends ComponentTFMazeRoom {
   public ComponentTFMazeRoomVault(int i, Random rand, int x, int y, int z) {
      super(i, rand, x, y, z);
      this.spawnListIndex = Integer.MAX_VALUE;
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithMetadataBlocks(world, sbb, 0, 1, 0, 15, 4, 15, 1362, 3, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 0, 2, 0, 15, 3, 15, 1362, 1, 0, 0, false);
      this.fillWithAir(world, sbb, 6, 2, 6, 9, 3, 9);
      this.fillWithMetadataBlocks(world, sbb, 6, 2, 5, 9, 2, 5, 72, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 6, 2, 10, 9, 2, 10, 72, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 5, 2, 6, 5, 2, 9, 72, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 10, 2, 6, 10, 2, 9, 72, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 6, 4, 5, 9, 4, 5, 12, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 6, 4, 10, 9, 4, 10, 12, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 5, 4, 6, 5, 4, 9, 12, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 10, 4, 6, 10, 4, 9, 12, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 6, 0, 5, 9, 0, 5, 46, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 6, 0, 10, 9, 0, 10, 46, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 5, 0, 6, 5, 0, 9, 46, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 10, 0, 6, 10, 0, 9, 46, 0, 0, 0, false);
      this.placeBlockAtCurrentPosition(world, 54, 0, 7, 2, 6, sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 8, 2, 6, TFTreasure.LABYRINTH_VAULT, sbb);
      this.placeBlockAtCurrentPosition(world, 54, 0, 8, 2, 9, sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 7, 2, 9, TFTreasure.LABYRINTH_VAULT, sbb);
      this.placeBlockAtCurrentPosition(world, 54, 0, 6, 2, 7, sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 6, 2, 8, TFTreasure.LABYRINTH_VAULT, sbb);
      this.placeBlockAtCurrentPosition(world, 54, 0, 9, 2, 8, sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 9, 2, 7, TFTreasure.LABYRINTH_VAULT, sbb);
      return true;
   }
}
