package dimdoors.world;

import dimdoors.DimDoors;
import dimdoors.block.DimDoorBlock;
import dimdoors.block.DimDoorBlockEntity;
import dimdoors.entity.Monolith;
import dimdoors.gen.DungeonBuilder;
import dimdoors.gen.Dungeons;
import dimdoors.gen.RiftGen;
import dimdoors.world.DDData.DimData;
import dimdoors.world.DDData.LinkData;
import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldLiveGen;
import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * dimHelper, pocketTeleporter and SchematicLoader of Dimensional Doors 1.4.1 on the server: links between rifts,
 * pockets (personal ones walled in Fabric of Reality, dungeons from schematics), and sending entities through.
 *
 * <p>Dimension ids are those of 1.5.2: 0 the overworld, -1 the Nether, 1 the End, {@value DimDoors#LIMBO_ID} Limbo,
 * and the pockets theirs (see {@link DDData}).
 */
public final class DDWorld {
    private static final byte[] NO_BLOCKS = new byte[256 * OldChunk.HEIGHT];
    private static final byte[] NO_BIOMES = new byte[256];
    /** The unseeded Random of dimHelper (and of the other classes that kept one). */
    public static final Random RAND = new Random();
    /** mod_pocketDim.teleTimer: ticks before a non-player may be sent again. */
    public static int teleTimer;
    private static MinecraftServer server;

    private DDWorld() {
    }

    public static void setServer(MinecraftServer current) {
        server = current;
    }

    public static MinecraftServer server() {
        return server;
    }

    public static DDData data() {
        return DDData.get(server);
    }

    // ---- dimensions ----

    /** The 1.5.2 id of the dimension at x/z of the level; {@link Integer#MIN_VALUE} for the pocket level outside any pocket. */
    public static int dimId(Level level, int x, int z) {
        ResourceKey<Level> key = level.dimension();
        if (key == Level.OVERWORLD) {
            return 0;
        }
        if (key == Level.NETHER) {
            return -1;
        }
        if (key == Level.END) {
            return 1;
        }
        if (key == DimDoors.LIMBO) {
            return DimDoors.LIMBO_ID;
        }
        DDData data = data();
        if (key == DimDoors.POCKET) {
            DimData pocket = data.pocketAt(x, z);
            return pocket == null ? Integer.MIN_VALUE : pocket.id;
        }
        String name = key.identifier().toString();
        Integer id = data.otherLevels.get(name);
        if (id == null) {
            id = 2;
            while (data.dims.containsKey(id) || data.otherLevels.containsValue(id) || id == DimDoors.LIMBO_ID) {
                id++;
            }
            data.otherLevels.put(name, id);
            data.setDirty();
        }
        return id;
    }

    public static int dimId(Level level, BlockPos pos) {
        return dimId(level, pos.getX(), pos.getZ());
    }

    /** The level of a dimension id, or {@code null}. */
    public static ServerLevel level(int dim) {
        if (dim == 0) {
            return server.overworld();
        }
        if (dim == -1) {
            return server.getLevel(Level.NETHER);
        }
        if (dim == 1) {
            return server.getLevel(Level.END);
        }
        if (dim == DimDoors.LIMBO_ID) {
            return server.getLevel(DimDoors.LIMBO);
        }
        DDData data = data();
        DimData d = data.dim(dim);
        if (d != null && d.isPocket) {
            return server.getLevel(DimDoors.POCKET);
        }
        for (var entry : data.otherLevels.entrySet()) {
            if (entry.getValue() == dim) {
                return server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(entry.getKey())));
            }
        }
        return null;
    }

    public static boolean isPocket(Level level) {
        return level.dimension() == DimDoors.POCKET;
    }

    public static boolean isLimbo(Level level) {
        return level.dimension() == DimDoors.LIMBO;
    }

    /** x in the frame of the original, where the pocket stood at the coordinates of its door. */
    public static int virtualX(int dim, int x) {
        DimData d = data().dim(dim);
        return d != null && d.isPocket ? x - d.offsetX : x;
    }

    public static int virtualZ(int dim, int z) {
        DimData d = data().dim(dim);
        return d != null && d.isPocket ? z - d.offsetZ : z;
    }

    /** getDimDepth: 1 for unknown dimensions. */
    public static int depth(int dim) {
        DimData d = data().dim(dim);
        return d != null ? d.depth : 1;
    }

    // ---- links ----

    public static LinkData link(int dim, int x, int y, int z) {
        DimData d = data().dim(dim);
        return d == null ? null : d.link(x, y, z);
    }

    public static LinkData link(Level level, BlockPos pos) {
        return link(dimId(level, pos), pos.getX(), pos.getY(), pos.getZ());
    }

    public static int flipDoorMetadata(int data) {
        return switch (data) {
            case 0 -> 2;
            case 1 -> 3;
            case 2 -> 0;
            case 3 -> 1;
            case 4 -> 6;
            case 5 -> 7;
            case 6 -> 4;
            case 7 -> 5;
            default -> -10;
        };
    }

    public static LinkData createLink(int locDim, int destDim, int locX, int locY, int locZ, int destX, int destY, int destZ) {
        LinkData existing = link(locDim, locX, locY, locZ);
        return createLink(locDim, destDim, locX, locY, locZ, destX, destY, destZ, existing != null ? existing.orientation : -10);
    }

    public static LinkData createLink(int locDim, int destDim, int locX, int locY, int locZ, int destX, int destY, int destZ, int orientation) {
        return createLink(new LinkData(locDim, destDim, locX, locY, locZ, destX, destY, destZ, false, orientation), true);
    }

    /** createLink: puts the link into the dimension of its location and a rift there, unless something immune is in the way. */
    public static LinkData createLink(LinkData link, boolean placeRift) {
        DDData data = data();
        if (!data.dims.containsKey(link.locDim)) {
            data.dims.put(link.locDim, new DimData(link.locDim, false, 0, link.locDim, link.locX, link.locY, link.locZ));
            link.isLocPocket = false;
        }
        if (!data.dims.containsKey(link.destDim)) {
            data.dims.put(link.destDim, new DimData(link.destDim, false, 0, link.locDim, link.locX, link.locY, link.locZ));
        }
        DimData loc = data.dims.get(link.locDim);
        link.isLocPocket = loc.isPocket;
        loc.links.put(DDData.key(link.locX, link.locY, link.locZ), link);
        data.setDirty();
        ServerLevel level = level(link.locDim);
        BlockPos pos = new BlockPos(link.locX, link.locY, link.locZ);
        if (level != null && level.isLoaded(pos)) {
            if (placeRift && !DimDoors.immuneToRift(level.getBlockState(pos))) {
                level.setBlock(pos, DimDoors.RIFT.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            refreshDoor(level, pos);
        }
        return link;
    }

    public static void removeLink(int dim, int x, int y, int z) {
        DDData data = data();
        DimData d = data.dims.get(dim);
        if (d == null) {
            return;
        }
        d.links.remove(DDData.key(x, y, z));
        data.setDirty();
        ServerLevel level = level(dim);
        if (level != null) {
            refreshDoor(level, new BlockPos(x, y, z));
        }
    }

    /** Tells the door there (and the one below) whether it is linked, for its portal. */
    public static void refreshDoor(ServerLevel level, BlockPos pos) {
        for (BlockPos at : new BlockPos[] {pos, pos.below()}) {
            if (level.isLoaded(at) && level.getBlockEntity(at) instanceof DimDoorBlockEntity door) {
                door.update();
            }
        }
    }

    /** getRandomLinkData. */
    public static LinkData randomLink(boolean allowInPocket) {
        DDData data = data();
        int size = data.dims.size();
        int i = 0;
        while (size > 0 && i < 100) {
            i++;
            List<LinkData> links = new ArrayList<>();
            size--;
            while (size > 0) {
                Object[] keys = data.dims.keySet().toArray();
                DimData d = data.dims.get(keys[RAND.nextInt(keys.length)]);
                if (d == null) {
                    break;
                }
                links = d.links();
                if (!links.isEmpty()) {
                    break;
                }
            }
            if (links.isEmpty()) {
                break;
            }
            LinkData link = links.get(RAND.nextInt(links.size()));
            if (link == null || link.isLocPocket && !allowInPocket) {
                continue;
            }
            return link;
        }
        return null;
    }

    public static int destOrientation(LinkData link) {
        if (link == null) {
            return 0;
        }
        LinkData dest = link(link.destDim, link.destX, link.destY, link.destZ);
        return dest != null ? dest.orientation : 0;
    }

    // ---- pockets ----

    /** createPocket: a new pocket behind the link, one deeper than where it starts (or one up). */
    public static LinkData createPocket(LinkData link, boolean isGoingDown, boolean isRandomRift) {
        DDData data = data();
        int id = data.nextFreeId();
        DimData loc = data.dims.get(link.locDim);
        DimData dest;
        if (loc != null) {
            if (loc.isPocket) {
                dest = new DimData(id, true, loc.depth + (isGoingDown ? 1 : -1), loc.exitDim, loc.exitX, loc.exitY, loc.exitZ);
            } else {
                dest = new DimData(id, true, 1, link.locDim, link.locX, link.locY, link.locZ);
            }
        } else {
            loc = new DimData(link.locDim, false, 0, link.locDim, link.locX, link.locY, link.locZ);
            dest = new DimData(id, true, 1, link.locDim, link.locX, link.locY, link.locZ);
        }
        dest.isDimRandomRift = isRandomRift;
        // the pocket stood at the destination's coordinates in a dimension of its own; here it gets a cell
        int vx = virtualX(link.locDim, link.destX);
        int vz = virtualZ(link.locDim, link.destZ);
        data.dims.put(link.locDim, loc);
        data.dims.put(id, dest);
        data.assignCell(dest);
        int centerX = dest.cellX * DDData.CELL;
        int centerZ = dest.cellZ * DDData.CELL;
        dest.offsetX = centerX - vx;
        dest.offsetZ = centerZ - vz;
        data.setDirty();
        LinkData out = createLink(link.locDim, id, link.locX, link.locY, link.locZ, centerX, link.destY, centerZ, link.orientation);
        createLink(id, link.locDim, centerX, link.destY, centerZ, link.locX, link.locY, link.locZ, flipDoorMetadata(link.orientation));
        if (isRandomRift) {
            generateDungeonLink(out);
        }
        return out;
    }

    /** DungeonHelper.generateDungeonLink: which dungeon the new pocket gets. */
    public static void generateDungeonLink(LinkData incoming) {
        int depth = depth(incoming.locDim);
        Dungeons.Dungeon dungeon = Dungeons.choose(RAND, depth, incoming.destY, () -> chainOf(data().dims.get(incoming.locDim)));
        DimData dest = data().dims.get(incoming.destDim);
        if (dest != null) {
            dest.dungeon = dungeon == null ? null : dungeon.path();
            data().setDirty();
        }
    }

    /** getDungeonDataInChain: the dungeons from the first pocket of the chain down. Throws where the original did. */
    private static Collection<Dungeons.Dungeon> chainOf(DimData dim) {
        LinkData first = link(dim.exitDim, dim.exitX, dim.exitY, dim.exitZ);
        DimData start = data().dims.get(first.destDim);
        return below(start);
    }

    private static List<Dungeons.Dungeon> below(DimData dim) {
        List<Dungeons.Dungeon> found = new ArrayList<>();
        Dungeons.Dungeon own = dim.dungeon == null ? null : Dungeons.byPath(dim.dungeon);
        if (own != null) {
            found.add(own);
            for (LinkData link : dim.links()) {
                DimData next = data().dims.get(link.destDim);
                if (next != null && next.dungeon != null && depth(link.destDim) == dim.depth + 1) {
                    for (Dungeons.Dungeon d : below(next)) {
                        if (!found.contains(d)) {
                            found.add(d);
                        }
                    }
                }
            }
        }
        return found;
    }

    /** generatePocket: fills a pocket the first time something goes in. */
    public static void generatePocket(LinkData incoming) {
        DimData data = data().dims.get(incoming.destDim);
        if (data == null || data.hasBeenFilled || !data.isPocket) {
            return;
        }
        ServerLevel level = server.getLevel(DimDoors.POCKET);
        if (!data.isDimRandomRift) {
            data.hasBeenFilled = true;
            data().setDirty();
            int x = incoming.destX;
            int y = incoming.destY + 13;
            int z = incoming.destZ;
            switch (incoming.orientation) {
                case 0 -> x += 15;
                case 1 -> z += 15;
                case 2 -> x -= 15;
                case 3 -> z -= 15;
                default -> {
                }
            }
            BlockState fabric = DimDoors.FABRIC.get().defaultBlockState();
            BlockState eternal = DimDoors.ETERNAL_FABRIC.get().defaultBlockState();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int dx = -19; dx <= 19; dx++) {
                for (int dy = -19; dy <= 19; dy++) {
                    for (int dz = -19; dz <= 19; dz++) {
                        if ((Math.abs(dx) >= 15 || Math.abs(dy) >= 15 || Math.abs(dz) >= 15) && y + dy > 0 && y + dy < 256) {
                            pos.set(x + dx, y + dy, z + dz);
                            if (level.getBlockState(pos).isAir()) {
                                boolean inner = Math.abs(dx) < 19 && Math.abs(dy) < 19 && Math.abs(dz) < 19;
                                level.setBlock(pos, inner ? fabric : eternal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                            }
                        }
                    }
                }
            }
        } else {
            if (data.dungeon == null) {
                generateDungeonLink(incoming);
            }
            String path = data.dungeon != null ? data.dungeon : Dungeons.DEFAULT_BREAK.path();
            generateSchematic(level, incoming, path);
        }
    }

    /** SchematicLoader.generateSchematic with the dungeon of the pocket behind {@code incoming}. */
    private static void generateSchematic(ServerLevel level, LinkData incoming, String path) {
        int destDim = incoming.destDim;
        int originDim = incoming.locDim;
        DimData data = data().dims.get(destDim);
        data.hasBeenFilled = true;
        data().setDirty();
        OldWorld world = new OldWorld(level.getSeed(), (cx, cz) -> new OldChunk(cx, cz, NO_BLOCKS, NO_BIOMES));
        int depth = depth(originDim) + 1;
        DungeonBuilder.Result result = DungeonBuilder.build(world, path, incoming.destX, incoming.destY, incoming.destZ, incoming.orientation,
                virtualX(destDim, incoming.destX), virtualZ(destDim, incoming.destZ), depth);
        if (result.minX() > result.maxX()) {
            return;
        }
        data.box = new int[] {result.minX(), result.minY(), result.minZ(), result.maxX(), result.maxY(), result.maxZ()};
        OldLiveGen.place(level, world, result.minX(), result.minY(), result.minZ(), result.maxX(), result.maxY(), result.maxZ());
        Random rand = result.random();
        int[] entrance = result.entrance();
        LinkData back = link(destDim, entrance[0], entrance[1], entrance[2]);
        if (back != null) {
            back.orientation = world.getBlockMetadata(entrance[0], entrance[1] - 1, entrance[2]);
        }
        for (DungeonBuilder.Door door : result.sideDoors()) {
            LinkData side = new LinkData(destDim, 0, door.x(), door.y(), door.z(), door.destX(), door.destY() + 1, door.destZ(), true, door.direction());
            createPocket(side, true, true);
        }
        DimData origin = data().dims.get(originDim);
        for (int[] door : result.exitDoors()) {
            int direction = world.getBlockMetadata(door[0], door[1] - 1, door[2]);
            LinkData random = randomLink(false);
            int target = origin != null ? origin.exitDim : 0;
            if (target == DimDoors.LIMBO_ID) {
                target = 0;
            } else if (rand.nextBoolean() && random != null) {
                target = random.locDim;
            }
            ServerLevel targetLevel = level(target);
            if (targetLevel == null) {
                continue;
            }
            int vx = virtualX(destDim, door[0]);
            int vz = virtualZ(destDim, door[2]);
            int y = firstUncovered(targetLevel, vx, 10, vz);
            if (y < 5) {
                y = 70;
            }
            LinkData exit = new LinkData(destDim, target, door[0], door[1], door[2], vx, y, vz, true, direction);
            createLink(exit, true);
            createLink(target, destDim, vx, y, vz, door[0], door[1], door[2], flipDoorMetadata(direction));
            BlockPos marker = new BlockPos(door[0], door[1] - 2, door[2]);
            int belowId = world.getBlockId(door[0], door[1] - 3, door[2]);
            BlockState replace = belowId == RiftGen.FABRIC ? Blocks.STONE_BRICKS.defaultBlockState()
                    : belowId == 0 ? Blocks.AIR.defaultBlockState() : OldBlockStates.get(belowId, world.getBlockMetadata(door[0], door[1] - 3, door[2]));
            level.setBlock(marker, replace, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        for (int[] frame : result.monoliths()) {
            Monolith mob = DimDoors.MONOLITH.get().create(level, EntitySpawnReason.STRUCTURE);
            if (mob != null) {
                mob.snapTo(frame[0], frame[1], frame[2], 1.0F, 1.0F);
                level.addFreshEntity(mob);
            }
        }
        Dungeons.Dungeon dungeon = Dungeons.byPath(path);
        if (dungeon != null && !dungeon.open()) {
            for (int cx = result.minX() >> 4; cx <= result.maxX() >> 4; cx++) {
                for (int cz = result.minZ() >> 4; cz <= result.maxZ() >> 4; cz++) {
                    Monolith.placeInPocketChunk(level, cx, cz);
                }
            }
        }
    }

    /**
     * resetPocket: a dungeon nobody is in is emptied (the original deleted the folder of its dimension) and built anew
     * the next time something goes in.
     */
    public static boolean resetPocket(DimData dim) {
        ServerLevel level = server.getLevel(DimDoors.POCKET);
        if (!dim.isPocket || level == null) {
            return false;
        }
        for (ServerPlayer player : level.players()) {
            DimData in = data().pocketAt(player.getBlockX(), player.getBlockZ());
            if (in != null && in.id == dim.id) {
                return false;
            }
        }
        if (dim.box != null) {
            int[] b = dim.box;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = b[0]; x <= b[3]; x++) {
                for (int z = b[2]; z <= b[5]; z++) {
                    for (int y = b[1]; y <= b[4]; y++) {
                        if (!level.getBlockState(pos.set(x, y, z)).isAir()) {
                            level.removeBlockEntity(pos);
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        }
                    }
                }
            }
            for (Entity entity : level.getEntities((Entity) null, new net.minecraft.world.phys.AABB(b[0], b[1], b[2], b[3] + 1, b[4] + 1, b[5] + 1),
                    e -> !(e instanceof Player))) {
                entity.discard();
            }
            dim.box = null;
        }
        dim.hasBeenFilled = false;
        for (LinkData link : dim.links()) {
            link.hasGennedDoor = false;
            LinkData out = link(link.destDim, link.destX, link.destY, link.destZ);
            if (out != null) {
                out.hasGennedDoor = false;
            }
        }
        data().setDirty();
        return true;
    }

    /** yCoordHelper.getFirstUncovered: from yStart up to the first two free blocks; the lower of them. */
    public static int firstUncovered(ServerLevel level, int x, int yStart, int z) {
        LevelChunk chunk = level.getChunk(x >> 4, z >> 4);
        int y = yStart;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean blocked;
        do {
            blocked = y > 0 && (solid(chunk.getBlockState(pos.set(x, y - 1, z))) || solid(chunk.getBlockState(pos.set(x, y, z))));
        } while (blocked && ++y < 245);
        return y - 1;
    }

    private static boolean solid(BlockState state) {
        return !state.isAir() && (!state.canBeReplaced() || !state.getFluidState().isEmpty());
    }

    // ---- going through ----

    /** teleportToPocket: sends the entity along the link, building what lies behind it first. */
    public static void teleport(Level world, LinkData link, Entity entity) {
        if (world.isClientSide() || link == null || !(world instanceof ServerLevel from)) {
            return;
        }
        DDData data = data();
        int worldDim = dimId(world, entity.blockPosition());
        if (!data.dims.containsKey(link.destDim) || !data.dims.containsKey(worldDim)) {
            return;
        }
        generatePocket(link);
        if (teleTimer != 0 && !(entity instanceof Player)) {
            return;
        }
        teleTimer = 2 + RAND.nextInt(2);
        Entity moved = teleportEntity(from, entity, link);
        if (moved == null) {
            return;
        }
        Level now = moved.level();
        now.playSound(null, moved.getX(), moved.getY(), moved.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
        BlockPos feet = BlockPos.containing(moved.getX(), moved.getY(), moved.getZ());
        DimData locData = data.dims.get(link.locDim);
        if (!now.getBlockState(feet.below()).isSolidRender() && locData != null && locData.isDimRandomRift && !link.hasGennedDoor) {
            for (int count = 0; count < 20 && !now.getBlockState(feet.below(1 + count)).isSolidRender(); count++) {
                if (count == 19) {
                    now.setBlock(feet.below(), DimDoors.FABRIC.get().defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        if (now.getBlockState(feet.below()).is(Blocks.LAVA)) {
            now.setBlock(feet.below(), DimDoors.FABRIC.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        generateDoor(from, link);
        for (BlockPos at : new BlockPos[] {feet.above(), feet}) {
            BlockState state = now.getBlockState(at);
            if (!state.isAir() && state.isSolidRender() && !DimDoors.immuneToRift(state)) {
                now.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    /** pocketTeleporter.placeInPortal: in front of the door at the destination, looking out of it. */
    private static Entity teleportEntity(ServerLevel from, Entity entity, LinkData link) {
        ServerLevel dest = level(link.destDim);
        if (dest == null) {
            return null;
        }
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            entity.stopRiding();
        }
        int id = destOrientation(link);
        double x = link.destX;
        double y = link.destY;
        double z = link.destZ;
        switch (id) {
            case 2, 6 -> {
                x += 1.5;
                z += 0.5;
            }
            case 3, 7 -> {
                x += 0.5;
                z += 1.5;
            }
            case 0, 4 -> {
                x -= 0.5;
                z += 0.5;
            }
            case 1, 5 -> {
                x += 0.5;
                z -= 0.5;
            }
            default -> {
            }
        }
        if (entity instanceof Player) {
            y -= 1;
        }
        Vec3 speed = Vec3.ZERO;
        if (entity instanceof AbstractMinecart) {
            speed = switch (id) {
                case 2, 6 -> new Vec3(0.39, 0, 0);
                case 3, 7 -> new Vec3(0, 0, 0.39);
                case 0, 4 -> new Vec3(-0.39, 0, 0);
                case 1, 5 -> new Vec3(0, 0, -0.39);
                default -> Vec3.ZERO;
            };
        }
        dest.getChunk(BlockPos.containing(x, y, z));
        Entity moved = entity.teleport(new TeleportTransition(dest, new Vec3(x, y, z), speed, id * 90 + 90, entity.getXRot(), Set.of(),
                TeleportTransition.DO_NOTHING));
        if (moved != null && vehicle != null) {
            Entity cart = teleportEntity(from, vehicle, link);
            if (cart != null) {
                moved.startRiding(cart, true, true);
            }
        }
        if (moved instanceof ServerPlayer player) {
            player.resetFallDistance();
        }
        return moved;
    }

    /** generateDoor: a door of the same kind on the other side, where none stands yet. */
    public static void generateDoor(ServerLevel world, LinkData incoming) {
        if (incoming.hasGennedDoor) {
            return;
        }
        BlockState here = world.getBlockState(new BlockPos(incoming.locX, incoming.locY, incoming.locZ));
        if (!(here.getBlock() instanceof DimDoorBlock door) || !door.makesDoorOnOtherSide()) {
            return;
        }
        ServerLevel dest = level(incoming.destDim);
        if (dest == null) {
            return;
        }
        LinkData destLink = link(incoming.destDim, incoming.destX, incoming.destY, incoming.destZ);
        int orientation = 0;
        if (destLink != null) {
            orientation = destLink.orientation;
            destLink.hasGennedDoor = true;
        }
        BlockPos top = new BlockPos(incoming.destX, incoming.destY, incoming.destZ);
        if (!(dest.getBlockState(top).getBlock() instanceof DimDoorBlock other) || !other.makesDoorOnOtherSide()) {
            dest.setBlock(top.below(), DimDoorBlock.state(door, orientation, false), Block.UPDATE_CLIENTS);
            dest.setBlock(top, DimDoorBlock.state(door, 8, false), Block.UPDATE_CLIENTS);
        }
        incoming.hasGennedDoor = true;
        data().setDirty();
    }

    /** LimboProvider.getRandomizedSpawnPoint: anywhere 5000 blocks around 0, 0, high up. */
    public static BlockPos limboSpawn(Random random) {
        return new BlockPos(random.nextInt(10000) - 5000, 700, random.nextInt(10000) - 5000);
    }

    /** The direction of 1.5.2's door metadata (bits 0-1). */
    public static Direction direction(int meta) {
        return new Direction[] {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH}[meta & 3];
    }

    public static int meta(Direction direction) {
        return switch (direction) {
            case EAST -> 0;
            case SOUTH -> 1;
            case WEST -> 2;
            default -> 3;
        };
    }

    /** MathHelper.floor_double((yaw + 90) * 4 / 360 + 0.5) & 3: the door direction an entity looks along. */
    public static int facing(Entity entity) {
        return (int) Math.floor((entity.getYRot() + 90.0F) * 4.0F / 360.0F + 0.5) & 3;
    }

    /** floor((yaw + 180) * 4 / 360 - 0.5) & 3: the direction ItemDoor placed a door in. */
    public static int placeFacing(Entity entity) {
        return (int) Math.floor((entity.getYRot() + 180.0F) * 4.0F / 360.0F - 0.5) & 3;
    }
}
