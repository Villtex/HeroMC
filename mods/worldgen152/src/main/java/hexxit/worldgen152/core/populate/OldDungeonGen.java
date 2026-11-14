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
                        OldLoot.DUNGEON_CHEST.fill(random, contents);
                        world.setTile(cx, y, cz, contents);
                        break;
                    }
                }
            }
        }

        world.setBlock(x, y, z, 52, 0);
        world.setTile(x, y, z, new OldTile.Spawner(OldLoot.randomDungeonMob(random)));
        return true;
    }
}
