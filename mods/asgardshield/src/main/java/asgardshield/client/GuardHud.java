package asgardshield.client;

import asgardshield.ASConfig;
import asgardshield.AsgardShield;
import asgardshield.guard.GuardEvents;
import asgardshield.guard.GuardState;
import asgardshield.item.ItemAsgardShield;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * ClientTickHandlerAsgardShield.onRenderTick (without TukMC): the Vanguard Count above the hotbar (VCGauge.png,
 * 16x16 per step) and the Guard Gauge of ten icons above the food bar (GuardGauge.png: full icon at u 0, gilded 18,
 * giant sword 36, empty +9; v 9 when broken, +18 with the ward augment).
 */
public final class GuardHud {
    private static final Identifier VANGUARD = Identifier.fromNamespaceAndPath(AsgardShield.MOD_ID, "textures/gui/vanguard_gauge.png");
    private static final Identifier GAUGE = Identifier.fromNamespaceAndPath(AsgardShield.MOD_ID, "textures/gui/guard_gauge.png");

    private GuardHud() {
    }

    private static boolean hasShield(LocalPlayer player) {
        for (int slot = 0; slot <= 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof ItemAsgardShield) {
                return true;
            }
        }
        return false;
    }

    static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }
        // isInCreativeMode / shouldDrawHUD (survival and adventure)
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        GuardState state = player.getData(AsgardShield.GUARD);
        int width = g.guiWidth();
        int height = g.guiHeight();
        if (hasShield(player)) {
            int charge = Math.max(0, state.vanguardCount * 16 - 16);
            g.blit(RenderPipelines.GUI_TEXTURED, VANGUARD, width / 2 - 8, height - 54 - ASConfig.HUD_VANGUARD_Y.get() * 16, charge, 0, 16, 16,
                    256, 256);
        }
        if (!GuardEvents.isGuarding(player) && state.gauge == 0) {
            return;
        }
        int offset = ASConfig.HUD_GUARD_GAUGE_Y.get();
        int total = state.gauge / 20;
        int gilded = state.gildedGauge ? 18 : 0;
        int sword = state.swordGauge ? 36 : 0;
        int icon = state.swordGauge ? 36 : gilded;
        int v = (state.breakGauge ? 9 : 0) + (state.wardGauge ? 18 : 0);
        for (int i = 0; i < 10; i++) {
            int x = width / 2 + 10 + i * 8;
            int y = height - 49 - offset * 9;
            if (total - 1 < i) {
                icon = 9 + gilded + sword;
            }
            if (player.getAirSupply() < player.getMaxAirSupply() && offset == 0) {
                y -= 9;
            }
            g.blit(RenderPipelines.GUI_TEXTURED, GAUGE, x, y, icon, v, 9, 9, 256, 256);
        }
    }
}
