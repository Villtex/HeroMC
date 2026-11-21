package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFTowerMain extends ComponentTFTowerWing {
   public ComponentTFTowerMain(OldWorld world, Random rand, int index, int x, int y, int z) {
      super(index, x, y, z, 15, 55 + rand.nextInt(32), 0);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random rand) {
      this.makeARoof(parent, list, rand);

      for (int i = 0; i < 4; i++) {
         int[] dest = this.getValidOpening(rand, i);
         if (dest[1] < this.height / 2) {
            dest[1] += 20;
         }

         int childHeight = Math.min(21 + rand.nextInt(10), this.height - dest[1] - 3);
         if (!this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], 9, childHeight, i)) {
            this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], 7, childHeight, i);
         }
      }

      for (int i = 0; i < 4; i++) {
         int[] dest = this.getValidOpening(rand, i);
         if (dest[1] < this.height / 2) {
            dest[1] += 10;
         }

         int childHeight = Math.min(21 + rand.nextInt(10), this.height - dest[1] - 3);
         if (!this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], 9, childHeight, i)) {
            this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], 7, childHeight, i);
         }
      }

      for (int i = 0; i < 4; i++) {
         int[] dest = this.getValidOpening(rand, i);
         int childHeight = Math.min(7 + rand.nextInt(6), this.height - dest[1] - 3);
         if (!this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], 5, childHeight, i)) {
            this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], 3, childHeight, i);
         }
      }

      for (int i = 0; i < 4; i++) {
         int[] dest = this.getOutbuildingOpening(rand, i);
         int childHeight = 11 + rand.nextInt(10);
         int childSize = 7 + rand.nextInt(2) * 2;
         this.makeTowerOutbuilding(list, rand, 1, dest[0], dest[1], dest[2], childSize, childHeight, i);
      }

      for (int i = 0; i < 16; i++) {
         int[] dest = this.getValidOpening(rand, i % 4);
         int childHeight = 6 + rand.nextInt(5);
         if (rand.nextInt(3) == 0 || !this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], 5, childHeight, i % 4)) {
            this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], 3, childHeight, i % 4);
         }
      }
   }

   public int[] getOutbuildingOpening(Random rand, int rotation) {
      int rx = 0;
      int ry = 1;
      int rz = 0;
      switch (rotation) {
         case 0:
            rx = this.size - 1;
            rz = 6 + rand.nextInt(8);
            break;
         case 1:
            rx = 1 + rand.nextInt(11);
            rz = this.size - 1;
            break;
         case 2:
            rx = 0;
            rz = 1 + rand.nextInt(8);
            break;
         case 3:
            rx = 3 + rand.nextInt(11);
            rz = 0;
      }

      return new int[]{rx, ry, rz};
   }

   public boolean makeTowerOutbuilding(List list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, int rotation) {
      int direction = (this.getCoordBaseMode() + rotation) % 4;
      int[] dx = this.offsetTowerCoords(x, y, z, wingSize, direction);
      ComponentTFTowerOutbuilding outbuilding = new ComponentTFTowerOutbuilding(index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
      StructureComponent intersect = StructureComponent.findIntersecting(list, outbuilding.getBoundingBox());
      if (intersect != null && intersect != this) {
         return false;
      }

      list.add(outbuilding);
      outbuilding.buildComponent(this, list, rand);
      this.addOpening(x, y, z, rotation);
      return true;
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithRandomizedBlocks(world, sbb, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithAir(world, sbb, 1, 1, 1, this.size - 2, this.height - 2, this.size - 2);

      for (int x = 0; x < this.size; x++) {
         for (int z = 0; z < this.size; z++) {
            this.fillCurrentPositionBlocksDownwards(world, 4, 0, x, -1, z, sbb);
         }
      }

      this.nullifySkyLightForBoundingBox(world);
      if (this.height - this.highestOpening > 15) {
         this.highestOpening = this.height - 15;
      }

      this.makeStairs(world, rand, sbb);
      this.makeOpenings(world, sbb);
      this.decorateStairFloor(world, rand, sbb);
      this.makeStairwayCrossings(world, rand, sbb);
      this.makeLichRoom(world, rand, sbb);
      this.makeTowerPaintings(world, rand, sbb);
      return true;
   }

   protected void makeStairwayCrossings(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int flights = this.highestOpening / 5 - 2;

      for (int i = 2 + rand.nextInt(2); i < flights; i += 1 + rand.nextInt(5)) {
         this.makeStairCrossing(world, rand, i, sbb);
      }
   }

   protected void makeStairCrossing(OldWorld world, Random rand, int flight, StructureBoundingBox sbb) {
      int temp = this.getCoordBaseMode();
      if (flight % 2 == 0) {
         this.setCoordBaseMode((this.getCoordBaseMode() + 1) % 4);
      }

      int floorMeta = rand.nextInt(2) == 0 ? 0 : 2;
      int floorLevel = 0 + flight * 5;

      for (int dx = 6; dx <= 8; dx++) {
         for (int dz = 4; dz <= 10; dz++) {
            this.placeBlockAtCurrentPosition(world, 43, floorMeta, dx, floorLevel, dz, sbb);
         }
      }

      floorLevel++;
      int dx = 6;

      for (int dz = 3; dz <= 11; dz++) {
         this.placeBlockAtCurrentPosition(world, 85, 0, dx, floorLevel, dz, sbb);
      }

      dx++;

      for (int dz = 3; dz <= 11; dz++) {
         this.placeBlockAtCurrentPosition(world, 0, 0, dx, floorLevel, dz, sbb);
      }

      dx++;

      for (int dz = 3; dz <= 11; dz++) {
         this.placeBlockAtCurrentPosition(world, 85, 0, dx, floorLevel, dz, sbb);
      }

      this.placeBlockAtCurrentPosition(world, 43, floorMeta, 6, floorLevel - 1, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 43, floorMeta, 8, floorLevel - 1, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 5, floorLevel, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 9, floorLevel, 3, sbb);
      String mobID = "Skeleton";
      switch (rand.nextInt(4)) {
         case 0:
         case 1:
            mobID = "Skeleton";
            break;
         case 2:
            mobID = "Zombie";
            break;
         case 3:
            mobID = "TwilightForest.Swarm Spider";
      }

      this.placeSpawnerAtCurrentPosition(world, rand, 7, floorLevel + 2, 7, mobID, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 6, floorLevel + 1, 7, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 8, floorLevel + 1, 7, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 6, floorLevel + 2, 7, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 8, floorLevel + 2, 7, sbb);
      this.setCoordBaseMode(temp);
   }

   protected void makeLichRoom(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int floorLevel = 2 + this.highestOpening / 5 * 5;
      this.makeLichFloor(world, floorLevel, this.highestOpening / 5 % 2, sbb);
      this.decorateLichChandelier(world, floorLevel, sbb);
      this.decoratePaintings(world, rand, floorLevel, sbb);
      this.decorateTorches(world, rand, floorLevel, sbb);
      this.placeBlockAtCurrentPosition(world, 1351, 1, this.size / 2, floorLevel + 2, this.size / 2, sbb);
   }

   protected void makeTowerPaintings(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int howMany = 10;
      this.generatePaintingsOnWall(world, rand, howMany, 0, 0, 48, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 0, 32, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 0, 0, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 1, 48, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 1, 32, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 1, 0, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 2, 48, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 2, 32, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 2, 0, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 3, 48, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 3, 32, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, 0, 3, 0, sbb);
   }

   protected void makeLichFloor(OldWorld world, int floorLevel, int rotation, StructureBoundingBox sbb) {
      int temp = this.getCoordBaseMode();
      this.setCoordBaseMode((this.getCoordBaseMode() + rotation) % 4);

      for (int fx = 1; fx < 14; fx++) {
         for (int fz = 1; fz < 14; fz++) {
            if ((fx == 1 || fx == 2) && fz >= 6 && fz <= 12) {
               if (fz == 6) {
                  this.placeBlockAtCurrentPosition(world, 126, 10, fx, floorLevel, fz, sbb);
               }
            } else if ((fx == 12 || fx == 13) && fz >= 3 && fz <= 8) {
               if (fz == 8) {
                  this.placeBlockAtCurrentPosition(world, 126, 10, fx, floorLevel, fz, sbb);
               }
            } else if (fx >= 4 && fx <= 10 && fz >= 4 && fz <= 10) {
               if ((fx != 4 || fz != 4) && (fx != 10 || fz != 10)) {
                  this.placeBlockAtCurrentPosition(world, 20, 0, fx, floorLevel, fz, sbb);
               } else {
                  this.placeBlockAtCurrentPosition(world, 5, 2, fx, floorLevel, fz, sbb);
               }
            } else if (fx != 2 && fx != 3 || fz != 2 && fz != 3) {
               if (fx != 11 && fx != 12 || fz != 11 && fz != 12) {
                  this.placeBlockAtCurrentPosition(world, 5, 2, fx, floorLevel, fz, sbb);
               } else {
                  this.placeBlockAtCurrentPosition(world, 20, 0, fx, floorLevel, fz, sbb);
               }
            } else {
               this.placeBlockAtCurrentPosition(world, 20, 0, fx, floorLevel, fz, sbb);
            }
         }
      }

      this.placeBlockAtCurrentPosition(world, 0, 0, 3, floorLevel + 1, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 3, floorLevel + 1, 10, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 3, floorLevel + 2, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 11, floorLevel + 1, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 11, floorLevel + 1, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, 11, floorLevel + 2, 3, sbb);
      this.setCoordBaseMode(temp);
   }

   protected void decorateLichChandelier(OldWorld world, int floorLevel, StructureBoundingBox sbb) {
      int cx = this.size / 2;
      int cy = floorLevel + 4;
      int cz = this.size / 2;
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 1, cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 2, cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 1, cy, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy, cz + 2, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx - 1, cy, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx - 1, cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx - 2, cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx - 1, cy, cz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy, cz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy, cz - 2, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 1, cy, cz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 1, ++cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx + 2, cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx + 1, cy, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx + 0, cy, cz + 2, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx - 1, cy, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx - 1, cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx - 2, cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx - 1, cy, cz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy, cz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx + 0, cy, cz - 2, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx + 1, cy, cz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx + 1, ++cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx + 0, cy, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx - 1, cy, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 50, 0, cx + 0, cy, cz - 1, sbb);

      for (int y = floorLevel + 5; y < this.height - 1; y++) {
         this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, y, cz + 0, sbb);
      }
   }

   protected void decoratePaintings(OldWorld world, Random rand, int floorLevel, StructureBoundingBox sbb) {
      int howMany = 100;
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 0, 48, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 0, 32, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 0, 0, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 1, 48, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 1, 32, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 1, 0, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 2, 48, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 2, 32, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 2, 0, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 3, 48, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 3, 32, sbb);
      this.generatePaintingsOnWall(world, rand, howMany, floorLevel, 3, 0, sbb);
   }

   protected void decorateTorches(OldWorld world, Random rand, int floorLevel, StructureBoundingBox sbb) {
      this.generateTorchesOnWall(world, rand, floorLevel, 0, sbb);
      this.generateTorchesOnWall(world, rand, floorLevel, 1, sbb);
      this.generateTorchesOnWall(world, rand, floorLevel, 2, sbb);
      this.generateTorchesOnWall(world, rand, floorLevel, 3, sbb);
   }

   protected void generateTorchesOnWall(OldWorld world, Random rand, int floorLevel, int direction, StructureBoundingBox sbb) {
      for (int i = 0; i < 10; i++) {
         ChunkCoordinates wCoords = this.getRandomWallSpot(rand, floorLevel, direction, sbb);
         if (wCoords == null) {
            continue; // a NullPointerException in the original (never for the main tower, which lies in one chunk)
         }
         ChunkCoordinates tCoords = new ChunkCoordinates(wCoords);
         if (direction == 0) {
            tCoords.posZ++;
         }

         if (direction == 1) {
            tCoords.posX--;
         }

         if (direction == 2) {
            tCoords.posZ--;
         }

         if (direction == 3) {
            tCoords.posX++;
         }

         if (world.getBlockId(tCoords.posX, tCoords.posY, tCoords.posZ) == 0
            && world.getBlockId(tCoords.posX, tCoords.posY + 1, tCoords.posZ) == 0
            && !hexxit.worldgen152.core.populate.OldPainting.hangingWithin(world, tCoords.posX, tCoords.posY, tCoords.posZ, tCoords.posX + 1.0,
               tCoords.posY + 2.0, tCoords.posZ + 1.0)) {
            world.setBlock(tCoords.posX, tCoords.posY, tCoords.posZ, 85, 0);
            world.setBlock(tCoords.posX, tCoords.posY + 1, tCoords.posZ, 50, 5);
         }
      }
   }
}
