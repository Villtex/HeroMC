package twilightforest.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import twilightforest.entity.ai.TFGoals;
import twilightforest.entity.projectile.TomeBolt;

/**
 * EntityTFDeathTome: a flying book of the wizard tower, 30 health, speed 0.25, that throws tome bolts every 60 ticks.
 * Fire hurts it twice as much; each hit loses a page 1 time in 2. Enchanting glyphs rise from it. Drops 0..2
 * (+ looting) paper and a book (a book and quill 1 in 5, more often with looting), rarely a magic map focus.
 */
public class DeathTome extends TFMonster {
    public DeathTome(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new TFGoals.MagicAttack(this, 1.0, 60, 1.0F, this::shoot));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private void shoot(LivingEntity target) {
        TomeBolt bolt = new TomeBolt(this.level(), this);
        this.playSound(SoundEvents.GHAST_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
        double tx = target.getX() - this.getX();
        double ty = target.getY() + target.getEyeHeight() - 1.1F - bolt.getY();
        double tz = target.getZ() - this.getZ();
        float heightOffset = Mth.sqrt((float) (tx * tx + tz * tz)) * 0.2F;
        bolt.shootOld(tx, ty + heightOffset, tz, 0.6F, 6.0F);
        this.level().addFreshEntity(bolt);
    }

    @Override
    public int attackStrength(Entity target) {
        return 4;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.ENCHANT, this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.random.nextDouble() * (this.getBbHeight() - 0.75) + 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), 0.0, 0.5, 0.0);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            amount *= 2.0F;
        }
        if (super.hurtServer(level, source, amount)) {
            if (this.random.nextInt(2) == 0) {
                this.spawnAtLocation(level, new ItemStack(Items.PAPER), 1.0F);
            }
            return true;
        }
        return false;
    }
}
