package qcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import qcraft.QuantumCraft;

/**
 * BlockQuantumOre "Quantum Ore" (rock, hardness 3, resistance 5, any pickaxe). Like redstone ore it starts to glow when
 * hit, walked on or right-clicked and turns back on a random tick of the glowing block (light 0.625 = 9), sparkling with
 * green dust. Drops 1-2 quantum dust plus 0..fortune and 1-5 experience; silk touch gives the plain ore.
 */
public class QuantumOreBlock extends DropExperienceBlock {
    private final boolean glowing;

    public QuantumOreBlock(boolean glowing, Properties properties) {
        super(UniformInt.of(1, 5), properties);
        this.glowing = glowing;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        this.glow(level, pos);
        super.attack(state, level, pos, player);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        this.glow(level, pos);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // onBlockActivated glowed and returned false: the click goes on to the item
        this.glow(level, pos);
        return InteractionResult.PASS;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return this.glowing;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.glowing) {
            level.setBlock(pos, QuantumCraft.QUANTUM_ORE.get().defaultBlockState(), 3);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.glowing) {
            sparkle(level, pos);
        }
    }

    private void glow(Level level, BlockPos pos) {
        sparkle(level, pos);
        if (!this.glowing && !level.isClientSide()) {
            level.setBlock(pos, QuantumCraft.QUANTUM_ORE_GLOWING.get().defaultBlockState(), 3);
        }
    }

    /** sparkle: six dust particles just outside the faces that are not covered (with the original's bottom slip). */
    private static void sparkle(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            return;
        }
        RandomSource random = level.getRandom();
        double d0 = 0.0625;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int l = 0; l < 6; l++) {
            double d1 = x + random.nextFloat();
            double d2 = y + random.nextFloat();
            double d3 = z + random.nextFloat();
            if (l == 0 && !level.getBlockState(pos.above()).isSolidRender()) {
                d2 = y + 1 + d0;
            }
            if (l == 1 && !level.getBlockState(pos.below()).isSolidRender()) {
                d2 = y - d0;
            }
            if (l == 2 && !level.getBlockState(pos.south()).isSolidRender()) {
                d3 = z + 1 + d0;
            }
            if (l == 3 && !level.getBlockState(pos.north()).isSolidRender()) {
                d3 = z - d0;
            }
            if (l == 4 && !level.getBlockState(pos.east()).isSolidRender()) {
                d1 = x + 1 + d0;
            }
            if (l == 5 && !level.getBlockState(pos.west()).isSolidRender()) {
                d1 = x - d0;
            }
            if (d1 < x || d1 > x + 1 || d2 < 0.0 || d2 > y + 1 || d3 < z || d3 > z + 1) {
                spawnQuantumDust(level, d1, d2, d3);
            }
        }
    }

    /** EntityQuantumDustFX: a redstone dust particle in green (0.48..1.0), only within 16 blocks of the camera. */
    public static void spawnQuantumDust(Level level, double x, double y, double z) {
        RandomSource random = level.getRandom();
        float f4 = random.nextFloat() * 0.4F + 0.6F;
        float green = (random.nextFloat() * 0.2F + 0.8F) * f4;
        int g = Math.min(255, (int) (green * 255.0F));
        level.addParticle(new DustParticleOptions(g << 8, 1.0F), x, y, z, 0.0, 0.0, 0.0);
    }
}
