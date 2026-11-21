package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFTowerBeard extends StructureTFComponent {
   int size;
   int height;

   public ComponentTFTowerBeard(int i, ComponentTFTowerWing wing) {
      super(i);
      this.setCoordBaseMode(wing.getCoordBaseMode());
      this.size = wing.size - 2;
      this.height = this.size / 2;
      this.boundingBox = new StructureBoundingBox(
         wing.getBoundingBox().minX + 1,
         wing.getBoundingBox().minY - this.height - 1,
         wing.getBoundingBox().minZ + 1,
         wing.getBoundingBox().maxX - 1,
         wing.getBoundingBox().minY - 1,
         wing.getBoundingBox().maxZ - 1
      );
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      return this.makePyramidBeard(world, rand, sbb);
   }

   private boolean makePyramidBeard(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int y = 0; y <= this.height; y++) {
         int min = y;
         int max = this.size - y - 1;
         this.fillWithRandomizedBlocks(world, sbb, min, this.height - y, min, max, this.height - y, max, false, rand, StructureTFComponent.getStrongholdStones());
      }

      return true;
   }
}
