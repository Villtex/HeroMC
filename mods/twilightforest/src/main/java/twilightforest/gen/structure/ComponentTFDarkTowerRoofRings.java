package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFDarkTowerRoofRings extends ComponentTFDarkTowerRoof {
   public ComponentTFDarkTowerRoofRings(int i, ComponentTFTowerWing wing) {
      super(i, wing);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);

      for (int y = 1; y < 10; y++) {
         this.placeBlockAtCurrentPosition(world, this.deco.blockID, this.deco.blockMeta, this.size / 2, y, this.size / 2, sbb);
      }

      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2, 10, this.size / 2, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 - 1, 1, this.size / 2, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 + 1, 1, this.size / 2, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2, 1, this.size / 2 - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2, 1, this.size / 2 + 1, sbb);
      this.makeARing(world, 6, sbb);
      this.makeARing(world, 8, sbb);
      return true;
   }

   protected void makeARing(OldWorld world, int y, StructureBoundingBox sbb) {
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 - 2, y, this.size / 2 + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 - 2, y, this.size / 2 + 0, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 - 2, y, this.size / 2 - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 + 2, y, this.size / 2 + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 + 2, y, this.size / 2 + 0, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 + 2, y, this.size / 2 - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 + 1, y, this.size / 2 - 2, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 + 0, y, this.size / 2 - 2, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 - 1, y, this.size / 2 - 2, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 + 1, y, this.size / 2 + 2, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 + 0, y, this.size / 2 + 2, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size / 2 - 1, y, this.size / 2 + 2, sbb);
   }
}
