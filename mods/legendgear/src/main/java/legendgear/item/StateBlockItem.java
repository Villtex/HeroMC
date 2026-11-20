package legendgear.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.UnaryOperator;

/** An ItemBlock with metadata of 1.5.2: places its block in one state (the locks, the energized shrub); may glow. */
public class StateBlockItem extends BlockItem {
    private final UnaryOperator<BlockState> state;
    private final boolean foil;

    public StateBlockItem(Block block, Properties properties, UnaryOperator<BlockState> state, boolean foil) {
        super(block, properties);
        this.state = state;
        this.foil = foil;
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        BlockState placed = super.getPlacementState(context);
        if (placed == null) {
            return null;
        }
        BlockState s = this.state.apply(placed);
        return this.canPlace(context, s) ? s : null;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.foil || super.isFoil(stack);
    }
}
