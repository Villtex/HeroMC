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
import qcraft.item.QuantumComputerItem;

/**
 * EntangledQuantumComputerRecipe: an essence of entanglement between two quantum computers that are not entangled gives
 * two entangled quantum computers (frequency 0 until taken out of the grid; stored areas are lost).
 */
public class EntangledComputerRecipe extends CustomRecipe {
    public static final EntangledComputerRecipe INSTANCE = new EntangledComputerRecipe();
    public static final RecipeSerializer<EntangledComputerRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE),
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
        int computersFound = 0;
        for (int x = 0; x < input.width(); x++) {
            for (int y = 0; y < input.height(); y++) {
                if (x == eosPosX && y == eosPosY) {
                    continue;
                }
                ItemStack item = QBlockRecipe.get(input, x, y);
                if ((x == eosPosX - 1 || x == eosPosX + 1) && y == eosPosY) {
                    if (!(item.getItem() instanceof QuantumComputerItem) || QuantumComputerItem.getEntanglementFrequency(item) >= 0) {
                        return ItemStack.EMPTY;
                    }
                    computersFound++;
                } else if (!item.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (computersFound != 2) {
            return ItemStack.EMPTY;
        }
        return QuantumComputerItem.create(0, computersFound);
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public RecipeSerializer<EntangledComputerRecipe> getSerializer() {
        return SERIALIZER;
    }
}
