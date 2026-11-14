package hexxit.worldgen152.core.populate;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.populate.OldTerrainGen.Decorate;
import hexxit.worldgen152.core.populate.OldTerrainGen.Ore;

import java.util.Random;

/**
 * BiomeDecorator of 1.5.2: ores, then sand and clay, trees, plants and springs, each with the per-chunk
 * counts a biome sets. A mod that had its own decorator subclasses this and assigns it to
 * {@link OldBiome#decorator}. Decorators are shared between threads, so they keep no state while running.
 */
public class OldDecorator {
    public OldGen clayGen = new OldOreGens.Clay(4);
    public OldGen sandGen = new OldOreGens.Sand(7, 12);
    public OldGen gravelAsSandGen = new OldOreGens.Sand(6, 13);
    public OldGen dirtGen = new OldOreGens.Minable(3, 32);
    public OldGen gravelGen = new OldOreGens.Minable(13, 32);
    public OldGen coalGen = new OldOreGens.Minable(16, 16);
    public OldGen ironGen = new OldOreGens.Minable(15, 8);
    public OldGen goldGen = new OldOreGens.Minable(14, 8);
    public OldGen redstoneGen = new OldOreGens.Minable(73, 7);
    public OldGen diamondGen = new OldOreGens.Minable(56, 7);
    public OldGen lapisGen = new OldOreGens.Minable(21, 6);
    public OldGen plantYellowGen = new OldPlantGens.Flowers(37);
    public OldGen plantRedGen = new OldPlantGens.Flowers(38);
    public OldGen mushroomBrownGen = new OldPlantGens.Flowers(39);
    public OldGen mushroomRedGen = new OldPlantGens.Flowers(40);
    public OldGen bigMushroomGen = new OldTreeGens.BigMushroom();
    public OldGen reedGen = new OldPlantGens.Reed();
    public OldGen cactusGen = new OldPlantGens.Cactus();
    public OldGen waterlilyGen = new OldPlantGens.Waterlily();
    public int waterlilyPerChunk = 0;
    public int treesPerChunk = 0;
    public int flowersPerChunk = 2;
    public int grassPerChunk = 1;
    public int deadBushPerChunk = 0;
    public int mushroomsPerChunk = 0;
    public int reedsPerChunk = 0;
    public int cactiPerChunk = 0;
    public int sandPerChunk = 1;
    public int sandPerChunk2 = 3;
    public int clayPerChunk = 1;
    public int bigMushroomsPerChunk = 0;
    public boolean generateLakes = true;

    /** @param x block x of the chunk corner; everything but ores is shifted 8 blocks into the chunk */
    public void decorate(OldWorld world, Random random, OldBiome biome, int x, int z) {
        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.decoratePre(world, random, x, z);
        }
        this.generateOres(world, random, x, z);

