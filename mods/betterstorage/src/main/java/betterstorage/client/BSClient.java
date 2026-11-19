package betterstorage.client;

import betterstorage.BS;
import betterstorage.BSUtil;
import betterstorage.BetterStorage;
import betterstorage.backpack.BackpackData;
import betterstorage.backpack.Backpacks;
import betterstorage.menu.BSMenu;
import betterstorage.menu.CrateMenu;
import betterstorage.menu.KeyringMenu;
import betterstorage.network.TeleportPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** ClientProxy: renderers, item renderers, the worn backpack, screens and the client half of the packets. */
public final class BSClient {
    private BSClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(TeleportPayload.TYPE, BSClient::handleTeleport));
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, e -> {
            e.registerLayerDefinition(BSModels.CHEST, () -> BSModels.chest(false));
            e.registerLayerDefinition(BSModels.LARGE_CHEST, () -> BSModels.chest(true));
            e.registerLayerDefinition(BSModels.ARMOR_STAND, BSModels::armorStand);
            e.registerLayerDefinition(BSModels.BACKPACK, BSModels::backpack);
            e.registerLayerDefinition(BSModels.BACKPACK_ARMOR, BackpackArmorModel::create);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, e -> {
            e.registerBlockEntityRenderer(BS.CHEST_ENTITY.get(), ReinforcedChestRenderer::new);
            e.registerBlockEntityRenderer(BS.LOCKER_ENTITY.get(), LockerRenderer::new);
            e.registerBlockEntityRenderer(BS.ARMOR_STAND_ENTITY.get(), ArmorStandRenderer::new);
            e.registerBlockEntityRenderer(BS.BACKPACK_ENTITY.get(), BackpackRenderer::new);
            e.registerEntityRenderer(BS.FRIENDERMAN.get(), FriendermanRenderer::new);
        });
        modBus.addListener(EntityRenderersEvent.AddLayers.class, e -> ArmorStandRenderer.equipmentRenderer = e.getContext().getEquipmentRenderer());
        modBus.addListener(RegisterSpecialModelRendererEvent.class, e -> e.register(BetterStorage.id("block"), BlockItemRenderer.MAP_CODEC));
        modBus.addListener(RegisterMenuScreensEvent.class, e -> {
            e.register(BS.MENU.get(), (BSMenu menu, Inventory inv, Component title) -> new BSScreen<>(menu, inv, title));
            e.register(BS.KEYRING_MENU.get(), (KeyringMenu menu, Inventory inv, Component title) -> new BSScreen<>(menu, inv, title));
            e.register(BS.CRATE_MENU.get(), CrateScreen::new);
        });
        modBus.addListener(RegisterClientExtensionsEvent.class, e -> e.registerItem(new IClientItemExtensions() {
            private BackpackArmorModel model;

            @Override
            public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
                if (this.model == null) {
                    this.model = new BackpackArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(BSModels.BACKPACK_ARMOR));
                }
                return this.model;
            }
        }, BS.BACKPACK.get(), BS.ENDER_BACKPACK.get()));
        modBus.addListener(RegisterRenderStateModifiersEvent.class, e -> e.registerEntityModifier(new com.google.common.reflect.TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {
                },
                (LivingEntity entity, LivingEntityRenderState state) -> {
                    if (entity.hasData(BS.BACKPACK_DATA.get())) {
                        BackpackData data = Backpacks.data(entity);
                        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
                        state.setRenderData(BackpackArmorModel.LID, data.prevLidAngle + (data.lidAngle - data.prevLidAngle) * partialTicks);
                    }
                }));
        NeoForge.EVENT_BUS.addListener(BSClient::onEntityTick);
    }

    /** The lid of a worn backpack moves on the client too (the original did it on both sides). */
    private static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || !entity.level().isClientSide() || !entity.hasData(BS.BACKPACK_STATE.get())
                || Backpacks.getBackpack(entity).isEmpty()) {
            return;
        }
        BackpackData data = Backpacks.data(entity);
        data.prevLidAngle = data.lidAngle;
        data.lidAngle = Backpacks.isOpen(entity) ? Math.min(1.0F, data.lidAngle + 0.2F) : Math.max(0.0F, data.lidAngle - 0.2F);
    }

    /** handleBackpackTeleport: 128 portal particles along the way. */
    private static void handleTeleport(TeleportPayload p, IPayloadContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        int amount = 128;
        for (int i = 0; i < amount; i++) {
            double a = (double) i / (amount - 1);
            double vX = BSUtil.getDouble(-0.3, 0.3);
            double vY = BSUtil.getDouble(-0.3, 0.3);
            double vZ = BSUtil.getDouble(-0.3, 0.3);
            double pX = p.sourceX() + (p.x() - p.sourceX()) * a + BSUtil.getDouble(0.3, 0.7);
            double pY = p.sourceY() + (p.y() - p.sourceY()) * a + BSUtil.getDouble(-0.5, 0.0) + a / 2.0;
            double pZ = p.sourceZ() + (p.z() - p.sourceZ()) * a + BSUtil.getDouble(0.3, 0.7);
            level.addParticle(ParticleTypes.PORTAL, pX, pY, pZ, vX, vY, vZ);
        }
    }
}
