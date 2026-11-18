package battletowers.gen;

import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Arrays;
import java.util.Random;

/**
 * AS_WorldGenTower of Battle Towers 1.3.4: checks nine points around the chunk corner, picks the tower type from the
 * ground (sandstone, ice, mossy, cobblestone, smooth stone, netherrack) and builds the tower floor by floor - seven
 * blocks each, from six below the surface up to y 120, or 70 down into the ground for an underground tower - with
 * two spawners and two chests per floor and the golem on top. Kept as close to the original as the port allows;
 * only {@code world.rand}, which was not bound to the seed, is replaced by {@code worldRand}.
 */
final class BTTower {
    private final BTConfig config;
    private final Random worldRand;
    private int floor;
    private int floorIterator;
    private boolean topFloor;
    private boolean underground;
    private final int[][] candidates = {{4, -5}, {4, 0}, {4, 5}, {0, -5}, {0, 0}, {0, 5}, {-4, -5}, {-4, 0}, {-4, 5}};
    private final int candidatecount = this.candidates.length;
    private TowerType towerChosen;
    /** Whether the last tower was built underground (for the log). */
    boolean wasUnderground() {
        return this.underground;
    }

    BTTower(BTConfig config, Random worldRand) {
        this.config = config;
        this.worldRand = worldRand;
    }

    /** WorldGenHandler.getTowerStageManagerForFloor: a fresh copy of the items of the floor (1 to 10). */
    private BTItems floorItems(int floor) {
        if (--floor >= this.config.floorItemManagers.length) {
            floor = this.config.floorItemManagers.length - 1;
        }
        if (floor < 0) {
            floor = 0;
        }
        return new BTItems(this.config.floorItemManagers[floor]);
    }

