package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeDeadEnd extends StructureTFComponent {
   public ComponentTFMazeDeadEnd(int i, int x, int y, int z, int rotation) {
      super(i);
      this.coordBaseMode = rotation;
      this.boundingBox = new StructureBoundingBox(x, y, z, x + 5, y + 5, z + 5);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithMetadataBlocks(world, sbb, 1, 1, 0, 4, 4, 0, 85, 0, 0, 0, false);
      this.fillWithAir(world, sbb, 2, 1, 0, 3, 3, 0);
      return true;
   }
}
