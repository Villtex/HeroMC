package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFHollowHill extends StructureTFComponent {
   int hillSize;
   int radius;

   public ComponentTFHollowHill(OldWorld world, Random rand, int i, int size, int x, int y, int z) {
      super(i);
      this.setCoordBaseMode(0);
      this.hillSize = size;
      this.radius = (this.hillSize * 2 + 1) * 8 - 6;
      this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, -this.radius, -3, -this.radius, this.radius * 2, 10, this.radius * 2, 0);
   }

   public void buildComponent(StructureComponent structurecomponent, List list, Random random) {
      super.buildComponent(structurecomponent, list, random);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int[] sna = new int[]{0, 128, 256, 512};
      int sn = sna[this.hillSize];
      int[] mga = new int[]{0, 1, 4, 9};
      int mg = mga[this.hillSize];
      int[] tca = new int[]{0, 2, 6, 12};
      int tc = tca[this.hillSize];

      for (int i = 0; i < mg; i++) {
         int[] dest = this.getCoordsInHill2D(rand);
         String mobID = this.getMobID(rand);
         this.placeSpawnerAtCurrentPosition(world, rand, dest[0], rand.nextInt(4), dest[1], mobID, sbb);
      }

      for (int i = 0; i < tc; i++) {
         int[] dest = this.getCoordsInHill2D(rand);
         this.generateTreasureChest(world, dest[0], 0, dest[1], sbb);
      }

      for (int i = 0; i < sn; i++) {
         int[] dest = this.getCoordsInHill2D(rand);
         this.generateOreStalactite(world, dest[0], 1, dest[1], sbb);
      }

      for (int i = 0; i < sn; i++) {
         int[] dest = this.getCoordsInHill2D(rand);
         this.generateStoneStalactite(world, 1.0F, true, dest[0], 1, dest[1], sbb);
      }

      for (int i = 0; i < sn; i++) {
         int[] dest = this.getCoordsInHill2D(rand);
         this.generateStoneStalactite(world, 0.9F, false, dest[0], 1, dest[1], sbb);
      }

      if (this.hillSize == 3) {
      }

      return true;
   }

   protected void generateTreasureChest(OldWorld world, int x, int y, int z, StructureBoundingBox sbb) {
      Random chestRNG = new Random(world.seed() + x * z);
      this.placeTreasureAtCurrentPosition(
         world, chestRNG, x, y, z, this.hillSize == 3 ? TFTreasure.HILL3 : (this.hillSize == 2 ? TFTreasure.HILL2 : TFTreasure.HILL1), sbb
      );
      this.fillCurrentPositionBlocksDownwards(world, 4, 0, x, y - 1, z, sbb);
   }

   protected void generateOreStalactite(OldWorld world, int x, int y, int z, StructureBoundingBox sbb) {
      int dx = this.getXWithOffset(x, z);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffset(x, z);
      if (sbb.isVecInside(dx, dy, dz) && world.getBlockId(dx, dy, dz) != 52) {
         Random stalRNG = new Random(world.seed() + dx * dz);
         TFRuins.Stalactite stalag = TFRuins.Stalactite.makeRandomOreStalactite(stalRNG, this.hillSize);
         stalag.generate(world, stalRNG, dx, dy, dz);
      }
   }

   protected void generateStoneStalactite(OldWorld world, float length, boolean up, int x, int y, int z, StructureBoundingBox sbb) {
      int dx = this.getXWithOffset(x, z);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffset(x, z);
      if (sbb.isVecInside(dx, dy, dz) && world.getBlockId(dx, dy, dz) != 52) {
         Random stalRNG = new Random(world.seed() + dx * dz);
         if (this.hillSize == 1) {
            length *= 1.9F;
         }

         new TFRuins.Stalactite(1, length, up).generate(world, stalRNG, dx, dy, dz);
      }
   }

   boolean isInHill(int cx, int cz) {
      int dx = this.radius - cx;
      int dz = this.radius - cz;
      int dist = (int)Math.sqrt(dx * dx + dz * dz);
      return dist < this.radius;
   }

   boolean isInHill(int mapX, int mapY, int mapZ) {
      int dx = this.boundingBox.minX + this.radius - mapX;
      int dy = (this.boundingBox.minY - mapY) * 2;
      int dz = this.boundingBox.minZ + this.radius - mapZ;
      int dist = dx * dx + dy * dy + dz * dz;
      return dist < this.radius * this.radius;
   }

   int[] getCoordsInHill2D(Random rand) {
      return this.getCoordsInHill2D(rand, this.radius);
   }

   int[] getCoordsInHill2D(Random rand, int rad) {
      int rx;
      int rz;
      do {
         rx = rand.nextInt(2 * rad);
         rz = rand.nextInt(2 * rad);
      } while (!this.isInHill(rx, rz));

      return new int[]{rx, rz};
   }

   protected String getMobID(Random rand) {
      return this.getMobID(rand, this.hillSize);
   }

   protected String getMobID(Random rand, int level) {
      if (level == 1) {
         return this.getLevel1Mob(rand);
      } else if (level == 2) {
         return this.getLevel2Mob(rand);
      } else {
         return level == 3 ? this.getLevel3Mob(rand) : "Spider";
      }
   }

   public String getLevel1Mob(Random rand) {
      switch (rand.nextInt(10)) {
         case 0:
         case 1:
         case 2:
            return "TwilightForest.Swarm Spider";
         case 3:
         case 4:
         case 5:
            return "Spider";
         case 6:
         case 7:
            return "Zombie";
         case 8:
            return "Silverfish";
         case 9:
            return "TwilightForest.Redcap";
         default:
            return "TwilightForest.Swarm Spider";
      }
   }

   public String getLevel2Mob(Random rand) {
      switch (rand.nextInt(10)) {
         case 0:
         case 1:
         case 2:
            return "TwilightForest.Redcap";
         case 3:
         case 4:
         case 5:
            return "Zombie";
         case 6:
         case 7:
            return "Skeleton";
         case 8:
            return "TwilightForest.Swarm Spider";
         case 9:
            return "CaveSpider";
         default:
            return "TwilightForest.Redcap";
      }
   }

   public String getLevel3Mob(Random rand) {
      switch (rand.nextInt(11)) {
         case 0:
            return "TwilightForest.Slime Beetle";
         case 1:
            return "TwilightForest.Fire Beetle";
         case 2:
            return "TwilightForest.Pinch Beetle";
         case 3:
         case 4:
         case 5:
            return "Skeleton";
         case 6:
         case 7:
         case 8:
            return "CaveSpider";
         case 9:
            return "Creeper";
         case 10:
         default:
            return "TwilightForest.Twilight Wraith";
      }
   }
}
