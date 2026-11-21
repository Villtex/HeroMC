package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdUpperAscender extends StructureTFStrongholdComponent {
   boolean exitTop;

   public ComponentTFStrongholdUpperAscender(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      if (y < 36) {
         this.exitTop = true;
         return StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -2, -1, 0, 5, 10, 10, facing);
      } else {
         this.exitTop = false;
         return StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -2, -6, 0, 5, 10, 10, facing);
      }
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addNewUpperComponent(parent, list, random, 0, 2, this.exitTop ? 6 : 1, 10);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      if (this.isLiquidInStructureBoundingBox(world, sbb)) {
         return false;
      }

      this.placeUpperStrongholdWalls(world, sbb, 0, 0, 0, 4, 9, 9, rand, this.deco.randomBlocks);
      this.placeSmallDoorwayAt(world, rand, 2, 2, this.exitTop ? 1 : 6, 0, sbb);
      this.placeSmallDoorwayAt(world, rand, 0, 2, this.exitTop ? 6 : 1, 9, sbb);
      if (this.exitTop) {
         this.makeStairsAt(world, 1, 3, 1, sbb);
         this.makeStairsAt(world, 2, 4, 1, sbb);
         this.makeStairsAt(world, 3, 5, 1, sbb);
         this.makeStairsAt(world, 4, 6, 1, sbb);
         this.makeStairsAt(world, 5, 7, 1, sbb);
         this.makePlatformAt(world, 5, 8, sbb);
      } else {
         this.makeStairsAt(world, 1, 6, 3, sbb);
         this.makeStairsAt(world, 2, 5, 3, sbb);
         this.makeStairsAt(world, 3, 4, 3, sbb);
         this.makeStairsAt(world, 4, 3, 3, sbb);
         this.makeStairsAt(world, 5, 2, 3, sbb);
         this.makePlatformAt(world, 5, 1, sbb);
      }

      return true;
   }

   private void makeStairsAt(OldWorld world, int y, int z, int facing, StructureBoundingBox sbb) {
      if (this.getBlockIdAtCurrentPosition(world, 0, y, z, sbb) != 0 || this.getBlockIdAtCurrentPosition(world, 4, y, z, sbb) != 0) {
         for (int x = 1; x < 4; x++) {
            this.placeBlockAtCurrentPosition(world, 109, this.getStairMeta(facing), x, y, z, sbb);
         }
      }
   }

   private void makePlatformAt(OldWorld world, int y, int z, StructureBoundingBox sbb) {
      if (this.getBlockIdAtCurrentPosition(world, 0, y, z, sbb) != 0 || this.getBlockIdAtCurrentPosition(world, 4, y, z, sbb) != 0) {
         for (int x = 1; x < 4; x++) {
            this.placeBlockAtCurrentPosition(world, 98, 0, x, y, z, sbb);
         }
      }
   }
}
