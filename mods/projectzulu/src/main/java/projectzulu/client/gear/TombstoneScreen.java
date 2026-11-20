package projectzulu.client.gear;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.mojang.blaze3d.platform.InputConstants;
import projectzulu.block.gear.TombstoneBlockEntity;
import projectzulu.network.gear.TombstoneTextPayload;

import java.util.Arrays;
import java.util.List;

/**
 * GuiTombstone: "Edit sign message:", four lines of up to 10 characters (up/down/enter change the line, the edited
 * line blinks between "> <"), "Done". On closing the lines go to the server. The original drew the tombstone model
 * behind the text; here the lines stand on a stone-grey plate.
 */
public class TombstoneScreen extends Screen {
    private final TombstoneBlockEntity tombstone;
    private final String[] lines = new String[TombstoneBlockEntity.LINES];
    private int updateCounter;
    private int editLine;

    public TombstoneScreen(TombstoneBlockEntity tombstone) {
        super(Component.literal("Edit sign message:"));
        this.tombstone = tombstone;
        for (int i = 0; i < this.lines.length; i++) {
            this.lines[i] = tombstone.getLine(i);
        }
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height / 4 + 120, 200, 20).build());
    }

    @Override
    public void removed() {
        for (int i = 0; i < this.lines.length; i++) {
            this.tombstone.setLine(i, this.lines[i]);
        }
        ClientPacketDistributor.sendToServer(new TombstoneTextPayload(this.tombstone.getBlockPos(), List.copyOf(Arrays.asList(this.lines))));
    }

    @Override
    public void tick() {
        this.updateCounter++;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == InputConstants.KEY_UP) {
            this.editLine = this.editLine - 1 & 3;
            return true;
        }
        if (key == InputConstants.KEY_DOWN || key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) {
            this.editLine = this.editLine + 1 & 3;
            return true;
        }
        if (key == InputConstants.KEY_BACKSPACE) {
            String line = this.lines[this.editLine];
            if (!line.isEmpty()) {
                this.lines[this.editLine] = line.substring(0, line.length() - 1);
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (event.isAllowedChatCharacter() && this.lines[this.editLine].length() < TombstoneBlockEntity.MAX_CHARS) {
            this.lines[this.editLine] += event.codepointAsString();
            return true;
        }
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title.getString(), this.width / 2, 40, 0xFFFFFFFF);
        int centerX = this.width / 2;
        int top = 60;
        graphics.fill(centerX - 50, top, centerX + 50, top + 64, 0xFF8A8A8A);
        graphics.fill(centerX - 48, top + 2, centerX + 48, top + 62, 0xFFA8A8A8);
        boolean blink = this.updateCounter / 6 % 2 == 0;
        for (int i = 0; i < this.lines.length; i++) {
            String line = this.lines[i];
            if (blink && i == this.editLine) {
                line = "> " + line + " <";
            }
            graphics.text(this.font, line, centerX - this.font.width(line) / 2, top + 8 + i * 12, 0xFF000000, false);
        }
    }
}
