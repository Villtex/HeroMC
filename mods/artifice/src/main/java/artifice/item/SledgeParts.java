package artifice.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ArtificeRegistry.registerSledgeBlock / registerWildSledgeBlock as filled by compat/vanilla/Vanilla.loadSledges:
 * the parts a sledge breaks a block into. "Metadata" registrations (one metadata value, here {@code exact}) also cost
 * the sledge durability and work from a dispenser; "wild" registrations (every metadata value) do neither, as in
 * 1.5.2 (DispenserBehaviorSledge looked them up with the wrong key, ItemSledge.onBlockDestroyed only damaged on
 * metadata registrations). The blocks must also be in the block tag {@code artifice:sledge_breakable} (mining speed).
 */
public final class SledgeParts {
    /** Parts of one block; every stack is one roll of the loss chance. */
    public record Entry(List<ItemStack> parts, boolean exact) {
    }

    private static @Nullable Map<Block, Entry> map;

    private SledgeParts() {
    }

    private static List<ItemStack> parts(Object... countsAndItems) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < countsAndItems.length; i += 2) {
            list.add(new ItemStack((ItemLike) countsAndItems[i + 1], (Integer) countsAndItems[i]));
        }
        return List.copyOf(list);
    }

    private static void exact(Map<Block, Entry> m, Block block, List<ItemStack> parts) {
        m.put(block, new Entry(parts, true));
    }

    private static void wild(Map<Block, Entry> m, Block block, List<ItemStack> parts) {
        m.put(block, new Entry(parts, false));
    }

    /** All registered blocks (in registration order of Vanilla.loadSledges). */
    public static synchronized Map<Block, Entry> all() {
        if (map != null) {
            return map;
        }
        Map<Block, Entry> m = new LinkedHashMap<>();
        // 1.5.2 planks metadata 0 and wooden slab metadata 0 are oak
        exact(m, Blocks.CRAFTING_TABLE, parts(4, Blocks.OAK_PLANKS));
        wild(m, Blocks.FURNACE, parts(8, Blocks.COBBLESTONE));
        wild(m, Blocks.CHEST, parts(8, Blocks.OAK_PLANKS));
        // Block.cloth (every colour) -> string
        for (DyeColor color : DyeColor.values()) {
            wild(m, wool(color), parts(4, Items.STRING));
        }
        exact(m, Blocks.TNT, parts(4, Blocks.SAND, 5, Items.GUNPOWDER));
        exact(m, Blocks.BRICKS, parts(4, Items.BRICK));
        exact(m, Blocks.NETHER_BRICKS, parts(4, Items.NETHER_BRICK));
        exact(m, Blocks.BOOKSHELF, parts(6, Blocks.OAK_PLANKS, 3, Items.BOOK));
        exact(m, Blocks.SANDSTONE, parts(4, Blocks.SAND));
        exact(m, Blocks.QUARTZ_BLOCK, parts(4, Items.QUARTZ));
        // the 1.5.2 pumpkin is the carved one of today
        wild(m, Blocks.JACK_O_LANTERN, parts(1, Blocks.CARVED_PUMPKIN, 1, Blocks.TORCH));
        List<ItemStack> anvil = parts(1, Blocks.IRON_BLOCK, 1, Blocks.IRON_BLOCK, 1, Blocks.IRON_BLOCK, 1, Items.IRON_INGOT, 1, Items.IRON_INGOT,
                1, Items.IRON_INGOT, 1, Items.IRON_INGOT);
        wild(m, Blocks.ANVIL, anvil);
        wild(m, Blocks.CHIPPED_ANVIL, anvil);
        wild(m, Blocks.DAMAGED_ANVIL, anvil);
        wild(m, Blocks.BEACON, parts(1, Items.NETHER_STAR, 3, Blocks.OBSIDIAN));
        wild(m, Blocks.NOTE_BLOCK, parts(8, Blocks.OAK_PLANKS, 1, Items.REDSTONE));
        wild(m, Blocks.JUKEBOX, parts(8, Blocks.OAK_PLANKS, 1, Items.DIAMOND));
        wild(m, Blocks.DISPENSER, parts(7, Blocks.COBBLESTONE, 1, Items.BOW, 1, Items.REDSTONE));
        wild(m, Blocks.DROPPER, parts(7, Blocks.COBBLESTONE, 1, Items.REDSTONE));
        wild(m, Blocks.PISTON, parts(3, Blocks.OAK_PLANKS, 4, Blocks.COBBLESTONE, 1, Items.IRON_INGOT, 1, Items.REDSTONE));
        wild(m, Blocks.STICKY_PISTON, parts(1, Blocks.PISTON, 1, Items.SLIME_BALL));
        // redstoneLampIdle and redstoneLampActive (one block with LIT today)
        exact(m, Blocks.REDSTONE_LAMP, parts(4, Items.REDSTONE, 4, Items.GLOWSTONE_DUST));
        exact(m, Blocks.DAYLIGHT_DETECTOR, parts(3, Items.QUARTZ, 3, Blocks.OAK_SLAB));
        wild(m, Blocks.HOPPER, parts(8, Blocks.OAK_PLANKS, 5, Items.IRON_INGOT));
        wild(m, Blocks.TRAPPED_CHEST, parts(8, Blocks.OAK_PLANKS, 1, Blocks.TRIPWIRE_HOOK));
        wild(m, Blocks.OAK_DOOR, parts(6, Blocks.OAK_PLANKS));
        wild(m, Blocks.IRON_DOOR, parts(6, Items.IRON_INGOT));
        wild(m, Blocks.REPEATER, parts(3, Blocks.STONE, 2, Blocks.REDSTONE_TORCH, 1, Items.REDSTONE));
        wild(m, Blocks.COMPARATOR, parts(3, Blocks.REDSTONE_TORCH, 3, Blocks.STONE, 1, Items.QUARTZ));
        wild(m, Blocks.OAK_FENCE_GATE, parts(2, Blocks.OAK_PLANKS, 4, Items.STICK));
        // Block.bed was white; the coloured beds of today give their own wool
        for (DyeColor color : DyeColor.values()) {
            wild(m, bed(color), parts(3, wool(color), 3, Blocks.OAK_PLANKS));
        }
        wild(m, Blocks.ENCHANTING_TABLE, parts(1, Items.BOOK, 2, Items.DIAMOND, 4, Blocks.OBSIDIAN));
        // the cauldron of 1.5.2 held its water as metadata
        List<ItemStack> cauldron = parts(7, Items.IRON_INGOT);
        wild(m, Blocks.CAULDRON, cauldron);
        wild(m, Blocks.WATER_CAULDRON, cauldron);
        wild(m, Blocks.LAVA_CAULDRON, cauldron);
        wild(m, Blocks.POWDER_SNOW_CAULDRON, cauldron);
        wild(m, Blocks.BREWING_STAND, parts(1, Items.BLAZE_ROD, 3, Blocks.COBBLESTONE));
        map = m;
        return m;
    }

    private static Block wool(DyeColor color) {
        return Blocks.WOOL.pick(color);
    }

    private static Block bed(DyeColor color) {
        return Blocks.BED.pick(color);
    }

    public static @Nullable Entry get(BlockState state) {
        return all().get(state.getBlock());
    }
}
