package hexxit.worldgen152;

import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldDecoratedChunk;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Puts what 1.5.2 population did to a chunk into the game's chunk, on top of the bare terrain already there. */
final class OldChunkWriter {
    private OldChunkWriter() {
    }

    static void write(ChunkAccess chunk, OldChunk bare, OldDecoratedChunk populated, RegistryAccess registries) {
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
                    if (state.hasProperty(BlockStateProperties.SNOWY)) {
                        state = state.setValue(BlockStateProperties.SNOWY, y + 1 < 256 && isSnow(populated.blockId(x, y + 1, z)));
                    }
                    pos.set(minX + x, y, minZ + z);
                    chunk.setBlockState(pos, state);
                    if (id == 8 || id == 10) {
                        // a spring: let the liquid start to flow once the chunk is part of the world
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
                OldStack[] slots = contents.slots();
                for (int slot = 0; slot < slots.length && slot < chest.getContainerSize(); slot++) {
                    if (slots[slot] != null) {
                        chest.setItem(slot, OldGameIds.stack(slots[slot], registries));
                    }
                }
            } else if (entry.getValue() instanceof OldTile.Spawner spawner && blockEntity instanceof SpawnerBlockEntity spawnerEntity) {
                spawnerEntity.setEntityId(OldGameIds.entity(spawner.mob()), RandomSource.create(pos.asLong()));
            }
            chunk.setBlockEntity(blockEntity);
        }
    }

    private static boolean isSnow(int oldId) {
        return oldId == 78 || oldId == 80;
    }
}
