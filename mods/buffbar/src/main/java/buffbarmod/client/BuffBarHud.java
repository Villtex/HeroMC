package buffbarmod.client;

import buffbarmod.BuffBarMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

/** BuffBarDisplayTicker: up to ten effects in the fixed order of 1.5.2, left to right above the hotbar. */
public final class BuffBarHud {
    private static final Identifier COUNTDOWN = Identifier.fromNamespaceAndPath(BuffBarMod.MOD_ID, "textures/gui/countdown.png");
    private static final List<Holder<MobEffect>> ORDER = List.of(MobEffects.SPEED, MobEffects.SLOWNESS, MobEffects.HASTE,
            MobEffects.MINING_FATIGUE, MobEffects.STRENGTH, MobEffects.WEAKNESS, MobEffects.POISON, MobEffects.REGENERATION,
            MobEffects.INVISIBILITY, MobEffects.HUNGER, MobEffects.JUMP_BOOST, MobEffects.NAUSEA, MobEffects.NIGHT_VISION,
            MobEffects.BLINDNESS, MobEffects.RESISTANCE, MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING);
    private static final int BUFFS = 10;
    private static final int BAR_LENGTH = 18 * BUFFS;
    private static final int PADDING = 5;

    private BuffBarHud() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAbove(VanillaGuiLayers.HOTBAR,
                Identifier.fromNamespaceAndPath(BuffBarMod.MOD_ID, "buff_bar"), BuffBarHud::render));
    }

    private static int scaled(int duration, int max) {
        return duration > max ? 18 : duration < 0 ? 0 : (int) ((float) duration / max * 18.0F);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.screen() != null) {
            return;
        }
        int type = BuffBarMod.DISPLAY_TYPE.get();
        int maxTicks = BuffBarMod.ANALOG_MAX_DURATION.get() * 20;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int number = 0;
        for (Holder<MobEffect> effect : ORDER) {
            MobEffectInstance instance = player.getEffect(effect);
            if (instance == null || number >= BUFFS) {
                continue;
            }
            int duration = instance.isInfiniteDuration() ? Integer.MAX_VALUE : instance.getDuration();
            int yOffset = BuffBarMod.Y_OFFSET.get() + (player.getAbilities().instabuild ? BuffBarMod.CREATIVE_Y_OFFSET.get() : 0);
            int x = width / 2 - BAR_LENGTH / 2 + (18 + PADDING) * number + BuffBarMod.X_OFFSET.get();
            int y = height - 54 - 7 + yOffset;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(effect), x, y, 18, 18);
            if (type == 0 || type == 2) {
                int value = scaled(duration, maxTicks);
                graphics.blit(RenderPipelines.GUI_TEXTURED, COUNTDOWN, x, y + 18, 0, 0, 18, 3, 256, 262);
                int v = value > 9 ? 3 : value > 4 ? 6 : 9;
                graphics.blit(RenderPipelines.GUI_TEXTURED, COUNTDOWN, x, y + 18, 0, v, value, 3, 256, 262);
            }
            if (type == 1 || type == 2) {
                int textY = y + 18 + (type == 2 ? 4 : 0);
                int seconds = duration / 20;
                String text = seconds / 60 % 60 == 0 ? String.format("%02d", seconds % 60)
                        : seconds / 60 % 60 + ":" + String.format("%02d", seconds % 60);
                // drawn at 85 % size, the way the original scaled the text around the screen origin
                float scale = 0.85F;
                int textX = x - (mc.font.width(text) - 20) / 2;
                graphics.pose().pushMatrix();
                graphics.pose().translate((x - (mc.font.width(text) - 18) / 2) * (1.0F - scale), textY * (1.0F - scale));
                graphics.pose().scale(scale, scale);
                graphics.text(mc.font, text, textX, textY, 0xFF000000 | BuffBarMod.FONT_COLOR.get(), false);
                graphics.pose().popMatrix();
            }
            number++;
        }
    }
}
