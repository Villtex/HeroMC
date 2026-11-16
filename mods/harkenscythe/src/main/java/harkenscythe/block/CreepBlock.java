package harkenscythe.block;

import harkenscythe.HSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockHSCreep: soul sand grown over by a creepball. {@link #SOIL} is the metadata: 0 plain, 1 tilled (by a glaive),
 * 2 tilled and soaked with blood (by a Bloodkeeper, the fertile soil of the biomass plant). In the dark under a
 * covering block it turns back into soul sand; in bright light it spreads onto soul sand nearby. Tilled soil without a
 * plant on it dries up step by step. It drops soul sand and slows like soul sand (collision 2/16 lower).
 */
public class CreepBlock extends Block {
    public static final IntegerProperty SOIL = IntegerProperty.create("soil", 0, 2);
    private static final VoxelShape COLLISION = Block.box(0, 0, 0, 16, 14, 16);

    public CreepBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SOIL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOIL);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return net.minecraft.world.phys.shapes.Shapes.block();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (level.getMaxLocalRawBrightness(above) < 4 && level.getBlockState(above).getLightDampening() > 2) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_SAND.defaultBlockState());
            return;
        }
        if (level.getMaxLocalRawBrightness(above) >= 9) {
            for (int i = 0; i < 4; i++) {
                BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                BlockPos targetAbove = target.above();
                if (level.getBlockState(target).is(Blocks.SOUL_SAND) && level.getMaxLocalRawBrightness(targetAbove) >= 4
                        && level.getBlockState(targetAbove).getLightDampening() <= 2) {
                    level.setBlockAndUpdate(target, this.defaultBlockState());
                }
            }
        }
        int soil = state.getValue(SOIL);
        if (soil >= 1 && !(level.getBlockState(above).getBlock() instanceof VegetationBlock)) {
            level.setBlock(pos, state.setValue(SOIL, soil - 1), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.MYCELIUM, pos.getX() + random.nextFloat(), pos.getY() + 1.1F, pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }

    public static boolean isCreep(BlockState state) {
        return state.is(HSBlocks.CREEP_BLOCK.get());
    }
}
