package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeMushRoom extends ComponentTFMazeRoom {
   public ComponentTFMazeMushRoom(int i, Random rand, int x, int y, int z) {
      super(i, rand, x, y, z);
      this.coordBaseMode = 0;
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);

      for (int x = 1; x < 14; x++) {
         for (int z = 1; z < 14; z++) {
            int dist = (int)Math.round(7.0 / Math.sqrt((7.5 - x) * (7.5 - x) + (7.5 - z) * (7.5 - z)));
            if (rand.nextInt(dist + 1) > 0) {
               this.placeBlockAtCurrentPosition(world, 110, 0, x, 0, z, sbb);
            }

            if (rand.nextInt(dist) > 0) {
               this.placeBlockAtCurrentPosition(world, rand.nextBoolean() ? 40 : 39, 0, x, 1, z, sbb);
            }
         }
      }

      this.makeMediumMushroom(world, sbb, 5, 2, 9, 100);
      this.makeMediumMushroom(world, sbb, 5, 3, 9, 100);
      this.makeMediumMushroom(world, sbb, 9, 2, 5, 100);
      this.makeMediumMushroom(world, sbb, 6, 3, 4, 99);
      this.makeMediumMushroom(world, sbb, 10, 1, 9, 99);
      this.placeBlockAtCurrentPosition(world, 100, 15, 1, 2, 1, sbb);
      this.placeBlockAtCurrentPosition(world, 100, 5, 1, 3, 1, sbb);
      this.placeBlockAtCurrentPosition(world, 100, 9, 2, 3, 1, sbb);
      this.placeBlockAtCurrentPosition(world, 100, 9, 1, 3, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 15, 14, 3, 1, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 5, 14, 4, 1, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 7, 13, 4, 1, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 7, 14, 4, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 15, 1, 1, 14, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 5, 1, 2, 14, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 3, 2, 2, 14, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 3, 1, 2, 13, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 5, 14, 1, 14, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 1, 13, 1, 14, sbb);
      this.placeBlockAtCurrentPosition(world, 99, 1, 14, 1, 13, sbb);
      return true;
   }

   private void makeMediumMushroom(OldWorld world, StructureBoundingBox sbb, int mx, int my, int mz, int blockID) {
      this.placeBlockAtCurrentPosition(world, blockID, 5, mx + 0, my, mz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, blockID, 6, mx + 1, my, mz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, blockID, 9, mx + 1, my, mz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, blockID, 8, mx + 0, my, mz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, blockID, 7, mx - 1, my, mz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, blockID, 4, mx - 1, my, mz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, blockID, 1, mx - 1, my, mz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, blockID, 2, mx + 0, my, mz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, blockID, 3, mx + 1, my, mz - 1, sbb);

      for (int y = 1; y < my; y++) {
         this.placeBlockAtCurrentPosition(world, blockID, 10, mx + 0, y, mz + 0, sbb);
      }
   }
}
