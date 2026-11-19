package bibliocraft.printing.client;

import bibliocraft.client.BiblioScreen;
import bibliocraft.client.BlockItemRenderers;
import bibliocraft.printing.Desks;
import bibliocraft.printing.PrintPressMenu;
import bibliocraft.printing.Presses;
import bibliocraft.printing.Printing;
import bibliocraft.printing.TypesettingBlockEntity;
import bibliocraft.printing.WritingDeskBlockEntity;
import bibliocraft.printing.WritingDeskMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Renderers, item renderers, screens and the client hooks of {@link bibliocraft.printing.Printing}. */
public final class PrintingClient {
    private PrintingClient() {
    }

    public static void init(IEventBus modBus) {
        Printing.client = new Printing.ClientHooks() {
            @Override
            public void openBook(BlockPos pos, boolean editable) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null && mc.level.getBlockEntity(pos) instanceof WritingDeskBlockEntity desk) {
                    mc.gui.setScreen(new BookWritingScreen(desk, editable));
                }
            }

            @Override
            public void openTypesetting(BlockPos pos) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null && mc.level.getBlockEntity(pos) instanceof TypesettingBlockEntity tile) {
                    mc.gui.setScreen(new TypesettingScreen(tile));
                }
            }
        };
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, e -> {
            e.registerLayerDefinition(PrintingModels.WRITING_DESK, PrintingModels::writingDesk);
            e.registerLayerDefinition(PrintingModels.PRINT_PRESS, PrintingModels::printPress);
            e.registerLayerDefinition(PrintingModels.PRINT_PRESS_BOOKS, PrintingModels::printPressBooks);
            e.registerLayerDefinition(PrintingModels.INK_PLATE, PrintingModels::inkPlate);
            e.registerLayerDefinition(PrintingModels.TYPESET, PrintingModels::typeset);
            e.registerLayerDefinition(PrintingModels.TYPESET_BASE, PrintingModels::typesetBase);
            e.registerLayerDefinition(PrintingModels.TYPESET_BOOK, PrintingModels::typesetBook);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            e.registerBlockEntityRenderer(Desks.DESK_ENTITY.get(), WritingDeskRenderer::new);
            e.registerBlockEntityRenderer(Presses.TYPESETTING_ENTITY.get(), TypesettingRenderer::new);
            e.registerBlockEntityRenderer(Presses.PRESS_ENTITY.get(), PrintPressRenderer::new);
        });
        modBus.addListener(RegisterMenuScreensEvent.class, e -> {
            e.register(Desks.DESK_MENU.get(), (WritingDeskMenu menu, Inventory inv, Component title) -> new DeskScreen(menu, inv, title));
            e.register(Presses.PRESS_MENU.get(), (PrintPressMenu menu, Inventory inv, Component title) -> new PressScreen(menu, inv, title));
        });
        for (int w = 0; w < 4; w++) {
            int wood = w;
            BlockItemRenderers.register(Desks.DESKS.get(w), ctx -> {
                PrintRender.Parts parts = WritingDeskRenderer.bake(ctx.entityModelSet()::bakeLayer);
                return (stack, pose, collector, light, overlay, foil) -> WritingDeskRenderer.submitItem(parts, wood, pose, collector, light);
            });
        }
        BlockItemRenderers.register(Presses.TYPESETTING_TABLE, ctx -> {
            TypesettingRenderer.Models models = TypesettingRenderer.Models.bake(ctx.entityModelSet()::bakeLayer);
            return (stack, pose, collector, light, overlay, foil) -> TypesettingRenderer.submitItem(models, pose, collector, light);
        });
        BlockItemRenderers.register(Presses.PRINTING_PRESS, ctx -> {
            PrintPressRenderer.Models models = PrintPressRenderer.Models.bake(ctx.entityModelSet()::bakeLayer);
            return (stack, pose, collector, light, overlay, foil) -> PrintPressRenderer.submitItem(models, pose, collector, light);
        });
    }

    /** GuiWritingDesk: writingdeskGUI, "Wooden Desk" at (58, 6), "Inventory" at (64, ySize - 94). */
    static final class DeskScreen extends BiblioScreen<WritingDeskMenu> {
        DeskScreen(WritingDeskMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, "writingdesk_gui", 176, 166, null, false);
        }

        @Override
        protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
            graphics.text(this.font, "Wooden Desk", 58, 6, 0xFF404040, false);
            graphics.text(this.font, Component.translatable("container.inventory"), 64, this.imageHeight - 96 + 2, 0xFF404040, false);
        }
    }

    /** GuiPrintPress: no background (the original drew none), "Printing Press" and "Inventory". */
    static final class PressScreen extends AbstractContainerScreen<PrintPressMenu> {
        PressScreen(PrintPressMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, 176, 166);
        }

        @Override
        protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
            graphics.text(this.font, "Printing Press", 8, 6, 0xFF404040, false);
            graphics.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, 0xFF404040, false);
        }
    }
}
