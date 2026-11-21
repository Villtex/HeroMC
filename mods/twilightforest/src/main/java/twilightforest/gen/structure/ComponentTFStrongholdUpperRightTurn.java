package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdUpperRightTurn extends StructureTFStrongholdComponent {
   public ComponentTFStrongholdUpperRightTurn(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -2, -1, 0, 5, 5, 5, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addNewUpperComponent(parent, list, random, 1, -1, 1, 2);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      if (this.isLiquidInStructureBoundingBox(world, sbb)) {
         return false;
      }

      this.placeUpperStrongholdWalls(world, sbb, 0, 0, 0, 4, 4, 4, rand, this.deco.randomBlocks);
      this.placeSmallDoorwayAt(world, rand, 2, 2, 1, 0, sbb);
      this.placeSmallDoorwayAt(world, rand, 1, 0, 1, 2, sbb);
      return true;
   }
}
