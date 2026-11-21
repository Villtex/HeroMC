package twilightforest.entity.projectile;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import twilightforest.TFBlocks;
import twilightforest.TFEntities;
import twilightforest.item.MoonwormQueenItem;

/**
 * EntityTFMoonwormShot (drawn as a glowing moonworm, RenderTFMoonwormShot): gravity 0.03; on a block it places a
 * moonworm as the queen does, an entity takes 0 thrown damage; moonworm crumbs.
 */
public class MoonwormShot extends TFThrowable {
    public MoonwormShot(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public MoonwormShot(Level level, LivingEntity owner) {
        super(TFEntities.MOONWORM_SHOT.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return TFBlocks.MOONWORM.get().asItem();
    }

    @Override
    protected float gravity() {
        return 0.03F;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (result instanceof BlockHitResult block) {
            MoonwormQueenItem.place(level, this.getOwner() instanceof Player player ? player : null, block);
        }
        if (result instanceof EntityHitResult hit) {
            hit.getEntity().hurtServer(level, this.damageSources().thrown(this, this.getOwner()), 0.0F);
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, TFBlocks.MOONWORM.get().defaultBlockState()), this.getX(), this.getY(),
                this.getZ(), 8, 0.0, 0.0, 0.0, 0.0);
        this.discard();
    }
}
