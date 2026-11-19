package bibliocraft.printing.client;

import bibliocraft.client.BiblioRender;
import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.printing.TypesettingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
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
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityTypeSetRenderer: ModelTypeset (frame, chases, plates; typeset_top), ModelTypesetBase (typeset_base) and
 * ModelTypesetBook (typeset_book1 unsaved / book2 saved signed book, book3 enchanted book). Enchanted book and
 * enchanted plate glint. With the reading glasses (top face, close, HighlightHandler.typetable): the chosen book's
 * name, the number of chases, or the level cost and the enchantments of the enchanted book (in Config.color2).
 */
public class TypesettingRenderer implements BlockEntityRenderer<TypesettingBlockEntity, TypesettingRenderer.State> {
    static final Identifier TOP = PrintRender.tex("typeset_top");
    static final Identifier BASE = PrintRender.tex("typeset_base");
    static final String[] FRAME = {"topedge", "buttomedge", "mid1edge", "mid2edge", "leftedge", "rightedge", "bcedge", "m2edge", "m3edge", "m4edge",
            "m1edge", "trcedge", "tlcedge", "mcb1edge", "mcb2edge", "mcb3edge", "mcb4edge", "tl1edge", "tl2edge", "tr1edge", "tr2edge", "tlmc2edge",
            "tlmc1edge", "trmc2edge", "trmc1edge"};
    static final String[] LEFT_CHASE = {"blcRight", "blcLeft", "blcTop", "blcBottom"};
    private final Models models;
    private final Font font;

    public static class State extends BlockEntityRenderState {
        int angle;
        int chaseNum;
        boolean hasBook;
        boolean bookSaved;
        boolean newPlate;
        boolean oldPlate;
        boolean lowerChase;
        boolean enchantedBook;
        boolean enchantedPlate;
        @Nullable String bookname;
        @Nullable String chaseText;
        @Nullable String levels;
        String[] enchants = new String[0];
    }

    /** The three baked models. */
    record Models(PrintRender.Parts typeset, PrintRender.Parts base, PrintRender.Parts book) {
        static Models bake(Function<ModelLayerLocation, ModelPart> baker) {
            return new Models(new PrintRender.Parts(baker.apply(PrintingModels.TYPESET)), new PrintRender.Parts(baker.apply(PrintingModels.TYPESET_BASE)),
                    new PrintRender.Parts(baker.apply(PrintingModels.TYPESET_BOOK)));
        }

        /** renderChaseStack(num): up to four frames of four sides. */
        void chases(PoseStack pose, SubmitNodeCollector collector, int num, int light) {
            for (int n = 1; n <= Math.min(4, num); n++) {
                this.typeset.submit(pose, collector, TOP, light, "tlc" + n, "trc" + n, "tbc" + n, "ttc" + n);
            }
        }
    }

