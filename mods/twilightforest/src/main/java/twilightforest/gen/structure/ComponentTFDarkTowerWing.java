package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerWing extends ComponentTFTowerWing {
   protected boolean keyTower = false;
   protected ArrayList openingTypes = new ArrayList();

   protected ComponentTFDarkTowerWing(int i, int x, int y, int z, int pSize, int pHeight, int direction) {
      super(i, x, y, z, pSize, pHeight, direction);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random rand) {
      if (parent != null && parent instanceof StructureTFComponent) {
         this.deco = ((StructureTFComponent)parent).deco;
      }

      this.addOpening(0, 1, this.size / 2, 2);
      this.makeARoof(parent, list, rand);
      this.makeABeard(parent, list, rand);
      if (this.size > 10) {
         for (int direction = 0; direction < 4; direction++) {
            int[] dest = this.getValidOpening(rand, direction);
            int childSize = this.size - 2;
            int childHeight = this.validateChildHeight(this.height - 4 + rand.nextInt(10) - rand.nextInt(10), childSize);
            boolean madeWing = this.makeTowerWing(list, rand, this.getComponentType(), dest[0], dest[1], dest[2], this.size - 2, childHeight, direction);
            if (!madeWing && (direction == 2 || rand.nextBoolean())) {
               this.makeTowerBalcony(list, rand, this.getComponentType(), dest[0], dest[1], dest[2], direction);
            }
         }
      } else if (rand.nextInt(4) == 0) {
         int direction = rand.nextInt(4);
         int[] dest = this.getValidOpening(rand, direction);
         this.makeTowerBalcony(list, rand, this.getComponentType(), dest[0], dest[1], dest[2], direction);
      }
   }

   protected int validateChildHeight(int childHeight, int childSize) {
      return childHeight / 4 * 4 + 1;
   }

   @Override
   public void makeARoof(StructureComponent parent, List list, Random rand) {
      int index = this.getComponentType();
      ComponentTFTowerRoof roof;
      switch (rand.nextInt(5)) {
         case 0:
         case 1:
         default:
            roof = new ComponentTFDarkTowerRoofAntenna(index, this);
            break;
         case 2:
            roof = new ComponentTFDarkTowerRoofCactus(index, this);
            break;
         case 3:
            roof = new ComponentTFDarkTowerRoofRings(index, this);
            break;
         case 4:
            roof = new ComponentTFDarkTowerRoofFourPost(index, this);
      }

      list.add(roof);
      roof.buildComponent(this, list, rand);
      this.roofType = roof.getClass();
   }

   @Override
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

   @Override
   public void makeABeard(StructureComponent parent, List list, Random rand) {
      ComponentTFDarkTowerBeard beard = new ComponentTFDarkTowerBeard(this.getComponentType() + 1, this);
      list.add(beard);
      beard.buildComponent(this, list, rand);
   }

   @Override
   public boolean makeTowerWing(List list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, int rotation) {
      if (wingHeight < 8) {
         return false;
      }

      int direction = (this.getCoordBaseMode() + rotation) % 4;
      int[] dx = this.offsetTowerCoords(x, y, z, 5, direction);
      if (dx[1] + wingHeight > 250) {
         return false;
      }

      ComponentTFDarkTowerBridge bridge = new ComponentTFDarkTowerBridge(index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
      StructureComponent intersect = StructureComponent.findIntersecting(list, bridge.getBoundingBox());
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

   protected boolean makeTowerBalcony(List list, Random rand, int index, int x, int y, int z, int rotation) {
      int direction = (this.getCoordBaseMode() + rotation) % 4;
      int[] dx = this.offsetTowerCoords(x, y, z, 5, direction);
      ComponentTFDarkTowerBalcony balcony = new ComponentTFDarkTowerBalcony(index, dx[0], dx[1], dx[2], direction);
      StructureComponent intersect = StructureComponent.findIntersecting(list, balcony.getBoundingBox());
      if (intersect != null && intersect != this) {
         return false;
      }

      list.add(balcony);
      balcony.buildComponent(this, list, rand);
      this.addOpening(x, y, z, rotation, EnumDarkTowerDoor.REAPPEARING);
      return true;
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      Random decoRNG = new Random(world.seed() + this.boundingBox.minX * 321534781 ^ this.boundingBox.minZ * 756839);
      this.makeEncasedWalls(world, rand, sbb, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1);
      this.fillWithAir(world, sbb, 1, 1, 1, this.size - 2, this.height - 2, this.size - 2);
      this.nullifySkyLightForBoundingBox(world);
      if (this.size > 9) {
         this.addHalfFloors(world, decoRNG, sbb, 4, this.height - 1);
      } else if (decoRNG.nextInt(3) == 0) {
         this.addSmallTimberBeams(world, decoRNG, sbb, 4, this.height - 1);
      } else {
         this.addHalfFloors(world, decoRNG, sbb, 4, this.height - 1);
      }

      this.makeOpenings(world, sbb);
      if (decoRNG.nextBoolean() && !this.isKeyTower() && this.height > 8) {
         int blobs = 1;
         if (this.size > 9 && decoRNG.nextBoolean()) {
            blobs++;
         }

         for (int i = 0; i < blobs; i++) {
            int x = decoRNG.nextInt(this.size);
            int y = decoRNG.nextInt(this.height - 7) + 2;
            int z = decoRNG.nextInt(this.size);
            this.destroyTower(world, decoRNG, x, y, z, 3, sbb);
         }
      }

      return true;
   }

   protected void destroyTower(OldWorld world, Random decoRNG, int x, int y, int z, int amount, StructureBoundingBox sbb) {
      int initialRadius = decoRNG.nextInt(amount) + amount;
      this.drawBlob(world, x, y, z, initialRadius, 0, 0, sbb);

      for (int i = 0; i < 3; i++) {
         int dx = x + (initialRadius - 1) * (decoRNG.nextBoolean() ? 1 : -1);
         int dy = y + (initialRadius - 1) * (decoRNG.nextBoolean() ? 1 : -1);
         int dz = z + (initialRadius - 1) * (decoRNG.nextBoolean() ? 1 : -1);
         this.netherTransformBlob(world, decoRNG, dx, dy, dz, initialRadius - 1, sbb);
         this.drawBlob(world, dx, dy, dz, initialRadius - 2, 0, 0, sbb);
      }
   }

   private void netherTransformBlob(OldWorld world, Random inRand, int sx, int sy, int sz, int rad, StructureBoundingBox sbb) {
      Random rand = new Random(inRand.nextLong());

      for (byte dx = 0; dx <= rad; dx++) {
         for (byte dy = 0; dy <= rad; dy++) {
            for (byte dz = 0; dz <= rad; dz++) {
               byte dist = 0;
               if (dx >= dy && dx >= dz) {
                  dist = (byte)(dx + (byte)(Math.max(dy, dz) * 0.5 + Math.min(dy, dz) * 0.25));
               } else if (dy >= dx && dy >= dz) {
                  dist = (byte)(dy + (byte)(Math.max(dx, dz) * 0.5 + Math.min(dx, dz) * 0.25));
               } else {
                  dist = (byte)(dz + (byte)(Math.max(dx, dy) * 0.5 + Math.min(dx, dy) * 0.25));
               }

               if (dist <= rad) {
                  this.testAndChangeToNetherrack(world, rand, sx + dx, sy + dy, sz + dz, sbb);
                  this.testAndChangeToNetherrack(world, rand, sx + dx, sy + dy, sz + dz, sbb);
                  this.testAndChangeToNetherrack(world, rand, sx + dx, sy + dy, sz - dz, sbb);
                  this.testAndChangeToNetherrack(world, rand, sx - dx, sy + dy, sz + dz, sbb);
                  this.testAndChangeToNetherrack(world, rand, sx - dx, sy + dy, sz - dz, sbb);
                  this.testAndChangeToNetherrack(world, rand, sx + dx, sy - dy, sz + dz, sbb);
                  this.testAndChangeToNetherrack(world, rand, sx + dx, sy - dy, sz - dz, sbb);
                  this.testAndChangeToNetherrack(world, rand, sx - dx, sy - dy, sz + dz, sbb);
                  this.testAndChangeToNetherrack(world, rand, sx - dx, sy - dy, sz - dz, sbb);
               }
            }
         }
      }
   }

   private void testAndChangeToNetherrack(OldWorld world, Random rand, int x, int y, int z, StructureBoundingBox sbb) {
      if (this.getBlockIdAtCurrentPosition(world, x, y, z, sbb) > 0) {
         this.placeBlockAtCurrentPosition(world, 87, 0, x, y, z, sbb);
         if (this.getBlockIdAtCurrentPosition(world, x, y + 1, z, sbb) == 0 && rand.nextBoolean()) {
            this.placeBlockAtCurrentPosition(world, 51, 0, x, y + 1, z, sbb);
         }
      }
   }

   public void drawBlob(OldWorld world, int sx, int sy, int sz, int rad, int blockValue, int metaValue, StructureBoundingBox sbb) {
      for (byte dx = 0; dx <= rad; dx++) {
         for (byte dy = 0; dy <= rad; dy++) {
            for (byte dz = 0; dz <= rad; dz++) {
               byte dist = 0;
               if (dx >= dy && dx >= dz) {
                  dist = (byte)(dx + (byte)(Math.max(dy, dz) * 0.5 + Math.min(dy, dz) * 0.25));
               } else if (dy >= dx && dy >= dz) {
                  dist = (byte)(dy + (byte)(Math.max(dx, dz) * 0.5 + Math.min(dx, dz) * 0.25));
               } else {
                  dist = (byte)(dz + (byte)(Math.max(dx, dy) * 0.5 + Math.min(dx, dy) * 0.25));
               }

               if (dist <= rad) {
                  this.placeBlockAtCurrentPosition(world, blockValue, metaValue, sx + dx, sy + dy, sz + dz, sbb);
                  this.placeBlockAtCurrentPosition(world, blockValue, metaValue, sx + dx, sy + dy, sz - dz, sbb);
                  this.placeBlockAtCurrentPosition(world, blockValue, metaValue, sx - dx, sy + dy, sz + dz, sbb);
                  this.placeBlockAtCurrentPosition(world, blockValue, metaValue, sx - dx, sy + dy, sz - dz, sbb);
                  this.placeBlockAtCurrentPosition(world, blockValue, metaValue, sx + dx, sy - dy, sz + dz, sbb);
                  this.placeBlockAtCurrentPosition(world, blockValue, metaValue, sx + dx, sy - dy, sz - dz, sbb);
                  this.placeBlockAtCurrentPosition(world, blockValue, metaValue, sx - dx, sy - dy, sz + dz, sbb);
                  this.placeBlockAtCurrentPosition(world, blockValue, metaValue, sx - dx, sy - dy, sz - dz, sbb);
               }
            }
         }
      }
   }

   protected void addHalfFloors(OldWorld world, Random rand, StructureBoundingBox sbb, int bottom, int top) {
      int spacing = 4;
      int rotation = (this.boundingBox.minY + bottom) % 3;
      if (bottom == 0) {
         bottom += spacing;
      }

      for (int y = bottom; y < top; y += spacing) {
         rotation += 2;
         rotation %= 4;
         if (y >= top - spacing) {
            this.makeFullFloor(world, sbb, rotation, y, spacing);
            if (this.isDeadEnd()) {
               this.decorateTreasureRoom(world, sbb, rotation, y, 4, this.deco);
            }
         } else {
            this.makeHalfFloor(world, sbb, rotation, y, spacing);
            switch (rand.nextInt(8)) {
               case 0:
                  if (this.size < 11) {
                     this.decorateReappearingFloor(world, rand, sbb, rotation, y);
                     break;
                  }
               case 1:
                  this.decorateSpawner(world, rand, sbb, rotation, y);
                  break;
               case 2:
                  this.decorateLounge(world, rand, sbb, rotation, y);
                  break;
               case 3:
                  this.decorateLibrary(world, rand, sbb, rotation, y);
                  break;
               case 4:
                  this.decorateExperimentPulser(world, rand, sbb, rotation, y);
                  break;
               case 5:
                  this.decorateExperimentLamp(world, rand, sbb, rotation, y);
                  break;
               case 6:
                  this.decoratePuzzleChest(world, rand, sbb, rotation, y);
            }
         }

         this.addStairsDown(world, sbb, rotation, y, this.size - 2, spacing);
         if (this.size > 9) {
            this.addStairsDown(world, sbb, rotation, y, this.size - 3, spacing);
         }
      }

      rotation += 2;
      rotation %= 4;
      this.addStairsDown(world, sbb, rotation, this.height - 1, this.size - 2, spacing);
   }

   protected void makeHalfFloor(OldWorld world, StructureBoundingBox sbb, int rotation, int y, int spacing) {
      this.fillBlocksRotated(world, sbb, this.size / 2, y, 1, this.size - 2, y, this.size - 2, this.deco.blockID, this.deco.blockMeta, rotation);
      this.fillBlocksRotated(world, sbb, this.size / 2 - 1, y, 1, this.size / 2 - 1, y, this.size - 2, this.deco.accentID, this.deco.accentMeta, rotation);
   }

   protected void makeFullFloor(OldWorld world, StructureBoundingBox sbb, int rotation, int y, int spacing) {
      this.fillWithMetadataBlocks(world, sbb, 1, y, 1, this.size - 2, y, this.size - 2, this.deco.blockID, this.deco.blockMeta, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, this.size / 2, y, 1, this.size / 2, y, this.size - 2, this.deco.accentID, this.deco.accentMeta, 0, 0, true);
   }

   protected void decorateTreasureRoom(OldWorld world, StructureBoundingBox sbb, int rotation, int y, int spacing, StructureTFDecorator myDeco) {
      int x = this.size / 2;
      int z = this.size / 2;

      for (int dy = 1; dy < spacing; dy++) {
         this.placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x - 1, y + dy, z - 1, rotation, sbb);
         this.placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x + 1, y + dy, z - 1, rotation, sbb);
         this.placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x - 1, y + dy, z + 1, rotation, sbb);
         this.placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x + 1, y + dy, z + 1, rotation, sbb);
      }

      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(1 + rotation), x + 0, y + 1, z - 1, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(0 + rotation), x - 1, y + 1, z + 0, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(2 + rotation), x + 1, y + 1, z + 0, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(3 + rotation), x + 0, y + 1, z + 1, rotation, sbb);

      for (int dy = 2; dy < spacing - 1; dy++) {
         this.placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta, x + 0, y + dy, z - 1, rotation, sbb);
         this.placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta, x - 1, y + dy, z + 0, rotation, sbb);
         this.placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta, x + 1, y + dy, z + 0, rotation, sbb);
         this.placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta, x + 0, y + dy, z + 1, rotation, sbb);
      }

      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(1 + rotation) + 4, x + 0, y + spacing - 1, z - 1, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(0 + rotation) + 4, x - 1, y + spacing - 1, z + 0, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(2 + rotation) + 4, x + 1, y + spacing - 1, z + 0, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(3 + rotation) + 4, x + 0, y + spacing - 1, z + 1, rotation, sbb);
      this.placeBlockRotated(world, myDeco.platformID, myDeco.platformMeta, x, y + 1, z, rotation, sbb);
      this.placeTreasureAtCurrentPosition(world, null, x, y + 2, z, this.isKeyTower() ? TFTreasure.DARKTOWER_KEY : TFTreasure.DARKTOWER_CACHE, sbb);
      if (this.isKeyTower()) {
         this.putItemInTreasure(world, x, y + 2, z, new OldStack(TOWER_KEY, 0, 1), sbb);
      }
   }

   private void decorateSpawner(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y) {
      int x = this.size > 9 ? 4 : 3;
      int z = this.size > 9 ? 5 : 4;
      String mobID;
      if (this.size > 9) {
         mobID = rand.nextBoolean() ? "TwilightForest.Tower Golem" : "TwilightForest.Redscale Broodling";
      } else {
         mobID = "TwilightForest.Redscale Broodling";
      }

      this.makePillarFrame(world, sbb, this.deco, rotation, x, y, z, true);
      this.placeSpawnerRotated(world, x + 1, y + 2, z + 1, rotation, mobID, sbb);
   }

   private void decorateLounge(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y) {
      int cx = this.size > 9 ? 9 : 7;
      int cz = this.size > 9 ? 4 : 3;
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), cx, y + 1, cz + 0, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation), cx, y + 1, cz + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation), cx, y + 1, cz + 2, rotation, sbb);
      cx = this.size > 9 ? 5 : 3;
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, cx, y + 1, cz + 0, rotation, sbb);
      this.placeBlockRotated(world, 126, 9, cx, y + 1, cz + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation) + 4, cx, y + 1, cz + 2, rotation, sbb);
   }

   private void decorateReappearingFloor(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y) {
      this.fillBlocksRotated(world, sbb, 4, y, 3, 7, y, 5, 1370, 0, rotation);
      this.fillBlocksRotated(world, sbb, 4, y + 1, 2, 7, y + 1, 2, 72, 0, rotation);
      this.fillBlocksRotated(world, sbb, 4, y + 1, 6, 7, y + 1, 6, 72, 0, rotation);
   }

   private void decorateExperimentLamp(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y) {
      int cx = this.size > 9 ? 5 : 3;
      int cz = this.size > 9 ? 5 : 4;
      this.placeBlockRotated(world, 29, 1, cx, y + 1, cz, rotation, sbb);
      this.placeBlockRotated(world, 123, 0, cx, y + 2, cz, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, cx, y + 1, cz + 1, rotation, sbb);
      this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, 3), cx, y + 1, cz + 2, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, cx, y + 3, cz - 1, rotation, sbb);
      this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, 2) + 8, cx, y + 3, cz - 2, rotation, sbb);
   }

   private void decorateExperimentPulser(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y) {
      int cx = this.size > 9 ? 6 : 5;
      int cz = this.size > 9 ? 4 : 3;
      this.placeBlockRotated(world, 29, 5 - this.getStairMeta(3 + rotation), cx, y + 1, cz + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, cx, y + 1, cz, rotation, sbb);
      this.placeBlockRotated(world, 55, 0, cx + 1, y + 1, cz, rotation, sbb);
      this.placeBlockRotated(world, 72, 0, cx + 2, y + 1, cz, rotation, sbb);
      this.placeBlockRotated(world, 93, (rotation + 1) % 4 + 4, cx - 1, y + 1, cz, rotation, sbb);
      this.placeBlockRotated(world, 55, 0, cx - 2, y + 1, cz, rotation, sbb);
      this.placeBlockRotated(world, 55, 0, cx - 2, y + 1, cz + 1, rotation, sbb);
      this.placeBlockRotated(world, 55, 0, cx - 1, y + 1, cz + 1, rotation, sbb);
   }

   private void decorateLibrary(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y) {
      int bx = this.size > 9 ? 4 : 3;
      int bz = this.size > 9 ? 3 : 2;
      this.makeSmallBookshelf(world, sbb, rotation, y, bx, bz);
      bx = this.size > 9 ? 9 : 7;
      bz = this.size > 9 ? 3 : 2;
      this.makeSmallBookshelf(world, sbb, rotation, y, bx, bz);
   }

   protected void makeSmallBookshelf(OldWorld world, StructureBoundingBox sbb, int rotation, int y, int bx, int bz) {
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation) + 0, bx, y + 1, bz + 0, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation) + 4, bx, y + 2, bz + 0, rotation, sbb);
      this.placeBlockRotated(world, 47, 0, bx, y + 1, bz + 1, rotation, sbb);
      this.placeBlockRotated(world, 47, 0, bx, y + 2, bz + 1, rotation, sbb);
      this.placeBlockRotated(world, 47, 0, bx, y + 1, bz + 2, rotation, sbb);
      this.placeBlockRotated(world, 47, 0, bx, y + 2, bz + 2, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 0, bx, y + 1, bz + 3, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, bx, y + 2, bz + 3, rotation, sbb);
   }

   private void decoratePuzzleChest(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y) {
      int x = this.size > 9 ? 4 : 3;
      int z = this.size > 9 ? 5 : 4;
      this.makePillarFrame(world, sbb, this.deco, rotation, x, y, z, true);
      this.placeBlockRotated(world, this.deco.platformID, this.deco.platformMeta, x + 1, y + 1, z + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 2, y + 1, z + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 0, y + 1, z + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 1, y + 1, z + 2, rotation, sbb);
      this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 1, y + 1, z + 0, rotation, sbb);
      this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 2, y + 3, z + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 0, y + 3, z + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 1, y + 3, z + 2, rotation, sbb);
      this.placeBlockRotated(world, 0, 0, x + 1, y + 3, z + 0, rotation, sbb);
      this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 1, y + 3, z + 1, rotation, sbb);
      this.placeBlockRotated(world, 29, 5 - this.getStairMeta(1 + rotation), x + 1, y + 3, z - 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 3, z - 2, rotation, sbb);
      this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, 5), x + 2, y + 3, z - 2, rotation, sbb);
      this.placeTreasureRotated(world, x + 1, y + 2, z + 1, rotation, TFTreasure.DARKTOWER_CACHE, sbb);
   }

   protected void makePillarFrame(OldWorld world, StructureBoundingBox sbb, StructureTFDecorator myDeco, int rotation, int x, int y, int z, boolean fenced) {
      this.makePillarFrame(world, sbb, myDeco, rotation, x, y, z, 3, 3, 3, fenced);
   }

   protected void makePillarFrame(
      OldWorld world, StructureBoundingBox sbb, StructureTFDecorator myDeco, int rotation, int x, int y, int z, int width, int height, int length, boolean fenced
   ) {
      for (int dx = 0; dx < width; dx++) {
         for (int dz = 0; dz < length; dz++) {
            if ((dx % 3 == 0 || dx == width - 1) && (dz % 3 == 0 || dz == length - 1)) {
               for (int py = 1; py <= height; py++) {
                  this.placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x + dx, y + py, z + dz, rotation, sbb);
               }
            } else {
               if (dx == 0) {
                  this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(0 + rotation), x + dx, y + 1, z + dz, rotation, sbb);
                  this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(0 + rotation) + 4, x + dx, y + height, z + dz, rotation, sbb);
               } else if (dx == width - 1) {
                  this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(2 + rotation), x + dx, y + 1, z + dz, rotation, sbb);
                  this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(2 + rotation) + 4, x + dx, y + height, z + dz, rotation, sbb);
               } else if (dz == 0) {
                  this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(1 + rotation), x + dx, y + 1, z + dz, rotation, sbb);
                  this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(1 + rotation) + 4, x + dx, y + height, z + dz, rotation, sbb);
               } else if (dz == length - 1) {
                  this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(3 + rotation), x + dx, y + 1, z + dz, rotation, sbb);
                  this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(3 + rotation) + 4, x + dx, y + height, z + dz, rotation, sbb);
               }

               if (fenced && (dx == 0 || dx == width - 1 || dz == 0 || dz == length - 1)) {
                  for (int fy = 2; fy <= height - 1; fy++) {
                     this.placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta, x + dx, y + fy, z + dz, rotation, sbb);
                  }
               }
            }
         }
      }
   }

   /** Tower key (item 7321 + 256). */
   static final int TOWER_KEY = 7321 + 256;

   protected void putItemInTreasure(OldWorld world, int x, int y, int z, OldStack itemToAdd, StructureBoundingBox sbb) {
      int dx = this.getXWithOffset(x, z);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffset(x, z);
      if (sbb.isVecInside(dx, dy, dz)) {
         if (world.getTile(dx, dy, dz) instanceof OldTile.Chest chest) {
            OldStack[] inventory = chest.slots();
            boolean alreadyPresent = false;
            int emptySlots = 0;

            for (int i = 0; i < inventory.length; i++) {
               OldStack inSlot = inventory[i];
               if (inSlot == null) {
                  emptySlots++;
               } else if (inSlot.itemId() == itemToAdd.itemId() && inSlot.damage() == itemToAdd.damage() && inSlot.count() == itemToAdd.count()
                     && inSlot.enchantment() < 0 && inSlot.applied().length == 0) {
                  // ItemStack.areItemStacksEqual: id, size, damage and tags
                  alreadyPresent = true;
                  break;
               }
            }

            if (!alreadyPresent && emptySlots > 0) {
               // world.rand of the original is not seed-fixed: a random from seed and position
               int slotsUntilPlaced = ComponentTFTowerWing.worldRand(world, dx, dy, dz).nextInt(emptySlots);

               for (int i = 0; i < inventory.length; i++) {
                  if (inventory[i] == null) {
                     if (slotsUntilPlaced == 0) {
                        inventory[i] = itemToAdd;
                        break;
                     }

                     slotsUntilPlaced--;
                  }
               }
            }
         }
      }
   }

   protected void addStairsDown(OldWorld world, StructureBoundingBox sbb, int rotation, int y, int sz, int spacing) {
      for (int i = 0; i < spacing; i++) {
         int sx = this.size - 3 - i;
         this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation), sx, y - i, sz, rotation, sbb);
         this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, sx, y - 1 - i, sz, rotation, sbb);
         this.placeBlockRotated(world, 0, 0, sx, y + 1 - i, sz, rotation, sbb);
         this.placeBlockRotated(world, 0, 0, sx, y + 2 - i, sz, rotation, sbb);
         this.placeBlockRotated(world, 0, 0, sx - 1, y + 2 - i, sz, rotation, sbb);
         this.placeBlockRotated(world, 0, 0, sx, y + 3 - i, sz, rotation, sbb);
         this.placeBlockRotated(world, 0, 0, sx - 1, y + 3 - i, sz, rotation, sbb);
      }
   }

   protected void addSmallTimberBeams(OldWorld world, Random rand, StructureBoundingBox sbb, int bottom, int top) {
      int spacing = 4;
      int rotation = 0;
      if (bottom == 0) {
         bottom += spacing;
      }

      for (int y = bottom; y < top; y += spacing) {
         rotation = ++rotation % 4;
         if (y >= top - spacing && this.isDeadEnd()) {
            this.makeTimberFloor(world, rand, sbb, rotation, y, spacing);
            StructureTFDecorator logDeco = new StructureDecoratorDarkTower();
            logDeco.pillarID = 1358;
            logDeco.pillarMeta = 3;
            logDeco.platformID = 1358;
            logDeco.pillarMeta = 3;
            this.decorateTreasureRoom(world, sbb, rotation, y, 4, logDeco);
         } else {
            this.makeSmallTimberBeams(world, rand, sbb, rotation, y, y == bottom && bottom != spacing, y >= top - spacing);
         }
      }
   }

   protected void makeTimberFloor(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y, int spacing) {
      int beamID = 1358;
      int beamMetaBase = 3;
      int beamMetaNS = (this.coordBaseMode + rotation) % 2 == 0 ? 4 : 8;
      int beamMetaEW = beamMetaNS == 4 ? 8 : 4;
      int beamMetaUD = 0;

      for (int z = 1; z < this.size - 1; z++) {
         for (int x = 1; x < this.size - 1; x++) {
            if (x < z) {
               this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaNS, x, y, z, rotation, sbb);
            } else {
               this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaEW, x, y, z, rotation, sbb);
            }
         }
      }

      for (int by = 1; by < 4; by++) {
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaUD, 2, y - by, 2, rotation, sbb);
         this.placeBlockRotated(world, 65, this.getLadderMeta(2 + rotation), 3, y - by, 2, rotation, sbb);
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaUD, 6, y - by, 6, rotation, sbb);
         this.placeBlockRotated(world, 65, this.getLadderMeta(4 + rotation), 5, y - by, 6, rotation, sbb);
      }

      this.placeBlockRotated(world, 0, 0, 3, y, 2, rotation, sbb);
      this.placeBlockRotated(world, 0, 0, 5, y, 6, rotation, sbb);
   }

   protected void makeSmallTimberBeams(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y, boolean bottom, boolean top) {
      int beamID = 1358;
      int beamMetaBase = 3;
      int beamMetaNS = (this.coordBaseMode + rotation) % 2 == 0 ? 4 : 8;
      int beamMetaEW = beamMetaNS == 4 ? 8 : 4;
      int beamMetaUD = 0;

      for (int z = 1; z < this.size - 1; z++) {
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaEW, 2, y, z, rotation, sbb);
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaEW, 6, y, z, rotation, sbb);
      }

      int z = this.pickBetweenExcluding(3, this.size - 3, rand, 2, 2, 6);

      for (int x = 3; x < 6; x++) {
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaNS, x, y, z, rotation, sbb);
      }

      int x1 = 2;
      int z1 = rand.nextBoolean() ? 2 : 6;
      int x3 = 6;
      int z3 = rand.nextBoolean() ? 2 : 6;

      for (int by = 1; by < 4; by++) {
         if (!bottom || this.checkPost(world, x1, y - 4, z1, rotation, sbb)) {
            this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaUD, x1, y - by, z1, rotation, sbb);
            this.placeBlockRotated(world, 65, this.getLadderMeta(2 + rotation), x1 + 1, y - by, z1, rotation, sbb);
         }

         if (!bottom || this.checkPost(world, x3, y - 4, z3, rotation, sbb)) {
            this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaUD, x3, y - by, z3, rotation, sbb);
            this.placeBlockRotated(world, 65, this.getLadderMeta(4 + rotation), x3 - 1, y - by, z3, rotation, sbb);
         }
      }
   }

   protected int pickBetweenExcluding(int low, int high, Random rand, int k, int l, int m) {
      int result;
      do {
         result = rand.nextInt(high - low) + low;
      } while (result == k || result == l || result == m);

      return result;
   }

   protected int pickFrom(Random rand, int i, int j, int k) {
      switch (rand.nextInt(3)) {
         case 0:
         default:
            return i;
         case 1:
            return j;
         case 2:
            return k;
      }
   }

   protected boolean checkPost(OldWorld world, int x, int y, int z, int rotation, StructureBoundingBox sbb) {
      int worldX = this.getXWithOffsetAsIfRotated(x, z, rotation);
      int worldY = this.getYWithOffset(y);
      int worldZ = this.getZWithOffsetAsIfRotated(x, z, rotation);
      int blockID = sbb.isVecInside(worldX, worldY, worldZ) ? world.getBlockId(worldX, worldY, worldZ) : 0;
      return blockID != 0 && (blockID != this.deco.accentID || world.getBlockMetadata(worldX, worldY, worldZ) != this.deco.accentMeta);
   }

   protected void makeEncasedWalls(OldWorld world, Random rand, StructureBoundingBox sbb, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                  if ((x != minY && x != maxX || y != minY && y != maxY && z != minZ && z != maxZ)
                     && (y != minY && y != maxY || x != minY && x != maxX && z != minZ && z != maxZ)
                     && (z != minZ && z != maxZ || x != minY && x != maxX && y != minY && y != maxY)) {
                     StructurePieceBlockSelector blocker = StructureTFComponent.getTowerWoods();
                     blocker.selectBlocks(rand, x, y, z, true);
                     this.placeBlockAtCurrentPosition(world, blocker.getSelectedBlockId(), blocker.getSelectedBlockMetaData(), x, y, z, sbb);
                  } else {
                     this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x, y, z, sbb);
                  }
               }
            }
         }
      }

      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX + 1, minY + 1, minZ, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX + 1, minY + 1, maxZ, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, minY + 1, minZ, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, minY + 1, maxZ, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX + 1, maxY - 1, minZ, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX + 1, maxY - 1, maxZ, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, maxY - 1, minZ, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, maxY - 1, maxZ, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX, minY + 1, minZ + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX, minY + 1, maxZ - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX, minY + 1, minZ + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX, minY + 1, maxZ - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX, maxY - 1, minZ + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX, maxY - 1, maxZ - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX, maxY - 1, minZ + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX, maxY - 1, maxZ - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX + 1, minY, minZ + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX + 1, minY, maxZ - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, minY, minZ + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, minY, maxZ - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX + 1, maxY, minZ + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, minX + 1, maxY, maxZ - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, maxY, minZ + 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, maxY, maxZ - 1, sbb);
   }

   @Override
   public int[] getValidOpening(Random rand, int direction) {
      int verticalOffset = this.size == 19 ? 5 : 4;
      if (direction != 0 && direction != 2) {
         if (direction != 1 && direction != 3) {
            return new int[]{0, 0, 0};
         }

         int rx = this.size / 2;
         int rz = direction == 1 ? this.size - 1 : 0;
         int ry = this.height - verticalOffset;
         return new int[]{rx, ry, rz};
      } else {
         int rx = direction == 0 ? this.size - 1 : 0;
         int rz = this.size / 2;
         int ry = this.height - verticalOffset;
         return new int[]{rx, ry, rz};
      }
   }

   @Override
   public void addOpening(int dx, int dy, int dz, int direction) {
      this.addOpening(dx, dy, dz, direction, EnumDarkTowerDoor.VANISHING);
   }

   protected void addOpening(int dx, int dy, int dz, int direction, EnumDarkTowerDoor type) {
      super.addOpening(dx, dy, dz, direction);
      this.openingTypes.add(this.openings.indexOf(new ChunkCoordinates(dx, dy, dz)), type);
   }

   @Override
   protected void makeOpenings(OldWorld world, StructureBoundingBox sbb) {
      for (int i = 0; i < this.openings.size(); i++) {
         ChunkCoordinates doorCoords = (ChunkCoordinates)this.openings.get(i);
         EnumDarkTowerDoor doorType = (EnumDarkTowerDoor)this.openingTypes.get(i);
         switch (doorType) {
            case VANISHING:
            default:
               this.makeDoorOpening(world, doorCoords.posX, doorCoords.posY, doorCoords.posZ, sbb);
               break;
            case REAPPEARING:
               this.makeReappearingDoorOpening(world, doorCoords.posX, doorCoords.posY, doorCoords.posZ, sbb);
               break;
            case LOCKED:
               this.makeLockedDoorOpening(world, doorCoords.posX, doorCoords.posY, doorCoords.posZ, sbb);
         }
      }
   }

   @Override
   protected void makeDoorOpening(OldWorld world, int dx, int dy, int dz, StructureBoundingBox sbb) {
      this.nullifySkyLightAtCurrentPosition(world, dx - 3, dy - 1, dz - 3, dx + 3, dy + 3, dz + 3);
      if (dx == 0 || dx == this.size - 1) {
         this.fillWithMetadataBlocks(world, sbb, dx, dy - 1, dz - 2, dx, dy + 3, dz + 2, this.deco.accentID, this.deco.accentMeta, 0, 0, false);
         this.fillWithMetadataBlocks(world, sbb, dx, dy, dz - 1, dx, dy + 2, dz + 1, 1370, 2, 0, 0, false);
      }

      if (dz == 0 || dz == this.size - 1) {
         this.fillWithMetadataBlocks(world, sbb, dx - 2, dy - 1, dz, dx + 2, dy + 3, dz, this.deco.accentID, this.deco.accentMeta, 0, 0, false);
         this.fillWithMetadataBlocks(world, sbb, dx - 1, dy, dz, dx + 1, dy + 2, dz, 1370, 2, 0, 0, false);
      }
   }

   protected void makeReappearingDoorOpening(OldWorld world, int dx, int dy, int dz, StructureBoundingBox sbb) {
      this.nullifySkyLightAtCurrentPosition(world, dx - 3, dy - 1, dz - 3, dx + 3, dy + 3, dz + 3);
      if (dx == 0 || dx == this.size - 1) {
         this.fillWithMetadataBlocks(world, sbb, dx, dy - 1, dz - 2, dx, dy + 3, dz + 2, this.deco.accentID, this.deco.accentMeta, 0, 0, false);
         this.fillWithMetadataBlocks(world, sbb, dx, dy, dz - 1, dx, dy + 2, dz + 1, 1370, 0, 0, 0, false);
      }

      if (dz == 0 || dz == this.size - 1) {
         this.fillWithMetadataBlocks(world, sbb, dx - 2, dy - 1, dz, dx + 2, dy + 3, dz, this.deco.accentID, this.deco.accentMeta, 0, 0, false);
         this.fillWithMetadataBlocks(world, sbb, dx - 1, dy, dz, dx + 1, dy + 2, dz, 1370, 0, 0, 0, false);
      }
   }

   protected void makeLockedDoorOpening(OldWorld world, int dx, int dy, int dz, StructureBoundingBox sbb) {
      this.nullifySkyLightAtCurrentPosition(world, dx - 3, dy - 1, dz - 3, dx + 3, dy + 3, dz + 3);
      if (dx == 0 || dx == this.size - 1) {
         this.fillWithMetadataBlocks(world, sbb, dx, dy - 1, dz - 2, dx, dy + 3, dz + 2, this.deco.accentID, this.deco.accentMeta, 0, 0, false);
         this.fillWithMetadataBlocks(world, sbb, dx, dy, dz - 1, dx, dy + 2, dz + 1, 1370, 2, 0, 0, false);
         this.placeBlockAtCurrentPosition(world, 1370, 4, dx, dy + 0, dz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 1370, 4, dx, dy + 0, dz - 1, sbb);
         this.placeBlockAtCurrentPosition(world, 1370, 4, dx, dy + 2, dz + 1, sbb);
         this.placeBlockAtCurrentPosition(world, 1370, 4, dx, dy + 2, dz - 1, sbb);
      }

      if (dz == 0 || dz == this.size - 1) {
         this.fillWithMetadataBlocks(world, sbb, dx - 2, dy - 1, dz, dx + 2, dy + 3, dz, this.deco.accentID, this.deco.accentMeta, 0, 0, false);
         this.fillWithMetadataBlocks(world, sbb, dx - 1, dy, dz, dx + 1, dy + 2, dz, 1370, 2, 0, 0, false);
         this.placeBlockAtCurrentPosition(world, 1370, 4, dx + 1, dy + 0, dz, sbb);
         this.placeBlockAtCurrentPosition(world, 1370, 4, dx - 1, dy + 0, dz, sbb);
         this.placeBlockAtCurrentPosition(world, 1370, 4, dx + 1, dy + 2, dz, sbb);
         this.placeBlockAtCurrentPosition(world, 1370, 4, dx - 1, dy + 2, dz, sbb);
      }
   }

   @Override
   public boolean isDeadEnd() {
      int nonBalconies = 0;

      for (Object o : this.openingTypes) {
         EnumDarkTowerDoor type = (EnumDarkTowerDoor) o;
         if (type != EnumDarkTowerDoor.REAPPEARING) {
            nonBalconies++;
         }
      }

      return nonBalconies <= 1;
   }

   public boolean isKeyTower() {
      return this.keyTower;
   }

   public void setKeyTower(boolean keyTower) {
      this.keyTower = keyTower;
   }

   protected int getLeverMeta(int rotation, int direction) {
      if (direction == 0) {
         return 0;
      }

      if (direction == 1) {
         return 5;
      }

      rotation += this.getCoordBaseMode();
      rotation %= 4;
      if (rotation == 0) {
         switch (direction) {
            case 2:
               return 4;
            case 3:
               return 3;
            case 4:
               return 2;
            case 5:
               return 1;
         }
      } else if (rotation == 1) {
         switch (direction) {
            case 2:
               return 1;
            case 3:
               return 2;
            case 4:
               return 4;
            case 5:
               return 3;
         }
      } else if (rotation == 2) {
         switch (direction) {
            case 2:
               return 3;
            case 3:
               return 4;
            case 4:
               return 1;
            case 5:
               return 2;
         }
      } else if (rotation == 3) {
         switch (direction) {
            case 2:
               return 2;
            case 3:
               return 1;
            case 4:
               return 3;
            case 5:
               return 4;
         }
      }

      return -1;
   }
}
