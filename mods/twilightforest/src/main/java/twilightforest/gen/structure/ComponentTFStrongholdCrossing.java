package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdCrossing extends StructureTFStrongholdComponent {
   public ComponentTFStrongholdCrossing(int i, int facing, int x, int y, int z) {
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
      this.addNewComponent(parent, list, random, 1, -1, 1, 13);
      this.addNewComponent(parent, list, random, 3, 18, 1, 4);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 17, 6, 17, rand, this.deco.randomBlocks);
      this.placeCornerStatue(world, 2, 1, 2, 0, sbb);
      this.placeCornerStatue(world, 15, 1, 15, 3, sbb);
      this.fillBlocksRotated(world, sbb, 8, 1, 8, 9, 5, 9, this.deco.pillarID, this.deco.pillarMeta, 0);
      this.placeWallStatue(world, 8, 1, 7, 0, sbb);
      this.placeWallStatue(world, 7, 1, 9, 3, sbb);
      this.placeWallStatue(world, 9, 1, 10, 2, sbb);
      this.placeWallStatue(world, 10, 1, 8, 1, sbb);
      this.placeTableAndChairs(world, sbb, 0);
      this.placeTableAndChairs(world, sbb, 1);
      this.placeTableAndChairs(world, sbb, 2);
      this.placeTableAndChairs(world, sbb, 3);
      this.placeDoors(world, rand, sbb);
      return true;
   }

   private void placeTableAndChairs(OldWorld world, StructureBoundingBox sbb, int rotation) {
      this.placeBlockRotated(world, 53, this.getStairMeta(0 + rotation) + 4, 5, 1, 3, rotation, sbb);
      this.placeBlockRotated(world, 53, this.getStairMeta(3 + rotation) + 4, 5, 1, 4, rotation, sbb);
      this.placeBlockRotated(world, 53, this.getStairMeta(1 + rotation) + 4, 6, 1, 3, rotation, sbb);
      this.placeBlockRotated(world, 53, this.getStairMeta(2 + rotation) + 4, 6, 1, 4, rotation, sbb);
      this.placeBlockRotated(world, 134, this.getStairMeta(3 + rotation), 5, 1, 2, rotation, sbb);
      this.placeBlockRotated(world, 134, this.getStairMeta(0 + rotation), 7, 1, 3, rotation, sbb);
      this.placeBlockRotated(world, 134, this.getStairMeta(1 + rotation), 6, 1, 5, rotation, sbb);
      this.placeBlockRotated(world, 134, this.getStairMeta(2 + rotation), 4, 1, 4, rotation, sbb);
   }
}
