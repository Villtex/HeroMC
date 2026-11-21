package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFMazeRoomSpawnerChests extends ComponentTFMazeRoom {
   public ComponentTFMazeRoomSpawnerChests(int i, Random rand, int x, int y, int z) {
      super(i, rand, x, y, z);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      super.addComponentParts(world, rand, sbb);
      this.placePillarEnclosure(world, sbb, 3, 3);
      this.placePillarEnclosure(world, sbb, 10, 3);
      this.placePillarEnclosure(world, sbb, 3, 10);
      this.placePillarEnclosure(world, sbb, 10, 10);
      this.placeSpawnerAtCurrentPosition(world, rand, 4, 2, 4, "TwilightForest.Minotaur", sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 4, 2, 11, TFTreasure.LABYRINTH_ROOM, sbb);
      this.placeTreasureAtCurrentPosition(world, rand, 11, 2, 4, TFTreasure.LABYRINTH_ROOM, sbb);
      this.placeBlockAtCurrentPosition(world, 72, 0, 11, 1, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 46, 0, 10, 0, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 46, 0, 11, 0, 10, sbb);
      this.placeBlockAtCurrentPosition(world, 46, 0, 11, 0, 12, sbb);
      this.placeBlockAtCurrentPosition(world, 46, 0, 12, 0, 11, sbb);
      return true;
   }

   private void placePillarEnclosure(OldWorld world, StructureBoundingBox sbb, int dx, int dz) {
      for (int y = 1; y < 5; y++) {
         this.placeBlockAtCurrentPosition(world, 1362, 2, dx + 0, y, dz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 1362, 2, dx + 2, y, dz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 1362, 2, dx + 0, y, dz + 2, sbb);
         this.placeBlockAtCurrentPosition(world, 1362, 2, dx + 2, y, dz + 2, sbb);
      }

      this.placeBlockAtCurrentPosition(world, 5, 0, dx + 1, 1, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 0, dx + 1, 4, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(1), dx + 1, 1, dz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(0), dx + 0, 1, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(2), dx + 2, 1, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(3), dx + 1, 1, dz + 2, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(1) + 4, dx + 1, 4, dz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(0) + 4, dx + 0, 4, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(2) + 4, dx + 2, 4, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 53, this.getStairMeta(3) + 4, dx + 1, 4, dz + 2, sbb);
      this.placeBlockAtCurrentPosition(world, 101, 0, dx + 1, 2, dz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 101, 0, dx + 0, 2, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 101, 0, dx + 2, 2, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 101, 0, dx + 1, 2, dz + 2, sbb);
      this.placeBlockAtCurrentPosition(world, 101, 0, dx + 1, 3, dz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 101, 0, dx + 0, 3, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 101, 0, dx + 2, 3, dz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 101, 0, dx + 1, 3, dz + 2, sbb);
   }
}
