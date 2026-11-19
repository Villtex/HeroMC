package bibliocraft.decor;

import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.common.BiblioMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockTable: one slot on the top. The legs and corners of the top depend on the tables next to it
 * (checkNeighborTables, kept in the tile entity as in the original): a table alone stands on its middle leg, tables in
 * a row or square lose the legs and rounded corners where they meet.
 *
 * <p>Right click on the top: sneaking opens the GUI, an item in the hand goes onto the empty table (the whole stack),
 * otherwise the item on the table drops off. Right click on a side with a tape measure, wrench, screwdriver or crowbar
 * turns the item on the table (north/south face: about the vertical, east/west face: tilts it).
 */
public class TableBlock extends BiblioBlock {
    /** setBlockBoundsBasedOnState: only the top (0.88..1) when the table has no leg at all, else the full block. */
    private static final VoxelShape TOP = Block.box(0.0, 14.08, 0.0, 16.0, 16.0, 16.0);
    /** Metadata of the original: 0 oak, 1 spruce, 2 birch, 3 jungle (texture table0..3). */
    public final int wood;

    public TableBlock(int wood, Properties properties) {
        super(false, properties);
        this.wood = wood;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // no angle
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TableBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof TableBlockEntity be && !be.leg1 && !be.leg2 && !be.leg3 && !be.leg4 && !be.monoleg) {
            return TOP;
        }
        return Shapes.block();
    }

    /** getLightValue: the light of the block lying on the table (Block.lightValue of the item's id). */
    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TableBlockEntity be && be.getItem(0).getItem() instanceof BlockItem item) {
            return item.getBlock().defaultBlockState().getLightEmission();
        }
        return 0;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TableBlockEntity be) {
            this.activate(level, pos, player, held, be, hit.getDirection());
        }
        return InteractionResult.SUCCESS;
    }

    /** onBlockActivated (server). */
    private void activate(Level level, BlockPos pos, Player player, ItemStack held, TableBlockEntity be, Direction face) {
        if (face == Direction.UP) {
            if (player.isShiftKeyDown()) {
                if (player instanceof ServerPlayer sp) {
                    BiblioMenu.open(sp, be, Component.literal("Wooden Table"), TableMenu::new);
                }
                return;
            }
            if (!held.isEmpty() && !be.isSlotFull()) {
                be.setItem(0, held);
                player.getInventory().setItem(player.getInventory().getSelectedSlot(), ItemStack.EMPTY);
                return;
            }
            if (be.isSlotFull()) {
                dropTableItem(level, pos, be);
                be.setItem(0, ItemStack.EMPTY);
            }
            return;
        }
        if (!held.isEmpty()) {
            String name = BCConfig.codeName(held);
            if (name.contains("measure") || name.contains("wrench") || name.contains("screwdriver") || name.contains("crowbar")) {
                if (face == Direction.NORTH || face == Direction.SOUTH) {
                    be.setSlotX((be.slotX() + 1) % 4);
                }
                if (face == Direction.WEST || face == Direction.EAST) {
                    be.setSlotY((be.slotY() + 1) % 4);
                }
            }
        }
    }

    /** dropTableItem: the item pops up above the middle of the table, without motion. */
    private static void dropTableItem(Level level, BlockPos pos, TableBlockEntity be) {
        ItemStack stuff = be.getItem(0);
        if (!stuff.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5F, pos.getY() + 1.4F, pos.getZ() + 0.5F, stuff.copy(), 0.0, 0.0, 0.0));
        }
    }

    /** onBlockPlacedBy: work out the legs, then tell the neighbours (which are tables) to do the same. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TableBlockEntity be) {
            checkNeighborTables(level, pos, be);
            level.updateNeighborsAt(pos, this, null);
        }
    }

    /**
     * onNeighborBlockChange: only when the block that changed was a table (the id passed is the old block, so a table
     * that is placed does not count here; it tells its neighbours itself in setPlacedBy, a removed one does count).
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide() && block instanceof TableBlock && level.getBlockEntity(pos) instanceof TableBlockEntity be) {
            checkNeighborTables(level, pos, be);
        }
    }

    /**
     * checkNeighborTables: east (x+1) removes legs/corners 1 and 2, west 3 and 4, south (z+1) 2 and 3, north 1 and 4;
     * without any neighbour table only the middle leg stands.
     */
    static void checkNeighborTables(Level level, BlockPos pos, TableBlockEntity table) {
        boolean east = isTable(level, pos.east());
        boolean west = isTable(level, pos.west());
        boolean south = isTable(level, pos.south());
        boolean north = isTable(level, pos.north());
        if (!east && !west && !south && !north) {
            table.setShape(false, false, false, false, true, true, true, true, true);
            return;
        }
        boolean q1 = !east && !north;
        boolean q2 = !east && !south;
        boolean q3 = !west && !south;
        boolean q4 = !west && !north;
        table.setShape(q1, q2, q3, q4, false, q1, q2, q3, q4);
    }

    /** checkBlockID: only BiblioCraft tables (the BiblioWoods add-ons are not in the pack). */
    private static boolean isTable(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof TableBlock;
    }
}