    boolean generate(OldWorld world, Random random, int ix, int jy, int kz) {
       int centerblockY = jy;
       int countWater = 0;
       int countSand = 0;
       int countSnow = 0;
       int countFoliage = 0;
       int countElse = 0;

       for (int ccounter = 0; ccounter < this.candidatecount; ccounter++) {
          int[] pair = this.candidates[ccounter];
          int checkBlockY = this.GetSurfaceBlockHeight(world, ix + pair[0], kz + pair[1]);
          int ID = world.getBlockId(ix + pair[0], checkBlockY, kz + pair[1]);
          if (world.getBlockId(ix + pair[0], checkBlockY + 1, kz + pair[1]) == 78
             || ID == 79) {
             countSnow++;
          } else if (ID == 12 || ID == 24) {
             countSand++;
          } else if (ID == 9) {
             countWater++;
          } else if (ID != 18 && ID != 111 && ID != 17) {
             countElse++;
          } else {
             countFoliage++;
          }

          if (Math.abs(checkBlockY - centerblockY) > 22) {
             return false;
          }

          for (int ycounter2 = 1; ycounter2 <= 3; ycounter2++) {
             ID = world.getBlockId(ix + pair[0], checkBlockY + ycounter2, kz + pair[1]);
             if (this.IsBannedBlockID(ID)) {
                return false;
             }
          }

          for (int ycounter = 1; ycounter <= 5; ycounter++) {
             ID = world.getBlockId(ix + pair[0], checkBlockY - ycounter, kz + pair[1]);
             if (ID == 0 || this.IsBannedBlockID(ID)) {
                return false;
             }
          }
       }

       int[] nums = new int[]{countWater, countSnow, countSand, countFoliage, countElse};
       Arrays.sort(nums);
       int result = nums[nums.length - 1];
       if (countSand == result) {
          this.towerChosen = TowerType.SandStone;
       } else if (countSnow == result) {
          this.towerChosen = TowerType.Ice;
       } else if (countWater == result) {
          this.towerChosen = TowerType.CobbleStoneMossy;
       } else if (countFoliage == result) {
          this.towerChosen = TowerType.CobbleStoneMossy;
       } else if (random.nextInt(10) == 0) {
          this.towerChosen = TowerType.Netherrack;
       } else {
          this.towerChosen = random.nextInt(5) == 0 ? TowerType.SmoothStone : TowerType.CobbleStone;
       }

       int towerWallBlockID = this.towerChosen.GetWallBlockID();
       int towerLightBlockID = this.towerChosen.GetLightBlockID();
       int towerFloorBlockID = this.towerChosen.GetFloorBlockID();
       this.underground = this.worldRand.nextInt(100) + 1 < this.config.chanceTowerIsUnderGround;
       int startingHeight = this.underground ? Math.max(jy - 70, 15) : jy - 6;
       int maximumHeight = this.underground ? jy + 7 : 120;
       this.floor = 1;
       this.topFloor = false;

       for (int builderHeight = startingHeight; builderHeight < maximumHeight; builderHeight += 7) {
          if (builderHeight + 7 >= maximumHeight) {
             this.topFloor = true;
          }

          for (this.floorIterator = 0; this.floorIterator < 7; this.floorIterator++) {
             if (this.floor == 1 && this.floorIterator < 4) {
                this.floorIterator = 4;
             }

             for (int xIterator = -7; xIterator < 7; xIterator++) {
                for (int zIterator = -7; zIterator < 7; zIterator++) {
                   int iCurrent = xIterator + ix;
                   int jCurrent = this.floorIterator + builderHeight;
                   int zCurrent = zIterator + kz;
                   if (zIterator == -7) {
                      if (xIterator > -5 && xIterator < 4) {
                         this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                      }
                   } else if (zIterator != -6 && zIterator != -5) {
                      if (zIterator != -4 && zIterator != -3 && zIterator != 2 && zIterator != 3) {
                         if (zIterator > -3 && zIterator < 2) {
                            if (xIterator != -7 && xIterator != 6) {
                               if (xIterator > -7 && xIterator < 6) {
                                  if (this.floorIterator == 5) {
                                     this.BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                                  } else {
                                     world.setBlock(iCurrent, jCurrent, zCurrent, 0, 0);
                                  }
                               }
                            } else if (this.floorIterator >= 0
                               && this.floorIterator <= 3
                               && (xIterator == -7 || xIterator == 6)
                               && !this.underground
                               && (zIterator == -1 || zIterator == 0)) {
                               world.setBlock(iCurrent, jCurrent, zCurrent, 0, 0);
                            } else {
                               this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                            }
                         } else if (zIterator == 4) {
                            if (xIterator == -5 || xIterator == 4) {
                               this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                            } else if (xIterator > -5 && xIterator < 4) {
                               if (this.floorIterator == 5) {
                                  this.BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                               } else {
                                  world.setBlock(iCurrent, jCurrent, zCurrent, 0, 0);
                               }
                            }
                         } else if (zIterator == 5) {
                            if (xIterator == -4 || xIterator == -3 || xIterator == 2 || xIterator == 3) {
                               this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                            } else if (xIterator > -3 && xIterator < 2) {
                               if (this.floorIterator == 5) {
                                  this.BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                               } else {
                                  this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                               }
                            }
                         } else if (zIterator == 6 && xIterator > -3 && xIterator < 2) {
                            if (this.floorIterator >= 0 && this.floorIterator <= 3 && (xIterator == -1 || xIterator == 0)) {
                               this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                            } else {
                               this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                            }
                         }
                      } else if (xIterator == -6 || xIterator == 5) {
                         this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                      } else if (xIterator > -6 && xIterator < 5) {
                         if (this.floorIterator == 5) {
                            this.BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                         } else if (world.getBlockId(iCurrent, jCurrent, zCurrent) != 54) {
                            world.setBlock(iCurrent, jCurrent, zCurrent, 0, 0);
                         }
                      }
                   } else if (xIterator == -5 || xIterator == 4) {
                      this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                   } else if (zIterator == -6) {
                      if (xIterator == (this.floorIterator + 1) % 7 - 3) {
                         if (!this.underground || this.floor != 1) {
                            world.setBlock(iCurrent, jCurrent, zCurrent, this.towerChosen.GetStairBlockID(), 0);
                         }

                         if (this.floorIterator == 5) {
                            world.setBlock(iCurrent - 7, jCurrent, zCurrent, towerFloorBlockID, 0);
                         }

                         if (this.floorIterator == 6 && this.topFloor) {
                            this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                         }
                      } else if (xIterator < 4 && xIterator > -5) {
                         world.setBlock(iCurrent, jCurrent, zCurrent, 0, 0);
                      }
                   } else if (zIterator == -5 && xIterator > -5 && xIterator < 5) {
                      if (this.floorIterator != 0 && this.floorIterator != 6 || xIterator != -4 && xIterator != 3) {
                         if (this.floorIterator != 5 || xIterator != 3 && xIterator != -4) {
                            this.BuildWallPiece(world, iCurrent, jCurrent, zCurrent, towerWallBlockID);
                         } else {
                            this.BuildFloorPiece(world, iCurrent, jCurrent, zCurrent, towerFloorBlockID);
                         }
                      } else {
                         world.setBlock(iCurrent, jCurrent, zCurrent, 0, 0);
                      }
                   }
                }
             }
          }

          if (this.floor == 2) {
             world.setBlock(ix + 3, builderHeight, kz - 5, towerWallBlockID, 0);
             world.setBlock(ix + 3, builderHeight - 1, kz - 5, towerWallBlockID, 0);
          }

          if ((this.underground || !this.topFloor) && (!this.underground || this.floor != 1)) {
             world.setBlock(ix + 2, builderHeight + 6, kz + 2, 52, 0);
             world.setTile(ix + 2, builderHeight + 6, kz + 2, new OldTile.Spawner(this.getMobType(random)));
             world.setBlock(ix - 3, builderHeight + 6, kz + 2, 52, 0);
             world.setTile(ix - 3, builderHeight + 6, kz + 2, new OldTile.Spawner(this.getMobType(random)));
          } else {
             // the golem's yaw came from world.rand: drawn from the tower's own random here
             world.spawnEntity(new OldEntity.Mob(BTWorldGen.golemName(this.towerChosen.ordinal()), ix, builderHeight + 6, kz + 0.5,
                     this.worldRand.nextFloat() * 360.0F, null));
          }

          world.setBlock(ix, builderHeight + 6, kz + 3, towerFloorBlockID, 0);
          world.setBlock(ix - 1, builderHeight + 6, kz + 3, towerFloorBlockID, 0);
          if (builderHeight + 56 >= 120 && this.floor == 1) {
             this.floor = 2;
          }

          BTItems floorChestManager = null;
          if (!this.underground) {
             floorChestManager = this.topFloor
                ? this.floorItems(10)
                : this.floorItems(this.floor);
          } else {
             floorChestManager = this.floor == 1
                ? this.floorItems(10)
                : this.floorItems(Math.abs(11 - this.floor));
          }

          for (int chestlength = 0; chestlength < 2; chestlength++) {
             world.setBlock(ix - chestlength, builderHeight + 7, kz + 3, 54, 2);
             OldTile.Chest tileentitychest = new OldTile.Chest();
             world.setTile(ix - chestlength, builderHeight + 7, kz + 3, tileentitychest);

             for (int attempt = 0;
                attempt < (this.underground ? this.config.itemGenerateAttemptsPerFloor * 2 : this.config.itemGenerateAttemptsPerFloor);
                attempt++
             ) {
                OldStack itemstack = floorChestManager.getStageItem(random);
                if (itemstack != null) {
                   tileentitychest.slots()[random.nextInt(tileentitychest.slots().length)] = itemstack;
                }
             }
          }

          if (towerLightBlockID != 0 && towerLightBlockID == 89) {
             builderHeight += 2;
          }

          world.setBlock(ix + 3, builderHeight, kz - 6, towerLightBlockID, 0);
          world.setBlock(ix - 4, builderHeight, kz - 6, towerLightBlockID, 0);
          world.setBlock(ix + 1, builderHeight, kz - 4, towerLightBlockID, 0);
          world.setBlock(ix - 2, builderHeight, kz - 4, towerLightBlockID, 0);
          if (towerLightBlockID != 0 && towerLightBlockID == 89) {
             builderHeight -= 2;
          }

          for (int l3 = 0; l3 < this.floor * 4 + this.towerChosen.ordinal() - 8 && !this.topFloor; l3++) {
             int k4 = 5 - random.nextInt(12);
             int k5 = builderHeight + 5;
             int j6 = 5 - random.nextInt(10);
             if (j6 >= -2 || k4 >= 4 || k4 <= -5 || k4 == 1 || k4 == -2) {
                k4 += ix;
                j6 += kz;
                if (world.getBlockId(k4, k5, j6) == towerFloorBlockID && world.getBlockId(k4, k5 + 1, j6) != 52) {
                   world.setBlock(k4, k5, j6, 0, 0);
                }
             }
          }

          this.floor++;
       }

       return true;
    }

