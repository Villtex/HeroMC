package qcraft.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import qcraft.QuantumCraft;
import qcraft.item.QBlockItem;

import java.util.Arrays;

/**
 * EntangledQBlockRecipe: an essence of entanglement between two qBlocks of the same kind and sides (at most one of them
 * already entangled) gives two entangled qBlocks; nothing else in the grid. Frequency 0 until taken out of the grid.
 */
public class EntangledQBlockRecipe extends CustomRecipe {
    public static final EntangledQBlockRecipe INSTANCE = new EntangledQBlockRecipe();
    public static final RecipeSerializer<EntangledQBlockRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE),
            StreamCodec.unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !this.assemble(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return craft(input);
    }

    public static ItemStack craft(CraftingInput input) {
        int eosPosX = -1;
        int eosPosY = -1;
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                if (QBlockRecipe.get(input, x, y).is(QuantumCraft.EOE.get())) {
                    eosPosX = x;
                    eosPosY = y;
                    break;
                }
            }
        }
        if (eosPosX < 0) {
            return ItemStack.EMPTY;
        }
        int subType = -1;
        int[] types = null;
        int entanglementFrequency = -1;
        int odbsFound = 0;
        for (int x = 0; x < input.width(); x++) {
            for (int y = 0; y < input.height(); y++) {
                if (x == eosPosX && y == eosPosY) {
                    continue;
                }
                ItemStack item = QBlockRecipe.get(input, x, y);
                if ((x == eosPosX - 1 || x == eosPosX + 1) && y == eosPosY) {
                    if (!(item.getItem() instanceof QBlockItem)) {
                        return ItemStack.EMPTY;
                    }
                    if (odbsFound == 0) {
                        subType = QBlockItem.getSubType(item);
                        types = QBlockItem.getTypes(item);
                        entanglementFrequency = QBlockItem.getEntanglementFrequency(item);
                    } else {
                        int odbFrequency = QBlockItem.getEntanglementFrequency(item);
                        if (QBlockItem.getSubType(item) != subType || !Arrays.equals(QBlockItem.getTypes(item), types)
                                || entanglementFrequency >= 0 && odbFrequency >= 0) {
                            return ItemStack.EMPTY;
                        }
                        entanglementFrequency = odbFrequency >= 0 ? odbFrequency : entanglementFrequency;
                    }
                    odbsFound++;
                } else if (!item.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (types != null && odbsFound >= 2) {
            if (entanglementFrequency < 0) {
                entanglementFrequency = 0;
            }
            return QBlockItem.create(subType, types, entanglementFrequency, odbsFound);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.BUILDING;
    }

    @Override
    public RecipeSerializer<EntangledQBlockRecipe> getSerializer() {
        return SERIALIZER;
    }
}
