package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFTowerRoofSlabForwards extends ComponentTFTowerRoofSlab {
   public ComponentTFTowerRoofSlabForwards(int i, ComponentTFTowerWing wing) {
      super(i, wing);
      this.setCoordBaseMode(wing.getCoordBaseMode());
      this.size = wing.size + 2;
      this.height = this.size / 2;
      this.makeAttachedOverhangBB(wing);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int y = 0; y <= this.height; y++) {
         int min = 2 * y;
         int max = this.size - 2 * y - 1;

         for (int x = 0; x <= max - 1; x++) {
            for (int z = min; z <= max; z++) {
               if (x != max - 1 && z != min && z != max) {
                  this.placeBlockAtCurrentPosition(world, 125, 2, x, y, z, sbb);
               } else {
                  this.placeBlockAtCurrentPosition(world, 126, 2, x, y, z, sbb);
               }
            }
         }
      }

      return true;
   }
}
