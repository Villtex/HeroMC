package tconstruct;

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

import java.util.List;

/**
 * Teil 26e: pipes, hoppers and machines of other mods reach the inventories of this mod. Every block entity of the
 * mod that is a {@link Container} (IInventory of 1.5.2) offers {@code Capabilities.Item.BLOCK}; a {@link WorldlyContainer}
 * (ISidedInventory) keeps its side and slot rules. The containers' own checks (locks, valid items) stay in force. Tanks, the smeltery drain, casting blocks and channels offer {@code Capabilities.Fluid.BLOCK}.
 */
final class ItemCapabilities {
    private ItemCapabilities() {
    }

    static void register(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
            if (id == null || !TConstruct.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            BlockEntityType<BlockEntity> t = (BlockEntityType<BlockEntity>) type;
            event.registerBlockEntity(Capabilities.Item.BLOCK, t, (be, side) -> be instanceof WorldlyContainer worldly
                    ? new WorldlyContainerWrapper(worldly, side)
                    : be instanceof Container container ? VanillaContainerWrapper.of(container) : null);
        }
        // Smeltery drain, seared tank, casting table and basin, channel (Forge 1.5.2 ITankContainer)
        for (var type : List.of(tconstruct.TSmeltery.DRAIN_ENTITY.get(), tconstruct.TSmeltery.TANK_ENTITY.get(),
                tconstruct.TSmeltery.CASTING_ENTITY.get(), tconstruct.TSmeltery.CHANNEL_ENTITY.get())) {
            @SuppressWarnings("unchecked")
            BlockEntityType<BlockEntity> t = (BlockEntityType<BlockEntity>) (BlockEntityType<?>) type;
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, t, (be, side) -> be instanceof tconstruct.smeltery.LiquidContainer liquid
                    ? new tconstruct.smeltery.LiquidHandler(liquid, side) : null);
        }
    }
}
