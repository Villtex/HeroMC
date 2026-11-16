package harkenscythe.block;

import harkenscythe.entity.SoulEntity;
import harkenscythe.entity.LostSoulEntity;
import harkenscythe.entity.SpectralPets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockHSSoulLight: the invisible light (level 7) a soul leaves where it floats. A random tick removes it unless a
 * soul, a lost soul or a spectral miner holding a torch is within a block of it (upwards to the top of the world).
 * It only stays on a solid opaque block (or leaves), like snow. 1.5.2 also removed it on the client at every random
 * display tick (so the player rarely saw the light); the port keeps the light the server has.
 */
public class SoulLightBlock extends Block {
    public SoulLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    /** canPlaceBlockAt: on leaves or an opaque block whose material blocks movement. */
    public static boolean canStayAbove(LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return (below.is(net.minecraft.tags.BlockTags.LEAVES) || below.canOcclude()) && below.isSolid();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canStayAbove(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AABB box = new AABB(pos).inflate(1.0);
        box = new AABB(box.minX, box.minY, box.minZ, box.maxX, level.getMaxY() + 1, box.maxZ);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity instanceof SoulEntity || entity instanceof LostSoulEntity || SpectralPets.keepsSoulLight(entity)) {
                return;
            }
        }
        level.removeBlock(pos, false);
    }
}
