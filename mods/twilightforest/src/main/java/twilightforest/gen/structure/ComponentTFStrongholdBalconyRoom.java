package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdBalconyRoom extends StructureTFStrongholdComponent {
   boolean enterBottom;

   public ComponentTFStrongholdBalconyRoom(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      if (y > 17) {
         this.enterBottom = false;
      } else if (y < 11) {
         this.enterBottom = true;
      } else {
         this.enterBottom = (z & 1) == 0;
      }

      return this.enterBottom
         ? StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 18, 14, 27, facing)
         : StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -13, -8, 0, 18, 14, 27, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addNewComponent(parent, list, random, 0, 13, 1, 27);
      this.addNewComponent(parent, list, random, 1, -1, 1, 13);
      this.addNewComponent(parent, list, random, 3, 18, 1, 13);
      this.addNewComponent(parent, list, random, 0, 4, 8, 27);
      this.addNewComponent(parent, list, random, 1, -1, 8, 4);
      this.addNewComponent(parent, list, random, 3, 18, 8, 22);
      if (this.enterBottom) {
         this.addDoor(4, 1, 0);
         this.addNewComponent(parent, list, random, 2, 13, 8, -1);
      } else {
         this.addDoor(13, 8, 0);
         this.addNewComponent(parent, list, random, 2, 4, 1, -1);
      }
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 17, 13, 26, rand, this.deco.randomBlocks);
      this.fillWithRandomizedBlocks(world, sbb, 1, 6, 1, 16, 7, 25, false, rand, this.deco.randomBlocks);
      this.fillWithBlocks(world, sbb, 4, 8, 4, 13, 8, 22, this.deco.fenceID, this.deco.fenceMeta, false);
      this.fillWithAir(world, sbb, 5, 6, 5, 12, 8, 21);
      this.placeStairsAndPillars(world, sbb, 0);
      this.placeStairsAndPillars(world, sbb, 2);
      this.placeDoors(world, rand, sbb);
      return true;
   }

   private void placeStairsAndPillars(OldWorld world, StructureBoundingBox sbb, int rotation) {
      this.fillBlocksRotated(world, sbb, 4, 1, 4, 4, 12, 4, this.deco.pillarID, this.deco.pillarMeta, rotation);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), 4, 1, 5, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation), 5, 1, 4, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 4, 5, 5, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation) + 4, 5, 5, 4, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 4, 12, 5, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation) + 4, 5, 12, 4, rotation, sbb);
      this.fillBlocksRotated(world, sbb, 13, 1, 4, 13, 12, 4, this.deco.pillarID, this.deco.pillarMeta, rotation);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), 13, 1, 5, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation), 12, 1, 4, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 13, 5, 5, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation) + 4, 12, 5, 4, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 13, 12, 5, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation) + 4, 12, 12, 4, rotation, sbb);
      this.fillBlocksRotated(world, sbb, 13, 1, 8, 13, 12, 8, this.deco.pillarID, this.deco.pillarMeta, rotation);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), 13, 1, 9, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation), 13, 1, 7, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation), 12, 1, 8, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 13, 5, 9, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation) + 4, 13, 5, 7, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 13, 12, 9, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation) + 4, 13, 12, 7, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation) + 4, 12, 12, 8, rotation, sbb);

      for (int y = 1; y < 8; y++) {
         for (int z = 5; z < 8; z++) {
            this.placeBlockRotated(world, 0, 0, y + 6, y + 1, z, rotation, sbb);
            this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation), y + 6, y, z, rotation, sbb);
            this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, y + 6, y - 1, z, rotation, sbb);
         }
      }
   }
}
