package legendgear.block;

import legendgear.LGConfig;
import legendgear.LGItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * MysticShrub: stage 0 a plain shrub (light 5), 1 energized in a thunderstorm (light 8), 2 cut (light 2, hardness 1).
 * Breaking a plain or energized shrub drops its prizes and leaves it cut; a cut shrub grows back in random ticks
 * (1 in 4 while it rains, else 1 in 20), energized during thunder. It only stays under the open sky, on the topmost
 * solid block of its column, on grass, dirt or farmland. Walking into an energized shrub calls lightning.
 */
public class MysticShrubBlock extends Block {
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 2);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);
    private static final VoxelShape CUT_SHAPE = Block.box(0, 0, 0, 16, 3, 16);

    public MysticShrubBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    public static int light(BlockState state) {
        return switch (state.getValue(STAGE)) {
            case 0 -> 5;
            case 1 -> 8;
            default -> 2;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(STAGE) == 2 ? CUT_SHAPE : SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (state.getValue(STAGE) != 2) {
            return 1.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    /** canBlockStay: open sky, the topmost block of the column (getTopSolidOrLiquidBlock) and EnumPlantType.Plains soil. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        boolean plains = soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.DIRT) || soil.is(Blocks.FARMLAND) || soil.is(BlockTags.DIRT);
        return level.canSeeSky(pos) && level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) == pos.getY() && plains;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            net.minecraft.world.level.redstone.@org.jspecify.annotations.Nullable Orientation orientation, boolean moved) {
        if (level instanceof ServerLevel server) {
            this.checkFlowerChange(state, server, pos);
        }
    }

    private boolean checkFlowerChange(BlockState state, ServerLevel level, BlockPos pos) {
        if (!state.canSurvive(level, pos)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            return false;
        }
        if (state.getValue(STAGE) == 1 && !level.isThundering()) {
            level.setBlock(pos, state.setValue(STAGE, 0), 2);
        }
        return true;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.checkFlowerChange(state, level, pos)) {
            return;
        }
        state = level.getBlockState(pos);
        if ((level.isRaining() ? random.nextInt(4) == 0 : random.nextInt(20) == 0) && state.getValue(STAGE) == 2) {
            level.setBlock(pos, state.setValue(STAGE, level.isThundering() ? 1 : 0), 2);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (state.getValue(STAGE) == 1 && entity instanceof LivingEntity && level instanceof ServerLevel server) {
            lightning(server, pos);
            level.setBlock(pos, state.setValue(STAGE, 0), 2);
        }
    }

    /** canLightningStrikeAt + addWeatherEffect(new EntityLightningBolt). */
    public static void lightning(ServerLevel level, BlockPos pos) {
        if (level.isRainingAt(pos)) {
            LightningBolt bolt = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt != null) {
                bolt.snapTo(pos.getX(), pos.getY(), pos.getZ());
                level.addFreshEntity(bolt);
            }
        }
    }

    /** onBlockDestroyedByPlayer: a plain or energized shrub stays as a cut shrub; its prizes drop (getBlockDropped). */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack tool, boolean willHarvest, FluidState fluid) {
        this.playerWillDestroy(level, pos, state, player);
        int stage = state.getValue(STAGE);
        if (!level.isClientSide() && !player.getAbilities().instabuild) {
            for (ItemStack drop : prizes(level.getRandom(), stage)) {
                Block.popResource(level, pos, drop);
            }
        }
        level.setBlock(pos, stage < 2 ? state.setValue(STAGE, 2) : fluid.createLegacyBlock(), 3);
        return false;
    }

    public static List<ItemStack> prizes(RandomSource random, int stage) {
        List<ItemStack> ret = new ArrayList<>();
        if (stage == 1 && LGConfig.SHRUB_SUPER_PRIZES) {
            switch (random.nextInt(5)) {
                case 0 -> ret.add(new ItemStack(Items.GOLD_NUGGET));
                case 1 -> {
                    for (int i = 0; i < 3; i++) {
                        ret.add(new ItemStack(LGItems.HEART.get()));
                    }
                }
                case 2 -> ret.add(new ItemStack(Items.ARROW, 5));
                case 3 -> ret.add(new ItemStack(LGItems.EMERALD_PIECE.get()));
                default -> ret.add(new ItemStack(LGItems.BOMB.get(), 3));
            }
        } else if (stage == 0) {
            if (random.nextFloat() < LGConfig.SHRUB_HEART_CHANCE) {
                ret.add(new ItemStack(LGItems.HEART.get()));
            }
            if (random.nextFloat() < LGConfig.SHRUB_SHARD_CHANCE) {
                ret.add(new ItemStack(LGItems.EMERALD_SHARD.get()));
            }
            if (random.nextFloat() < LGConfig.SHRUB_ARROW_CHANCE) {
                ret.add(new ItemStack(Items.ARROW));
            }
        }
        return ret;
    }

    /** ShrubClusterMaker.putShrub: only where the block can be placed and stay. */
    public boolean put(Level level, BlockPos pos, int stage) {
        BlockState here = level.getBlockState(pos);
        BlockState shrub = this.defaultBlockState().setValue(STAGE, stage);
        if ((here.isAir() || here.canBeReplaced()) && shrub.canSurvive(level, pos)) {
            level.setBlock(pos, shrub, 2);
            return true;
        }
        return false;
    }

    /** ShrubClusterMaker.buildShrubStar: the star of twelve shrubs a mystic seed plants around itself. */
    public void buildStar(Level level, BlockPos c, int stage) {
        int[][] offsets = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}, {-3, 0}, {0, -3}, {3, 0}, {0, 3}, {-2, -2}, {2, -2}, {2, 2}, {-2, 2}};
        for (int[] o : offsets) {
            this.put(level, c.offset(o[0], 0, o[1]), stage);
        }
    }
}
