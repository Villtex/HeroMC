package tconstruct.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import tconstruct.menu.CraftingStationMenu;
import tconstruct.menu.PartBuilderMenu;
import tconstruct.menu.PatternChestMenu;
import tconstruct.menu.StencilTableMenu;
import tconstruct.menu.ToolStationMenu;

/**
 * {@code ToolStationBlock} (tool station, part builders, pattern chest, stencil tables), {@code ToolForgeBlock},
 * {@code CraftingStationBlock} and {@code CraftingSlab}: one block per kind and wood or metal today. The tables are
 * drawn as a table (top and four legs, TableRender) with a full block as collision box; the pattern chest is 14/16
 * high. The slabs (CraftingSlab) are half blocks, at the top when placed from below or into the upper half of a face
 * (also onto the top face, as in 1.5.2). Right-click (not sneaking) opens the GUI.
 */
public class TableBlock extends BaseEntityBlock {
    private static final VoxelShape CHEST = Block.box(0, 0, 0, 16, 14, 16);

    /** What the block does (the GUI of 1.5.2). */
    public enum Kind {
        TOOL_STATION, PART_BUILDER, PATTERN_CHEST, STENCIL_TABLE, TOOL_FORGE, CRAFTING_STATION
    }

    public final Kind kind;
    public final boolean slab;

    public TableBlock(Kind kind, boolean slab, Properties properties) {
        super(properties);
        this.kind = kind;
        this.slab = slab;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.kind == Kind.PATTERN_CHEST ? CHEST : Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (this.kind) {
            case TOOL_STATION, TOOL_FORGE -> new ToolStationBlockEntity(pos, state);
            case PART_BUILDER -> new PartBuilderBlockEntity(pos, state);
            case PATTERN_CHEST -> new PatternChestBlockEntity(pos, state);
            case STENCIL_TABLE -> new StencilTableBlockEntity(pos, state);
            case CRAFTING_STATION -> new CraftingStationBlockEntity(pos, state);
        };
    }

    /** InventoryBlock.onBlockActivated: not while sneaking. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            this.open(serverPlayer, level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private void open(ServerPlayer player, Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        Component title = Component.translatable(this.getDescriptionId());
        switch (this.kind) {
            case TOOL_STATION, TOOL_FORGE -> {
                if (entity instanceof ToolStationBlockEntity station) {
                    boolean forge = station.isForge();
                    player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ToolStationMenu(id, inv, station, forge), title), buf -> {
                        buf.writeBlockPos(pos);
                        buf.writeBoolean(forge);
                    });
                }
            }
            case PART_BUILDER -> {
                if (entity instanceof PartBuilderBlockEntity builder) {
                    PatternChestBlockEntity chest = PartBuilderMenu.findChest(level, pos);
                    player.openMenu(new SimpleMenuProvider((id, inv, p) -> new PartBuilderMenu(id, inv, builder, chest), title), buf -> {
                        buf.writeBlockPos(pos);
                        buf.writeBoolean(chest != null);
                    });
                }
            }
            case PATTERN_CHEST -> {
                if (entity instanceof PatternChestBlockEntity chest) {
                    player.openMenu(new SimpleMenuProvider((id, inv, p) -> new PatternChestMenu(id, inv, chest), title), buf -> buf.writeBlockPos(pos));
                }
            }
            case STENCIL_TABLE -> {
                if (entity instanceof StencilTableBlockEntity table) {
                    player.openMenu(new SimpleMenuProvider((id, inv, p) -> new StencilTableMenu(id, inv, table), title), buf -> buf.writeBlockPos(pos));
                }
            }
            case CRAFTING_STATION -> {
                if (entity instanceof CraftingStationBlockEntity station) {
                    player.openMenu(new SimpleMenuProvider((id, inv, p) -> new CraftingStationMenu(id, inv, station), title));
                }
            }
        }
    }
}
