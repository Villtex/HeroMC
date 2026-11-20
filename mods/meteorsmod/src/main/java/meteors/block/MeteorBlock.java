package meteors.block;

import meteors.MeteorsMod;
import meteors.entity.AlienCreeper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockMeteor (the fallen meteor and, as BlockRareFallenMeteor, the rare one): heat 1..4 after a crash (metadata),
 * cooling down by one per random tick, at once next to water. While hot it glows, sets entities in it on fire
 * (isBlockBurning; the collision box is 0.006 smaller, so whoever stands on it is inside) and keeps fire on top
 * burning. Mined by a player, 1 of 100 releases an alien creeper. Cold it needs a stone pickaxe, hot an iron one.
 */
public class MeteorBlock extends Block {
    public static final IntegerProperty HEAT = IntegerProperty.create("heat", 0, 4);
    private static final VoxelShape SHAPE = Block.box(0.096, 0.096, 0.096, 15.904, 15.904, 15.904);

    public MeteorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HEAT, 0));
    }

    /** Hardness 10, resistance 200, stone step sound, light 0.5 while hot, random ticks. */
    public static Properties properties(Properties p, int light) {
        return p.strength(10.0F, 120.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().randomTicks()
                .lightLevel(s -> s.getValue(HEAT) > 0 ? light : 0);
    }

    /** The state with that heat if the block has one (the crash blocks), else the state itself. */
    public static BlockState hot(BlockState state, int heat) {
        return state.hasProperty(HEAT) ? state.setValue(HEAT, heat) : state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HEAT);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int heat = state.getValue(HEAT);
        if (heat > 0) {
            heat--;
            level.setBlock(pos, state.setValue(HEAT, heat), 2);
        }
        if (heat > 0 && isWaterAround(level, pos)) {
            level.setBlock(pos, state.setValue(HEAT, 0), 2);
        }
    }

    private static boolean isWaterAround(Level level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(p).is(Blocks.WATER)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        // Entity.isBurning: dealFireDamage(1) and 8 seconds of fire for whoever is not wet
        if (state.getValue(HEAT) > 0 && !entity.isInWaterOrRain() && !entity.fireImmune() && level instanceof ServerLevel server) {
            entity.hurtServer(server, level.damageSources().inFire(), 1.0F);
            if (entity.getRemainingFireTicks() <= 0) {
                entity.igniteForSeconds(8.0F);
            }
        }
    }

    @Override
    public boolean isBurning(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(HEAT) != 0;
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return direction == Direction.UP && state.getValue(HEAT) > 0 || super.isFireSource(state, level, pos, direction);
    }

    /** setBlockHarvestLevel(block, "pickaxe", 2) with (block, 0, "pickaxe", 1) for the cold one. */
    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        if (state.getValue(HEAT) > 0) {
            return player.hasCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState());
        }
        return super.canHarvestBlock(state, level, pos, player);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof ServerLevel server && server.getRandom().nextInt(100) == 95) {
            this.releaseMob(server, pos);
        }
    }

    /** onBlockDestroyedByPlayer: an alien creeper at the block corner with explosion particles. */
    protected void releaseMob(ServerLevel level, BlockPos pos) {
        AlienCreeper creeper = MeteorsMod.ALIEN_CREEPER.get().create(level, EntitySpawnReason.EVENT);
        if (creeper != null) {
            creeper.snapTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
            level.addFreshEntity(creeper);
            creeper.spawnAnim();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(HEAT) > 0) {
            glowParticles(level, pos, random, MeteorsMod.METEOR_DUST.get());
        }
    }

    /** renderGlowParticles: dust on the uncovered faces. */
    public static void glowParticles(Level level, BlockPos pos, RandomSource random, net.minecraft.core.particles.ParticleOptions particle) {
        double d = 0.0625;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        for (int l = 0; l < 6; l++) {
            double d1 = i + random.nextFloat();
            double d2 = j + random.nextFloat();
            double d3 = k + random.nextFloat();
            if (l == 0 && !level.getBlockState(pos.above()).isSolidRender()) {
                d2 = j + 1 + d;
            }
            if (l == 1 && !level.getBlockState(pos.below()).isSolidRender()) {
                d2 = j - d;
            }
            if (l == 2 && !level.getBlockState(pos.south()).isSolidRender()) {
                d3 = k + 1 + d;
            }
            if (l == 3 && !level.getBlockState(pos.north()).isSolidRender()) {
                d3 = k - d;
            }
            if (l == 4 && !level.getBlockState(pos.east()).isSolidRender()) {
                d1 = i + 1 + d;
            }
            if (l == 5 && !level.getBlockState(pos.west()).isSolidRender()) {
                d1 = i - d;
            }
            if (d1 < i || d1 > i + 1 || d2 < 0.0 || d2 > j + 1 || d3 < k || d3 > k + 1) {
                level.addParticle(particle, d1, d2, d3, 0.0, 0.0, 0.0);
            }
        }
    }
}
