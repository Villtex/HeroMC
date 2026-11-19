package bibliocraft.printing.client;

import bibliocraft.client.BiblioRender;
import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.printing.PrintPressBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityPrintPressRenderer: ink plate (inkplate0..8 by the amount of ink), the press (printpress), the plate, the
 * blank books (one to four) and the printed book (pressbooks, pressbooks2 for an enchanted book); enchanted plate and
 * enchanted book glint. The press animation runs here as in the original: every frame the plate and the rollers move
 * a step (the roller swings while a print runs, "pressani" false, then the press plate comes down). With the reading
 * glasses (top face, close, HighlightHandler.printpress): the plate's book, the ink count or the blank book count.
 */
public class PrintPressRenderer implements BlockEntityRenderer<PrintPressBlockEntity, PrintPressRenderer.State> {
    static final Identifier PRESS = PrintRender.tex("printpress");
    static final String[] MAIN = {"LLeg", "RLeg", "frontbrace", "bottombrace", "backbrace", "pibrace", "pback", "table", "irshaft"};
    private final Models models;
    private final Font font;

    public static class State extends BlockEntityRenderState {
        int angle;
        int emptyBooks;
        boolean finishedBook;
        boolean plate;
        boolean enchantedPlate;
        boolean enchantedBook;
        int inksize;
        float pressRot;
        float rollerRot;
        @Nullable String text;
        int textColor;
        double tx;
        double ty;
        double tz;
    }

    /** ModelPrintPress, ModelPrintPressBooks and ModelInkPlate. */
    record Models(PrintRender.Parts press, PrintRender.Parts books, PrintRender.Parts ink) {
        static Models bake(Function<ModelLayerLocation, ModelPart> baker) {
            return new Models(new PrintRender.Parts(baker.apply(PrintingModels.PRINT_PRESS)),
                    new PrintRender.Parts(baker.apply(PrintingModels.PRINT_PRESS_BOOKS)), new PrintRender.Parts(baker.apply(PrintingModels.INK_PLATE)));
        }

        /** renderPress / renderRollers with rotateAngleX. */
        void pressAndRollers(PoseStack pose, SubmitNodeCollector collector, float pressRot, float rollerRot, int light) {
            pose.pushPose();
            PrintRender.rotateX(pose, this.press.get("press"), pressRot);
            this.press.submit(pose, collector, PRESS, light, "press");
            pose.popPose();
            pose.pushPose();
            PrintRender.rotateX(pose, this.press.get("inkRollers"), rollerRot);
            this.press.submit(pose, collector, PRESS, light, "inkRollers");
            pose.popPose();
        }
    }

