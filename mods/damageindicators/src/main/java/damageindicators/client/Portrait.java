package damageindicators.client;

import damageindicators.DamageIndicatorsMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * The skinned portrait of the original (DITicker.updateMouseOversSkinned, DIGuiTools.DrawPortraitSkinned) with the
 * "Default" skin and its skin.cfg: background, mob preview, health bar, frame, name plate, mob type icon, effects,
 * health text and name, in the order of the skin. Position and scale come from the config.
 */
final class Portrait {
    private static final String NS = DamageIndicatorsMod.MOD_ID;
    private static final Identifier FRAME = tex("frame");
    private static final Identifier TYPES = tex("type_icons");
    private static final Identifier PLATE = tex("name_plate");
    private static final Identifier BACKGROUND = tex("background");
    private static final Identifier HEALTH = tex("health");
    private static final Identifier DAMAGE = tex("damage");
    private static final Identifier POTIONS_LEFT = tex("potions_left");
    private static final Identifier POTIONS_RIGHT = tex("potions_right");
    private static final Identifier POTIONS_CENTER = tex("potions_center");

    // skin.cfg of the Default skin
    private static final int BG_W = 49, BG_H = 51, BG_X = -4, BG_Y = -4;
    private static final int FRAME_W = 178, FRAME_H = 64, FRAME_X = -15, FRAME_Y = -5;
    private static final int BAR_W = 112, BAR_H = 17, BAR_X = 49, BAR_Y = 13;
    private static final int PLATE_W = 112, PLATE_H = 12, PLATE_X = 49, PLATE_Y = 0;
    private static final int TYPE_W = 18, TYPE_H = 18, TYPE_X = -13, TYPE_Y = 39;
    private static final int PREVIEW_X = -4, PREVIEW_Y = -3;
    private static final int BOX_H = 22, BOX_SIDE = 4, BOX_X = 48, BOX_Y = 31;

    private static int lastTargeted = -1;
    private static int tick = 0;
    private static int updateSkip = 4;

