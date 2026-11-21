package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdFoundry extends StructureTFStrongholdComponent {
   int entranceLevel;

   public ComponentTFStrongholdFoundry(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      if (y > 17) {
         this.entranceLevel = 3;
         return StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -4, -20, 0, 18, 25, 18, facing);
      } else if (y < 11) {
         this.entranceLevel = 1;
         return StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -4, -6, 0, 18, 25, 18, facing);
      } else {
         this.entranceLevel = 2;
         return StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -4, -13, 0, 18, 25, 18, facing);
      }
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      switch (this.entranceLevel) {
         case 1:
            this.addDoor(4, 6, 0);
            this.addNewComponent(parent, list, random, 1, -1, 13, 13);
            this.addNewComponent(parent, list, random, 3, 18, 13, 4);
            this.addNewComponent(parent, list, random, 0, 13, 20, 18);
            break;
         case 2:
            this.addDoor(4, 13, 0);
            this.addNewComponent(parent, list, random, 1, -1, 6, 13);
            this.addNewComponent(parent, list, random, 3, 18, 20, 4);
            this.addNewComponent(parent, list, random, 0, 13, 13, 18);
            break;
         case 3:
            this.addDoor(4, 20, 0);
            this.addNewComponent(parent, list, random, 0, 13, 6, 18);
            this.addNewComponent(parent, list, random, 1, -1, 13, 13);
            this.addNewComponent(parent, list, random, 3, 18, 13, 4);
      }
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 17, 25, 17, rand, this.deco.randomBlocks);
      this.fillWithBlocks(world, sbb, 1, 0, 1, 16, 4, 16, 11, 0, false);
      this.fillWithRandomizedBlocks(world, sbb, 1, 19, 1, 16, 19, 16, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithAir(world, sbb, 2, 19, 2, 15, 19, 15);
      this.fillWithRandomizedBlocks(world, sbb, 1, 12, 1, 16, 12, 16, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithAir(world, sbb, 2, 12, 2, 15, 12, 15);
      this.fillWithRandomizedBlocks(world, sbb, 1, 5, 1, 16, 5, 16, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithAir(world, sbb, 2, 5, 2, 15, 5, 15);
      this.fillWithRandomizedBlocks(world, sbb, 1, 1, 1, 1, 24, 2, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithRandomizedBlocks(world, sbb, 2, 1, 1, 2, 24, 1, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithRandomizedBlocks(world, sbb, 16, 1, 1, 16, 24, 2, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithRandomizedBlocks(world, sbb, 15, 1, 1, 15, 24, 1, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithRandomizedBlocks(world, sbb, 1, 1, 15, 1, 24, 16, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithRandomizedBlocks(world, sbb, 2, 1, 16, 2, 24, 16, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithRandomizedBlocks(world, sbb, 16, 1, 15, 16, 24, 16, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithRandomizedBlocks(world, sbb, 15, 1, 16, 15, 24, 16, false, rand, StructureTFComponent.getStrongholdStones());
      Random massRandom = new Random(rand.nextLong());

      for (int x = 4; x < 14; x++) {
         for (int z = 4; z < 14; z++) {
            for (int y = 8; y < 23; y++) {
               float c = Math.abs(x - 8.5F) + Math.abs(z - 8.5F) + Math.abs(y - 18.0F);
               float r = 5.5F + (massRandom.nextFloat() - massRandom.nextFloat()) * 3.5F;
               if (c < r) {
                  this.placeBlockAtCurrentPosition(world, 1, 0, x, y, z, sbb);
               }
            }
         }
      }

      for (int i = 0; i < 400; i++) {
         int dx = massRandom.nextInt(9) + 5;
         int dz = massRandom.nextInt(9) + 5;
         int dy = massRandom.nextInt(13) + 10;
         if (this.getBlockIdAtCurrentPosition(world, dx, dy, dz, sbb) > 0) {
            for (int y = 0; y < 3; y++) {
               this.placeBlockAtCurrentPosition(world, 1, 0, dx, dy - y, dz, sbb);
            }
         }
      }

      this.placeDoors(world, rand, sbb);
      return true;
   }
}
