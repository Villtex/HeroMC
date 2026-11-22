package hexxit.worldgen152;

import hexxit.worldgen152.core.OldChunk;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldDecoratedChunk;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import net.minecraft.world.level.block.VineBlock;
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
public final class OldChunkWriter {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private OldChunkWriter() {
    }

    public static void write(WorldGenLevel level, ChunkAccess chunk, OldChunk bare, OldDecoratedChunk populated) {
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
                    if (cell == (y < OldChunk.HEIGHT ? bare.cell(x, y, z) : 0)) {
                        continue;
                    }
                    int id = cell >> 4;
                    BlockState state = id == 0 ? air : OldBlockStates.get(id, cell & 15);
                    if (state.getBlock() instanceof LeavesBlock) {
                        state = state.setValue(LeavesBlock.DISTANCE, populated.leafDistance(x, y, z));
                    } else if (state.getBlock() instanceof VineBlock) {
                        // vines of mods too (Natura's thornvines): the top face only shows under a solid block, as in 1.5.2
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
                    if (state.getBlock() instanceof EntityBlock entityBlock && populated.tile(x, y, z) == null) {
                        // beds, enchanting tables, signs ... without data of their own still need their block entity
                        BlockEntity blockEntity = entityBlock.newBlockEntity(pos.immutable(), state);
                        if (blockEntity != null) {
                            chunk.setBlockEntity(blockEntity);
                        }
                    }
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
            if (entry.getValue() instanceof OldTile.Skull skull && state.is(Blocks.SKELETON_SKULL)) {
                // a floor skull (metadata 1): kind and rotation lived in the tile entity; the rotation was stored
                // as a byte and drawn as rotation * 360 / 16, so only its lowest four bits count
                state = SKULLS[Math.floorMod(skull.type(), SKULLS.length)].defaultBlockState()
                        .setValue(BlockStateProperties.ROTATION_16, skull.rotation() & 15);
                chunk.setBlockState(pos, state);
            }
            if (!(state.getBlock() instanceof EntityBlock entityBlock)) {
                continue;
            }
            BlockEntity blockEntity = entityBlock.newBlockEntity(pos.immutable(), state);
            if (blockEntity == null) {
                continue;
            }
            fillTile(blockEntity, entry.getValue(), registries, pos);
            chunk.setBlockEntity(blockEntity);
        }

        for (OldEntity old : populated.entities()) {
            Entity entity = entity(old, level.getLevel(), registries);
            if (entity != null) {
                if (!(entity instanceof HangingEntity)) {
                    entity.snapTo(old.x(), old.y(), old.z(), old instanceof OldEntity.Mob mob ? mob.yaw() : 0.0F, 0.0F);
                }
                chunk.addEntity(entity);
            }
        }
    }

