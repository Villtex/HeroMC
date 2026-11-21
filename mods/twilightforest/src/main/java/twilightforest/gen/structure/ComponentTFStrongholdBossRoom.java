package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdBossRoom extends StructureTFStrongholdComponent {
   boolean enterBottom;

   public ComponentTFStrongholdBossRoom(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -13, -1, 0, 27, 7, 27, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addDoor(13, 1, 0);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 26, 6, 26, rand, this.deco.randomBlocks);
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
