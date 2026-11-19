package betterstorage.backpack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * PropertiesBackpack: what an entity carries in its worn backpack ({@link #contents}, null until first opened) and
 * whether it spawns with one. The lid angle and the number of players using it live here too (not saved).
 */
public final class BackpackData {
    private record Saved(Optional<List<ItemStack>> contents, boolean spawnsWithBackpack) {
    }

    public static final com.mojang.serialization.MapCodec<BackpackData> CODEC = RecordCodecBuilder.<Saved>mapCodec(i -> i.group(
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("Items").forGetter(Saved::contents),
            Codec.BOOL.optionalFieldOf("spawnsWithBackpack", false).forGetter(Saved::spawnsWithBackpack)).apply(i, Saved::new))
            .xmap(BackpackData::fromSaved, BackpackData::toSaved);

    public @Nullable NonNullList<ItemStack> contents;
    public boolean spawnsWithBackpack;
    public int playersUsing;
    public float lidAngle;
    public float prevLidAngle;

    private static BackpackData fromSaved(Saved saved) {
        BackpackData data = new BackpackData();
        saved.contents().ifPresent(items -> {
            data.contents = NonNullList.withSize(items.size(), ItemStack.EMPTY);
            for (int i = 0; i < items.size(); i++) {
                data.contents.set(i, items.get(i));
            }
        });
        data.spawnsWithBackpack = saved.spawnsWithBackpack();
        return data;
    }

    private Saved toSaved() {
        return new Saved(Optional.ofNullable(this.contents).map(List::copyOf), this.spawnsWithBackpack);
    }

    public boolean shouldSave() {
        return this.contents != null || this.spawnsWithBackpack;
    }

    public boolean hasItems() {
        if (this.contents != null) {
            for (ItemStack stack : this.contents) {
                if (!stack.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
}