    public PrintPressRenderer(BlockEntityRendererProvider.Context context) {
        this.models = Models.bake(context::bakeLayer);
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    /** The animation steps of renderTileEntityAt, on the client copy of the block entity (once per frame). */
    private static void animate(PrintPressBlockEntity t, State state) {
        float move1 = t.move1;
        float move2 = t.move2;
        if (t.plateturn) {
            if (move1 < 0.3F && !t.movetest1) {
                t.move1 = move1 + 0.016666668F;
                if (t.move1 >= 0.3F) {
                    t.movetest1 = true;
                }
            } else {
                float m = move1 - 0.016666668F;
                t.move1 = m;
                if (m <= 0.28F) {
                    t.rollerturn = true;
                }
                if (m <= 0.0F) {
                    t.movetest1 = false;
                    t.plateturn = false;
                }
            }
        }
        state.pressRot = 0.5F - t.move1;
        if ((!t.pressAnimation() || move2 >= 0.0F) && t.rollerturn) {
            if (move2 < 1.7F && !t.movetest2) {
                t.move2 = move2 += 0.071428575F;
                if (move2 >= 1.7F) {
                    t.movetest2 = true;
                }
            } else {
                t.move2 = move2 -= 0.071428575F;
                if (move2 <= 1.4F) {
                    t.plateturn = true;
                }
                if (move2 <= 0.0F) {
                    t.movetest2 = false;
                    t.rollerturn = false;
                }
            }
        }
        state.rollerRot = 0.12F + move2;
    }

    @Override
    public void extractRenderState(PrintPressBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        state.emptyBooks = be.numberBlanks();
        state.finishedBook = be.hasSigned();
        state.plate = be.hasPlate();
        state.enchantedPlate = be.hasEnchantedPlate();
        state.enchantedBook = be.hasEnchantedBook();
        state.inksize = be.inkStackSize();
        animate(be, state);
        state.text = null;
        BiblioRender.Look look = BiblioRender.look(be.getBlockPos());
        if (look == null || look.side() != Direction.UP || !look.close()) {
            return;
        }
        double hitX = look.hitX();
        double hitZ = look.hitZ();
        boolean plate;
        boolean books;
        boolean ink;
        switch (state.angle) {
            case 0 -> {
                plate = hitX > 0.4 && hitX < 0.7;
                books = hitX < 0.4 && hitZ < 0.5;
                ink = hitX > 0.7;
            }
            case 1 -> {
                plate = hitZ > 0.4 && hitZ < 0.7;
                books = hitZ < 0.4 && hitX > 0.5;
                ink = hitZ > 0.7;
            }
            case 2 -> {
                plate = hitX < 0.6 && hitX > 0.3;
                books = hitX > 0.6 && hitZ > 0.5;
                ink = hitX < 0.3;
            }
            default -> {
                plate = hitZ < 0.6 && hitZ > 0.3;
                books = hitZ > 0.6 && hitX < 0.5;
                ink = hitZ < 0.3;
            }
        }
        if (plate && be.hasPlate()) {
            state.text = be.plateName();
            state.textColor = state.enchantedPlate ? PrintRender.COLOR2 : BCConfig.textColor();
            state.tx = 0.0;
            state.ty = 0.0;
            state.tz = 0.0;
        } else if (ink && be.hasInk()) {
            state.text = be.getItem(0).getCount() + " Ink";
            state.textColor = BCConfig.textColor();
            state.tx = 0.0;
            state.ty = -16.0;
            state.tz = 10.0;
        } else if (books && be.hasBlank()) {
            state.text = be.getItem(2).getCount() + "  Books";
            state.textColor = BCConfig.textColor();
            state.tx = -68.0;
            state.ty = -6.0;
            state.tz = -135.0;
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float iadjust;
        float kadjust;
        switch (state.angle) {
            case 0 -> {
                iadjust = 0.3F;
                kadjust = 0.0F;
            }
            case 1 -> {
                iadjust = 0.15F;
                kadjust = 0.15F;
            }
            case 2 -> {
                iadjust = 0.0F;
                kadjust = 0.0F;
            }
            default -> {
                iadjust = 0.15F;
                kadjust = -0.15F;
            }
        }
        int light = state.lightCoords;
        pose.pushPose();
        pose.translate(0.35F + iadjust, 0.85F + 0.3F, 0.5 + kadjust);
        pose.rotateDegrees(Axis.YP, PrintRender.degrees(state.angle));
        pose.rotateDegrees(Axis.XP, 180.0F);
        pose.pushPose();
        pose.scale(0.8F, 0.8F, 0.8F);
        this.models.ink.submit(pose, collector, PrintRender.tex("inkplate" + state.inksize), light, "inkplate");
        this.models.press.submit(pose, collector, PRESS, light, MAIN);
        if (state.plate) {
            if (state.enchantedPlate) {
                this.models.press.submitGlint(pose, collector, PRESS, light, "plate");
            } else {
                this.models.press.submit(pose, collector, PRESS, light, "plate");
            }
        }
        this.models.pressAndRollers(pose, collector, state.pressRot, state.rollerRot, light);
        Identifier books = PrintRender.tex(state.enchantedBook ? "pressbooks2" : "pressbooks");
        for (int b = 1; b <= Math.min(4, state.emptyBooks); b++) {
            this.models.books.submit(pose, collector, books, light, "Bbook" + b);
        }
        if (state.finishedBook) {
            if (state.enchantedBook) {
                this.models.books.submitGlint(pose, collector, books, light, "Fbook");
            } else {
                this.models.books.submit(pose, collector, books, light, "Fbook");
            }
        }
        pose.popPose();
        if (state.text != null) {
            // renderText: scale 0.005, translate (x, 20 + y, -5 + z), drawString at (-width/2, 0)
            pose.scale(0.005F, 0.005F, 0.005F);
            pose.translate(state.tx, 20.0 + state.ty, -5.0 + state.tz);
            PrintRender.centered(pose, collector, this.font, state.text, 0, state.textColor, light);
        }
        pose.popPose();
    }

    /** getRenderBoundingBox: two blocks high. */
    @Override
    public AABB getRenderBoundingBox(PrintPressBlockEntity be) {
        return new AABB(be.getBlockPos()).expandTowards(0, 1, 0);
    }

    /** ItemPrintPressRenderer: frame, press (0.5), plate, rollers (0.2) and the clean ink plate, at 0.85. */
    static void submitItem(Models models, PoseStack pose, SubmitNodeCollector collector, int light) {
        pose.pushPose();
        pose.translate(0.5, 0.93, 0.5);
        pose.rotateDegrees(Axis.XP, 180.0F);
        pose.scale(0.8F * 0.85F, 0.8F * 0.85F, 0.8F * 0.85F);
        models.press.submit(pose, collector, PRESS, light, MAIN);
        models.press.submit(pose, collector, PRESS, light, "plate");
        models.pressAndRollers(pose, collector, 0.5F, 0.2F, light);
        models.ink.submit(pose, collector, PrintRender.tex("inkplate0"), light, "inkplate");
        pose.popPose();
    }
}
