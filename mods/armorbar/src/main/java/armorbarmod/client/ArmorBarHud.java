package armorbarmod.client;

import armorbarmod.ArmorBarMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

/** MOSIDisplayTicker with its DisplayUnits: each unit updates every few ticks and draws icon, bar and number. */
public final class ArmorBarHud {
    private static final Identifier COUNTDOWN = Identifier.fromNamespaceAndPath(ArmorBarMod.MOD_ID, "textures/gui/countdown.png");
    private static final List<State> STATES = ArmorBarMod.UNITS.stream().map(State::new).toList();
    private static int ticks;

    /** What a unit remembers between updates (prevTrackedValue, opacity, the tracked value). */
    private static final class State {
        final ArmorBarMod.Unit unit;
        int value;
        int previous;
        float opacity = 1.0F;

        State(ArmorBarMod.Unit unit) {
            this.unit = unit;
        }
    }

    private ArmorBarHud() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAbove(VanillaGuiLayers.HOTBAR,
                Identifier.fromNamespaceAndPath(ArmorBarMod.MOD_ID, "armor_bar"), ArmorBarHud::render));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
    }

    private static EquipmentSlot slot(ArmorBarMod.Kind kind) {
        return switch (kind) {
            case FEET -> EquipmentSlot.FEET;
            case LEGS -> EquipmentSlot.LEGS;
            case CHEST -> EquipmentSlot.CHEST;
            default -> EquipmentSlot.HEAD;
        };
    }

    private static Item tracked(ArmorBarMod.Unit unit) {
        Identifier id = Identifier.tryParse(unit.item.get());
        return id == null ? Items.AIR : BuiltInRegistries.ITEM.getValue(id);
    }

    /** The first matching stack of the main inventory, or a stand-in (fully used up when tracking durability). */
    private static ItemStack find(LocalPlayer player, ArmorBarMod.Unit unit) {
        Item item = tracked(unit);
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(item)) {
                return stack;
            }
        }
        ItemStack stand = new ItemStack(item);
        if (unit.kind == ArmorBarMod.Kind.DURABILITY && stand.isDamageableItem()) {
            stand.setDamageValue(stand.getMaxDamage());
        }
        return stand;
    }

    private static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        for (State state : STATES) {
            ArmorBarMod.Unit unit = state.unit;
            if (ticks % unit.updateFrequency.get() == 0) {
                switch (unit.kind) {
                    case AMOUNT -> {
                        Item item = tracked(unit);
                        int amount = 0;
                        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                            if (stack.is(item)) {
                                amount += stack.getCount();
                            }
                        }
                        state.value = amount;
                    }
                    case DURABILITY -> {
                        ItemStack stack = find(player, unit);
                        state.value = stack.getMaxDamage() - stack.getDamageValue();
                    }
                    default -> {
                        ItemStack stack = player.getItemBySlot(slot(unit.kind));
                        if (!stack.isEmpty()) {
                            state.value = stack.getMaxDamage() - stack.getDamageValue();
                        }
                    }
                }
            }
            // fading: full opacity whenever the value changes, then fadeRate ticks to vanish
            if (state.previous != state.value) {
                state.opacity = 1.0F;
                state.previous = state.value;
            } else {
                int fade = unit.fadeRate.get();
                state.opacity = Math.max(0.0F, state.opacity - (fade > 0 ? 1.0F / fade : 0.0F));
            }
        }
        ticks++;
    }

    private static int toScale(int value, int max, int scale) {
        return value > max ? scale : value < 0 ? 0 : (int) ((float) value / max * scale);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.screen() != null) {
            return;
        }
        for (State state : STATES) {
            ArmorBarMod.Unit unit = state.unit;
            if (!unit.shouldDisplay.get() || state.opacity <= 0.0F) {
                continue;
            }
            ItemStack stack;
            int max;
            switch (unit.kind) {
                case AMOUNT -> {
                    if (!unit.displayWhenEmpty.get() && state.value <= 0) {
                        continue;
                    }
                    stack = find(player, unit);
                    max = unit.analogUpperLimit.get();
                }
                case DURABILITY -> {
                    if (!unit.displayWhenEmpty.get() && state.value <= 0) {
                        continue;
                    }
                    stack = find(player, unit);
                    max = stack.getMaxDamage();
                }
                default -> {
                    stack = player.getItemBySlot(slot(unit.kind));
                    if (stack.isEmpty()) {
                        continue;
                    }
                    max = stack.getMaxDamage();
                }
            }
            int x = graphics.guiWidth() / 2 + unit.offsetX.get();
            int y = graphics.guiHeight() - unit.offsetY.get();
            int alpha = ARGB.white(state.opacity);
            graphics.fakeItem(stack, x, y);
            if (unit.analogBar.get()) {
                int value = toScale(state.value, max, 16);
                graphics.blit(RenderPipelines.GUI_TEXTURED, COUNTDOWN, x, y + 16, 0, 0, 16, 3, 256, 256, alpha);
                int v = value > 9 ? 3 : value > 4 ? 6 : 9;
                graphics.blit(RenderPipelines.GUI_TEXTURED, COUNTDOWN, x + unit.analogX.get(), y + unit.analogY.get(), 0, v, value, 3,
                        256, 256, alpha);
            }
            if (unit.numericCounter.get()) {
                String text = Integer.toString(state.value);
                graphics.text(mc.font, text, x + 8 - mc.font.width(text) / 2 + unit.counterX.get(), y - unit.counterY.get(),
                        ARGB.color(Math.round(state.opacity * 255), unit.color.get()), false);
            }
        }
    }
}
