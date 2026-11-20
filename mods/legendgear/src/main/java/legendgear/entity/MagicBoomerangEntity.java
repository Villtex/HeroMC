package legendgear.entity;

import legendgear.LGConfig;
import legendgear.LGEntities;
import legendgear.LGFx;
import legendgear.LGItems;
import legendgear.LGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * EntityMagicBoomerang: flies 10 ticks out, then curves ever tighter back to the thrower's eyes at 1.5 and goes back into
 * the slot it was thrown from. On the way it hits mobs (6 damage, one use), cuts plants and glass of hardness 0 (one use),
 * turns back from solid blocks and brings items and fallen stars along. After 110 ticks it drops where it is.
 */
public class MagicBoomerangEntity extends LGThrowable implements ItemSupplier {
    public static final int MAX_THROW_TIME = 10;
    public static final float BOOMERANG_SPEED = 1.5F;
    public int returnTime = MAX_THROW_TIME;
    public ItemStack boomerangItem = ItemStack.EMPTY;
    public int thrownFromSlot = -1;
    private boolean turned;

    public MagicBoomerangEntity(EntityType<? extends MagicBoomerangEntity> type, Level level) {
        super(type, level);
    }

    public MagicBoomerangEntity(Level level, LivingEntity thrower, ItemStack thrown) {
        super(LGEntities.MAGIC_BOOMERANG.get(), level, thrower);
        this.boomerangItem = thrown;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(LGItems.MAGIC_BOOMERANG.get());
    }

    @Override
    protected float gravity() {
        return 0.0F;
    }

    private @Nullable LivingEntity owner() {
        return this.getOwner() instanceof LivingEntity l ? l : null;
    }

    private void wear() {
        LivingEntity owner = this.owner();
        if (!this.boomerangItem.isEmpty() && owner != null) {
            LGFx.damage(this.boomerangItem, 1, owner);
            if (this.boomerangItem.isEmpty()) {
                LGFx.sound(this, SoundEvents.ITEM_BREAK.value(), 1.0F, 0.8F);
                this.discard();
            }
        }
    }

    @Override
    protected void impact(HitResult hit) {
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        if (hit instanceof EntityHitResult eh) {
            if (!(eh.getEntity() instanceof LivingEntity target)) {
                return;
            }
            if (target == this.getOwner() && this.returnTime <= 0 && target instanceof Player player) {
                this.giveBack(player);
                return;
            }
            if (target != this.getOwner()) {
                this.wear();
                target.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), LGConfig.BOOMERANG_DAMAGE);
            }
            return;
        }
        BlockHitResult bh = block(hit);
        if (bh == null) {
            return;
        }
        BlockPos pos = bh.getBlockPos();
        BlockState state = level.getBlockState(pos);
        boolean solid = !state.getCollisionShape(level, pos).isEmpty();
        boolean cuttable = state.getDestroySpeed(level, pos) == 0.0F
                && (state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS) || state.is(net.minecraft.world.level.block.Blocks.GLASS) || state.getSoundType() == net.minecraft.world.level.block.SoundType.GLASS);
        if (cuttable) {
            level.destroyBlock(pos, true, this);
            this.wear();
        } else if (!this.turned && solid) {
            this.returnTime = Math.min(this.returnTime, 0);
            this.turned = true;
            this.setDeltaMovement(this.getDeltaMovement().scale(-1.0));
        }
    }

    private void giveBack(Player player) {
        int slot = this.thrownFromSlot;
        if (slot >= 0) {
            ItemStack crowded = player.getInventory().getItem(slot);
            player.getInventory().setItem(slot, this.boomerangItem);
            if (!crowded.isEmpty() && !player.getInventory().add(crowded)) {
                player.drop(crowded, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        } else if (!player.getInventory().add(this.boomerangItem)) {
            player.drop(this.boomerangItem, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        for (Entity e : this.getPassengers()) {
            e.stopRiding();
        }
        player.take(this, 1);
        this.discard();
    }

    private static float turn(float from, float to, float max) {
        float d = (to - from) % (float) (Math.PI * 2);
        if (d >= Math.PI) {
            d -= (float) (Math.PI * 2);
        }
        if (d < -Math.PI) {
            d += (float) (Math.PI * 2);
        }
        return from + Math.max(-max, Math.min(max, d));
    }

    @Override
    public void tick() {
        LivingEntity thrower = this.owner();
        if (thrower != null && this.returnTime % 3 == 0) {
            LGFx.sound(this, LGSounds.BOOMERANG.get(), 2.0F, 1.0F + 1.0F / (1.0F + this.distanceTo(thrower) / 16.0F));
        }
        var m = this.getDeltaMovement();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 4; i++) {
                this.level().addParticle(ParticleTypes.CRIT, this.getX() + m.x * i / 4.0, this.getY() + m.y * i / 4.0, this.getZ() + m.z * i / 4.0,
                        -m.x, -m.y + 0.2, -m.z);
            }
        }
        if (!this.level().isClientSide() && this.getPassengers().isEmpty()) {
            for (Entity e : this.level().getEntities(this, this.getBoundingBox().inflate(1.0))) {
                if (e instanceof ItemEntity || e instanceof FallingStarEntity) {
                    e.startRiding(this, true, true);
                    break;
                }
            }
        }
        if (--this.returnTime <= 0 && thrower != null) {
            float heading = (float) Math.atan2(m.z, m.x);
            float toThrower = (float) Math.atan2(thrower.getZ() - this.getZ(), thrower.getX() - this.getX());
            float curve = -this.returnTime * 0.007F;
            float newHeading = turn(heading, toThrower, curve);
            double pitch = Math.atan2(m.y, Math.sqrt(m.x * m.x + m.z * m.z));
            double targetPitch = Math.atan2(thrower.getEyeY() - this.getY(),
                    Math.sqrt((thrower.getX() - this.getX()) * (thrower.getX() - this.getX()) + (thrower.getZ() - this.getZ()) * (thrower.getZ() - this.getZ())));
            float newPitch = turn((float) pitch, (float) targetPitch, curve * 0.3F);
            this.heading(Math.cos(newHeading) * Math.cos(newPitch), Math.sin(newPitch), Math.sin(newHeading) * Math.cos(newPitch), BOOMERANG_SPEED, 0.0F);
        }
        if (this.returnTime < -100 && !this.level().isClientSide()) {
            if (!this.boomerangItem.isEmpty()) {
                this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.boomerangItem));
            }
            this.discard();
            return;
        }
        super.tick();
    }

    @Override
    public boolean shouldBeSaved() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.boomerangItem = input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.returnTime = input.getIntOr("returnTime", MAX_THROW_TIME);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (!this.boomerangItem.isEmpty()) {
            output.store("Item", ItemStack.CODEC, this.boomerangItem);
        }
        output.putInt("returnTime", this.returnTime);
    }
}
