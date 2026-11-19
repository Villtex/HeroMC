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
import qcraft.block.Impostors;
import qcraft.item.QBlockItem;

/**
 * QBlockRecipe: an essence of observation (observer dependent block) or of superposition (quantum block) with up to six
 * impostor blocks around it, at least one. Relative to the essence: above = side 2 (north), below = 3 (south), left = 4
 * (west), right = 5 (east), upper left = 1 (top), lower left = 0 (bottom); the other two corners must stay empty.
 */
public class QBlockRecipe extends CustomRecipe {
    public static final QBlockRecipe INSTANCE = new QBlockRecipe();
    public static final RecipeSerializer<QBlockRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    static ItemStack get(CraftingInput input, int x, int y) {
        return x >= 0 && y >= 0 && x < input.width() && y < input.height() ? input.getItem(x, y) : ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !this.assemble(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return craft(input);
    }

    public static ItemStack craft(CraftingInput input) {
        int stonePosX = -1;
        int stonePosY = -1;
        int stoneType = -1;
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                ItemStack item = get(input, x, y);
                if (item.is(QuantumCraft.EOO.get()) || item.is(QuantumCraft.EOS.get())) {
                    stonePosX = x;
                    stonePosY = y;
                    stoneType = item.is(QuantumCraft.EOO.get()) ? 1 : 0;
                    break;
                }
            }
        }
        if (stonePosX < 0 || stonePosY < 0) {
            return ItemStack.EMPTY;
        }
        int numTypes = 0;
        int[] types = new int[6];
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                if (x == stonePosX && y == stonePosY) {
                    continue;
                }
                int type = Impostors.typeOf(get(input, x, y));
                if (type < 0) {
                    return ItemStack.EMPTY;
                }
                int lx = x - stonePosX;
                int ly = y - stonePosY;
                if (lx == 0 && ly == -1) {
                    types[2] = type;
                } else if (lx == 0 && ly == 1) {
                    types[3] = type;
                } else if (lx == -1 && ly == 0) {
                    types[4] = type;
                } else if (lx == 1 && ly == 0) {
                    types[5] = type;
                } else if (lx == -1 && ly == 1) {
                    types[0] = type;
                } else if (lx == -1 && ly == -1) {
                    types[1] = type;
                } else if (type != 0) {
                    return ItemStack.EMPTY;
                }
                if (type > 0) {
                    numTypes++;
                }
            }
        }
        if (numTypes > 0) {
            // essence of observation: observer dependent block; essence of superposition: quantum block
            return QBlockItem.create(stoneType == 1 ? 0 : 1, types, -1, 1);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.BUILDING;
    }

    @Override
    public RecipeSerializer<QBlockRecipe> getSerializer() {
        return SERIALIZER;
    }
}
