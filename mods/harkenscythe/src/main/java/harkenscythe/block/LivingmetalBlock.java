package harkenscythe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** BlockHSLivingmetal: each random tick heals every hurt living thing within two blocks (upwards to the top of the world) by 1. */
public class LivingmetalBlock extends Block {
    public LivingmetalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AABB box = new AABB(pos).inflate(2.0);
        box = new AABB(box.minX, box.minY, box.minZ, box.maxX, level.getMaxY() + 1, box.maxZ);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(1.0F);
            }
        }
    }
}
