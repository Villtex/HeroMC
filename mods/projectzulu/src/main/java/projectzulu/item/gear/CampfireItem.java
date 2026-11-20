package projectzulu.item.gear;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import projectzulu.block.gear.CampfireBlock;

import java.util.Map;

/** ItemCampFire: one item per metadata of 1.5.2 (wood, stone, lit wood, lit stone), all placing the campfire block. */
public class CampfireItem extends BlockItem {
    private final CampfireBlock.Kind kind;
    private final boolean lit;

    public CampfireItem(Block block, CampfireBlock.Kind kind, boolean lit, Properties properties) {
        super(block, properties);
        this.kind = kind;
        this.lit = lit;
    }

    /** Only the plain wood campfire is the block's item (pick block, drops); the others place it lit or in stone. */
    @Override
    public void registerBlocks(Map<Block, Item> map, Item item) {
        if (this.kind == CampfireBlock.Kind.WOOD && !this.lit) {
            super.registerBlocks(map, item);
        }
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        return state == null ? null : state.setValue(CampfireBlock.KIND, this.kind).setValue(CampfireBlock.LIT, this.lit);
    }
}
