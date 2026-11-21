package twilightforest.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;


/**
 * EntityTFCharmEffect: the charm that saved a player circles 1.75 blocks around his eyes for 10 seconds (two of them,
 * half a turn apart), shedding crumbs of itself. Not saved (1.5.2 swapped reading and writing of its NBT).
 */
public class CharmEffect extends Entity implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(CharmEffect.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_OWNER = SynchedEntityData.defineId(CharmEffect.class, EntityDataSerializers.INT);
    private @Nullable LivingEntity orbiting;
    public float offset;

    public CharmEffect(EntityType<? extends CharmEffect> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public CharmEffect(Level level, LivingEntity owner, ItemStack charm, float offset) {
        this(TFEntities.CHARM_EFFECT.get(), level);
        this.orbiting = owner;
        this.offset = offset;
        this.entityData.set(DATA_OWNER, owner.getId());
        this.entityData.set(DATA_ITEM, charm.copyWithCount(1));
        this.snapTo(owner.getX() + 1.75, owner.getEyeY(), owner.getZ(), owner.getYRot(), owner.getXRot());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_OWNER, -1);
    }

    @Override
    public ItemStack getItem() {
        return this.entityData.get(DATA_ITEM);
    }

    @Override
    public void tick() {
        super.tick();
        float rotation = this.tickCount / 5.0F + this.offset;
        if (this.orbiting == null) {
            this.orbiting = this.level().getEntity(this.entityData.get(DATA_OWNER)) instanceof LivingEntity living ? living : null;
        }
        if (this.orbiting != null && !this.level().isClientSide()) {
            Vec3 look = new Vec3(1.75, 0.0, 0.0).yRot(rotation);
            this.setPos(this.orbiting.getX() + look.x, this.orbiting.getEyeY(), this.orbiting.getZ() + look.z);
            this.setRot(this.orbiting.getYRot(), this.orbiting.getXRot());
        }
        if (this.level().isClientSide() && !this.getItem().isEmpty()) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(this.getItem())),
                        this.getX() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()),
                        this.getY() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()),
                        this.getZ() + 0.5 * (this.random.nextDouble() - this.random.nextDouble()), 0.0, 0.2, 0.0);
            }
        }
        if (!this.level().isClientSide() && (this.tickCount > 200 || this.orbiting == null || !this.orbiting.isAlive())) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
