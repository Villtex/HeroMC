package bibliocraft.printing.client;

import bibliocraft.client.BiblioRender;
import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.printing.WritingDeskBlock;
import bibliocraft.printing.WritingDeskBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityWritingDeskRenderer: the desk in the texture of its wood, the open book, one to four books on each side,
 * and with the reading glasses (close by) the name of the open book over the front edge.
 */
public class WritingDeskRenderer implements BlockEntityRenderer<WritingDeskBlockEntity, WritingDeskRenderer.State> {
    static final Identifier BOOKS = PrintRender.tex("writingdeskbooks");
    static final String[] DESK = {"top", "rightside", "rightfoot", "leftfoot", "leftside", "back", "shelf", "candlestick", "candleholder", "inkwell",
            "pen", "ring"};
    static final String[] WRITTEN = {"wBookBack", "wBookRight", "wBookLeft"};
    static final String[] LEFT = {"lbook1", "lbook2", "lbook3", "lbook4"};
    static final String[] RIGHT = {"rbook1", "rbook2", "rbook3", "rbook4"};
    private final PrintRender.Parts parts;
    private final Font font;

    public static class State extends BlockEntityRenderState {
        int angle;
        int wood;
        int left;
        int right;
        boolean book;
        @Nullable String text;
    }

    public WritingDeskRenderer(BlockEntityRendererProvider.Context context) {
        this.parts = bake(context::bakeLayer);
        this.font = context.font();
    }

    static PrintRender.Parts bake(Function<ModelLayerLocation, ModelPart> baker) {
        return new PrintRender.Parts(baker.apply(PrintingModels.WRITING_DESK));
    }

    /** getModelTexture: writingdesk1..4 by wood. */
    static Identifier texture(int wood) {
        return PrintRender.tex("writingdesk" + (wood + 1));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WritingDeskBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        state.wood = be.getBlockState().getBlock() instanceof WritingDeskBlock b ? b.wood : 0;
        state.left = be.leftBookStack();
        state.right = be.rightBookStack();
        state.book = be.hasWrittenBook();
        state.text = null;
        // HighlightHandler: setShowBookName when close, on any face
        BiblioRender.Look look = BiblioRender.look(be.getBlockPos());
        if (look != null && look.close() && state.book) {
            state.text = be.openBook().getHoverName().getString();
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5, 1.0, 0.5);
        pose.rotateDegrees(Axis.YP, PrintRender.degrees(state.angle));
        pose.rotateDegrees(Axis.XP, 180.0F);
        pose.pushPose();
        pose.scale(0.5F, 0.5F, 0.5F);
        int light = state.lightCoords;
        this.parts.submit(pose, collector, texture(state.wood), light, DESK);
        if (state.book) {
            this.parts.submit(pose, collector, BOOKS, light, WRITTEN);
        }
        for (int b = 0; b < Math.min(4, state.left); b++) {
            this.parts.submit(pose, collector, BOOKS, light, LEFT[b]);
        }
        for (int b = 0; b < Math.min(4, state.right); b++) {
            this.parts.submit(pose, collector, BOOKS, light, RIGHT[b]);
        }
        pose.popPose();
        if (state.text != null) {
            // renderText: scale 0.005, translate (0, 15, -100.5), drawString at (-width/2, -8)
            pose.scale(0.005F, 0.005F, 0.005F);
            pose.translate(0.0, 15.0, -100.5);
            PrintRender.centered(pose, collector, this.font, state.text, -8, BCConfig.textColor(), light);
        }
        pose.popPose();
    }

    /** getRenderBoundingBox: two blocks high. */
    @Override
    public AABB getRenderBoundingBox(WritingDeskBlockEntity be) {
        return new AABB(be.getBlockPos()).expandTowards(0, 1, 0);
    }

    /** ItemWritingDeskRenderer: the desk with every book, at 1/39 instead of 1/32 per unit. */
    static void submitItem(PrintRender.Parts parts, int wood, PoseStack pose, SubmitNodeCollector collector, int light) {
        float s = 16.0F / 39.0F;
        pose.pushPose();
        // the model is 32 units high: its top (y 0) half its height above the middle of the item cube
        pose.translate(0.5, 0.5 + s, 0.5);
        pose.rotateDegrees(Axis.XP, 180.0F);
        pose.scale(s, s, s);
        parts.submit(pose, collector, texture(wood), light, DESK);
        parts.submit(pose, collector, BOOKS, light, WRITTEN);
        parts.submit(pose, collector, BOOKS, light, LEFT);
        parts.submit(pose, collector, BOOKS, light, RIGHT);
        pose.popPose();
    }
}
