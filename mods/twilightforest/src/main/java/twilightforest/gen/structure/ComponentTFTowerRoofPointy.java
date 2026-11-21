package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFTowerRoofPointy extends ComponentTFTowerRoof {
   public ComponentTFTowerRoofPointy(int i, ComponentTFTowerWing wing) {
      super(i, wing);
      this.setCoordBaseMode(wing.getCoordBaseMode());
      this.size = wing.size;
      this.height = this.size;
      this.makeCapBB(wing);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int slabMeta = 2;

      for (int y = 0; y <= this.height; y++) {
         int slopeChange = this.slopeChangeForSize(this.size);
         int min;
         int max;
         if (y < slopeChange) {
            min = y;
            max = this.size - y - 1;
         } else {
            min = (y + slopeChange) / 2;
            max = this.size - (y + slopeChange) / 2 - 1;
         }

         int mid = min + (max - min) / 2;

         for (int x = min; x <= max; x++) {
            for (int z = min; z <= max; z++) {
               this.placeBlockAtCurrentPosition(world, 5, slabMeta, x, y, z, sbb);
               if (x == min && (z == min || z == max) || x == max && (z == min || z == max)) {
                  this.placeBlockAtCurrentPosition(world, 126, slabMeta, x, y + 1, z, sbb);
               }

               if (((x == min || x == max) && z == mid && x % 2 == 0 || (z == min || z == max) && x == mid && z % 2 == 0) && mid != min + 1) {
                  this.placeBlockAtCurrentPosition(world, 126, slabMeta, x, y + 1, z, sbb);
               }
            }
         }
      }

      return true;
   }

   public int slopeChangeForSize(int pSize) {
      if (this.size > 10) {
         return 3;
      } else {
         return this.size > 6 ? 2 : 1;
      }
   }
}
