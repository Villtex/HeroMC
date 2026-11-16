package harkenscythe.mob;

import harkenscythe.HSItems;
import harkenscythe.item.Essence;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * What all EntityHSSpectral* (EntityTameable) of Harken Scythe share: a raised soul that slowly fades (one health every
 * 200 ticks, angry particles at 5 and less when tamed, death by starvation at 1), tamed with a creepball (full 20
 * health, sitting), healed with blood from a Bloodkeeper or Blood Vessel (Regeneration I for 10 s), sits and stands on
 * a sneaking right-click of its owner, cannot be hurt by its owner and takes half damage from mobs; its own bite does
 * 1 (2 when tamed) whatever it holds. A Harbinger minion ("Evil") attacks players and cannot be tamed.
 * <p>
 * Movement speeds of 1.5.2 were absolute; the goal factors here are the old value divided by the mob's moveSpeed.
 */
public abstract class SpectralPet extends TamableAnimal {
    /** The sounds of a spectral mob: living (null for none), hurt, death, step (volume 0.15), and the volume of the rest. */
    public record Sounds(@Nullable SoundEvent ambient, @Nullable SoundEvent hurt, @Nullable SoundEvent death, @Nullable SoundEvent step,
            float volume) {
    }

    private final Sounds sounds;
    private final float untamedHealth;
    protected int petDamageReg = 1;
    protected int petDamageTamed = 2;
    private int soulAge;
    private int harbingerMinion;
    private boolean minionGoalAdded;

    protected SpectralPet(EntityType<? extends SpectralPet> type, Level level, float untamedHealth, Sounds sounds) {
        super(type, level);
        this.untamedHealth = untamedHealth;
        this.sounds = sounds;
        this.xpReward = 0;
    }

    /** getMaxHealth (untamed) and moveSpeed of the original; tamed the health goes to 20. */
    public static AttributeSupplier.Builder spectralAttributes(double health, double speed) {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, health).add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    protected double speed() {
        return this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
    }

    /** Old absolute AI speed as a factor of the mob's own speed. */
    protected double factor(double oldSpeed) {
        return oldSpeed / this.speed();
    }

    // ------------------------------------------------------------------------------------------------ goals
    /** The goals are set up by the constructor of each mob ({@link #setupGoals}), after its own fields. */
    @Override
    protected final void registerGoals() {
    }

