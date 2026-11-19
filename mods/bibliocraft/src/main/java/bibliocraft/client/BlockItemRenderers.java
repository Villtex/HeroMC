package bibliocraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * The IItemRenderers of the original: every BiblioCraft block item is the special model "bibliocraft:block" with the
 * block's id (items/&lt;block&gt;.json, written by tools/gen.py). Its group registers how the item is drawn with
 * {@link #register}. The renderer draws in the unit cube 0..1 like a block entity renderer; the display transforms
 * (gui, hand, ground) come from the base model models/item/&lt;kind&gt;.json.
 */
public final class BlockItemRenderers {
    /** Draws one item stack of a block. {@code light}/{@code overlay} as given by the item renderer. */
    public interface ItemRenderer {
        void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean foil);
    }

    private static final Map<Identifier, Function<SpecialModelRenderer.BakingContext, ItemRenderer>> FACTORIES = new HashMap<>();

    private BlockItemRenderers() {
    }

    /** Call from the group's client init (mod construction). The factory runs on every resource reload. */
    public static void register(DeferredBlock<?> block, Function<SpecialModelRenderer.BakingContext, ItemRenderer> factory) {
        FACTORIES.put(block.getId(), factory);
    }

    public record Unbaked(Block block) implements SpecialModelRenderer.Unbaked<ItemStack> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Unbaked::block)).apply(i, Unbaked::new));

        @Override
        public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
            Function<SpecialModelRenderer.BakingContext, ItemRenderer> factory = FACTORIES.get(BuiltInRegistries.BLOCK.getKey(this.block));
            return new Baked(factory == null ? null : factory.apply(context));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }

    private record Baked(ItemRenderer renderer) implements SpecialModelRenderer<ItemStack> {
        @Override
        public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
            if (this.renderer != null && stack != null) {
                this.renderer.submit(stack, poseStack, collector, light, overlay, foil);
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            for (int i = 0; i < 8; i++) {
                output.accept(new Vector3f(i & 1, i >> 1 & 1, i >> 2 & 1));
            }
        }

        @Override
        public ItemStack extractArgument(ItemStack stack) {
            return stack;
        }
    }
}
