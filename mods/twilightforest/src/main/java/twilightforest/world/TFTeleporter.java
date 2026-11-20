package twilightforest.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForest;
import twilightforest.gen.TFTerrain;

import java.util.Set;

/**
 * TFTeleporter: the entity keeps its x and z; at the destination it lands beside the nearest Twilight portal within
 * 200 blocks, or a new portal is built near it (on grass if possible), with a ring of flowers, mushrooms and ferns.
 *
 * <p>The original scanned every block of the 401 x 401 area, which loaded and generated all its chunks; here the
 * portals of the area come from the point-of-interest index, which only knows chunks that exist. The choice among them
 * is the original one (lowest portal block of a column, nearest by distance in x, y and z).
 */
public final class TFTeleporter {
    private static final int SEARCH = 200;
    /** Radius of the circle around the search square. */
    private static final int SEARCH_RADIUS = 283;

    private TFTeleporter() {
    }

    /** onEntityCollidedWithBlock: overworld → Twilight Forest, Twilight Forest → overworld. */
    public static void travel(ServerLevel from, Entity entity) {
        ServerLevel dest = from.getServer().getLevel(from.dimension() == TwilightForest.DIMENSION ? Level.OVERWORLD : TwilightForest.DIMENSION);
        if (dest == null) {
            return;
        }
        // transferEntityToWorld: same coordinates (movement factor 1), clamped to the world border of 1.5.2
        double x = Mth.clamp(entity.getX(), -29999872.0, 29999872.0);
        double y = entity.getY();
        double z = Mth.clamp(entity.getZ(), -29999872.0, 29999872.0);
        Vec3 target = placeInExistingPortal(dest, x, y, z, entity.getRandom());
        if (target == null) {
            makePortal(dest, x, y, z);
            target = placeInExistingPortal(dest, x, y, z, entity.getRandom());
        }
        if (target == null) {
            target = new Vec3(x, y, z);
        }
        Entity moved = entity.teleport(new TeleportTransition(dest, target, Vec3.ZERO, entity.getYRot(), 0.0F, Set.of(), TeleportTransition.DO_NOTHING));
        if (moved != null) {
            moved.setPortalCooldown();
        }
    }

    private static boolean isBlockPortal(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(TwilightForest.PORTAL.get());
    }

    /** placeInExistingPortal: the position beside the nearest portal, or {@code null}. */
    private static Vec3 placeInExistingPortal(ServerLevel level, double posX, double posY, double posZ, RandomSource rand) {
        BlockPos origin = BlockPos.containing(posX, posY, posZ);
        PoiManager pois = level.getPoiManager();
        pois.ensureLoadedAndValid(level, origin, SEARCH_RADIUS);
        int l = Mth.floor(posX);
        int i1 = Mth.floor(posZ);
        double d = -1.0;
        int i = 0;
        int j = 0;
        int k = 0;
        // in the order the original scanned: x, then z ascending, y descending (ties keep the first one)
        java.util.List<BlockPos> portals = new java.util.ArrayList<>(pois.findAll(type -> type.is(TwilightForest.PORTAL_POI.getKey()), p -> true, origin,
                SEARCH_RADIUS, PoiManager.Occupancy.ANY).toList());
        portals.sort(java.util.Comparator.<BlockPos>comparingInt(p -> p.getX()).thenComparingInt(p -> p.getZ()).thenComparingInt(p -> -p.getY()));
        for (BlockPos found : portals) {
            int j1 = found.getX();
            int j2 = found.getZ();
            if (Math.abs(j1 - l) > SEARCH || Math.abs(j2 - i1) > SEARCH || found.getY() >= TFTerrain.WORLDHEIGHT || !isBlockPortal(level, found)) {
                continue;
            }
            int k2 = found.getY();
            // only the top of a run counts as found, then it walks down to the lowest portal block of the run
            if (isBlockPortal(level, found.above()) && k2 + 1 < TFTerrain.WORLDHEIGHT) {
                continue;
            }
            while (isBlockPortal(level, new BlockPos(j1, k2 - 1, j2))) {
                k2--;
            }
            double d1 = j1 + 0.5 - posX;
            double d3 = j2 + 0.5 - posZ;
            double d5 = k2 + 0.5 - posY;
            double d7 = d1 * d1 + d5 * d5 + d3 * d3;
            if (d < 0.0 || d7 < d) {
                d = d7;
                i = j1;
                j = k2;
                k = j2;
            }
        }
        if (!(d >= 0.0)) {
            return null;
        }
        double portalX = i + 0.5;
        double portalY = j + 0.5;
        double portalZ = k + 0.5;
        if (isBlockPortal(level, new BlockPos(i - 1, j, k))) {
            portalX -= 0.5;
        }
        if (isBlockPortal(level, new BlockPos(i + 1, j, k))) {
            portalX += 0.5;
        }
        if (isBlockPortal(level, new BlockPos(i, j, k - 1))) {
            portalZ -= 0.5;
        }
        if (isBlockPortal(level, new BlockPos(i, j, k + 1))) {
            portalZ += 0.5;
        }
        int xOffset = 0;
        int zOffset;
        for (zOffset = 0; xOffset == zOffset && xOffset == 0; zOffset = rand.nextInt(3) - rand.nextInt(3)) {
            xOffset = rand.nextInt(3) - rand.nextInt(3);
        }
        return new Vec3(portalX + xOffset, portalY + 1.0, portalZ + zOffset);
    }

