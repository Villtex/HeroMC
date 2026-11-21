package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFDarkTowerRoofFourPost extends ComponentTFDarkTowerRoof {
   public ComponentTFDarkTowerRoofFourPost(int i, ComponentTFTowerWing wing) {
      super(i, wing);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);
      this.makeSmallAntenna(world, sbb, 4, this.size - 2, this.size - 2);
      this.makeSmallAntenna(world, sbb, 5, 1, this.size - 2);
      this.makeSmallAntenna(world, sbb, 6, this.size - 2, 1);
      this.makeSmallAntenna(world, sbb, 7, 1, 1);
      return true;
   }

   protected void makeSmallAntenna(OldWorld world, StructureBoundingBox sbb, int height, int x, int z) {
      for (int y = 1; y < height; y++) {
         this.placeBlockAtCurrentPosition(world, this.deco.blockID, this.deco.blockMeta, x, y, z, sbb);
      }

      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, height + 0, z, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, height + 1, z, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x + 1, height + 1, z, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x - 1, height + 1, z, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, height + 1, z + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, height + 1, z - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, height + 2, z, sbb);
   }
}
