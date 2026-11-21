package tconstruct.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import tconstruct.entity.BlueSlime;
import tconstruct.entity.TEntities;

/**
 * SlimeFluid (BlockFluidClassic with Material.water, "liquid.slime"): on its ticks, 1 in 100, a blue slime climbs out
 * when no living entity is in the 3x3x3 around the block.
 */
public final class SlimeFluid {
    private SlimeFluid() {
    }

    static void spawnSlime(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0 && level.getEntitiesOfClass(LivingEntity.class, new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2)).isEmpty()) {
            BlueSlime slime = TEntities.BLUE_SLIME.get().create(level, EntitySpawnReason.TRIGGERED);
            if (slime != null) {
                slime.randomizeSize();
                slime.setPos(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
                level.addFreshEntity(slime);
            }
        }
    }

    public static final class Source extends BaseFlowingFluid.Source {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        protected boolean isRandomlyTicking() {
            return true;
        }

        @Override
        protected void randomTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource random) {
            spawnSlime(level, pos, random);
        }
    }

    public static final class Flowing extends BaseFlowingFluid.Flowing {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        protected boolean isRandomlyTicking() {
            return true;
        }

        @Override
        protected void randomTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource random) {
            spawnSlime(level, pos, random);
        }
    }
}
