package projectzulu.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jspecify.annotations.Nullable;
import projectzulu.ZuluPlants;

/**
 * BlockWateredDirt: metadata 0-3 dirt, 4-7 sand, each getting wetter under an aloe vera. Here {@code sandy} is bit 4
 * and {@code moisture} the low two bits. Gravel step sound for dirt, sand step sound for sand. The drops (dirt or sand,
 * and three water droplets when fully wet) are in the loot table.
 */
public class WateredDirtBlock extends Block {
    public static final IntegerProperty MOISTURE = IntegerProperty.create("moisture", 0, 3);
    public static final BooleanProperty SANDY = BooleanProperty.create("sandy");

    public WateredDirtBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 0).setValue(SANDY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE, SANDY);
    }

    /** The state of a 1.5.2 metadata value 0-7. */
    public static BlockState state(int meta) {
        return ZuluPlants.WATERED_DIRT.get().defaultBlockState().setValue(MOISTURE, meta & 3).setValue(SANDY, (meta & 4) != 0);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return state.getValue(SANDY) ? SoundType.SAND : SoundType.GRAVEL;
    }
}
