package betterdungeons.client;

import betterdungeons.BetterDungeons;
import betterdungeons.projectile.BDProjectile;
import betterdungeons.projectile.ProjectileBehavior;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * RenderBallProjectile: the ball as a camera-facing 16 x 16 cell of /bdimg/items.png (index from the behaviour,
 * scaled by its size, full bright), and for hooks, webs and thunder bolts a rope to the shooter's hand that sags by
 * the hook's spare length. Client check open.
 */
public class BallRenderer extends EntityRenderer<BDProjectile, BallRenderer.State> {
    static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "textures/entity/items.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucent(TEXTURE);

    public BallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static class State extends EntityRenderState {
        int textureIndex = -1;
        float size = 1.0F;
        int ropeColor = -1;
        boolean thunder;
        Vec3 ropeOffset = Vec3.ZERO;
        double hang;
        int seed;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected int getBlockLightLevel(BDProjectile entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected boolean affectedByCulling(BDProjectile entity) {
        return entity.behavior().getRopeColor() == -1;
    }

    @Override
    public void extractRenderState(BDProjectile entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ProjectileBehavior behavior = entity.behavior();
        state.textureIndex = behavior.getTextureIndex();
        state.size = behavior.getSize();
        state.ropeColor = behavior.getRopeColor();
        state.thunder = entity.getProjectileType() == betterdungeons.projectile.ProjectileType.THUNDER.id;
        state.seed = entity.getId();
        LivingEntity shooter = entity.shooter();
        if (state.ropeColor != -1 && shooter != null) {
            Vec3 hand = handPosition(shooter, partialTicks);
            Vec3 start = entity.getPosition(partialTicks).add(0.0, 0.25, 0.0);
            state.ropeOffset = hand.subtract(start);
            double hang = behavior.getRadius() - entity.distanceTo(shooter);
            state.hang = hang < 0.2 ? 0.0 : hang;
        } else {
            state.ropeColor = -1;
        }
    }

    /** Where the rope ends: the eyes of the player in first person, else the right hand (1.5.2's offsets). */
    private Vec3 handPosition(LivingEntity shooter, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (shooter == mc.player && mc.options.getCameraType().isFirstPerson()) {
            return shooter.getEyePosition(partialTicks).add(shooter.getViewVector(partialTicks).scale(0.5)).add(0.0, -0.2, 0.0);
        }
        float yaw = Mth.lerp(partialTicks, shooter.yBodyRotO, shooter.yBodyRot) * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yaw);
        double cos = Mth.cos(yaw);
        Vec3 pos = shooter.getPosition(partialTicks);
        return new Vec3(pos.x - cos * 0.35 - sin * 0.85, pos.y + shooter.getEyeHeight() - 0.45, pos.z - sin * 0.35 + cos * 0.85);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.textureIndex != -1) {
            poseStack.pushPose();
            poseStack.scale(state.size, state.size, state.size);
            poseStack.rotate(camera.orientation);
            int index = state.textureIndex;
            float u0 = (index % 16 * 16) / 256.0F;
            float u1 = (index % 16 * 16 + 16) / 256.0F;
            float v0 = (index / 16 * 16) / 256.0F;
            float v1 = (index / 16 * 16 + 16) / 256.0F;
            collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> {
                vertex(buffer, pose, state.lightCoords, -0.5F, -0.25F, u0, v1);
                vertex(buffer, pose, state.lightCoords, 0.5F, -0.25F, u1, v1);
                vertex(buffer, pose, state.lightCoords, 0.5F, 0.75F, u1, v0);
                vertex(buffer, pose, state.lightCoords, -0.5F, 0.75F, u0, v0);
            });
            poseStack.popPose();
        }
        if (state.ropeColor != -1) {
            float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
            int color = 0xFF000000 | state.ropeColor;
            Vec3 d = state.ropeOffset;
            int steps = 16;
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                Random rnd = new Random(state.seed);
                for (int i = 0; i < steps; i++) {
                    Vec3 a = point(d, i, steps, state, rnd);
                    Vec3 b = point(d, i + 1, steps, state, rnd);
                    Vec3 n = b.subtract(a).normalize();
                    buffer.addVertex(pose, (float) a.x, (float) a.y, (float) a.z).setColor(color).setNormal(pose, (float) n.x, (float) n.y, (float) n.z)
                            .setLineWidth(width);
                    buffer.addVertex(pose, (float) b.x, (float) b.y, (float) b.z).setColor(color).setNormal(pose, (float) n.x, (float) n.y, (float) n.z)
                            .setLineWidth(width);
                }
            });
        }
        super.submit(state, poseStack, collector, camera);
    }

    /** A point of the rope: straight with sag for hooks and webs, a jagged arc for the thunder bolt. */
    private static Vec3 point(Vec3 d, int i, int steps, State state, Random rnd) {
        float f = (float) i / steps;
        if (state.thunder) {
            double varY = -Math.sin(i * Math.PI / steps) * 4.0;
            double dx = i == 0 || i == steps ? 0.0 : (rnd.nextDouble() - 0.5) * varY;
            double dy = i == 0 || i == steps ? 0.0 : (rnd.nextDouble() - 0.5) * varY;
            double dz = i == 0 || i == steps ? 0.0 : (rnd.nextDouble() - 0.5) * varY;
            return new Vec3(d.x * f + dx, d.y * f + dy + 0.25, d.z * f + dz);
        }
        double sag = -Math.sin(i * Math.PI / steps) * state.hang;
        return new Vec3(d.x * f, d.y * f + sag + 0.25, d.z * f);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, float y, float u, float v) {
        buffer.addVertex(pose, x, y, 0.0F).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
