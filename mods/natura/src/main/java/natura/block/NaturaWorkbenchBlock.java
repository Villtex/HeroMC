package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * AlternateWorkbench: a crafting table of each Natura wood. The original opened its own copy of the vanilla
 * crafting container (WorkbenchContainer/WorkbenchGui); here it is the vanilla crafting menu, which only checks that
 * the player still stands at this block instead of at a vanilla crafting table. The client shows the vanilla screen.
 */
public class NaturaWorkbenchBlock extends CraftingTableBlock {
    private static final Component TITLE = Component.translatable("container.crafting");

    public NaturaWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) -> new Menu(id, inventory, ContainerLevelAccess.create(level, pos), this), TITLE);
    }

    /** The vanilla crafting menu, valid while the player is near this workbench. */
    public static final class Menu extends CraftingMenu {
        private final ContainerLevelAccess access;
        private final Block block;

        public Menu(int id, Inventory inventory, ContainerLevelAccess access, Block block) {
            super(id, inventory, access);
            this.access = access;
            this.block = block;
        }

        @Override
        public boolean stillValid(Player player) {
            return stillValid(this.access, player, this.block);
        }
    }
}
