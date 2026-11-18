package betterdungeons.projectile;

import betterdungeons.BDFx;
import betterdungeons.BDSounds;
import betterdungeons.api.BDEntityIds;
import betterdungeons.item.CapsuleData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Random;

/**
 * The ProjectileBase subclasses of chocolate.mods.BD.projectile, one nested class each. Damage values are the 1.5.2
 * half-hearts (as today). Code that ran on both sides runs on both sides; effects that need the server are guarded.
 */
final class Behaviors {
    private Behaviors() {
    }

    static Level level(BDProjectile e) {
        return e.level();
    }

    static boolean client(BDProjectile e) {
        return e.level().isClientSide();
    }

    static Entity hitEntity(HitResult hit) {
        return hit instanceof EntityHitResult e ? e.getEntity() : null;
    }

    /** The block the hit face points to (the switch over sideHit of 1.5.2). */
    static BlockPos beside(BlockHitResult hit) {
        return hit.getBlockPos().relative(hit.getDirection());
    }

    /** "tilecrack_" + id + "_0" of the block under the ball, glass for air. */
    static BlockParticleOption crack(BDProjectile e, int x, int y, int z) {
        BlockState state = level(e).getBlockState(new BlockPos(x, y, z));
        if (state.isAir()) {
            state = Blocks.GLASS.defaultBlockState();
        }
        return new BlockParticleOption(ParticleTypes.BLOCK, state);
    }

    static void addMotion(Entity e, double x, double y, double z) {
        e.setDeltaMovement(e.getDeltaMovement().add(x, y, z));
    }

    static void setMotion(Entity e, double x, double y, double z) {
        e.setDeltaMovement(x, y, z);
    }

    /** Glass, panes and glowstone (Material.glass) break under bullets. */
    static boolean isGlass(BlockState state) {
        return state.is(Tags.BlockItems.GLASS_BLOCKS.block()) || state.is(Tags.BlockItems.GLASS_PANES.block()) || state.is(Blocks.GLOWSTONE);
    }

    static float size(Entity e) {
        return e.getBbWidth() * 2.0F + e.getBbHeight();
    }

    static void fireSound(BDProjectile ball, double x, double y, double z) {
        BDFx.sound(level(ball), x, y, z, SoundEvents.FIRE_AMBIENT, 4.0F, BDFx.pitch(level(ball)));
    }

    // ---------------------------------------------------------------- dummy

    static final class Dummy extends ProjectileBehavior {
        Dummy(BDProjectile entity) {
            super(entity);
        }

        @Override
        public int getTextureIndex() {
            return 0;
        }

        @Override
        public void onImpact(HitResult hit) {
        }
    }

    // ---------------------------------------------------------------- grenades (types 0..7)

    /** The common part of ProjectileGrenadeX: hit everything living within 4 blocks, splash, fall with 0.1 per tick. */
    abstract static class Grenade extends ProjectileBehavior {
        private final int texture;
        private final int particleEffect;
        private final boolean skipsShooter;

        Grenade(BDProjectile entity, int texture, int particleEffect, boolean skipsShooter) {
            super(entity);
            this.texture = texture;
            this.particleEffect = particleEffect;
            this.skipsShooter = skipsShooter;
        }

        @Override
        public int getTextureIndex() {
            return this.texture;
        }

        abstract void affect(LivingEntity e);

        @Override
        public void onImpact(HitResult hit) {
            if (client(this.entity)) {
                return;
            }
            List<LivingEntity> list = level(this.entity).getEntitiesOfClass(LivingEntity.class, this.entity.getBoundingBox().inflate(4.0, 2.0, 4.0));
            for (LivingEntity e : list) {
                if ((!this.skipsShooter || e != this.entity.shooter()) && this.entity.distanceToSqr(e) < 16.0) {
                    this.affect(e);
                }
            }
            if (this.entity.hitsSolid(hit)) {
                BDFx.splash(level(this.entity), this.entity.getX(), this.entity.getY(), this.entity.getZ(), this.particleEffect);
                this.entity.discard();
            }
        }

        @Override
        public void onUpdateInAir() {
            addMotion(this.entity, 0.0, -0.1F, 0.0);
        }
    }

    static final class GrenadeWater extends Grenade {
        GrenadeWater(BDProjectile e) {
            super(e, 224, 0, false);
        }

        @Override
        void affect(LivingEntity e) {
            e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
            e.clearFire();
            if (e != this.entity.shooter()) {
                BDFx.hurt(e, BDFx.indirect(this.entity, this.entity.shooter()), e.fireImmune() ? 16 : 8);
            }
        }
    }

    static final class GrenadePoison extends Grenade {
        GrenadePoison(BDProjectile e) {
            super(e, 225, 4, true);
        }

