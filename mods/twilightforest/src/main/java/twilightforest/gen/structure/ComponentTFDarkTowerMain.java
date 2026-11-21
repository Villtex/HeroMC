package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerMain extends ComponentTFDarkTowerWing {
   /** Borer essence (item 7252 + 256) and carminite (7253 + 256). */
   static final int BORER_ESSENCE = 7252 + 256;
   static final int CARMINITE = 7253 + 256;
   /** Facing.offsets*ForSide. */
   private static final int[] OFFSET_X = {0, 0, 0, 0, -1, 1};
   private static final int[] OFFSET_Y = {-1, 1, 0, 0, 0, 0};
   private static final int[] OFFSET_Z = {0, 0, -1, 1, 0, 0};

   private boolean placedKeys = false;

   public ComponentTFDarkTowerMain(OldWorld world, Random rand, int index, int x, int y, int z) {
      this(world, rand, index, x + 19, y, z + 19, 2);
   }

   public ComponentTFDarkTowerMain(OldWorld world, Random rand, int index, int x, int y, int z, int rotation) {
      super(index, x, y, z, 19, 56 + rand.nextInt(32) / 5 * 5, rotation);
      if (this.deco == null) {
         this.deco = new StructureDecoratorDarkTower();
      }
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random rand) {
      if (parent != null && parent instanceof StructureTFComponent) {
         this.deco = ((StructureTFComponent)parent).deco;
      }

      if (this.getComponentType() > 0) {
         this.addOpening(0, 1, this.size / 2, 2);
      }

      int mainDir = -1;
      if (this.getComponentType() < 2) {
         mainDir = rand.nextInt(4);

         for (int i = 0; i < 4; i++) {
            if (i != mainDir) {
               int[] dest = this.getValidOpening(rand, i);
               int childHeight = this.validateChildHeight(21 + rand.nextInt(10), 11);
               this.makeTowerWing(list, rand, this.getComponentType(), dest[0], dest[1], dest[2], 11, childHeight, i);
            }
         }
      } else {
         for (int i = 0; i < 4; i++) {
            int[] dest = this.getValidOpening(rand, i);
            this.makeBossTrapWing(list, rand, this.getComponentType(), dest[0], dest[1], dest[2], i);
         }
      }

      if (this.getComponentType() > 0) {
         for (int i = 0; i < 4; i++) {
            if (i != 2) {
               int[] dest = this.getValidOpening(rand, i);
               dest[1] = 1;
               int childHeight = this.validateChildHeight(21 + rand.nextInt(10), 11);
               this.makeTowerWing(list, rand, this.getComponentType(), dest[0], dest[1], dest[2], 11, childHeight, i);
            }
         }

         this.makeABeard(parent, list, rand);
      } else {
         for (int i = 0; i < 4; i += 2) {
            int[] dest = this.getValidOpening(rand, i);
            dest[1] = 1;
            int childHeight = this.validateChildHeight(10 + rand.nextInt(5), 9);
            this.makeEntranceTower(list, rand, 5, dest[0], dest[1], dest[2], 9, childHeight, i);
         }
      }

      if (mainDir > -1) {
         int[] dest = this.getValidOpening(rand, mainDir);
         this.makeNewLargeTower(list, rand, this.getComponentType() + 1, dest[0], dest[1], dest[2], mainDir);
      }

      this.makeARoof(parent, list, rand);
      if (!this.placedKeys && this.getComponentType() < 2) {
         ArrayList possibleKeyTowers = new ArrayList();
         int smallTowers = 0;

         for (Object piece : list) {
            if (piece instanceof ComponentTFDarkTowerWing) {
               ComponentTFDarkTowerWing wing = (ComponentTFDarkTowerWing)piece;
               if (wing.size == 9 && wing.getComponentType() == this.getComponentType()) {
                  smallTowers++;
                  possibleKeyTowers.add(wing);
               }
            }
         }

         for (int i = 0; i < 4; i++) {
            if (possibleKeyTowers.size() < 1) {
               // FMLLog.warning("[TwilightForest] Dark forest tower could not find four small towers to place keys in.") (stdout is the prediction of expect.sh)
               break;
            }

            int towerNum = rand.nextInt(possibleKeyTowers.size());
            ((ComponentTFDarkTowerWing)possibleKeyTowers.get(towerNum)).setKeyTower(true);
            possibleKeyTowers.remove(towerNum);
         }

         this.placedKeys = true;
      }
   }

   private boolean makeEntranceTower(List list, Random rand, int index, int x, int y, int z, int childSize, int childHeight, int rotation) {
      int direction = (this.getCoordBaseMode() + rotation) % 4;
      int[] dx = this.offsetTowerCoords(x, y, z, 5, direction);
      ComponentTFDarkTowerBridge bridge = new ComponentTFDarkTowerEntranceBridge(index, dx[0], dx[1], dx[2], childSize, childHeight, direction);
      list.add(bridge);
      bridge.buildComponent(this, list, rand);
      this.addOpening(x, y, z, rotation);
      return true;
   }

   private boolean makeNewLargeTower(List list, Random rand, int index, int x, int y, int z, int rotation) {
      int wingSize = 15;
      int wingHeight = 56;
      int direction = (this.getCoordBaseMode() + rotation) % 4;
      int[] dx = this.offsetTowerCoords(x, y, z, 5, direction);
      ComponentTFDarkTowerMainBridge bridge = new ComponentTFDarkTowerMainBridge(index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
      list.add(bridge);
      bridge.buildComponent(this, list, rand);
      this.addOpening(x, y, z, rotation, EnumDarkTowerDoor.LOCKED);
      return true;
   }

   private boolean makeBossTrapWing(List list, Random rand, int index, int x, int y, int z, int rotation) {
      int wingSize = 11;
      int wingHeight = 9;
      int direction = (this.getCoordBaseMode() + rotation) % 4;
      int[] dx = this.offsetTowerCoords(x, y, z, 5, direction);
      ComponentTFDarkTowerBossBridge bridge = new ComponentTFDarkTowerBossBridge(index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
      list.add(bridge);
      bridge.buildComponent(this, list, rand);
      this.addOpening(x, y, z, rotation);
      return true;
   }

   @Override
   public void makeARoof(StructureComponent parent, List list, Random rand) {
      if (this.getComponentType() < 2) {
         super.makeARoof(parent, list, rand);
      }
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      Random decoRNG = new Random(world.seed() + this.boundingBox.minX * 321534781 ^ this.boundingBox.minZ * 756839);
      this.makeEncasedWalls(world, rand, sbb, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1);
      this.fillWithAir(world, sbb, 1, 1, 1, this.size - 2, this.height - 2, this.size - 2);
      if (this.getComponentType() == 0) {
         for (int x = 0; x < this.size; x++) {
            for (int z = 0; z < this.size; z++) {
               this.fillCurrentPositionBlocksDownwards(world, this.deco.accentID, this.deco.accentMeta, x, -1, z, sbb);
            }
         }
      }

      this.nullifySkyLightForBoundingBox(world);
      int totalFloors = this.height / 5;
      boolean beamMaze = decoRNG.nextBoolean();
      int centerFloors = beamMaze ? 4 : totalFloors / 2;
      int bottomFloors = (totalFloors - centerFloors) / 2;
      centerFloors = totalFloors - bottomFloors * 2;
      int topFloorsStartY = this.height - (bottomFloors * 5 + 1);
      this.addThreeQuarterFloors(world, decoRNG, sbb, 0, bottomFloors * 5);
      if (this.getComponentType() < 2) {
         this.addThreeQuarterFloors(world, decoRNG, sbb, topFloorsStartY, this.height - 1);
      } else {
         this.addThreeQuarterFloorsDecorateBoss(world, decoRNG, sbb, topFloorsStartY, this.height - 1);
         this.destroyTower(world, decoRNG, 12, this.height + 4, 3, 4, sbb);
         this.destroyTower(world, decoRNG, 3, this.height + 4, 12, 4, sbb);
         this.destroyTower(world, decoRNG, 3, this.height + 4, 3, 4, sbb);
         this.destroyTower(world, decoRNG, 12, this.height + 4, 12, 4, sbb);
         this.destroyTower(world, decoRNG, 8, this.height + 4, 8, 5, sbb);
         this.decorateBossSpawner(world, decoRNG, sbb, 0, this.height - 6);
      }

      if (beamMaze) {
         this.addTimberMaze(world, decoRNG, sbb, bottomFloors * 5, topFloorsStartY);
      } else {
         this.addBuilderPlatforms(world, decoRNG, sbb, bottomFloors * 5, topFloorsStartY);
      }

      this.makeOpenings(world, sbb);
      return true;
   }

   protected void addThreeQuarterFloors(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int bottom, int top) {
      int spacing = 5;
      int rotation = (this.boundingBox.minY + bottom) % 4;
      if (bottom == 0) {
         this.makeLargeStairsUp(world, sbb, rotation, 0);
         int var12 = rotation + 3;
         rotation = var12 % 4;
         this.makeBottomEntrance(world, decoRNG, sbb, rotation, bottom);
         bottom += spacing;
      }

      for (int y = bottom; y < top; y += spacing) {
         boolean isBottomFloor = y == bottom && bottom != spacing;
         boolean isTopFloor = y >= top - spacing;
         boolean isTowerTopFloor = y >= this.height - spacing - 2;
         this.makeThreeQuarterFloor(world, sbb, rotation, y, isBottomFloor, isTowerTopFloor);
         if (!isTopFloor) {
            this.makeLargeStairsUp(world, sbb, rotation, y);
         }

         if (!isTopFloor || isTowerTopFloor) {
            this.decorateFloor(world, decoRNG, sbb, rotation, y, isBottomFloor, isTopFloor);
         }

         rotation += 3;
         rotation %= 4;
      }
   }

   protected void addThreeQuarterFloorsDecorateBoss(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int bottom, int top) {
      int spacing = 5;
      int rotation = (this.boundingBox.minY + bottom) % 4;
      if (bottom == 0) {
         this.makeLargeStairsUp(world, sbb, rotation, 0);
         rotation += 3;
         rotation %= 4;
         bottom += spacing;
      }

      for (int y = bottom; y < top; y += spacing) {
         boolean isBottomFloor = y == bottom && bottom != spacing;
         boolean isTopFloor = y >= top - spacing;
         boolean isTowerTopFloor = y >= this.height - spacing - 2;
         this.makeThreeQuarterFloor(world, sbb, rotation, y, isBottomFloor, isTowerTopFloor);
         if (!isTopFloor) {
            this.makeLargeStairsUp(world, sbb, rotation, y);
            this.decorateExperiment(world, decoRNG, sbb, rotation, y);
         }

         rotation += 3;
         rotation %= 4;
      }
   }

   private void decorateFloor(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y, boolean isBottom, boolean isTop) {
      if (isTop) {
         switch (decoRNG.nextInt(3)) {
            case 0:
            default:
               this.decorateAquarium(world, decoRNG, sbb, rotation, y);
               break;
            case 1:
               this.decorateBotanical(world, decoRNG, sbb, rotation, y);
               break;
            case 2:
               this.decorateNetherwart(world, decoRNG, sbb, rotation, y, isTop);
         }
      } else if (isBottom) {
         switch (decoRNG.nextInt(4)) {
            case 0:
            default:
               this.decorateAquarium(world, decoRNG, sbb, rotation, y);
               break;
            case 1:
               this.decorateBotanical(world, decoRNG, sbb, rotation, y);
               break;
            case 2:
               if (y + this.boundingBox.minY > 64) {
                  this.decorateNetherwart(world, decoRNG, sbb, rotation, y, isTop);
                  break;
               }
            case 3:
               this.decorateForge(world, decoRNG, sbb, rotation, y);
         }
      } else {
         switch (decoRNG.nextInt(8)) {
            case 0:
            case 1:
            default:
               this.decorateReappearingMaze(world, decoRNG, sbb, rotation, y);
               break;
            case 2:
               this.decorateUnbuilderMaze(world, decoRNG, sbb, rotation, y);
               break;
            case 3:
               this.decorateAquarium(world, decoRNG, sbb, rotation, y);
               break;
            case 4:
               this.decorateBotanical(world, decoRNG, sbb, rotation, y);
               break;
            case 5:
               if (y + this.boundingBox.minY > 64) {
                  this.decorateNetherwart(world, decoRNG, sbb, rotation, y, isTop);
                  break;
               }
            case 6:
               this.decorateLounge(world, decoRNG, sbb, rotation, y);
               break;
            case 7:
               this.decorateForge(world, decoRNG, sbb, rotation, y);
         }
      }
   }

   protected void makeThreeQuarterFloor(OldWorld world, StructureBoundingBox sbb, int rotation, int y, boolean isBottom, boolean isTowerTopFloor) {
      int half = this.size / 2;
      this.fillBlocksRotated(world, sbb, half + 1, y, 1, this.size - 2, y, half + 1, this.deco.blockID, this.deco.blockMeta, rotation);
      this.fillBlocksRotated(world, sbb, 1, y, half + 1, this.size - 2, y, this.size - 2, this.deco.blockID, this.deco.blockMeta, rotation);
      int startZ = isBottom ? 1 : 3;
      this.fillBlocksRotated(world, sbb, 1, y, half, half, y, half, this.deco.accentID, this.deco.accentMeta, rotation);
      this.fillBlocksRotated(world, sbb, half, y, startZ, half, y, half, this.deco.accentID, this.deco.accentMeta, rotation);
      this.fillBlocksRotated(world, sbb, 1, y + 1, half, half, y + 1, half, this.deco.fenceID, this.deco.fenceMeta, rotation);
      this.fillBlocksRotated(world, sbb, half, y + 1, startZ, half, y + 1, half, this.deco.fenceID, this.deco.fenceMeta, rotation);
      if (isTowerTopFloor) {
         this.fillBlocksRotated(world, sbb, 1, y + 0, half - 2, 3, y + 0, half, this.deco.accentID, this.deco.accentMeta, rotation);
         this.fillBlocksRotated(world, sbb, 1, y + 1, half - 2, 3, y + 1, half, this.deco.fenceID, this.deco.fenceMeta, rotation);
         this.fillBlocksRotated(world, sbb, 1, y + 0, half - 1, 2, y + 0, half, this.deco.blockID, this.deco.blockMeta, rotation);
         this.fillBlocksRotated(world, sbb, 1, y + 1, half - 1, 2, y + 1, half, 0, 0, rotation);
      }
   }

   protected void makeLargeStairsUp(OldWorld world, StructureBoundingBox sbb, int rotation, int y) {
      for (int i = 0; i < 5; i++) {
         int z = this.size / 2 - i + 4;
         int sy = y + i + 1;
         this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), 1, sy, z, rotation, sbb);
         this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), 2, sy, z, rotation, sbb);
         this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, 1, sy, z - 1, rotation, sbb);
         this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, 2, sy, z - 1, rotation, sbb);
         this.placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, 3, sy, z - 1, rotation, sbb);
         if (i > 0 && i < 4) {
            this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 3, sy, z, rotation, sbb);
            this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 3, sy + 1, z, rotation, sbb);
            this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 3, sy + 2, z, rotation, sbb);
         } else if (i == 0) {
            this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation), 3, sy, z, rotation, sbb);
         }
      }
   }

   private void decorateReappearingMaze(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y) {
      int mazeSize = 6;
      TFMaze maze = new TFMaze(mazeSize, mazeSize);
      maze.setSeed(world.seed() + this.boundingBox.minX * 90342903 + y * 90342903 ^ this.boundingBox.minZ);

      for (int i = 0; i < 13; i++) {
         maze.putRaw(i, 0, 5);
         maze.putRaw(i, 12, 5);
         maze.putRaw(0, i, 5);
         maze.putRaw(12, i, 5);
      }

      maze.doorRarity = 0.3F;
      switch (rotation) {
         case 0:
            for (int x = 1; x < 6; x++) {
               for (int z = 1; z < 6; z++) {
                  maze.putRaw(x, z, 5);
               }
            }

            maze.putRaw(1, 6, 5);
            maze.putRaw(1, 7, 5);
            maze.putRaw(1, 8, 5);
            maze.putRaw(1, 9, 5);
            maze.putRaw(1, 10, 6);
            maze.putRaw(6, 1, 5);
            maze.putRaw(7, 1, 5);
            maze.putRaw(8, 1, 6);
            maze.generateRecursiveBacktracker(0, 5);
            break;
         case 1:
            for (int x = 7; x < 12; x++) {
               for (int z = 1; z < 6; z++) {
                  maze.putRaw(x, z, 5);
               }
            }

            maze.putRaw(6, 1, 5);
            maze.putRaw(5, 1, 5);
            maze.putRaw(4, 1, 5);
            maze.putRaw(3, 1, 5);
            maze.putRaw(2, 1, 6);
            maze.putRaw(11, 6, 5);
            maze.putRaw(11, 7, 5);
            maze.putRaw(11, 8, 6);
            maze.generateRecursiveBacktracker(0, 0);
            break;
         case 2:
            for (int x = 7; x < 12; x++) {
               for (int z = 7; z < 12; z++) {
                  maze.putRaw(x, z, 5);
               }
            }

            maze.putRaw(11, 6, 5);
            maze.putRaw(11, 5, 5);
            maze.putRaw(11, 4, 5);
            maze.putRaw(11, 3, 5);
            maze.putRaw(11, 2, 6);
            maze.putRaw(6, 11, 5);
            maze.putRaw(5, 11, 5);
            maze.putRaw(4, 11, 6);
            maze.generateRecursiveBacktracker(5, 0);
            break;
         case 3:
            for (int x = 1; x < 6; x++) {
               for (int z = 7; z < 12; z++) {
                  maze.putRaw(x, z, 5);
               }
            }

            maze.putRaw(6, 11, 5);
            maze.putRaw(7, 11, 5);
            maze.putRaw(8, 11, 5);
            maze.putRaw(9, 11, 5);
            maze.putRaw(10, 11, 6);
            maze.putRaw(1, 6, 5);
            maze.putRaw(1, 5, 5);
            maze.putRaw(1, 4, 6);
            maze.generateRecursiveBacktracker(5, 5);
      }

      maze.wallBlockID = this.deco.blockID;
      maze.wallBlockMeta = this.deco.blockMeta;
      maze.headBlockID = this.deco.accentID;
      maze.headBlockMeta = this.deco.accentMeta;
      maze.pillarBlockID = this.deco.accentID;
      maze.pillarBlockMeta = this.deco.accentMeta;
      maze.doorBlockID = 1370;
      maze.doorBlockMeta = 0;
      maze.torchRarity = 0.0F;
      maze.tall = 3;
      maze.head = 1;
      maze.oddBias = 2;
      maze.copyToStructure(world, 0, y + 1, 0, this, sbb);
      this.decorateMazeDeadEnds(world, decoRNG, maze, y, rotation, sbb);
   }

   protected void decorateMazeDeadEnds(OldWorld world, Random decoRNG, TFMaze maze, int y, int rotation, StructureBoundingBox sbb) {
      for (int x = 0; x < maze.width; x++) {
         for (int z = 0; z < maze.depth; z++) {
            if (!maze.isWall(x, z, x - 1, z) && maze.isWall(x, z, x + 1, z) && maze.isWall(x, z, x, z - 1) && maze.isWall(x, z, x, z + 1)) {
               this.decorateDeadEnd(world, decoRNG, maze, x, y, z, 3, rotation, sbb);
            }

            if (maze.isWall(x, z, x - 1, z) && !maze.isWall(x, z, x + 1, z) && maze.isWall(x, z, x, z - 1) && maze.isWall(x, z, x, z + 1)) {
               this.decorateDeadEnd(world, decoRNG, maze, x, y, z, 1, rotation, sbb);
            }

            if (maze.isWall(x, z, x - 1, z) && maze.isWall(x, z, x + 1, z) && !maze.isWall(x, z, x, z - 1) && maze.isWall(x, z, x, z + 1)) {
               this.decorateDeadEnd(world, decoRNG, maze, x, y, z, 0, rotation, sbb);
            }

            if (maze.isWall(x, z, x - 1, z) && maze.isWall(x, z, x + 1, z) && maze.isWall(x, z, x, z - 1) && !maze.isWall(x, z, x, z + 1)) {
               this.decorateDeadEnd(world, decoRNG, maze, x, y, z, 2, rotation, sbb);
            }
         }
      }
   }

   private void decorateDeadEnd(OldWorld world, Random decoRNG, TFMaze maze, int mx, int y, int mz, int facing, int rotation, StructureBoundingBox sbb) {
      int x = mx * 3 + 1;
      int z = mz * 3 + 1;
      switch (facing) {
         case 0:
            this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x + 0, y + 1, z + 1, sbb);
            this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 1, z + 1, sbb);
            this.placeBlockAtCurrentPosition(world, 54, 0, x + 0, y + 2, z + 1, sbb);
            this.placeTreasureAtCurrentPosition(world, decoRNG, x + 1, y + 2, z + 1, TFTreasure.DARKTOWER_CACHE, sbb);
            break;
         case 1:
            this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x + 0, y + 1, z + 0, sbb);
            this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x + 0, y + 1, z + 1, sbb);
            this.placeBlockAtCurrentPosition(world, 54, rotation, x + 0, y + 2, z + 0, sbb);
            this.placeTreasureAtCurrentPosition(world, decoRNG, x + 0, y + 2, z + 1, TFTreasure.DARKTOWER_CACHE, sbb);
            break;
         case 2:
            this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x + 0, y + 1, z + 0, sbb);
            this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 1, z + 0, sbb);
            this.placeBlockAtCurrentPosition(world, 54, rotation, x + 0, y + 2, z + 0, sbb);
            this.placeTreasureAtCurrentPosition(world, decoRNG, x + 1, y + 2, z + 0, TFTreasure.DARKTOWER_CACHE, sbb);
            break;
         case 3:
            this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 1, z + 0, sbb);
            this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 1, z + 1, sbb);
            this.placeBlockAtCurrentPosition(world, 54, rotation, x + 1, y + 2, z + 0, sbb);
            this.placeTreasureAtCurrentPosition(world, decoRNG, x + 1, y + 2, z + 1, TFTreasure.DARKTOWER_CACHE, sbb);
      }
   }

   private void decorateUnbuilderMaze(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y) {
      for (int x = this.size / 2; x < this.size - 1; x++) {
         for (int z = 3; z < this.size - 1; z++) {
            if (x % 2 == 1 && z % 2 == 1) {
               for (int py = 1; py < 5; py++) {
                  this.placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, x, y + py, z, rotation, sbb);
               }
            } else if (x % 2 == 1 || z % 2 == 1) {
               for (int py = 1; py < 5; py++) {
                  this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, x, y + py, z, rotation, sbb);
               }

               if (x != this.size / 2 && x != this.size - 2 && z != this.size - 2) {
                  int ay = decoRNG.nextInt(4) + 1;
                  this.placeBlockRotated(world, 0, 0, x, y + ay, z, rotation, sbb);
                  if (x > this.size - 7) {
                     ay = decoRNG.nextInt(3) + 1;
                     this.placeBlockRotated(world, 0, 0, x, y + ay, z, rotation, sbb);
                  }
               }
            }
         }
      }

      this.placeBlockRotated(world, 1370, 9, 15, y + 2, 7, rotation, sbb);
      this.placeBlockRotated(world, 1370, 9, 11, y + 3, 7, rotation, sbb);
      this.placeBlockRotated(world, 1370, 9, 15, y + 2, 13, rotation, sbb);
      this.placeBlockRotated(world, 1370, 9, 11, y + 3, 13, rotation, sbb);
      this.placeBlockRotated(world, 1370, 9, 5, y + 3, 13, rotation, sbb);
   }

   private void decorateLounge(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y) {
      this.fillBlocksRotated(world, sbb, 17, y + 1, 1, 17, y + 4, 6, this.deco.pillarID, this.deco.pillarMeta, rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 1, 17, y + 4, 1, this.deco.pillarID, this.deco.pillarMeta, rotation);
      this.fillBlocksRotated(world, sbb, 13, y + 1, 2, 16, y + 1, 5, this.deco.blockID, this.deco.blockMeta, rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 2, 12, y + 1, 6, this.deco.stairID, this.getStairMeta(0 + rotation), rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 6, 16, y + 1, 6, this.deco.stairID, this.getStairMeta(3 + rotation), rotation);
      this.makeDispenserPillar(world, this.deco, 13, y, 1, this.getStairMeta(3 + rotation), rotation, sbb);
      this.makeDispenserPillar(world, this.deco, 15, y, 1, this.getStairMeta(3 + rotation), rotation, sbb);
      this.makeDispenserPillar(world, this.deco, 17, y, 3, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeDispenserPillar(world, this.deco, 17, y, 5, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeStonePillar(world, this.deco, 12, y, 1, this.getStairMeta(3 + rotation), rotation, sbb);
      this.makeStonePillar(world, this.deco, 17, y, 6, this.getStairMeta(0 + rotation), rotation, sbb);
      this.placeBlockRotated(world, 117, 0, 13, y + 2, 5, rotation, sbb);
      this.placeBlockRotated(world, 118, 3, 15, y + 2, 3, rotation, sbb);
      this.fillBlocksRotated(world, sbb, 10, y + 1, 17, 17, y + 4, 17, this.deco.blockID, this.deco.blockMeta, rotation);
      this.fillBlocksRotated(world, sbb, 17, y + 1, 10, 17, y + 4, 17, this.deco.blockID, this.deco.blockMeta, rotation);
      this.fillBlocksRotated(world, sbb, 11, y + 1, 17, 12, y + 4, 17, 47, 0, rotation);
      this.fillBlocksRotated(world, sbb, 14, y + 1, 17, 15, y + 4, 17, 47, 0, rotation);
      this.fillBlocksRotated(world, sbb, 17, y + 1, 11, 17, y + 4, 12, 47, 0, rotation);
      this.fillBlocksRotated(world, sbb, 17, y + 1, 14, 17, y + 4, 15, 47, 0, rotation);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation) + 4, 13, y + 1, 14, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation) + 4, 14, y + 1, 14, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation) + 4, 14, y + 1, 13, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation) + 4, 13, y + 1, 13, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation), 11, y + 1, 13, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), 13, y + 1, 11, rotation, sbb);
      this.placeBlockRotated(world, 123, 0, 8, y + 3, 8, rotation, sbb);
      this.placeBlockRotated(world, 69, decoRNG.nextBoolean() ? 0 : 7, 8, y + 2, 8, rotation, sbb);
      this.placeTreePlanter(world, decoRNG.nextInt(5), 6, y + 1, 12, rotation, sbb);
   }

   private void makeDispenserPillar(OldWorld world, StructureTFDecorator forgeDeco, int x, int y, int z, int stairMeta, int rotation, StructureBoundingBox sbb) {
      this.placeBlockRotated(world, forgeDeco.stairID, stairMeta + 4, x, y + 2, z, rotation, sbb);
      this.placeBlockRotated(world, 23, stairMeta + 4, x, y + 3, z, rotation, sbb);
      this.placeBlockRotated(world, forgeDeco.stairID, stairMeta, x, y + 4, z, rotation, sbb);
   }

   private void decorateBossSpawner(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y) {
      this.placeBlockRotated(world, 1351, 3, 9, y + 4, 9, rotation, sbb);
   }

   private void decorateExperiment(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y) {
      this.fillBlocksRotated(world, sbb, 17, y + 1, 1, 17, y + 4, 6, this.deco.pillarID, this.deco.pillarMeta, rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 1, 17, y + 4, 1, this.deco.pillarID, this.deco.pillarMeta, rotation);
      this.fillBlocksRotated(world, sbb, 13, y + 1, 2, 16, y + 1, 5, this.deco.blockID, this.deco.blockMeta, rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 2, 12, y + 1, 6, this.deco.stairID, this.getStairMeta(0 + rotation), rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 6, 16, y + 1, 6, this.deco.stairID, this.getStairMeta(3 + rotation), rotation);
      this.makeWoodPillar(world, this.deco, 13, y, 1, this.getStairMeta(3 + rotation), rotation, sbb);
      this.makeWoodPillar(world, this.deco, 15, y, 1, this.getStairMeta(3 + rotation), rotation, sbb);
      this.makeWoodPillar(world, this.deco, 17, y, 3, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeWoodPillar(world, this.deco, 17, y, 5, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeStonePillar(world, this.deco, 12, y, 1, this.getStairMeta(3 + rotation), rotation, sbb);
      this.makeStonePillar(world, this.deco, 17, y, 6, this.getStairMeta(0 + rotation), rotation, sbb);
      this.placeBlockRotated(world, 58, 0, 14, y + 2, 4, rotation, sbb);
      this.placeItemFrameRotated(world, 13, y + 2, 1, rotation, 0, new OldStack(BORER_ESSENCE, 0, 1), sbb);
      this.placeItemFrameRotated(world, 14, y + 2, 1, rotation, 0, new OldStack(331, 0, 1), sbb);
      this.placeItemFrameRotated(world, 15, y + 2, 1, rotation, 0, new OldStack(BORER_ESSENCE, 0, 1), sbb);
      this.placeItemFrameRotated(world, 13, y + 3, 1, rotation, 0, new OldStack(331, 0, 1), sbb);
      this.placeItemFrameRotated(world, 14, y + 3, 1, rotation, 0, new OldStack(370, 0, 1), sbb);
      this.placeItemFrameRotated(world, 15, y + 3, 1, rotation, 0, new OldStack(331, 0, 1), sbb);
      this.placeItemFrameRotated(world, 13, y + 4, 1, rotation, 0, new OldStack(BORER_ESSENCE, 0, 1), sbb);
      this.placeItemFrameRotated(world, 14, y + 4, 1, rotation, 0, new OldStack(331, 0, 1), sbb);
      this.placeItemFrameRotated(world, 15, y + 4, 1, rotation, 0, new OldStack(BORER_ESSENCE, 0, 1), sbb);
      this.placeItemFrameRotated(world, 17, y + 2, 3, rotation, 1, new OldStack(1372, 1, 1), sbb);
      this.placeItemFrameRotated(world, 17, y + 2, 4, rotation, 1, new OldStack(1372, 0, 1), sbb);
      this.placeItemFrameRotated(world, 17, y + 2, 5, rotation, 1, new OldStack(1372, 1, 1), sbb);
      this.placeItemFrameRotated(world, 17, y + 3, 3, rotation, 1, new OldStack(1372, 0, 1), sbb);
      this.placeItemFrameRotated(world, 17, y + 3, 4, rotation, 1, new OldStack(CARMINITE, 0, 1), sbb);
      this.placeItemFrameRotated(world, 17, y + 3, 5, rotation, 1, new OldStack(1372, 0, 1), sbb);
      this.placeItemFrameRotated(world, 17, y + 4, 3, rotation, 1, new OldStack(1372, 1, 1), sbb);
      this.placeItemFrameRotated(world, 17, y + 4, 4, rotation, 1, new OldStack(1372, 0, 1), sbb);
      this.placeItemFrameRotated(world, 17, y + 4, 5, rotation, 1, new OldStack(1372, 1, 1), sbb);
      if (y < this.height - 13) {
         this.placeBlockRotated(world, 49, 0, 13, y + 1, 13, rotation, sbb);
         this.placeBlockRotated(world, 49, 0, 15, y + 1, 13, rotation, sbb);
         this.placeBlockRotated(world, 49, 0, 13, y + 1, 15, rotation, sbb);
         this.placeBlockRotated(world, 49, 0, 15, y + 1, 15, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 13, y + 1, 14, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 14, y + 1, 13, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 15, y + 1, 14, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 14, y + 1, 15, rotation, sbb);
         this.placeBlockRotated(world, 152, 0, 14, y + 1, 14, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 13, y + 2, 13, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 15, y + 2, 13, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 13, y + 2, 15, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 15, y + 2, 15, rotation, sbb);
         this.placeBlockRotated(world, 1370, 12, 14, y + 2, 14, rotation, sbb);
         this.placeBlockRotated(world, 49, 0, 13, y + 3, 13, rotation, sbb);
         this.placeBlockRotated(world, 49, 0, 15, y + 3, 13, rotation, sbb);
         this.placeBlockRotated(world, 49, 0, 13, y + 3, 15, rotation, sbb);
         this.placeBlockRotated(world, 49, 0, 15, y + 3, 15, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 13, y + 3, 14, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 14, y + 3, 13, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 15, y + 3, 14, rotation, sbb);
         this.placeBlockRotated(world, 87, 0, 14, y + 3, 15, rotation, sbb);
         this.placeBlockRotated(world, 152, 0, 14, y + 3, 14, rotation, sbb);
      }

      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 14, y + 1, 17, rotation, sbb);
      this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, 4), 13, y + 1, 17, rotation, sbb);
      this.placeBlockRotated(world, 33, 5 - this.getStairMeta(3 + rotation), 14, y + 2, 17, rotation, sbb);
      this.placeBlockRotated(world, 152, 0, 14, y + 2, 16, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 17, y + 1, 14, rotation, sbb);
      this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, 2), 17, y + 1, 13, rotation, sbb);
      this.placeBlockRotated(world, 33, 5 - this.getStairMeta(2 + rotation), 17, y + 2, 14, rotation, sbb);
      this.placeBlockRotated(world, 152, 0, 16, y + 2, 14, rotation, sbb);
      this.placeBlockRotated(world, 152, 0, 14, y + 2, 11, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 14, y + 1, 11, rotation, sbb);
      this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, 4) + 8, 13, y + 1, 11, rotation, sbb);
      this.placeBlockRotated(world, 33, 5 - this.getStairMeta(1 + rotation), 14, y + 2, 10, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 14, y + 1, 9, rotation, sbb);
      this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, 4), 13, y + 1, 9, rotation, sbb);
      this.placeBlockRotated(world, 29, 5 - this.getStairMeta(1 + rotation), 14, y + 2, 9, rotation, sbb);
      this.placeBlockRotated(world, 152, 0, 11, y + 2, 14, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 11, y + 1, 14, rotation, sbb);
      this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, 2) + 8, 11, y + 1, 13, rotation, sbb);
      this.placeBlockRotated(world, 33, 5 - this.getStairMeta(0 + rotation), 10, y + 2, 14, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 9, y + 1, 14, rotation, sbb);
      this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, 2), 9, y + 1, 13, rotation, sbb);
      this.placeBlockRotated(world, 29, 5 - this.getStairMeta(0 + rotation), 9, y + 2, 14, rotation, sbb);
   }

   private void makeWoodPillar(OldWorld world, StructureTFDecorator forgeDeco, int x, int y, int z, int stairMeta, int rotation, StructureBoundingBox sbb) {
      this.placeBlockRotated(world, 1358, 3, x, y + 2, z, rotation, sbb);
      this.placeBlockRotated(world, 1358, 3, x, y + 3, z, rotation, sbb);
      this.placeBlockRotated(world, 1358, 3, x, y + 4, z, rotation, sbb);
   }

   private void placeItemFrameRotated(OldWorld world, int x, int y, int z, int rotation, int direction, OldStack itemStack, StructureBoundingBox sbb) {
      int dx = this.getXWithOffsetAsIfRotated(x, z, rotation);
      int dy = this.getYWithOffset(y);
      int dz = this.getZWithOffsetAsIfRotated(x, z, rotation);
      if (sbb.isVecInside(dx, dy, dz)) {
         // EntityItemFrame(world, dx, dy, dz, direction): spawnEntityInWorld does not check the wall
         world.spawnEntity(new OldEntity.ItemFrame(dx, dy, dz, (this.getCoordBaseMode() + direction + rotation) % 4, itemStack, 0));
      }
   }

   private void decorateAquarium(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y) {
      this.makePillarFrame(world, sbb, this.deco, rotation, 12, y, 3, 4, 4, 13, false);
      this.fillBlocksRotated(world, sbb, 13, y + 4, 4, 14, y + 4, 14, 8, 0, rotation);
      this.makePillarFrame(world, sbb, this.deco, rotation, 6, y, 12, 4, 4, 4, false);
      this.fillBlocksRotated(world, sbb, 6, y + 5, 12, 9, y + 5, 15, this.deco.accentID, this.deco.accentMeta, rotation);
      this.fillBlocksRotated(world, sbb, 7, y + 4, 13, 8, y + 5, 14, 8, 0, rotation);
   }

   private void decorateForge(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y) {
      StructureTFDecorator forgeDeco = this.deco;
      this.fillBlocksRotated(world, sbb, 17, y + 1, 1, 17, y + 4, 6, forgeDeco.pillarID, forgeDeco.pillarMeta, rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 1, 17, y + 4, 1, forgeDeco.pillarID, forgeDeco.pillarMeta, rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 17, 17, y + 4, 17, forgeDeco.pillarID, forgeDeco.pillarMeta, rotation);
      this.fillBlocksRotated(world, sbb, 17, y + 1, 12, 17, y + 4, 17, forgeDeco.pillarID, forgeDeco.pillarMeta, rotation);
      this.fillBlocksRotated(world, sbb, 13, y + 1, 2, 16, y + 1, 5, forgeDeco.blockID, forgeDeco.blockMeta, rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 2, 12, y + 1, 6, forgeDeco.stairID, this.getStairMeta(0 + rotation), rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 6, 16, y + 1, 6, forgeDeco.stairID, this.getStairMeta(3 + rotation), rotation);
      this.fillBlocksRotated(world, sbb, 13, y + 1, 13, 16, y + 1, 16, forgeDeco.blockID, forgeDeco.blockMeta, rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 12, 12, y + 1, 16, forgeDeco.stairID, this.getStairMeta(0 + rotation), rotation);
      this.fillBlocksRotated(world, sbb, 12, y + 1, 12, 16, y + 1, 12, forgeDeco.stairID, this.getStairMeta(1 + rotation), rotation);
      this.makeFurnacePillar(world, forgeDeco, decoRNG, 13, y, 1, this.getStairMeta(3 + rotation), rotation, sbb);
      this.makeFurnacePillar(world, forgeDeco, decoRNG, 15, y, 1, this.getStairMeta(3 + rotation), rotation, sbb);
      this.makeFurnacePillar(world, forgeDeco, decoRNG, 17, y, 3, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeFurnacePillar(world, forgeDeco, decoRNG, 17, y, 5, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeFurnacePillar(world, forgeDeco, decoRNG, 13, y, 17, this.getStairMeta(1 + rotation), rotation, sbb);
      this.makeFurnacePillar(world, forgeDeco, decoRNG, 15, y, 17, this.getStairMeta(1 + rotation), rotation, sbb);
      this.makeFurnacePillar(world, forgeDeco, decoRNG, 17, y, 13, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeFurnacePillar(world, forgeDeco, decoRNG, 17, y, 15, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeStonePillar(world, forgeDeco, 12, y, 1, this.getStairMeta(3 + rotation), rotation, sbb);
      this.makeStonePillar(world, forgeDeco, 17, y, 6, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeStonePillar(world, forgeDeco, 12, y, 17, this.getStairMeta(1 + rotation), rotation, sbb);
      this.makeStonePillar(world, forgeDeco, 17, y, 12, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeStonePillar(world, forgeDeco, 17, y, 9, this.getStairMeta(0 + rotation), rotation, sbb);
      this.makeStonePillar(world, forgeDeco, 9, y, 17, this.getStairMeta(1 + rotation), rotation, sbb);
      this.placeBlockRotated(world, 145, decoRNG.nextInt(16), 13, y + 2, 5, rotation, sbb);
      this.placeBlockRotated(world, 145, decoRNG.nextInt(16), 13, y + 2, 13, rotation, sbb);
      this.makeFirePit(world, forgeDeco, 6, y + 1, 12, rotation, sbb);
   }

   private void makeFurnacePillar(
      OldWorld world, StructureTFDecorator forgeDeco, Random rand, int x, int y, int z, int stairMeta, int rotation, StructureBoundingBox sbb
   ) {
      this.placeBlockRotated(world, forgeDeco.stairID, stairMeta + 4, x, y + 2, z, rotation, sbb);
      this.placeBlockRotated(world, 61, stairMeta + 4, x, y + 3, z, rotation, sbb);
      int amount = rand.nextBoolean() ? rand.nextInt(5) + 4 : 0;
      if (amount > 0) {
         int dx = this.getXWithOffsetAsIfRotated(x, z, rotation);
         int dy = this.getYWithOffset(y + 3);
         int dz = this.getZWithOffsetAsIfRotated(x, z, rotation);
         if (sbb.isVecInside(dx, dy, dz) && world.getBlockId(dx, dy, dz) == 61) {
            // TileEntityFurnace slot 1 (fuel): charcoal
            OldTile.Furnace furnace = world.getTile(dx, dy, dz) instanceof OldTile.Furnace f ? f : new OldTile.Furnace();
            furnace.slots()[1] = new OldStack(263, 1, amount);
            world.setTile(dx, dy, dz, furnace);
         }
      }

      this.placeBlockRotated(world, forgeDeco.stairID, stairMeta, x, y + 4, z, rotation, sbb);
   }

   private void makeStonePillar(OldWorld world, StructureTFDecorator forgeDeco, int x, int y, int z, int stairMeta, int rotation, StructureBoundingBox sbb) {
      for (int py = 1; py <= 4; py++) {
         this.placeBlockRotated(world, forgeDeco.pillarID, forgeDeco.pillarMeta, x, y + py, z, rotation, sbb);
      }

      int sx = this.getXWithOffsetAsIfRotated(x, z, rotation);
      int sy = this.getYWithOffset(y + 1);
      int sz = this.getZWithOffsetAsIfRotated(x, z, rotation);
      switch (stairMeta) {
         case 0:
            sx--;
            break;
         case 1:
            sx++;
            break;
         case 2:
            sz--;
            break;
         case 3:
            sz++;
      }

      if (sbb.isVecInside(sx, sy, sz)) {
         world.setBlock(sx, sy + 0, sz, forgeDeco.stairID, stairMeta);
         world.setBlock(sx, sy + 3, sz, forgeDeco.stairID, stairMeta + 4);
      }
   }

   private void makeFirePit(OldWorld world, StructureTFDecorator myDeco, int x, int y, int z, int rotation, StructureBoundingBox sbb) {
      this.placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x + 1, y, z + 1, rotation, sbb);
      this.placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x + 1, y, z - 1, rotation, sbb);
      this.placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x - 1, y, z + 1, rotation, sbb);
      this.placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x - 1, y, z - 1, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(0 + rotation), x - 1, y, z + 0, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(2 + rotation), x + 1, y, z + 0, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(3 + rotation), x + 0, y, z + 1, rotation, sbb);
      this.placeBlockRotated(world, myDeco.stairID, this.getStairMeta(1 + rotation), x + 0, y, z - 1, rotation, sbb);
      this.placeBlockRotated(world, 87, 0, x, y, z, rotation, sbb);
      this.placeBlockRotated(world, 51, 0, x, y + 1, z, rotation, sbb);
   }

   private void decorateNetherwart(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y, boolean isTop) {
      StructureTFDecorator netherDeco = this.deco;
      this.makePillarFrame(world, sbb, netherDeco, rotation, 12, y, 9, 4, 4, 7, true);
      this.fillBlocksRotated(world, sbb, 13, y + 1, 10, 14, y + 1, 14, 88, 0, rotation);
      this.fillBlocksRotated(world, sbb, 13, y + 2, 10, 14, y + 2, 14, 115, 0, rotation);
      this.fillBlocksRotated(world, sbb, 13, y + 4, 10, 14, y + 4, 14, 88, 0, rotation);
      this.makePillarFrame(world, sbb, netherDeco, rotation, 5, y, 12, 3, isTop ? 4 : 9, 3, true);
      this.placeBlockRotated(world, netherDeco.blockID, netherDeco.blockMeta, 6, y + 1, 13, rotation, sbb);
      this.placeBlockRotated(world, netherDeco.blockID, netherDeco.blockMeta, 6, y + (isTop ? 4 : 9), 13, rotation, sbb);
      this.placeSpawnerRotated(world, 6, y + 3, 13, rotation, "Blaze", sbb);
      this.destroyTower(world, decoRNG, 12, y, 3, 2, sbb);
   }

   private void decorateBotanical(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y) {
      this.makePillarFrame(world, sbb, this.deco, rotation, 12, y, 12, 4, 4, 4, true);
      this.fillBlocksRotated(world, sbb, 13, y + 1, 13, 14, y + 1, 14, this.deco.blockID, this.deco.blockMeta, rotation);
      this.fillBlocksRotated(world, sbb, 13, y + 4, 13, 14, y + 4, 14, this.deco.blockID, this.deco.blockMeta, rotation);
      this.placeRandomPlant(world, decoRNG, 13, y + 2, 13, rotation, sbb);
      this.placeRandomPlant(world, decoRNG, 13, y + 2, 14, rotation, sbb);
      this.placeRandomPlant(world, decoRNG, 14, y + 2, 13, rotation, sbb);
      this.placeRandomPlant(world, decoRNG, 14, y + 2, 14, rotation, sbb);

      for (int py = 1; py <= 4; py++) {
         this.placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, 12, y + py, 4, rotation, sbb);
         this.placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, 15, y + py, 4, rotation, sbb);
      }

      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation) + 4, 13, y + 1, 4, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation) + 4, 14, y + 1, 4, rotation, sbb);
      this.placeTreasureRotated(world, 13, y + 2, 4, rotation, TFTreasure.BASEMENT, sbb);
      this.placeBlockRotated(world, 58, 0, 14, y + 2, 4, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation) + 4, 12, y + 1, 7, rotation, sbb);
      this.placeBlockRotated(world, 126, 9, 13, y + 1, 7, rotation, sbb);
      this.placeBlockRotated(world, 126, 9, 14, y + 1, 7, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation) + 4, 15, y + 1, 7, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation) + 4, 12, y + 1, 10, rotation, sbb);
      this.placeBlockRotated(world, 126, 9, 13, y + 1, 10, rotation, sbb);
      this.placeBlockRotated(world, 126, 9, 14, y + 1, 10, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation) + 4, 15, y + 1, 10, rotation, sbb);

      for (int x = 12; x <= 15; x++) {
         this.placeRandomPlant(world, decoRNG, x, y + 2, 7, rotation, sbb);
         this.placeRandomPlant(world, decoRNG, x, y + 2, 10, rotation, sbb);
      }

      this.placeTreePlanter(world, decoRNG.nextInt(5), 6, y + 1, 12, rotation, sbb);
   }

   private void placeTreePlanter(OldWorld world, int treeNum, int x, int y, int z, int rotation, StructureBoundingBox sbb) {
      this.placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, x + 1, y, z + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, x + 1, y, z - 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, x - 1, y, z + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, x - 1, y, z - 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(0 + rotation), x - 1, y, z + 0, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(2 + rotation), x + 1, y, z + 0, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(3 + rotation), x + 0, y, z + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.stairID, this.getStairMeta(1 + rotation), x + 0, y, z - 1, rotation, sbb);
      this.placeBlockRotated(world, 3, 0, x, y, z, rotation, sbb);
      int dx = this.getXWithOffsetAsIfRotated(x, z, rotation);
      int dy = this.getYWithOffset(y + 1);
      int dz = this.getZWithOffsetAsIfRotated(x, z, rotation);
      if (sbb.isVecInside(dx, dy, dz)) {
         hexxit.worldgen152.core.populate.OldGen treeGen;
         switch (treeNum) {
            case 0:
            default:
               treeGen = new hexxit.worldgen152.core.populate.OldTreeGens.Trees();
               break;
            case 1:
               treeGen = new hexxit.worldgen152.core.populate.OldTreeGens.Trees(3, 3, 3, false);
               break;
            case 2:
               treeGen = new hexxit.worldgen152.core.populate.OldTreeGens.Birch();
               break;
            case 3:
               treeGen = new TFTrees.SmallOak(0);
               break;
            case 4:
               treeGen = new TFTrees.SmallOak(3);
         }

         int i = 0;
         // world.rand of the original is not seed-fixed: a random from seed and position (ComponentTFTowerWing.worldRand)
         Random treeRand = ComponentTFTowerWing.worldRand(world, dx, dy, dz);

         while (i < 100 && !treeGen.generate(world, treeRand, dx, dy, dz)) {
            i++;
         }
      }
   }

   private void placeRandomPlant(OldWorld world, Random decoRNG, int x, int y, int z, int rotation, StructureBoundingBox sbb) {
      int potMeta = decoRNG.nextInt(12);
      this.placeBlockRotated(world, 140, potMeta, x, y, z, rotation, sbb);
   }

   private void makeBottomEntrance(OldWorld world, Random decoRNG, StructureBoundingBox sbb, int rotation, int y) {
      this.makeFirePit(world, this.deco, 13, y + 1, 3, rotation, sbb);
      this.makeFirePit(world, this.deco, 3, y + 1, 13, rotation, sbb);
      this.makeFirePit(world, this.deco, 13, y + 1, 13, rotation, sbb);
      this.makePillarFrame(world, sbb, this.deco, rotation, 7, y, 7, 3, 4, 3, false);
   }

   protected void addTimberMaze(OldWorld world, Random rand, StructureBoundingBox sbb, int bottom, int top) {
      int spacing = 5;
      int floorside = 0;
      if (bottom == 0) {
         bottom += spacing;
      }

      for (int y = bottom; y < top; y += spacing) {
         floorside = ++floorside % 4;
         this.makeTimberBeams(world, rand, sbb, floorside, y, y == bottom && bottom != spacing, y >= top - spacing, top);
      }
   }

   protected void makeTimberBeams(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y, boolean isBottom, boolean isTop, int top) {
      int beamID = 1358;
      int beamMetaBase = 3;
      int beamMetaNS = (this.coordBaseMode + rotation) % 2 == 0 ? 4 : 8;
      int beamMetaEW = beamMetaNS == 4 ? 8 : 4;
      int beamMetaUD = 0;

      for (int z = 1; z < this.size - 1; z++) {
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaEW, 4, y, z, rotation, sbb);
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaEW, 9, y, z, rotation, sbb);
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaEW, 14, y, z, rotation, sbb);
      }

      int z = this.pickBetweenExcluding(3, this.size - 3, rand, 4, 9, 14);

      for (int x = 5; x < 9; x++) {
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaNS, x, y, z, rotation, sbb);
      }

      z = this.pickBetweenExcluding(3, this.size - 3, rand, 4, 9, 14);

      for (int x = 10; x < 14; x++) {
         this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaNS, x, y, z, rotation, sbb);
      }

      int x1 = 4;
      int z1 = this.pickFrom(rand, 4, 9, 14);
      int x2 = 9;
      int z2 = this.pickFrom(rand, 4, 9, 14);
      int x3 = 14;
      int z3 = this.pickFrom(rand, 4, 9, 14);

      for (int by = 1; by < 5; by++) {
         if (!isBottom || this.checkPost(world, x1, y - 5, z1, rotation, sbb)) {
            this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaUD, x1, y - by, z1, rotation, sbb);
            this.placeBlockRotated(world, 65, this.getLadderMeta(2, rotation), x1 + 1, y - by, z1, rotation, sbb);
         }

         if (!isBottom || this.checkPost(world, x2, y - 5, z2, rotation, sbb)) {
            this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaUD, x2, y - by, z2, rotation, sbb);
         }

         if (!isBottom || this.checkPost(world, x3, y - 5, z3, rotation, sbb)) {
            this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaUD, x3, y - by, z3, rotation, sbb);
            this.placeBlockRotated(world, 65, this.getLadderMeta(4, rotation), x3 - 1, y - by, z3, rotation, sbb);
         }
      }

      if (isTop) {
         int topFloorRotation = (this.boundingBox.minY + top + 1) % 4;
         int ladderX = 4;
         int ladderZ = 10;
         int ladderMeta = 3;

         for (int by = 1; by < 5; by++) {
            this.placeBlockRotated(world, beamID, beamMetaBase + beamMetaUD, ladderX, y + by, 9, topFloorRotation, sbb);
            this.placeBlockRotated(
               world, 65, this.getLadderMeta(ladderMeta, topFloorRotation), ladderX, y + by, ladderZ, topFloorRotation, sbb
            );
         }

         this.placeBlockRotated(world, 0, 0, ladderX, y + 6, 9, topFloorRotation, sbb);
         this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, ladderX + 1, y + 5, ladderZ, topFloorRotation, sbb);
         this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, ladderX - 1, y + 5, ladderZ, topFloorRotation, sbb);
         this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, ladderX + 1, y + 6, ladderZ, topFloorRotation, sbb);
         this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, ladderX - 1, y + 6, ladderZ, topFloorRotation, sbb);
      }

      if (!isBottom && !isTop) {
         int sx = this.pickFrom(rand, 6, 7, 11);
         int sz = this.pickFrom(rand, 6, 11, 12);
         this.makeMiniGhastSpawner(world, rand, y, sx, sz, sbb);
      }

      int lx = this.pickFrom(rand, 2, 12, 16);
      int lz = 2 + rand.nextInt(15);
      this.placeBlockRotated(world, 123, 0, lx, y + 2, lz, rotation, sbb);
      this.placeBlockRotated(world, 69, rand.nextBoolean() ? 0 : 7, lx, y + 1, lz, rotation, sbb);
   }

   private void makeMiniGhastSpawner(OldWorld world, Random rand, int y, int sx, int sz, StructureBoundingBox sbb) {
      OldTile.Spawner spawner = this.placeSpawnerAtCurrentPosition(world, rand, sx, y + 2, sz, "TwilightForest.Mini Ghast", sbb);
      if (spawner != null) {
         // SpawnRange 16, MaxNearbyEntities = world.difficultySetting (1, easy: the default of server.properties), SpawnCount 1
         int dx = this.getXWithOffset(sx, sz);
         int dy = this.getYWithOffset(y + 2);
         int dz = this.getZWithOffset(sx, sz);
         world.setTile(dx, dy, dz, spawner.with(16, 1, 1));
      }
   }

   protected void addBuilderPlatforms(OldWorld world, Random rand, StructureBoundingBox sbb, int bottom, int top) {
      int spacing = 5;
      int floorside = 0;
      if (bottom == 0) {
         bottom += spacing;
      }

      for (int y = bottom; y < top - spacing; y += spacing) {
         this.makeBuilderPlatforms(world, rand, sbb, floorside, y, y == bottom && bottom != spacing, y >= top - spacing);
         floorside += 1 + rand.nextInt(3);
         floorside %= 4;
      }

      this.makeBuilderPlatform(world, rand, 1, bottom, 5, true, sbb);
      this.makeBuilderPlatform(world, rand, 3, bottom, 5, true, sbb);

      for (int y = bottom - 4; y < bottom; y++) {
         this.placeBlockRotated(world, 65, this.getLadderMeta(2, 1), 1, y, 5, 1, sbb);
         this.placeBlockRotated(world, 65, this.getLadderMeta(2, 3), 1, y, 5, 3, sbb);
      }

      this.addTopBuilderPlatform(world, rand, bottom, top, spacing, sbb);
   }

   protected void makeBuilderPlatforms(OldWorld world, Random rand, StructureBoundingBox sbb, int rotation, int y, boolean bottom, boolean top) {
      int z = this.size / 2 + rand.nextInt(5) - rand.nextInt(5);
      this.makeBuilderPlatform(world, rand, rotation, y, z, false, sbb);
      this.placeBlockRotated(world, 65, this.getLadderMeta(2, rotation), 1, y + 1, z, rotation, sbb);
      this.placeBlockRotated(world, 65, this.getLadderMeta(2, rotation), 1, y + 2, z, rotation, sbb);
      this.placeBlockRotated(world, 65, this.getLadderMeta(2, rotation), 1, y + 3, z, rotation, sbb);
      this.placeBlockRotated(world, 65, this.getLadderMeta(2, rotation), 1, y + 4, z, rotation, sbb);
      this.makeBuilderPlatform(world, rand, rotation, y + 5, z, true, sbb);
      if (y % 2 == 1) {
         int sx = this.pickFrom(rand, 5, 9, 13);
         int sz = sx == 9 ? (rand.nextBoolean() ? 5 : 13) : 9;
         this.placeBlockRotated(world, 1370, 9, sx, y + 2, sz, rotation, sbb);
      } else {
         int sx = rand.nextBoolean() ? 5 : 13;
         int sz = rand.nextBoolean() ? 5 : 13;
         this.makeLampCluster(world, rand, sx, y, sz, rotation, sbb);
      }
   }

   private void addTopBuilderPlatform(OldWorld world, Random rand, int bottom, int top, int spacing, StructureBoundingBox sbb) {
      int rotation = (this.boundingBox.minY + top + 1) % 4;
      this.fillBlocksRotated(world, sbb, 5, top - spacing, 9, 7, top - spacing, 11, this.deco.accentID, this.deco.accentMeta, rotation);
      this.fillBlocksRotated(world, sbb, 6, top - spacing, 9, 6, top, 9, this.deco.accentID, this.deco.accentMeta, rotation);
      this.fillBlocksRotated(
         world, sbb, 6, top - spacing + 1, 10, 6, top - 1, 10, 65, this.getLadderMeta(3, rotation), rotation
      );
      this.placeBlockRotated(world, 0, 0, 6, top + 1, 9, rotation, sbb);
      this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 5, top + 0, 10, rotation, sbb);
      this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 7, top + 0, 10, rotation, sbb);
      this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 5, top + 1, 10, rotation, sbb);
      this.placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 7, top + 1, 10, rotation, sbb);
      this.placeBlockRotated(world, 1370, 6, 7, top - spacing, 10, rotation, sbb);
      this.placeBlockRotated(world, 69, rand.nextBoolean() ? 5 : 6, 7, top - spacing + 1, 11, rotation, sbb);
   }

   private void makeBuilderPlatform(OldWorld world, Random rand, int rotation, int y, int z, boolean hole, StructureBoundingBox sbb) {
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 1, y, z - 1, rotation, sbb);
      if (!hole) {
         this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 1, y, z - 0, rotation, sbb);
      }

      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 1, y, z + 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 2, y, z - 1, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 2, y, z - 0, rotation, sbb);
      this.placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 2, y, z + 1, rotation, sbb);
      this.placeBlockRotated(world, 1370, 6, 2, y, hole ? z + 1 : z - 1, rotation, sbb);
      this.placeBlockRotated(world, 69, rand.nextBoolean() ? 5 : 6, 2, y + 1, z + 0, rotation, sbb);
   }

   private void makeLampCluster(OldWorld world, Random rand, int sx, int y, int sz, int rotation, StructureBoundingBox sbb) {
      int radius = 4;

      for (int i = 0; i < 5; i++) {
         int lx = sx;
         int ly = y;
         int lz = sz;

         for (int move = 0; move < 10; move++) {
            this.placeBlockRotated(world, 123, 0, lx, ly, lz, rotation, sbb);
            int direction = rand.nextInt(8);
            if (direction > 5) {
               direction -= 2;
            }

            lx += OFFSET_X[direction];
            ly += OFFSET_Y[direction];
            lz += OFFSET_Z[direction];
            if (lx > sx + radius || lx < sx - radius || ly > y + radius || ly < y - radius || lz > sz + radius || lz < sz - radius) {
               break;
            }
         }
      }

      for (int i = 0; i < 5; i++) {
         int lx = sx;
         int ly = y;
         int lz = sz;
         int[] directions = new int[10];

         for (int move = 0; move < 10; move++) {
            directions[move] = rand.nextInt(8);
            if (directions[move] > 5) {
               directions[move] -= 2;
            }
         }

         for (int move = 0; move < 10; move++) {
            int direction = directions[move];
            lx += OFFSET_X[direction];
            ly += OFFSET_Y[direction];
            lz += OFFSET_Z[direction];
            if (lx > sx + radius || lx < sx - radius || ly > y + radius || ly < y - radius || lz > sz + radius || lz < sz - radius) {
               break;
            }

            if (this.getBlockIDRotated(world, lx, ly, lz, rotation, sbb) != 123) {
               this.placeBlockRotated(world, 69, this.getLeverMeta(rotation, direction), lx, ly, lz, rotation, sbb);
               break;
            }
         }
      }
   }
}
