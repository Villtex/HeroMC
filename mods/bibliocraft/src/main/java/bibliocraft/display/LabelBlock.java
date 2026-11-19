package bibliocraft.display;

import bibliocraft.common.BiblioBlock;
import bibliocraft.common.BiblioMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockLabel ("Wooden Label", metadata = wood): a small plate on the side of a block showing up to three items. No
 * collision (getCollisionBoundingBoxFromPool null); sneak-click opens the GUI, any other click does nothing (but is
 * still used up, so nothing can be placed against the label). The light is that of the brightest shown block.
 */
public class LabelBlock extends BiblioBlock {
    /** setBlockBoundsBasedOnState: 0.94..1, 0.2..0.4, 0.3..0.7 for angle 0, turned for the others. */
    private static final VoxelShape[] SHAPES = rotations(15.04, 3.2, 4.8, 16, 6.4, 11.2);
    /** 0 oak, 1 spruce, 2 birch, 3 jungle (texture woodlabel0..3). */
    public final int wood;

    public LabelBlock(int wood, Properties properties) {
        super(true, properties);
        this.wood = wood;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LabelBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[angle(state)];
    }

    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    /** getLightValue: the largest Block.lightValue of the three slots. */
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return DisplayLight.get(level, pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer sp && player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof LabelBlockEntity be) {
            BiblioMenu.open(sp, be, Component.literal("Wooden Label"), LabelMenu::new);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        DisplayLight.remove(level, pos);
    }
}
