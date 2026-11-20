package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/**
 * MM_Alchemist of Infernal Mobs 1.3.1, ported 1:1.
 * Potion damage values 32732/32698/32660/32696 (splash harming, slowness, poison, weakness) as the witch's potions.
 */
public class MM_Alchemist extends MobModifier {
    private long nextAbilityUse = 0L;
    private static final long coolDown = 6000L;
    private static final float MIN_DISTANCE = 2.0F;
    private static final String[] suffix = {" the Witchkin", " the Brewmaster", " the Singed"};
    private static final String[] prefix = {" witchkin ", " brewing ", " singed "};

    public MM_Alchemist(LivingEntity mob, MobModifier prevMod) {
        this.modName = "Alchemist";
        this.nextMod = prevMod;
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        long time = System.currentTimeMillis();
        if (time > this.nextAbilityUse) {
            this.nextAbilityUse = time + coolDown;
            this.tryAbility(mob, closestVulnerablePlayer(mob, 12.0));
        }
        return super.onUpdate(mob);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target != null && mob.hasLineOfSight(target) && mob.distanceTo(target) > MIN_DISTANCE) {
            Holder<Potion> potion = Potions.HARMING;
            double diffX = target.getX() + target.getDeltaMovement().x - mob.getX();
            double diffY = target.getEyeY() - 1.1F - mob.getY();
            double diffZ = target.getZ() + target.getDeltaMovement().z - mob.getZ();
            float distance = (float) Math.sqrt(diffX * diffX + diffZ * diffZ);
            if (distance >= 8.0F && !target.hasEffect(MobEffects.SLOWNESS)) {
                potion = Potions.SLOWNESS;
            } else if (target.getHealth() >= 8 && !target.hasEffect(MobEffects.POISON)) {
                potion = Potions.POISON;
            } else if (distance <= 3.0F && !target.hasEffect(MobEffects.WEAKNESS) && mob.getRandom().nextFloat() < 0.25F) {
                potion = Potions.WEAKNESS;
            }
            Projectile.spawnProjectileUsingShoot(ThrownSplashPotion::new, serverLevel(mob), PotionContents.createItemStack(Items.SPLASH_POTION, potion),
                    mob, diffX, diffY + distance * 0.2F, diffZ, 0.75F, 8.0F);
        }
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }
}
