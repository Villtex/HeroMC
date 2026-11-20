package atomicstryker.infernalmobs.client;

import atomicstryker.infernalmobs.common.IMConfig;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * InfernalMobsClient and RendererBossGlow: the name, health bar and modifier list of the rare mob under the
 * crosshair (up to 32 blocks), and spell particles around every rare mob. The modifiers come from the synced
 * attachment; the health is the mob's own (its maximum is raised on the server), so the old health packets are gone.
 */
public final class InfernalMobsClient {
    private static final Identifier BAR_BACKGROUND = Identifier.withDefaultNamespace("hud/experience_bar_background");
    private static final Identifier BAR_PROGRESS = Identifier.withDefaultNamespace("hud/experience_bar_progress");
    private static final double NAME_VISION_DISTANCE = 32.0;

    private record Cached(String names, MobModifier mod) {
    }

    /** proxy.getRareMobs() of the client, rebuilt from the synced names. */
    private static final Map<LivingEntity, Cached> RARE_MOBS_CLIENT = new WeakHashMap<>();
    private static ClientLevel lastWorld;

    private InfernalMobsClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAboveAll(
                Identifier.fromNamespaceAndPath(InfernalMobsCore.MOD_ID, "boss_overlay"), InfernalMobsClient::renderBossOverlay));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
    }

    /** The client's modifier chain for {@code ent}, or null if it is not rare. */
    public static MobModifier getMobModifiers(LivingEntity ent) {
        String names = ent.getExistingDataOrNull(InfernalMobsCore.MOD_NAMES);
        if (names == null || names.isEmpty()) {
            RARE_MOBS_CLIENT.remove(ent);
            return null;
        }
        Cached cached = RARE_MOBS_CLIENT.get(ent);
        if (cached == null || !cached.names().equals(names)) {
            cached = new Cached(names, InfernalMobsCore.stringToMobModifiers(ent, names));
            RARE_MOBS_CLIENT.put(ent, cached);
        }
        return cached.mod();
    }

    private static void renderBossOverlay(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (IMConfig.disableGuiOverlay() || mc.level == null) {
            return;
        }
        Entity ent = getEntityCrosshairOver(delta.getGameTimeDeltaPartialTick(true), mc);
        if (!(ent instanceof LivingEntity target)) {
            return;
        }
        MobModifier mod = getMobModifiers(target);
        if (mod == null) {
            return;
        }
        String buffer = mod.getEntityDisplayName(target);
        int screenwidth = g.guiWidth();
        Font fontR = mc.font;
        int lifeBarLength = 182;
        int x = screenwidth / 2 - lifeBarLength / 2;
        int lifeBarLeft = (int) (target.getHealth() / mod.getActualMaxHealth(target) * (lifeBarLength + 1));
        int y = 12;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACKGROUND, x, y, lifeBarLength, 5);
        if (lifeBarLeft > 0) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_PROGRESS, lifeBarLength, 5, 0, 0, x, y, Math.min(lifeBarLeft, lifeBarLength), 5);
        }
        int yCoord = 10;
        g.text(fontR, buffer, screenwidth / 2 - fontR.width(buffer) / 2, yCoord, 0xFF000000 | 3118827, true);
        String[] display = mod.getDisplayNames();
        for (int i = 0; i < display.length && display[i] != null; i++) {
            yCoord += 10;
            g.text(fontR, display[i], screenwidth / 2 - fontR.width(display[i]) / 2, yCoord, 0xFFFFFFFF, true);
        }
    }

    /** The entity under the crosshair up to 32 blocks, unless a block is closer. */
    private static Entity getEntityCrosshairOver(float renderTick, Minecraft mc) {
        Entity view = mc.getCameraEntity();
        if (view == null || mc.level == null) {
            return null;
        }
        double reachDistance = NAME_VISION_DISTANCE;
        HitResult block = view.pick(reachDistance, renderTick, false);
        Vec3 eye = view.getEyePosition(renderTick);
        double reachDist2 = block.getType() != HitResult.Type.MISS ? block.getLocation().distanceTo(eye) : reachDistance;
        Vec3 look = view.getViewVector(renderTick);
        Vec3 end = eye.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        AABB box = view.getBoundingBox().expandTowards(look.scale(reachDistance)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(view, eye, end, box, e -> !e.isSpectator() && e.isPickable(), reachDist2 * reachDist2);
        return hit == null ? null : hit.getEntity();
    }

    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != lastWorld) {
            RARE_MOBS_CLIENT.clear();
            lastWorld = mc.level;
        }
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        renderBossGlow(mc);
    }

    /**
     * RendererBossGlow: a mobSpell particle per rare mob every 10 ms of rendering (here five per tick); the
     * particle colour was the velocity arguments, clamped to 0..1.
     */
    private static void renderBossGlow(Minecraft mc) {
        Entity view = mc.getCameraEntity();
        if (view == null) {
            return;
        }
        Vec3 cam = view.position();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity ent) || !ent.isAlive() || getMobModifiers(ent) == null
                    || !ent.shouldRender(cam.x, cam.y, cam.z)) {
                continue;
            }
            RandomSource rand = ent.level().getRandom();
            for (int i = 0; i < 5; i++) {
                double px = ent.getX() + (rand.nextDouble() - 0.5) * ent.getBbWidth();
                double py = ent.getY() + rand.nextDouble() * ent.getBbHeight() - 0.25;
                double pz = ent.getZ() + (rand.nextDouble() - 0.5) * ent.getBbWidth();
                float r = clamp((rand.nextDouble() - 0.5) * 2.0);
                float gr = clamp(-rand.nextDouble());
                float b = clamp((rand.nextDouble() - 0.5) * 2.0);
                mc.level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, gr, b), px, py, pz, 0.0, 0.0, 0.0);
            }
        }
    }

    private static float clamp(double v) {
        return (float) Math.max(0.0, Math.min(1.0, v));
    }
}
