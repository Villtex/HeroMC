package betterdungeons.item;

import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * ItemHookShoot (hookshot 0, longshot 1, manual hook 2) and ItemHookShootSpider (3): use fires the hook
 * ({@link ProjectileType#HOOKSHOT} with the level), use again lets go. 1.5.2 kept the hook's entity id in the item
 * damage; here it is the custom data "Hook" (the item model shows the loaded hook only without it).
 */
public class HookShotItem extends Item {
    private final int level;

    public HookShotItem(Properties properties, int level) {
        super(properties);
        this.level = level;
    }

    static int hook(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? 0 : data.copyTag().getIntOr("Hook", 0);
    }

    static void setHook(ItemStack stack, int id) {
        if (id == 0) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Hook", id);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /** ProjectileHookShoot.setDead: the hook shot that fired this hook is free again. */
    public static void release(Player player, int hookId) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof HookShotItem && hook(stack) == hookId) {
                setHook(stack, 0);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        int id = hook(stack);
        if (id != 0) {
            Entity hook = level.getEntity(id);
            if (hook == null || !hook.isAlive()) {
                setHook(stack, 0);
            }
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        int id = hook(stack);
        if (id == 0) {
            BDProjectile ball = BDProjectile.spawn(player, ProjectileType.HOOKSHOT, stack, this.level);
            setHook(stack, ball.getId());
        } else {
            Entity e = level.getEntity(id);
            if (e != null) {
                e.discard();
            }
            setHook(stack, 0);
        }
        return InteractionResult.SUCCESS;
    }
}
