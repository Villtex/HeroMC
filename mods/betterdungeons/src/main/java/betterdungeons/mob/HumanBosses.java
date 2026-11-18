package betterdungeons.mob;

import betterdungeons.BDItems;
import betterdungeons.BDSounds;
import betterdungeons.api.BDEntityIds;
import betterdungeons.api.BDSummonedMob;
import betterdungeons.mob.ai.HumanGoals;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The human bosses of mobs.boss (EntityPirateBoss, EntityWalkerBoss, EntitySpecterBoss, EntityGremlinBoss,
 * EntityMinotaurBoss, EntityGoblinBoss). They did not implement IBossDisplayData: no boss bar, as in 1.5.2.
 */
public final class HumanBosses {
    private HumanBosses() {
    }

    /** What the bosses share: a fixed constructor (no level or class from the spawner), no class names, no leader search. */
    public abstract static class Boss extends HumanEntity implements Enemy {
        protected Boss(EntityType<? extends Boss> type, Level level) {
            super(type, level);
        }

        @Override
        public void initDungeonMob(int level, int type) {
            this.initAsWorld();
            this.setHealth(this.getMaxHealth());
        }

        @Override
        public void setRandomType() {
        }

        @Override
        public List<String> getClassNameKeys() {
            return List.of();
        }

        @Override
        public void getLeader() {
        }

        @Override
        public boolean shouldRenderCape() {
            return true;
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.VILLAGER_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.VILLAGER_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.VILLAGER_DEATH;
        }

        /** castTeleport of the pirate and walker bosses: near the entity, large smoke where it stood. */
        protected boolean castTeleport(Entity entity, double spread) {
            double x = entity.getX() + (this.random.nextDouble() - 0.5) * spread;
            double z = entity.getZ() + (this.random.nextDouble() - 0.5) * spread;
            return BDMobUtil.teleport(this, x, entity.getY(), z, false, true);
        }
    }

    /** EntityPirateBoss ("Pirate Captain"): turns invisible below 450 and leaves a bait behind; blocks with its shield. */
    public static class PirateBoss extends Boss {
        int invisibleCD = 10;

        public PirateBoss(EntityType<? extends PirateBoss> type, Level level) {
            super(type, level);
        }

        @Override
        public void initAsWorld() {
            super.initAsWorld();
            this.despawn = false;
        }

        @Override
        protected void configRaze() {
            this.leaderShip = 10;
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 5;
            this.attackStrength = 7;
            this.attackSpeed = 30;
            this.experienceValue = 30;
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "pirate";
            this.rivalHumans = new String[]{"goblin", "walker", "beast", "zombie", "friend"};
            this.setShied(true);
        }

        @Override
        protected void addAITasks() {
            this.goalSelector.addGoal(4, new HumanGoals.Firefighter(this, this.baseMoveSpeed));
            super.addAITasks();
        }

        @Override
        protected int maxHealthValue() {
            return 500;
        }

        @Override
        public void tick() {
            if (!this.level().isClientSide()) {
                LivingEntity target = this.getTarget();
                if (target != null) {
                    if (this.getHealth() < 450 && !this.isInvisible() && this.invisibleCD < 0) {
                        this.setInvisible(true);
                        EntityType<?> bait = BDEntityIds.type("SummonedBait");
                        if (bait != null) {
                            Entity e = bait.create(this.level(), EntitySpawnReason.MOB_SUMMONED);
                            if (e instanceof BDSummonedMob summoned) {
                                summoned.initSummoned(this, 0, 0);
                            }
                            if (e != null) {
                                e.setPos(this.getX(), this.getY(), this.getZ());
                                this.level().addFreshEntity(e);
                            }
                        }
                        this.castTeleport(target, 8.0);
                    }
                    this.invisibleCD--;
                }
                if (this.isInvisible()) {
                    if (this.isDefender() || this.isDefending()) {
                        if (this.isDefending()) {
                            this.toogleBlocking();
                        }
                        this.setShied(false);
                    }
                    this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    if (this.getTarget() == null) {
                        this.setInvisible(false);
                    }
                }
            }
            super.tick();
        }

