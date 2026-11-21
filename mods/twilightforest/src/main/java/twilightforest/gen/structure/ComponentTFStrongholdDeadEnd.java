package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdDeadEnd extends StructureTFStrongholdComponent {
   public ComponentTFStrongholdDeadEnd(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 9, 6, 9, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addDoor(4, 1, 0);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 8, 6, 8, rand, this.deco.randomBlocks);
      this.placeWallStatue(world, 1, 1, 4, 1, sbb);
      this.placeWallStatue(world, 7, 1, 4, 3, sbb);
      this.placeWallStatue(world, 4, 1, 7, 0, sbb);
      this.placeDoors(world, rand, sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 4, 1, 3, TFTreasure.STRONGHOLD_CACHE, sbb);

      for (int z = 2; z < 5; z++) {
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(0), 3, 1, z, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(2), 5, 1, z, sbb);
      }

      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1), 4, 1, 2, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(3), 4, 1, 4, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1), 4, 2, 3, sbb);
      return true;
   }
}
