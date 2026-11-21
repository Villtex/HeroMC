package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFQuestGrove extends StructureTFComponent {
   /** The mob of this building, with its home radius (OldGameIds.registerEntity of the game side). */
   public static final String QUEST_RAM = "TwilightForest.Questing Ram@13";

   public static final int RADIUS = 13;
   protected boolean beastPlaced = false;
   protected boolean dispenserPlaced = false;

   public ComponentTFQuestGrove(OldWorld world, Random rand, int i, int x, int y, int z) {
      super(i);
      this.setCoordBaseMode(0);
      this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, -13, 0, -13, 26, 10, 26, 0);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int i = 0; i < 4; i++) {
         this.makeWallSide(world, rand, i, sbb);
      }

      for (int x = 10; x < 17; x++) {
         for (int z = 10; z < 17; z++) {
            if (x != 10 && x != 16 && z != 10 && z != 16) {
               if (x != 11 && x != 15 && z != 11 && z != 15) {
                  this.placeBlockAtCurrentPosition(world, 98, 1, x, -1, z, sbb);
               } else if (rand.nextInt(3) > 0) {
                  this.placeBlockAtCurrentPosition(world, 98, 1, x, -1, z, sbb);
               }
            } else if (rand.nextInt(2) > 0) {
               this.placeBlockAtCurrentPosition(world, 98, 1, x, -1, z, sbb);
            }
         }
      }

      this.placeBlockAtCurrentPosition(world, 77, 4, 13, 5, 19, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 1, 12, 7, 20, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 1, 13, 7, 20, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 1, 14, 7, 20, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 1, 12, 7, 21, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 1, 13, 7, 21, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 1, 14, 7, 21, sbb);
      if (!this.dispenserPlaced) {
         int bx = this.getXWithOffset(13, 20);
         int by = this.getYWithOffset(6);
         int bz = this.getZWithOffset(13, 20);
         if (sbb.isVecInside(bx, by, bz)) {
            this.dispenserPlaced = true;
            world.setBlock(bx, by, bz, 23, 2);
            OldTile.Dispenser ted = new OldTile.Dispenser();
            world.setTile(bx, by, bz, ted);

            for (int i = 0; i < 4; i++) {
               ted.slots()[i] = new OldStack(35, rand.nextInt(16), 1);
            }
         }
      }

      if (!this.beastPlaced) {
         int bx = this.getXWithOffset(13, 13);
         int by = this.getYWithOffset(0);
         int bz = this.getZWithOffset(13, 13);
         if (sbb.isVecInside(bx, by, bz)) {
            this.beastPlaced = true;
            // EntityTFQuestRam with setHomeArea(bx, by, bz, 13): the name carries the home radius
            world.spawnEntity(new OldEntity.Mob(QUEST_RAM, bx, by, bz, 0.0F, null));
         }
      }

      return true;
   }

   private void makeWallSide(OldWorld world, Random rand, int direction, StructureBoundingBox sbb) {
      int temp = this.getCoordBaseMode();
      this.setCoordBaseMode(direction);
      this.placeOuterArch(world, 3, -1, sbb);
      this.placeOuterArch(world, 11, -1, sbb);
      this.placeOuterArch(world, 19, -1, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 0, 0, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 0, 1, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 0, 2, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 0, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 1, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 2, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 8, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 9, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 10, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 16, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 17, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 18, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 24, 3, 0, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 25, 3, 0, sbb);

      for (int x = 0; x < 9; x++) {
         for (int y = 0; y < 9; y++) {
            for (int z = 0; z < 2; z++) {
               if (x == 0 || x == 1 || x == 7 || x == 8 || y == 0 || y == 1 || y == 7 || y == 8) {
                  this.placeBlockAtCurrentPosition(world, 98, 1, x + 9, y - 2, z + 5, sbb);
               }
            }
         }
      }

      this.placeBlockAtCurrentPosition(world, 98, 3, 6, 0, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 6, 1, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 6, 2, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 6, 3, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 6, 4, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 7, 4, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 8, 4, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 18, 4, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 3, 19, 4, 6, sbb);
      this.setCoordBaseMode(temp);
   }

   private void placeOuterArch(OldWorld world, int ox, int oy, StructureBoundingBox sbb) {
      for (int x = 0; x < 5; x++) {
         for (int y = 0; y < 6; y++) {
            if (x == 0 || x == 4 || y == 0 || y == 5) {
               this.placeBlockAtCurrentPosition(world, 98, 1, x + ox, y + oy, 0, sbb);
            }
         }
      }
   }
}
