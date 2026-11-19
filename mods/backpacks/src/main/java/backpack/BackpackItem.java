package backpack;

import backpack.inventory.BackpackMenu;
import backpack.inventory.WorkbenchBackpackMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * ItemBackpack and ItemWorkbenchBackpack (both IBackpack): every damage value of the original is its own item.
 * {@code color} is the index into Constants.BACKPACK_COLORS (0..15 the dye colours in 1.5.2 dye order, 16 the plain
 * backpack), -1 for the ender and the workbench backpack. Worn in the chest slot (ItemArmor with armour type 1); the
 * contents live in the item (DataComponents.CONTAINER instead of the "Inventory" NBT).
 *
 * <p>Right click opens the backpack (unless openOnlyWearedBackpacks), sneaking right click opens the rename screen on
 * the client (not for the ender backpack).
 */
public class BackpackItem extends Item {
    public enum Kind { NORMAL, ENDER, WORKBENCH }

    public static final String[] COLORS = {"black", "red", "green", "brown", "blue", "purple", "cyan", "light_gray", "gray", "pink", "lime",
            "yellow", "light_blue", "magenta", "orange", "white"};

    /** Set by the client: opens GuiBackpackAlt (the rename screen). */
    public static Runnable openRenameScreen = () -> { };

    private final Kind kind;
    private final int color;
    private final boolean big;

    public BackpackItem(Properties properties, Kind kind, int color, boolean big) {
        super(properties);
        this.kind = kind;
        this.color = color;
        this.big = big;
    }

    public Kind kind() {
        return this.kind;
    }

    public int color() {
        return this.color;
    }

    public boolean big() {
        return this.big;
    }

    /** The item name of a variant: backpack[_color][_big], backpack_ender, backpack_workbench[_big]. */
    public static String name(Kind kind, int color, boolean big) {
        String name = switch (kind) {
            case ENDER -> "backpack_ender";
            case WORKBENCH -> "backpack_workbench";
            case NORMAL -> color == 16 ? "backpack" : "backpack_" + COLORS[color];
        };
        return big ? name + "_big" : name;
    }

    /** InventoryBackpack.getInventorySize in rows: normal 3 / big 6 (config), workbench 0 / big 2, ender 3. */
    public int rows() {
        return switch (this.kind) {
            case NORMAL -> this.big ? BackpackConfig.sizeL() : BackpackConfig.sizeM();
            case WORKBENCH -> this.big ? 2 : 0;
            case ENDER -> 3;
        };
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            if (player.isShiftKeyDown() && this.kind != Kind.ENDER) {
                openRenameScreen.run();
            }
        } else if (!player.isShiftKeyDown() && !BackpackConfig.openOnlyWearedBackpacks() && player instanceof ServerPlayer sp) {
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;
            open(sp, player.getItemInHand(hand), slot);
        }
        return InteractionResult.SUCCESS;
    }

    /** The key binding (doKeyBindingAction): open the backpack worn in the chest slot. */
    public static void openWorn(ServerPlayer player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof BackpackItem) {
            open(player, stack, -1);
        }
    }

    /**
     * Opens {@code stack} (held in inventory slot {@code slot}, or worn when -1). The menu stays valid while that
     * very stack is still in its place (ContainerBackpack.canInteractWith).
     */
    public static void open(ServerPlayer player, ItemStack stack, int slot) {
        if (!(stack.getItem() instanceof BackpackItem item)) {
            return;
        }
        BackpackMenu.Location location = new BackpackMenu.Location(player, stack, slot);
        int rows = item.rows();
        switch (item.kind) {
            case ENDER -> {
                player.getEnderChestInventory().setActiveChest(null);
                player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) -> new BackpackMenu(id, inv,
                        player.getEnderChestInventory(), 3, location), Component.translatable("container.enderchest")), buf -> buf.writeVarInt(3));
            }
            case NORMAL -> player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) -> new BackpackMenu(id, inv,
                    new BackpackContainer(stack, DataComponents.CONTAINER, rows * 9), rows, location), stack.getHoverName()), buf -> buf.writeVarInt(rows));
            case WORKBENCH -> player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) -> new WorkbenchBackpackMenu(id, inv,
                    new BackpackContainer(stack, DataComponents.CONTAINER, rows * 9), rows, location), stack.getHoverName()), buf -> buf.writeVarInt(rows));
        }
    }
}
