package tconstruct.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;

/** Draws a box of liquid (the still texture, cut to the box like RenderBlocks did with render bounds). */
public final class LiquidQuads {
    private static final RenderType TYPE = RenderTypes.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
    /** Liquid metal glows (light 9 of 1.5.2); it is drawn at full block light. */
    public static final int GLOW = 0xF000F0;

    private LiquidQuads() {
    }

    public static int light(Fluid fluid, int light) {
        return fluid.getFluidType().getLightLevel() > 0 ? GLOW : light;
    }

    /** A box within one block (0..1) of the given fluid. */
    public static void box(PoseStack poseStack, SubmitNodeCollector collector, Fluid fluid, float x0, float y0, float z0, float x1, float y1, float z1,
                           int light) {
        if (y1 <= y0) {
            return;
        }
        TextureAtlasSprite sprite = LiquidRender.still(fluid);
        int l = light(fluid, light);
        collector.submitCustomGeometry(poseStack, TYPE, (pose, buffer) -> {
            // up, down
            quad(buffer, pose, sprite, l, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, z0, x1, z1, 0, 1, 0);
            quad(buffer, pose, sprite, l, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, z0, x1, z1, 0, -1, 0);
            // north (z0), south (z1)
            quad(buffer, pose, sprite, l, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, x0, 1 - y1, x1, 1 - y0, 0, 0, -1);
            quad(buffer, pose, sprite, l, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, 1 - y1, x1, 1 - y0, 0, 0, 1);
            // west (x0), east (x1)
            quad(buffer, pose, sprite, l, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, z0, 1 - y1, z1, 1 - y0, -1, 0, 0);
            quad(buffer, pose, sprite, l, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, z0, 1 - y1, z1, 1 - y0, 1, 0, 0);
        });
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int light, float ax, float ay, float az, float bx,
                             float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, float u0, float v0, float u1, float v1,
                             float nx, float ny, float nz) {
        u0 = clamp(u0);
        u1 = clamp(u1);
        v0 = clamp(v0);
        v1 = clamp(v1);
        float su0 = sprite.getU(u0), su1 = sprite.getU(u1), sv0 = sprite.getV(v0), sv1 = sprite.getV(v1);
        vertex(buffer, pose, ax, ay, az, su0, sv0, light, nx, ny, nz);
        vertex(buffer, pose, bx, by, bz, su0, sv1, light, nx, ny, nz);
        vertex(buffer, pose, cx, cy, cz, su1, sv1, light, nx, ny, nz);
        vertex(buffer, pose, dx, dy, dz, su1, sv0, light, nx, ny, nz);
    }

    private static float clamp(float f) {
        return Math.clamp(f, 0.0F, 1.0F);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light, float nx, float ny,
                               float nz) {
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
