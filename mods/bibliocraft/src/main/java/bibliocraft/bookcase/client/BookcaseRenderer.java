package bibliocraft.bookcase.client;

import bibliocraft.BiblioCraft;
import bibliocraft.bookcase.BookcaseBlock;
import bibliocraft.bookcase.BookcaseBlockEntity;
import bibliocraft.client.BiblioRender;
import bibliocraft.common.BiblioBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityBookcaseRenderer: the shelf in the texture of its wood, a book model for each filled slot, and with the
 * reading glasses the name of the book under the cross-hair (HighlightHandler.bookcase sets showText).
 */
public class BookcaseRenderer implements BlockEntityRenderer<BookcaseBlockEntity, BookcaseRenderer.State> {
    public static final Identifier BOOKS_TEXTURE = BiblioCraft.id("textures/model/bookcase_books.png");
    private final Parts parts;
    private final Font font;

    public static class State extends BlockEntityRenderState {
        int angle;
        int wood;
        final boolean[] books = new boolean[16];
        @Nullable String text;
    }

    /** The baked shelf and the 16 books. */
    public record Parts(ModelPart shelf, ModelPart[] books) {
        public static Parts bake(java.util.function.Function<net.minecraft.client.model.geom.ModelLayerLocation, ModelPart> baker) {
            ModelPart b = baker.apply(BookcaseModels.BOOKS);
            ModelPart[] books = new ModelPart[16];
            for (int i = 0; i < 16; i++) {
                books[i] = b.getChild("Book" + (i + 1));
            }
            return new Parts(baker.apply(BookcaseModels.SHELF), books);
        }

        /** renderShelf + renderBook(r + 1) for each {@code books[r]}, at scale 1/32 around the current pose. */
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int wood, boolean[] filled, int light, int overlay) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            collector.submitModelPart(this.shelf, poseStack, RenderTypes.entityCutout(shelfTexture(wood)), light, overlay, null);
            for (int r = 0; r < 16; r++) {
                if (filled[r]) {
                    collector.submitModelPart(this.books[r], poseStack, RenderTypes.entityCutout(BOOKS_TEXTURE), light, overlay, null);
                }
            }
            poseStack.popPose();
        }
    }

    /** getModelTexture: bookcase_empty1..4 by wood. */
    public static Identifier shelfTexture(int wood) {
        return BiblioCraft.id("textures/model/bookcase_empty" + (wood + 1) + ".png");
    }

    public BookcaseRenderer(BlockEntityRendererProvider.Context context) {
        this.parts = Parts.bake(context::bakeLayer);
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BookcaseBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        state.wood = be.getBlockState().getBlock() instanceof BookcaseBlock b ? b.wood : 0;
        for (int i = 0; i < 16; i++) {
            state.books[i] = !be.getItem(i).isEmpty();
        }
        state.text = null;
        BiblioRender.Look look = BiblioRender.look(be.getBlockPos());
        if (look != null) {
            int book = -1;
            if (look.hitY() > 0.5) {
                book = BookcaseBlock.whatBook(state.angle, look.hitX(), look.hitZ());
            } else if (look.hitY() < 0.5) {
                book = BookcaseBlock.whatBook(state.angle, look.hitX(), look.hitZ()) + 8;
            }
            if (book >= 0 && book < 16 && !be.getItem(book).isEmpty()) {
                state.text = bookName(be.getItem(book));
            }
        }
    }

    /** The shown name: count and name; for an enchanted book the first stored enchantment with its level. */
    static String bookName(ItemStack stack) {
        String name = BiblioRender.label(stack);
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored != null && !stored.isEmpty()) {
            Object2IntMap.Entry<Holder<Enchantment>> first = stored.entrySet().iterator().next();
            name = Enchantment.getFullname(first.getKey(), first.getIntValue()).getString();
        }
        return name;
    }

    /** angle 0..3 -> glRotatef 270, 180, 90, 0 about y. */
    public static float degrees(int angle) {
        return switch (angle) {
            case 0 -> 270.0F;
            case 1 -> 180.0F;
            case 2 -> 90.0F;
            default -> 0.0F;
        };
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.rotateDegrees(Axis.YP, degrees(state.angle));
        this.parts.submit(poseStack, collector, state.wood, state.books, state.lightCoords, OverlayTexture.NO_OVERLAY);
        if (state.text != null) {
            BiblioRender.text(poseStack, collector, this.font, state.text, 0.0, 0.0, 0.0, state.lightCoords);
        }
        poseStack.popPose();
    }
}
