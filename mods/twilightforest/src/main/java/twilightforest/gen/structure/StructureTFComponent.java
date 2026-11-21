package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

/**
 * StructureTFComponent: the base of the Twilight Forest structure pieces (rotated placement, spawners, treasure chests).
 * Kern-Klasse ohne Spielklassen (wie {@code twilightforest.gen}).
 */
public abstract class StructureTFComponent extends StructureComponent {
   private static final StructureTFStrongholdStones strongholdStones = new StructureTFStrongholdStones();
   private static final StructureTFTowerWoods towerWoods = new StructureTFTowerWoods();
   public StructureTFDecorator deco = null;
   public int spawnListIndex = 0;

   public StructureTFComponent(int i) {
      super(i);
   }

   public static StructureBoundingBox getComponentToAddBoundingBox(int x, int y, int z, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int dir) {
      switch (dir) {
         case 0:
            return new StructureBoundingBox(x + minX, y + minY, z + minZ, x + maxX + minX, y + maxY + minY, z + maxZ + minZ);
         case 1:
            return new StructureBoundingBox(x - maxZ + minZ, y + minY, z + minX, x + minZ, y + maxY + minY, z + maxX + minX);
         case 2:
            return new StructureBoundingBox(x - maxX - minX, y + minY, z - maxZ - minZ, x - minX, y + maxY + minY, z - minZ);
         case 3:
            return new StructureBoundingBox(x + minZ, y + minY, z - maxX, x + maxZ + minZ, y + maxY + minY, z + minX);
         default:
            return new StructureBoundingBox(x + minX, y + minY, z + minZ, x + maxX + minX, y + maxY + minY, z + maxZ + minZ);
      }
   }

   protected OldTile.Spawner placeSpawnerAtCurrentPosition(OldWorld world, Random rand, int x, int y, int z, String monsterID, StructureBoundingBox sbb) {
      OldTile.Spawner tileEntitySpawner = null;
      int dx = this.getXWithOffset(x, z);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffset(x, z);
      if (sbb.isVecInside(dx, dy, dz) && world.getBlockId(dx, dy, dz) != 52) {
         world.setBlock(dx, dy, dz, 52, 0);
         if (world.getBlockId(dx, dy, dz) == 52) {
            tileEntitySpawner = new OldTile.Spawner(monsterID);
            world.setTile(dx, dy, dz, tileEntitySpawner);
         }
      }

      return tileEntitySpawner;
   }

   protected OldTile.Spawner placeSpawnerRotated(OldWorld world, int x, int y, int z, int rotation, String monsterID, StructureBoundingBox sbb) {
      OldTile.Spawner tileEntitySpawner = null;
      int dx = this.getXWithOffsetAsIfRotated(x, z, rotation);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffsetAsIfRotated(x, z, rotation);
      if (sbb.isVecInside(dx, dy, dz) && world.getBlockId(dx, dy, dz) != 52) {
         world.setBlock(dx, dy, dz, 52, 0);
         if (world.getBlockId(dx, dy, dz) == 52) {
            tileEntitySpawner = new OldTile.Spawner(monsterID);
            world.setTile(dx, dy, dz, tileEntitySpawner);
         }
      }

      return tileEntitySpawner;
   }

   protected void placeTreasureAtCurrentPosition(OldWorld world, Random rand, int x, int y, int z, TFTreasure treasureType, StructureBoundingBox sbb) {
      int dx = this.getXWithOffset(x, z);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffset(x, z);
      if (sbb.isVecInside(dx, dy, dz) && world.getBlockId(dx, dy, dz) != 54) {
         treasureType.generate(world, dx, dy, dz);
      }
   }

   protected void placeTreasureRotated(OldWorld world, int x, int y, int z, int rotation, TFTreasure treasureType, StructureBoundingBox sbb) {
      int dx = this.getXWithOffsetAsIfRotated(x, z, rotation);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffsetAsIfRotated(x, z, rotation);
      if (sbb.isVecInside(dx, dy, dz) && world.getBlockId(dx, dy, dz) != 54) {
         treasureType.generate(world, dx, dy, dz);
      }
   }

   protected int[] offsetTowerCoords(int x, int y, int z, int towerSize, int direction) {
      int dx = this.getXWithOffset(x, z);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffset(x, z);
      if (direction == 0) {
         return new int[]{dx + 1, dy - 1, dz - towerSize / 2};
      } else if (direction == 1) {
         return new int[]{dx + towerSize / 2, dy - 1, dz + 1};
      } else if (direction == 2) {
         return new int[]{dx - 1, dy - 1, dz + towerSize / 2};
      } else {
         return direction == 3 ? new int[]{dx - towerSize / 2, dy - 1, dz - 1} : new int[]{x, y, z};
      }
   }

   public int[] getOffsetAsIfRotated(int[] src, int rotation) {
      int temp = this.getCoordBaseMode();
      int[] dest = new int[3];
      this.setCoordBaseMode(rotation);
      dest[0] = this.getXWithOffset(src[0], src[2]);
      dest[1] = this.getYWithOffset(src[1]);
      dest[2] = this.getZWithOffset(src[0], src[2]);
      this.setCoordBaseMode(temp);
      return dest;
   }

   protected int getXWithOffset(int x, int z) {
      switch (this.getCoordBaseMode()) {
         case 0:
            return this.boundingBox.minX + x;
         case 1:
            return this.boundingBox.maxX - z;
         case 2:
            return this.boundingBox.maxX - x;
         case 3:
            return this.boundingBox.minX + z;
         default:
            return x;
      }
   }

