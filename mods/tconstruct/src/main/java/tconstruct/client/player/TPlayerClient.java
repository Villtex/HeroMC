package tconstruct.client.player;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import tconstruct.TPlayer;
import tconstruct.client.manual.ManualScreen;
import tconstruct.player.PlayerItems;

import java.util.function.Consumer;

/** TControls and TProxyClient.addTabsToInventory: the armor key (O), the tabs on the vanilla inventory, the screens, the manual. */
public final class TPlayerClient {
    public static final KeyMapping ARMOR_KEY = new KeyMapping("key.tarmor", InputConstants.KEY_O, KeyMapping.Category.INVENTORY);

    private TPlayerClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterMenuScreensEvent.class, event -> {
            event.register(TPlayer.ARMOR_MENU.get(), ArmorExtendedScreen::new);
            event.register(TPlayer.KNAPSACK_MENU.get(), KnapsackScreen::new);
        });
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(ARMOR_KEY));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            Minecraft mc = Minecraft.getInstance();
            while (ARMOR_KEY.consumeClick()) {
                if (mc.level != null && mc.gui.screen() == null) {
                    send(1);
                }
            }
        });
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, event -> {
            if (event.getScreen().getClass() == InventoryScreen.class) {
                InventoryScreen screen = (InventoryScreen) event.getScreen();
                addTabs(screen, event::addListener, 2);
                if (TPlayer.clientKnapsack) {
                    event.addListener(knapsackTab(screen, true));
                }
            }
        });
        PlayerItems.openManual = stack -> Minecraft.getInstance().gui.setScreen(new ManualScreen(stack));
    }

    public static void send(int which) {
        ClientPacketDistributor.sendToServer(new TPlayer.OpenPayload(which));
    }

    /** Tab 2 (inventory): back to the vanilla inventory screen. */
    public static void openInventory(Minecraft mc) {
        if (mc.player != null) {
            mc.player.closeContainer();
            mc.gui.setScreen(new InventoryScreen(mc.player));
        }
    }

    /** The inventory and armor tabs; {@code current} (2 or 3) is drawn selected. */
    public static void addTabs(AbstractContainerScreen<?> screen, Consumer<InventoryTab> add, int current) {
        int x = screen.getLeftPos();
        int y = screen.getTopPos() - 28;
        add.accept(new InventoryTab(2, x, y, new ItemStack(Items.CRAFTING_TABLE), 0, current != 2));
        add.accept(new InventoryTab(3, x + 28, y, new ItemStack(Items.DIAMOND_CHESTPLATE), 1, current != 3));
    }

    public static InventoryTab knapsackTab(AbstractContainerScreen<?> screen, boolean enabled) {
        return new InventoryTab(4, screen.getLeftPos() + 56, screen.getTopPos() - 28, new ItemStack(TPlayer.KNAPSACK.get()), 1, enabled);
    }
}
