package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdAtrium extends StructureTFStrongholdComponent {
   private boolean enterBottom;

   public ComponentTFStrongholdAtrium(int i, int facing, int x, int y, int z) {
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
         ? StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 18, 14, 18, facing)
         : StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -13, -8, 0, 18, 14, 18, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      if (this.enterBottom) {
         this.addDoor(4, 1, 0);
         this.addNewComponent(parent, list, random, 2, 13, 8, -1);
      } else {
         this.addDoor(13, 8, 0);
         this.addNewComponent(parent, list, random, 2, 4, 1, -1);
      }

      this.addNewComponent(parent, list, random, 0, 13, 1, 18);
      this.addNewComponent(parent, list, random, 0, 4, 8, 18);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 17, 13, 17, rand, this.deco.randomBlocks);
      this.fillWithRandomizedBlocks(world, sbb, 1, 6, 1, 16, 7, 16, false, rand, this.deco.randomBlocks);
      this.fillWithBlocks(world, sbb, 5, 8, 5, 12, 8, 12, this.deco.fenceID, this.deco.fenceMeta, false);
      this.fillWithAir(world, sbb, 6, 6, 6, 11, 8, 11);
      this.placeBalconyPillar(world, sbb, 0);
      this.placeBalconyPillar(world, sbb, 1);
      this.placeBalconyPillar(world, sbb, 2);
      this.placeBalconyPillar(world, sbb, 3);
      this.fillWithRandomizedBlocks(world, sbb, 1, 1, 1, 1, 12, 2, false, rand, this.deco.randomBlocks);
      this.fillWithRandomizedBlocks(world, sbb, 2, 1, 1, 2, 12, 1, false, rand, this.deco.randomBlocks);
      this.fillWithRandomizedBlocks(world, sbb, 16, 1, 1, 16, 12, 2, false, rand, this.deco.randomBlocks);
      this.fillWithRandomizedBlocks(world, sbb, 15, 1, 1, 15, 12, 1, false, rand, this.deco.randomBlocks);
      this.fillWithRandomizedBlocks(world, sbb, 1, 1, 15, 1, 12, 16, false, rand, this.deco.randomBlocks);
      this.fillWithRandomizedBlocks(world, sbb, 2, 1, 16, 2, 12, 16, false, rand, this.deco.randomBlocks);
      this.fillWithRandomizedBlocks(world, sbb, 16, 1, 15, 16, 12, 16, false, rand, this.deco.randomBlocks);
      this.fillWithRandomizedBlocks(world, sbb, 15, 1, 16, 15, 12, 16, false, rand, this.deco.randomBlocks);
      this.randomlyFillWithBlocks(world, sbb, rand, 0.5F, 6, 0, 6, 11, 0, 11, 2, 2, false);
      this.fillWithBlocks(world, sbb, 7, 0, 7, 10, 0, 10, 2, 0, false);
      this.spawnATree(world, rand.nextInt(5), 8, 1, 8, sbb);
      this.placeCornerStatue(world, 2, 8, 2, 0, sbb);
      this.placeCornerStatue(world, 2, 1, 15, 1, sbb);
      this.placeCornerStatue(world, 15, 1, 2, 2, sbb);
      this.placeCornerStatue(world, 15, 8, 15, 3, sbb);
      this.placeDoors(world, rand, sbb);
      return true;
   }

   private void spawnATree(OldWorld world, int treeNum, int x, int y, int z, StructureBoundingBox sbb) {
      int dx = this.getXWithOffset(x, z);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffset(x, z);
      if (sbb.isVecInside(dx, dy, dz)) {
         int minHeight = 8;
         hexxit.worldgen152.core.populate.OldGen treeGen;
         switch (treeNum) {
            case 0:
            default:
               treeGen = new hexxit.worldgen152.core.populate.OldTreeGens.Trees(minHeight, 0, 0, false);
               break;
            case 1:
               treeGen = new hexxit.worldgen152.core.populate.OldTreeGens.Trees(minHeight, 3, 3, false);
               break;
            case 2:
               treeGen = new hexxit.worldgen152.core.populate.OldTreeGens.Trees(minHeight, 2, 2, false);
               break;
            case 3:
               treeGen = new TFTrees.SmallOak(0, minHeight);
               break;
            case 4:
               treeGen = new TFTrees.SmallOak(3);
         }

         // world.rand of the original is not seed-fixed: a random from seed and position (ComponentTFTowerWing.worldRand)
         Random treeRand = ComponentTFTowerWing.worldRand(world, dx, dy, dz);
         for (int i = 0; i < 100 && !treeGen.generate(world, treeRand, dx, dy, dz); i++) {
            if (i == 99) {
            }
         }
      }
   }

   private void placeBalconyPillar(OldWorld world, StructureBoundingBox sbb, int rotation) {
      this.fillBlocksRotated(world, sbb, 5, 1, 5, 5, 12, 5, this.deco.pillarID, this.deco.pillarMeta, rotation);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), 5, 1, 6, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation), 6, 1, 5, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 5, 5, 6, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation) + 4, 6, 5, 5, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 5, 12, 6, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation) + 4, 6, 12, 5, rotation, sbb);
   }
}
