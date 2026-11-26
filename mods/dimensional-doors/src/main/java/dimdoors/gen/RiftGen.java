package dimdoors.gen;

import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldTerrainGen;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * RiftGenerator of Dimensional Doors 1.4.1 without game classes: clusters of rifts and rift gateways (a transient door
 * in a little stone brick ruin) in the overworld and the Nether. Limbo and the pocket dimensions are never populated
 * (their chunks came out of the generator already marked as populated), so the original's Limbo branch never ran.
 *
 * <p>Both lead into a new dungeon pocket. Which pocket that is lives in the save data of the game, not in the blocks:
 * {@link #links} is told of every rift the generator made and creates the pockets there (more than once for the same
 * chunk must do nothing).
 */
public final class RiftGen {
    public static final String FML_ID = "DimDoors";
    /** Ids of the pack's DimDoors.cfg. */
    public static final int DIMENSIONAL_DOOR = 1111;
    public static final int FABRIC = 1112;
    public static final int RIFT = 1113;
    public static final int TRANSDIMENSIONAL_TRAPDOOR = 1114;
    public static final int TRANSIENT_DOOR = 1115;
    public static final int UNSTABLE_DOOR = 1116;
    public static final int WARP_DOOR = 1117;
    /** "world generation block ids": the terrain of Limbo and the walls of the pockets. */
    public static final int UNRAVELED_FABRIC = 217;
    public static final int ETERNAL_FABRIC = 220;
    /** Pack values ("Cluster Generation Chance", "Gateway Generation Chance", out of 10000). */
    static final int CLUSTER_CHANCE = 3;
    static final int GATEWAY_CHANCE = 40;

    /** The game side: turns the rifts into links to new dungeon pockets. */
    public interface Links {
        /** A rift gateway: the transient door's lower half at x/y/z, the rift's orientation. */
        void gateway(int dimension, int x, int y, int z, int orientation);

        /** A cluster: the rifts, the first one leading to a new pocket and the others to the same place. */
        void cluster(int dimension, List<int[]> rifts, int orientation);
    }

    /** Set by the game; {@code null} in tools/expect.sh. */
    public static volatile Links links;
    /** Told of every gateway and cluster ("gateway x y z" / "cluster x y z n"), for tools/expect.sh; {@code null} in the game. */
    public static volatile java.util.function.Consumer<String> built;

    private RiftGen() {
    }

    public static void register() {
        OldBlockInfo.register(RIFT, OldBlockInfo.Material.AIR, false, 14, false);
        OldBlockInfo.register(TRANSIENT_DOOR, OldBlockInfo.Material.GRASS, false, 0, false);
        OldBlockInfo.register(DIMENSIONAL_DOOR, OldBlockInfo.Material.IRON, false, 0, false);
        OldBlockInfo.register(WARP_DOOR, OldBlockInfo.Material.WOOD, false, 0, false);
        OldBlockInfo.register(UNSTABLE_DOOR, OldBlockInfo.Material.IRON, false, 0, false);
        OldBlockInfo.register(TRANSDIMENSIONAL_TRAPDOOR, OldBlockInfo.Material.IRON, false, 0, false);
        OldBlockInfo.registerCube(FABRIC, OldBlockInfo.Material.GROUND);
        OldBlockInfo.registerCube(UNRAVELED_FABRIC, OldBlockInfo.Material.GROUND);
        OldBlockInfo.registerCube(ETERNAL_FABRIC, OldBlockInfo.Material.GROUND);
        OldTerrainGen.registerWorldGenerator(FML_ID, "dimdoors:rifts", RiftGen::generate);
        DDLoot.register();
    }

