package asgardshield.item;

import asgardshield.AsgardShield;
import asgardshield.guard.Effects;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * What ItemAsgardShield and ItemZedSword share: right-click guards (EnumAction.block, 72000 ticks) unless the guard
 * is broken, item-crack particles when a damaged item is raised, portal particles of the ender items, and the hit
 * effects (portal particles, the skull cloud, a small knockback). Only the main hand guards, as in 1.5.2.
 */
public abstract class AbstractGuardItem extends Item {
    private final GuardSound sound;
    private final float damageMultiplier;
    private final float knockback;
    private final Special special;

    protected AbstractGuardItem(Properties properties, GuardSound sound, float damageMultiplier, float knockback, Special special) {
        super(properties);
        this.sound = sound;
        this.damageMultiplier = damageMultiplier;
        this.knockback = knockback;
        this.special = special;
    }

    public GuardSound guardSound() {
        return this.sound;
    }

    /** getDamageMultiplier: the share of the damage the player takes while holding (not guarding) the item. */
    public float damageMultiplier() {
        return this.damageMultiplier;
    }

    public Special special() {
        return this.special;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.getData(AsgardShield.GUARD).broken) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (level instanceof ServerLevel server) {
            if (stack.getDamageValue() > 0) {
                crackParticles(server, player, stack);
            }
            if (this.special == Special.ENDER) {
                Effects.enderFx(server, (int) Math.round(player.getX()), (int) Math.round(player.getY() - 1.0), (int) Math.round(player.getZ()),
                        player.getRandom());
            }
        }
        return InteractionResult.CONSUME;
    }

    /** The "iconcrack" particles of a damaged item when it is raised. */
    private static void crackParticles(ServerLevel level, Player player, ItemStack stack) {
        var random = player.getRandom();
        float pitch = -player.getXRot() * Mth.DEG_TO_RAD;
        float yaw = -player.getYRot() * Mth.DEG_TO_RAD;
        for (int i = 0; i < 5; i++) {
            Vec3 velocity = new Vec3((random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0).xRot(pitch).yRot(yaw);
            Vec3 pos = new Vec3((random.nextFloat() - 0.5) * 0.3, -random.nextFloat() * 0.6 - 0.3, 0.6).xRot(pitch).yRot(yaw)
                    .add(player.getX(), player.getEyeY(), player.getZ());
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)), pos.x, pos.y, pos.z, 0, velocity.x, velocity.y + 0.05,
                    velocity.z, 1.0);
        }
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.level() instanceof ServerLevel server) {
            if (this.special == Special.ENDER) {
                Effects.enderFx(server, (int) Math.round(target.getX()), (int) Math.round(target.getY()), (int) Math.round(target.getZ()),
                        attacker.getRandom());
            }
            if (this.special == Special.SKULL) {
                server.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 1.0, target.getZ(), 0, 0.0, 0.0, 0.0, 1.0);
            }
        }
        // the item loses one point per hit through the WEAPON component
        float yaw = target.getYRot() * Mth.DEG_TO_RAD;
        target.push(-Mth.sin(yaw) * this.knockback * 0.5F, 0.1, Mth.cos(yaw) * this.knockback * 0.5F);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!level.isClientSide() && entity instanceof Player player) {
            onStoppedGuarding(player);
        }
        return false;
    }

    /** onPlayerStoppedUsing (also when the guard breaks). */
    public void onStoppedGuarding(Player player) {
    }
}
