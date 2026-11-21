package twilightforest.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;

/**
 * What ItemTFMagicMap and ItemTFMazeMap (both ItemMap of 1.5.2) share: a map without data gets new data around the
 * world spawn (getMapData), every tick the carrier is tracked on it (updateVisiblePlayers), and only the held map is
 * drawn (onUpdate with par5). The name carries the map number ("Magic Map #3", getItemDisplayName).
 */
public abstract class TFFilledMapItem extends MapItem {
    protected TFFilledMapItem(Properties properties) {
        super(properties);
    }

    /** getMapData of 1.5.2 when the stack has no data yet: centred on the world spawn. */
    protected abstract MapItemSavedData createDefaultData(ServerLevel level, ItemStack stack);

    /** onUpdate after the player has been tracked; {@code held} is par5 (the selected item). */
    protected abstract void tickMap(ItemStack stack, ServerLevel level, Entity owner, MapItemSavedData data, boolean held);

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        MapItemSavedData data = getSavedData(stack, level);
        if (data == null) {
            data = this.createDefaultData(level, stack);
        }
        if (owner instanceof Player player) {
            data.tickCarriedBy(player, stack, null);
        }
        this.tickMap(stack, level, owner, data, slot != null && slot.getType() == EquipmentSlot.Type.HAND);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        MapId id = stack.get(DataComponents.MAP_ID);
        return id == null ? name : name.copy().append(" #" + id.id());
    }
}
