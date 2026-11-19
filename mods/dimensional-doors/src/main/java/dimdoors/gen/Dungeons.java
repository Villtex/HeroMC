package dimdoors.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * The dungeons of DungeonHelper (Dimensional Doors 1.4.1): the schematics of the mod in their lists by kind, the
 * dungeons of the custom schematic folder, and the choice of the next dungeon of a chain (generateDungeonLink).
 */
public final class Dungeons {
    /** DungeonGenerator: a schematic with its weight; "open" dungeons have no monoliths and spawn no mobs. */
    public record Dungeon(int weight, String path, boolean open) {
        /** The file name without ".schematic" (what dd-rift lists). */
        public String name() {
            String file = new File(this.path).getName();
            return file.substring(0, file.length() - ".schematic".length());
        }
    }

    public static final Pattern SCHEMATIC_NAME = Pattern.compile("[A-Za-z0-9_\\-]+");
    private static final List<String> TYPES = List.of("hub", "trap", "simplehall", "complexhall", "exit", "deadend", "maze");

    public static final Dungeon DEFAULT_BREAK = new Dungeon(100, "/schematics/somethingBroke.schematic", true);
    public static final Dungeon DEFAULT_UP = new Dungeon(100, "/schematics/simpleStairsUp.schematic", true);

    private static final List<Dungeon> SIMPLE_HALLS = new ArrayList<>();
    private static final List<Dungeon> COMPLEX_HALLS = new ArrayList<>();
    private static final List<Dungeon> DEAD_ENDS = new ArrayList<>();
    private static final List<Dungeon> HUBS = new ArrayList<>();
    private static final List<Dungeon> MAZES = new ArrayList<>();
    private static final List<Dungeon> PISTON_TRAPS = new ArrayList<>();
    private static final List<Dungeon> EXITS = new ArrayList<>();
    public static final List<Dungeon> REGISTERED = new ArrayList<>();
    public static final List<Dungeon> CUSTOM = new ArrayList<>();

    private Dungeons() {
    }

    private static List<Dungeon> ofType(String type) {
        return switch (type) {
            case "simplehall" -> SIMPLE_HALLS;
            case "complexhall" -> COMPLEX_HALLS;
            case "hub" -> HUBS;
            case "exit" -> EXITS;
            case "deadend" -> DEAD_ENDS;
            case "maze" -> MAZES;
            default -> PISTON_TRAPS;
        };
    }

