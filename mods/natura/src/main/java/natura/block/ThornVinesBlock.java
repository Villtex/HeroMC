package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/** ThornVines: glowing nether vines that hurt (1/30 per tick inside: cactus or lava damage, 1 point). */
public class ThornVinesBlock extends VineBlock {
    public ThornVinesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (level instanceof ServerLevel server && !(entity instanceof ItemEntity) && !(entity instanceof Ghast)
                && level.getRandom().nextInt(30) == 0) {
            entity.hurtServer(server, level.getRandom().nextBoolean() ? level.damageSources().cactus() : level.damageSources().lava(), 1.0F);
        }
    }
}
