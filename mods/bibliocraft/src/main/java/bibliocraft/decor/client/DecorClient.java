package bibliocraft.decor.client;

import bibliocraft.client.BiblioScreen;
import bibliocraft.client.BlockItemRenderers;
import bibliocraft.decor.Decor;
import bibliocraft.decor.TableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Renderers, item renderers, GUI and candle flame of {@link Decor}. The IItemRenderers translated and scaled the
 * models per render type; here each item is drawn in the unit cube like the block (table alone on its middle leg,
 * lantern and lamp standing, style 0) with the vanilla block display transforms, the lantern 1.4 times as big
 * (the original drew it 1.7 times, the lamp 1.2 times, both larger than a block).
 */
public final class DecorClient {
    private static final boolean[] MONOLEG = {false, false, false, false, true};
    private static final boolean[] ROUND = {true, true, true, true};

    private DecorClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            e.registerBlockEntityRenderer(Decor.TABLE_ENTITY.get(), TableRenderer::new);
            e.registerBlockEntityRenderer(Decor.LANTERN_ENTITY.get(), LightRenderer.provider(true));
            e.registerBlockEntityRenderer(Decor.LAMP_ENTITY.get(), LightRenderer.provider(false));
        });
        // GuiTable: weaponcaseGUI.png, "Wooden Table" and "Inventory"
        modBus.addListener(RegisterMenuScreensEvent.class, e -> e.register(Decor.TABLE_MENU.get(),
                (TableMenu menu, Inventory inv, Component title) -> new BiblioScreen<>(menu, inv, title, "weaponcase_gui", 176, 166, "Wooden Table", true)));
        modBus.addListener(RegisterParticleProvidersEvent.class, e -> e.registerSpriteSet(Decor.CANDLE_FLAME.get(), CandleFlameParticle.Provider::new));
        for (int w = 0; w < 4; w++) {
            int wood = w;
            BlockItemRenderers.register(Decor.TABLES.get(w), ctx -> {
                ObjModel model = ObjModel.load(TableRenderer.MODEL);
                return (stack, poseStack, collector, light, overlay, foil) -> {
                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.0, 0.5);
                    TableRenderer.submitTable(model, poseStack, collector, wood, MONOLEG, ROUND, light);
                    poseStack.popPose();
                };
            });
        }
        for (int c = 0; c < 16; c++) {
            int color = c;
            BlockItemRenderers.register(Decor.LANTERNS.get(c), ctx -> light(true, color, 1.4F));
            BlockItemRenderers.register(Decor.LAMPS.get(c), ctx -> light(false, color, 1.0F));
        }
    }

    /** The model as placed with angle 2 (no turn, moved one block along z), scaled about the bottom middle. */
    private static BlockItemRenderers.ItemRenderer light(boolean lantern, int color, float scale) {
        LightRenderer renderer = new LightRenderer(lantern);
        return (stack, poseStack, collector, light, overlay, foil) -> {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5, 0.0, 0.5);
            renderer.submitModel(poseStack, collector, 0, color, light);
            poseStack.popPose();
        };
    }
}