    private Portrait() {
    }

    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath(NS, "textures/gui/" + name + ".png");
    }

    static void clear() {
        lastTargeted = -1;
        tick = 0;
    }

    /** isTargetingEntity: the closest living entity whose box the line of sight hits. */
    private static LivingEntity pick(Minecraft mc, double range) {
        Entity view = mc.getCameraEntity();
        if (view == null) {
            return null;
        }
        Vec3 eye = view.getEyePosition(1.0F);
        Vec3 look = view.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        AABB search = view.getBoundingBox().expandTowards(look.scale(range)).inflate(0.5);
        LivingEntity best = null;
        double closest = range;
        for (LivingEntity e : mc.level.getEntitiesOfClass(LivingEntity.class, search, x -> x != view && !(x instanceof ArmorStand))) {
            Optional<Vec3> hit = e.getBoundingBox().inflate(e.getPickRadius()).clip(eye, end);
            double d = view.distanceTo(e);
            if (hit.isPresent() && d < closest) {
                best = e;
                closest = d;
            }
        }
        if (best != null && best.hasEffect(MobEffects.INVISIBILITY)) {
            return null;
        }
        return best;
    }

    static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!DamageIndicatorsMod.PORTRAIT.get() || mc.level == null || mc.player == null) {
            return;
        }
        if (DamageIndicatorsMod.DEBUG_HIDES.get() && mc.gui.hud.getDebugOverlay().showDebugScreen()) {
            lastTargeted = -1;
            return;
        }
        if (mc.gui.screen() != null && !(mc.gui.screen() instanceof ChatScreen)) {
            lastTargeted = -1;
            return;
        }
        LivingEntity el = null;
        if (updateSkip-- <= 0) {
            updateSkip = 4;
            el = pick(mc, DamageIndicatorsMod.PORTRAIT_RANGE.get());
            if (el != null) {
                lastTargeted = el.getId();
            }
        }
        int lifetime = DamageIndicatorsMod.PORTRAIT_LIFETIME.get();
        if (el != null) {
            tick = lifetime;
        } else {
            if (lastTargeted == -1 || !(lifetime == -1 || tick > 0)) {
                return;
            }
            tick--;
            Entity last = mc.level.getEntity(lastTargeted);
            if (!(last instanceof LivingEntity living) || last.isRemoved()) {
                lastTargeted = -1;
                return;
            }
            el = living;
        }
        draw(g, mc, el);
    }

    private static boolean hostile(Minecraft mc, LivingEntity el) {
        if (el instanceof Enemy) {
            return true;
        }
        return el instanceof Mob mob && mob.getTarget() == mc.player;
    }

    private static int typeIndex(LivingEntity el) {
        if (el instanceof EnderDragon || el instanceof WitherBoss) {
            return 4;
        }
        if (el.typeHolder().is(EntityTypeTags.UNDEAD)) {
            return 0;
        }
        if (el.typeHolder().is(EntityTypeTags.ARTHROPOD)) {
            return 3;
        }
        if (el instanceof Player || el instanceof Witch || el instanceof Villager || el instanceof IronGolem) {
            return 2;
        }
        return 1;
    }

    private static void draw(GuiGraphicsExtractor g, Minecraft mc, LivingEntity el) {
        int sw = g.guiWidth();
        int sh = g.guiHeight();
        float scale = DamageIndicatorsMod.GUI_SCALE.get().floatValue();
        int lx = Math.max(0, Math.min(DamageIndicatorsMod.X_POS.get(), sw - 135));
        int ly = Math.max(0, Math.min(DamageIndicatorsMod.Y_POS.get(), sh - 50));
        int hp = (int) Math.ceil(el.getHealth());
        int max = Math.max(hp, (int) Math.ceil(el.getMaxHealth()));
        String name = el instanceof Player p ? p.getName().getString() : el.getName().getString();
        if (el.isBaby() && !(el instanceof Player)) {
            name = Component.translatable("damageindicatorsmod.baby", name).getString();
        }

        g.pose().pushMatrix();
        g.pose().translate((1.0F - scale) * lx, (1.0F - scale) * ly);
        g.pose().scale(scale, scale);
        // Ordering of the Default skin: background, preview, health bar, frame, name plate, type icon, effects, texts
        g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, lx + BG_X, ly + BG_Y, 0, 0, BG_W, BG_H, 64, 64, 64, 64);
        drawPreview(g, el, scale, lx, ly);
        float fill = hp < max ? Math.max(0.0F, (float) hp / max) : 1.0F;
        int fillW = Math.round(BAR_W * fill);
        g.blit(RenderPipelines.GUI_TEXTURED, DAMAGE, lx + BAR_X, ly + BAR_Y, 0, 0, BAR_W, BAR_H, 2, 32, 2, 32);
        if (fillW > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, HEALTH, lx + BAR_X, ly + BAR_Y, 0, 0, fillW, BAR_H, 64, 32, 64, 32);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, FRAME, lx + FRAME_X, ly + FRAME_Y, 0, 0, FRAME_W, FRAME_H, 350, 128, 350, 128);
        g.blit(RenderPipelines.GUI_TEXTURED, PLATE, lx + PLATE_X, ly + PLATE_Y, 0, 0, PLATE_W, PLATE_H, 1, 1, 1, 1);
        int type = typeIndex(el);
        int tint = type == 4 ? 0x99FFFFFF : hostile(mc, el) ? 0x99FF0000 : 0x9900FF00;
        g.blit(RenderPipelines.GUI_TEXTURED, TYPES, lx + TYPE_X, ly + TYPE_Y, type * 51.2F, 0, TYPE_W, TYPE_H, 51, 64, 256, 64, tint);
        if (DamageIndicatorsMod.POTIONS.get()) {
            drawEffects(g, mc, el, lx, ly);
        }
        String health = hp + "/" + max;
        g.text(mc.font, health, lx + BAR_X + (BAR_W - mc.font.width(health)) / 2, ly + BAR_Y + (BAR_H - mc.font.lineHeight) / 2, 0xFFFFFFFF, true);
        g.text(mc.font, name, lx + PLATE_X + (PLATE_W - mc.font.width(name)) / 2, ly + PLATE_Y + (PLATE_H - mc.font.lineHeight) / 2 + 1, 0xFFFFFFFF, true);
        g.pose().popMatrix();
    }

    /**
     * drawTargettedMobPreview: the mob in the box left of the plates. {@code g.entity} draws into a rectangle of the
     * screen and is not moved by {@code g.pose()}, so the box is converted into screen coordinates by hand and the
     * mob is clipped to it; the size is chosen so that its bounding box fits in with a little air.
     */
    private static void drawPreview(GuiGraphicsExtractor g, LivingEntity el, float scale, int lx, int ly) {
        // the same transformation the pose does for everything else: translate((1-s)*loc) then scale(s)
        int x0 = Math.round((1.0F - scale) * lx + scale * (lx + PREVIEW_X));
        int y0 = Math.round((1.0F - scale) * ly + scale * (ly + PREVIEW_Y));
        int x1 = Math.round(x0 + BG_W * scale);
        int y1 = Math.round(y0 + BG_H * scale);
        // the model is usually longer than the bounding box is wide (a cow), so the width counts for more; the
        // remaining air is what the original got from its extra 0.85 scaling
        float extent = Math.max(0.8F, Math.max(el.getBbHeight(), el.getBbWidth() * 1.4F));
        int size = (int) Math.max(1.0F, (y1 - y0) * 0.45F / extent);
        // locked: a fixed three-quarter view (the original turned by renderYawOffset - 30 degrees);
        // unlocked: the mob keeps the direction it faces in the world
        float angle = 1.5F;
        if (!DamageIndicatorsMod.LOCK_MOB.get() && Minecraft.getInstance().player != null) {
            angle = Mth.wrapDegrees(el.getYRot() - Minecraft.getInstance().player.getYRot()) / 20.0F;
        }
        g.enableScissor(x0, y0, x1, y1);
        InventoryScreen.renderEntityInInventoryFollowsAngle(g, x0, y0, x1, y1, size, 0.0625F, angle, 0.0F, el);
        g.disableScissor();
    }

    private static void drawEffects(GuiGraphicsExtractor g, Minecraft mc, LivingEntity el, int lx, int ly) {
        int position = 0;
        boolean boxDrawn = false;
        for (damageindicators.DINetwork.Effect effect : DIClient.effects(el.getId())) {
            boolean infinite = effect.duration() < 0;
            int duration = infinite ? 20 * 3600 : effect.duration();
            if (duration <= 10) {
                continue;
            }
            int bx = lx + BOX_X;
            int by = ly + BOX_Y;
            if (!boxDrawn) {
                boxDrawn = true;
                g.blit(RenderPipelines.GUI_TEXTURED, POTIONS_LEFT, bx, by, 0, 0, BOX_SIDE, BOX_H, 8, 32, 8, 32);
            }
            int cx = bx + position * 20 + BOX_SIDE;
            g.blit(RenderPipelines.GUI_TEXTURED, POTIONS_CENTER, cx, by, 0, 0, 20, BOX_H, 2, 64, 2, 64);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(effect.effect()), cx + 2, by + 2, BOX_H - 4, BOX_H - 4);
            if (!infinite) {
                int seconds = duration / 20;
                int minutes = seconds / 60;
                if (minutes < 10) {
                    String time = minutes + ":" + (seconds % 60 < 10 ? "0" : "") + (seconds % 60);
                    g.pose().pushMatrix();
                    g.pose().translate(cx + 13 - mc.font.width(time) / 2.0F, by + BOX_H - 2 - mc.font.lineHeight * 0.815F);
                    g.pose().scale(0.815F, 0.815F);
                    g.text(mc.font, time, 0, 0, 0xFFFFFF80, true);
                    g.pose().popMatrix();
                }
            }
            position++;
        }
        if (boxDrawn) {
            g.blit(RenderPipelines.GUI_TEXTURED, POTIONS_RIGHT, lx + BOX_X + position * 20 + BOX_SIDE, ly + BOX_Y, 0, 0, BOX_SIDE, BOX_H, 8, 32, 8, 32);
        }
    }
}
