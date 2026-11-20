package damageindicators.client;

import damageindicators.DamageIndicatorsMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Pop-offs (DIWordParticles, ParticleHandling): when the health of a mob near the player changes, the number of
 * health points it lost (or gained) hops off it, grows and shrinks again. The health comes straight from the client
 * entity (the original had the server send it). The numbers are projected onto the screen and drawn in the HUD.
 */
final class Popoffs {
    private static final Map<Integer, Integer> HEALTH = new HashMap<>();
    private static final List<Pop> POPS = new ArrayList<>();

    private Popoffs() {
    }

    private static final class Pop {
        double x, y, z, px, py, pz, vx, vy, vz;
        int age;
        final int value;
        final boolean heal;
        final boolean onTop;

        Pop(double x, double y, double z, double vy, int value, boolean onTop) {
            this.x = this.px = x;
            this.y = this.py = y;
            this.z = this.pz = z;
            this.vx = 0.0024;
            this.vy = vy;
            this.vz = 0.0024;
            this.heal = value < 0;
            this.value = Math.abs(value);
            this.onTop = onTop;
        }
    }

    static void clear() {
        HEALTH.clear();
        POPS.clear();
    }

    private static int health(LivingEntity e) {
        return (int) Math.ceil(e.getHealth());
    }

    static void tick(Minecraft mc) {
        Player player = mc.player;
        double range = DamageIndicatorsMod.POP_RANGE.get();
        // existing numbers
        for (Iterator<Pop> it = POPS.iterator(); it.hasNext(); ) {
            Pop p = it.next();
            p.px = p.x;
            p.py = p.y;
            p.pz = p.z;
            p.vy -= 0.04 * DamageIndicatorsMod.GRAVITY.get();
            p.x += p.vx;
            p.y += p.vy;
            p.z += p.vz;
            p.vx *= 0.98;
            p.vy *= 0.98;
            p.vz *= 0.98;
            if (++p.age >= DamageIndicatorsMod.LIFESPAN.get()) {
                it.remove();
            }
        }
        // health changes
        AABB box = player.getBoundingBox().inflate(range);
        Map<Integer, Integer> seen = new HashMap<>();
        for (LivingEntity e : mc.level.getEntitiesOfClass(LivingEntity.class, box, x -> x != player && !(x instanceof ArmorStand))) {
            int hp = health(e);
            Integer last = HEALTH.get(e.getId());
            seen.put(e.getId(), hp);
            if (last != null && last != hp && DamageIndicatorsMod.POPOFFS.get()) {
                spawn(player, e, last - hp);
            }
        }
        HEALTH.clear();
        HEALTH.putAll(seen);
    }

    private static void spawn(Player player, LivingEntity e, int damage) {
        // 0.05 * bounce, normalised with the sideways 0.001, times 0.12: the number leaves mostly straight up
        double len = Math.sqrt(0.001 * 0.001 + Math.pow(0.05 * DamageIndicatorsMod.BOUNCE.get(), 2) + 0.001 * 0.001);
        double vy = 0.05 * DamageIndicatorsMod.BOUNCE.get() / len * 0.12;
        boolean onTop = player.hasLineOfSight(e) || DamageIndicatorsMod.ALWAYS_RENDER.get();
        // the number starts a sixth of the way from the mob to the player
        POPS.add(new Pop((e.getX() * 5 + player.getX()) / 6, (e.getY() * 5 + player.getY()) / 6, (e.getZ() * 5 + player.getZ()) / 6, vy, damage, onTop));
    }

    /** Scale of the quad in frames since spawn: grows 8 % per frame up to three times the size, then shrinks 4 %. */
    private static double scaleAt(double frames) {
        double base = DamageIndicatorsMod.SIZE.get();
        double growFrames = Math.log(3.0) / Math.log(1.08);
        if (frames <= growFrames) {
            return base * Math.pow(1.08, frames);
        }
        return base * 3.0 * Math.pow(0.96, frames - growFrames);
    }

    static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (POPS.isEmpty() || mc.level == null || mc.gui.screen() != null || !DamageIndicatorsMod.POPOFFS.get()) {
            return;
        }
        Camera cam = mc.gameRenderer.mainCamera();
        Vec3 cp = cam.position();
        Vector3fc fwd = cam.forwardVector();
        Vector3fc left = cam.leftVector();
        Vector3fc up = cam.upVector();
        double tan = Math.tan(Math.toRadians(cam.getFov()) / 2.0);
        float pt = delta.getGameTimeDeltaPartialTick(false);
        int w = g.guiWidth();
        int h = g.guiHeight();
        int color = DamageIndicatorsMod.rgb(DamageIndicatorsMod.COLOR.get(), 0xFFAA00);
        int heal = DamageIndicatorsMod.rgb(DamageIndicatorsMod.HEAL_COLOR.get(), 0x00FF00);
        int alpha = (int) (DamageIndicatorsMod.TRANSPARENCY.get() * 0.9947 * 255);
        for (Pop p : POPS) {
            double x = p.px + (p.x - p.px) * pt - cp.x;
            double y = p.py + (p.y - p.py) * pt - cp.y + 0.2;
            double z = p.pz + (p.z - p.pz) * pt - cp.z;
            if (!p.onTop && mc.level.clip(new net.minecraft.world.level.ClipContext(cp, new Vec3(p.x, p.y + 0.2, p.z),
                    net.minecraft.world.level.ClipContext.Block.VISUAL, net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player))
                    .getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                continue;
            }
            double depth = x * fwd.x() + y * fwd.y() + z * fwd.z();
            if (depth < 0.1) {
                continue;
            }
            double sx = w / 2.0 - (x * left.x() + y * left.y() + z * left.z()) / depth / tan * (h / 2.0);
            double sy = h / 2.0 - (x * up.x() + y * up.y() + z * up.z()) / depth / tan * (h / 2.0);
            double frames = (p.age + pt) * 3.0;           // about 60 frames per second against 20 ticks
            double worldHeight = 0.1 * scaleAt(frames);   // half of the quad of the original, in blocks
            float scale = (float) Math.min(8.0, Math.max(0.5, worldHeight * (h / 2.0) / tan / depth / 8.0));
            String text = Integer.toString(p.value);
            int argb = (alpha << 24) | (p.heal ? heal : color);
            g.pose().pushMatrix();
            g.pose().translate((float) sx, (float) sy);
            g.pose().scale(scale, scale);
            g.text(mc.font, text, -mc.font.width(text) / 2, -4, argb, true);
            g.pose().popMatrix();
        }
    }
}
