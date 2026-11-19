package chococraft.entity;

import chococraft.ChocoConfig;
import chococraft.ChocoCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityAnimalChocobo: what chocobos and chicobos share. Name, gender and the follow/wander/stay state are synced
 * (the DataWatcher ids 18/19 of the original); taming with a gysahl green (one in three), healing with one when tamed
 * (10 health), the Chocopedia (opens the chocobo's page for a tamed chocobo), a book and quill turns into a Chocopedia.
 * A hurt tamed chocobo standing on straw heals 1-3 health with a chance of {@code penHealProbability} % every 40 ticks
 * when a filled cauldron is within {@code penHealCauldronRange} blocks (2 blocks up and down).
 * <p>
 * The owner is today's owner reference (the original kept the player name); the name is kept as well, for the
 * Chocopedia page. Hunger (hungerEnabled=false in the pack) is not ported.
 */
public abstract class AbstractChocobo extends TamableAnimal {
    private static final EntityDataAccessor<String> DATA_NAME = SynchedEntityData.defineId(AbstractChocobo.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_OWNER_NAME = SynchedEntityData.defineId(AbstractChocobo.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> DATA_FLAGS = SynchedEntityData.defineId(AbstractChocobo.class, EntityDataSerializers.BYTE);
    // Constants.DW_VAL_EAC_*
    private static final int MALE = 1;
    private static final int IN_LOVE = 2;
    private static final int HIDE_NAME = 4;
    private static final int WANDER = 8;
    private static final int FOLLOWING = 16;

    /** fleeingTick: set to 60 by every hit; the chocobo runs about and stops mating meanwhile. */
    protected int fleeingTick;

    protected AbstractChocobo(EntityType<? extends AbstractChocobo> type, Level level) {
        super(type, level);
        this.setHidename(true);
        this.setWander(true);
        this.setFollowing(false);
        this.setIsMale(this.random.nextInt(100) < ChocoConfig.GENDER_MALE_CHANCE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_NAME, "");
        builder.define(DATA_OWNER_NAME, "");
        builder.define(DATA_FLAGS, (byte) 0);
    }

    // ---- synced attributes ----

    private boolean flag(int bit) {
        return (this.entityData.get(DATA_FLAGS) & bit) != 0;
    }

    private void setFlag(int bit, boolean on) {
        byte flags = this.entityData.get(DATA_FLAGS);
        this.entityData.set(DATA_FLAGS, (byte) (on ? flags | bit : flags & ~bit));
    }

    /** getName: a random name of the gender the first time it is asked for. */
    public String getChocoboName() {
        String name = this.entityData.get(DATA_NAME);
        if (name.isEmpty() && !this.level().isClientSide()) {
            name = ChocoboNames.random(this.random, this.isMale());
            this.entityData.set(DATA_NAME, name);
        }
        return name;
    }

    public void setChocoboName(String name) {
        this.entityData.set(DATA_NAME, name);
    }

    public String getOwnerName() {
        return this.entityData.get(DATA_OWNER_NAME);
    }

    public void setOwnerName(String name) {
        this.entityData.set(DATA_OWNER_NAME, name);
    }

    public boolean isMale() {
        return this.flag(MALE);
    }

    public void setIsMale(boolean male) {
        this.setFlag(MALE, male);
    }

    public boolean isInLove() {
        return this.flag(IN_LOVE);
    }

    public void setInLove(boolean inLove) {
        this.setFlag(IN_LOVE, inLove);
    }

    public boolean isHidename() {
        return this.flag(HIDE_NAME);
    }

    public void setHidename(boolean hide) {
        this.setFlag(HIDE_NAME, hide);
    }

    public boolean isWander() {
        return this.flag(WANDER);
    }

    public void setWander(boolean wander) {
        this.setFlag(WANDER, wander);
    }

    public boolean isFollowing() {
        return this.flag(FOLLOWING);
    }

    public void setFollowing(boolean following) {
        this.setFlag(FOLLOWING, following);
    }

    public abstract ChocoboColor getColor();

    /** canRenderName without the client checks: names shown, tamed, no rider, not hidden, a name. */
    public boolean showsName() {
        return ChocoConfig.SHOW_CHOCOBO_NAMES.get() && this.isTame() && !this.isVehicle() && !this.isHidename()
                && !this.entityData.get(DATA_NAME).isEmpty();
    }

    /** toggleFollowWanderStay: follow -> stay -> wander -> follow. */
    public void toggleFollowWanderStay() {
        if (this.isFollowing() && !this.isWander()) {
            this.setFollowing(false);
            this.setWander(false);
        } else if (!this.isFollowing() && !this.isWander()) {
            this.setFollowing(false);
            this.setWander(true);
        } else {
            this.setFollowing(true);
            this.setWander(false);
        }
        this.particles(this.isFollowing() ? ParticleTypes.HEART : ParticleTypes.SMOKE, 7);
    }

    /** toggleFollowStay: stay -> follow, anything else -> stay. */
    public void toggleFollowStay() {
        if (!this.isFollowing() && !this.isWander()) {
            this.setFollowing(true);
            this.setWander(false);
        } else {
            this.setFollowing(false);
            this.setWander(false);
        }
        this.particles(this.isFollowing() ? ParticleTypes.HEART : ParticleTypes.SMOKE, 7);
    }

    /** isOwner: on a server only the owner; the original let anybody in single player. */
    public boolean isOwner(Player player) {
        return this.isOwnedBy(player);
    }

    // ---- save data ----

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Name", this.getChocoboName());
        output.putString("OwnerName", this.getOwnerName());
        output.putBoolean("isMale", this.isMale());
        output.putBoolean("Follow", this.isFollowing());
        output.putBoolean("wander", this.isWander());
        output.putBoolean("hidename", this.isHidename());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setChocoboName(input.getStringOr("Name", ""));
        this.setOwnerName(input.getStringOr("OwnerName", ""));
        this.setHidename(input.getBooleanOr("hidename", true));
        this.setFollowing(input.getBooleanOr("Follow", false));
        this.setWander(input.getBooleanOr("wander", true));
        this.setIsMale(input.getBooleanOr("isMale", this.isMale()));
    }

    // ---- ticking ----

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.fleeingTick > 0) {
            this.fleeingTick--;
        }
        this.doStrawHealing();
    }

    private void doStrawHealing() {
        if (!(this.level() instanceof ServerLevel level) || !this.isTame() || this.getHealth() >= this.getMaxHealth()
                || level.getGameTime() % 40L != 0L) {
            return;
        }
        if (this.random.nextInt(100) < ChocoConfig.PEN_HEAL_PROBABILITY.get()) {
            BlockPos feet = BlockPos.containing(this.getX(), this.getBoundingBox().minY, this.getZ());
            if (level.getBlockState(feet).is(ChocoCraft.STRAW.get())) {
                int range = ChocoConfig.PEN_HEAL_CAULDRON_RANGE.get();
                if (this.isFilledCauldronNear((int) this.getX(), (int) this.getY(), (int) this.getZ(), range, 2)) {
                    this.heal(this.random.nextInt(3) + 1);
                    this.particles(ParticleTypes.HEART, 1);
                }
            }
        }
    }

    /** isFilledCauldronNear: the level above the feet first, then upwards, then from the feet downwards. */
    private boolean isFilledCauldronNear(int x, int y, int z, int range, int rangeY) {
        if (this.isFilledCauldronAtY(x, y + 1, z, range)) {
            return true;
        }
        for (int yy = y + 2; yy <= y + rangeY; yy++) {
            if (this.isFilledCauldronAtY(x, yy, z, range)) {
                return true;
            }
        }
        for (int yy = y; yy >= y - rangeY; yy--) {
            if (this.isFilledCauldronAtY(x, yy, z, range)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFilledCauldronAtY(int x, int y, int z, int range) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int xx = x - range; xx <= x + range; xx++) {
            for (int zz = z - range; zz <= z + range; zz++) {
                // a 1.5.2 cauldron with metadata > 0 is one holding water
                BlockState state = this.level().getBlockState(pos.set(xx, y, zz));
                if (state.is(Blocks.WATER_CAULDRON)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ---- interaction ----

    /**
     * EntityAnimalChocobo.interact: Chocopedia, gysahl green and book and quill. Returns null when none of them
     * matched, so the subclasses go on with their items.
     */
    protected @Nullable InteractionResult interactCommon(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ChocoCraft.CHOCOPEDIA.get())) {
            if (this.level().isClientSide()) {
                if (this.isTame()) {
                    ChocoCraft.openChocopedia(this);
                }
            } else if (!this.isTame()) {
                this.particles(ParticleTypes.SMOKE, 7);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(ChocoCraft.GYSAHL_GREEN_ITEM.get())) {
            if (!this.level().isClientSide()) {
                this.onGysahlGreenUse(player, stack);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.WRITABLE_BOOK)) {
            // onWritableBookUse: the book itself becomes a Chocopedia
            if (!this.level().isClientSide()) {
                player.setItemInHand(hand, stack.transmuteCopy(ChocoCraft.CHOCOPEDIA.get(), stack.getCount()));
            }
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    private void onGysahlGreenUse(Player player, ItemStack stack) {
        if (!this.isTame()) {
            this.useItem(player, stack);
            if (this.random.nextInt(3) == 0) {
                this.setTame(true, true);
                this.setWander(true);
                this.setFollowing(false);
                this.setHidename(true);
                this.setOwner(player);
                this.setOwnerName(player.getGameProfile().name());
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.particles(ParticleTypes.HEART, 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.particles(ParticleTypes.SMOKE, 7);
            }
        } else if (this.getHealth() < this.getMaxHealth()) {
            this.useItem(player, stack);
            this.heal(10.0F);
            this.particles(ParticleTypes.HEART, 7);
        }
    }

    /** useItem: one of the held stack, also in creative mode. */
    protected void useItem(Player player, ItemStack stack) {
        stack.shrink(1);
    }

    // ---- damage ----

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        this.fleeingTick = 60;
        this.onHurtStopMating();
        this.setInLove(false);
        if (this.isTame()) {
            if (source.is(DamageTypes.IN_WALL)) {
                return false;
            }
            if (source.getEntity() instanceof Player player) {
                if (this.isOwner(player) && player.isShiftKeyDown()) {
                    return false;
                }
                if (this.hasPassenger(player)) {
                    return false;
                }
            }
        }
        return super.hurtServer(level, source, damage);
    }

    /** entityToAttack = null of attackEntityFrom. */
    protected void onHurtStopMating() {
    }

    // ---- particles (PacketChocoboParticles: showParticleAroundEntityFx for everybody around) ----

    public void particles(ParticleOptions type, int amount) {
        if (this.level() instanceof ServerLevel level) {
            for (int i = 0; i < amount; i++) {
                double x = this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth();
                double y = this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight();
                double z = this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth();
                level.sendParticles(type, x, y, z, 0, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02,
                        this.random.nextGaussian() * 0.02, 1.0);
            }
        }
    }

    // ---- vanilla breeding and despawning are not used ----

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return ChocoConfig.WILD_CAN_DESPAWN.get() && !this.isTame();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 150;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ChocoCraft.KWEH.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ChocoCraft.KWEH.get();
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return super.isInvulnerableTo(level, source) || this.getColor().fireImmune && source.is(DamageTypeTags.IS_FIRE);
    }

    /** The owner within the level, or null (getOwner of the original looked the name up the same way). */
    public @Nullable Player getOwnerPlayer() {
        LivingEntity owner = this.getOwner();
        return owner instanceof Player player ? player : null;
    }

    /** teleportToOwner: the first free spot on the ring around the owner (5 x 5 without the inner 3 x 3). */
    public boolean teleportToOwner() {
        Player owner = this.getOwnerPlayer();
        if (owner == null) {
            return false;
        }
        int ox = Mth.floor(owner.getX()) - 2;
        int oz = Mth.floor(owner.getZ()) - 2;
        int oy = Mth.floor(owner.getBoundingBox().minY);
        for (int dx = 0; dx <= 4; dx++) {
            for (int dz = 0; dz <= 4; dz++) {
                if (dx < 1 || dz < 1 || dx > 3 || dz > 3) {
                    int x = ox + dx;
                    int z = oz + dz;
                    if (this.canSpawnAtLoc(x, oy, z)) {
                        this.snapTo(x + 0.5, oy, z + 0.5, this.getYRot(), this.getXRot());
                        this.getNavigation().stop();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** canSpawnAtLoc: a solid top below and air 1 block around, 3 high. */
    protected boolean canSpawnAtLoc(int x, int y, int z) {
        BlockPos below = new BlockPos(x, y - 1, z);
        return this.level().getBlockState(below).isFaceSturdy(this.level(), below, net.minecraft.core.Direction.UP)
                && isSpaceAroundLocationFree(this.level(), x, y, z, 1, 3, 1);
    }

    /** ChocoboEntityHelper.isSpaceAroundLocationFree: only air from y + 1 to y + spcY, radX / radZ around. */
    public static boolean isSpaceAroundLocationFree(Level level, int x, int y, int z, int radX, int spcY, int radZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int xx = x - radX; xx <= x + radX; xx++) {
            for (int yy = y + 1; yy <= y + spcY; yy++) {
                for (int zz = z - radZ; zz <= z + radZ; zz++) {
                    if (!level.getBlockState(pos.set(xx, yy, zz)).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