    /** Puts what a 1.5.2 tile entity held (chest contents, spawner mob, sign text ...) into the block entity of today. */
    static void fillTile(BlockEntity blockEntity, OldTile tile, RegistryAccess registries, BlockPos pos) {
        if (tile instanceof OldTile.Chest contents && blockEntity instanceof ChestBlockEntity chest) {
            fill(chest, contents.slots(), registries);
        } else if (tile instanceof OldTile.Dispenser contents && blockEntity instanceof DispenserBlockEntity dispenser) {
            fill(dispenser, contents.slots(), registries);
        } else if (tile instanceof OldTile.Spawner spawner && blockEntity instanceof SpawnerBlockEntity spawnerEntity) {
            spawnerEntity.setEntityId(OldGameIds.entity(spawner.mob()), RandomSource.create(pos.asLong()));
            if (spawner.spawnRange() != 4 || spawner.maxNearbyEntities() != 6 || spawner.spawnCount() != 4) {
                // MobSpawnerBaseLogic.readFromNBT after writeToNBT, as the 1.5.2 mods changed the settings
                CompoundTag tag = spawnerEntity.saveCustomOnly(registries);
                tag.putShort("SpawnRange", (short) spawner.spawnRange());
                tag.putShort("MaxNearbyEntities", (short) spawner.maxNearbyEntities());
                tag.putShort("SpawnCount", (short) spawner.spawnCount());
                spawnerEntity.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
            }
        } else if (tile instanceof OldTile.Furnace contents && blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
            fill(furnace, contents.slots(), registries);
        } else if (tile instanceof OldTile.BrewingStand contents && blockEntity instanceof BrewingStandBlockEntity stand) {
            fill(stand, contents.slots(), registries);
        } else if (tile instanceof OldTile.Jukebox jukebox && blockEntity instanceof JukeboxBlockEntity player
                && jukebox.record() != null) {
            ItemStack record = item(jukebox.record(), registries);
            if (!record.isEmpty()) {
                // through the saved data: the setters want the level, which a chunk being generated has not yet
                CompoundTag tag = new CompoundTag();
                tag.put(JukeboxBlockEntity.SONG_ITEM_TAG_ID,
                        ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), record).getOrThrow());
                player.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
            }
        } else if (tile instanceof OldTile.Sign sign && blockEntity instanceof SignBlockEntity signEntity) {
            // through the saved data (setText wants the level)
            ListTag messages = new ListTag();
            for (int i = 0; i < 4; i++) {
                messages.add(StringTag.valueOf(i < sign.lines().length && sign.lines()[i] != null ? sign.lines()[i] : ""));
            }
            CompoundTag front = new CompoundTag();
            front.put("messages", messages);
            CompoundTag tag = new CompoundTag();
            tag.put("front_text", front);
            signEntity.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
        } else if (tile instanceof OldTile.Mod mod) {
            OldModData.write(mod, blockEntity, registries);
        }
    }

    /** 1.5.2 hanging direction (Direction.directions) to the side the entity faces. */
    private static final Direction[] HANGING = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
    /** Entity equipment slots of 1.5.2, in the order of its "Equipment" list. */
    private static final EquipmentSlot[] EQUIPMENT = {EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST,
            EquipmentSlot.HEAD};
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    /** The stack, or empty (reported once per item) when the item belongs to a mod that is not ported yet. */
    private static ItemStack item(OldStack old, RegistryAccess registries) {
        try {
            return OldGameIds.stack(old, registries);
        } catch (IllegalStateException e) {
            if (REPORTED.add(old.itemId() + ":" + old.damage())) {
                LOGGER.warn("Generated item left out: {}", e.getMessage());
            }
            return ItemStack.EMPTY;
        }
    }

    /** The entity type of a 1.5.2 entity name, or {@code null} (reported once) when it is not known (yet). */
    private static EntityType<?> type(String name) {
        try {
            return OldGameIds.entity(name);
        } catch (IllegalStateException e) {
            if (REPORTED.add(name)) {
                LOGGER.warn("Generated entity left out: {}", e.getMessage());
            }
            return null;
        }
    }

    /** TileEntitySkull.skullType 0 to 4. */
    private static final Block[] SKULLS = {Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.ZOMBIE_HEAD, Blocks.PLAYER_HEAD,
            Blocks.CREEPER_HEAD};

    private static void fill(Container container, OldStack[] slots, RegistryAccess registries) {
        for (int slot = 0; slot < slots.length && slot < container.getContainerSize(); slot++) {
            if (slots[slot] != null) {
                container.setItem(slot, item(slots[slot], registries));
            }
        }
    }

    private static Entity entity(OldEntity old, ServerLevel level, RegistryAccess registries) {
        return entity(old, level, registries, 0, 0);
    }

    /** The entity of today for {@code old}, moved by dx/dz blocks (hanging entities and items; mobs are placed by the caller). */
    static Entity entity(OldEntity old, ServerLevel level, RegistryAccess registries, int dx, int dz) {
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
        if (old instanceof OldEntity.Painting painting) {
            // 1.5.2 kept the wall block, today's paintings keep the block in front of it; both centre the
            // picture the same way, so the motive lands where it was
            Direction facing = HANGING[painting.direction() & 3];
            BlockPos at = new BlockPos(painting.tileX() + dx, painting.tileY(), painting.tileZ() + dz).relative(facing);
            Identifier variant = Identifier.withDefaultNamespace(paintingName(painting.motive()));
            var holder = registries.lookupOrThrow(Registries.PAINTING_VARIANT).get(ResourceKey.create(Registries.PAINTING_VARIANT, variant));
            return holder.isEmpty() ? null : new Painting(level, at, facing, holder.get());
        }
        if (old instanceof OldEntity.ItemFrame frame) {
            Direction facing = HANGING[frame.direction() & 3];
            ItemFrame entity = new ItemFrame(level, new BlockPos(frame.tileX() + dx, frame.tileY(), frame.tileZ() + dz).relative(facing), facing);
            if (frame.item() != null) {
                entity.setItem(item(frame.item(), registries), false);
            }
            // 1.5.2 turned the item in quarter turns, today in eighths; setRotation(int) updates the neighbours
            // (comparator output), which a chunk being generated must not do: the private variant without updates
            int rotation = frame.rotation() * 2 & 7;
            if (rotation != 0) {
                try {
                    var set = ItemFrame.class.getDeclaredMethod("setRotation", int.class, boolean.class);
                    set.setAccessible(true);
                    set.invoke(entity, rotation, false);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("ItemFrame.setRotation(int, boolean) not found", e);
                }
            }
            return entity;
        }
        if (old instanceof OldEntity.Item dropped) {
            ItemStack stack = item(dropped.stack(), registries);
            return stack.isEmpty() ? null : new ItemEntity(level, dropped.x() + dx, dropped.y(), dropped.z() + dz, stack, 0.0, 0.0, 0.0);
        }
        if (old instanceof OldEntity.Mob mob) {
            EntityType<?> type = type(mob.name());
            Entity entity = type == null ? null : type.create(level, EntitySpawnReason.STRUCTURE);
            if (entity != null) {
                OldGameIds.initEntity(mob.name(), entity);
            }
            if (entity instanceof Mob living && mob.equipment() != null) {
                for (int slot = 0; slot < mob.equipment().length && slot < EQUIPMENT.length; slot++) {
                    if (mob.equipment()[slot] != null) {
                        living.setItemSlot(EQUIPMENT[slot], item(mob.equipment()[slot], registries));
                    }
                }
            }
            return entity;
        }
        return null;
    }

    /** EnumArt title of 1.5.2 ("SkullAndRoses") to today's painting variant ("skull_and_roses"). */
    private static String paintingName(String motive) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < motive.length(); i++) {
            char c = motive.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                name.append('_');
            }
            name.append(Character.toLowerCase(c));
        }
        return name.toString();
    }

    /** Fences, panes, bars, stairs, redstone wire and tripwire show their connections as block states today. */
    static boolean connects(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CrossCollisionBlock || block instanceof StairBlock || block instanceof RedstoneWireBlock
                || block instanceof TripWireBlock || block instanceof WallBlock || OldBlockStates.connecting(block);
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

    static boolean isSnow(int oldId) {
        return oldId == 78 || oldId == 80;
    }
}
