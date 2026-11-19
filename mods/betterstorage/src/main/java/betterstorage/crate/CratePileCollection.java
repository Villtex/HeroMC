package betterstorage.crate;

import betterstorage.BSUtil;
import betterstorage.BetterStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * CratePileCollection: all crate piles of one dimension. The original wrote every pile into its own file
 * (data/crates/&lt;id&gt;.dat of the dimension) and loaded them when a crate asked; here they are one saved data file
 * per dimension (data/betterstorage_crates.dat), saved with the world.
 */
public final class CratePileCollection extends SavedData {
    private static final int MAX_COUNT = 32767;
    public static final Codec<CratePileCollection> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("count", 0).forGetter(c -> c.count),
            CratePileData.CODEC.listOf().fieldOf("piles").forGetter(c -> List.copyOf(c.piles.values()))).apply(i, CratePileCollection::new));
    public static final SavedDataType<CratePileCollection> TYPE = new SavedDataType<>(BetterStorage.id("crates"), CratePileCollection::new, CODEC);

    private int count;
    private final Map<Integer, CratePileData> piles = new HashMap<>();

    public CratePileCollection() {
        this.count = BSUtil.RANDOM.nextInt(MAX_COUNT);
    }

    private CratePileCollection(int count, List<CratePileData> piles) {
        this.count = count;
        for (CratePileData data : piles) {
            data.collection = this;
            this.piles.put(data.id, data);
        }
    }

    public static CratePileCollection get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /** getCratePile: a pile that is not known (any more) starts empty. */
    public CratePileData getCratePile(int id) {
        return this.piles.computeIfAbsent(id, k -> {
            this.setDirty();
            return new CratePileData(this, k, 0);
        });
    }

    public CratePileData createCratePile() {
        while (this.piles.containsKey(this.count)) {
            this.count = (this.count + 1) % MAX_COUNT;
        }
        int id = this.count;
        this.count = (this.count + 1) % MAX_COUNT;
        CratePileData data = new CratePileData(this, id, 0);
        this.piles.put(id, data);
        this.setDirty();
        return data;
    }

    public void removeCratePile(CratePileData data) {
        this.piles.remove(data.id);
        this.setDirty();
    }
}
