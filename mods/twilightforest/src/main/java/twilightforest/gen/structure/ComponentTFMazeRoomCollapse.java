package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeRoomCollapse extends ComponentTFMazeRoom {
   public ComponentTFMazeRoomCollapse(int i, Random rand, int x, int y, int z) {
      super(i, rand, x, y, z);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);

      for (int x = 1; x < 14; x++) {
         for (int z = 1; z < 14; z++) {
            int dist = (int)Math.round(7.0 / Math.sqrt((7.5 - x) * (7.5 - x) + (7.5 - z) * (7.5 - z)));
            int gravel = rand.nextInt(dist);
            int root = rand.nextInt(dist);
            if (gravel > 0) {
               this.fillWithBlocks(world, sbb, x, 1, z, x, ++gravel, z, 13, 0, false);
               this.fillWithAir(world, sbb, x, gravel, z, x, gravel + 5, z);
            } else if (root > 0) {
               this.fillWithBlocks(world, sbb, x, 5, z, x, 5 + root, z, 3, 0, true);
               this.fillWithMetadataBlocks(world, sbb, x, 5 - rand.nextInt(5), z, x, 5, z, 1365, 14, 0, 0, false);
            } else if (rand.nextInt(dist + 1) > 0) {
               this.fillWithAir(world, sbb, x, 5, z, x, 5, z);
            }
         }
      }

      return true;
   }
}
