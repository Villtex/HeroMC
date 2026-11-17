package projectzulu.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluItems;
import projectzulu.ZuluMonsters;

/**
 * EntityMummyPharaoh, the boss of the pyramid (Teil 11 summons it with the jasper block): undead, holds an ankh, hunts
 * every player within 16 blocks. Health 200, speed 0.35, damage 5 (3/4/5/6), 0.6 x 1.8, with a boss bar ("Pharaoh
 * Health", BossHealthDisplayTicker). The fight has four stages; at 90 %, 70 % and 45 % health it teleports back to
 * where it first stood (2 blocks up, then down to the ground) and enters the next one. Armour 4/6/8/10 by stage.
 * <ul>
 * <li>stage 2: a large fireball every 120 ticks, dropped from 20 blocks above it onto the nearest player in 32 blocks;</li>
 * <li>stage 3: a mummy every 80 ticks, 5-8 blocks away, digging itself out of the ground (the two blocks at the top
 * there become air);</li>
 * <li>stage 4: both.</li>
 * </ul>
 * The timers run from the start, so a pending fireball or mummy comes at once when its stage begins. When a player
 * hits it from more than ~3 blocks it teleports next to him half the time, up close 1 in 5 times somewhere within 12
 * blocks. Hurt sound: the short mummy roar (louder via SoundHookContainerClass). Rare drop: an ankh.
 * The summoned variant of the jasper block (constructor with a position) was 0.6 x 1.4 and faster (0.4):
 * {@link #setSummoned()}.
 */
public class MummyPharaohEntity extends ZuluMonster {
    private static final EntityDimensions SUMMONED_SIZE = EntityDimensions.scalable(0.6F, 1.4F);

