package betterdungeons.mob;

import betterdungeons.BDSounds;
import betterdungeons.mob.ai.HumanGoals;
import betterdungeons.mob.ai.HumanTargetGoals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** The dungeon humans of mobs.* (subclasses of EntityHuman without boss behaviour). */
public final class HumanMobs {
    private HumanMobs() {
    }

    /** EntitySkeletonMele ("ArmoredSkeleton"): rides spiders; in the Nether a wither skeleton (bigger, strength 6). */
    public static class ArmoredSkeleton extends HumanEntity implements Enemy {
        static final EntityDataAccessor<Byte> DATA_TYPE = SynchedEntityData.defineId(ArmoredSkeleton.class, EntityDataSerializers.BYTE);

        public ArmoredSkeleton(EntityType<? extends ArmoredSkeleton> type, Level level) {
            super(type, level);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
            super.defineSynchedData(builder);
            builder.define(DATA_TYPE, (byte) 0);
        }

        @Override
        protected void configRaze() {
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 4;
            this.attackStrength = 2;
            this.attackSpeed = 25;
            this.arrowAccuracy = 12.0F;
            this.range = 15;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "zombie";
            this.rivalHumans = new String[]{"walker", "dwarf", "pirate", "beast", "goblin", "friend"};
            if (this.getSkeletonType() == 1) {
                this.attackStrength = 6;
            }
        }

        @Override
        protected int maxHealthValue() {
            return 15;
        }

        @Override
        public void initCreature() {
            super.initCreature();
            if (this.level().dimension() == Level.NETHER) {
                this.setSkeletonType(1);
            }
        }

        public int getSkeletonType() {
            return this.entityData.get(DATA_TYPE);
        }

        public void setSkeletonType(int type) {
            this.entityData.set(DATA_TYPE, (byte) type);
            this.attackStrength = type == 1 ? 6 : 2;
            this.refreshDimensions();
        }

        @Override
        public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
            super.onSyncedDataUpdated(accessor);
            if (DATA_TYPE.equals(accessor)) {
                this.refreshDimensions();
            }
        }

        @Override
        public EntityDimensions getDefaultDimensions(Pose pose) {
            return this.getSkeletonType() == 1 ? EntityDimensions.scalable(0.72F, 2.34F) : super.getDefaultDimensions(pose);
        }

        @Override
        public boolean fireImmune() {
            return this.getSkeletonType() == 1 || super.fireImmune();
        }

        @Override
        public float getSizeModifier() {
            return this.getSkeletonType() == 1 ? 1.2F : 1.0F;
        }

        @Override
        protected void addAdditionalSaveData(ValueOutput output) {
            super.addAdditionalSaveData(output);
            output.putByte("SkeletonType", (byte) this.getSkeletonType());
        }

        @Override
        protected void readAdditionalSaveData(ValueInput input) {
            super.readAdditionalSaveData(input);
            if (input.getInt("SkeletonType").isPresent()) {
                this.setSkeletonType(input.getIntOr("SkeletonType", 0));
            }
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.SKELETON_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.SKELETON_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.SKELETON_DEATH;
        }

        @Override
        public boolean isSuitableMount(@Nullable Entity entity) {
            return !this.isHealer() && entity != null && entity.getType() == EntityTypes.SPIDER;
        }

        @Override
        public Class<? extends LivingEntity> getMountClass() {
            return Spider.class;
        }

