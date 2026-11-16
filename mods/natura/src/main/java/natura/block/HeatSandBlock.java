package natura.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * HeatSand: falling nether sand, 2/16 lower than a block like soul sand. Burns players without boots and every
 * living thing that is not fire-proof (1 point of fire damage).
 */
public class HeatSandBlock extends ColoredFallingBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

    public HeatSandBlock(Properties properties) {
        super(new ColorRGBA(0xFFB0602C), properties);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (entity instanceof Player player) {
            if (player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
                entity.hurtServer(server, level.damageSources().inFire(), 1.0F);
            }
        } else if (entity instanceof LivingEntity && !entity.fireImmune()) {
            entity.hurtServer(server, level.damageSources().inFire(), 1.0F);
        }
    }
}
