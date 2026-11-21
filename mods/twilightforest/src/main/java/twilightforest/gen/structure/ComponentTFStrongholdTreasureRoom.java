package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdTreasureRoom extends StructureTFStrongholdComponent {
   private boolean enterBottom;

   public ComponentTFStrongholdTreasureRoom(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 9, 7, 18, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addDoor(4, 1, 0);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 8, 6, 17, rand, this.deco.randomBlocks);
      this.placeWallStatue(world, 1, 1, 4, 1, sbb);
      this.placeWallStatue(world, 1, 1, 13, 1, sbb);
      this.placeWallStatue(world, 7, 1, 4, 3, sbb);
      this.placeWallStatue(world, 7, 1, 13, 3, sbb);
      this.placeWallStatue(world, 4, 1, 16, 0, sbb);
      this.fillWithRandomizedBlocks(world, sbb, 1, 1, 8, 7, 5, 9, false, rand, this.deco.randomBlocks);
      this.fillWithBlocks(world, sbb, 3, 1, 8, 5, 4, 9, 101, 0, false);
      this.placeSpawnerAtCurrentPosition(world, rand, 4, 1, 4, "TwilightForest.Helmet Crab", sbb);
      this.placeSpawnerAtCurrentPosition(world, rand, 4, 4, 15, "TwilightForest.Helmet Crab", sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 2, 4, 13, TFTreasure.STRONGHOLD_ROOM, sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 6, 4, 13, TFTreasure.STRONGHOLD_ROOM, sbb);
      this.placeDoors(world, rand, sbb);
      return true;
   }

   @Override
   protected void placeDoorwayAt(OldWorld world, Random rand, int x, int y, int z, StructureBoundingBox sbb) {
      if (x != 0 && x != this.getXSize()) {
         this.fillWithMetadataBlocks(world, sbb, x - 1, y, z, x + 1, y + 3, z, 101, 0, 0, 0, false);
      } else {
         this.fillWithMetadataBlocks(world, sbb, x, y, z - 1, x, y + 3, z + 1, 101, 0, 0, 0, false);
      }
   }
}
