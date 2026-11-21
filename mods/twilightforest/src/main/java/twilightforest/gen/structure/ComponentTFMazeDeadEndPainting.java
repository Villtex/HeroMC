package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeDeadEndPainting extends ComponentTFMazeDeadEnd {
   public ComponentTFMazeDeadEndPainting(int i, int x, int y, int z, int rotation) {
      super(i, x, y, z, rotation);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, 1, 3, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, 4, 3, 3, sbb);
      return true;
   }
}
