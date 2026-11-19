package bibliocraft.bookcase.client;

import bibliocraft.bookcase.BookcaseMenu;
import bibliocraft.bookcase.Bookcases;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import bibliocraft.client.BiblioScreen;
import bibliocraft.client.BlockItemRenderers;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Renderer, item renderer (ItemBookcaseRenderer: the shelf full of books) and GUI (GuiBookcase) of the bookcase. */
public final class BookcasesClient {
    private static final boolean[] ALL_BOOKS = {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};

    private BookcasesClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, e -> {
            e.registerLayerDefinition(BookcaseModels.SHELF, BookcaseModels::shelf);
            e.registerLayerDefinition(BookcaseModels.BOOKS, BookcaseModels::books);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class,
                e -> e.registerBlockEntityRenderer(Bookcases.BOOKCASE_ENTITY.get(), BookcaseRenderer::new));
        modBus.addListener(RegisterMenuScreensEvent.class, e -> e.register(Bookcases.BOOKCASE_MENU.get(),
                (BookcaseMenu menu, Inventory inv, Component title) -> new BiblioScreen<>(menu, inv, title, "bookshelf_gui", 176, 166, "Bookcase", true)));
        for (int w = 0; w < 4; w++) {
            int wood = w;
            BlockItemRenderers.register(Bookcases.BOOKCASES.get(w), ctx -> {
                BookcaseRenderer.Parts parts = BookcaseRenderer.Parts.bake(ctx.entityModelSet()::bakeLayer);
                return (stack, poseStack, collector, light, overlay, foil) -> {
                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.rotateDegrees(Axis.YP, BookcaseRenderer.degrees(1));
                    parts.submit(poseStack, collector, wood, ALL_BOOKS, light, OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();
                };
            });
        }
    }
}
