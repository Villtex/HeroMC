package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeDeadEndTorches extends ComponentTFMazeDeadEnd {
   public ComponentTFMazeDeadEndTorches(int i, int x, int y, int z, int rotation) {
      super(i, x, y, z, rotation);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);
      this.fillWithMetadataBlocks(world, sbb, 2, 1, 4, 3, 4, 4, 50, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 1, 1, 1, 1, 4, 4, 50, 0, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 4, 1, 1, 4, 4, 4, 50, 0, 0, 0, false);
      return true;
   }
}
