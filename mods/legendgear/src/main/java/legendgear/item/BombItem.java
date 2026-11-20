package legendgear.item;

import legendgear.LGFx;
import legendgear.entity.BombEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** ItemBomb (16 a stack): thrown with a fuse of 80 ticks; a dispenser shoots it out too (LegendGear.dispenseBomb). */
public class BombItem extends Item {
    public BombItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!LGFx.creative(player)) {
            stack.shrink(1);
        }
        LGFx.sound(player, SoundEvents.ARROW_SHOOT, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel) {
            level.addFreshEntity(BombEntity.thrown(level, player, 80));
        }
        return InteractionResult.SUCCESS;
    }
}
