package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFTowerRoofFence extends ComponentTFTowerRoof {
   public ComponentTFTowerRoofFence(int i, ComponentTFTowerWing wing) {
      super(i, wing);
      this.setCoordBaseMode(wing.getCoordBaseMode());
      this.size = wing.size;
      this.height = 0;
      this.makeCapBB(wing);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int y = this.height + 1;

      for (int x = 0; x <= this.size - 1; x++) {
         for (int z = 0; z <= this.size - 1; z++) {
            if (x == 0 || x == this.size - 1 || z == 0 || z == this.size - 1) {
               this.placeBlockAtCurrentPosition(world, 85, 0, x, y, z, sbb);
            }
         }
      }

      return true;
   }
}
