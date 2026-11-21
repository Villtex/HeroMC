package twilightforest.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import twilightforest.TwilightForest;

import java.util.EnumSet;

/**
 * EntityTFGoblinKnightUpper: the rider of the goblin knight, 30 health, 1.1 x 1.3, 20 armour while its armour holds,
 * a shield on its back side. Every hit it either strikes with the spear or (1 in 2) starts the heavy spear: 60 ticks,
 * during which it and its mount stand still, the upper body rearing and slamming down. The shield takes hits from
 * behind (over 10 damage cracks it, the third such breaks it) and knocks the knight back; it falls off when the knight
 * is on foot. No suffocation while riding.
 */
public class GoblinKnightUpper extends TFMonster {
    private static final EntityDataAccessor<Byte> DATA_EQUIP = SynchedEntityData.defineId(GoblinKnightUpper.class, EntityDataSerializers.BYTE);
    private static final Identifier ARMOR_ID = TwilightForest.id("goblin_armor");
    public int shieldHits;
    public int heavySpearTimer;

    public GoblinKnightUpper(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new HeavySpearGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_EQUIP, (byte) 3);
    }

    public boolean hasArmor() {
        return (this.entityData.get(DATA_EQUIP) & 1) != 0;
    }

    public boolean hasShield() {
        return (this.entityData.get(DATA_EQUIP) & 2) != 0;
    }

    public void setHasArmor(boolean armor) {
        byte flags = this.entityData.get(DATA_EQUIP);
        this.entityData.set(DATA_EQUIP, (byte) (armor ? flags | 1 : flags & ~1));
        var attribute = this.getAttribute(Attributes.ARMOR);
        if (attribute != null) {
            attribute.removeModifier(ARMOR_ID);
            if (armor) {
                attribute.addPermanentModifier(new AttributeModifier(ARMOR_ID, 20.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    public void setHasShield(boolean shield) {
        byte flags = this.entityData.get(DATA_EQUIP);
        this.entityData.set(DATA_EQUIP, (byte) (shield ? flags | 2 : flags & ~2));
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        this.setHasArmor(this.hasArmor());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("hasArmor", this.hasArmor());
        output.putBoolean("hasShield", this.hasShield());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setHasArmor(input.getBooleanOr("hasArmor", true));
        this.setHasShield(input.getBooleanOr("hasShield", true));
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            if (this.getVehicle() instanceof net.minecraft.world.entity.Mob mount && this.getTarget() == null && mount.getTarget() != null) {
                this.setTarget(mount.getTarget());
            }
            if (this.heavySpearTimer > 0) {
                this.heavySpearTimer--;
            }
            if (!this.level().isClientSide() && this.getVehicle() == null && this.hasShield()) {
                this.breakShield((ServerLevel) this.level());
            }
        }
        super.tick();
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof LivingEntity mount) {
            this.yBodyRot = mount.yBodyRot;
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.heavySpearTimer = 60;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.heavySpearTimer > 0) {
            return false;
        }
        if (this.random.nextInt(2) == 0) {
            this.heavySpearTimer = 60;
            level.broadcastEntityEvent(this, (byte) 4);
            return false;
        }
        this.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        return super.doHurtTarget(level, target);
    }

    /** The yaw of the knight's body against the line from the attacker, 0..360 (180 = from behind). */
    static float facingDifference(LivingEntity me, Entity attacker) {
        double dx = me.getX() - attacker.getX();
        double dz = me.getZ() - attacker.getZ();
        float angle = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        return Math.abs((me.yBodyRot - angle) % 360.0F);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL) && this.getVehicle() != null) {
            return false;
        }
        Entity attacker = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (attacker != null) {
            float difference = facingDifference(this, attacker);
            if (this.hasShield() && difference > 150.0F && difference < 230.0F && this.takeHitOnShield(level, source, amount)) {
                return false;
            }
            if (this.hasArmor() && (difference > 300.0F || difference < 60.0F)) {
                breakEffect(level, this);
                this.setHasArmor(false);
            }
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.getVehicle() instanceof LivingEntity mount && attacker != null) {
            mount.knockback(amount * 0.1, attacker.getX() - mount.getX(), attacker.getZ() - mount.getZ(), source, amount);
        }
        return hurt;
    }

    /** renderBrokenItemStack of an iron chestplate, three times. */
    static void breakEffect(ServerLevel level, LivingEntity entity) {
        for (int i = 0; i < 3; i++) {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.IRON_CHESTPLATE), entity.getX(), entity.getEyeY(), entity.getZ(),
                    5, 0.1, 0.1, 0.1, 0.05);
        }
        entity.playSound(SoundEvents.ITEM_BREAK.value(), 0.8F, 0.8F + level.getRandom().nextFloat() * 0.4F);
    }

    public void breakShield(ServerLevel level) {
        breakEffect(level, this);
        this.setHasShield(false);
    }

    public boolean takeHitOnShield(ServerLevel level, DamageSource source, float amount) {
        if (amount > 10.0F) {
            level.levelEvent(1020, this.blockPosition(), 0);
            this.shieldHits++;
            if (this.shieldHits >= 3) {
                this.breakShield(level);
            }
        } else {
            this.playSound(SoundEvents.ITEM_BREAK.value(), 1.0F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
        LivingEntity toKnockback = this.getVehicle() instanceof LivingEntity mount ? mount : this;
        Entity attacker = source.getEntity();
        if (attacker != null) {
            double d0 = attacker.getX() - this.getX();
            double d1 = attacker.getZ() - this.getZ();
            while (d0 * d0 + d1 * d1 < 1.0E-4) {
                d0 = (Math.random() - Math.random()) * 0.01;
                d1 = (Math.random() - Math.random()) * 0.01;
            }
            toKnockback.knockback(0.4, d0 / 4.0, d1 / 4.0, source, amount);
            if (attacker instanceof LivingEntity living) {
                this.setLastHurtByMob(living);
            }
        }
        return true;
    }

    /** EntityAITFHeavySpearAttack: holds everything (mutex 7) while the heavy spear is under way (timer 1..49). */
    static final class HeavySpearGoal extends Goal {
        private final GoblinKnightUpper knight;

        HeavySpearGoal(GoblinKnightUpper knight) {
            this.knight = knight;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.knight.heavySpearTimer > 0 && this.knight.heavySpearTimer < 50;
        }
    }
}
