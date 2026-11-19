package dimdoors;

import dimdoors.block.RiftBlockEntity;
import dimdoors.gen.RiftGen;
import dimdoors.world.DDData;
import dimdoors.world.DDData.DimData;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** EventHookContainer, CommonTickHandler and PlayerRespawnTracker of Dimensional Doors 1.4.1. */
final class DDEvents {
    private static final Random RAND = new Random();
    /** mod_pocketDim.limboSpawnInventory: what a player dropped dying in a pocket, given back in Limbo. */
    private static final Map<UUID, List<ItemStack>> LIMBO_INVENTORY = new HashMap<>();
    private static int tickCount;
    private static int tickCount2;

    private DDEvents() {
    }

    static void register(IEventBus bus) {
        bus.addListener(ServerStartingEvent.class, event -> {
            DDWorld.setServer(event.getServer());
            DDWorld.data().ensure(DimDoors.LIMBO_ID);
            RiftGen.links = new GeneratedRifts(event.getServer());
        });
        bus.addListener(ServerStartedEvent.class, event -> restoreRifts());
        bus.addListener(ServerStoppedEvent.class, event -> {
            RiftGen.links = null;
            DDWorld.setServer(null);
            LIMBO_INVENTORY.clear();
        });
        bus.addListener(RegisterCommandsEvent.class, event -> DDCommands.register(event.getDispatcher()));
        bus.addListener(ServerTickEvent.Pre.class, event -> tick());
        bus.addListener(LivingFallEvent.class, event -> {
            if (DDWorld.isLimbo(event.getEntity().level())) {
                event.setCanceled(true);
            }
        });
        bus.addListener(PlayerInteractEvent.RightClickBlock.class, event -> {
            if (event.getEntity() instanceof ServerPlayer && DDWorld.isLimbo(event.getLevel())
                    && event.getItemStack().getItem() instanceof BlockItem) {
                BlockPos pos = event.getPos();
                DDWorld.data().blocksToDecay().add(new int[] {pos.getX(), pos.getY(), pos.getZ()});
                DDWorld.data().setDirty();
            }
        });
        bus.addListener(LivingDropsEvent.class, event -> {
            // PlayerDropsEvent: kept for a respawn in Limbo; there the stacks went back into the inventory
            if (event.getEntity() instanceof ServerPlayer player && DimDoors.LIMBO_ENABLED && DimDoors.LIMBO_RETURNS_INVENTORY
                    && DDWorld.isPocket(player.level())) {
                List<ItemStack> stacks = new ArrayList<>();
                for (ItemEntity drop : event.getDrops()) {
                    stacks.add(drop.getItem().copy());
                }
                LIMBO_INVENTORY.put(player.getUUID(), stacks);
                event.setCanceled(true);
            }
        });
        bus.addListener(PlayerRespawnPositionEvent.class, event -> {
            // pocketProvider.getRespawnDimension: Limbo, at its randomized spawn point
            if (DimDoors.LIMBO_ENABLED && event.getEntity() instanceof ServerPlayer player && DDWorld.isPocket(player.level())) {
                ServerLevel limbo = player.level().getServer().getLevel(DimDoors.LIMBO);
                if (limbo != null) {
                    BlockPos spawn = DDWorld.limboSpawn(RAND);
                    event.setTeleportTransition(new TeleportTransition(limbo, Vec3.atBottomCenterOf(spawn), Vec3.ZERO, 0.0F, 0.0F,
                            TeleportTransition.DO_NOTHING));
                    event.setCopyOriginalSpawnPosition(false);
                }
            }
        });
        bus.addListener(PlayerEvent.PlayerRespawnEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player && DDWorld.isLimbo(player.level())) {
                List<ItemStack> stacks = LIMBO_INVENTORY.remove(player.getUUID());
                if (stacks != null) {
                    for (ItemStack stack : stacks) {
                        if (!player.getInventory().add(stack)) {
                            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack));
                        }
                    }
                }
            }
        });
    }

    /** EventHookContainer.onWorldLoad: up to 100 rifts per dimension are set again where nothing immune stands. */
    private static void restoreRifts() {
        DDData data = DDWorld.data();
        for (DimData dim : new ArrayList<>(data.dims().values())) {
            ServerLevel level = DDWorld.level(dim.id);
            if (level == null) {
                continue;
            }
            int count = 0;
            for (LinkData link : dim.links()) {
                if (count++ > 100) {
                    break;
                }
                BlockPos pos = new BlockPos(link.locX, link.locY, link.locZ);
                if (level.isLoaded(pos) && !DimDoors.immuneToRift(level.getBlockState(pos))) {
                    level.setBlock(pos, DimDoors.RIFT.get().defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    /** CommonTickHandler.onTickInGame. */
    private static void tick() {
        if (DDWorld.server() == null) {
            return;
        }
        if (tickCount > 100) {
            tickCount = 0;
            // fifteen random links get their rift back
            for (int i = 0; i < 15; i++) {
                LinkData link = DDWorld.randomLink(true);
                if (link == null) {
                    continue;
                }
                ServerLevel level = DDWorld.level(link.locDim);
                BlockPos pos = new BlockPos(link.locX, link.locY, link.locZ);
                if (level != null && level.isLoaded(pos) && !DimDoors.immuneToRift(level.getBlockState(pos))
                        && DDWorld.link(link.locDim, link.locX, link.locY, link.locZ) != null) {
                    level.setBlock(pos, DimDoors.RIFT.get().defaultBlockState(), Block.UPDATE_ALL);
                    if (level.getBlockEntity(pos) instanceof RiftBlockEntity rift) {
                        rift.hasGrownRifts = true;
                    }
                }
            }
        }
        tickCount++;
        List<int[]> decay = DDWorld.data().blocksToDecay();
        if (tickCount2 > 10) {
            tickCount2 = 0;
            ServerLevel limbo = DDWorld.server().getLevel(DimDoors.LIMBO);
            if (!decay.isEmpty() && limbo != null && decay.size() > RAND.nextInt(400)) {
                int index = RAND.nextInt(decay.size());
                int[] point = decay.get(index);
                BlockPos pos = new BlockPos(point[0], point[1], point[2]);
                BlockState state = limbo.getBlockState(pos);
                if (!state.isAir() && !state.is(DimDoors.UNRAVELED_FABRIC.get())) {
                    BlockState set = Blocks.STONE.defaultBlockState();
                    boolean change = true;
                    if (state.getBlock() instanceof EntityBlock) {
                        // the original asked this of the stone it was going to set, which never was a container
                    }
                    if (state.is(Blocks.COBBLESTONE)) {
                        set = Blocks.GRAVEL.defaultBlockState();
                    }
                    if (state.is(Blocks.STONE)) {
                        set = Blocks.COBBLESTONE.defaultBlockState();
                    }
                    if (state.is(Blocks.GRAVEL) && !limbo.getBlockState(pos.below()).isAir()) {
                        set = DimDoors.UNRAVELED_FABRIC.get().defaultBlockState();
                        limbo.scheduleTick(pos, set.getBlock(), 10);
                    } else if (state.is(Blocks.GRAVEL)) {
                        decay.remove(index);
                        change = false;
                    }
                    if (change) {
                        limbo.setBlock(pos, set, Block.UPDATE_ALL);
                    }
                } else {
                    decay.remove(index);
                }
                DDWorld.data().setDirty();
            }
        }
        tickCount2++;
        if (DDWorld.teleTimer > 0) {
            DDWorld.teleTimer--;
        }
    }

    /** RiftGenerator's createPocket / createLink for the rifts worldgen152 made; on the server thread, once per place. */
    private record GeneratedRifts(MinecraftServer server) implements RiftGen.Links {
        @Override
        public void gateway(int dimension, int x, int y, int z, int orientation) {
            this.server.execute(() -> {
                if (DDWorld.server() == null || DDWorld.link(dimension, x, y, z) != null) {
                    return;
                }
                DDWorld.createPocket(new LinkData(dimension, 0, x, y, z, x, y, z, true, orientation), true, true);
            });
        }

        @Override
        public void cluster(int dimension, List<int[]> rifts, int orientation) {
            List<int[]> copy = List.copyOf(rifts);
            this.server.execute(() -> {
                int[] first = copy.get(0);
                if (DDWorld.server() == null || DDWorld.link(dimension, first[0], first[1], first[2]) != null) {
                    return;
                }
                LinkData link = DDWorld.createPocket(new LinkData(dimension, 0, first[0], first[1], first[2], first[0], first[1], first[2], true,
                        orientation), true, true);
                for (int i = 1; i < copy.size(); i++) {
                    int[] rift = copy.get(i);
                    if (DDWorld.link(dimension, rift[0], rift[1], rift[2]) == null) {
                        DDWorld.createLink(dimension, link.destDim, rift[0], rift[1], rift[2], link.destX, link.destY, link.destZ);
                    }
                }
            });
        }
    }
}
