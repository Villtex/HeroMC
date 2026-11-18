package betterdungeons.item;

import betterdungeons.BDItems;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** ItemPistol and ItemMagicGrenade. */
public final class RangedItems {
    private RangedItems() {
    }

    /**
     * ItemPistol (revolver): fires a bullet (8 damage) when it is ready; its damage bar is the 20 tick reload, counted
     * down while it sits in the inventory. Every stack of bullets loses one per shot (1.5.2 took one from each stack).
     */
    public static class Pistol extends Item {
        public static final int COOLDOWN = 20;

        public Pistol(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            boolean creative = player.hasInfiniteMaterials();
            if ((creative || player.getInventory().contains(s -> s.is(BDItems.BULLET.get()))) && stack.getDamageValue() == 0) {
                if (!level.isClientSide()) {
                    BDProjectile.spawn(player, ProjectileType.BULLET_PISTOL);
                }
                stack.setDamageValue(COOLDOWN);
                if (!creative) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack s = player.getInventory().getItem(i);
                        if (s.is(BDItems.BULLET.get())) {
                            player.getInventory().removeItem(i, 1);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        @Override
        public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
            if (stack.getDamageValue() > 0) {
                stack.setDamageValue(stack.getDamageValue() - 1);
            }
        }
    }

    /** ItemMagicGrenade: hold use and let go to throw (any time held); the stack always loses one. */
    public static class Grenade extends Item {
        private final ProjectileType type;

        public Grenade(Properties properties, ProjectileType type) {
            super(properties);
            this.type = type;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        @Override
        public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
            if (!level.isClientSide()) {
                BDProjectile.spawn(entity, this.type);
            }
            stack.shrink(1);
            return true;
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity user) {
            return 72000;
        }

        @Override
        public ItemUseAnimation getUseAnimation(ItemStack stack) {
            return ItemUseAnimation.BOW;
        }
    }
}
