package chococraft.entity;

import chococraft.ChocoConfig;
import chococraft.ChocoCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

/**
 * ServerSpawnTickHandler + ChocoboSpawner: every spawnTimeDelay ticks, for every player, a spawnProbability % chance
 * of a flock of spawnGroupMin to spawnGroupMax - 1 wild chocobos 32 to 95 blocks away (Nether/End: 32 to 79),
 * yellow in the spawn biomes, purple anywhere in the Nether and the End. No flock with another player within 16 blocks
 * of the spot, with more than spawnTotalMax wild chocobos within spawnLimitChunkRadius blocks (the original's "chunk"
 * radius counted blocks) or a wild chocobo closer than distanceNextWild. Each chocobo needs a 3 x 3 floor of full
 * blocks, nothing but non-cubes 3 high above and no liquid at its feet.
 */
public final class ChocoboSpawner {
    private static int ticks;

    private ChocoboSpawner() {
    }

    public static void tick(ServerTickEvent.Post event) {
        if (++ticks < ChocoConfig.SPAWN_TIME_DELAY.get()) {
            return;
        }
        ticks = 0;
        MinecraftServer server = event.getServer();
        for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
            doChocoboSpawning(player.level(), player.getX(), player.getZ());
        }
    }

    private static boolean isHell(Level level) {
        return level.dimension() == Level.NETHER || level.dimension() == Level.END;
    }

    public static int doChocoboSpawning(ServerLevel level, double posX, double posZ) {
        RandomSource random = level.getRandom();
        if (random.nextInt(100) > ChocoConfig.SPAWN_PROBABILITY.get()) {
            return 0;
        }
        boolean hell = isHell(level);
        int outer = hell ? 48 : 64;
        int inner = 32;
        int dx = 0;
        int dz = 0;
        boolean canSpawnHere = false;
        for (int i = 0; i < 20; i++) {
            dx = random.nextInt(outer) + inner;
            dz = random.nextInt(outer) + inner;
            if (random.nextBoolean()) {
                dx = -dx;
            }
            if (random.nextBoolean()) {
                dz = -dz;
            }
            canSpawnHere = hell || inSpawnBiome(level, posX + dx, posZ + dz);
            if (canSpawnHere && isOtherPlayerNear(level, posX + dx, posZ + dz, inner / 2)) {
                canSpawnHere = false;
            }
            if (canSpawnHere) {
                break;
            }
        }
        if (!canSpawnHere) {
            return 0;
        }
        int cx = Mth.floor(posX + dx);
        int cz = Mth.floor(posZ + dz);
        if (countWild(level, cx, cz, ChocoConfig.SPAWN_LIMIT_CHUNK_RADIUS.get()) > ChocoConfig.SPAWN_TOTAL_MAX.get()) {
            return 0;
        }
        if (distanceToNextWild(level, cx, cz) < ChocoConfig.DISTANCE_NEXT_WILD.get()) {
            return 0;
        }
        int spawned = 0;
        int groupSize = ChocoConfig.SPAWN_GROUP_MIN.get();
        int delta = ChocoConfig.SPAWN_GROUP_MAX.get() - groupSize;
        if (delta > 0) {
            groupSize += random.nextInt(delta);
        }
        for (int i = 0; i < 20; i++) {
            int x = (int) posX + dx + random.nextInt(6);
            int z = (int) posZ + dz + random.nextInt(6);
            int y = hell ? firstUncoveredBlock(level, x, z) : level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            float yaw = random.nextInt(360);
            if (canSpawnAt(level, x, y, z)) {
                Chocobo chocobo = ChocoCraft.chocoboType(hell ? ChocoboColor.PURPLE : ChocoboColor.YELLOW).create(level, EntitySpawnReason.NATURAL);
                if (chocobo != null) {
                    chocobo.snapTo(x, y, z, yaw, 0.0F);
                    chocobo.getChocoboName();
                    level.addFreshEntity(chocobo);
                    if (++spawned >= groupSize) {
                        break;
                    }
                }
            }
        }
        return spawned;
    }

    /** World.getFirstUncoveredBlock: from y 63 up to the first block with air above. */
    private static int firstUncoveredBlock(ServerLevel level, int x, int z) {
        int y = 63;
        while (y < level.getMaxY() && !level.getBlockState(new BlockPos(x, y + 1, z)).isAir()) {
            y++;
        }
        return y;
    }

    /** Biomes of 1.5.2 (worldgen152, ExtrabiomesXL): they spawn wild chocobos only as configured. */
    private static final TagKey<Biome> OLD_BIOMES = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("worldgen152", "old_biomes"));
    /** Biomes of today and of other mods (Hexxit Integrated): the tags that stand for the configured old biomes. */
    private static final TagKey<Biome> WILD_SPAWNS = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(ChocoCraft.MOD_ID, "wild_chocobo_spawns"));

    private static boolean inSpawnBiome(ServerLevel level, double x, double z) {
        BlockPos pos = BlockPos.containing(x, 64, z);
        Holder<Biome> holder = level.getBiome(pos);
        if (!holder.is(OLD_BIOMES)) {
            return holder.is(WILD_SPAWNS);
        }
        Identifier biome = holder.unwrapKey().map(k -> k.identifier()).orElse(null);
        return biome != null && ChocoConfig.SPAWN_BIOMES.get().contains(biome.toString());
    }

    private static boolean isOtherPlayerNear(ServerLevel level, double x, double z, int distance) {
        int ix = Mth.floor(x);
        int iz = Mth.floor(z);
        int iy = isHell(level) ? firstUncoveredBlock(level, ix, iz) : level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ix, iz);
        return level.getNearestPlayer(ix, iy, iz, distance, false) != null;
    }

    /** countWildInChunkRadius: wild chocobos with x and z within radius blocks of the spot. */
    private static int countWild(ServerLevel level, int x, int z, int radius) {
        int count = 0;
        for (Chocobo chocobo : wild(level)) {
            if (Math.abs(Mth.floor(chocobo.getX()) - x) <= radius && Math.abs(Mth.floor(chocobo.getZ()) - z) <= radius) {
                count++;
            }
        }
        return count;
    }

    private static int distanceToNextWild(ServerLevel level, double x, double z) {
        double distance = 10000.0;
        for (Chocobo chocobo : wild(level)) {
            double d = Math.sqrt((x - chocobo.getX()) * (x - chocobo.getX()) + (z - chocobo.getZ()) * (z - chocobo.getZ()));
            distance = Math.min(distance, d);
        }
        return Mth.floor(distance);
    }

    private static List<Chocobo> wild(ServerLevel level) {
        List<Chocobo> result = new java.util.ArrayList<>();
        for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
            if (e instanceof Chocobo chocobo && !chocobo.isTame()) {
                result.add(chocobo);
            }
        }
        return result;
    }

    /** canChocoboSpawnAtLocation. */
    private static boolean canSpawnAt(ServerLevel level, int x, int y, int z) {
        if (!normalCubesAround(level, x, y - 1, z) || normalCubesAround(level, x, y, z)) {
            return false;
        }
        if (level.getBlockState(new BlockPos(x, y, z)).liquid()) {
            return false;
        }
        return !normalCubesAround(level, x, y + 1, z) && !normalCubesAround(level, x, y + 2, z);
    }

    /** isNormalCubesAround: all nine blocks of the 3 x 3 square are full opaque cubes. */
    private static boolean normalCubesAround(ServerLevel level, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int xx = x - 1; xx <= x + 1; xx++) {
            for (int zz = z - 1; zz <= z + 1; zz++) {
                pos.set(xx, y, zz);
                BlockState state = level.getBlockState(pos);
                if (!state.isRedstoneConductor(level, pos)) {
                    return false;
                }
            }
        }
        return true;
    }
}
