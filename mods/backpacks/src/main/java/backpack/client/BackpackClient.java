package backpack.client;

import backpack.Backpack;
import backpack.BackpackItem;
import backpack.network.BackpackNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import com.mojang.blaze3d.platform.InputConstants;

/** ClientProxy: screens, the "Open Backpack" key (B, KeyHandlerBackpack) and the backpack model in the chest slot. */
public final class BackpackClient {
    /** KeyHandlerBackpack: key 48 of LWJGL 2 is B. */
    public static final KeyMapping OPEN = new KeyMapping("key.backpack.open", InputConstants.KEY_B, KeyMapping.Category.INVENTORY);

    private BackpackClient() {
    }

    public static void init(IEventBus modBus) {
        BackpackItem.openRenameScreen = () -> Minecraft.getInstance().gui.setScreen(new RenameScreen());
        modBus.addListener(RegisterMenuScreensEvent.class, e -> {
            e.register(Backpack.BACKPACK_MENU.get(), BackpackScreen::new);
            e.register(Backpack.WORKBENCH_BACKPACK_MENU.get(), WorkbenchBackpackScreen::new);
        });
        modBus.addListener(RegisterKeyMappingsEvent.class, e -> e.register(OPEN));
        modBus.addListener(RegisterClientExtensionsEvent.class, e -> e.registerItem(new IClientItemExtensions() {
            private HumanoidModel<HumanoidRenderState> model;

            /** getArmorModel: ModelBackpack in the chest slot. */
            @Override
            public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
                if (layerType != EquipmentClientInfo.LayerType.HUMANOID) {
                    return original;
                }
                if (this.model == null) {
                    this.model = BackpackModel.bake();
                }
                return this.model;
            }
        }, Backpack.backpacks().toArray(BackpackItem[]::new)));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> tick());
    }

    /** keyDown: only in game (no screen open) and with a backpack in the chest slot; the server opens it. */
    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN.consumeClick()) {
            if (mc.player != null && mc.gui.screen() == null && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BackpackItem) {
                ClientPacketDistributor.sendToServer(BackpackNetwork.OpenBackpack.INSTANCE);
            }
        }
    }
}