    private final ServerBossEvent bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random),
            Component.literal("Pharaoh Health"), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS);
    private @Nullable Vec3 startingPosition;
    private int stage = 1;
    private boolean spawnMummy;
    private final int spawnCooldown = 80;
    private int spawnTimer = 60;
    private boolean shootFireball;
    private final int shootCooldown = 120;
    private int shootTimer = 30;
    private boolean summoned;

    public MummyPharaohEntity(EntityType<? extends MummyPharaohEntity> type, Level level) {
        super(type, level);
        this.equipAnkh();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZuluMonster.createZuluAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 120));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new StateTargetGoal<>(this, Player.class, ALL_STATES));
    }

    /** getHeldItem: always the ankh; it is only shown, never dropped as equipment. */
    private void equipAnkh() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ZuluItems.ANKH.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.equipAnkh();
        return data;
    }

    /** The jasper-summoned pharaoh of the original (second constructor): 0.6 x 1.4 and speed 0.4. */
    public void setSummoned() {
        this.summoned = true;
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.4);
        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.summoned ? SUMMONED_SIZE : super.getDefaultDimensions(pose);
    }

    public int getStage() {
        return this.stage;
    }

    @Override
    public int getArmorValue() {
        return switch (this.stage) {
            case 1 -> 4;
            case 2 -> 6;
            case 3 -> 8;
            case 4 -> 10;
            default -> 2;
        };
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.startingPosition == null) {
            this.startingPosition = this.position();
        }
        float health = this.getHealth();
        float max = this.getMaxHealth();
        switch (this.stage) {
            case 1 -> {
                if (health < 0.9 * max) {
                    this.teleportToStart();
                    this.stage++;
                }
            }
            case 2 -> {
                if (this.shootFireball) {
                    this.shootFireballAtTarget();
                    this.shootFireball = false;
                }
                if (health < 0.7 * max) {
                    this.teleportToStart();
                    this.stage++;
                }
            }
            case 3 -> {
                if (this.spawnMummy) {
                    this.spawnMummy();
                    this.spawnMummy = false;
                }
                if (health < 0.45 * max) {
                    this.teleportToStart();
                    this.stage++;
                }
            }
            case 4 -> {
                if (this.spawnMummy) {
                    this.spawnMummy();
                    this.spawnMummy = false;
                }
                if (this.shootFireball) {
                    this.shootFireballAtTarget();
                    this.shootFireball = false;
                }
            }
            default -> {
            }
        }
        if (this.spawnTimer == 0) {
            this.spawnMummy = true;
            this.spawnTimer = this.spawnCooldown;
        }
        this.spawnTimer = Math.max(this.spawnTimer - 1, 0);
        if (this.shootTimer == 0) {
            this.shootFireball = true;
            this.shootTimer = this.shootCooldown;
        }
        this.shootTimer = Math.max(this.shootTimer - 1, 0);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private void teleportToStart() {
        if (this.startingPosition != null) {
            this.teleport(this.startingPosition.x, this.startingPosition.y + 2.0, this.startingPosition.z);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player player) {
            if (player.distanceToSqr(this) > 10.0) {
                if (this.random.nextInt(2) == 1) {
                    int x = Mth.floor(player.getX()) + 1;
                    int z = Mth.floor(player.getZ()) + 1;
                    this.teleport(player.getX() + 1.0, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), player.getZ() + 1.0);
                }
            } else if (this.random.nextInt(5) == 4) {
                double x = this.getX() + (this.random.nextDouble() - 0.5) * 24.0;
                double z = this.getZ() + (this.random.nextDouble() - 0.5) * 24.0;
                this.teleport(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z)), z);
            }
        }
        return super.hurtServer(level, source, damage);
    }

    /** spawnMummy: 5-8 blocks off on both axes, out of the ground. */
    private void spawnMummy() {
        ServerLevel level = (ServerLevel) this.level();
        int offsetX = this.random.nextInt(4) + 5;
        double x = this.random.nextInt(2) != 0 ? Math.floor(this.getX()) + offsetX : Math.floor(this.getX()) - offsetX;
        int offsetZ = this.random.nextInt(4) + 5;
        double z = this.random.nextInt(2) != 0 ? Math.floor(this.getZ()) + offsetZ : Math.floor(this.getZ()) - offsetZ;
        int bx = (int) x;
        int bz = (int) z;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz);
        if (!level.getBlockState(new BlockPos(bx, y - 2, bz)).isAir()) {
            level.setBlockAndUpdate(new BlockPos(bx, y, bz), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(bx, y - 1, bz), Blocks.AIR.defaultBlockState());
        }
        y--;
        MummyEntity mummy = ZuluMonsters.MUMMY.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mummy != null) {
            mummy.snapTo(x + 0.5, y, z + 0.5, 1.0F, 1.0F);
            level.addFreshEntity(mummy);
        }
    }

    /** shootFireballAtTarget: a ghast fireball from up to 5 blocks around, 20 blocks above the pharaoh. */
    private void shootFireballAtTarget() {
        Player target = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 32.0, true);
        if (target == null) {
            return;
        }
        double sourceX = this.getX() + (this.random.nextInt(10) - 5);
        double sourceY = this.getY() + 20.0;
        double sourceZ = this.getZ() + (this.random.nextInt(10) - 5);
        double dx = target.getX() - sourceX;
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (sourceY + this.getBbHeight() / 2.0F);
        double dz = target.getZ() - sourceZ;
        float yaw = -((float) Math.atan2(dx, dz)) * Mth.RAD_TO_DEG;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.level().levelEvent(null, LevelEvent.SOUND_GHAST_FIREBALL, this.blockPosition(), 0);
        // EntityFireball(world, shooter, dx, dy, dz) added a gaussian spread of 0.4 to the direction
        Vec3 direction = new Vec3(dx + this.random.nextGaussian() * 0.4, dy + this.random.nextGaussian() * 0.4,
                dz + this.random.nextGaussian() * 0.4);
        LargeFireball fireball = new LargeFireball(this.level(), this, direction, 1);
        Vec3 look = this.getViewVector(1.0F);
        fireball.setPos(sourceX + look.x, sourceY + this.getBbHeight() / 2.0F + 0.5, sourceZ + look.z);
        this.level().addFreshEntity(fireball);
    }

    /** teleportTo of the original: down to the first solid block, no collision, no liquid; ender sounds. */
    private boolean teleport(double x, double y, double z) {
        double oldX = this.getX();
        double oldY = this.getY();
        double oldZ = this.getZ();
        if (!this.randomTeleport(x, y, z, true, state -> false)) {
            return false;
        }
        this.level().playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZuluMonsters.MUMMY_SHORT_ROAR.get();
    }

    /** SoundHookContainerClass played the roar at volume 2 for the pharaoh. */
    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
    }

    /** dropRareDrop: an ankh, 5 blocks up (rare drop chance of 1.5.2: player kill, 5 in 200). */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (killedByPlayer && this.random.nextInt(200) < 5) {
            this.spawnAtLocation(level, new ItemStack(ZuluItems.ANKH.get()), 5.0F);
        }
    }

    // ---- boss bar (BossHealthDisplayTicker: shown while the pharaoh is rendered) ----

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    // ---- NBT: the fight state (the original lost it on reload) ----

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Stage", this.stage);
        output.putBoolean("Summoned", this.summoned);
        if (this.startingPosition != null) {
            output.putDouble("StartX", this.startingPosition.x);
            output.putDouble("StartY", this.startingPosition.y);
            output.putDouble("StartZ", this.startingPosition.z);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.stage = input.getIntOr("Stage", 1);
        if (input.getBooleanOr("Summoned", false)) {
            this.setSummoned();
        }
        if (!Double.isNaN(input.getDoubleOr("StartX", Double.NaN))) {
            this.startingPosition = new Vec3(input.getDoubleOr("StartX", 0.0), input.getDoubleOr("StartY", 0.0), input.getDoubleOr("StartZ", 0.0));
        }
    }
}
