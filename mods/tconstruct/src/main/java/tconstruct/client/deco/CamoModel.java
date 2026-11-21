package tconstruct.client.deco;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import tconstruct.TDeco;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The camouflage of the drawbridge (getBlockTexture: the camo block's faces on the whole block) and of the landmine
 * (RenderLandmine: the camo block's faces cut to the plate), after secretrooms.client.CamoModel. Client check open.
 */
public class CamoModel implements DynamicBlockStateModel {
    public enum Kind { FULL, BOX }

    private final BlockStateModel own;
    private final Kind kind;

    public CamoModel(BlockStateModel own, Kind kind) {
        this.own = own;
        this.kind = kind;
    }

    static BlockStateModel model(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    /** What the block at pos wears, or null. */
    static @Nullable BlockState camo(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return level.getModelData(pos).get(TDeco.CAMO);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        BlockState camo = camo(level, pos, state);
        if (camo == null) {
            this.own.collectParts(level, pos, state, random, parts);
            return;
        }
        List<BlockStateModelPart> copied = new ArrayList<>();
        model(camo).collectParts(level, pos, camo, random, copied);
        switch (this.kind) {
            case FULL -> parts.addAll(copied);
            case BOX -> parts.add(box(state.getShape(level, pos).toAabbs(), copied));
        }
    }

    /** The faces of the copy cut to each box of the shape. */
    private static Part box(List<AABB> boxes, List<BlockStateModelPart> copied) {
        Map<Direction, BakedQuad> faces = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            for (BlockStateModelPart p : copied) {
                for (BakedQuad q : p.getQuads(d)) {
                    faces.putIfAbsent(d, q);
                }
                for (BakedQuad q : p.getQuads(null)) {
                    if (q.direction() == d) {
                        faces.putIfAbsent(d, q);
                    }
                }
            }
        }
        Map<Direction, List<BakedQuad>> culled = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            culled.put(d, new ArrayList<>());
        }
        List<BakedQuad> free = new ArrayList<>();
        for (AABB box : boxes) {
            for (Map.Entry<Direction, BakedQuad> e : faces.entrySet()) {
                Direction d = e.getKey();
                BakedQuad cut = cut(e.getValue(), d, box);
                if (cut == null) {
                    continue;
                }
                boolean onEdge = d.getAxisDirection() == Direction.AxisDirection.POSITIVE ? box.max(d.getAxis()) >= 1.0 : box.min(d.getAxis()) <= 0.0;
                (onEdge ? culled.get(d) : free).add(cut);
            }
        }
        return new Part(culled, free, copied.isEmpty() ? null : copied.get(0));
    }

    /** The quad moved onto the face d of box, its UVs interpolated over the full face it came from. */
    private static @Nullable BakedQuad cut(BakedQuad q, Direction d, AABB box) {
        Direction.Axis normal = d.getAxis();
        Direction.Axis a = normal == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
        Direction.Axis b = normal == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;
        long[] corner = new long[4];
        boolean[] seen = new boolean[4];
        for (int i = 0; i < 4; i++) {
            int ca = Math.round(coord(q.position(i), a));
            int cb = Math.round(coord(q.position(i), b));
            if (ca < 0 || ca > 1 || cb < 0 || cb > 1) {
                return null;
            }
            corner[ca + 2 * cb] = q.packedUV(i);
            seen[ca + 2 * cb] = true;
        }
        for (boolean s : seen) {
            if (!s) {
                return null;
            }
        }
        Vector3f[] pos = new Vector3f[4];
        long[] uv = new long[4];
        double plane = d.getAxisDirection() == Direction.AxisDirection.POSITIVE ? box.max(normal) : box.min(normal);
        for (int i = 0; i < 4; i++) {
            int ca = Math.round(coord(q.position(i), a));
            int cb = Math.round(coord(q.position(i), b));
            float ta = (float) (ca == 0 ? box.min(a) : box.max(a));
            float tb = (float) (cb == 0 ? box.min(b) : box.max(b));
            Vector3f p = new Vector3f();
            set(p, normal, (float) plane);
            set(p, a, ta);
            set(p, b, tb);
            pos[i] = p;
            uv[i] = bilinear(corner, ta, tb);
        }
        return new BakedQuad(pos[0], pos[1], pos[2], pos[3], uv[0], uv[1], uv[2], uv[3], d, q.materialInfo(), q.bakedNormals(), q.bakedColors());
    }

    private static long bilinear(long[] c, float fa, float fb) {
        float u = lerp(lerp(UVPair.unpackU(c[0]), UVPair.unpackU(c[1]), fa), lerp(UVPair.unpackU(c[2]), UVPair.unpackU(c[3]), fa), fb);
        float v = lerp(lerp(UVPair.unpackV(c[0]), UVPair.unpackV(c[1]), fa), lerp(UVPair.unpackV(c[2]), UVPair.unpackV(c[3]), fa), fb);
        return UVPair.pack(u, v);
    }

    private static float lerp(float x, float y, float t) {
        return x + (y - x) * t;
    }

    private static float coord(org.joml.Vector3fc v, Direction.Axis axis) {
        return switch (axis) {
            case X -> v.x();
            case Y -> v.y();
            case Z -> v.z();
        };
    }

    private static void set(Vector3f v, Direction.Axis axis, float value) {
        switch (axis) {
            case X -> v.x = value;
            case Y -> v.y = value;
            case Z -> v.z = value;
        }
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        BlockState camo = camo(level, pos, state);
        return camo == null ? this.own.particleMaterial() : model(camo).particleMaterial();
    }

    @Override
    @Deprecated
    public Material.Baked particleMaterial() {
        return this.own.particleMaterial();
    }

    @Override
    @Deprecated
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.own.materialFlags();
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        BlockState camo = camo(level, pos, state);
        int flags = this.own.materialFlags();
        if (camo != null) {
            flags |= model(camo).materialFlags();
        }
        return flags;
    }

    /** A model part made of ready quads. */
    record Part(Map<Direction, List<BakedQuad>> culled, List<BakedQuad> free, @Nullable BlockStateModelPart source) implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            return direction == null ? this.free : this.culled.getOrDefault(direction, List.of());
        }

        @Override
        @Deprecated
        public boolean useAmbientOcclusion() {
            return this.source == null || this.source.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return this.source != null ? this.source.particleMaterial() : CamoModel.model(Blocks.STONE.defaultBlockState()).particleMaterial();
        }

        @Override
        @BakedQuad.MaterialFlags
        public int materialFlags() {
            int flags = 0;
            for (List<BakedQuad> l : this.culled.values()) {
                for (BakedQuad q : l) {
                    flags |= q.materialInfo().flags();
                }
            }
            for (BakedQuad q : this.free) {
                flags |= q.materialInfo().flags();
            }
            return flags;
        }
    }
}
