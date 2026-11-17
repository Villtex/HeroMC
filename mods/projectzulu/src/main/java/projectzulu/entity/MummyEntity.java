package projectzulu.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import projectzulu.ZuluMonsters;

/**
 * EntityMummy: an undead desert monster, always hostile (anger refreshed to 100 every AI tick), that climbs walls like
 * a spider (besideClimbableBlock while it bumps into one) and walks through cobwebs. Health 16, speed 0.25, damage 5
 * (3/4/5/6), 0.6 x 1.8. Hurt sound: the short mummy roar of SoundHookContainerClass. The pharaoh summons them.
 */
public class MummyEntity extends ZuluMonster {
    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(MummyEntity.class, EntityDataSerializers.BOOLEAN);

    public MummyEntity(EntityType<? extends MummyEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZuluMonster.createZuluAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0, 80));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new StateTargetGoal<>(this, Player.class, ANGRY));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
    }

    @Override
    protected void updateAITick(ServerLevel level) {
        this.angerLevel = 100;
        this.entityData.set(CLIMBING, this.horizontalCollision);
    }

    @Override
    public boolean onClimbable() {
        return this.entityData.get(CLIMBING);
    }

    @Override
    protected boolean ignoresCobwebs() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluMonsters.MUMMY_SHORT_ROAR.get();
    }
}
