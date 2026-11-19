package bibliocraft.shelf.client;

import bibliocraft.BiblioCraft;
import bibliocraft.client.BiblioRender;
import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.shelf.ShelfBlock;
import bibliocraft.shelf.ShelfBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityGenericShelfRenderer: the shelf (with the upper centre board when a block stands on top), the four items
 * at full size (blocks turned by the angle, items by the angle + 180), and with the reading glasses the name of the
 * item under the cross-hair (HighlightHandler.shelf sets show1..4). The original cleared a show flag only when it drew
 * the text, so a flag set on an empty place waited for an item for one frame; that is left out.
 */
public class GenericShelfRenderer implements BlockEntityRenderer<ShelfBlockEntity, GenericShelfRenderer.State> {
    /** ichange1, ichange2, kchange1, kchange2, xchange, zchange by angle. */
    private static final float[][] OFFSETS = {
            {0.75F, 0.75F, 0.25F, 0.75F, 0.5F, 0.0F},
            {0.75F, 0.25F, 0.75F, 0.75F, 0.0F, 0.5F},
            {0.25F, 0.25F, 0.75F, 0.25F, -0.5F, 0.0F},
            {0.25F, 0.75F, 0.25F, 0.25F, 0.0F, -0.5F}};
    /** The text position (xadjust, yadjust) of the four places. */
    private static final double[][] TEXT = {{-50.0, 0.0}, {50.0, 0.0}, {-50.0, 100.0}, {50.0, 100.0}};

    private final Parts parts;
    private final ItemModelResolver itemModelResolver;
    private final Font font;

    public static class State extends BlockEntityRenderState {
        int angle;
        int wood;
        boolean top;
        @Nullable ItemStackRenderState[] items = new ItemStackRenderState[4];
        final boolean[] blocks = new boolean[4];
        int textSlot = -1;
        @Nullable String text;
    }

    /** ModelGenericShelf: renderShelf (topshelf, bottomshelf, centerBottom) and renderTop (centerTop). */
    public record Parts(ModelPart topshelf, ModelPart bottomshelf, ModelPart centerBottom, ModelPart centerTop) {
        static Parts of(ModelPart root) {
            return new Parts(root.getChild("topshelf"), root.getChild("bottomshelf"), root.getChild("centerBottom"), root.getChild("centerTop"));
        }

        void submit(PoseStack poseStack, SubmitNodeCollector collector, int wood, boolean top, int light) {
            var type = RenderTypes.entityCutout(texture(wood));
            collector.submitModelPart(this.topshelf, poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
            collector.submitModelPart(this.bottomshelf, poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
            collector.submitModelPart(this.centerBottom, poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
            if (top) {
                collector.submitModelPart(this.centerTop, poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
            }
        }
    }

    public GenericShelfRenderer(BlockEntityRendererProvider.Context context) {
        this.parts = Parts.of(context.bakeLayer(ShelfModels.SHELF));
        this.itemModelResolver = context.itemModelResolver();
        this.font = context.font();
    }

    /** getModelTexture: genericshelf1..4 by wood. */
    public static Identifier texture(int wood) {
        return BiblioCraft.id("textures/model/genericshelf" + (wood + 1) + ".png");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ShelfBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        state.wood = be.getBlockState().getBlock() instanceof ShelfBlock b ? b.wood : 0;
        state.top = be.hasTop();
        state.items = ShelfItems.extract(this.itemModelResolver, be);
        for (int i = 0; i < 4; i++) {
            state.blocks[i] = !be.getItem(i).isEmpty() && BCConfig.isBlock(be.getItem(i));
        }
        state.text = null;
        state.textSlot = -1;
        BiblioRender.Look look = BiblioRender.look(be.getBlockPos());
        if (look != null) {
            int slot = showSlot(state.angle, look.hitX(), look.hitY(), look.hitZ());
            if (slot >= 0 && !be.getItem(slot).isEmpty()) {
                state.textSlot = slot;
                state.text = BiblioRender.label(be.getItem(slot));
            }
        }
    }

    /** HighlightHandler.shelf: the place whose show flag the hit sets, -1 on the middle lines. */
    static int showSlot(int angle, double hitX, double hitY, double hitZ) {
        int row = hitY > 0.5 ? 0 : hitY < 0.5 ? 2 : -1;
        double across = angle == 0 || angle == 2 ? hitZ : hitX;
        if (row < 0 || across == 0.5) {
            return -1;
        }
        boolean high = across > 0.5;
        // the first place lies at z < 0.5 (angle 0), x > 0.5 (1), z > 0.5 (2), x < 0.5 (3)
        boolean first = angle == 0 || angle == 3 ? !high : high;
        return row + (first ? 0 : 1);
    }

    /** The frame of the model: (0.5 + xchange, 1, 0.5 + zchange), turned by degreeAngle + 180 and upside down. */
    static void frame(PoseStack poseStack, int angle) {
        float[] o = OFFSETS[angle];
        poseStack.translate(0.5 + o[4], 1.0, 0.5 + o[5]);
        poseStack.rotateDegrees(Axis.YP, PotionShelfRenderer.degrees(angle) + 180.0F);
        poseStack.rotateDegrees(Axis.XP, 180.0F);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        frame(poseStack, state.angle);
        this.parts.submit(poseStack, collector, state.wood, state.top, state.lightCoords);
        if (state.text != null) {
            // renderText: scale 0.005, turn 180, translate (xadjust, 90 + yadjust, -102)
            poseStack.pushPose();
            poseStack.scale(0.005F, 0.005F, 0.005F);
            poseStack.rotateDegrees(Axis.YP, 180.0F);
            poseStack.translate(TEXT[state.textSlot][0], 90.0 + TEXT[state.textSlot][1], -102.0);
            BiblioRender.drawCentered(poseStack, collector, this.font, state.text, state.lightCoords);
            poseStack.popPose();
        }
        poseStack.popPose();
        float[] o = OFFSETS[state.angle];
        float deg = PotionShelfRenderer.degrees(state.angle);
        for (int slot = 0; slot < 4; slot++) {
            if (state.items[slot] == null) {
                continue;
            }
            boolean right = slot == 1 || slot == 3;
            poseStack.pushPose();
            poseStack.translate(right ? o[1] : o[0], slot < 2 ? 0.66F : 0.17F, right ? o[3] : o[2]);
            poseStack.rotateDegrees(Axis.YP, state.blocks[slot] ? deg : deg + 180.0F);
            ShelfItems.submit(state.items[slot], poseStack, collector, state.lightCoords);
            poseStack.popPose();
        }
    }

    /** The model as ItemGenericShelfRenderer drew it (with the top board), placed like a shelf of angle 1. */
    static void submitItem(Parts parts, PoseStack poseStack, SubmitNodeCollector collector, int wood, int light) {
        poseStack.pushPose();
        frame(poseStack, 1);
        parts.submit(poseStack, collector, wood, true, light);
        poseStack.popPose();
    }
}
