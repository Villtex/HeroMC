package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFTowerRoofStairs extends ComponentTFTowerRoof {
   public ComponentTFTowerRoofStairs(int i, ComponentTFTowerWing wing) {
      super(i, wing);
      this.setCoordBaseMode(0);
      this.size = wing.size;
      this.height = this.size / 2;
      this.makeCapBB(wing);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int y = 0; y <= this.height; y++) {
         int min = y;
         int max = this.size - y - 1;

         for (int x = min; x <= max; x++) {
            for (int z = min; z <= max; z++) {
               if (x == min) {
                  if (z != min && z != max) {
                     this.placeBlockAtCurrentPosition(world, 135, 0, x, y, z, sbb);
                  } else {
                     this.placeBlockAtCurrentPosition(world, 126, 2, x, y, z, sbb);
                  }
               } else if (x == max) {
                  if (z != min && z != max) {
                     this.placeBlockAtCurrentPosition(world, 135, 1, x, y, z, sbb);
                  } else {
                     this.placeBlockAtCurrentPosition(world, 126, 2, x, y, z, sbb);
                  }
               } else if (z == max) {
                  this.placeBlockAtCurrentPosition(world, 135, 3, x, y, z, sbb);
               } else if (z == min) {
                  this.placeBlockAtCurrentPosition(world, 135, 2, x, y, z, sbb);
               } else {
                  this.placeBlockAtCurrentPosition(world, 5, 2, x, y, z, sbb);
               }
            }
         }
      }

      return true;
   }
}
