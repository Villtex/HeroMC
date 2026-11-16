package harkenscythe.mob;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityHSSpectralEnderman: picks up blocks (never puts them down; its owner takes the block with an empty hand, it
 * drops on death), hurts in water and teleports away, attacks any player but its owner that looks at it (unless it
 * sits) and teleports like an enderman around its target.
 */
public class SpectralEnderman extends SpectralPet {
    private static final EntityDataAccessor<Optional<BlockState>> CARRIED = SynchedEntityData.defineId(SpectralEnderman.class,
            EntityDataSerializers.OPTIONAL_BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> SCREAMING = SynchedEntityData.defineId(SpectralEnderman.class, EntityDataSerializers.BOOLEAN);
    private int teleportDelay;

    public SpectralEnderman(EntityType<? extends SpectralEnderman> type, Level level) {
        super(type, level, 10.0F, new Sounds(null, SoundEvents.ENDERMAN_HURT, SoundEvents.ENDERMAN_DEATH, HSMobs.sound("entity.wolf.step"), 0.4F));
        this.setupGoals();
    }

    @Override
    protected void setupGoals() {
        this.registerHumanoidGoals(true);
        this.registerTargets(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CARRIED, Optional.empty());
        builder.define(SCREAMING, false);
    }

    public @Nullable BlockState getCarried() {
        return this.entityData.get(CARRIED).orElse(null);
    }

    public boolean isScreaming() {
        return this.entityData.get(SCREAMING);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isScreaming() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    public void aiStep() {
        if (this.level() instanceof ServerLevel level) {
            this.serverStep(level);
        } else {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5), this.getRandomY() - 0.25, this.getRandomZ(0.5),
                        (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
            }
        }
        this.jumping = false;
        super.aiStep();
    }

    private void serverStep(ServerLevel level) {
        if (this.isInWaterOrRain()) {
            this.hurtServer(level, this.damageSources().drown(), 1.0F);
        }
        if (level.getGameRules().get(GameRules.MOB_GRIEFING) && this.getCarried() == null && this.random.nextInt(20) == 0 && !this.isOrderedToSit()) {
            BlockPos pos = BlockPos.containing(this.getX() - 2.0 + this.random.nextDouble() * 4.0, this.getY() + this.random.nextDouble() * 3.0,
                    this.getZ() - 2.0 + this.random.nextDouble() * 4.0);
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.ENDERMAN_HOLDABLE)) {
                this.entityData.set(CARRIED, Optional.of(state));
                level.removeBlock(pos, false);
            }
        }
        if (this.isInWaterOrRain()) {
            this.setTarget(null);
            this.entityData.set(SCREAMING, false);
            Teleports.randomly(this);
        }
        if (this.getTarget() == null) {
            Player player = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 64.0, true);
            if (player != null && this.shouldAttack(player)) {
                this.entityData.set(SCREAMING, true);
                this.setTarget(player);
            }
        }
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.lookAt(target, 100.0F, 100.0F);
        }
        if (this.isAlive()) {
            if (target != null) {
                this.entityData.set(SCREAMING, true);
                if (target instanceof Player player && this.shouldAttack(player)) {
                    if (target.distanceToSqr(this) < 16.0) {
                        Teleports.randomly(this);
                    }
                    this.teleportDelay = 0;
                } else if (target.distanceToSqr(this) > 256.0 && this.teleportDelay++ >= 30 && Teleports.towards(this, target)) {
                    this.teleportDelay = 0;
                }
            } else {
                this.entityData.set(SCREAMING, false);
                this.teleportDelay = 0;
            }
        }
    }

    private boolean shouldAttack(Player player) {
        return !this.isOwnedBy(player) && !this.isOrderedToSit() && Teleports.isLookedAt(this, player);
    }

    @Override
    protected @Nullable InteractionResult interactTamed(Player player, InteractionHand hand, ItemStack stack) {
        BlockState carried = this.getCarried();
        if (stack.isEmpty() && carried != null && this.isOwnedBy(player) && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                player.getInventory().add(new ItemStack(carried.getBlock()));
                this.entityData.set(CARRIED, Optional.empty());
            }
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        BlockState carried = this.getCarried();
        if (carried != null) {
            this.spawnAtLocation(level, new ItemStack(carried.getBlock()));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        BlockState carried = this.getCarried();
        if (carried != null) {
            output.store("carriedBlockState", BlockState.CODEC, carried);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(CARRIED, input.read("carriedBlockState", BlockState.CODEC).filter(s -> !s.isAir()));
    }

}
