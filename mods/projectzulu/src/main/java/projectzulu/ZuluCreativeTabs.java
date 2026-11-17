package projectzulu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * The "Project Zulu" creative tab of the original (icon: the campfire): every item of the mod, sorted by id. Spawn
 * eggs also go into the vanilla "Spawn Eggs" tab. Batches add extra stacks (potions, …) with {@link #addExtra}.
 */
public final class ZuluCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectZulu.MOD_ID);
    private static final List<BiConsumer<CreativeModeTab.ItemDisplayParameters, CreativeModeTab.Output>> EXTRA = new ArrayList<>();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + ProjectZulu.MOD_ID))
            .icon(() -> {
                Item campfire = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "campfire"));
                return new ItemStack(campfire == Items.AIR ? Items.CACTUS : campfire);
            })
            .displayItems((parameters, output) -> {
                BuiltInRegistries.ITEM.entrySet().stream()
                        .filter(e -> e.getKey().identifier().getNamespace().equals(ProjectZulu.MOD_ID))
                        .sorted(Comparator.comparing(e -> e.getKey().identifier().getPath()))
                        .forEach(e -> output.accept(e.getValue()));
                EXTRA.forEach(extra -> extra.accept(parameters, output));
            })
            .build());

    private ZuluCreativeTabs() {
    }

    /** Extra stacks for the Project Zulu tab (call during mod construction). */
    public static void addExtra(BiConsumer<CreativeModeTab.ItemDisplayParameters, CreativeModeTab.Output> extra) {
        EXTRA.add(extra);
    }

    private static void spawnEggs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.SPAWN_EGGS) {
            return;
        }
        BuiltInRegistries.ITEM.entrySet().stream()
                .filter(e -> e.getKey().identifier().getNamespace().equals(ProjectZulu.MOD_ID))
                .sorted(Comparator.comparing(e -> e.getKey().identifier().getPath()))
                .map(e -> (Item) e.getValue())
                .filter(item -> item instanceof SpawnEggItem)
                .forEach(event::accept);
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
        modBus.addListener(ZuluCreativeTabs::spawnEggs);
    }
}