    /** DungeonHelper(): the custom schematics of {@code customFolder} (may be {@code null}), then the mod's own. */
    public static synchronized void initialize(File customFolder) {
        if (!REGISTERED.isEmpty()) {
            return;
        }
        File[] files = customFolder == null ? null : customFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".schematic")) {
                    registerCustom(file);
                }
            }
        }
        registerBase();
    }

    static boolean validateSchematicName(String name) {
        if (!name.endsWith(".schematic")) {
            return false;
        }
        String[] data = name.substring(0, name.length() - ".schematic".length()).split("_");
        if (data.length < 3 || data.length > 4 || !TYPES.contains(data[0].toLowerCase(Locale.ROOT))
                || !SCHEMATIC_NAME.matcher(data[1]).matches()
                || !data[2].equalsIgnoreCase("open") && !data[2].equalsIgnoreCase("closed")) {
            return false;
        }
        if (data.length == 4) {
            try {
                int weight = Integer.parseInt(data[3]);
                return weight >= 0 && weight <= 10000;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static void registerCustom(File file) {
        String name = file.getName();
        String path = file.getAbsolutePath();
        if (validateSchematicName(name)) {
            String[] data = name.substring(0, name.length() - ".schematic".length()).split("_");
            Dungeon dungeon = new Dungeon(data.length == 4 ? Integer.parseInt(data[3]) : 100, path, data[2].equalsIgnoreCase("open"));
            ofType(data[0].toLowerCase(Locale.ROOT)).add(dungeon);
            REGISTERED.add(dungeon);
            CUSTOM.add(dungeon);
        } else {
            CUSTOM.add(new Dungeon(100, path, true));
        }
    }

    private static void add(List<Dungeon> list, int weight, String name, boolean open) {
        list.add(new Dungeon(weight, "/schematics/" + name + ".schematic", open));
    }

    private static void registerBase() {
        add(HUBS, 200, "4WayBasicHall", false);
        add(HUBS, 200, "4WayHallExit", false);
        add(HUBS, 100, "doorTotemRuins", true);
        add(HUBS, 100, "hallwayTrapRooms1", false);
        add(HUBS, 100, "longDoorHallway", false);
        add(HUBS, 100, "smallRotundaWithExit", false);
        add(HUBS, 100, "fortRuins", true);
        add(HUBS, 10, "Hub_SK-Claustrophobia_Open_10", true);
        add(HUBS, 50, "Hub_SK-HeartOfDisorder_Open_50", true);
        add(SIMPLE_HALLS, 100, "collapsedSingleTunnel1", false);
        add(SIMPLE_HALLS, 100, "singleStraightHall1", false);
        add(SIMPLE_HALLS, 100, "smallBranchWithExit", false);
        add(SIMPLE_HALLS, 100, "smallSimpleLeft", false);
        add(SIMPLE_HALLS, 100, "smallSimpleRight", false);
        add(SIMPLE_HALLS, 100, "simpleStairsUp", false);
        add(SIMPLE_HALLS, 100, "simpleStairsDown", false);
        add(SIMPLE_HALLS, 100, "simpleSmallT1", false);
        add(SIMPLE_HALLS, 50, "SimpleHall_SK-LeftDownStairs_Open_50", true);
        add(SIMPLE_HALLS, 50, "SimpleHall_SK-LeftUpPath_Open_50", true);
        add(SIMPLE_HALLS, 50, "SimpleHall_SK-RightDownStairs_Open_50", true);
        add(SIMPLE_HALLS, 50, "SimpleHall_SK-RightUpPath_Open_50", true);
        add(SIMPLE_HALLS, 100, "SimpleHall_SK-SpiralHallway_Open_100", true);
        add(SIMPLE_HALLS, 100, "complexHall_largeBrokenHall_closed_100", false);
        add(COMPLEX_HALLS, 100, "tntPuzzleTrap", false);
        add(COMPLEX_HALLS, 100, "brokenPillarsO", true);
        add(COMPLEX_HALLS, 100, "buggyTopEntry1", true);
        add(COMPLEX_HALLS, 100, "exitRuinsWithHiddenDoor", true);
        add(COMPLEX_HALLS, 100, "hallwayHiddenTreasure", false);
        add(COMPLEX_HALLS, 100, "mediumPillarStairs", true);
        add(COMPLEX_HALLS, 100, "ruinsO", true);
        add(COMPLEX_HALLS, 100, "pitStairs", true);
        add(COMPLEX_HALLS, 100, "ComplexHall_SK-HiddenStairs_Open_100", true);
        add(COMPLEX_HALLS, 10, "ComplexHall_SK-LostGarden_Open_10", true);
        add(DEAD_ENDS, 100, "azersDungeonO", false);
        add(DEAD_ENDS, 100, "diamondTowerTemple1", true);
        add(DEAD_ENDS, 100, "fallingTrapO", true);
        add(DEAD_ENDS, 100, "hiddenStaircaseO", true);
        add(DEAD_ENDS, 100, "lavaTrapO", true);
        add(DEAD_ENDS, 100, "randomTree", true);
        add(DEAD_ENDS, 100, "smallHiddenTowerO", true);
        add(DEAD_ENDS, 100, "smallSilverfishRoom", false);
        add(DEAD_ENDS, 100, "tntTrapO", true);
        add(DEAD_ENDS, 100, "smallDesert", true);
        add(DEAD_ENDS, 100, "smallPond", true);
        add(DEAD_ENDS, 100, "DeadEnd_SK-FarAwayInTheDark_Open_100", true);
        add(DEAD_ENDS, 50, "DeadEnd_SK-UnstableDesert_Open_50", true);
        add(PISTON_TRAPS, 200, "hallwayPitFallTrap", false);
        add(PISTON_TRAPS, 200, "pistonFloorHall", false);
        add(PISTON_TRAPS, 200, "wallFallcomboPistonHall", false);
        add(PISTON_TRAPS, 100, "fakeTNTTrap", false);
        add(PISTON_TRAPS, 100, "pistonFallRuins", true);
        add(PISTON_TRAPS, 100, "pistonSmasherHall", false);
        add(PISTON_TRAPS, 100, "simpleDropHall", false);
        add(PISTON_TRAPS, 100, "fallingTNThall", false);
        add(PISTON_TRAPS, 100, "lavaPyramid", true);
        add(PISTON_TRAPS, 10, "Trap_SK-RestlessCorridor_Open_10", true);
        add(PISTON_TRAPS, 100, "trap_pistonFloorPlatform_closed_100", false);
        add(PISTON_TRAPS, 50, "trap_pistonFloorPlatform2_closed_100", false);
        add(MAZES, 100, "smallMaze1", false);
        add(MAZES, 100, "smallMultilevelMaze", false);
        add(EXITS, 200, "lockingExitHall", false);
        add(EXITS, 100, "exitCube", true);
        add(EXITS, 100, "smallExitPrison", true);
        REGISTERED.addAll(SIMPLE_HALLS);
        REGISTERED.addAll(EXITS);
        REGISTERED.addAll(PISTON_TRAPS);
        REGISTERED.addAll(MAZES);
        REGISTERED.addAll(DEAD_ENDS);
        REGISTERED.addAll(COMPLEX_HALLS);
        REGISTERED.addAll(HUBS);
    }

    /** Sorted names of all dungeons (dd-rift list). */
    public static List<String> names() {
        HashSet<String> names = new HashSet<>();
        for (Dungeon d : REGISTERED) {
            names.add(d.name());
        }
        for (Dungeon d : CUSTOM) {
            names.add(d.name());
        }
        List<String> sorted = new ArrayList<>(names);
        Collections.sort(sorted);
        return sorted;
    }

    /** The dungeon whose name contains the query and is the shortest such name, or {@code null} (dd-rift). */
    public static Dungeon byPartialName(String query, Collection<Dungeon> dungeons) {
        String normal = query.toLowerCase(Locale.ROOT);
        Dungeon best = null;
        int length = Integer.MAX_VALUE;
        for (Dungeon dungeon : dungeons) {
            String name = dungeon.name().toLowerCase(Locale.ROOT);
            if (name.length() < length && name.contains(normal)) {
                length = name.length();
                best = dungeon;
            }
        }
        return best;
    }

    /** Finds a dungeon by its path (saved data). */
    public static Dungeon byPath(String path) {
        for (List<Dungeon> list : List.of(REGISTERED, CUSTOM)) {
            for (Dungeon d : list) {
                if (d.path.equals(path)) {
                    return d;
                }
            }
        }
        if (DEFAULT_BREAK.path.equals(path)) {
            return DEFAULT_BREAK;
        }
        return DEFAULT_UP.path.equals(path) ? DEFAULT_UP : null;
    }

    /** WeightedRandom.getRandomItem over the weights of the dungeons. */
    static Dungeon randomDungeon(Random random, Collection<Dungeon> dungeons) {
        int total = 0;
        for (Dungeon d : dungeons) {
            total += d.weight;
        }
        if (total <= 0) {
            return null;
        }
        int pick = random.nextInt(total);
        for (Dungeon d : dungeons) {
            pick -= d.weight;
            if (pick < 0) {
                return d;
            }
        }
        return null;
    }

    private static Dungeon pick(Random rand, List<Dungeon> list) {
        return list.get(rand.nextInt(list.size()));
    }

    /**
     * DungeonHelper.generateDungeonLink: the dungeon behind a door at {@code depth} (of the pocket the door is in)
     * whose destination is at height {@code destY}. {@code chain} gives the dungeons already in the chain; it may throw,
     * as getDungeonDataInChain could, and then a random dungeon of all is taken.
     */
    public static Dungeon choose(Random rand, int depth, int destY, Supplier<Collection<Dungeon>> chain) {
        int depthWeight = rand.nextInt(depth + 2) + rand.nextInt(depth + 2) - 2;
        int count = 10;
        boolean flag;
        Dungeon dungeon;
        try {
            if (destY <= 15) {
                return DEFAULT_UP;
            }
            do {
                count--;
                flag = true;
                dungeon = randomDungeon(rand, REGISTERED);
                if (depth <= 1) {
                    if (rand.nextBoolean()) {
                        dungeon = pick(rand, COMPLEX_HALLS);
                    } else if (rand.nextBoolean()) {
                        dungeon = pick(rand, HUBS);
                    } else if (rand.nextBoolean()) {
                        dungeon = pick(rand, HUBS);
                    } else if (DEAD_ENDS.contains(dungeon) || EXITS.contains(dungeon)) {
                        flag = false;
                    }
                } else if (depth > 3 || !DEAD_ENDS.contains(dungeon) && !EXITS.contains(dungeon) && !rand.nextBoolean()) {
                    if (rand.nextInt(3) == 0 && !COMPLEX_HALLS.contains(dungeon)) {
                        if (rand.nextInt(3) == 0) {
                            dungeon = pick(rand, SIMPLE_HALLS);
                        } else if (rand.nextBoolean()) {
                            dungeon = pick(rand, PISTON_TRAPS);
                        } else if (depth < 4) {
                            dungeon = pick(rand, HUBS);
                        }
                    } else if (depthWeight - depthWeight / 2 <= depth - 4 || !DEAD_ENDS.contains(dungeon) && !EXITS.contains(dungeon)) {
                        if (depthWeight > 7 && HUBS.contains(dungeon)) {
                            if (rand.nextInt(12) + 5 < depthWeight) {
                                if (rand.nextBoolean()) {
                                    dungeon = pick(rand, EXITS);
                                } else if (rand.nextBoolean()) {
                                    dungeon = pick(rand, DEAD_ENDS);
                                } else {
                                    dungeon = pick(rand, PISTON_TRAPS);
                                }
                            } else {
                                flag = false;
                            }
                        } else if (depth > 10 && HUBS.contains(dungeon)) {
                            flag = false;
                        }
                    } else if (rand.nextBoolean()) {
                        dungeon = pick(rand, SIMPLE_HALLS);
                    } else if (rand.nextBoolean()) {
                        dungeon = pick(rand, COMPLEX_HALLS);
                    } else if (rand.nextBoolean()) {
                        dungeon = pick(rand, PISTON_TRAPS);
                    } else {
                        flag = false;
                    }
                } else if (rand.nextBoolean()) {
                    dungeon = pick(rand, HUBS);
                } else if (rand.nextBoolean()) {
                    dungeon = pick(rand, MAZES);
                } else if (rand.nextBoolean()) {
                    dungeon = pick(rand, PISTON_TRAPS);
                } else {
                    flag = false;
                }
                if (chain.get().contains(dungeon)) {
                    flag = false;
                }
            } while (!flag && count > 0);
        } catch (RuntimeException e) {
            if (REGISTERED.isEmpty()) {
                return null;
            }
            dungeon = randomDungeon(rand, REGISTERED);
        }
        return dungeon;
    }
}
