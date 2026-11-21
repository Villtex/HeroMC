package twilightforest;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import twilightforest.entity.CharmEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TFEventListener: the charms. A charm of life saves a player from a hit that would kill him (II: full health,
 * regeneration IV, resistance and fire resistance for 30 s; I: 8 health and regeneration for 5 s). A charm of keeping
 * keeps, when he dies without keepInventory, the armour and the held item (I), the hotbar (II) or everything (III) until
 * he respawns; tower keys are always kept. Items kept for a player who logs out are dropped.
 */
public final class TFEventListener {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static final Map<UUID, Kept> KEPT = new HashMap<>();

    private TFEventListener() {
    }

    private static final class Kept {
        final ItemStack[] armor = new ItemStack[4];
        final ItemStack[] main = new ItemStack[Inventory.INVENTORY_SIZE];
        ItemStack offhand = ItemStack.EMPTY;
        ItemStack charm = ItemStack.EMPTY;

        Kept() {
            java.util.Arrays.fill(this.armor, ItemStack.EMPTY);
            java.util.Arrays.fill(this.main, ItemStack.EMPTY);
        }
    }

    /** InventoryPlayer.consumeInventoryItem: one of the item from the first main inventory slot that has it. */
    private static boolean consume(Player player, Item item) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    /** willEntityDie, here with the damage after armour and resistance. */
    static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || Math.ceil(event.getNewDamage()) < player.getHealth()) {
            return;
        }
        boolean charm2 = consume(player, TFItems.CHARM_OF_LIFE_2.get());
        boolean charm1 = !charm2 && consume(player, TFBlocks.CHARM_OF_LIFE_1.get());
        if (!charm1 && !charm2) {
            return;
        }
        event.setNewDamage(0.0F);
        if (charm1) {
            player.setHealth(8.0F);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
        } else {
            player.setHealth(player.getMaxHealth());
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 3));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 600, 0));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
        }
        charmEffect(player, new ItemStack(charm1 ? TFBlocks.CHARM_OF_LIFE_1.get() : TFItems.CHARM_OF_LIFE_2.get()));
    }

    private static void charmEffect(ServerPlayer player, ItemStack charm) {
        ServerLevel level = player.level();
        level.addFreshEntity(new CharmEffect(level, player, charm, 0.0F));
        level.addFreshEntity(new CharmEffect(level, player, charm, (float) Math.PI));
        level.playSound(null, player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS,
                1.5F, 1.0F);
    }

    static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }
        Inventory inv = player.getInventory();
        Kept keep = null;
        if (consume(player, TFItems.CHARM_OF_KEEPING_3.get())) {
            TwilightForest.LOGGER.info("[TwilightForest] Player died with charm of keeping III!  Keep it all!");
            keep = keepArmor(player, new Kept());
            for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                keep.main[i] = inv.getItem(i);
                inv.setItem(i, ItemStack.EMPTY);
            }
            keep.offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
            player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            keep.charm = new ItemStack(TFItems.CHARM_OF_KEEPING_3.get());
        } else if (consume(player, TFItems.CHARM_OF_KEEPING_2.get())) {
            TwilightForest.LOGGER.info("[TwilightForest] Player died with charm of keeping II!  Keep armor and hotbar!");
            keep = keepArmor(player, new Kept());
            for (int i = 0; i < 9; i++) {
                keep.main[i] = inv.getItem(i);
                inv.setItem(i, ItemStack.EMPTY);
            }
            keep.charm = new ItemStack(TFItems.CHARM_OF_KEEPING_2.get());
        } else if (consume(player, TFBlocks.CHARM_OF_KEEPING_1.get())) {
            TwilightForest.LOGGER.info("[TwilightForest] Player died with charm of keeping I!  Keep armor and current item!");
            keep = keepArmor(player, new Kept());
            int slot = inv.getSelectedSlot();
            keep.main[slot] = inv.getItem(slot);
            inv.setItem(slot, ItemStack.EMPTY);
            keep.charm = new ItemStack(TFBlocks.CHARM_OF_KEEPING_1.get());
        }
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            if (inv.getItem(i).is(TFTower.TOWER_KEY.get())) {
                if (keep == null) {
                    keep = new Kept();
                }
                keep.main[i] = inv.getItem(i);
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        if (keep != null) {
            KEPT.put(player.getUUID(), keep);
        }
    }

    private static Kept keepArmor(Player player, Kept keep) {
        for (int i = 0; i < ARMOR.length; i++) {
            keep.armor[i] = player.getItemBySlot(ARMOR[i]);
            player.setItemSlot(ARMOR[i], ItemStack.EMPTY);
        }
        return keep;
    }

    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Kept keep = KEPT.remove(event.getEntity().getUUID());
        if (keep == null || event.isEndConquered() || !(event.getEntity() instanceof ServerPlayer player)) {
            if (keep != null) {
                KEPT.put(event.getEntity().getUUID(), keep);
            }
            return;
        }
        TwilightForest.LOGGER.info("[TwilightForest] Player {} respawned and recieved items held in storage", player.getName().getString());
        for (int i = 0; i < ARMOR.length; i++) {
            if (!keep.armor[i].isEmpty()) {
                player.setItemSlot(ARMOR[i], keep.armor[i]);
            }
        }
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            if (!keep.main[i].isEmpty()) {
                player.getInventory().setItem(i, keep.main[i]);
            }
        }
        if (!keep.offhand.isEmpty()) {
            player.setItemSlot(EquipmentSlot.OFFHAND, keep.offhand);
        }
        if (!keep.charm.isEmpty()) {
            charmEffect(player, keep.charm);
        }
    }

    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Kept keep = KEPT.remove(event.getEntity().getUUID());
        if (keep != null && event.getEntity() instanceof ServerPlayer player) {
            TwilightForest.LOGGER.warn("[TwilightForest] Mod was keeping inventory items in reserve for player {} but they logged out!  Items are being dropped.",
                    player.getName().getString());
            for (ItemStack stack : keep.armor) {
                player.drop(stack, true, net.minecraft.util.Prediction.SERVER_ONLY);
            }
            for (ItemStack stack : keep.main) {
                player.drop(stack, true, net.minecraft.util.Prediction.SERVER_ONLY);
            }
            player.drop(keep.offhand, true, net.minecraft.util.Prediction.SERVER_ONLY);
        }
    }
}
