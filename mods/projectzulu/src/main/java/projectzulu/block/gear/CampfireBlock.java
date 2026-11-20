package projectzulu.block.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import projectzulu.ZuluGear;

/**
 * BlockCampfire: a ring of logs (wood) or logs in a stone circle (stone), metadata 0 wood, 1 stone, 2 lit wood,
 * 3 lit stone. Coal lights it, a water bucket puts it out. Lit it gives light 15, sets flammable blocks above it on
 * fire (the lit wood campfire also one flammable neighbour) while fire ticks, smokes and crackles, and burns entities
 * standing in it when the game rule {@code projectzulu:does_campfire_burn} (doesCampfireBurn, default false) is on.
 * Only placeable on top of a block. Always drops the plain wood campfire (no damageDropped in the original).
 */
public class CampfireBlock extends Block {
    public enum Kind implements StringRepresentable {
        WOOD, STONE;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 5.6, 16);
    private static final VoxelShape COLLISION = Block.box(0, 0, 0, 16, 4.8, 16);

    public CampfireBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(KIND, Kind.WOOD).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(KIND, LIT);
    }

    /** The block state of 1.5.2 metadata 0-3 (OldBlockStates). */
    public BlockState byMeta(int meta) {
        return this.defaultBlockState().setValue(KIND, (meta & 1) == 0 ? Kind.WOOD : Kind.STONE).setValue(LIT, (meta & 3) > 1);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    /** canPlaceBlockOnSide: only on the top of a block. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return context.getClickedFace() == Direction.UP ? this.defaultBlockState() : null;
    }

    private static boolean flammable(Level level, BlockPos pos, Direction face) {
        return level.getBlockState(pos).isFlammable(level, pos, face);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getGameRules().get(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER) == 0) {
            return; // doFireTick false
        }
        if (state.getValue(LIT) && flammable(level, pos.above(), Direction.UP)) {
            level.setBlockAndUpdate(pos.above(), BaseFireBlock.getState(level, pos.above()));
        }
        if (state.getValue(LIT) && state.getValue(KIND) == Kind.WOOD) {
            // ForgeDirection of the original: NORTH = +z side, SOUTH = -z, WEST = +x, EAST = -x
            BlockPos[] sides = {pos.south(), pos.north(), pos.east(), pos.west()};
            Direction[] faces = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
            for (int i = 0; i < sides.length; i++) {
                if (flammable(level, sides[i], faces[i])) {
                    level.setBlockAndUpdate(sides[i], BaseFireBlock.getState(level, sides[i]));
                    break;
                }
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        boolean lit = state.getValue(LIT);
        // Item.coal of 1.5.2 was coal and charcoal (damage 1)
        if (!lit && (stack.is(Items.COAL) || stack.is(Items.CHARCOAL))) {
            if (!level.isClientSide()) {
                stack.consume(1, player);
                level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
            }
            return InteractionResult.SUCCESS;
        }
        if (lit && stack.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide()) {
                if (!player.hasInfiniteMaterials()) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (state.getValue(LIT) && level instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(ZuluGear.DOES_CAMPFIRE_BURN.get())) {
            entity.igniteForSeconds(1.0F);
        }
        super.entityInside(state, level, pos, entity, effects, intersects);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        if (random.nextInt(24) == 0) {
            level.playLocalSound(x + 0.5, y + 0.5, z + 0.5, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.3F, false);
        }
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && !flammable(level, below, Direction.UP)) {
            if (flammable(level, pos.west(), Direction.EAST)) {
                smoke(level, random, x, y, z, true);
            }
            if (flammable(level, pos.east(), Direction.WEST)) {
                for (int i = 0; i < 2; i++) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x + 1 - random.nextFloat() * 0.1F, y + random.nextFloat(), z + random.nextFloat(), 0, 0, 0);
                }
            }
            if (flammable(level, pos.north(), Direction.SOUTH)) {
                smoke(level, random, x, y, z, false);
            }
            if (flammable(level, pos.south(), Direction.NORTH)) {
                for (int i = 0; i < 2; i++) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextFloat(), y + random.nextFloat(), z + 1 - random.nextFloat() * 0.1F, 0, 0, 0);
                }
            }
            if (flammable(level, pos.above(), Direction.DOWN)) {
                for (int i = 0; i < 2; i++) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextFloat(), y + 1 - random.nextFloat() * 0.1F, z + random.nextFloat(), 0, 0, 0);
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextFloat(), y + random.nextFloat() * 0.5F + 0.5F, z + random.nextFloat(), 0, 0, 0);
            }
        }
    }

    /** Two smoke puffs on the low x (alongX) or low z side of the block. */
    private static void smoke(Level level, RandomSource random, int x, int y, int z, boolean alongX) {
        for (int i = 0; i < 2; i++) {
            float px = alongX ? x + random.nextFloat() * 0.1F : x + random.nextFloat();
            float pz = alongX ? z + random.nextFloat() : z + random.nextFloat() * 0.1F;
            level.addParticle(ParticleTypes.LARGE_SMOKE, px, y + random.nextFloat(), pz, 0, 0, 0);
        }
    }
}
