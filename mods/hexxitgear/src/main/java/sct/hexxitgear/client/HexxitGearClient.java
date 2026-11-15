package sct.hexxitgear.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import sct.hexxitgear.HexxitGear;
import sct.hexxitgear.core.SetBonuses;

import java.util.function.Supplier;

/** Client side: the helmets' own models (getArmorModel of ItemSkullHelmet, ItemScaleHelmet, ItemHoodHelmet). */
public final class HexxitGearClient {
    private HexxitGearClient() {
    }

    /** getArmorModel: the helmet model for the head slot, built on first use. */
    private static IClientItemExtensions helmet(Supplier<LayerDefinition> layer) {
        return new IClientItemExtensions() {
            private HumanoidModel<HumanoidRenderState> model;

            @Override
            public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
                if (layerType != EquipmentClientInfo.LayerType.HUMANOID) {
                    return original;
                }
                if (this.model == null) {
                    this.model = HelmetModels.bake(layer.get());
                }
                return this.model;
            }
        };
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterClientExtensionsEvent.class, event -> {
            event.registerItem(helmet(HelmetModels::skull), HexxitGear.TRIBAL_HELMET.get());
            event.registerItem(helmet(HelmetModels::scale), HexxitGear.SCALE_HELMET.get());
            event.registerItem(helmet(HelmetModels::hood), HexxitGear.THIEF_HELMET.get());
        });
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> SetBonuses.clearClient());
    }
}
