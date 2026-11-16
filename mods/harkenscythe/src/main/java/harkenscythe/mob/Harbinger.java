package harkenscythe.mob;

import harkenscythe.HSEntities;
import harkenscythe.HSItems;
import harkenscythe.entity.SoulEntity;
import java.time.LocalDate;
import java.time.Month;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntityHSHarbinger: a skeletal reaper with the Soul Reaper (160 health, 30 xp) that hunts souls first, then players
 * and villagers, and reaps what it kills ({@link HSMobs#harbingerSoulReap}): souls it slays rise as its minions.
 * It vanishes in daylight unless its helmet takes the sun; at a third of its health it calls four souls around it
 * once, at a quarter it turns invisible, and from half its health it teleports away from projectiles. Killing a
 * soul heals it (Regeneration I for 5 s).
 * <p>
 * The teleporting towards its target of the original used the old AI's entityToAttack, which an AI mob never sets;
 * that code never ran and is left out.
 */
public class Harbinger extends Monster {
    /** randSoulList: the vanilla mobs whose souls it calls. */
    private static final String[] SOUL_LIST = {"chicken", "cow", "sheep", "pig", "wolf", "ocelot", "villager", "creeper", "zombie", "skeleton",
            "witch", "spider", "cave_spider", "silverfish", "enderman"};
    private int lvl;
    public int reAnimateResource = 5;
    private int summonMinion = 1;

    public Harbinger(EntityType<? extends Harbinger> type, Level level) {
        super(type, level);
        this.xpReward = 30;
        if (this.getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 160.0).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new BreakDoorGoal(this, d -> d == Difficulty.HARD));
        this.goalSelector.addGoal(8, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> true));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, SoulEntity.class, false));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    /** initCreature: random armour, the Soul Reaper, the chance to pick up loot and the Halloween pumpkin. */
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        data = super.finalizeSpawn(level, difficulty, reason, data);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(HSItems.SOUL_REAPER.get()));
        this.setCanPickUpLoot(this.random.nextFloat() < 0.55F * difficulty.getSpecialMultiplier());
        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate date = LocalDate.now();
            if (date.getMonth() == Month.OCTOBER && date.getDayOfMonth() == 31 && this.random.nextFloat() < 0.25F) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Items.JACK_O_LANTERN : Items.CARVED_PUMPKIN));
                this.setDropChance(EquipmentSlot.HEAD, 0.0F);
            }
        }
        return data;
    }

    @Override
    public void aiStep() {
        if (this.level() instanceof ServerLevel level && this.isAlive()) {
            this.sunCheck(level);
            float max = this.getMaxHealth();
            if (this.getHealth() <= (int) max / 3 && this.summonMinion == 1) {
                level.levelEvent(2002, BlockPos.containing(Math.round(this.getX()), Math.round(this.getY() + 0.5), Math.round(this.getZ())), 0);
                level.playSound(null, this, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 1.0F, 1.0F);
                this.summonMinions(level);
                this.summonMinion--;
                this.reAnimateResource += 4;
            }
            if (this.getHealth() <= (int) max / 4) {
                this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 2));
            }
        }
        super.aiStep();
    }

    private void sunCheck(ServerLevel level) {
        if (!level.isBrightOutside()) {
            return;
        }
        float brightness = this.getLightLevelDependentMagicValue();
        BlockPos pos = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        if (brightness > 0.5F && this.random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F && level.canSeeSky(pos)) {
            ItemStack helmet = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty()) {
                if (helmet.isDamageableItem()) {
                    helmet.setDamageValue(helmet.getDamageValue() + this.random.nextInt(2));
                    if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
                        this.onEquippedItemBroken(helmet, EquipmentSlot.HEAD);
                        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }
                return;
            }
            level.playSound(null, this, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 1.0F, 1.0F);
            level.levelEvent(2003, BlockPos.containing(Math.round(this.getX() - 0.5), Math.round(this.getY() + 0.5), Math.round(this.getZ())), 0);
            this.discard();
        }
    }

    /** summonMinons: a soul (of a random vanilla mob) in each diagonal direction, as far out as four blocks where there is air. */
    private void summonMinions(ServerLevel level) {
        int[][] dirs = {{1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
        for (int[] d : dirs) {
            for (int i = 4; i >= 0; i--) {
                int x = (int) Math.round(this.getX() + d[0] * i);
                int y = (int) Math.round(this.getY() + 0.5);
                int z = (int) Math.round(this.getZ() + d[1] * i);
                if (level.isEmptyBlock(new BlockPos(x, y, z))) {
                    LivingEntity fallen = this.randomSoul(level);
                    SoulEntity.spawn(HSEntities.SOUL_COMMON.get(), level, x, y, z, 1, fallen);
                    break;
                }
            }
        }
    }

    /** getRandomSouls: a vanilla mob (not added to the world) whose soul the new soul is. */
    private LivingEntity randomSoul(ServerLevel level) {
        String name = SOUL_LIST[this.random.nextInt(SOUL_LIST.length)];
        EntityType<?> type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(name));
        Entity e = type.create(level, EntitySpawnReason.TRIGGERED);
        if (e instanceof LivingEntity living) {
            return living;
        }
        return HSMobs.ECTOPLASM.get().create(level, EntitySpawnReason.TRIGGERED);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        super.doHurtTarget(level, target);
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        return true;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity, DamageSource source) {
        this.lvl++;
        if (entity instanceof SoulEntity) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
        }
        return super.killedEntity(level, entity, source);
    }

    /** From half its health an indirect hit (arrow, fireball, ...) makes it teleport away (64 tries) instead of hurting it. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
        if (source.getDirectEntity() != source.getEntity() && source.getDirectEntity() != null && this.getHealth() <= (int) this.getMaxHealth() / 2) {
            for (int i = 0; i < 64; i++) {
                if (Teleports.randomly(this)) {
                    return true;
                }
            }
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("Lvl", (short) this.lvl);
        output.putShort("ReAnimateResource", (short) this.reAnimateResource);
        output.putShort("SummonMinions", (short) this.summonMinion);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lvl = input.getShortOr("Lvl", (short) 0);
        this.reAnimateResource = input.getShortOr("ReAnimateResource", (short) 5);
        this.summonMinion = input.getShortOr("SummonMinions", (short) 1);
    }
}