    /** makePortal: an ideal spot (on grass), else an okay one (on something solid), else where the entity is. */
    private static void makePortal(ServerLevel level, double posX, double posY, double posZ) {
        BlockPos spot = findPortalCoords(level, posX, posY, posZ, true);
        if (spot == null) {
            spot = findPortalCoords(level, posX, posY, posZ, false);
        }
        if (spot == null) {
            double yFactor = level.dimension() == Level.OVERWORLD ? 2.0 : 0.5;
            spot = new BlockPos(Mth.floor(posX), Mth.floor(posY * yFactor), Mth.floor(posZ));
        }
        makePortalAt(level, spot.getX(), spot.getY(), spot.getZ());
    }

    private static BlockPos findPortalCoords(ServerLevel level, double posX, double posY, double posZ, boolean ideal) {
        double yFactor = level.dimension() == Level.OVERWORLD ? 2.0 : 0.5;
        int entityX = Mth.floor(posX);
        int entityZ = Mth.floor(posZ);
        double spotWeight = -1.0;
        BlockPos spot = null;
        int range = 16;
        for (int rx = entityX - range; rx <= entityX + range; rx++) {
            double xWeight = rx + 0.5 - posX;
            for (int rz = entityZ - range; rz <= entityZ + range; rz++) {
                double zWeight = rz + 0.5 - posZ;
                for (int ry = 127; ry >= 0; ry--) {
                    if (level.getBlockState(new BlockPos(rx, ry, rz)).isAir()) {
                        while (ry > 0 && level.getBlockState(new BlockPos(rx, ry - 1, rz)).isAir()) {
                            ry--;
                        }
                        if (ideal ? isPortalSpot(level, rx, rz, ry, true) : isPortalSpot(level, rx, rz, ry, false)) {
                            double yWeight = ry + 0.5 - posY * yFactor;
                            double rPosWeight = xWeight * xWeight + yWeight * yWeight + zWeight * zWeight;
                            if (spotWeight < 0.0 || rPosWeight < spotWeight) {
                                spotWeight = rPosWeight;
                                spot = new BlockPos(rx, ry, rz);
                            }
                        }
                    }
                }
            }
        }
        return spot;
    }

    /** isIdealPortal (grass below) and isOkayPortal (anything solid below): a 4x4 area, three replaceable blocks high. */
    private static boolean isPortalSpot(ServerLevel level, int rx, int rz, int ry, boolean ideal) {
        for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
            for (int potentialX = 0; potentialX < 4; potentialX++) {
                for (int potentialY = -1; potentialY < 3; potentialY++) {
                    BlockState state = level.getBlockState(new BlockPos(rx + potentialX - 1, ry + potentialY, rz + potentialZ - 1));
                    if (potentialY == -1) {
                        if (ideal ? !(state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM)) : !state.isSolid()) {
                            return false;
                        }
                    } else if (!state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void makePortalAt(ServerLevel level, int px, int py, int pz) {
        if (py < 30) {
            py = 30;
        }
        if (py > 118) {
            py = 118;
        }
        py--;
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        int[][] rim = {{-1, -1}, {0, -1}, {1, -1}, {2, -1}, {-1, 0}, {2, 0}, {-1, 1}, {2, 1}, {-1, 2}, {0, 2}, {1, 2}, {2, 2}};
        for (int[] r : rim) {
            set(level, px + r[0], py, pz + r[1], grass);
        }
        for (int[] p : new int[][] {{0, 0}, {1, 0}, {0, 1}, {1, 1}}) {
            set(level, px + p[0], py - 1, pz + p[1], Blocks.DIRT.defaultBlockState());
        }
        for (int[] p : new int[][] {{0, 0}, {1, 0}, {0, 1}, {1, 1}}) {
            set(level, px + p[0], py, pz + p[1], TwilightForest.PORTAL.get().defaultBlockState());
        }
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    set(level, px + dx, py + dy, pz + dz, Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int[] r : rim) {
            set(level, px + r[0], py + 1, pz + r[1], randNatureBlock(level.getRandom()).defaultBlockState());
        }
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    /** randNatureBlockID with metadata 2: mushrooms, fern (tall grass 2), rose, dandelion. */
    private static Block randNatureBlock(RandomSource random) {
        Block[] block = {Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.FERN, Blocks.POPPY, Blocks.DANDELION};
        return block[random.nextInt(block.length)];
    }
}