    private void BuildFloorPiece(OldWorld world, int i, int j, int k, int towerFloorBlockID) {
        world.setBlock(i, j, k, towerFloorBlockID, 0);
        if (this.towerChosen.GetFloorBlockMetaData() != 0) {
            world.setMetadata(i, j, k, this.towerChosen.GetFloorBlockMetaData());
        }
    }

    private void BuildWallPiece(OldWorld world, int i, int j, int k, int towerWallBlockID) {
        world.setBlock(i, j, k, towerWallBlockID, 0);
        if (this.floor == 1 && this.floorIterator == 4) {
            this.FillTowerBaseToGround(world, i, j, k, towerWallBlockID);
        }
    }

    private void FillTowerBaseToGround(OldWorld world, int i, int j, int k, int blocktype) {
        for (int x = j - 1; x > 0 && !IsBuildableBlockID(world.getBlockId(i, x, k)); x--) {
            world.setBlock(i, x, k, blocktype, 0);
        }
    }

    private int GetSurfaceBlockHeight(OldWorld world, int x, int z) {
        int h = 50;
        do {
            h++;
        } while (world.getBlockId(x, h, z) != 0 && !IsFoliageBlockID(world.getBlockId(x, h, z)));
        return h - 1;
    }

    private static boolean IsFoliageBlockID(int id) {
        return id == 78 || id == 31 || id == 32 || id == 17 || id == 18;
    }

