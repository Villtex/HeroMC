package qcraft.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * BlockQBlock.getImpostorBlockList: the blocks a qBlock can look like, by type number (0 = nothing). The numbers are
 * stored in items and block entities and the quantum computer frame tests them (21 obsidian, 31 gold block), so the
 * order is that of the original. Metadata variants of 1.5.2 are the 26.3 blocks they became (sandstone 1/2 chiseled
 * and smooth = cut, quartz 1/2 chiseled and pillar); the 1.5.2 pumpkin always had a face, so it is the carved
 * pumpkin (face south, as {@code getIcon(side, 0)} drew it).
 */
public final class Impostors {
    private static final Block[] BLOCKS = {
            null,
            Blocks.STONE, Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.BEDROCK, Blocks.SAND, Blocks.GRAVEL, Blocks.GOLD_ORE,
            Blocks.IRON_ORE, Blocks.COAL_ORE, Blocks.OAK_LOG, Blocks.LAPIS_ORE, Blocks.SANDSTONE, Blocks.DIAMOND_ORE,
            Blocks.REDSTONE_ORE, Blocks.EMERALD_ORE, Blocks.ICE, Blocks.CLAY, Blocks.CARVED_PUMPKIN, Blocks.MELON,
            Blocks.MYCELIUM, Blocks.OBSIDIAN, Blocks.COBBLESTONE, Blocks.OAK_PLANKS, Blocks.BOOKSHELF,
            Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.GLOWSTONE, Blocks.END_STONE,
            Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.LAPIS_BLOCK, Blocks.WOOL.white(), Blocks.GLASS,
            Blocks.WOOL.orange(), Blocks.WOOL.magenta(), Blocks.WOOL.lightBlue(), Blocks.WOOL.yellow(), Blocks.WOOL.lime(),
            Blocks.WOOL.pink(), Blocks.WOOL.gray(), Blocks.WOOL.lightGray(), Blocks.WOOL.cyan(), Blocks.WOOL.purple(),
            Blocks.WOOL.blue(), Blocks.WOOL.brown(), Blocks.WOOL.green(), Blocks.WOOL.red(), Blocks.WOOL.black(),
            Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS,
            Blocks.JUNGLE_PLANKS, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.STONE_BRICKS,
            Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.NETHER_BRICKS,
            Blocks.BRICKS, Blocks.REDSTONE_BLOCK, Blocks.NETHER_QUARTZ_ORE, Blocks.QUARTZ_BLOCK,
            Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR
    };

    /** Number of types, 0 included. */
    public static final int COUNT = BLOCKS.length;
    public static final int GRASS = 2;
    public static final int ICE = 16;
    public static final int OBSIDIAN = 21;
    public static final int GOLD_BLOCK = 31;
    public static final int GLASS = 35;
    public static final int REDSTONE_BLOCK = 65;

    private Impostors() {
    }

    /** The impostor block of a type, null for 0 and unknown numbers. */
    public static @Nullable Block block(int type) {
        return type > 0 && type < COUNT ? BLOCKS[type] : null;
    }

    /** The impostor state (default state of the block); air for nothing. */
    public static BlockState state(int type) {
        Block block = block(type);
        if (block == Blocks.CARVED_PUMPKIN) {
            return block.defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,
                    net.minecraft.core.Direction.SOUTH);
        }
        return block == null ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
    }

    /** QBlockRecipe.getImpostorType: 0 for an empty slot, the type of a listed block, -1 for anything else. */
    public static int typeOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        for (int i = 1; i < COUNT; i++) {
            if (stack.is(BLOCKS[i].asItem())) {
                return i;
            }
        }
        return -1;
    }
}
