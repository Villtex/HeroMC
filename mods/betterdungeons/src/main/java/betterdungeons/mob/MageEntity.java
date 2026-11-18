package betterdungeons.mob;

import betterdungeons.api.BDEntityIds;
import betterdungeons.api.BDSummonedMob;
import betterdungeons.api.BDSummoner;
import betterdungeons.mob.ai.HumanGoals;
import betterdungeons.mob.ai.HumanTargetGoals;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * mobs.boss.EntityMage: the casters (Necromancer, Liche, PigMage). No weapon, armour or class; AIMageAttack calls
 * {@link #attackEntity} while it moves, which hits in melee range and casts the spells of the kind by their cooldowns.
 * The health depends on the difficulty (set when the mage is made).
 */
public abstract class MageEntity extends HumanEntity implements Enemy, HumanGoals.Caster, BDSummoner {
    protected int vampiricCD = 60;
    protected int summonCD = 120;
    protected int gravelTombCD = 120;
    protected int teleportCD = 40;
    int fireballCD = 60;
    int fireShiedCD = 60;
    protected int earthquakeCD = 120;
    protected int customSpell = 120;
    public int summonCount;
    int gravel = 3;

    protected MageEntity(EntityType<? extends MageEntity> type, Level level) {
        super(type, level);
    }

    /** difficultySetting of 1.5.2 (0 peaceful .. 3 hard). */
    protected int difficulty() {
        return this.level().getDifficulty().getId();
    }

    @Override
    protected void addAITasks() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HumanGoals.MageAttack(this, this, 0.25F, false));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, this.speed(0.25F)));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HumanTargetGoals.HurtBy(this) {
            @Override
            public boolean canUse() {
                // EntityAIHurtByTarget: also against its own kind
                return this.isSuitableTarget(this.taskOwner.getLastHurtByMob(), true);
            }
        });
        this.targetSelector.addGoal(2, new HumanTargetGoals.NearestAttackable(this, Player.class, 25.0F, true));
        if (this.rivalHumans != null) {
            this.targetSelector.addGoal(2, new HumanTargetGoals.NearestRival(this, HumanEntity.class, 25.0F, true, this.rivalHumans, false));
        }
    }

    @Override
    public void initDungeonMob(int level, int type) {
        this.initAsWorld();
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public void getLeader() {
    }

    @Override
    public void setRandomType() {
    }

    @Override
    public @Nullable ItemStack getEquipedWeapon() {
        return null;
    }

    @Override
    public @Nullable ItemStack getShiedItem() {
        return null;
    }

    @Override
    public @Nullable ItemStack getArmor(int slot) {
        return null;
    }

    @Override
    public List<String> getClassNameKeys() {
        return List.of();
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public int getAttackStrength(Entity entity) {
        return 6;
    }

    @Override
    public void summonGone() {
        this.summonCount--;
    }

    /** The melee blow of attackEntity (distance squared below 2, overlapping in height) with nausea. */
    protected boolean melee(LivingEntity entity, float f, boolean nausea) {
        if (this.attackTime <= 0 && f < 2.0F && entity.getBoundingBox().maxY > this.getBoundingBox().minY
                && entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
            this.attackTime = 20;
            if (this.level() instanceof ServerLevel server) {
                this.doHurtTarget(server, entity);
            }
            if (nausea) {
                entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 300, 5));
            }
            return true;
        }
        return false;
    }

    @Override
    public void attackEntity(LivingEntity entity, float f) {
        this.melee(entity, f, true);
        f = (float) Math.sqrt(f);
        this.castVampiric(entity, f, 4);
        this.castSummon(2);
        this.castGravel(entity, f);
        this.castTeleport(entity, f);
        this.castFireBall(entity, f);
    }

    private BDProjectile aimedBall(Entity entity, float f, double spreadDiv, ProjectileType type, int lvl) {
        double d = entity.getX() - this.getX();
        double d1 = entity.getBoundingBox().minY + entity.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
        float f1 = Mth.sqrt(f) * 0.5F;
        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
        double d2 = entity.getZ() - this.getZ();
        BDProjectile ball = BDProjectile.createAimed(this, d + this.random.nextGaussian() / spreadDiv * f1, d1,
                d2 + this.random.nextGaussian() / spreadDiv * f1, type, lvl);
        ball.setPos(ball.getX(), this.getY() + this.getBbHeight() / 2.0F + 0.5, ball.getZ());
        this.level().addFreshEntity(ball);
        this.setYRot((float) (Math.atan2(d1, d) * 180.0 / Math.PI) - 90.0F);
        return ball;
    }

    protected void castVampiric(LivingEntity entity, float f, int lvl) {
        if (f < 100.0F) {
            if (this.vampiricCD <= 0) {
                this.aimedBall(entity, f, lvl * 2, ProjectileType.VAMPIRE, lvl);
                this.vampiricCD = 30;
                for (int c = 0; c < 3; c++) {
                    BDMobUtil.particle(this, net.minecraft.core.particles.SpellParticleOption.create(ParticleTypes.EFFECT, -1, 1.0F), this.getX() + this.random.nextFloat() - 0.5, this.getY() + this.random.nextFloat() - 0.5,
                            this.getZ() + this.random.nextFloat() - 0.5, 0.0, 0.0, 0.0);
                }
                this.increaseAllCooldowns();
            } else {
                double d = entity.getX() - this.getX();
                double d1 = entity.getBoundingBox().minY + entity.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
                this.setYRot((float) (Math.atan2(d1, d) * 180.0 / Math.PI) - 90.0F);
            }
        }
        this.swingItem();
        this.vampiricCD--;
    }

    /** castSummon: up to four EntitySummonedUndead of the level (registered by the other half of the port). */
    protected void castSummon(int lvl) {
        if (this.summonCD < 0 && this.summonCount < 4) {
            for (int c = 0; c < 3 * lvl; c++) {
                BDMobUtil.particle(this, ParticleTypes.WITCH, this.getX() + this.random.nextFloat() - 0.5, this.getY() + this.random.nextFloat() - 0.5,
                        this.getZ() + this.random.nextFloat() - 0.5, 0.0, 0.0, 0.0);
            }
            EntityType<?> undead = BDEntityIds.type("SummonedUndead");
            if (undead != null) {
                Entity e = undead.create(this.level(), EntitySpawnReason.MOB_SUMMONED);
                if (e != null) {
                    if (e instanceof BDSummonedMob summoned) {
                        summoned.initSummoned(this, lvl, lvl);
                    }
                    e.setPos(this.getX(), this.getY(), this.getZ());
                    this.level().addFreshEntity(e);
                }
            }
            this.summonCD = 100;
            this.summonCount++;
            this.increaseAllCooldowns();
        }
        this.summonCD--;
    }

    /** castGravel: a cross of gravel above the target, three times, then a long pause. */
    protected void castGravel(LivingEntity entity, float f) {
        if (this.gravelTombCD < 0 && f > 2.0F) {
            if (this.gravel == 0) {
                this.gravel = 3;
            }
            int x = (int) entity.getX();
            int y = Mth.floor(entity.getBoundingBox().maxY) + 1;
            int z = (int) entity.getZ();
            Level level = this.level();
            BlockPos[] cross = {new BlockPos(x, y, z), new BlockPos(x + 1, y, z), new BlockPos(x - 1, y, z), new BlockPos(x, y, z + 1),
                    new BlockPos(x, y, z - 1)};
            boolean free = true;
            for (BlockPos p : cross) {
                free &= level.getBlockState(p).isAir();
            }
            if (this.gravel > 0 && free) {
                for (BlockPos p : cross) {
                    level.setBlock(p, Blocks.GRAVEL.defaultBlockState(), 3);
                }
                this.gravel--;
            }
            if (this.gravel == 0) {
                this.gravelTombCD = 1020;
                this.increaseAllCooldowns();
            }
        }
        this.gravelTombCD--;
    }

    protected boolean castTeleport(LivingEntity entity, float dist) {
        if (this.teleportCD < 0 && dist < 8.0F) {
            double x = entity.getX() + (this.random.nextDouble() - 0.5) * 16.0;
            double z = entity.getZ() + (this.random.nextDouble() - 0.5) * 16.0;
            if (!BDMobUtil.teleport(this, x, entity.getY(), z, true, false)) {
                return false;
            }
            this.teleportCD = 70;
            this.increaseAllCooldowns();
            return true;
        }
        this.teleportCD--;
        return false;
    }

    protected void castFireBall(LivingEntity entity, float f) {
        if (this.fireballCD < 0 && f < 100.0F && this.getHealth() > 95.0F) {
            this.aimedBall(entity, f, 2.0, ProjectileType.FIREBALL, 1);
            this.attackTime = 30;
            this.fireballCD = 30;
            this.increaseAllCooldowns();
        }
        this.fireballCD--;
    }

    protected void castCustomSpell(LivingEntity entity, float f) {
        if (this.customSpell < 0) {
            BDProjectile.spawn(this, ProjectileType.THUNDER);
            this.customSpell = 60;
            this.increaseAllCooldowns();
        }
        this.customSpell--;
    }

    protected void castFireShied(LivingEntity entity, float f) {
        if (this.fireShiedCD < 0) {
            BDProjectile.spawn(this, ProjectileType.FIRESHIELD);
            this.fireShiedCD = 400;
            this.increaseAllCooldowns();
        }
        this.fireShiedCD--;
    }

    protected void castEarthQuake(LivingEntity entity, float f) {
        if (this.earthquakeCD < 0 && f < 8.0F && this.onGround()) {
            BDProjectile.spawn(this, ProjectileType.EARTHQUAKE_AREA);
            this.earthquakeCD = 300;
            this.increaseAllCooldowns();
        }
        this.earthquakeCD--;
    }

    protected void increaseAllCooldowns() {
        this.vampiricCD += 2 + this.random.nextInt(10);
        this.summonCD += 2 + this.random.nextInt(10);
        this.gravelTombCD += 2 + this.random.nextInt(10);
        this.teleportCD += 2 + this.random.nextInt(10);
        this.fireballCD += 2 + this.random.nextInt(10);
        this.fireShiedCD += 2 + this.random.nextInt(10);
        this.earthquakeCD += 2 + this.random.nextInt(10);
    }

    /** The XP orb of 50 every mage dropped besides its experience. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        ExperienceOrb.award(level, this.position(), 50);
    }

    /** The difficulty decides the health: the base value is set again once the level is known. */
    protected void applyDifficultyHealth() {
        double max = this.maxHealthValue();
        if (this.getAttributeBaseValue(Attributes.MAX_HEALTH) != max) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(max);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public void initAsWorld() {
        super.initAsWorld();
        this.applyDifficultyHealth();
    }

    /** EntityNecromancer: summons one level-1 undead at a time, thunder bolts; armour = difficulty; a bat escapes when it dies. */
    public static class Necromancer extends MageEntity {
        public Necromancer(EntityType<? extends Necromancer> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.experienceValue = 20;
            this.humanType = "zombie";
            this.rivalHumans = new String[]{"walker", "dwarf", "pirate", "beast", "goblin", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 100 + this.difficulty() * 10;
        }

        @Override
        public void initAsWorld() {
            super.initAsWorld();
            this.getAttribute(Attributes.ARMOR).setBaseValue(this.difficulty());
        }

        @Override
        public void attackEntity(LivingEntity entity, float f) {
            this.melee(entity, f, true);
            f = (float) Math.sqrt(f);
            this.castVampiric(entity, f, 3);
            this.castSummon(1);
            this.castCustomSpell(entity, f);
        }

        @Override
        public void die(DamageSource source) {
            if (!this.level().isClientSide()) {
                Entity bat = EntityTypes.BAT.create(this.level(), EntitySpawnReason.MOB_SUMMONED);
                if (bat != null) {
                    bat.setPos(this.getX(), this.getY(), this.getZ());
                    this.level().addFreshEntity(bat);
                }
            }
            super.die(source);
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
    }

    /** EntityLiche ("Lich"): fire immune; gravel tombs, earthquakes, teleports below 40. */
    public static class Liche extends MageEntity {
        public Liche(EntityType<? extends Liche> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.experienceValue = 30;
            this.humanType = "zombie";
            this.rivalHumans = new String[]{"walker", "goblin", "dwarf", "pigman", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 200 + this.difficulty() * 20;
        }

        @Override
        public void attackEntity(LivingEntity entity, float f) {
            f = (float) Math.sqrt(f);
            this.melee(entity, f, false);
            f = (float) Math.sqrt(f);
            this.castVampiric(entity, f, 3);
            this.castSummon(2);
            this.castGravel(entity, f);
            this.castEarthQuake(entity, f);
            if (this.getHealth() < 40.0F) {
                this.castTeleport(entity, f);
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
    }

    /** EntityPigMage: fire immune; fire shield, fire walls, fire balls (vampire balls below 100). */
    public static class PigMage extends MageEntity {
        public PigMage(EntityType<? extends PigMage> type, Level level) {
            super(type, level);
        }

        @Override
        protected void configRaze() {
            this.experienceValue = 20;
            this.grenadeType = 3;
            this.humanType = "pigman";
            this.rivalHumans = new String[]{"walker", "zombie", "friend"};
        }

        @Override
        protected int maxHealthValue() {
            return 200 + this.difficulty() * 30;
        }

        @Override
        public void attackEntity(LivingEntity entity, float f) {
            if (this.melee(entity, f, true)) {
                this.increaseAllCooldowns();
            }
            f = (float) Math.sqrt(f);
            this.castSummon(3);
            this.castFireShied(entity, f);
            this.castEarthQuake(entity, f);
            if (this.getHealth() < 100.0F) {
                this.castVampiric(entity, f, 5);
            } else {
                this.castFireBall(entity, f);
            }
            if (this.getHealth() < 30.0F) {
                this.castTeleport(entity, f);
            }
        }

        /** castEarthQuake of the pig mage: a fire wall, without raising the other cooldowns. */
        @Override
        protected void castEarthQuake(LivingEntity entity, float f) {
            if (this.earthquakeCD < 0 && f < 8.0F && this.onGround()) {
                BDProjectile.spawn(this, ProjectileType.FIREWALL);
                this.earthquakeCD = 400;
            }
            this.earthquakeCD--;
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
}
