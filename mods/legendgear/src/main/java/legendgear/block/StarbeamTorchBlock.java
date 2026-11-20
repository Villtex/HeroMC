package legendgear.block;

import legendgear.LGParticles;
import legendgear.entity.GrindStarEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockGrindNode, the starbeam torch: a torch with sparkles instead of flames. A player who falls onto one while
 * sneaking grinds along the line to the best aligned torch within 6 blocks ({@link GrindStarEntity}).
 */
public final class StarbeamTorchBlock {
    private StarbeamTorchBlock() {
    }

    static void sparkle(Level level, double x, double y, double z, RandomSource random) {
        LGParticles.sparkle(level, x + random.nextGaussian() * 0.08, y + random.nextGaussian() * 0.08 - 0.04, z + random.nextGaussian() * 0.08,
                0.0, 0.0, 0.0, 0.5F);
    }

    static void collide(Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Player player && player.fallDistance > 0.1 && player.getVehicle() == null && player.isShiftKeyDown()
                && !level.isClientSide()) {
            GrindStarEntity star = GrindStarEntity.tryMakingStar(level, pos, player);
            if (star != null) {
                level.addFreshEntity(star);
                player.startRiding(star, true, true);
            }
        }
    }

    public static class Standing extends TorchBlock {
        public Standing(Properties properties) {
            super(ParticleTypes.FLAME, properties);
        }

        @Override
        public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
            sparkle(level, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, random);
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
            collide(level, pos, entity);
        }
    }

    public static class Wall extends WallTorchBlock {
        public Wall(Properties properties) {
            super(ParticleTypes.FLAME, properties);
        }

        @Override
        public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
            Direction facing = state.getValue(FACING);
            Direction opposite = facing.getOpposite();
            sparkle(level, pos.getX() + 0.5 + 0.27 * opposite.getStepX(), pos.getY() + 0.7 + 0.22, pos.getZ() + 0.5 + 0.27 * opposite.getStepZ(),
                    random);
        }

        @Override
        protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
            collide(level, pos, entity);
        }
    }
}
