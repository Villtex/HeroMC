package idiamondhunter.morebows;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;

import java.util.List;

/**
 * The bows of More Bows (bowDiamond, bowEnder, ... of 1.5.2). All of them shoot like ItemBow.onPlayerStoppedUsing,
 * with their own draw time and arrow speed f * 2.4 instead of f * 2.0 (the Ender Bow keeps 2.0); one point of
 * durability per shot, however many arrows leave the bow. The arrows carry the bow as weapon, so Power, Punch, Flame and
 * Infinity work through the vanilla enchantment effects.
 */
public class MoreBowItem extends BowItem {
    public enum Kind {
        /** Crystal, Golden, Iron and Reinforced Bow. */
        PLAIN,
        /** Blazing Bow: the arrow burns (setFire(70), 70 seconds). */
        FLAME,
        /** Frost Bow: a frost arrow instead of the arrow. */
        FROST,
        /** Legia Bow: a second arrow, a third one in one shot of four. */
        MULTI,
        /** Ender Bow: the arrow and five more around the player. */
        ENDER
    }

    /** EntityArrow multiplied the velocity it was given by 1.5. */
    private static final float ARROW_SPEED = 1.5F;

    /** Ender Bow: velocity and offset (x, y, z) of the five extra arrows. */
    private static final float[][] ENDER_VOLLEY = {
            {1.0F, 0.0F, 0.0F, 0.0F},
            {1.2F, -1.25F, 1.0F, 1.75F},
            {1.5F, -2.25F, 1.45F, -0.75F},
            {1.75F, 0.25F, 2.0F, 2.5F},
            {1.825F, 1.75F, 1.75F, 1.5F},
    };

    private final int drawTicks;
    private final Kind kind;

