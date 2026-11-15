package eplus.network;

import eplus.EnchantException;
import eplus.EnchantTableMenu;
import eplus.EnchantingPlus;
import eplus.TableEvents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** PacketHandler of 1.5.2 as NeoForge payloads; every client request is checked again on the server. */
public final class EplusNetwork {
    private EplusNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(EnchantPayload.TYPE, EnchantPayload.CODEC, EplusNetwork::enchant);
        registrar.playToServer(OpenModTablePayload.TYPE, OpenModTablePayload.CODEC, EplusNetwork::openModTable);
        registrar.playToServer(UseModPayload.TYPE, UseModPayload.CODEC, EplusNetwork::useMod);
        // handled by the client (eplus.client.EplusClient)
        registrar.playToClient(ClientOptionPayload.TYPE, ClientOptionPayload.CODEC);
    }

    private static void enchant(EnchantPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof EnchantTableMenu menu && menu.stillValid(player)) {
            try {
                menu.enchant(player, payload.levels(), payload.cost());
            } catch (EnchantException e) {
                EnchantingPlus.LOGGER.info("Enchant failed because: {}", e.getMessage());
            }
            menu.broadcastChanges();
        }
    }

    private static void openModTable(OpenModTablePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            TableEvents.switchToMod(player);
        }
    }

    private static void useMod(UseModPayload payload, IPayloadContext context) {
        context.player().setData(EnchantingPlus.USE_MOD.get(), payload.useMod());
    }
}
