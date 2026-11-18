package betterdungeons.mob;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * EntitySecretBoss ("SlimeBoss", the Giant Slime of the slime caves): an EntitySlime of size lvl + 1 (0.6 per size)
 * whose health starts at (1 + lvl + difficulty) * 2 of size * size. When it dies it splits into two SlimeBosses of one
 * level less, and at level 1 into four ordinary slimes of a random size (1, 2 or 4). Drops 0..2 (+ looting) of one
 * random thing: slime balls, gold or iron ingots, coal, redstone or diamonds. Despawns like a slime.
 *
 * <p>The boss block of the dungeons makes it with level 9 (EntitySecretBoss(world, 9)), everything else (eggs,
 * /summon, the plain constructor) with level 0. The world generator creates it without finalizeSpawn, so a SlimeBoss
 * that is ticked or saved without a level yet takes level 9. "Size" in its data is the level, as in 1.5.2 (the same
 * number as the slime's own Size). On loading the health was reset to (1 + lvl + difficulty) * 2 (initBoss); so it is
 * here. Level 0 left two level -1 slimes in 1.5.2, which split on without end; here it leaves nothing.
 */
public class SlimeBoss extends Slime {
    private static final int UNSET = Integer.MIN_VALUE;
    private int lvl = UNSET;

    public SlimeBoss(EntityType<? extends SlimeBoss> type, Level level) {
        super(type, level);
    }

    public int getLvl() {
        return this.lvl;
    }

    /** EntitySecretBoss(world, lvl) / initBoss: size lvl + 1, xp lvl + 1, health (1 + lvl + difficulty) * 2. */
    public void initBoss(int lvl) {
        this.lvl = lvl;
        this.setSize(lvl + 1, false);
        this.setHealth((1 + lvl + this.level().getDifficulty().getId()) * 2);
    }

    /** The constructor EntitySecretBoss(world): size 1, health 1, 1 xp. */
    private void initPlain() {
        this.lvl = 0;
        this.setSize(1, true);
    }

    private void ensureLvl() {
        if (this.lvl == UNSET) {
            this.initBoss(9);
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        // the slime's random spawn size is replaced by the boss's own
        groupData = super.finalizeSpawn(level, difficulty, reason, groupData);
        if (this.lvl == UNSET) {
            this.initPlain();
        } else {
            this.initBoss(this.lvl);
        }
        return groupData;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            this.ensureLvl();
        }
        super.tick();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        this.ensureLvl();
        super.addAdditionalSaveData(output);
        output.putInt("Size", this.lvl);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.initBoss(input.getIntOr("Size", 0));
    }

    /** setDead -> setEntityDead: the children, then no vanilla split (size 1 before the slime's remove). */
    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel level && this.isDeadOrDying() && reason == RemovalReason.KILLED) {
            this.split(level);
        }
        if (this.getSize() > 1) {
            this.setSize(1, false);
        }
        super.remove(reason);
    }

    private void split(ServerLevel level) {
        if (this.lvl == 1) {
            int size = this.getSize();
            if (size > 1) {
                for (int j = 0; j < 4; j++) {
                    float f = (j % 2 - 0.5F) * size / 4.0F;
                    float f1 = (j / 2 - 0.5F) * size / 4.0F;
                    Slime slime = EntityTypes.SLIME.create(level, EntitySpawnReason.TRIGGERED);
                    if (slime != null) {
                        // new EntitySlime(world): size 1 << rand(3)
                        slime.setSize(1 << this.random.nextInt(3), true);
                        slime.snapTo(this.getX() + f, this.getY() + 0.5, this.getZ() + f1, this.random.nextFloat() * 360.0F, 0.0F);
                        level.addFreshEntity(slime);
                    }
                }
            }
        } else if (this.lvl > 1) {
            for (int j = 0; j < 2; j++) {
                float f = (j % 2 - 0.5F) * (this.lvl / 10) / 4.0F;
                float f1 = (j / 2 - 0.5F) * (this.lvl / 10) / 4.0F;
                SlimeBoss child = (SlimeBoss) this.getType().create(level, EntitySpawnReason.TRIGGERED);
                if (child != null) {
                    child.snapTo(this.getX() + f, this.getY() + 0.5, this.getZ() + f1, this.random.nextFloat() * 360.0F, 0.0F);
                    child.initBoss(this.lvl - 1);
                    level.addFreshEntity(child);
                }
            }
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }
}
