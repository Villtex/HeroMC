package legendgear.entity;

import legendgear.LGEntities;
import legendgear.LGFx;
import legendgear.LGParticles;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * EntityArrowStorm: for 50 ticks one arrow a tick rains from 3 blocks above the thrower onto the spot the wind medallion
 * hit (or onto the mob it hit, following it): damage 5, speed 2.5, spread 12, not to be picked up.
 */
public class ArrowStormEntity extends LGEffectEntity {
    public static final int MAX_LIFESPAN = 50;
    private @Nullable LivingEntity target;

    public ArrowStormEntity(EntityType<? extends ArrowStormEntity> type, Level level) {
        super(type, level);
    }

    public ArrowStormEntity(Level level, double x, double y, double z, @Nullable LivingEntity responsible, @Nullable LivingEntity target) {
        this(LGEntities.ARROW_STORM.get(), level);
        this.setPos(x, y, z);
        this.thrower = responsible;
        this.target = target;
    }

    @Override
    public void tick() {
        super.tick();
        if (LGParticles.client(this.level()) && this.lifetime == 0) {
            for (int i = 0; i < 15; i++) {
                LGParticles.rune(this.level(), this.getX() + this.random.nextGaussian() * 0.3, this.getY(), this.getZ() + this.random.nextGaussian() * 0.3,
                        0.0, this.random.nextDouble(), 0.0, 1.5F);
            }
        }
        if (!this.level().isClientSide() && this.thrower instanceof LivingEntity shooter) {
            if (this.target != null) {
                this.setPos(this.target.getX(), this.target.getY(), this.target.getZ());
            }
            Arrow arrow = new Arrow(this.level(), shooter, new ItemStack(Items.ARROW), null);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            arrow.setBaseDamage(5.0);
            arrow.setPos(shooter.getX(), shooter.getY() + 3.0, shooter.getZ());
            double dist = this.distanceTo(arrow);
            double vx = this.getX() - arrow.getX();
            double vy = this.getY() + dist * 0.05 * 2.5 - arrow.getY();
            double vz = this.getZ() - arrow.getZ();
            arrow.shoot(vx, vy, vz, 2.5F, 12.0F);
            this.level().addFreshEntity(arrow);
            LGFx.sound(shooter, SoundEvents.FIRE_EXTINGUISH, 0.1F, 1.0F / (this.random.nextFloat() * 0.4F + 1.2F) + 0.5F);
        }
        this.lifetime++;
        if (this.lifetime > MAX_LIFESPAN && !this.level().isClientSide()) {
            this.discard();
        }
    }
}
