package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFTowerOutbuilding extends ComponentTFTowerWing {
   protected ComponentTFTowerOutbuilding(int i, int x, int y, int z, int pSize, int pHeight, int direction) {
      super(i, x, y, z, pSize, pHeight, direction);
   }

   @Override
   public void makeABeard(StructureComponent parent, List list, Random rand) {
   }

   @Override
   public boolean makeTowerWing(List list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, int direction) {
      return y > 7 ? super.makeTowerWing(list, rand, index, x, y, z, wingSize, wingHeight, direction) : false;
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int x = 0; x < this.size; x++) {
         for (int z = 0; z < this.size; z++) {
            this.fillCurrentPositionBlocksDownwards(world, 4, 0, x, -1, z, sbb);
         }
      }

      return super.addComponentParts(world, rand, sbb);
   }
}
