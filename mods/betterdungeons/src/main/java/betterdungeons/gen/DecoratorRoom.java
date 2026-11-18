package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * DecoratorRoom (builder/docorator): the furniture of the rooms of BuilderDungeon by room type (bedroom, kitchen,
 * library, two kinds of monster room, big library), paintings, chests, furnaces, beds, cakes and flower pots.
 * Sides: 1 to 4 the wall the block stands at (-x, +x, -z, +z), 5 the middle of the room.
 */
final class DecoratorRoom {
    private final int mobId;

    DecoratorRoom(int mobid) {
        this.mobId = mobid;
    }

    int getRandomRoomType(Random random) {
        return random.nextInt(30) == 0 ? 200 : random.nextInt(6);
    }

    void placeDecorationBlock(Random random, OldWorld world, int x, int y, int z, int roomType, int roomHeight, int side) {
        if (random.nextInt(20) == 0) {
            this.placePainting(world, x, y + 1, z, side);
        }
        if (roomType == 0) {
            this.decorateBedroom(random, world, x, y, z, side);
        } else if (roomType == 1) {
            this.decorateKitchen(random, world, x, y, z, side);
        } else if (roomType == 2) {
            this.decorateLibrary(random, world, x, y, z, side);
        } else if (roomType == 3) {
            this.decorateMonsterRoom(random, world, x, y, z, side);
        } else if (roomType == 4) {
            this.decorateAnotherMonsterRoom(random, world, x, y, z, side);
        } else if (roomType == 200) {
            this.decorateLibrary(random, world, x, y, z, side);
        } else if (side != 5 && random.nextInt(10) == 0) {
            if (random.nextInt(4000) == 0) {
                replace(world, x, y, z, 130);
            } else if (random.nextInt(2000) == 0) {
                replace(world, x, y, z, 120);
            } else if (random.nextInt(800) == 0) {
                replace(world, x, y, z, 145);
            } else if (random.nextInt(400) == 0) {
                replace(world, x, y, z, 84);
            } else if (random.nextInt(500) == 0) {
                replace(world, x, y, z, 117);
            } else if (random.nextInt(400) == 0) {
                replace(world, x, y, z, 116);
            } else if (random.nextInt(100) == 0) {
                world.setBlock(x, y + random.nextInt(roomHeight - 2) + 1, z, 30, 0);
            } else if (random.nextInt(100) == 0) {
                this.placeCake(random, world, x, y, z);
            } else if (random.nextInt(100) == 0) {
                this.placeBed(world, x, y, z, side);
            } else if (random.nextInt(50) == 0) {
                replace(world, x, y, z, 118);
            } else if (random.nextInt(10) == 0) {
                this.placeFurnace(random, world, x, y, z);
            } else if (random.nextInt(10) == 0) {
                world.setBlock(x, y, z, 47, 0);
            } else if (random.nextInt(10) == 0) {
                this.placeFlowerPot(random, world, x, y, z);
            } else if (random.nextInt(10) == 0) {
                replace(world, x, y, z, 58);
            }
        }
    }

    /** "world.setBlock(x, y, z, 0); world.setBlock(x, y, z, id)". */
    private static void replace(OldWorld world, int x, int y, int z, int id) {
        world.setBlock(x, y, z, 0, 0);
        world.setBlock(x, y, z, id, 0);
    }

    private void decorateBedroom(Random random, OldWorld world, int x, int y, int z, int side) {
        if (random.nextInt(3) == 0) {
            if (random.nextInt(3) == 0) {
                this.placeBed(world, x, y, z, side);
            } else if (random.nextInt(5) == 0) {
                world.setBlock(x, y, z, 47, 0);
            } else if (random.nextInt(5) == 0) {
                replace(world, x, y, z, 58);
            } else if (random.nextInt(5) == 0) {
                this.placeFlowerPot(random, world, x, y, z);
            } else if (random.nextInt(20) == 0) {
                BuilderHelper.addWeaponChest(random, world, x, y, z);
            } else if (random.nextInt(15) == 0) {
                BuilderHelper.addChest(random, world, x, y, z);
            }
        }
    }

    private void decorateLibrary(Random random, OldWorld world, int x, int y, int z, int side) {
        if (side == 5 || random.nextInt(3) == 0) {
            world.setBlock(x, y, z, 47, 0);
        }
    }

    private void decorateMonsterRoom(Random random, OldWorld world, int x, int y, int z, int side) {
        if (side == 5) {
            world.setBlock(x, y, z, 0, 0);
            BuilderHelper.addCustomBlock(x, y, z, BuilderHelper.MOB_SPAWNER_BLOCK, random.nextInt(5), world, this.mobId);
            if (random.nextInt(15) == 0) {
                BuilderHelper.addSpawner(random, world, x, y, z, this.mobId);
            }
        } else {
            this.decorateAnotherMonsterRoom(random, world, x, y, z, side);
        }
    }

