package twilightforest.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import twilightforest.TFEventListener;
import twilightforest.TFMaps;

/**
 * ItemTFEmptyMagicMap (item 7260 + 256): right click makes a magic map of scale 4 whose centre is the player's
 * position rounded to a multiple of 2048 blocks ({@code 128 << 4}), and awards "I Can See For Miles"
 * (TFAchievementPage.twilightMagicMap).
 */
public class EmptyMagicMapItem extends Item {
    public EmptyMagicMapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack map = new ItemStack(TFMaps.MAGIC_MAP.get());
        int step = 128 * (1 << MagicMapItem.SCALE);
        int centerX = (int) (Math.round(player.getX() / step) * step);
        int centerZ = (int) (Math.round(player.getZ() / step) * step);
        MapItemSavedData data = TFMaps.newMapData(server, map, centerX, centerZ, MagicMapItem.SCALE);
        MagicMapItem.mark(map, data);
        return giveMap(stack, player, map, this, "magic_map");
    }

    /** The end of onItemRightClick: one blank map used up, the map in its place or in the inventory. */
    static InteractionResult giveMap(ItemStack stack, Player player, ItemStack map, Item used, String advancement) {
        // 1.5.2 used the blank map up in creative mode too
        stack.shrink(1);
        player.awardStat(Stats.ITEM_USED.get(used));
        if (player instanceof ServerPlayer serverPlayer) {
            TFEventListener.award(serverPlayer, advancement);
        }
        if (stack.isEmpty()) {
            return InteractionResult.SUCCESS.heldItemTransformedTo(map);
        }
        if (!player.getInventory().add(map.copy())) {
            player.drop(map, false, Prediction.SERVER_ONLY);
        }
        return InteractionResult.SUCCESS;
    }
}
