package twilightforest.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import twilightforest.entity.ai.TFGoals;
import twilightforest.entity.projectile.SlimeBlob;

/**
 * EntityTFSlimeBeetle: 25 health, 0.9 x 1.75, speed 0.23; keeps 2 blocks from players and spits slime blobs every 30
 * ticks (EntityAITFMagicAttack type 4). Drops slimeballs.
 */
public class SlimeBeetle extends TFBeetle {
    public SlimeBeetle(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.MOVEMENT_SPEED, 0.23);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 2.0F, 1.0, 0.4 / 0.23));
        this.goalSelector.addGoal(3, new TFGoals.MagicAttack(this, 1.0, 30, 1.0F, this::spit));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** EntityAITFMagicAttack.doRangedAttack with the slime projectile. */
    private void spit(LivingEntity target) {
        SlimeBlob blob = new SlimeBlob(this.level(), this);
        this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
        double tx = target.getX() - this.getX();
        double ty = target.getY() + target.getEyeHeight() - 1.1F - blob.getY();
        double tz = target.getZ() - this.getZ();
        float heightOffset = Mth.sqrt((float) (tx * tx + tz * tz)) * 0.2F;
        blob.shootOld(tx, ty + heightOffset, tz, 0.6F, 6.0F);
        this.level().addFreshEntity(blob);
    }
}
