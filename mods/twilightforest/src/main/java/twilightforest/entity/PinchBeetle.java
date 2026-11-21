package twilightforest.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.ai.TFGoals;

/**
 * EntityTFPinchBeetle: 40 health, 2 armour, 1.2 x 1.1 (1.9 x 2.0 with a passenger), speed 0.23, pinch 8. It grabs
 * what it bites (rides it in its jaws 0.9 blocks ahead), runs off with it (EntityAITFKidnapRider) and charges.
 */
public class PinchBeetle extends TFBeetle {
    private static final EntityDimensions CARRYING = EntityDimensions.scalable(1.9F, 2.0F);

    public PinchBeetle(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TFGoals.KidnapRider(this, 0.4 / 0.23));
        this.goalSelector.addGoal(2, new TFGoals.ChargeAttack(this, 0.4 / 0.23));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int attackStrength(Entity target) {
        return 8;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isVehicle() ? CARRYING : super.getDefaultDimensions(pose);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        this.refreshDimensions();
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.refreshDimensions();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Entity rider = this.getFirstPassenger();
        if (rider != null) {
            this.getLookControl().setLookAt(rider, 100.0F, 100.0F);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (!this.isVehicle() && !target.isPassenger()) {
            target.startRiding(this, true, true);
        }
        return super.doHurtTarget(level, target);
    }

    /** getRiderPosition: 0.9 blocks in front, 0.75 up. */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        float distance = 0.9F;
        double dx = Math.cos((this.getYRot() + 90.0F) * Math.PI / 180.0) * distance;
        double dz = Math.sin((this.getYRot() + 90.0F) * Math.PI / 180.0) * distance;
        return new Vec3(dx, 0.75, dz);
    }
}
