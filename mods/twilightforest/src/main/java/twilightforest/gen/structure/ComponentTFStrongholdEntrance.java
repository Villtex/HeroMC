package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFStrongholdEntrance extends StructureTFStrongholdComponent {
   public TFStrongholdPieces lowerPieces;

   public ComponentTFStrongholdEntrance(OldWorld world, Random rand, int i, int x, int y, int z) {
      super(i, 0, x, y - 10, z);
      this.deco = new StructureTFDecoratorStronghold();
      this.lowerPieces = new TFStrongholdPieces();
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random random) {
      super.buildComponent(parent, list, random);
      this.lowerPieces.prepareStructurePieces();
      this.addNewComponent(parent, list, random, 0, 4, 1, 18);
      this.lowerPieces.prepareStructurePieces();
      if (this.listContainsBossRoom(list)) {
         this.lowerPieces.markBossRoomUsed();
      }

      this.addNewComponent(parent, list, random, 1, -1, 1, 13);
      this.lowerPieces.prepareStructurePieces();
      if (this.listContainsBossRoom(list)) {
         this.lowerPieces.markBossRoomUsed();
      }

      this.addNewComponent(parent, list, random, 2, 13, 1, -1);
      this.lowerPieces.prepareStructurePieces();
      if (this.listContainsBossRoom(list)) {
         this.lowerPieces.markBossRoomUsed();
      }

      this.addNewComponent(parent, list, random, 3, 18, 1, 4);
      if (!this.listContainsBossRoom(list)) {
         // System.out.println("Did not find boss room from exit 3 - EPIC FAIL");
      }

      List pieceList = list;
      StructureBoundingBox shieldBox = new StructureBoundingBox(this.boundingBox);
      int tStairs = 0;
      int tCorridors = 0;
      int deadEnd = 0;
      int tRooms = 0;
      int bossRooms = 0;

      for (Object o : pieceList) {
         StructureComponent component = (StructureComponent) o;
         shieldBox.expandTo(component.getBoundingBox());
         if (component instanceof ComponentTFStrongholdSmallStairs && ((ComponentTFStrongholdSmallStairs)component).hasTreasure) {
            tStairs++;
         }

         if (component instanceof ComponentTFStrongholdTreasureCorridor) {
            tCorridors++;
         }

         if (component instanceof ComponentTFStrongholdDeadEnd) {
            deadEnd++;
         }

         if (component instanceof ComponentTFStrongholdTreasureRoom) {
            tRooms++;
         }

         if (component instanceof ComponentTFStrongholdBossRoom) {
            bossRooms++;
         }
      }

      // System.out.printf("Room count! TStairs = %d, TCorr = %d, Dead End = %d, TRoom = %d, Boss = %d \n", tStairs, tCorridors, deadEnd, tRooms, bossRooms);
      StructureTFStrongholdShield shield = new StructureTFStrongholdShield(
         shieldBox.minX, shieldBox.minY, shieldBox.minZ, shieldBox.maxX, shieldBox.maxY, shieldBox.maxZ
      );
      list.add(shield);
      StructureTFStrongholdComponent accessChamber = new ComponentTFStrongholdAccessChamber(
         2, this.getCoordBaseMode(), this.boundingBox.minX + 8, this.boundingBox.minY + 9, this.boundingBox.minZ + 4
      );
      list.add(accessChamber);
      accessChamber.buildComponent(this, list, random);
   }

   private boolean listContainsBossRoom(List list) {
      for (Object o : list) {
         StructureComponent component = (StructureComponent) o;
         if (component instanceof ComponentTFStrongholdBossRoom) {
            return true;
         }
      }

      return false;
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 18, 6, 18, facing);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithRandomizedBlocks(world, sbb, 0, 0, 0, 17, 6, 17, false, rand, StructureTFComponent.getStrongholdStones());
      int var8 = this.getXWithOffset(0, 0);
      int var9 = this.getYWithOffset(0);
      int var10 = this.getZWithOffset(0, 0);
      this.placeCornerStatue(world, 5, 1, 5, 0, sbb);
      this.placeCornerStatue(world, 5, 1, 12, 1, sbb);
      this.placeCornerStatue(world, 12, 1, 5, 2, sbb);
      this.placeCornerStatue(world, 12, 1, 12, 3, sbb);
      this.placeWallStatue(world, 9, 1, 16, 0, sbb);
      this.placeWallStatue(world, 1, 1, 9, 1, sbb);
      this.placeWallStatue(world, 8, 1, 1, 2, sbb);
      this.placeWallStatue(world, 16, 1, 8, 3, sbb);
      this.placeDoors(world, rand, sbb);
      return true;
   }
}
