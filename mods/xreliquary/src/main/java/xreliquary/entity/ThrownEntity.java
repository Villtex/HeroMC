package xreliquary.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import xreliquary.XREntities;
import xreliquary.XRFx;
import xreliquary.item.MercyCrossItem;
import xreliquary.item.PotionKind;

import java.util.Optional;

/**
 * The thrown things of the Reliquary (EntityThrowable of 1.5.2): glowing water, the holy hand grenade, the ten splash
 * serums (EntityCondensedSplashXxx, EntityCondensedFertility) and the ice rod's snowball (EntitySpecialSnowball, which
 * moved twice per tick). One entity with a {@link Kind}; the sprite index of thrownItemsSheet.png comes with the kind.
 */
public class ThrownEntity extends Projectile {
    private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(ThrownEntity.class, EntityDataSerializers.BYTE);
    private int ticksInAir;

    /** speed (func_70182_d), pitch offset (func_70183_g), gravity, render index, splash colour (red, green, blue). */
    public enum Kind {
        GLOWING_WATER(0.7F, -20.0F, 0.03F, 0, 0x385DC6),
        GRENADE(0.9F, -20.0F, 0.04F, 12, 0),
        APHRODITE(0.7F, -20.0F, 0.03F, 2, 0xFF0000),
        POISON(0.7F, -20.0F, 0.03F, 3, 0x00FF00),
        ACID(0.7F, -20.0F, 0.03F, 4, 0xFF7F00),
        CONFUSION(0.7F, -20.0F, 0.03F, 5, 0xFF00FF),
        SLOWING(0.7F, -20.0F, 0.03F, 6, 0x0066FF),
        WEAKNESS(0.7F, -20.0F, 0.03F, 7, 0xFFFF00),
        WITHER(0.7F, -20.0F, 0.03F, 8, 0x666666),
        BLINDING(0.7F, -20.0F, 0.03F, 9, 0x000000),
        RUINATION(0.7F, -20.0F, 0.03F, 10, 0x4DFF1A),
        FERTILIZER(0.7F, -20.0F, 0.03F, 11, 0xFF0000),
        ICE(1.2F, 0.0F, 0.01F, -1, 0);

        public final float speed;
        public final float pitchOffset;
        public final float gravity;
        public final int sprite;
        public final int color;

        Kind(float speed, float pitchOffset, float gravity, int sprite, int color) {
            this.speed = speed;
            this.pitchOffset = pitchOffset;
            this.gravity = gravity;
            this.sprite = sprite;
            this.color = color;
        }

        public static Kind splash(PotionKind potion) {
            return values()[potion.ordinal() + 1];
        }
    }

    public ThrownEntity(EntityType<? extends ThrownEntity> type, Level level) {
        super(type, level);
    }

    public ThrownEntity(Level level, LivingEntity thrower, Kind kind) {
        this(XREntities.THROWN.get(), level);
        this.setOwner(thrower);
        this.entityData.set(KIND, (byte) kind.ordinal());
        float yaw = thrower.getYRot();
        float pitch = thrower.getXRot();
        double x = thrower.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
        double y = thrower.getEyeY() - 0.1F;
        double z = thrower.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
        this.snapTo(x, y, z, yaw, pitch);
        float f = 0.4F;
        double mx = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f;
        double mz = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f;
        double my = -Mth.sin((pitch + kind.pitchOffset) / 180.0F * (float) Math.PI) * f;
        double len = Math.sqrt(mx * mx + my * my + mz * mz);
        mx = mx / len + this.random.nextGaussian() * 0.0075F;
        my = my / len + this.random.nextGaussian() * 0.0075F;
        mz = mz / len + this.random.nextGaussian() * 0.0075F;
        this.setDeltaMovement(mx * kind.speed, my * kind.speed, mz * kind.speed);
    }

