package bibliocraft.bookcase;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

/**
 * ItemRedstoneBook: in a bookcase it powers the neighbours with the number of its slot (TileEntityBookcase). The
 * tooltip says "by &lt;player&gt;": the original wrote the name of the first player who looked at it into the stack
 * ("redstonebook"), or "by James Maxwell" without a player; here the name is kept in the stack's custom data the
 * same way, but only shown (the client cannot change the stack).
 */
public class RedstoneBookItem extends Item {
    public RedstoneBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        String name = null;
        if (data != null) {
            CompoundTag tag = data.copyTag();
            name = tag.getStringOr("redstonebook", null);
        }
        if (name == null) {
            name = ClientName.get();
        }
        tooltip.accept(Component.literal(name));
    }

    /** "by " + the viewing player's name, kept out of the server's class loading. */
    private static final class ClientName {
        static String get() {
            try {
                Minecraft mc = Minecraft.getInstance();
                return mc.player != null ? "by " + mc.player.getName().getString() : "by James Maxwell";
            } catch (Throwable t) {
                return "by James Maxwell";
            }
        }
    }
}
