package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import twilightforest.TFBlocks;

/**
 * EntityTFMazeSlime: a slime of size 2 or 4 (children down to 1) with 2 × size² health that always hurts what it
 * touches, for size + 3. Mazestone bits instead of slime particles, quiet (0.1 × size), size + 3 experience. Spawns
 * in the dark on any block. Drops slimeballs at size 1, rarely a Charm of Keeping I.
 */
public class MazeSlime extends Slime {
    public MazeSlime(EntityType<? extends Slime> type, Level level) {
        super(type, level);
        this.setSize(1 << 1 + this.random.nextInt(2), true);
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        super.setSize(size, updateHealth);
        this.xpReward = size + 3;
    }

    @Override
    protected void setCubeMobHealth(int size) {
        net.minecraft.world.entity.ai.attributes.AttributeInstance health = this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(2 * size * size);
        }
    }

    @Override
    protected void setSpawnSize(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty) {
        this.setSize(1 << 1 + this.random.nextInt(2), true);
    }

    @Override
    protected boolean canDealDamage() {
        return this.isEffectiveAi();
    }

    @Override
    protected float getAttackDamage() {
        return this.getSize() + 3;
    }

    @Override
    protected ParticleOptions getParticleType() {
        return new BlockParticleOption(ParticleTypes.BLOCK, TFBlocks.MAZESTONES.get(1).get().defaultBlockState());
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F * this.getSize();
    }

    /** getCanSpawnHere: not on peaceful, no obstruction, monster light (isValidLightLevel). */
    public static boolean checkSpawnRules(EntityType<MazeSlime> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(level, pos, random);
    }

}
