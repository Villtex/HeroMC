package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFHedgeMaze extends StructureTFComponent {
   static int MSIZE = 16;
   static int RADIUS = MSIZE / 2 * 3 + 1;
   static int DIAMETER = 2 * RADIUS;
   static int FLOOR_LEVEL = 3;

   public ComponentTFHedgeMaze(OldWorld world, Random rand, int i, int x, int y, int z) {
      super(i);
      this.setCoordBaseMode(0);
      this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, -RADIUS, -3, -RADIUS, RADIUS * 2, 10, RADIUS * 2, 0);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      TFMaze maze = new TFMaze(MSIZE, MSIZE);
      maze.oddBias = 2;
      maze.torchBlockID = 1353;
      maze.wallBlockID = 1356;
      maze.wallBlockMeta = 0;
      maze.type = 4;
      maze.tall = 3;
      maze.roots = 3;
      maze.setSeed(world.seed() + this.boundingBox.minX * this.boundingBox.minZ);

      for (int fx = 0; fx <= DIAMETER; fx++) {
         for (int fz = 0; fz <= DIAMETER; fz++) {
            this.placeBlockAtCurrentPosition(world, 2, 0, fx, FLOOR_LEVEL - 1, fz, sbb);
         }
      }

      this.placeBlockAtCurrentPosition(world, 91, 1, 0, FLOOR_LEVEL, 24, sbb);
      this.placeBlockAtCurrentPosition(world, 91, 1, 0, FLOOR_LEVEL, 29, sbb);
      this.placeBlockAtCurrentPosition(world, 91, 3, 50, FLOOR_LEVEL, 24, sbb);
      this.placeBlockAtCurrentPosition(world, 91, 3, 50, FLOOR_LEVEL, 29, sbb);
      this.placeBlockAtCurrentPosition(world, 91, 2, 24, FLOOR_LEVEL, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 91, 2, 29, FLOOR_LEVEL, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 91, 0, 24, FLOOR_LEVEL, 50, sbb);
      this.placeBlockAtCurrentPosition(world, 91, 0, 29, FLOOR_LEVEL, 50, sbb);
      int nrooms = MSIZE / 3;
      int[] rcoords = new int[nrooms * 2];

      for (int i = 0; i < nrooms; i++) {
         int rx;
         int rz;
         do {
            rx = maze.rand.nextInt(MSIZE - 2) + 1;
            rz = maze.rand.nextInt(MSIZE - 2) + 1;
         } while (this.isNearRoom(rx, rz, rcoords));

         maze.carveRoom1(rx, rz);
         rcoords[i * 2] = rx;
         rcoords[i * 2 + 1] = rz;
      }

      maze.generateRecursiveBacktracker(0, 0);
      maze.add4Exits();
      maze.copyToStructure(world, 1, FLOOR_LEVEL, 1, this, sbb);
      this.decorate3x3Rooms(world, rcoords, sbb);
      return true;
   }

   protected boolean isNearRoom(int dx, int dz, int[] rcoords) {
      if (dx == 1 && dz == 1) {
         return true;
      }

      for (int i = 0; i < rcoords.length / 2; i++) {
         int rx = rcoords[i * 2];
         int rz = rcoords[i * 2 + 1];
         if ((rx != 0 || rz != 0) && Math.abs(dx - rx) < 3 && Math.abs(dz - rz) < 3) {
            return true;
         }
      }

      return false;
   }

   void decorate3x3Rooms(OldWorld world, int[] rcoords, StructureBoundingBox sbb) {
      for (int i = 0; i < rcoords.length / 2; i++) {
         int dx = rcoords[i * 2];
         int dz = rcoords[i * 2 + 1];
         dx = dx * 3 + 3;
         dz = dz * 3 + 3;
         this.decorate3x3Room(world, dx, dz, sbb);
      }
   }

   void decorate3x3Room(OldWorld world, int x, int z, StructureBoundingBox sbb) {
      Random roomRNG = new Random(world.seed() ^ x + z);
      this.roomJackO(world, roomRNG, x, z, 8, sbb);
      if (roomRNG.nextInt(4) == 0) {
         this.roomJackO(world, roomRNG, x, z, 8, sbb);
      }

      this.roomSpawner(world, roomRNG, x, z, 8, sbb);
      this.roomTreasure(world, roomRNG, x, z, 8, sbb);
      if (roomRNG.nextInt(4) == 0) {
         this.roomTreasure(world, roomRNG, x, z, 8, sbb);
      }
   }

   private void roomSpawner(OldWorld world, Random rand, int x, int z, int diameter, StructureBoundingBox sbb) {
      int rx = x + rand.nextInt(diameter) - diameter / 2;
      int rz = z + rand.nextInt(diameter) - diameter / 2;
      String mobID;
      switch (rand.nextInt(3)) {
         case 0:
         default:
            mobID = "TwilightForest.Hedge Spider";
            break;
         case 1:
            mobID = "TwilightForest.Swarm Spider";
            break;
         case 2:
            mobID = "TwilightForest.Hostile Wolf";
      }

      this.placeSpawnerAtCurrentPosition(world, rand, rx, FLOOR_LEVEL, rz, mobID, sbb);
   }

   private void roomTreasure(OldWorld world, Random rand, int x, int z, int diameter, StructureBoundingBox sbb) {
      int rx = x + rand.nextInt(diameter) - diameter / 2;
      int rz = z + rand.nextInt(diameter) - diameter / 2;
      this.placeTreasureAtCurrentPosition(world, rand, rx, FLOOR_LEVEL, rz, TFTreasure.HEDGEMAZE, sbb);
   }

   private void roomJackO(OldWorld world, Random rand, int x, int z, int diameter, StructureBoundingBox sbb) {
      int rx = x + rand.nextInt(diameter) - diameter / 2;
      int rz = z + rand.nextInt(diameter) - diameter / 2;
      this.placeBlockAtCurrentPosition(world, 91, rand.nextInt(4), rx, FLOOR_LEVEL, rz, sbb);
   }
}
