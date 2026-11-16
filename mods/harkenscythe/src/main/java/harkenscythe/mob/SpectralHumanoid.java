package harkenscythe.mob;

import harkenscythe.HSBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

/**
 * The armed humanoid spectral mobs. A tamed one takes an item from its owner into its empty hand and gives it back on
 * an empty-handed right-click; on death the item comes back 50 more worn unless that breaks it.
 * <ul>
 * <li>Zombie, Pig Zombie, Wither Skeleton: a sword (EntityHSSpectralZombie/PigZombie/SkeletonWither).</li>
 * <li>Skeleton: a bow; with it the skeleton shoots arrows (EntityAIArrowAttack 0.25, 20-60 ticks, 15 blocks).</li>
 * <li>Witch: a (drinkable) potion; a harmful one she throws at her target (10 blocks), a helpful one at her owner
 * when he lacks its effect, at most every 200 ticks and without using it up.</li>
 * <li>Miner: a bow, a sword or a torch, and armour (+2 armour); with a torch he lights his way with soul lights
 * and sets what he hits on fire for 2 s.</li>
 * </ul>
 * The weapon only changes the attack goal: the bite stays at 1 (2 when tamed), as in the original.
 */
public class SpectralHumanoid extends SpectralPet implements RangedAttackMob {
    public enum Kind {
        ZOMBIE, PIG_ZOMBIE, WITHER_SKELETON, SKELETON, WITCH, MINER
    }

    private static final List<Holder<MobEffect>> BUFFS = List.of(MobEffects.STRENGTH, MobEffects.HASTE, MobEffects.FIRE_RESISTANCE,
            MobEffects.INSTANT_HEALTH, MobEffects.INVISIBILITY, MobEffects.JUMP_BOOST, MobEffects.SPEED, MobEffects.NIGHT_VISION,
            MobEffects.REGENERATION, MobEffects.RESISTANCE, MobEffects.WATER_BREATHING);
    private static final List<Holder<MobEffect>> DEBUFFS = List.of(MobEffects.WEAKNESS, MobEffects.MINING_FATIGUE, MobEffects.NAUSEA,
            MobEffects.BLINDNESS, MobEffects.INSTANT_DAMAGE, MobEffects.HUNGER, MobEffects.SLOWNESS, MobEffects.POISON, MobEffects.WITHER);

    private final Kind kind;
    private Goal leap;
    private Goal melee;
    private Goal ranged;
    private int witchAttackTimer;
    private int witchBuffTimer;

