package bibliocraft.decor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;

/**
 * The Wavefront models of the original (Forge AdvancedModelLoader / WavefrontObject, IModelCustom.renderPart): the
 * groups ("g name") are drawn one by one, since the renderers show and hide them at run time (table legs and
 * corners, lantern and lamp styles) and draw them in two textures. Read like WavefrontObject did: v = 1 - vt.v, a
 * face normal from the first three corners, and the texture coordinates pulled 0.0005 towards the face's middle.
 * The faces are all quads.
 */
public final class ObjModel {
    private static final float TEXTURE_OFFSET = 0.0005F;
    /** group -> faces; a face is 4 corners of x, y, z, u, v and then the normal. */
    private final Map<String, List<float[]>> groups;

    private ObjModel(Map<String, List<float[]>> groups) {
        this.groups = groups;
    }

    /** Loads {@code assets/bibliocraft/models/obj/<name>.obj}; a missing or broken file gives an empty model. */
    public static ObjModel load(Identifier id) {
        ResourceProvider resources = Minecraft.getInstance().getResourceManager();
        try (BufferedReader reader = resources.openAsReader(id)) {
            return parse(reader);
        } catch (IOException | RuntimeException e) {
            LogUtils.getLogger().error("Could not read model {}", id, e);
            return new ObjModel(Map.of());
        }
    }

    private static ObjModel parse(BufferedReader reader) throws IOException {
        List<float[]> vertices = new ArrayList<>();
        List<float[]> uvs = new ArrayList<>();
        Map<String, List<float[]>> groups = new HashMap<>();
        List<float[]> current = null;
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            String[] t = line.split("\\s+");
            switch (t[0]) {
                case "v" -> vertices.add(new float[] {Float.parseFloat(t[1]), Float.parseFloat(t[2]), Float.parseFloat(t[3])});
                case "vt" -> uvs.add(new float[] {Float.parseFloat(t[1]), 1.0F - Float.parseFloat(t[2])});
                case "g", "o" -> current = groups.computeIfAbsent(t.length > 1 ? t[1] : "", k -> new ArrayList<>());
                case "f" -> {
                    if (current == null) {
                        current = groups.computeIfAbsent("", k -> new ArrayList<>());
                    }
                    current.add(face(t, vertices, uvs));
                }
                default -> {
                }
            }
        }
        return new ObjModel(groups);
    }

    /** Face.addFaceForRender: corners with the offset texture coordinates, a triangle repeats its last corner. */
    private static float[] face(String[] t, List<float[]> vertices, List<float[]> uvs) {
        int n = t.length - 1;
        float[][] pos = new float[n][];
        float[][] uv = new float[n][];
        float avgU = 0.0F;
        float avgV = 0.0F;
        for (int i = 0; i < n; i++) {
            String[] idx = t[i + 1].split("/");
            pos[i] = vertices.get(Integer.parseInt(idx[0]) - 1);
            uv[i] = idx.length > 1 && !idx[1].isEmpty() ? uvs.get(Integer.parseInt(idx[1]) - 1) : new float[] {0.0F, 0.0F};
            avgU += uv[i][0];
            avgV += uv[i][1];
        }
        avgU /= n;
        avgV /= n;
        // calculateFaceNormal: (v1 - v0) x (v2 - v0), normalised
        float ax = pos[1][0] - pos[0][0], ay = pos[1][1] - pos[0][1], az = pos[1][2] - pos[0][2];
        float bx = pos[2][0] - pos[0][0], by = pos[2][1] - pos[0][1], bz = pos[2][2] - pos[0][2];
        float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0.0F) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        float[] out = new float[4 * 5 + 3];
        for (int c = 0; c < 4; c++) {
            int i = Math.min(c, n - 1);
            float du = uv[i][0] > avgU ? -TEXTURE_OFFSET : TEXTURE_OFFSET;
            float dv = uv[i][1] > avgV ? -TEXTURE_OFFSET : TEXTURE_OFFSET;
            out[c * 5] = pos[i][0];
            out[c * 5 + 1] = pos[i][1];
            out[c * 5 + 2] = pos[i][2];
            out[c * 5 + 3] = uv[i][0] + du;
            out[c * 5 + 4] = uv[i][1] + dv;
        }
        out[20] = nx;
        out[21] = ny;
        out[22] = nz;
        return out;
    }

    /** renderPart for each of {@code parts} with the bound texture {@code texture}, at the current pose. */
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, Identifier texture, int light, String... parts) {
        List<List<float[]>> faces = new ArrayList<>(parts.length);
        for (String part : parts) {
            List<float[]> group = this.groups.get(part);
            if (group != null) {
                faces.add(group);
            }
        }
        if (faces.isEmpty()) {
            return;
        }
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(texture), (pose, buffer) -> {
            for (List<float[]> group : faces) {
                for (float[] f : group) {
                    for (int c = 0; c < 4; c++) {
                        buffer.addVertex(pose, f[c * 5], f[c * 5 + 1], f[c * 5 + 2])
                                .setColor(-1)
                                .setUv(f[c * 5 + 3], f[c * 5 + 4])
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(light)
                                .setNormal(pose, f[20], f[21], f[22]);
                    }
                }
            }
        });
    }
}
