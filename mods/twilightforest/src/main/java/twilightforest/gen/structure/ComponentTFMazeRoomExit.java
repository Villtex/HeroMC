package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeRoomExit extends ComponentTFMazeRoom {
   public ComponentTFMazeRoomExit(int i, Random rand, int x, int y, int z) {
      super(i, rand, x, y, z);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);
      this.fillWithMetadataBlocks(world, sbb, 5, -5, 5, 10, 0, 10, 1362, 1, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 5, 1, 5, 10, 1, 10, 1362, 3, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 5, 2, 5, 10, 3, 10, 101, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 5, 4, 5, 10, 4, 10, 1362, 3, 0, 0, false);
      this.fillWithAir(world, sbb, 6, -5, 6, 9, 4, 9);
      return true;
   }
}
