package bibliocraft.shelf.client;

import bibliocraft.client.BiblioScreen;
import bibliocraft.client.BlockItemRenderers;
import bibliocraft.shelf.PotionShelfMenu;
import bibliocraft.shelf.ShelfMenu;
import bibliocraft.shelf.Shelves;
import bibliocraft.shelf.ToolRackMenu;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Renderers, item renderers and screens of {@link bibliocraft.shelf.Shelves}. The item renderers (ItemPotionShelfRenderer,
 * ItemGenericShelfRenderer with the top board, ItemWeaponRackRenderer) draw the model as the placed block of angle 1
 * (like the bookcase item); the per-view translations of the original are the display transforms of the item model.
 * GuiPotionShelf, GuiGenericShelf, GuiWeaponRack: 176 x 166 with title and "Inventory".
 */
public final class ShelvesClient {
    private ShelvesClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, e -> {
            e.registerLayerDefinition(ShelfModels.POTION_SHELF, ShelfModels::potionShelf);
            e.registerLayerDefinition(ShelfModels.SHELF, ShelfModels::shelf);
            e.registerLayerDefinition(ShelfModels.TOOL_RACK, ShelfModels::toolRack);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            e.registerBlockEntityRenderer(Shelves.POTION_SHELF_ENTITY.get(), PotionShelfRenderer::new);
            e.registerBlockEntityRenderer(Shelves.SHELF_ENTITY.get(), GenericShelfRenderer::new);
            e.registerBlockEntityRenderer(Shelves.TOOL_RACK_ENTITY.get(), ToolRackRenderer::new);
        });
        modBus.addListener(RegisterMenuScreensEvent.class, e -> {
            e.register(Shelves.POTION_SHELF_MENU.get(),
                    (PotionShelfMenu menu, Inventory inv, Component title) -> new BiblioScreen<>(menu, inv, title, "potionshelf_gui", 176, 166, "Potion Shelf", true));
            e.register(Shelves.SHELF_MENU.get(),
                    (ShelfMenu menu, Inventory inv, Component title) -> new BiblioScreen<>(menu, inv, title, "genericshelf_gui", 176, 166, "Shelf", true));
            e.register(Shelves.TOOL_RACK_MENU.get(),
                    (ToolRackMenu menu, Inventory inv, Component title) -> new BiblioScreen<>(menu, inv, title, "toolrack_gui", 176, 166, "Tool Rack", true));
        });
        for (int w = 0; w < 4; w++) {
            int wood = w;
            BlockItemRenderers.register(Shelves.POTION_SHELVES.get(w), ctx -> {
                ModelPart model = ctx.entityModelSet().bakeLayer(ShelfModels.POTION_SHELF);
                return (stack, poseStack, collector, light, overlay, foil) -> {
                    poseStack.pushPose();
                    PotionShelfRenderer.submitShelf(model, poseStack, collector, wood, 1, light);
                    poseStack.popPose();
                };
            });
            BlockItemRenderers.register(Shelves.SHELVES.get(w), ctx -> {
                GenericShelfRenderer.Parts parts = GenericShelfRenderer.Parts.of(ctx.entityModelSet().bakeLayer(ShelfModels.SHELF));
                return (stack, poseStack, collector, light, overlay, foil) -> GenericShelfRenderer.submitItem(parts, poseStack, collector, wood, light);
            });
            BlockItemRenderers.register(Shelves.TOOL_RACKS.get(w), ctx -> {
                ModelPart model = ctx.entityModelSet().bakeLayer(ShelfModels.TOOL_RACK);
                return (stack, poseStack, collector, light, overlay, foil) -> {
                    poseStack.pushPose();
                    ToolRackRenderer.submitRack(model, poseStack, collector, wood, 1, light);
                    poseStack.popPose();
                };
            });
        }
    }
}
