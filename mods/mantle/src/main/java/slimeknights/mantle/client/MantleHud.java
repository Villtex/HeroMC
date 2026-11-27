package slimeknights.mantle.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.GaugeBlock;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.compat.FluidHandlers;
import slimeknights.mantle.fluid.compat.IFluidHandler;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.mantle.util.OffhandCooldownTracker;
import slimeknights.mantle.util.RegistryHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Port (Teil 27c): HUD parts of the old {@code ClientEvents}: the offhand attack indicator and the tooltip of a targeted
 * gauge, as GUI layers above the vanilla crosshair and hotbar.
 */
public final class MantleHud {
  private MantleHud() {}

  private static final Identifier CROSSHAIR_BACKGROUND = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_background");
  private static final Identifier CROSSHAIR_PROGRESS = Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
  private static final Identifier HOTBAR_BACKGROUND = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_background");
  private static final Identifier HOTBAR_PROGRESS = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_progress");

  public static void register(IEventBus bus) {
    bus.addListener(RegisterGuiLayersEvent.class, event -> {
      event.registerAbove(VanillaGuiLayers.CROSSHAIR, Mantle.getResource("offhand_attack_crosshair"), (g, d) -> renderOffhandAttackIndicator(g, false));
      event.registerAbove(VanillaGuiLayers.HOTBAR, Mantle.getResource("offhand_attack_hotbar"), (g, d) -> renderOffhandAttackIndicator(g, true));
      event.registerAbove(VanillaGuiLayers.CROSSHAIR, Mantle.getResource("gauge_tooltip"), MantleHud::renderGaugeTooltip);
    });
  }

  /** Renders the attack cooldown of the offhand, opposite of the vanilla indicator */
  private static void renderOffhandAttackIndicator(GuiGraphicsExtractor graphics, boolean isHotbar) {
    // must have a player, not be in spectator, and have the indicator enabled
    Minecraft minecraft = Minecraft.getInstance();
    Options settings = minecraft.options;
    AttackIndicatorStatus indicator = settings.attackIndicator().get();
    if (minecraft.player == null || minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR || indicator == AttackIndicatorStatus.OFF) {
      return;
    }
    // fetch the current cooldown
    OffhandCooldownTracker tracker = OffhandCooldownTracker.get(minecraft.player);
    if (tracker == null) {
      return;
    }
    float cooldown = tracker.getCooldown();
    if (cooldown >= 1.0f) {
      return;
    }
    if (indicator == AttackIndicatorStatus.CROSSHAIR) {
      if (!isHotbar && minecraft.options.getCameraType().isFirstPerson()) {
        int scaledHeight = graphics.guiHeight();
        // integer division makes this a pain to line up, there might be a simplier version of this formula but I cannot think of one
        int y = (scaledHeight / 2) - 14 + (2 * (scaledHeight % 2));
        int x = graphics.guiWidth() / 2 - 8;
        int width = (int)(cooldown * 17.0F);
        graphics.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_BACKGROUND, x, y, 16, 4);
        graphics.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_PROGRESS, 16, 4, 0, 0, x, y, width, 4);
      }
    } else if (indicator == AttackIndicatorStatus.HOTBAR) {
      if (isHotbar && minecraft.getCameraEntity() == minecraft.player) {
        int centerWidth = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() - 20;
        int x;
        // opposite of the vanilla hand location, extra bit to offset past the offhand slot
        if (minecraft.player.getMainArm() == HumanoidArm.RIGHT) {
          x = centerWidth - 91 - 22 - 32;
        } else {
          x = centerWidth + 91 + 6 + 32;
        }
        int l1 = (int)(cooldown * 19.0F);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_BACKGROUND, x, y, 18, 18);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_PROGRESS, 18, 18, 0, 18 - l1, x, y + 18 - l1, 18, l1);
      }
    }
  }

  /** Renders the tooltip when targeting the gauge block */
  private static void renderGaugeTooltip(GuiGraphicsExtractor graphics, DeltaTracker delta) {
    // must not be in a screen, though chat is fine
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.gui.screen() != null && minecraft.gui.screen().getClass() != ChatScreen.class) {
      return;
    }
    // must have a hit result
    if (minecraft.level == null || minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
      return;
    }
    BlockHitResult blockHit = (BlockHitResult) minecraft.hitResult;
    BlockPos pos = blockHit.getBlockPos();
    // must be targeting a gauge
    BlockState targeted = minecraft.level.getBlockState(pos);
    if (!targeted.is(MantleTags.Blocks.GAUGES)) {
      return;
    }
    BlockPos containerPos;
    Direction side;
    if (targeted.is(MantleTags.Blocks.ATTACHED_GAUGES)) {
      side = targeted.getValue(BlockStateProperties.FACING);
      containerPos = pos.relative(side.getOpposite());
    } else {
      side = blockHit.getDirection();
      containerPos = pos;
    }
    BlockEntity gaugeContainer = minecraft.level.getBlockEntity(containerPos);
    // must have a block entity behind the gauge that is not blacklisted
    if (gaugeContainer == null || RegistryHelper.contains(BuiltInRegistries.BLOCK_ENTITY_TYPE, MantleTags.BlockEntities.GAUGE_BLACKLIST, gaugeContainer.getType())) {
      return;
    }
    // block entity must have a fluid handler
    IFluidHandler handler = FluidHandlers.getBlockHandler(minecraft.level, containerPos, side).orElse(null);
    if (handler == null || handler.getTanks() <= 0) {
      return;
    }
    // if the fluid is empty, just render the capacity
    FluidStack fluid = handler.getFluidInTank(0);
    List<Component> tooltip;
    if (fluid.isEmpty()) {
      tooltip = List.of(GaugeBlock.formatCapacity(handler.getTankCapacity(0)));
    } else if (RegistryHelper.contains(BuiltInRegistries.BLOCK_ENTITY_TYPE, MantleTags.BlockEntities.HIDES_GAUGE_AMOUNT, gaugeContainer.getType())) {
      // in the tag, don't show capacity
      Identifier id = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
      tooltip = new ArrayList<>(3);
      tooltip.add(fluid.getHoverName());
      FluidTooltipHandler.appendAdvanced(id, tooltip);
      tooltip.add(GaugeBlock.formatCapacity(handler.getTankCapacity(0)).withStyle(ChatFormatting.GRAY));
      tooltip.add(FluidTooltipHandler.formatModName(id));
    } else {
      // render full fluid tooltip
      tooltip = FluidTooltipHandler.getFluidTooltip(fluid);
    }
    graphics.setComponentTooltipForNextFrame(minecraft.font, tooltip, graphics.guiWidth() / 2, graphics.guiHeight() / 2);
  }
}