    public Kind kind() {
        int k = this.entityData.get(KIND);
        return k >= 0 && k < Kind.values().length ? Kind.values()[k] : Kind.GLOWING_WATER;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, (byte) 0);
    }

    @Override
    protected double getDefaultGravity() {
        return this.kind().gravity;
    }

    @Override
    public void tick() {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        this.baseTick();
        if (this.isRemoved()) {
            return;
        }
        this.step();
        if (this.kind() == Kind.ICE && !this.isRemoved()) {
            if (this.level().isClientSide() && this.ticksInAir % 4 == this.random.nextInt(5)) {
                this.level().addParticle(new net.minecraft.core.particles.DustParticleOptions(0xFFFFFF, 1.0F), this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
            this.step();
        }
    }

    /** One EntityThrowable.onUpdate move. */
    private void step() {
        this.ticksInAir++;
        Kind kind = this.kind();
        Vec3 start = this.position();
        Vec3 motion = this.getDeltaMovement();
        Vec3 end = start.add(motion);
        HitResult hit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            end = hit.getLocation();
        }
        if (!this.level().isClientSide()) {
            Entity owner = this.getOwner();
            Entity closest = null;
            double best = 0.0;
            double grow = kind == Kind.ICE ? 0.1 : 0.3;
            for (Entity e : this.level().getEntities(this, this.getBoundingBox().expandTowards(motion).inflate(1.0))) {
                if (e.isPickable() && e.isAlive() && (e != owner || this.ticksInAir >= 5)) {
                    Optional<Vec3> clip = e.getBoundingBox().inflate(grow).clip(start, end);
                    if (clip.isPresent()) {
                        double d = start.distanceTo(clip.get());
                        if (d < best || best == 0.0) {
                            closest = e;
                            best = d;
                        }
                    }
                }
            }
            if (closest != null) {
                hit = new EntityHitResult(closest);
            }
            if (hit.getType() != HitResult.Type.MISS) {
                this.impact((ServerLevel) this.level(), hit, kind);
                if (this.isRemoved()) {
                    return;
                }
            }
        }
        motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
        this.updateRotation();
        float f = 0.99F;
        if (this.isInWater()) {
            f = 0.8F;
        }
        this.setDeltaMovement(motion.x * f, motion.y * f - kind.gravity, motion.z * f);
    }

    private @Nullable Player thrower() {
        return this.getOwner() instanceof Player p ? p : null;
    }

    private void impact(ServerLevel level, HitResult hit, Kind kind) {
        BlockPos at = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        AABB area = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        switch (kind) {
            case ICE -> {
                if (hit instanceof EntityHitResult eh) {
                    eh.getEntity().hurtServer(level, level.damageSources().thrown(this, this.getOwner()), eh.getEntity() instanceof Blaze ? 8 : 2);
                }
                for (int i = 0; i < 8; i++) {
                    level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.SNOWBALL), this.getX(), this.getY(), this.getZ(),
                            1, 0, 0, 0, 0);
                }
            }
            case GRENADE -> ConcussiveExplosion.explode(level, this, this.thrower(), this.getX(), this.getY(), this.getZ(), 4.0F);
            case GLOWING_WATER -> {
                for (int i = 0; i < 40; i++) {
                    level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 1.0F, 1.0F, 0.0F),
                            this.getX() + this.random.nextGaussian() * 4.0, this.getY() + this.random.nextGaussian() * 4.0,
                            this.getZ() + this.random.nextGaussian() * 4.0, 1, 0, 0, 0, 0);
                }
                Player player = this.thrower();
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (MercyCrossItem.undead(e) && player != null) {
                        e.hurtServer(level, level.damageSources().playerAttack(player), 18 + this.random.nextInt(17));
                    }
                }
                level.levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, BlockPos.containing(Math.round(this.getX()), Math.round(this.getY()),
                        Math.round(this.getZ())), kind.color);
            }
            case FERTILIZER -> {
                this.splash(level, kind);
                BlockPos c = BlockPos.containing(this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5);
                if (this.thrower() != null) {
                    for (BlockPos pos : BlockPos.betweenClosed(c.offset(-1, -2, -1), c.offset(1, 1, 1))) {
                        if (BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, pos)) {
                            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
                        }
                    }
                }
            }
            default -> {
                this.splash(level, kind);
                if (kind == Kind.APHRODITE) {
                    for (Animal e : level.getEntitiesOfClass(Animal.class, area)) {
                        if (e.getAge() == 0) {
                            e.setInLoveTime(600);
                            for (int i = 0; i < 7; i++) {
                                level.sendParticles(ParticleTypes.HEART, e.getRandomX(1.0), e.getRandomY() + 0.5, e.getRandomZ(1.0), 1, 0.02, 0.02, 0.02, 0);
                            }
                        }
                    }
                } else {
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area)) {
                        switch (kind) {
                            case POISON -> e.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 0));
                            case ACID -> e.hurtServer(level, level.damageSources().magic(), 12);
                            case CONFUSION -> e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
                            case SLOWING -> e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 1200, 0));
                            case WEAKNESS -> e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0));
                            case WITHER -> e.addEffect(new MobEffectInstance(MobEffects.WITHER, 1200, 0));
                            case BLINDING -> e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 1200, 0));
                            case RUINATION -> {
                                e.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 0));
                                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0));
                                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 1200, 0));
                            }
                            default -> {
                            }
                        }
                    }
                }
            }
        }
        this.discard();
    }

    /** spawnParticles: the potion break of 1.5.2 in the serum's colour, and "random.glass". */
    private void splash(ServerLevel level, Kind kind) {
        level.levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, this.blockPosition(), kind.color);
        XRFx.sound(level, this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5, SoundEvents.GLASS_BREAK, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
