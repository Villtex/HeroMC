package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BuilderEmptyCave ("emptyCave": the slime cave and the swamp turtle cave of the pack): a cave of
 * {@code size} x {@code height} x {@code size} cut by the product of two Perlin3D noises, filled with
 * {@code caveBlock}, its boss (boss block 302) on the first empty or water block above the middle of its floor.
 * The original read {@code spawnBoss} and {@code initChance} from the configuration but never used them.
 */
final class BuilderEmptyCave implements BDDungeons.Builder {
    private final int caveBlock;
    private final int size;
    private final int height;
    private final int posY;
    private final int boss;

    BuilderEmptyCave(int caveBlock, int size, int height, int posY, int boss) {
        this.caveBlock = caveBlock;
        this.size = size;
        this.height = height;
        this.posY = posY;
        this.boss = boss;
    }

    @Override
    public void generate(Random random, OldWorld world, int i, int k, int mob) {
        this.generate(random, world, i, this.posY, k, mob);
    }

    void generate(Random random, OldWorld world, int i, int j, int k, int idMob) {
        int width = 32;
        int rooms = Math.max(1, width / 8);
        // wallX of the original: drawn, never used
        random.nextInt(width / rooms - 1);
        Perlin3D p = new Perlin3D(world.seed(), 8, random);
        Perlin3D p2 = new Perlin3D(world.seed(), 32, random);
        int wallSize = 4;
        int freq = 2;
        for (int x = 0; x < this.size; x++) {
            for (int y = 0; y < this.height; y++) {
                for (int z = 0; z < this.size; z++) {
                    float noiseVar = Math.max(0.0F, 3.0F - y / 2.0F);
                    noiseVar += Math.max(0.0F, 3.0F - (this.height - y) / 2.0F);
                    noiseVar += Math.max(0, wallSize - x / freq);
                    noiseVar += Math.max(0, wallSize - (this.size - x - 1) / freq);
                    noiseVar += Math.max(0, wallSize - z / freq);
                    noiseVar += Math.max(0, wallSize - (this.size - z) / freq);
                    if (p.getNoiseAt(x + i, y + j, z + k) * p2.getNoiseAt(x + i, y + j, z + k) * noiseVar < 0.5) {
                        world.setBlock(i + x, j + y, k + z, this.caveBlock, 0);
                    }
                }
            }
        }
        int y = 0;
        int half = this.size / 2;
        while (world.getBlockId(i + half, j + y, k + half) != 0 && world.material(i + half, j + y, k + half) != OldBlockInfo.Material.WATER) {
            y++;
        }
        BuilderHelper.addCustomBlock(i + half, j + y, k + half, BuilderHelper.BOSS_BLOCK, this.boss, world, idMob);
    }
}