    private static boolean IsBuildableBlockID(int id) {
        return id == 1 || id == 2 || id == 12 || id == 24 || id == 13 || id == 3;
    }

    /** Dandelion, rose, both mushrooms, cactus, pumpkin and lava. */
    private boolean IsBannedBlockID(int id) {
        return id == 37 || id == 38 || id == 39 || id == 40 || id == 81 || id == 86 || id == 10 || id == 11;
    }

    private String getMobType(Random random) {
        return switch (random.nextInt(4)) {
            case 0 -> "Skeleton";
            case 2 -> "Spider";
            case 3 -> "CaveSpider";
            default -> "Zombie";
        };
    }

    /** towerTypes: wall, light, floor, floor metadata, stairs. The jungle tower was never chosen. */
    enum TowerType {
        CobbleStone(4, 50, 43, 0, 67),
        CobbleStoneMossy(48, 50, 43, 0, 67),
        SandStone(24, 50, 43, 1, 67),
        Ice(79, 0, 82, 2, 53),
        SmoothStone(1, 50, 43, 3, 67),
        Netherrack(87, 89, 88, 0, 67),
        Jungle(48, 30, 3, 0, 53);

        private final int wallBlockID;
        private final int lightBlockID;
        private final int floorBlockID;
        private final int floorBlockMetaData;
        private final int stairBlockID;

        TowerType(int a, int b, int c, int d, int e) {
            this.wallBlockID = a;
            this.lightBlockID = b;
            this.floorBlockID = c;
            this.floorBlockMetaData = d;
            this.stairBlockID = e;
        }

        int GetWallBlockID() {
            return this.wallBlockID;
        }

        int GetLightBlockID() {
            return this.lightBlockID;
        }

        int GetFloorBlockID() {
            return this.floorBlockID;
        }

        int GetFloorBlockMetaData() {
            return this.floorBlockMetaData;
        }

        int GetStairBlockID() {
            return this.stairBlockID;
        }
    }
}
