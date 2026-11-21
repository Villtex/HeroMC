package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFHydraLair extends ComponentTFHollowHill {
   public ComponentTFHydraLair(OldWorld world, Random rand, int i, int x, int y, int z) {
      super(world, rand, i, 2, x, y + 2, z);
   }

   @Override
   public void buildComponent(StructureComponent structurecomponent, List list, Random random) {
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int stalacts = 64;
      int stalags = 8;

      for (int i = 0; i < stalacts; i++) {
         int[] dest = this.getCoordsInHill2D(rand);
         this.generateOreStalactite(world, dest[0], 1, dest[1], sbb);
      }

      for (int i = 0; i < stalacts; i++) {
         int[] dest = this.getCoordsInHill2D(rand);
         this.generateStoneStalactite(world, 1.0F, true, dest[0], 1, dest[1], sbb);
      }

      for (int i = 0; i < stalags; i++) {
         int[] dest = this.getCoordsInHill2D(rand);
         this.generateStoneStalactite(world, 0.9F, false, dest[0], 1, dest[1], sbb);
      }

      this.placeBlockAtCurrentPosition(world, 1351, 2, 27, 3, 27, sbb);
      return true;
   }
}
