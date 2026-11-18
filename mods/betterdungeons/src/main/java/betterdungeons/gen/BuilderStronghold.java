package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BuilderStronghold ("linearDungeon", stronghold.prop): a tower of dungeons/base on the surface (at least y 41), the
 * entrance 20 blocks below it, then up to 23 rooms of dungeons/dungeonRooms on a fixed path of 15-block steps down
 * three floors, and the boss room.
 *
 * <p>The original drew everything (which templates, and their chests and spawners) from unseeded
 * {@code new Random()}: here from randoms seeded with the world seed and the position of the dungeon or the piece.
 */
final class BuilderStronghold implements BDDungeons.Builder {
    private static final int[] PATTERN = {0, 7, 10, 6, 9, 1, 8, 20, 10, 4, 5, 8, 0, 7, 10, 20, 8, 2, 9, 3, 10, 4, 5};
    private static final byte[] MOVE = {3, 2, 2, 1, 1, 0, 0, 2, 2, 3, 0, 0, 3, 2, 2, 0, 0, 1, 1, 2, 2, 3, 0};
    private final int rooms;

    BuilderStronghold(int rooms) {
        this.rooms = rooms;
    }

    @Override
    public void generate(Random random, OldWorld world, int i, int k, int mob) {
        int cant = 0;
        int media = 0;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                media += world.getTopSolidOrLiquidBlock(i + x, y + k);
                cant++;
            }
        }
        this.generate(random, world, i, media / cant, k, mob);
    }

    void generate(Random random, OldWorld world, int i, int j, int k, int mob) {
        j = Math.max(41, j);
        this.generateDungeon(BuilderHelper.positionRandom(world, i, j, k, 1), world, i, j, k, mob);
    }

    /** {@code piece(world, x, y, z)} stands for each {@code new Random()} the original gave fillMap. */
    private static Random piece(OldWorld world, int x, int y, int z) {
        return BuilderHelper.positionRandom(world, x, y, z, 2);
    }

    private void generateDungeon(Random random, OldWorld world, int x, int y, int z, int mob) {
        BuilderHelper b = new BuilderHelper(world);
        b.fillMap(world, piece(world, x, y, z), BDTemplate.random("dungeons/base", random), x, y, z, mob);
        y -= 20;
        b.fillMap(world, piece(world, x, y, z), BDTemplate.map("Building/dungeons/entrance"), x, y, z, mob);
        z += 15;
        for (int i = 0; i < PATTERN.length && i < this.rooms; i++) {
            if (PATTERN[i] < 20) {
                b.fillMap(world, piece(world, x, y, z), BDTemplate.random("dungeons/dungeonRooms/" + PATTERN[i], random), x, y, z, mob);
            } else {
                y -= 10;
                b.fillMap(world, piece(world, x, y, z), BDTemplate.map("Building/dungeons/stairs" + MOVE[i - 1]), x, y, z, mob);
            }
            switch (MOVE[i]) {
                case 0 -> z += 15;
                case 1 -> x -= 15;
                case 2 -> z -= 15;
                case 3 -> x += 15;
                default -> {
                }
            }
        }
        b.fillMap(world, piece(world, x, y, z), BDTemplate.map("Building/dungeons/bossRoom"), x, y, z, mob);
    }
}
