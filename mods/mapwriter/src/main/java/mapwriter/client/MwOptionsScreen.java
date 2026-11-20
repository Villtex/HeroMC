package mapwriter.client;

import mapwriter.MapWriterMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** MwGuiOptions: draw coordinates, circular (rotating) minimap, trail markers. Texture size and scaling options are config-only. */
final class MwOptionsScreen extends Screen {
    private final Mw mw;
    private final Screen parent;

    MwOptionsScreen(Mw mw, Screen parent) {
        super(Component.literal("MapWriter Options"));
        this.mw = mw;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = Math.max(60, height / 4);
        int x = width / 2 - 100;
        addRenderableWidget(Button.builder(Component.literal("Draw coords: " + MapWriterMod.COORDS.get()), b -> {
            MapWriterMod.COORDS.set(!MapWriterMod.COORDS.get());
            b.setMessage(Component.literal("Draw coords: " + MapWriterMod.COORDS.get()));
        }).bounds(x, y, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Circular mode: " + MapWriterMod.SMALL.circular.get()), b -> {
            mw.toggleRotating();
            b.setMessage(Component.literal("Circular mode: " + MapWriterMod.SMALL.circular.get()));
        }).bounds(x, y + 25, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Trail Markers: " + MapWriterMod.TRAIL.get()), b -> {
            MapWriterMod.TRAIL.set(!MapWriterMod.TRAIL.get());
            b.setMessage(Component.literal("Trail Markers: " + MapWriterMod.TRAIL.get()));
        }).bounds(x, y + 50, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x, y + 75, 200, 20).build());
    }

    @Override
    public void onClose() {
        MapWriterMod.saveConfig();
        minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(font, "MapWriter Options", width / 2, 40, 0xFFFFFFFF);
    }
}
