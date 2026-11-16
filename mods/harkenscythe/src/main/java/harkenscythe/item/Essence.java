package harkenscythe.item;

import harkenscythe.HSItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The soul and blood store of ItemHSKeeper. As in 1.5.2 a keeper holds its essence as durability: a full keeper has
 * no damage, the stored amount is {@code max damage - damage}. Soulkeeper 20, Bloodkeeper 40, Soul Vessel 40, Blood
 * Vessel 80; an emptied one turns back into an Essence Keeper or Essence Vessel. Only the main inventory counts (not
 * the armour or the off hand), like InventoryPlayer.mainInventory.
 */
public final class Essence {
    private Essence() {
    }

    public static int stored(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    public static Item keeper(boolean blood) {
        return blood ? HSItems.BLOODKEEPER.get() : HSItems.SOULKEEPER.get();
    }

    public static Item vessel(boolean blood) {
        return blood ? HSItems.BLOOD_VESSEL.get() : HSItems.SOUL_VESSEL.get();
    }

    private static NonNullList<ItemStack> main(Player player) {
        return player.getInventory().getNonEquipmentItems();
    }

    private static boolean hasItem(Player player, Item item) {
        for (ItemStack stack : main(player)) {
            if (stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    /** A filled keeper of {@code amount} (1 or more) made from an empty one: the new stack replaces it in the slot. */
    private static ItemStack filledFromEmpty(Item filled, int amount) {
        ItemStack stack = new ItemStack(filled);
        int room = stack.getMaxDamage();
        stack.setDamageValue(Math.max(0, room - amount));
        return stack;
    }

    /**
     * ItemHSKeeper.soulkeeperFillCheck / bloodkeeperFillCheck: puts {@code amount} essence into the keepers of the player,
     * first into the one in the hand (a vessel before a keeper), then vessels, empty vessels, keepers and empty keepers
     * of the inventory. Returns whether everything found a place.
     */
    public static boolean fill(Player player, int amount, boolean blood) {
        if (amount == 0) {
            return false;
        }
        Item keeper = keeper(blood);
        Item vessel = vessel(blood);
        Item emptyKeeper = HSItems.ESSENCE_KEEPER.get();
        Item emptyVessel = HSItems.ESSENCE_VESSEL.get();
        if (!hasItem(player, keeper) && !hasItem(player, emptyKeeper) && !hasItem(player, vessel) && !hasItem(player, emptyVessel)) {
            return false;
        }
        int[] pool = {amount};
        int selected = player.getInventory().getSelectedSlot();
        ItemStack current = player.getInventory().getSelectedItem();
        if (current.is(vessel) && fillStack(current, pool)) {
            return true;
        }
        if (current.is(emptyVessel) && fillEmpty(player, selected, vessel, pool)) {
            return true;
        }
        if (current.is(keeper) && fillStack(current, pool)) {
            return true;
        }
        if (current.is(emptyKeeper) && fillEmpty(player, selected, keeper, pool)) {
            return true;
        }
        Item[][] passes = {{vessel, null}, {emptyVessel, vessel}, {keeper, null}, {emptyKeeper, keeper}};
        NonNullList<ItemStack> items = main(player);
        for (Item[] pass : passes) {
            for (int slot = 0; slot < items.size(); slot++) {
                if (!items.get(slot).is(pass[0])) {
                    continue;
                }
                if (pass[1] == null ? fillStack(items.get(slot), pool) : fillEmpty(player, slot, pass[1], pool)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean fillStack(ItemStack stack, int[] pool) {
        int room = stack.getDamageValue();
        if (room >= pool[0]) {
            stack.setDamageValue(room - pool[0]);
            return true;
        }
        pool[0] -= room;
        stack.setDamageValue(0);
        return false;
    }

    private static boolean fillEmpty(Player player, int slot, Item filled, int[] pool) {
        ItemStack stack = filledFromEmpty(filled, pool[0]);
        int room = stack.getMaxDamage();
        player.getInventory().setItem(slot, stack);
        if (room >= pool[0]) {
            return true;
        }
        pool[0] -= room;
        return false;
    }

    /**
     * ItemHSKeeper.soulkeeperCheck / bloodkeeperCheck: whether one keeper or vessel holds at least {@code required}.
     * (The talisman looked at keepers first, the rest at vessels first; the answer is the same.)
     */
    public static boolean has(Player player, int required, boolean blood) {
        if (required == 0) {
            return true;
        }
        Item keeper = keeper(blood);
        Item vessel = vessel(blood);
        for (ItemStack stack : main(player)) {
            if ((stack.is(keeper) || stack.is(vessel)) && stored(stack) >= required) {
                return true;
            }
        }
        return false;
    }

    /**
     * The payment of the talisman, the amulets and the Necronomicon: {@code cost} from the first keeper that holds
     * enough, else from the first such vessel. An emptied one becomes an Essence Keeper or Vessel.
     */
    public static void consume(Player player, int cost, boolean blood) {
        if (cost <= 0) {
            return;
        }
        if (!consumeFrom(player, keeper(blood), HSItems.ESSENCE_KEEPER.get(), cost)) {
            consumeFrom(player, vessel(blood), HSItems.ESSENCE_VESSEL.get(), cost);
        }
    }

    private static boolean consumeFrom(Player player, Item item, Item empty, int cost) {
        NonNullList<ItemStack> items = main(player);
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.is(item) && stored(stack) >= cost) {
                stack.setDamageValue(stack.getDamageValue() + cost);
                if (stack.getDamageValue() >= stack.getMaxDamage()) {
                    player.getInventory().setItem(slot, new ItemStack(empty));
                }
                return true;
            }
        }
        return false;
    }

    /** Takes {@code cost} from the stack itself; an emptied keeper or vessel becomes the empty one (returned). */
    public static ItemStack drain(ItemStack stack, int cost) {
        stack.setDamageValue(stack.getDamageValue() + cost);
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            boolean vessel = stack.is(HSItems.SOUL_VESSEL.get()) || stack.is(HSItems.BLOOD_VESSEL.get());
            return new ItemStack(vessel ? HSItems.ESSENCE_VESSEL.get() : HSItems.ESSENCE_KEEPER.get());
        }
        return stack;
    }
}
