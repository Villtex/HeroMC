package legendgear.item;

import legendgear.LGConfig;
import legendgear.LGFx;
import legendgear.LGItems;
import legendgear.entity.BombEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Quiver and ItemBombBag: the item damage of 1.5.2 counted the arrows (up to 200) or bombs (up to 50), here the
 * {@link LGItems#AMMO} component. The quiver soaks up the arrows of the inventory on use and gives back up to 64 when
 * sneaking; arrows and bombs picked up go in first (ForgeEventHooksHandler). A bomb bag throws one bomb per use.
 */
public class AmmoBagItem extends Item {
    private final boolean bombs;

    public AmmoBagItem(Properties properties, boolean bombs) {
        super(properties);
        this.bombs = bombs;
    }

    public static int ammo(ItemStack stack) {
        return stack.getOrDefault(LGItems.AMMO.get(), 0);
    }

    public static void setAmmo(ItemStack stack, int count) {
        stack.set(LGItems.AMMO.get(), Math.max(0, count));
    }

    public int capacity() {
        return this.bombs ? LGConfig.BOMB_BAG_CAPACITY : LGConfig.QUIVER_CAPACITY;
    }

    public boolean bombs() {
        return this.bombs;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.bombs) {
            if (ammo(stack) > 0) {
                if (!LGFx.creative(player)) {
                    setAmmo(stack, ammo(stack) - 1);
                }
                LGFx.sound(player, SoundEvents.ARROW_SHOOT, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
                if (level instanceof ServerLevel) {
                    level.addFreshEntity(BombEntity.thrown(level, player, 80));
                }
                player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            var inv = player.getInventory().getNonEquipmentItems();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack arrows = inv.get(i);
                if (arrows.is(Items.ARROW)) {
                    int absorb = Math.min(arrows.getCount(), this.capacity() - ammo(stack));
                    setAmmo(stack, ammo(stack) + absorb);
                    arrows.shrink(absorb);
                    break;
                }
            }
        } else {
            int total = Math.min(ammo(stack), 64);
            if (total > 0) {
                ItemStack dumped = new ItemStack(Items.ARROW, total);
                setAmmo(stack, ammo(stack) - total);
                player.getInventory().add(dumped);
                setAmmo(stack, ammo(stack) + dumped.getCount());
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** onLeftClickEntity: hitting a lit bomb with the bag puts it out. */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (this.bombs && entity instanceof BombEntity bomb) {
            bomb.fizzle();
            return true;
        }
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ammo(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * Math.min(ammo(stack), this.capacity()) / this.capacity());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ammo(stack) >= this.capacity() ? 0xFFFF55 : 0xAAAAAA;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(this.bombs ? "item.legendgear.bomb_bag.count" : "item.legendgear.quiver.count", ammo(stack),
                this.capacity()));
    }
}
