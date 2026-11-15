package customlan.client;

import customlan.CLPermissions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * GuiCustomLanPermissions and GuiCustomLanPermissions2: one button per command, "Everyone" or "OPs only", 14 on the
 * first page and 11 on the second; the arrow buttons of the original (texture cstmlan) are "&gt;" and "&lt;" buttons.
 */
public final class GuiCustomLanPermissions extends Screen {
    private static final int PER_PAGE = 14;
    private final Screen parent;
    private final int page;

    public GuiCustomLanPermissions(Screen parent, int page) {
        super(Component.literal("Command Permissions"));
        this.parent = parent;
        this.page = page;
    }

    @Override
    protected void init() {
        CLPermissions.checkOptions();
        int first = page * PER_PAGE;
        int last = Math.min(CLPermissions.KEYS.length, first + PER_PAGE);
        int top = this.height / 6 - 12;
        for (int i = first; i < last; i++) {
            String[] key = CLPermissions.KEYS[i];
            int slot = i - first;
            int x = this.width / 2 - 155 + slot % 2 * 160;
            int y = top + 24 * (slot / 2);
            addRenderableWidget(Button.builder(label(key), b -> {
                CLPermissions.toggle(key[0]);
                b.setMessage(label(key));
            }).bounds(x, y, 150, 20).build());
        }
        if (page == 0 && CLPermissions.KEYS.length > PER_PAGE) {
            addRenderableWidget(Button.builder(Component.literal(">"), b -> open(1)).bounds(this.width / 2 + 133, top + 24 * 7, 23, 20).build());
        } else if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> open(page - 1)).bounds(this.width / 2 - 155, top + 24 * 7, 23, 20).build());
        }
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> done()).bounds(this.width / 2 - 100, this.height / 6 + 168, 200, 20).build());
    }

    private static Component label(String[] key) {
        return Component.literal(key[2] + ": " + (CLPermissions.get(key[0]) == 0 ? "Everyone" : "OPs"));
    }

    private void open(int newPage) {
        CLPermissions.saveOptions();
        Minecraft.getInstance().gui.setScreen(new GuiCustomLanPermissions(parent, newPage));
    }

    private void done() {
        CLPermissions.saveOptions();
        // the command tree of every player depends on these permissions
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> {
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    server.getCommands().sendCommands(p);
                }
            });
        }
        Minecraft.getInstance().gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(this.font, "Command Permissions (" + (page + 1) + "/2)", this.width / 2, this.height / 6 - 30, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        done();
    }
}
