package betterstorage.client;

import betterstorage.ChestMaterial;
import betterstorage.block.BackpackBlock;
import betterstorage.block.LockerBlock;
import betterstorage.block.ReinforcedChestBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * BetterStorageRenderingHandler.renderInventoryBlock and ItemRendererBackpack: the reinforced chests, the locker and
 * the backpacks are drawn as their block (closed, facing the viewer) in the item cube; the display transforms come
 * from the base model (minecraft:item/template_chest). Item JSON: {@code {"type": "betterstorage:block", "block": ...}}.
 */
public record BlockItemRenderer(Block block) implements SpecialModelRenderer.Unbaked<ItemStack> {
    public static final MapCodec<BlockItemRenderer> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockItemRenderer::block)).apply(i, BlockItemRenderer::new));

    private interface Drawer {
        void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light);
    }

    @Override
    public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
        Drawer drawer;
        if (this.block instanceof ReinforcedChestBlock chest) {
            BSModels.ChestModel model = new BSModels.ChestModel(context.entityModelSet().bakeLayer(BSModels.CHEST));
            ChestMaterial material = chest.material;
            drawer = (stack, pose, collector, light) -> ReinforcedChestRenderer.submitChest(pose, collector, model,
                    ReinforcedChestRenderer.texture(material, false), false, 0, 0.0F, light);
        } else if (this.block instanceof LockerBlock) {
            ObjModel model = ObjModel.load(betterstorage.BetterStorage.id("models/obj/locker.obj"));
            drawer = (stack, pose, collector, light) -> LockerRenderer.submitLocker(pose, collector, model, LockerRenderer.TEXTURE, false, 0, 0.0F,
                    light);
        } else if (this.block instanceof BackpackBlock backpack) {
            BSModels.BackpackModel model = new BSModels.BackpackModel(context.entityModelSet().bakeLayer(BSModels.BACKPACK));
            boolean ender = backpack.ender;
            // renderInventoryBlock(rotation -160, scale 1.5, yOffset 0.14): here turned to show the pocket, 1.5 times as big
            drawer = (stack, pose, collector, light) -> {
                pose.translate(0.5F, -0.06F, 0.5F);
                pose.scale(1.5F, 1.5F, 1.5F);
                pose.translate(-0.5F, 0.0F, -0.5F);
                BackpackRenderer.submitBackpack(pose, collector, model, ender, BackpackRenderer.color(stack), 180.0F, 0.0F, light);
            };
        } else {
            drawer = null;
        }
        return new Baked(drawer);
    }

    @Override
    public MapCodec<BlockItemRenderer> type() {
        return MAP_CODEC;
    }

    private record Baked(Drawer drawer) implements SpecialModelRenderer<ItemStack> {
        @Override
        public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
            if (this.drawer != null && stack != null) {
                poseStack.pushPose();
                this.drawer.submit(stack, poseStack, collector, light);
                poseStack.popPose();
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
