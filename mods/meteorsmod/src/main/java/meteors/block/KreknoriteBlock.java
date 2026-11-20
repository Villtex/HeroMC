package meteors.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/**
 * BlockKreknorite: a BlockMeteor (heat, glow, fire) that turns into obsidian when it cools down completely or touches
 * water on a side or on top; mined by a player, 1 of 100 releases a blaze. Diamond pickaxe at any heat.
 */
public class KreknoriteBlock extends MeteorBlock {
    private static final Direction[] HARDEN_SIDES = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};

    public KreknoriteBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return player.hasCorrectToolForDrops(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int heat = state.getValue(HEAT);
        if (heat > 0) {
            heat--;
            level.setBlock(pos, state.setValue(HEAT, heat), 2);
            if (heat <= 0) {
                level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 2);
                lavaMixEffects(level, pos);
            } else {
                this.checkForHarden(level, pos);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        this.checkForHarden(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean movedByPiston) {
        this.checkForHarden(level, pos);
    }

    private void checkForHarden(Level level, BlockPos pos) {
        if (level.isClientSide() || !level.getBlockState(pos).is(this)) {
            return;
        }
        for (Direction side : HARDEN_SIDES) {
            if (level.getFluidState(pos.relative(side)).is(FluidTags.WATER)) {
                level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 2);
                lavaMixEffects(level, pos);
                return;
            }
        }
    }

    /** triggerLavaMixEffects: fizz and eight puffs of large smoke. */
    private static void lavaMixEffects(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
        if (level instanceof ServerLevel server) {
            for (int l = 0; l < 8; l++) {
                server.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + 1.2, pos.getZ() + Math.random(), 1,
                        0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void releaseMob(ServerLevel level, BlockPos pos) {
        Blaze blaze = EntityTypes.BLAZE.create(level, EntitySpawnReason.EVENT);
        if (blaze != null) {
            blaze.snapTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
            level.addFreshEntity(blaze);
            blaze.spawnAnim();
        }
    }
}
