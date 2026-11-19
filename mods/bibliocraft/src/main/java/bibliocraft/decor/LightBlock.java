package bibliocraft.decor;

import bibliocraft.common.BiblioBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Common part of BlockLantern and BlockLamp: light 15, the colour is the block (metadata of the original), the angle
 * comes from the player's yaw (onBlockPlacedBy, {@link BiblioBlock#ANGLE}) and the style from the face clicked
 * (onBlockPlaced): 0 standing on the floor, 1 on a wall, 2 hanging from the ceiling. The original kept the style in a
 * field of the block between onBlockPlaced and onBlockPlacedBy (shared by all players); the pitch it also worked out
 * there was never used. Every right click is taken (onBlockActivated returned true).
 */
public abstract class LightBlock extends BiblioBlock {
    public static final IntegerProperty STYLE = IntegerProperty.create("style", 0, 2);
    /** Metadata of the original: the dye colour 0..15 (lanternCandle0..15 / lampLight0..15). */
    public final int color;

    protected LightBlock(int color, Properties properties) {
        super(true, properties);
        this.color = color;
        this.registerDefaultState(this.defaultBlockState().setValue(STYLE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STYLE);
    }

    public static int style(BlockState state) {
        return state.hasProperty(STYLE) ? state.getValue(STYLE) : 0;
    }

    /** onBlockPlaced: bottom face (side 0) -> 2, top face (1) -> 0, a side -> 1. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        int style = side == Direction.DOWN ? 2 : side == Direction.UP ? 0 : 1;
        return this.defaultBlockState().setValue(STYLE, style);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return InteractionResult.SUCCESS;
    }
}
