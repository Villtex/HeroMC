package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeCorridorRoots extends ComponentTFMazeCorridor {
   public ComponentTFMazeCorridorRoots(int i, int x, int y, int z, int rotation) {
      super(i, x, y, z, rotation);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int x = 1; x < 5; x++) {
         for (int z = 0; z < 5; z++) {
            int freq = x;
            if (rand.nextInt(freq + 2) > 0) {
               int length = rand.nextInt(6);
               this.placeBlockAtCurrentPosition(world, 3, 0, x, 6, z, sbb);

               for (int y = 6 - length; y < 6; y++) {
                  this.placeBlockAtCurrentPosition(world, 1365, 14, x, y, z, sbb);
               }

               if (rand.nextInt(freq + 1) > 1) {
                  this.placeBlockAtCurrentPosition(world, 13, 0, x, 1, z, sbb);
                  if (rand.nextInt(freq + 1) > 1) {
                     this.placeBlockAtCurrentPosition(world, 13, 0, x, 2, z, sbb);
                  }
               }
            }
         }
      }

      return true;
   }
}
