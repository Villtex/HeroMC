package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeRoomBoss extends ComponentTFMazeRoom {
   /** The mob of this building, with its home radius (OldGameIds.registerEntity of the game side). */
   public static final String MINOSHROOM = "TwilightForest.Minoshroom@7";

   private boolean taurPlaced = false;

   public ComponentTFMazeRoomBoss(int i, Random rand, int x, int y, int z) {
      super(i, rand, x, y, z);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      if (this.getBlockIdAtCurrentPosition(world, 7, 1, 0, sbb) == 0) {
         this.fillWithBlocks(world, sbb, 6, 1, 0, 9, 4, 0, 85, 0, false);
      }

      if (this.getBlockIdAtCurrentPosition(world, 7, 1, 15, sbb) == 0) {
         this.fillWithBlocks(world, sbb, 6, 1, 15, 9, 4, 15, 85, 0, false);
      }

      if (this.getBlockIdAtCurrentPosition(world, 0, 1, 7, sbb) == 0) {
         this.fillWithBlocks(world, sbb, 0, 1, 6, 0, 4, 9, 85, 0, false);
      }

      if (this.getBlockIdAtCurrentPosition(world, 15, 1, 7, sbb) == 0) {
         this.fillWithBlocks(world, sbb, 15, 1, 6, 15, 4, 9, 85, 0, false);
      }

      for (int x = 1; x < 14; x++) {
         for (int z = 1; z < 14; z++) {
            int dist = (int)Math.round(7.0 / Math.sqrt((7.5 - x) * (7.5 - x) + (7.5 - z) * (7.5 - z)));
            boolean mycelium = rand.nextInt(dist + 1) > 0;
            boolean mushroom = rand.nextInt(dist) > 0;
            boolean mushRed = rand.nextBoolean();
            if (mycelium) {
               this.placeBlockAtCurrentPosition(world, 110, 0, x, 0, z, sbb);
            }

            if (mushroom) {
               this.placeBlockAtCurrentPosition(world, mushRed ? 40 : 39, 0, x, 1, z, sbb);
            }
         }
      }

      this.fillWithMetadataBlocks(world, sbb, 1, 1, 1, 3, 1, 3, 100, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 1, 2, 1, 1, 3, 4, 100, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 2, 2, 1, 4, 3, 1, 100, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 1, 4, 1, 3, 4, 3, 100, 14, 0, 0, false);
      this.placeTreasureAtCurrentPosition(world, rand, 3, 2, 3, TFTreasure.LABYRINTH_ROOM, sbb);
      this.fillWithMetadataBlocks(world, sbb, 12, 1, 12, 14, 1, 14, 100, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 14, 2, 11, 14, 3, 14, 100, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 11, 2, 14, 14, 3, 14, 100, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 12, 4, 12, 14, 4, 14, 100, 14, 0, 0, false);
      this.placeTreasureAtCurrentPosition(world, rand, 12, 2, 12, TFTreasure.LABYRINTH_ROOM, sbb);
      this.fillWithMetadataBlocks(world, sbb, 1, 1, 12, 3, 1, 14, 99, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 1, 2, 11, 1, 3, 14, 99, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 2, 2, 14, 4, 3, 14, 99, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 1, 4, 12, 3, 4, 14, 99, 14, 0, 0, false);
      this.placeTreasureAtCurrentPosition(world, rand, 3, 2, 12, TFTreasure.LABYRINTH_ROOM, sbb);
      this.fillWithMetadataBlocks(world, sbb, 12, 1, 1, 14, 1, 3, 99, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 11, 2, 1, 14, 3, 1, 99, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 14, 2, 2, 14, 3, 4, 99, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 12, 4, 1, 14, 4, 3, 99, 14, 0, 0, false);
      this.placeTreasureAtCurrentPosition(world, rand, 12, 2, 3, TFTreasure.LABYRINTH_ROOM, sbb);
      this.fillWithMetadataBlocks(world, sbb, 5, 4, 5, 7, 5, 7, 99, 14, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 8, 4, 8, 10, 5, 10, 100, 14, 0, 0, false);
      if (!this.taurPlaced) {
         int bx = this.getXWithOffset(7, 7);
         int by = this.getYWithOffset(1);
         int bz = this.getZWithOffset(7, 7);
         if (sbb.isVecInside(bx, by, bz)) {
            this.taurPlaced = true;
            // EntityTFMinoshroom with setHomeArea(bx, by, bz, 7): the name carries the home radius
            world.spawnEntity(new OldEntity.Mob(MINOSHROOM, bx, by, bz, 0.0F, null));
         }
      }

      return true;
   }
}
