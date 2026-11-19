package bibliocraft.display.client;

import bibliocraft.client.BiblioScreen;
import bibliocraft.client.BlockItemRenderers;
import bibliocraft.display.ArmorStandMenu;
import bibliocraft.display.Displays;
import bibliocraft.display.LabelBlock;
import bibliocraft.display.LabelMenu;
import bibliocraft.display.WeaponCaseBlock;
import bibliocraft.display.WeaponCaseMenu;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Renderers, item renderers (ItemWeaponCaseRenderer, ItemLabelRenderer, ItemArmorStandRenderer) and screens
 * (GuiWeaponCase "Glass Case", GuiLabel "Wooden Label", GuiArmorStand "Armor Stand") of {@link Displays}. The item
 * renderers draw the block as the original IItemRenderer did (lid slightly open, label plate, stand at 2/3 size), but
 * centred in the item cube instead of the odd per-type offsets of 1.5.2.
 */
public final class DisplaysClient {
    private DisplaysClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, e -> {
            e.registerLayerDefinition(DisplayModels.WEAPON_CASE, DisplayModels::weaponCase);
            e.registerLayerDefinition(DisplayModels.LABEL, DisplayModels::label);
            e.registerLayerDefinition(DisplayModels.ARMOR_STAND, DisplayModels::armorStand);
            e.registerLayerDefinition(DisplayModels.DUMMY_HEAD, () -> DisplayModels.dummy(0));
            e.registerLayerDefinition(DisplayModels.DUMMY_CHEST, () -> DisplayModels.dummy(1));
            e.registerLayerDefinition(DisplayModels.DUMMY_LEGS, () -> DisplayModels.dummy(2));
            e.registerLayerDefinition(DisplayModels.DUMMY_FEET, () -> DisplayModels.dummy(3));
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            e.registerBlockEntityRenderer(Displays.WEAPON_CASE_ENTITY.get(), WeaponCaseRenderer::new);
            e.registerBlockEntityRenderer(Displays.LABEL_ENTITY.get(), LabelRenderer::new);
            e.registerBlockEntityRenderer(Displays.ArmorStand.ENTITY.get(), ArmorStandRenderer::new);
        });
        // the armor stand draws armor with the vanilla equipment layers; their renderer is handed out here only
        modBus.addListener(EntityRenderersEvent.AddLayers.class, e -> ArmorStandRenderer.equipmentRenderer = e.getContext().getEquipmentRenderer());
        modBus.addListener(RegisterMenuScreensEvent.class, e -> {
            e.register(Displays.WEAPON_CASE_MENU.get(), (WeaponCaseMenu menu, Inventory inv, Component title) -> new BiblioScreen<>(menu, inv, title,
                    "weaponcase_gui", 176, 166, "Glass Case", true));
            e.register(Displays.LABEL_MENU.get(), (LabelMenu menu, Inventory inv, Component title) -> new BiblioScreen<>(menu, inv, title,
                    "woodlabel_gui", 176, 166, "Wooden Label", true));
            e.register(Displays.ArmorStand.MENU.get(), (ArmorStandMenu menu, Inventory inv, Component title) -> new BiblioScreen<>(menu, inv, title,
                    "armorstand_gui", 176, 166, "Armor Stand", true));
        });
        for (DeferredBlock<WeaponCaseBlock> block : Displays.WEAPON_CASES) {
            BlockItemRenderers.register(block, ctx -> {
                WeaponCaseRenderer.Parts parts = WeaponCaseRenderer.Parts.bake(ctx.entityModelSet()::bakeLayer);
                return (stack, poseStack, collector, light, overlay, foil) -> {
                    if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem bi) || !(bi.getBlock() instanceof WeaponCaseBlock b)) {
                        return;
                    }
                    // renderWeaponCase: glRotatef(180, x) then (-90, y), the lid open by 0.2
                    poseStack.pushPose();
                    poseStack.translate(0.5, 1.75, 0.5);
                    poseStack.rotateDegrees(Axis.XP, 180.0F);
                    poseStack.rotateDegrees(Axis.YP, -90.0F);
                    parts.submit(poseStack, collector, b.wood, b.cloth, parts.lidItem(), light);
                    poseStack.popPose();
                };
            });
        }
        for (DeferredBlock<LabelBlock> block : Displays.LABELS) {
            BlockItemRenderers.register(block, ctx -> {
                ModelPart model = ctx.entityModelSet().bakeLayer(DisplayModels.LABEL);
                return (stack, poseStack, collector, light, overlay, foil) -> {
                    int w = stack.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof LabelBlock b ? b.wood : 0;
                    // renderWoodLabel: glRotatef(180, x), scale 1/12; turned to show its face in the inventory view
                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.rotateDegrees(Axis.YP, 180.0F);
                    poseStack.rotateDegrees(Axis.XP, 180.0F);
                    poseStack.scale(16.0F / 12.0F, 16.0F / 12.0F, 16.0F / 12.0F);
                    collector.submitModelPart(model, poseStack, RenderTypes.entityCutout(LabelRenderer.texture(w)), light, OverlayTexture.NO_OVERLAY, null);
                    poseStack.popPose();
                };
            });
        }
        BlockItemRenderers.register(Displays.ArmorStand.BLOCK, ctx -> {
            ModelPart stand = ctx.entityModelSet().bakeLayer(DisplayModels.ARMOR_STAND);
            return (stack, poseStack, collector, light, overlay, foil) -> ArmorStandRenderer.submitItem(stand, poseStack, collector, light);
        });
    }
}
