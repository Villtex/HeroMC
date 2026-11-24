package eplus.client;

import eplus.EnchantingPlus;
import eplus.EplusConfig;
import eplus.network.ClientOptionPayload;
import eplus.network.OpenModTablePayload;
import eplus.network.UseModPayload;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** ClientProxy, GuiVanillaTable, ConnectionHandler (client half) and the portal particles of BlockEnchantTable. */
public final class EplusClient {
    private EplusClient() {
    }

    private static final RandomSource RANDOM = RandomSource.createThreadSafe();

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(EnchantingPlus.MENU.get(), EnchantTableScreen::new));
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(ClientOptionPayload.TYPE, EplusClient::handleOption));
        NeoForge.EVENT_BUS.addListener(EplusClient::loggingIn);
        NeoForge.EVENT_BUS.addListener(EplusClient::initScreen);
        NeoForge.EVENT_BUS.addListener(EplusClient::renderScreen);
        NeoForge.EVENT_BUS.addListener(EplusClient::tick);
    }

    /** clientLoggedIn: tell the server whether this client wants the custom table; the table light follows the server. */
    private static void loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientPacketDistributor.sendToServer(new UseModPayload(EplusConfig.clientUseMod()));
        EnchantingPlus.applyLight(EplusConfig.server().hasLight());
    }

    private static void handleOption(ClientOptionPayload payload, IPayloadContext context) {
        if (payload.option().equals(ClientOptionPayload.USE_MOD)) {
            EplusConfig.CLIENT_USE_MOD.set(payload.value());
        } else if (payload.option().equals(ClientOptionPayload.HAS_PARTICLES)) {
            EplusConfig.HAS_PARTICLES.set(payload.value());
        }
        EplusConfig.CLIENT_SPEC.save();
    }

    /** GuiVanillaTable.initGui: a "Mod" button right of the vanilla GUI. */
    private static void initScreen(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof EnchantmentScreen screen && EplusConfig.clientUseMod()) {
            Font font = Minecraft.getInstance().font;
            event.addListener(Button.builder(Component.literal("Mod"), b -> ClientPacketDistributor.sendToServer(OpenModTablePayload.INSTANCE))
                    .bounds(screen.getLeftPos() + screen.getImageWidth() + 10, screen.getTopPos() + 5, font.width("Mod") + 10, 20).build());
        }
    }

    /** GuiVanillaTable.drawScreen: the player's level in a box left of the GUI. */
    private static void renderScreen(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof EnchantmentScreen screen && Minecraft.getInstance().player != null) {
            Font font = Minecraft.getInstance().font;
            String text = String.format("%s: %s", Component.translatable("eplus.player.level").getString(),
                    Minecraft.getInstance().player.experienceLevel);
            EnchantTableScreen.drawHoveringText(event.getGuiGraphics(), font, List.of(Component.literal(text).getVisualOrderText()),
                    screen.getLeftPos() - 20 - font.width(text), screen.getTopPos() + font.lineHeight + 8, screen.width, screen.height);
        }
    }

    /**
     * BlockEnchantTable.randomDisplayTick (portal particles on top of the vanilla ones). The vanilla block has no hook
     * for it, so the client samples blocks around the player the way ClientLevel.animateTick does.
     */
    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || mc.isPaused() || !EplusConfig.hasParticles()) {
            return;
        }
        BlockPos center = mc.player.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 667; i++) {
            sample(level, center, 16, pos);
            sample(level, center, 32, pos);
        }
    }

    private static void sample(ClientLevel level, BlockPos center, int range, BlockPos.MutableBlockPos pos) {
        pos.set(center.getX() + RANDOM.nextInt(range) - RANDOM.nextInt(range), center.getY() + RANDOM.nextInt(range) - RANDOM.nextInt(range),
                center.getZ() + RANDOM.nextInt(range) - RANDOM.nextInt(range));
        if (level.getBlockState(pos).is(Blocks.ENCHANTING_TABLE)) {
            portalParticles(level, pos.immutable(), RANDOM);
        }
    }

    private static void portalParticles(ClientLevel level, BlockPos pos, RandomSource random) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int i = 0; i < 4; i++) {
            double px = x + random.nextFloat();
            double py = y + random.nextFloat();
            double pz = z + random.nextFloat();
            int side = random.nextInt(2) * 2 - 1;
            double vx = (random.nextFloat() - 0.5) * 0.5;
            double vy = (random.nextFloat() - 0.5) * 0.5;
            double vz = (random.nextFloat() - 0.5) * 0.5;
            if (!level.getBlockState(pos.west()).is(Blocks.ENCHANTING_TABLE) && !level.getBlockState(pos.east()).is(Blocks.ENCHANTING_TABLE)) {
                px = x + 0.5 + 0.25 * side;
                vx = random.nextFloat() * 2.0F * side;
            } else {
                pz = z + 0.5 + 0.25 * side;
                vz = random.nextFloat() * 2.0F * side;
            }
            level.addParticle(ParticleTypes.PORTAL, px, py, pz, vx, vy, vz);
        }
    }
}
