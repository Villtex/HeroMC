package legendgear.item;

import legendgear.LGFx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;

/**
 * ItemMedallion (earth, wind, fire, ender): 51 uses of charge, crafted empty (damage 50). Only a full medallion (damage 0,
 * glowing) can be drawn like a bow for 15 ticks and is then thrown and used up; it charges by what its element does to
 * the player (ForgeEventHooksHandler, the ender medallion by enderman stares).
 */
public class MedallionItem extends Item {
    public static final int MAX_DAMAGE = 51;
    public static final int THROW_TIME = 15;
    private final BiFunction<Level, LivingEntity, ? extends Projectile> thrown;

    public MedallionItem(Properties properties, BiFunction<Level, LivingEntity, ? extends Projectile> thrown) {
        super(properties);
        this.thrown = thrown;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return THROW_TIME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getDamageValue() == 0) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getDamageValue() == 0;
    }

    /** onEaten: thrown and used up (not in creative). */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        LGFx.sound(entity, SoundEvents.ARROW_SHOOT, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel) {
            level.addFreshEntity(this.thrown.apply(level, entity));
        }
        if (!LGFx.creative(entity)) {
            stack.shrink(1);
        }
        return stack;
    }

    /** ForgeEventHooksHandler: charge(amount); a full charge plays "fullcharge", else "partialcharge". */
    public static void charge(Player player, ItemStack stack, int amount) {
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
        LGFx.sound(player, stack.getDamageValue() == 0 ? legendgear.LGSounds.FULLCHARGE.get() : legendgear.LGSounds.PARTIALCHARGE.get(), 0.2F, 1.0F);
    }
}
