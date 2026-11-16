package projectzulu.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import projectzulu.ZuluSounds;

import java.util.EnumSet;

/**
 * EntityVulture: always in the air (max flight height 20). A player within 16 blocks (horizontally) makes it curious
 * for 140 ticks: it follows the player, circling 10 blocks above him (EntityAIVultureFollow). When the player is weak
 * (health below "Max Target Health To Attack" = 20 of the pack, and below 4 per vulture following or attacking within
 * 10 blocks) the vultures get angry and dive at him. Fights back when hit. Health 14, damage 5, speed 0.18,
 * 1.0 x 1.4. Unlike the other birds it keeps updateFallState (no fall damage, though).
 */
public class VultureEntity extends AerialAnimal {
    /** "Max Target Health To Attack" in ProjectZuluMobBiomeConfig.cfg of the pack. */
    private static final int MAX_TARGET_HEALTH_TO_ATTACK = 20;
    private static final int TICKS_TO_CHECK_ABILITIES = 3;

    private float curiosity;

    public VultureEntity(EntityType<? extends VultureEntity> type, Level level) {
        super(type, level);
        this.maxFlightHeight = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AerialAnimal.createAerialAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, AerialGoals.Chase.vultureFollow(this, EnumSet.of(EntityState.FOLLOWING)));
        this.goalSelector.addGoal(3, AerialGoals.Chase.attackOnCollide(this));
        this.goalSelector.addGoal(6, new AerialGoals.FlyingWander(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new AerialGoals.StateTarget(this,
                EnumSet.of(EntityState.ATTACKING, EntityState.LOOKING, EntityState.FOLLOWING)));
    }

    @Override
    protected boolean keepsFallState() {
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % TICKS_TO_CHECK_ABILITIES != 0) {
            return;
        }
        // getClosestVulnerablePlayerToEntity(this, 100)
        Player player = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 100.0, true);
        if (player != null) {
            int distXZ = (int) Math.sqrt(Math.pow(player.getX() - this.getX(), 2.0) + Math.pow(player.getZ() - this.getZ(), 2.0));
            if (distXZ < 16) {
                this.curiosity = 140.0F;
            }
        }
        this.shouldFollow = this.curiosity > 0.0F;
        this.curiosity = Math.max(this.curiosity - TICKS_TO_CHECK_ABILITIES, 0.0F);
        if (this.curiosity > 0.0F && player != null) {
            int nearbyVultures = 0;
            for (VultureEntity vulture : level.getEntitiesOfClass(VultureEntity.class, this.getBoundingBox().inflate(10.0))) {
                EntityState state = vulture.getEntityState();
                if (state == EntityState.FOLLOWING || state == EntityState.ATTACKING) {
                    nearbyVultures++;
                }
            }
            if (player.getHealth() < MAX_TARGET_HEALTH_TO_ATTACK && player.getHealth() < nearbyVultures * 4) {
                this.angerLevel = 400;
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZuluSounds.VULTURE_HURT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluSounds.VULTURE_HURT.get();
    }
}
