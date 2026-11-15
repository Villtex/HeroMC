package customlan.client;

import customlan.CLOptions;
import customlan.CustomLAN;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * GuiCustomLan: MOTD, online mode, PvP and the command permissions. The original also had the player limit and the
 * switches for flight, animals and NPCs; the integrated server of 26.3 has none of these settings any more.
 */
public final class GuiCustomLan extends Screen {
    private final Screen parent;
    private EditBox textfieldMOTD;
    private Button btnDone;
    private Button yesNoOnlineMode;
    private Button yesNoAllowPvP;

    public GuiCustomLan(Screen parent) {
        super(Component.literal("Custom LAN Options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CLOptions.checkOptions();
        int top = this.height / 6;
        int left = this.width / 2 - 155;
        textfieldMOTD = new EditBox(this.font, left, top - 5, 310, 20, Component.literal("MOTD"));
        textfieldMOTD.setMaxLength(59);
        textfieldMOTD.setValue(CLOptions.serverMOTD);
        textfieldMOTD.setResponder(s -> btnDone.active = !s.isEmpty());
        addRenderableWidget(textfieldMOTD);
        yesNoOnlineMode = addRenderableWidget(Button.builder(Component.empty(), b -> {
            CLOptions.serverOnlineMode = !CLOptions.serverOnlineMode;
            updateNames();
        }).bounds(left - 2, top + 19, 150, 20).build());
        yesNoAllowPvP = addRenderableWidget(Button.builder(Component.empty(), b -> {
            CLOptions.serverAllowPvP = !CLOptions.serverAllowPvP;
            updateNames();
        }).bounds(left + 160, top + 19, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Command Permissions"), b -> {
            save();
            Minecraft.getInstance().gui.setScreen(new GuiCustomLanPermissions(this, 0));
        }).bounds(left + 160, top + 43, 150, 20).build());
        btnDone = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            save();
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                server.execute(() -> CustomLAN.applyOptions(server));
            }
            Minecraft.getInstance().gui.setScreen(parent);
        }).bounds(this.width / 2 - 100, top + 168, 200, 20).build());
        updateNames();
    }

    private void updateNames() {
        yesNoOnlineMode.setMessage(Component.literal("Online Mode: " + (CLOptions.serverOnlineMode ? "ON" : "OFF")));
        yesNoAllowPvP.setMessage(Component.literal("Allow PvP: " + (CLOptions.serverAllowPvP ? "ON" : "OFF")));
    }

    private void save() {
        CLOptions.serverMOTD = textfieldMOTD.getValue();
        CLOptions.saveOptions();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        int top = this.height / 6;
        g.centeredText(this.font, "Custom LAN Options", this.width / 2, top - 30, 0xFFFFFFFF);
        g.centeredText(this.font, "MOTD", this.width / 2 - 80, top - 17, 0xFFA0A0A0);
    }

    @Override
    public void onClose() {
        save();
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
