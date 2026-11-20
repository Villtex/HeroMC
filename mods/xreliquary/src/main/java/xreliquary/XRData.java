package xreliquary;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

/**
 * ItemXR.getShort/setShort/getBoolean/setBoolean: the item NBT of 1.5.2 lives in {@code minecraft:custom_data}. Item
 * states the icon depends on are mirrored into {@code minecraft:custom_model_data} (flag 0, colour 0).
 */
public final class XRData {
    private XRData() {
    }

    public static CompoundTag tag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    public static int getShort(ItemStack stack, String key) {
        return tag(stack).getShortOr(key, (short) 0);
    }

    public static void setShort(ItemStack stack, String key, int value) {
        CompoundTag tag = tag(stack);
        tag.putShort(key, (short) value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean getBool(ItemStack stack, String key) {
        return tag(stack).getBooleanOr(key, false);
    }

    public static void setBool(ItemStack stack, String key, boolean value) {
        CompoundTag tag = tag(stack);
        tag.putBoolean(key, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** The second icon of an item (filled chalice, active coin, ...). */
    public static void setFlag(ItemStack stack, boolean flag) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(flag), List.of(), List.of()));
    }

    public static boolean flag(ItemStack stack) {
        CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return data != null && !data.flags().isEmpty() && data.flags().get(0);
    }
}
