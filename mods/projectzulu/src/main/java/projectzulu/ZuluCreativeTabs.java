package projectzulu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.Comparator;

/**
 * Puts the items of Project Zulu into the vanilla creative tabs: the spawn eggs of all creature batches into
 * "Spawn Eggs", the creature drops into "Ingredients". The original had its own Project Zulu tab; that comes
 * with the blocks and items of Teil 11.
 */
public final class ZuluCreativeTabs {
    private ZuluCreativeTabs() {
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        boolean eggs = event.getTabKey() == CreativeModeTabs.SPAWN_EGGS;
        if (!eggs && event.getTabKey() != CreativeModeTabs.INGREDIENTS) {
            return;
        }
        BuiltInRegistries.ITEM.entrySet().stream()
                .filter(e -> e.getKey().identifier().getNamespace().equals(ProjectZulu.MOD_ID))
                .sorted(Comparator.comparing(e -> e.getKey().identifier().getPath()))
                .map(e -> (Item) e.getValue())
                .filter(item -> (item instanceof SpawnEggItem) == eggs)
                .forEach(event::accept);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ZuluCreativeTabs::creativeTab);
    }
}
