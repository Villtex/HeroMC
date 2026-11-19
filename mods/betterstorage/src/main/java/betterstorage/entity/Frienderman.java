package betterstorage.entity;

import betterstorage.BS;
import betterstorage.BSUtil;
import betterstorage.block.BackpackBlock;
import betterstorage.block.BackpackBlockEntity;
import betterstorage.network.BSNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/**
 * EntityFrienderman: an enderman that spawns with an ender backpack (1/80 of the natural endermen outside the End,
 * {@link betterstorage.backpack.BackpackEvents}). It never attacks or stares, does not scream, has 60 health and
 * teleports away when it is hurt. The only block it picks up is an ender chest, and only while it wears its backpack
 * and stands in air: then the backpack is set down where it stood (with the teleport particles) and it keeps the chest.
 */
public class Frienderman extends Enderman {
    public Frienderman(EntityType<? extends Enderman> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Enderman.createAttributes().add(Attributes.MAX_HEALTH, 60.0);
    }

    /** findPlayerToAttack returned null: no stare, no melee, no target goals. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(11, new TakeEnderChestGoal(this));
    }

    @Override
    public void setTarget(LivingEntity target) {
        super.setTarget(null);
    }

    @Override
    public boolean isCreepy() {
        return false;
    }

    /** attackEntityFrom: after a hit it tries up to 40 - health / 2 times to teleport away. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean success = super.hurtServer(level, source, damage);
        if (success && source.getEntity() instanceof LivingEntity && this.isAlive()) {
            for (int i = 0; i < 40 - this.getHealth() / 2 && !this.teleport(); i++) {
            }
        }
        return success;
    }

    /** despawnEntity: only far from players, very slowly; near players its age is reset. */
    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL && !this.getType().isAllowedInPeaceful()) {
            this.discard();
            return;
        }
        if (this.isPersistenceRequired() || this.requiresCustomPersistence()) {
            this.noActionTime = 0;
            return;
        }
        Player player = this.level().getNearestPlayer(this, -1.0);
        double distance = player != null ? this.distanceTo(player) : 64000.0;
        if (distance < 16.0) {
            this.noActionTime = 0;
        } else if (distance < 32.0) {
            this.noActionTime = Math.max(0, this.noActionTime - 2);
        } else if (distance < 48.0) {
            this.noActionTime = Math.max(0, this.noActionTime - 1);
        } else if (distance > 64.0 && this.noActionTime > 6000 && BSUtil.getBoolean(0.001)) {
            this.discard();
        }
    }

    /** The ender chest it took becomes its ender backpack, set down at its feet. */
    void swapForEnderChest(ServerLevel level, BlockPos chest) {
        ItemStack backpack = this.getItemBySlot(EquipmentSlot.CHEST);
        BlockPos pos = this.blockPosition();
        if (!backpack.is(BS.ENDER_BACKPACK.get()) || !level.getBlockState(pos).isAir()) {
            return;
        }
        BlockState state = level.getBlockState(chest);
        level.removeBlock(chest, false);
        level.gameEvent(GameEvent.BLOCK_DESTROY, chest, GameEvent.Context.of(this, state));
        this.setCarriedBlock(Blocks.ENDER_CHEST.defaultBlockState());
        betterstorage.backpack.Backpacks.removeBackpack(this);
        level.setBlock(pos, BS.ENDER_BACKPACK_BLOCK.get().defaultBlockState()
                .setValue(BackpackBlock.FACING, Direction.from3DDataValue(BSUtil.getInt(2, 6))), 3);
        if (level.getBlockEntity(pos) instanceof BackpackBlockEntity entity) {
            entity.stack = new ItemStack(BS.ENDER_BACKPACK.get());
            entity.sync();
        }
        BSNetwork.sendTeleport(level, this.getX(), this.getY() + 1.0, this.getZ(), pos.getX(), pos.getY(), pos.getZ());
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    /** EndermanTakeBlockGoal with friendermanCarriable (only the ender chest), independent of mobGriefing. */
    private static final class TakeEnderChestGoal extends Goal {
        private final Frienderman frienderman;

        TakeEnderChestGoal(Frienderman frienderman) {
            this.frienderman = frienderman;
        }

        @Override
        public boolean canUse() {
            return this.frienderman.getCarriedBlock() == null && !this.frienderman.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
                    && this.frienderman.getRandom().nextInt(reducedTickDelay(20)) == 0;
        }

        @Override
        public void tick() {
            Frienderman e = this.frienderman;
            if (!(e.level() instanceof ServerLevel level)) {
                return;
            }
            int x = Mth.floor(e.getX() - 2.0 + e.getRandom().nextDouble() * 4.0);
            int y = Mth.floor(e.getY() + e.getRandom().nextDouble() * 3.0);
            int z = Mth.floor(e.getZ() - 2.0 + e.getRandom().nextDouble() * 4.0);
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).is(Blocks.ENDER_CHEST)) {
                return;
            }
            Vec3 from = new Vec3(e.getBlockX() + 0.5, y + 0.5, e.getBlockZ() + 0.5);
            Vec3 to = new Vec3(x + 0.5, y + 0.5, z + 0.5);
            if (level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, e)).getBlockPos().equals(pos)) {
                e.swapForEnderChest(level, pos);
            }
        }
    }
}
