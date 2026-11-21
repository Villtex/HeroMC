package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFTowerBeardAttached extends ComponentTFTowerBeard {
   public ComponentTFTowerBeardAttached(int i, ComponentTFTowerWing wing) {
      super(i, wing);
      this.boundingBox = new StructureBoundingBox(
         wing.getBoundingBox().minX,
         wing.getBoundingBox().minY - this.height - 1,
         wing.getBoundingBox().minZ,
         wing.getBoundingBox().maxX,
         wing.getBoundingBox().minY - 1,
         wing.getBoundingBox().maxZ
      );
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      return this.makeAttachedBeard(world, rand, sbb);
   }

   private boolean makeAttachedBeard(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int y = 0; y <= this.height; y++) {
         int min = y + 1;
         int max = this.size - y;
         this.fillWithRandomizedBlocks(world, sbb, 0, this.height - y, min, max, this.height - y, max, false, rand, StructureTFComponent.getStrongholdStones());
      }

      return true;
   }
}
