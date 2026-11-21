package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeDeadEndFountain extends ComponentTFMazeDeadEnd {
   public ComponentTFMazeDeadEndFountain(int i, int x, int y, int z, int rotation) {
      super(i, x, y, z, rotation);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);
      this.fillWithMetadataBlocks(world, sbb, 1, 1, 4, 4, 4, 4, 1362, 1, 0, 0, false);
      this.placeBlockAtCurrentPosition(world, 8, 0, 2, 3, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 8, 0, 3, 3, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 2, 0, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 3, 0, 3, sbb);
      return true;
   }
}