   protected int getYWithOffset(int par1) {
      return super.getYWithOffset(par1);
   }

   protected int getZWithOffset(int x, int z) {
      switch (this.getCoordBaseMode()) {
         case 0:
            return this.boundingBox.minZ + z;
         case 1:
            return this.boundingBox.minZ + x;
         case 2:
            return this.boundingBox.maxZ - z;
         case 3:
            return this.boundingBox.maxZ - x;
         default:
            return z;
      }
   }

   protected int getXWithOffsetAsIfRotated(int x, int z, int rotation) {
      if (this.coordBaseMode < 0) {
         return x;
      }

      switch ((this.coordBaseMode + rotation) % 4) {
         case 0:
            return this.boundingBox.minX + x;
         case 1:
            return this.boundingBox.maxX - z;
         case 2:
            return this.boundingBox.maxX - x;
         case 3:
            return this.boundingBox.minX + z;
         default:
            return x;
      }
   }

   protected int getZWithOffsetAsIfRotated(int x, int z, int rotation) {
      if (this.coordBaseMode < 0) {
         return x;
      }

      switch ((this.coordBaseMode + rotation) % 4) {
         case 0:
            return this.boundingBox.minZ + z;
         case 1:
            return this.boundingBox.minZ + x;
         case 2:
            return this.boundingBox.maxZ - z;
         case 3:
            return this.boundingBox.maxZ - x;
         default:
            return z;
      }
   }

   public int getCoordBaseMode() {
      return this.coordBaseMode;
   }

   public void setCoordBaseMode(int coordBaseMode) {
      this.coordBaseMode = coordBaseMode;
   }

   protected int getBlockIdAtCurrentPosition(OldWorld par1World, int par2, int par3, int par4, StructureBoundingBox par5StructureBoundingBox) {
      return super.getBlockIdAtCurrentPosition(par1World, par2, par3, par4, par5StructureBoundingBox);
   }

   protected void placeBlockAtCurrentPosition(OldWorld par1World, int par2, int par3, int par4, int par5, int par6, StructureBoundingBox par7StructureBoundingBox) {
      super.placeBlockAtCurrentPosition(par1World, par2, par3, par4, par5, par6, par7StructureBoundingBox);
   }

   protected void placeBlockRotated(OldWorld world, int blockID, int meta, int x, int y, int z, int rotation, StructureBoundingBox sbb) {
      int dx = this.getXWithOffsetAsIfRotated(x, z, rotation);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffsetAsIfRotated(x, z, rotation);
      if (sbb.isVecInside(dx, dy, dz)) {
         world.setBlock(dx, dy, dz, blockID, meta);
      }
   }

   protected int getBlockIDRotated(OldWorld world, int x, int y, int z, int rotation, StructureBoundingBox sbb) {
      int dx = this.getXWithOffsetAsIfRotated(x, z, rotation);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffsetAsIfRotated(x, z, rotation);
      return sbb.isVecInside(dx, dy, dz) ? world.getBlockId(dx, dy, dz) : 0;
   }

   protected void fillBlocksRotated(
      OldWorld world, StructureBoundingBox sbb, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int blockID, int meta, int rotation
   ) {
      for (int dx = minY; dx <= maxY; dx++) {
         for (int dy = minX; dy <= maxX; dy++) {
            for (int dz = minZ; dz <= maxZ; dz++) {
               this.placeBlockRotated(world, blockID, meta, dy, dx, dz, rotation, sbb);
            }
         }
      }
   }

   protected void fillAirRotated(OldWorld world, StructureBoundingBox sbb, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int rotation) {
      this.fillBlocksRotated(world, sbb, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, rotation);
   }

   public static StructurePieceBlockSelector getStrongholdStones() {
      return strongholdStones;
   }

   public static StructurePieceBlockSelector getTowerWoods() {
      return towerWoods;
   }

   protected int getStairMeta(int dir) {
      switch ((this.getCoordBaseMode() + dir) % 4) {
         case 0:
            return 0;
         case 1:
            return 2;
         case 2:
            return 1;
         case 3:
            return 3;
         default:
            return -1;
      }
   }

   protected int getLadderMeta(int ladderDir) {
      switch ((this.getCoordBaseMode() + ladderDir) % 4) {
         case 0:
            return 4;
         case 1:
            return 2;
         case 2:
            return 5;
         case 3:
            return 3;
         default:
            return -1;
      }
   }

   protected int getLadderMeta(int ladderDir, int rotation) {
      return this.getLadderMeta(ladderDir + rotation);
   }

   public void nullifySkyLightForBoundingBox(OldWorld world) {
      this.nullifySkyLight(
         world,
         this.boundingBox.minX - 1,
         this.boundingBox.minY - 1,
         this.boundingBox.minZ - 1,
         this.boundingBox.maxX + 1,
         this.boundingBox.maxY + 1,
         this.boundingBox.maxZ + 1
      );
   }

   public void nullifySkyLightAtCurrentPosition(OldWorld world, int sx, int sy, int sz, int dx, int dy, int dz) {
      this.nullifySkyLight(
         world,
         this.getXWithOffset(sx, sz),
         this.getYWithOffset(sy),
         this.getZWithOffset(sx, sz),
         this.getXWithOffset(dx, dz),
         this.getYWithOffset(dy),
         this.getZWithOffset(dx, dz)
      );
   }

   public void nullifySkyLight(OldWorld world, int sx, int sy, int sz, int dx, int dy, int dz) {
      // World.setLightValue(Sky, ..., 0): the core keeps no light values, it only changes lighting and mob spawns
   }
}
