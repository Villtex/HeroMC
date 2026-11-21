package twilightforest.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;

/**
 * EntityThrowable of 1.5.2 for the mobs' magic: thrown from the eyes of its owner, drawn as an item
 * (RenderSnowball), with its own gravity.
 */
public abstract class TFThrowable extends ThrowableItemProjectile {
    protected TFThrowable(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    protected TFThrowable(EntityType<? extends ThrowableItemProjectile> type, LivingEntity owner, Level level) {
        super(type, owner, level, net.minecraft.world.item.ItemStack.EMPTY);
        this.setItem(new net.minecraft.world.item.ItemStack(this.getDefaultItem()));
    }

    /** setThrowableHeading: the spread of 1.5.2 (gaussian × 0.0075 × inaccuracy) in today's terms. */
    public void shootOld(double x, double y, double z, float velocity, float inaccuracy) {
        this.shoot(x, y, z, velocity, inaccuracy * 0.0075F / 0.0172275F);
    }

    /** getGravityVelocity of the subclass. */
    protected abstract float gravity();

    @Override
    protected double getDefaultGravity() {
        return this.gravity();
    }
}