        @Override
        public @Nullable ItemStack getArmor(int slot) {
            return null;
        }

        @Override
        public boolean doHurtTarget(ServerLevel level, Entity entity) {
            if (this.random.nextInt(15) == 1) {
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 3));
                }
            } else if (this.random.nextInt(15) == 1 && entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 5));
            }
            return super.doHurtTarget(level, entity);
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            if (this.isInvisible()) {
                this.setInvisible(false);
                this.invisibleCD = 60 - level.getDifficulty().getId() * 10;
            }
            if (!this.isDefending() || !source.is(DamageTypeTags.IS_PROJECTILE) && this.random.nextInt(5) != 0) {
                return super.hurtServer(level, source, amount);
            }
            return false;
        }

        /** dropFewItems: the weapon it carried (the loot table has the rest). */
        @Override
        protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
            super.dropCustomDeathLoot(level, source, killedByPlayer);
            ItemStack weapon = this.getMainHandItem();
            if (!weapon.isEmpty()) {
                this.spawnAtLocation(level, new ItemStack(weapon.getItem()));
            }
        }

        @Override
        public @Nullable ItemStack getEquipedWeapon() {
            return this.random.nextBoolean() ? new ItemStack(BDItems.NINJA_DAGGER.get()) : new ItemStack(BDItems.TRICKSTER_DAGGER.get());
        }

        @Override
        public boolean removeWhenFarAway(double distSqr) {
            return false;
        }
    }

    /** EntityWalkerBoss ("Walker King"): sheds its armour as it loses health, teleports to archers, heals from lightning. */
    public static class WalkerBoss extends Boss {
        public WalkerBoss(EntityType<? extends WalkerBoss> type, Level level) {
            super(type, level);
        }

        /** EntityWalkerBoss(world) was EntityHuman(world, 1). */
        @Override
        public void initAsWorld() {
            this.initAsLevel(1);
            this.despawn = false;
        }

        @Override
        protected void configRaze() {
            this.blockRate = 8;
            this.shiedBlockDefense = 4;
            this.shiedID = 6;
            this.leaderShip = 10;
            this.renderEars = false;
            this.sneak = false;
            this.attackStrength = 5;
            this.attackSpeed = 30;
            this.experienceValue = 40;
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "walker";
            this.rivalHumans = new String[]{"pigman", "goblin", "pirate", "enderman", "zombie", "friend"};
            this.setShied(true);
        }

        @Override
        protected int maxHealthValue() {
            return 250;
        }

        @Override
        public boolean isPushable() {
            return false;
        }

        @Override
        public @Nullable ItemStack getArmor(int slot) {
            return switch (slot) {
                case 1 -> new ItemStack(Items.DIAMOND_BOOTS);
                case 2 -> new ItemStack(Items.DIAMOND_LEGGINGS);
                case 3 -> new ItemStack(Items.DIAMOND_CHESTPLATE);
                case 4 -> new ItemStack(Items.WITHER_SKELETON_SKULL);
                default -> null;
            };
        }

        @Override
        public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
            super.knockback(power, xd, zd, source, damage, comesFromEffect);
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, 0.0, m.z);
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_EXPLOSION)) {
                return false;
            }
            if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getEntity() instanceof LivingEntity attacker) {
                this.setTarget(attacker);
                this.castTeleport(attacker);
            }
            return super.hurtServer(level, source, amount);
        }

        @Override
        public void aiStep() {
            if (!this.level().isClientSide()) {
                float health = this.getHealth();
                int max = this.maxHealthValue();
                if (health < max / 5) {
                    this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                } else if (health < max / 5 * 2) {
                    this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                } else if (health < max / 5 * 3) {
                    this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
                } else if (health < max / 5 * 4) {
                    this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }
                LivingEntity target = this.getTarget();
                if (target != null) {
                    float dist = this.distanceTo(target);
                    ItemStack held = target.getMainHandItem();
                    boolean bow = !held.isEmpty() && dist > 5.0F && held.getItem() instanceof BowItem;
                    if (dist > 15.0F && dist < 40.0F || bow) {
                        this.castTeleport(target);
                    }
                    if (this.random.nextInt(300) == 0) {
                        BDProjectile.spawn(this, ProjectileType.WINDBALL);
                    }
                }
                if (this.getTarget() == null && this.isDefending()) {
                    this.setDefending(false);
                }
            }
            super.aiStep();
        }

        boolean castTeleport(Entity entity) {
            return !this.isPassenger() && this.castTeleport(entity, 4.0);
        }

        @Override
        public @Nullable ItemStack getEquipedWeapon() {
            return new ItemStack(BDItems.WALKER_SWORD.get());
        }

        @Override
        public void thunderHit(ServerLevel level, LightningBolt bolt) {
            this.heal(120.0F);
            for (int i = 0; i < 3; i++) {
                BDMobUtil.particle(this, ParticleTypes.HEART, this.getX() + this.random.nextFloat() - 0.5, this.getY() + this.random.nextFloat() - 0.5,
                        this.getZ() + this.random.nextFloat() - 0.5, 0.0, 1.0, 0.0);
            }
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.WITHER_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.WITHER_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.WITHER_DEATH;
        }
    }

    /** EntitySpecterBoss ("Specter Lord"): only hurt while it swings or has no target (or by fire, explosions, magic). */
    public static class SpecterBoss extends Boss {
        public SpecterBoss(EntityType<? extends SpecterBoss> type, Level level) {
            super(type, level);
        }

        @Override
        public void initAsWorld() {
            super.initAsWorld();
            this.despawn = false;
        }

        @Override
        protected void configRaze() {
            this.leaderShip = 10;
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 8;
            this.attackStrength = 4;
            this.attackSpeed = 30;
            this.experienceValue = 30;
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "zombie";
            this.rivalHumans = new String[]{"dwarf", "pirate", "goblin", "beast", "friend"};
        }

        @Override
        public void tick() {
            if (!this.level().isClientSide()) {
                this.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 0));
            }
            super.tick();
        }

        @Override
        public @Nullable ItemStack getArmor(int slot) {
            return null;
        }

        @Override
        protected int maxHealthValue() {
            return 200;
        }

        boolean tangible() {
            return this.isSwinging() || this.getTarget() == null;
        }

        @Override
        public boolean isPickable() {
            return this.tangible() && super.isPickable();
        }

        @Override
        public boolean isPushable() {
            return this.tangible();
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            if (this.tangible() || source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
                return super.hurtServer(level, source, amount);
            }
            return false;
        }

        @Override
        public @Nullable ItemStack getEquipedWeapon() {
            return new ItemStack(BDItems.DEFENSE_SWORD.get());
        }

        @Override
        public boolean removeWhenFarAway(double distSqr) {
            return false;
        }
    }

    /** EntityGremlinBoss ("Gremlin Shaman"): a hooker whose aura pushes everything (and projectiles) away from it. */
    public static class GremlinBoss extends Boss {
        public GremlinBoss(EntityType<? extends GremlinBoss> type, Level level) {
            super(type, level);
        }

        @Override
        public void initAsWorld() {
            super.initAsWorld();
            this.despawn = false;
            this.setHooker(true);
        }

        @Override
        protected void configRaze() {
            this.leaderShip = 10;
            this.shiedID = 12;
            this.attackStrength = 2;
            this.attackSpeed = 30;
            this.experienceValue = 15;
            this.baseMoveSpeed = 0.28F;
            this.canMount = true;
            this.humanType = "goblin";
            this.rivalHumans = new String[]{"pirate", "beast", "golem", "dwarf", "zombie", "friend"};
        }

        @Override
        public void tick() {
            double dist = 15.0;
            for (Entity e : this.level().getEntities(this, this.getBoundingBox().inflate(dist - 1.0, 2.0, dist - 1.0))) {
                if (e == this.getTarget()) {
                    continue;
                }
                // Vec3.normalize of 1.5.2 returned a new vector, the code dropped it: d stays the raw offset
                Vec3 d = new Vec3(this.getX() - e.getX(), this.getY() - e.getY(), this.getZ() - e.getZ());
                if (this.level().isClientSide()) {
                    this.level().addParticle(ParticleTypes.ENCHANT, e.getX() + this.random.nextFloat() - 0.5, e.getY() + this.random.nextFloat() - 0.5,
                            e.getZ() + this.random.nextFloat() - 0.5, d.x, d.y, d.z);
                    continue;
                }
                double distToEntity = Math.max(20.0, (d.x * d.x + d.z * d.z) * 10.0);
                double x = d.x / distToEntity;
                double y = d.y / Math.max(15.0, d.y * d.y * 10.0) / distToEntity;
                double z = d.z / distToEntity;
                double speedModifier = 3.0;
                if (e instanceof LivingEntity living) {
                    if (living.isPushable() && !living.isVehicle() && !this.isSuitableTargetAlly(living)) {
                        e.push(-x * speedModifier, -y, -z * speedModifier);
                        if (e instanceof ServerPlayer) {
                            e.needsSync = true;
                        }
                    }
                } else if (this.distanceTo(e) < 10.0F) {
                    double dist3D = this.distanceToSqr(e);
                    if (e instanceof AbstractHurtingProjectile) {
                        e.push(-d.x / dist3D, -d.y / dist3D, -d.z / dist3D);
                    } else if (e instanceof Projectile p) {
                        if (p.getOwner() != this) {
                            e.push(-d.x / dist3D, -d.y / dist3D, -d.z / dist3D);
                        }
                    } else {
                        e.push(-d.x / 20.0, -d.y / 20.0, -d.z / 20.0);
                    }
                }
            }
            super.tick();
        }

        @Override
        public @Nullable ItemStack getArmor(int slot) {
            return slot == 1 ? new ItemStack(BDItems.CLOUD_BOOTS.get()) : null;
        }

        @Override
        public boolean doHurtTarget(ServerLevel level, Entity entity) {
            this.heal(1.0F);
            return super.doHurtTarget(level, entity);
        }

        @Override
        protected int maxHealthValue() {
            return 200;
        }

        @Override
        public @Nullable ItemStack getEquipedWeapon() {
            return new ItemStack(BDItems.STAFF_WIND.get());
        }

        @Override
        public boolean removeWhenFarAway(double distSqr) {
            return false;
        }
    }

    /** EntityMinotaurBoss ("Baddass Minotaur"): a charging berserker; its blows shake the ground. */
    public static class MinotaurBoss extends Boss {
        public MinotaurBoss(EntityType<? extends MinotaurBoss> type, Level level) {
            super(type, level);
        }

        @Override
        public void initAsWorld() {
            super.initAsWorld();
            this.despawn = false;
            this.setBerseker(true);
            this.setItemSlot(EquipmentSlot.MAINHAND, HumanEntity.orEmpty(this.getEquipedWeapon()));
        }

        @Override
        protected void configRaze() {
            this.leaderShip = 10;
            this.shiedID = 11;
            this.attackStrength = 6;
            this.attackSpeed = 30;
            this.experienceValue = 15;
            this.baseMoveSpeed = 0.28F;
            this.canMount = true;
            this.humanType = "beast";
            this.rivalHumans = new String[]{"zombie", "walker", "dwarf", "friend"};
        }

        @Override
        protected void addAITasks() {
            super.addAITasks();
            this.setAttackAI(3, new HumanGoals.ChargeAttack(this, LivingEntity.class, this.baseMoveSpeed, false));
        }

        @Override
        public void tick() {
            Vec3 m = this.getDeltaMovement();
            if (!this.level().isClientSide() && this.isSprinting() && (this.getTarget() == null || m.x == 0.0 && m.z == 0.0)) {
                this.setSprinting(false);
            }
            super.tick();
        }

        @Override
        public @Nullable ItemStack getArmor(int slot) {
            return null;
        }

        @Override
        public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
            if (!this.isSprinting()) {
                super.knockback(power, xd, zd, source, damage, comesFromEffect);
            }
        }

        @Override
        public boolean doHurtTarget(ServerLevel level, Entity entity) {
            if (!this.isSprinting()) {
                BDProjectile.spawn(this, ProjectileType.EARTHQUAKE, 1);
            }
            return super.doHurtTarget(level, entity);
        }

        @Override
        protected int maxHealthValue() {
            return 500;
        }

        @Override
        public @Nullable ItemStack getEquipedWeapon() {
            return new ItemStack(BDItems.EARTH_SWORD.get());
        }

        @Override
        public boolean removeWhenFarAway(double distSqr) {
            return false;
        }
    }

    /** EntityGoblinBoss (goblinBoss): rides an orc that regenerates; hits on the boss go to its mount. */
    public static class GoblinBoss extends Boss {
        public GoblinBoss(EntityType<? extends GoblinBoss> type, Level level) {
            super(type, level);
        }

        /** EntityGoblinBoss(world) was EntityHuman(world, 1). */
        @Override
        public void initAsWorld() {
            this.initAsLevel(1);
            this.despawn = false;
        }

        @Override
        protected void configRaze() {
            this.blockRate = 8;
            this.shiedBlockDefense = 4;
            this.shiedID = 7;
            this.leaderShip = 10;
            this.renderEars = false;
            this.sneak = false;
            this.attackStrength = 2;
            this.attackSpeed = 30;
            this.experienceValue = 40;
            this.baseMoveSpeed = 0.3F;
            this.canMount = true;
            this.humanType = "goblin";
            this.rivalHumans = new String[]{"zombie", "walker", "dwarf", "friend"};
            this.setShied(true);
        }

        @Override
        protected int maxHealthValue() {
            return 50;
        }

        @Override
        public boolean isPushable() {
            return false;
        }

        @Override
        public @Nullable ItemStack getArmor(int slot) {
            return switch (slot) {
                case 1 -> new ItemStack(Items.DIAMOND_BOOTS);
                case 2 -> new ItemStack(Items.DIAMOND_LEGGINGS);
                case 3 -> new ItemStack(Items.DIAMOND_CHESTPLATE);
                case 4 -> new ItemStack(Items.DIAMOND_HELMET);
                default -> null;
            };
        }

        @Override
        public float getSizeModifier() {
            return 0.7F * super.getSizeModifier();
        }

        @Override
        public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
            super.knockback(power, xd, zd, source, damage, comesFromEffect);
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, 0.0, m.z);
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            if (source.is(DamageTypeTags.IS_FALL)) {
                return false;
            }
            return this.getVehicle() != null ? this.getVehicle().hurtServer(level, source, amount) : super.hurtServer(level, source, amount);
        }

        @Override
        public void aiStep() {
            if (!this.level().isClientSide() && this.getVehicle() instanceof LivingEntity mount) {
                mount.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10, 1));
            }
            super.aiStep();
        }

        @Override
        public @Nullable ItemStack getEquipedWeapon() {
            return new ItemStack(BDItems.MOON_SWORD.get());
        }

        @Override
        protected @Nullable SoundEvent getAmbientSound() {
            return this.random.nextInt(3) == 0 ? BDSounds.SMALL_MONSTER_LAUGHT.get() : null;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return this.random.nextInt(5) == 0 ? BDSounds.SMALL_MONSTER_LAUGHT.get() : BDSounds.SMALL_MONSTER_HURT.get();
        }

        @Override
        protected SoundEvent getDeathSound() {
            return BDSounds.SMALL_MONSTER_DEAD.get();
        }

        @Override
        public boolean isSuitableMount(@Nullable Entity entity) {
            return entity instanceof HumanMobs.Orc;
        }

        @Override
        public Class<? extends LivingEntity> getMountClass() {
            return HumanMobs.Orc.class;
        }

        @Override
        public void setMountAI() {
        }
    }
}
