package harkenscythe.client;

import harkenscythe.HSBlocks;
import harkenscythe.HSEntities;
import harkenscythe.HSItems;
import harkenscythe.HarkenScythe;
import harkenscythe.altar.Altars;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.HashMap;
import java.util.Map;

/** ClientProxyHarkenScythe: renderers of the souls, the skull block and the altars, the altar screen, the drink gauge, the dyed weave armour. */
public final class HSClient {
    /** getArmorTextureFile of the weave armour: the 1.5.2 colour of a single dye (and today's) picks a texture. */
    private static final Map<Integer, String> WEAVE_COLORS = new HashMap<>();

    static {
        int[] old = {1644825, 10040115, 6717235, 6704179, 15892389, 6724056, 14188339, 8339378, 5013401, 3361970, 11685080, 15066419, 8375321,
                5000268, 10066329, 16777215};
        DyeColor[] dyes = {DyeColor.BLACK, DyeColor.RED, DyeColor.GREEN, DyeColor.BROWN, DyeColor.PINK, DyeColor.LIGHT_BLUE, DyeColor.ORANGE,
                DyeColor.PURPLE, DyeColor.CYAN, DyeColor.BLUE, DyeColor.MAGENTA, DyeColor.YELLOW, DyeColor.LIME, DyeColor.GRAY, DyeColor.LIGHT_GRAY,
                DyeColor.WHITE};
        String[] names = {"black", "red", "green", "brown", "pink", "lightblue", "orange", "purple", "cyan", "blue", "magenta", "yellow", "lime",
                "gray", "silver", "white"};
        for (int i = 0; i < names.length; i++) {
            WEAVE_COLORS.put(old[i], names[i]);
            WEAVE_COLORS.putIfAbsent(dyes[i].getTextureDiffuseColor() & 0xFFFFFF, names[i]);
        }
    }

    private HSClient() {
    }

    private static IClientItemExtensions weave(String material) {
        return new IClientItemExtensions() {
            @Override
            public Identifier getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type, EquipmentClientInfo.Layer layer, Identifier fallback) {
                DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
                String name = color == null ? "default" : WEAVE_COLORS.getOrDefault(color.rgb() & 0xFFFFFF, "default");
                return fallback.withPath(path -> path.replace(material + "_default", material + "_" + name));
            }
        };
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, event -> {
            event.registerLayerDefinition(SoulRenderer.LAYER, SoulModel::createLayer);
            event.registerLayerDefinition(HSSkullRenderer.LAYER, HSSkullRenderer::createLayer);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerEntityRenderer(HSEntities.SOUL_COMMON.get(), context -> new SoulRenderer<>(context, "soulcommon"));
            event.registerEntityRenderer(HSEntities.SOUL_GRIEVING.get(), context -> new SoulRenderer<>(context, "soulgrieving"));
            event.registerEntityRenderer(HSEntities.SOUL_CULLED.get(), context -> new SoulRenderer<>(context, "soulculled"));
            event.registerEntityRenderer(HSEntities.SOUL_WRATHFUL.get(), context -> new SoulRenderer<>(context, "soulwrathful"));
            event.registerEntityRenderer(HSEntities.SOUL_LOST.get(), context -> new SoulRenderer<>(context, "soullost"));
            event.registerBlockEntityRenderer(HSBlocks.SKULL_ENTITY.get(), HSSkullRenderer::new);
            event.registerBlockEntityRenderer(HSBlocks.ALTAR_ENTITY.get(), AltarRenderer::new);
        });
        modBus.addListener(RegisterMenuScreensEvent.class, event -> event.register(Altars.ALTAR_MENU.get(), AltarScreen::new));
        AltarRecipesClient.register();
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAbove(VanillaGuiLayers.HOTBAR, HarkenScythe.id("drink_gauge"),
                DrinkHud::render));
        modBus.addListener(RegisterClientExtensionsEvent.class, event -> {
            event.registerItem(weave("soulweave"), HSItems.SOULWEAVE_HOOD.get(), HSItems.SOULWEAVE_ROBE.get(), HSItems.SOULWEAVE_PANTS.get(),
                    HSItems.SOULWEAVE_SHOES.get());
            event.registerItem(weave("bloodweave"), HSItems.BLOODWEAVE_HOOD.get(), HSItems.BLOODWEAVE_ROBE.get(), HSItems.BLOODWEAVE_PANTS.get(),
                    HSItems.BLOODWEAVE_SHOES.get());
        });
    }
}