    private void decorateAnotherMonsterRoom(Random random, OldWorld world, int x, int y, int z, int side) {
        if (side == 5 || random.nextInt(20) == 0) {
            world.setBlock(x, y, z, 0, 0);
            BuilderHelper.addCustomBlock(x, y, z, BuilderHelper.MOB_SPAWNER_BLOCK, random.nextInt(5), world, this.mobId);
        } else if (random.nextInt(40) == 0) {
            BuilderHelper.addWeaponChest(random, world, x, y, z);
        } else if (random.nextInt(40) == 0) {
            BuilderHelper.addChest(random, world, x, y, z);
        } else if (random.nextInt(40) == 0) {
            BuilderHelper.addMineralChest(random, world, x, y, z);
        } else if (random.nextInt(80) == 0) {
            BuilderHelper.addTreasure(random, world, x, y, z);
        }
    }

    private void decorateKitchen(Random random, OldWorld world, int x, int y, int z, int side) {
        if (random.nextInt(250) == 0) {
            replace(world, x, y, z, 117);
        } else if (random.nextInt(30) == 0) {
            this.placeCake(random, world, x, y, z);
        } else if (random.nextInt(50) == 0) {
            replace(world, x, y, z, 118);
        } else if (random.nextInt(50) == 0) {
            world.setBlock(x, y, z, 103, 0);
        } else if (random.nextInt(50) == 0) {
            world.setBlock(x, y, z, 86, 0);
        } else if (random.nextInt(10) == 0) {
            this.placeFurnace(random, world, x, y, z);
        } else if (random.nextInt(10) == 0) {
            replace(world, x, y, z, 58);
        } else if (random.nextInt(5) == 0) {
            BuilderHelper.addFoodChest(random, world, x, y, z);
        }
    }

    private void placeBed(OldWorld world, int x, int y, int z, int side) {
        if (side <= 4) {
            int xo = 0;
            int zo = 0;
            int md = 0;
            switch (side) {
                case 1 -> {
                    xo = 1;
                    md = 1;
                }
                case 2 -> {
                    xo = -1;
                    md = 3;
                }
                case 3 -> {
                    zo = 1;
                    md = 2;
                }
                case 4 -> {
                    zo = -1;
                    md = 0;
                }
                default -> {
                }
            }
            world.setBlock(x + xo, y, z + zo, 0, 0);
            world.setBlock(x, y, z, 0, 0);
            world.setBlock(x + xo, y, z + zo, 26, md);
            world.setBlock(x, y, z, 26, md + 8);
        }
    }

    private void placeCake(Random random, OldWorld world, int x, int y, int z) {
        world.setBlock(x, y, z, 5, random.nextInt(5));
        int md = random.nextInt(5);
        if (md > 2) {
            md += 4;
        }
        world.setBlock(x, y + 1, z, 0, 0);
        world.setBlock(x, y + 1, z, 92, md);
    }

    private void placeFlowerPot(Random random, OldWorld world, int x, int y, int z) {
        world.setBlock(x, y, z, 5, random.nextInt(5));
        int md = random.nextInt(7);
        if (md > 2) {
            md += 4;
        }
        world.setBlock(x, y + 1, z, 0, 0);
        world.setBlock(x, y + 1, z, 140, md);
    }

    private void placeFurnace(Random random, OldWorld world, int x, int y, int z) {
        world.setBlockToAir(x, y, z);
        world.setBlock(x, y, z, 61, 0);
        OldTile.Furnace furnace = new OldTile.Furnace();
        world.setTile(x, y, z, furnace);
        if (random.nextInt(3) == 0) {
            furnace.slots()[1] = new OldStack(263, 0, 1 + random.nextInt(15));
        }
        if (random.nextInt(5) == 0) {
            furnace.slots()[2] = new OldStack(265, 0, 1 + random.nextInt(3));
        } else if (random.nextInt(7) == 0) {
            furnace.slots()[2] = new OldStack(266, 0, 1 + random.nextInt(3));
        } else if (random.nextInt(15) == 0) {
            furnace.slots()[2] = new OldStack(264, 0, 1 + random.nextInt(3));
        }
    }

    /** The painting hangs on the wall behind the block: side 1 to 4 become the wall block and its direction. */
    private void placePainting(OldWorld world, int x, int y, int z, int side) {
        switch (side) {
            case 1 -> {
                x--;
                side = 3;
            }
            case 2 -> {
                x++;
                side = 1;
            }
            case 3 -> {
                z--;
                side = 0;
            }
            case 4 -> {
                z++;
                side = 2;
            }
            default -> {
            }
        }
        BDPainting.place(world, x, y, z, side);
    }
}
