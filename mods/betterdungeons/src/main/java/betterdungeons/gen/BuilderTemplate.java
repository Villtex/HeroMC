package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/** BuilderTemplate ("templateFloating"): a random template of the folder at a fixed height (the ships at y 60). */
public final class BuilderTemplate implements BDDungeons.Builder {
    private final String folderName;
    private int posY;

    public BuilderTemplate(String folderName, int height) {
        this.folderName = folderName;
        this.posY = height;
    }

    @Override
    public void generate(Random random, OldWorld world, int x, int z, int mob) {
        this.posY = Math.max(1, this.posY);
        this.generate(random, world, x, this.posY, z, mob);
    }

    public void generate(Random random, OldWorld world, int i, int j, int k, int idMob) {
        BuilderHelper b = new BuilderHelper(world);
        j = Math.max(1, j);
        b.fillMap(world, random, BDTemplate.random(this.folderName, random), i, j, k, idMob);
    }
}
