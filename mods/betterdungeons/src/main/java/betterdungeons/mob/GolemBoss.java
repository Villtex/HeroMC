package betterdungeons.mob;

import betterdungeons.BDFx;
import betterdungeons.api.BDEntityClass;
import betterdungeons.mob.ai.BossAttackGoal;
import betterdungeons.mob.ai.RivalTargetGoal;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityGolemBoss ("GolemBoss", the dwarves' Exterminator Prototype, an EntityGiant): 1000 health, 1.8 x 3.5, 5 xp, the
 * Monking's close attacks and wind-up (35 ticks) but no weak back; its hits do 7..21 and throw up by 0.4. Further than
 * sqrt(30) blocks, 1 in 20 per tick a thunder ball. Targets players (in sight, and nearby ones without sight) and the
 * rival humans spectre, skeleton, zombie and friend; it is of the kind "dwarf". Electric sparks and smoke the more,
 * the less health it has (client side, drawn by the renderer from {@link #powersource}).
 */
public class GolemBoss extends MonkingBoss implements BDEntityClass {
    /** Client side: whether the renderer draws the electric field this tick (rand(health / 20 + 1) == 0). */
    public boolean powersource;

    public GolemBoss(EntityType<? extends GolemBoss> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.totalAttackAnimTime = 35;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** addAITasks plus the third target task of the constructor. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BossAttackGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
        this.targetSelector.addGoal(2, new RivalTargetGoal(this, true, "spectre", "skeleton", "zombie", "friend"));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 0, false, true, null));
    }

    @Override
    public String getEntityClass() {
        return "dwarf";
    }

    @Override
    public boolean isHuman() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.getHealth() > 0.0F) {
            int health = (int) this.getHealth();
            this.powersource = this.random.nextInt(health / 20 + 1) == 0;
            if (this.random.nextInt(health / 5 + 1) == 0) {
                for (int i = 0; i < 10; i++) {
                    ParticleOptions particle;
                    if (this.random.nextInt(3) == 0) {
                        particle = ParticleTypes.LARGE_SMOKE;
                    } else if (this.random.nextInt(2) == 0) {
                        particle = ParticleTypes.CLOUD;
                    } else if (this.random.nextInt(2) == 0) {
                        // "mobspell" with the speeds 0, 0, 0: a black swirl
                        particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000);
                    } else if (this.random.nextInt(2) == 0) {
                        particle = SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1.0F);
                    } else {
                        // "explosion" was no particle of 1.5.2: nothing
                        particle = null;
                    }
                    double x = this.getX() + this.random.nextFloat() * 2.0F - 1.0;
                    double y = this.getY() + this.random.nextFloat() * 3.0F + 1.0;
                    double z = this.getZ() + this.random.nextFloat() * 2.0F - 1.0;
                    if (particle != null) {
                        this.level().addParticle(particle, x, y, z, 0.0, 0.0, 0.0);
                    }
                }
            }
        }
    }

    /** renderHeal: health / max health (the renderer tints it red). */
    public float renderHeal() {
        return this.getHealth() / this.getMaxHealth();
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel level) {
            BDFx.sound(level, this.getX(), this.getY() + 1.0, this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), 4.0F, BDFx.pitch(level));
            this.bigBang();
        }
        super.die(source);
        if (this.level() instanceof ServerLevel) {
            this.bigBang();
        }
    }

    private void bigBang() {
        BDFx.particle(this.level(), ParticleTypes.EXPLOSION_EMITTER, this.getX() + this.random.nextFloat() * 2.0F - 1.0,
                this.getY() + this.random.nextFloat() * 3.0F + 1.0, this.getZ() + this.random.nextFloat() * 2.0F - 1.0, 0.0, 0.0, 0.0);
    }

    @Override
    public boolean isAssWeakPoint() {
        return false;
    }

    @Override
    public void attackEntity(Entity entity, float f) {
        if (this.isAttacking()) {
            return;
        }
        if (f < 8.0F) {
            this.startMelee(entity);
        } else if (f > 30.0F && this.random.nextInt(20) == 0 && !this.level().isClientSide()) {
            BDProjectile.spawn(this, ProjectileType.THUNDER);
        }
    }

    /** attackEntityAsMob: 7 + rand(15) as a mob attack, the target flies up 0.4, the iron golem's throw sound. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        DamageSource source = this.damageSources().mobAttack(this);
        boolean flag = target.hurtServer(level, source, 7 + this.random.nextInt(15));
        if (flag) {
            Vec3 m = target.getDeltaMovement();
            target.setDeltaMovement(m.x, m.y + 0.4F, m.z);
            target.needsSync = true;
        }
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        return flag;
    }

    /** getLivingSound "none": silent. */
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
}
