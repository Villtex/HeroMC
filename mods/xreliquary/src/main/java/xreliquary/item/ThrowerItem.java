package xreliquary.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xreliquary.XRFx;
import xreliquary.entity.ThrownEntity;

/** ItemGlowingWater, ItemHolyHandGrenade: thrown like a splash potion ("random.bow"). */
public class ThrowerItem extends XRItem {
    private final ThrownEntity.Kind kind;
    private final boolean foil;

    public ThrowerItem(Properties properties, ThrownEntity.Kind kind, boolean foil) {
        super(properties, 2);
        this.kind = kind;
        this.foil = foil;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.foil;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!XRFx.creative(player)) {
                stack.shrink(1);
            }
            XRFx.sound(player, SoundEvents.ARROW_SHOOT, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            level.addFreshEntity(new ThrownEntity(level, player, this.kind));
        }
        return InteractionResult.SUCCESS;
    }
}
