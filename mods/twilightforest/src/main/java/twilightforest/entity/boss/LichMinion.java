package twilightforest.entity.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBosses;

/**
 * EntityTFLichMinion: a zombie of the lich. Dies when its master is gone; hurt by the lich it keeps its target and gets
 * speed V and strength II for 10 seconds. initCreature: only random armour (no babies, no villagers).
 */
public class LichMinion extends Zombie {
    @Nullable Lich master;

    public LichMinion(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public LichMinion(Level level, Lich master) {
        this(TFBosses.LICH_MINION.get(), level);
        this.master = master;
    }

    void initCreature(ServerLevel level) {
        this.populateDefaultEquipmentSlots(this.random, level.getCurrentDifficultyAt(this.blockPosition()));
        this.populateDefaultEquipmentEnchantments(level, this.random, level.getCurrentDifficultyAt(this.blockPosition()));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        LivingEntity prevTarget = this.getTarget();
        if (super.hurtServer(level, source, amount)) {
            if (source.getEntity() instanceof Lich) {
                this.setTarget(prevTarget);
                this.setLastHurtByMob(prevTarget);
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 4));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1));
            }
            return true;
        }
        return false;
    }

    @Override
    public void aiStep() {
        if (this.level() instanceof ServerLevel level) {
            if (this.master == null) {
                for (Lich lich : Lich.nearby(level, this)) {
                    if (!lich.isShadowClone() && lich.wantsNewMinion(level)) {
                        this.master = lich;
                        lich.makeBlackMagicTrail(level, this.getX(), this.getEyeY(), this.getZ(), lich.getX(), lich.getEyeY(), lich.getZ());
                        this.setTarget(lich.attackTarget());
                        break;
                    }
                }
            }
            if (this.master == null || !this.master.isAlive()) {
                this.setHealth(0.0F);
            }
        }
        super.aiStep();
    }

    /** No turning into a drowned, no sun burning differences: a plain zombie otherwise. */
    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