        @Override
        void affect(LivingEntity e) {
            e.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 0));
        }
    }

    static final class GrenadeIce extends Grenade {
        GrenadeIce(BDProjectile e) {
            super(e, 226, 2, true);
        }

        @Override
        void affect(LivingEntity e) {
            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1));
            BDFx.hurt(e, BDFx.indirect(this.entity, this.entity.shooter()), 8);
        }
    }

    static final class GrenadeFire extends Grenade {
        GrenadeFire(BDProjectile e) {
            super(e, 227, 3, true);
        }

        @Override
        void affect(LivingEntity e) {
            e.igniteForSeconds(8);
        }
    }

    static final class GrenadeArcane extends Grenade {
        GrenadeArcane(BDProjectile e) {
            super(e, 228, 12, true);
        }

        @Override
        void affect(LivingEntity e) {
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1));
            BDFx.hurt(e, BDFx.indirect(this.entity, this.entity.shooter()), 8);
        }
    }

    static final class GrenadeElectric extends Grenade {
        GrenadeElectric(BDProjectile e) {
            super(e, 229, 6, true);
        }

        @Override
        void affect(LivingEntity e) {
            int damage = 4;
            if (e.getEffect(MobEffects.NAUSEA) != null || e.isInWaterOrRain()) {
                damage = 8;
                e.removeEffect(MobEffects.NAUSEA);
            }
            BDFx.hurt(e, BDFx.magic(this.entity, this.entity.shooter()), damage);
        }
    }

    static final class GrenadeWind extends Grenade {
        GrenadeWind(BDProjectile e) {
            super(e, 230, 2, false);
        }

        /** repel() runs once per living entity in range (the shooter too), so the push adds up as in 1.5.2. */
        @Override
        void affect(LivingEntity e) {
            BDProjectile ball = this.entity;
            double size = 3.0;
            double strength = 2.0;
            AABB box = new AABB(Mth.floor(ball.getX() - size - 1.0), Mth.floor(ball.getY() - size - 1.0), Mth.floor(ball.getZ() - size - 1.0),
                    Mth.floor(ball.getX() + size + 1.0), Mth.floor(ball.getY() + size + 1.0), Mth.floor(ball.getZ() + size + 1.0));
            for (Entity other : level(ball).getEntities(ball, box)) {
                if (other != ball.shooter()) {
                    other.push(-(ball.getX() - other.getX()) * strength, 0.0, -(ball.getZ() - other.getZ()) * strength);
                    other.syncVelocity = true;
                    BDFx.hurt(other, other.damageSources().generic(), 4);
                }
            }
        }
    }

    static final class GrenadeLife extends Grenade {
        GrenadeLife(BDProjectile e) {
            super(e, 231, 1, false);
        }

        @Override
        void affect(LivingEntity e) {
            e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        }
    }

    // ---------------------------------------------------------------- balls of the mages

    static final class VampiricBall extends ProjectileBehavior {
        private final Random rand = new Random();

        VampiricBall(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return 244;
        }

        @Override
        public void onImpact(HitResult hit) {
            if (client(this.entity)) {
                return;
            }
            Entity target = hitEntity(hit);
            if (target != null) {
                if (target instanceof LivingEntity && BDFx.hurt(target, BDFx.bullet(this.entity, this.entity.shooter()), this.entity.getLvl())) {
                    LivingEntity shooter = this.entity.shooter();
                    if (shooter != null) {
                        BDProjectile heal = BDProjectile.create(shooter, ProjectileType.HEALBALL);
                        heal.setLvl(this.entity.getLvl());
                        heal.setPos(this.entity.getX(), this.entity.getY(), this.entity.getZ());
                        level(this.entity).addFreshEntity(heal);
                    }
                    this.entity.discard();
                }
            } else {
                this.entity.discard();
            }
        }

        @Override
        public void onUpdateInAir() {
            for (int i = 0; i < 1 + this.entity.getLvl(); i++) {
                level(this.entity).addParticle(ParticleTypes.PORTAL, this.entity.getX() + this.rand.nextFloat() - 0.5,
                        this.entity.getY() + this.rand.nextFloat() - 0.5, this.entity.getZ() + this.rand.nextFloat() - 0.5,
                        this.rand.nextFloat() - 0.5F, this.rand.nextFloat() - 0.5F, this.rand.nextFloat() - 0.5F);
            }
        }

        @Override
        public float getSize() {
            return 0.5F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        @Override
        public void onSpawn() {
            BDFx.sound(level(this.entity), this.entity.getX(), this.entity.getY(), this.entity.getZ(), SoundEvents.ARROW_SHOOT, 4.0F,
                    BDFx.pitch(level(this.entity)));
        }
    }

    /** ProjectileFireBall. Its particles were named "fire", which 1.5.2 did not know: it flies without any. */
    static final class FireBall extends ProjectileBehavior {
        FireBall(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return 243;
        }

        @Override
        public void onImpact(HitResult hit) {
            if (client(this.entity)) {
                return;
            }
            Entity e = hitEntity(hit);
            if (e != null) {
                if (!BDEntityIds.is(e, "SummonedUndead")) {
                    BDFx.hurt(e, e.damageSources().lava(), 3);
                    e.igniteForSeconds(8);
                    fireSound(this.entity, e.getX(), e.getY(), e.getZ());
                    this.entity.discard();
                }
            } else {
                this.entity.discard();
            }
        }

        @Override
        public float getSize() {
            return 0.5F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }
    }

    /** ProjectileCapsule: a thrown capsule unpacks its building on the block it hits (nothing in 1.5.2 throws one). */
    static final class Capsule extends ProjectileBehavior {
        Capsule(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return 24;
        }

        @Override
        public void onImpact(HitResult hit) {
            if (!(level(this.entity) instanceof ServerLevel server)) {
                return;
            }
            ItemStack item = this.entity.item;
            BlockPos at = hit instanceof BlockHitResult block ? block.getBlockPos() : BlockPos.ZERO;
            if (CapsuleData.isFull(item)) {
                int[] size = CapsuleData.size(item);
                CapsuleData.paste(server, item, at.offset(-size[0] / 2, 1, -size[2] / 2));
            }
            BDFx.splash(server, this.entity.getX(), this.entity.getY(), this.entity.getZ(), 2);
            this.entity.discard();
        }

        @Override
        public void onUpdateInAir() {
            addMotion(this.entity, 0.0, -0.1F, 0.0);
        }
    }

    // ---------------------------------------------------------------- webs, bubbles, hooks

    static final class SpiderWeb extends ProjectileBehavior {
        SpiderWeb(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return 241;
        }

        @Override
        public void onImpact(HitResult hit) {
            if (this.entity.shooter() != null) {
                if (hitEntity(hit) instanceof LivingEntity target) {
                    if (!client(this.entity)) {
                        this.entity.startRiding(target, true, true);
                    }
                } else if (!client(this.entity)) {
                    this.entity.discard();
                }
            }
        }

        @Override
        public int getMaxLifeTime() {
            return this.entity.getVehicle() == null ? 30 : 1000;
        }

        @Override
        public void onUpdateInAir() {
            LivingEntity shooting = this.entity.shooter();
            if (shooting == null) {
                if (!client(this.entity)) {
                    this.entity.discard();
                }
                return;
            }
            Entity ride = this.entity.getVehicle();
            if (ride != null) {
                Vec3 fc = new Vec3(shooting.getX() - this.entity.getX(), shooting.getY() + 0.4 - this.entity.getY(), shooting.getZ() - this.entity.getZ());
                if (fc.length() < 2.0 && !client(this.entity)) {
                    this.entity.discard();
                }
                // ProjectileBase.setDead() did nothing: a sneaking player is not freed
                boolean pullToShooter = size(shooting) + 0.2F > size(ride);
                double s = 0.5;
                if (pullToShooter) {
                    fc = fc.normalize();
                    setMotion(ride, fc.x * s, fc.y * s, fc.z * s);
                    setMotion(this.entity, fc.x * s, fc.y * s, fc.z * s);
                } else {
                    setMotion(shooting, -fc.x * s, -fc.y * s, -fc.z * s);
                }
            }
        }

        @Override
        public float getSize() {
            return 0.5F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        @Override
        public int getRopeColor() {
            return 0xFFFFFF;
        }
    }

    static final class Bubble extends ProjectileBehavior {
        private final Random rand = new Random();
        private int mountTime;

        Bubble(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return 240;
        }

        @Override
        public void onImpact(HitResult hit) {
            BDFx.sound(level(this.entity), this.entity.getX(), this.entity.getY(), this.entity.getZ(), BDSounds.BUBBLE.get(), 4.0F,
                    BDFx.pitch(level(this.entity)));
            if (!client(this.entity)) {
                if (hitEntity(hit) instanceof LivingEntity target) {
                    if (target.getVehicle() == null) {
                        target.startRiding(this.entity, true, true);
                    }
                } else {
                    this.entity.discard();
                }
            }
        }

        @Override
        public void onUpdateInAir() {
            if (this.entity.isVehicle()) {
                setMotion(this.entity, 0.0, 0.1, 0.0);
                this.mountTime++;
                if (this.mountTime == 70 && !client(this.entity)) {
                    this.entity.discard();
                }
            }
            for (int i = 0; i < 1 + this.entity.getLvl(); i++) {
                level(this.entity).addParticle(ParticleTypes.SPLASH, this.entity.getX() + this.rand.nextFloat() - 0.5,
                        this.entity.getY() + this.rand.nextFloat() - 0.5, this.entity.getZ() + this.rand.nextFloat() - 0.5, 0.0, -1.0, 0.0);
            }
        }

        @Override
        public float getSize() {
            return 1.5F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        @Override
        public void onSpawn() {
            BDFx.sound(level(this.entity), this.entity.getX(), this.entity.getY(), this.entity.getZ(), SoundEvents.GENERIC_SWIM, 4.0F,
                    BDFx.pitch(level(this.entity)));
        }
    }

    /** Shared by the pistol bullet and the slingshot stone: break glass, stop at anything solid. */
    abstract static class Bullet extends ProjectileBehavior {
        Bullet(BDProjectile e) {
            super(e);
        }

        abstract float damage();

        @Override
        public void onImpact(HitResult hit) {
            if (client(this.entity)) {
                return;
            }
            Entity target = hitEntity(hit);
            if (target != null) {
                BDFx.hurt(target, BDFx.bullet(this.entity, this.entity.shooter()), this.damage());
                this.entity.discard();
            }
            if (hit instanceof BlockHitResult block && isGlass(level(this.entity).getBlockState(block.getBlockPos()))) {
                level(this.entity).setBlock(block.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            }
            if (this.entity.hitsSolid(hit)) {
                this.entity.discard();
            }
        }

        @Override
        public boolean canBounce() {
            return false;
        }
    }

    static final class BulletPistol extends Bullet {
        BulletPistol(BDProjectile e) {
            super(e);
        }

        @Override
        float damage() {
            return 8;
        }

        @Override
        public int getTextureIndex() {
            return 242;
        }

        @Override
        public void onSpawn() {
            level(this.entity).playSound(null, this.entity, BDSounds.HANDGUN.get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
        }

        @Override
        public void onUpdateInAir() {
            level(this.entity).addParticle(ParticleTypes.SMOKE, this.entity.getX(), this.entity.getY(), this.entity.getZ(), 0.0, 0.0, 0.0);
        }

        @Override
        public float getSize() {
            return 0.1F;
        }
    }

    static final class Slingshot extends Bullet {
        Slingshot(BDProjectile e) {
            super(e);
        }

        @Override
        float damage() {
            return this.entity.getLvl();
        }

        @Override
        public int getTextureIndex() {
            return 248;
        }

        @Override
        public float getSize() {
            return 0.2F;
        }

        @Override
        public void onSpawn() {
            double speed = Math.min(1.0, this.entity.getLvl() / 3.0);
            this.entity.setDeltaMovement(this.entity.getDeltaMovement().scale(speed));
            this.entity.accelerationX *= speed;
            this.entity.accelerationY = -0.03;
            this.entity.accelerationZ *= speed;
        }
    }

    static final class WindBall extends ProjectileBehavior {
        private final Random rand = new Random();

        WindBall(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return -1;
        }

        @Override
        public void onImpact(HitResult hit) {
        }

        @Override
        public void onUpdateInAir() {
            BDProjectile ball = this.entity;
            if (ball.tickCount % 30 == 0) {
                level(ball).playSound(null, ball, BDSounds.WIND.get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            if (ball.tickCount >= 80 && !client(ball)) {
                ball.discard();
            }
            ball.accelerationX = 0.0;
            ball.accelerationY = 0.0;
            ball.accelerationZ = 0.0;
            ball.setDeltaMovement(ball.getDeltaMovement().scale(0.9));
            BlockParticleOption crack = crack(ball, (int) ball.getX(), (int) ball.getY() - 2, (int) ball.getZ());
            double dist = 5.0;
            for (Entity e : level(ball).getEntitiesOfClass(Entity.class, ball.getBoundingBox().inflate(dist - 1.0, 2.0, dist - 1.0))) {
                if (e == ball || e == ball.shooter()) {
                    continue;
                }
                double dx = ball.getX() - e.getX();
                double dy = ball.getY() - e.getY();
                double dz = ball.getZ() - e.getZ();
                // Vec3.normalize() returned a new vector the original threw away: the offsets stay as they are
                double distToEntity = Math.max(15.0, (dx * dx + dz * dz) * 10.0);
                double x = dx / distToEntity;
                double y = dy / Math.max(15.0, dy * dy * 10.0);
                double z = dz / distToEntity;
                if (e instanceof LivingEntity) {
                    if (e.isPushable()) {
                        addMotion(e, x, y, z);
                    }
                    if (ball.distanceToSqr(e) <= 3.0 && !client(ball)) {
                        BDFx.hurt(e, BDFx.magic(ball, ball.shooter()), 1);
                    }
                    e.resetFallDistance();
                } else if (!(e instanceof BDProjectile other) || other.getProjectileType() != ProjectileType.WINDBALL.id) {
                    addMotion(e, x, y, z);
                } else if (other.getOwner() != ball.getOwner()) {
                    if (level(ball) instanceof ServerLevel server) {
                        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(server, EntitySpawnReason.TRIGGERED);
                        if (bolt != null) {
                            bolt.snapTo(ball.getX(), ball.getY() - 1.0, ball.getZ());
                            server.addFreshEntity(bolt);
                        }
                    }
                    addMotion(e, x, y, z);
                } else if (ball.tickCount > e.tickCount) {
                    if (!client(ball)) {
                        ball.discard();
                    }
                } else {
                    ball.tickCount = ball.tickCount - e.tickCount / 2;
                }
            }
            if (client(ball)) {
                for (int i = 0; i < 4; i++) {
                    level(ball).addParticle(crack, ball.getX() + this.rand.nextFloat() - 0.5, ball.getY() + this.rand.nextFloat() - 0.5,
                            ball.getZ() + this.rand.nextFloat() - 0.5, this.rand.nextFloat() - 0.5F, this.rand.nextFloat() - 0.5F,
                            this.rand.nextFloat() - 0.5F);
                }
            }
        }

        @Override
        public float getSize() {
            return 0.001F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        @Override
        public void onSpawn() {
            this.entity.setImmuneToFire(false);
        }
    }

    static final class FireShield extends ProjectileBehavior {
        private final Random rand = new Random();
        private int lifeTime = 1000;
        private int deathTime;

        FireShield(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return 243;
        }

        @Override
        public void onImpact(HitResult hit) {
            if (!client(this.entity) && hitEntity(hit) instanceof LivingEntity e && e != this.entity.shooter() && !BDEntityIds.is(e, "SummonedUndead")) {
                BDFx.hurt(e, BDFx.indirect(this.entity, this.entity.shooter()), 2);
                e.igniteForSeconds(4);
                this.deathTime += 100;
                fireSound(this.entity, e.getX(), e.getY(), e.getZ());
            }
        }

        @Override
        public void onUpdateInAir() {
            this.lifeTime--;
            this.entity.igniteForSeconds(2);
            if (this.lifeTime <= this.deathTime && !client(this.entity)) {
                this.entity.discard();
            }
            LivingEntity shooter = this.entity.shooter();
            if (shooter != null) {
                double x = shooter.getX() + Math.cos(this.lifeTime / 10.0F) * 2.0;
                double z = shooter.getZ() + Math.sin(this.lifeTime / 10.0F) * 2.0;
                this.entity.setPos(x, shooter.getY() + 1.0, z);
            }
            if (client(this.entity)) {
                level(this.entity).addParticle(ParticleTypes.FLAME, this.entity.getX() + this.rand.nextFloat() - 0.5,
                        this.entity.getY() + this.rand.nextFloat() - 0.5, this.entity.getZ() + this.rand.nextFloat() - 0.5, 0.0, 0.0, 0.0);
            }
        }

        @Override
        public float getSize() {
            return 0.5F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        @Override
        public void onSpawn() {
            this.entity.accelerationX = 0.0;
            this.entity.accelerationY = 0.0;
            this.entity.accelerationZ = 0.0;
            this.entity.setDeltaMovement(Vec3.ZERO);
            fireSound(this.entity, this.entity.getX(), this.entity.getY(), this.entity.getZ());
        }
    }

    /** ProjectileEarthQuakeArea and ProjectileFireWall: a ring that grows by a tenth of a block per tick. */
    abstract static class Ring extends ProjectileBehavior {
        final Random rand = new Random();
        int lifeTime = 10;
        final int deathTime = 100;

        Ring(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return -1;
        }

        @Override
        public void onImpact(HitResult hit) {
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        /** The living entities on the ring this tick. */
        List<LivingEntity> onRing() {
            double dist = this.lifeTime / 10.0F;
            return level(this.entity).getEntitiesOfClass(LivingEntity.class, this.entity.getBoundingBox().inflate(dist, 0.0, dist),
                    e -> e != this.entity.shooter() && (int) e.distanceTo(this.entity) == this.lifeTime / 10);
        }

        void stand() {
            this.lifeTime++;
            if (this.lifeTime >= this.deathTime && !client(this.entity)) {
                this.entity.discard();
            }
            this.entity.accelerationX = 0.0;
            this.entity.accelerationY = 0.0;
            this.entity.accelerationZ = 0.0;
            this.entity.setDeltaMovement(Vec3.ZERO);
        }
    }

    static final class EarthQuakeArea extends Ring {
        EarthQuakeArea(BDProjectile e) {
            super(e);
        }

        @Override
        public void onUpdateInAir() {
            this.stand();
            BlockParticleOption crack = crack(this.entity, Mth.floor(this.entity.getX()), (int) this.entity.getY() - 1, Mth.floor(this.entity.getZ()));
            for (LivingEntity e : this.onRing()) {
                if (e.onGround() && !BDEntityIds.is(e, "SummonedUndead") && !client(this.entity)) {
                    BDFx.hurt(e, BDFx.magic(this.entity, this.entity.shooter()), 1);
                }
            }
            if (client(this.entity)) {
                double step = Math.PI / 8;
                for (int i = 0; i < 16; i++) {
                    double x = Math.sin(this.lifeTime / 10.0F + step * i) * (this.lifeTime / 10.0F);
                    double z = Math.cos(this.lifeTime / 10.0F + step * i) * (this.lifeTime / 10.0F);
                    for (int a = 0; a < 5; a++) {
                        level(this.entity).addParticle(crack, this.entity.getX() + x, this.entity.getY() + this.rand.nextFloat() - 0.5,
                                this.entity.getZ() + z, 0.0, 1.0, 0.0);
                    }
                }
            }
        }

        @Override
        public float getSize() {
            return 0.0F;
        }

        @Override
        public void onSpawn() {
            this.entity.setPos(this.entity.getX(), this.entity.getY() - 1.0, this.entity.getZ());
            this.entity.setImmuneToFire(true);
        }
    }

    static final class FireWall extends Ring {
        FireWall(BDProjectile e) {
            super(e);
        }

        @Override
        public void onUpdateInAir() {
            this.stand();
            for (LivingEntity e : this.onRing()) {
                if (!client(this.entity)) {
                    BDFx.hurt(e, BDFx.indirect(this.entity, this.entity.shooter()), 1);
                    e.igniteForSeconds(4);
                }
            }
            if (client(this.entity)) {
                double step = Math.PI / 4;
                for (int i = 0; i < 8; i++) {
                    double x = Math.sin(this.lifeTime / 10.0F + step * i) * (this.lifeTime / 10.0F);
                    double z = Math.cos(this.lifeTime / 10.0F + step * i) * (this.lifeTime / 10.0F);
                    for (int a = 0; a < 2; a++) {
                        level(this.entity).addParticle(ParticleTypes.FLAME, this.entity.getX() + x,
                                this.entity.getY() + this.rand.nextFloat() - 0.5, this.entity.getZ() + z, 0.0, 0.5, 0.0);
                    }
                }
            } else if (this.lifeTime % 10 == 0) {
                fireSound(this.entity, this.entity.getX(), this.entity.getY(), this.entity.getZ());
            }
        }

        @Override
        public float getSize() {
            return 1.5F;
        }

        @Override
        public void onSpawn() {
            fireSound(this.entity, this.entity.getX(), this.entity.getY(), this.entity.getZ());
            this.entity.setPos(this.entity.getX(), this.entity.getY() - 1.0, this.entity.getZ());
            this.entity.setImmuneToFire(true);
        }
    }

    static final class EarthQuake extends ProjectileBehavior {
        private final Random rand = new Random();
        private int lifeTime = 60;

        EarthQuake(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return -1;
        }

        @Override
        public void onImpact(HitResult hit) {
            if (!client(this.entity) && !(hitEntity(hit) instanceof LivingEntity)) {
                Vec3 m = this.entity.getDeltaMovement();
                setMotion(this.entity, m.x, 0.0, m.z);
            }
        }

        @Override
        public void onUpdateInAir() {
            this.lifeTime--;
            if (this.lifeTime <= 0 && !client(this.entity)) {
                this.entity.discard();
            }
            BlockParticleOption crack = crack(this.entity, (int) this.entity.getX(), (int) this.entity.getY() - 2, (int) this.entity.getZ());
            for (LivingEntity e : level(this.entity).getEntitiesOfClass(LivingEntity.class, this.entity.getBoundingBox().inflate(1.0, 2.0, 1.0))) {
                if (e != this.entity.shooter() && !client(this.entity) && e.onGround()) {
                    Vec3 m = e.getDeltaMovement();
                    setMotion(e, m.x, 0.3, m.z);
                    e.syncVelocity = true;
                    BDFx.hurt(e, BDFx.magic(this.entity, this.entity.shooter()), 1);
                }
            }
            if (client(this.entity)) {
                for (int i = 0; i < 8; i++) {
                    level(this.entity).addParticle(crack, this.entity.getX() + this.rand.nextFloat() - 0.5,
                            this.entity.getY() + this.rand.nextFloat() - 0.5, this.entity.getZ() + this.rand.nextFloat() - 0.5,
                            this.rand.nextFloat() - 0.5F, this.rand.nextFloat(), this.rand.nextFloat() - 0.5F);
                }
            }
        }

        @Override
        public float getSize() {
            return 1.5F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        @Override
        public void onSpawn() {
            Vec3 m = this.entity.getDeltaMovement();
            this.entity.setDeltaMovement(m.x / 32.0, -1.0, m.z / 32.0);
            this.entity.accelerationX = m.x / 32.0 / 2.0;
            this.entity.accelerationY = -0.5;
            this.entity.accelerationZ = m.z / 32.0 / 2.0;
            this.entity.setImmuneToFire(true);
        }
    }

    static final class HealBall extends ProjectileBehavior {
        HealBall(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return 245;
        }

        @Override
        public void onImpact(HitResult hit) {
            LivingEntity shooter = this.entity.shooter();
            if (!client(this.entity) && shooter != null && hitEntity(hit) == shooter && !this.entity.isRemoved()) {
                this.entity.discard();
                shooter.heal(this.entity.getLvl());
            }
        }

        @Override
        public void onUpdateInAir() {
            LivingEntity shooting = this.entity.shooter();
            if (shooting != null) {
                Vec3 fc = new Vec3(shooting.getX() - this.entity.getX(), shooting.getY() + 0.4 - this.entity.getY(), shooting.getZ() - this.entity.getZ());
                if (fc.length() < 1.0 && !client(this.entity) && !this.entity.isRemoved()) {
                    this.entity.discard();
                    shooting.heal(this.entity.getLvl());
                }
                fc = fc.normalize();
                double s = 0.2;
                setMotion(this.entity, fc.x * s, fc.y * s, fc.z * s);
                if (!shooting.isAlive() && !client(this.entity)) {
                    this.entity.discard();
                }
            } else if (!client(this.entity)) {
                this.entity.discard();
            }
        }

        @Override
        public float getSize() {
            return 0.4F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }
    }

    static final class ThunderBolt extends ProjectileBehavior {
        private int lifeTime = 10;

        ThunderBolt(BDProjectile e) {
            super(e);
        }

        @Override
        public int getTextureIndex() {
            return -1;
        }

        @Override
        public void onImpact(HitResult hit) {
            if (!client(this.entity)) {
                Entity target = hitEntity(hit);
                if (target != null) {
                    if (target instanceof LivingEntity) {
                        BDFx.hurt(target, target.damageSources().generic(), 2);
                    }
                } else {
                    this.entity.discard();
                }
            }
        }

        @Override
        public void onUpdateInAir() {
            this.lifeTime--;
            if (this.lifeTime <= 0 && !client(this.entity)) {
                this.entity.discard();
            }
        }

        @Override
        public float getSize() {
            return 0.5F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        @Override
        public int getRopeColor() {
            return 0x5555FF;
        }

        @Override
        public int getMaxLifeTime() {
            return 20;
        }
    }

    /** ProjectileSpiderWebVertical (the spider boss): shoots up, sticks under a block, weaves a web and pulls the shooter up. */
    static final class SpiderWebVertical extends ProjectileBehavior {
        /** 1.5.2 marked the anchored web by setting the ball's type to 1; the behaviour stays. */
        private static final int ANCHORED = 1;

        SpiderWebVertical(BDProjectile e) {
            super(e);
        }

        @Override
        public void onSpawn() {
            this.entity.setDeltaMovement(0.0, 1.0, 0.0);
            this.entity.accelerationX = 0.0;
            this.entity.accelerationZ = 0.0;
        }

        @Override
        public int getTextureIndex() {
            return 241;
        }

        @Override
        public void onImpact(HitResult hit) {
            if (hit instanceof BlockHitResult block && level(this.entity).getBlockState(block.getBlockPos()).isSolid()) {
                this.entity.setProjectileType(ANCHORED);
                this.entity.setOnGround(true);
                Vec3 m = this.entity.getDeltaMovement();
                this.entity.setDeltaMovement(m.x, 0.0, m.z);
                this.entity.accelerationY = 0.0;
                BlockPos pos = block.getBlockPos();
                this.entity.setPos(pos.getX(), pos.getY(), pos.getZ());
                BlockPos web = beside(block);
                if (level(this.entity).isEmptyBlock(web) && !client(this.entity)) {
                    level(this.entity).setBlock(web, Blocks.COBWEB.defaultBlockState(), 3);
                }
            }
        }

        @Override
        public int getMaxLifeTime() {
            return this.entity.getVehicle() == null ? 100 : 1000;
        }

        @Override
        public void onUpdateInAir() {
            LivingEntity shooting = this.entity.shooter();
            if (this.entity.getProjectileType() == ANCHORED && shooting != null) {
                double s = -0.2;
                setMotion(shooting, (shooting.getX() - this.entity.getX()) * s, (shooting.getY() - this.entity.getY()) * s,
                        (shooting.getZ() - this.entity.getZ()) * s);
            }
        }

        @Override
        public float getSize() {
            return 0.5F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        @Override
        public int getRopeColor() {
            return 0xFFFFFF;
        }
    }

    /**
     * ProjectileHookShoot (levels 0 hookshot, 1 longshot, 2 manual hook, 3 spider hook): flies out, holds on to a block
     * or an entity and pulls the shooter (or a smaller entity towards the shooter); sneaking shortens, jumping lengthens
     * the rope of the manual and spider hooks. jumpMovementFactor (more air control while swinging) has no counterpart.
     */
    static final class HookShoot extends ProjectileBehavior {
        private int lifeTime;
        double radio;
        boolean returning;

        HookShoot(BDProjectile e) {
            super(e);
        }

        boolean isReeling() {
            return this.entity.getProjectileType() == ProjectileType.HOOKSHOT_REELING.id;
        }

        @Override
        public int getTextureIndex() {
            return this.entity.getLvl() == 3 ? 241 : 247;
        }

        @Override
        public void onImpact(HitResult hit) {
            if (hit instanceof BlockHitResult && !client(this.entity)) {
                this.entity.setProjectileType(ProjectileType.HOOKSHOT_REELING.id);
                this.entity.setOnGround(true);
            }
            Entity target = hitEntity(hit);
            if (target != null && target != this.entity.shooter() && !client(this.entity)) {
                this.entity.startRiding(target, true, true);
                this.returning = true;
                this.entity.setProjectileType(ProjectileType.HOOKSHOT_REELING.id);
            }
        }

        @Override
        public void onUpdateInAir() {
            BDProjectile ball = this.entity;
            if (this.isReeling()) {
                ball.accelerationX = 0.0;
                ball.accelerationY = 0.0;
                ball.accelerationZ = 0.0;
                ball.setDeltaMovement(Vec3.ZERO);
            }
            this.lifeTime++;
            LivingEntity shooting = ball.shooter();
            if (shooting == null) {
                if (!client(ball)) {
                    ball.discard();
                }
                return;
            }
            double dist = ball.distanceTo(shooting);
            boolean jumping = BDFx.isJumping(shooting);
            if (shooting.isShiftKeyDown()) {
                this.radio -= 0.2;
                if (ball.getLvl() == 3) {
                    this.radio = this.radio - Math.min(1.5, this.radio / 5.0);
                }
                if (shooting.onGround()) {
                    Vec3 m = shooting.getDeltaMovement();
                    setMotion(shooting, m.x, 0.01, m.z);
                }
            }
            if (jumping && this.radio < this.getMaxRadius()) {
                this.radio += 0.2;
            }
            if (!this.isReeling()) {
                if (dist > this.getMaxRadius()) {
                    this.returning = true;
                }
                if (this.returning) {
                    this.radio = dist;
                    // Vec3.normalize() was thrown away in 1.5.2: the hook comes back faster from farther away
                    double s = 0.2;
                    setMotion(ball, (shooting.getX() - ball.getX()) * s, (shooting.getY() + 1.4 - ball.getY()) * s, (shooting.getZ() - ball.getZ()) * s);
                    if (dist <= 1.5 && !client(ball)) {
                        ball.discard();
                    }
                } else {
                    this.radio = Math.min(dist, this.getMaxRadius());
                }
            } else {
                if (!client(ball) && level(ball).isEmptyBlock(BlockPos.containing(ball.getX(), ball.getY(), ball.getZ())) && ball.getVehicle() == null) {
                    ball.setProjectileType(ProjectileType.HOOKSHOT.id);
                }
                shooting.resetFallDistance();
                boolean pullToShooter = false;
                Entity ride = ball.getVehicle();
                if (ride != null) {
                    if (ride instanceof Player && ride.isShiftKeyDown()) {
                        this.setDead();
                    }
                    pullToShooter = size(shooting) + 0.2F > size(ride);
                }
                double fx = shooting.getX() - ball.getX();
                double fy = shooting.getY() - ball.getY();
                double fz = shooting.getZ() - ball.getZ();
                if (ball.getLvl() != 2 && ball.getLvl() != 3) {
                    double s = 0.2;
                    if (pullToShooter) {
                        setMotion(ride, fx * s, fy * s, fz * s);
                        setMotion(ball, fx * s, fy * s, fz * s);
                    } else {
                        setMotion(shooting, fx * -s, fy * -s, fz * -s);
                    }
                } else {
                    double totalDist = Mth.clamp(dist - this.radio, 0.0, 2.0) * 0.02;
                    fx *= totalDist;
                    fy *= totalDist;
                    fz *= totalDist;
                    if (pullToShooter) {
                        if (dist > this.radio || this.radio < 1.0) {
                            setMotion(ride, fx, fy, fz);
                            setMotion(ball, fx, fy, fz);
                        }
                    } else {
                        addMotion(shooting, -fx, -fy, -fz);
                    }
                }
            }
            if (this.lifeTime > 10 && jumping && shooting.horizontalCollision && (dist > this.radio || dist < 1.0)) {
                Vec3 m = shooting.getDeltaMovement();
                setMotion(shooting, m.x, 0.4, m.z);
            }
        }

        /** Frees the hook shot item that fired this hook (it remembers the hook's entity id). */
        @Override
        public void setDead() {
            if (this.entity.shooter() instanceof Player player) {
                betterdungeons.item.HookShotItem.release(player, this.entity.getId());
            }
        }

        int getMaxRadius() {
            return switch (this.entity.getLvl()) {
                case 3 -> 40;
                case 2 -> 30;
                case 1 -> 20;
                case 0 -> 10;
                default -> 40;
            };
        }

        @Override
        public float getSize() {
            return 0.3F;
        }

        @Override
        public boolean canBounce() {
            return false;
        }

        @Override
        public void onSpawn() {
            if (this.entity.getLvl() != 3) {
                this.entity.accelerationX *= 0.1;
                this.entity.accelerationY *= 0.1;
                this.entity.accelerationZ *= 0.1;
            }
            BDFx.sound(level(this.entity), this.entity.getX(), this.entity.getY(), this.entity.getZ(), SoundEvents.ARROW_SHOOT, 4.0F,
                    BDFx.pitch(level(this.entity)));
        }

        @Override
        public double getRadius() {
            return this.radio;
        }

        @Override
        public int getMaxLifeTime() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getRopeColor() {
            return this.entity.getLvl() == 3 ? 0xFFFFFF : 0x444444;
        }
    }
}
