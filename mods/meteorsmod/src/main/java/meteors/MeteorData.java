package meteors;

import meteors.block.MeteorShieldBlockEntity;
import meteors.entity.FallingMeteor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HandlerMeteor of the overworld: the announced meteors (GhostMeteor, ghostMets.bin), the crash sites of the last
 * 20 meteors (CrashedChunkSet, crashedMetChunks.bin; no new meteor within 3 chunks of one), the meteor shields and
 * the last meteor each shield owner's land was protected from. The shields are not saved: as in the original every
 * shield registers itself again when its block entity first ticks.
 */
public final class MeteorData extends SavedData {
    public static final SavedDataType<MeteorData> TYPE = new SavedDataType<>(MeteorsMod.id("meteors"), MeteorData::new,
            CompoundTag.CODEC.xmap(MeteorData::load, MeteorData::save));

    /** GhostMeteor: a meteor that will fall at x, z once tick reaches goal (counted at night only). */
    public static final class Ghost {
        public final int x;
        public final int z;
        public final int size;
        public final MeteorType type;
        int tick;
        final int goal;

        Ghost(int x, int z, int size, int tick, int goal, MeteorType type) {
            this.x = x;
            this.z = z;
            this.size = size;
            this.tick = tick;
            this.goal = goal;
            this.type = type;
        }

        public int remainingTicks() {
            return this.goal - this.tick;
        }
    }

    /** CrashedChunkSet: the 7x7 chunks around a crash site and its age in crashes. */
    record Crash(int chunkX, int chunkZ, BlockPos pos, int age) {
        boolean contains(int cx, int cz) {
            return Math.abs(cx - this.chunkX) <= 3 && Math.abs(cz - this.chunkZ) <= 3;
        }
    }

    /** A shield: its chunk, the radius in chunks (ShieldRadiusMultiplier * power) and the owner's name. */
    public record Shield(int chunkX, int chunkZ, int radius, String owner) {
        /**
         * addSafeChunks: the chunk row and column through the shield plus the four quarter triangles, that is every
         * chunk within the Manhattan distance of the radius.
         */
        boolean protects(int cx, int cz) {
            return this.radius >= 0 && Math.abs(cx - this.chunkX) + Math.abs(cz - this.chunkZ) <= this.radius;
        }
    }

    final List<Ghost> ghosts = new ArrayList<>();
    final List<Crash> crashes = new ArrayList<>();
    private final Map<BlockPos, Shield> shields = new LinkedHashMap<>();
    private final Map<String, MeteorType> lastPrevented = new HashMap<>();
    /** HandlerMeteorTick: ticks counted towards the next announcement, -1 = no goal yet. Not saved (as in 1.5.2). */
    int ticks;
    int tickGoal = -1;

    public MeteorData() {
    }

    public static MeteorData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    // ---- shields ----

    public void addShield(BlockPos pos, Shield shield) {
        this.shields.put(pos.immutable(), shield);
    }

    public void removeShield(BlockPos pos) {
        this.shields.remove(pos);
    }

