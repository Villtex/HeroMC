package twilightforest.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.TFEntities;

/**
 * EntityTFBlockGoblin (block and chain goblin): 20 health, 11 armour, 0.9 x 1.4, speed 0.28. It whirls a spiked
 * block on a chain around itself (16° a tick; length 0.9 while it has a target and no recoil, else 0.3); whatever the
 * block touches is hit (strength 2) and thrown up 0.4, then the chain rests 40 ticks. Its own melee only swings the
 * arm. Runs from primed TNT. Redcap sounds. Drops coal. The block and the chain links (EntityTFSpikeBlock,
 * EntityTFGoblinChain) are only drawn; they are not entities here.
 */
public class BlockGoblin extends TFMonster {
    private static final EntityDataAccessor<Byte> DATA_CHAIN_LENGTH = SynchedEntityData.defineId(BlockGoblin.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_CHAIN_POS = SynchedEntityData.defineId(BlockGoblin.class, EntityDataSerializers.BYTE);
    private int recoilCounter;
    private float chainAngle;

    public BlockGoblin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.ARMOR, 11.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new twilightforest.entity.ai.RedcapGoals.AvoidTnt(this, 2.0F, 1.0, 0.5 / 0.28));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHAIN_LENGTH, (byte) 0);
        builder.define(DATA_CHAIN_POS, (byte) 0);
    }

    public double chainYOffset() {
        return 1.5 - this.getChainLength() / 4.0;
    }

    /** getChainPosition: on the circle of the chain angle and length, at the chain height. */
    public Vec3 chainPosition(float angle, float distance) {
        double dx = Math.cos(angle * Math.PI / 180.0) * distance;
        double dz = Math.sin(angle * Math.PI / 180.0) * distance;
        return new Vec3(this.getX() + dx, this.getY() + this.chainYOffset(), this.getZ() + dz);
    }

    public boolean isSwingingChain() {
        return this.isSwinging() || this.getTarget() != null && this.recoilCounter == 0;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.recoilCounter > 0) {
            this.recoilCounter--;
        }
        this.chainAngle += 16.0F;
        this.chainAngle %= 360.0F;
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_CHAIN_LENGTH, (byte) Math.floor(this.getChainLength() * 127.0F));
            this.entityData.set(DATA_CHAIN_POS, (byte) Math.floor(this.getChainAngle() / 360.0F * 255.0F));
        } else if (Math.abs(this.chainAngle - this.getChainAngle()) > 32.0F) {
            this.chainAngle = this.getChainAngle();
        }
        if (this.level() instanceof ServerLevel level && this.isSwingingChain()) {
            Vec3 block = this.chainPosition(this.getChainAngle(), this.getChainLength());
            AABB box = new AABB(block.x - 0.375, block.y, block.z - 0.375, block.x + 0.375, block.y + 0.75, block.z + 0.375).inflate(0.2, 0.0, 0.2);
            for (Entity entity : level.getEntities(this, box)) {
                if (entity.isPushable() && entity instanceof LivingEntity living) {
                    if (oldAttack(this, level, living, this.attackStrength(living))) {
                        living.setDeltaMovement(living.getDeltaMovement().add(0.0, 0.4, 0.0));
                        living.needsSync = true;
                        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
                        this.recoilCounter = 40;
                    }
                }
            }
        }
    }

    public float getChainAngle() {
        return !this.level().isClientSide() ? this.chainAngle : (this.entityData.get(DATA_CHAIN_POS) & 0xFF) / 255.0F * 360.0F;
    }

    /** The client's smooth angle (it runs on between the updates). */
    public float clientChainAngle() {
        return this.chainAngle;
    }

    public float getChainLength() {
        if (!this.level().isClientSide()) {
            return this.isSwingingChain() ? 0.9F : 0.3F;
        }
        return (this.entityData.get(DATA_CHAIN_LENGTH) & 0xFF) / 127.0F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFEntities.REDCAP_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFEntities.REDCAP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFEntities.REDCAP_DIE.get();
    }
}
