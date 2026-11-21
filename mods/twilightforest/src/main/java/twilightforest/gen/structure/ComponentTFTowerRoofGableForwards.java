package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFTowerRoofGableForwards extends ComponentTFTowerRoof {
   public ComponentTFTowerRoofGableForwards(int i, ComponentTFTowerWing wing) {
      super(i, wing);
      this.setCoordBaseMode(wing.getCoordBaseMode());
      this.size = wing.size + 2;
      this.height = this.size;
      this.makeAttachedOverhangBB(wing);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int slabMeta = 2;
      int slopeChange = this.slopeChangeForSize(this.size);

      for (int y = 0; y <= this.height; y++) {
         int min;
         int max;
         if (y < slopeChange) {
            min = y;
            max = this.size - y - 1;
         } else {
            min = (y + slopeChange) / 2;
            max = this.size - (y + slopeChange) / 2 - 1;
         }

         for (int x = 0; x <= this.size - 2; x++) {
            for (int z = min; z <= max; z++) {
               if (z == min || z == max) {
                  this.placeBlockAtCurrentPosition(world, 5, slabMeta, x, y, z, sbb);
               } else if (x < this.size - 2) {
                  this.placeBlockAtCurrentPosition(world, 5, slabMeta, x, y, z, sbb);
               }
            }
         }
      }

      int top = this.size + 1 - slopeChange;
      int zMid = this.size / 2;
      this.placeBlockAtCurrentPosition(world, 126, slabMeta | 10, this.size - 1, top - 1, zMid, sbb);
      this.placeBlockAtCurrentPosition(world, 126, slabMeta, 0, top, zMid, sbb);
      this.placeBlockAtCurrentPosition(world, 126, slabMeta, this.size - 3, top, zMid, sbb);
      this.placeBlockAtCurrentPosition(world, 5, slabMeta, this.size - 2, top, zMid, sbb);
      this.placeBlockAtCurrentPosition(world, 5, slabMeta, this.size - 1, top, zMid, sbb);
      this.placeBlockAtCurrentPosition(world, 5, slabMeta, this.size - 1, top + 1, zMid, sbb);
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
