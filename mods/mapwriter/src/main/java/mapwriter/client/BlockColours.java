package mapwriter.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BlockColours / BlockColourGen: one colour per block state, the average of its texture (the top face, as the original's
 * "terrain.png" slot of the block was), with the opacity the block has on the map. Grass, leaves, water and other
 * tinted blocks are multiplied with their biome colour from the game's own tint sources, which replaces the
 * blocktype/biome tables of BlockColourGen.
 */
final class BlockColours {
    /** argb: the alpha is how much of what lies below it is covered; tint -1 = none, -2 = water. */
    record Entry(int argb, int tint) {
        static final Entry NONE = new Entry(0, -1);
    }

    private static final Map<BlockState, Entry> CACHE = new HashMap<>();

    private BlockColours() {
    }

    static void clear() {
        CACHE.clear();
    }

    static Entry get(BlockState state) {
        Entry e = CACHE.get(state);
        if (e == null) {
            try {
                e = compute(state);
            } catch (RuntimeException ex) {
                e = Entry.NONE;
            }
            CACHE.put(state, e);
        }
        return e;
    }

    private static Entry compute(BlockState state) {
        if (state.isAir()) {
            return Entry.NONE;
        }
        if (state.is(Blocks.DANDELION)) {
            return new Entry(0x60FFFF00, -1);   // blockcolour 025/026 of the pack config
        }
        if (state.is(Blocks.POPPY)) {
            return new Entry(0x60FF0000, -1);
        }
        if (state.getBlock() instanceof LiquidBlock) {
            if (state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
                return new Entry(0xA0FFFFFF, -2);
            }
            return new Entry(0xFFCF5B12, -1);
        }
        Minecraft mc = Minecraft.getInstance();
        BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(state);
        TextureAtlasSprite sprite = null;
        int tint = -1;
        ChunkSectionLayer layer = null;
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(42L), parts);
        for (BlockStateModelPart part : parts) {
            List<BakedQuad> quads = part.getQuads(Direction.UP);
            if (!quads.isEmpty()) {
                BakedQuad.MaterialInfo info = quads.get(0).materialInfo();
                sprite = info.sprite();
                tint = info.tintIndex();
                layer = info.layer();
                break;
            }
        }
        if (sprite == null) {
            sprite = model.particleMaterial().sprite();
            tint = mc.getBlockColors().getTintSources(state).isEmpty() ? -1 : 0;
        }
        long a = 0, r = 0, g = 0, b = 0;
        SpriteContents contents = sprite.contents();
        NativeImage image = contents.getOriginalImage();
        int w = contents.width();
        int h = contents.height();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = image.getPixel(x, y);
                int pa = p >>> 24;
                a += pa;
                r += (long) (p >> 16 & 0xFF) * pa;
                g += (long) (p >> 8 & 0xFF) * pa;
                b += (long) (p & 0xFF) * pa;
            }
        }
        if (a == 0) {
            return Entry.NONE;
        }
        int cr = (int) (r / a), cg = (int) (g / a), cb = (int) (b / a);
        int alpha = (int) (a / (w * h));
        boolean opaque = state.canOcclude() && layer != ChunkSectionLayer.TRANSLUCENT || state.is(BlockTags.LEAVES);
        if (opaque) {
            alpha = 255;
        } else if (alpha < 16) {
            return Entry.NONE;
        }
        return new Entry(alpha << 24 | cr << 16 | cg << 8 | cb, tint);
    }

    /** The biome colour multiplier of a block in the world, white when it has none. */
    static int tint(Entry e, BlockState state, Level level, BlockPos pos) {
        if (e.tint() == -1) {
            return 0xFFFFFF;
        }
        if (e.tint() == -2) {
            return level.getBiome(pos).value().getWaterColor() & 0xFFFFFF;
        }
        try {
            var src = Minecraft.getInstance().getBlockColors().getTintSource(state, e.tint());
            return src == null ? 0xFFFFFF : src.colorInWorld(state, (net.minecraft.client.renderer.block.BlockAndTintGetter) level, pos) & 0xFFFFFF;
        } catch (RuntimeException ex) {
            return 0xFFFFFF;
        }
    }
}