    public MoreBowItem(Properties properties, int drawTicks, Kind kind) {
        super(properties);
        this.drawTicks = drawTicks;
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    /** Ticks until the bow is fully drawn (the divisor in onPlayerStoppedUsing). */
    public int drawTicks() {
        return this.drawTicks;
    }

    public float powerForTime(int timeHeld) {
        float pow = timeHeld / (float) this.drawTicks;
        pow = (pow * pow + pow * 2.0F) / 3.0F;
        return Math.min(pow, 1.0F);
    }

    @Override
    public boolean releaseUsing(ItemStack bow, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        ItemStack ammo = player.getProjectile(bow);
        // drawing needs an arrow (BowItem.use, as ItemBow), but on release the Ender Bow shot even when the arrow was
        // gone by then (its "second == 3" check was always true)
        if (ammo.isEmpty() && this.kind != Kind.ENDER) {
            return false;
        }
        int charge = EventHooks.onArrowLoose(bow, level, player, this.getUseDuration(bow, entity) - remainingTime);
        if (charge < 0) {
            return false;
        }
        float pow = this.powerForTime(charge);
        if (pow < 0.1F) {
            return false;
        }
        if (level instanceof ServerLevel server) {
            List<ItemStack> drawn = ammo.isEmpty() ? List.of() : draw(bow, ammo, player);
            ItemStack shot;
            if (!drawn.isEmpty()) {
                shot = drawn.getFirst();
            } else if (this.kind == Kind.ENDER) {
                shot = new ItemStack(Items.ARROW);
            } else {
                return false;
            }
            // creative or Infinity: the arrow did not come out of the inventory ("flag" of the original)
            boolean infinite = shot.has(DataComponents.INTANGIBLE_PROJECTILE);
            double power = powerBonus(server, bow);
            boolean crit = pow == 1.0F;
            switch (this.kind) {
                case PLAIN -> this.fire(server, player, bow, this.arrow(server, player, bow, shot, crit), shot, pow * 2.4F);
                case FLAME -> {
                    AbstractArrow arrow = this.arrow(server, player, bow, shot, crit);
                    this.fire(server, player, bow, arrow, shot, pow * 2.4F);
                    // setFire(70) before the Flame enchantment's setFire(100); setFire kept the longer one
                    arrow.setRemainingFireTicks(Math.max(arrow.getRemainingFireTicks(), 70 * 20));
                }
                case FROST -> {
                    ItemStack pickup = new ItemStack(Items.ARROW);
                    if (infinite) {
                        pickup.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
                    }
                    FrostArrow arrow = new FrostArrow(server, player, pickup.copy(), bow);
                    arrow.setFrostCritical(crit);
                    // setDamage(damage + k * 0.5 + 0.5); Punch did nothing (setKnockbackStrength set 0)
                    arrow.setBaseDamage(2.0 + power);
                    this.fire(server, player, bow, arrow, pickup, pow * 2.4F);
                }
                case MULTI -> this.multi(server, player, bow, shot, infinite, crit, pow, power);
                case ENDER -> this.ender(server, player, bow, shot, crit, pow, power);
            }
            bow.hurtAndBreak(1, player, player.getUsedItemHand().asEquipmentSlot());
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + pow * 0.5F);
        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }

    /**
     * Legia Bow: arrows at speed f * 2.4, f * 1.65 and (one time in four) f * 1.275, damage x1.5, x1.3 and x1.3 x1.15;
     * the second arrow sits yaw / 180 blocks east, the third as far west. Power counted twice on the other two arrows
     * (they added it to the first arrow's damage). Only the first and the third arrow can be picked up, both only when
     * an arrow was used up.
     */
    private void multi(ServerLevel server, Player player, ItemStack bow, ItemStack shot, boolean infinite, boolean crit, float pow, double power) {
        AbstractArrow first = this.arrow(server, player, bow, shot, crit);
        setDamage(first, (2.0 + power) * 1.5, power);
        this.fire(server, player, bow, first, shot, pow * 2.4F);

        double other = power > 0.0 ? 2.0 + 2.0 * power : 2.0;
        float offset = player.getYRot() / 180.0F;
        AbstractArrow second = extraArrow(server, player, bow, crit);
        second.pickup = AbstractArrow.Pickup.DISALLOWED;
        setDamage(second, other * 1.3, power);
        second.setPos(second.getX() + offset, second.getY(), second.getZ());
        this.fire(server, player, bow, second, second.getPickupItemStackOrigin(), pow * 1.65F);

        if (server.getRandom().nextInt(4) == 0) {
            AbstractArrow third = extraArrow(server, player, bow, crit);
            third.pickup = infinite ? AbstractArrow.Pickup.DISALLOWED : AbstractArrow.Pickup.ALLOWED;
            setDamage(third, other * 1.3 * 1.15, power);
            third.setPos(third.getX() - offset, third.getY(), third.getZ());
            this.fire(server, player, bow, third, third.getPickupItemStackOrigin(), pow * 1.275F);
        }
    }

    /**
     * Ender Bow: the arrow (speed f * 2.0, damage x1.25) and five more from fixed spots around the player with fixed
     * speeds. None of them can be picked up. The original slept the server thread for three seconds before the five
     * extra arrows: the world stood still, so all six left in the same game tick. Here they do the same, without the
     * freeze.
     */
    private void ender(ServerLevel server, Player player, ItemStack bow, ItemStack shot, boolean crit, float pow, double power) {
        AbstractArrow first = this.arrow(server, player, bow, shot, crit);
        first.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        setDamage(first, (2.0 + power) * 1.25, power);
        this.fire(server, player, bow, first, shot, pow * 2.0F);

        double other = 2.0 + 2.0 * power;
        for (float[] spot : ENDER_VOLLEY) {
            AbstractArrow arrow = extraArrow(server, player, bow, crit);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            setDamage(arrow, other, power);
            arrow.setPos(arrow.getX() + spot[1], arrow.getY() + spot[2], arrow.getZ() + spot[3]);
            this.fire(server, player, bow, arrow, arrow.getPickupItemStackOrigin(), spot[0]);
        }
    }

    private AbstractArrow arrow(ServerLevel server, Player player, ItemStack bow, ItemStack shot, boolean crit) {
        if (this.createProjectile(server, player, bow, shot, crit) instanceof AbstractArrow arrow) {
            return arrow;
        }
        AbstractArrow arrow = new Arrow(server, player, shot, bow);
        arrow.setCritArrow(crit);
        return arrow;
    }

    private static AbstractArrow extraArrow(ServerLevel server, Player player, ItemStack bow, boolean crit) {
        AbstractArrow arrow = new Arrow(server, player, new ItemStack(Items.ARROW), bow);
        arrow.setCritArrow(crit);
        return arrow;
    }

    /**
     * Spawns the arrow flying where the player looks with the given 1.5.2 velocity (x1.5 as in EntityArrow) and
     * inaccuracy 1. Unlike today's bow, the player's own motion is not added, as in 1.5.2.
     */
    private void fire(ServerLevel server, Player player, ItemStack bow, Projectile arrow, ItemStack projectile, float velocity) {
        float xRot = player.getXRot() * Mth.DEG_TO_RAD;
        float yRot = player.getYRot() * Mth.DEG_TO_RAD;
        double xd = -Mth.sin(yRot) * Mth.cos(xRot);
        double yd = -Mth.sin(xRot);
        double zd = Mth.cos(yRot) * Mth.cos(xRot);
        Projectile.spawnProjectile(arrow, server, projectile, p -> p.shoot(xd, yd, zd, velocity * ARROW_SPEED, 1.0F));
    }

    /**
     * Sets the damage the 1.5.2 arrow ended up with. Power is added again when the arrow hits (vanilla effect, the same
     * 0.5 * level + 0.5), so it is taken off here.
     */
    private static void setDamage(AbstractArrow arrow, double damage, double power) {
        arrow.setBaseDamage(damage - power);
    }

    /** Damage Power adds to an arrow: k * 0.5 + 0.5 for level k, as in 1.5.2 and today. */
    static double powerBonus(ServerLevel server, ItemStack bow) {
        int level = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.POWER)
                .map(bow::getEnchantmentLevel).orElse(0);
        return level > 0 ? level * 0.5 + 0.5 : 0.0;
    }
}
