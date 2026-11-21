package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdSmallHallway extends StructureTFStrongholdComponent {
   public ComponentTFStrongholdSmallHallway(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 9, 6, 18, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addDoor(4, 1, 0);
      this.addNewComponent(parent, list, random, 0, 4, 1, 18);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 8, 6, 17, rand, this.deco.randomBlocks);
      this.placeWallStatue(world, 1, 1, 9, 1, sbb);
      this.placeWallStatue(world, 7, 1, 9, 3, sbb);
      this.placeDoors(world, rand, sbb);
      return true;
   }
}
