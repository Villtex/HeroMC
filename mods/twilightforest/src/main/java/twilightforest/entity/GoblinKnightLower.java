package twilightforest.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;
import twilightforest.TwilightForest;

import java.util.EnumSet;

/**
 * EntityTFGoblinKnightLower: the legs of the goblin knight, 20 health, 0.7 x 1.1, speed 0.28, 17 armour while its
 * breastplate holds; a hit from the front breaks it. It carries the upper knight (spawned with it), shares its target
 * and lets it strike; while the rider charges its heavy spear it stands still. A hit from behind lands on the rider's
 * shield.
 */
public class GoblinKnightLower extends TFMonster {
    private static final EntityDataAccessor<Boolean> DATA_ARMOR = SynchedEntityData.defineId(GoblinKnightLower.class, EntityDataSerializers.BOOLEAN);
    private static final Identifier ARMOR_ID = TwilightForest.id("goblin_breastplate");

    public GoblinKnightLower(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RiderSpearGoal(this));
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
        builder.define(DATA_ARMOR, true);
    }

    public boolean hasArmor() {
        return this.entityData.get(DATA_ARMOR);
    }

    public void setHasArmor(boolean armor) {
        this.entityData.set(DATA_ARMOR, armor);
        var attribute = this.getAttribute(Attributes.ARMOR);
        if (attribute != null) {
            attribute.removeModifier(ARMOR_ID);
            if (armor) {
                attribute.addPermanentModifier(new AttributeModifier(ARMOR_ID, 17.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }
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
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setHasArmor(input.getBooleanOr("hasArmor", true));
    }

    /** initCreature: the upper knight rides it. */
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        GoblinKnightUpper upper = TFEntities.UPPER_GOBLIN_KNIGHT.get().create(this.level(), EntitySpawnReason.JOCKEY);
        if (upper != null) {
            upper.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
            upper.finalizeSpawn(level, difficulty, reason, null);
            upper.startRiding(this, false, false);
        }
        return data;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0, 1.0, 0.0);
    }

    @Override
    public void tick() {
        if (this.isAlive() && this.getFirstPassenger() instanceof LivingEntity rider && this.getTarget() == null
                && rider instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() != null) {
            this.setTarget(mob.getTarget());
        }
        super.tick();
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.getFirstPassenger() instanceof GoblinKnightUpper upper) {
            return upper.doHurtTarget(level, target);
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Entity attacker = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (attacker != null) {
            float difference = GoblinKnightUpper.facingDifference(this, attacker);
            if (this.getFirstPassenger() instanceof GoblinKnightUpper upper && upper.hasShield() && difference > 150.0F && difference < 230.0F
                    && upper.takeHitOnShield(level, source, amount)) {
                return false;
            }
            if (this.hasArmor() && (difference > 300.0F || difference < 60.0F)) {
                this.breakArmor(level);
            }
        }
        return super.hurtServer(level, source, amount);
    }

    public void breakArmor(ServerLevel level) {
        GoblinKnightUpper.breakEffect(level, this);
        this.setHasArmor(false);
    }

    /** EntityAITFRiderSpearAttack: holds everything (mutex 7) while the rider charges the heavy spear. */
    static final class RiderSpearGoal extends Goal {
        private final GoblinKnightLower knight;

        RiderSpearGoal(GoblinKnightLower knight) {
            this.knight = knight;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.knight.getFirstPassenger() instanceof GoblinKnightUpper upper) {
                return upper.heavySpearTimer > 0 && upper.heavySpearTimer < 50;
            }
            return false;
        }
    }

}
