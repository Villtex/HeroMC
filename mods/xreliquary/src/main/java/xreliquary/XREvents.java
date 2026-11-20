package xreliquary;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import xreliquary.item.PotionKind;
import xreliquary.item.VoidContents;
import xreliquary.recipe.AlkahestValues;

/** The three ICraftingHandlers of XRItems: WaterHandler, AlkahestHandler and the spilling of VoidSatchelHandler. */
public final class XREvents {
    private XREvents() {
    }

    @SubscribeEvent
    public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        Container matrix = event.getInventory();
        ItemStack result = event.getCrafting();
        boolean server = !player.level().isClientSide();
        // WaterHandler: every glowing water in the grid gives back an empty vial
        for (int slot = 0; slot < matrix.getContainerSize(); slot++) {
            if (matrix.getItem(slot).is(XRItems.GLOWING_WATER.get()) && server) {
                XRFx.give(player, new ItemStack(XRItems.potion(PotionKind.EMPTY_VIAL)));
            }
        }
        // AlkahestHandler: the whole stack of the dissolved item counts, paid in lapis blocks, lapis and redstone
        if (result.is(XRItems.ALKAHEST.get())) {
            int points = 0;
            for (int slot = 0; slot < matrix.getContainerSize(); slot++) {
                ItemStack s = matrix.getItem(slot);
                if (!s.isEmpty()) {
                    int value = AlkahestValues.value(s.getItem());
                    points += value * s.getCount();
                    if (s.getCount() > 1) {
                        s.setCount(1);
                    }
                }
            }
            matrix.setChanged();
            if (server) {
                for (; points >= 2304; points -= 2304) {
                    XRFx.give(player, new ItemStack(Items.LAPIS_BLOCK));
                }
                for (; points >= 256; points -= 256) {
                    XRFx.give(player, new ItemStack(Items.LAPIS_LAZULI));
                }
                for (; points >= 64; points -= 64) {
                    XRFx.give(player, new ItemStack(Items.REDSTONE));
                }
            }
        }
        // VoidSatchelHandler: what the tear held beyond the satchel's 64 spills out
        if (result.is(XRItems.VOID_SATCHEL.get()) && server) {
            for (int slot = 0; slot < matrix.getContainerSize(); slot++) {
                ItemStack tear = matrix.getItem(slot);
                if (tear.is(XRItems.VOID_TEAR.get())) {
                    Item item = VoidContents.item(tear);
                    int leftover = VoidContents.quantity(tear) - 64;
                    if (leftover > 0 && item != Items.AIR) {
                        XRFx.sound(player, SoundEvents.GLASS_BREAK, 0.1F,
                                0.5F * ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.8F));
                        while (leftover > 0) {
                            int n = Math.min(leftover, new ItemStack(item).getMaxStackSize());
                            leftover -= n;
                            XRFx.spawnAt(player, new ItemStack(item, n));
                        }
                    }
                }
            }
        }
    }

    static String id(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }
}