    public boolean isProtected(int chunkX, int chunkZ) {
        for (Shield shield : this.shields.values()) {
            if (shield.protects(chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    /** getSafeChunkCoords: the owners of the shields protecting that chunk, each once. */
    public Set<String> owners(int chunkX, int chunkZ) {
        Set<String> owners = new LinkedHashSet<>();
        for (Shield shield : this.shields.values()) {
            if (shield.protects(chunkX, chunkZ) && owners.stream().noneMatch(o -> o.equalsIgnoreCase(shield.owner()))) {
                owners.add(shield.owner());
            }
        }
        return owners;
    }

    public MeteorType lastPrevented(@Nullable String owner) {
        return owner == null ? MeteorType.METEORITE : this.lastPrevented.getOrDefault(owner, MeteorType.METEORITE);
    }

    /**
     * A meteor of that type was stopped over the chunk: every owner gets the "meteor blocked" message and advancement
     * (meteorProtectCheck), and the shields of the owner show the colour of that type from now on.
     */
    public void meteorPrevented(ServerLevel level, int chunkX, int chunkZ, MeteorType type) {
        for (String owner : this.owners(chunkX, chunkZ)) {
            this.lastPrevented.put(owner, type);
            ServerPlayer player = level.getServer().getPlayerList().getPlayerByName(owner);
            if (player != null) {
                player.sendSystemMessage(Component.translatable("MeteorShield.meteorBlocked").withStyle(net.minecraft.ChatFormatting.GREEN));
                MeteorEvents.grant(player, "meteor_blocked");
            }
            for (Map.Entry<BlockPos, Shield> entry : this.shields.entrySet()) {
                if (entry.getValue().owner().equalsIgnoreCase(owner) && level.isLoaded(entry.getKey())
                        && level.getBlockEntity(entry.getKey()) instanceof MeteorShieldBlockEntity shield) {
                    shield.setLastStopped(type);
                }
            }
        }
        this.setDirty();
    }

    // ---- meteors ----

    /** canSpawnNewMeteor: at most three announced meteors. */
    public boolean canSpawnNewMeteor() {
        return this.ghosts.size() < 3;
    }

    public boolean canSpawnNewMeteorAt(int chunkX, int chunkZ) {
        for (Crash crash : this.crashes) {
            if (crash.contains(chunkX, chunkZ)) {
                return false;
            }
        }
        return true;
    }

    public void readyNewMeteor(ServerLevel level, int x, int z, int size, int goal, MeteorType type) {
        if (this.canSpawnNewMeteor()) {
            this.ghosts.add(new Ghost(x, z, size, 0, goal, type));
            this.setDirty();
            MeteorNetwork.sendToAll(level.getServer());
            if (type == MeteorType.KITTY) {
                level.getServer().getPlayerList().broadcastSystemMessage(
                        Component.translatable("Meteor.kittiesDetected.one").withStyle(net.minecraft.ChatFormatting.DARK_RED), false);
                level.getServer().getPlayerList().broadcastSystemMessage(
                        Component.translatable("Meteor.kittiesDetected.two").withStyle(net.minecraft.ChatFormatting.DARK_RED), false);
            }
        }
    }

    /** updateMeteors: counts the announced meteors down at night and lets the ready ones fall. */
    void updateMeteors(ServerLevel level) {
        boolean night = !level.isBrightOutside() || !MeteorsConfig.ONLY_AT_NIGHT.get();
        boolean changed = false;
        // the index loop of the original: after a removal the next meteor waits a tick
        for (int i = 0; i < this.ghosts.size(); i++) {
            if (!night) {
                continue;
            }
            Ghost ghost = this.ghosts.get(i);
            int cx = ghost.x >> 4;
            int cz = ghost.z >> 4;
            if (!this.canSpawnNewMeteorAt(cx, cz)) {
                this.ghosts.remove(i);
                changed = true;
                continue;
            }
            ghost.tick++;
            if (ghost.tick >= ghost.goal) {
                if (this.isProtected(cx, cz)) {
                    this.meteorPrevented(level, cx, cz, ghost.type);
                } else if (ghost.type == MeteorType.KITTY) {
                    this.kittyAttack(level);
                } else {
                    FallingMeteor meteor = new FallingMeteor(level, ghost.size, ghost.x, ghost.z, ghost.type, false);
                    keepLoaded(level, ghost.x, ghost.z);
                    level.addFreshEntity(meteor);
                    this.applyMeteorCrash(level, ghost.x, firstUncoveredBlock(level, ghost.x, ghost.z), ghost.z);
                    MeteorEvents.playCrashSound(level, meteor);
                }
                this.ghosts.remove(i);
                changed = true;
            }
        }
        if (changed) {
            MeteorNetwork.sendToAll(level.getServer());
        }
        if (!this.ghosts.isEmpty()) {
            this.setDirty();
        }
    }

    /** The meteor's chunks stay loaded while it falls (in 1.5.2 getChunkFromBlockCoords loaded them for good). */
    public static void keepLoaded(ServerLevel level, int x, int z) {
        level.getChunkSource().addTicketWithRadius(TicketType.PORTAL, new ChunkPos(x >> 4, z >> 4), 3);
    }

    /** World.getFirstUncoveredBlock: from y 63 up to the first block with air above. */
    public static int firstUncoveredBlock(ServerLevel level, int x, int z) {
        int y = 63;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y + 1, z);
        while (!level.isEmptyBlock(pos.setY(y + 1)) && y < level.getMaxY()) {
            y++;
        }
        return y;
    }

    /** kittyAttack: 51..114 kitty comets within 64 blocks of every player, each after up to 99 ticks. */
    public void kittyAttack(ServerLevel level) {
        level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("Meteor.kittyAttack")
                .withStyle(net.minecraft.ChatFormatting.DARK_RED), false);
        net.minecraft.util.RandomSource random = level.getRandom();
        for (ServerPlayer player : level.players()) {
            for (int r = random.nextInt(64) + 50; r >= 0; r--) {
                int x = random.nextInt(64);
                int z = random.nextInt(64);
                if (random.nextBoolean()) {
                    x = -x;
                }
                if (random.nextBoolean()) {
                    z = -z;
                }
                x = (int) (x + player.getX());
                z = (int) (z + player.getZ());
                FallingMeteor kitty = new FallingMeteor(level, 1, x, z, MeteorType.KITTY, false);
                kitty.setSpawnPauseTicks(random.nextInt(100));
                keepLoaded(level, x, z);
                level.addFreshEntity(kitty);
            }
            MeteorEvents.grant(player, "comet_kitty_event");
        }
    }

    /** applyMeteorCrash: ages the old crash sites (gone after 20), adds this one and tells the clients. */
    void applyMeteorCrash(ServerLevel level, int x, int y, int z) {
        List<Crash> aged = new ArrayList<>();
        for (Crash crash : this.crashes) {
            if (crash.age() + 1 < 20) {
                aged.add(new Crash(crash.chunkX(), crash.chunkZ(), crash.pos(), crash.age() + 1));
            }
        }
        this.crashes.clear();
        this.crashes.addAll(aged);
        this.crashes.add(new Crash(x >> 4, z >> 4, new BlockPos(x, y, z), 0));
        this.setDirty();
        MeteorNetwork.sendToAll(level.getServer());
        if (MeteorsConfig.TEXT_CRASH_NOTIFICATION.get()) {
            level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("Meteor.crashed"), false);
        }
    }

    /** getNearestTimeMeteor: the announced meteor (no kitty) with the fewest ticks left. */
    public @Nullable Ghost nearestTimeMeteor() {
        Ghost closest = null;
        for (Ghost ghost : this.ghosts) {
            if (ghost.type != MeteorType.KITTY && (closest == null || ghost.remainingTicks() < closest.remainingTicks())) {
                closest = ghost;
            }
        }
        return closest;
    }

    /** getLastCrashLocation: the crash of age 0. */
    public @Nullable BlockPos lastCrash() {
        for (Crash crash : this.crashes) {
            if (crash.age() == 0) {
                return crash.pos();
            }
        }
        return null;
    }

    public List<Ghost> ghosts() {
        return this.ghosts;
    }

    // ---- saving ----

    private static MeteorData load(CompoundTag tag) {
        MeteorData data = new MeteorData();
        for (Tag t : tag.getListOrEmpty("ghosts")) {
            if (t instanceof CompoundTag g) {
                data.ghosts.add(new Ghost(g.getIntOr("x", 0), g.getIntOr("z", 0), g.getIntOr("size", 1), g.getIntOr("tick", 0),
                        g.getIntOr("goal", 0), MeteorType.byId(g.getIntOr("type", 0))));
            }
        }
        for (Tag t : tag.getListOrEmpty("crashes")) {
            if (t instanceof CompoundTag c) {
                data.crashes.add(new Crash(c.getIntOr("cx", 0), c.getIntOr("cz", 0),
                        new BlockPos(c.getIntOr("x", 0), c.getIntOr("y", 0), c.getIntOr("z", 0)), c.getIntOr("age", 0)));
            }
        }
        CompoundTag prevented = tag.getCompoundOrEmpty("prevented");
        for (String owner : prevented.keySet()) {
            data.lastPrevented.put(owner, MeteorType.byId(prevented.getIntOr(owner, 0)));
        }
        return data;
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag ghosts = new ListTag();
        for (Ghost ghost : this.ghosts) {
            CompoundTag g = new CompoundTag();
            g.putInt("x", ghost.x);
            g.putInt("z", ghost.z);
            g.putInt("size", ghost.size);
            g.putInt("tick", ghost.tick);
            g.putInt("goal", ghost.goal);
            g.putInt("type", ghost.type.id());
            ghosts.add(g);
        }
        tag.put("ghosts", ghosts);
        ListTag crashes = new ListTag();
        for (Crash crash : this.crashes) {
            CompoundTag c = new CompoundTag();
            c.putInt("cx", crash.chunkX());
            c.putInt("cz", crash.chunkZ());
            c.putInt("x", crash.pos().getX());
            c.putInt("y", crash.pos().getY());
            c.putInt("z", crash.pos().getZ());
            c.putInt("age", crash.age());
            crashes.add(c);
        }
        tag.put("crashes", crashes);
        CompoundTag prevented = new CompoundTag();
        this.lastPrevented.forEach((owner, type) -> prevented.putInt(owner, type.id()));
        tag.put("prevented", prevented);
        return tag;
    }
}