        boolean doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.SAND);
        for (int i = 0; doGen && i < this.sandPerChunk2; i++) {
            int bx = x + random.nextInt(16) + 8;
            int bz = z + random.nextInt(16) + 8;
            this.sandGen.generate(world, random, bx, world.getTopSolidOrLiquidBlock(bx, bz), bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.CLAY);
        for (int i = 0; doGen && i < this.clayPerChunk; i++) {
            int bx = x + random.nextInt(16) + 8;
            int bz = z + random.nextInt(16) + 8;
            this.clayGen.generate(world, random, bx, world.getTopSolidOrLiquidBlock(bx, bz), bz);
        }

        // 1.5.2 used the sand generator here again, not the gravel one
        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.SAND_PASS2);
        for (int i = 0; doGen && i < this.sandPerChunk; i++) {
            int bx = x + random.nextInt(16) + 8;
            int bz = z + random.nextInt(16) + 8;
            this.sandGen.generate(world, random, bx, world.getTopSolidOrLiquidBlock(bx, bz), bz);
        }

        int trees = this.treesPerChunk;
        if (random.nextInt(10) == 0) {
            trees++;
        }
        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.TREE);
        for (int i = 0; doGen && i < trees; i++) {
            int bx = x + random.nextInt(16) + 8;
            int bz = z + random.nextInt(16) + 8;
            OldGen tree = biome.trees.pick(world, random);
            tree.setScale(1.0, 1.0, 1.0);
            tree.generate(world, random, bx, world.getHeightValue(bx, bz), bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.BIG_SHROOM);
        for (int i = 0; doGen && i < this.bigMushroomsPerChunk; i++) {
            int bx = x + random.nextInt(16) + 8;
            int bz = z + random.nextInt(16) + 8;
            this.bigMushroomGen.generate(world, random, bx, world.getHeightValue(bx, bz), bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.FLOWERS);
        for (int i = 0; doGen && i < this.flowersPerChunk; i++) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            this.plantYellowGen.generate(world, random, bx, by, bz);
            if (random.nextInt(4) == 0) {
                bx = x + random.nextInt(16) + 8;
                by = random.nextInt(128);
                bz = z + random.nextInt(16) + 8;
                this.plantRedGen.generate(world, random, bx, by, bz);
            }
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.GRASS);
        for (int i = 0; doGen && i < this.grassPerChunk; i++) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            biome.grass.pick(random).generate(world, random, bx, by, bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.DEAD_BUSH);
        for (int i = 0; doGen && i < this.deadBushPerChunk; i++) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            new OldPlantGens.DeadBush().generate(world, random, bx, by, bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.LILYPAD);
        for (int i = 0; doGen && i < this.waterlilyPerChunk; i++) {
            int bx = x + random.nextInt(16) + 8;
            int bz = z + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            while (by > 0 && world.getBlockId(bx, by - 1, bz) == 0) {
                by--;
            }
            this.waterlilyGen.generate(world, random, bx, by, bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.SHROOM);
        for (int i = 0; doGen && i < this.mushroomsPerChunk; i++) {
            if (random.nextInt(4) == 0) {
                int bx = x + random.nextInt(16) + 8;
                int bz = z + random.nextInt(16) + 8;
                this.mushroomBrownGen.generate(world, random, bx, world.getHeightValue(bx, bz), bz);
            }
            if (random.nextInt(8) == 0) {
                int bx = x + random.nextInt(16) + 8;
                int bz = z + random.nextInt(16) + 8;
                int by = random.nextInt(128);
                this.mushroomRedGen.generate(world, random, bx, by, bz);
            }
        }
        if (doGen && random.nextInt(4) == 0) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            this.mushroomBrownGen.generate(world, random, bx, by, bz);
        }
        if (doGen && random.nextInt(8) == 0) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            this.mushroomRedGen.generate(world, random, bx, by, bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.REED);
        for (int i = 0; doGen && i < this.reedsPerChunk; i++) {
            int bx = x + random.nextInt(16) + 8;
            int bz = z + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            this.reedGen.generate(world, random, bx, by, bz);
        }
        for (int i = 0; doGen && i < 10; i++) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            this.reedGen.generate(world, random, bx, by, bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.PUMPKIN);
        if (doGen && random.nextInt(32) == 0) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            new OldPlantGens.Pumpkin().generate(world, random, bx, by, bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.CACTUS);
        for (int i = 0; doGen && i < this.cactiPerChunk; i++) {
            int bx = x + random.nextInt(16) + 8;
            int by = random.nextInt(128);
            int bz = z + random.nextInt(16) + 8;
            this.cactusGen.generate(world, random, bx, by, bz);
        }

        doGen = OldTerrainGen.decorate(world, random, x, z, Decorate.LAKE);
        if (doGen && this.generateLakes) {
            for (int i = 0; i < 50; i++) {
                int bx = x + random.nextInt(16) + 8;
                int by = random.nextInt(random.nextInt(120) + 8);
                int bz = z + random.nextInt(16) + 8;
                new OldLakeGens.Spring(8).generate(world, random, bx, by, bz);
            }
            for (int i = 0; i < 20; i++) {
                int bx = x + random.nextInt(16) + 8;
                int by = random.nextInt(random.nextInt(random.nextInt(112) + 8) + 8);
                int bz = z + random.nextInt(16) + 8;
                new OldLakeGens.Spring(10).generate(world, random, bx, by, bz);
            }
        }

        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.decoratePost(world, random, x, z);
        }
    }

    /** {@code count} veins at a height spread evenly over minY..maxY. */
    protected void genStandardOre1(OldWorld world, Random random, int x, int z, int count, OldGen gen, int minY, int maxY) {
        for (int i = 0; i < count; i++) {
            int bx = x + random.nextInt(16);
            int by = random.nextInt(maxY - minY) + minY;
            int bz = z + random.nextInt(16);
            gen.generate(world, random, bx, by, bz);
        }
    }

    /** {@code count} veins at a height clustered around centerY (within spread). */
    protected void genStandardOre2(OldWorld world, Random random, int x, int z, int count, OldGen gen, int centerY, int spread) {
        for (int i = 0; i < count; i++) {
            int bx = x + random.nextInt(16);
            int by = random.nextInt(spread) + random.nextInt(spread) + (centerY - spread);
            int bz = z + random.nextInt(16);
            gen.generate(world, random, bx, by, bz);
        }
    }

    protected void generateOres(OldWorld world, Random random, int x, int z) {
        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.oreGenPre(world, random, x, z);
        }
        if (OldTerrainGen.generateOre(world, random, x, z, Ore.DIRT)) {
            this.genStandardOre1(world, random, x, z, 20, this.dirtGen, 0, 128);
        }
        if (OldTerrainGen.generateOre(world, random, x, z, Ore.GRAVEL)) {
            this.genStandardOre1(world, random, x, z, 10, this.gravelGen, 0, 128);
        }
        if (OldTerrainGen.generateOre(world, random, x, z, Ore.COAL)) {
            this.genStandardOre1(world, random, x, z, 20, this.coalGen, 0, 128);
        }
        if (OldTerrainGen.generateOre(world, random, x, z, Ore.IRON)) {
            this.genStandardOre1(world, random, x, z, 20, this.ironGen, 0, 64);
        }
        if (OldTerrainGen.generateOre(world, random, x, z, Ore.GOLD)) {
            this.genStandardOre1(world, random, x, z, 2, this.goldGen, 0, 32);
        }
        if (OldTerrainGen.generateOre(world, random, x, z, Ore.REDSTONE)) {
            this.genStandardOre1(world, random, x, z, 8, this.redstoneGen, 0, 16);
        }
        if (OldTerrainGen.generateOre(world, random, x, z, Ore.DIAMOND)) {
            this.genStandardOre1(world, random, x, z, 1, this.diamondGen, 0, 16);
        }
        if (OldTerrainGen.generateOre(world, random, x, z, Ore.LAPIS)) {
            this.genStandardOre2(world, random, x, z, 1, this.lapisGen, 16, 16);
        }
        for (OldTerrainGen.Listener listener : OldTerrainGen.listeners()) {
            listener.oreGenPost(world, random, x, z);
        }
    }
}
