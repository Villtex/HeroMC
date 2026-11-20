package tconstruct.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import tconstruct.TItems;
import tconstruct.tools.ToolCombat;
import tconstruct.tools.ToolCoreItem;
import tconstruct.tools.ToolType;

/**
 * DaggerEntity (RotatingBase): a thrown dagger, speed 0.75, gravity 0.03 (water 0.8). A hit does 4 damage from the
 * thrower, then the dagger's own hit (AbilityHelper.onLeftClickEntity) and it bounces back; in the ground it wears by 1
 * and waits to be picked up (not in creative). Spinning while it flies is the renderer's.
 */
public class ThrownDagger extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.ITEM_STACK);
    private boolean hitGround;

    public ThrownDagger(EntityType<? extends ThrownDagger> type, Level level) {
        super(type, level);
    }

    public ThrownDagger(Level level, Player thrower, ItemStack dagger) {
        super(TEntities.DAGGER.get(), thrower.getX(), thrower.getEyeY() - 0.1F, thrower.getZ(), level, dagger.copy(), null);
        this.setOwner(thrower);
        this.entityData.set(DATA_ITEM, dagger.copy());
        this.pickup = thrower.getAbilities().instabuild ? Pickup.CREATIVE_ONLY : Pickup.ALLOWED;
        TinkerArrow.aim(this, thrower, 0.75F, 0.8F);
        this.setBaseDamage(4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getDagger() {
        return this.entityData.get(DATA_ITEM);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    protected float getWaterInertia() {
        return 0.8F;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ARROW_HIT;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(TItems.tool(ToolType.DAGGER));
    }

    /** DaggerEntity.onHit (an entity): 4 damage as the thrower's mob attack, bounce back, then the dagger's own hit. */
    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        if (!(this.level() instanceof ServerLevel level) || !(this.getOwner() instanceof LivingEntity owner)) {
            return;
        }
        if (target.hurtServer(level, level.damageSources().mobAttack(owner), 4.0F)) {
            this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
            ItemStack dagger = this.getPickupItemStackOrigin();
            if (target instanceof LivingEntity && dagger.getItem() instanceof ToolCoreItem tool) {
                ToolCombat.attack(dagger, owner, target, tool, 0);
                this.setPickupItemStack(dagger);
                this.entityData.set(DATA_ITEM, dagger.copy());
            }
        }
    }

    /** In the ground: the dagger wears by one (the first time). */
    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!this.hitGround && this.level() instanceof ServerLevel && this.getOwner() instanceof LivingEntity owner) {
            this.hitGround = true;
            ItemStack dagger = this.getPickupItemStackOrigin();
            ToolCoreItem.damageTool(dagger, 1, owner);
            this.setPickupItemStack(dagger);
            this.entityData.set(DATA_ITEM, dagger.copy());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_ITEM, this.getPickupItemStackOrigin().copy());
    }
}