    private static void generate(Random random, int chunkX, int chunkZ, OldWorld world) {
        int dimension = world.dimension();
        if (random.nextInt(10000) < CLUSTER_CHANCE) {
            List<int[]> rifts = new ArrayList<>();
            int orientation = 0;
            do {
                int x = chunkX * 16 + random.nextInt(16);
                int z = chunkZ * 16 + random.nextInt(16);
                int y = world.getHeightValue(x, z);
                if (y >= 4 && y <= 250 && world.isAirBlock(x, y + 1, z) && world.getBlockId(x, y, z) != 7
                        && world.getBlockId(x, y - 1, z) != 7 && world.getBlockId(x, y - 2, z) != 7) {
                    if (rifts.isEmpty()) {
                        orientation = random.nextInt(4);
                    }
                    // createLink: the rift replaces whatever was there (the spot is air)
                    world.setBlock(x, y + 1, z, RIFT);
                    rifts.add(new int[] {world.realX(x), world.realY(y + 1), world.realZ(z)});
                }
            } while (random.nextInt(100) < 80);
            if (!rifts.isEmpty()) {
                report(dimension, rifts, orientation);
            }
        } else if (random.nextInt(10000) < GATEWAY_CHANCE) {
            boolean valid = false;
            int x = 0;
            int y = 0;
            int z = 0;
            for (int attempts = 0; attempts < 10 && !valid; attempts++) {
                x = chunkX * 16 + random.nextInt(16);
                z = chunkZ * 16 + random.nextInt(16);
                y = world.getHeightValue(x, z);
                valid = checkGatewayLocation(world, x, y, z);
            }
            if (valid) {
                // The original links the gateway with random.nextInt(4), but the door always faces 0: coming back
                // with 1 or 3 put the player into a pillar, and teleportToPocket then cleared its two lower blocks.
                // The draw stays for the world generation; the link takes the door's facing, as later DimDoors did.
                random.nextInt(4);
                int orientation = 0;
                // createPocket -> createLink: the rift at the door's lower half, overwritten by the door below
                world.setBlock(x, y + 1, z, RIFT);
                int block = 98;
                for (int xc = -4; xc <= 4; xc++) {
                    for (int zc = -4; zc <= 4; zc++) {
                        if (OldBlockInfo.opaqueCube(world.getBlockId(x + xc, y - 2, z + zc))) {
                            if (Math.abs(xc) + Math.abs(zc) < random.nextInt(2) + 3) {
                                world.setBlock(x + xc, y - 1, z + zc, block, 0);
                            } else if (Math.abs(xc) + Math.abs(zc) < random.nextInt(3) + 3) {
                                world.setBlock(x + xc, y - 1, z + zc, block, 2);
                            }
                        }
                    }
                }
                world.setBlock(x, y + 2, z + 1, block, 3);
                world.setBlock(x, y + 2, z - 1, block, 3);
                world.placeDoor(x, y + 1, z, 0, TRANSIENT_DOOR);
                world.setBlock(x, y + 1, z - 1, block, 0);
                world.setBlock(x, y + 1, z + 1, block, 0);
                world.setBlock(x, y, z - 1, block, 0);
                world.setBlock(x, y, z + 1, block, 0);
                Links target = links;
                if (target != null) {
                    target.gateway(dimension, world.realX(x), world.realY(y + 1), world.realZ(z), orientation);
                }
                java.util.function.Consumer<String> report = built;
                if (report != null) {
                    report.accept("gateway " + x + " " + (y + 1) + " " + z);
                }
            }
        }
    }

    private static void report(int dimension, List<int[]> rifts, int orientation) {
        Links target = links;
        if (target != null) {
            target.cluster(dimension, rifts, orientation);
        }
        java.util.function.Consumer<String> report = built;
        if (report != null) {
            int[] first = rifts.get(0);
            report.accept("cluster " + first[0] + " " + first[1] + " " + first[2] + " " + rifts.size());
        }
    }

    private static boolean checkGatewayLocation(OldWorld world, int x, int y, int z) {
        return y >= 4 && y <= 250 && world.isAirBlock(x, y + 1, z) && world.getBlockId(x, y, z) != 7 && world.getBlockId(x, y - 1, z) != 7
                && checkFoundationMaterial(world, x, y - 2, z);
    }

    private static boolean checkFoundationMaterial(OldWorld world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        OldBlockInfo.Material material = OldBlockInfo.material(id);
        return material != OldBlockInfo.Material.LEAVES && material != OldBlockInfo.Material.WOOD && material != OldBlockInfo.Material.PUMPKIN
                && OldBlockInfo.opaqueCube(id) && id != 7;
    }
}
