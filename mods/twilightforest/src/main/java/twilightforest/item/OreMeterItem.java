package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import twilightforest.TFBlocks;

/**
 * ItemTFOreMeter ("Ore Meter [WIP]"): counts stone, dirt, gravel, the ores and the roots in the chunks within 3 of the
 * player's and writes the share of each in the chat (English, as in 1.5.2).
 */
public class OreMeterItem extends Item {
    public OreMeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server) {
            countOreInArea(player, server, player.getBlockX(), player.getBlockZ(), 3);
        }
        return InteractionResult.PASS;
    }

    private static void countOreInArea(Player player, ServerLevel level, int useX, int useZ, int radius) {
        int chunkX = useX >> 4;
        int chunkZ = useZ >> 4;
        Block[] blocks = {Blocks.STONE, Blocks.DIRT, Blocks.GRAVEL, Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.DIAMOND_ORE,
                Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE, TFBlocks.ROOT.get(), TFBlocks.LIVEROOT_BLOCK.get()};
        int[] counts = new int[blocks.length];
        int exposedDiamond = 0;
        for (int cx = chunkX - radius; cx <= chunkX + radius; cx++) {
            for (int cz = chunkZ - radius; cz <= chunkZ + radius; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = level.getMinY(); y < level.getMaxY(); y++) {
                            pos.set((cx << 4) + x, y, (cz << 4) + z);
                            BlockState state = chunk.getBlockState(pos);
                            for (int i = 0; i < blocks.length; i++) {
                                if (state.is(blocks[i])) {
                                    counts[i]++;
                                }
                            }
                            if (state.is(Blocks.DIAMOND_ORE) && (level.isEmptyBlock(pos.east()) || level.isEmptyBlock(pos.west())
                                    || level.isEmptyBlock(pos.above()) || level.isEmptyBlock(pos.below()))) {
                                exposedDiamond++;
                            }
                        }
                    }
                }
            }
        }
        int total = 0;
        for (int c : counts) {
            total += c;
        }
        player.sendSystemMessage(Component.literal("Ore Meter!"));
        player.sendSystemMessage(Component.literal("Metering chunks in radius " + radius + " around chunk [" + chunkX + ", " + chunkZ + "]"));
        String[] names = {null, null, null, "Coal", "Iron", "Gold", "Diamond", "Lapis", "Redstone", "Roots", "Ore Roots"};
        for (int i = 3; i < names.length; i++) {
            String line = names[i] + " - " + counts[i] + " " + (float) counts[i] / total * 100.0F;
            if (i == 6) {
                line += ", exposed - " + exposedDiamond;
            }
            player.sendSystemMessage(Component.literal(line));
        }
    }
}
