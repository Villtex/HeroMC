package ruins.gen;

import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * RuinTemplate of Ruins 10.3: one .tml file - its variables, rules and layers - and how it checks and builds its site.
 * Parsing, the checks and the build order are those of the original, mistakes included.
 */
final class RuinTemplate {
    private final String name;
    private int[] targets;
    private int height;
    private int width;
    private int length;
    private int overhang;
    private int weight = 1;
    private int embed = 1;
    private int leveling;
    private int lbuffer;
    private int cutIn;
    private int cbuffer;
    private int wOff;
    private int lOff;
    private boolean preserveWater;
    private boolean preserveLava;
    private boolean preservePlants;
    private boolean unique;
    private final List<RuinTemplateRule> rules = new ArrayList<>();
    private final List<int[][]> layers = new ArrayList<>();
    private final Set<String> biomes = new HashSet<>();

    /** {@code name}: the file's path, which the original used as name, equality and hash (File.getCanonicalPath). */
    RuinTemplate(String name, List<String> lines) throws Exception {
        this.name = name;
        this.parseFile(lines);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RuinTemplate other && other.name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    String getName() {
        return this.name;
    }

    int getWeight() {
        return this.weight;
    }

    Set<String> getBiomesToSpawnIn() {
        return this.biomes;
    }

    int getMinDistance() {
        return Math.max(this.width, this.length) + Math.max(this.cbuffer, this.lbuffer);
    }

    boolean isUnique() {
        return this.unique;
    }

    boolean isAir(int blockID) {
        return switch (blockID) {
            case 0, 30, 31, 32, 37, 38, 39, 40, 59, 78, 106, 111 -> true;
            default -> this.preserveBlock(blockID);
        };
    }

    boolean preserveBlock(int blockID) {
        if (this.preserveWater && (blockID == 8 || blockID == 9 || blockID == 79)) {
            return true;
        }
        if (this.preserveLava && (blockID == 10 || blockID == 11)) {
            return true;
        }
        if (this.preservePlants) {
            switch (blockID) {
                case 17, 18, 81, 83, 86, 100, 99, 104, 105, 106, 111:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    boolean isAcceptable(OldWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        for (int target : this.targets) {
            if (id == target) {
                return true;
            }
        }
        return false;
    }

    boolean checkArea(OldWorld world, int xBase, int y, int zBase, int rotate) {
        int x = xBase + this.wOff;
        int z = zBase + this.lOff;
        int xDim = this.width;
        int zDim = this.length;
        if (rotate == 1 || rotate == 3) {
            x = xBase + this.lOff;
            z = zBase + this.wOff;
            xDim = this.length;
            zDim = this.width;
        }
        for (int x1 = 0; x1 < xDim; x1++) {
            for (int z1 = 0; z1 < zDim; z1++) {
                if (!this.isAir(world.getBlockId(x + x1, y, z + z1)) && !this.isAcceptable(world, x + x1, y, z + z1)) {
                    return false;
                }
            }
        }
        if (this.leveling > 0) {
            for (int x1 = -this.lbuffer; x1 < xDim + this.lbuffer * 2; x1++) {
                for (int z1 = -this.lbuffer; z1 < zDim + this.lbuffer * 2; z1++) {
                    if (this.isAir(world.getBlockId(x + x1, y - (this.leveling + 1), z + z1))) {
                        return false;
                    }
                }
            }
        } else {
            int aircount = 0;
            for (int x1 = 0; x1 < xDim; x1++) {
                for (int z1 = 0; z1 < zDim; z1++) {
                    if (world.getBlockId(x + x1, y - 1, z + z1) == 0) {
                        aircount++;
                    }
                    if (aircount > this.overhang) {
                        return false;
                    }
                }
            }
        }
        if (this.cutIn > 0) {
            int cutHeight = this.cutIn + 1;
            for (int x1 = -this.cbuffer; x1 < xDim + 2 * this.cbuffer; x1++) {
                for (int z1 = -this.cbuffer; z1 < zDim + 2 * this.cbuffer; z1++) {
                    if (!this.isAir(world.getBlockId(x + x1, y + cutHeight, z + z1))) {
                        return false;
                    }
                }
            }
        } else {
            int mod = this.height - this.embed;
            for (int y1 = 1; y1 < mod; y1++) {
                for (int x1 = 0; x1 < xDim; x1++) {
                    for (int z1 = 0; z1 < zDim; z1++) {
                        if (!this.isAir(world.getBlockId(x + x1, y + y1, z + z1))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /** RuinBoundingBox: xMin, xMax, zMin, zMax (the maxima only add the buffer once). */
    int[] getBoundingBox(int x, int z, int rotate) {
        int add = Math.max(this.cbuffer, this.lbuffer);
        if (rotate != 1 && rotate != 3) {
            int xMin = x + this.wOff - add;
            int zMin = z + this.lOff - add;
            return new int[] {xMin, xMin + this.width + add, zMin, zMin + this.length + add};
        }
        int xMin = x + this.lOff - add;
        int zMin = z + this.wOff - add;
        return new int[] {xMin, xMin + this.length + add, zMin, zMin + this.width + add};
    }

    /**
     * The block columns the site may read or change around (x, z), as {minX, minZ, maxX, maxZ} inclusive: the
     * template, the leveling and cut-in buffers (the checks look twice as far on the far side) and the neighbours
     * the adjacent rules look at.
     */
    int[] extent(int xBase, int zBase, int rotate) {
        boolean eastwest = rotate == 1 || rotate == 3;
        int x = xBase + (eastwest ? this.lOff : this.wOff);
        int z = zBase + (eastwest ? this.wOff : this.lOff);
        int xDim = eastwest ? this.length : this.width;
        int zDim = eastwest ? this.width : this.length;
        int b = Math.max(this.lbuffer, this.cbuffer);
        return new int[] {x - b - 1, z - b - 1, x + xDim + 2 * b, z + zDim + 2 * b};
    }

    void doBuild(OldWorld world, Random random, int xBase, int y, int zBase, int rotate) {
        List<Object[]> laterun = new ArrayList<>();
        List<Object[]> lastrun = new ArrayList<>();
        int yOff = 1 - this.embed;
        boolean eastwest;
        int x;
        int z;
        int xDim;
        int zDim;
        if (rotate != 1 && rotate != 3) {
            eastwest = false;
            x = xBase + this.wOff;
            xDim = this.width;
            z = zBase + this.lOff;
            zDim = this.length;
        } else {
            eastwest = true;
            x = xBase + this.lOff;
            xDim = this.length;
            z = zBase + this.wOff;
            zDim = this.width;
        }
        if (this.cutIn > 0) {
            this.cutInSite(world, xBase, y, zBase, eastwest);
        }
        if (this.leveling > 0) {
            this.levelSite(world, world.getBlockId(xBase, y, zBase), xBase, y, zBase, eastwest);
        }
        for (int[][] layer : this.layers) {
            for (int x1 = 0; x1 < xDim; x1++) {
                for (int z1 = 0; z1 < zDim; z1++) {
                    int rulenum = switch (rotate) {
                        case 1 -> ruleAt(layer, z1, xDim - (x1 + 1));
                        case 2 -> ruleAt(layer, xDim - (x1 + 1), zDim - (z1 + 1));
                        case 3 -> ruleAt(layer, zDim - (z1 + 1), x1);
                        default -> ruleAt(layer, x1, z1);
                    };
                    RuinTemplateRule rule = this.rules.get(rulenum);
                    if (rule.runLater()) {
                        laterun.add(new Object[] {rule, x + x1, y + yOff, z + z1});
                    } else if (rule.runLast()) {
                        lastrun.add(new Object[] {rule, x + x1, y + yOff, z + z1});
                    } else {
                        rule.doBlock(world, random, x + x1, y + yOff, z + z1, rotate);
                    }
                }
            }
            yOff++;
        }
        for (Object[] p : laterun) {
            ((RuinTemplateRule) p[0]).doBlock(world, random, (int) p[1], (int) p[2], (int) p[3], rotate);
        }
        for (Object[] p : lastrun) {
            ((RuinTemplateRule) p[0]).doBlock(world, random, (int) p[1], (int) p[2], (int) p[3], rotate);
        }
    }

    /** RuinTemplateLayer.getRuleAt: 0 (air) outside the layer. */
    private static int ruleAt(int[][] layer, int x, int z) {
        if (x < 0 || x >= layer.length || z < 0 || z >= layer[x].length) {
            return 0;
        }
        return layer[x][z];
    }

    private void cutInSite(OldWorld world, int xBase, int y, int zBase, boolean eastwest) {
        int x = xBase + this.wOff - this.cbuffer;
        int z = zBase + this.lOff - this.cbuffer;
        int xDim = this.width + 2 * this.cbuffer;
        int zDim = this.length + 2 * this.cbuffer;
        int yDim = Math.max(this.cutIn, this.height);
        if (eastwest) {
            x = xBase + this.lOff - this.cbuffer;
            z = zBase + this.wOff - this.cbuffer;
            xDim = this.length + 2 * this.cbuffer;
            zDim = this.width + 2 * this.cbuffer;
        }
        y++;
        for (int y1 = 0; y1 < yDim; y1++) {
            for (int x1 = 0; x1 < xDim; x1++) {
                for (int z1 = 0; z1 < zDim; z1++) {
                    if (!this.preserveBlock(world.getBlockId(x + x1, y + y1, z + z1))) {
                        world.setBlock(x + x1, y + y1, z + z1, 0, 0);
                    }
                }
            }
        }
    }

    private void levelSite(OldWorld world, int fillBlockID, int xBase, int y, int zBase, boolean eastwest) {
        int x = xBase + this.wOff - this.lbuffer;
        int z = zBase + this.lOff - this.lbuffer;
        int xDim = this.width + 2 * this.lbuffer;
        int zDim = this.length + 2 * this.lbuffer;
        int yDim = -(this.leveling + 1);
        if (eastwest) {
            x = xBase + this.lOff - this.lbuffer;
            z = zBase + this.wOff - this.lbuffer;
            xDim = this.length + 2 * this.lbuffer;
            zDim = this.width + 2 * this.lbuffer;
        }
        for (int y1 = 0; y1 > yDim; y1--) {
            for (int x1 = 0; x1 < xDim; x1++) {
                for (int z1 = 0; z1 < zDim; z1++) {
                    if (this.isAir(world.getBlockId(x + x1, y + y1, z + z1))) {
                        world.setBlock(x + x1, y + y1, z + z1, fillBlockID, 0);
                    }
                }
            }
        }
    }

    private void parseFile(List<String> lines) throws Exception {
        this.parseVariables(lines);
        this.rules.add(new AirRule(this));
        Iterator<String> i = lines.iterator();
        while (i.hasNext()) {
            String line = i.next();
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }
            if (line.startsWith("layer")) {
                List<String> layerlines = new ArrayList<>();
                for (String row = i.next(); !row.startsWith("endlayer"); row = i.next()) {
                    if (row.charAt(0) != '#') {
                        layerlines.add(row);
                    }
                }
                this.layers.add(parseLayer(layerlines, this.width, this.length));
            } else if (line.startsWith("rule")) {
                this.rules.add(new RuinTemplateRule(this, line.split("=")[1]));
            }
        }
    }

    /** RuinTemplateLayer: {@code width} rows of {@code length} rule numbers, each row read back to front. */
    private static int[][] parseLayer(List<String> rows, int width, int length) {
        int[][] layer = new int[width][length];
        Iterator<String> i = rows.iterator();
        for (int x = 0; x < width; x++) {
            String[] rowdata = i.next().split(",");
            for (int z = 0; z < length; z++) {
                layer[x][z] = Integer.parseInt(rowdata[length - z - 1]);
            }
        }
        return layer;
    }

    private void parseVariables(List<String> variables) throws Exception {
        for (String line : variables) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("acceptable_target_blocks")) {
                String[] check = line.split("=")[1].split(",");
                if (check.length < 1) {
                    throw new Exception("No targets specified!");
                }
                this.targets = new int[check.length];
                for (int x = 0; x < check.length; x++) {
                    this.targets[x] = Integer.parseInt(check[x]);
                }
            }
            if (line.startsWith("dimensions")) {
                String[] check = line.split("=")[1].split(",");
                this.height = Integer.parseInt(check[0]);
                this.width = Integer.parseInt(check[1]);
                this.length = Integer.parseInt(check[2]);
            } else if (line.startsWith("biomesToSpawnIn")) {
                for (String s : line.split("=")[1].split(",")) {
                    this.biomes.add(s);
                }
            } else if (line.startsWith("weight")) {
                this.weight = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("unique")) {
                if (Integer.parseInt(line.split("=")[1]) == 1) {
                    this.unique = true;
                }
            } else if (line.startsWith("embed_into_distance")) {
                this.embed = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("allowable_overhang")) {
                this.overhang = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("max_cut_in")) {
                this.cutIn = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("cut_in_buffer")) {
                this.cbuffer = Math.min(Integer.parseInt(line.split("=")[1]), 5);
            } else if (line.startsWith("max_leveling")) {
                this.leveling = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("leveling_buffer")) {
                this.lbuffer = Math.min(Integer.parseInt(line.split("=")[1]), 5);
            } else if (line.startsWith("preserve_water")) {
                if (Integer.parseInt(line.split("=")[1]) == 1) {
                    this.preserveWater = true;
                }
            } else if (line.startsWith("preserve_lava")) {
                if (Integer.parseInt(line.split("=")[1]) == 1) {
                    this.preserveLava = true;
                }
            } else if (line.startsWith("preserve_plants")) {
                if (Integer.parseInt(line.split("=")[1]) == 1) {
                    this.preservePlants = true;
                }
            }
        }
        this.wOff = this.width % 2 == 1 ? -(this.width - 1) / 2 : -this.width / 2;
        this.lOff = this.length % 2 == 1 ? -(this.length - 1) / 2 : -this.length / 2;
    }

    /** RuinRuleAir, rule 0 of every template: air where nothing preserved is, without a random draw. */
    private static final class AirRule extends RuinTemplateRule {
        AirRule(RuinTemplate owner) throws Exception {
            super(owner, "0,100,0");
        }

        @Override
        void doBlock(OldWorld world, Random random, int x, int y, int z, int rotate) {
            if (this.canReplace(0, world.getBlockId(x, y, z))) {
                world.setBlock(x, y, z, 0, 0);
            }
        }

        @Override
        boolean runLater() {
            return false;
        }
    }
}
