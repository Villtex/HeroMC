package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFTowerRoofSlab extends ComponentTFTowerRoof {
   public ComponentTFTowerRoofSlab(int i, ComponentTFTowerWing wing) {
      super(i, wing);
      this.setCoordBaseMode(wing.getCoordBaseMode());
      this.size = wing.size;
      this.height = this.size / 2;
      this.makeCapBB(wing);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int slabMeta = 2;
      return this.makePyramidCap(world, slabMeta, sbb);
   }

   protected boolean makePyramidCap(OldWorld world, int slabMeta, StructureBoundingBox sbb) {
      for (int y = 0; y <= this.height; y++) {
         int min = 2 * y;
         int max = this.size - 2 * y - 1;

         for (int x = min; x <= max; x++) {
            for (int z = min; z <= max; z++) {
               if (x != min && x != max && z != min && z != max) {
                  this.placeBlockAtCurrentPosition(world, 5, slabMeta, x, y, z, sbb);
               } else {
                  this.placeBlockAtCurrentPosition(world, 126, slabMeta, x, y, z, sbb);
               }
            }
         }
      }

      return true;
   }

   protected boolean makeConnectedCap(OldWorld world, int slabMeta, StructureBoundingBox sbb) {
      for (int y = 0; y < this.height; y++) {
         int min = 2 * y;
         int max = this.size - 2 * y - 1;

         for (int x = 0; x <= max; x++) {
            for (int z = min; z <= max; z++) {
               if (x != max && z != min && z != max) {
                  this.placeBlockAtCurrentPosition(world, 5, slabMeta, x, y, z, sbb);
               } else {
                  this.placeBlockAtCurrentPosition(world, 126, slabMeta, x, y, z, sbb);
               }
            }
         }
      }

      return true;
   }
}
