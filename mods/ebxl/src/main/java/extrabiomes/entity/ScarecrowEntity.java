package extrabiomes.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** EntityScarecrow: stands still, never despawns, cannot be pushed or drown, and watches players and mobs. */
public class ScarecrowEntity extends PathfinderMob {
    public ScarecrowEntity(EntityType<? extends ScarecrowEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 50.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, PathfinderMob.class, 50.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Monster.class, 50.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    protected float getSoundVolume() {
        return 0.0F;
    }
}
