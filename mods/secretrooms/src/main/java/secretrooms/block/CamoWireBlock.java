package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * BlockCamoWire: a camo block that carries redstone like wire in every direction (strong to all sides). Its power is
 * the strongest signal not coming from wires, or else the strongest neighbouring wire, camo wire, comparator output or
 * source minus one. As in 1.5.2 the vanilla wire is switched off by reflection while the outside signal is measured.
 */
public class CamoWireBlock extends CamoFullBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private static @Nullable Field wireSignal;
    private static boolean shouldPower = true;

    static {
        try {
            wireSignal = RedstoneWireBlock.class.getDeclaredField("shouldSignal");
            wireSignal.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            wireSignal = null;
        }
    }

    public CamoWireBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide()) {
            this.calcPower(level, pos, state);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moved) {
        if (!level.isClientSide()) {
            this.calcPower(level, pos, state);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        level.updateNeighborsAt(pos, this);
    }

    private static void wires(boolean on) {
        if (wireSignal != null) {
            try {
                wireSignal.setBoolean(Blocks.REDSTONE_WIRE, on);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private void calcPower(Level level, BlockPos pos, BlockState state) {
        int old = state.getValue(POWER);
        wires(false);
        shouldPower = false;
        int nonWire;
        try {
            nonWire = level.getBestNeighborSignal(pos);
        } finally {
            shouldPower = true;
            wires(true);
        }
        int power = this.inputPower(level, pos);
        if (nonWire > 0 && nonWire > power - 1) {
            power = nonWire;
        } else if (power > 0) {
            power--;
        }
        if (old != power) {
            level.setBlock(pos, state.setValue(POWER, power), 2);
            level.updateNeighborsAt(pos, this);
        }
    }

    private int inputPower(Level level, BlockPos pos) {
        int power = 0;
        for (Direction dir : Direction.values()) {
            BlockPos p = pos.relative(dir);
            BlockState s = level.getBlockState(p);
            if (s.isAir()) {
                continue;
            }
            int n = 0;
            if (s.is(Blocks.REDSTONE_WIRE) || s.is(this)) {
                n = s.getValue(POWER);
            } else if (s.hasAnalogOutputSignal()) {
                n = s.getAnalogOutputSignal(level, p, dir.getOpposite());
            } else if (s.isSignalSource()) {
                n = Math.max(s.getDirectSignal(level, p, dir), s.getSignal(level, p, dir));
            }
            power = Math.max(power, n);
        }
        return Math.max(power, level.getBestNeighborSignal(pos));
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return shouldPower;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return shouldPower ? state.getValue(POWER) : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return shouldPower ? state.getValue(POWER) : 0;
    }
}
