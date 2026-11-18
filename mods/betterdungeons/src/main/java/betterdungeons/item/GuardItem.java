package betterdungeons.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.Holder;

/**
 * Items whose 1.5.2 effect came while the player blocked with them (EnumAction.block; the blocking itself is the
 * BLOCKS_ATTACKS component, see BDItems.blocking): ItemSwordDefensive (Moonlight, Morning Shine) and ItemSwordEnd
 * (walker sword) give Resistance I, ItemShied Resistance II and turns arrows close by, ItemBanner Speed II.
 */
public class GuardItem extends Item {
    private final Holder<MobEffect> effect;
    private final int duration;
    private final int amplifier;
    private final boolean turnsArrows;
    private final boolean teleports;

    public GuardItem(Properties properties, Holder<MobEffect> effect, int duration, int amplifier, boolean turnsArrows, boolean teleports) {
        super(properties);
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.turnsArrows = turnsArrows;
        this.teleports = teleports;
    }

    /** ItemSwordEnd: sneak + use teleports 4 blocks along the view (one use of durability), then blocks. */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (this.teleports && player.isShiftKeyDown()) {
            BDItemUtil.teleportForward(player);
            player.getItemInHand(hand).hurtAndBreak(1, player, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (level.isClientSide()) {
            return;
        }
        entity.addEffect(new MobEffectInstance(this.effect, this.duration, this.amplifier));
        if (this.turnsArrows) {
            for (Entity e : level.getEntities(entity, entity.getBoundingBox().inflate(1.2))) {
                if (e instanceof AbstractArrow) {
                    var look = entity.getLookAngle();
                    e.setDeltaMovement(look.x, e.getDeltaMovement().y, look.z);
                }
            }
        }
    }
}
