package customlan;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

/**
 * Custom LAN Mod 2.2 (CustomPortForge.jar): for the integrated server of a world opened to LAN. The settings come from
 * optionsCLP.txt and CLPermissions.txt in the game directory, which the client screens edit. Commands and settings
 * are only used by the integrated server (the original read them from client classes, which a dedicated server does
 * not have).
 */
@Mod(CustomLAN.MOD_ID)
public final class CustomLAN {
    public static final String MOD_ID = "customlan";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomLAN(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> LanCommands.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, e -> applyOptions(e.getServer()));
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, LanCommands::onDeath);
        customlan.client.CustomLanClient.init();
        LOGGER.info("[CustomLAN]: Custom LAN Mod by CptHunter loaded.");
    }

    /** onServerStarted / the "Done" button of the options screen: MOTD, online mode, PvP.
     * The player limit is a constant (8) of the integrated server in 26.3 and is not set any more. */
    public static void applyOptions(MinecraftServer server) {
        CLOptions.checkOptions();
        server.setMotd(CLOptions.serverMOTD);
        server.setUsesAuthentication(CLOptions.serverOnlineMode);
        server.getGameRules().set(GameRules.PVP, CLOptions.serverAllowPvP, server);
    }
}
