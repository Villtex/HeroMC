package tconstruct.client;

import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import tconstruct.TConstruct;
import tconstruct.TSmeltery;
import tconstruct.smeltery.TFluids;

/** Client side of part 25c: fluid models, the smeltery screen and the renderers of the liquids. */
public final class TSmelteryClient {
    private TSmelteryClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterFluidModelsEvent.class, event -> {
            for (TFluids.Liquid liquid : TFluids.ALL) {
                String tex = TFluids.NAMES[liquid.index()][1];
                event.register(new net.minecraft.client.renderer.block.FluidModel.Unbaked(new Material(TConstruct.id("block/liquid_" + tex)),
                        new Material(TConstruct.id("block/liquid_" + tex + "_flow")), null, (net.neoforged.neoforge.client.fluid.FluidTintSource) null),
                        liquid.source(), liquid.flowing());
            }
            event.register(new net.minecraft.client.renderer.block.FluidModel.Unbaked(new Material(TConstruct.id("block/slime_blue")),
                    new Material(TConstruct.id("block/slime_blue_flow")), null, (net.neoforged.neoforge.client.fluid.FluidTintSource) null),
                    tconstruct.TWorld.SLIME_SOURCE, tconstruct.TWorld.SLIME_FLOWING);
        });
        modBus.addListener(RegisterMenuScreensEvent.class, event -> event.register(TSmeltery.SMELTERY_MENU.get(), SmelteryScreen::new));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerBlockEntityRenderer(TSmeltery.TANK_ENTITY.get(), SmelteryRenderers.Tank::new);
            event.registerBlockEntityRenderer(TSmeltery.CASTING_ENTITY.get(), SmelteryRenderers.Casting::new);
            event.registerBlockEntityRenderer(TSmeltery.FAUCET_ENTITY.get(), SmelteryRenderers.Faucet::new);
            event.registerBlockEntityRenderer(TSmeltery.CHANNEL_ENTITY.get(), SmelteryRenderers.Channel::new);
            event.registerBlockEntityRenderer(TSmeltery.DRYING_RACK_ENTITY.get(), SmelteryRenderers.DryingRack::new);
            event.registerBlockEntityRenderer(TSmeltery.SMELTERY_ENTITY.get(), SmelteryRenderers.Smeltery::new);
            event.registerEntityRenderer(tconstruct.entity.TEntities.BLUE_SLIME.get(), BlueSlimeRenderer::new);
        });
    }
}
