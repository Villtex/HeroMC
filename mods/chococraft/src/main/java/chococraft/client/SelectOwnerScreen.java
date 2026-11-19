package chococraft.client;

import chococraft.entity.AbstractChocobo;
import chococraft.network.ChocoNetwork;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.List;

/** GuiSelectNewOwner + GuiChangeOwner: one button per other player online (at most 10), the choice goes to the server. */
public class SelectOwnerScreen extends Screen {
    private final ChocopediaScreen parent;
    private final AbstractChocobo chocobo;

    public SelectOwnerScreen(ChocopediaScreen parent, AbstractChocobo chocobo) {
        super(Component.translatable("gui.chococraft.select_owner"));
        this.parent = parent;
        this.chocobo = chocobo;
    }

    @Override
    protected void init() {
        int y = 40;
        if (this.minecraft.getConnection() != null) {
            List<PlayerInfo> players = List.copyOf(this.minecraft.getConnection().getOnlinePlayers());
            for (PlayerInfo info : players) {
                String name = info.getProfile().name();
                if (name.equals(this.chocobo.getOwnerName()) || y > 40 + 9 * 22) {
                    continue;
                }
                this.addRenderableWidget(Button.builder(Component.literal(name), b -> {
                    this.parent.send(ChocoNetwork.CHANGE_OWNER, name);
                    this.minecraft.gui.setScreen(null);
                }).bounds(this.width / 2 - 100, y, 200, 20).build());
                y += 22;
            }
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.minecraft.gui.setScreen(this.parent))
                .bounds(this.width / 2 - 100, y + 10, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(this.font, this.getTitle(), this.width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
