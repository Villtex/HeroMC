package betterdungeons.client.human;

import betterdungeons.mob.BDHumans;
import betterdungeons.mob.ai.HumanGoals.EnumAiState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * friendlyMobs.GuiMobControl: four buttons (Follow, Ward, Hold position, Sit) at x 60, y 40..130, and the keys f, d,
 * h, s, p (Path has no button); sets the AI mode of the mob assigned to the controller in hand.
 */
public class MobControlScreen extends Screen {
    private final Player player;

    public MobControlScreen(Player player) {
        super(Component.literal("Mercenary controller"));
        this.player = player;
    }

    public static void open(Player player, ItemStack stack) {
        Minecraft.getInstance().gui.setScreen(new MobControlScreen(player));
    }

    @Override
    protected void init() {
        super.init();
        EnumAiState[] buttons = {EnumAiState.FOLLOW, EnumAiState.WARD, EnumAiState.HOLD, EnumAiState.SIT};
        for (int i = 0; i < buttons.length; i++) {
            EnumAiState state = buttons[i];
            this.addRenderableWidget(Button.builder(Component.literal(state.ainame), b -> this.changeMode(state.id))
                    .bounds(60, 40 + 30 * i, 200, 20).build());
        }
        if (this.entityId() == 0) {
            this.onClose();
        }
    }

    private int entityId() {
        ItemStack stack = this.player.getMainHandItem();
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? 0 : data.copyTag().getIntOr("EntityID", 0);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        int mode = switch (event.codepoint()) {
            case 'f' -> EnumAiState.FOLLOW.id;
            case 'd' -> EnumAiState.WARD.id;
            case 'h' -> EnumAiState.HOLD.id;
            case 's' -> EnumAiState.SIT.id;
            case 'p' -> EnumAiState.PATH.id;
            default -> -1;
        };
        if (mode != -1) {
            this.changeMode(mode);
            return true;
        }
        return super.charTyped(event);
    }

    private void changeMode(int mode) {
        int id = this.entityId();
        @Nullable Entity e = id != 0 ? this.player.level().getEntity(id) : null;
        ClientPacketDistributor.sendToServer(new BDHumans.AIModePayload(id, mode));
        if (e != null) {
            this.player.sendSystemMessage(Component.literal("Set AI mode \"").append(Component.literal(EnumAiState.getStateName(mode))
                    .withStyle(ChatFormatting.GOLD)).append("\" to ").append(e.getName().copy().withStyle(ChatFormatting.DARK_GREEN)));
        }
        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
