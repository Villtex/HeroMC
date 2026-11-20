package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jspecify.annotations.Nullable;
import secretrooms.SRBlocks;

/** BlockCamoLightDetector: a camo daylight sensor; every 20 ticks its weak power follows the sky light above it. */
public class CamoLightDetectorBlock extends CamoFullBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public CamoLightDetectorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CamoBlockEntity(SRBlocks.CAMO_DETECTOR_ENTITY.get(), pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, SRBlocks.CAMO_DETECTOR_ENTITY.get(), (l, p, s, be) -> {
            if (l.getGameTime() % 20L == 0L) {
                update(l, p, s);
            }
        });
    }

    /** updateLightLevel. */
    private static void update(Level level, BlockPos pos, BlockState state) {
        if (!level.dimensionType().hasSkyLight()) {
            return;
        }
        int light = level.getBrightness(LightLayer.SKY, pos.above()) - level.getSkyDarken();
        float angle = level.environmentAttributes().getValue(net.minecraft.world.attribute.EnvironmentAttributes.SUN_ANGLE, pos) * (float) (Math.PI / 180.0);
        if (angle < (float) Math.PI) {
            angle += (0.0F - angle) * 0.2F;
        } else {
            angle += ((float) (Math.PI * 2) - angle) * 0.2F;
        }
        light = Mth.clamp(Math.round(light * Mth.cos(angle)), 0, 15);
        if (state.getValue(POWER) != light) {
            level.setBlock(pos, state.setValue(POWER, light), 3);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }
}
