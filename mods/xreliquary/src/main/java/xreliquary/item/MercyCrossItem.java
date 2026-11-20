package xreliquary.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import xreliquary.XRFx;

/** ItemMercyCross: a gold sword (4, 64 uses) that deals twice its damage to skeletons, ghasts, the wither and zombies. */
public class MercyCrossItem extends XRItem {
    public MercyCrossItem(Properties properties) {
        super(properties, 3);
    }

    public static boolean undead(Entity e) {
        return e instanceof AbstractSkeleton || e instanceof Ghast || e instanceof WitherBoss || e instanceof Zombie;
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
        if (undead(victim)) {
            XRFx.particle(victim.level(), ParticleTypes.EXPLOSION, victim.getX(), victim.getY() + victim.getBbHeight() / 2.0F, victim.getZ(), 0, 0, 0);
            return 4.0F;
        }
        return 0.0F;
    }
}
