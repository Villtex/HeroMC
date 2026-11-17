package projectzulu.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluMonsters;

/**
 * EntityMimic: a chest on legs. While idle it sits there as a closed chest (legs folded in, no movement, cannot be
 * pushed). Opening it (right click) or hitting it wakes it (anger 400); while awake a player within 5 blocks keeps it
 * angry. Then it runs on four legs and bites with its lid. Health 20, armour 6, speed 0.4, damage 5 (3/4/5/6),
 * 1.0 x 1.5. Never despawns. Hurt sound: mimicliving (SoundHookContainerClass). Structures (Teil 11) can place it
 * hovering: {@link #setHovering()} keeps it in place for the first 200 ticks.
 */
public class MimicEntity extends ZuluMonster {
    private boolean shouldHover;

    public MimicEntity(EntityType<? extends MimicEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZuluMonster.createZuluAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new StayStillGoal(this, State.IDLE));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new StateTargetGoal<>(this, Player.class, ANGRY));
    }

    public void setHovering() {
        this.shouldHover = true;
    }

    @Override
    public void aiStep() {
        if (this.shouldHover) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            if (this.tickCount > 200) {
                this.shouldHover = false;
            }
        }
        super.aiStep();
        if (!this.level().isClientSide() && this.getState() != State.IDLE && this.level().getNearestPlayer(this, 5.0) != null) {
            this.angerLevel = 400;
        }
    }

    @Override
    public boolean isNoGravity() {
        return this.shouldHover || super.isNoGravity();
    }

    @Override
    public boolean isPushable() {
        return this.getState() != State.IDLE && super.isPushable();
    }

    /** canDespawn() returned false. */
    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    /** interact: opening the "chest" counts as an attack. */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            this.attackedByPlayer(player);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluMonsters.MIMIC_LIVING.get();
    }
}
