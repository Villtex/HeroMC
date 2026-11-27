package slimeknights.mantle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.registration.MantleData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Handles events for any Mantle driven logic.
 *
 * <p>Port: the soulbound slot is the data component {@link MantleData#SOULBOUND_SLOT} instead of the NBT key
 * {@code mantle_soulbound}; the lectern book handling moved to Teil 27c (book engine).
 */
@EventBusSubscriber(modid = Mantle.modId)
public class MantleEvents {
  private MantleEvents() {}

  /** True if the world keeps inventories on death */
  private static boolean keepInventory(Level level) {
    return level instanceof ServerLevel server && server.getGameRules().get(GameRules.KEEP_INVENTORY);
  }

  /* Soulbound */

  /** Called when the player dies to store the slot to return items into */
  @SubscribeEvent
  static void onLivingDeath(LivingDeathEvent event) {
    // this is the latest we can add slot markers to the items so we can return them to slots
    LivingEntity entity = event.getEntity();
    if (!keepInventory(entity.level()) && entity instanceof Player player && !(player instanceof FakePlayer)) {
      Inventory inventory = player.getInventory();
      int totalSize = inventory.getContainerSize();
      for (int i = 0; i < totalSize; i++) {
        ItemStack stack = inventory.getItem(i);
        if (!stack.isEmpty() && stack.is(MantleTags.Items.SOULBOUND)) {
          stack.set(MantleData.SOULBOUND_SLOT.get(), i);
        }
      }
    }
  }

  /** Called when the player dies to store the soulbound items in the original inventory */
  @SubscribeEvent(priority = EventPriority.HIGH)
  static void onPlayerDropItems(LivingDropsEvent event) {
    // only care about real players with keep inventory off
    LivingEntity entity = event.getEntity();
    if (!keepInventory(entity.level()) && entity instanceof Player player && !(entity instanceof FakePlayer)) {
      Collection<ItemEntity> drops = event.getDrops();
      Iterator<ItemEntity> iter = drops.iterator();
      Inventory inventory = player.getInventory();
      List<ItemEntity> takenSlot = new ArrayList<>();
      while (iter.hasNext()) {
        ItemEntity itemEntity = iter.next();
        ItemStack stack = itemEntity.getItem();
        // find items with our soulbound slot set and move them back into the inventory
        Integer slot = stack.get(MantleData.SOULBOUND_SLOT.get());
        if (slot != null) {
          if (slot >= 0 && slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty()) {
            inventory.setItem(slot, stack);
          } else {
            // hold off on items that did not get their slot, so they do not take the slot of an item not yet seen
            takenSlot.add(itemEntity);
          }
          iter.remove();
          // keep the slot for the player clone
        }
      }
      for (ItemEntity itemEntity : takenSlot) {
        ItemStack stack = itemEntity.getItem();
        if (!inventory.add(stack)) {
          // last resort, drop it on the ground without the marker to keep stacking working
          stack.remove(MantleData.SOULBOUND_SLOT.get());
          drops.add(itemEntity);
        }
      }
    }
  }

  /** Called when the new player is created to fetch the soulbound item from the old */
  @SubscribeEvent(priority = EventPriority.HIGH)
  static void onPlayerClone(PlayerEvent.Clone event) {
    if (!event.isWasDeath()) {
      return;
    }
    Player original = event.getOriginal();
    Player clone = event.getEntity();
    // inventory already copied
    if (keepInventory(clone.level()) || original.isSpectator()) {
      return;
    }
    Inventory originalInv = original.getInventory();
    Inventory cloneInv = clone.getInventory();
    int size = Math.min(originalInv.getContainerSize(), cloneInv.getContainerSize());
    List<ItemStack> takenSlot = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ItemStack stack = originalInv.getItem(i);
      if (!stack.isEmpty() && stack.has(MantleData.SOULBOUND_SLOT.get())) {
        stack.remove(MantleData.SOULBOUND_SLOT.get());
        if (cloneInv.getItem(i).isEmpty()) {
          cloneInv.setItem(i, stack);
        } else {
          takenSlot.add(stack);
        }
      }
    }
    for (ItemStack stack : takenSlot) {
      if (!cloneInv.add(stack)) {
        clone.drop(stack, false, Prediction.SERVER_ONLY);
      }
    }
  }
}
