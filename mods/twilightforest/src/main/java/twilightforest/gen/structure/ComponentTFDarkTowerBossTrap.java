package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerBossTrap extends ComponentTFDarkTowerWing {
   protected ComponentTFDarkTowerBossTrap(int i, int x, int y, int z, int pSize, int pHeight, int direction) {
      super(i, x, y, z, pSize, pHeight, direction);
      this.spawnListIndex = -1;
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random rand) {
      if (parent != null && parent instanceof StructureTFComponent) {
         this.deco = ((StructureTFComponent)parent).deco;
      }

      this.addOpening(0, 1, this.size / 2, 2);
      this.makeABeard(parent, list, rand);

      for (int i = 0; i < 4; i++) {
         if (i != 2 && !rand.nextBoolean()) {
            int[] dest = this.getValidOpening(rand, i);
            dest[1] = 1;
            this.makeTowerBalcony(list, rand, this.getComponentType(), dest[0], dest[1], dest[2], i);
         }
      }
   }

   @Override
   public void makeARoof(StructureComponent parent, List list, Random rand) {
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      Random decoRNG = new Random(world.seed() + this.boundingBox.minX * 321534781 ^ this.boundingBox.minZ * 756839);
      this.makeEncasedWalls(world, rand, sbb, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1);
      this.fillWithAir(world, sbb, 1, 1, 1, this.size - 2, this.height - 2, this.size - 2);
      this.makeOpenings(world, sbb);
      this.addBossTrapFloors(world, decoRNG, sbb, 4, this.height - 1);
      this.destroyTower(world, decoRNG, 5, this.height + 2, 5, 4, sbb);
      this.destroyTower(world, decoRNG, 0, this.height, 0, 3, sbb);
      this.destroyTower(world, decoRNG, 0, this.height, 8, 4, sbb);
      this.destroyTower(world, decoRNG, 5, 6, 5, 2, sbb);
      this.fillWithMetadataBlocks(world, sbb, 1, 0, 1, this.size / 2, 0, this.size - 2, this.deco.blockID, this.deco.blockMeta, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 1, 1, 1, this.size / 2, 1, this.size - 2, 0, 0, 0, 0, false);
      this.placeBlockAtCurrentPosition(world, 1370, 10, 5, 1, 5, sbb);
      this.placeBlockAtCurrentPosition(world, 55, 0, 5, 1, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 55, 0, 5, 1, 7, sbb);
      this.placeBlockAtCurrentPosition(world, 55, 0, 5, 1, 8, sbb);
      this.placeBlockAtCurrentPosition(world, 55, 0, 4, 1, 8, sbb);
      this.placeBlockAtCurrentPosition(world, 55, 0, 3, 1, 8, sbb);
      this.placeBlockAtCurrentPosition(world, 72, 0, 2, 1, 8, sbb);
      return true;
   }

   protected void addBossTrapFloors(OldWorld world, Random rand, StructureBoundingBox sbb, int bottom, int top) {
      this.makeFullFloor(world, sbb, 3, 4, 4);
      this.addStairsDown(world, sbb, 3, 4, this.size - 2, 4);
      this.addStairsDown(world, sbb, 3, 4, this.size - 3, 4);
      this.addStairsDown(world, sbb, 1, this.height - 1, this.size - 2, 4);
      this.addStairsDown(world, sbb, 1, this.height - 1, this.size - 3, 4);
   }
}
