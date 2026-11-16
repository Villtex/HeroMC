package harkenscythe.mob;

import harkenscythe.HSEntities;
import harkenscythe.HarkenScythe;
import harkenscythe.entity.LostSoulEntity;
import harkenscythe.event.HSEvents;
import harkenscythe.item.ScytheItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityHSSpectralMinerEvil: the angry ghost of a miner (50 health, a random pickaxe, 5 + pickaxe damage, 5 xp) freed
 * from a lost soul by a keeper or raised by a Harbinger. Only soul-stealing hits hurt it (a scythe in hand or a
 * Soulsteal weapon); each such hit resets its 6000 tick life, and at 50, 40, 30, 20 and 10 health (and below 1) it
 * sheds a small ectoplasm worth one soul. Players without night vision within 10 blocks go blind; what it hits gets
 * mining fatigue (Normal 60 s, Hard 120 s). At the end of its life it turns back into a lost soul. Undead, fireproof,
 * breathes under water.
 */
public class EvilSpectralMiner extends Monster {
    private static final Item[] PICKAXES = {Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE};
    private int soulAge;
    private final boolean[] souls = {true, true, true, true, true};

    public EvilSpectralMiner(EntityType<? extends EvilSpectralMiner> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(PICKAXES[this.random.nextInt(PICKAXES.length)]));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.EVENTS;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_INFECT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SHOOT;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (!super.doHurtTarget(level, target)) {
            return false;
        }
        if (target instanceof LivingEntity living) {
            Difficulty d = level.getDifficulty();
            int seconds = d == Difficulty.NORMAL ? 60 : d == Difficulty.HARD ? 120 : 0;
            if (seconds > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, seconds * 20, 0), this);
            }
        }
        return true;
    }

    /** isSoulStealingAttack: a scythe in the attacker's hand or a Soulsteal weapon. */
    private static boolean isSoulStealing(ServerLevel level, DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            return weapon.getItem() instanceof ScytheItem || HSEvents.level(level, weapon, HarkenScythe.SOULSTEAL) > 0;
        }
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, damage);
        }
        if (this.isInvulnerableTo(level, source) || !isSoulStealing(level, source) || !super.hurtServer(level, source, damage)) {
            return false;
        }
        this.soulAge = 0;
        this.soulStolen(level);
        return true;
    }

    /** soulStolen: an ectoplasm at each tenth of its health it falls below (and one more below 1). */
    private void soulStolen(ServerLevel level) {
        float health = this.getHealth();
        for (int i = 0; i < 5; i++) {
            if (health < 50 - 10 * i && this.souls[i]) {
                this.createSlime(level);
                this.souls[i] = false;
            }
        }
        if (health < 1.0F) {
            this.createSlime(level);
        }
    }

    private void createSlime(ServerLevel level) {
        EssenceSlime slime = HSMobs.ECTOPLASM.get().create(level, EntitySpawnReason.TRIGGERED);
        if (slime != null) {
            slime.snapTo(this.getX() - 0.25, this.getY() + 0.5, this.getZ() - 0.25, this.random.nextFloat() * 360.0F, 0.0F);
            slime.setSize(1, true);
            slime.setResourceWorth(2);
            level.addFreshEntity(slime);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level) {
            for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0))) {
                if (player.isAlive() && this.distanceToSqr(player) < 100.0 && !player.hasEffect(MobEffects.NIGHT_VISION)) {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && ++this.soulAge >= 6000 && this.isAlive()) {
            this.discard();
            LostSoulEntity lost = HSEntities.SOUL_LOST.get().create(level, EntitySpawnReason.TRIGGERED);
            if (lost != null) {
                lost.setPos(Math.round(this.getX() - 0.5), Math.round(this.getY() + 0.5), Math.round(this.getZ()));
                level.addFreshEntity(lost);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("Age", (short) this.soulAge);
        for (int i = 0; i < 5; i++) {
            output.putBoolean("Soul" + (i + 1), this.souls[i]);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.soulAge = input.getShortOr("Age", (short) 0);
        for (int i = 0; i < 5; i++) {
            this.souls[i] = input.getBooleanOr("Soul" + (i + 1), true);
        }
    }
}
