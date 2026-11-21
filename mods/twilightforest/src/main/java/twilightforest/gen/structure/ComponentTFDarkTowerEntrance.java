package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerEntrance extends ComponentTFDarkTowerWing {
   protected ComponentTFDarkTowerEntrance(int i, int x, int y, int z, int pSize, int pHeight, int direction) {
      super(i, x, y, z, pSize, pHeight, direction);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random rand) {
      super.buildComponent(parent, list, rand);
      this.addOpening(this.size / 2, 1, 0, 1, EnumDarkTowerDoor.REAPPEARING);
      this.addOpening(this.size / 2, 1, this.size - 1, 3, EnumDarkTowerDoor.REAPPEARING);
   }

   @Override
   public void makeABeard(StructureComponent parent, List list, Random rand) {
   }

   @Override
   public void makeARoof(StructureComponent parent, List list, Random rand) {
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      new Random(world.seed() + this.boundingBox.minX * 321534781 ^ this.boundingBox.minZ * 756839);
      this.makeEncasedWalls(world, rand, sbb, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1);

      for (int x = 0; x < this.size; x++) {
         for (int z = 0; z < this.size; z++) {
            this.fillCurrentPositionBlocksDownwards(world, this.deco.accentID, this.deco.accentMeta, x, -1, z, sbb);
         }
      }

      this.fillWithAir(world, sbb, 1, 1, 1, this.size - 2, this.height - 2, this.size - 2);
      this.nullifySkyLightForBoundingBox(world);
      this.makeOpenings(world, sbb);
      return true;
   }
}