    public SpectralHumanoid(EntityType<? extends SpectralHumanoid> type, Level level, Kind kind, Sounds sounds) {
        super(type, level, 10.0F, sounds);
        this.kind = kind;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0F);
        }
        this.setupGoals();
    }

    public Kind kind() {
        return this.kind;
    }

    @Override
    protected void setupGoals() {
        this.leap = new LeapAtTargetGoal(this, 0.4F);
        this.melee = new MeleeAttackGoal(this, 1.0, true);
        this.ranged = new RangedAttackGoal(this, this.factor(0.25), 20, 60, this.kind == Kind.WITCH ? 10.0F : 15.0F);
        boolean armed = this.kind == Kind.SKELETON || this.kind == Kind.WITCH || this.kind == Kind.MINER;
        this.registerHumanoidGoals(!armed);
        this.registerTargets(true);
        if (armed && !this.level().isClientSide()) {
            this.setCombatTask();
        }
    }

    /** setCombatTask: the attack goals for what it holds. */
    public void setCombatTask() {
        this.goalSelector.removeGoal(this.leap);
        this.goalSelector.removeGoal(this.melee);
        this.goalSelector.removeGoal(this.ranged);
        ItemStack held = this.getMainHandItem();
        switch (this.kind) {
            case SKELETON -> {
                if (held.getItem() instanceof BowItem) {
                    this.goalSelector.addGoal(4, this.ranged);
                } else {
                    this.addMelee(true);
                }
            }
            case WITCH -> {
                if (held.is(Items.POTION)) {
                    if (potionType(held) == 2) {
                        this.goalSelector.addGoal(4, this.ranged);
                    }
                } else {
                    this.addMelee(true);
                }
            }
            case MINER -> {
                if (held.getItem() instanceof BowItem) {
                    this.goalSelector.addGoal(4, this.ranged);
                } else {
                    this.addMelee(isSword(held));
                }
            }
            default -> this.addMelee(true);
        }
    }

    private void addMelee(boolean leap) {
        if (leap) {
            this.goalSelector.addGoal(3, this.leap);
        }
        this.goalSelector.addGoal(4, this.melee);
    }

    /** ItemSword of 1.5.2: swords, scythes, glaives and the giant swords of Asgard Shield (the altar's tag). */
    static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> SWORDS = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.ITEM, harkenscythe.HarkenScythe.id("altar/sword"));

    static boolean isSword(ItemStack stack) {
        return stack.is(SWORDS);
    }

    /** The spectral miner can always be tamed with a creepball, even as a minion. */
    @Override
    protected boolean canTameWithCreepball() {
        return this.kind == Kind.MINER || super.canTameWithCreepball();
    }

    /** What its hand takes: swords, the skeleton a bow, the witch a drinkable potion, the miner a bow, a sword or a torch. */
    private boolean accepts(ItemStack stack) {
        return switch (this.kind) {
            case SKELETON -> stack.getItem() instanceof BowItem;
            case WITCH -> stack.is(Items.POTION);
            case MINER -> stack.getItem() instanceof BowItem || isSword(stack);
            default -> isSword(stack);
        };
    }

    // ------------------------------------------------------------------------------------------------ interaction
    @Override
    protected @Nullable InteractionResult interactTamed(Player player, InteractionHand hand, ItemStack stack) {
        if (stack.isEmpty()) {
            if (this.giveBackHeld(player, this::accepts)) {
                if (!this.level().isClientSide()) {
                    this.setCombatTask();
                }
                return InteractionResult.SUCCESS;
            }
        } else {
            if (this.accepts(stack) && this.getMainHandItem().isEmpty()) {
                if (this.kind == Kind.WITCH) {
                    if (!this.level().isClientSide()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, stack.copyWithCount(1));
                    }
                    stack.consume(1, player);
                } else {
                    this.takeInHand(player, stack);
                }
                if (!this.level().isClientSide()) {
                    this.setCombatTask();
                }
                return InteractionResult.SUCCESS;
            }
            if (this.kind == Kind.MINER) {
                EquipmentSlot slot = this.getEquipmentSlotForItem(stack);
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !stack.is(Items.SHIELD) && this.getItemBySlot(slot).isEmpty()) {
                    if (!this.level().isClientSide()) {
                        this.setItemSlot(slot, plainCopy(stack));
                    }
                    stack.consume(1, player);
                    return InteractionResult.SUCCESS;
                }
                if (stack.is(Items.TORCH) && this.getMainHandItem().isEmpty()) {
                    this.takeInHand(player, stack);
                    if (!this.level().isClientSide()) {
                        this.setCombatTask();
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        if (this.kind == Kind.MINER && this.isOwnedBy(player) && !player.isSecondaryUseActive() && this.getMainHandItem().is(Items.TORCH)) {
            if (!this.level().isClientSide()) {
                player.getInventory().add(new ItemStack(Items.TORCH));
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                BlockPos above = this.blockPosition().above();
                if (this.level().getBlockState(above).is(HSBlocks.SOUL_LIGHT.get())) {
                    this.level().removeBlock(above, false);
                }
                this.setCombatTask();
            }
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    // ------------------------------------------------------------------------------------------------ miner
    public boolean holdsTorch() {
        return this.getMainHandItem().is(Items.TORCH);
    }

    @Override
    protected void soulTick(ServerLevel level) {
        super.soulTick(level);
        if (this.kind == Kind.MINER && this.holdsTorch()) {
            BlockPos above = this.blockPosition().above();
            if (level.isEmptyBlock(above) && !level.getBlockState(above).is(HSBlocks.SOUL_LIGHT.get())) {
                level.setBlockAndUpdate(above, HSBlocks.SOUL_LIGHT.get().defaultBlockState());
            }
        }
        if (this.kind == Kind.WITCH) {
            if (this.witchAttackTimer < 200) {
                this.witchAttackTimer++;
            }
            this.witchBuff(level);
        }
    }

    /** EntityAIHSSpectralMinerFollowOwner: before he teleports to his owner a miner with a torch puts out the soul lights around him. */
    @Override
    public void tryToTeleportToOwner() {
        if (this.kind == Kind.MINER && this.holdsTorch()) {
            BlockPos pos = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
                if (this.level().getBlockState(p).is(HSBlocks.SOUL_LIGHT.get())) {
                    this.level().setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                }
            }
        }
        super.tryToTeleportToOwner();
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.kind == Kind.MINER && this.isTame() && this.holdsTorch()) {
            target.igniteForSeconds(2.0F);
        }
        return super.doHurtTarget(level, target);
    }

    // ------------------------------------------------------------------------------------------------ witch
    /** getPotionEffectType: 1 helpful, 2 harmful, 0 neither (the first listed effect decides). */
    static int potionType(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return 0;
        }
        for (MobEffectInstance e : contents.getAllEffects()) {
            if (BUFFS.contains(e.getEffect())) {
                return 1;
            }
            if (DEBUFFS.contains(e.getEffect())) {
                return 2;
            }
        }
        return 0;
    }

    private static @Nullable Holder<MobEffect> firstEffect(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            for (MobEffectInstance e : contents.getAllEffects()) {
                return e.getEffect();
            }
        }
        return null;
    }

    private void witchBuff(ServerLevel level) {
        ItemStack held = this.getMainHandItem();
        LivingEntity owner = this.getOwner();
        if (!this.isTame() || this.witchAttackTimer < 200 || !held.is(Items.POTION) || potionType(held) != 1 || owner == null) {
            return;
        }
        Holder<MobEffect> effect = firstEffect(held);
        if (effect != null && owner.hasEffect(effect)) {
            return;
        }
        if (this.witchBuffTimer >= 200) {
            this.throwPotion(level, owner, held);
            this.witchAttackTimer = 0;
            this.witchBuffTimer = 0;
        } else {
            this.witchBuffTimer++;
        }
    }

    private void throwPotion(ServerLevel level, LivingEntity at, ItemStack held) {
        ItemStack splash = new ItemStack(Items.SPLASH_POTION);
        splash.set(DataComponents.POTION_CONTENTS, held.get(DataComponents.POTION_CONTENTS));
        double dx = at.getX() + at.getDeltaMovement().x - this.getX();
        double dy = at.getEyeY() - 1.1F - this.getY();
        double dz = at.getZ() + at.getDeltaMovement().z - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        Projectile.spawnProjectileUsingShoot(ThrownSplashPotion::new, level, splash, this, dx, dy + dist * 0.2F, dz, 0.75F, 8.0F);
    }

    // ------------------------------------------------------------------------------------------------ ranged
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack held = this.getMainHandItem();
        if (this.kind == Kind.WITCH) {
            if (this.witchAttackTimer >= 200 && held.is(Items.POTION)) {
                Holder<MobEffect> effect = firstEffect(held);
                if (effect == null || !target.hasEffect(effect)) {
                    this.throwPotion(level, target, held);
                    this.witchAttackTimer = 0;
                }
            }
            return;
        }
        ItemStack projectile = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectile, power, held);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        Projectile.spawnProjectileUsingShoot(arrow, level, projectile, dx, dy + dist * 0.2F, dz, 1.6F, this.rangedAttackUncertainty(level));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    // ------------------------------------------------------------------------------------------------ drops
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        ItemStack held = this.getMainHandItem();
        // the witch's potion has no durability: the glass bottle of the original never dropped
        if (this.kind != Kind.WITCH && this.accepts(held)) {
            this.dropWornOut(held);
        }
        if (this.kind == Kind.MINER) {
            for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                this.dropWornOut(this.getItemBySlot(slot));
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setCombatTask();
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder minerAttributes() {
        return spectralAttributes(10.0, 0.3).add(Attributes.ARMOR, 2.0);
    }
}
