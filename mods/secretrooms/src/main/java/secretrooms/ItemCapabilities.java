package secretrooms;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

/**
 * Teil 26e: pipes, hoppers and machines of other mods reach the inventories of this mod. Every block entity of the
 * mod that is a {@link Container} (IInventory of 1.5.2) offers {@code Capabilities.Item.BLOCK}; a {@link WorldlyContainer}
 * (ISidedInventory) keeps its side and slot rules. The containers' own checks (locks, valid items) stay in force.
 */
final class ItemCapabilities {
    private ItemCapabilities() {
    }

    static void register(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
            if (id == null || !SecretRooms.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            BlockEntityType<BlockEntity> t = (BlockEntityType<BlockEntity>) type;
            event.registerBlockEntity(Capabilities.Item.BLOCK, t, (be, side) -> be instanceof WorldlyContainer worldly
                    ? new WorldlyContainerWrapper(worldly, side)
                    : be instanceof Container container ? VanillaContainerWrapper.of(container) : null);
        }
    }
}