        @Override
        public void setMountAI() {
            this.mountAttacksTarget();
        }
    }

    /** EntityZombieArmored. */
    public static class ArmoredZombie extends HumanEntity implements Enemy {
        public ArmoredZombie(EntityType<? extends ArmoredZombie> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 3;
            this.attackStrength = 3;
            this.attackSpeed = 25;
            this.arrowAccuracy = 15.0F;
            this.range = 20;
            this.experienceValue = 6 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "zombie";
            this.rivalHumans = new String[]{"goblin", "dwarf", "pirate", "triton", "pigman", "friend"};
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.ZOMBIE_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.ZOMBIE_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.ZOMBIE_DEATH;
        }

        /** dropFewItems spawned an XP orb of lvl + 4 (the rest is the loot table). */
        @Override
        protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
            super.dropCustomDeathLoot(level, source, killedByPlayer);
            net.minecraft.world.entity.ExperienceOrb.award(level, this.position(), this.getlvl() + 4);
        }
    }

    /** EntityDwarf: fire immune, hunts monsters too; spawners only below y 60 on netherrack or bricks. */
    public static class Dwarf extends HumanEntity implements Enemy {
        public Dwarf(EntityType<? extends Dwarf> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 2;
            this.attackStrength = 3;
            this.attackSpeed = 25;
            this.arrowAccuracy = 3.0F;
            this.range = 15;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.humanType = "dwarf";
            this.rivalHumans = new String[]{"walker", "goblin", "zombie", "ogre", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 25;
        }

        @Override
        protected void addAITasks() {
            super.addAITasks();
            this.targetSelector.addGoal(3, new HumanTargetGoals.NearestAttackable(this, Monster.class, 30.0F, true));
        }

        @Override
        public float getSizeModifier() {
            return 0.7F * super.getSizeModifier();
        }

        @Override
        public @Nullable ItemStack getEquipedWeapon() {
            return switch (this.getlvl()) {
                case 1 -> new ItemStack(Items.STONE_SWORD);
                case 2 -> new ItemStack(Items.IRON_SWORD);
                case 3 -> new ItemStack(Items.GOLDEN_SWORD);
                case 4 -> new ItemStack(Items.DIAMOND_AXE);
                case 5 -> null;
                default -> new ItemStack(Items.IRON_PICKAXE);
            };
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.VILLAGER_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.PLAYER_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.VILLAGER_DEATH;
        }

        @Override
        protected boolean canSpawnHere(LevelAccessor level) {
            BlockState below = level.getBlockState(BlockPos.containing(this.getX(), this.getBoundingBox().minY - 1, this.getZ()));
            return this.getY() < 60.0 && (below.is(Blocks.NETHERRACK) || below.is(Blocks.BRICKS));
        }
    }

    /** EntityPirate: puts out fires, takes a boat in water, makes its victims dizzy. */
    public static class Pirate extends HumanEntity implements Enemy {
        boolean boat = true;

        public Pirate(EntityType<? extends Pirate> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 5;
            this.attackStrength = 4;
            this.attackSpeed = 30;
            this.arrowAccuracy = 6.0F;
            this.range = 25;
            this.experienceValue = 8 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.grenadeType = 0;
            this.humanType = "pirate";
            this.rivalHumans = new String[]{"goblin", "enderman", "beast", "zombie", "friend"};
        }

        @Override
        protected void addAITasks() {
            this.goalSelector.addGoal(4, new HumanGoals.Firefighter(this, this.baseMoveSpeed));
            super.addAITasks();
        }

        @Override
        public void aiStep() {
            if (this.boat && !this.level().isClientSide() && this.getVehicle() == null
                    && this.level().getFluidState(this.blockPosition()).is(net.minecraft.tags.FluidTags.WATER)) {
                this.boat = false;
                AbstractBoat boat = EntityTypes.OAK_BOAT.create(this.level(), EntitySpawnReason.EVENT);
                if (boat != null) {
                    boat.setPos(this.getX(), this.getY(), this.getZ());
                    this.level().addFreshEntity(boat);
                    this.startRiding(boat, false, true);
                }
            }
            if (this.random.nextInt(1000) == 1) {
                for (int r = 0; r < 10; r++) {
                    this.clientParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextFloat() - 0.5, this.getY() + 1.0 + this.random.nextFloat(),
                            this.getZ() + this.random.nextFloat() - 0.5, 0.0, 0.0, 0.0);
                }
            }
            super.aiStep();
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            if (this.random.nextInt(10) == 0) {
                BDMobUtil.hurtParticles(this, ParticleTypes.SPLASH, 10);
            }
            return super.hurtServer(level, source, amount);
        }

        @Override
        public boolean doHurtTarget(ServerLevel level, Entity entity) {
            if (entity instanceof LivingEntity living && this.getlvl() > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 50 * this.getlvl(), 4));
            }
            return super.doHurtTarget(level, entity);
        }

        @Override
        protected boolean canSpawnHere(LevelAccessor level) {
            if (this.random.nextInt(5) == 0) {
                BlockPos below = BlockPos.containing(this.getX(), this.getBoundingBox().minY - 1, this.getZ());
                BlockState state = level.getBlockState(below);
                if (this.getY() > 60.0 && (state.is(BlockTags.PLANKS) || state.is(Blocks.STONE) || state.is(BlockTags.LOGS)
                        || state.is(Blocks.STONE_BRICKS) || state.is(Blocks.MOSSY_STONE_BRICKS) || state.is(Blocks.CRACKED_STONE_BRICKS)
                        || state.is(Blocks.CHISELED_STONE_BRICKS) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK))) {
                    return true;
                }
                if (this.getY() > 60.0 && level.getFluidState(below).is(Fluids.WATER)) {
                    return true;
                }
                return this.random.nextInt(4) == 1;
            }
            return false;
        }

        /** dropFewItems: an XP orb of lvl + 6. */
        @Override
        protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
            super.dropCustomDeathLoot(level, source, killedByPlayer);
            net.minecraft.world.entity.ExperienceOrb.award(level, this.position(), this.getlvl() + 6);
        }

        /** setArcher of the pirate: a revolver in the second hand and the arrow AI (shooting plain arrows). */
        @Override
        public void setArcher(boolean flag) {
            if (flag) {
                this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(betterdungeons.BDItems.REVOLVER.get()));
                this.setRangedAI(1, new HumanGoals.ArrowAttack(this, 0.25F));
                this.setClassByte(ARCHER);
            }
        }

        @Override
        public boolean isSuitableMount(@Nullable Entity entity) {
            return entity instanceof Cow || entity instanceof AbstractBoat;
        }

        @Override
        public Class<? extends LivingEntity> getMountClass() {
            return Animal.class;
        }

        @Override
        public void setMountAI() {
            if (this.getVehicle() instanceof AbstractBoat boat) {
                LivingEntity target = this.getTarget();
                if (target != null) {
                    float rot = Mth.clamp(Mth.wrapDegrees(this.getYHeadRot() - 90.0F - boat.getYRot()), -5.0F, 5.0F);
                    boat.setYRot(boat.getYRot() + rot);
                    this.setYRot(boat.getYRot() + 90.0F);
                    double x = Math.cos(boat.getYRot() * Math.PI / 180.0);
                    double z = Math.sin(boat.getYRot() * Math.PI / 180.0);
                    boat.setDeltaMovement(-x / 12.0, boat.getDeltaMovement().y, -z / 12.0);
                    if (boat.horizontalCollision && target.onGround()) {
                        this.stopRiding();
                    }
                }
            } else {
                super.setMountAI();
            }
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.VILLAGER_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.PLAYER_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.VILLAGER_DEATH;
        }
    }

    /** EntityPigManArmored: fire immune, never sits. */
    public static class ArmoredPigZombie extends HumanEntity implements Enemy {
        public ArmoredPigZombie(EntityType<? extends ArmoredPigZombie> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 1;
            this.attackStrength = 4;
            this.attackSpeed = 25;
            this.experienceValue = 12 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "pigman";
            this.rivalHumans = new String[]{"walker", "zombie", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 30;
        }

        @Override
        public boolean isSitting() {
            return false;
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
        }
    }

    /** EntityWalker (shadow walkers). */
    public static class Walker extends HumanEntity implements Enemy {
        public Walker(EntityType<? extends Walker> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 6;
            this.attackStrength = 6;
            this.attackSpeed = 30;
            this.arrowAccuracy = 3.0F;
            this.range = 35;
            this.experienceValue = 10 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "walker";
            this.rivalHumans = new String[]{"pigman", "goblin", "pirate", "golem", "enderman", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 30;
        }

        @Override
        protected void addAITasks() {
            this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
            super.addAITasks();
        }

        @Override
        public @Nullable ItemStack getEquipedWeapon() {
            int lvl = this.getlvl();
            return lvl >= 0 && lvl <= 4 ? super.getEquipedWeapon() : new ItemStack(Items.GOLDEN_SWORD);
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return BDSounds.BREATH_LOUD.get();
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return BDSounds.BREATH_LOUD.get();
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.WITHER_AMBIENT;
        }
    }

    /** EntitySpecter (drawn as a ghost). */
    public static class Specter extends HumanEntity implements Enemy {
        public Specter(EntityType<? extends Specter> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 8;
            this.attackStrength = 3;
            this.attackSpeed = 20;
            this.arrowAccuracy = 8.0F;
            this.range = 25;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "zombie";
            this.rivalHumans = new String[]{"dwarf", "pirate", "goblin", "beast", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 25;
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.ENDERMAN_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.ENDERMAN_SCREAM;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.ENDERMAN_DEATH;
        }
    }

    static SoundEvent smallLaugh(Entity e, int chance) {
        return e.getRandom().nextInt(chance) == 0 ? BDSounds.SMALL_MONSTER_LAUGHT.get() : null;
    }

    /** EntityGoblin (green): rides spiders, always sneaks. */
    public static class Goblin extends HumanEntity implements Enemy {
        public Goblin(EntityType<? extends Goblin> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = true;
            this.sneak = true;
            this.shiedID = 7;
            this.attackStrength = 2;
            this.arrowAccuracy = 15.0F;
            this.range = 15;
            this.attackSpeed = 25;
            this.experienceValue = 1 + this.getlvl() / 2;
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "goblin";
            this.rivalHumans = new String[]{"zombie", "walker", "ogre", "friend"};
        }

        @Override
        public float getSizeModifier() {
            return 0.7F * super.getSizeModifier();
        }

        @Override
        protected int maxHealthValue() {
            return 15;
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            if (this.random.nextInt(10) == 0) {
                BDMobUtil.hurtParticles(this, ParticleTypes.LARGE_SMOKE, 10);
            }
            return super.hurtServer(level, source, amount);
        }

        /** "soudns.smallMonsterSpeak" (a typo of 1.5.2) played nothing: only the laugh, one time in five. */
        @Override
        protected @Nullable SoundEvent getAmbientSound() {
            return smallLaugh(this, 5);
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return this.random.nextInt(10) == 0 ? BDSounds.SMALL_MONSTER_LAUGHT.get() : BDSounds.SMALL_MONSTER_HURT.get();
        }

        @Override
        protected SoundEvent getDeathSound() {
            return BDSounds.SMALL_MONSTER_DEAD.get();
        }

        @Override
        public boolean isSuitableMount(@Nullable Entity entity) {
            return entity instanceof Spider;
        }

        @Override
        public Class<? extends LivingEntity> getMountClass() {
            return Spider.class;
        }

        @Override
        public void setMountAI() {
            this.mountAttacksTarget();
        }
    }

    /** EntityGoblinBlue ("SnowGoblin"): axes, rides wolves. */
    public static class SnowGoblin extends HumanEntity implements Enemy {
        public SnowGoblin(EntityType<? extends SnowGoblin> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = true;
            this.sneak = true;
            this.shiedID = 7;
            this.attackStrength = 2;
            this.arrowAccuracy = 10.0F;
            this.range = 15;
            this.attackSpeed = 25;
            this.experienceValue = 1 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "goblin";
            this.rivalHumans = new String[]{"zombie", "walker", "dwarf", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 15;
        }

        @Override
        public float getSizeModifier() {
            return 0.65F * super.getSizeModifier();
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            if (this.random.nextInt(10) == 0) {
                BDMobUtil.hurtParticles(this, ParticleTypes.ITEM_SNOWBALL, 10);
            }
            if (!source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL) && this.random.nextInt(5) == 1) {
                SoundEvent hurt = this.getHurtSound(source);
                this.playSound(hurt, this.getSoundVolume(), 1.5F);
            }
            return super.hurtServer(level, source, amount);
        }

        @Override
        protected @Nullable SoundEvent getAmbientSound() {
            return smallLaugh(this, 5);
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return this.random.nextInt(10) == 0 ? BDSounds.SMALL_MONSTER_LAUGHT.get() : BDSounds.SMALL_MONSTER_HURT.get();
        }

        @Override
        protected SoundEvent getDeathSound() {
            return BDSounds.SMALL_MONSTER_DEAD.get();
        }

        @Override
        public @Nullable ItemStack getEquipedWeapon() {
            return switch (this.getlvl()) {
                case 0 -> new ItemStack(Items.WOODEN_AXE);
                case 1, 2 -> new ItemStack(Items.IRON_AXE);
                case 3 -> new ItemStack(Items.GOLDEN_AXE);
                case 4 -> new ItemStack(Items.DIAMOND_AXE);
                default -> new ItemStack(Items.GOLDEN_AXE);
            };
        }

        @Override
        public boolean isSuitableMount(@Nullable Entity entity) {
            return entity instanceof Wolf;
        }

        @Override
        public Class<? extends LivingEntity> getMountClass() {
            return Wolf.class;
        }

        @Override
        public void setMountAI() {
            this.mountAttacksTarget();
        }
    }

    /** EntityTriton (naga): swims after its target, breathes water. */
    public static class Triton extends HumanEntity implements Enemy {
        public Triton(EntityType<? extends Triton> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.shiedID = 10;
            this.attackStrength = 3;
            this.attackSpeed = 20;
            this.arrowAccuracy = 6.0F;
            this.range = 25;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "beast";
            this.rivalHumans = new String[]{"walker", "pirate", "goblin", "zombie", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 25;
        }

        @Override
        public float speedFactor() {
            return this.isInWater() ? super.speedFactor() + 0.6F : super.speedFactor();
        }

        @Override
        public boolean isSitting() {
            return false;
        }

        @Override
        public void aiStep() {
            LivingEntity target = this.getTarget();
            var m = this.getDeltaMovement();
            if (target != null) {
                if (this.isInWater()) {
                    if (this.getY() + 1.0 < target.getY() && m.y < 0.2) {
                        this.setDeltaMovement(m.add(0.0, 0.03, 0.0));
                    } else if (this.getY() + 1.0 > target.getY() && m.y > -0.2) {
                        this.setDeltaMovement(m.add(0.0, -0.03, 0.0));
                    }
                }
            } else if (this.isInWater() && m.y < 0.1) {
                this.setDeltaMovement(m.add(0.0, -0.03, 0.0));
            }
            super.aiStep();
        }

        @Override
        public boolean canBreatheUnderwater() {
            return true;
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.VILLAGER_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.PLAYER_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.VILLAGER_DEATH;
        }
    }

    /** Biped humans that only differ in values: EntityMonkey, EntityGremlin, EntityGolemSmall, EntityOgre, EntityOrc. */
    public abstract static class Simple extends HumanEntity implements Enemy {
        protected Simple(EntityType<? extends Simple> type, Level level) {
            super(type, level);
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return SoundEvents.VILLAGER_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.PLAYER_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.VILLAGER_DEATH;
        }
    }

    /** EntityMonkey (mandrill): leaps at its target. */
    public static class Monkey extends Simple {
        public Monkey(EntityType<? extends Monkey> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = false;
            this.sneak = true;
            this.shiedID = 9;
            this.attackStrength = 3;
            this.attackSpeed = 20;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "beast";
            this.rivalHumans = new String[]{"skeleton", "zombie", "spectre", "walker", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 25;
        }

        @Override
        protected void addAITasks() {
            super.addAITasks();
            this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4F));
        }
    }

    /** EntityGremlin. */
    public static class Gremlin extends HumanEntity implements Enemy {
        public Gremlin(EntityType<? extends Gremlin> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.shiedID = 12;
            this.attackStrength = 2;
            this.attackSpeed = 20;
            this.arrowAccuracy = 12.0F;
            this.range = 15;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.33F;
            this.canMount = true;
            this.humanType = "goblin";
            this.rivalHumans = new String[]{"pirate", "beast", "golem", "dwarf", "friend"};
        }

        @Override
        public boolean isSitting() {
            return false;
        }

        @Override
        protected @Nullable SoundEvent getAmbientSound() {
            return smallLaugh(this, 5);
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return this.random.nextInt(10) == 0 ? BDSounds.SMALL_MONSTER_LAUGHT.get() : BDSounds.SMALL_MONSTER_HURT.get();
        }

        @Override
        protected SoundEvent getDeathSound() {
            return BDSounds.SMALL_MONSTER_DEAD.get();
        }
    }

    /** EntityGolemSmall. */
    public static class GolemSmall extends HumanEntity implements Enemy {
        public GolemSmall(EntityType<? extends GolemSmall> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.shiedID = 2;
            this.attackStrength = 4;
            this.attackSpeed = 20;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.arrowAccuracy = 6.0F;
            this.range = 30;
            this.canMount = true;
            this.humanType = "dwarf";
            this.rivalHumans = new String[]{"beast", "zombie", "goblin", "walker", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 30;
        }

        /** getLivingSound "none": no such sound. */
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

    /** EntityOgre ("OgreArmored"). */
    public static class Ogre extends Simple {
        public Ogre(EntityType<? extends Ogre> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.shiedID = 7;
            this.attackStrength = 3;
            this.attackSpeed = 20;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.humanType = "ogre";
            this.arrowAccuracy = 12.0F;
            this.range = 30;
            this.rivalHumans = new String[]{"goblin", "zombie", "walker", "friend"};
        }

        @Override
        public float getSizeModifier() {
            return 1.5F * super.getSizeModifier();
        }

        @Override
        protected int maxHealthValue() {
            return 30;
        }
    }

    /** EntityMinotaur ("MinotaurArmored"): the charging attack. */
    public static class Minotaur extends Simple {
        public Minotaur(EntityType<? extends Minotaur> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.shiedID = 11;
            this.attackStrength = 3;
            this.attackSpeed = 20;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.29F;
            this.arrowAccuracy = 6.0F;
            this.range = 20;
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
        protected int maxHealthValue() {
            return 25;
        }
    }

    /** EntityOrc ("OrcArmored"), also the mount of the goblin boss. */
    public static class Orc extends Simple {
        public Orc(EntityType<? extends Orc> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.shiedID = 7;
            this.attackStrength = 3;
            this.attackSpeed = 20;
            this.experienceValue = 5 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = true;
            this.arrowAccuracy = 12.0F;
            this.range = 30;
            this.humanType = "goblin";
            this.rivalHumans = new String[]{"zombie", "walker", "ogre", "friend"};
        }

        @Override
        public float getSizeModifier() {
            return 1.1F;
        }

        @Override
        protected int maxHealthValue() {
            return 30;
        }
    }

    /** EntityEndermenArmored: teleports away from 1 in 5 hits and from every indirect hit. */
    public static class ArmoredEnderman extends HumanEntity implements Enemy {
        public ArmoredEnderman(EntityType<? extends ArmoredEnderman> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.renderEars = false;
            this.sneak = false;
            this.shiedID = 0;
            this.attackStrength = 8;
            this.attackSpeed = 25;
            this.arrowAccuracy = 3.0F;
            this.range = 30;
            this.experienceValue = 10 + this.getlvl();
            this.baseMoveSpeed = 0.25F;
            this.canMount = false;
            this.grenadeType = 4;
            this.humanType = "enderman";
            this.rivalHumans = new String[]{"walker", "goblin", "dwarf", "pirate", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 40;
        }

        @Override
        public boolean isSitting() {
            return false;
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            if (this.isInvulnerableTo(level, source)) {
                return false;
            }
            if (source.isDirect() && this.random.nextInt(5) != 0) {
                return super.hurtServer(level, source, amount);
            }
            for (int i = 0; i < 64; i++) {
                if (this.teleportRandomly()) {
                    return true;
                }
            }
            return false;
        }

        protected boolean teleportRandomly() {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 32.0;
            double y = this.getY() + (this.random.nextInt(64) - 32);
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * 32.0;
            return BDMobUtil.teleport(this, x, y, z, true, false);
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return this.getTarget() != null ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource source) {
            return SoundEvents.ENDERMAN_HURT;
        }

        @Override
        protected SoundEvent getDeathSound() {
            return SoundEvents.ENDERMAN_DEATH;
        }
    }
}
