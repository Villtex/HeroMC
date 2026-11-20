package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Metadata 0 of BlockTFHedge: Material.cactus, hardness 2, resistance 10, drops nothing. Its collision box is a
 * sixteenth lower than the block, so walking on it or brushing against it pricks like cactus (3 damage), as does
 * breaking it. Spiders, items and bats are spared. Hitting it schedules a tick in 10 ticks (onBlockClicked); the
 * tick pricks every player within 4 blocks who is swinging and looking at a hedge, and checks again 10 ticks later
 * (updateTick).
 */
public final class HedgeBlock extends Block {
    private static final VoxelShape COLLISION = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    private static final float DAMAGE = 3.0F;

    public HedgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        prick(level, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        prick(level, entity);
    }

    @Override
    public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        player.hurtServer(level, level.damageSources().cactus(), DAMAGE);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 10);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double range = 4.0;
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(range))) {
            if (player.isSwinging()) {
                Vec3 eye = player.getEyePosition();
                Vec3 dest = eye.add(player.getViewVector(1.0F).scale(range));
                BlockHitResult hit = level.clip(new ClipContext(eye, dest, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                if (hit.getType() == HitResult.Type.BLOCK && level.getBlockState(hit.getBlockPos()).is(this)) {
                    player.hurtServer(level, level.damageSources().cactus(), DAMAGE);
                    level.scheduleTick(pos, this, 10);
                }
            }
        }
    }

    private static void prick(Level level, Entity entity) {
        if (level instanceof ServerLevel serverLevel && !(entity instanceof Spider) && !(entity instanceof ItemEntity) && !(entity instanceof Bat)) {
            entity.hurtServer(serverLevel, level.damageSources().cactus(), DAMAGE);
        }
    }
}
