package idiamondhunter.morebows;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityFrostArrow of 1.5.2: flies like an arrow (same drag and gravity) but dies in water, leaves "splash" particles
 * instead of crit particles, and on a hit deals "thrown" damage plus Slowness III for 15 seconds, without knockback and
 * without sticking in the target. Stuck in a block it drips, turns a water source at its spot into ice, leaves a snow
 * layer there after 64 ticks (or ice on water) and melts away after 80 ticks.
 */
public class FrostArrow extends AbstractArrow {
    private static final EntityDataAccessor<Boolean> CRITICAL = SynchedEntityData.defineId(FrostArrow.class, EntityDataSerializers.BOOLEAN);

    private int ticksInGround;
    /** AbstractArrow keeps its base damage to itself. */
    private double damage = 2.0;

    public FrostArrow(EntityType<? extends FrostArrow> type, Level level) {
        super(type, level);
    }

    public FrostArrow(Level level, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack weapon) {
        super(MoreBows.FROST_ARROW.get(), shooter, level, pickup, weapon);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CRITICAL, false);
    }

    public void setFrostCritical(boolean critical) {
        this.entityData.set(CRITICAL, critical);
    }

    public boolean isFrostCritical() {
        return this.entityData.get(CRITICAL);
    }

    @Override
    public void setBaseDamage(double baseDamage) {
        super.setBaseDamage(baseDamage);
        this.damage = baseDamage;
    }

    @Override
    public void tick() {
        if (!this.isInGround() && this.isFrostCritical()) {
            Vec3 m = this.getDeltaMovement();
            for (int i = 0; i < 4; i++) {
                this.level().addParticle(ParticleTypes.SPLASH, this.getX() + m.x * i / 4.0, this.getY() + m.y * i / 4.0,
                        this.getZ() + m.z * i / 4.0, -m.x, -m.y + 0.2, -m.z);
            }
        }
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.isInGround()) {
            BlockPos pos = this.blockPosition();
            if (!this.level().isClientSide()) {
                if (isWaterSource(this.level().getBlockState(pos))) {
                    this.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                }
                if (this.ticksInGround >= 64 && this.ticksInGround <= 65) {
                    BlockState state = this.level().getBlockState(pos);
                    if (state.isAir()) {
                        this.level().setBlock(pos, Blocks.SNOW.defaultBlockState(), 3);
                    } else if (state.is(Blocks.WATER)) {
                        this.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                    }
                }
            }
            this.ticksInGround++;
            if (this.ticksInGround <= 2) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
            if (this.ticksInGround <= 30) {
                this.level().addParticle(ParticleTypes.DRIPPING_WATER, this.getX(), this.getY() - 0.3, this.getZ(), 0.0, 0.0, 0.0);
            }
            if (this.ticksInGround > 80 && !this.level().isClientSide()) {
                this.discard();
            }
        } else {
            this.ticksInGround = 0;
            if (this.isInWater() && !this.level().isClientSide()) {
                this.discard();
            }
        }
    }

    private static boolean isWaterSource(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity target = hitResult.getEntity();
        float speed = (float) this.getDeltaMovement().length();
        int amount = Mth.ceil(Mth.clamp(speed * this.damage, 0.0, Integer.MAX_VALUE));
        if (this.isFrostCritical()) {
            amount += this.random.nextInt(amount / 2 + 2);
        }
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().thrown(this, owner != null ? owner : this);
        if (target.hurtOrSimulate(source, amount) && target instanceof LivingEntity living && !this.level().isClientSide()) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 300, 2), owner);
        }
        if (this.isOnFire()) {
            target.igniteForSeconds(5.0F);
        }
        // the original hit the target a second time for knockback, arrow count and sound; that hit always ran into
        // the target's hurt cooldown, so none of it ever happened
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        this.setFrostCritical(false);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("ticksInGround", (short) this.ticksInGround);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damage = input.getDoubleOr("damage", 2.0);
        this.ticksInGround = input.getShortOr("ticksInGround", (short) 0);
    }
}
