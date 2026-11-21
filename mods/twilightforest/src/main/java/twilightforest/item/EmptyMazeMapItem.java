package twilightforest.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import twilightforest.TFMaps;

/**
 * ItemTFEmptyMazeMap (blank maze map 7261 + 256, blank ore map 7262 + 256): right click makes a maze (ore) map of
 * scale 0 centred on the player's position rounded to a multiple of 128 blocks plus 10, for the layer of the
 * player's feet, and awards "And Now, to Find the Exit" or "How Can That Be Worth It?".
 */
public class EmptyMazeMapItem extends Item {
    private final boolean mapOres;

    public EmptyMazeMapItem(Properties properties, boolean mapOres) {
        super(properties);
        this.mapOres = mapOres;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack map = new ItemStack(this.mapOres ? TFMaps.ORE_MAP.get() : TFMaps.MAZE_MAP.get());
        int step = 128;
        int centerX = (int) (Math.round(player.getX() / step) * step) + 10;
        int centerZ = (int) (Math.round(player.getZ() / step) * step) + 10;
        TFMaps.newMapData(server, map, centerX, centerZ, 0);
        map.set(TFMaps.MAZE_MAP_Y.get(), Mth.floor(player.getY()));
        return EmptyMagicMapItem.giveMap(stack, player, map, this, this.mapOres ? "ore_map" : "maze_map");
    }
}
