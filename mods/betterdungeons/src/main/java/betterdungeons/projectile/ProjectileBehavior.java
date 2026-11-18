package betterdungeons.projectile;

import net.minecraft.world.phys.HitResult;

/** ProjectileBase: what one kind of {@link BDProjectile} does in flight and on impact. Runs on both sides like 1.5.2. */
public abstract class ProjectileBehavior {
    protected final BDProjectile entity;

    protected ProjectileBehavior(BDProjectile entity) {
        this.entity = entity;
    }

    public void onUpdateInAir() {
    }

    /** Sprite index in /bdimg/items.png (16 x 16 cells of 16 px), -1 draws nothing. */
    public abstract int getTextureIndex();

    /** Called every tick the flight path touches a block or an entity (not only once), as in 1.5.2. */
    public abstract void onImpact(HitResult hit);

    public boolean canBounce() {
        return true;
    }

    public float getSize() {
        return 1.0F;
    }

    /** Server only, at the end of the constructor (EntityBaseBall.playShootSound). */
    public void onSpawn() {
    }

    public void setDead() {
    }

    /** Length of the rope the renderer lets hang (hook shot). */
    public double getRadius() {
        return 0.0;
    }

    public int getMaxLifeTime() {
        return 600;
    }

    /** Colour of a rope from the shooter's hand to the ball, -1 for none. */
    public int getRopeColor() {
        return -1;
    }
}
