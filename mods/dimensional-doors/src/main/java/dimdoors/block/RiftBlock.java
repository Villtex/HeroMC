package dimdoors.block;

import dimdoors.DimDoors;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * BlockRift of Dimensional Doors 1.4.1: where a link starts. Of material air (a block placed there replaces it), not
 * to be hit or walked into, dimming light by 14, drawn only by its particles. Rifts near other rifts eat the blocks
 * around them (random ticks, "Enable Rift Griefing").
 */
public class RiftBlock extends BaseEntityBlock {
    public RiftBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RiftBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, DimDoors.RIFT_ENTITY.get(), RiftBlockEntity::tick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 14;
    }

    /** updateTick: a rift near another one eats blocks within a few steps. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (DDWorld.link(level, pos) == null || !DimDoors.RIFT_GRIEFING || !(level.getBlockEntity(pos) instanceof RiftBlockEntity rift)
                || !rift.isNearRift) {
            return;
        }
        int range = 4;
        float distance = range + range / 4;
        boolean flag = true;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = -range; i < range && flag; i++) {
            for (int j = -range; j < range && flag; j++) {
                for (int k = -range; k < range && flag; k++) {
                    at.set(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                    BlockState there = level.getBlockState(at);
                    int d = Math.abs(i) + Math.abs(j) + Math.abs(k);
                    if (!DimDoors.immuneToRift(there) && d < distance && !there.isAir() && d != 0 && random.nextInt(2) == 0) {
                        level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        flag = random.nextBoolean() || random.nextBoolean();
                    }
                }
            }
        }
    }

    /** randomDisplayTick: the black sparks (grey in pockets) RiftFX drew, two by two along the line to the nearest rift. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (!(level.getBlockEntity(pos) instanceof RiftBlockEntity tile)) {
            return;
        }
        DustParticleOptions spark = new DustParticleOptions(DDWorld.isPocket(level) ? 0xB3B3B3 : 0x000000, 0.75F);
        float xGrowth = 0;
        float yGrowth = 0;
        float zGrowth = 0;
        float xGrowthN = 0;
        float yGrowthN = 0;
        float zGrowthN = 0;
        for (int count = 0; count < 12; count++) {
            if (tile.xOffset > 0) {
                if (rand.nextInt(tile.xOffset) == 0) {
                    xGrowth += 0.15F * tile.xOffset;
                }
            } else if (tile.xOffset < 0 && rand.nextInt(-tile.xOffset) == 0) {
                xGrowthN -= 0.15F * -tile.xOffset;
            }
            if (tile.yOffset > 0) {
                if (rand.nextInt(tile.yOffset) == 0) {
                    yGrowth += 0.15F * tile.yOffset;
                }
            } else if (tile.yOffset < 0 && rand.nextInt(-tile.yOffset) == 0) {
                yGrowthN -= 0.15F * -tile.yOffset;
            }
            if (tile.zOffset > 0) {
                if (rand.nextInt(tile.zOffset) == 0) {
                    zGrowth += 0.15F * tile.zOffset;
                }
            } else if (tile.zOffset < 0 && rand.nextInt(-tile.zOffset) == 0) {
                zGrowthN -= 0.15F * -tile.zOffset;
            }
            float xChange = (float) (xGrowth + xGrowthN + rand.nextGaussian() * 0.05F);
            float yChange = (float) (yGrowth + yGrowthN + rand.nextGaussian() * 0.05F);
            float zChange = (float) (zGrowth + zGrowthN + rand.nextGaussian() * 0.05F);
            float xOff = 0.25F / (1.0F + Math.abs(xChange));
            float yOff = 0.25F / (1.0F + Math.abs(yChange));
            float zOff = 0.25F / (1.0F + Math.abs(zChange));
            level.addParticle(spark, pos.getX() + 0.5 + xChange + xOff * rand.nextGaussian(), pos.getY() + 0.5 + yChange + yOff * rand.nextGaussian(),
                    pos.getZ() + 0.5 + zChange + zOff * rand.nextGaussian(), 0, 0, 0);
            level.addParticle(spark, pos.getX() + 0.5 - xChange - xOff * rand.nextGaussian(), pos.getY() + 0.5 - yChange - yOff * rand.nextGaussian(),
                    pos.getZ() + 0.5 - zChange - zOff * rand.nextGaussian(), 0, 0, 0);
            if (rand.nextBoolean()) {
                level.addParticle(spark, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, rand.nextGaussian() * 0.01, rand.nextGaussian() * 0.01,
                        rand.nextGaussian() * 0.01);
            }
            if (tile.shouldClose) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        rand.nextGaussian() * 0.01, rand.nextGaussian() * 0.01, rand.nextGaussian() * 0.01);
            }
        }
    }
}
