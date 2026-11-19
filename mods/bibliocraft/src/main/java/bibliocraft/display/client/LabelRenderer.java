package bibliocraft.display.client;

import bibliocraft.BiblioCraft;
import bibliocraft.client.BiblioRender;
import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.display.LabelBlock;
import bibliocraft.display.LabelBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityLabelRenderer: the plate on the wall and its three items (slot 1 left, slot 0 centre, slot 2 right; the
 * centre item alone is drawn bigger), with the reading glasses the name of the item under the cursor
 * (HighlightHandler.label; with only the centre slot used always that one). All three names are drawn at the same
 * place in the middle of the plate, as in the original. Only the "fancy graphics" item turning is ported.
 */
public class LabelRenderer implements BlockEntityRenderer<LabelBlockEntity, LabelRenderer.State> {
    // ichange1/kchange1 (left), ichange2/kchange2 (right), ichange3/kchange3 (centre) per angle
    private static final float[][] LEFT = {{0.97F, 0.35F}, {0.65F, 0.97F}, {0.03F, 0.65F}, {0.35F, 0.03F}};
    private static final float[][] RIGHT = {{0.97F, 0.65F}, {0.35F, 0.97F}, {0.03F, 0.35F}, {0.65F, 0.03F}};
    private static final float[][] CENTRE = {{0.97F, 0.5F}, {0.5F, 0.97F}, {0.03F, 0.5F}, {0.5F, 0.03F}};
    private static final double[][] PLATE = {{0.99, 0.5}, {0.5, 0.99}, {0.01, 0.5}, {0.5, 0.01}};
    private final ModelPart model;
    private final Font font;
    private final ItemModelResolver itemModels;

    public static class State extends BlockEntityRenderState {
        int angle;
        int wood;
        final @Nullable ItemStackRenderState[] items = new ItemStackRenderState[3];
        final boolean[] blocks = new boolean[3];
        @Nullable String text;
    }

    public LabelRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(DisplayModels.LABEL);
        this.font = context.font();
        this.itemModels = context.itemModelResolver();
    }

    /** getModelTexture: woodlabel0..3 by wood. */
    static Identifier texture(int wood) {
        return BiblioCraft.id("textures/model/woodlabel" + wood + ".png");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LabelBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        state.wood = be.getBlockState().getBlock() instanceof LabelBlock b ? b.wood : 0;
        for (int i = 0; i < 3; i++) {
            ItemStack stack = be.getItem(i);
            state.items[i] = BiblioRender.item(this.itemModels, stack, ItemDisplayContext.GROUND, be, i);
            state.blocks[i] = !stack.isEmpty() && BCConfig.isBlock(stack);
        }
        state.text = null;
        BiblioRender.Look look = BiblioRender.look(be.getBlockPos());
        if (look != null) {
            int slot = be.getItem(1).isEmpty() && be.getItem(2).isEmpty() ? 0 : lookedAt(state.angle, look.hitX(), look.hitZ());
            if (!be.getItem(slot).isEmpty()) {
                state.text = be.getItem(slot).getHoverName().getString();
            }
        }
    }

    /** HighlightHandler.label: the slot under the cursor, 1 left, 2 right, 0 centre (0.43..0.57 across the plate). */
    static int lookedAt(int angle, double hitX, double hitZ) {
        return switch (angle) {
            case 0 -> hitZ < 0.43 ? 1 : hitZ > 0.57 ? 2 : 0;
            case 1 -> hitX < 0.43 ? 2 : hitX > 0.57 ? 1 : 0;
            case 2 -> hitZ < 0.43 ? 2 : hitZ > 0.57 ? 1 : 0;
            default -> hitX < 0.43 ? 1 : hitX > 0.57 ? 2 : 0;
        };
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int angle = state.angle;
        float deg = WeaponCaseRenderer.degrees(angle);
        int light = state.lightCoords;
        poseStack.pushPose();
        poseStack.translate(PLATE[angle][0], 0.3F, PLATE[angle][1]);
        poseStack.rotateDegrees(Axis.YP, deg);
        poseStack.rotateDegrees(Axis.XP, 180.0F);
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        collector.submitModelPart(this.model, poseStack, RenderTypes.entityCutout(texture(state.wood)), light, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        if (state.text != null) {
            // renderText
            poseStack.scale(0.005F, 0.005F, 0.005F);
            poseStack.translate(0.0, -25.0, -6.5);
            DisplayText.centered(poseStack, collector, this.font, state.text, -8, BCConfig.textColor(), light);
        }
        poseStack.popPose();
        boolean sides = state.items[1] != null || state.items[2] != null;
        if (state.items[1] != null) {
            this.sideItem(state.items[1], state.blocks[1], LEFT[angle], deg, poseStack, collector, light);
        }
        if (state.items[0] != null) {
            boolean block = state.blocks[0];
            poseStack.pushPose();
            poseStack.translate(CENTRE[angle][0], 0.24F, CENTRE[angle][1]);
            poseStack.rotateDegrees(Axis.YP, block ? deg : deg + 180.0F);
            if (!sides) {
                poseStack.scale(0.8F, 0.8F, 0.8F);
                if (block) {
                    poseStack.translate(0.0, 0.06F, 0.0);
                } else {
                    poseStack.scale(0.75F, 0.75F, 0.75F);
                }
            } else if (block) {
                poseStack.scale(0.45F, 0.45F, 0.45F);
            } else {
                poseStack.scale(0.35F, 0.35F, 0.35F);
            }
            if (sides && block) {
                poseStack.translate(0.0, 0.02F, 0.0);
            }
            DisplayText.entityItem(state.items[0], block, poseStack, collector, light);
            poseStack.popPose();
        }
        if (state.items[2] != null) {
            this.sideItem(state.items[2], state.blocks[2], RIGHT[angle], deg, poseStack, collector, light);
        }
    }

    /** The left and right items: blocks turned to the front at 0.45, items turned round at 0.35. */
    private void sideItem(ItemStackRenderState item, boolean block, float[] pos, float deg, PoseStack poseStack, SubmitNodeCollector collector,
            int light) {
        poseStack.pushPose();
        poseStack.translate(pos[0], 0.32F, pos[1]);
        poseStack.rotateDegrees(Axis.YP, block ? deg : deg + 180.0F);
        if (block) {
            poseStack.translate(0.0, 0.05F, 0.0);
            poseStack.scale(0.45F, 0.45F, 0.45F);
        } else {
            poseStack.scale(0.35F, 0.35F, 0.35F);
        }
        DisplayText.entityItem(item, block, poseStack, collector, light);
        poseStack.popPose();
    }
}
