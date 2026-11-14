package hexxit.worldgen152;

import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldDecoratedChunk;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Puts what 1.5.2 population did to a chunk into the game's chunk, on top of the bare terrain already there. */
final class OldChunkWriter {
    private OldChunkWriter() {
    }

    static void write(WorldGenLevel level, ChunkAccess chunk, OldChunk bare, OldDecoratedChunk populated) {
        RegistryAccess registries = level.registryAccess();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int minY = Math.max(0, chunk.getMinY());
        int maxY = Math.min(populated.height(), chunk.getMinY() + chunk.getHeight());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    int cell = populated.cell(x, y, z);
                    if (cell == (y < OldChunk.HEIGHT ? bare.blockId(x, y, z) << 4 : 0)) {
                        continue;
                    }
                    int id = cell >> 4;
                    BlockState state = id == 0 ? air : OldBlockStates.get(id, cell & 15);
                    if (state.getBlock() instanceof LeavesBlock) {
                        state = state.setValue(LeavesBlock.DISTANCE, populated.leafDistance(x, y, z));
                    } else if (state.is(Blocks.VINE)) {
                        state = state.setValue(BlockStateProperties.UP, y + 1 < 256 && OldBlockInfo.holdsVines(populated.blockId(x, y + 1, z)));
                    }
                    if (state.getBlock() instanceof DoorBlock) {
                        state = door(state, populated, x, y, z);
                    }
                    if (state.hasProperty(BlockStateProperties.SNOWY)) {
                        state = state.setValue(BlockStateProperties.SNOWY, y + 1 < 256 && isSnow(populated.blockId(x, y + 1, z)));
                    }
                    pos.set(minX + x, y, minZ + z);
                    chunk.setBlockState(pos, state);
                    if (id == 8 || id == 10 || connects(state)) {
                        // a spring: let the liquid start to flow once the chunk is part of the world;
                        // fences, panes, stairs, wires: let them find their neighbours
                        chunk.markPosForPostProcessing(pos);
                    } else if (isSnow(id) && y > minY) {
                        // the block below may be untouched terrain that now carries snow
                        pos.set(minX + x, y - 1, minZ + z);
                        BlockState below = chunk.getBlockState(pos);
                        if (below.hasProperty(BlockStateProperties.SNOWY) && !below.getValue(BlockStateProperties.SNOWY)) {
                            chunk.setBlockState(pos, below.setValue(BlockStateProperties.SNOWY, true));
                        }
                    }
                }
            }
        }

        for (var entry : populated.tiles().entrySet()) {
            int key = entry.getKey();
            pos.set(minX + (key >> 12), key & 255, minZ + (key >> 8 & 15));
            BlockState state = chunk.getBlockState(pos);
            if (!(state.getBlock() instanceof EntityBlock entityBlock)) {
                continue;
            }
            BlockEntity blockEntity = entityBlock.newBlockEntity(pos.immutable(), state);
            if (blockEntity == null) {
                continue;
            }
            if (entry.getValue() instanceof OldTile.Chest contents && blockEntity instanceof ChestBlockEntity chest) {
                fill(chest, contents.slots(), registries);
            } else if (entry.getValue() instanceof OldTile.Dispenser contents && blockEntity instanceof DispenserBlockEntity dispenser) {
                fill(dispenser, contents.slots(), registries);
            } else if (entry.getValue() instanceof OldTile.Spawner spawner && blockEntity instanceof SpawnerBlockEntity spawnerEntity) {
                spawnerEntity.setEntityId(OldGameIds.entity(spawner.mob()), RandomSource.create(pos.asLong()));
            }
            chunk.setBlockEntity(blockEntity);
        }

        for (OldEntity old : populated.entities()) {
            Entity entity = entity(old, level.getLevel(), registries);
            if (entity != null) {
                entity.snapTo(old.x(), old.y(), old.z(), 0.0F, 0.0F);
                chunk.addEntity(entity);
            }
        }
    }

    private static void fill(Container container, OldStack[] slots, RegistryAccess registries) {
        for (int slot = 0; slot < slots.length && slot < container.getContainerSize(); slot++) {
            if (slots[slot] != null) {
                container.setItem(slot, OldGameIds.stack(slots[slot], registries));
            }
        }
    }

    private static Entity entity(OldEntity old, ServerLevel level, RegistryAccess registries) {
        if (old instanceof OldEntity.Villager villager) {
            Villager entity = EntityTypes.VILLAGER.create(level, EntitySpawnReason.STRUCTURE);
            if (entity != null) {
                // experience keeps the profession: a villager of today without a workstation would forget it
                entity.setVillagerData(entity.getVillagerData().withProfession(registries, OldGameIds.profession(villager.profession())).withLevel(2));
                entity.setVillagerXp(VillagerData.getMinXpPerLevel(2));
            }
            return entity;
        }
        if (old instanceof OldEntity.Witch) {
            return EntityTypes.WITCH.create(level, EntitySpawnReason.STRUCTURE);
        }
        if (old instanceof OldEntity.MinecartChest cart) {
            MinecartChest entity = EntityTypes.CHEST_MINECART.create(level, EntitySpawnReason.STRUCTURE);
            if (entity != null) {
                fill(entity, cart.slots(), registries);
            }
            return entity;
        }
        return null;
    }

    /** Fences, panes, bars, stairs, redstone wire and tripwire show their connections as block states today. */
    private static boolean connects(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CrossCollisionBlock || block instanceof StairBlock || block instanceof RedstoneWireBlock
                || block instanceof TripWireBlock || block instanceof WallBlock;
    }

    /** A door half with what 1.5.2 stored in the other half: facing and "open" below, the hinge side above. */
    private static BlockState door(BlockState state, OldDecoratedChunk populated, int x, int y, int z) {
        boolean upper = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
        int otherY = upper ? y - 1 : y + 1;
        if (otherY < 0 || otherY >= 256 || populated.blockId(x, otherY, z) != populated.blockId(x, y, z)) {
            return state;
        }
        BlockState other = OldBlockStates.get(populated.blockId(x, otherY, z), populated.metadata(x, otherY, z));
        if (other.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state;
        }
        BlockState lower = upper ? other : state;
        BlockState top = upper ? state : other;
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, lower.getValue(BlockStateProperties.HORIZONTAL_FACING))
                .setValue(BlockStateProperties.OPEN, lower.getValue(BlockStateProperties.OPEN))
                .setValue(BlockStateProperties.DOOR_HINGE, top.getValue(BlockStateProperties.DOOR_HINGE));
    }

    private static boolean isSnow(int oldId) {
        return oldId == 78 || oldId == 80;
    }
}
