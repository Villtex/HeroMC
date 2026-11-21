package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdTreasureCorridor extends StructureTFStrongholdComponent {
   public ComponentTFStrongholdTreasureCorridor(int i, int facing, int x, int y, int z) {
      super(i, facing, x, y, z);
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return StructureTFStrongholdComponent.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 9, 6, 27, facing);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.addDoor(4, 1, 0);
      this.addNewComponent(parent, list, random, 0, 4, 1, 27);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeStrongholdWalls(world, sbb, 0, 0, 0, 8, 6, 26, rand, this.deco.randomBlocks);
      this.placeWallStatue(world, 1, 1, 9, 1, sbb);
      this.placeWallStatue(world, 1, 1, 17, 1, sbb);
      this.placeWallStatue(world, 7, 1, 9, 3, sbb);
      this.placeWallStatue(world, 7, 1, 17, 3, sbb);
      int rotation = (this.boundingBox.minX ^ this.boundingBox.minZ) % 2 == 0 ? 0 : 2;
      this.placeTreasureRotated(world, 8, 2, 13, rotation, TFTreasure.STRONGHOLD_CACHE, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 8, 3, 12, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation) + 4, 8, 3, 13, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation) + 4, 8, 3, 14, rotation, sbb);
      this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 8, 2, 12, rotation, sbb);
      this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 8, 2, 14, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation), 7, 1, 12, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation), 7, 1, 13, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), 7, 1, 14, rotation, sbb);
      this.placeDoors(world, rand, sbb);
      return true;
   }
}
