package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFNagaCourtyard extends StructureTFComponent {
   static int RADIUS = 46;
   static int DIAMETER = 2 * RADIUS + 1;

   public ComponentTFNagaCourtyard(OldWorld world, Random rand, int i, int x, int y, int z) {
      super(i);
      this.setCoordBaseMode(0);
      this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, -RADIUS, -1, -RADIUS, RADIUS * 2, 10, RADIUS * 2, 0);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int fx = 0; fx <= DIAMETER; fx++) {
         for (int fz = 0; fz <= DIAMETER; fz++) {
            if (rand.nextInt(3) == 0) {
               this.placeBlockAtCurrentPosition(world, 43, 0, fx, 0, fz, sbb);
               if (rand.nextInt(20) == 0) {
                  this.placeBlockAtCurrentPosition(world, 44, 0, fx, 1, fz, sbb);
               } else {
                  this.placeBlockAtCurrentPosition(world, 0, 0, fx, 1, fz, sbb);
               }
            } else {
               this.placeBlockAtCurrentPosition(world, 2, 0, fx, 0, fz, sbb);
            }
         }
      }

      for (int fx = 0; fx <= DIAMETER; fx++) {
         this.randomBrick(world, rand, fx, 0, DIAMETER, sbb);
         this.randomBrick(world, rand, fx, 0, 0, sbb);
         this.randomBrick(world, rand, fx, 1, DIAMETER, sbb);
         this.randomBrick(world, rand, fx, 1, 0, sbb);
         this.randomBrick(world, rand, fx, 2, DIAMETER, sbb);
         this.randomBrick(world, rand, fx, 2, 0, sbb);
         this.randomBrick(world, rand, fx, 3, DIAMETER, sbb);
         this.randomBrick(world, rand, fx, 3, 0, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, fx, 4, DIAMETER, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, fx, 4, 0, sbb);
         switch (fx % 23) {
            case 2:
               this.placeBlockAtCurrentPosition(world, 1364, 7, fx, 3, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 7, fx, 3, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 11, fx, 1, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 11, fx, 1, 0, sbb);
               break;
            case 3:
               this.placeBlockAtCurrentPosition(world, 1364, 3, fx, 3, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 3, fx, 3, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 12, fx, 1, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 12, fx, 1, 0, sbb);
               break;
            case 4:
            case 8:
            case 16:
            case 20:
               this.placeBlockAtCurrentPosition(world, 1364, 12, fx, 1, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 12, fx, 1, 0, sbb);
               break;
            case 5:
            case 9:
            case 17:
               this.placeBlockAtCurrentPosition(world, 1364, 7, fx, 3, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 7, fx, 3, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 10, fx, 1, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 10, fx, 1, 0, sbb);
               break;
            case 6:
            case 10:
            case 14:
            case 18:
               this.placeBlockAtCurrentPosition(world, 1364, 12, fx, 3, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 12, fx, 3, 0, sbb);
               break;
            case 7:
            case 15:
            case 19:
               this.placeBlockAtCurrentPosition(world, 1364, 6, fx, 3, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 6, fx, 3, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 11, fx, 1, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 11, fx, 1, 0, sbb);
               break;
            case 11:
               this.placeBlockAtCurrentPosition(world, 1364, 6, fx, 3, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 6, fx, 3, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 1, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 1, 0, sbb);
            case 12:
            default:
               break;
            case 13:
               this.placeBlockAtCurrentPosition(world, 1364, 7, fx, 3, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 7, fx, 3, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 1, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 1, 0, sbb);
               break;
            case 21:
               this.placeBlockAtCurrentPosition(world, 1364, 2, fx, 3, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 2, fx, 3, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 12, fx, 1, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 12, fx, 1, 0, sbb);
               break;
            case 22:
               this.placeBlockAtCurrentPosition(world, 1364, 6, fx, 3, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 6, fx, 3, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, fx, 2, 0, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 10, fx, 1, DIAMETER, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 10, fx, 1, 0, sbb);
         }
      }

      for (int fz = 0; fz <= DIAMETER; fz++) {
         this.randomBrick(world, rand, DIAMETER, 0, fz, sbb);
         this.randomBrick(world, rand, 0, 0, fz, sbb);
         this.randomBrick(world, rand, DIAMETER, 1, fz, sbb);
         this.randomBrick(world, rand, 0, 1, fz, sbb);
         this.randomBrick(world, rand, DIAMETER, 2, fz, sbb);
         this.randomBrick(world, rand, 0, 2, fz, sbb);
         this.randomBrick(world, rand, DIAMETER, 3, fz, sbb);
         this.randomBrick(world, rand, 0, 3, fz, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, DIAMETER, 4, fz, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, 0, 4, fz, sbb);
         switch (fz % 23) {
            case 2:
               this.placeBlockAtCurrentPosition(world, 1364, 5, DIAMETER, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 5, 0, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, DIAMETER, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, 0, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 9, DIAMETER, 1, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 9, 0, 1, fz, sbb);
               break;
            case 3:
               this.placeBlockAtCurrentPosition(world, 1364, 1, DIAMETER, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 1, 0, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 13, DIAMETER, 1, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 13, 0, 1, fz, sbb);
               break;
            case 4:
            case 8:
            case 16:
            case 20:
               this.placeBlockAtCurrentPosition(world, 1364, 13, DIAMETER, 1, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 13, 0, 1, fz, sbb);
               break;
            case 5:
            case 9:
            case 17:
               this.placeBlockAtCurrentPosition(world, 1364, 5, DIAMETER, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 5, 0, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, DIAMETER, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, 0, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 8, DIAMETER, 1, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 8, 0, 1, fz, sbb);
               break;
            case 6:
            case 10:
            case 14:
            case 18:
               this.placeBlockAtCurrentPosition(world, 1364, 13, DIAMETER, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 13, 0, 3, fz, sbb);
               break;
            case 7:
            case 15:
            case 19:
               this.placeBlockAtCurrentPosition(world, 1364, 4, DIAMETER, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 4, 0, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, DIAMETER, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, 0, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 9, DIAMETER, 1, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 9, 0, 1, fz, sbb);
               break;
            case 11:
               this.placeBlockAtCurrentPosition(world, 1364, 4, DIAMETER, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 4, 0, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, DIAMETER, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, 0, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, DIAMETER, 1, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, 0, 1, fz, sbb);
            case 12:
            default:
               break;
            case 13:
               this.placeBlockAtCurrentPosition(world, 1364, 5, DIAMETER, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 5, 0, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, DIAMETER, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, 0, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, DIAMETER, 1, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, 0, 1, fz, sbb);
               break;
            case 21:
               this.placeBlockAtCurrentPosition(world, 1364, 0, DIAMETER, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 0, 0, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 13, DIAMETER, 1, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 13, 0, 1, fz, sbb);
               break;
            case 22:
               this.placeBlockAtCurrentPosition(world, 1364, 4, DIAMETER, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 4, 0, 3, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, DIAMETER, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 14, 0, 2, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 8, DIAMETER, 1, fz, sbb);
               this.placeBlockAtCurrentPosition(world, 1364, 8, 0, 1, fz, sbb);
         }
      }

      Random pillarRand = new Random(world.seed() + this.boundingBox.minX * this.boundingBox.minZ);

      for (int i = 0; i < 20; i++) {
         int rx = 2 + pillarRand.nextInt(DIAMETER - 4);
         int rz = 2 + pillarRand.nextInt(DIAMETER - 4);
         this.makePillar(world, pillarRand, rx, 1, rz, sbb);
      }

      this.placeBlockAtCurrentPosition(world, 1351, 0, RADIUS + 1, 2, RADIUS + 1, sbb);
      return true;
   }

   public boolean makePillar(OldWorld world, Random rand, int x, int y, int z, StructureBoundingBox sbb) {
      int height = 8;
      this.placeBlockAtCurrentPosition(world, 44, 0, x - 1, y + 0, z - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, x + 0, y + 0, z - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, x + 1, y + 0, z - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, x - 1, y + 0, z + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, x + 1, y + 0, z + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, x - 1, y + 0, z + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, x + 0, y + 0, z + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, x + 1, y + 0, z + 1, sbb);

      for (int i = 0; i < height; i++) {
         this.randomBrick(world, rand, x, y + i, z, sbb);
         if (i > 0 && rand.nextInt(2) == 0) {
            switch (rand.nextInt(4)) {
               case 0:
                  this.placeBlockAtCurrentPosition(world, 106, 8, x - 1, y + i, z + 0, sbb);
                  break;
               case 1:
                  this.placeBlockAtCurrentPosition(world, 106, 2, x + 1, y + i, z + 0, sbb);
                  break;
               case 2:
                  this.placeBlockAtCurrentPosition(world, 106, 4, x + 0, y + i, z + 1, sbb);
                  break;
               case 3:
                  this.placeBlockAtCurrentPosition(world, 106, 1, x + 0, y + i, z - 1, sbb);
            }
         } else if (i > 0 && rand.nextInt(4) == 0) {
            switch (rand.nextInt(4)) {
               case 0:
                  this.placeBlockAtCurrentPosition(world, 1353, 0, x - 1, y + i, z + 0, sbb);
                  break;
               case 1:
                  this.placeBlockAtCurrentPosition(world, 1353, 0, x + 1, y + i, z + 0, sbb);
                  break;
               case 2:
                  this.placeBlockAtCurrentPosition(world, 1353, 0, x + 0, y + i, z + 1, sbb);
                  break;
               case 3:
                  this.placeBlockAtCurrentPosition(world, 1353, 0, x + 0, y + i, z - 1, sbb);
            }
         }
      }

      if (height == 8) {
         this.placeBlockAtCurrentPosition(world, 44, 0, x - 1, y + 8, z - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 0, x + 0, y + 8, z - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 0, x + 1, y + 8, z - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 0, x - 1, y + 8, z + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, x + 0, y + 8, z + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 0, x + 1, y + 8, z + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 0, x - 1, y + 8, z + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 0, x + 0, y + 8, z + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 0, x + 1, y + 8, z + 1, sbb);
      }

      return true;
   }

   public void randomBrick(OldWorld world, Random rand, int x, int y, int z, StructureBoundingBox sbb) {
      this.placeBlockAtCurrentPosition(world, 98, rand.nextInt(3), x, y, z, sbb);
   }
}
