package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeDeadEndChest extends ComponentTFMazeDeadEnd {
   public ComponentTFMazeDeadEndChest(int i, int x, int y, int z, int rotation) {
      super(i, x, y, z, rotation);
      this.spawnListIndex = Integer.MAX_VALUE;
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeBlockAtCurrentPosition(world, 5, 0, 2, 1, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 0, 3, 1, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(1), 2, 1, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(1), 3, 1, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 54, 0, 2, 2, 4, sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 3, 2, 4, TFTreasure.LABYRINTH_DEADEND, sbb);
      this.fillWithMetadataBlocks(world, sbb, 1, 1, 0, 4, 3, 1, 1362, 2, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 1, 4, 0, 4, 4, 1, 1362, 3, 0, 0, false);
      this.fillWithBlocks(world, sbb, 2, 1, 0, 3, 3, 1, 101, 0, false);
      return true;
   }
}
