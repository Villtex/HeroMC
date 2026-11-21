package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdTrainingRoom extends StructureTFStrongholdComponent {
   public ComponentTFStrongholdTrainingRoom(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -13, -1, 0, 18, 6, 18, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addDoor(13, 1, 0);
      this.addNewComponent(parent, list, random, 0, 4, 1, 18);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 17, 6, 17, rand, this.deco.randomBlocks);
      this.placeCornerStatue(world, 2, 1, 2, 0, sbb);
      this.placeCornerStatue(world, 15, 1, 15, 3, sbb);
      this.randomlyFillWithBlocks(world, sbb, rand, 0.7F, 4, 0, 4, 8, 0, 8, 12, 12, false);
      this.randomlyFillWithBlocks(world, sbb, rand, 0.7F, 9, 0, 4, 13, 0, 8, 12, 12, false);
      this.randomlyFillWithBlocks(world, sbb, rand, 0.7F, 9, 0, 9, 13, 0, 13, 12, 12, false);
      this.placeTrainingDummy(world, sbb, 0);
      this.placeTrainingDummy(world, sbb, 1);
      this.placeTrainingDummy(world, sbb, 2);
      this.fillWithBlocks(world, sbb, 5, 0, 10, 7, 0, 12, 4, 0, false);
      this.placeBlockAtCurrentPosition(world, this.deco.pillarID, this.deco.pillarMeta, 5, 1, 12, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.pillarID, this.deco.pillarMeta, 5, 2, 12, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.pillarID, this.deco.pillarMeta, 6, 1, 12, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(2), 6, 2, 12, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(2), 7, 1, 12, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.pillarID, this.deco.pillarMeta, 5, 1, 11, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1), 5, 2, 11, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1), 5, 1, 10, sbb);
      this.placeBlockAtCurrentPosition(world, 145, 0, 6, 1, 11, sbb);
      this.placeDoors(world, rand, sbb);
      return true;
   }

   private void placeTrainingDummy(OldWorld world, StructureBoundingBox sbb, int rotation) {
      this.fillBlocksRotated(world, sbb, 5, 0, 5, 7, 0, 7, 12, 0, rotation);
      this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 6, 1, 6, rotation, sbb);
      this.placeBlockRotated(world, 5, 2, 6, 2, 6, rotation, sbb);
      this.placeBlockRotated(world, 85, 0, 5, 2, 6, rotation, sbb);
      this.placeBlockRotated(world, 85, 0, 7, 2, 6, rotation, sbb);
      this.placeBlockRotated(world, 86, this.getStairMeta(0 + rotation), 6, 3, 6, rotation, sbb);
   }
}
