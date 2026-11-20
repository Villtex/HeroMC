package secretrooms;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * SecretRoomsMod 4.6.0 (1.5.2) for NeoForge: camo blocks that copy a neighbour's look, with the ids of the pack's
 * secretroomsmod.cfg. /srm-show (alias /show) makes camo blocks obvious for the player who runs it; the one-way key
 * (backslash) turns the facing of one-way glass placed by that player.
 */
@Mod(SecretRooms.MOD_ID)
public final class SecretRooms {
    public static final String MOD_ID = "secretroomsmod";
    /** ProxyCommon.awaySet: players whose one-way glass faces away from them. */
    private static final Set<UUID> AWAY = new HashSet<>();

    public SecretRooms(IEventBus modBus) {
        SRBlocks.register(modBus);
        SRItems.register(modBus);
        modBus.addListener(SRNetwork::register);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, SecretRooms::commands);
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, e -> AWAY.clear());
        registerOldIds();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            secretrooms.client.SRClient.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static boolean facesTowards(Player player) {
        return !AWAY.contains(player.getUUID());
    }

    static void toggleFacing(ServerPlayer player) {
        if (!AWAY.remove(player.getUUID())) {
            AWAY.add(player.getUUID());
        }
    }

    private static void commands(RegisterCommandsEvent event) {
        for (String name : new String[]{"srm-show", "show"}) {
            event.getDispatcher().register(Commands.literal(name).executes(ctx -> {
                PacketDistributor.sendToPlayer(ctx.getSource().getPlayerOrException(), new SRNetwork.ToggleShow());
                return 1;
            }));
        }
    }

    private static void registerOldIds() {
        OldBlockStates.register(2340, () -> SRBlocks.TORCH_LEVER.get().defaultBlockState());
        OldBlockStates.register(2338, () -> SRBlocks.ONE_WAY_GLASS.get().defaultBlockState());
        OldBlockStates.register(2325, () -> SRBlocks.CAMO_GATE.get().defaultBlockState());
        OldBlockStates.register(2324, () -> SRBlocks.CAMO_DUMMY.get().defaultBlockState());
        OldBlockStates.register(2335, () -> SRBlocks.SECRET_TRAPDOOR.get().defaultBlockState());
        OldBlockStates.registerMeta(2336 - 256, meta -> OldBlockStates.door(SRBlocks.SECRET_WOODEN_DOOR.get(), meta));
        OldBlockStates.registerMeta(2326, meta -> OldBlockStates.door(SRBlocks.SECRET_IRON_DOOR.get(), meta));
        OldBlockStates.register(2337, () -> SRBlocks.GHOST_BLOCK.get().defaultBlockState());
        OldBlockStates.register(2327, () -> SRBlocks.SECRET_LEVER.get().defaultBlockState());
        OldBlockStates.register(2333, () -> SRBlocks.SECRET_REDSTONE.get().defaultBlockState());
        OldBlockStates.registerMeta(2321, meta -> (meta & 7) == 1 ? SRBlocks.SECRET_WOOD_BUTTON.get().defaultBlockState()
                : SRBlocks.SECRET_STONE_BUTTON.get().defaultBlockState());
        OldBlockStates.register(2329, () -> SRBlocks.SECRET_PRESSURE_PLATE.get().defaultBlockState());
        OldBlockStates.register(2330, () -> SRBlocks.SECRET_PLAYER_PLATE.get().defaultBlockState());
        OldBlockStates.register(2332, () -> SRBlocks.SECRET_LIGHT_PLATE.get().defaultBlockState());
        OldBlockStates.register(2331, () -> SRBlocks.SECRET_HEAVY_PLATE.get().defaultBlockState());
        OldBlockStates.register(2334, () -> SRBlocks.SECRET_STAIRS.get().defaultBlockState());
        OldBlockStates.register(2322, () -> SRBlocks.SECRET_CHEST.get().defaultBlockState());
        OldBlockStates.register(2323, () -> SRBlocks.SECRET_TRAPPED_CHEST.get().defaultBlockState());
        OldBlockStates.register(2328, () -> SRBlocks.SECRET_LIGHT_DETECTOR.get().defaultBlockState());
        OldBlockStates.register(2339, () -> SRBlocks.SOLID_AIR.get().defaultBlockState());
        int[][] blockItems = {{2340}, {2338}, {2325}, {2335}, {2337}, {2327}, {2333}, {2329}, {2330}, {2332}, {2331}, {2334}, {2322}, {2323}, {2328}, {2339}};
        String[] names = {"torch_lever", "one_way_glass", "camo_gate", "secret_trapdoor", "ghost_block", "secret_lever", "secret_redstone",
                "secret_pressure_plate", "secret_player_plate", "secret_light_plate", "secret_heavy_plate", "secret_stairs", "secret_chest",
                "secret_trapped_chest", "secret_light_detector", "solid_air"};
        for (int i = 0; i < names.length; i++) {
            OldGameIds.registerItem(blockItems[i][0], MOD_ID + ":" + names[i]);
        }
        OldGameIds.registerItem(2321, 0, MOD_ID + ":secret_stone_button");
        OldGameIds.registerItem(2321, 1, MOD_ID + ":secret_wood_button");
        OldGameIds.registerItem(7502, MOD_ID + ":camo_paste");
        OldGameIds.registerItem(7501, MOD_ID + ":secret_iron_door");
        OldGameIds.registerItem(7503 + 256, MOD_ID + ":secret_wooden_door");
    }
}
