package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeRoomFountain extends ComponentTFMazeRoom {
   public ComponentTFMazeRoomFountain(int i, Random rand, int x, int y, int z) {
      super(i, rand, x, y, z);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);
      this.fillWithMetadataBlocks(world, sbb, 5, 1, 5, 10, 1, 10, 1362, 3, 0, 0, false);
      this.fillWithBlocks(world, sbb, 6, 1, 6, 9, 1, 9, 8, 0, false);
      return true;
   }
}
