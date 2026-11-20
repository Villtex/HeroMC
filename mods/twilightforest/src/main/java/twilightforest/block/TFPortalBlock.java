package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import twilightforest.world.TFTeleporter;

/**
 * BlockTFPortal: the 2x2 pool of the Twilight Forest portal, three quarters of a block high, without collision,
 * light 11. A diamond thrown into a pool of water ringed by grass or dirt and plants turns the water into portal
 * ({@link #tryToCreatePortal}, called from the player tick as TFTickHandler did). It falls back to water when its
 * grass or dirt rim is broken.
 */
public final class TFPortalBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    public TFPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /** tryToCreatePortal: lightning and the water turns to portal, if the pool around the diamond is right. */
    public boolean tryToCreatePortal(ServerLevel level, int dx, int dy, int dz) {
        if (!isGoodPortalPool(level, dx, dy, dz)) {
            return false;
        }
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.snapTo(dx, dy, dz);
            level.addFreshEntity(bolt);
        }
        this.transmuteWaterToPortal(level, dx, dy, dz);
        return true;
    }

    private void transmuteWaterToPortal(Level level, int dx, int dy, int dz) {
        int px = dx;
        int pz = dz;
        if (isWater(level, px - 1, dy, pz)) {
            px--;
        }
        if (isWater(level, px, dy, pz - 1)) {
            pz--;
        }
        BlockState portal = this.defaultBlockState();
        level.setBlock(new BlockPos(px, dy, pz), portal, 2);
        level.setBlock(new BlockPos(px + 1, dy, pz), portal, 2);
        level.setBlock(new BlockPos(px + 1, dy, pz + 1), portal, 2);
        level.setBlock(new BlockPos(px, dy, pz + 1), portal, 2);
    }

    public static boolean isGoodPortalPool(Level level, int dx, int dy, int dz) {
        boolean flag = false;
        flag |= isGoodPortalPoolStrict(level, dx, dy, dz);
        flag |= isGoodPortalPoolStrict(level, dx - 1, dy, dz - 1);
        flag |= isGoodPortalPoolStrict(level, dx, dy, dz - 1);
        flag |= isGoodPortalPoolStrict(level, dx + 1, dy, dz - 1);
        flag |= isGoodPortalPoolStrict(level, dx - 1, dy, dz);
        flag |= isGoodPortalPoolStrict(level, dx + 1, dy, dz);
        flag |= isGoodPortalPoolStrict(level, dx - 1, dy, dz + 1);
        flag |= isGoodPortalPoolStrict(level, dx, dy, dz + 1);
        return flag | isGoodPortalPoolStrict(level, dx + 1, dy, dz + 1);
    }

    /** Four water blocks at dx..dx+1, dz..dz+1 on solid ground, a rim of grass or dirt, plants on the rim. */
    private static boolean isGoodPortalPoolStrict(Level level, int dx, int dy, int dz) {
        for (int[] p : new int[][] {{0, 0}, {1, 0}, {1, 1}, {0, 1}}) {
            if (!isWater(level, dx + p[0], dy, dz + p[1]) || !level.getBlockState(new BlockPos(dx + p[0], dy - 1, dz + p[1])).isSolid()) {
                return false;
            }
        }
        for (int[] p : new int[][] {{-1, -1}, {-1, 0}, {-1, 1}, {-1, 2}, {0, -1}, {1, -1}, {0, 2}, {1, 2}, {2, -1}, {2, 0}, {2, 1}, {2, 2}}) {
            if (!isGrassOrDirt(level, dx + p[0], dy, dz + p[1]) || !isNatureBlock(level, dx + p[0], dy + 1, dz + p[1])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWater(BlockGetter level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER);
    }

    /** Material.plants, Material.vine or Material.leaves of 1.5.2. */
    private static boolean isNatureBlock(BlockGetter level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.getBlock() instanceof VegetationBlock || state.getBlock() instanceof LeavesBlock || state.getBlock() instanceof VineBlock;
    }

    /** Material.grass (grass, mycelium) or Material.ground (dirt, farmland) of 1.5.2. */
    static boolean isGrassOrDirt(BlockGetter level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM) || state.is(Blocks.PODZOL) || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.FARMLAND);
    }

    /** onNeighborBlockChange: without grass or dirt beyond the portal on both axes it turns back into water. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, Orientation orientation, boolean movedByPiston) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean good = true;
        if (level.getBlockState(pos.west()).is(this)) {
            good &= isGrassOrDirt(level, x + 1, y, z);
        } else if (level.getBlockState(pos.east()).is(this)) {
            good &= isGrassOrDirt(level, x - 1, y, z);
        } else {
            good = false;
        }
        if (level.getBlockState(pos.north()).is(this)) {
            good &= isGrassOrDirt(level, x, y, z + 1);
        } else if (level.getBlockState(pos.south()).is(this)) {
            good &= isGrassOrDirt(level, x, y, z - 1);
        } else {
            good = false;
        }
        if (!good) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
    }

    /** onEntityCollidedWithBlock: to the Twilight Forest, or from there back to the overworld. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (level instanceof ServerLevel serverLevel && !entity.isPassenger() && !entity.isVehicle() && entity.canUsePortal(false)) {
            if (entity.isOnPortalCooldown()) {
                entity.setPortalCooldown();
                return;
            }
            TFTeleporter.travel(serverLevel, entity);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (random.nextInt(100) == 0) {
            level.playLocalSound(i + 0.5, j + 0.5, k + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F, false);
        }
        for (int l = 0; l < 4; l++) {
            double d = i + random.nextFloat();
            double d1 = j + random.nextFloat();
            double d2 = k + random.nextFloat();
            int i1 = random.nextInt(2) * 2 - 1;
            double d3 = (random.nextFloat() - 0.5) * 0.5;
            double d4 = (random.nextFloat() - 0.5) * 0.5;
            double d5 = (random.nextFloat() - 0.5) * 0.5;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                d = i + 0.5 + 0.25 * i1;
                d3 = random.nextFloat() * 2.0F * i1;
            } else {
                d2 = k + 0.5 + 0.25 * i1;
                d5 = random.nextFloat() * 2.0F * i1;
            }
            level.addParticle(ParticleTypes.PORTAL, d, d1, d2, d3, d4, d5);
        }
    }
}
