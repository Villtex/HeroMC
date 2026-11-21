package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import twilightforest.TFTower;

/**
 * BlockTFTowerTranslucent (1371), one block per metadata value ({@link TFTower#TRANSLUCENTS}): 0/1 the trace a
 * vanished reappearing block leaves (no collision, small selection box; it comes back after 80 ticks), 2/3 the blocks
 * of the carminite builder (they vanish when activated), 4 what the antibuilder puts back, 5 the debris of the
 * carminite reactor, 6/7 fake gold and diamond around the reactor. Material.glass, hardness 50 (4 and 5: 0.3),
 * drops nothing.
 */
public class TowerTranslucentBlock extends Block {
    public static final int REAPPEARING_INACTIVE = 0;
    public static final int REAPPEARING_ACTIVE = 1;
    public static final int BUILT_INACTIVE = 2;
    public static final int BUILT_ACTIVE = 3;
    public static final int REVERTER_REPLACEMENT = 4;
    public static final int REACTOR_DEBRIS = 5;
    public static final int FAKE_GOLD = 6;
    public static final int FAKE_DIAMOND = 7;
    private static final VoxelShape TRACE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);

    public final int meta;

    public TowerTranslucentBlock(int meta, Properties properties) {
        super(properties);
        this.meta = meta;
    }

    public static float hardness(int meta) {
        return meta == REVERTER_REPLACEMENT || meta == REACTOR_DEBRIS ? 0.3F : 50.0F;
    }

    public static BlockState translucent(int meta) {
        return TFTower.TRANSLUCENTS.get(meta).get().defaultBlockState();
    }

    /** The trace of a reappearing block has no collision box (getCollisionBoundingBoxFromPool null). */
    public static boolean solid(int meta) {
        return meta != REAPPEARING_INACTIVE && meta != REAPPEARING_ACTIVE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // setBlockBoundsBasedOnState: the reactor debris had random bounds on every call; here a full block
        return solid(this.meta) ? Shapes.block() : TRACE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return solid(this.meta) ? Shapes.block() : Shapes.empty();
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.meta == BUILT_ACTIVE) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, this);
            TowerDeviceBlock.pop(level, pos, 0.3F, 0.5F);
            TowerDeviceBlock.activateNeighbours(level, pos);
        }
        if (this.meta == REAPPEARING_ACTIVE) {
            level.setBlock(pos, TowerDeviceBlock.device(TowerDeviceBlock.REAPPEARING_INACTIVE), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, this);
            TowerDeviceBlock.click(level, pos, 0.3F, 0.5F);
        } else if (this.meta == REAPPEARING_INACTIVE) {
            TowerDeviceBlock.changeToActiveVanishBlock(level, pos, translucent(REAPPEARING_ACTIVE));
        }
    }
}
