package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdAccessChamber extends StructureTFStrongholdComponent {
   public ComponentTFStrongholdAccessChamber(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 8, 6, 8, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addNewUpperComponent(parent, list, random, 0, 4, 1, 9);
      this.addNewUpperComponent(parent, list, random, 1, -1, 1, 4);
      this.addNewUpperComponent(parent, list, random, 2, 4, 1, -1);
      this.addNewUpperComponent(parent, list, random, 3, 9, 1, 4);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithRandomizedBlocks(world, sbb, 0, 0, 0, 8, 4, 8, true, rand, StructureTFComponent.getStrongholdStones());
      this.placeSmallDoorwayAt(world, rand, 0, 4, 1, 8, sbb);
      this.placeSmallDoorwayAt(world, rand, 1, 0, 1, 4, sbb);
      this.placeSmallDoorwayAt(world, rand, 2, 4, 1, 0, sbb);
      this.placeSmallDoorwayAt(world, rand, 3, 8, 1, 4, sbb);
      this.fillWithMetadataBlocks(world, sbb, 2, -2, 2, 6, 0, 6, 98, 1, 0, 0, false);
      this.fillWithAir(world, sbb, 3, -2, 3, 5, 2, 5);
      return true;
   }
}
