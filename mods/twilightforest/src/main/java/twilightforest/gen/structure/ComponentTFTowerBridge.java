package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFTowerBridge extends ComponentTFTowerWing {
   int dSize;
   int dHeight;

   protected ComponentTFTowerBridge(int i, int x, int y, int z, int pSize, int pHeight, int direction) {
      super(i, x, y, z, 3, 3, direction);
      this.dSize = pSize;
      this.dHeight = pHeight;
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random rand) {
      int[] dest = new int[]{2, 1, 1};
      this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], this.dSize, this.dHeight, 0);
   }

   public StructureBoundingBox getWingBB() {
      int[] dest = this.offsetTowerCoords(2, 1, 1, this.dSize, this.getCoordBaseMode());
      return StructureTFComponent.getComponentToAddBoundingBox(
         dest[0], dest[1], dest[2], 0, 0, 0, this.dSize - 1, this.dHeight - 1, this.dSize - 1, this.getCoordBaseMode()
      );
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int x = 0; x < 3; x++) {
         this.placeBlockAtCurrentPosition(world, 85, 0, x, 2, 0, sbb);
         this.placeBlockAtCurrentPosition(world, 85, 0, x, 2, 2, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, x, 1, 0, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, x, 1, 2, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, x, 0, 0, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, x, 0, 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, x, 0, 2, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, x, -1, 1, sbb);
      }

      this.placeBlockAtCurrentPosition(world, 98, 0, -1, -1, 1, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 0, 3, -1, 1, sbb);
      this.fillWithAir(world, sbb, 0, 1, 1, 2, 2, 1);
      return true;
   }
}
