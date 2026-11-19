package qcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import qcraft.QuantumCraft;
import qcraft.block.AreaData;

import java.util.List;

/**
 * The item NBT of qCraft as data components: ItemQBlock "s0".."s5" and the damage (sub type), "e" (entanglement
 * frequency) and "R" (a random number that keeps freshly crafted entangled stacks apart until they get a frequency),
 * ItemQuantumComputer "e", "R" and "d" (stored area).
 */
public final class QComponents {
    /** Damage and "s0".."s5" of a qBlock: sub type (0 observer dependent, 1 quantum) and the six side types. */
    public record QBlockData(int subType, List<Integer> sides) {
        public static final Codec<QBlockData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("sub_type").forGetter(QBlockData::subType),
                Codec.INT.listOf(6, 6).fieldOf("sides").forGetter(QBlockData::sides)
        ).apply(i, QBlockData::new));
        public static final StreamCodec<ByteBuf, QBlockData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, QBlockData::subType,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(6)), QBlockData::sides,
                QBlockData::new);

        public int[] types() {
            int[] types = new int[6];
            for (int i = 0; i < 6; i++) {
                types[i] = this.sides.get(i);
            }
            return types;
        }

        public static QBlockData of(int subType, int[] types) {
            return new QBlockData(subType, List.of(types[0], types[1], types[2], types[3], types[4], types[5]));
        }
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<QBlockData>> QBLOCK = QuantumCraft.DATA_COMPONENTS.registerComponentType(
            "qblock", b -> b.persistent(QBlockData.CODEC).networkSynchronized(QBlockData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENTANGLEMENT = QuantumCraft.DATA_COMPONENTS.registerComponentType(
            "entanglement", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> NONCE = QuantumCraft.DATA_COMPONENTS.registerComponentType(
            "nonce", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AreaData>> STORED_AREA = QuantumCraft.DATA_COMPONENTS.registerComponentType(
            "stored_area", b -> b.persistent(AreaData.CODEC).networkSynchronized(AreaData.STREAM_CODEC));

    private QComponents() {
    }

    static void init() {
    }
}
