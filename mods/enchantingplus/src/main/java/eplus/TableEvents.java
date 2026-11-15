package eplus;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eplus.network.ClientOptionPayload;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** BlockEnchantTable.onBlockActivated, GuiHandler, GuiPacket and EplusCommands on the server. */
public final class TableEvents {
    private TableEvents() {
    }

    /** Vanilla enchanting menus opened through this mod, with their table (for the "Mod" button). */
    private static final Map<AbstractContainerMenu, BlockPos> VANILLA_MENUS = new WeakHashMap<>();

    @SubscribeEvent
    public static void serverStarting(ServerAboutToStartEvent event) {
        EnchantingPlus.applyLight(EplusConfig.server().hasLight());
    }

    /** Right-click on the enchanting table: the custom GUI if the player and the server want it, else the vanilla one. */
    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (event.getHand() != InteractionHand.MAIN_HAND || !level.getBlockState(pos).is(Blocks.ENCHANTING_TABLE)) {
            return;
        }
        // like vanilla: sneaking with something in hand uses the item, not the block
        if (player.isSecondaryUseActive() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            openTable(serverPlayer, pos, useMod(serverPlayer));
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /** tag "eplus:useMod" && ConfigurationSettings.useMod */
    public static boolean useMod(ServerPlayer player) {
        return player.getData(EnchantingPlus.USE_MOD.get()) && EplusConfig.server().useMod();
    }

    public static void openTable(ServerPlayer player, BlockPos pos, boolean mod) {
        if (mod) {
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new EnchantTableMenu(id, inventory, pos),
                    Component.translatable("container.enchant")), buf -> buf.writeBlockPos(pos));
        } else {
            BlockState state = player.level().getBlockState(pos);
            MenuProvider provider = state.getMenuProvider(player.level(), pos);
            if (provider != null) {
                player.openMenu(provider);
                if (player.containerMenu instanceof EnchantmentMenu menu) {
                    VANILLA_MENUS.put(menu, pos.immutable());
                }
            }
        }
    }

    /** "Mod" button of the vanilla GUI (GuiPacket with id 0). */
    public static void switchToMod(ServerPlayer player) {
        BlockPos pos = VANILLA_MENUS.get(player.containerMenu);
        if (pos != null && player.containerMenu.stillValid(player) && useMod(player)) {
            openTable(player, pos, true);
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> eplus = Commands.literal("eplus");
        for (String option : new String[]{ClientOptionPayload.USE_MOD, ClientOptionPayload.HAS_PARTICLES}) {
            eplus.then(Commands.literal(option).then(Commands.argument("value", BoolArgumentType.bool())
                    .executes(context -> setOption(context, option))));
        }
        event.getDispatcher().register(eplus);
    }

    /** "/eplus useMod|hasParticles true|false": changes the player's client setting (and the server's copy of useMod). */
    private static int setOption(CommandContext<CommandSourceStack> context, String option) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        boolean value = BoolArgumentType.getBool(context, "value");
        if (option.equals(ClientOptionPayload.USE_MOD)) {
            player.setData(EnchantingPlus.USE_MOD.get(), value);
        }
        PacketDistributor.sendToPlayer(player, new ClientOptionPayload(option, value));
        context.getSource().sendSuccess(() -> Component.literal(String.format("%s: Config '%s' changed to %s.", "EPLUS", option, value)), false);
        return 1;
    }
}
