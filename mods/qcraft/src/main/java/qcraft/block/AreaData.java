package qcraft.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * TileEntityQuantumComputer.AreaData: the blocks of a stored area, y, then x, then z, relative to the computer. The
 * original kept block id and metadata per position ("blockData", "metaData"); here a palette of block states and one
 * index per position. The computer's own position holds air.
 */
public record AreaData(Shape shape, List<BlockState> palette, List<Integer> blocks) {
    public static final Codec<AreaData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Shape.CODEC.fieldOf("shape").forGetter(AreaData::shape),
            BlockState.CODEC.listOf().fieldOf("palette").forGetter(AreaData::palette),
            Codec.INT.listOf().fieldOf("blocks").forGetter(AreaData::blocks)
    ).apply(i, AreaData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AreaData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public BlockState block(int index) {
        return this.palette.get(this.blocks.get(index));
    }

    /** TileEntityQuantumComputer.AreaShape: offsets of the area from the computer. */
    public record Shape(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax) {
        public static final Codec<Shape> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("xmin").forGetter(Shape::xMin),
                Codec.INT.fieldOf("xmax").forGetter(Shape::xMax),
                Codec.INT.fieldOf("ymin").forGetter(Shape::yMin),
                Codec.INT.fieldOf("ymax").forGetter(Shape::yMax),
                Codec.INT.fieldOf("zmin").forGetter(Shape::zMin),
                Codec.INT.fieldOf("zmax").forGetter(Shape::zMax)
        ).apply(i, Shape::new));
    }
}
