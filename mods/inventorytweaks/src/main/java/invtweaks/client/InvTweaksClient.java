package invtweaks.client;

import com.mojang.blaze3d.platform.InputConstants;
import invtweaks.InvTweaksMod;
import invtweaks.client.gui.InvTweaksGuiIconButton;
import invtweaks.client.gui.InvTweaksGuiSettingsButton;
import invtweaks.client.gui.InvTweaksGuiSortingButton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * ClientProxy, ForgeClientTick and InvTweaks.handleGUILayout: registers the tick, the mouse handling in container
 * screens (shortcuts, middle click) and the buttons on chests and inventories.
 */
public final class InvTweaksClient {
    /** Chest-like screens of other mods (IMC "chest"), class name to row size. */
    private static final Map<String, Integer> SPECIAL_CHESTS = new ConcurrentHashMap<>();

    private InvTweaksClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(FMLClientSetupEvent.class, event -> new InvTweaks(Minecraft.getInstance()));
        modBus.addListener(InterModProcessEvent.class, event -> InterModComms.getMessages(InvTweaksMod.MOD_ID, "chest"::equals)
                .forEach(message -> registerChest(String.valueOf(message.messageSupplier().get()))));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, event -> {
            if (InvTweaks.getInstance() != null) {
                InvTweaks.getInstance().onTickStart();
            }
        });
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            if (InvTweaks.getInstance() != null) {
                InvTweaks.getInstance().onTick();
            }
        });
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, InvTweaksClient::initScreen);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Pre.class, InvTweaksClient::renderScreen);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.MouseButtonPressed.Pre.class, InvTweaksClient::mousePressed);
    }

    private static void registerChest(String spec) {
        String[] parts = spec.split(";");
        int rowSize = 9;
        if (parts.length > 1) {
            try {
                rowSize = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                InvTweaks.log.warn("Invalid row size in chest registration {}", spec);
            }
        }
        SPECIAL_CHESTS.put(parts[0].trim(), rowSize);
    }

    private static Integer specialChestRowSize(Screen screen) {
        for (Class<?> c = screen.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            Integer rowSize = SPECIAL_CHESTS.get(c.getName());
            if (rowSize != null) {
                return rowSize;
            }
        }
        return null;
    }

    // --- screen classification (InvTweaksObfuscation.isValidChest / isStandardInventory) -------------------------

    public static boolean isValidChest(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen) || screen instanceof CreativeModeInventoryScreen) {
            return false;
        }
        AbstractContainerMenu menu = containerScreen.getMenu();
        return menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu || menu instanceof DispenserMenu || specialChestRowSize(screen) != null;
    }

    public static boolean isStandardInventory(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CraftingScreen || screen instanceof AbstractFurnaceScreen<?>
                || screen instanceof BrewingStandScreen || screen instanceof EnchantmentScreen || screen instanceof MerchantScreen
                || screen instanceof AnvilScreen || screen instanceof BeaconScreen || screen instanceof HopperScreen
                // workstations that came after 1.5.2, treated like the anvil
                || screen instanceof ItemCombinerScreen<?> || screen instanceof GrindstoneScreen || screen instanceof StonecutterScreen
                || screen instanceof LoomScreen || screen instanceof CartographyTableScreen;
    }

    public static boolean isValidInventory(Screen screen) {
        return isStandardInventory(screen);
    }

    public static int getContainerRowSize(AbstractContainerScreen<?> screen) {
        Integer special = specialChestRowSize(screen);
        if (special != null) {
            return special;
        }
        return screen.getMenu() instanceof DispenserMenu ? 3 : 9;
    }

    // --- events ---------------------------------------------------------------------------------------------------

    /** handleGUILayout: "..." settings button on inventories; on chests also the three sorting buttons. */
    private static void initScreen(ScreenEvent.Init.Post event) {
        InvTweaks invTweaks = InvTweaks.getInstance();
        if (invTweaks == null || Minecraft.getInstance().player == null
                || !(event.getScreen() instanceof AbstractContainerScreen<?> screen) || screen instanceof CreativeModeInventoryScreen) {
            return;
        }
        boolean chest = isValidChest(screen);
        if (!chest && (!isStandardInventory(screen) || screen instanceof EnchantmentScreen)) {
            return;
        }
        InvTweaksConfigManager manager = InvTweaks.getConfigManager();
        if (!manager.makeSureConfigurationIsLoaded()) {
            return;
        }
        InvTweaksConfig config = manager.getConfig();
        int width = 10;
        int height = 10;
        if (!chest) {
            event.addListener(new InvTweaksGuiSettingsButton(screen, -15, 5, width, height));
        } else {
            invTweaks.resetChestAlgorithm();
            event.addListener(new InvTweaksGuiSettingsButton(screen, -16 - 1, 5, width, height));
            if (!config.getProperty(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS).equals(InvTweaksConfig.VALUE_FALSE)) {
                event.addListener(new InvTweaksGuiSortingButton(screen, -16 - 13, 5, width, height, "h",
                        InvTweaksLocalization.get("invtweaks.button.chest3.tooltip"), InvTweaksHandlerSorting.ALGORITHM_HORIZONTAL));
                event.addListener(new InvTweaksGuiSortingButton(screen, -16 - 25, 5, width, height, "v",
                        InvTweaksLocalization.get("invtweaks.button.chest2.tooltip"), InvTweaksHandlerSorting.ALGORITHM_VERTICAL));
                event.addListener(new InvTweaksGuiSortingButton(screen, -16 - 37, 5, width, height, "s",
                        InvTweaksLocalization.get("invtweaks.button.chest1.tooltip"), InvTweaksHandlerSorting.ALGORITHM_DEFAULT));
            }
        }
    }

    /** The buttons follow the GUI when it moves (recipe book). */
    private static void renderScreen(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            for (GuiEventListener child : screen.children()) {
                if (child instanceof InvTweaksGuiIconButton button) {
                    button.reposition(screen);
                }
            }
        }
    }

    /** InvTweaks.handleShortcuts and handleMiddleClick. */
    private static void mousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        InvTweaks invTweaks = InvTweaks.getInstance();
        if (invTweaks == null || Minecraft.getInstance().player == null
                || !(event.getScreen() instanceof AbstractContainerScreen<?> screen) || screen instanceof CreativeModeInventoryScreen) {
            return;
        }
        if (!isValidChest(screen) && !isStandardInventory(screen)) {
            return;
        }
        int button = event.getButton();
        if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
            if (invTweaks.handleMiddleClick(screen)) {
                event.setCanceled(true);
            }
        } else if (button == InputConstants.MOUSE_BUTTON_LEFT || button == InputConstants.MOUSE_BUTTON_RIGHT) {
            for (GuiEventListener child : screen.children()) {
                if (child instanceof InvTweaksGuiIconButton && child.isMouseOver(event.getMouseX(), event.getMouseY())) {
                    return;
                }
            }
            if (invTweaks.handleShortcut(button == InputConstants.MOUSE_BUTTON_RIGHT)) {
                event.setCanceled(true);
            }
        }
    }
}
