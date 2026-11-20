package tconstruct.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import tconstruct.TConstruct;
import tconstruct.menu.ToolStationMenu;
import tconstruct.network.TNetwork;
import tconstruct.tools.InfiTool;
import tconstruct.tools.ToolCoreItem;
import tconstruct.tools.ToolTips;
import tconstruct.tools.ToolType;

import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code ToolStationGui} / {@code ToolForgeGui}: the tool buttons on the left (the forge adds the big tools), the name
 * field at the top, and on the right the description of the chosen tool or the stats of the tool in the left slot.
 * A button moves the input slots to the layout of its tool.
 */
public class ToolStationScreen extends AbstractContainerScreen<ToolStationMenu> {
    private static final Identifier TEXTURE = TConstruct.id("textures/gui/toolstation.png");
    private static final Identifier ICONS = TConstruct.id("textures/gui/icons.png");
    private static final Identifier DESCRIPTION = TConstruct.id("textures/gui/description.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFF404040;
    private static Field slotX;
    private static Field slotY;

    private EditBox name;
    private ToolGuiElement selected = ToolGuiElement.TIER_ONE.get(0);
    private final List<ToolButton> buttons = new ArrayList<>();
    private int[][] positions;

    public ToolStationScreen(ToolStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private List<ToolGuiElement> elements() {
        List<ToolGuiElement> list = new ArrayList<>();
        list.add(ToolGuiElement.TIER_ONE.get(0));
        if (this.menu.forge) {
            list.addAll(ToolGuiElement.TIER_TWO);
        }
        list.addAll(ToolGuiElement.TIER_ONE.subList(1, ToolGuiElement.TIER_ONE.size()));
        return list;
    }

    @Override
    protected void init() {
        super.init();
        this.buttons.clear();
        List<ToolGuiElement> elements = this.elements();
        for (int i = 0; i < elements.size(); i++) {
            ToolButton button = new ToolButton(this.leftPos - 110 + 22 * (i % 5), this.topPos + 22 * (i / 5), elements.get(i));
            button.active = elements.get(i) != this.selected;
            this.buttons.add(this.addRenderableWidget(button));
        }
        this.name = new EditBox(this.font, this.leftPos + 70, this.topPos + 8, 100, 12, Component.empty());
        this.name.setBordered(false);
        this.name.setMaxLength(40);
        this.name.setTextColor(WHITE);
        this.name.setResponder(text -> ClientPacketDistributor.sendToServer(new TNetwork.ToolNamePayload(text.trim())));
        this.addRenderableWidget(this.name);
        this.setSlotType(this.selected.slotType());
    }

    private void select(ToolGuiElement element) {
        this.selected = element;
        for (ToolButton button : this.buttons) {
            button.active = button.element != element;
        }
        this.setSlotType(element.slotType());
    }

    /** setSlotType: the inputs move to the layout of the tool (only the client draws them there). */
    private void setSlotType(int type) {
        this.positions = ToolGuiElement.slotPositions(type, this.menu.forge);
        try {
            if (slotX == null) {
                slotX = Slot.class.getDeclaredField("x");
                slotY = Slot.class.getDeclaredField("y");
                slotX.setAccessible(true);
                slotY.setAccessible(true);
            }
            for (int i = 0; i < this.positions[0].length; i++) {
                Slot slot = this.menu.slots.get(i + 1);
                slotX.setInt(slot, this.positions[0][i] + 1);
                slotY.setInt(slot, this.positions[1][i] + 1);
            }
        } catch (ReflectiveOperationException e) {
            com.mojang.logging.LogUtils.getLogger().warn("cannot move the slots of the tool station", e);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE) {
            return super.keyPressed(event);
        }
        if (this.name.isFocused()) {
            return this.name.keyPressed(event) || true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        if (this.name.isFocused()) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 62, this.topPos, 0, this.imageHeight, 112, 22, 256, 256);
        }
        for (int i = 0; i < this.positions[0].length; i++) {
            int x = this.leftPos + this.positions[0][i];
            int y = this.topPos + this.positions[1][i];
            g.blit(RenderPipelines.GUI_TEXTURED, ICONS, x - 4, y - 4, 140, 212, 28, 28, 256, 256);
            if (this.menu.slots.get(i + 1).getItem().isEmpty()) {
                g.blit(RenderPipelines.GUI_TEXTURED, ICONS, x, y, 18 * this.selected.iconsX()[i], 18 * this.selected.iconsY()[i], 18, 18, 256, 256);
            }
        }
        g.blit(RenderPipelines.GUI_TEXTURED, DESCRIPTION, this.leftPos + this.imageWidth, this.topPos, 0, 0, 126, this.imageHeight + 30, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(this.font, this.title, 6, 8, GREY, false);
        g.text(this.font, Component.translatable("container.inventory"), 8, this.imageHeight - 96 + 2, GREY, false);
        ItemStack stack = this.menu.slots.get(0).getItem();
        if (stack.getItem() instanceof ToolCoreItem tool && InfiTool.of(stack) instanceof InfiTool t) {
            g.centeredText(this.font, Component.translatable(tool.type.langKey()).withStyle(s -> s.withUnderlined(true)), this.imageWidth + 63, 8, WHITE);
            this.drawToolStats(g, tool.type, t);
        } else {
            String key = "gui.tconstruct.tool." + this.selected.key();
            g.centeredText(this.font, Component.translatable(key + ".title").withStyle(s -> s.withUnderlined(true)), this.imageWidth + 63, 8, WHITE);
            g.textWithWordWrap(this.font, Component.translatable(key + ".body"), this.imageWidth + 8, 24, 115, WHITE, false);
        }
    }

    private static String hearts(int attack) {
        return attack % 2 == 0 ? String.valueOf(attack / 2) : String.valueOf(attack / 2.0F);
    }

    private static Component heartLine(String key, int attack) {
        return Component.translatable(key, Component.translatable(attack == 2 ? "gui.tconstruct.heart" : "gui.tconstruct.hearts", hearts(attack)));
    }

    /** drawModularToolStats. */
    private void drawToolStats(GuiGraphicsExtractor g, ToolType type, InfiTool t) {
        int x = this.imageWidth + 8;
        int base = 24;
        int offset = 0;
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.DOWN);
        int durability = t.damage;
        int maxDur = t.totalDurability;
        if (maxDur > 0) {
            if (maxDur >= 10000) {
                g.text(this.font, Component.translatable("gui.tconstruct.durability_label"), x, base, WHITE, false);
                g.text(this.font, "- " + (maxDur - durability) + "/" + maxDur, x, base + ++offset * 10, WHITE, false);
            } else {
                g.text(this.font, Component.translatable("gui.tconstruct.durability", (maxDur - durability) + "/" + maxDur), x, base, WHITE, false);
            }
            offset++;
        }
        float stonebound = t.shoddy;
        if (type.is("weapon")) {
            float stoneboundDamage = (float) Math.log(durability / 72.0F + 1.0F) * -2.0F * stonebound;
            int attack = Math.max(1, (int) (t.attack + stoneboundDamage));
            g.text(this.font, heartLine("gui.tconstruct.attack", attack), x, base + offset * 10, WHITE, false);
            offset++;
            if (stoneboundDamage != 0.0F) {
                g.text(this.font, Component.translatable(stoneboundDamage > 0 ? "gui.tconstruct.bonus" : "gui.tconstruct.loss",
                        Component.translatable(stoneboundDamage == 2.0F ? "gui.tconstruct.heart" : "gui.tconstruct.hearts",
                                String.valueOf((int) stoneboundDamage / 2))), x, base + offset * 10, WHITE, false);
                offset++;
            }
            offset++;
        }
        if (type.is("bow")) {
            float flightSpeed = t.getFloat("FlightSpeed");
            float trueDraw = t.getInt("DrawSpeed") / 20.0F * flightSpeed;
            g.text(this.font, Component.translatable("gui.tconstruct.draw_speed", df.format(trueDraw)), x, base + offset * 10, WHITE, false);
            g.text(this.font, Component.translatable("gui.tconstruct.arrow_speed", df.format(flightSpeed)), x, base + ++offset * 10, WHITE, false);
            offset += 2;
        }
        if (type.is("ammo")) {
            int attack = t.attack;
            g.text(this.font, Component.translatable("gui.tconstruct.base_attack"), x, base + offset * 10, WHITE, false);
            offset++;
            g.text(this.font, "- " + Component.translatable(attack == 2 ? "gui.tconstruct.heart" : "gui.tconstruct.hearts", hearts(attack)).getString(),
                    x, base + offset * 10, WHITE, false);
            offset++;
            g.text(this.font, Component.translatable("gui.tconstruct.shortbow_attack"), x, base + offset * 10, WHITE, false);
            offset++;
            g.text(this.font, Component.translatable("gui.tconstruct.hearts", df.format(attack / 2.0F) + "-" + df.format(attack)),
                    x, base + offset * 10, WHITE, false);
            offset++;
            g.text(this.font, Component.translatable("gui.tconstruct.weight", df.format(t.getFloat("Mass"))), x, base + ++offset * 10, WHITE, false);
            offset++;
            g.text(this.font, Component.translatable("gui.tconstruct.accuracy", df.format(t.getFloat("Accuracy") - 4.0F)), x, base + offset * 10, WHITE,
                    false);
            offset += 2;
        }
        if (type.is("dualharvest")) {
            float stoneboundSpeed = (float) Math.log(durability / 90.0F + 1.0F) * 2.0F * stonebound;
            // 1.5.2 showed the first speed twice
            float trueSpeed = t.miningSpeed / 100.0F + stoneboundSpeed;
            g.text(this.font, Component.translatable("gui.tconstruct.mining_speeds"), x, base + offset * 10, WHITE, false);
            g.text(this.font, "- " + df.format(trueSpeed) + ", " + df.format(trueSpeed), x, base + ++offset * 10, WHITE, false);
            offset++;
            if (stoneboundSpeed != 0.0F) {
                g.text(this.font, Component.translatable(stoneboundSpeed > 0 ? "gui.tconstruct.bonus" : "gui.tconstruct.loss", df.format(stoneboundSpeed)),
                        x, base + offset * 10, WHITE, false);
                offset++;
            }
            g.text(this.font, Component.translatable("gui.tconstruct.harvest_levels"), x, base + ++offset * 10, WHITE, false);
            g.text(this.font, Component.literal("- ").append(level(t.harvestLevel)).append(", ").append(level(t.harvestLevel2)), x,
                    base + ++offset * 10, WHITE, false);
            offset += 2;
        } else if (type.is("harvest")) {
            int[] heads = new int[1];
            float trueSpeed = t.summedSpeed(heads) / (heads[0] * 100.0F);
            float stoneboundSpeed = (float) Math.log(durability / 90.0F + 1.0F) * 2.0F * stonebound;
            trueSpeed = Math.max(0.0F, trueSpeed + stoneboundSpeed);
            g.text(this.font, Component.translatable("gui.tconstruct.mining_speed", df.format(trueSpeed)), x, base + offset * 10, WHITE, false);
            offset++;
            if (stoneboundSpeed != 0.0F) {
                g.text(this.font, Component.translatable(stoneboundSpeed > 0 ? "gui.tconstruct.bonus" : "gui.tconstruct.loss", df.format(stoneboundSpeed)),
                        x, base + offset * 10, WHITE, false);
                offset++;
            }
            g.text(this.font, Component.translatable("gui.tconstruct.mining_level", level(t.harvestLevel)), x, base + offset * 10, WHITE, false);
            offset += 2;
        } else if (type.is("utility")) {
            g.text(this.font, Component.translatable("gui.tconstruct.usage_speed", String.valueOf(t.miningSpeed / 100.0F)), x, base + offset * 10, WHITE,
                    false);
            offset += 2;
        }
        if (t.modifiers > 0) {
            g.text(this.font, Component.translatable("gui.tconstruct.modifiers_remaining", t.modifiers), x, base + offset * 10, WHITE, false);
            offset++;
        }
        if (!t.tips.isEmpty()) {
            g.text(this.font, Component.translatable("gui.tconstruct.modifiers"), x, base + offset * 10, WHITE, false);
        }
        for (int i = 0; i < t.tips.size(); i++) {
            g.text(this.font, Component.literal("- ").append(ToolTips.translate(t.tips.get(i)[1])), x, base + (offset + i + 1) * 10, WHITE, false);
        }
    }

    /** getHarvestLevelName. */
    public static Component level(int level) {
        return level >= 0 && level <= 5 ? Component.translatable("gui.tconstruct.level." + level) : Component.literal(String.valueOf(level));
    }

    /** GuiButtonTool: the button frame of icons.png (by state) and the tool's icon. */
    private class ToolButton extends AbstractButton {
        final ToolGuiElement element;

        ToolButton(int x, int y, ToolGuiElement element) {
            super(x, y, 18, 18, Component.translatable("gui.tconstruct.tool." + element.key() + ".title"));
            this.element = element;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            ToolStationScreen.this.select(this.element);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
            int state = !this.active ? 0 : this.isHoveredOrFocused() ? 2 : 1;
            g.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.getX(), this.getY(), 144 + 36 * state, 216, 18, 18, 256, 256);
            g.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.getX(), this.getY(), this.element.buttonX() * 18, this.element.buttonY() * 18, 18, 18,
                    256, 256);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
