package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldPainting;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComponentTFTowerWing extends StructureTFComponent {
   public int size;
   protected int height;
   protected Class roofType;
   protected ArrayList<ChunkCoordinates> openings = new ArrayList<>();
   protected int highestOpening;
   protected boolean[] openingTowards;

   protected ComponentTFTowerWing(int i, int x, int y, int z, int pSize, int pHeight, int direction) {
      super(i);
      this.size = pSize;
      this.height = pHeight;
      this.setCoordBaseMode(direction);
      this.highestOpening = 0;
      this.openingTowards = new boolean[]{false, false, true, false};
      this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1, direction);
   }

   public void buildComponent(StructureComponent parent, List list, Random rand) {
      this.addOpening(0, 1, this.size / 2, 2);
      this.makeARoof(parent, list, rand);
      this.makeABeard(parent, list, rand);
      if (this.size > 4) {
         for (int i = 0; i < 4; i++) {
            if (i != 2) {
               int[] dest = this.getValidOpening(rand, i);
               if (!this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], this.size - 2, this.height - 4, i)
                  && this.size > 8
                  && !this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], this.size - 4, this.height - 6, i)) {
                  this.makeTowerWing(list, rand, 1, dest[0], dest[1], dest[2], this.size - 6, this.height - 12, i);
               }
            }
         }
      }
   }

   public boolean makeTowerWing(List list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, int rotation) {
      if (wingHeight < 6) {
         return false;
      } else {
         int direction = (this.getCoordBaseMode() + rotation) % 4;
         int[] dx = this.offsetTowerCoords(x, y, z, wingSize, direction);
         if (rand.nextInt(6) == 0) {
            return this.makeBridge(list, rand, index, x, y, z, wingSize, wingHeight, rotation);
         } else {
            ComponentTFTowerWing wing = new ComponentTFTowerWing(index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
            StructureComponent intersect = StructureComponent.findIntersecting(list, wing.boundingBox);
            if (intersect == null || intersect == this) {
               list.add(wing);
               wing.buildComponent(this, list, rand);
               this.addOpening(x, y, z, rotation);
               return true;
            } else {
               return rand.nextInt(3) > 0 ? this.makeBridge(list, rand, index, x, y, z, wingSize, wingHeight, rotation) : false;
            }
         }
      }
   }

   protected boolean makeBridge(List list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, int rotation) {
      int direction = (this.getCoordBaseMode() + rotation) % 4;
      int[] dx = this.offsetTowerCoords(x, y, z, 3, direction);
      if (wingSize == 3 && wingHeight > 10) {
         wingHeight = 6 + rand.nextInt(5);
      }

      ComponentTFTowerBridge bridge = new ComponentTFTowerBridge(index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
      StructureComponent intersect = StructureComponent.findIntersecting(list, bridge.boundingBox);
      if (intersect != null && intersect != this) {
         return false;
      }

      intersect = StructureComponent.findIntersecting(list, bridge.getWingBB());
      if (intersect != null && intersect != this) {
         return false;
      }

      list.add(bridge);
      bridge.buildComponent(this, list, rand);
      this.addOpening(x, y, z, rotation);
      return true;
   }

   public void addOpening(int dx, int dy, int dz, int direction) {
      this.openingTowards[direction] = true;
      if (dy > this.highestOpening) {
         this.highestOpening = dy;
      }

      this.openings.add(new ChunkCoordinates(dx, dy, dz));
   }

   public void makeABeard(StructureComponent parent, List list, Random rand) {
      boolean attached = parent.getBoundingBox().minY < this.boundingBox.minY;
      int index = this.getComponentType();
      ComponentTFTowerBeard beard;
      if (attached) {
         beard = new ComponentTFTowerBeardAttached(index + 1, this);
      } else {
         beard = new ComponentTFTowerBeard(index + 1, this);
      }

      list.add(beard);
      beard.buildComponent(this, list, rand);
   }

   public void makeARoof(StructureComponent parent, List list, Random rand) {
      boolean attached = parent.getBoundingBox().maxY > this.boundingBox.maxY;
      if (attached) {
         this.makeAttachedRoof(list, rand);
      } else {
         this.makeFreestandingRoof(list, rand);
      }
   }

   protected void makeAttachedRoof(List list, Random rand) {
      int index = this.getComponentType();
      if (this.roofType == null && rand.nextInt(32) != 0) {
         this.tryToFitRoof(list, rand, new ComponentTFTowerRoofGableForwards(index + 1, this));
      }

      if (this.roofType == null && rand.nextInt(8) != 0) {
         this.tryToFitRoof(list, rand, new ComponentTFTowerRoofSlabForwards(index + 1, this));
      }

      if (this.roofType == null && rand.nextInt(32) != 0) {
         ComponentTFTowerRoof roof = new ComponentTFTowerRoofAttachedSlab(index + 1, this);
         this.tryToFitRoof(list, rand, roof);
      }

      if (this.roofType == null) {
         ComponentTFTowerRoof roof = new ComponentTFTowerRoofFence(index + 1, this);
         this.tryToFitRoof(list, rand, roof);
      }
   }

   protected void tryToFitRoof(List list, Random rand, ComponentTFTowerRoof roof) {
      if (roof.fits(this, list, rand)) {
         list.add(roof);
         roof.buildComponent(this, list, rand);
         this.roofType = roof.getClass();
      }
   }

   protected void makeFreestandingRoof(List list, Random rand) {
      int index = this.getComponentType();
      if (this.roofType == null && rand.nextInt(8) != 0) {
         ComponentTFTowerRoof roof = new ComponentTFTowerRoofPointyOverhang(index + 1, this);
         this.tryToFitRoof(list, rand, roof);
      }

      if (this.roofType == null) {
         ComponentTFTowerRoof roof = new ComponentTFTowerRoofStairsOverhang(index + 1, this);
         this.tryToFitRoof(list, rand, roof);
      }

      if (this.roofType == null) {
         ComponentTFTowerRoof roof = new ComponentTFTowerRoofStairs(index + 1, this);
         this.tryToFitRoof(list, rand, roof);
      }

      if (this.roofType == null && rand.nextInt(53) != 0) {
         ComponentTFTowerRoof roof = new ComponentTFTowerRoofSlab(index + 1, this);
         this.tryToFitRoof(list, rand, roof);
      }

      if (this.roofType == null) {
         ComponentTFTowerRoof roof = new ComponentTFTowerRoofFence(index + 1, this);
         this.tryToFitRoof(list, rand, roof);
      }
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithRandomizedBlocks(world, sbb, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1, false, rand, StructureTFComponent.getStrongholdStones());
      this.fillWithAir(world, sbb, 1, 1, 1, this.size - 2, this.height - 2, this.size - 2);
      this.nullifySkyLightForBoundingBox(world);
      if (this.highestOpening > 1) {
         this.makeStairs(world, rand, sbb);
      }

      this.decorateThisTower(world, rand, sbb);
      this.makeWindows(world, rand, sbb, this.size < 4);
      this.makeOpenings(world, sbb);
      return true;
   }

   protected void makeOpeningMarkers(OldWorld world, Random rand, int numMarkers, StructureBoundingBox sbb) {
      if (this.size > 4) {
         for (int i = 0; i < numMarkers; i++) {
            int[] spot = this.getValidOpening(rand, 0);
            this.placeBlockAtCurrentPosition(world, 35, 0, spot[0], spot[1], spot[2], sbb);
         }

         for (int i = 0; i < numMarkers; i++) {
            int[] spot = this.getValidOpening(rand, 1);
            this.placeBlockAtCurrentPosition(world, 35, 1, spot[0], spot[1], spot[2], sbb);
         }

         for (int i = 0; i < numMarkers; i++) {
            int[] spot = this.getValidOpening(rand, 2);
            this.placeBlockAtCurrentPosition(world, 35, 2, spot[0], spot[1], spot[2], sbb);
         }

         for (int i = 0; i < numMarkers; i++) {
            int[] spot = this.getValidOpening(rand, 3);
            this.placeBlockAtCurrentPosition(world, 35, 3, spot[0], spot[1], spot[2], sbb);
         }
      }
   }

   protected void decorateThisTower(OldWorld world, Random rand, StructureBoundingBox sbb) {
      Random decoRNG = new Random(world.seed() + this.boundingBox.minX * 321534781 * this.boundingBox.minZ * 756839);
      if (this.size > 3) {
         if (this.isDeadEnd()) {
            this.decorateDeadEnd(world, decoRNG, sbb);
         } else {
            this.decorateStairTower(world, decoRNG, sbb);
         }
      }
   }

   protected void decorateDeadEnd(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int floors = (this.height - 1) / 5;
      int floorHeight = this.height / floors;

      for (int i = 1; i < floors; i++) {
         for (int x = 1; x < this.size - 1; x++) {
            for (int z = 1; z < this.size - 1; z++) {
               this.placeBlockAtCurrentPosition(world, 5, 2, x, i * floorHeight, z, sbb);
            }
         }
      }

      if (floors > 1) {
         int ladderDir = 3;
         int downLadderDir = -1;
         this.decorateFloor(world, rand, 0, 1, floorHeight, ladderDir, -1, sbb);

         for (int i = 1; i < floors - 1; i++) {
            int bottom = 1 + floorHeight * i;
            int top = floorHeight * (i + 1);
            downLadderDir = ladderDir++;
            ladderDir %= 4;
            this.decorateFloor(world, rand, i, bottom, top, ladderDir, downLadderDir, sbb);
         }

         this.decorateFloor(world, rand, floors, 1 + floorHeight * (floors - 1), this.height - 1, -1, ladderDir, sbb);
      } else {
         this.decorateFloor(world, rand, 0, 1, this.height - 1, -1, -1, sbb);
      }
   }

   protected void decorateFloor(OldWorld world, Random rand, int floor, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      if (ladderUpDir > -1) {
         int meta = this.getLadderMeta(ladderUpDir);
         int dx = this.getLadderX(ladderUpDir);
         int dz = this.getLadderZ(ladderUpDir);

         for (int dy = bottom; dy < top; dy++) {
            this.placeBlockAtCurrentPosition(world, 65, meta, dx, dy, dz, sbb);
         }
      }

      if (ladderDownDir > -1) {
         int meta = this.getLadderMeta(ladderDownDir);
         int dx = this.getLadderX(ladderDownDir);
         int dz = this.getLadderZ(ladderDownDir);

         for (int dy = bottom - 1; dy < bottom + 2; dy++) {
            this.placeBlockAtCurrentPosition(world, 65, meta, dx, dy, dz, sbb);
         }
      }

      if (rand.nextInt(7) == 0 && ladderDownDir == -1) {
         this.decorateWell(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      } else if (rand.nextInt(7) == 0 && ladderDownDir == -1) {
         this.decorateSkeletonRoom(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      } else if (rand.nextInt(6) == 0 && ladderDownDir == -1) {
         this.decorateZombieRoom(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      } else if (rand.nextInt(5) == 0 && ladderDownDir == -1) {
         this.decorateCactusRoom(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      } else if (rand.nextInt(4) == 0 && ladderDownDir > -1) {
         this.decorateTreasureChest(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      } else if (rand.nextInt(5) == 0) {
         this.decorateSpiderWebs(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      } else if (rand.nextInt(12) == 0 && ladderDownDir > -1) {
         this.decorateSolidRock(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      } else if (rand.nextInt(3) == 0) {
         this.decorateFullLibrary(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      } else {
         this.decorateLibrary(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      }
   }

   protected void decorateWell(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      int cx = this.size / 2;
      int cz = cx;
      int cy = bottom;
      int waterOrLava = rand.nextInt(4) == 0 ? 11 : 9;
      if (this.size > 5) {
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, cy + 0, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, cx - 1, cy + 1, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 0, cy + 0, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, cy + 0, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, cx + 1, cy + 1, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, cy + 0, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, waterOrLava, 0, cx + 0, cy + 0, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, cy + 0, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, cy + 0, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, cx - 1, cy + 1, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 0, cy + 0, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, cy + 0, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, cx + 1, cy + 1, cz + 1, sbb);
      }

      this.placeBlockAtCurrentPosition(world, waterOrLava, 0, cx + 0, cy - 1, cz + 0, sbb);
   }

   protected void decorateSkeletonRoom(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      this.placeSpawnerAtCurrentPosition(world, rand, this.size / 2, bottom + 2, this.size / 2, "Skeleton", sbb);
      ArrayList chainList = new ArrayList();
      chainList.add(new ChunkCoordinates(this.size / 2, bottom + 2, this.size / 2));

      for (int i = 0; i < this.size + 2; i++) {
         ChunkCoordinates chain = new ChunkCoordinates(2 + rand.nextInt(this.size - 4), this.height - 2, 2 + rand.nextInt(this.size - 4));
         if (!this.chainCollides(chain, chainList)) {
            for (int dy = bottom; dy < top; dy++) {
               this.placeBlockAtCurrentPosition(world, 101, 0, chain.posX, dy, chain.posZ, sbb);
            }

            chainList.add(chain);
         }
      }

      for (int dx = 1; dx <= this.size - 2; dx++) {
         for (int dz = 1; dz <= this.size - 2; dz++) {
            if ((dx == 1 || dx == this.size - 2 || dz == 1 || dz == this.size - 2)
               && !this.isWindowPos(dx, dz)
               && !this.isLadderPos(dx, dz, ladderUpDir, ladderDownDir)) {
               this.placeBlockAtCurrentPosition(world, 30, 0, dx, top - 1, dz, sbb);
            }
         }
      }
   }

   protected void decorateZombieRoom(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      this.placeSpawnerAtCurrentPosition(world, rand, this.size / 2, bottom + 2, this.size / 2, "Zombie", sbb);

      for (int dx = 1; dx <= this.size - 2; dx++) {
         for (int dz = 1; dz <= this.size - 2; dz++) {
            if (!this.isWindowPos(dx, dz) && !this.isLadderPos(dx, dz, ladderUpDir, ladderDownDir) && rand.nextInt(5) == 0) {
               this.placeBlockAtCurrentPosition(world, 39, 0, dx, bottom, dz, sbb);
            }
         }
      }

      ArrayList slabList = new ArrayList();
      slabList.add(new ChunkCoordinates(this.size / 2, bottom + 2, this.size / 2));

      for (int i = 0; i < this.size - 1; i++) {
         ChunkCoordinates slab = new ChunkCoordinates(2 + rand.nextInt(this.size - 4), this.height - 2, 2 + rand.nextInt(this.size - 4));
         if (!this.chainCollides(slab, slabList)) {
            this.placeBlockAtCurrentPosition(world, 101, 0, slab.posX, bottom + 0, slab.posZ, sbb);
            this.placeBlockAtCurrentPosition(world, 126, 2, slab.posX, bottom + 1, slab.posZ, sbb);
            this.placeBlockAtCurrentPosition(world, 88, 0, slab.posX, bottom + 2, slab.posZ, sbb);
            slabList.add(slab);
         }
      }
   }

   protected void decorateCactusRoom(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      for (int dx = 1; dx <= this.size - 2; dx++) {
         for (int dz = 1; dz <= this.size - 2; dz++) {
            this.placeBlockAtCurrentPosition(world, 12, 0, dx, bottom - 1, dz, sbb);
            if (!this.isWindowPos(dx, dz) && !this.isLadderPos(dx, dz, ladderUpDir, ladderDownDir) && rand.nextInt(4) == 0) {
               this.placeBlockAtCurrentPosition(world, 32, 0, dx, bottom, dz, sbb);
            }
         }
      }

      ArrayList cactusList = new ArrayList();
      cactusList.add(new ChunkCoordinates(this.size / 2, bottom + 2, this.size / 2));

      for (int i = 0; i < this.size + 12; i++) {
         ChunkCoordinates cactus = new ChunkCoordinates(2 + rand.nextInt(this.size - 4), this.height - 2, 2 + rand.nextInt(this.size - 4));
         if (!this.chainCollides(cactus, cactusList)) {
            for (int dy = bottom; dy < top; dy++) {
               this.placeBlockAtCurrentPosition(world, 81, 0, cactus.posX, dy, cactus.posZ, sbb);
            }

            cactusList.add(cactus);
         }
      }
   }

   protected void decorateTreasureChest(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      int cx = this.size / 2;
      int cz = cx;
      this.placeBlockAtCurrentPosition(world, 109, this.getStairMeta(1), cx + 0, bottom, cz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 109, this.getStairMeta(0), cx - 1, bottom, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 109, this.getStairMeta(2), cx + 1, bottom, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 109, this.getStairMeta(3), cx + 0, bottom, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 0, cx + 0, bottom, cz + 0, sbb);
      if (this.size > 5) {
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, bottom, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, bottom, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, bottom, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, bottom, cz + 1, sbb);
      }

      this.placeBlockAtCurrentPosition(world, 109, this.getStairMeta(1) + 4, cx + 0, top - 1, cz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 109, this.getStairMeta(0) + 4, cx - 1, top - 1, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 109, this.getStairMeta(2) + 4, cx + 1, top - 1, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 109, this.getStairMeta(3) + 4, cx + 0, top - 1, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 98, 0, cx + 0, top - 1, cz + 0, sbb);
      if (this.size > 5) {
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, top - 1, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, top - 1, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, top - 1, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, top - 1, cz + 1, sbb);
      }

      if (this.size > 5) {
         for (int cy = bottom + 1; cy < top - 1; cy++) {
            this.placeBlockAtCurrentPosition(world, 98, 5, cx - 1, cy, cz - 1, sbb);
            this.placeBlockAtCurrentPosition(world, 98, 5, cx + 1, cy, cz - 1, sbb);
            this.placeBlockAtCurrentPosition(world, 98, 5, cx - 1, cy, cz + 1, sbb);
            this.placeBlockAtCurrentPosition(world, 98, 5, cx + 1, cy, cz + 1, sbb);
         }
      }

      this.placeTreasureAtCurrentPosition(world, rand, cx + 0, bottom + 1, cz + 0, TFTreasure.TOWER_ROOM, sbb);
      int i = 0;

      while (i < 4) {
         i++;
      }
   }

   protected void decorateSpiderWebs(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      for (int dy = bottom; dy < top; dy++) {
         int chance = top - dy + 2;

         for (int dx = 1; dx <= this.size - 2; dx++) {
            for (int dz = 1; dz <= this.size - 2; dz++) {
               if (!this.isLadderPos(dx, dz, ladderUpDir, ladderDownDir) && rand.nextInt(chance) == 0) {
                  this.placeBlockAtCurrentPosition(world, 30, 0, dx, dy, dz, sbb);
               }
            }
         }
      }

      if (rand.nextInt(5) == 0) {
         String spiderName;
         switch (rand.nextInt(4)) {
            case 0:
            default:
               spiderName = "Spider";
               break;
            case 1:
               spiderName = "TwilightForest.Hedge Spider";
               break;
            case 2:
               spiderName = "TwilightForest.Swarm Spider";
               break;
            case 3:
               spiderName = "CaveSpider";
         }

         this.placeSpawnerAtCurrentPosition(world, rand, this.size / 2, bottom + 2, this.size / 2, spiderName, sbb);
      } else {
         this.decorateFurniture(world, rand, bottom, this.size - 2, sbb);
      }
   }

   protected void decorateFurniture(OldWorld world, Random rand, int bottom, int freeSpace, StructureBoundingBox sbb) {
      if (rand.nextInt(3) > 0) {
         this.placeBlockAtCurrentPosition(world, 85, 0, this.size / 2, bottom, this.size / 2, sbb);
         this.placeBlockAtCurrentPosition(world, 72, 0, this.size / 2, bottom + 1, this.size / 2, sbb);
      }

      if (rand.nextInt(3) == 0 && freeSpace > 1) {
         this.placeBlockAtCurrentPosition(world, 134, this.getStairMeta(0), this.size / 2 + 1, bottom, this.size / 2, sbb);
      }

      if (rand.nextInt(3) == 0 && freeSpace > 1) {
         this.placeBlockAtCurrentPosition(world, 134, this.getStairMeta(1), this.size / 2, bottom, this.size / 2 + 1, sbb);
      }

      if (rand.nextInt(3) == 0 && freeSpace > 1) {
         this.placeBlockAtCurrentPosition(world, 134, this.getStairMeta(2), this.size / 2 - 1, bottom, this.size / 2, sbb);
      }

      if (rand.nextInt(3) == 0 && freeSpace > 1) {
         this.placeBlockAtCurrentPosition(world, 134, this.getStairMeta(3), this.size / 2, bottom, this.size / 2 - 1, sbb);
      }
   }

   protected void decorateSolidRock(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      for (int dy = bottom; dy < top; dy++) {
         for (int dx = 1; dx <= this.size - 2; dx++) {
            for (int dz = 1; dz <= this.size - 2; dz++) {
               if (!this.isLadderPos(dx, dz, ladderUpDir, ladderDownDir) && rand.nextInt(9) != 0) {
                  this.placeBlockAtCurrentPosition(world, 1, 0, dx, dy, dz, sbb);
               }
            }
         }
      }
   }

   protected void decorateLibrary(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      for (int dx = 1; dx <= this.size - 2; dx++) {
         for (int dz = 1; dz <= this.size - 2; dz++) {
            for (int dy = bottom; dy < top - 1; dy++) {
               if ((dx == 1 || dx == this.size - 2 || dz == 1 || dz == this.size - 2)
                  && !this.isWindowPos(dx, dz)
                  && !this.isLadderPos(dx, dz, ladderUpDir, ladderDownDir)) {
                  this.placeBlockAtCurrentPosition(world, 47, 0, dx, dy, dz, sbb);
               }
            }
         }
      }

      if (rand.nextInt(2) == 0 && this.size > 5) {
         this.decorateLibraryTreasure(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      }

      if (rand.nextInt(2) == 0 && this.size > 5) {
         this.decorateFurniture(world, rand, bottom, this.size - 2, sbb);
      }
   }

   protected void decorateLibraryTreasure(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      switch (rand.nextInt(4)) {
         case 0:
         default:
            if (!this.isLadderPos(2, 1, ladderUpDir, ladderDownDir)) {
               this.placeTreasureAtCurrentPosition(world, rand, 2, top - 2, 1, TFTreasure.TOWER_LIBRARY, sbb);
               break;
            }
         case 1:
            if (!this.isLadderPos(this.size - 2, 2, ladderUpDir, ladderDownDir)) {
               this.placeTreasureAtCurrentPosition(world, rand, this.size - 2, top - 2, 2, TFTreasure.TOWER_LIBRARY, sbb);
               break;
            }
         case 2:
            if (!this.isLadderPos(this.size - 3, this.size - 2, ladderUpDir, ladderDownDir)) {
               this.placeTreasureAtCurrentPosition(world, rand, this.size - 3, top - 2, this.size - 2, TFTreasure.TOWER_LIBRARY, sbb);
               break;
            }
         case 3:
            if (!this.isLadderPos(1, this.size - 3, ladderUpDir, ladderDownDir)) {
               this.placeTreasureAtCurrentPosition(world, rand, 1, top - 2, this.size - 3, TFTreasure.TOWER_LIBRARY, sbb);
            }
      }
   }

   protected void decorateFullLibrary(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      for (int dx = 1; dx <= this.size - 2; dx++) {
         for (int dz = 1; dz <= this.size - 2; dz++) {
            for (int dy = bottom; dy < top; dy++) {
               if ((
                     dx % 2 != 0 && (dz >= dx && dz <= this.size - dx - 1 || dz >= this.size - dx - 1 && dz <= dx)
                        || dz % 2 != 0 && (dx >= dz && dx <= this.size - dz - 1 || dx >= this.size - dz - 1 && dx <= dz)
                  )
                  && !this.isWindowPos(dx, dy, dz)
                  && !this.isOpeningPos(dx, dy, dz)
                  && !this.isLadderPos(dx, dz, ladderUpDir, ladderDownDir)) {
                  this.placeBlockAtCurrentPosition(world, 47, 0, dx, dy, dz, sbb);
               }
            }
         }
      }

      if (rand.nextInt(2) == 0 && this.size > 5) {
         this.decorateLibraryTreasure(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
      }
   }

   protected void decorateTrap(OldWorld world, Random rand, int bottom, int top, int ladderUpDir, int ladderDownDir, StructureBoundingBox sbb) {
      for (int dx = 2; dx <= this.size - 3; dx++) {
         for (int dz = 2; dz <= this.size - 3; dz++) {
            if (dx == 2 || dx == this.size - 3 || dz == 2 || dz == this.size - 3) {
               this.placeBlockAtCurrentPosition(world, 46, 0, dx, -1, dz, sbb);
            }
         }
      }

      for (int dy = bottom - 2; dy < top - 2; dy++) {
         this.placeBlockAtCurrentPosition(world, 46, 0, 1, dy, 1, sbb);
         this.placeBlockAtCurrentPosition(world, 46, 0, 1, dy, this.size - 2, sbb);
         this.placeBlockAtCurrentPosition(world, 46, 0, this.size - 2, dy, 1, sbb);
         this.placeBlockAtCurrentPosition(world, 46, 0, this.size - 2, dy, this.size - 2, sbb);
      }
   }

   protected boolean isWindowPos(int x, int z) {
      if (x == 1 && z == this.size / 2) {
         return true;
      } else if (x == this.size - 2 && z == this.size / 2) {
         return true;
      } else {
         return x == this.size / 2 && z == 1 ? true : x == this.size / 2 && z == this.size - 2;
      }
   }

   protected boolean isWindowPos(int x, int y, int z) {
      int checkYDir = -1;
      if (x == 1 && z == this.size / 2) {
         checkYDir = 2;
      } else if (x == this.size - 2 && z == this.size / 2) {
         checkYDir = 0;
      } else if (x == this.size / 2 && z == 1) {
         checkYDir = 3;
      } else if (x == this.size / 2 && z == this.size - 2) {
         checkYDir = 1;
      }

      return checkYDir <= -1
         ? false
         : !this.openingTowards[checkYDir] && (y == 2 || y == 3 || this.height > 8 && (y == this.height - 3 || y == this.height - 4));
   }

   protected boolean isOpeningPos(int x, int y, int z) {
      for (ChunkCoordinates door : this.openings) {
         ChunkCoordinates inside = new ChunkCoordinates(door);
         if (inside.posX == 0) {
            inside.posX++;
         } else if (inside.posX == this.size - 1) {
            inside.posX--;
         } else if (inside.posZ == 0) {
            inside.posZ++;
         } else if (inside.posZ == this.size - 1) {
            inside.posZ--;
         }

         if (inside.posX == x && inside.posZ == z && (inside.posY == y || inside.posY + 1 == y)) {
            return true;
         }
      }

      return false;
   }

   protected boolean isLadderPos(int x, int z, int ladderUpDir, int ladderDownDir) {
      return x == this.getLadderX(ladderUpDir) && z == this.getLadderZ(ladderUpDir)
         ? true
         : x == this.getLadderX(ladderDownDir) && z == this.getLadderZ(ladderDownDir);
   }

   protected int getLadderX(int ladderDir) {
      switch (ladderDir) {
         case 0:
            return this.size - 2;
         case 1:
            return this.size / 2 + 1;
         case 2:
            return 1;
         case 3:
            return this.size / 2 - 1;
         default:
            return this.size / 2;
      }
   }

   protected int getLadderZ(int ladderDir) {
      switch (ladderDir) {
         case 0:
            return this.size / 2 - 1;
         case 1:
            return this.size - 2;
         case 2:
            return this.size / 2 + 1;
         case 3:
            return 1;
         default:
            return this.size / 2;
      }
   }

   protected void decorateStairTower(OldWorld world, Random rand, StructureBoundingBox sbb) {
      if (this.height - this.highestOpening > 8) {
         int base = this.highestOpening + 3;
         int floors = (this.height - base) / 5;
         int floorHeight = (this.height - base) / floors;

         for (int i = 0; i < floors; i++) {
            for (int x = 1; x < this.size - 1; x++) {
               for (int z = 1; z < this.size - 1; z++) {
                  this.placeBlockAtCurrentPosition(world, 5, 2, x, i * floorHeight + base, z, sbb);
               }
            }
         }

         int ladderDir = 3;
         int downLadderDir = -1;
         int meta = this.getLadderMeta(ladderDir);
         int dx = this.getLadderX(ladderDir);
         int dz = this.getLadderZ(ladderDir);

         for (int dy = 1; dy < 3; dy++) {
            this.placeBlockAtCurrentPosition(world, 65, meta, dx, base - dy, dz, sbb);
         }

         for (int i = 0; i < floors - 1; i++) {
            int bottom = base + 1 + floorHeight * i;
            int top = base + floorHeight * (i + 1);
            downLadderDir = ladderDir++;
            ladderDir %= 4;
            this.decorateFloor(world, rand, i, bottom, top, ladderDir, downLadderDir, sbb);
         }

         this.decorateFloor(world, rand, floors, base + 1 + floorHeight * (floors - 1), this.height - 1, -1, ladderDir, sbb);
         if (base > 8) {
            switch (rand.nextInt(4)) {
               case 0:
                  this.decorateChandelier(world, rand, base + 1, sbb);
                  break;
               case 1:
                  this.decorateHangingChains(world, rand, base + 1, sbb);
                  break;
               case 2:
                  this.decorateFloatingBooks(world, rand, base + 1, sbb);
                  break;
               case 3:
                  this.decorateFloatingVines(world, rand, base + 1, sbb);
            }
         }
      } else if (this.size > 5) {
         switch (rand.nextInt(4)) {
            case 0:
               this.decorateChandelier(world, rand, this.height, sbb);
               break;
            case 1:
               this.decorateHangingChains(world, rand, this.height, sbb);
               break;
            case 2:
               this.decorateFloatingBooks(world, rand, this.height, sbb);
               break;
            case 3:
               this.decorateFloatingVines(world, rand, this.height, sbb);
         }
      } else if (this.size > 3) {
         switch (rand.nextInt(3)) {
            case 0:
               this.decorateHangingChains(world, rand, this.height, sbb);
               break;
            case 1:
               this.decorateFloatingBooks(world, rand, this.height, sbb);
               break;
            case 2:
               this.decorateFloatingVines(world, rand, this.height, sbb);
         }
      }

      this.decorateStairFloor(world, rand, sbb);
   }

   protected void decorateStairFloor(OldWorld world, Random rand, StructureBoundingBox sbb) {
      if (this.size > 5) {
         if (rand.nextInt(3) == 0) {
            this.decorateStairWell(world, rand, sbb);
         } else if (rand.nextInt(3) > 0 || this.size >= 15) {
            this.decoratePlanter(world, rand, sbb);
         }
      }
   }

   protected void decorateChandelier(OldWorld world, Random rand, int decoTop, StructureBoundingBox sbb) {
      if (decoTop >= 8 && this.size >= 8) {
         int cx = this.size / 2;
         int cy = decoTop - rand.nextInt(decoTop - 7) - 2;
         int cz = this.size / 2;
         this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy + 0, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 85, 0, cx - 1, cy + 0, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 85, 0, cx + 1, cy + 0, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy + 0, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy + 0, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, cy + 1, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 50, 0, cx - 1, cy + 1, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 50, 0, cx + 1, cy + 1, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 50, 0, cx + 0, cy + 1, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 50, 0, cx + 0, cy + 1, cz + 1, sbb);

         for (int y = cy; y < decoTop - 1; y++) {
            this.placeBlockAtCurrentPosition(world, 85, 0, cx + 0, y, cz + 0, sbb);
         }
      }
   }

   protected void decorateHangingChains(OldWorld world, Random rand, int decoTop, StructureBoundingBox sbb) {
      ArrayList chainList = new ArrayList();

      for (int i = 0; i < this.size + 2; i++) {
         int filled = this.size < 15 ? 2 : 4;
         ChunkCoordinates chain = new ChunkCoordinates(
            filled + rand.nextInt(this.size - filled * 2), decoTop - 2, filled + rand.nextInt(this.size - filled * 2)
         );
         if (!this.chainCollides(chain, chainList)) {
            int length = 1 + rand.nextInt(decoTop - 7);
            this.decorateOneChain(world, rand, chain.posX, decoTop, length, chain.posZ, sbb);
            chainList.add(chain);
         }
      }
   }

   protected boolean chainCollides(ChunkCoordinates coords, List<ChunkCoordinates> list) {
      for (ChunkCoordinates existing : list) {
         if (coords.posZ == existing.posZ && Math.abs(coords.posX - existing.posX) <= 1) {
            return true;
         }

         if (coords.posX == existing.posX && Math.abs(coords.posZ - existing.posZ) <= 1) {
            return true;
         }
      }

      return false;
   }

   protected void decorateOneChain(OldWorld world, Random rand, int dx, int decoTop, int length, int dz, StructureBoundingBox sbb) {
      for (int y = 1; y <= length; y++) {
         this.placeBlockAtCurrentPosition(world, 101, 0, dx, decoTop - y - 1, dz, sbb);
      }

      int ballMeta;
      int ballBlock;
      switch (rand.nextInt(10)) {
         case 0:
            ballBlock = 42;
            ballMeta = 0;
            break;
         case 1:
            ballBlock = 47;
            ballMeta = 0;
            break;
         case 2:
            ballBlock = 87;
            ballMeta = 0;
            break;
         case 3:
            ballBlock = 88;
            ballMeta = 0;
            break;
         case 4:
            ballBlock = 20;
            ballMeta = 0;
            break;
         case 5:
            ballBlock = 22;
            ballMeta = 0;
            break;
         case 6:
            ballBlock = 97;
            ballMeta = 2;
            break;
         case 7:
         default:
            ballBlock = 89;
            ballMeta = 0;
      }

      this.placeBlockAtCurrentPosition(world, ballBlock, ballMeta, dx, decoTop - length - 2, dz, sbb);
   }

   protected void decorateFloatingBooks(OldWorld world, Random rand, int decoTop, StructureBoundingBox sbb) {
      ArrayList shelfList = new ArrayList();

      for (int i = 0; i < this.size + 2; i++) {
         int filled = this.size < 15 ? 2 : 4;
         ChunkCoordinates shelf = new ChunkCoordinates(
            filled + rand.nextInt(this.size - filled * 2), decoTop - 2, filled + rand.nextInt(this.size - filled * 2)
         );
         if (!this.chainCollides(shelf, shelfList)) {
            int bottom = 2 + rand.nextInt(decoTop - 7);
            int top = rand.nextInt(bottom - 1) + 2;

            for (int y = top; y <= bottom; y++) {
               this.placeBlockAtCurrentPosition(world, 47, 0, shelf.posX, decoTop - y, shelf.posZ, sbb);
            }

            shelfList.add(shelf);
         }
      }
   }

   protected void decorateFloatingVines(OldWorld world, Random rand, int decoTop, StructureBoundingBox sbb) {
      ArrayList mossList = new ArrayList();

      for (int i = 0; i < this.size + 2; i++) {
         int filled = this.size < 15 ? 2 : 4;
         ChunkCoordinates moss = new ChunkCoordinates(filled + rand.nextInt(this.size - filled * 2), decoTop - 2, filled + rand.nextInt(this.size - filled * 2));
         if (!this.chainCollides(moss, mossList)) {
            int bottom = 2 + rand.nextInt(decoTop - 7);
            int top = rand.nextInt(bottom - 1) + 2;

            for (int y = top; y <= bottom; y++) {
               this.placeBlockAtCurrentPosition(world, 48, 0, moss.posX, decoTop - y, moss.posZ, sbb);
               this.placeBlockAtCurrentPosition(
                  world, 106, this.getVineMeta(2), moss.posX + 1, decoTop - y, moss.posZ + 0, sbb
               );
               this.placeBlockAtCurrentPosition(
                  world, 106, this.getVineMeta(0), moss.posX - 1, decoTop - y, moss.posZ + 0, sbb
               );
               this.placeBlockAtCurrentPosition(
                  world, 106, this.getVineMeta(3), moss.posX + 0, decoTop - y, moss.posZ + 1, sbb
               );
               this.placeBlockAtCurrentPosition(
                  world, 106, this.getVineMeta(1), moss.posX + 0, decoTop - y, moss.posZ - 1, sbb
               );
            }

            mossList.add(moss);
         }
      }

      for (int y = this.highestOpening + 3; y < decoTop - 1; y++) {
         for (int x = 1; x < this.size - 1; x++) {
            if (rand.nextInt(3) == 0) {
               this.placeBlockAtCurrentPosition(world, 106, this.getVineMeta(3), x, y, 1, sbb);
            }

            if (rand.nextInt(3) == 0) {
               this.placeBlockAtCurrentPosition(world, 106, this.getVineMeta(1), x, y, this.size - 2, sbb);
            }
         }

         for (int z = 1; z < this.size - 1; z++) {
            if (rand.nextInt(3) == 0) {
               this.placeBlockAtCurrentPosition(world, 106, this.getVineMeta(2), 1, y, z, sbb);
            }

            if (rand.nextInt(3) == 0) {
               this.placeBlockAtCurrentPosition(world, 106, this.getVineMeta(0), this.size - 2, y, z, sbb);
            }
         }
      }
   }

   protected int getVineMeta(int vineDir) {
      switch ((this.getCoordBaseMode() + vineDir) % 4) {
         case 0:
            return 8;
         case 1:
            return 1;
         case 2:
            return 2;
         case 3:
            return 4;
         default:
            return -1;
      }
   }

   protected void decoratePlanter(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int cx = this.size / 2;
      int cz = cx;
      this.placeBlockAtCurrentPosition(world, 44, 0, cx + 0, 1, cz + 1, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, cx + 0, 1, cz - 1, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, cx + 1, 1, cz + 0, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, cx - 1, 1, cz + 0, sbb);
      if (this.size > 7) {
         this.placeBlockAtCurrentPosition(world, 43, 0, cx - 1, 1, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 43, 0, cx + 1, 1, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 43, 0, cx + 1, 1, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 43, 0, cx - 1, 1, cz + 1, sbb);
      }

      this.placeBlockAtCurrentPosition(world, 2, 0, cx + 0, 1, cz + 0, sbb);
      int planterBlock;
      int planterMeta;
      switch (rand.nextInt(6)) {
         case 0:
            planterBlock = 6;
            planterMeta = 0;
            break;
         case 1:
            planterBlock = 6;
            planterMeta = 1;
            break;
         case 2:
            planterBlock = 6;
            planterMeta = 2;
            break;
         case 3:
            planterBlock = 6;
            planterMeta = 3;
            break;
         case 4:
            planterBlock = 39;
            planterMeta = 0;
            break;
         case 5:
         default:
            planterBlock = 40;
            planterMeta = 0;
      }

      this.placeBlockAtCurrentPosition(world, planterBlock, planterMeta, cx + 0, 2, cz + 0, sbb);
      if (planterBlock == 6) {
         int wx = this.getXWithOffset(cx, cz);
         int wy = this.getYWithOffset(2);
         int wz = this.getZWithOffset(cx, cz);
         growTree(world, wx, wy, wz, worldRand(world, wx, wy, wz));
      }

      if (planterBlock == 39 || planterBlock == 40) {
         int wx = this.getXWithOffset(cx, cz);
         int wy = this.getYWithOffset(2);
         int wz = this.getZWithOffset(cx, cz);
         fertilizeMushroom(world, planterBlock, wx, wy, wz, worldRand(world, wx, wy, wz));
      }

      int whatHappened = this.getBlockIdAtCurrentPosition(world, cx + 0, 2, cz + 0, sbb);
      if (whatHappened == planterBlock || whatHappened == 0) {
         int potMeta = planterBlock == 6 ? 3 + (planterMeta & 3) : planterBlock == 40 ? 7 : 8; // BlockFlowerPot.getMetaForPlant
         this.placeBlockAtCurrentPosition(world, 140, potMeta, cx + 0, 2, cz + 0, sbb);
      }
   }

   protected void decorateStairWell(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int cx = this.size / 2;
      int cz = cx;
      int cy = 1;
      int waterOrLava = rand.nextInt(4) == 0 ? 11 : 9;
      if (this.size > 7) {
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, cy + 0, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, cx - 1, cy + 1, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 0, cy + 0, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, cy + 0, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, cx + 1, cy + 1, cz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, cy + 0, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, waterOrLava, 0, cx + 0, cy + 0, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, cy + 0, cz + 0, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx - 1, cy + 0, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, cx - 1, cy + 1, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 0, cy + 0, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 98, 0, cx + 1, cy + 0, cz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 44, 5, cx + 1, cy + 1, cz + 1, sbb);
      }

      this.placeBlockAtCurrentPosition(world, waterOrLava, 0, cx + 0, cy - 1, cz + 0, sbb);
   }

   public boolean isDeadEnd() {
      return this.openings.size() == 1;
   }

   public boolean hasExitsOnAllWalls() {
      int exits = 0;

      for (int i = 0; i < 4; i++) {
         exits += this.openingTowards[i] ? 1 : 0;
      }

      return exits == 4;
   }

   public boolean hasStairs() {
      return this.highestOpening > 1;
   }

   protected void makeOpenings(OldWorld world, StructureBoundingBox sbb) {
      for (ChunkCoordinates door : this.openings) {
         this.makeDoorOpening(world, door.posX, door.posY, door.posZ, sbb);
      }
   }

   protected void makeDoorOpening(OldWorld world, int dx, int dy, int dz, StructureBoundingBox sbb) {
      this.placeBlockAtCurrentPosition(world, 0, 0, dx, dy + 0, dz, sbb);
      this.placeBlockAtCurrentPosition(world, 0, 0, dx, dy + 1, dz, sbb);
      if (this.getBlockIdAtCurrentPosition(world, dx, dy + 2, dz, sbb) != 0) {
         this.placeBlockAtCurrentPosition(world, 43, 0, dx, dy + 2, dz, sbb);
      }

      if (dx == 0) {
         this.updateLight(world, dx - 1, dy + 0, dz);
         this.updateLight(world, dx - 1, dy + 1, dz);
      }

      if (dx == this.size - 1) {
         this.updateLight(world, dx + 1, dy + 0, dz);
         this.updateLight(world, dx + 1, dy + 1, dz);
      }

      if (dz == 0) {
         this.updateLight(world, dx, dy + 0, dz - 1);
         this.updateLight(world, dx, dy + 1, dz - 1);
      }

      if (dz == this.size - 1) {
         this.updateLight(world, dx, dy + 0, dz + 1);
         this.updateLight(world, dx, dy + 1, dz + 1);
      }
   }

   public void updateLight(OldWorld world, int dx, int dy, int dz) {
      // World.updateAllLightTypes: the core keeps no light values
   }

   /**
    * world.rand of 1.5.2, which the planters let trees and mushrooms grow with: not seed-fixed in the original, here a
    * random of the world seed and the position.
    */
   static Random worldRand(OldWorld world, int x, int y, int z) {
      return new Random(world.seed() + x * 341873128712L + z * 132897987541L + y);
   }

   /** BlockSapling.growTree of a single sapling (a jungle sapling never has three more beside it here). */
   static void growTree(OldWorld world, int x, int y, int z, Random random) {
      int l = world.getBlockMetadata(x, y, z) & 3;
      hexxit.worldgen152.core.populate.OldGen gen;
      if (l == 1) {
         gen = new hexxit.worldgen152.core.populate.OldTreeGens.Spruce();
      } else if (l == 2) {
         gen = new hexxit.worldgen152.core.populate.OldTreeGens.Birch();
      } else if (l == 3) {
         gen = new hexxit.worldgen152.core.populate.OldTreeGens.Trees(4 + random.nextInt(7), 3, 3, false);
      } else {
         gen = new hexxit.worldgen152.core.populate.OldTreeGens.Trees();
         if (random.nextInt(10) == 0) {
            gen = new hexxit.worldgen152.core.populate.OldTreeGens.BigTree();
         }
      }
      world.setBlock(x, y, z, 0, 0);
      if (!gen.generate(world, random, x, y, z)) {
         world.setBlock(x, y, z, 6, l);
      }
   }

   /** BlockMushroom.fertilizeMushroom. */
   static void fertilizeMushroom(OldWorld world, int blockId, int x, int y, int z, Random random) {
      int l = world.getBlockMetadata(x, y, z);
      world.setBlockToAir(x, y, z);
      if (!new hexxit.worldgen152.core.populate.OldTreeGens.BigMushroom(blockId == 39 ? 0 : 1).generate(world, random, x, y, z)) {
         world.setBlock(x, y, z, blockId, l);
      }
   }

   public int[] getValidOpening(Random rand, int direction) {
      int wLength = this.size - 2;
      int offset = 1;
      if (this.size == 15) {
         wLength = 11;
         offset = 2;
      }

      if (direction != 0 && direction != 2) {
         if (direction != 1 && direction != 3) {
            return new int[]{0, 0, 0};
         }

         int rx = offset + rand.nextInt(wLength);
         int rz = direction == 1 ? this.size - 1 : 0;
         int ry = this.getYByStairs(rx, rand, direction);
         return new int[]{rx, ry, rz};
      } else {
         int rx = direction == 0 ? this.size - 1 : 0;
         int rz = offset + rand.nextInt(wLength);
         int ry = this.getYByStairs(rz, rand, direction);
         return new int[]{rx, ry, rz};
      }
   }

   protected int getYByStairs(int rx, Random rand, int direction) {
      int rise = 1;
      int base = 0;
      if (this.size == 15) {
         rise = 10;
         base = direction != 0 && direction != 2 ? 28 : 23;
      }

      if (this.size == 9) {
         rise = 6;
         base = direction != 0 && direction != 2 ? 5 : 2;
      }

      if (this.size == 7) {
         rise = 4;
         base = direction != 0 && direction != 2 ? 4 : 2;
      }

      if (this.size == 5) {
         rise = 4;
         switch (direction) {
            case 0:
               base = 3;
               break;
            case 1:
               base = 2;
               break;
            case 2:
               base = 5;
               break;
            case 3:
               base = 4;
         }
      }

      int flights = (this.height - 6 - base) / rise + 1;
      if (base > 0 && flights > 0) {
         int flightChosen = rand.nextInt(flights);
         int dy = flightChosen * rise + base;
         if (this.size == 15) {
            dy -= direction != 0 && direction != 3 ? (this.size - rx - 3) / 2 : (rx - 2) / 2;
         } else {
            dy -= direction != 0 && direction != 3 ? (this.size - rx - 2) / 2 : (rx - 1) / 2;
         }

         if (dy < 1) {
            dy = 1;
         }

         return dy;
      } else {
         return 0;
      }
   }

   protected void makeWindows(OldWorld world, Random rand, StructureBoundingBox sbb, boolean real) {
      for (int i = 0; i < 4; i++) {
         boolean realWindows = real && !this.openingTowards[i];
         this.makeWindowBlock(world, this.size - 1, 2, this.size / 2, i, sbb, realWindows);
         this.makeWindowBlock(world, this.size - 1, 3, this.size / 2, i, sbb, realWindows);
         this.makeWindowBase(world, this.size - 1, 1, this.size / 2, i, sbb);
         if (this.height > 8) {
            this.makeWindowBlock(world, this.size - 1, this.height - 3, this.size / 2, i, sbb, realWindows);
            this.makeWindowBlock(world, this.size - 1, this.height - 4, this.size / 2, i, sbb, realWindows);
            this.makeWindowBase(world, this.size - 1, this.height - 5, this.size / 2, i, sbb);
         }
      }
   }

   protected void makeWindowBlock(OldWorld world, int x, int y, int z, int rotation, StructureBoundingBox sbb, boolean realWindows) {
      int temp = this.getCoordBaseMode();
      this.setCoordBaseMode((this.getCoordBaseMode() + rotation) % 4);
      int outside = this.getBlockIdAtCurrentPosition(world, x + 1, y, z, sbb);
      int inside = this.getBlockIdAtCurrentPosition(world, x - 1, y, z, sbb);
      if (realWindows && inside == 0 && outside == 0) {
         this.placeBlockAtCurrentPosition(world, 102, 0, x, y, z, sbb);
      } else {
         this.placeBlockAtCurrentPosition(world, 4, 0, x, y, z, sbb);
      }

      this.setCoordBaseMode(temp);
   }

   protected void makeWindowBase(OldWorld world, int x, int y, int z, int rotation, StructureBoundingBox sbb) {
      int temp = this.getCoordBaseMode();
      this.setCoordBaseMode((this.getCoordBaseMode() + rotation) % 4);
      this.placeBlockAtCurrentPosition(world, 43, 0, x, y, z, sbb);
      this.setCoordBaseMode(temp);
   }

   protected boolean makeStairs(OldWorld world, Random rand, StructureBoundingBox sbb) {
      if (this.size == 15) {
         return this.makeStairs15(world, rand, sbb);
      } else if (this.size == 9) {
         return this.makeStairs9(world, rand, sbb);
      } else if (this.size == 7) {
         return this.makeStairs7(world, rand, sbb);
      } else {
         return this.size == 5 ? this.makeStairs5(world, rand, sbb) : false;
      }
   }

   protected boolean makeStairs5(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int rise = 1;
      int numFlights = this.highestOpening / rise;

      for (int i = 0; i < numFlights; i++) {
         this.makeStairs5flight(world, rand, sbb, i * rise, 0 + i * 3, 2);
      }

      return true;
   }

   protected void makeStairs5flight(OldWorld world, Random rand, StructureBoundingBox sbb, int height, int rotation, int meta) {
      int temp = this.getCoordBaseMode();
      this.setCoordBaseMode((this.getCoordBaseMode() + rotation) % 4);
      int singleSlabBlock = meta == 0 ? 44 : 126;
      int doubleSlabBlock = meta == 0 ? 43 : 5;
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 2, 1 + height, 3, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 3, 1 + height, 3, sbb);
      this.setCoordBaseMode(temp);
   }

   protected boolean makeStairs7(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeBlockAtCurrentPosition(world, 126, 2, 1, 1, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 1, 1, 5, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, 5, 1, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 5, 1, 1, sbb);
      int rise = 2;
      int numFlights = this.highestOpening / rise;

      for (int i = 0; i < numFlights; i++) {
         this.makeStairs7flight(world, rand, sbb, 1 + i * rise, 0 + i * 3, 2);
         this.makeStairs7flight(world, rand, sbb, 1 + i * rise, 2 + i * 3, 0);
      }

      return true;
   }

   protected void makeStairs7flight(OldWorld world, Random rand, StructureBoundingBox sbb, int height, int rotation, int meta) {
      int temp = this.getCoordBaseMode();
      this.setCoordBaseMode((this.getCoordBaseMode() + rotation) % 4);
      int singleSlabBlock = meta == 0 ? 44 : 126;
      int doubleSlabBlock = meta == 0 ? 43 : 5;
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 2, 1 + height, 5, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 3, 1 + height, 5, sbb);
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 4, 2 + height, 5, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 5, 2 + height, 5, sbb);
      this.setCoordBaseMode(temp);
   }

   protected boolean makeStairs9(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeBlockAtCurrentPosition(world, 126, 2, 1, 1, 6, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 1, 1, 7, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, 7, 1, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 7, 1, 1, sbb);
      int rise = 3;
      int numFlights = this.highestOpening / rise;

      for (int i = 0; i < numFlights; i++) {
         this.makeStairs9flight(world, rand, sbb, 1 + i * rise, 0 + i * 3, 2);
         this.makeStairs9flight(world, rand, sbb, 1 + i * rise, 2 + i * 3, 0);
      }

      return true;
   }

   protected void makeStairs9flight(OldWorld world, Random rand, StructureBoundingBox sbb, int height, int rotation, int meta) {
      int temp = this.getCoordBaseMode();
      this.setCoordBaseMode((this.getCoordBaseMode() + rotation) % 4);
      int singleSlabBlock = meta == 0 ? 44 : 126;
      int doubleSlabBlock = meta == 0 ? 43 : 5;
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 2, 1 + height, 7, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 3, 1 + height, 7, sbb);
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 4, 2 + height, 7, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 5, 2 + height, 7, sbb);
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 6, 3 + height, 7, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 7, 3 + height, 7, sbb);
      this.setCoordBaseMode(temp);
   }

   protected boolean makeStairs15(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.placeBlockAtCurrentPosition(world, 126, 2, 1, 1, 9, sbb);
      this.placeBlockAtCurrentPosition(world, 126, 2, 2, 1, 9, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 1, 1, 10, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 2, 1, 10, sbb);
      this.placeBlockAtCurrentPosition(world, 126, 2, 1, 2, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 126, 2, 2, 2, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 1, 2, 12, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 2, 2, 12, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 1, 2, 13, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 2, 2, 13, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 3, 2, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 3, 3, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 3, 4, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 3, 1, 10, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 3, 2, 10, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 3, 3, 10, sbb);
      this.placeBlockAtCurrentPosition(world, 5, 2, 3, 1, 9, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 3, 2, 9, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, 13, 1, 5, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, 12, 1, 5, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 13, 1, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 12, 1, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, 13, 2, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 44, 0, 12, 2, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 13, 2, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 12, 2, 2, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 13, 2, 1, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 12, 2, 1, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 11, 2, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 11, 3, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 11, 4, 3, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 11, 1, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 11, 2, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 11, 3, 4, sbb);
      this.placeBlockAtCurrentPosition(world, 43, 0, 11, 1, 5, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 11, 2, 5, sbb);
      int rise = 5;
      int numFlights = this.highestOpening / rise;

      for (int i = 0; i < numFlights; i++) {
         this.makeStairs15flight(world, rand, sbb, 2 + i * rise, 0 + i * 3, 2);
         this.makeStairs15flight(world, rand, sbb, 2 + i * rise, 2 + i * 3, 0);
      }

      return true;
   }

   protected void makeStairs15flight(OldWorld world, Random rand, StructureBoundingBox sbb, int height, int rotation, int meta) {
      int temp = this.getCoordBaseMode();
      this.setCoordBaseMode((this.getCoordBaseMode() + rotation) % 4);
      int singleSlabBlock = meta == 0 ? 44 : 126;
      int doubleSlabBlock = meta == 0 ? 43 : 5;
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 3, 1 + height, 13, sbb);
      this.randomlyPlaceBlock(world, sbb, rand, 0.9F, 4, 1 + height, 13, doubleSlabBlock, meta);
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 5, 2 + height, 13, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 6, 2 + height, 13, sbb);
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 7, 3 + height, 13, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 8, 3 + height, 13, sbb);
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 9, 4 + height, 13, sbb);
      this.randomlyPlaceBlock(world, sbb, rand, 0.9F, 10, 4 + height, 13, doubleSlabBlock, meta);
      this.randomlyPlaceBlock(world, sbb, rand, 0.9F, 11, 5 + height, 13, singleSlabBlock, meta);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 12, 5 + height, 13, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 13, 5 + height, 13, sbb);
      this.randomlyPlaceBlock(world, sbb, rand, 0.9F, 3, 1 + height, 12, singleSlabBlock, meta);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 4, 1 + height, 12, sbb);
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 5, 2 + height, 12, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 6, 2 + height, 12, sbb);
      this.randomlyPlaceBlock(world, sbb, rand, 0.9F, 7, 3 + height, 12, singleSlabBlock, meta);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 8, 3 + height, 12, sbb);
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 9, 4 + height, 12, sbb);
      this.randomlyPlaceBlock(world, sbb, rand, 0.9F, 10, 4 + height, 12, doubleSlabBlock, meta);
      this.placeBlockAtCurrentPosition(world, singleSlabBlock, meta, 11, 5 + height, 12, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 12, 5 + height, 12, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 13, 5 + height, 12, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 4, 1 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 5, 2 + height, 11, sbb);
      this.randomlyPlaceBlock(world, sbb, rand, 0.9F, 6, 2 + height, 11, doubleSlabBlock, meta);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 7, 3 + height, 11, sbb);
      this.randomlyPlaceBlock(world, sbb, rand, 0.9F, 8, 3 + height, 11, doubleSlabBlock, meta);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 9, 4 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 10, 4 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, doubleSlabBlock, meta, 11, 5 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 4, 2 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 5, 3 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 6, 3 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 7, 4 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 8, 4 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 9, 5 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 10, 5 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 11, 6 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 4, 3 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 6, 4 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 8, 5 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 10, 6 + height, 11, sbb);
      this.placeBlockAtCurrentPosition(world, 85, 0, 11, 7 + height, 11, sbb);
      this.setCoordBaseMode(temp);
   }

   protected void generatePaintingsOnWall(OldWorld world, Random rand, int howMany, int floorLevel, int direction, int minSize, StructureBoundingBox sbb) {
      for (int i = 0; i < howMany; i++) {
         ChunkCoordinates pCoords = this.getRandomWallSpot(rand, floorLevel, direction, sbb);
         // the original threw a NullPointerException here (never for the main tower, which lies in one chunk)
         int art = this.getPaintingOfSize(rand, minSize);
         if (pCoords != null && art >= 0 && this.checkPainting(world, pCoords, direction, art)) {
            world.spawnEntity(new OldEntity.Painting(pCoords.posX, pCoords.posY, pCoords.posZ, direction, OldPainting.TITLES[art]));
         }
      }
   }

   /** The index of an EnumArt motive at least this wide or high, or -1. */
   protected int getPaintingOfSize(Random rand, int minSize) {
      ArrayList<Integer> valid = new ArrayList<>();

      for (int art = 0; art < OldPainting.TITLES.length; art++) {
         if (OldPainting.WIDTH[art] >= minSize || OldPainting.HEIGHT[art] >= minSize) {
            valid.add(art);
         }
      }

      return valid.size() > 0 ? valid.get(rand.nextInt(valid.size())) : -1;
   }

   /** checkPainting: no block collision box and no other painting in the box of the painting, widened along the wall. */
   protected boolean checkPainting(OldWorld world, ChunkCoordinates pos, int direction, int art) {
      OldPainting.Hanging h = OldPainting.Hanging.of(pos.posX, pos.posY, pos.posZ, direction, OldPainting.WIDTH[art], OldPainting.HEIGHT[art]);
      double ex = direction != 0 && direction != 2 ? 0.06 : 1.0;
      double ez = direction != 0 && direction != 2 ? 1.0 : 0.06;
      // AxisAlignedBB.expand
      double minX = h.minX() - ex, minY = h.minY() - 1.0, minZ = h.minZ() - ez;
      double maxX = h.maxX() + ex, maxY = h.maxY() + 1.0, maxZ = h.maxZ() + ez;
      if (OldPainting.collides(world, minX, minY, minZ, maxX, maxY, maxZ)) {
         return false;
      }
      return !OldPainting.hangingWithin(world, minX, minY, minZ, maxX, maxY, maxZ);
   }

   protected ChunkCoordinates getRandomWallSpot(Random rand, int floorLevel, int direction, StructureBoundingBox sbb) {
      int minX = this.boundingBox.minX + 2;
      int maxX = this.boundingBox.maxX - 2;
      int minY = this.boundingBox.minY + floorLevel + 2;
      int maxY = this.boundingBox.maxY - 2;
      int minZ = this.boundingBox.minZ + 2;
      int maxZ = this.boundingBox.maxZ - 2;
      if (direction == 0) {
         minZ = this.boundingBox.minZ;
         maxZ = this.boundingBox.minZ;
      }

      if (direction == 1) {
         maxX = this.boundingBox.maxX;
         minX = this.boundingBox.maxX;
      }

      if (direction == 2) {
         maxZ = this.boundingBox.maxZ;
         minZ = this.boundingBox.maxZ;
      }

      if (direction == 3) {
         minX = this.boundingBox.minX;
         maxX = this.boundingBox.minX;
      }

      for (int i = 0; i < 30; i++) {
         int cx = minX + (maxX > minX ? rand.nextInt(maxX - minX) : 0);
         int cy = minY + (maxY > minY ? rand.nextInt(maxY - minY) : 0);
         int cz = minZ + (maxZ > minZ ? rand.nextInt(maxZ - minZ) : 0);
         if (sbb.isVecInside(cx, cy, cz)) {
            return new ChunkCoordinates(cx, cy, cz);
         }
      }

      return null;
   }
}
