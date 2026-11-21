package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerBridge extends ComponentTFTowerWing {
   int dSize;
   int dHeight;

   protected ComponentTFDarkTowerBridge(int i, int x, int y, int z, int pSize, int pHeight, int direction) {
      super(i, x, y, z, 5, 5, direction);
      this.dSize = pSize;
      this.dHeight = pHeight;
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random rand) {
      if (parent != null && parent instanceof StructureTFComponent) {
         this.deco = ((StructureTFComponent)parent).deco;
      }

      this.makeTowerWing(list, rand, this.getComponentType(), 4, 1, 2, this.dSize, this.dHeight, 0);
   }

   @Override
   public boolean makeTowerWing(List list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, int rotation) {
      if (wingHeight < 6) {
         return false;
      }

      int direction = (this.getCoordBaseMode() + rotation) % 4;
      int[] dx = this.offsetTowerCoords(x, y, z, wingSize, direction);
      if (dx[1] + wingHeight > 255) {
         return false;
      }

      ComponentTFTowerWing wing = new ComponentTFDarkTowerWing(index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
      StructureComponent intersect = StructureComponent.findIntersecting(list, wing.getBoundingBox());
      if (intersect != null && intersect != this) {
         return false;
      }

      list.add(wing);
      wing.buildComponent(this, list, rand);
      this.addOpening(x, y, z, rotation);
      return true;
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithBlocks(world, sbb, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1, this.deco.blockID, this.deco.blockMeta, false);

      for (int x = 0; x < this.size; x++) {
         this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, 0, 0, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, this.height - 1, 0, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, 0, this.size - 1, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, this.height - 1, this.size - 1, sbb);
      }

      this.nullifySkyLightForBoundingBox(world);
      this.fillWithAir(world, sbb, 0, 1, 1, this.size - 1, this.height - 2, this.size - 2);
      return true;
   }

   public StructureBoundingBox getWingBB() {
      int[] dest = this.offsetTowerCoords(4, 1, 2, this.dSize, this.getCoordBaseMode());
      return StructureTFComponent.getComponentToAddBoundingBox(
         dest[0], dest[1], dest[2], 0, 0, 0, this.dSize - 1, this.dHeight - 1, this.dSize - 1, this.getCoordBaseMode()
      );
   }
}
