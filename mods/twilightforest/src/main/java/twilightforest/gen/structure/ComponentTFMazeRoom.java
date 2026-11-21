package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFMazeRoom extends StructureTFComponent {
   public ComponentTFMazeRoom(int i, Random rand, int x, int y, int z) {
      super(i);
      this.coordBaseMode = rand.nextInt(4);
      this.boundingBox = new StructureBoundingBox(x, y, z, x + 15, y + 4, z + 15);
   }

   public void buildComponent(StructureComponent structurecomponent, List list, Random random) {
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithMetadataBlocks(world, sbb, 1, 0, 1, 14, 0, 14, 1362, 7, 0, 0, true);
      this.fillWithMetadataBlocks(world, sbb, 2, 0, 2, 13, 0, 13, 1362, 6, 0, 0, true);
      if (this.getBlockIdAtCurrentPosition(world, 7, 1, 0, sbb) == 0) {
         this.fillWithBlocks(world, sbb, 6, 1, 0, 9, 4, 0, 85, 0, false);
         this.fillWithAir(world, sbb, 7, 1, 0, 8, 3, 0);
      }

      if (this.getBlockIdAtCurrentPosition(world, 7, 1, 15, sbb) == 0) {
         this.fillWithBlocks(world, sbb, 6, 1, 15, 9, 4, 15, 85, 0, false);
         this.fillWithAir(world, sbb, 7, 1, 15, 8, 3, 15);
      }

      if (this.getBlockIdAtCurrentPosition(world, 0, 1, 7, sbb) == 0) {
         this.fillWithBlocks(world, sbb, 0, 1, 6, 0, 4, 9, 85, 0, false);
         this.fillWithAir(world, sbb, 0, 1, 7, 0, 3, 8);
      }

      if (this.getBlockIdAtCurrentPosition(world, 15, 1, 7, sbb) == 0) {
         this.fillWithBlocks(world, sbb, 15, 1, 6, 15, 4, 9, 85, 0, false);
         this.fillWithAir(world, sbb, 15, 1, 7, 15, 3, 8);
      }

      return true;
   }
}
