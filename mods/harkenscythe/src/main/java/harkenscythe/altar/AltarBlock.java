package harkenscythe.altar;

import harkenscythe.HSBlocks;
import harkenscythe.block.CrucibleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockHSSoulAltar / BlockHSBloodAltar: a 3/4 high table with a floating book (like the enchanting table). Right-click
 * (not sneaking) opens the altar; it works with the souls or blood of the crucibles around it, like the bookshelves of
 * the enchanting table: in a ring two blocks out, one block lower to one block higher, with air between altar and
 * crucible one block above the crucible's height.
 */
public class AltarBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);
    private final boolean blood;

    public AltarBlock(boolean blood, Properties properties) {
        super(properties);
        this.blood = blood;
    }

    public boolean isBlood() {
        return this.blood;
    }

    public AltarRecipe.Kind kind() {
        return this.blood ? AltarRecipe.Kind.BLOOD : AltarRecipe.Kind.SOUL;
    }

    public Block crucible() {
        return this.blood ? HSBlocks.BLOOD_CRUCIBLE.get() : HSBlocks.SOUL_CRUCIBLE.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, HSBlocks.ALTAR_ENTITY.get(), AltarBlockEntity::bookAnimationTick) : null;
    }

    /** onBlockActivated: not while sneaking. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof AltarBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Component title = Component.translatable(this.getDescriptionId());
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, p) -> new AltarMenu(id, inventory, pos, this.blood), title), buf -> {
                buf.writeBlockPos(pos);
                buf.writeBoolean(this.blood);
            });
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * The crucible loop of onBlockActivated, soulDrain and randomDisplayTick: the ring of the 5x5 square (the inner
     * columns only at the far rows), heights -1 to +1; the first crucible of a column with something in the way (one
     * block above it, halfway to the altar) ends that column.
     */
    private static void crucibles(Level level, BlockPos pos, Block crucible, @Nullable RandomSource random, CrucibleVisitor visitor) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x > -2 && x < 2 && z == -1) {
                    z = 2;
                }
                if (random != null && random.nextInt(16) != 0) {
                    continue;
                }
                for (int y = -1; y <= 1; y++) {
                    BlockPos at = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(at);
                    if (state.is(crucible)) {
                        if (!level.isEmptyBlock(pos.offset(x / 2, y + 1, z / 2))) {
                            break;
                        }
                        visitor.visit(at, state, x, y, z);
                    }
                }
            }
        }
    }

    private interface CrucibleVisitor {
        void visit(BlockPos at, BlockState state, int x, int y, int z);
    }

    /** soulCountTotal / bloodCountTotal: the essence of all reachable crucibles (levels times 5 or 10). */
    public int essence(Level level, BlockPos pos) {
        int[] levels = {0};
        crucibles(level, pos, this.crucible(), null, (at, state, x, y, z) -> levels[0] += state.getValue(CrucibleBlock.LEVEL));
        return levels[0] * this.kind().unit;
    }

    /** soulDrain / bloodDrain: takes {@code levels} crucible levels, crucible after crucible. */
    public void drain(Level level, BlockPos pos, int levels) {
        int[] cost = {levels};
        crucibles(level, pos, this.crucible(), null, (at, state, x, y, z) -> {
            int content = state.getValue(CrucibleBlock.LEVEL);
            if (content > cost[0]) {
                level.setBlock(at, state.setValue(CrucibleBlock.LEVEL, content - cost[0]), Block.UPDATE_CLIENTS);
                cost[0] = 0;
            } else if (content > 0) {
                level.setBlock(at, state.setValue(CrucibleBlock.LEVEL, 0), Block.UPDATE_CLIENTS);
                cost[0] -= content;
            }
        });
    }

    /** randomDisplayTick: portal particles (souls) or red dust (blood) above the altar for the crucibles around it. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        crucibles(level, pos, this.crucible(), random, (at, crucible, x, y, z) -> {
            double px = pos.getX() + 0.5;
            double py = pos.getY() + 2.0;
            double pz = pos.getZ() + 0.5;
            if (this.blood) {
                level.addParticle(new DustParticleOptions(0xCC0000, 1.0F), px, py, pz, 0.8, 0.0, 0.0);
            } else {
                level.addParticle(ParticleTypes.PORTAL, px, py, pz, x + random.nextFloat() - 0.5, y - random.nextFloat() - 1.0F,
                        z + random.nextFloat() - 0.5);
            }
        });
    }
}
