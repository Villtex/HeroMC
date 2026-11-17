package projectzulu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.cow.CowSoundVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * EntityMinotaur, the labyrinth guard of Teil 11: always hostile (anger refreshed to 100 every AI tick), swings its
 * axe overhead (attack animation over animTime), walks through cobwebs. Health 30, armour 6, speed 0.25, damage 4
 * (getAttackStrength 3/3/4/6), attack reach 2.3 blocks (attackDistanceSq 5.29), 1.0 x 2.4. Steps like a cow at volume
 * 0.15. The "charge" timer of the original never changed the speed (getSpeedModifier returned the unmodified value),
 * so it is not ported.
 */
public class MinotaurEntity extends ZuluMonster {
    public MinotaurEntity(EntityType<? extends MinotaurEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZuluMonster.createZuluAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new StayStillGoal(this, State.IDLE));
        this.goalSelector.addGoal(3, new ReachAttackGoal(this, false, 5.29));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0, 120));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new StateTargetGoal<>(this, Player.class, ANGRY));
    }

    @Override
    protected void updateAITick(ServerLevel level) {
        this.angerLevel = 100;
    }

    @Override
    protected boolean ignoresCobwebs() {
        return true;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.COW_SOUNDS.get(CowSoundVariants.SoundSet.CLASSIC).stepSound().value(), 0.15F, 1.0F);
    }
}
