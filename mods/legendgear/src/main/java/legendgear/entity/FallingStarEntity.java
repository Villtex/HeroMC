package legendgear.entity;

import legendgear.LGConfig;
import legendgear.LGEntities;
import legendgear.LGFx;
import legendgear.LGItems;
import legendgear.LGParticles;
import legendgear.LGSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EntityFallingStar: falls from 150 above a spot 48 blocks from a player at night (StarfallHandler), explodes without
 * breaking blocks where it lands and glows there for 440 ticks. A player who touches it gets a star piece and 15
 * experience; left alone it dwindles into one stardust and 5 experience.
 */
public class FallingStarEntity extends Entity {
    private static final EntityDataAccessor<Boolean> IMPACT = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DWINDLE = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.INT);

    public FallingStarEntity(EntityType<? extends FallingStarEntity> type, Level level) {
        super(type, level);
    }

    public FallingStarEntity(Player player) {
        this(LGEntities.FALLING_STAR.get(), player.level());
        double theta = this.random.nextDouble() * Math.PI * 2.0;
        double r = 48.0;
        this.setPos(player.getX() + Math.cos(theta) * r, player.getY() + 150.0, player.getZ() + Math.sin(theta) * r);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IMPACT, false);
        builder.define(DWINDLE, LGConfig.FALLEN_STAR_LIFETIME);
    }

    public boolean impact() {
        return this.entityData.get(IMPACT);
    }

    public int dwindle() {
        return this.entityData.get(DWINDLE);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level() instanceof ServerLevel server && player.getInventory().add(new ItemStack(LGItems.STAR_PIECE.get()))) {
            LGFx.sound(this, SoundEvents.ITEM_PICKUP, 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            LGFx.sound(player, LGSounds.STARCAUGHT.get(), 2.0F, 1.0F);
            for (int i = 0; i < 15; i++) {
                server.addFreshEntity(new ExperienceOrb(server, this.getX(), this.getY(), this.getZ(), 1));
            }
            player.take(this, 1);
            this.discard();
        }
    }

    @Override
    public void tick() {
        // 26.3 lets only the server move an entity it tracks; the client follows its position packets and just
        // reads the (synchronized) motion for the sparkle trail below
        if (!this.level().isClientSide()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.05, 0.0));
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
        if (this.tickCount == 2 && !this.impact()) {
            LGFx.sound(this, LGSounds.STARFALL.get(), 100.0F, 1.0F);
        }
        if ((this.horizontalCollision || this.verticalCollision) && !this.impact()) {
            if (this.level() instanceof ServerLevel server) {
                server.explode(this, this.getX(), this.getY(), this.getZ(), 5.0F, Level.ExplosionInteraction.NONE);
                this.entityData.set(IMPACT, true);
            }
        }
        int dwindle = this.dwindle();
        if (LGParticles.client(this.level())) {
            var m = this.getDeltaMovement();
            LGParticles.sparkle(this.level(), this.getX() - m.x, this.getY() - m.y, this.getZ() - m.z, this.random.nextGaussian() * 0.2,
                    this.random.nextGaussian() * 0.2, this.random.nextGaussian() * 0.2, 4.0F * dwindle / LGConfig.FALLEN_STAR_LIFETIME);
        } else if (this.impact() && dwindle % 25 == 0) {
            LGFx.sound(this, LGSounds.TWINKLE.get(), 2.0F, 1.0F);
        }
        if (this.impact() && this.level() instanceof ServerLevel server) {
            this.entityData.set(DWINDLE, --dwindle);
            if (dwindle <= 0) {
                server.addFreshEntity(new ItemEntity(server, this.getX(), this.getY(), this.getZ(), new ItemStack(LGItems.STARDUST.get())));
                for (int i = 0; i < 5; i++) {
                    server.addFreshEntity(new ExperienceOrb(server, this.getX(), this.getY(), this.getZ(), 1));
                }
                this.discard();
            }
        }
        super.tick();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(IMPACT, input.getBooleanOr("impact", false));
        this.entityData.set(DWINDLE, input.getIntOr("dwindle", LGConfig.FALLEN_STAR_LIFETIME));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putBoolean("impact", this.impact());
        output.putInt("dwindle", this.dwindle());
    }
}
