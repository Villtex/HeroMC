package betterdungeons.projectile;

import java.util.function.Function;

/**
 * EnumProjectile: the kinds of {@link BDProjectile} with their 1.5.2 numbers (synced as the entity's type byte).
 * Numbers without a kind (8, 9, 25..49) made a ProjectileDummy, as do unknown ones here.
 */
public enum ProjectileType {
    GRENADE_WATER(0, Behaviors.GrenadeWater::new),
    GRENADE_POISON(1, Behaviors.GrenadePoison::new),
    GRENADE_ICE(2, Behaviors.GrenadeIce::new),
    GRENADE_FIRE(3, Behaviors.GrenadeFire::new),
    GRENADE_ARCANE(4, Behaviors.GrenadeArcane::new),
    GRENADE_ELECTRIC(5, Behaviors.GrenadeElectric::new),
    GRENADE_WIND(6, Behaviors.GrenadeWind::new),
    GRENADE_LIFE(7, Behaviors.GrenadeLife::new),
    VAMPIRE(10, Behaviors.VampiricBall::new),
    FIREBALL(11, Behaviors.FireBall::new),
    CAPSULE(12, Behaviors.Capsule::new),
    WEB(13, Behaviors.SpiderWeb::new),
    BUBBLE(14, Behaviors.Bubble::new),
    BULLET_PISTOL(15, Behaviors.BulletPistol::new),
    WINDBALL(16, Behaviors.WindBall::new),
    FIRESHIELD(17, Behaviors.FireShield::new),
    EARTHQUAKE_AREA(18, Behaviors.EarthQuakeArea::new),
    FIREWALL(19, Behaviors.FireWall::new),
    EARTHQUAKE(20, Behaviors.EarthQuake::new),
    HEALBALL(21, Behaviors.HealBall::new),
    THUNDER(22, Behaviors.ThunderBolt::new),
    WEB_VERTICAL(23, Behaviors.SpiderWebVertical::new),
    SLINGSHOT(24, Behaviors.Slingshot::new),
    HOOKSHOT(50, Behaviors.HookShoot::new),
    /** The hook while it holds on (EnumProjectile.HOOKSHOTREELING); getBallData kept the hook behaviour. */
    HOOKSHOT_REELING(51, Behaviors.HookShoot::new);

    public final int id;
    final Function<BDProjectile, ProjectileBehavior> factory;

    ProjectileType(int id, Function<BDProjectile, ProjectileBehavior> factory) {
        this.id = id;
        this.factory = factory;
    }

    /** EnumProjectile.getBallData. */
    static ProjectileBehavior behavior(BDProjectile ball) {
        int type = ball.getProjectileType();
        for (ProjectileType t : values()) {
            if (t.id == type) {
                return t.factory.apply(ball);
            }
        }
        return new Behaviors.Dummy(ball);
    }

    public static ProjectileType byId(int id) {
        for (ProjectileType t : values()) {
            if (t.id == id) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown Better Dungeons projectile " + id);
    }
}
