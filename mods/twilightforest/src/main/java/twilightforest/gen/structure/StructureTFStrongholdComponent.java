package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class StructureTFStrongholdComponent extends StructureTFComponent {
   public List doors;

   public StructureTFStrongholdComponent(int i, int facing, int x, int y, int z) {
      super(i);
      this.boundingBox = this.generateBoundingBox(facing, x, y, z);
      this.setCoordBaseMode(facing);
   }

   public abstract StructureBoundingBox generateBoundingBox(int var1, int var2, int var3, int var4);

   public static StructureBoundingBox getComponentToAddBoundingBox(
      int x, int y, int z, int xOff, int yOff, int zOff, int xSize, int ySize, int zSize, int facing
   ) {
      switch (facing) {
         case 0:
            return new StructureBoundingBox(x + xOff, y + yOff, z + zOff, x + xSize - 1 + xOff, y + ySize - 1 + yOff, z + zSize - 1 + zOff);
         case 1:
            return new StructureBoundingBox(x - zSize + 1 + zOff, y + yOff, z + xOff, x + zOff, y + ySize - 1 + yOff, z + xSize - 1 + xOff);
         case 2:
            return new StructureBoundingBox(x - xSize + 1 - xOff, y + yOff, z - zSize + 1 + zOff, x - xOff, y + ySize - 1 + yOff, z + zOff);
         case 3:
            return new StructureBoundingBox(x + zOff, y + yOff, z - xSize + 1 - xOff, x + zSize - 1 + zOff, y + ySize - 1 + yOff, z - xOff);
         default:
            return new StructureBoundingBox(x + xOff, y + yOff, z + zOff, x + xSize - 1 + xOff, y + ySize - 1 + yOff, z + zSize - 1 + zOff);
      }
   }

   public void buildComponent(StructureComponent parent, List list, Random rand) {
      if (parent != null && parent instanceof StructureTFComponent) {
         this.deco = ((StructureTFComponent)parent).deco;
      }
   }

   protected void addNewComponent(StructureComponent entrance, List list, Random random, int facing, int x, int y, int z) {
      int index = this.componentType + 1;
      int nFacing = (this.coordBaseMode + facing) % 4;
      int nx = this.getXWithOffset(x, z);
      int ny = this.getYWithOffset(y);
      int nz = this.getZWithOffset(x, z);
      if (index <= 50 && !this.isOutOfRange(entrance, nx, ny, nz, 112)) {
         StructureTFStrongholdComponent breakIn = this.findBreakInComponent(list, nx, ny, nz);
         if (breakIn != null && breakIn.attemptToBreakIn(nx, ny, nz)) {
            this.addDoorwayTo(x, y, z, facing);
         } else {
            TFStrongholdPieces pieceList = ((ComponentTFStrongholdEntrance)entrance).lowerPieces;
            StructureTFStrongholdComponent nextComponent = pieceList.getNextComponent(entrance, list, random, index, nFacing, nx, ny, nz);
            if (nextComponent != null) {
               list.add(nextComponent);
               nextComponent.buildComponent(entrance, list, random);
               this.addDoorwayTo(x, y, z, facing);
            }
         }
      }
   }

   protected StructureTFStrongholdComponent findBreakInComponent(List list, int x, int y, int z) {
      for (Object o : list) {
         StructureTFStrongholdComponent component = (StructureTFStrongholdComponent) o;
         if (component.boundingBox != null && component.boundingBox.isVecInside(x, y, z)) {
            return component;
         }
      }

      return null;
   }

   protected void addNewUpperComponent(StructureComponent parent, List list, Random random, int facing, int x, int y, int z) {
      StructureTFStrongholdComponent attempted = null;
      int index = this.componentType + 1;
      int nFacing = (this.coordBaseMode + facing) % 4;
      int nx = this.getXWithOffset(x, z);
      int ny = this.getYWithOffset(y);
      int nz = this.getZWithOffset(x, z);
      if (index <= 100 && !this.isOutOfRange(parent, nx, ny, nz, 48)) {
         switch (random.nextInt(5)) {
            case 0:
            default:
               attempted = new ComponentTFStrongholdUpperTIntersection(index, nFacing, nx, ny, nz);
               break;
            case 1:
               attempted = new ComponentTFStrongholdUpperLeftTurn(index, nFacing, nx, ny, nz);
               break;
            case 2:
               attempted = new ComponentTFStrongholdUpperRightTurn(index, nFacing, nx, ny, nz);
               break;
            case 3:
               attempted = new ComponentTFStrongholdUpperCorridor(index, nFacing, nx, ny, nz);
               break;
            case 4:
               attempted = new ComponentTFStrongholdUpperAscender(index, nFacing, nx, ny, nz);
         }

         if (attempted != null && StructureComponent.findIntersecting(list, attempted.getBoundingBox()) == null) {
            list.add(attempted);
            attempted.buildComponent(parent, list, random);
         }
      }
   }

   private boolean isOutOfRange(StructureComponent parent, int nx, int ny, int nz, int range) {
      return Math.abs(nx - parent.getBoundingBox().minX) > range || Math.abs(nz - parent.getBoundingBox().minZ) > range;
   }

   protected void placeDoorwayAt(OldWorld world, Random rand, int x, int y, int z, StructureBoundingBox sbb) {
      if (x != 0 && x != this.getXSize()) {
         this.fillWithMetadataBlocks(world, sbb, x - 2, y, z, x + 2, y + 3, z, this.deco.fenceID, this.deco.fenceMeta, 0, 0, false);
         this.fillWithAir(world, sbb, x - 1, y, z, x + 1, y + 3, z);
      } else {
         this.fillWithMetadataBlocks(world, sbb, x, y, z - 2, x, y + 3, z + 2, this.deco.fenceID, this.deco.fenceMeta, 0, 0, false);
         this.fillWithAir(world, sbb, x, y, z - 1, x, y + 3, z + 1);
      }
   }

   protected int getXSize() {
      switch (this.getCoordBaseMode()) {
         case 0:
         case 2:
         default:
            return this.boundingBox.getXSize() - 1;
         case 1:
         case 3:
            return this.boundingBox.getZSize() - 1;
      }
   }

   protected void placeSmallDoorwayAt(OldWorld world, Random rand, int facing, int x, int y, int z, StructureBoundingBox sbb) {
      if (facing != 0 && facing != 2) {
         this.fillWithMetadataBlocks(world, sbb, x, y, z - 1, x, y + 1, z + 1, 139, 0, 0, 0, true);
         this.fillWithAir(world, sbb, x, y, z, x, y + 1, z);
      } else {
         this.fillWithMetadataBlocks(world, sbb, x - 1, y, z, x + 1, y + 1, z, 139, 0, 0, 0, true);
         this.fillWithAir(world, sbb, x, y, z, x, y + 1, z);
      }
   }

   public void placeCornerStatue(OldWorld world, int x, int y, int z, int facing, StructureBoundingBox sbb) {
      int ox = 1;
      int oz = 1;
      int smx = 2;
      int smz = 3;
      switch (facing) {
         case 0:
         default:
            break;
         case 1:
            oz = -1;
            smz = 1;
            break;
         case 2:
            ox = -1;
            smx = 0;
            break;
         case 3:
            ox = -1;
            oz = -1;
            smx = 0;
            smz = 1;
      }

      for (int sy = 0; sy < 5; sy++) {
         this.placeBlockAtCurrentPosition(world, this.deco.pillarID, this.deco.pillarMeta, x, y + sy, z, sbb);
      }

      this.placeBlockAtCurrentPosition(world, 85, 0, x + 0, y + 4, z + oz, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, x + ox, y + 4, z + 0, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(smz), x + 0, y + 3, z + oz, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(smx), x + ox, y + 3, z + 0, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(smz) + 4, x + 0, y + 2, z + oz, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(smx) + 4, x + ox, y + 2, z + 0, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(smx) + 4, x + ox, y + 2, z + oz, sbb);
      this.placeBlockAtCurrentPosition(world, 139, 0, x + ox, y + 0, z + oz, sbb);
      this.placeBlockAtCurrentPosition(world, 139, 0, x + ox, y + 1, z + oz, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(smz), x + 0, y + 0, z + oz, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(smx), x + ox, y + 0, z + 0, sbb);
   }

   public void placeWallStatue(OldWorld world, int x, int y, int z, int facing, StructureBoundingBox sbb) {
      int ox = 1;
      int oz = 1;

      for (int sy = 0; sy < 5; sy++) {
         this.placeBlockAtCurrentPosition(world, this.deco.pillarID, this.deco.pillarMeta, x, y + sy, z, sbb);
      }

      if (facing != 0 && facing != 2) {
         if (facing == 3) {
            oz = -oz;
            ox = -ox;
         }

         this.placeBlockAtCurrentPosition(world, 85, 0, x, y + 4, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, 85, 0, x, y + 4, z + oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(0 + facing), x, y + 3, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(2 + facing), x, y + 3, z + oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing), x + ox, y + 3, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing), x + ox, y + 3, z + oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(0 + facing) + 4, x, y + 2, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(2 + facing) + 4, x, y + 2, z + oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing) + 4, x + oz, y + 2, z + 0, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing) + 4, x + ox, y + 2, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing) + 4, x + ox, y + 2, z + oz, sbb);
         this.placeBlockAtCurrentPosition(world, 139, 0, x + ox, y + 0, z, sbb);
         this.placeBlockAtCurrentPosition(world, 139, 0, x + ox, y + 1, z, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(0 + facing), x, y + 0, z - ox, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(2 + facing), x, y + 0, z + ox, sbb);
      } else {
         if (facing == 2) {
            ox = -ox;
            oz = -oz;
         }

         this.placeBlockAtCurrentPosition(world, 85, 0, x - ox, y + 4, z, sbb);
         this.placeBlockAtCurrentPosition(world, 85, 0, x + ox, y + 4, z, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(0 + facing), x - ox, y + 3, z, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(2 + facing), x + ox, y + 3, z, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing), x - ox, y + 3, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing), x + ox, y + 3, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(0 + facing) + 4, x - ox, y + 2, z, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(2 + facing) + 4, x + ox, y + 2, z, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing) + 4, x + 0, y + 2, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing) + 4, x - ox, y + 2, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(1 + facing) + 4, x + ox, y + 2, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, 139, 0, x, y + 0, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, 139, 0, x, y + 1, z - oz, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(0 + facing), x - ox, y + 0, z + 0, sbb);
         this.placeBlockAtCurrentPosition(world, this.deco.stairID, this.getStairMeta(2 + facing), x + ox, y + 0, z + 0, sbb);
      }
   }

   public boolean attemptToBreakIn(int wx, int wy, int wz) {
      if (!this.isValidBreakInPoint(wx, wy, wz)) {
         return false;
      }

      int dx = this.getRelativeX(wx, wz);
      int dy = this.getRelativeY(wy);
      int dz = this.getRelativeZ(wx, wz);
      this.addDoor(dx, dy, dz);
      return true;
   }

   public void addDoorwayTo(int dx, int dy, int dz, int facing) {
      switch (facing) {
         case 0:
            this.addDoor(dx, dy, dz - 1);
            break;
         case 1:
            this.addDoor(dx + 1, dy, dz);
            break;
         case 2:
            this.addDoor(dx, dy, dz + 1);
            break;
         case 3:
            this.addDoor(dx - 1, dy, dz);
      }
   }

   public void addDoor(int dx, int dy, int dz) {
      if (this.doors == null) {
         this.doors = new ArrayList();
      }

      this.doors.add(new ChunkCoordinates(dx, dy, dz));
   }

   protected boolean isValidBreakInPoint(int wx, int wy, int wz) {
      if (wy < this.boundingBox.minY || wy > this.boundingBox.maxY) {
         return false;
      } else if (wx == this.boundingBox.minX || wx == this.boundingBox.maxX) {
         return wz > this.boundingBox.minZ && wz < this.boundingBox.maxZ;
      } else {
         return wz != this.boundingBox.minZ && wz != this.boundingBox.maxZ
            ? false
            : wx > this.boundingBox.minX && wx < this.boundingBox.maxX;
      }
   }

   protected int getRelativeX(int x, int z) {
      switch (this.getCoordBaseMode()) {
         case 0:
            return x - this.boundingBox.minX;
         case 1:
            return z - this.boundingBox.minZ;
         case 2:
            return this.boundingBox.maxX - x;
         case 3:
            return this.boundingBox.maxZ - z;
         default:
            return x;
      }
   }

   protected int getRelativeY(int par1) {
      return par1 - this.boundingBox.minY;
   }

   protected int getRelativeZ(int x, int z) {
      switch (this.getCoordBaseMode()) {
         case 0:
            return z - this.boundingBox.minZ;
         case 1:
            return this.boundingBox.maxX - x;
         case 2:
            return this.boundingBox.maxZ - z;
         case 3:
            return x - this.boundingBox.minX;
         default:
            return z;
      }
   }

   public void placeDoors(OldWorld world, Random rand, StructureBoundingBox sbb) {
      if (this.doors != null) {
         for (Object o : this.doors) {
            ChunkCoordinates doorCoords = (ChunkCoordinates) o;
            this.placeDoorwayAt(world, rand, doorCoords.posX, doorCoords.posY, doorCoords.posZ, sbb);
         }
      }
   }

   protected void placeStrongholdWalls(
      OldWorld world, StructureBoundingBox sbb, int sx, int sy, int sz, int dx, int dy, int dz, Random rand, StructurePieceBlockSelector randomBlocks
   ) {
      for (int y = sy; y <= dy; y++) {
         for (int x = sx; x <= dx; x++) {
            for (int z = sz; z <= dz; z++) {
               boolean wall = y == sy || y == dy || x == sx || x == dx || z == sz || z == dz;
               int blockID = this.getBlockIdAtCurrentPosition(world, x, y, z, sbb);
               if (blockID == 0) {
                  if (wall) {
                     this.placeBlockAtCurrentPosition(world, 4, 0, x, y, z, sbb);
                  }
               } else if (!wall || blockID != 3) {
                  randomBlocks.selectBlocks(rand, x, y, z, wall);
                  this.placeBlockAtCurrentPosition(world, randomBlocks.getSelectedBlockId(), randomBlocks.getSelectedBlockMetaData(), x, y, z, sbb);
               }
            }
         }
      }
   }

   protected void placeUpperStrongholdWalls(
      OldWorld world, StructureBoundingBox sbb, int sx, int sy, int sz, int dx, int dy, int dz, Random rand, StructurePieceBlockSelector randomBlocks
   ) {
      for (int y = sy; y <= dy; y++) {
         for (int x = sx; x <= dx; x++) {
            for (int z = sz; z <= dz; z++) {
               boolean wall = y == sy || y == dy || x == sx || x == dx || z == sz || z == dz;
               int blockID = this.getBlockIdAtCurrentPosition(world, x, y, z, sbb);
               if (blockID > 0
                  && (
                     OldBlockInfo.material(blockID) == OldBlockInfo.Material.ROCK
                        || OldBlockInfo.material(blockID) == OldBlockInfo.Material.GRASS
                        || OldBlockInfo.material(blockID) == OldBlockInfo.Material.GROUND
                  )) {
                  randomBlocks.selectBlocks(rand, x, y, z, wall);
                  this.placeBlockAtCurrentPosition(world, randomBlocks.getSelectedBlockId(), randomBlocks.getSelectedBlockMetaData(), x, y, z, sbb);
               }
            }
         }
      }
   }
}
