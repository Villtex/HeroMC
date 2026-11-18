package betterdungeons.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * What a full capsule carries (ItemCapsule.copy / BuilderHelper.fillMap of 1.5.2, which stored 1.5.2 block ids): the
 * blocks, block entities and entities of the area as a structure template in the item's custom data ("template"),
 * plus "data" {sx, sy, sz, name} as in the original. Out of creative mode the area is emptied (inventories first, so
 * nothing drops) and its entities removed.
 */
public final class CapsuleData {
    private CapsuleData() {
    }

    public static boolean isFull(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().contains("template");
    }

    /** sx, sy, sz of the stored area. */
    public static int[] size(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompoundOrEmpty("data");
        return new int[] {data.getIntOr("sx", 0), data.getIntOr("sy", 0), data.getIntOr("sz", 0)};
    }

    /** ItemCapsule.copy: stores the area from corner with the size into the capsule. */
    public static void copy(ServerLevel level, ItemStack capsule, BlockPos corner, int sx, int sy, int sz, String name, boolean creative) {
        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, corner, new Vec3i(sx, sy, sz), true, List.of());
        CompoundTag tag = new CompoundTag();
        CompoundTag data = new CompoundTag();
        data.putInt("sx", sx);
        data.putInt("sy", sy);
        data.putInt("sz", sz);
        data.putString("name", name);
        tag.put("data", data);
        tag.put("template", template.save(new CompoundTag()));
        capsule.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        capsule.set(DataComponents.ITEM_NAME, Component.literal(name));
        if (!creative) {
            for (BlockPos pos : BlockPos.betweenClosed(corner, corner.offset(sx - 1, sy - 1, sz - 1))) {
                if (level.getBlockEntity(pos) instanceof Clearable clearable) {
                    clearable.clearContent();
                }
            }
            for (BlockPos pos : BlockPos.betweenClosed(corner, corner.offset(sx - 1, sy - 1, sz - 1))) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
            }
            AABB box = new AABB(corner.getX(), corner.getY(), corner.getZ(), corner.getX() + sx, corner.getY() + sy, corner.getZ() + sz);
            for (Entity e : level.getEntities((Entity) null, box, e -> !(e instanceof Player))) {
                e.discard();
            }
            for (BlockPos pos : BlockPos.betweenClosed(corner, corner.offset(sx - 1, sy - 1, sz - 1))) {
                level.updateNeighborsAt(pos, Blocks.AIR);
            }
        }
    }

    /** BuilderHelper.fillMap(capsule): sets the stored area with its lower north-west corner at corner, then empties the capsule. */
    public static void paste(ServerLevel level, ItemStack capsule, BlockPos corner) {
        CompoundTag tag = capsule.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        StructureTemplate template = new StructureTemplate();
        template.load(level.holderLookup(Registries.BLOCK), tag.getCompoundOrEmpty("template"));
        template.placeInWorld(level, corner, corner, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_ALL);
    }

    public static void clear(ItemStack capsule) {
        capsule.remove(DataComponents.CUSTOM_DATA);
        capsule.set(DataComponents.ITEM_NAME, capsule.getItem().components().get(DataComponents.ITEM_NAME));
    }
}
