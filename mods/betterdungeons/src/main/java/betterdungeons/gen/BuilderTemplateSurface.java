package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * BuilderTemplateSurface ("templateSurface"): a random template of the folder, set into the ground at the mean
 * height of its area minus {@code underGroundOffset} (the castles).
 */
public final class BuilderTemplateSurface implements BDDungeons.Builder {
    private final String folderName;
    private final int underGroundOffset;
    /**
     * Hexxit Integrated only: leave out a template whose area is mostly under water. 1.5.2 had flat coasts, so a
     * castle chosen by the biome at the chunk corner stood mostly on land; the coasts of today drop into deep sea.
     */
    boolean landOnly;
    private BDTemplate mapcache;

    public BuilderTemplateSurface(String folderName, int underGroundOffset) {
        this.folderName = folderName;
        this.underGroundOffset = underGroundOffset;
    }

    @Override
    public void generate(Random random, OldWorld world, int i, int k, int mob) {
        this.mapcache = BDTemplate.random(this.folderName, random);
        int maxX = this.mapcache.sx;
        int maxY = this.mapcache.sz;
        int media = 0;
        int cant = 0;
        int wet = 0;
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                int top = world.getTopSolidOrLiquidBlock(i + x, y + k);
                media += top;
                cant++;
                if (this.landOnly && top > 0 && top < 256 && world.material(i + x, top, y + k) == OldBlockInfo.Material.WATER) {
                    wet++;
                }
            }
        }
        if (wet * 2 > cant) {
            this.mapcache = null;
            return;
        }
        this.generate(random, world, i, media / cant, k, mob);
    }

    public void generate(Random random, OldWorld world, int i, int j, int k, int idMob) {
        j -= this.underGroundOffset;
        j = Math.max(1, j);
        BuilderHelper b = new BuilderHelper(world);
        if (this.mapcache == null) {
            this.mapcache = BDTemplate.random(this.folderName, random);
        }
        b.fillMap(world, random, this.mapcache, i, j, k, idMob);
        this.mapcache = null;
    }
}
