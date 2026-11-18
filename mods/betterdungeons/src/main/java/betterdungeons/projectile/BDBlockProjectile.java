package betterdungeons.projectile;

import betterdungeons.BDEntities;
import betterdungeons.BDFx;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * EntityBallBlock ("BlockProjectile"): a block thrown by the Monking boss. Flies from the thrower's feet with the given
 * motion, falls (its type stays 0, so the water grenade's 0.1 per tick pull acts on it as in 1.5.2), hurts what it hits
 * by 4 or sets its block next to the block it hits.
 *
 * <p>Firing: {@code level.addFreshEntity(BDBlockProjectile.create(boss, mx, my, mz, Blocks.DIRT.defaultBlockState()))}.
 */
public class BDBlockProjectile extends BDProjectile {
    private static final EntityDataAccessor<BlockState> DATA_BLOCK = SynchedEntityData.defineId(BDBlockProjectile.class, EntityDataSerializers.BLOCK_STATE);

    public BDBlockProjectile(EntityType<? extends BDBlockProjectile> type, Level level) {
        super(type, level);
    }

    /** EntityBallBlock(world, thrower, mx, my, mz, block). */
    public static BDBlockProjectile create(LivingEntity thrower, double mx, double my, double mz, BlockState block) {
        BDBlockProjectile ball = new BDBlockProjectile(BDEntities.BLOCK_PROJECTILE.get(), thrower.level());
        ball.setOwner(thrower);
        ball.setBlock(block);
        ball.snapTo(thrower.getX(), thrower.getY(), thrower.getZ(), thrower.getYRot(), thrower.getXRot());
        ball.setDeltaMovement(mx, my, mz);
        ball.accelerationX = 0.01;
        ball.accelerationY = -0.1;
        ball.accelerationZ = 0.01;
        return ball;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BLOCK, Blocks.STONE.defaultBlockState());
    }

    public void setBlock(BlockState block) {
        this.entityData.set(DATA_BLOCK, block);
    }

    public BlockState getBlock() {
        return this.entityData.get(DATA_BLOCK);
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (this.level().isClientSide()) {
            return;
        }
        if (hit instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof LivingEntity target) {
                LivingEntity thrower = this.shooter();
                BDFx.hurt(target, thrower != null ? BDFx.direct(thrower) : this.damageSources().generic(), 4);
            }
        } else if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
            if (this.level().isEmptyBlock(pos)) {
                this.level().setBlock(pos, this.getBlock(), 3);
            }
        }
        this.discard();
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
