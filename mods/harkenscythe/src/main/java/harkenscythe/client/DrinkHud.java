package harkenscythe.client;

import harkenscythe.HarkenScythe;
import harkenscythe.item.KeeperItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * ClientTickHandlerHarkenScythe.onRenderTick: while drinking from a keeper (not in creative) a gauge above the hotbar
 * shows the drinks taken (a flask filling up in five steps, souls in the top row, blood below) and the strength the
 * feast will have (1-4). "HUD Drink Count Element y_offset" of the pack is 0 (16 pixels per step).
 */
public final class DrinkHud {
    private static final Identifier TEXTURE = HarkenScythe.id("textures/gui/drink_gauge.png");
    private static final int Y_OFFSET = 0;

    private DrinkHud() {
    }

    static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.screen() != null && !(mc.gui.screen() instanceof net.minecraft.client.gui.screens.ChatScreen)) {
            return;
        }
        ItemStack stack = player.getUseItem();
        if (!player.isUsingItem() || !(stack.getItem() instanceof KeeperItem keeper) || player.getAbilities().instabuild) {
            return;
        }
        int drinks = stack.getOrDefault(HarkenScythe.DRINK_COUNT.get(), 0);
        int u = drinks % 5 * 16;
        int v = keeper.isBlood() ? 16 : 0;
        int rank = Math.min(drinks >= 5 ? 1 + drinks / 5 : 1, 4);
        int yOffset = Y_OFFSET * 16;
        int x = graphics.guiWidth() / 2 - 8;
        int y = graphics.guiHeight() - 54 - yOffset;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, u, v, 16, 16, 256, 256);
        graphics.text(mc.font, Integer.toString(rank), graphics.guiWidth() / 2 - 3, graphics.guiHeight() - 50 - yOffset, 0xFFFFFFFF, true);
    }
}
