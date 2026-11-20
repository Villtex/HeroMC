package secretrooms.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** A block whose block entity copies a neighbour (BlockCamoFull.getIdCamoStyle and its variants). */
public interface CamoSource {
    @Nullable BlockState chooseCamo(Level level, BlockPos pos, BlockState state);

    /**
     * getInfo: nothing for air; the copy of another camo block; else the block itself if it is a full cube drawn by
     * a model (1.5.2: a normal or opaque cube or a standard-rendered block).
     */
    static @Nullable BlockState info(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return null;
        }
        if (level.getBlockEntity(pos) instanceof CamoBlockEntity camo) {
            return camo.camo();
        }
        if (state.getRenderShape() != RenderShape.MODEL || !Block.isShapeFullBlock(state.getShape(level, pos)) || state.hasBlockEntity()) {
            return null;
        }
        return state;
    }

    /**
     * BlockCamoFull.getIdCamoStyle: the only neighbour if there is one; else a block found on both sides along two
     * axes, or on both sides along one axis (y before x before z); else the first neighbour (1.5.2 took an arbitrary
     * one: BlockHolder.equals(Object) was always false, so its tally counted every neighbour once).
     */
    static @Nullable BlockState full(Level level, BlockPos pos) {
        BlockState[] h = {info(level, pos.below()), info(level, pos.above()), info(level, pos.west()), info(level, pos.east()),
                info(level, pos.north()), info(level, pos.south())};
        int count = 0;
        BlockState only = null;
        for (BlockState s : h) {
            if (s != null) {
                count++;
                only = s;
            }
        }
        if (count == 1) {
            return only;
        }
        BlockState px = h[2] != null && h[2] == h[3] ? h[2] : null;
        BlockState py = h[0] != null && h[0] == h[1] ? h[0] : null;
        BlockState pz = h[4] != null && h[4] == h[5] ? h[4] : null;
        if (px != null && px == pz) {
            return px;
        }
        if (px != null && px == py) {
            return px;
        }
        if (py != null && py == pz) {
            return py;
        }
        if (py != null) {
            return py;
        }
        if (px != null) {
            return px;
        }
        if (pz != null) {
            return pz;
        }
        for (BlockState s : h) {
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    /** BlockOneWay.getIdCamoStyle: the four neighbours around the facing axis; a matching pair wins, else the first. */
    static @Nullable BlockState around(Level level, BlockPos pos, Direction facing) {
        BlockState[] h = switch (facing.getAxis()) {
            case X -> new BlockState[]{info(level, pos.above()), info(level, pos.below()), info(level, pos.south()), info(level, pos.north())};
            case Y -> new BlockState[]{info(level, pos.east()), info(level, pos.west()), info(level, pos.south()), info(level, pos.north())};
            case Z -> new BlockState[]{info(level, pos.above()), info(level, pos.below()), info(level, pos.east()), info(level, pos.west())};
        };
        int count = 0;
        BlockState only = null;
        for (BlockState s : h) {
            if (s != null) {
                count++;
                only = s;
            }
        }
        if (count == 1) {
            return only;
        }
        if (h[0] != null && h[0] == h[1]) {
            return h[0];
        }
        if (h[2] != null && h[2] == h[3]) {
            return h[0];
        }
        for (BlockState s : h) {
            if (s != null) {
                return s;
            }
        }
        return null;
    }
}
