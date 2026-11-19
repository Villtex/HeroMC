package betterstorage.backpack;

import betterstorage.BS;
import betterstorage.BSConfig;
import betterstorage.item.BackpackItem;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** The static backpack helpers of ItemBackpack: the backpack an entity wears in its chest slot and its contents. */
public final class Backpacks {
    private Backpacks() {
    }

    public static ItemStack getBackpack(LivingEntity entity) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        return stack.getItem() instanceof BackpackItem ? stack : ItemStack.EMPTY;
    }

    public static BackpackData data(LivingEntity entity) {
        return entity.getData(BS.BACKPACK_DATA.get());
    }

    public static BackpackState state(LivingEntity entity) {
        return entity.getData(BS.BACKPACK_STATE.get());
    }

    public static @Nullable List<ItemStack> contents(LivingEntity entity) {
        return data(entity).contents;
    }

    public static int size() {
        return 9 * BSConfig.backpackRows();
    }

    /** setBackpack: into the chest slot, with these contents (or none). */
    public static void setBackpack(LivingEntity entity, ItemStack backpack, @Nullable List<ItemStack> contents) {
        entity.setItemSlot(EquipmentSlot.CHEST, backpack);
        BackpackData data = data(entity);
        if (contents == null) {
            data.contents = null;
        } else {
            data.contents = NonNullList.withSize(Math.max(contents.size(), size()), ItemStack.EMPTY);
            for (int i = 0; i < contents.size(); i++) {
                data.contents.set(i, contents.get(i));
            }
        }
        entity.setData(BS.BACKPACK_DATA.get(), data);
        updateHasItems(entity);
    }

    public static void removeBackpack(LivingEntity entity) {
        setBackpack(entity, ItemStack.EMPTY, null);
    }

    /** getBackpackItems: the worn backpack's inventory as {@code player} sees it (the ender backpack: their ender chest). */
    public static @Nullable Container getBackpackItems(LivingEntity carrier, @Nullable Player player) {
        ItemStack backpack = getBackpack(carrier);
        return backpack.isEmpty() ? null : ((BackpackItem) backpack.getItem()).getBackpackItems(carrier, player);
    }

    /** updateHasItems: tells the clients (the original only told the carrying player). */
    public static void updateHasItems(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        boolean hasItems = data(entity).hasItems();
        BackpackState state = state(entity);
        if (state.hasItems() != hasItems) {
            entity.setData(BS.BACKPACK_STATE.get(), new BackpackState(state.open(), hasItems));
        }
    }

    public static boolean isOpen(LivingEntity entity) {
        return state(entity).open();
    }

    public static void setOpen(LivingEntity entity, boolean open) {
        BackpackState state = state(entity);
        if (state.open() != open) {
            entity.setData(BS.BACKPACK_STATE.get(), new BackpackState(open, state.hasItems()));
        }
    }
}
