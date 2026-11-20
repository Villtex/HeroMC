package legendgear.entity;

import legendgear.LGEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** EntityWhirlwind (wind augment of a sword): flies straight for 30 ticks and carries every mob it touches along. */
public class WhirlwindEntity extends LGThrowable {
    public WhirlwindEntity(EntityType<? extends WhirlwindEntity> type, Level level) {
        super(type, level);
    }

    public WhirlwindEntity(Level level, LivingEntity thrower) {
        super(LGEntities.WHIRLWIND.get(), level);
        this.setOwner(thrower);
        float yaw = thrower.getYRot();
        float pitch = thrower.getXRot();
        this.snapTo(thrower.getX(), thrower.getEyeY(), thrower.getZ(), yaw, pitch);
        float f = 0.4F;
        this.heading(-Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f,
                -Mth.sin(pitch / 180.0F * (float) Math.PI) * f,
                Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * f, 1.5F, 0.0F);
    }

    @Override
    protected float gravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        Vec3 m = this.getDeltaMovement();
        if (this.level().isClientSide() && m.lengthSqr() > 0) {
            Vec3 forward = m.normalize();
            Vec3 right = forward.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 up = right.cross(forward).normalize();
            double theta = this.tickCount * -0.4;
            for (int i = 0; i < 3; i++) {
                Vec3 out = right.scale(Math.cos(theta)).add(up.scale(Math.sin(theta)));
                if (!this.isInWater()) {
                    this.level().addParticle(ParticleTypes.CLOUD, this.getX() + out.x, this.getY() + out.y, this.getZ() + out.z, out.x * 0.3, out.y * 0.3,
                            out.z * 0.3);
                    this.level().addParticle(ParticleTypes.POOF, this.getX() + out.x * 0.5, this.getY() + out.y * 0.5, this.getZ() + out.z * 0.5,
                            out.x * 0.3, out.y * 0.3, out.z * 0.3);
                } else {
                    this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + out.x * 0.5, this.getY() + out.y * 0.5, this.getZ() + out.z * 0.5,
                            out.x * 0.3, out.y * 0.3, out.z * 0.3);
                }
                theta += Math.PI * 2.0 / 3.0;
            }
        }
        Entity owner = this.getOwner();
        for (Entity ent : this.level().getEntities(this, this.getBoundingBox().inflate(1.0))) {
            if (ent != owner && ent instanceof LivingEntity) {
                ent.setDeltaMovement(m);
                ent.needsSync = true;
            }
        }
        if (this.tickCount > 30 && !this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void impact(HitResult hit) {
        if (block(hit) != null && !this.level().isClientSide()) {
            this.discard();
        }
    }
}
