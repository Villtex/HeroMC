package hexxit.worldgen152.core.populate;

import java.util.Random;

/** WorldGenDungeons: the cobblestone room with a spawner and up to two chests. */
public final class OldDungeonGen extends OldGen {
    @Override
    public boolean generate(OldWorld world, Random random, int x, int y, int z) {
        int height = 3;
        int rx = random.nextInt(2) + 2;
        int rz = random.nextInt(2) + 2;
        int openings = 0;

        for (int bx = x - rx - 1; bx <= x + rx + 1; bx++) {
            for (int by = y - 1; by <= y + height + 1; by++) {
                for (int bz = z - rz - 1; bz <= z + rz + 1; bz++) {
                    boolean solid = world.material(bx, by, bz).solid;
                    if (by == y - 1 && !solid) {
                        return false;
                    }
                    if (by == y + height + 1 && !solid) {
                        return false;
                    }
                    if ((bx == x - rx - 1 || bx == x + rx + 1 || bz == z - rz - 1 || bz == z + rz + 1) && by == y
                            && world.isAirBlock(bx, by, bz) && world.isAirBlock(bx, by + 1, bz)) {
                        openings++;
                    }
                }
            }
        }
        if (openings < 1 || openings > 5) {
            return false;
        }

        for (int bx = x - rx - 1; bx <= x + rx + 1; bx++) {
            for (int by = y + height; by >= y - 1; by--) {
                for (int bz = z - rz - 1; bz <= z + rz + 1; bz++) {
                    if (bx != x - rx - 1 && by != y - 1 && bz != z - rz - 1 && bx != x + rx + 1 && by != y + height + 1 && bz != z + rz + 1) {
                        world.setBlockToAir(bx, by, bz);
                    } else if (by >= 0 && !world.material(bx, by - 1, bz).solid) {
                        world.setBlockToAir(bx, by, bz);
                    } else if (world.material(bx, by, bz).solid) {
                        if (by == y - 1 && random.nextInt(4) != 0) {
                            world.setBlock(bx, by, bz, 48, 0);
                        } else {
                            world.setBlock(bx, by, bz, 4, 0);
                        }
                    }
                }
            }
        }

        for (int chest = 0; chest < 2; chest++) {
            for (int attempt = 0; attempt < 3; attempt++) {
                int cx = x + random.nextInt(rx * 2 + 1) - rx;
                int cz = z + random.nextInt(rz * 2 + 1) - rz;
                if (world.isAirBlock(cx, y, cz)) {
                    int walls = 0;
                    if (world.material(cx - 1, y, cz).solid) {
                        walls++;
                    }
                    if (world.material(cx + 1, y, cz).solid) {
                        walls++;
                    }
                    if (world.material(cx, y, cz - 1).solid) {
                        walls++;
                    }
                    if (world.material(cx, y, cz + 1).solid) {
                        walls++;
                    }
                    if (walls == 1) {
                        world.setBlock(cx, y, cz, 54, 0);
                        OldTile.Chest contents = new OldTile.Chest();
                        if (vanillaLoot) {
                            for (int n = 0; n < 8; n++) {
                                OldStack stack = pickCheckLootItem(random);
                                if (stack != null) {
                                    contents.slots()[random.nextInt(contents.slots().length)] = stack;
                                }
                            }
                        } else {
                            OldLoot.DUNGEON_CHEST.fill(random, contents);
                        }
                        world.setTile(cx, y, cz, contents);
                        break;
                    }
                }
            }
        }

        world.setBlock(x, y, z, 52, 0);
        world.setTile(x, y, z, new OldTile.Spawner(vanillaLoot ? pickMobSpawner(random) : OldLoot.randomDungeonMob(random)));
        return true;
    }

    /**
     * Dungeon chests and spawners as Minecraft 1.5.2 filled them without Forge. Forge replaced both with its
     * ChestGenHooks and DungeonHooks tables, which draw differently from the chunk's random sequence, so the
     * pack (and this port) follows Forge. Only for comparing the core with a world of the vanilla server.
     */
    public static volatile boolean vanillaLoot;

    /** WorldGenDungeons.pickCheckLootItem of vanilla 1.5.2. */
    private static OldStack pickCheckLootItem(Random random) {
        int pick = random.nextInt(12);
        switch (pick) {
            case 0:
                return new OldStack(329, 0, 1);
            case 1:
                return new OldStack(265, 0, random.nextInt(4) + 1);
            case 2:
                return new OldStack(297, 0, 1);
            case 3:
                return new OldStack(296, 0, random.nextInt(4) + 1);
            case 4:
                return new OldStack(289, 0, random.nextInt(4) + 1);
            case 5:
                return new OldStack(287, 0, random.nextInt(4) + 1);
            case 6:
                return new OldStack(325, 0, 1);
            case 7:
                return random.nextInt(100) == 0 ? new OldStack(322, 0, 1) : null;
            case 8:
                return random.nextInt(2) == 0 ? new OldStack(331, 0, random.nextInt(4) + 1) : null;
            case 9:
                return random.nextInt(10) == 0 ? new OldStack(2256 + random.nextInt(2), 0, 1) : null;
            case 10:
                return new OldStack(351, 3, 1);
            default:
                return OldLoot.randomBook(random);
        }
    }

    /** WorldGenDungeons.pickMobSpawner of vanilla 1.5.2. */
    private static String pickMobSpawner(Random random) {
        int pick = random.nextInt(4);
        return pick == 0 ? "Skeleton" : pick == 3 ? "Spider" : "Zombie";
    }
}
