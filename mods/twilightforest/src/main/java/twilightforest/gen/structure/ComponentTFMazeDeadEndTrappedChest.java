package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeDeadEndTrappedChest extends ComponentTFMazeDeadEndChest {
   public ComponentTFMazeDeadEndTrappedChest(int i, int x, int y, int z, int rotation) {
      super(i, x, y, z, rotation);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);
      this.placeBlockAtCurrentPosition(world, 131, this.getHookMeta(3), 1, 1, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 131, this.getHookMeta(1), 4, 1, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 132, 0, 2, 1, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 132, 0, 3, 1, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 46, 0, 0, 0, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 0, -1, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 1, -1, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 46, 0, 2, 0, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 46, 0, 3, 0, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 46, 0, 2, 0, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 46, 0, 3, 0, 3, sbb);
      return true;
   }

   protected int getHookMeta(int dir) {
      return (this.getCoordBaseMode() + dir) % 4;
   }
}
