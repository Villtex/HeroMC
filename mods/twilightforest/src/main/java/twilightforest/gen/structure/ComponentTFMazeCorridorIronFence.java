package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeCorridorIronFence extends ComponentTFMazeCorridor {
   public ComponentTFMazeCorridorIronFence(int i, int x, int y, int z, int rotation) {
      super(i, x, y, z, rotation);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithMetadataBlocks(world, sbb, 1, 4, 2, 4, 4, 3, 1362, 3, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 1, 1, 2, 4, 3, 3, 1362, 2, 0, 0, false);
      this.fillWithBlocks(world, sbb, 2, 1, 2, 3, 3, 3, 101, 0, false);
      return true;
   }
}