    public TypesettingRenderer(BlockEntityRendererProvider.Context context) {
        this.models = Models.bake(context::bakeLayer);
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TypesettingBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        state.chaseNum = be.chaseNum();
        state.hasBook = be.signedBookCheck();
        state.bookSaved = be.hasSavedBook();
        state.newPlate = be.hasNewPlate();
        state.oldPlate = be.hasOldPlate();
        state.lowerChase = be.hasLowerChase();
        state.enchantedBook = be.enchantedBookCheck();
        state.enchantedPlate = be.hasEnchantedPlate();
        state.bookname = null;
        state.chaseText = null;
        state.levels = null;
        state.enchants = new String[0];
        BiblioRender.Look look = BiblioRender.look(be.getBlockPos());
        if (look == null || look.side() != Direction.UP || !look.close()) {
            return;
        }
        double hitX = look.hitX();
        double hitZ = look.hitZ();
        boolean name;
        boolean chase;
        boolean levels;
        switch (state.angle) {
            case 0 -> {
                name = hitX > 0.4 && (hitX < 0.6 || hitZ < 0.3 || hitZ > 0.7);
                chase = hitX > 0.6 && hitZ > 0.3 && hitZ < 0.7;
                levels = hitX < 0.4 && hitZ < 0.5;
            }
            case 1 -> {
                name = hitZ > 0.4 && (hitZ < 0.6 || hitX < 0.3 || hitX > 0.7);
                chase = hitZ > 0.6 && hitX > 0.3 && hitX < 0.7;
                levels = hitX > 0.5 && hitZ < 0.4;
            }
            case 2 -> {
                name = hitX < 0.6 && (hitX > 0.4 || hitZ < 0.3 || hitZ > 0.7);
                chase = hitX < 0.4 && hitZ > 0.3 && hitZ < 0.7;
                levels = hitX > 0.6 && hitZ > 0.5;
            }
            default -> {
                name = hitZ < 0.6 && (hitZ > 0.4 || hitX < 0.3 || hitX > 0.7);
                chase = hitZ < 0.4 && hitX > 0.3 && hitX < 0.7;
                levels = hitX < 0.5 && hitZ > 0.6;
            }
        }
        if (levels && state.enchantedBook) {
            state.levels = "Requires " + be.levels() + " Levels";
            // ench1..ench4: "test" where the book has fewer enchantments; as many lines as it has (at least one)
            int count = TypesettingBlockEntity.stored(be.getItem(0)).size();
            String[] lines = new String[Math.max(1, Math.min(4, count))];
            java.util.Arrays.fill(lines, "test");
            int i = 0;
            for (Object2IntMap.Entry<Holder<Enchantment>> e : TypesettingBlockEntity.stored(be.getItem(0)).entrySet()) {
                if (i < lines.length) {
                    lines[i] = Enchantment.getFullname(e.getKey(), e.getIntValue()).getString();
                }
                i++;
            }
            state.enchants = lines;
        }
        if (name) {
            state.bookname = be.bookname();
        }
        if (chase && state.chaseNum > 0) {
            state.chaseText = be.getItem(1).getCount() + " Chases";
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float iadjust;
        float kadjust;
        switch (state.angle) {
            case 0 -> {
                iadjust = -0.04F;
                kadjust = 0.0F;
            }
            case 1 -> {
                iadjust = -0.02F;
                kadjust = -0.02F;
            }
            case 2 -> {
                iadjust = 0.0F;
                kadjust = 0.0F;
            }
            default -> {
                iadjust = -0.02F;
                kadjust = 0.02F;
            }
        }
        int light = state.lightCoords;
        pose.pushPose();
        pose.translate(0.52F + iadjust, 0.62F + 0.3F, 0.5 + kadjust);
        pose.rotateDegrees(Axis.YP, PrintRender.degrees(state.angle));
        pose.rotateDegrees(Axis.XP, 180.0F);
        pose.pushPose();
        pose.scale(1.0F / 3.0F, 1.0F / 3.0F, 1.0F / 3.0F);
        this.models.typeset.submit(pose, collector, TOP, light, FRAME);
        this.models.chases(pose, collector, state.chaseNum, light);
        if (state.newPlate) {
            if (state.enchantedPlate) {
                this.models.typeset.submitGlint(pose, collector, TOP, light, "brPlate");
            } else {
                this.models.typeset.submit(pose, collector, TOP, light, "brPlate");
            }
        }
        if (state.oldPlate) {
            this.models.typeset.submit(pose, collector, TOP, light, "blPlate");
        }
        if (state.lowerChase) {
            this.models.typeset.submit(pose, collector, TOP, light, LEFT_CHASE);
        }
        this.models.base.submit(pose, collector, BASE, light, "base");
        if (state.hasBook) {
            this.models.book.submit(pose, collector, PrintRender.tex(state.bookSaved ? "typeset_book2" : "typeset_book1"), light, "book");
        }
        if (state.enchantedBook) {
            this.models.book.submitGlint(pose, collector, PrintRender.tex("typeset_book3"), light, "book");
        }
        pose.popPose();
        if (state.levels != null) {
            // renderText(levels) leaves its scale and translate on the matrix, the enchantment lines follow from there
            pose.pushPose();
            pose.scale(0.005F, 0.005F, 0.005F);
            pose.translate(0.0, 0.0, -5.0);
            PrintRender.centered(pose, collector, this.font, state.levels, -8, PrintRender.COLOR2, light);
            pose.translate(-44.0, -2.0, -94.0);
            for (int l = 0; l < state.enchants.length; l++) {
                PrintRender.centered(pose, collector, this.font, state.enchants[l], l * 10, PrintRender.COLOR2, light);
            }
            pose.popPose();
        }
        if (state.bookname != null) {
            this.text(pose, collector, state.bookname, 0.0, light);
        }
        if (state.chaseText != null) {
            this.text(pose, collector, state.chaseText, 32.0, light);
        }
        pose.popPose();
    }

    /** renderText(text, 0, 0, zadjust, Config.color). */
    private void text(PoseStack pose, SubmitNodeCollector collector, String text, double zadjust, int light) {
        pose.pushPose();
        pose.scale(0.005F, 0.005F, 0.005F);
        pose.translate(0.0, 0.0, -5.0 + zadjust);
        PrintRender.centered(pose, collector, this.font, text, -8, BCConfig.textColor(), light);
        pose.popPose();
    }

    /** ItemTypesetRenderer: frame, three chase frames, a new plate and the base, at 0.85. */
    static void submitItem(Models models, PoseStack pose, SubmitNodeCollector collector, int light) {
        pose.pushPose();
        pose.translate(0.5, 0.89, 0.5);
        pose.rotateDegrees(Axis.XP, 180.0F);
        pose.scale(0.85F / 3.0F, 0.85F / 3.0F, 0.85F / 3.0F);
        models.typeset.submit(pose, collector, TOP, light, FRAME);
        models.chases(pose, collector, 3, light);
        models.typeset.submit(pose, collector, TOP, light, "brPlate");
        models.base.submit(pose, collector, BASE, light, "base");
        pose.popPose();
    }
}
