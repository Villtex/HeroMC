package legendgear.entity;

import legendgear.LGEntities;
import legendgear.LGFx;
import legendgear.LGItems;
import legendgear.LGParticles;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * EntityEarthMedallion, EntityWindMedallion, EntityFireMedallion, EntityEnderMedallion in one entity (the kind is a
 * synced byte): a thrown medallion trailing runes that breaks (glass) where it lands into a quake, an arrow storm (on
 * the mob it hit), a firestorm or an ender bomb.
 */
public class MedallionEntity extends LGThrowable implements ItemSupplier {
    public enum Kind { EARTH, WIND, FIRE, ENDER }

    private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(MedallionEntity.class, EntityDataSerializers.BYTE);

    public MedallionEntity(EntityType<? extends MedallionEntity> type, Level level) {
        super(type, level);
    }

    public MedallionEntity(Level level, LivingEntity thrower, Kind kind) {
        super(LGEntities.MEDALLION.get(), level, thrower);
        this.entityData.set(KIND, (byte) kind.ordinal());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, (byte) 0);
    }

    public Kind kind() {
        return Kind.values()[Math.floorMod(this.entityData.get(KIND), Kind.values().length)];
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(switch (this.kind()) {
            case EARTH -> LGItems.EARTH_MEDALLION.get();
            case WIND -> LGItems.WIND_MEDALLION.get();
            case FIRE -> LGItems.FIRE_MEDALLION.get();
            case ENDER -> LGItems.ENDER_MEDALLION.get();
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (LGParticles.client(this.level())) {
            LGParticles.rune(this.level(), this.getX() + this.random.nextGaussian() * 0.1, this.getY() + this.random.nextGaussian() * 0.1,
                    this.getZ() + this.random.nextGaussian() * 0.1, this.random.nextGaussian() * 0.03, this.random.nextGaussian() * 0.03,
                    this.random.nextGaussian() * 0.03, 1.0F);
        }
    }

    @Override
    protected void impact(HitResult hit) {
        if (!this.level().isClientSide()) {
            Entity owner = this.getOwner();
            Level level = this.level();
            switch (this.kind()) {
                case EARTH -> {
                    level.addFreshEntity(new QuakeEntity(level, this.getX(), this.getY(), this.getZ(), owner, false));
                    LGFx.sound(this, SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0F, 0.3F);
                }
                case WIND -> level.addFreshEntity(new ArrowStormEntity(level, this.getX(), this.getY(), this.getZ(),
                        owner instanceof LivingEntity l ? l : null,
                        hit instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity target ? target : null));
                case FIRE -> level.addFreshEntity(new FireblastEntity(level, this.getX(), this.getY(), this.getZ(), owner));
                case ENDER -> level.addFreshEntity(new EnderBombEntity(level, this.getX(), this.getY(), this.getZ()));
            }
            LGFx.sound(this, SoundEvents.GLASS_BREAK, 2.0F, 0.7F);
            this.discard();
        }
    }
}