    /** The goals of most spectral animals (chicken, cow, wolf, spider, ...). */
    protected void setupGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(6, this.creepballTempt());
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.registerTargets(true);
    }

    /** The goals of the humanoid ones (zombie, villager, skeleton, ...): door opening and villager watching. */
    protected void registerHumanoidGoals(boolean melee) {
        if (this.getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
        }
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        if (melee) {
            this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
            this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        }
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(6, this.creepballTempt());
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, AbstractVillager.class, 5.0F, 0.02F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    protected TemptGoal creepballTempt() {
        return new TemptGoal(this, this.factor(0.25), s -> s.is(HSItems.CREEPBALL.get()), false);
    }

    /** EntityAIOwnerHurtByTarget, EntityAIOwnerHurtTarget, EntityAIHurtByTarget(this, alertOthers). */
    protected void registerTargets(boolean alertOthers) {
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        HurtByTargetGoal hurtBy = new HurtByTargetGoal(this);
        this.targetSelector.addGoal(3, alertOthers ? hurtBy.setAlertOthers() : hurtBy);
    }

    // ------------------------------------------------------------------------------------------------ minion and life span
    public boolean isHarbingerMinion() {
        return this.harbingerMinion == 1;
    }

    /** createSpectralPet(..., needsMaster): a minion of the Harbinger with 15 health (setEntityHealth did not clamp in 1.5.2). */
    public void makeHarbingerMinion() {
        this.harbingerMinion = 1;
        if (this.getAttributeBaseValue(Attributes.MAX_HEALTH) < 15.0) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(15.0);
        }
        this.setHealth(15.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            this.soulTick(level);
        }
    }

    /** onUpdate: the minion's player target task, then the fading of the soul. */
    protected void soulTick(ServerLevel level) {
        if (this.harbingerMinion == 1 && !this.minionGoalAdded) {
            this.minionGoalAdded = true;
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, false));
        }
        if (++this.soulAge >= 200) {
            this.soulAge = 0;
            if (this.getHealth() <= 1.0F) {
                this.hurtServer(level, this.damageSources().starve(), 1000.0F);
            } else {
                this.setHealth(this.getHealth() - 1.0F);
                if (this.getHealth() <= 5.0F && this.isTame()) {
                    level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 0.5, this.getZ(), 0,
                            this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, 1.0);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ combat
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
        Entity attacker = source.getEntity();
        this.setOrderedToSit(false);
        if (attacker instanceof Player player && this.isOwnedBy(player)) {
            return false;
        }
        if (attacker != null && !(attacker instanceof Player)) {
            damage = (float) (((int) damage + 1) / 2);
        }
        return super.hurtServer(level, source, damage);
    }

    /** attackEntityAsMob: 1, tamed 2 (the silverfish 1), no matter what it holds. */
    protected float petDamage() {
        return this.isTame() ? this.petDamageTamed : this.petDamageReg;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        return target.hurtServer(level, this.damageSources().mobAttack(this), this.petDamage());
    }

    // ------------------------------------------------------------------------------------------------ taming
    @Override
    protected void applyTamingSideEffects() {
        if (!this.isHarbingerMinion()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.isTame() ? 20.0 : this.untamedHealth);
        }
    }

    /** Whether a creepball tames it (not a Harbinger minion; the spectral miner always). */
    protected boolean canTameWithCreepball() {
        return !this.isHarbingerMinion();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isTame()) {
            if (!stack.isEmpty() && this.getHealth() < 20.0F && !this.hasEffect(MobEffects.REGENERATION)
                    && (stack.is(HSItems.BLOODKEEPER.get()) || stack.is(HSItems.BLOOD_VESSEL.get()))) {
                if (!this.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, Essence.drain(stack, 1));
                    }
                    this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                }
                return InteractionResult.SUCCESS;
            }
            InteractionResult result = this.interactTamed(player, hand, stack);
            if (result != null) {
                return result;
            }
            if (this.isOwnedBy(player) && player.isSecondaryUseActive()) {
                if (!this.level().isClientSide()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                }
                return InteractionResult.SUCCESS;
            }
        } else if (stack.is(HSItems.CREEPBALL.get()) && this.canTameWithCreepball()) {
            stack.consume(1, player);
            if (!this.level().isClientSide()) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.setHealth(20.0F);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    /** What a tamed one does with the item of its owner before sitting down (weapons, saddles, collars, ...); null: go on. */
    protected @Nullable InteractionResult interactTamed(Player player, InteractionHand hand, ItemStack stack) {
        return null;
    }

    /** {@code new ItemStack(item, 1, damage)} of 1.5.2: one of the item with its damage, nothing else. */
    protected static ItemStack plainCopy(ItemStack stack) {
        ItemStack copy = new ItemStack(stack.getItem());
        if (stack.isDamageableItem()) {
            copy.setDamageValue(stack.getDamageValue());
        }
        return copy;
    }

    /** Hands a held item of the given kind back to the owner (empty hand, not sneaking). */
    protected boolean giveBackHeld(Player player, java.util.function.Predicate<ItemStack> kind) {
        ItemStack held = this.getMainHandItem();
        if (this.isOwnedBy(player) && !player.isSecondaryUseActive() && !held.isEmpty() && kind.test(held)) {
            if (!this.level().isClientSide()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, plainCopy(held));
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

    /** Takes one of the player's item into its empty hand. */
    protected boolean takeInHand(Player player, ItemStack stack) {
        if (!this.getMainHandItem().isEmpty()) {
            return false;
        }
        if (!this.level().isClientSide()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, plainCopy(stack));
            this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        }
        stack.consume(1, player);
        return true;
    }

    /** dropFewItems of the armed ones: the held weapon comes back 50 more damaged unless that breaks it. */
    protected void dropWornOut(ItemStack stack) {
        if (!stack.isEmpty() && stack.isDamageableItem() && stack.getDamageValue() + 50 < stack.getMaxDamage()) {
            ItemStack old = plainCopy(stack);
            old.setDamageValue(stack.getDamageValue() + 50);
            this.spawnAtLocation((ServerLevel) this.level(), old);
        }
    }

    // ------------------------------------------------------------------------------------------------ misc
    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return !this.isTame();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.sounds.ambient();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return this.sounds.hurt();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return this.sounds.death();
    }

    @Override
    protected float getSoundVolume() {
        return this.sounds.volume();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.sounds.step() != null) {
            this.playSound(this.sounds.step(), 0.15F, 1.0F);
        } else {
            super.playStepSound(pos, state);
        }
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        return !(target instanceof TamableAnimal t && t.isTame() && t.getOwner() == owner) && super.wantsToAttack(target, owner);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("SAge", (short) this.soulAge);
        output.putShort("Evil", (short) this.harbingerMinion);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.soulAge = input.getShortOr("SAge", (short) 0);
        this.harbingerMinion = input.getShortOr("Evil", (short) 0);
    }
}
