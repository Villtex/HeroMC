package twilightforest.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;

/**
 * EntityTFNatureBolt (looks like seeds): 2 magic damage and poison (3 s on normal, 7 s on hard). Where it hits grass
 * it bone-meals it if aimed at a player; other solid blocks with hardness 0..50 become birch leaves.
 */
public class NatureBolt extends TFThrowable {
    private @Nullable Player playerTarget;

    public NatureBolt(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public NatureBolt(Level level, LivingEntity owner) {
        super(TFEntities.NATURE_BOLT.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.WHEAT_SEEDS;
    }

    @Override
    protected float gravity() {
        return 0.003F;
    }

    public void setTarget(LivingEntity target) {
        if (target instanceof Player player) {
            this.playerTarget = player;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 5; i++) {
                this.level().addParticle(ParticleTypes.CRIT, this.getX() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()),
                        this.getY() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()),
                        this.getZ() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (result instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living
                && living.hurtServer(level, this.damageSources().indirectMagic(this, this.getOwner()), 2.0F)) {
            int seconds = level.getDifficulty() == Difficulty.PEACEFUL ? 0 : level.getDifficulty() == Difficulty.NORMAL ? 3 : 7;
            if (seconds > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0));
            }
        }
        for (int i = 0; i < 8; i++) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LEAVES.defaultBlockState()), this.getX(), this.getY(), this.getZ(),
                    0, this.random.nextGaussian() * 0.05, this.random.nextDouble() * 0.2, this.random.nextGaussian() * 0.05, 1.0);
        }
        this.discard();
        if (result instanceof BlockHitResult block) {
            BlockPos pos = block.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.GRASS_BLOCK) && this.playerTarget != null) {
                BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, pos);
            } else if (state.isSolid()) {
                float hardness = state.getDestroySpeed(level, pos);
                if (hardness >= 0.0F && hardness < 50.0F) {
                    level.setBlock(pos, Blocks.BIRCH_LEAVES.defaultBlockState(), 3);
                }
            }
        }
    }

}
