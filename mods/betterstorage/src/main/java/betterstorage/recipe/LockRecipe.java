package betterstorage.recipe;

import betterstorage.BS;
import betterstorage.item.KeyItem;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * LockRecipe: {@code  g  / gkg / gig} (gold, any key, iron) gives a lock with the key's id. The key stays in the grid
 * (it was its own container item). No other items may lie in the grid.
 */
public class LockRecipe extends ComboRecipe {
    public static final MapCodec<LockRecipe> CODEC = codec(LockRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, LockRecipe> STREAM_CODEC = streamCodec(CODEC);
    public static final RecipeSerializer<LockRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public LockRecipe(List<String> pattern, Map<String, Item> key) {
        super(pattern, key);
    }

    @Override
    public boolean checkShapelessItems(CraftingInput input, List<ItemStack> shapeless) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = new ItemStack(BS.LOCK.get());
        int id = KeyItem.keyId(find(input, BS.KEY.get()));
        if (id != 0) {
            result.set(BS.KEY_ID.get(), id);
        }
        return result;
    }

    @Override
    public RecipeSerializer<LockRecipe> getSerializer() {
        return SERIALIZER;
    }
}
