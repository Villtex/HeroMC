package tconstruct.deco;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * SpeedBlock / SpeedSlab (brownstone): walking on it multiplies the sideways motion by 2.2 (the two roads: 2.7) while it
 * is below 0.5 (onEntityWalking).
 */
public final class SpeedBlock {
    private SpeedBlock() {
    }

    static void boost(Entity entity, boolean road) {
        double boost = road ? 2.7 : 2.2;
        Vec3 motion = entity.getDeltaMovement();
        double x = Math.abs(motion.x) < 0.5 ? motion.x * boost : motion.x;
        double z = Math.abs(motion.z) < 0.5 ? motion.z * boost : motion.z;
        entity.setDeltaMovement(x, motion.y, z);
    }

    public static class Full extends Block {
        private final boolean road;

        public Full(boolean road, Properties properties) {
            super(properties);
            this.road = road;
        }

        @Override
        public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
            boost(entity, this.road);
            super.stepOn(level, pos, state, entity);
        }
    }

    public static class Slab extends SlabBlock {
        private final boolean road;

        public Slab(boolean road, Properties properties) {
            super(properties);
            this.road = road;
        }

        @Override
        public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
            boost(entity, this.road);
            super.stepOn(level, pos, state, entity);
        }
    }
}
