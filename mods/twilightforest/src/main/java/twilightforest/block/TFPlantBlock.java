package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import twilightforest.TFBlocks;

/**
 * BlockTFPlant (Material.plants, no collision). Each metadata is a block of its own: where it may stand
 * (canBlockStay), its outline (setBlockBoundsBasedOnState) and its light (lightValue). The drops (getBlockDropped,
 * shears drop the plant itself) are in the loot tables, the grass colour of mayapple, clover and fiddlehead on the
 * client.
 */
public class TFPlantBlock extends VegetationBlock {
    public enum Kind {
        MOSS_PATCH(3, 0),
        MAYAPPLE(4, 0),
        CLOVER_PATCH(5, 0),
        FIDDLEHEAD(8, 0),
        MUSHGLOOM(9, 3),
        TORCHBERRY(13, 8),
        ROOT_STRAND(14, 0);

        public final int meta;
        public final int light;

        Kind(int meta, int light) {
            this.meta = meta;
            this.light = light;
        }
    }

    private static final VoxelShape MAYAPPLE_SHAPE = Block.box(4.0, 0.0, 4.0, 13.0, 6.0, 13.0);
    private final Kind kind;

    public TFPlantBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SUPPORTS_VEGETATION);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState soil = level.getBlockState(below);
        return switch (this.kind) {
            // moss and mushgloom: anything with a solid top
            case MOSS_PATCH, MUSHGLOOM -> soil.isFaceSturdy(level, below, Direction.UP);
            // torchberries and root strands hang below ground or grass, a root strand or a root block
            case TORCHBERRY, ROOT_STRAND -> canPlaceRootBelow(level, pos.above());
            // EnumPlantType.Plains: grass, dirt, farmland, and light 3 or the sky
            default -> (level.getRawBrightness(pos, 0) >= 3 || level.canSeeSky(pos)) && this.mayPlaceOn(soil, level, below);
        };
    }

    /** BlockTFPlant.canPlaceRootBelow: Material.ground or grass, a root strand, or a root block (metadata 0). */
    public static boolean canPlaceRootBelow(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND) || state.is(TFBlocks.ROOT_STRAND.get()) || state.is(TFBlocks.ROOT.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (this.kind) {
            case MOSS_PATCH -> patch(level, pos, false);
            case CLOVER_PATCH -> patch(level, pos, true);
            case MAYAPPLE -> MAYAPPLE_SHAPE;
            default -> Shapes.block();
        };
    }

    /** setBlockBoundsBasedOnState for moss (1/16 high) and clover: random insets, flush where the same plant is next to it. */
    private VoxelShape patch(BlockGetter level, BlockPos pos, boolean clover) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        long seed = x * 3129871 ^ y * 116129781L ^ z;
        seed = seed * seed * 42317861L + seed * 11L;
        int xOff0 = (int) (seed >> 12 & 3L);
        int xOff1 = (int) (seed >> 15 & 3L);
        int zOff0 = (int) (seed >> 18 & 3L);
        int zOff1 = (int) (seed >> 21 & 3L);
        int yOff0 = (int) (seed >> 24 & 1L);
        int yOff1 = (int) (seed >> 27 & 1L);
        boolean xConnect0 = level.getBlockState(pos.east()).is(this);
        boolean xConnect1 = level.getBlockState(pos.west()).is(this);
        boolean zConnect0 = level.getBlockState(pos.south()).is(this);
        boolean zConnect1 = level.getBlockState(pos.north()).is(this);
        double maxY = clover ? 1.0 + yOff0 + yOff1 : 1.0;
        return Block.box(xConnect1 ? 0.0 : 1.0 + xOff1, 0.0, zConnect1 ? 0.0 : 1.0 + zOff1,
                xConnect0 ? 16.0 : 15.0 - xOff0, maxY, zConnect0 ? 16.0 : 15.0 - zOff0);
    }

    /** randomDisplayTick: moss gives off "townaura" (mycelium) 1 in 10. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.kind == Kind.MOSS_PATCH && random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.MYCELIUM, pos.getX() + random.nextFloat(), pos.getY() + 0.1F, pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }
}
