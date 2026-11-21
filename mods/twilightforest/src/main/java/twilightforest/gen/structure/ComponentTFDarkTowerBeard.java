package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFDarkTowerBeard extends StructureTFComponent {
   protected int size;
   protected int height;

   public ComponentTFDarkTowerBeard(int i, ComponentTFTowerWing wing) {
      super(i);
      this.setCoordBaseMode(wing.getCoordBaseMode());
      this.size = wing.size;
      this.height = this.size / 2;
      this.boundingBox = new StructureBoundingBox(
         wing.getBoundingBox().minX,
         wing.getBoundingBox().minY - this.height,
         wing.getBoundingBox().minZ,
         wing.getBoundingBox().maxX,
         wing.getBoundingBox().minY,
         wing.getBoundingBox().maxZ
      );
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.makeDarkBeard(world, sbb, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1);
      return true;
   }

   protected void makeDarkBeard(OldWorld world, StructureBoundingBox sbb, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      int frameID = 1372;
      int frameMeta = 1;

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            if (x == minX || x == maxX || z == minZ || z == maxZ) {
               int length = Math.min(Math.abs(x - this.height) - 1, Math.abs(z - this.height) - 1);
               if (length == this.height - 1) {
                  length++;
               }

               if (length == -1) {
                  length = 1;
               }

               for (int y = maxY; y >= this.height - length; y--) {
                  this.placeBlockAtCurrentPosition(world, frameID, frameMeta, x, y, z, sbb);
               }
            }
         }
      }
   }
}
