package harkenscythe.block;

import harkenscythe.HSBlocks;
import harkenscythe.HSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockHSCrops as the biomass plant: grows in four stages on tilled creep (metadata 1, rate 1) or blood-soaked creep
 * (metadata 2, rate 5; growing soaks up the blood). It needs light 8 or the open sky to stay. No bone meal. Ripe it
 * drops biomass and up to three seeds (each with 4 in 15), else a seed; the loot table does that.
 */
public class BiomassPlantBlock extends VegetationBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SHAPE = Block.box(4.8, 0, 4.8, 11.2, 9.6, 11.2);

    public BiomassPlantBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return CreepBlock.isCreep(state);
    }

    /** canBlockStay: on creep, with light 8 or open sky. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && CreepBlock.isCreep(level.getBlockState(pos.below()));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (level.getMaxLocalRawBrightness(pos.above()) < 9) {
            return;
        }
        int age = state.getValue(AGE);
        if (age < 3) {
            float rate = growthRate(level, pos);
            if (random.nextInt((int) (25.0F / rate) + 1) == 0) {
                BlockPos soilPos = pos.below();
                BlockState soil = level.getBlockState(soilPos);
                if (soil.getValue(CreepBlock.SOIL) == 2) {
                    level.levelEvent(2005, pos, 0);
                }
                level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
                level.setBlock(soilPos, soil.setValue(CreepBlock.SOIL, 1), Block.UPDATE_CLIENTS);
            }
        }
    }

    private float growthRate(ServerLevel level, BlockPos pos) {
        float rate = 1.0F;
        boolean x = this.is(level, pos.west()) || this.is(level, pos.east());
        boolean z = this.is(level, pos.north()) || this.is(level, pos.south());
        boolean diagonal = this.is(level, pos.offset(-1, 0, -1)) || this.is(level, pos.offset(1, 0, -1)) || this.is(level, pos.offset(1, 0, 1))
                || this.is(level, pos.offset(-1, 0, 1));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockState soil = level.getBlockState(pos.offset(dx, -1, dz));
                float f = 0.0F;
                if (CreepBlock.isCreep(soil)) {
                    int meta = soil.getValue(CreepBlock.SOIL);
                    if (meta == 1) {
                        f = 1.0F;
                    }
                    if (meta == 2) {
                        f = 5.0F;
                    }
                }
                if (dx != 0 || dz != 0) {
                    f /= 4.0F;
                }
                rate += f;
            }
        }
        if (diagonal || x && z) {
            rate /= 2.0F;
        }
        return rate;
    }

    private boolean is(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(HSBlocks.BIOMASS_PLANT.get());
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(HSItems.BIOMASS_SEED.get());
    }
}
