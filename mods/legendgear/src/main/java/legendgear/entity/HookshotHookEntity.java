package legendgear.entity;

import legendgear.LGEntities;
import legendgear.LGFx;
import legendgear.LGItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityShotHook: flies 15 ticks without gravity, then back to its shooter. Hitting wood on the way it anchors (synced
 * byte 24 of 1.5.2) and pulls the shooter towards it at 1 block a tick, without fall damage, until it reaches it. It
 * goes away when the shooter stops using the hookshot; the ExtraHookshotBlocks list of the pack is empty.
 */
public class HookshotHookEntity extends LGThrowable implements ItemSupplier {
    private static final EntityDataAccessor<Boolean> ANCHORED = SynchedEntityData.defineId(HookshotHookEntity.class, EntityDataSerializers.BOOLEAN);
    public int travelTime = 15;

    public HookshotHookEntity(EntityType<? extends HookshotHookEntity> type, Level level) {
        super(type, level);
    }

    public HookshotHookEntity(Level level, Player shooter) {
        super(LGEntities.HOOKSHOT_HOOK.get(), level, shooter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ANCHORED, false);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(LGItems.HOOKSHOT.get());
    }

    public boolean anchored() {
        return this.entityData.get(ANCHORED);
    }

    public @Nullable Player shooter() {
        return this.getOwner() instanceof Player p ? p : null;
    }

    @Override
    protected float gravity() {
        return 0.0F;
    }

    private boolean isValidMaterial(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        return state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.WOODEN_FENCES) || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_STAIRS) || state.is(BlockTags.WOODEN_DOORS) || state.is(BlockTags.WOODEN_TRAPDOORS)
                || state.getSoundType() == net.minecraft.world.level.block.SoundType.WOOD;
    }

    private static void stop(Player player) {
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
    }

    @Override
    public void remove(RemovalReason reason) {
        Player shooter = this.shooter();
        if (shooter != null && this.anchored()) {
            stop(shooter);
        }
        super.remove(reason);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        boolean server = !this.level().isClientSide();
        if (this.travelTime == -25 && server) {
            this.discard();
            return;
        }
        if (this.anchored()) {
            this.setDeltaMovement(Vec3.ZERO);
            if (!this.isValidMaterial(BlockPos.containing(this.getX(), this.getY(), this.getZ())) && server) {
                this.discard();
                return;
            }
        }
        Player player = this.shooter();
        if (player == null) {
            if (server) {
                this.discard();
            }
            return;
        }
        if ((!player.getUseItem().is(LGItems.HOOKSHOT.get()) || !player.isUsingItem()) && server) {
            this.discard();
            return;
        }
        this.travelTime--;
        if (this.anchored()) {
            Vec3 toMe = new Vec3(this.getX() - player.getX(), this.getY() - (player.getY() - 0.12), this.getZ() - player.getZ()).normalize();
            player.setDeltaMovement(toMe);
            player.resetFallDistance();
            if (this.travelTime % 2 == 0) {
                LGFx.sound(player, SoundEvents.ITEM_BREAK.value(), 0.2F, 2.0F);
            }
            if (this.getBoundingBox().inflate(0.5).intersects(player.getBoundingBox())) {
                stop(player);
                if (server) {
                    this.discard();
                }
                return;
            }
        }
        if (this.travelTime <= 0 && !this.anchored()) {
            if (this.getBoundingBox().inflate(0.25).intersects(player.getBoundingBox())) {
                if (server) {
                    this.discard();
                }
            } else {
                this.heading(player.getX() - this.getX(), player.getEyeY() - this.getY(), player.getZ() - this.getZ(), 1.5F, 0.0F);
            }
        }
    }

    @Override
    protected void impact(HitResult hit) {
        BlockHitResult bh = block(hit);
        if (bh == null || this.anchored() || this.travelTime <= 0 || this.level().isClientSide()) {
            return;
        }
        BlockPos pos = bh.getBlockPos();
        if (this.isValidMaterial(pos)) {
            this.entityData.set(ANCHORED, true);
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            this.needsSync = true;
        }
        this.travelTime = Math.min(this.travelTime, 0);
        BlockState state = this.level().getBlockState(pos);
        LGFx.sound(this, state.getSoundType().getPlaceSound(), 1.0F, 1.0F);
        LGFx.particle(this.level(), new BlockParticleOption(ParticleTypes.BLOCK, state), this.getX(), this.getY(), this.getZ(), 0, 0, 0);
    }
}
