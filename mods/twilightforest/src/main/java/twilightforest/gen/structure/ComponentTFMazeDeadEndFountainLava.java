package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeDeadEndFountainLava extends ComponentTFMazeDeadEndFountain {
   public ComponentTFMazeDeadEndFountainLava(int i, int x, int y, int z, int rotation) {
      super(i, x, y, z, rotation);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 2, 3, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 3, 3, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 10, 0, 2, 3, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 10, 0, 3, 3, 4, sbb);
      return true;
   }
}
