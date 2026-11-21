package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeCorridorShrooms extends ComponentTFMazeCorridor {
   public ComponentTFMazeCorridorShrooms(int i, int x, int y, int z, int rotation) {
      super(i, x, y, z, rotation);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int x = 1; x < 5; x++) {
         for (int z = 0; z < 5; z++) {
            if (rand.nextInt(2) > 0) {
               this.placeBlockAtCurrentPosition(world, 110, 0, x, 0, z, sbb);
            }

            if (rand.nextInt(2) > 0) {
               this.placeBlockAtCurrentPosition(world, rand.nextBoolean() ? 40 : 39, 0, x, 1, z, sbb);
            }
         }
      }

      int mushType = rand.nextBoolean() ? 100 : 99;
      int mushY = rand.nextInt(4) + 1;
      int mushZ = rand.nextInt(4) + 1;
      this.placeBlockAtCurrentPosition(world, mushType, 15, 1, mushY - 1, mushZ, sbb);
      this.fillWithMetadataBlocks(world, sbb, 1, 1, mushZ, 1, mushY, mushZ, mushType, 10, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 1, mushY, mushZ - 1, 2, mushY, mushZ + 1, mushType, 14, 0, 0, false);
      mushType = mushType == 99 ? 100 : 99;
      mushY = rand.nextInt(4) + 1;
      mushZ = rand.nextInt(4) + 1;
      this.fillWithMetadataBlocks(world, sbb, 4, 1, mushZ, 4, mushY, mushZ, mushType, 10, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 3, mushY, mushZ - 1, 4, mushY, mushZ + 1, mushType, 14, 0, 0, false);
      return true;
   }
}
